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
package org.htmlunit.cyberneko.xerces.parsers;

import org.htmlunit.cyberneko.xerces.xni.Augmentations;
import org.htmlunit.cyberneko.xerces.xni.NamespaceContext;
import org.htmlunit.cyberneko.xerces.xni.QName;
import org.htmlunit.cyberneko.xerces.xni.XMLAttributes;
import org.htmlunit.cyberneko.xerces.xni.XMLDocumentHandler;
import org.htmlunit.cyberneko.xerces.xni.XMLLocator;
import org.htmlunit.cyberneko.xerces.xni.XMLString;
import org.htmlunit.cyberneko.xerces.xni.XNIException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLDocumentSource;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLParserConfiguration;

/**
 * This is the base class for all XML document parsers. XMLDocumentParser
 * provides a common implementation shared by the various document parsers in
 * the Xerces package. While this class is provided for convenience, it does not
 * prevent other kinds of parsers to be constructed using the XNI interfaces.
 *
 * @author Arnaud Le Hors, IBM
 * @author Andy Clark, IBM
 */
public abstract class AbstractXMLDocumentParser extends XMLParser implements XMLDocumentHandler {

    /** Document source */
    protected XMLDocumentSource fDocumentSource;

    /**
     * Constructs a document parser using the default symbol table and grammar pool.
     *
     * @param config the config
     */
    protected AbstractXMLDocumentParser(final XMLParserConfiguration config) {
        super(config);

        // set handlers
        config.setDocumentHandler(this);
    }

    /**
     * The start of the document.
     *
     * @param locator          The system identifier of the entity if the entity is
     *                         external, null otherwise.
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
     * @param augs             Additional information that may include infoset
     *                         augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */

    @Override
    public void startDocument(final XMLLocator locator, final String encoding, final NamespaceContext namespaceContext, final Augmentations augs) throws XNIException {
    }

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
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void xmlDecl(final String version, final String encoding, final String standalone, final Augmentations augs) throws XNIException {
    }

    /**
     * Notifies of the presence of the DOCTYPE line in the document.
     *
     * @param rootElement The name of the root element.
     * @param publicId    The public identifier if an external DTD or null if the
     *                    external DTD is specified using SYSTEM.
     * @param systemId    The system identifier if an external DTD, null
     * @param augs        Additional information that may include infoset
     *                    augmentations otherwise.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void doctypeDecl(final String rootElement, final String publicId, final String systemId, final Augmentations augs)
            throws XNIException {
    }

    /**
     * The start of an element. If the document specifies the start element by using
     * an empty tag, then the startElement method will immediately be followed by
     * the endElement method, with no intervening methods.
     *
     * @param element    The name of the element.
     * @param attributes The element attributes.
     * @param augs       Additional information that may include infoset
     *                   augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void startElement(final QName element, final XMLAttributes attributes, final Augmentations augs) throws XNIException {
    }

    /**
     * An empty element.
     *
     * @param element    The name of the element.
     * @param attributes The element attributes.
     * @param augs       Additional information that may include infoset
     *                   augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void emptyElement(final QName element, final XMLAttributes attributes, final Augmentations augs) throws XNIException {
        startElement(element, attributes, augs);
        endElement(element, augs);
    }

    /**
     * Character content.
     *
     * @param text The content.
     * @param augs Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void characters(final XMLString text, final Augmentations augs) throws XNIException {
    }

    /**
     * Ignorable whitespace. For this method to be called, the document source must
     * have some way of determining that the text containing only whitespace
     * characters should be considered ignorable. For example, the validator can
     * determine if a length of whitespace characters in the document are ignorable
     * based on the element content model.
     *
     * @param text The ignorable whitespace.
     * @param augs Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void ignorableWhitespace(final XMLString text, final Augmentations augs) throws XNIException {
    }

    /**
     * The end of an element.
     *
     * @param element The name of the element.
     * @param augs    Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void endElement(final QName element, final Augmentations augs) throws XNIException {
    }

    /**
     * The start of a CDATA section.
     *
     * @param augs Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void startCDATA(final Augmentations augs) throws XNIException {
    }

    /**
     * The end of a CDATA section.
     *
     * @param augs Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void endCDATA(final Augmentations augs) throws XNIException {
    }

    /**
     * The end of the document.
     *
     * @param augs Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void endDocument(final Augmentations augs) throws XNIException {
    }

    /**
     * This method notifies the start of an entity.
     * <p>
     * <strong>Note:</strong> This method is not called for entity references
     * appearing as part of attribute values.
     *
     * @param name     The name of the entity.
     * @param encoding The auto-detected IANA encoding name of the entity stream.
     *                 This value will be null in those situations where the entity
     *                 encoding is not auto-detected (e.g. internal entities or a
     *                 document entity that is parsed from a java.io.Reader).
     * @param augs     Additional information that may include infoset augmentations
     *
     * @exception XNIException Thrown by handler to signal an error.
     */
    @Override
    public void startGeneralEntity(final String name, final String encoding, final Augmentations augs) throws XNIException {
    }

    /**
     * Notifies of the presence of a TextDecl line in an entity. If present, this
     * method will be called immediately following the startEntity call.
     * <p>
     * <strong>Note:</strong> This method will never be called for the document
     * entity; it is only called for external general entities referenced in
     * document content.
     * <p>
     * <strong>Note:</strong> This method is not called for entity references
     * appearing as part of attribute values.
     *
     * @param version  The XML version, or null if not specified.
     * @param encoding The IANA encoding name of the entity.
     * @param augs     Additional information that may include infoset augmentations
     *
     * @exception XNIException Thrown by handler to signal an error.
     */
    @Override
    public void textDecl(final String version, final String encoding, final Augmentations augs) throws XNIException {
    }

    /**
     * This method notifies the end of an entity.
     * <p>
     * <strong>Note:</strong> This method is not called for entity references
     * appearing as part of attribute values.
     *
     * @param name The name of the entity.
     * @param augs Additional information that may include infoset augmentations
     *
     * @exception XNIException Thrown by handler to signal an error.
     */
    @Override
    public void endGeneralEntity(final String name, final Augmentations augs) throws XNIException {
    }

    /**
     * A comment.
     *
     * @param text The text in the comment.
     * @param augs Additional information that may include infoset augmentations
     *
     * @exception XNIException Thrown by application to signal an error.
     */
    @Override
    public void comment(final XMLString text, final Augmentations augs) throws XNIException {
    }

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
    @Override
    public void processingInstruction(final String target, final XMLString data, final Augmentations augs) throws XNIException {
    }

    /** Sets the document source */
    @Override
    public void setDocumentSource(final XMLDocumentSource source) {
        fDocumentSource = source;
    }

    /** Returns the document source */
    @Override
    public XMLDocumentSource getDocumentSource() {
        return fDocumentSource;
    }
}
