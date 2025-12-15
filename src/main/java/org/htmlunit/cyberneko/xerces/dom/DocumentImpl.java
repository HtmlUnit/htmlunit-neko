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

import org.htmlunit.cyberneko.xerces.util.DOMMessageFormatter;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;

/**
 * The Document interface represents the entire HTML or XML document.
 * Conceptually, it is the root of the document tree, and provides the primary
 * access to the document's data.
 * <P>
 * Since elements, text nodes, comments, processing instructions, etc. cannot
 * exist outside the context of a Document, the Document interface also contains
 * the factory methods needed to create these objects. The Node objects created
 * have a ownerDocument attribute which associates them with the Document within
 * whose context they were created.
 *
 * @author Arnaud Le Hors, IBM
 * @author Joe Kesselman, IBM
 * @author Andy Clark, IBM
 * @author Ralf Pfeiffer, IBM
 */
public class DocumentImpl extends CoreDocumentImpl implements DocumentEvent {

    /**
     * NON-DOM: Actually creating a Document is outside the DOM's spec, since it has
     * to operate in terms of a particular implementation.
     */
    public DocumentImpl() {
        super();
    }

    /**
     * For DOM2 support. The createDocument factory method is in DOMImplementation.
     *
     * @param doctype the {@link DocumentType}
     */
    public DocumentImpl(final DocumentType doctype) {
        super(doctype);
    }

    /**
     * {@inheritDoc}
     *
     * Deep-clone a document, including fixing ownerDoc for the cloned children.
     * Note that this requires bypassing the WRONG_DOCUMENT_ERR protection. I've
     * chosen to implement it by calling importNode which is DOM Level 2.
     *
     * @return org.w3c.dom.Node
     * @param deep boolean, iff true replicate children
     */
    @Override
    public Node cloneNode(final boolean deep) {

        final DocumentImpl newdoc = new DocumentImpl();
        cloneNode(newdoc, deep);

        return newdoc;

    }

    /**
     * {@inheritDoc}
     *
     * Retrieve information describing the abilities of this particular DOM
     * implementation. Intended to support applications that may be using DOMs
     * retrieved from several different sources, potentially with different
     * underlying representations.
     */
    @Override
    public DOMImplementation getImplementation() {
        // Currently implemented as a singleton, since it's hardcoded
        // information anyway.
        return DOMImplementationImpl.getDOMImplementation();
    }

    /**
     * {@inheritDoc}
     *
     * A method to be called when some text was changed in a text node, so that live
     * objects can be notified.
     */
    @Override
    void replacedText(final CharacterDataImpl node) {
    }

    /**
     * {@inheritDoc}
     *
     * A method to be called when some text was deleted from a text node, so that
     * live objects can be notified.
     */
    @Override
    void deletedText(final CharacterDataImpl node, final int offset, final int count) {
    }

    /**
     * {@inheritDoc}
     *
     * A method to be called when some text was inserted into a text node, so that
     * live objects can be notified.
     */
    @Override
    void insertedText(final CharacterDataImpl node, final int offset, final int count) {
    }

    /**
     * {@inheritDoc}
     *
     * Introduced in DOM Level 2. Optional.
     * <p>
     * Create and return Event objects.
     *
     * @param type The eventType parameter specifies the type of Event interface to
     *             be created. If the Event interface specified is supported by the
     *             implementation this method will return a new Event of the
     *             interface type requested. If the Event is to be dispatched via
     *             the dispatchEvent method the appropriate event init method must
     *             be called after creation in order to initialize the Event's
     *             values. As an example, a user wishing to synthesize some kind of
     *             Event would call createEvent with the parameter "Events". The
     *             initEvent method could then be called on the newly created Event
     *             to set the specific type of Event to be dispatched and set its
     *             context information.
     * @return Newly created Event
     * @exception DOMException NOT_SUPPORTED_ERR: Raised if the implementation does
     *                         not support the type of Event interface requested
     */
    @Override
    public Event createEvent(final String type) throws DOMException {
        final String msg = DOMMessageFormatter.formatMessage("NOT_SUPPORTED_ERR", null);
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
    }

    /**
     * {@inheritDoc}
     *
     * A method to be called when a character data node has been modified
     */
    @Override
    void modifyingCharacterData(final NodeImpl node, final boolean replace) {
    }

    /**
     * {@inheritDoc}
     *
     * A method to be called when a character data node has been modified
     */
    @Override
    void modifiedCharacterData(final NodeImpl node, final String oldvalue, final String value, final boolean replace) {
    }

    /**
     * {@inheritDoc}
     *
     * A method to be called when a character data node has been replaced
     */
    @Override
    void replacedCharacterData(final NodeImpl node, final String oldvalue, final String value) {
        // now that we have finished replacing data, we need to perform the same actions
        // that are required after a character data node has been modified
        // send the value of false for replace parameter so that mutation
        // events if appropriate will be initiated
        modifiedCharacterData(node, oldvalue, value, false);
    }

    /**
     * {@inheritDoc}
     *
     * A method to be called when a node is about to be inserted in the tree.
     */
    @Override
    void insertingNode(final NodeImpl node, final boolean replace) {
    }

    /**
     * {@inheritDoc}
     *
     * A method to be called when a node has been inserted in the tree.
     */
    @Override
    void insertedNode(final NodeImpl node, final NodeImpl newInternal, final boolean replace) {
    }

    /**
     * {@inheritDoc}
     *
     * A method to be called when a node is about to be removed from the tree.
     */
    @Override
    void removingNode(final NodeImpl node, final NodeImpl oldChild, final boolean replace) {
    }

    /**
     * {@inheritDoc}
     *
     * A method to be called when a node has been removed from the tree.
     */
    @Override
    void removedNode(final NodeImpl node, final boolean replace) {
    }

    /**
     * {@inheritDoc}
     *
     * A method to be called when a node is about to be replaced in the tree.
     */
    @Override
    void replacingNode(final NodeImpl node) {
    }

    /**
     * {@inheritDoc}
     *
     * A method to be called when character data is about to be replaced in the
     * tree.
     */
    @Override
    void replacingData(final NodeImpl node) {
    }

    /**
     * {@inheritDoc}
     *
     * A method to be called when a node has been replaced in the tree.
     */
    @Override
    void replacedNode(final NodeImpl node) {
    }

    /**
     * {@inheritDoc}
     *
     * A method to be called when an attribute value has been modified
     */
    @Override
    void modifiedAttrValue(final AttrImpl attr, final String oldvalue) {
    }

    /**
     * {@inheritDoc}
     *
     * A method to be called when an attribute node has been set
     */
    @Override
    void setAttrNode(final AttrImpl attr, final AttrImpl previous) {
    }

    /**
     * {@inheritDoc}
     *
     * A method to be called when an attribute node has been removed
     */
    @Override
    void removedAttrNode(final AttrImpl attr, final NodeImpl oldOwner, final String name) {
    }

    /**
     * {@inheritDoc}
     *
     * A method to be called when an attribute node has been renamed
     */
    @Override
    void renamedAttrNode(final Attr oldAt, final Attr newAt) {
        // REVISIT: To be implemented!!!
    }

    /**
     * {@inheritDoc}
     *
     * A method to be called when an element has been renamed
     */
    @Override
    void renamedElement(final Element oldEl, final Element newEl) {
        // REVISIT: To be implemented!!!
    }
}
