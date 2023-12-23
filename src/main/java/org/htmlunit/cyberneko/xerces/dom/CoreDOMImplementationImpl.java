/*
 * Copyright 2017-2023 Ronald Brill
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

import org.htmlunit.cyberneko.xerces.util.XMLChar;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

/**
 * The DOMImplementation class is description of a particular implementation of
 * the Document Object Model. As such its data is static, shared by all
 * instances of this implementation.
 * <P>
 * The DOM API requires that it be a real object rather than static methods.
 * However, there's nothing that says it can't be a singleton, so that's how
 * I've implemented it.
 * <P>
 * This particular class, along with CoreDocumentImpl, supports the DOM Core and
 * Load/Save (Experimental). Optional modules are supported by the more complete
 * DOMImplementation class along with DocumentImpl.
 */
public class CoreDOMImplementationImpl implements DOMImplementation {

    // Document and doctype counter. Used to assign order to documents and
    // doctypes without owners, on an demand basis. Used for
    // compareDocumentPosition
    private int docAndDoctypeCounter_ = 0;

    /** Dom implementation singleton. */
    private static final CoreDOMImplementationImpl singleton = new CoreDOMImplementationImpl();

    // NON-DOM: Obtain and return the single shared object
    public static DOMImplementation getDOMImplementation() {
        return singleton;
    }

    /**
     * {@inheritDoc}
     *
     * Test if the DOM implementation supports a specific "feature" -- currently
     * meaning language and level thereof.
     *
     * @param feature The package name of the feature to test. In Level 1, supported
     *                values are "HTML" and "XML" (case-insensitive). At this
     *                writing, org.htmlunit.cyberneko.xerces.dom supports only
     *                XML.
     *
     * @param version The version number of the feature being tested. This is
     *                interpreted as "Version of the DOM API supported for the
     *                specified Feature", and in Level 1 should be "1.0"
     *
     * @return true iff this implementation is compatible with the specified feature
     *         and version.
     */
    @Override
    public boolean hasFeature(String feature, final String version) {

        final boolean anyVersion = version == null || version.length() == 0;

        if (feature.startsWith("+")) {
            feature = feature.substring(1);
        }
        return ("Core".equalsIgnoreCase(feature)
                && (anyVersion || "1.0".equals(version) || "2.0".equals(version) || "3.0".equals(version)))
                || ("XML".equalsIgnoreCase(feature)
                        && (anyVersion || "1.0".equals(version) || "2.0".equals(version) || "3.0".equals(version)))
                || ("XMLVersion".equalsIgnoreCase(feature)
                        && (anyVersion || "1.0".equals(version) || "1.1".equals(version)))
                || ("LS".equalsIgnoreCase(feature) && (anyVersion || "3.0".equals(version)))
                || ("ElementTraversal".equalsIgnoreCase(feature) && (anyVersion || "1.0".equals(version)));
    }

    /**
     * {@inheritDoc}
     *
     * Introduced in DOM Level 2.
     * <p>
     *
     * Creates an empty DocumentType node.
     *
     * @param qualifiedName The qualified name of the document type to be created.
     * @param publicID      The document type public identifier.
     * @param systemID      The document type system identifier.
     */
    @Override
    public DocumentType createDocumentType(final String qualifiedName, final String publicID, final String systemID) {
        // REVISIT: this might allow creation of invalid name for DOCTYPE
        // xmlns prefix.
        // also there is no way for a user to turn off error checking.
        checkQName(qualifiedName);
        return new DocumentTypeImpl(null, qualifiedName, publicID, systemID);
    }

    final void checkQName(final String qname) {
        final int index = qname.indexOf(':');
        final int lastIndex = qname.lastIndexOf(':');
        final int length = qname.length();

        // it is an error for NCName to have more than one ':'
        // check if it is valid QName [Namespace in XML production 6]
        if (index == 0 || index == length - 1 || lastIndex != index) {
            final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NAMESPACE_ERR", null);
            throw new DOMException(DOMException.NAMESPACE_ERR, msg);
        }
        int start = 0;
        // Namespace in XML production [6]
        if (index > 0) {
            // check that prefix is NCName
            if (!XMLChar.isNCNameStart(qname.charAt(start))) {
                final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_CHARACTER_ERR", null);
                throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
            }
            for (int i = 1; i < index; i++) {
                if (!XMLChar.isNCName(qname.charAt(i))) {
                    final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_CHARACTER_ERR", null);
                    throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
                }
            }
            start = index + 1;
        }

        // check local part
        if (!XMLChar.isNCNameStart(qname.charAt(start))) {
            // REVISIT: add qname parameter to the message
            final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_CHARACTER_ERR", null);
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
        }
        for (int i = start + 1; i < length; i++) {
            if (!XMLChar.isNCName(qname.charAt(i))) {
                final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_CHARACTER_ERR", null);
                throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Introduced in DOM Level 2.
     * <p>
     *
     * Creates an XML Document object of the specified type with its document
     * element.
     *
     * @param namespaceURI  The namespace URI of the document element to create, or
     *                      null.
     * @param qualifiedName The qualified name of the document element to create.
     * @param doctype       The type of document to be created or null.
     *                      <p>
     *
     *                      When doctype is not null, its Node.ownerDocument
     *                      attribute is set to the document being created.
     * @return Document A new Document object.
     * @throws DOMException WRONG_DOCUMENT_ERR: Raised if doctype has already been
     *                      used with a different document.
     */
    @Override
    public Document createDocument(final String namespaceURI, final String qualifiedName, final DocumentType doctype)
            throws DOMException {
        if (doctype != null && doctype.getOwnerDocument() != null) {
            final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null);
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, msg);
        }
        final CoreDocumentImpl doc = createDocument(doctype);
        // If namespaceURI and qualifiedName are null return a Document with no document
        // element.
        if (qualifiedName != null || namespaceURI != null) {
            final Element e = doc.createElementNS(namespaceURI, qualifiedName);
            doc.appendChild(e);
        }
        return doc;
    }

    protected CoreDocumentImpl createDocument(final DocumentType doctype) {
        return new CoreDocumentImpl(doctype);
    }

    /**
     * {@inheritDoc}
     *
     * DOM Level 3 WD - Experimental.
     */
    @Override
    public Object getFeature(final String feature, final String version) {
        if (singleton.hasFeature(feature, version)) {
            return singleton;
        }
        return null;
    }

    // NON-DOM: increment document/doctype counter
    protected synchronized int assignDocumentNumber() {
        return ++docAndDoctypeCounter_;
    }

    // NON-DOM: increment document/doctype counter
    protected synchronized int assignDocTypeNumber() {
        return ++docAndDoctypeCounter_;
    }
}
