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
import org.htmlunit.cyberneko.xerces.util.DOMMessageFormatter;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * NamedNodeMaps represent collections of Nodes that can be accessed by name.
 * Entity and Notation nodes are stored in NamedNodeMaps attached to the
 * DocumentType. Attributes are placed in a NamedNodeMap attached to the elem
 * they're related too. However, because attributes require more work, such as
 * firing mutation events, they are stored in a subclass of NamedNodeMapImpl.
 * <P>
 * Only one Node may be stored per name; attempting to store another will
 * replace the previous value.
 * <P>
 * NOTE: The "primary" storage key is taken from the NodeName attribute of the
 * node. The "secondary" storage key is the namespaceURI and localName, when
 * accessed by DOM level 2 nodes. All nodes, even DOM Level 2 nodes are stored
 * in a single ArrayList sorted by the primary "nodename" key.
 * <P>
 * NOTE: item()'s integer index does _not_ imply that the named nodes must be
 * stored in an array; that's only an access method. Note too that these indices
 * are "live"; if someone changes the map's contents, the indices associated
 * with nodes may change.
 */
public class NamedNodeMapImpl implements NamedNodeMap {

    protected short flags;

    protected static final short READONLY = 0x1 << 0;
    protected static final short CHANGED = 0x1 << 1;
    protected static final short HASDEFAULTS = 0x1 << 2;

    /** Nodes. */
    protected List<Node> nodes;

    protected final NodeImpl ownerNode; // the node this map belongs to

    /**
     * Constructs a named node map.
     *
     * @param ownerNode the owner node
     */
    protected NamedNodeMapImpl(final NodeImpl ownerNode) {
        this.ownerNode = ownerNode;
    }

    /**
     * {@inheritDoc}
     *
     * Report how many nodes are currently stored in this NamedNodeMap. Caveat: This
     * is a count rather than an index, so the highest-numbered node at any time can
     * be accessed via item(getLength()-1).
     */
    @Override
    public int getLength() {
        return (nodes != null) ? nodes.size() : 0;
    }

    /**
     * {@inheritDoc}
     *
     * Retrieve an item from the map by 0-based index.
     *
     * @param index Which item to retrieve. Note that indices are just an
     *              enumeration of the current contents; they aren't guaranteed to
     *              be stable, nor do they imply any promises about the order of the
     *              NamedNodeMap's contents. In other words, DO NOT assume either
     *              that index(i) will always refer to the same entry, or that there
     *              is any stable ordering of entries... and be prepared for
     *              double-reporting or skips as insertion and deletion occur.
     *
     * @return the node which currenly has the specified index, or null if index is
     *         greater than or equal to getLength().
     */
    @Override
    public Node item(final int index) {
        return (nodes != null && index < nodes.size()) ? nodes.get(index) : null;
    }

    /**
     * {@inheritDoc}
     *
     * Retrieve a node by name.
     *
     * @param name Name of a node to look up.
     * @return the Node (of unspecified sub-class) stored with that name, or null if
     *         no value has been assigned to that name.
     */
    @Override
    public Node getNamedItem(final String name) {
        final int i = findNamePoint(name);
        return (i < 0) ? null : nodes.get(i);

    }

    /**
     * {@inheritDoc}
     *
     * Introduced in DOM Level 2.
     * <p>
     * Retrieves a node specified by local name and namespace URI.
     *
     * @param namespaceURI The namespace URI of the node to retrieve. When it is
     *                     null or an empty string, this method behaves like
     *                     getNamedItem.
     * @param localName    The local name of the node to retrieve.
     * @return Node A Node (of any type) with the specified name, or null if the
     *         specified name did not identify any node in the map.
     */
    @Override
    public Node getNamedItemNS(final String namespaceURI, final String localName) {
        final int i = findNamePoint(namespaceURI, localName);
        return (i < 0) ? null : nodes.get(i);

    }

    /**
     * {@inheritDoc}
     *
     * Adds a node using its nodeName attribute. As the nodeName attribute is used
     * to derive the name which the node must be stored under, multiple nodes of
     * certain types (those that have a "special" string value) cannot be stored as
     * the names would clash. This is seen as preferable to allowing nodes to be
     * aliased.
     *
     * @see org.w3c.dom.NamedNodeMap#setNamedItem
     * @return If the new Node replaces an existing node the replaced Node is
     *         returned, otherwise null is returned.
     * @param arg A node to store in a named node map. The node will later be
     *            accessible using the value of the namespaceURI and localName
     *            attribute of the node. If a node with those namespace URI and
     *            local name is already present in the map, it is replaced by the
     *            new one.
     * @exception org.w3c.dom.DOMException The exception description.
     */
    @Override
    public Node setNamedItem(final Node arg) throws DOMException {
        final CoreDocumentImpl ownerDocument = ownerNode.ownerDocument();
        if (ownerDocument.errorChecking) {
            if (arg.getOwnerDocument() != ownerDocument) {
                final String msg = DOMMessageFormatter.formatMessage("WRONG_DOCUMENT_ERR", null);
                throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, msg);
            }
        }

        int i = findNamePoint(arg.getNodeName());
        NodeImpl previous = null;
        if (i >= 0) {
            previous = (NodeImpl) nodes.get(i);
            nodes.set(i, arg);
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
        return previous;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node setNamedItemNS(final Node arg) throws DOMException {

        final CoreDocumentImpl ownerDocument = ownerNode.ownerDocument();
        if (ownerDocument.errorChecking) {
            if (arg.getOwnerDocument() != ownerDocument) {
                final String msg = DOMMessageFormatter.formatMessage("WRONG_DOCUMENT_ERR", null);
                throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, msg);
            }
        }

        int i = findNamePoint(arg.getNamespaceURI(), arg.getLocalName());
        NodeImpl previous = null;
        if (i >= 0) {
            previous = (NodeImpl) nodes.get(i);
            nodes.set(i, arg);
        }
        else {
            // If we can't find by namespaceURI, localName, then we find by
            // nodeName so we know where to insert.
            i = findNamePoint(arg.getNodeName());
            if (i >= 0) {
                previous = (NodeImpl) nodes.get(i);
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
        return previous;

    }

    /**
     * {@inheritDoc}
     *
     * Removes a node specified by name.
     *
     * @param name The name of a node to remove.
     * @return The node removed from the map if a node with such a name exists.
     */
    @Override
    public Node removeNamedItem(final String name) throws DOMException {
        final int i = findNamePoint(name);
        if (i < 0) {
            final String msg = DOMMessageFormatter.formatMessage("NOT_FOUND_ERR", null);
            throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
        }

        final NodeImpl n = (NodeImpl) nodes.get(i);
        nodes.remove(i);

        return n;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node removeNamedItemNS(final String namespaceURI, final String name) throws DOMException {
        final int i = findNamePoint(namespaceURI, name);
        if (i < 0) {
            final String msg = DOMMessageFormatter.formatMessage("NOT_FOUND_ERR", null);
            throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
        }

        final NodeImpl n = (NodeImpl) nodes.get(i);
        nodes.remove(i);

        return n;

    }

    /**
     * Cloning a NamedNodeMap is a DEEP OPERATION; it always clones all the nodes
     * contained in the map.
     *
     * @param ownerNode the owner node
     * @return the cloned map
     */
    public NamedNodeMapImpl cloneMap(final NodeImpl ownerNode) {
        final NamedNodeMapImpl newmap = new NamedNodeMapImpl(ownerNode);
        newmap.cloneContent(this);
        return newmap;
    }

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
                for (int i = 0; i < size; ++i) {
                    final NodeImpl n = (NodeImpl) srcmap.nodes.get(i);
                    final NodeImpl clone = (NodeImpl) n.cloneNode(true);
                    clone.isSpecified(n.isSpecified());
                    nodes.add(clone);
                }
            }
        }
    }

    /**
     * NON-DOM set the ownerDocument of this node, and the attributes it contains.
     *
     * @param doc the doc
     */
    protected void setOwnerDocument(final CoreDocumentImpl doc) {
        if (nodes != null) {
            final int size = nodes.size();
            for (int i = 0; i < size; ++i) {
                ((NodeImpl) item(i)).setOwnerDocument(doc);
            }
        }
    }

    final boolean changed() {
        return (flags & CHANGED) != 0;
    }

    final void changed(final boolean value) {
        flags = (short) (value ? flags | CHANGED : flags & ~CHANGED);
    }

    final boolean hasDefaults() {
        return (flags & HASDEFAULTS) != 0;
    }

    final void hasDefaults(final boolean value) {
        flags = (short) (value ? flags | HASDEFAULTS : flags & ~HASDEFAULTS);
    }

    /**
     * Subroutine: Locate the named item, or the point at which said item should be
     * added.
     *
     * @param name  Name of a node to look up.
     *
     * @return If positive or zero, the index of the found item. If negative, index
     *         of the appropriate point at which to insert the item, encoded as
     *         -1-index and hence reconvertable by subtracting it from -1. (Encoding
     *         because I don't want to recompare the strings but don't want to burn
     *         bytes on a datatype to hold a flagged value.)
     */
    protected int findNamePoint(final String name) {

        // Binary search
        int i = 0;
        if (nodes != null) {
            int first = 0;
            int last = nodes.size() - 1;

            while (first <= last) {
                i = (first + last) / 2;
                final int test = name.compareTo((nodes.get(i)).getNodeName());
                if (test == 0) {
                    return i; // Name found
                }
                else if (test < 0) {
                    last = i - 1;
                }
                else {
                    first = i + 1;
                }
            }

            if (first > i) {
                i = first;
            }
        }

        return -1 - i; // not-found has to be encoded.
    }

    protected int findNamePoint(final String namespaceURI, final String name) {
        // This findNamePoint is for DOM Level 2 Namespaces.

        if ((nodes == null) || (name == null)) {
            return -1;
        }

        // This is a linear search through the same nodes ArrayList.
        // The ArrayList is sorted on the DOM Level 1 nodename.
        // The DOM Level 2 NS keys are namespaceURI and Localname,
        // so we must linear search thru it.
        // In addition, to get this to work with nodes without any namespace
        // (namespaceURI and localNames are both null) we then use the nodeName
        // as a secondary key.
        final int size = nodes.size();
        for (int i = 0; i < size; ++i) {
            final NodeImpl a = (NodeImpl) nodes.get(i);
            final String aNamespaceURI = a.getNamespaceURI();
            final String aLocalName = a.getLocalName();
            if (namespaceURI == null) {
                if (aNamespaceURI == null && (name.equals(aLocalName)
                        || (aLocalName == null && name.equals(a.getNodeName())))) {
                    return i;
                }
            }
            else {
                if (namespaceURI.equals(aNamespaceURI) && name.equals(aLocalName)) {
                    return i;
                }
            }
        }
        return -1;
    }

    // compare 2 nodes in the map. If a precedes b, return true, otherwise
    // return false
    protected boolean precedes(final Node a, final Node b) {
        if (nodes != null) {
            for (final Node node : nodes) {
                if (node == a) {
                    return true;
                }
                if (node == b) {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * NON-DOM remove all elements from this map.
     */
    public void removeAll() {
        if (nodes != null) {
            nodes.clear();
        }
    }
}
