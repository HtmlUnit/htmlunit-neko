/*
 * Copyright (c) 2017-2026 Ronald Brill
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

import org.htmlunit.cyberneko.xerces.util.DOMMessageFormatter;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

/**
 * NodeImpl provides the basic structure of a DOM tree. It is never used
 * directly, but instead is subclassed to add type and data information, and
 * additional methods, appropriate to each node of the tree. Only its subclasses
 * should be instantiated -- and those, with the exception of Document itself,
 * only through a specific Document's factory methods.
 * <p>
 * The Node interface provides shared behaviors such as siblings and children,
 * both for consistency and so that the most common tree operations may be
 * performed without constantly having to downcast to specific node types. When
 * there is no obvious mapping for one of these queries, it will respond with
 * {@code null}. Note that the default behavior is that children are forbidden. To
 * permit them, the subclass ParentNode overrides several methods.
 * </p>
 * <p>
 * NodeImpl also implements NodeList, so it can return itself in response to the
 * getChildNodes() query. This eliminates the need for a separate ChildNodeList
 * object. Note that this is an IMPLEMENTATION DETAIL; applications should
 * <em>never</em> assume that this identity exists.
 * </p>
 * <p>
 * All nodes in a single document must originate in that document. (Note that
 * this is much tighter than "must be same implementation") Nodes are all aware
 * of their ownerDocument, and attempts to mismatch will throw
 * WRONG_DOCUMENT_ERR.
 * </p>
 * <p>
 * However, to save memory not all nodes always have a direct reference to their
 * ownerDocument. When a node is owned by another node it relies on its owner to
 * store its ownerDocument. Parent nodes always store it though, so there is
 * never more than one level of indirection. And when a node doesn't have an
 * owner, ownerNode refers to its ownerDocument.
 * </p>
 * <p>
 * This class doesn't directly support mutation events; however, it still
 * implements the EventTarget interface and forwards all related calls to the
 * document so that the document class can do so.
 * </p>
 *
 * @author Arnaud Le Hors, IBM
 * @author Joe Kesselman, IBM
 * @author Ronald Brill
 */
public abstract class NodeImpl implements Node, NodeList, EventTarget, Cloneable {

    // TreePosition Constants.
    // Taken from DOM L3 Node interface.

    // DocumentPosition
    public static final short DOCUMENT_POSITION_DISCONNECTED = 0x01;
    public static final short DOCUMENT_POSITION_PRECEDING = 0x02;
    public static final short DOCUMENT_POSITION_FOLLOWING = 0x04;
    public static final short DOCUMENT_POSITION_CONTAINS = 0x08;
    public static final short DOCUMENT_POSITION_IS_CONTAINED = 0x10;
    public static final short DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC = 0x20;

    /** Typically the parent, but not always! */
    protected NodeImpl ownerNode_;

    private int flags_;

    protected static final int READONLY = 0x1 << 0;
    protected static final int SYNCCHILDREN = 0x1 << 2;
    protected static final int OWNED = 0x1 << 3;
    protected static final int FIRSTCHILD = 0x1 << 4;
    protected static final int SPECIFIED = 0x1 << 5;
    protected static final int HASSTRING = 0x1 << 7;
    protected static final int NORMALIZED = 0x1 << 8;
    protected static final int ID = 0x1 << 9;

    /**
     * No public constructor; only subclasses of Node should be instantiated, and
     * those normally via a Document's factory methods.
     * <p>
     * Every Node knows what Document it belongs to.
     * </p>
     *
     * @param ownerDocument the owner document
     */
    protected NodeImpl(final CoreDocumentImpl ownerDocument) {
        // as long as we do not have any owner, ownerNode is our ownerDocument
        ownerNode_ = ownerDocument;
    }

    /**
     * {@inheritDoc}
     *
     * A short integer indicating what type of node this is. The named constants for
     * this value are defined in the org.w3c.dom.Node interface.
     */
    @Override
    public abstract short getNodeType();

    /**
     * {@inheritDoc}
     *
     * Returns the name of this node.
     */
    @Override
    public abstract String getNodeName();

    /**
     * {@inheritDoc}
     *
     * Returns the node value.
     *
     * @return the node value, or {@code null}
     * @throws DOMException DOMSTRING_SIZE_ERR
     */
    @Override
    public String getNodeValue() throws DOMException {
        return null; // overridden in some subclasses
    }

    /**
     * {@inheritDoc}
     *
     * Sets the node value.
     *
     * @param x the new node value
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR
     */
    @Override
    public void setNodeValue(final String x) throws DOMException {
        // Default behavior is to do nothing, overridden in some subclasses
    }

    /**
     * {@inheritDoc}
     *
     * Adds a child node to the end of the list of children for this node.
     * Convenience shorthand for insertBefore(newChild,null).
     *
     * @see #insertBefore(Node, Node)
     *      <P>
     *      By default we do not accept any children, ParentNode overrides this.
     * @see ParentNode
     *
     * @param newChild the child node to add
     * @return newChild, in its new state (relocated, or emptied in the case of DocumentNode)
     * @throws DOMException HIERARCHY_REQUEST_ERR if newChild is of a type that shouldn't be a child of this node
     * @throws DOMException WRONG_DOCUMENT_ERR if newChild has a different owner document than we do
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR if this node is read-only
     */
    @Override
    public Node appendChild(final Node newChild) throws DOMException {
        return insertBefore(newChild, null);
    }

    /**
     * {@inheritDoc}
     *
     * Returns a duplicate of a given node. You can consider this a generic "copy
     * constructor" for nodes. The newly returned object should be completely
     * independent of the source object's subtree, so changes in one after the clone
     * has been made will not affect the other.
     * <p>
     * Note: since we never have any children deep is meaningless here, ParentNode
     * overrides this behavior.
     * </p>
     *
     * @param deep whether to recursively clone all children in the subtree
     * @return the cloned node
     * @see ParentNode
     *
     *      <p>
     *      Example: Cloning a Text node will copy both the node and the text it
     *      contains.
     *      <p>
     *      Example: Cloning something that has children -- Element or Attr, for
     *      example -- will _not_ clone those children unless a "deep clone" has
     *      been requested. A shallow clone of an Attr node will yield an empty Attr
     *      of the same name.
     *      <p>
     *      NOTE: Clones will always be read/write, even if the node being cloned is
     *      read-only, to permit applications using only the DOM API to obtain
     *      editable copies of locked portions of the tree.
     */
    @Override
    public Node cloneNode(final boolean deep) {
        final NodeImpl newnode;
        try {
            newnode = (NodeImpl) clone();
        }
        catch (final CloneNotSupportedException e) {
            // if we get here we have an error in our program we may as well
            // be vocal about it, so that people can take appropriate action.
            throw new RuntimeException("**Internal Error**" + e);
        }

        // Need to break the association w/ original kids
        newnode.ownerNode_ = ownerDocument();
        newnode.isOwned(false);

        return newnode;
    }

    /**
     * {@inheritDoc}
     *
     * Finds the Document that this Node belongs to (the document in whose context
     * the Node was created). The Node may or may not currently be part of that
     * Document's actual contents.
     */
    @Override
    public Document getOwnerDocument() {
        // if we have an owner simply forward the request
        // otherwise ownerNode is our ownerDocument
        if (isOwned()) {
            return ownerNode_.ownerDocument();
        }
        return (Document) ownerNode_;
    }

    /**
     * Same as above but returns internal type and this one is not overridden by
     * CoreDocumentImpl to return null.
     *
     * @return the core document implementation instance
     */
    CoreDocumentImpl ownerDocument() {
        // if we have an owner simply forward the request
        // otherwise ownerNode is our ownerDocument
        if (isOwned()) {
            return ownerNode_.ownerDocument();
        }
        return (CoreDocumentImpl) ownerNode_;
    }

    // NON-DOM set the ownerDocument of this node
    protected void setOwnerDocument(final CoreDocumentImpl doc) {
        // if we have an owner we rely on it to have it right
        // otherwise ownerNode is our ownerDocument
        if (!isOwned()) {
            ownerNode_ = doc;
        }
    }

    /**
     * Returns the node number.
     *
     * @return the node number
     */
    protected int getNodeNumber() {
        final CoreDocumentImpl cd = (CoreDocumentImpl) getOwnerDocument();
        return cd.getNodeNumber(this);
    }

    /**
     * Obtain the DOM-tree parent of this node, or {@code null} if it is not currently
     * active in the DOM tree (perhaps because it has just been created or removed).
     * Note that Document, DocumentFragment, and Attribute will never have parents.
     *
     * @return the parent node, or {@code null}
     */
    @Override
    public Node getParentNode() {
        return null; // overridden by ChildNode
    }

    /*
     * Same as above but returns internal type
     */
    NodeImpl parentNode() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getNextSibling() {
        return null; // default behavior, overridden in ChildNode
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getPreviousSibling() {
        return null; // default behavior, overridden in ChildNode
    }

    ChildNode previousSibling() {
        return null; // default behavior, overridden in ChildNode
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NamedNodeMap getAttributes() {
        return null; // overridden in ElementImpl
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasAttributes() {
        return false; // overridden in ElementImpl
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasChildNodes() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeList getChildNodes() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getFirstChild() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getLastChild() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node insertBefore(final Node newChild, final Node refChild) throws DOMException {
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                DOMMessageFormatter.formatMessage("HIERARCHY_REQUEST_ERR", null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node removeChild(final Node oldChild) throws DOMException {
        throw new DOMException(DOMException.NOT_FOUND_ERR,
                DOMMessageFormatter.formatMessage("NOT_FOUND_ERR", null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node replaceChild(final Node newChild, final Node oldChild) throws DOMException {
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                DOMMessageFormatter.formatMessage("HIERARCHY_REQUEST_ERR", null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLength() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node item(final int index) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void normalize() {
        /*
         * by default we do not have any children, ParentNode overrides this behavior
         */
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSupported(final String feature, final String version) {
        return ownerDocument().getImplementation().hasFeature(feature, version);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNamespaceURI() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPrefix() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPrefix(final String prefix) throws DOMException {
        throw new DOMException(DOMException.NAMESPACE_ERR,
                DOMMessageFormatter.formatMessage("NAMESPACE_ERR", null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLocalName() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEventListener(final String type, final EventListener listener, final boolean useCapture) {
        // simply forward to Document
        ownerDocument().addEventListener(this, type, listener, useCapture);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeEventListener(final String type, final EventListener listener, final boolean useCapture) {
        // simply forward to Document
        ownerDocument().removeEventListener(this, type, listener, useCapture);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean dispatchEvent(final Event event) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBaseURI() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short compareDocumentPosition(final Node other) throws DOMException {
        // If the nodes are the same, no flags should be set
        if (this == other) {
            return 0;
        }

        // check if other is from a different implementation
        if (other != null && !(other instanceof NodeImpl)) {
            // other comes from a different implementation
            final String msg = DOMMessageFormatter.formatMessage("NOT_SUPPORTED_ERR", null);
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
        }

        final Document thisOwnerDoc;
        final Document otherOwnerDoc;
        // get the respective Document owners.
        if (getNodeType() == Node.DOCUMENT_NODE) {
            thisOwnerDoc = (Document) this;
        }
        else {
            thisOwnerDoc = getOwnerDocument();
        }
        if (other.getNodeType() == Node.DOCUMENT_NODE) {
            otherOwnerDoc = (Document) other;
        }
        else {
            otherOwnerDoc = other.getOwnerDocument();
        }

        // If from different documents, we know they are disconnected.
        // and have an implementation dependent order
        if (thisOwnerDoc != otherOwnerDoc && thisOwnerDoc != null && otherOwnerDoc != null) {
            final int otherDocNum = ((CoreDocumentImpl) otherOwnerDoc).getNodeNumber();
            final int thisDocNum = ((CoreDocumentImpl) thisOwnerDoc).getNodeNumber();
            if (otherDocNum > thisDocNum) {
                return DOCUMENT_POSITION_DISCONNECTED | DOCUMENT_POSITION_FOLLOWING
                        | DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC;
            }

            return DOCUMENT_POSITION_DISCONNECTED | DOCUMENT_POSITION_PRECEDING
                    | DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC;
        }

        // Find the ancestor of each node, and the distance each node is from
        // its ancestor.
        // During this traversal, look for ancestor/descendent relationships
        // between the 2 nodes in question.
        // We do this now, so that we get this info correct for attribute nodes
        // and their children.

        Node node;
        Node thisAncestor = this;

        int thisDepth = 0;
        for (node = this; node != null; node = node.getParentNode()) {
            thisDepth += 1;
            if (node == other) {
                // The other node is an ancestor of this one.
                return DOCUMENT_POSITION_CONTAINS | DOCUMENT_POSITION_PRECEDING;
            }
            thisAncestor = node;
        }

        int otherDepth = 0;
        Node otherAncestor = other;
        for (node = other; node != null; node = node.getParentNode()) {
            otherDepth += 1;
            if (node == this) {
                // The other node is a descendent of the reference node.
                return DOCUMENT_POSITION_IS_CONTAINED | DOCUMENT_POSITION_FOLLOWING;
            }
            otherAncestor = node;
        }

        final int thisAncestorType = thisAncestor.getNodeType();
        final int otherAncestorType = otherAncestor.getNodeType();
        Node thisNode = this;
        Node otherNode = other;

        // Special casing for ENTITY, NOTATION, DOCTYPE and ATTRIBUTES
        // LM: should rewrite this.
        switch (thisAncestorType) {
            case Node.NOTATION_NODE:
            case Node.ENTITY_NODE: {
                final DocumentType container = thisOwnerDoc.getDoctype();
                if (container == otherAncestor) {
                    return DOCUMENT_POSITION_CONTAINS | DOCUMENT_POSITION_PRECEDING;
                }

                switch (otherAncestorType) {
                    case Node.NOTATION_NODE:
                    case Node.ENTITY_NODE: {
                        if (thisAncestorType != otherAncestorType) {
                            // the nodes are of different types
                            return (thisAncestorType > otherAncestorType)
                                        ? DOCUMENT_POSITION_PRECEDING
                                                : DOCUMENT_POSITION_FOLLOWING;
                        }

                        // the nodes are of the same type. Find order.
                        if (thisAncestorType == Node.NOTATION_NODE) {
                            if (((NamedNodeMapImpl) container.getNotations()).precedes(otherAncestor, thisAncestor)) {
                                return DOCUMENT_POSITION_PRECEDING | DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC;
                            }
                            return DOCUMENT_POSITION_FOLLOWING | DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC;
                        }
                        if (((NamedNodeMapImpl) container.getEntities()).precedes(otherAncestor, thisAncestor)) {
                            return DOCUMENT_POSITION_PRECEDING | DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC;
                        }
                        return DOCUMENT_POSITION_FOLLOWING | DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC;
                    }
                }
                thisNode = thisAncestor = thisOwnerDoc;
                break;
            }
            case Node.DOCUMENT_TYPE_NODE: {
                if (otherNode == thisOwnerDoc) {
                    return DOCUMENT_POSITION_PRECEDING | DOCUMENT_POSITION_CONTAINS;
                }
                else if (thisOwnerDoc != null && thisOwnerDoc == otherOwnerDoc) {
                    return DOCUMENT_POSITION_FOLLOWING;
                }
                break;
            }
            case Node.ATTRIBUTE_NODE: {
                thisNode = ((AttrImpl) thisAncestor).getOwnerElement();
                if (otherAncestorType == Node.ATTRIBUTE_NODE) {
                    otherNode = ((AttrImpl) otherAncestor).getOwnerElement();
                    if (otherNode == thisNode) {
                        if (((NamedNodeMapImpl) thisNode.getAttributes()).precedes(other, this)) {
                            return DOCUMENT_POSITION_PRECEDING | DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC;
                        }

                        return DOCUMENT_POSITION_FOLLOWING | DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC;
                    }
                }

                // Now, find the ancestor of the element
                thisDepth = 0;
                for (node = thisNode; node != null; node = node.getParentNode()) {
                    thisDepth += 1;
                    if (node == otherNode) {
                        // The other node is an ancestor of the owning element
                        return DOCUMENT_POSITION_CONTAINS | DOCUMENT_POSITION_PRECEDING;
                    }
                    thisAncestor = node;
                }
            }
        }

        switch (otherAncestorType) {
            case Node.NOTATION_NODE:
            case Node.ENTITY_NODE: {
                final DocumentType container = thisOwnerDoc.getDoctype();
                if (container == this) {
                    return DOCUMENT_POSITION_IS_CONTAINED | DOCUMENT_POSITION_FOLLOWING;
                }
                otherNode = otherAncestor = thisOwnerDoc;
                break;
            }
            case Node.DOCUMENT_TYPE_NODE: {
                if (thisNode == otherOwnerDoc) {
                    return DOCUMENT_POSITION_FOLLOWING | DOCUMENT_POSITION_IS_CONTAINED;
                }
                else if (otherOwnerDoc != null && thisOwnerDoc == otherOwnerDoc) {
                    return DOCUMENT_POSITION_PRECEDING;
                }
                break;
            }
            case Node.ATTRIBUTE_NODE: {
                otherDepth = 0;
                otherNode = ((AttrImpl) otherAncestor).getOwnerElement();
                for (node = otherNode; node != null; node = node.getParentNode()) {
                    otherDepth += 1;
                    if (node == thisNode) {
                        // The other node is a descendent of the reference
                        // node's element
                        return DOCUMENT_POSITION_FOLLOWING | DOCUMENT_POSITION_IS_CONTAINED;
                    }
                    otherAncestor = node;
                }

            }
        }

        // thisAncestor and otherAncestor must be the same at this point,
        // otherwise, the original nodes are disconnected
        if (thisAncestor != otherAncestor) {
            final int thisAncestorNum;
            final int otherAncestorNum;
            thisAncestorNum = ((NodeImpl) thisAncestor).getNodeNumber();
            otherAncestorNum = ((NodeImpl) otherAncestor).getNodeNumber();

            if (thisAncestorNum > otherAncestorNum) {
                return DOCUMENT_POSITION_DISCONNECTED | DOCUMENT_POSITION_FOLLOWING
                        | DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC;
            }

            return DOCUMENT_POSITION_DISCONNECTED | DOCUMENT_POSITION_PRECEDING
                    | DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC;
        }

        // Go up the parent chain of the deeper node, until we find a node
        // with the same depth as the shallower node

        if (thisDepth > otherDepth) {
            for (int i = 0; i < thisDepth - otherDepth; i++) {
                thisNode = thisNode.getParentNode();
            }
            // Check if the node we have reached is in fact "otherNode". This can
            // happen in the case of attributes. In this case, otherNode
            // "precedes" this.
            if (thisNode == otherNode) {
                return DOCUMENT_POSITION_PRECEDING;
            }
        }

        else {
            for (int i = 0; i < otherDepth - thisDepth; i++) {
                otherNode = otherNode.getParentNode();
            }
            // Check if the node we have reached is in fact "thisNode". This can
            // happen in the case of attributes. In this case, otherNode
            // "follows" this.
            if (otherNode == thisNode) {
                return DOCUMENT_POSITION_FOLLOWING;
            }
        }

        // We now have nodes at the same depth in the tree. Find a common
        // ancestor.
        Node thisNodeP;
        Node otherNodeP;
        for (thisNodeP = thisNode.getParentNode(), otherNodeP = otherNode.getParentNode(); thisNodeP != otherNodeP;) {
            thisNode = thisNodeP;
            otherNode = otherNodeP;
            thisNodeP = thisNodeP.getParentNode();
            otherNodeP = otherNodeP.getParentNode();
        }

        // At this point, thisNode and otherNode are direct children of
        // the common ancestor.
        // See whether thisNode or otherNode is the leftmost

        for (Node current = thisNodeP.getFirstChild(); current != null; current = current.getNextSibling()) {
            if (current == otherNode) {
                return DOCUMENT_POSITION_PRECEDING;
            }
            else if (current == thisNode) {
                return DOCUMENT_POSITION_FOLLOWING;
            }
        }
        // REVISIT: shouldn't get here. Should probably throw an
        // exception
        return 0;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTextContent() throws DOMException {
        return getNodeValue(); // overridden in some subclasses
    }

    // internal method taking a StringBuilder in parameter
    void getTextContent(final StringBuilder builder) throws DOMException {
        final String content = getNodeValue();
        if (content != null) {
            builder.append(content);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTextContent(final String textContent) throws DOMException {
        setNodeValue(textContent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSameNode(final Node other) {
        // we do not use any wrapper so the answer is obvious
        return this == other;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDefaultNamespace(final String namespaceURI) {
        // REVISIT: remove casts when DOM L3 becomes REC.
        final short type = getNodeType();
        switch (type) {
            case Node.ELEMENT_NODE: {
                final String namespace = this.getNamespaceURI();
                final String prefix = this.getPrefix();

                // REVISIT: is it possible that prefix is empty string?
                if (prefix == null || prefix.isEmpty()) {
                    if (namespaceURI == null) {
                        return namespace == null;
                    }
                    return namespaceURI.equals(namespace);
                }
                if (hasAttributes()) {
                    final ElementImpl elem = (ElementImpl) this;
                    final NodeImpl attr = (NodeImpl) elem.getAttributeNodeNS("http://www.w3.org/2000/xmlns/", "xmlns");
                    if (attr != null) {
                        final String value = attr.getNodeValue();
                        if (namespaceURI == null) {
                            return namespace == value;
                        }
                        return namespaceURI.equals(value);
                    }
                }

                final NodeImpl ancestor = (NodeImpl) getElementAncestor(this);
                if (ancestor != null) {
                    return ancestor.isDefaultNamespace(namespaceURI);
                }
                return false;
            }
            case Node.DOCUMENT_NODE: {
                final Element docElement = ((Document) this).getDocumentElement();
                if (docElement != null) {
                    return docElement.isDefaultNamespace(namespaceURI);
                }
                return false;
            }

            case Node.ENTITY_NODE:
            case Node.NOTATION_NODE:
            case Node.DOCUMENT_FRAGMENT_NODE:
            case Node.DOCUMENT_TYPE_NODE:
                // type is unknown
                return false;
            case Node.ATTRIBUTE_NODE: {
                if (ownerNode_.getNodeType() == Node.ELEMENT_NODE) {
                    return ownerNode_.isDefaultNamespace(namespaceURI);

                }
                return false;
            }
            default: {
                final NodeImpl ancestor = (NodeImpl) getElementAncestor(this);
                if (ancestor != null) {
                    return ancestor.isDefaultNamespace(namespaceURI);
                }
                return false;
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String lookupPrefix(final String namespaceURI) {

        // REVISIT: When Namespaces 1.1 comes out this may not be true
        // Prefix can't be bound to null namespace
        if (namespaceURI == null) {
            return null;
        }

        final short type = getNodeType();
        switch (type) {
            case Node.ELEMENT_NODE: {
                getNamespaceURI(); // to flip out children
                return lookupNamespacePrefix(namespaceURI, (ElementImpl) this);
            }
            case Node.DOCUMENT_NODE: {
                final Element docElement = ((Document) this).getDocumentElement();
                if (docElement != null) {
                    return docElement.lookupPrefix(namespaceURI);
                }
                return null;
            }

            case Node.ENTITY_NODE:
            case Node.NOTATION_NODE:
            case Node.DOCUMENT_FRAGMENT_NODE:
            case Node.DOCUMENT_TYPE_NODE:
                // type is unknown
                return null;
            case Node.ATTRIBUTE_NODE: {
                if (ownerNode_.getNodeType() == Node.ELEMENT_NODE) {
                    return ownerNode_.lookupPrefix(namespaceURI);

                }
                return null;
            }
            default: {
                final NodeImpl ancestor = (NodeImpl) getElementAncestor(this);
                if (ancestor != null) {
                    return ancestor.lookupPrefix(namespaceURI);
                }
                return null;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String lookupNamespaceURI(final String specifiedPrefix) {
        switch (getNodeType()) {
            case Node.ELEMENT_NODE:
                String namespace = getNamespaceURI();
                if (namespace != null) {
                    final String prefix = getPrefix();
                    // REVISIT: is it possible that prefix is empty string?
                    if (specifiedPrefix == null && prefix == null) {
                        // looking for default namespace
                        return namespace;
                    }
                    else if (prefix != null && prefix.equals(specifiedPrefix)) {
                        // non default namespace
                        return namespace;
                    }
                }
                if (hasAttributes()) {
                    final NamedNodeMap map = getAttributes();
                    final int length = map.getLength();
                    for (int i = 0; i < length; i++) {
                        final Node attr = map.item(i);
                        namespace = attr.getNamespaceURI();
                        if ("http://www.w3.org/2000/xmlns/".equals(namespace)) {
                            final String attrPrefix = attr.getPrefix();
                            final String value = attr.getNodeValue();
                            // at this point we are dealing with DOM Level 2 nodes only
                            if (specifiedPrefix == null && "xmlns".equals(attr.getNodeName())) {
                                // default namespace
                                return !value.isEmpty() ? value : null;
                            }
                            else if ("xmlns".equals(attrPrefix)
                                        && attr.getLocalName().equals(specifiedPrefix)) {
                                // non default namespace
                                return !value.isEmpty() ? value : null;
                            }
                        }
                    }
                }
                final NodeImpl ancestor = (NodeImpl) getElementAncestor(this);
                if (ancestor != null) {
                    return ancestor.lookupNamespaceURI(specifiedPrefix);
                }

                return null;

            case Node.DOCUMENT_NODE:
                final Element docElement = ((Document) this).getDocumentElement();
                if (docElement != null) {
                    return docElement.lookupNamespaceURI(specifiedPrefix);
                }
                return null;

            case Node.ENTITY_NODE:
            case Node.NOTATION_NODE:
            case Node.DOCUMENT_FRAGMENT_NODE:
            case Node.DOCUMENT_TYPE_NODE:
                // type is unknown
                return null;

            case Node.ATTRIBUTE_NODE:
                if (ownerNode_.getNodeType() == Node.ELEMENT_NODE) {
                    return ownerNode_.lookupNamespaceURI(specifiedPrefix);

                }
                return null;

            default:
                final NodeImpl ancestorDef = (NodeImpl) getElementAncestor(this);
                if (ancestorDef != null) {
                    return ancestorDef.lookupNamespaceURI(specifiedPrefix);
                }
                return null;
        }
    }

    Node getElementAncestor(final Node currentNode) {
        Node parent = currentNode.getParentNode();
        while (parent != null) {
            final short type = parent.getNodeType();
            if (type == Node.ELEMENT_NODE) {
                return parent;
            }
            parent = parent.getParentNode();
        }
        return null;
    }

    String lookupNamespacePrefix(final String namespaceURI, final ElementImpl el) {
        String namespace = getNamespaceURI();
        if (namespace != null && namespace.equals(namespaceURI)) {
            // REVISIT: if no prefix is available is it null or empty string, or
            // could be both?
            final String prefix = getPrefix();
            if (prefix != null) {
                final String foundNamespace = el.lookupNamespaceURI(prefix);
                if (foundNamespace != null && foundNamespace.equals(namespaceURI)) {
                    return prefix;
                }

            }
        }
        if (hasAttributes()) {
            final NamedNodeMap map = getAttributes();
            final int length = map.getLength();
            for (int i = 0; i < length; i++) {
                final Node attr = map.item(i);
                namespace = attr.getNamespaceURI();
                if ("http://www.w3.org/2000/xmlns/".equals(namespace)) {
                    final String attrPrefix = attr.getPrefix();
                    final String value = attr.getNodeValue();
                    // DOM Level 2 nodes
                    if (("xmlns".equals(attr.getNodeName()))
                            || ("xmlns".equals(attrPrefix)) && value.equals(namespaceURI)) {

                        final String localname = attr.getLocalName();
                        final String foundNamespace = el.lookupNamespaceURI(localname);
                        if (foundNamespace != null && foundNamespace.equals(namespaceURI)) {
                            return localname;
                        }
                    }

                }
            }
        }
        final NodeImpl ancestor = (NodeImpl) getElementAncestor(this);

        if (ancestor != null) {
            return ancestor.lookupNamespacePrefix(namespaceURI, el);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEqualNode(final Node arg) {
        if (arg == this) {
            return true;
        }
        if (arg.getNodeType() != getNodeType()) {
            return false;
        }
        // in theory nodeName can't be null but better be careful
        // who knows what other implementations may be doing?...
        if (getNodeName() == null) {
            if (arg.getNodeName() != null) {
                return false;
            }
        }
        else if (!getNodeName().equals(arg.getNodeName())) {
            return false;
        }

        if (getLocalName() == null) {
            if (arg.getLocalName() != null) {
                return false;
            }
        }
        else if (!getLocalName().equals(arg.getLocalName())) {
            return false;
        }

        if (getNamespaceURI() == null) {
            if (arg.getNamespaceURI() != null) {
                return false;
            }
        }
        else if (!getNamespaceURI().equals(arg.getNamespaceURI())) {
            return false;
        }

        if (getPrefix() == null) {
            if (arg.getPrefix() != null) {
                return false;
            }
        }
        else if (!getPrefix().equals(arg.getPrefix())) {
            return false;
        }

        if (getNodeValue() == null) {
            if (arg.getNodeValue() != null) {
                return false;
            }
        }
        else if (!getNodeValue().equals(arg.getNodeValue())) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getFeature(final String feature, final String version) {
        // we don't have any alternate node, either this node does the job
        // or we don't have anything that does
        return isSupported(feature, version) ? this : null;
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

    /**
     * Denotes that this node has changed.
     */
    protected void changed() {
        // we do not actually store this information on every node, we only
        // have a global indicator on the Document. Doing otherwise cost us too
        // much for little gain.
        ownerDocument().changed();
    }

    /**
     * Returns the number of changes to this node.
     *
     * @return the number of changes
     */
    protected int changes() {
        // we do not actually store this information on every node, we only
        // have a global indicator on the Document. Doing otherwise cost us too
        // much for little gain.
        return ownerDocument().changes();
    }

    final boolean needsSyncChildren() {
        return (flags_ & SYNCCHILDREN) != 0;
    }

    public final void needsSyncChildren(final boolean value) {
        flags_ = value ? flags_ | SYNCCHILDREN : flags_ & ~SYNCCHILDREN;
    }

    final boolean isOwned() {
        return (flags_ & OWNED) != 0;
    }

    final void isOwned(final boolean value) {
        flags_ = value ? flags_ | OWNED : flags_ & ~OWNED;
    }

    final boolean isFirstChild() {
        return (flags_ & FIRSTCHILD) != 0;
    }

    final void isFirstChild(final boolean value) {
        flags_ = value ? flags_ | FIRSTCHILD : flags_ & ~FIRSTCHILD;
    }

    final boolean isSpecified() {
        return (flags_ & SPECIFIED) != 0;
    }

    final void isSpecified(final boolean value) {
        flags_ = value ? flags_ | SPECIFIED : flags_ & ~SPECIFIED;
    }

    final boolean hasStringValue() {
        return (flags_ & HASSTRING) != 0;
    }

    final void hasStringValue(final boolean value) {
        flags_ = value ? flags_ | HASSTRING : flags_ & ~HASSTRING;
    }

    final boolean isNormalized() {
        return (flags_ & NORMALIZED) != 0;
    }

    final void isNormalized(final boolean value) {
        // See if flag should propagate to parent.
        if (!value && isNormalized() && ownerNode_ != null) {
            ownerNode_.isNormalized(false);
        }
        flags_ = value ? flags_ | NORMALIZED : flags_ & ~NORMALIZED;
    }

    final boolean isIdAttribute() {
        return (flags_ & ID) != 0;
    }

    final void isIdAttribute(final boolean value) {
        flags_ = value ? flags_ | ID : flags_ & ~ID;
    }

    // NON-DOM method for debugging convenience.
    @Override
    public String toString() {
        return "[" + getNodeName() + ": " + getNodeValue() + "]";
    }
}
