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
package org.htmlunit.cyberneko.xerces.xni;

import org.htmlunit.cyberneko.xerces.xni.parser.XMLDocumentSource;

/**
 * The document handler interface defines callback methods to report information
 * items in XML documents. Parser components interested in document information
 * implement this interface and are registered as the document handler on the
 * document source.
 *
 * @author Andy Clark, IBM
 */
public interface XMLDocumentHandler {
    /**
     * The start of the document.
     *
     * @param locator          The document locator, or null if the document
     *                         location cannot be reported during the parsing of
     *                         this document. However, it is <em>strongly</em>
     *                         recommended that a locator be supplied that can at
     *                         least report the system identifier of the document.
     * @param encoding         The auto-detected IANA encoding name of the entity
     *                         stream. This value will be null in those situations
     *                         where the entity encoding is not auto-detected (e.g.
     *                         internal entities or a document entity that is parsed
     *                         from a java.io.Reader).
     * @param namespaceContext The namespace context in effect at the start of this
     *                         document. This object represents the current context.
     *                         Implementors of this class are responsible for
     *                         copying the namespace bindings from the the current
     *                         context (and its parent contexts) if that information
     *                         is important.
     *
     * @param augs             Additional information that may include infoset
     *                         augmentations
     * @exception XNIException Thrown by handler to signal an error.
     */
    void startDocument(XMLLocator locator, String encoding, NamespaceContext namespaceContext, Augmentations augs)
            throws XNIException;

    /**
     * Notifies of the presence of an XMLDecl line in the document. If present, this
     * method will be called immediately following the startDocument call.
     *
     * @param version    The XML version.
     * @param encoding   The IANA encoding name of the document, or null if not
     *                   specified.
     * @param standalone The standalone value, or null if not specified.
     * @param augs       Additional information that may include infoset
     *                   augmentations
     *
     * @exception XNIException Thrown by handler to signal an error.
     */
    void xmlDecl(String version, String encoding, String standalone, Augmentations augs) throws XNIException;

    /**
     * Notifies of the presence of the DOCTYPE line in the document.
     *
     * @param rootElement The name of the root element.
     * @param publicId    The public identifier if an external DTD or null if the
     *                    external DTD is specified using SYSTEM.
     * @param systemId    The system identifier if an external DTD, null otherwise.
     * @param augs        Additional information that may include infoset
     *                    augmentations
     *
     * @exception XNIException Thrown by handler to signal an error.
     */
    void doctypeDecl(String rootElement, String publicId, String systemId, Augmentations augs) throws XNIException;

    /**
     * A comment.
     *
     * @param text The text in the comment.
     * @param augs Additional information that may include infoset augmentations
     *
     * @exception XNIException Thrown by application to signal an error.
     */
    void comment(XMLString text, Augmentations augs) throws XNIException;

    /**
     * A processing instruction. Processing instructions consist of a target name
     * and, optionally, text data. The data is only meaningful to the application.
     * <p>
     * Typically, a processing instruction's data will contain a series of
     * pseudo-attributes. These pseudo-attributes follow the form of element
     * attributes but are <strong>not</strong> parsed or presented to the
     * application as anything other than text. The application is responsible for
     * parsing the data.
     *
     * @param target The target.
     * @param data   The data or null if none specified.
     * @param augs   Additional information that may include infoset augmentations
     *
     * @exception XNIException Thrown by handler to signal an error.
     */
    void processingInstruction(String target, XMLString data, Augmentations augs) throws XNIException;

    /**
     * The start of an element.
     *
     * @param element    The name of the element.
     * @param attributes The element attributes.
     * @param augs       Additional information that may include infoset
     *                   augmentations
     *
     * @exception XNIException Thrown by handler to signal an error.
     */
    void startElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException;

    /**
     * An empty element.
     *
     * @param element    The name of the element.
     * @param attributes The element attributes.
     * @param augs       Additional information that may include infoset
     *                   augmentations
     *
     * @exception XNIException Thrown by handler to signal an error.
     */
    void emptyElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException;

    /**
     * Character content.
     *
     * @param text The content.
     * @param augs Additional information that may include infoset augmentations
     *
     * @exception XNIException Thrown by handler to signal an error.
     */
    void characters(XMLString text, Augmentations augs) throws XNIException;

    /**
     * The end of an element.
     *
     * @param element The name of the element.
     * @param augs    Additional information that may include infoset augmentations
     *
     * @exception XNIException Thrown by handler to signal an error.
     */
    void endElement(QName element, Augmentations augs) throws XNIException;

    /**
     * The start of a CDATA section.
     *
     * @param augs Additional information that may include infoset augmentations
     *
     * @exception XNIException Thrown by handler to signal an error.
     */
    void startCDATA(Augmentations augs) throws XNIException;

    /**
     * The end of a CDATA section.
     *
     * @param augs Additional information that may include infoset augmentations
     *
     * @exception XNIException Thrown by handler to signal an error.
     */
    void endCDATA(Augmentations augs) throws XNIException;

    /**
     * The end of the document.
     *
     * @param augs Additional information that may include infoset augmentations
     *
     * @exception XNIException Thrown by handler to signal an error.
     */
    void endDocument(Augmentations augs) throws XNIException;

    /**
     * Sets the document source.
     *
     * @param source the new source
     */
    void setDocumentSource(XMLDocumentSource source);

    /**
     * @return the document source.
     */
    XMLDocumentSource getDocumentSource();
}
