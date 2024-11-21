/*
 * Copyright (c) 2017-2024 Ronald Brill
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
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

/**
 * Attribute represents an XML-style attribute of an Element. Typically, the
 * allowable values are controlled by its declaration in the Document Type
 * Definition (DTD) governing this kind of document.
 * <P>
 * If the attribute has not been explicitly assigned a value, but has been
 * declared in the DTD, it will exist and have that default. Only if neither the
 * document nor the DTD specifies a value will the Attribute really be
 * considered absent and have no value; in that case, querying the attribute
 * will return null.
 * <P>
 * Attributes may have multiple children that contain their data. (XML allows
 * attributes to contain entity references, and tokenized attribute types such
 * as NMTOKENS may have a child for each token.) For convenience, the Attribute
 * object's getValue() method returns the string version of the attribute's
 * value.
 * <P>
 * Attributes are not children of the Elements they belong to, in the usual
 * sense, and have no valid Parent reference. However, the spec says they _do_
 * belong to a specific Element, and an INUSE exception is to be thrown if the
 * user attempts to explicitly share them between elements.
 * <P>
 * Note that Elements do not permit attributes to appear to be shared (see the
 * INUSE exception), so this object's mutability is officially not an issue.
 * <p>
 * Note: The ownerNode attribute is used to store the Element the Attr node is
 * associated with. Attr nodes do not have parent nodes. Besides, the
 * getOwnerElement() method can be used to get the element node this attribute
 * is associated with.
 * <P>
 * AttrImpl does not support Namespaces. AttrNSImpl, which inherits from it,
 * does.
 *
 * <p>
 * AttrImpl used to inherit from ParentNode. It now directly inherits from
 * NodeImpl and provide its own implementation of the ParentNode's behavior. The
 * reason is that we now try and avoid to always create a Text node to hold the
 * value of an attribute. The DOM spec requires it, so we still have to do it in
 * case getFirstChild() is called for instance. The reason attribute values are
 * stored as a list of nodes is so that they can carry more than a simple
 * string. They can also contain EntityReference nodes. However, most of the
 * times people only have a single string that they only set and get through
 * Element.set/getAttribute or Attr.set/getValue. In this new version, the Attr
 * node has a value pointer which can either be the String directly or a pointer
 * to the first ChildNode. A flag tells which one it currently is. Note that
 * while we try to stick with the direct String as much as possible once we've
 * switched to a node there is no going back. This is because we have no way to
 * know whether the application keeps referring to the node we once returned.
 * <p>
 * The gain in memory varies on the density of attributes in the document. But
 * in the tests I've run I've seen up to 12% of memory gain. And the good thing
 * is that it also leads to a slight gain in speed because we allocate fewer
 * objects! I mean, that's until we have to actually create the node...
 * <p>
 * To avoid too much duplicated code, I got rid of ParentNode and renamed
 * ChildAndParentNode, which I never really liked, to ParentNode for simplicity,
 * this doesn't make much of a difference in memory usage because there are only
 * very few objects that are only a Parent. This is only true now because
 * AttrImpl now inherits directly from NodeImpl and has its own implementation
 * of the ParentNode's node behavior. So there is still some duplicated code
 * there.
 * <p>
 * This class doesn't directly support mutation events, however, it notifies the
 * document when mutations are performed so that the document class do so.
 *
 * <p>
 * <b>WARNING</b>: Some of the code here is partially duplicated in ParentNode,
 * be careful to keep these two classes in sync!
 * <p>
 *
 * @see AttrNSImpl
 *
 * @author Arnaud Le Hors, IBM
 * @author Joe Kesselman, IBM
 * @author Andy Clark, IBM
 */
public class AttrImpl extends NodeImpl implements Attr, TypeInfo {

    /** DTD namespace. */
    protected static final String DTD_URI = "http://www.w3.org/TR/REC-xml";

    /** This can either be a String or the first child node. */
    private Object value_;

    /** Attribute name. */
    protected String name;

    /** Type information */
    protected String type;

    /**
     * Attribute has no public constructor. Please use the factory method in the
     * Document class.
     *
     * @param ownerDocument the owner document
     * @param name          the name
     */
    protected AttrImpl(final CoreDocumentImpl ownerDocument, final String name) {
        super(ownerDocument);
        this.name = name;
        /* False for default attributes. */
        isSpecified(true);
        hasStringValue(true);
    }

    // Support for DOM Level 3 renameNode method.
    // Note: This only deals with part of the pb. It is expected to be
    // called after the Attr has been detached for one thing.
    // CoreDocumentImpl does all the work.
    void rename(final String name) {
        this.name = name;
    }

    // create a real text node as child if we don't have one yet
    protected void makeChildNode() {
        if (hasStringValue()) {
            if (value_ != null) {
                final TextImpl text = (TextImpl) ownerDocument().createTextNode((String) value_);
                value_ = text;
                text.isFirstChild(true);
                text.previousSibling_ = text;
                text.ownerNode_ = this;
                text.isOwned(true);
            }
            hasStringValue(false);
        }
    }

    /**
     * NON-DOM set the ownerDocument of this node and its children
     */
    @Override
    protected void setOwnerDocument(final CoreDocumentImpl doc) {
        super.setOwnerDocument(doc);
        if (!hasStringValue()) {
            for (ChildNode child = (ChildNode) value_; child != null; child = child.nextSibling_) {
                child.setOwnerDocument(doc);
            }
        }
    }

    /**
     * DOM Level 3: isId {@inheritDoc}
     */
    @Override
    public boolean isId() {
        // REVISIT: should an attribute that is not in the tree return
        // isID true?
        return isIdAttribute();
    }

    @Override
    public Node cloneNode(final boolean deep) {
        final AttrImpl clone = (AttrImpl) super.cloneNode(deep);

        // take care of case where there are kids
        if (!clone.hasStringValue()) {
            // Need to break the association w/ original kids
            clone.value_ = null;

            // Cloning an Attribute always clones its children,
            // since they represent its value, no matter whether this
            // is a deep clone or not
            for (Node child = (Node) value_; child != null; child = child.getNextSibling()) {
                clone.appendChild(child.cloneNode(true));
            }
        }
        clone.isSpecified(true);
        return clone;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getNodeType() {
        return Node.ATTRIBUTE_NODE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNodeName() {
        return name;
    }

    /**
     * {@inheritDoc} Implicit in the rerouting of getNodeValue to getValue is the
     * need to redefine setNodeValue, for symmetry's sake. Note that since we're
     * explicitly providing a value, Specified should be set true.... even if that
     * value equals the default.
     */
    @Override
    public void setNodeValue(final String value) throws DOMException {
        setValue(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTypeName() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTypeNamespace() {
        if (type != null) {
            return DTD_URI;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TypeInfo getSchemaTypeInfo() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNodeValue() {
        return getValue();
    }

    /**
     * {@inheritDoc} In Attributes, NodeName is considered a synonym for the
     * attribute's Name
     */
    @Override
    public String getName() {
        return name;

    }

    /**
     * {@inheritDoc} The DOM doesn't clearly define what setValue(null) means. I've
     * taken it as "remove all children", which from outside should appear similar
     * to setting it to the empty string.
     */
    @Override
    public void setValue(final String newvalue) {
        final CoreDocumentImpl ownerDocument = ownerDocument();
        final Element ownerElement = getOwnerElement();

        if (value_ != null) {
            final String oldvalue;

            if (hasStringValue()) {
                oldvalue = (String) value_;
            }
            else {
                // simply discard children if any
                oldvalue = getValue();
                // remove ref from first child to last child
                final ChildNode firstChild = (ChildNode) value_;
                firstChild.previousSibling_ = null;
                firstChild.isFirstChild(false);
                firstChild.ownerNode_ = ownerDocument;
            }
            // then remove ref to current value
            value_ = null;
            needsSyncChildren(false);

            if (isIdAttribute() && ownerElement != null) {
                ownerDocument.removeIdentifier(oldvalue);
            }
        }

        // Create and add the new one, generating only non-aggregate events
        // (There are no listeners on the new Text, but there may be
        // capture/bubble listeners on the Attr.
        // Note that aggregate events are NOT dispatched here,
        // since we need to combine the remove and insert.
        isSpecified(true);
        // directly store the string
        value_ = newvalue;
        hasStringValue(true);
        changed();

        if (isIdAttribute() && ownerElement != null) {
            ownerDocument.putIdentifier(newvalue, ownerElement);
        }

    }

    /**
     * {@inheritDoc} The "string value" of an Attribute is its text representation,
     * which in turn is a concatenation of the string values of its children.
     */
    @Override
    public String getValue() {
        if (value_ == null) {
            return "";
        }
        if (hasStringValue()) {
            return (String) value_;
        }

        final ChildNode firstChild = (ChildNode) value_;

        String data;
        if (firstChild.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
            data = ((EntityReferenceImpl) firstChild).getEntityRefValue();
        }
        else {
            data = firstChild.getNodeValue();
        }

        ChildNode node = firstChild.nextSibling_;

        if (node == null || data == null) {
            return (data == null) ? "" : data;
        }

        final StringBuilder v = new StringBuilder(data);
        while (node != null) {
            if (node.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
                data = ((EntityReferenceImpl) node).getEntityRefValue();
                if (data == null) {
                    return "";
                }
                v.append(data);
            }
            else {
                v.append(node.getNodeValue());
            }
            node = node.nextSibling_;
        }
        return v.toString();

    }

    /**
     * {@inheritDoc} The "specified" flag is true if and only if this attribute's
     * value was explicitly specified in the original document. Note that the
     * implementation, not the user, is in charge of this property. If the user
     * asserts an Attribute value (even if it ends up having the same value as the
     * default), it is considered a specified attribute. If you really want to
     * revert to the default, delete the attribute from the Element, and the
     * Implementation will re-assert the default (if any) in its place, with the
     * appropriate specified=false setting.
     */
    @Override
    public boolean getSpecified() {
        return isSpecified();
    }

    /**
     * {@inheritDoc} Returns the element node that this attribute is associated
     * with, or null if the attribute has not been added to an element.
     */
    @Override
    public Element getOwnerElement() {
        // if we have an owner, ownerNode is our ownerElement, otherwise it's
        // our ownerDocument and we don't have an ownerElement
        return (Element) (isOwned() ? ownerNode_ : null);
    }

    // NON-DOM, for use by parser
    public void setSpecified(final boolean arg) {
        isSpecified(arg);

    }

    // NON-DOM: used by the parser
    public void setType(final String type) {
        this.type = type;
    }

    // NON-DOM method for debugging convenience
    @Override
    public String toString() {
        return getName() + "=" + "\"" + getValue() + "\"";
    }

    /**
     * {@inheritDoc} Test whether this node has any children. Convenience shorthand
     * for (Node.getFirstChild()!=null)
     */
    @Override
    public boolean hasChildNodes() {
        return value_ != null;
    }

    /**
     * {@inheritDoc} Obtain a NodeList enumerating all children of this node. If
     * there are none, an (initially) empty NodeList is returned.
     * <p>
     * NodeLists are "live"; as children are added/removed the NodeList will
     * immediately reflect those changes. Also, the NodeList refers to the actual
     * nodes, so changes to those nodes made via the DOM tree will be reflected in
     * the NodeList and vice versa.
     * <p>
     * In this implementation, Nodes implement the NodeList interface and provide
     * their own getChildNodes() support. Other DOMs may solve this differently.
     */
    @Override
    public NodeList getChildNodes() {
        // JKESS: KNOWN ISSUE HERE
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getFirstChild() {
        makeChildNode();
        return (Node) value_;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getLastChild() {
        return lastChild();
    }

    final ChildNode lastChild() {
        // last child is stored as the previous sibling of first child
        makeChildNode();
        return value_ != null ? ((ChildNode) value_).previousSibling_ : null;
    }

    /**
     * {@inheritDoc}
     *
     * Move one or more node(s) to our list of children. Note that this implicitly
     * removes them from their previous parent.
     *
     * @param newChild The Node to be moved to our subtree. As a convenience
     *                 feature, inserting a DocumentNode will instead insert all its
     *                 children.
     *
     * @param refChild Current child which newChild should be placed immediately
     *                 before. If refChild is null, the insertion occurs after all
     *                 existing Nodes, like appendChild().
     *
     * @return newChild, in its new state (relocated, or emptied in the case of
     *         DocumentNode.)
     *
     * @throws DOMException HIERARCHY_REQUEST_ERR if newChild is of a type that
     *                      shouldn't be a child of this node, or if newChild is an
     *                      ancestor of this node.
     *
     * @throws DOMException WRONG_DOCUMENT_ERR if newChild has a different owner
     *                      document than we do.
     *
     * @throws DOMException NOT_FOUND_ERR if refChild is not a child of this node.
     *
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR if this node is read-only.
     */
    @Override
    public Node insertBefore(final Node newChild, final Node refChild) throws DOMException {
        // Tail-call; optimizer should be able to do good things with.
        return internalInsertBefore(newChild, refChild, false);
    }

    // NON-DOM INTERNAL: Within DOM actions,we sometimes need to be able
    // to control which mutation events are spawned. This version of the
    // insertBefore operation allows us to do so. It is not intended
    // for use by application programs.
    Node internalInsertBefore(final Node newChild, Node refChild, final boolean replace) throws DOMException {

        final CoreDocumentImpl ownerDocument = ownerDocument();
        final boolean errorChecking = ownerDocument.errorChecking;

        if (newChild.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE) {
            // SLOW BUT SAFE: We could insert the whole subtree without
            // juggling so many next/previous pointers. (Wipe out the
            // parent's child-list, patch the parent pointers, set the
            // ends of the list.) But we know some subclasses have special-
            // case behavior they add to insertBefore(), so we don't risk it.
            // This approch also takes fewer bytecodes.

            // NOTE: If one of the children is not a legal child of this
            // node, throw HIERARCHY_REQUEST_ERR before _any_ of the children
            // have been transferred. (Alternative behaviors would be to
            // reparent up to the first failure point or reparent all those
            // which are acceptable to the target node, neither of which is
            // as robust. PR-DOM-0818 isn't entirely clear on which it
            // recommends?????

            // No need to check kids for right-document; if they weren't,
            // they wouldn't be kids of that DocFrag.
            if (errorChecking) {
                for (Node kid = newChild.getFirstChild(); // Prescan
                        kid != null; kid = kid.getNextSibling()) {

                    if (!ownerDocument.isKidOK(this, kid)) {
                        final String msg = DOMMessageFormatter.formatMessage("HIERARCHY_REQUEST_ERR", null);
                        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, msg);
                    }
                }
            }

            while (newChild.hasChildNodes()) {
                insertBefore(newChild.getFirstChild(), refChild);
            }
            return newChild;
        }

        if (newChild == refChild) {
            // stupid case that must be handled as a no-op triggering events...
            refChild = refChild.getNextSibling();
            removeChild(newChild);
            insertBefore(newChild, refChild);
            return newChild;
        }

        if (errorChecking) {
            if (newChild.getOwnerDocument() != ownerDocument) {
                final String msg = DOMMessageFormatter.formatMessage("WRONG_DOCUMENT_ERR", null);
                throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, msg);
            }
            if (!ownerDocument.isKidOK(this, newChild)) {
                final String msg = DOMMessageFormatter.formatMessage("HIERARCHY_REQUEST_ERR", null);
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, msg);
            }
            // refChild must be a child of this node (or null)
            if (refChild != null && refChild.getParentNode() != this) {
                final String msg = DOMMessageFormatter.formatMessage("NOT_FOUND_ERR", null);
                throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
            }

            // Prevent cycles in the tree
            // newChild cannot be ancestor of this Node,
            // and actually cannot be this
            boolean treeSafe = true;
            for (NodeImpl a = this; treeSafe && a != null; a = a.parentNode()) {
                treeSafe = newChild != a;
            }
            if (!treeSafe) {
                final String msg = DOMMessageFormatter.formatMessage("HIERARCHY_REQUEST_ERR", null);
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, msg);
            }
        }

        makeChildNode(); // make sure we have a node and not a string

        // notify document
        ownerDocument.insertingNode(this, replace);

        // Convert to internal type, to avoid repeated casting
        final ChildNode newInternal = (ChildNode) newChild;

        final Node oldparent = newInternal.parentNode();
        if (oldparent != null) {
            oldparent.removeChild(newInternal);
        }

        // Convert to internal type, to avoid repeated casting
        final ChildNode refInternal = (ChildNode) refChild;

        // Attach up
        newInternal.ownerNode_ = this;
        newInternal.isOwned(true);

        // Attach before and after
        // Note: firstChild.previousSibling == lastChild!!
        final ChildNode firstChild = (ChildNode) value_;
        if (firstChild == null) {
            // this our first and only child
            value_ = newInternal; // firstchild = newInternal;
            newInternal.isFirstChild(true);
            newInternal.previousSibling_ = newInternal;
        }
        else {
            if (refInternal == null) {
                // this is an append
                final ChildNode lastChild = firstChild.previousSibling_;
                lastChild.nextSibling_ = newInternal;
                newInternal.previousSibling_ = lastChild;
                firstChild.previousSibling_ = newInternal;
            }
            else {
                // this is an insert
                if (refChild == firstChild) {
                    // at the head of the list
                    firstChild.isFirstChild(false);
                    newInternal.nextSibling_ = firstChild;
                    newInternal.previousSibling_ = firstChild.previousSibling_;
                    firstChild.previousSibling_ = newInternal;
                    value_ = newInternal; // firstChild = newInternal;
                    newInternal.isFirstChild(true);
                }
                else {
                    // somewhere in the middle
                    final ChildNode prev = refInternal.previousSibling_;
                    newInternal.nextSibling_ = refInternal;
                    prev.nextSibling_ = newInternal;
                    refInternal.previousSibling_ = newInternal;
                    newInternal.previousSibling_ = prev;
                }
            }
        }

        changed();

        // notify document
        ownerDocument.insertedNode(this, newInternal, replace);

        checkNormalizationAfterInsert(newInternal);

        return newChild;
    }

    /**
     * Remove a child from this Node. The removed child's subtree remains intact so
     * it may be re-inserted elsewhere.
     *
     * @return oldChild, in its new state (removed).
     *
     * @throws DOMException NOT_FOUND_ERR if oldChild is not a child of this node.
     *
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR if this node is read-only.
     */
    @Override
    public Node removeChild(final Node oldChild) throws DOMException {
        // Tail-call, should be optimizable
        if (hasStringValue()) {
            // we don't have any child per say so it can't be one of them!
            final String msg = DOMMessageFormatter.formatMessage("NOT_FOUND_ERR", null);
            throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
        }
        return internalRemoveChild(oldChild, false);
    }

    /**
     * NON-DOM INTERNAL: Within DOM actions,we sometimes need to be able to control
     * which mutation events are spawned. This version of the removeChild operation
     * allows us to do so. It is not intended for use by application programs.
     */
    Node internalRemoveChild(final Node oldChild, final boolean replace) throws DOMException {

        final         CoreDocumentImpl ownerDocument = ownerDocument();
        if (ownerDocument.errorChecking) {
            if (oldChild != null && oldChild.getParentNode() != this) {
                final String msg = DOMMessageFormatter.formatMessage("NOT_FOUND_ERR", null);
                throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
            }
        }

        final ChildNode oldInternal = (ChildNode) oldChild;

        // notify document
        ownerDocument.removingNode(this, oldInternal, replace);

        // Patch linked list around oldChild
        // Note: lastChild == firstChild.previousSibling
        if (oldInternal == value_) { // oldInternal == firstChild
            // removing first child
            oldInternal.isFirstChild(false);
            // next line is: firstChild = oldInternal.nextSibling
            value_ = oldInternal.nextSibling_;
            final ChildNode firstChild = (ChildNode) value_;
            if (firstChild != null) {
                firstChild.isFirstChild(true);
                firstChild.previousSibling_ = oldInternal.previousSibling_;
            }
        }
        else {
            final ChildNode prev = oldInternal.previousSibling_;
            final ChildNode next = oldInternal.nextSibling_;
            prev.nextSibling_ = next;
            if (next == null) {
                // removing last child
                final ChildNode firstChild = (ChildNode) value_;
                firstChild.previousSibling_ = prev;
            }
            else {
                // removing some other child in the middle
                next.previousSibling_ = prev;
            }
        }

        // Save previous sibling for normalization checking.
        final ChildNode oldPreviousSibling = oldInternal.previousSibling();

        // Remove oldInternal's references to tree
        oldInternal.ownerNode_ = ownerDocument;
        oldInternal.isOwned(false);
        oldInternal.nextSibling_ = null;
        oldInternal.previousSibling_ = null;

        changed();

        // notify document
        ownerDocument.removedNode(this, replace);

        checkNormalizationAfterRemove(oldPreviousSibling);

        return oldInternal;
    }

    /**
     * Make newChild occupy the location that oldChild used to have. Note that
     * newChild will first be removed from its previous parent, if any. Equivalent
     * to inserting newChild before oldChild, then removing oldChild.
     *
     * @return oldChild, in its new state (removed).
     *
     * @throws DOMException HIERARCHY_REQUEST_ERR if newChild is of a type that
     *                      shouldn't be a child of this node, or if newChild is one
     *                      of our ancestors.
     *
     * @throws DOMException WRONG_DOCUMENT_ERR if newChild has a different owner
     *                      document than we do.
     *
     * @throws DOMException NOT_FOUND_ERR if oldChild is not a child of this node.
     *
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR if this node is read-only.
     */
    @Override
    public Node replaceChild(final Node newChild, final Node oldChild) throws DOMException {

        makeChildNode();

        // If Mutation Events are being generated, this operation might
        // throw aggregate events twice when modifying an Attr -- once
        // on insertion and once on removal. DOM Level 2 does not specify
        // this as either desirable or undesirable, but hints that
        // aggregations should be issued only once per user request.

        // notify document
        final CoreDocumentImpl ownerDocument = ownerDocument();
        ownerDocument.replacingNode(this);

        internalInsertBefore(newChild, oldChild, true);
        if (newChild != oldChild) {
            internalRemoveChild(oldChild, true);
        }

        // notify document
        ownerDocument.replacedNode(this);

        return oldChild;
    }

    /**
     * NodeList method: Count the immediate children of this node
     *
     * @return int
     */
    @Override
    public int getLength() {

        if (hasStringValue()) {
            return 1;
        }
        ChildNode node = (ChildNode) value_;
        int length = 0;
        for ( ; node != null; node = node.nextSibling_) {
            length++;
        }
        return length;
    }

    /**
     * NodeList method: Return the Nth immediate child of this node, or null if the
     * index is out of bounds.
     *
     * @return org.w3c.dom.Node
     * @param index int
     */
    @Override
    public Node item(final int index) {

        if (hasStringValue()) {
            if (index != 0 || value_ == null) {
                return null;
            }
            makeChildNode();
            return (Node) value_;
        }
        if (index < 0) {
            return null;
        }

        ChildNode node = (ChildNode) value_;
        for (int i = 0; i < index && node != null; i++) {
            node = node.nextSibling_;
        }
        return node;

    }

    /**
     * DOM Level 3 WD- Experimental. Override inherited behavior from ParentNode to
     * support deep equal. isEqualNode is always deep on Attr nodes.
     */
    @Override
    public boolean isEqualNode(final Node arg) {
        return super.isEqualNode(arg);
    }

    /**
     * Introduced in DOM Level 3.
     * <p>
     * Checks if a type is derived from another by restriction. See:
     * http://www.w3.org/TR/DOM-Level-3-Core/core.html#TypeInfo-isDerivedFrom
     *
     * @param typeNamespaceArg The namspace of the ancestor type declaration
     * @param typeNameArg      The name of the ancestor type declaration
     * @param derivationMethod The derivation method
     *
     * @return boolean True if the type is derived by restriciton for the reference
     *         type
     */
    @Override
    public boolean isDerivedFrom(final String typeNamespaceArg, final String typeNameArg, final int derivationMethod) {
        return false;
    }

    /**
     * Checks the normalized state of this node after inserting a child. If the
     * inserted child causes this node to be unnormalized, then this node is flagged
     * accordingly. The conditions for changing the normalized state are:
     * <ul>
     * <li>The inserted child is a text node and one of its adjacent siblings is
     * also a text node.
     * <li>The inserted child is is itself unnormalized.
     * </ul>
     *
     * @param insertedChild the child node that was inserted into this node
     *
     * @throws NullPointerException if the inserted child is <code>null</code>
     */
    void checkNormalizationAfterInsert(final ChildNode insertedChild) {
        // See if insertion caused this node to be unnormalized.
        if (insertedChild.getNodeType() == Node.TEXT_NODE) {
            final ChildNode prev = insertedChild.previousSibling();
            final ChildNode next = insertedChild.nextSibling_;
            // If an adjacent sibling of the new child is a text node,
            // flag this node as unnormalized.
            if ((prev != null && prev.getNodeType() == Node.TEXT_NODE)
                    || (next != null && next.getNodeType() == Node.TEXT_NODE)) {
                isNormalized(false);
            }
        }
        else {
            // If the new child is not normalized,
            // then this node is inherently not normalized.
            if (!insertedChild.isNormalized()) {
                isNormalized(false);
            }
        }
    }

    /**
     * Checks the normalized of this node after removing a child. If the removed
     * child causes this node to be unnormalized, then this node is flagged
     * accordingly. The conditions for changing the normalized state are:
     * <ul>
     * <li>The removed child had two adjacent siblings that were text nodes.
     * </ul>
     *
     * @param previousSibling the previous sibling of the removed child, or
     *                        <code>null</code>
     */
    void checkNormalizationAfterRemove(final ChildNode previousSibling) {
        // See if removal caused this node to be unnormalized.
        // If the adjacent siblings of the removed child were both text nodes,
        // flag this node as unnormalized.
        if (previousSibling != null && previousSibling.getNodeType() == Node.TEXT_NODE) {

            final ChildNode next = previousSibling.nextSibling_;
            if (next != null && next.getNodeType() == Node.TEXT_NODE) {
                isNormalized(false);
            }
        }
    }
}
