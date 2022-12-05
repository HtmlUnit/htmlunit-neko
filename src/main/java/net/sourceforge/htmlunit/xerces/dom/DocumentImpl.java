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

import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventException;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.MutationEvent;
import org.w3c.dom.ranges.DocumentRange;
import org.w3c.dom.ranges.Range;


/**
 * The Document interface represents the entire HTML or XML document.
 * Conceptually, it is the root of the document tree, and provides the
 * primary access to the document's data.
 * <P>
 * Since elements, text nodes, comments, processing instructions,
 * etc. cannot exist outside the context of a Document, the Document
 * interface also contains the factory methods needed to create these
 * objects. The Node objects created have a ownerDocument attribute
 * which associates them with the Document within whose context they
 * were created.
 * <p>
 * The DocumentImpl class also implements the DOM Level 2 DocumentTraversal
 * interface. This interface is comprised of factory methods needed to
 * create NodeIterators and TreeWalkers. The process of creating NodeIterator
 * objects also adds these references to this document.
 * After finishing with an iterator it is important to remove the object
 * using the remove methods in this implementation. This allows the release of
 * the references from the iterator objects to the DOM Nodes.
 * <p>
 * <b>Note:</b> When any node in the document is serialized, the
 * entire document is serialized along with it.
 *
 * @xerces.internal
 *
 * @author Arnaud  Le Hors, IBM
 * @author Joe Kesselman, IBM
 * @author Andy Clark, IBM
 * @author Ralf Pfeiffer, IBM
 * @version $Id$
 * @since  PR-DOM-Level-1-19980818.
 */
public class DocumentImpl
    extends CoreDocumentImpl
    implements DocumentEvent, DocumentRange {

    //
    // Constants
    //

    /** Serialization version. */
    static final long serialVersionUID = 515687835542616694L;

    //
    // Data
    //

    /** Reference queue for cleared Node Iterator references */
    protected transient ReferenceQueue iteratorReferenceQueue;

    /** Ranges */
    protected transient List ranges;

    /** Reference queue for cleared Range references */
    protected transient ReferenceQueue rangeReferenceQueue;

    //
    // Constructors
    //

    /**
     * NON-DOM: Actually creating a Document is outside the DOM's spec,
     * since it has to operate in terms of a particular implementation.
     */
    public DocumentImpl() {
        super();
    }

    /** Constructor. */
    public DocumentImpl(boolean grammarAccess) {
        super(grammarAccess);
    }

    /**
     * For DOM2 support.
     * The createDocument factory method is in DOMImplementation.
     */
    public DocumentImpl(DocumentType doctype)
    {
        super(doctype);
    }

    /** For DOM2 support. */
    public DocumentImpl(DocumentType doctype, boolean grammarAccess) {
        super(doctype, grammarAccess);
    }

    //
    // Node methods
    //

    /**
     * Deep-clone a document, including fixing ownerDoc for the cloned
     * children. Note that this requires bypassing the WRONG_DOCUMENT_ERR
     * protection. I've chosen to implement it by calling importNode
     * which is DOM Level 2.
     *
     * @return org.w3c.dom.Node
     * @param deep boolean, iff true replicate children
     */
    @Override
    public Node cloneNode(boolean deep) {

        DocumentImpl newdoc = new DocumentImpl();
        callUserDataHandlers(this, newdoc, UserDataHandler.NODE_CLONED);
        cloneNode(newdoc, deep);

        return newdoc;

    } // cloneNode(boolean):Node

    /**
     * Retrieve information describing the abilities of this particular
     * DOM implementation. Intended to support applications that may be
     * using DOMs retrieved from several different sources, potentially
     * with different underlying representations.
     */
    @Override
    public DOMImplementation getImplementation() {
        // Currently implemented as a singleton, since it's hardcoded
        // information anyway.
        return DOMImplementationImpl.getDOMImplementation();
    }

    /**
     * Remove stale references from the given list.
     */
    private void removeStaleReferences(ReferenceQueue queue, List list) {
        Reference ref = queue.poll();
        int count = 0;
        while (ref != null) {
            ++count;
            ref = queue.poll();
        }
        if (count > 0) {
            final Iterator i = list.iterator();
            while (i.hasNext()) {
                Object o = ((Reference) i.next()).get();
                if (o == null) {
                    i.remove();
                    if (--count <= 0) {
                        return;
                    }
                }
            }
        }
    }

    //
    // DocumentRange methods
    //
    /**
     */
    @Override
    public Range createRange() {

        if (ranges == null) {
            ranges = new LinkedList();
            rangeReferenceQueue = new ReferenceQueue();
        }

        Range range = new RangeImpl(this);

        removeStaleRangeReferences();
        ranges.add(new WeakReference(range, rangeReferenceQueue));

        return range;

    }

    /** Not a client function. Called by Range.detach(),
     *  so a Range can remove itself from the list of
     *  Ranges.
     */
    void removeRange(Range range) {

        if ((range == null) || (ranges == null)) return;

        removeStaleRangeReferences();
        Iterator i = ranges.iterator();
        while (i.hasNext()) {
            Object otherRange = ((Reference) i.next()).get();
            if (otherRange == range) {
                i.remove();
                return;
            }
            // Remove stale reference from the list.
            else if (otherRange == null) {
                i.remove();
            }
        }
    }

    /**
     * A method to be called when some text was changed in a text node,
     * so that live objects can be notified.
     */
    @Override
    void replacedText(CharacterDataImpl node) {
        // notify ranges
        if (ranges != null) {
            notifyRangesReplacedText(node);
        }
    }

    private void notifyRangesReplacedText(CharacterDataImpl node) {
        removeStaleRangeReferences();
        final Iterator i = ranges.iterator();
        while (i.hasNext()) {
            RangeImpl range = (RangeImpl) ((Reference) i.next()).get();
            if (range != null) {
                range.receiveReplacedText(node);
            }
            // Remove stale reference from the list.
            else {
                i.remove();
            }
        }
    }

    /**
     * A method to be called when some text was deleted from a text node,
     * so that live objects can be notified.
     */
    @Override
    void deletedText(CharacterDataImpl node, int offset, int count) {
        // notify ranges
        if (ranges != null) {
            notifyRangesDeletedText(node, offset, count);
        }
    }

    private void notifyRangesDeletedText(CharacterDataImpl node, int offset, int count) {
        removeStaleRangeReferences();
        final Iterator i = ranges.iterator();
        while (i.hasNext()) {
            RangeImpl range = (RangeImpl) ((Reference) i.next()).get();
            if (range != null) {
                range.receiveDeletedText(node, offset, count);
            }
            // Remove stale reference from the list.
            else {
                i.remove();
            }
        }
    }

    /**
     * A method to be called when some text was inserted into a text node,
     * so that live objects can be notified.
     */
    @Override
    void insertedText(CharacterDataImpl node, int offset, int count) {
        // notify ranges
        if (ranges != null) {
            notifyRangesInsertedText(node, offset, count);
        }
    }

    private void notifyRangesInsertedText(CharacterDataImpl node, int offset, int count) {
        removeStaleRangeReferences();
        final Iterator i = ranges.iterator();
        while (i.hasNext()) {
            RangeImpl range = (RangeImpl) ((Reference) i.next()).get();
            if (range != null) {
                range.receiveInsertedText(node, offset, count);
            }
            // Remove stale reference from the list.
            else {
                i.remove();
            }
        }
    }

    /**
     * A method to be called when a text node has been split,
     * so that live objects can be notified.
     */
    void splitData(Node node, Node newNode, int offset) {
        // notify ranges
        if (ranges != null) {
            notifyRangesSplitData(node, newNode, offset);
        }
    }

    private void notifyRangesSplitData(Node node, Node newNode, int offset) {
        removeStaleRangeReferences();
        final Iterator i = ranges.iterator();
        while (i.hasNext()) {
            RangeImpl range = (RangeImpl) ((Reference) i.next()).get();
            if (range != null) {
                range.receiveSplitData(node, newNode, offset);
            }
            // Remove stale reference from the list.
            else {
                i.remove();
            }
        }
    }

    /**
     * Remove stale range references from the range list.
     */
    private void removeStaleRangeReferences() {
        removeStaleReferences(rangeReferenceQueue, ranges);
    }

    //
    // DocumentEvent methods
    //

    /**
     * Introduced in DOM Level 2. Optional. <p>
     * Create and return Event objects.
     *
     * @param type The eventType parameter specifies the type of Event
     * interface to be created.  If the Event interface specified is supported
     * by the implementation this method will return a new Event of the
     * interface type requested. If the Event is to be dispatched via the
     * dispatchEvent method the appropriate event init method must be called
     * after creation in order to initialize the Event's values.  As an
     * example, a user wishing to synthesize some kind of Event would call
     * createEvent with the parameter "Events". The initEvent method could then
     * be called on the newly created Event to set the specific type of Event
     * to be dispatched and set its context information.
     * @return Newly created Event
     * @exception DOMException NOT_SUPPORTED_ERR: Raised if the implementation
     * does not support the type of Event interface requested
     * @since WD-DOM-Level-2-19990923
     */
    @Override
    public Event createEvent(String type) throws DOMException {
        String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }

    /**
     * Sets whether the DOM implementation generates mutation events
     * upon operations.
     */
    @Override
    void setMutationEvents(boolean set) {
    }

    /**
     * Returns true if the DOM implementation generates mutation events.
     */
    @Override
    boolean getMutationEvents() {
        return false;
    }

    //
    // Constants
    //

    /*
     * NON-DOM INTERNAL: Class LEntry is just a struct used to represent
     * event listeners registered with this node. Copies of this object
     * are hung from the nodeListeners Vector.
     * <p>
     * I considered using two vectors -- one for capture,
     * one for bubble -- but decided that since the list of listeners
     * is probably short in most cases, it might not be worth spending
     * the space. ***** REVISIT WHEN WE HAVE MORE EXPERIENCE.
     */
    class LEntry implements Serializable {

        private static final long serialVersionUID = -8426757059492421631L;
        final String type;
        final EventListener listener;
        final boolean useCapture;

        /** NON-DOM INTERNAL: Constructor for Listener list Entry
         * @param type Event name (NOT event group!) to listen for.
         * @param listener Who gets called when event is dispatched
         * @param useCaptue True iff listener is registered on
         *  capturing phase rather than at-target or bubbling
         */
        LEntry(String type, EventListener listener, boolean useCapture)
        {
            this.type = type;
            this.listener = listener;
            this.useCapture = useCapture;
        }

    } // LEntry

    /**
     * NON-DOM INTERNAL: Return object for getEnclosingAttr. Carries
     * (two values, the Attr node affected (if any) and its previous
     * string value. Simple struct, no methods.
     */
    class EnclosingAttr implements Serializable {
        private static final long serialVersionUID = 5208387723391647216L;
        AttrImpl node;
        String oldvalue;
    }

    EnclosingAttr savedEnclosingAttr;

    /**
     * A method to be called when a character data node has been modified
     */
    @Override
    void modifyingCharacterData(NodeImpl node, boolean replace) {
    }

    /**
     * A method to be called when a character data node has been modified
     */
    @Override
    void modifiedCharacterData(NodeImpl node, String oldvalue, String value, boolean replace) {
    }

    /**
     * A method to be called when a character data node has been replaced
     */
    @Override
    void replacedCharacterData(NodeImpl node, String oldvalue, String value) {
        //now that we have finished replacing data, we need to perform the same actions
        //that are required after a character data node has been modified
        //send the value of false for replace parameter so that mutation
        //events if appropriate will be initiated
        modifiedCharacterData(node, oldvalue, value, false);
    }

    /**
     * A method to be called when a node is about to be inserted in the tree.
     */
    @Override
    void insertingNode(NodeImpl node, boolean replace) {
    }

    /**
     * A method to be called when a node has been inserted in the tree.
     */
    @Override
    void insertedNode(NodeImpl node, NodeImpl newInternal, boolean replace) {
        // notify the range of insertions
        if (ranges != null) {
            notifyRangesInsertedNode(newInternal);
        }
    }

    private void notifyRangesInsertedNode(NodeImpl newInternal) {
        removeStaleRangeReferences();
        final Iterator i = ranges.iterator();
        while (i.hasNext()) {
            RangeImpl range = (RangeImpl) ((Reference) i.next()).get();
            if (range != null) {
                range.insertedNodeFromDOM(newInternal);
            }
            // Remove stale reference from the list.
            else {
                i.remove();
            }
        }
    }

    /**
     * A method to be called when a node is about to be removed from the tree.
     */
    @Override
    void removingNode(NodeImpl node, NodeImpl oldChild, boolean replace) {

        // notify ranges
        if (ranges != null) {
            notifyRangesRemovingNode(oldChild);
        }
    }

    private void notifyRangesRemovingNode(NodeImpl oldChild) {
        removeStaleRangeReferences();
        final Iterator i = ranges.iterator();
        while (i.hasNext()) {
            RangeImpl range = (RangeImpl) ((Reference) i.next()).get();
            if (range != null) {
                range.removeNode(oldChild);
            }
            // Remove stale reference from the list.
            else {
                i.remove();
            }
        }
    }

    /**
     * A method to be called when a node has been removed from the tree.
     */
    @Override
    void removedNode(NodeImpl node, boolean replace) {
    }

    /**
     * A method to be called when a node is about to be replaced in the tree.
     */
    @Override
    void replacingNode(NodeImpl node) {
    }

    /**
     * A method to be called when character data is about to be replaced in the tree.
     */
    @Override
    void replacingData (NodeImpl node) {
    }

    /**
     * A method to be called when a node has been replaced in the tree.
     */
    @Override
    void replacedNode(NodeImpl node) {
    }

    /**
     * A method to be called when an attribute value has been modified
     */
    @Override
    void modifiedAttrValue(AttrImpl attr, String oldvalue) {
    }

    /**
     * A method to be called when an attribute node has been set
     */
    @Override
    void setAttrNode(AttrImpl attr, AttrImpl previous) {
    }

    /**
     * A method to be called when an attribute node has been removed
     */
    @Override
    void removedAttrNode(AttrImpl attr, NodeImpl oldOwner, String name) {
    }

    /**
     * A method to be called when an attribute node has been renamed
     */
    @Override
    void renamedAttrNode(Attr oldAt, Attr newAt) {
    // REVISIT: To be implemented!!!
    }

    /**
     * A method to be called when an element has been renamed
     */
    @Override
    void renamedElement(Element oldEl, Element newEl) {
    // REVISIT: To be implemented!!!
    }

} // class DocumentImpl
