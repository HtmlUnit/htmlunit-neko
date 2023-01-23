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

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

import net.sourceforge.htmlunit.xerces.util.URI;

/**
 * Elements represent most of the "markup" and structure of the
 * document.  They contain both the data for the element itself
 * (element name and attributes), and any contained nodes, including
 * document text (as children).
 * <P>
 * Elements may have Attributes associated with them; the API for this is
 * defined in Node, but the function is implemented here. In general, XML
 * applications should retrive Attributes as Nodes, since they may contain
 * entity references and hence be a fairly complex sub-tree. HTML users will
 * be dealing with simple string values, and convenience methods are provided
 * to work in terms of Strings.
 * <P>
 * ElementImpl does not support Namespaces. ElementNSImpl, which inherits from
 * it, does.
 * <p>
 *
 * @see ElementNSImpl
 *
 * @author Arnaud  Le Hors, IBM
 * @author Joe Kesselman, IBM
 * @author Andy Clark, IBM
 * @author Ralf Pfeiffer, IBM
 * @author Michael Glavassevich, IBM
 */
public class ElementImpl
    extends ParentNode
    implements Element, TypeInfo {

    /** Element name. */
    protected String name;

    /** Attributes. */
    protected AttributeMap attributes;

    // Factory constructor.
    public ElementImpl(CoreDocumentImpl ownerDoc, String name) {
        super(ownerDoc);
        this.name = name;
        needsSyncData(true);    // synchronizeData will initialize attributes
    }

    // Support for DOM Level 3 renameNode method.
    // Note: This only deals with part of the pb. CoreDocumentImpl
    // does all the work.
    void rename(String name) {
        if (needsSyncData()) {
            synchronizeData();
        }
        if (ownerDocument.errorChecking) {
            int colon1 = name.indexOf(':');
            if(colon1 != -1){
                String msg =
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "NAMESPACE_ERR",
                            null);
                throw new DOMException(DOMException.NAMESPACE_ERR, msg);
            }
            if (!CoreDocumentImpl.isXMLName(name, ownerDocument.isXML11Version())) {
                String msg = DOMMessageFormatter.formatMessage(
                        DOMMessageFormatter.DOM_DOMAIN,
                        "INVALID_CHARACTER_ERR", null);
                throw new DOMException(DOMException.INVALID_CHARACTER_ERR,
                        msg);
            }
        }
        this.name = name;
        reconcileDefaultAttributes();
    }

    /**
     * {@inheritDoc}
     *
     * A short integer indicating what type of node this is. The named
     * constants for this value are defined in the org.w3c.dom.Node interface.
     */
    @Override
    public short getNodeType() {
        return Node.ELEMENT_NODE;
    }

    /**
     * {@inheritDoc}
     *
     * Returns the element name
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
     * Retrieve all the Attributes as a set. Note that this API is inherited
     * from Node rather than specified on Element; in fact only Elements will
     * ever have Attributes, but they want to allow folks to "blindly" operate
     * on the tree as a set of Nodes.
     */
    @Override
    public NamedNodeMap getAttributes() {

        if (needsSyncData()) {
            synchronizeData();
        }
        if (attributes == null) {
            attributes = new AttributeMap(this, null);
        }
        return attributes;

    }

    /**
     * {@inheritDoc}
     *
     * Return a duplicate copy of this Element. Note that its children
     * will not be copied unless the "deep" flag is true, but Attributes
     * are <i>always</i> replicated.
     *
     * @see org.w3c.dom.Node#cloneNode(boolean)
     */
    @Override
    public Node cloneNode(boolean deep) {

        ElementImpl newnode = (ElementImpl) super.cloneNode(deep);
        // Replicate NamedNodeMap rather than sharing it.
        if (attributes != null) {
            newnode.attributes = (AttributeMap) attributes.cloneMap(newnode);
        }
        return newnode;

    }

    /**
     * {@inheritDoc}
     *
     * DOM Level 3 WD - Experimental.
     * Retrieve baseURI
     */
    @Override
    public String getBaseURI() {

        if (needsSyncData()) {
            synchronizeData();
        }
        // Absolute base URI is computed according to
        // XML Base (http://www.w3.org/TR/xmlbase/#granularity)
        // 1. The base URI specified by an xml:base attribute on the element,
        // if one exists
        if (attributes != null) {
            final Attr attrNode = getXMLBaseAttribute();
            if (attrNode != null) {
                final String uri =  attrNode.getNodeValue();
                if (uri.length() != 0) {// attribute value is always empty string
                    try {
                        URI _uri = new URI(uri, true);
                        // If the URI is already absolute return it; otherwise it's relative and we need to resolve it.
                        if (_uri.isAbsoluteURI()) {
                            return _uri.toString();
                        }

                        // Make any parentURI into a URI object to use with the URI(URI, String) constructor
                        String parentBaseURI = (this.ownerNode != null) ? this.ownerNode.getBaseURI() : null;
                        if (parentBaseURI != null) {
                            try {
                                URI _parentBaseURI = new URI(parentBaseURI);
                                _uri.absolutize(_parentBaseURI);
                                return _uri.toString();
                            }
                            catch (net.sourceforge.htmlunit.xerces.util.URI.MalformedURIException ex) {
                                // This should never happen: parent should have checked the URI and returned null if invalid.
                                return null;
                            }
                        }
                        // REVISIT: what should happen in this case?
                        return null;
                    }
                    catch (net.sourceforge.htmlunit.xerces.util.URI.MalformedURIException ex) {
                        return null;
                    }
                }
            }
        }

        // 2.the base URI of the element's parent element within the
        // document or external entity, if one exists
        // 3. the base URI of the document entity or external entity
        // containing the element

        // ownerNode serves as a parent or as document
        return (this.ownerNode != null) ? this.ownerNode.getBaseURI() : null;
    }

    // NON-DOM Returns the xml:base attribute.
    protected Attr getXMLBaseAttribute() {
        return (Attr) attributes.getNamedItem("xml:base");
    } // getXMLBaseAttribute():Attr

    // NON-DOM set the ownerDocument of this node, its children, and its attributes
    @Override
    protected void setOwnerDocument(CoreDocumentImpl doc) {
        super.setOwnerDocument(doc);
        if (attributes != null) {
            attributes.setOwnerDocument(doc);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Look up a single Attribute by name. Returns the Attribute's
     * string value, or an empty string (NOT null!) to indicate that the
     * name did not map to a currently defined attribute.
     * <p>
     * Note: Attributes may contain complex node trees. This method
     * returns the "flattened" string obtained from Attribute.getValue().
     * If you need the structure information, see getAttributeNode().
     */
    @Override
    public String getAttribute(String name) {

        if (needsSyncData()) {
            synchronizeData();
        }
        if (attributes == null) {
            return "";
        }
        Attr attr = (Attr)(attributes.getNamedItem(name));
        return (attr == null) ? "" : attr.getValue();

    }

    /**
     * {@inheritDoc}
     *
     * Look up a single Attribute by name. Returns the Attribute Node,
     * so its complete child tree is available. This could be important in
     * XML, where the string rendering may not be sufficient information.
     * <p>
     * If no matching attribute is available, returns null.
     */
    @Override
    public Attr getAttributeNode(String name) {

        if (needsSyncData()) {
            synchronizeData();
        }
        if (attributes == null) {
            return null;
        }
        return (Attr)attributes.getNamedItem(name);

    }


    /**
     * {@inheritDoc}
     *
     * Returns a NodeList of all descendent nodes (children,
     * grandchildren, and so on) which are Elements and which have the
     * specified tag name.
     * <p>
     * Note: NodeList is a "live" view of the DOM. Its contents will
     * change as the DOM changes, and alterations made to the NodeList
     * will be reflected in the DOM.
     *
     * @param tagname The type of element to gather. To obtain a list of
     * all elements no matter what their names, use the wild-card tag
     * name "*".
     *
     * @see DeepNodeListImpl
     */
    @Override
    public NodeList getElementsByTagName(String tagname) {
        return new DeepNodeListImpl(this,tagname);
    }

    /**
     * {@inheritDoc}
     *
     * Returns the name of the Element. Note that Element.nodeName() is
     * defined to also return the tag name.
     * <p>
     * This is case-preserving in XML. HTML should uppercasify it on the
     * way in.
     */
    @Override
    public String getTagName() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return name;
    }

    /**
     * {@inheritDoc}
     *
     * Remove the named attribute from this Element. If the removed
     * Attribute has a default value, it is immediately replaced thereby.
     * <P>
     * The default logic is actually implemented in NamedNodeMapImpl.
     * PR-DOM-Level-1-19980818 doesn't fully address the DTD, so some
     * of this behavior is likely to change in future versions. ?????
     * <P>
     * Note that this call "succeeds" even if no attribute by this name
     * existed -- unlike removeAttributeNode, which will throw a not-found
     * exception in that case.
     */
    @Override
    public void removeAttribute(String name) {
        if (needsSyncData()) {
            synchronizeData();
        }

        if (attributes == null) {
            return;
        }

        attributes.internalRemoveNamedItem(name, false);

    } // removeAttribute(String)


    /**
     * {@inheritDoc}
     *
     * Remove the specified attribute/value pair. If the removed
     * Attribute has a default value, it is immediately replaced.
     * <p>
     * NOTE: Specifically removes THIS NODE -- not the node with this
     * name, nor the node with these contents. If the specific Attribute
     * object passed in is not stored in this Element, we throw a
     * DOMException.  If you really want to remove an attribute by name,
     * use removeAttribute().
     *
     * @return the Attribute object that was removed.
     * @throws DOMException NOT_FOUND_ERR if oldattr is not an attribute of
     * this Element.
     */
    @Override
    public Attr removeAttributeNode(Attr oldAttr)
        throws DOMException {

        if (needsSyncData()) {
            synchronizeData();
        }

        if (attributes == null) {
            String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null);
            throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
        }
        return (Attr) attributes.removeItem(oldAttr);

    }


    /**
     * {@inheritDoc}
     *
     * Add a new name/value pair, or replace the value of the existing
     * attribute having that name.
     * <p>
     * Note: this method supports only the simplest kind of Attribute,
     * one whose value is a string contained in a single Text node.
     * If you want to assert a more complex value (which XML permits,
     * though HTML doesn't), see setAttributeNode().
     * <p>
     * The attribute is created with specified=true, meaning it's an
     * explicit value rather than inherited from the DTD as a default.
     * Again, setAttributeNode can be used to achieve other results.
     *
     * @throws DOMException INVALID_NAME_ERR if the name is not acceptable.
     * (Attribute factory will do that test for us.)
     */
    @Override
    public void setAttribute(String name, String value) {
        if (needsSyncData()) {
            synchronizeData();
        }

        Attr newAttr = getAttributeNode(name);
        if (newAttr == null) {
            newAttr = getOwnerDocument().createAttribute(name);

            if (attributes == null) {
                attributes = new AttributeMap(this, null);
            }

            newAttr.setNodeValue(value);
            attributes.setNamedItem(newAttr);
        }
        else {
            newAttr.setNodeValue(value);
        }

    }

    /**
     * {@inheritDoc}
     *
     * Add a new attribute/value pair, or replace the value of the
     * existing attribute with that name.
     * <P>
     * This method allows you to add an Attribute that has already been
     * constructed, and hence avoids the limitations of the simple
     * setAttribute() call. It can handle attribute values that have
     * arbitrarily complex tree structure -- in particular, those which
     * had entity references mixed into their text.
     *
     * @throws DOMException INUSE_ATTRIBUTE_ERR if the Attribute object
     * has already been assigned to another Element.
     */
    @Override
    public Attr setAttributeNode(Attr newAttr)
        throws DOMException
        {

        if (needsSyncData()) {
            synchronizeData();
        }

        if (ownerDocument.errorChecking) {
            if (newAttr.getOwnerDocument() != ownerDocument) {
                String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null);
                throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, msg);
            }
        }

        if (attributes == null) {
            attributes = new AttributeMap(this, null);
        }
        // This will throw INUSE if necessary
        return (Attr) attributes.setNamedItem(newAttr);

    }

    /**
     * {@inheritDoc}
     *
     * Introduced in DOM Level 2. <p>
     *
     * Retrieves an attribute value by local name and namespace URI.
     *
     * @param namespaceURI
     *                      The namespace URI of the attribute to
     *                      retrieve.
     * @param localName     The local name of the attribute to retrieve.
     * @return String       The Attr value as a string, or empty string
     *                      if that attribute
     *                      does not have a specified or default value.
     */
    @Override
    public String getAttributeNS(String namespaceURI, String localName) {

        if (needsSyncData()) {
            synchronizeData();
        }

        if (attributes == null) {
            return "";
        }

        Attr attr = (Attr)(attributes.getNamedItemNS(namespaceURI, localName));
        return (attr == null) ? "" : attr.getValue();

    } // getAttributeNS(String,String):String

    /**
     * {@inheritDoc}
     *
     * Introduced in DOM Level 2. <p>
     *
     *  Adds a new attribute.
     *  If the given namespaceURI is null or an empty string and the
     *  qualifiedName has a prefix that is "xml", the new attribute is bound to
     *  the predefined namespace "http://www.w3.org/XML/1998/namespace"
     *  [Namespaces].  If an attribute with the same local name and namespace
     *  URI is already present on the element, its prefix is changed to be the
     *  prefix part of the qualifiedName, and its value is changed to be the
     *  value parameter. This value is a simple string, it is not parsed as it
     *  is being set. So any markup (such as syntax to be recognized as an
     *  entity reference) is treated as literal text, and needs to be
     *  appropriately escaped by the implementation when it is written out. In
     *  order to assign an attribute value that contains entity references, the
     *  user must create an Attr node plus any Text and EntityReference nodes,
     *  build the appropriate subtree, and use setAttributeNodeNS or
     *  setAttributeNode to assign it as the value of an attribute.
     *
     * @param namespaceURI      The namespace URI of the attribute to create
     *                          or alter.
     * @param qualifiedName     The qualified name of the attribute to create or
     *                          alter.
     * @param value             The value to set in string form.
     */
     @Override
    public void setAttributeNS(String namespaceURI,String qualifiedName,
                                  String value) {
        if (needsSyncData()) {
            synchronizeData();
        }
        int index = qualifiedName.indexOf(':');
        String prefix, localName;
        if (index < 0) {
            prefix = null;
            localName = qualifiedName;
        }
        else {
            prefix = qualifiedName.substring(0, index);
            localName = qualifiedName.substring(index + 1);
        }
        Attr newAttr = getAttributeNodeNS(namespaceURI, localName);
        if (newAttr == null) {
            // REVISIT: this is not efficient, we are creating twice the same
            //          strings for prefix and localName.
            newAttr = getOwnerDocument().createAttributeNS(
                    namespaceURI,
                    qualifiedName);
            if (attributes == null) {
                attributes = new AttributeMap(this, null);
            }
            newAttr.setNodeValue(value);
            attributes.setNamedItemNS(newAttr);
        }
        else {
            if (newAttr instanceof AttrNSImpl){
                // change prefix and value
                ((AttrNSImpl)newAttr).name= (prefix!=null)?(prefix+":"+localName):localName;
            }
            else {
                // This case may happen if user calls:
                //      elem.setAttribute("name", "value");
                //      elem.setAttributeNS(null, "name", "value");
                // This case is not defined by the DOM spec, we choose
                // to create a new attribute in this case and remove an old one from the tree
                // note this might cause events to be propagated or user data to be lost
                newAttr = ((CoreDocumentImpl)getOwnerDocument()).createAttributeNS(namespaceURI, qualifiedName, localName);
                attributes.setNamedItemNS(newAttr);
            }

            newAttr.setNodeValue(value);
        }

    }


    /**
     * {@inheritDoc}
     *
     * Introduced in DOM Level 2. <p>
     *
     * Removes an attribute by local name and namespace URI. If the removed
     * attribute has a default value it is immediately replaced.
     * The replacing attribute has the same namespace URI and local name,
     * as well as the original prefix.<p>
     *
     * @param namespaceURI  The namespace URI of the attribute to remove.
     *
     * @param localName     The local name of the attribute to remove.
     */
    @Override
    public void removeAttributeNS(String namespaceURI, String localName) {
        if (needsSyncData()) {
            synchronizeData();
        }

        if (attributes == null) {
            return;
        }

        attributes.internalRemoveNamedItemNS(namespaceURI, name, false);

    }

    /**
     * {@inheritDoc}
     *
     * Retrieves an Attr node by local name and namespace URI.
     *
     * @param namespaceURI  The namespace URI of the attribute to
     *                      retrieve.
     * @param localName     The local name of the attribute to retrieve.
     * @return Attr         The Attr node with the specified attribute
     *                      local name and namespace
     *                      URI or null if there is no such attribute.
     */
    @Override
    public Attr getAttributeNodeNS(String namespaceURI, String localName){

        if (needsSyncData()) {
            synchronizeData();
        }
        if (attributes == null) {
            return null;
        }
        return (Attr)attributes.getNamedItemNS(namespaceURI, localName);

    } // getAttributeNodeNS(String,String):Attr

    /**
     * {@inheritDoc}
     *
     * Introduced in DOM Level 2. <p>
     *
     * Adds a new attribute. If an attribute with that local name and
     * namespace URI is already present in the element, it is replaced
     * by the new one.
     *
     * @param newAttr   The Attr node to add to the attribute list. When
     *                  the Node has no namespaceURI, this method behaves
     *                  like setAttributeNode.
     * @return Attr     If the newAttr attribute replaces an existing attribute
     *                  with the same local name and namespace URI, the *
     *                  previously existing Attr node is returned, otherwise
     *                  null is returned.
     * @throws          DOMException WRONG_DOCUMENT_ERR: Raised if newAttr
     *                  was created from a different document than the one that
     *                  created the element.
     *
     * @throws          DOMException INUSE_ATTRIBUTE_ERR: Raised if newAttr is
     *                  already an attribute of another Element object. The
     *                  DOM user must explicitly clone Attr nodes to re-use
     *                  them in other elements.
     */
    @Override
    public Attr setAttributeNodeNS(Attr newAttr)
        throws DOMException
        {

        if (needsSyncData()) {
            synchronizeData();
        }
        if (ownerDocument.errorChecking) {
            if (newAttr.getOwnerDocument() != ownerDocument) {
                String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null);
                throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, msg);
            }
        }

        if (attributes == null) {
            attributes = new AttributeMap(this, null);
        }
        // This will throw INUSE if necessary
        return (Attr) attributes.setNamedItemNS(newAttr);

    }

    /**
     * {@inheritDoc}
     *
     * Introduced in DOM Level 2.
     */
    @Override
    public boolean hasAttributes() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return (attributes != null && attributes.getLength() != 0);
    }

    /**
     * {@inheritDoc}
     *
     * Introduced in DOM Level 2.
     */
    @Override
    public boolean hasAttribute(String name) {
        return getAttributeNode(name) != null;
    }

    /**
     * {@inheritDoc}
     *
     * Introduced in DOM Level 2.
     */
    @Override
    public boolean hasAttributeNS(String namespaceURI, String localName) {
        return getAttributeNodeNS(namespaceURI, localName) != null;
    }

    /**
     * {@inheritDoc}
     *
     * Introduced in DOM Level 2. <p>
     *
     * Returns a NodeList of all the Elements with a given local name and
     * namespace URI in the order in which they would be encountered in a
     * preorder traversal of the Document tree, starting from this node.
     *
     * @param namespaceURI The namespace URI of the elements to match
     *                     on. The special value "*" matches all
     *                     namespaces. When it is null or an empty
     *                     string, this method behaves like
     *                     getElementsByTagName.
     * @param localName    The local name of the elements to match on.
     *                     The special value "*" matches all local names.
     * @return NodeList    A new NodeList object containing all the matched
     *                     Elements.
     */
    @Override
    public NodeList getElementsByTagNameNS(String namespaceURI,
                                           String localName) {
        return new DeepNodeListImpl(this, namespaceURI, localName);
    }

    /**
     * {@inheritDoc}
     *
     * DOM Level 3 WD- Experimental.
     * Override inherited behavior from NodeImpl and ParentNode to check on
     * attributes
     */
    @Override
    public boolean isEqualNode(Node arg) {
        if (!super.isEqualNode(arg)) {
            return false;
        }
        boolean hasAttrs = hasAttributes();
        if (hasAttrs != arg.hasAttributes()) {
            return false;
        }
        if (hasAttrs) {
            NamedNodeMap map1 = getAttributes();
            NamedNodeMap map2 = arg.getAttributes();
            int len = map1.getLength();
            if (len != map2.getLength()) {
                return false;
            }
            for (int i = 0; i < len; i++) {
                Node n1 = map1.item(i);
                if (n1.getLocalName() == null) { // DOM Level 1 Node
                    Node n2 = map2.getNamedItem(n1.getNodeName());
                    if (n2 == null || !n1.isEqualNode(n2)) {
                        return false;
                    }
                }
                else {
                    Node n2 = map2.getNamedItemNS(n1.getNamespaceURI(),
                                                  n1.getLocalName());
                    if (n2 == null || !n1.isEqualNode(n2)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * DOM Level 3: register the given attribute node as an ID attribute
     */
    @Override
    public void setIdAttributeNode(Attr at, boolean makeId) {
        if (needsSyncData()) {
            synchronizeData();
        }
        if (ownerDocument.errorChecking) {
            if (at.getOwnerElement() != this) {
                String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null);
                throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
            }
        }
        ((AttrImpl) at).isIdAttribute(makeId);
        if (!makeId) {
            ownerDocument.removeIdentifier(at.getValue());
        }
        else {
            ownerDocument.putIdentifier(at.getValue(), this);
        }
    }

    /**
     * {@inheritDoc}
     *
     * DOM Level 3: register the given attribute node as an ID attribute
     */
    @Override
    public void setIdAttribute(String name, boolean makeId) {
        if (needsSyncData()) {
            synchronizeData();
        }
        Attr at = getAttributeNode(name);

        if( at == null){
               String msg = DOMMessageFormatter.formatMessage(
                                    DOMMessageFormatter.DOM_DOMAIN,
                                    "NOT_FOUND_ERR", null);
            throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
        }

        if (ownerDocument.errorChecking) {
            if (at.getOwnerElement() != this) {
                String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null);
                throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
            }
        }

        ((AttrImpl) at).isIdAttribute(makeId);
        if (!makeId) {
            ownerDocument.removeIdentifier(at.getValue());
        }
        else {
            ownerDocument.putIdentifier(at.getValue(), this);
        }
    }

    /**
     * {@inheritDoc}
     *
     * DOM Level 3: register the given attribute node as an ID attribute
     */
    @Override
    public void setIdAttributeNS(String namespaceURI, String localName,
                                    boolean makeId) {
        if (needsSyncData()) {
            synchronizeData();
        }
        Attr at = getAttributeNodeNS(namespaceURI, localName);

        if( at == null){
               String msg = DOMMessageFormatter.formatMessage(
                                    DOMMessageFormatter.DOM_DOMAIN,
                                    "NOT_FOUND_ERR", null);
            throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
        }

        if (ownerDocument.errorChecking) {
            if (at.getOwnerElement() != this) {
                String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null);
                throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
            }
        }
        ((AttrImpl) at).isIdAttribute(makeId);
        if (!makeId) {
            ownerDocument.removeIdentifier(at.getValue());
        }
        else {
            ownerDocument.putIdentifier(at.getValue(), this);
        }
   }

    /**
     * {@inheritDoc}
     *
     * @see org.w3c.dom.TypeInfo#getTypeName()
     */
     @Override
    public String getTypeName() {
        return null;
     }

    /**
     * {@inheritDoc}
     *
     * @see org.w3c.dom.TypeInfo#getTypeNamespace()
     */
    @Override
    public String getTypeNamespace() {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Introduced in DOM Level 3. <p>
     * Checks if a type is derived from another by restriction. See:
     * http://www.w3.org/TR/DOM-Level-3-Core/core.html#TypeInfo-isDerivedFrom
     *
     * @param typeNamespaceArg
     *        The namspace of the ancestor type declaration
     * @param typeNameArg
     *        The name of the ancestor type declaration
     * @param derivationMethod
     *        The derivation method
     *
     * @return boolean True if the type is derived by restriciton for the
     *         reference type
     */
    @Override
    public boolean isDerivedFrom(String typeNamespaceArg,
                                 String typeNameArg,
                                 int derivationMethod) {

        return false;
    }

    /**
     * {@inheritDoc}
     *
     * Method getSchemaTypeInfo.
     * @return TypeInfo
     */
    @Override
    public TypeInfo getSchemaTypeInfo(){
        if(needsSyncData()) {
            synchronizeData();
        }
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * Synchronizes the data (name and value) for fast nodes.
     */
    @Override
    protected void synchronizeData() {

        // no need to sync in the future
        needsSyncData(false);

        // attributes
        setupDefaultAttributes();
    } // synchronizeData()

    // support for DOM Level 3 renameNode method
    // @param el The element from which to take the attributes
    void moveSpecifiedAttributes(ElementImpl el) {
        if (needsSyncData()) {
            synchronizeData();
        }
        if (el.hasAttributes()) {
            if (attributes == null) {
                attributes = new AttributeMap(this, null);
            }
            attributes.moveSpecifiedAttributes(el.attributes);
        }
    }

    /** Setup the default attributes. */
    protected void setupDefaultAttributes() {
        NamedNodeMapImpl defaults = getDefaultAttributes();
        if (defaults != null) {
            attributes = new AttributeMap(this, defaults);
        }
    }

    /** Reconcile default attributes. */
    protected void reconcileDefaultAttributes() {
        if (attributes != null) {
            NamedNodeMapImpl defaults = getDefaultAttributes();
            attributes.reconcileDefaults(defaults);
        }
    }

    // Get the default attributes.
    protected NamedNodeMapImpl getDefaultAttributes() {

        DocumentTypeImpl doctype =
            (DocumentTypeImpl) ownerDocument.getDoctype();
        if (doctype == null) {
            return null;
        }
        ElementDefinitionImpl eldef =
            (ElementDefinitionImpl)doctype.getElements()
                                               .getNamedItem(getNodeName());
        if (eldef == null) {
            return null;
        }
        return (NamedNodeMapImpl) eldef.getAttributes();

    }

    // Returns the first element node found from a
    // non-recursive in order traversal of the given node.
    private Element getFirstElementChild(Node n) {
        final Node top = n;
        while (n != null) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) n;
            }
            Node next = n.getFirstChild();
            while (next == null) {
                if (top == n) {
                    break;
                }
                next = n.getNextSibling();
                if (next == null) {
                    n = n.getParentNode();
                    if (n == null || top == n) {
                        return null;
                    }
                }
            }
            n = next;
        }
        return null;
    }

    // Returns the next logical sibling with respect to the given node.
    private Node getNextLogicalSibling(Node n) {
        Node next = n.getNextSibling();
        // If "n" has no following sibling and its parent is an entity reference node we
        // need to continue the search through the following siblings of the entity
        // reference as these are logically siblings of the given node.
        if (next == null) {
            Node parent = n.getParentNode();
            while (parent != null && parent.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
                next = parent.getNextSibling();
                if (next != null) {
                    break;
                }
                parent = parent.getParentNode();
            }
        }
        return next;
    }

}
