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

import java.util.List;

import org.htmlunit.cyberneko.util.SimpleArrayList;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 * AttributeMap inherits from NamedNodeMapImpl and extends it to deal with the
 * specifics of storing attributes. These are:
 * <ul>
 * <li>managing ownership of attribute nodes
 * <li>managing default attributes
 * <li>firing mutation events
 * </ul>
 * <p>
 * This class doesn't directly support mutation events, however, it notifies the
 * document when mutations are performed so that the document class do so.
 *
 */
public class AttributeMap extends NamedNodeMapImpl {

    /**
     * Constructs a named node map.
     *
     * @param ownerNode the owner node
     */
    protected AttributeMap(final ElementImpl ownerNode) {
        super(ownerNode);
    }

    /**
     * Adds an attribute using its nodeName attribute.
     *
     * @see org.w3c.dom.NamedNodeMap#setNamedItem
     * @return If the new Node replaces an existing node the replaced Node is
     *         returned, otherwise null is returned.
     * @param arg An Attr node to store in this map.
     * @exception org.w3c.dom.DOMException The exception description.
     */
    @Override
    public Node setNamedItem(final Node arg) throws DOMException {

        final boolean errCheck = ownerNode.ownerDocument().errorChecking;
        if (errCheck) {
            if (arg.getOwnerDocument() != ownerNode.ownerDocument()) {
                final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null);
                throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, msg);
            }
            if (arg.getNodeType() != Node.ATTRIBUTE_NODE) {
                final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "HIERARCHY_REQUEST_ERR", null);
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, msg);
            }
        }
        final AttrImpl argn = (AttrImpl) arg;

        if (argn.isOwned()) {
            if (errCheck && argn.getOwnerElement() != ownerNode) {
                final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INUSE_ATTRIBUTE_ERR", null);
                throw new DOMException(DOMException.INUSE_ATTRIBUTE_ERR, msg);
            }
            // replacing an Attribute with itself does nothing
            return arg;
        }

        // set owner
        argn.ownerNode_ = ownerNode;
        argn.isOwned(true);

        int i = findNamePoint(argn.getNodeName());
        AttrImpl previous = null;
        if (i >= 0) {
            previous = (AttrImpl) nodes.get(i);
            nodes.set(i, arg);
            previous.ownerNode_ = ownerNode.ownerDocument();
            previous.isOwned(false);
            // make sure it won't be mistaken with defaults in case it's reused
            previous.isSpecified(true);
        }
        else {
            i = -1 - i; // Insert point (may be end of list)
            if (null == nodes) {
                nodes = new SimpleArrayList<>(1);
                nodes.add(arg);
            }
            else {
                nodes.add(i, arg);
            }
        }

        // notify document
        ownerNode.ownerDocument().setAttrNode(argn, previous);

        // If the new attribute is not normalized,
        // the owning element is inherently not normalized.
        if (!argn.isNormalized()) {
            ownerNode.isNormalized(false);
        }
        return previous;

    }

    /**
     * Adds an attribute using its namespaceURI and localName.
     *
     * @see org.w3c.dom.NamedNodeMap#setNamedItem
     * @return If the new Node replaces an existing node the replaced Node is
     *         returned, otherwise null is returned.
     * @param arg A node to store in a named node map.
     */
    @Override
    public Node setNamedItemNS(final Node arg) throws DOMException {

        final boolean errCheck = ownerNode.ownerDocument().errorChecking;
        if (errCheck) {
            if (arg.getOwnerDocument() != ownerNode.ownerDocument()) {
                final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null);
                throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, msg);
            }
            if (arg.getNodeType() != Node.ATTRIBUTE_NODE) {
                final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "HIERARCHY_REQUEST_ERR", null);
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, msg);
            }
        }
        final AttrImpl argn = (AttrImpl) arg;

        if (argn.isOwned()) {
            if (errCheck && argn.getOwnerElement() != ownerNode) {
                final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INUSE_ATTRIBUTE_ERR", null);
                throw new DOMException(DOMException.INUSE_ATTRIBUTE_ERR, msg);
            }
            // replacing an Attribute with itself does nothing
            return arg;
        }

        // set owner
        argn.ownerNode_ = ownerNode;
        argn.isOwned(true);

        int i = findNamePoint(argn.getNamespaceURI(), argn.getLocalName());
        AttrImpl previous = null;
        if (i >= 0) {
            previous = (AttrImpl) nodes.get(i);
            nodes.set(i, arg);
            previous.ownerNode_ = ownerNode.ownerDocument();
            previous.isOwned(false);
            // make sure it won't be mistaken with defaults in case it's reused
            previous.isSpecified(true);
        }
        else {
            // If we can't find by namespaceURI, localName, then we find by
            // nodeName so we know where to insert.
            i = findNamePoint(arg.getNodeName());
            if (i >= 0) {
                previous = (AttrImpl) nodes.get(i);
                nodes.add(i, arg);
            }
            else {
                i = -1 - i; // Insert point (may be end of list)
                if (null == nodes) {
                    nodes = new SimpleArrayList<>(1);
                    nodes.add(arg);
                }
                else {
                    nodes.add(i, arg);
                }
            }
        }
        // changed(true);

        // notify document
        ownerNode.ownerDocument().setAttrNode(argn, previous);

        // If the new attribute is not normalized,
        // the owning element is inherently not normalized.
        if (!argn.isNormalized()) {
            ownerNode.isNormalized(false);
        }
        return previous;

    }

    /**
     * Removes an attribute specified by name.
     *
     * @param name The name of a node to remove. If the removed attribute is known
     *             to have a default value, an attribute immediately appears
     *             containing the default value as well as the corresponding
     *             namespace URI, local name, and prefix when applicable.
     * @return The node removed from the map if a node with such a name exists.
     * @throws DOMException NOT_FOUND_ERR: Raised if there is no node named name in
     *                      the map.
     */
    @Override
    public Node removeNamedItem(final String name) throws DOMException {
        return internalRemoveNamedItem(name, true);
    }

    /**
     * NON-DOM: Remove the node object
     * <p>
     * NOTE: Specifically removes THIS NODE -- not the node with this name, nor the
     * node with these contents. If node does not belong to this named node map, we
     * throw a DOMException.
     *
     * @param item The node to remove
     * @return Removed node
     * @throws DOMException on error
     */
    protected Node removeItem(final Node item) throws DOMException {

        int index = -1;
        if (nodes != null) {
            final int size = nodes.size();
            for (int i = 0; i < size; ++i) {
                if (nodes.get(i) == item) {
                    index = i;
                    break;
                }
            }
        }
        if (index < 0) {
            final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null);
            throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
        }

        return remove((AttrImpl) item, index);
    }

    /**
     * Internal removeNamedItem method allowing to specify whether an exception must
     * be thrown if the specified name is not found.
     *
     * @param name    the mane
     * @param raiseEx if true raise an exception
     * @return the node
     */
    protected final Node internalRemoveNamedItem(final String name, final boolean raiseEx) {
        final int i = findNamePoint(name);
        if (i < 0) {
            if (raiseEx) {
                final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null);
                throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
            }

            return null;
        }

        return remove((AttrImpl) nodes.get(i), i);
    }

    private Node remove(final AttrImpl attr, final int index) {
        final CoreDocumentImpl ownerDocument = ownerNode.ownerDocument();
        final String name = attr.getNodeName();
        if (attr.isIdAttribute()) {
            ownerDocument.removeIdentifier(attr.getValue());
        }

        nodes.remove(index);

        // remove reference to owner
        attr.ownerNode_ = ownerDocument;
        attr.isOwned(false);

        // make sure it won't be mistaken with defaults in case it's reused
        attr.isSpecified(true);
        attr.isIdAttribute(false);

        // notify document
        ownerDocument.removedAttrNode(attr, ownerNode, name);

        return attr;
    }

    /**
     * Introduced in DOM Level 2.
     * <p>
     * Removes an attribute specified by local name and namespace URI.
     *
     * @param namespaceURI The namespace URI of the node to remove. When it is null
     *                     or an empty string, this method behaves like
     *                     removeNamedItem.
     * @param name         The local name of the node to remove. If the removed
     *                     attribute is known to have a default value, an attribute
     *                     immediately appears containing the default value.
     * @return Node The node removed from the map if a node with such a local name
     *         and namespace URI exists.
     * @throws DOMException NOT_FOUND_ERR: Raised if there is no node named name in
     *                      the map.
     */
    @Override
    public Node removeNamedItemNS(final String namespaceURI, final String name) throws DOMException {
        return internalRemoveNamedItemNS(namespaceURI, name, true);
    }

    /**
     * Internal removeNamedItemNS method allowing to specify whether an exception
     * must be thrown if the specified local name and namespace URI is not found.
     *
     * @param namespaceURI the namespace uri
     * @param name         the name
     * @param raiseEx      if true raise exception
     * @return the node
     */
    protected final Node internalRemoveNamedItemNS(final String namespaceURI, final String name, final boolean raiseEx) {

        final CoreDocumentImpl ownerDocument = ownerNode.ownerDocument();
        final int i = findNamePoint(namespaceURI, name);
        if (i < 0) {
            if (raiseEx) {
                final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null);
                throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
            }

            return null;
        }

        final AttrImpl n = (AttrImpl) nodes.get(i);

        if (n.isIdAttribute()) {
            ownerDocument.removeIdentifier(n.getValue());
        }

        nodes.remove(i);

        // changed(true);

        // remove reference to owner
        n.ownerNode_ = ownerDocument;
        n.isOwned(false);
        // make sure it won't be mistaken with defaults in case it's
        // reused
        n.isSpecified(true);
        // update id table if needed
        n.isIdAttribute(false);

        // notify document
        ownerDocument.removedAttrNode(n, ownerNode, name);

        return n;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NamedNodeMapImpl cloneMap(final NodeImpl ownerNode) {
        // Cloning a NamedNodeMap is a DEEP OPERATION; it always clones
        // all the nodes contained in the map.
        final AttributeMap newmap = new AttributeMap((ElementImpl) ownerNode);
        newmap.hasDefaults(hasDefaults());
        newmap.cloneContent(this);
        return newmap;
    }

    /**
     * Override parent's method to set the ownerNode correctly
     *
     * @param srcmap the source map
     */
    @Override
    protected void cloneContent(final NamedNodeMapImpl srcmap) {
        final List<Node> srcnodes = srcmap.nodes;
        if (srcnodes != null) {
            final int size = srcnodes.size();
            if (size != 0) {
                if (nodes == null) {
                    nodes = new SimpleArrayList<>(size);
                }
                else {
                    nodes.clear();
                }
                for (final Object srcnode : srcnodes) {
                    final NodeImpl n = (NodeImpl) srcnode;
                    final NodeImpl clone = (NodeImpl) n.cloneNode(true);
                    clone.isSpecified(n.isSpecified());
                    nodes.add(clone);
                    clone.ownerNode_ = ownerNode;
                    clone.isOwned(true);
                }
            }
        }
    }

    /**
     * Move specified attributes from the given map to this one
     *
     * @param srcmap the source map
     */
    void moveSpecifiedAttributes(final AttributeMap srcmap) {
        final int nsize = (srcmap.nodes != null) ? srcmap.nodes.size() : 0;
        for (int i = nsize - 1; i >= 0; i--) {
            final AttrImpl attr = (AttrImpl) srcmap.nodes.get(i);
            if (attr.isSpecified()) {
                srcmap.remove(attr, i);
                if (attr.getLocalName() != null) {
                    setNamedItem(attr);
                }
                else {
                    setNamedItemNS(attr);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final int addItem(final Node arg) {

        final AttrImpl argn = (AttrImpl) arg;

        // set owner
        argn.ownerNode_ = ownerNode;
        argn.isOwned(true);

        int i = findNamePoint(argn.getNamespaceURI(), argn.getLocalName());
        if (i >= 0) {
            nodes.set(i, arg);
        }
        else {
            // If we can't find by namespaceURI, localName, then we find by
            // nodeName so we know where to insert.
            i = findNamePoint(argn.getNodeName());
            if (i >= 0) {
                nodes.add(i, arg);
            }
            else {
                i = -1 - i; // Insert point (may be end of list)
                if (null == nodes) {
                    nodes = new SimpleArrayList<>(1);
                    nodes.add(arg);
                }
                else {
                    nodes.add(i, arg);
                }
            }
        }

        // notify document
        ownerNode.ownerDocument().setAttrNode(argn, null);
        return i;
    }
}
