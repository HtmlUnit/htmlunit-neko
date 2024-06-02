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

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.htmlunit.cyberneko.xerces.util.URI;
import org.htmlunit.cyberneko.xerces.util.XML11Char;
import org.htmlunit.cyberneko.xerces.util.XMLChar;
import org.htmlunit.cyberneko.xerces.xni.NamespaceContext;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.events.EventListener;

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
 * <p>
 * The CoreDocumentImpl class only implements the DOM Core. Additional modules
 * are supported by the more complete DocumentImpl subclass.
 * <p>
 *
 * @author Arnaud Le Hors, IBM
 * @author Joe Kesselman, IBM
 * @author Andy Clark, IBM
 * @author Ralf Pfeiffer, IBM
 */

public class CoreDocumentImpl extends ParentNode implements Document {

    /** Document type. */
    private DocumentTypeImpl docType_;

    /** Document element. */
    private ElementImpl docElement_;

    /** NodeListCache free list */
    private NodeListCache fFreeNLCache_;

    /** Experimental DOM Level 3 feature: Document encoding */
    private String encoding_;

    /** Experimental DOM Level 3 feature: Document actualEncoding */
    private String actualEncoding_;

    /** Experimental DOM Level 3 feature: Document version */
    private String version_;

    /** Experimental DOM Level 3 feature: Document standalone */
    private boolean standalone_;

    /** Experimental DOM Level 3 feature: documentURI */
    private String fDocumentURI_;

    /** Identifiers. */
    private HashMap<String, Element> identifiers_;

    /** Table for quick check of child insertion. */
    private static final int[] kidOK;

    /**
     * Number of alterations made to this document since its creation. Serves as a
     * "dirty bit" so that live objects such as NodeList can recognize when an
     * alteration has been made and discard its cached state information.
     * <p>
     * Any method that alters the tree structure MUST cause or be accompanied by a
     * call to changed(), to inform it that any outstanding NodeLists may have to be
     * updated.
     * <p>
     * (Required because NodeList is simultaneously "live" and integer- indexed -- a
     * bad decision in the DOM's design.)
     * <p>
     * Note that changes which do not affect the tree's structure -- changing the
     * node's name, for example -- do _not_ have to call changed().
     * <p>
     * Alternative implementation would be to use a cryptographic Digest value
     * rather than a count. This would have the advantage that "harmless" changes
     * (those producing equal() trees) would not force NodeList to resynchronize.
     * Disadvantage is that it's slightly more prone to "false negatives", though
     * that's the difference between "wildly unlikely" and "absurdly unlikely". IF
     * we start maintaining digests, we should consider taking advantage of them.
     * <p>
     * Note: This used to be done a node basis, so that we knew what subtree
     * changed. But since only DeepNodeList really use this today, the gain appears
     * to be really small compared to the cost of having an int on every (parent)
     * node plus having to walk up the tree all the way to the root to mark the
     * branch as changed everytime a node is changed. So we now have a single
     * counter global to the document. It means that some objects may flush their
     * cache more often than necessary, but this makes nodes smaller and only the
     * document needs to be marked as changed.
     */
    protected int changes = 0;

    // experimental

    /** Bypass error checking. */
    protected boolean errorChecking = true;

    /**
     * The following are required for compareDocumentPosition
     */
    // Document number. Documents are ordered across the implementation using
    // positive integer values. Documents are assigned numbers on demand.
    private int documentNumber_ = 0;
    // Node counter and table. Used to assign numbers to nodes for this
    // document. Node number values are negative integers. Nodes are
    // assigned numbers on demand.
    private int nodeCounter_ = 0;
    private Map<Node, Integer> nodeTable_;
    private boolean xml11Version_ = false; // by default 1.0

    static {
        kidOK = new int[13];

        kidOK[DOCUMENT_NODE] = 1 << ELEMENT_NODE | 1 << PROCESSING_INSTRUCTION_NODE | 1 << COMMENT_NODE
                | 1 << DOCUMENT_TYPE_NODE;

        kidOK[DOCUMENT_FRAGMENT_NODE] = kidOK[ENTITY_NODE] = kidOK[ENTITY_REFERENCE_NODE] = kidOK[ELEMENT_NODE] = 1 << ELEMENT_NODE
                | 1 << PROCESSING_INSTRUCTION_NODE | 1 << COMMENT_NODE | 1 << TEXT_NODE | 1 << CDATA_SECTION_NODE
                | 1 << ENTITY_REFERENCE_NODE;

        kidOK[ATTRIBUTE_NODE] = 1 << TEXT_NODE | 1 << ENTITY_REFERENCE_NODE;

        kidOK[DOCUMENT_TYPE_NODE] = kidOK[PROCESSING_INSTRUCTION_NODE] = kidOK[COMMENT_NODE] = kidOK[TEXT_NODE] = kidOK[CDATA_SECTION_NODE] = kidOK[NOTATION_NODE] = 0;
    }

    /**
     * NON-DOM: Actually creating a Document is outside the DOM's spec, since it has
     * to operate in terms of a particular implementation.
     */
    public CoreDocumentImpl() {
        super(null);
        ownerDocument = this;
    }

    /**
     * For DOM2 support. The createDocument factory method is in DOMImplementation.
     *
     * @param doctype the {@link DocumentType}
     */
    public CoreDocumentImpl(final DocumentType doctype) {
        this();
        if (doctype != null) {
            final DocumentTypeImpl doctypeImpl;
            try {
                doctypeImpl = (DocumentTypeImpl) doctype;
            }
            catch (final ClassCastException e) {
                final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null);
                throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, msg);
            }
            doctypeImpl.ownerDocument = this;
            appendChild(doctype);
        }
    }

    // even though ownerDocument refers to this in this implementation
    // the DOM Level 2 spec says it must be null, so make it appear so
    @Override
    public final Document getOwnerDocument() {
        return null;
    }

    /** Returns the node type. */
    @Override
    public short getNodeType() {
        return Node.DOCUMENT_NODE;
    }

    /** Returns the node name. */
    @Override
    public String getNodeName() {
        return "#document";
    }

    /**
     * Deep-clone a document, including fixing ownerDoc for the cloned children.
     * Note that this requires bypassing the WRONG_DOCUMENT_ERR protection. I've
     * chosen to implement it by calling importNode which is DOM Level 2.
     *
     * @return org.w3c.dom.Node
     * @param deep boolean, iff true replicate children
     */
    @Override
    public Node cloneNode(final boolean deep) {
        final CoreDocumentImpl newdoc = new CoreDocumentImpl();
        cloneNode(newdoc, deep);

        return newdoc;
    }

    // internal method to share code with subclass
    protected void cloneNode(final CoreDocumentImpl newdoc, final boolean deep) {
        // clone the children by importing them
        if (needsSyncChildren()) {
            synchronizeChildren();
        }

        if (deep) {
            HashMap<Node, String> reversedIdentifiers = null;
            if (identifiers_ != null) {
                // Build a reverse mapping from element to identifier.
                reversedIdentifiers = new HashMap<>();
                for (final Map.Entry<String, Element> stringElementEntry : identifiers_.entrySet()) {
                    reversedIdentifiers.put(stringElementEntry.getValue(), stringElementEntry.getKey());
                }
            }

            // Copy children into new document.
            for (ChildNode kid = firstChild; kid != null; kid = kid.nextSibling_) {
                newdoc.appendChild(newdoc.importNode(kid, true, true, reversedIdentifiers));
            }
        }

        // experimental
        newdoc.errorChecking = errorChecking;
    }

    /**
     * Since a Document may contain at most one top-level Element child, and at most
     * one DocumentType declaraction, we need to subclass our add-children methods
     * to implement this constraint. Since appendChild() is implemented as
     * insertBefore(,null), altering the latter fixes both.
     * <p>
     * While I'm doing so, I've taken advantage of the opportunity to cache
     * documentElement and docType so we don't have to search for them.
     * <p>
     * REVISIT: According to the spec it is not allowed to alter neither the
     * document element nor the document type in any way
     */
    @Override
    public Node insertBefore(final Node newChild, final Node refChild) throws DOMException {
        // Only one such child permitted
        final int type = newChild.getNodeType();
        if (errorChecking) {
            if (needsSyncChildren()) {
                synchronizeChildren();
            }
            if ((type == Node.ELEMENT_NODE && docElement_ != null)
                    || (type == Node.DOCUMENT_TYPE_NODE && docType_ != null)) {
                final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "HIERARCHY_REQUEST_ERR", null);
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, msg);
            }
        }
        // Adopt orphan doctypes
        if (newChild.getOwnerDocument() == null && newChild instanceof DocumentTypeImpl) {
            ((DocumentTypeImpl) newChild).ownerDocument = this;
        }
        super.insertBefore(newChild, refChild);

        // If insert succeeded, cache the kid appropriately
        if (type == Node.ELEMENT_NODE) {
            docElement_ = (ElementImpl) newChild;
        }
        else if (type == Node.DOCUMENT_TYPE_NODE) {
            docType_ = (DocumentTypeImpl) newChild;
        }

        return newChild;
    }

    /**
     * Since insertBefore caches the docElement (and, currently, docType),
     * removeChild has to know how to undo the cache
     * <p>
     * REVISIT: According to the spec it is not allowed to alter neither the
     * document element nor the document type in any way
     */
    @Override
    public Node removeChild(final Node oldChild) throws DOMException {
        super.removeChild(oldChild);

        // If remove succeeded, un-cache the kid appropriately
        final int type = oldChild.getNodeType();
        if (type == Node.ELEMENT_NODE) {
            docElement_ = null;
        }
        else if (type == Node.DOCUMENT_TYPE_NODE) {
            docType_ = null;
        }

        return oldChild;
    }

    /**
     * Since we cache the docElement (and, currently, docType), replaceChild has to
     * update the cache
     * <p>
     * REVISIT: According to the spec it is not allowed to alter neither the
     * document element nor the document type in any way
     */
    @Override
    public Node replaceChild(final Node newChild, final Node oldChild) throws DOMException {
        // Adopt orphan doctypes
        if (newChild.getOwnerDocument() == null && newChild instanceof DocumentTypeImpl) {
            ((DocumentTypeImpl) newChild).ownerDocument = this;
        }

        if (errorChecking && ((docType_ != null && oldChild.getNodeType() != Node.DOCUMENT_TYPE_NODE
                && newChild.getNodeType() == Node.DOCUMENT_TYPE_NODE)
                || (docElement_ != null && oldChild.getNodeType() != Node.ELEMENT_NODE
                        && newChild.getNodeType() == Node.ELEMENT_NODE))) {

            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                    DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "HIERARCHY_REQUEST_ERR", null));
        }
        super.replaceChild(newChild, oldChild);

        final int type = oldChild.getNodeType();
        if (type == Node.ELEMENT_NODE) {
            docElement_ = (ElementImpl) newChild;
        }
        else if (type == Node.DOCUMENT_TYPE_NODE) {
            docType_ = (DocumentTypeImpl) newChild;
        }
        return oldChild;
    }

    /*
     * Get Node text content
     */
    @Override
    public String getTextContent() throws DOMException {
        return null;
    }

    /*
     * Set Node text content
     */
    @Override
    public void setTextContent(final String textContent) throws DOMException {
        // no-op
    }

    /**
     * Factory method; creates an Attribute having this Document as its OwnerDoc.
     *
     * @param name The name of the attribute. Note that the attribute's value is
     *             _not_ established at the factory; remember to set it!
     *
     * @throws DOMException INVALID_NAME_ERR if the attribute name is not
     *                      acceptable.
     */
    @Override
    public Attr createAttribute(final String name) throws DOMException {
        if (errorChecking && !isXMLName(name, xml11Version_)) {
            final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_CHARACTER_ERR", null);
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
        }
        return new AttrImpl(this, name);

    }

    /**
     * Factory method; creates a CDATASection having this Document as its OwnerDoc.
     *
     * @param data The initial contents of the CDATA
     *
     * @throws DOMException NOT_SUPPORTED_ERR for HTML documents. (HTML not yet
     *                      implemented.)
     */
    @Override
    public CDATASection createCDATASection(final String data) throws DOMException {
        return new CDATASectionImpl(this, data);
    }

    /**
     * Factory method; creates a Comment having this Document as its OwnerDoc.
     *
     * @param data The initial contents of the Comment.
     * @return comment
     */
    @Override
    public Comment createComment(final String data) {
        return new CommentImpl(this, data);
    }

    /**
     * Factory method; creates a DocumentFragment having this Document as its
     * OwnerDoc.
     */
    @Override
    public DocumentFragment createDocumentFragment() {
        return new DocumentFragmentImpl(this);
    }

    /**
     * Factory method; creates an Element having this Document as its OwnerDoc.
     *
     * @param tagName The name of the element type to instantiate. For XML, this is
     *                case-sensitive. For HTML, the tagName parameter may be
     *                provided in any case, but it must be mapped to the canonical
     *                uppercase form by the DOM implementation.
     *
     * @throws DOMException INVALID_NAME_ERR if the tag name is not acceptable.
     */
    @Override
    public Element createElement(final String tagName) throws DOMException {
        // don't check the name here - this is done by the HTMLScanner and
        // not all valid html tag names are valid xml names
        //        if (errorChecking && !isXMLName(tagName, xml11Version_)) {
        //            final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_CHARACTER_ERR", null);
        //            throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
        //        }
        return new ElementImpl(this, tagName);
    }

    /**
     * Factory method; creates an EntityReference having this Document as its
     * OwnerDoc.
     *
     * @param name The name of the Entity we wish to refer to
     *
     * @throws DOMException NOT_SUPPORTED_ERR for HTML documents, where nonstandard
     *                      entities are not permitted. (HTML not yet implemented.)
     */
    @Override
    public EntityReference createEntityReference(final String name) throws DOMException {
        if (errorChecking && !isXMLName(name, xml11Version_)) {
            final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_CHARACTER_ERR", null);
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
        }
        return new EntityReferenceImpl(this, name);

    }

    /**
     * Factory method; creates a ProcessingInstruction having this Document as its
     * OwnerDoc.
     *
     * @param target The target "processor channel"
     * @param data   Parameter string to be passed to the target.
     *
     * @throws DOMException INVALID_NAME_ERR if the target name is not acceptable.
     *
     * @throws DOMException NOT_SUPPORTED_ERR for HTML documents. (HTML not yet
     *                      implemented.)
     */
    @Override
    public ProcessingInstruction createProcessingInstruction(final String target, final String data) throws DOMException {
        if (errorChecking && !isXMLName(target, xml11Version_)) {
            final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_CHARACTER_ERR", null);
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
        }
        return new ProcessingInstructionImpl(this, target, data);
    }

    /**
     * Factory method; creates a Text node having this Document as its OwnerDoc.
     *
     * @param data The initial contents of the Text.
     * @return the text
     */
    @Override
    public Text createTextNode(final String data) {
        return new TextImpl(this, data);
    }

    /**
     * {@inheritDoc}
     *
     * For XML, this provides access to the Document Type Definition. For HTML
     * documents, and XML documents which don't specify a DTD, it will be null.
     */
    @Override
    public DocumentType getDoctype() {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        return docType_;
    }

    /**
     * {@inheritDoc}
     *
     * Convenience method, allowing direct access to the child node which is
     * considered the root of the actual document content. For HTML, where it is
     * legal to have more than one Element at the top level of the document, we pick
     * the one with the tagName "HTML". For XML there should be only one top-level
     * <p>
     * (HTML not yet supported.)
     */
    @Override
    public Element getDocumentElement() {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        return docElement_;
    }

    /**
     * Return a <em>live</em> collection of all descendant Elements (not just
     * immediate children) having the specified tag name.
     *
     * @param tagname The type of Element we want to gather. "*" will be taken as a
     *                wildcard, meaning "all elements in the document."
     *
     * @see DeepNodeListImpl
     */
    @Override
    public NodeList getElementsByTagName(final String tagname) {
        return new DeepNodeListImpl(this, tagname);
    }

    /**
     * Retrieve information describing the abilities of this particular DOM
     * implementation. Intended to support applications that may be using DOMs
     * retrieved from several different sources, potentially with different
     * underlying representations.
     */
    @Override
    public DOMImplementation getImplementation() {
        // Currently implemented as a singleton, since it's hardcoded
        // information anyway.
        return CoreDOMImplementationImpl.INSTANCE;
    }

    /**
     * Sets whether the DOM implementation performs error checking upon operations.
     * Turning off error checking only affects the following DOM checks:
     * <ul>
     * <li>Checking strings to make sure that all characters are legal XML
     * characters
     * <li>Hierarchy checking such as allowed children, checks for cycles, etc.
     * </ul>
     * <p>
     * Turning off error checking does <em>not</em> turn off the following checks:
     * <ul>
     * <li>Read only checks
     * <li>Checks related to DOM events
     * </ul>
     *
     * @param check check flag
     */
    public void setErrorChecking(final boolean check) {
        errorChecking = check;
    }

    /*
     * DOM Level 3 WD - Experimental. {@inheritDoc}
     */
    @Override
    public void setStrictErrorChecking(final boolean check) {
        errorChecking = check;
    }

    /**
     * @return true if the DOM implementation performs error checking.
     */
    public boolean getErrorChecking() {
        return errorChecking;
    }

    /*
     * DOM Level 3 WD - Experimental. {@inheritDoc}
     */
    @Override
    public boolean getStrictErrorChecking() {
        return errorChecking;
    }

    /**
     * {@inheritDoc} DOM Level 3 CR - Experimental. (Was getActualEncoding)
     * <p>
     * An attribute specifying the encoding used for this document at the time of
     * the parsing. This is <code>null</code> when it is not known, such as when the
     * <code>Document</code> was created in memory.
     */
    @Override
    public String getInputEncoding() {
        return actualEncoding_;
    }

    /**
     * DOM Internal (Was a DOM L3 Core WD public interface method setActualEncoding
     * )
     * <p>
     * An attribute specifying the actual encoding of this document. This is
     * <code>null</code> otherwise. <br>
     * This attribute represents the property [character encoding scheme] defined
     * in.
     *
     * @param value the value
     */
    public void setInputEncoding(final String value) {
        actualEncoding_ = value;
    }

    /**
     * DOM Internal (Was a DOM L3 Core WD public interface method setXMLEncoding )
     * <p>
     * An attribute specifying, as part of the XML declaration, the encoding of this
     * document. This is null when unspecified.
     *
     * @param value the value
     */
    public void setXmlEncoding(final String value) {
        encoding_ = value;
    }

    /**
     * DOM Level 3 WD - Experimental. The encoding of this document (part of XML
     * Declaration)
     */
    @Override
    public String getXmlEncoding() {
        return encoding_;
    }

    /**
     * {@inheritDoc}
     *
     * DOM Level 3 CR - Experimental. version - An attribute specifying, as part of
     * the XML declaration, the version number of this document.
     */
    @Override
    public void setXmlVersion(final String value) {
        if ("1.0".equals(value) || "1.1".equals(value)) {
            // we need to change the flag value only --
            // when the version set is different than already set.
            if (!getXmlVersion().equals(value)) {
                // change the normalization value back to false
                isNormalized(false);
                version_ = value;
            }
        }
        else {
            // NOT_SUPPORTED_ERR: Raised if the vesion is set to a value that is not
            // supported by
            // this document
            // we dont support any other XML version
            final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);

        }
        xml11Version_ = "1.1".equals(getXmlVersion());
    }

    /**
     * {@inheritDoc}
     *
     * DOM Level 3 WD - Experimental. The version of this document (part of XML
     * Declaration)
     */
    @Override
    public String getXmlVersion() {
        return (version_ == null) ? "1.0" : version_;
    }

    /**
     * {@inheritDoc}
     *
     * DOM Level 3 CR - Experimental.
     * <p>
     * Xmlstandalone - An attribute specifying, as part of the XML declaration,
     * whether this document is standalone
     *
     * @exception DOMException NOT_SUPPORTED_ERR: Raised if this document does not
     *                         support the "XML" feature.
     */
    @Override
    public void setXmlStandalone(final boolean value) throws DOMException {
        standalone_ = value;
    }

    /**
     * {@inheritDoc}
     *
     * DOM Level 3 WD - Experimental. standalone that specifies whether this
     * document is standalone (part of XML Declaration)
     */
    @Override
    public boolean getXmlStandalone() {
        return standalone_;
    }

    /**
     * {@inheritDoc}
     *
     * DOM Level 3 WD - Experimental. The location of the document or
     * <code>null</code> if undefined. <br>
     * Beware that when the <code>Document</code> supports the feature "HTML" , the
     * href attribute of the HTML BASE element takes precedence over this attribute.
     */
    @Override
    public String getDocumentURI() {
        return fDocumentURI_;
    }

    // NON-DOM Used by DOM Level 3 WD remameNode.
    // Some DOM implementations do not allow nodes to be renamed and require
    // creating new elements.
    // In this case this method should be overwritten.
    // @return true if the given element can be renamed, false, if it must be
    // replaced.
    protected boolean canRenameElements(final String newNamespaceURI, final String newNodeName, final ElementImpl el) {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * DOM Level 3 WD - Experimental. Renaming node
     */
    @Override
    public Node renameNode(final Node n, final String namespaceURI, final String name) throws DOMException {
        if (errorChecking && n.getOwnerDocument() != this && n != this) {
            final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null);
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, msg);
        }
        switch (n.getNodeType()) {
            case ELEMENT_NODE: {
                ElementImpl el = (ElementImpl) n;
                if (el instanceof ElementNSImpl) {
                    if (canRenameElements(namespaceURI, name, el)) {
                        ((ElementNSImpl) el).rename(namespaceURI, name);
                    }
                    else {
                        el = replaceRenameElement(el, namespaceURI, name);
                    }
                }
                else {
                    if (namespaceURI == null && canRenameElements(null, name, el)) {
                        el.rename(name);
                    }
                    else {
                        el = replaceRenameElement(el, namespaceURI, name);
                    }
                }
                // fire ElementNameChanged event
                renamedElement((Element) n, el);
                return el;
            }
            case ATTRIBUTE_NODE: {
                AttrImpl at = (AttrImpl) n;

                // detach attr from element
                final Element el = at.getOwnerElement();
                if (el != null) {
                    el.removeAttributeNode(at);
                }
                if (n instanceof AttrNSImpl) {
                    ((AttrNSImpl) at).rename(namespaceURI, name);
                    // reattach attr to element
                    if (el != null) {
                        el.setAttributeNodeNS(at);
                    }
                }
                else {
                    if (namespaceURI == null) {
                        at.rename(name);
                        // reattach attr to element
                        if (el != null) {
                            el.setAttributeNode(at);
                        }
                    }
                    else {
                        // we need to create a new object
                        final AttrNSImpl nat = (AttrNSImpl) createAttributeNS(namespaceURI, name);

                        // register event listeners on new node
                        copyEventListeners(at, nat);

                        // move children to new node
                        Node child = at.getFirstChild();
                        while (child != null) {
                            at.removeChild(child);
                            nat.appendChild(child);
                            child = at.getFirstChild();
                        }

                        // reattach attr to element
                        if (el != null) {
                            el.setAttributeNode(nat);
                        }
                        at = nat;
                    }
                }
                // fire AttributeNameChanged event
                renamedAttrNode((Attr) n, at);

                return at;
            }
            default: {
                final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
            }
        }
    }

    private ElementImpl replaceRenameElement(final ElementImpl el, final String namespaceURI, final String name) {
        // we need to create a new object
        final ElementNSImpl nel = (ElementNSImpl) createElementNS(namespaceURI, name);

        // register event listeners on new node
        copyEventListeners(el, nel);

        // remove old node from parent if any
        final Node parent = el.getParentNode();
        final Node nextSib = el.getNextSibling();
        if (parent != null) {
            parent.removeChild(el);
        }
        // move children to new node
        Node child = el.getFirstChild();
        while (child != null) {
            el.removeChild(child);
            nel.appendChild(child);
            child = el.getFirstChild();
        }
        // move specified attributes to new node
        nel.moveSpecifiedAttributes(el);

        // insert new node where old one was
        if (parent != null) {
            parent.insertBefore(nel, nextSib);
        }
        return nel;
    }

    /**
     * DOM Level 3 WD - Experimental Normalize document.
     */
    @Override
    public void normalizeDocument() {
    }

    /**
     * DOM Level 3 CR - Experimental
     * <p>
     * The configuration used when <code>Document.normalizeDocument</code> is
     * invoked.
     */
    @Override
    public DOMConfiguration getDomConfig() {
        return null;
    }

    /**
     * Returns the absolute base URI of this node or null if the implementation
     * wasn't able to obtain an absolute URI. Note: If the URI is malformed, a null
     * is returned.
     *
     * @return The absolute base URI of this node or null.
     */
    @Override
    public String getBaseURI() {
        if (fDocumentURI_ != null && fDocumentURI_.length() != 0) { // attribute value is always empty string
            try {
                return new URI(fDocumentURI_).toString();
            }
            catch (final org.htmlunit.cyberneko.xerces.util.URI.MalformedURIException e) {
                // REVISIT: what should happen in this case?
                return null;
            }
        }
        return fDocumentURI_;
    }

    /**
     * DOM Level 3 WD - Experimental.
     */
    @Override
    public void setDocumentURI(final String documentURI) {
        fDocumentURI_ = documentURI;
    }

    /**
     * NON-DOM Factory method; creates a DocumentType having this Document as its
     * OwnerDoc. (REC-DOM-Level-1-19981001 left the process of building DTD
     * information unspecified.)
     *
     * @param qualifiedName the name
     * @param publicID      the public id
     * @param systemID      the system id
     * @return the {@link DocumentType}
     *
     * @throws DOMException NOT_SUPPORTED_ERR for HTML documents, where DTDs are not
     *                      permitted. (HTML not yet implemented.)
     */
    public DocumentType createDocumentType(final String qualifiedName, final String publicID, final String systemID) throws DOMException {

        return new DocumentTypeImpl(this, qualifiedName, publicID, systemID);

    }

    /**
     * NON-DOM Factory method; creates an Entity having this Document as its
     * OwnerDoc. (REC-DOM-Level-1-19981001 left the process of building DTD
     * information unspecified.)
     *
     * @param name The name of the Entity we wish to provide a value for.
     * @return the new entity
     *
     * @throws DOMException NOT_SUPPORTED_ERR for HTML documents, where nonstandard
     *                      entities are not permitted. (HTML not yet implemented.)
     */
    public Entity createEntity(final String name) throws DOMException {
        if (errorChecking && !isXMLName(name, xml11Version_)) {
            final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_CHARACTER_ERR", null);
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
        }
        return new EntityImpl(this, name);

    }

    /**
     * {@inheritDoc} NON-DOM: Get the number associated with this document. Used to
     * order documents in the implementation.
     */
    @Override
    protected int getNodeNumber() {
        if (documentNumber_ == 0) {

            final CoreDOMImplementationImpl cd = CoreDOMImplementationImpl.INSTANCE;
            documentNumber_ = cd.assignDocumentNumber();
        }
        return documentNumber_;
    }

    // NON-DOM: Get a number associated with a node created with respect
    // to this document. Needed for compareDocumentPosition when nodes
    // are disconnected. This is only used on demand.
    protected int getNodeNumber(final Node node) {
        // Check if the node is already in the hash
        // If so, retrieve the node number
        // If not, assign a number to the node
        // Node numbers are negative, from -1 to -n
        final int num;
        if (nodeTable_ == null) {
            nodeTable_ = new WeakHashMap<>();
            num = --nodeCounter_;
            nodeTable_.put(node, Integer.valueOf(num));
        }
        else {
            final Integer n = nodeTable_.get(node);
            if (n == null) {
                num = --nodeCounter_;
                nodeTable_.put(node, Integer.valueOf(num));
            }
            else {
                num = n.intValue();
            }
        }
        return num;
    }

    /**
     * {@inheritDoc}
     *
     * Copies a node from another document to this document. The new nodes are
     * created using this document's factory methods and are populated with the data
     * from the source's accessor methods defined by the DOM interfaces. Its
     * behavior is otherwise similar to that of cloneNode.
     * <p>
     * According to the DOM specifications, document nodes cannot be imported and a
     * NOT_SUPPORTED_ERR exception is thrown if attempted.
     */
    @Override
    public Node importNode(final Node source, final boolean deep) throws DOMException {
        return importNode(source, deep, false, null);
    }

    /**
     * Overloaded implementation of DOM's importNode method. This method provides
     * the core functionality for the public importNode and cloneNode methods.
     * <p>
     * The reversedIdentifiers parameter is provided for cloneNode to preserve the
     * document's identifiers. The HashMap has Elements as the keys and their
     * identifiers as the values. When an element is being imported, a check is done
     * for an associated identifier. If one exists, the identifier is registered
     * with the new, imported element. If reversedIdentifiers is null, the parameter
     * is not applied.
     *
     * @param source              the source node
     * @param deep                true for deep iport
     * @param cloningDoc          the cloning Doc
     * @param reversedIdentifiers helper
     */
    private Node importNode(final Node source, boolean deep, final boolean cloningDoc, final HashMap<Node, String> reversedIdentifiers)
            throws DOMException {
        Node newnode = null;

        // Sigh. This doesn't work; too many nodes have private data that
        // would have to be manually tweaked. May be able to add local
        // shortcuts to each nodetype. Consider ?????
        // if(source instanceof NodeImpl &&
        // !(source instanceof DocumentImpl))
        // {
        // // Can't clone DocumentImpl since it invokes us...
        // newnode=(NodeImpl)source.cloneNode(false);
        // newnode.ownerDocument=this;
        // }
        // else
        final int type = source.getNodeType();
        switch (type) {
            case ELEMENT_NODE: {
                final Element newElement;
                final boolean domLevel20 = source.getOwnerDocument().getImplementation().hasFeature("XML", "2.0");
                // Create element according to namespace support/qualification.
                if (!domLevel20 || source.getLocalName() == null) {
                    newElement = createElement(source.getNodeName());
                }
                else {
                    newElement = createElementNS(source.getNamespaceURI(), source.getNodeName());
                }

                // Copy element's attributes, if any.
                final NamedNodeMap sourceAttrs = source.getAttributes();
                if (sourceAttrs != null) {
                    final int length = sourceAttrs.getLength();
                    for (int index = 0; index < length; index++) {
                        final Attr attr = (Attr) sourceAttrs.item(index);

                        // NOTE: this methods is used for both importingNode
                        // and cloning the document node. In case of the
                        // clonning default attributes should be copied.
                        // But for importNode defaults should be ignored.
                        if (attr.getSpecified() || cloningDoc) {
                            final Attr newAttr = (Attr) importNode(attr, true, cloningDoc, reversedIdentifiers);

                            // Attach attribute according to namespace
                            // support/qualification.
                            if (!domLevel20 || attr.getLocalName() == null) {
                                newElement.setAttributeNode(newAttr);
                            }
                            else {
                                newElement.setAttributeNodeNS(newAttr);
                            }
                        }
                    }
                }

                // Register element identifier.
                if (reversedIdentifiers != null) {
                    // Does element have an associated identifier?
                    final String elementId = reversedIdentifiers.get(source);
                    if (elementId != null) {
                        if (identifiers_ == null) {
                            identifiers_ = new HashMap<>();
                        }

                        identifiers_.put(elementId, newElement);
                    }
                }

                newnode = newElement;
                break;
            }

            case ATTRIBUTE_NODE: {

                if (source.getOwnerDocument().getImplementation().hasFeature("XML", "2.0")) {
                    if (source.getLocalName() == null) {
                        newnode = createAttribute(source.getNodeName());
                    }
                    else {
                        newnode = createAttributeNS(source.getNamespaceURI(), source.getNodeName());
                    }
                }
                else {
                    newnode = createAttribute(source.getNodeName());
                }
                // if source is an AttrImpl from this very same implementation
                // avoid creating the child nodes if possible
                if (source instanceof AttrImpl) {
                    final AttrImpl attr = (AttrImpl) source;
                    if (attr.hasStringValue()) {
                        final AttrImpl newattr = (AttrImpl) newnode;
                        newattr.setValue(attr.getValue());
                        deep = false;
                    }
                    else {
                        deep = true;
                    }
                }
                else {
                    // According to the DOM spec the kids carry the value.
                    // However, there are non compliant implementations out
                    // there that fail to do so. To avoid ending up with no
                    // value at all, in this case we simply copy the text value
                    // directly.
                    if (source.getFirstChild() == null) {
                        newnode.setNodeValue(source.getNodeValue());
                        deep = false;
                    }
                    else {
                        deep = true;
                    }
                }
                break;
            }

            case TEXT_NODE: {
                newnode = createTextNode(source.getNodeValue());
                break;
            }

            case CDATA_SECTION_NODE: {
                newnode = createCDATASection(source.getNodeValue());
                break;
            }

            case ENTITY_REFERENCE_NODE: {
                newnode = createEntityReference(source.getNodeName());
                // the subtree is created according to this doc by the method
                // above, so avoid carrying over original subtree
                deep = false;
                break;
            }

            case ENTITY_NODE: {
                final Entity srcentity = (Entity) source;
                final EntityImpl newentity = (EntityImpl) createEntity(source.getNodeName());
                newentity.setPublicId(srcentity.getPublicId());
                newentity.setSystemId(srcentity.getSystemId());
                newentity.setNotationName(srcentity.getNotationName());
                newnode = newentity;
                break;
            }

            case PROCESSING_INSTRUCTION_NODE: {
                newnode = createProcessingInstruction(source.getNodeName(), source.getNodeValue());
                break;
            }

            case COMMENT_NODE: {
                newnode = createComment(source.getNodeValue());
                break;
            }

            case DOCUMENT_TYPE_NODE: {
                // unless this is used as part of cloning a Document
                // forbid it for the sake of being compliant to the DOM spec
                if (!cloningDoc) {
                    final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
                    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
                }
                final DocumentType srcdoctype = (DocumentType) source;
                final DocumentTypeImpl newdoctype = (DocumentTypeImpl) createDocumentType(srcdoctype.getNodeName(),
                        srcdoctype.getPublicId(), srcdoctype.getSystemId());
                newdoctype.setInternalSubset(srcdoctype.getInternalSubset());
                // Values are on NamedNodeMaps
                NamedNodeMap smap = srcdoctype.getEntities();
                NamedNodeMap tmap = newdoctype.getEntities();
                if (smap != null) {
                    for (int i = 0; i < smap.getLength(); i++) {
                        tmap.setNamedItem(importNode(smap.item(i), true, true, reversedIdentifiers));
                    }
                }
                smap = srcdoctype.getNotations();
                tmap = newdoctype.getNotations();
                if (smap != null) {
                    for (int i = 0; i < smap.getLength(); i++) {
                        tmap.setNamedItem(importNode(smap.item(i), true, true, reversedIdentifiers));
                    }
                }

                // NOTE: At this time, the DOM definition of DocumentType
                // doesn't cover Elements and their Attributes. domimpl's
                // extentions in that area will not be preserved, even if
                // copying from domimpl to domimpl. We could special-case
                // that here. Arguably we should. Consider. ?????
                newnode = newdoctype;
                break;
            }

            case DOCUMENT_FRAGMENT_NODE: {
                newnode = createDocumentFragment();
                // No name, kids carry value
                break;
            }

            case NOTATION_NODE: {
                break;
            }
            case DOCUMENT_NODE: // Can't import document nodes
            default: { // Unknown node type
                final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
            }
        }

        // If deep, replicate and attach the kids.
        if (deep) {
            for (Node srckid = source.getFirstChild(); srckid != null; srckid = srckid.getNextSibling()) {
                newnode.appendChild(importNode(srckid, true, cloningDoc, reversedIdentifiers));
            }
        }
        return newnode;

    }

    /**
     * DOM Level 3 WD - Experimental Change the node's ownerDocument, and its
     * subtree, to this Document
     *
     * @param source The node to adopt.
     * @see #importNode
     */
    @Override
    public Node adoptNode(final Node source) {
        final NodeImpl node;
        try {
            node = (NodeImpl) source;
        }
        catch (final ClassCastException e) {
            // source node comes from a different DOMImplementation
            return null;
        }

        // Return null if the source is null

        if (source == null) {
            return null;
        }

        if (source.getOwnerDocument() != null) {
            final DOMImplementation thisImpl = this.getImplementation();
            final DOMImplementation otherImpl = source.getOwnerDocument().getImplementation();

            // when the source node comes from a different implementation.
            if (thisImpl != otherImpl) {
                // Adopting from a deferred DOM to a non-deferred DOM
                // Adopting between two dissimilar DOMs is not allowed
                return null;
            }
        }

        switch (node.getNodeType()) {
            case ATTRIBUTE_NODE: {
                final AttrImpl attr = (AttrImpl) node;
                // remove node from wherever it is
                if (attr.getOwnerElement() != null) {
                    // 1. owner element attribute is set to null
                    attr.getOwnerElement().removeAttributeNode(attr);
                }
                // 2. specified flag is set to true
                attr.isSpecified(true);

                // 3. change ownership
                attr.setOwnerDocument(this);
                break;
            }
            // entity, notation nodes are read only nodes.. so they can't be adopted.
            // runtime will fall through to NOTATION_NODE
            case ENTITY_NODE:
            case NOTATION_NODE: {
                final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null);
                throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, msg);

            }
            // document, documentype nodes can't be adopted.
            // runtime will fall through to DocumentTypeNode
            case DOCUMENT_NODE:
            case DOCUMENT_TYPE_NODE: {
                final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
            }
            case ENTITY_REFERENCE_NODE: {
                // remove node from wherever it is
                final Node parent = node.getParentNode();
                if (parent != null) {
                    parent.removeChild(source);
                }
                // discard its replacement value
                Node child;
                while ((child = node.getFirstChild()) != null) {
                    node.removeChild(child);
                }
                // change ownership
                node.setOwnerDocument(this);
                // set its new replacement value if any
                if (docType_ == null) {
                    break;
                }
                final NamedNodeMap entities = docType_.getEntities();
                final Node entityNode = entities.getNamedItem(node.getNodeName());
                if (entityNode == null) {
                    break;
                }
                for (child = entityNode.getFirstChild(); child != null; child = child.getNextSibling()) {
                    final Node childClone = child.cloneNode(true);
                    node.appendChild(childClone);
                }
                break;
            }
            case ELEMENT_NODE: {
                // remove node from wherever it is
                final Node parent = node.getParentNode();
                if (parent != null) {
                    parent.removeChild(source);
                }
                // change ownership
                node.setOwnerDocument(this);
                break;
            }
            default: {
                // remove node from wherever it is
                final Node parent = node.getParentNode();
                if (parent != null) {
                    parent.removeChild(source);
                }
                // change ownership
                node.setOwnerDocument(this);
            }
        }

        return node;
    }

    /**
     * Traverses the DOM Tree and expands deferred nodes and their children.
     *
     * @param node the node
     *
     */
    protected void undeferChildren(Node node) {
        final Node top = node;

        while (null != node) {
            final NamedNodeMap attributes = node.getAttributes();
            if (attributes != null) {
                final int length = attributes.getLength();
                for (int i = 0; i < length; ++i) {
                    undeferChildren(attributes.item(i));
                }
            }

            Node nextNode;
            nextNode = node.getFirstChild();

            while (null == nextNode) {

                if (top.equals(node)) {
                    break;
                }

                nextNode = node.getNextSibling();

                if (null == nextNode) {
                    node = node.getParentNode();

                    if ((null == node) || (top.equals(node))) {
                        nextNode = null;
                        break;
                    }
                }
            }

            node = nextNode;
        }
    }

    /**
     * Introduced in DOM Level 2 Returns the Element whose ID is given by elementId.
     * If no such element exists, returns null. Behavior is not defined if more than
     * one element has this ID.
     * <p>
     * Note: The DOM implementation must have information that says which attributes
     * are of type ID. Attributes with the name "ID" are not of type ID unless so
     * defined. Implementations that do not know whether attributes are of type ID
     * or not are expected to return null.
     *
     * @see #getIdentifier
     */
    @Override
    public Element getElementById(final String elementId) {
        return getIdentifier(elementId);
    }

    /**
     * Remove all identifiers from the ID table
     */
    protected final void clearIdentifiers() {
        if (identifiers_ != null) {
            identifiers_.clear();
        }
    }

    /**
     * Registers an identifier name with a specified element node. If the identifier
     * is already registered, the new element node replaces the previous node. If
     * the specified element node is null, removeIdentifier() is called.
     *
     * @param idName  the name
     * @param element the element
     *
     * @see #getIdentifier(String)
     * @see #removeIdentifier(String)
     */
    public void putIdentifier(final String idName, final Element element) {
        if (element == null) {
            removeIdentifier(idName);
            return;
        }

        if (identifiers_ == null) {
            identifiers_ = new HashMap<>();
        }

        identifiers_.put(idName, element);
    }

    /**
     * Returns a previously registered element with the specified identifier name,
     * or null if no element is registered.
     *
     * @param idName the name
     * @return the element
     *
     * @see #putIdentifier(String, Element)
     * @see #removeIdentifier(String)
     */
    public Element getIdentifier(final String idName) {
        if (identifiers_ == null) {
            return null;
        }
        final Element elem = identifiers_.get(idName);
        if (elem != null) {
            // check that the element is in the tree
            Node parent = elem.getParentNode();
            while (parent != null) {
                if (parent == this) {
                    return elem;
                }
                parent = parent.getParentNode();
            }
        }
        return null;
    }

    /**
     * Removes a previously registered element with the specified identifier name.
     *
     * @param idName the name
     *
     * @see #putIdentifier(String, Element)
     * @see #getIdentifier(String)
     */
    public void removeIdentifier(final String idName) {
        if (identifiers_ == null) {
            return;
        }

        identifiers_.remove(idName);
    }

    /**
     * Introduced in DOM Level 2.
     * <p>
     * Creates an element of the given qualified name and namespace URI. If the
     * given namespaceURI is null or an empty string and the qualifiedName has a
     * prefix that is "xml", the created element is bound to the predefined
     * namespace "http://www.w3.org/XML/1998/namespace" [Namespaces].
     *
     * @param namespaceURI  The namespace URI of the element to create.
     * @param qualifiedName The qualified name of the element type to instantiate.
     * @return Element A new Element object with the following attributes:
     * @throws DOMException INVALID_CHARACTER_ERR: Raised if the specified name
     *                      contains an invalid character.
     * @throws DOMException NAMESPACE_ERR: Raised if the qualifiedName has a prefix
     *                      that is "xml" and the namespaceURI is neither null nor
     *                      an empty string nor
     *                      "http://www.w3.org/XML/1998/namespace", or if the
     *                      qualifiedName has a prefix different from "xml" and the
     *                      namespaceURI is null or an empty string.
     */
    @Override
    public Element createElementNS(final String namespaceURI, final String qualifiedName) throws DOMException {
        return new ElementNSImpl(this, namespaceURI, qualifiedName);
    }

    /**
     * NON-DOM: a factory method used by the Xerces DOM parser to create an element.
     *
     * @param namespaceURI  The namespace URI of the element to create.
     * @param qualifiedName The qualified name of the element type to instantiate.
     * @param localpart     The local name of the attribute to instantiate.
     *
     * @return Element A new Element object with the following attributes:
     * @exception DOMException INVALID_CHARACTER_ERR: Raised if the specified name
     *                         contains an invalid character.
     */
    public Element createElementNS(final String namespaceURI, final String qualifiedName, final String localpart) throws DOMException {
        return new ElementNSImpl(this, namespaceURI, qualifiedName, localpart);
    }

    /**
     * Introduced in DOM Level 2.
     * <p>
     * Creates an attribute of the given qualified name and namespace URI. If the
     * given namespaceURI is null or an empty string and the qualifiedName has a
     * prefix that is "xml", the created element is bound to the predefined
     * namespace "http://www.w3.org/XML/1998/namespace" [Namespaces].
     *
     * @param namespaceURI  The namespace URI of the attribute to create. When it is
     *                      null or an empty string, this method behaves like
     *                      createAttribute.
     * @param qualifiedName The qualified name of the attribute to instantiate.
     * @return Attr A new Attr object.
     * @throws DOMException INVALID_CHARACTER_ERR: Raised if the specified name
     *                      contains an invalid character.
     */
    @Override
    public Attr createAttributeNS(final String namespaceURI, final String qualifiedName) throws DOMException {
        return new AttrNSImpl(this, namespaceURI, qualifiedName);
    }

    /**
     * NON-DOM: a factory method used by the Xerces DOM parser to create an element.
     *
     * @param namespaceURI  The namespace URI of the attribute to create. When it is
     *                      null or an empty string, this method behaves like
     *                      createAttribute.
     * @param qualifiedName The qualified name of the attribute to instantiate.
     * @param localpart     The local name of the attribute to instantiate.
     *
     * @return Attr A new Attr object.
     * @throws DOMException INVALID_CHARACTER_ERR: Raised if the specified name
     *                      contains an invalid character.
     */
    public Attr createAttributeNS(final String namespaceURI, final String qualifiedName, final String localpart) throws DOMException {
        return new AttrNSImpl(this, namespaceURI, qualifiedName, localpart);
    }

    /**
     * Introduced in DOM Level 2.
     * <p>
     * Returns a NodeList of all the Elements with a given local name and namespace
     * URI in the order in which they would be encountered in a preorder traversal
     * of the Document tree.
     *
     * @param namespaceURI The namespace URI of the elements to match on. The
     *                     special value "*" matches all namespaces. When it is null
     *                     or an empty string, this method behaves like
     *                     getElementsByTagName.
     * @param localName    The local name of the elements to match on. The special
     *                     value "*" matches all local names.
     * @return NodeList A new NodeList object containing all the matched Elements.
     */
    @Override
    public NodeList getElementsByTagNameNS(final String namespaceURI, final String localName) {
        return new DeepNodeListImpl(this, namespaceURI, localName);
    }

    /** Clone. */
    @Override
    public Object clone() throws CloneNotSupportedException {
        final CoreDocumentImpl newdoc = (CoreDocumentImpl) super.clone();
        newdoc.docType_ = null;
        newdoc.docElement_ = null;
        return newdoc;
    }

    /**
     * Check the string against XML's definition of acceptable names for elements
     * and attributes and so on using the XMLCharacterProperties utility class
     *
     * @param s            the string to check
     * @param xml11Version if true use xml 11 rules
     * @return true or false
     */
    public static boolean isXMLName(final String s, final boolean xml11Version) {
        if (s == null) {
            return false;
        }
        if (!xml11Version) {
            return XMLChar.isValidName(s);
        }

        return XML11Char.isXML11ValidName(s);
    }

    /**
     * Checks if the given qualified name is legal with respect to the version of
     * XML to which this document must conform.
     *
     * @param prefix       prefix of qualified name
     * @param local        local part of qualified name
     * @param xml11Version if true use xml 11 rules
     * @return true or false
     */
    public static boolean isValidQName(final String prefix, final String local, final boolean xml11Version) {

        // check that both prefix and local part match NCName
        if (local == null) {
            return false;
        }

        final boolean validNCName;

        if (!xml11Version) {
            validNCName = (prefix == null || XMLChar.isValidNCName(prefix)) && XMLChar.isValidNCName(local);
        }
        else {
            validNCName = (prefix == null || XML11Char.isXML11ValidNCName(prefix))
                    && XML11Char.isXML11ValidNCName(local);
        }

        return validNCName;
    }

    /**
     * Uses the kidOK lookup table to check whether the proposed tree structure is
     * legal.
     *
     * @param parent the parent
     * @param child  the child
     * @return true or false
     */
    protected boolean isKidOK(final Node parent, final Node child) {
        return 0 != (kidOK[parent.getNodeType()] & 1 << child.getNodeType());
    }

    /**
     * Denotes that this node has changed.
     */
    @Override
    protected void changed() {
        changes++;
    }

    /**
     * Returns the number of changes to this node.
     */
    @Override
    protected int changes() {
        return changes;
    }

    /**
     * Returns a NodeListCache for the given node.
     */
    NodeListCache getNodeListCache(final ParentNode owner) {
        if (fFreeNLCache_ == null) {
            return new NodeListCache(owner);
        }
        final NodeListCache c = fFreeNLCache_;
        fFreeNLCache_ = fFreeNLCache_.next;
        c.fChild = null;
        c.fChildIndex = -1;
        c.fLength = -1;
        // revoke previous ownership
        if (c.fOwner != null) {
            c.fOwner.fNodeListCache = null;
        }
        c.fOwner = owner;
        // c.next = null; not necessary, except for confused people...
        return c;
    }

    /**
     * Puts the given NodeListCache in the free list. Note: The owner node can keep
     * using it until we reuse it
     */
    void freeNodeListCache(final NodeListCache c) {
        c.next = fFreeNLCache_;
        fFreeNLCache_ = c;
    }

    protected final void checkNamespaceWF(final String qname, final int colon1, final int colon2) {

        if (!errorChecking) {
            return;
        }
        // it is an error for NCName to have more than one ':'
        // check if it is valid QName [Namespace in XML production 6]
        // :camera , nikon:camera:minolta, camera:
        if (colon1 == 0 || colon1 == qname.length() - 1 || colon2 != colon1) {
            final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NAMESPACE_ERR", null);
            throw new DOMException(DOMException.NAMESPACE_ERR, msg);
        }
    }

    protected final void checkDOMNSErr(final String prefix, final String namespace) {
        if (errorChecking) {
            if (namespace == null) {
                final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NAMESPACE_ERR", null);
                throw new DOMException(DOMException.NAMESPACE_ERR, msg);
            }
            else if ("xml".equals(prefix) && !namespace.equals(NamespaceContext.XML_URI)) {
                final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NAMESPACE_ERR", null);
                throw new DOMException(DOMException.NAMESPACE_ERR, msg);
            }
            else if ("xmlns".equals(prefix) && !namespace.equals(NamespaceContext.XMLNS_URI)
                    || (!"xmlns".equals(prefix) && namespace.equals(NamespaceContext.XMLNS_URI))) {
                final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NAMESPACE_ERR", null);
                throw new DOMException(DOMException.NAMESPACE_ERR, msg);
            }
        }
    }

    /**
     * Checks if the given qualified name is legal with respect to the version of
     * XML to which this document must conform.
     *
     * @param prefix prefix of qualified name
     * @param local  local part of qualified name
     */
    protected final void checkQName(final String prefix, final String local) {
        if (!errorChecking) {
            return;
        }

        // check that both prefix and local part match NCName
        final boolean validNCName;
        if (!xml11Version_) {
            validNCName = (prefix == null || XMLChar.isValidNCName(prefix)) && XMLChar.isValidNCName(local);
        }
        else {
            validNCName = (prefix == null || XML11Char.isXML11ValidNCName(prefix))
                    && XML11Char.isXML11ValidNCName(local);
        }

        if (!validNCName) {
            // REVISIT: add qname parameter to the message
            final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_CHARACTER_ERR", null);
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
        }
    }

    /**
     * We could have more xml versions in future , but for now we could do with this
     * to handle XML 1.0 and 1.1
     */
    boolean isXML11Version() {
        return xml11Version_;
    }

    protected void addEventListener(final NodeImpl node, final String type, final EventListener listener, final boolean useCapture) {
        // does nothing by default - overidden in subclass
    }

    protected void removeEventListener(final NodeImpl node, final String type, final EventListener listener, final boolean useCapture) {
        // does nothing by default - overidden in subclass
    }

    protected void copyEventListeners(final NodeImpl src, final NodeImpl tgt) {
        // does nothing by default - overidden in subclass
    }

    /**
     * A method to be called when some text was changed in a text node, so that live
     * objects can be notified.
     */
    void replacedText(final CharacterDataImpl node) {
    }

    /**
     * A method to be called when some text was deleted from a text node, so that
     * live objects can be notified.
     */
    void deletedText(final CharacterDataImpl node, final int offset, final int count) {
    }

    /**
     * A method to be called when some text was inserted into a text node, so that
     * live objects can be notified.
     */
    void insertedText(final CharacterDataImpl node, final int offset, final int count) {
    }

    /**
     * A method to be called when a character data node is about to be modified
     */
    void modifyingCharacterData(final NodeImpl node, final boolean replace) {
    }

    /**
     * A method to be called when a character data node has been modified
     */
    void modifiedCharacterData(final NodeImpl node, final String oldvalue, final String value, final boolean replace) {
    }

    /**
     * A method to be called when a node is about to be inserted in the tree.
     */
    void insertingNode(final NodeImpl node, final boolean replace) {
    }

    /**
     * A method to be called when a node has been inserted in the tree.
     */
    void insertedNode(final NodeImpl node, final NodeImpl newInternal, final boolean replace) {
    }

    /**
     * A method to be called when a node is about to be removed from the tree.
     */
    void removingNode(final NodeImpl node, final NodeImpl oldChild, final boolean replace) {
    }

    /**
     * A method to be called when a node has been removed from the tree.
     */
    void removedNode(final NodeImpl node, final boolean replace) {
    }

    /**
     * A method to be called when a node is about to be replaced in the tree.
     */
    void replacingNode(final NodeImpl node) {
    }

    /**
     * A method to be called when a node has been replaced in the tree.
     */
    void replacedNode(final NodeImpl node) {
    }

    /**
     * A method to be called when a character data node is about to be replaced
     */
    void replacingData(final NodeImpl node) {
    }

    /**
     * method to be called when a character data node has been replaced.
     */
    void replacedCharacterData(final NodeImpl node, final String oldvalue, final String value) {
    }

    /**
     * A method to be called when an attribute value has been modified
     */
    void modifiedAttrValue(final AttrImpl attr, final String oldvalue) {
    }

    /**
     * A method to be called when an attribute node has been set
     */
    void setAttrNode(final AttrImpl attr, final AttrImpl previous) {
    }

    /**
     * A method to be called when an attribute node has been removed
     */
    void removedAttrNode(final AttrImpl attr, final NodeImpl oldOwner, final String name) {
    }

    /**
     * A method to be called when an attribute node has been renamed
     */
    void renamedAttrNode(final Attr oldAt, final Attr newAt) {
    }

    /**
     * A method to be called when an element has been renamed
     */
    void renamedElement(final Element oldEl, final Element newEl) {
    }
}
