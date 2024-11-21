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
import org.htmlunit.cyberneko.xerces.xni.NamespaceContext;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;

/**
 * ElementNSImpl inherits from ElementImpl and adds namespace support.
 * <P>
 * The qualified name is the node name, and we store localName which is also
 * used in all queries. On the other hand we recompute the prefix when
 * necessary.
 * <p>
 *
 * @author Elena litani, IBM
 * @author Neeraj Bajaj, Sun Microsystems
 */
public class ElementNSImpl extends ElementImpl {

    static final String xmlURI = "http://www.w3.org/XML/1998/namespace";

    /** DOM2: Namespace URI. */
    protected String namespaceURI;

    /** DOM2: localName. */
    protected String localName;

    // DOM2: Constructor for Namespace implementation.
    protected ElementNSImpl(final CoreDocumentImpl ownerDocument, final String namespaceURI, final String qualifiedName)
            throws DOMException {
        super(ownerDocument, qualifiedName);
        setName(namespaceURI, qualifiedName);
    }

    private void setName(final String namespaceURI, final String qname) {
        final String prefix;
        // DOM Level 3: namespace URI is never empty string.
        this.namespaceURI = namespaceURI;
        if (namespaceURI != null) {
            // convert the empty string to 'null'
            this.namespaceURI = (namespaceURI.length() == 0) ? null : namespaceURI;
        }

        final int colon1;
        final int colon2;

        // NAMESPACE_ERR:
        // 1. if the qualified name is 'null' it is malformed.
        // 2. or if the qualifiedName is null and the namespaceURI is different from
        // null,
        // We dont need to check for namespaceURI != null, if qualified name is null
        // throw DOMException.
        if (qname == null) {
            final String msg = DOMMessageFormatter.formatMessage("NAMESPACE_ERR", null);
            throw new DOMException(DOMException.NAMESPACE_ERR, msg);
        }

        colon1 = qname.indexOf(':');
        colon2 = qname.lastIndexOf(':');

        ownerDocument.checkNamespaceWF(qname, colon1, colon2);
        if (colon1 < 0) {
            // there is no prefix
            localName = qname;
            if (ownerDocument.errorChecking) {
                ownerDocument.checkQName(null, localName);
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
            localName = qname.substring(colon2 + 1);

            // NAMESPACE_ERR:
            // 1. if the qualifiedName has a prefix and the namespaceURI is null,

            // 2. or if the qualifiedName has a prefix that is "xml" and the namespaceURI
            // is different from " http://www.w3.org/XML/1998/namespace"

            if (ownerDocument.errorChecking) {
                if (namespaceURI == null || ("xml".equals(prefix) && !namespaceURI.equals(NamespaceContext.XML_URI))) {
                    final String msg = DOMMessageFormatter.formatMessage("NAMESPACE_ERR", null);
                    throw new DOMException(DOMException.NAMESPACE_ERR, msg);
                }

                ownerDocument.checkQName(prefix, localName);
                ownerDocument.checkDOMNSErr(prefix, namespaceURI);
            }
        }
    }

    // when local name is known
    protected ElementNSImpl(final CoreDocumentImpl ownerDocument, final String namespaceURI, final String qualifiedName, final String localName)
            throws DOMException {
        super(ownerDocument, qualifiedName);

        this.localName = localName;
        this.namespaceURI = namespaceURI;
    }

    // Support for DOM Level 3 renameNode method.
    // Note: This only deals with part of the pb. CoreDocumentImpl
    // does all the work.
    void rename(final String namespaceURI, final String qualifiedName) {
        name_ = qualifiedName;
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
        return namespaceURI;
    }

    /**
     * {@inheritDoc}
     *
     * Introduced in DOM Level 2.
     * <p>
     *
     * The namespace prefix of this node, or null if it is unspecified.
     * <p>
     *
     * For nodes created with a DOM Level 1 method, such as createElement from the
     * Document interface, this is null.
     */
    @Override
    public String getPrefix() {
        final int index = name_.indexOf(':');
        return index < 0 ? null : name_.substring(0, index);
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
        if (ownerDocument.errorChecking) {
            if (prefix != null && prefix.length() != 0) {
                if (!CoreDocumentImpl.isXMLName(prefix, ownerDocument.isXML11Version())) {
                    final String msg = DOMMessageFormatter.formatMessage("INVALID_CHARACTER_ERR", null);
                    throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
                }
                if (namespaceURI == null || prefix.indexOf(':') >= 0) {
                    final String msg = DOMMessageFormatter.formatMessage("NAMESPACE_ERR", null);
                    throw new DOMException(DOMException.NAMESPACE_ERR, msg);
                }
                else if ("xml".equals(prefix)) {
                    if (!namespaceURI.equals(xmlURI)) {
                        final String msg = DOMMessageFormatter.formatMessage("NAMESPACE_ERR", null);
                        throw new DOMException(DOMException.NAMESPACE_ERR, msg);
                    }
                }
            }

        }
        // update node name with new qualifiedName
        if (prefix != null && prefix.length() != 0) {
            name_ = prefix + ":" + localName;
        }
        else {
            name_ = localName;
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
        return localName;
    }

    /**
     * {@inheritDoc}
     *
     * NON-DOM Returns the xml:base attribute.
     */
    @Override
    protected Attr getXMLBaseAttribute() {
        return (Attr) attributes_.getNamedItemNS("http://www.w3.org/XML/1998/namespace", "base");
    }

    /**
     * @return type name
     * @see org.w3c.dom.TypeInfo#getTypeName()
     */
    @Override
    public String getTypeName() {
        return null;
    }

    /**
     * @return namespace tyoe
     * @see org.w3c.dom.TypeInfo#getTypeNamespace()
     */
    @Override
    public String getTypeNamespace() {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Introduced in DOM Level 2.
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
}
