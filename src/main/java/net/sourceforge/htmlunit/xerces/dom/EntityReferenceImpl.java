/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sourceforge.htmlunit.xerces.dom;

import org.w3c.dom.DocumentType;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import net.sourceforge.htmlunit.xerces.util.URI;

/**
 * EntityReference models the XML &amp;entityname; syntax, when used for
 * entities defined by the DOM. Entities hardcoded into XML, such as
 * character entities, should instead have been translated into text
 * by the code which generated the DOM tree.
 * <P>
 * An XML processor has the alternative of fully expanding Entities
 * into the normal document tree. If it does so, no EntityReference nodes
 * will appear.
 * <P>
 * Similarly, non-validating XML processors are not required to read
 * or process entity declarations made in the external subset or
 * declared in external parameter entities. Hence, some applications
 * may not make the replacement value available for Parsed Entities
 * of these types.
 * <P>
 * EntityReference behaves as a read-only node, and the children of
 * the EntityReference (which reflect those of the Entity, and should
 * also be read-only) give its replacement value, if any. They are
 * supposed to automagically stay in synch if the DocumentType is
 * updated with new values for the Entity.
 * <P>
 * The defined behavior makes efficient storage difficult for the DOM
 * implementor. We can't just look aside to the Entity's definition
 * in the DocumentType since those nodes have the wrong parent (unless
 * we can come up with a clever "imaginary parent" mechanism). We
 * must at least appear to clone those children... which raises the
 * issue of keeping the reference synchronized with its parent.
 * This leads me back to the "cached image of centrally defined data"
 * solution, much as I dislike it.
 * <P>
 * For now I have decided, since REC-DOM-Level-1-19980818 doesn't
 * cover this in much detail, that synchronization doesn't have to be
 * considered while the user is deep in the tree. That is, if you're
 * looking within one of the EntityReferennce's children and the Entity
 * changes, you won't be informed; instead, you will continue to access
 * the same object -- which may or may not still be part of the tree.
 * This is the same behavior that obtains elsewhere in the DOM if the
 * subtree you're looking at is deleted from its parent, so it's
 * acceptable here. (If it really bothers folks, we could set things
 * up so deleted subtrees are walked and marked invalid, but that's
 * not part of the DOM's defined behavior.)
 * <P>
 * As a result, only the EntityReference itself has to be aware of
 * changes in the Entity. And it can take advantage of the same
 * structure-change-monitoring code I implemented to support
 * DeepNodeList.
 * <p>
 *
 * @author Arnaud  Le Hors, IBM
 * @author Joe Kesselman, IBM
 * @author Andy Clark, IBM
 * @author Ralf Pfeiffer, IBM
 */
public class EntityReferenceImpl
extends ParentNode
implements EntityReference {

    /** Name of Entity referenced */
    protected final String name;
    /** Base URI*/
    protected String baseURI;

    // Factory constructor.
    public EntityReferenceImpl(CoreDocumentImpl ownerDoc, String name) {
        super(ownerDoc);
        this.name = name;
        needsSyncChildren(true);
    }

    /**
     * {@inheritDoc}
     *
     * A short integer indicating what type of node this is. The named
     * constants for this value are defined in the org.w3c.dom.Node interface.
     */
    @Override
    public short getNodeType() {
        return Node.ENTITY_REFERENCE_NODE;
    }

    /**
     * {@inheritDoc}
     *
     * Returns the name of the entity referenced
     */
    @Override
    public String getNodeName() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return name;
    }

    /**
     * {@inheritDoc}
     *
     * Clone node.
     */
    @Override
    public Node cloneNode(boolean deep) {
        EntityReferenceImpl er = (EntityReferenceImpl)super.cloneNode(deep);
        return er;
    }

    /**
     * {@inheritDoc}
     *
     * Returns the absolute base URI of this node or null if the implementation
     * wasn't able to obtain an absolute URI. Note: If the URI is malformed, a
     * null is returned.
     *
     * @return The absolute base URI of this node or null.
     */
    @Override
    public String getBaseURI() {
        if (needsSyncData()) {
            synchronizeData();
        }
        if (baseURI == null) {
            DocumentType doctype;
            NamedNodeMap entities;
            EntityImpl entDef;
            if (null != (doctype = getOwnerDocument().getDoctype()) &&
                null != (entities = doctype.getEntities())) {

                entDef = (EntityImpl)entities.getNamedItem(getNodeName());
                if (entDef !=null) {
                    return entDef.getBaseURI();
                }
            }
        } else if (baseURI != null && baseURI.length() != 0 ) {// attribute value is always empty string
            try {
                return new URI(baseURI).toString();
            }
            catch (net.sourceforge.htmlunit.xerces.util.URI.MalformedURIException e){
                // REVISIT: what should happen in this case?
                return null;
            }
        }
        return baseURI;
    }


    // NON-DOM: set base uri
    public void setBaseURI(String uri){
        if (needsSyncData()) {
            synchronizeData();
        }
        baseURI = uri;
    }

    /**
     * NON-DOM: compute string representation of the entity reference.
     * This method is used to retrieve a string value for an attribute node that has child nodes.
     * @return String representing a value of this entity ref. or
     *          null if any node other than EntityReference, Text is encountered
     *          during computation
     */
    protected String getEntityRefValue (){
        if (needsSyncChildren()){
            synchronizeChildren();
        }

        String value;
        if (firstChild != null){
          if (firstChild.getNodeType() == Node.ENTITY_REFERENCE_NODE){
              value = ((EntityReferenceImpl)firstChild).getEntityRefValue();
          }
          else if (firstChild.getNodeType() == Node.TEXT_NODE){
            value = firstChild.getNodeValue();
          }
          else {
             // invalid to have other types of nodes in attr value
            return null;
          }

          if (firstChild.nextSibling == null){
            return value;
          }
          else {
            StringBuilder buff = new StringBuilder(value);
            ChildNode next = firstChild.nextSibling;
            while (next != null){

                if (next.getNodeType() == Node.ENTITY_REFERENCE_NODE){
                   value = ((EntityReferenceImpl)next).getEntityRefValue();
                }
                else if (next.getNodeType() == Node.TEXT_NODE){
                  value = next.getNodeValue();
                }
                else {
                    // invalid to have other types of nodes in attr value
                    return null;
                }
                buff.append(value);
                next = next.nextSibling;

            }
            return buff.toString();
          }
        }
        return "";
    }

    /**
     * {@inheritDoc}
     *
     * EntityReference's children are a reflection of those defined in the
     * named Entity. This method creates them if they haven't been created yet.
     * This doesn't support editing the Entity though, since this only called
     * once for all.
     */
    @Override
    protected void synchronizeChildren() {
        // no need to synchronize again
        needsSyncChildren(false);

        DocumentType doctype;
        NamedNodeMap entities;
        EntityImpl entDef;
        if (null != (doctype = getOwnerDocument().getDoctype()) &&
            null != (entities = doctype.getEntities())) {

            entDef = (EntityImpl)entities.getNamedItem(getNodeName());

            // No Entity by this name, stop here.
            if (entDef == null)
                return;

            // If entity's definition exists, clone its kids
            for (Node defkid = entDef.getFirstChild();
                defkid != null;
                defkid = defkid.getNextSibling()) {
                Node newkid = defkid.cloneNode(true);
                insertBefore(newkid, null);
            }
        }
    }
}
