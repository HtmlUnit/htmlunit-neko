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

import java.lang.ref.SoftReference;

import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

import net.sourceforge.htmlunit.xerces.impl.RevalidationHandler;
import net.sourceforge.htmlunit.xerces.util.ObjectFactory;
import net.sourceforge.htmlunit.xerces.util.XMLChar;
import net.sourceforge.htmlunit.xerces.xni.grammars.XMLGrammarDescription;

/**
 * The DOMImplementation class is description of a particular
 * implementation of the Document Object Model. As such its data is
 * static, shared by all instances of this implementation.
 * <P>
 * The DOM API requires that it be a real object rather than static
 * methods. However, there's nothing that says it can't be a singleton,
 * so that's how I've implemented it.
 * <P>
 * This particular class, along with CoreDocumentImpl, supports the DOM
 * Core and Load/Save (Experimental). Optional modules are supported by
 * the more complete DOMImplementation class along with DocumentImpl.
 */
public class CoreDOMImplementationImpl
    implements DOMImplementation {

    // validator pools
    private static final int SIZE = 2;

    private SoftReference[] schemaValidators = new SoftReference[SIZE];
    private SoftReference[] xml10DTDValidators = new SoftReference[SIZE];
    private SoftReference[] xml11DTDValidators = new SoftReference[SIZE];

    private int freeSchemaValidatorIndex = -1;
    private int freeXML10DTDValidatorIndex = -1;
    private int freeXML11DTDValidatorIndex = -1;

    private int schemaValidatorsCurrentSize = SIZE;
    private int xml10DTDValidatorsCurrentSize = SIZE;
    private int xml11DTDValidatorsCurrentSize = SIZE;

    private SoftReference[] xml10DTDLoaders = new SoftReference[SIZE];
    private SoftReference[] xml11DTDLoaders = new SoftReference[SIZE];

    private int freeXML10DTDLoaderIndex = -1;
    private int freeXML11DTDLoaderIndex = -1;

    private int xml10DTDLoaderCurrentSize = SIZE;
    private int xml11DTDLoaderCurrentSize = SIZE;

    // Document and doctype counter.  Used to assign order to documents and
    // doctypes without owners, on an demand basis.   Used for
    // compareDocumentPosition
    private int docAndDoctypeCounter = 0;

    /** Dom implementation singleton. */
    static final CoreDOMImplementationImpl singleton = new CoreDOMImplementationImpl();

    // NON-DOM: Obtain and return the single shared object
    public static DOMImplementation getDOMImplementation() {
        return singleton;
    }

    /**
     * {@inheritDoc}
     *
     * Test if the DOM implementation supports a specific "feature" --
     * currently meaning language and level thereof.
     *
     * @param feature The package name of the feature to test.
     * In Level 1, supported values are "HTML" and "XML" (case-insensitive).
     * At this writing, net.sourceforge.htmlunit.xerces.dom supports only XML.
     *
     * @param version The version number of the feature being tested.
     * This is interpreted as "Version of the DOM API supported for the
     * specified Feature", and in Level 1 should be "1.0"
     *
     * @return true iff this implementation is compatible with the specified
     * feature and version.
     */
    @Override
    public boolean hasFeature(String feature, String version) {

        boolean anyVersion = version == null || version.length() == 0;

        if (feature.startsWith("+")) {
            feature = feature.substring(1);
        }
        return (
            feature.equalsIgnoreCase("Core")
                && (anyVersion
                    || version.equals("1.0")
                    || version.equals("2.0")
                    || version.equals("3.0")))
                    || (feature.equalsIgnoreCase("XML")
                && (anyVersion
                    || version.equals("1.0")
                    || version.equals("2.0")
                    || version.equals("3.0")))
                    || (feature.equalsIgnoreCase("XMLVersion")
                && (anyVersion
                    || version.equals("1.0")
                    || version.equals("1.1")))
                    || (feature.equalsIgnoreCase("LS")
                && (anyVersion
                    || version.equals("3.0")))
                    || (feature.equalsIgnoreCase("ElementTraversal")
                && (anyVersion
                    || version.equals("1.0")));
    } // hasFeature(String,String):boolean


    /**
     * {@inheritDoc}
     *
     * Introduced in DOM Level 2. <p>
     *
     * Creates an empty DocumentType node.
     *
     * @param qualifiedName The qualified name of the document type to be created.
     * @param publicID The document type public identifier.
     * @param systemID The document type system identifier.
     */
    @Override
    public DocumentType createDocumentType( String qualifiedName,
                                    String publicID, String systemID) {
        // REVISIT: this might allow creation of invalid name for DOCTYPE
        //          xmlns prefix.
        //          also there is no way for a user to turn off error checking.
        checkQName(qualifiedName);
        return new DocumentTypeImpl(null, qualifiedName, publicID, systemID);
    }

    final void checkQName(String qname){
        int index = qname.indexOf(':');
        int lastIndex = qname.lastIndexOf(':');
        int length = qname.length();

        // it is an error for NCName to have more than one ':'
        // check if it is valid QName [Namespace in XML production 6]
        if (index == 0 || index == length - 1 || lastIndex != index) {
            String msg =
                DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    "NAMESPACE_ERR",
                    null);
            throw new DOMException(DOMException.NAMESPACE_ERR, msg);
        }
        int start = 0;
        // Namespace in XML production [6]
        if (index > 0) {
            // check that prefix is NCName
            if (!XMLChar.isNCNameStart(qname.charAt(start))) {
                String msg =
                    DOMMessageFormatter.formatMessage(
                        DOMMessageFormatter.DOM_DOMAIN,
                        "INVALID_CHARACTER_ERR",
                        null);
                throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
            }
            for (int i = 1; i < index; i++) {
                if (!XMLChar.isNCName(qname.charAt(i))) {
                    String msg =
                        DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "INVALID_CHARACTER_ERR",
                            null);
                    throw new DOMException(
                        DOMException.INVALID_CHARACTER_ERR,
                        msg);
                }
            }
            start = index + 1;
        }

        // check local part
        if (!XMLChar.isNCNameStart(qname.charAt(start))) {
            // REVISIT: add qname parameter to the message
            String msg =
                DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    "INVALID_CHARACTER_ERR",
                    null);
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
        }
        for (int i = start + 1; i < length; i++) {
            if (!XMLChar.isNCName(qname.charAt(i))) {
                String msg =
                    DOMMessageFormatter.formatMessage(
                        DOMMessageFormatter.DOM_DOMAIN,
                        "INVALID_CHARACTER_ERR",
                        null);
                throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * Introduced in DOM Level 2. <p>
     *
     * Creates an XML Document object of the specified type with its document
     * element.
     *
     * @param namespaceURI     The namespace URI of the document
     *                         element to create, or null.
     * @param qualifiedName    The qualified name of the document
     *                         element to create.
     * @param doctype          The type of document to be created or null.<p>
     *
     *                         When doctype is not null, its
     *                         Node.ownerDocument attribute is set to
     *                         the document being created.
     * @return Document        A new Document object.
     * @throws DOMException    WRONG_DOCUMENT_ERR: Raised if doctype has
     *                         already been used with a different document.
     */
    @Override
    public Document createDocument(
        String namespaceURI,
        String qualifiedName,
        DocumentType doctype)
        throws DOMException {
        if (doctype != null && doctype.getOwnerDocument() != null) {
            String msg =
                DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    "WRONG_DOCUMENT_ERR",
                    null);
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, msg);
        }
        CoreDocumentImpl doc = createDocument(doctype);
        // If namespaceURI and qualifiedName are null return a Document with no document element.
        if (qualifiedName != null || namespaceURI != null) {
            Element e = doc.createElementNS(namespaceURI, qualifiedName);
            doc.appendChild(e);
        }
        return doc;
    }

    protected CoreDocumentImpl createDocument(DocumentType doctype) {
        return new CoreDocumentImpl(doctype);
    }

    /**
     * {@inheritDoc}
     *
     * DOM Level 3 WD - Experimental.
     */
    @Override
    public Object getFeature(String feature, String version) {
        if (singleton.hasFeature(feature, version)) {
            return singleton;
        }
        return null;
    }

    // NON-DOM: retrieve validator.
    synchronized RevalidationHandler getValidator(String schemaType, String xmlVersion) {
        if (schemaType == XMLGrammarDescription.XML_SCHEMA) {
            // create new validator - we should not attempt
            // to restrict the number of validation handlers being
            // requested
            while (freeSchemaValidatorIndex >= 0) {
                // return first available validator
                SoftReference ref = schemaValidators[freeSchemaValidatorIndex];
                RevalidationHandlerHolder holder = (RevalidationHandlerHolder) ref.get();
                if (holder != null && holder.handler != null) {
                    RevalidationHandler val = holder.handler;
                    holder.handler = null;
                    --freeSchemaValidatorIndex;
                    return val;
                }
                schemaValidators[freeSchemaValidatorIndex--] = null;
            }
            return (RevalidationHandler) (ObjectFactory
                    .newInstance(
                        "net.sourceforge.htmlunit.xerces.impl.xs.XMLSchemaValidator",
                        ObjectFactory.findClassLoader(),
                        true));
        }
        else if(schemaType == XMLGrammarDescription.XML_DTD) {
            // return an instance of XML11DTDValidator
            if ("1.1".equals(xmlVersion)) {
                while (freeXML11DTDValidatorIndex >= 0) {
                    // return first available validator
                    SoftReference ref = xml11DTDValidators[freeXML11DTDValidatorIndex];
                    RevalidationHandlerHolder holder = (RevalidationHandlerHolder) ref.get();
                    if (holder != null && holder.handler != null) {
                        RevalidationHandler val = holder.handler;
                        holder.handler = null;
                        --freeXML11DTDValidatorIndex;
                        return val;
                    }
                    xml11DTDValidators[freeXML11DTDValidatorIndex--] = null;
                }
                return (RevalidationHandler) (ObjectFactory
                        .newInstance(
                                "net.sourceforge.htmlunit.xerces.impl.dtd.XML11DTDValidator",
                                ObjectFactory.findClassLoader(),
                                true));
            }
            // return an instance of XMLDTDValidator
            else {
                while (freeXML10DTDValidatorIndex >= 0) {
                    // return first available validator
                    SoftReference ref = xml10DTDValidators[freeXML10DTDValidatorIndex];
                    RevalidationHandlerHolder holder = (RevalidationHandlerHolder) ref.get();
                    if (holder != null && holder.handler != null) {
                        RevalidationHandler val = holder.handler;
                        holder.handler = null;
                        --freeXML10DTDValidatorIndex;
                        return val;
                    }
                    xml10DTDValidators[freeXML10DTDValidatorIndex--] = null;
                }
                return (RevalidationHandler) (ObjectFactory
                        .newInstance(
                            "net.sourceforge.htmlunit.xerces.impl.dtd.XMLDTDValidator",
                            ObjectFactory.findClassLoader(),
                            true));
            }
        }
        return null;
    }

    // NON-DOM: release validator
    synchronized void releaseValidator(String schemaType, String xmlVersion,
            RevalidationHandler validator) {
        if (schemaType == XMLGrammarDescription.XML_SCHEMA) {
            ++freeSchemaValidatorIndex;
            if (schemaValidators.length == freeSchemaValidatorIndex) {
                // resize size of the validators
                schemaValidatorsCurrentSize += SIZE;
                SoftReference[] newarray =  new SoftReference[schemaValidatorsCurrentSize];
                System.arraycopy(schemaValidators, 0, newarray, 0, schemaValidators.length);
                schemaValidators = newarray;
            }
            SoftReference ref = schemaValidators[freeSchemaValidatorIndex];
            if (ref != null) {
                RevalidationHandlerHolder holder = (RevalidationHandlerHolder) ref.get();
                if (holder != null) {
                    holder.handler = validator;
                    return;
                }
            }
            schemaValidators[freeSchemaValidatorIndex] = new SoftReference(new RevalidationHandlerHolder(validator));
        }
        else if (schemaType == XMLGrammarDescription.XML_DTD) {
            // release an instance of XML11DTDValidator
            if ("1.1".equals(xmlVersion)) {
                ++freeXML11DTDValidatorIndex;
                if (xml11DTDValidators.length == freeXML11DTDValidatorIndex) {
                    // resize size of the validators
                    xml11DTDValidatorsCurrentSize += SIZE;
                    SoftReference [] newarray = new SoftReference[xml11DTDValidatorsCurrentSize];
                    System.arraycopy(xml11DTDValidators, 0, newarray, 0, xml11DTDValidators.length);
                    xml11DTDValidators = newarray;
                }
                SoftReference ref = xml11DTDValidators[freeXML11DTDValidatorIndex];
                if (ref != null) {
                    RevalidationHandlerHolder holder = (RevalidationHandlerHolder) ref.get();
                    if (holder != null) {
                        holder.handler = validator;
                        return;
                    }
                }
                xml11DTDValidators[freeXML11DTDValidatorIndex] = new SoftReference(new RevalidationHandlerHolder(validator));
            }
            // release an instance of XMLDTDValidator
            else {
                ++freeXML10DTDValidatorIndex;
                if (xml10DTDValidators.length == freeXML10DTDValidatorIndex) {
                    // resize size of the validators
                    xml10DTDValidatorsCurrentSize += SIZE;
                    SoftReference [] newarray = new SoftReference[xml10DTDValidatorsCurrentSize];
                    System.arraycopy(xml10DTDValidators, 0, newarray, 0, xml10DTDValidators.length);
                    xml10DTDValidators = newarray;
                }
                SoftReference ref = xml10DTDValidators[freeXML10DTDValidatorIndex];
                if (ref != null) {
                    RevalidationHandlerHolder holder = (RevalidationHandlerHolder) ref.get();
                    if (holder != null) {
                        holder.handler = validator;
                        return;
                    }
                }
                xml10DTDValidators[freeXML10DTDValidatorIndex] = new SoftReference(new RevalidationHandlerHolder(validator));
            }
        }
    }

    // NON-DOM:  increment document/doctype counter
    protected synchronized int assignDocumentNumber() {
        return ++docAndDoctypeCounter;
    }

    // NON-DOM:  increment document/doctype counter
    protected synchronized int assignDocTypeNumber() {
        return ++docAndDoctypeCounter;
    }

    /**
     * A holder for RevalidationHandlers. This allows us to reuse
     * SoftReferences which haven't yet been cleared by the garbage
     * collector.
     */
    static final class RevalidationHandlerHolder {
        RevalidationHandlerHolder(RevalidationHandler handler) {
            this.handler = handler;
        }
        RevalidationHandler handler;
    }
}
