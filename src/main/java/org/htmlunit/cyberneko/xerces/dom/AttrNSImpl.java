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
import org.htmlunit.cyberneko.xerces.xni.NamespaceContext;
import org.w3c.dom.DOMException;

/**
 * AttrNSImpl inherits from AttrImpl and adds namespace support.
 * <P>
 * The qualified name is the node name, and we store localName which is also
 * used in all queries. On the other hand we recompute the prefix when
 * necessary.
 * <p>
 *
 * @author Arnaud Le Hors, IBM
 * @author Andy Clark, IBM
 * @author Ralf Pfeiffer, IBM
 */
public class AttrNSImpl extends AttrImpl {

    static final String xmlnsURI = "http://www.w3.org/2000/xmlns/";
    static final String xmlURI = "http://www.w3.org/XML/1998/namespace";

    /** DOM2: Namespace URI. */
    private String namespaceURI_;

    /** DOM2: localName. */
    private String localName_;

    // DOM2: Constructor for Namespace implementation.
    protected AttrNSImpl(final CoreDocumentImpl ownerDocument, final String namespaceURI, final String qualifiedName) {
        super(ownerDocument, qualifiedName);
        setName(namespaceURI, qualifiedName);
    }

    private void setName(final String namespaceURI, final String qname) {
        final CoreDocumentImpl ownerDocument = ownerDocument();
        final String prefix;
        // DOM Level 3: namespace URI is never empty string.
        namespaceURI_ = namespaceURI;
        if (namespaceURI != null) {
            namespaceURI_ = (namespaceURI.isEmpty()) ? null : namespaceURI;

        }
        final int colon1 = qname.indexOf(':');
        final int colon2 = qname.lastIndexOf(':');
        ownerDocument.checkNamespaceWF(qname, colon1, colon2);
        if (colon1 < 0) {
            // there is no prefix
            localName_ = qname;
            if (ownerDocument.errorChecking) {
                ownerDocument.checkQName(null, localName_);

                if ("xmlns".equals(qname) && (namespaceURI == null || !namespaceURI.equals(NamespaceContext.XMLNS_URI))
                        || (namespaceURI != null && namespaceURI.equals(NamespaceContext.XMLNS_URI)
                                && !"xmlns".equals(qname))) {
                    final String msg = DOMMessageFormatter.formatMessage("NAMESPACE_ERR", null);
                    throw new DOMException(DOMException.NAMESPACE_ERR, msg);
                }
            }
        }
        else {
            prefix = qname.substring(0, colon1);
            localName_ = qname.substring(colon2 + 1);
            ownerDocument.checkQName(prefix, localName_);
            ownerDocument.checkDOMNSErr(prefix, namespaceURI);
        }
    }

    // when local name is known
    public AttrNSImpl(final CoreDocumentImpl ownerDocument, final String namespaceURI,
                        final String qualifiedName, final String localName) {
        super(ownerDocument, qualifiedName);

        this.localName_ = localName;
        namespaceURI_ = namespaceURI;
    }

    // Support for DOM Level 3 renameNode method.
    // Note: This only deals with part of the pb. It is expected to be
    // called after the Attr has been detached for one thing.
    // CoreDocumentImpl does all the work.
    void rename(final String namespaceURI, final String qualifiedName) {
        super.rename(qualifiedName);
        setName(namespaceURI, qualifiedName);
    }

    /**
     * {@inheritDoc}
     *
     * Introduced in DOM Level 2.
     * <p>
     *
     * The namespace URI of this node, or null if it is unspecified.
     * <p>
     *
     * This is not a computed value that is the result of a namespace lookup based
     * on an examination of the namespace declarations in scope. It is merely the
     * namespace URI given at creation time.
     * <p>
     *
     * For nodes created with a DOM Level 1 method, such as createElement from the
     * Document interface, this is null.
     */
    @Override
    public String getNamespaceURI() {
        // REVIST: This code could/should be done at a lower-level, such that
        // the namespaceURI is set properly upon creation. However, there still
        // seems to be some DOM spec interpretation grey-area.
        return namespaceURI_;
    }

    /**
     * {@inheritDoc}
     *
     * Introduced in DOM Level 2.
     * <p>
     *
     * The namespace prefix of this node, or null if it is unspecified.
     * <p>
     * For nodes created with a DOM Level 1 method, such as createElement from the
     * Document interface, this is null.
     */
    @Override
    public String getPrefix() {
        final int index = name.indexOf(':');
        return index < 0 ? null : name.substring(0, index);
    }

    /**
     * {@inheritDoc}
     *
     * Introduced in DOM Level 2.
     * <p>
     *
     * Note that setting this attribute changes the nodeName attribute, which holds
     * the qualified name, as well as the tagName and name attributes of the Element
     * and Attr interfaces, when applicable.
     * <p>
     *
     * @param prefix The namespace prefix of this node, or null(empty string) if it
     *               is unspecified.
     *
     * @exception DOMException INVALID_CHARACTER_ERR Raised if the specified prefix
     *                         contains an invalid character.
     */
    @Override
    public void setPrefix(final String prefix) throws DOMException {
        if (ownerDocument().errorChecking) {
            if (prefix != null && !prefix.isEmpty()) {

                if (!CoreDocumentImpl.isXMLName(prefix, ownerDocument().isXML11Version())) {
                    final String msg = DOMMessageFormatter.formatMessage("INVALID_CHARACTER_ERR", null);
                    throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
                }
                if (namespaceURI_ == null || prefix.indexOf(':') >= 0) {
                    final String msg = DOMMessageFormatter.formatMessage("NAMESPACE_ERR", null);
                    throw new DOMException(DOMException.NAMESPACE_ERR, msg);

                }
                if ("xmlns".equals(prefix)) {
                    if (!namespaceURI_.equals(xmlnsURI)) {
                        final String msg = DOMMessageFormatter.formatMessage("NAMESPACE_ERR", null);
                        throw new DOMException(DOMException.NAMESPACE_ERR, msg);
                    }
                }
                else if ("xml".equals(prefix)) {
                    if (!namespaceURI_.equals(xmlURI)) {
                        final String msg = DOMMessageFormatter.formatMessage("NAMESPACE_ERR", null);
                        throw new DOMException(DOMException.NAMESPACE_ERR, msg);
                    }
                }
                else if ("xmlns".equals(name)) {
                    final String msg = DOMMessageFormatter.formatMessage("NAMESPACE_ERR", null);
                    throw new DOMException(DOMException.NAMESPACE_ERR, msg);
                }
            }
        }

        // update node name with new qualifiedName
        if (prefix != null && !prefix.isEmpty()) {
            name = prefix + ":" + localName_;
        }
        else {
            name = localName_;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Introduced in DOM Level 2.
     * <p>
     *
     * Returns the local part of the qualified name of this node.
     */
    @Override
    public String getLocalName() {
        return localName_;
    }

    /**
     * {@inheritDoc}
     *
     * Introduced in DOM Level 3.
     * <p>
     * Checks if a type is derived from another by restriction. See:
     * <a href="http://www.w3.org/TR/DOM-Level-3-Core/core.html#TypeInfo-isDerivedFrom">...</a>
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
}
