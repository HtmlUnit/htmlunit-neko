/*
 * Copyright (c) 2017-2025 Ronald Brill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.htmlunit.cyberneko.xerces.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;

/**
 * This class represents a Document Type <em>declaraction</em> in the document
 * itself, <em>not</em> a Document Type Definition (DTD). An XML document may
 * (or may not) have such a reference.
 * <P>
 * DocumentType is an Extended DOM feature, used in XML documents but not in
 * HTML.
 * <P>
 * Note that Entities and Notations are no longer children of the DocumentType,
 * but are parentless nodes hung only in their appropriate NamedNodeMaps.
 * <P>
 * This area is UNDERSPECIFIED IN REC-DOM-Level-1-19981001 Most notably,
 * absolutely no provision was made for storing and using Element and Attribute
 * information. Nor was the linkage between Entities and Entity References
 * nailed down solidly.
 * <p>
 *
 * @author Arnaud Le Hors, IBM
 * @author Joe Kesselman, IBM
 * @author Andy Clark, IBM
 */
public class DocumentTypeImpl extends ParentNode implements DocumentType {

    /** Document type name. */
    private final String name_;

    /** Entities. */
    private NamedNodeMapImpl entities_;

    /** Notations. */
    private NamedNodeMapImpl notations_;

    /** Elements. */
    private NamedNodeMapImpl elements_;

    // DOM2: support public ID.
    private String publicID_;

    // DOM2: support system ID.
    private String systemID_;

    // DOM2: support internal subset.
    private String internalSubset_;

    // The following are required for compareDocumentPosition
    // Doctype number. Doc types which have no owner may be assigned
    // a number, on demand, for ordering purposes for compareDocumentPosition
    private int doctypeNumber_ = 0;

    /**
     * Factory method for creating a document type node.
     *
     * @param ownerDocument the owner document
     * @param name          the name
     */
    public DocumentTypeImpl(final CoreDocumentImpl ownerDocument, final String name) {
        super(ownerDocument);

        name_ = name;
        // DOM
        entities_ = new NamedNodeMapImpl(this);
        notations_ = new NamedNodeMapImpl(this);

        // NON-DOM
        elements_ = new NamedNodeMapImpl(this);

    }

    // Factory method for creating a document type node.
    public DocumentTypeImpl(final CoreDocumentImpl ownerDocument, final String qualifiedName,
                                final String publicID, final String systemID) {
        this(ownerDocument, qualifiedName);
        publicID_ = publicID;
        systemID_ = systemID;

    }

    /**
     * {@inheritDoc}
     *
     * Introduced in DOM Level 2.
     * <p>
     *
     * Return the public identifier of this Document type.
     */
    @Override
    public String getPublicId() {
        return publicID_;
    }

    /**
     * {@inheritDoc}
     *
     * Introduced in DOM Level 2.
     * <p>
     *
     * Return the system identifier of this Document type.
     */
    @Override
    public String getSystemId() {
        return systemID_;
    }

    // NON-DOM
    // Set the internalSubset given as a string.
    public void setInternalSubset(final String internalSubset) {
        internalSubset_ = internalSubset;
    }

    /**
     * {@inheritDoc}
     *
     * Introduced in DOM Level 2.
     * <p>
     *
     * Return the internalSubset given as a string.
     */
    @Override
    public String getInternalSubset() {
        return internalSubset_;
    }

    /**
     * {@inheritDoc}
     *
     * A short integer indicating what type of node this is. The named constants for
     * this value are defined in the org.w3c.dom.Node interface.
     */
    @Override
    public short getNodeType() {
        return Node.DOCUMENT_TYPE_NODE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNodeName() {
        return name_;
    }

    /**
     * {@inheritDoc}
     *
     * Clones the node.
     */
    @Override
    public Node cloneNode(final boolean deep) {

        final DocumentTypeImpl newnode = (DocumentTypeImpl) super.cloneNode(deep);
        // NamedNodeMaps must be cloned explicitly, to avoid sharing them.
        newnode.entities_ = entities_.cloneMap(newnode);
        newnode.notations_ = notations_.cloneMap(newnode);
        newnode.elements_ = elements_.cloneMap(newnode);

        return newnode;

    }

    /**
     * {@inheritDoc}
     *
     * Get Node text content
     */
    @Override
    public String getTextContent() throws DOMException {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Set Node text content
     */
    @Override
    public void setTextContent(final String textContent) throws DOMException {
        // no-op
    }

    /**
     * {@inheritDoc}
     *
     * DOM Level 3 WD- Experimental. Override inherited behavior from ParentNodeImpl
     * to support deep equal.
     */
    @Override
    public boolean isEqualNode(final Node arg) {

        if (!super.isEqualNode(arg)) {
            return false;
        }
        final DocumentTypeImpl argDocType = (DocumentTypeImpl) arg;

        // test if the following string attributes are equal: publicId,
        // systemId, internalSubset.
        if ((getPublicId() == null && argDocType.getPublicId() != null)
                || (getPublicId() != null && argDocType.getPublicId() == null)
                || (getSystemId() == null && argDocType.getSystemId() != null)
                || (getSystemId() != null && argDocType.getSystemId() == null)
                || (getInternalSubset() == null && argDocType.getInternalSubset() != null)
                || (getInternalSubset() != null && argDocType.getInternalSubset() == null)) {
            return false;
        }

        if (getPublicId() != null) {
            if (!getPublicId().equals(argDocType.getPublicId())) {
                return false;
            }
        }

        if (getSystemId() != null) {
            if (!getSystemId().equals(argDocType.getSystemId())) {
                return false;
            }
        }

        if (getInternalSubset() != null) {
            if (!getInternalSubset().equals(argDocType.getInternalSubset())) {
                return false;
            }
        }

        // test if NamedNodeMaps entities and notations are equal
        final NamedNodeMapImpl argEntities = argDocType.entities_;

        if ((entities_ == null && argEntities != null) || (entities_ != null && argEntities == null)) {
            return false;
        }

        if (entities_ != null && argEntities != null) {
            if (entities_.getLength() != argEntities.getLength()) {
                return false;
            }

            for (int index = 0; entities_.item(index) != null; index++) {
                final Node entNode1 = entities_.item(index);
                final Node entNode2 = argEntities.getNamedItem(entNode1.getNodeName());

                if (!entNode1.isEqualNode(entNode2)) {
                    return false;
                }
            }
        }

        final NamedNodeMapImpl argNotations = argDocType.notations_;

        if ((notations_ == null && argNotations != null) || (notations_ != null && argNotations == null)) {
            return false;
        }

        if (notations_ != null && argNotations != null) {
            if (notations_.getLength() != argNotations.getLength()) {
                return false;
            }

            for (int index = 0; notations_.item(index) != null; index++) {
                final Node noteNode1 = notations_.item(index);
                final Node noteNode2 = argNotations.getNamedItem(noteNode1.getNodeName());

                if (!noteNode1.isEqualNode(noteNode2)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     *
     * NON-DOM set the ownerDocument of this node and its children
     */
    @Override
    protected void setOwnerDocument(final CoreDocumentImpl doc) {
        super.setOwnerDocument(doc);
        entities_.setOwnerDocument(doc);
        notations_.setOwnerDocument(doc);
        elements_.setOwnerDocument(doc);
    }

    /**
     * {@inheritDoc}
     *
     * NON-DOM Get the number associated with this doctype.
     */
    @Override
    protected int getNodeNumber() {
        // If the doctype has a document owner, get the node number
        // relative to the owner doc
        if (getOwnerDocument() != null) {
            return super.getNodeNumber();
        }

        // The doctype is disconnected and not associated with any document.
        // Assign the doctype a number relative to the implementation.
        if (doctypeNumber_ == 0) {

            final DOMImplementationImpl cd = (DOMImplementationImpl) DOMImplementationImpl.getDOMImplementation();
            doctypeNumber_ = cd.assignDocTypeNumber();
        }
        return doctypeNumber_;
    }

    /**
     * {@inheritDoc}
     *
     * Name of this document type. If we loaded from a DTD, this should be the name
     * immediately following the DOCTYPE keyword.
     */
    @Override
    public String getName() {
        return name_;

    }

    /**
     * {@inheritDoc}
     *
     * Access the collection of general Entities, both external and internal,
     * defined in the DTD. For example, in:
     *
     * <pre>
     *   &lt;!doctype example SYSTEM "ex.dtd" [
     *     &lt;!ENTITY foo "foo"&gt;
     *     &lt;!ENTITY bar "bar"&gt;
     *     &lt;!ENTITY % baz "baz"&gt;
     *     ]&gt;
     * </pre>
     * <p>
     * The Entities map includes foo and bar, but not baz. It is promised that only
     * Nodes which are Entities will exist in this NamedNodeMap.
     * <p>
     * For HTML, this will always be null.
     * <p>
     * Note that "built in" entities such as &amp; and &lt; should be converted to
     * their actual characters before being placed in the DOM's contained text, and
     * should be converted back when the DOM is rendered as XML or HTML, and hence
     * DO NOT appear here.
     */
    @Override
    public NamedNodeMap getEntities() {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        return entities_;
    }

    /**
     * {@inheritDoc}
     *
     * Access the collection of Notations defined in the DTD. A notation declares,
     * by name, the format of an XML unparsed entity or is used to formally declare
     * a Processing Instruction target.
     */
    @Override
    public NamedNodeMap getNotations() {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        return notations_;
    }

    // NON-DOM: Access the collection of ElementDefinitions.
    // @see ElementDefinitionImpl
    public NamedNodeMap getElements() {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        return elements_;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object setUserData(final String key, final Object data, final UserDataHandler handler) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getUserData(final String key) {
        return null;
    }
}
