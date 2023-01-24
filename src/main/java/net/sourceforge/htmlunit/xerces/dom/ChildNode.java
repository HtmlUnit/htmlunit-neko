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

import org.w3c.dom.Node;

/**
 * ChildNode inherits from NodeImpl and adds the capability of being a child by
 * having references to its previous and next siblings.
 *
 */
public abstract class ChildNode extends NodeImpl {

    /** Previous sibling. */
    protected ChildNode previousSibling;

    /** Next sibling. */
    protected ChildNode nextSibling;

    /**
     * No public constructor; only subclasses of Node should be instantiated, and
     * those normally via a Document's factory methods
     * <p>
     * Every Node knows what Document it belongs to.
     * 
     * @param ownerDocument the owner document
     */
    protected ChildNode(CoreDocumentImpl ownerDocument) {
        super(ownerDocument);
    }

    /**
     * {@inheritDoc}
     *
     * Returns a duplicate of a given node. You can consider this a generic "copy
     * constructor" for nodes. The newly returned object should be completely
     * independent of the source object's subtree, so changes in one after the clone
     * has been made will not affect the other.
     * <P>
     * Note: since we never have any children deep is meaningless here, ParentNode
     * overrides this behavior.
     * 
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
    public Node cloneNode(boolean deep) {

        ChildNode newnode = (ChildNode) super.cloneNode(deep);

        // Need to break the association w/ original kids
        newnode.previousSibling = null;
        newnode.nextSibling = null;
        newnode.isFirstChild(false);

        return newnode;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getParentNode() {
        // if we have an owner, ownerNode is our parent, otherwise it's
        // our ownerDocument and we don't have a parent
        return isOwned() ? ownerNode : null;
    }

    /*
     * {@inheritDoc}
     */
    @Override
    final NodeImpl parentNode() {
        // if we have an owner, ownerNode is our parent, otherwise it's
        // our ownerDocument and we don't have a parent
        return isOwned() ? ownerNode : null;
    }

    /**
     * {@inheritDoc} The next child of this node's parent, or null if none
     */
    @Override
    public Node getNextSibling() {
        return nextSibling;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getPreviousSibling() {
        // if we are the firstChild, previousSibling actually refers to our
        // parent's lastChild, but we hide that
        return isFirstChild() ? null : previousSibling;
    }

    /*
     * {@inheritDoc}
     */
    @Override
    final ChildNode previousSibling() {
        // if we are the firstChild, previousSibling actually refers to our
        // parent's lastChild, but we hide that
        return isFirstChild() ? null : previousSibling;
    }
}
