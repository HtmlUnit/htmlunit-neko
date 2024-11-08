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

import java.util.ArrayList;

import org.htmlunit.cyberneko.util.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class implements the DOM's NodeList behavior for
 * Element.getElementsByTagName()
 * <P>
 * The DOM describes NodeList as follows:
 * <P>
 * 1) It may represent EITHER nodes scattered through a subtree (when returned
 * by Element.getElementsByTagName), or just the immediate children (when
 * returned by Node.getChildNodes). The latter is easy, but the former (which
 * this class addresses) is more challenging.
 * <P>
 * 2) Its behavior is "live" -- that is, it always reflects the current state of
 * the document tree. To put it another way, the NodeLists obtained before and
 * after a series of insertions and deletions are effectively identical (as far
 * as the user is concerned, the former has been dynamically updated as the
 * changes have been made).
 * <P>
 * 3) Its API accesses individual nodes via an integer index, with the listed
 * nodes numbered sequentially in the order that they were found during a
 * preorder depth-first left-to-right search of the tree. (Of course in the case
 * of getChildNodes, depth is not involved.) As nodes are inserted or deleted in
 * the tree, and hence the NodeList, the numbering of nodes that follow them in
 * the NodeList will change.
 * <P>
 * It is rather painful to support the latter two in the getElementsByTagName
 * case. The current solution is for Nodes to maintain a change count
 * (eventually that may be a Digest instead), which the NodeList tracks and uses
 * to invalidate itself.
 * <P>
 * Unfortunately, this does _not_ respond efficiently in the case that the
 * dynamic behavior was supposed to address: scanning a tree while it is being
 * extended. That requires knowing which subtrees have changed, which can become
 * an arbitrarily complex problem.
 * <P>
 * We save some work by filling the ArrayList only as we access the item()s...
 * but I suspect the same users who demanded index-based access will also start
 * by doing a getLength() to control their loop, blowing this optimization out
 * of the water.
 * <P>
 * NOTE: Level 2 of the DOM will probably _not_ use NodeList for its extended
 * search mechanisms, partly for the reasons just discussed.
 */
public class DeepNodeListImpl implements NodeList {

    protected final NodeImpl rootNode_; // Where the search started
    protected final String tagName_; // Or "*" to mean all-tags-acceptable
    private int changes_ = 0;
    private ArrayList<Node> nodes_;

    private String nsName_;
    private boolean enableNS_ = false;

    /**
     * Constructor.
     *
     * @param rootNode the root node
     * @param tagName  the tag name
     */
    public DeepNodeListImpl(final NodeImpl rootNode, final String tagName) {
        rootNode_ = rootNode;
        tagName_ = tagName;
        nodes_ = new ArrayList<>();
    }

    /**
     * Constructor for Namespace support.
     *
     * @param rootNode the root node
     * @param nsName   the namespace
     * @param tagName  the tag name
     */
    public DeepNodeListImpl(final NodeImpl rootNode, final String nsName, final String tagName) {
        this(rootNode, tagName);
        nsName_ = (nsName != null && nsName.length() != 0) ? nsName : null;
        enableNS_ = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLength() {
        // Preload all matching elements. (Stops when we run out of subtree!)
        item(java.lang.Integer.MAX_VALUE);
        return nodes_.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node item(final int index) {
        Node thisNode;

        // Tree changed. Do it all from scratch!
        if (rootNode_.changes() != changes_) {
            nodes_ = new ArrayList<>();
            changes_ = rootNode_.changes();
        }

        // In the cache
        final int currentSize = nodes_.size();
        if (index < currentSize) {
            return nodes_.get(index);
        }

        // Not yet seen

        // Pick up where we left off (Which may be the beginning)
        if (currentSize == 0) {
            thisNode = rootNode_;
        }
        else {
            thisNode = nodes_.get(currentSize - 1);
        }

        // Add nodes up to the one we're looking for
        while (thisNode != null && index >= nodes_.size()) {
            thisNode = nextMatchingElementAfter(thisNode);
            if (thisNode != null) {
                nodes_.add(thisNode);
            }
        }

        // Either what we want, or null (not avail.)
        return thisNode;

    }

    /**
     * Iterative tree-walker. When you have a Parent link, there's often no need to
     * resort to recursion. NOTE THAT only Element nodes are matched since we're
     * specifically supporting getElementsByTagName().
     *
     * @param current the current node
     * @return next node
     */
    protected Node nextMatchingElementAfter(Node current) {
        Node next;
        while (current != null) {
            // Look down to first child.
            if (current.hasChildNodes()) {
                current = current.getFirstChild();
            }

            // Look right to sibling (but not from root!)
            else if (current != rootNode_ && null != (next = current.getNextSibling())) {
                current = next;
            }

            // Look up and right (but not past root!)
            else {
                next = null;
                for ( ; current != rootNode_; // Stop when we return to starting point
                        current = current.getParentNode()) {

                    next = current.getNextSibling();
                    if (next != null) {
                        break;
                    }
                }
                current = next;
            }

            // Have we found an Element with the right tagName?
            // ("*" matches anything.)
            if (current != rootNode_ && current != null && current.getNodeType() == Node.ELEMENT_NODE) {
                if (!enableNS_) {
                    if (StringUtils.equalsChar('*', tagName_) || ((ElementImpl) current).getTagName().equals(tagName_)) {
                        return current;
                    }
                }
                else {
                    // DOM2: Namespace logic.
                    if (StringUtils.equalsChar('*', tagName_)) {
                        if (StringUtils.equalsChar('*', nsName_)) {
                            return current;
                        }

                        final ElementImpl el = (ElementImpl) current;
                        if ((nsName_ == null && el.getNamespaceURI() == null)
                                || (nsName_ != null && nsName_.equals(el.getNamespaceURI()))) {
                            return current;
                        }
                    }
                    else {
                        final ElementImpl el = (ElementImpl) current;
                        if (el.getLocalName() != null && el.getLocalName().equals(tagName_)) {
                            if (StringUtils.equalsChar('*', nsName_)) {
                                return current;
                            }

                            if ((nsName_ == null && el.getNamespaceURI() == null)
                                    || (nsName_ != null && nsName_.equals(el.getNamespaceURI()))) {
                                return current;
                            }
                        }
                    }
                }
            }

            // Otherwise continue walking the tree
        }

        // Fell out of tree-walk; no more instances found
        return null;

    }
}
