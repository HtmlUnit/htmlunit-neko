/*
 * Copyright 2002-2009 Andy Clark, Marc Guillemot
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
package org.htmlunit.cyberneko.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.htmlunit.cyberneko.HTMLConfiguration;
import org.htmlunit.cyberneko.xerces.impl.Constants;
import org.htmlunit.cyberneko.xerces.util.ErrorHandlerWrapper;
import org.htmlunit.cyberneko.xerces.util.XMLChar;
import org.htmlunit.cyberneko.xerces.xni.Augmentations;
import org.htmlunit.cyberneko.xerces.xni.NamespaceContext;
import org.htmlunit.cyberneko.xerces.xni.QName;
import org.htmlunit.cyberneko.xerces.xni.XMLAttributes;
import org.htmlunit.cyberneko.xerces.xni.XMLDocumentHandler;
import org.htmlunit.cyberneko.xerces.xni.XMLLocator;
import org.htmlunit.cyberneko.xerces.xni.XMLString;
import org.htmlunit.cyberneko.xerces.xni.XNIException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLConfigurationException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLDocumentSource;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLErrorHandler;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLInputSource;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLParseException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLParserConfiguration;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;

/**
 * A DOM parser for HTML fragments.
 *
 * @author Andy Clark
 */
public class DOMFragmentParser implements XMLDocumentHandler {

    /** Document fragment balancing only. */
    protected static final String DOCUMENT_FRAGMENT = "http://cyberneko.org/html/features/document-fragment";

    /** Recognized features. */
    protected static final String[] RECOGNIZED_FEATURES = {
        DOCUMENT_FRAGMENT,
    };

    /** Property identifier: error handler. */
    protected static final String ERROR_HANDLER = Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_HANDLER_PROPERTY;

    /** Recognized properties. */
    protected static final String[] RECOGNIZED_PROPERTIES = {
        ERROR_HANDLER,
    };

    /** Parser configuration. */
    private final XMLParserConfiguration parserConfiguration_;

    /** Document source. */
    private XMLDocumentSource documentSource_;

    /** DOM document fragment. */
    private DocumentFragment documentFragment_;

    /** Document. */
    private Document document_;

    /** Current node. */
    private Node currentNode_;

    /** True if within a CDATA section. */
    private boolean inCDATASection_;

    /** Default constructor. */
    public DOMFragmentParser() {
        parserConfiguration_ = new HTMLConfiguration();
        parserConfiguration_.addRecognizedFeatures(RECOGNIZED_FEATURES);
        parserConfiguration_.addRecognizedProperties(RECOGNIZED_PROPERTIES);
        parserConfiguration_.setFeature(DOCUMENT_FRAGMENT, true);
        parserConfiguration_.setDocumentHandler(this);
    }

    /**
     * Parses a document fragment
     * @param systemId systemId
     * @param fragment fragment
     * @throws SAXException in case of sax error
     * @throws IOException in case of io problems
     */
    public void parse(final String systemId, final DocumentFragment fragment)
        throws SAXException, IOException {
        parse(new InputSource(systemId), fragment);
    }

    /**
     * Parses a document fragment
     * @param source input source
     * @param fragment fragment
     * @throws SAXException in case of sax error
     * @throws IOException in case of io problems
     */
    public void parse(final InputSource source, final DocumentFragment fragment)
        throws SAXException, IOException {

        currentNode_ = fragment;
        documentFragment_ = fragment;
        document_ = documentFragment_.getOwnerDocument();

        try {
            final String pubid = source.getPublicId();
            final String sysid = source.getSystemId();
            final String encoding = source.getEncoding();
            final InputStream stream = source.getByteStream();
            final Reader reader = source.getCharacterStream();

            final XMLInputSource inputSource = new XMLInputSource(pubid, sysid, sysid);
            inputSource.setEncoding(encoding);
            inputSource.setByteStream(stream);
            inputSource.setCharacterStream(reader);

            parserConfiguration_.parse(inputSource);
        }
        catch (final XMLParseException e) {
            final Exception ex = e.getException();
            if (ex != null) {
                throw new SAXParseException(e.getMessage(), null, ex);
            }
            throw new SAXParseException(e.getMessage(), null);
        }
    }

    /**
     * Allow an application to register an error event handler.
     *
     * <p>If the application does not register an error handler, all
     * error events reported by the SAX parser will be silently
     * ignored; however, normal processing may not continue.  It is
     * highly recommended that all SAX applications implement an
     * error handler to avoid unexpected bugs.</p>
     *
     * <p>Applications may register a new or different handler in the
     * middle of a parse, and the SAX parser must begin using the new
     * handler immediately.</p>
     *
     * @param errorHandler The error handler.
     * @exception java.lang.NullPointerException If the handler
     *            argument is null.
     * @see #getErrorHandler
     */
    public void setErrorHandler(final ErrorHandler errorHandler) {
        parserConfiguration_.setErrorHandler(new ErrorHandlerWrapper(errorHandler));
    }

    /**
     * Return the current error handler.
     *
     * @return The current error handler, or null if none
     *         has been registered.
     * @see #setErrorHandler
     */
    public ErrorHandler getErrorHandler() {

        ErrorHandler errorHandler = null;
        try {
            final XMLErrorHandler xmlErrorHandler =
                (XMLErrorHandler) parserConfiguration_.getProperty(ERROR_HANDLER);
            if (xmlErrorHandler != null && xmlErrorHandler instanceof ErrorHandlerWrapper) {
                errorHandler = ((ErrorHandlerWrapper) xmlErrorHandler).getErrorHandler();
            }
        }
        catch (final XMLConfigurationException e) {
            // do nothing
        }
        return errorHandler;
    }

    /**
     * Set the state of any feature in a SAX2 parser.  The parser
     * might not recognize the feature, and if it does recognize
     * it, it might not be able to fulfill the request.
     *
     * @param featureId The unique identifier (URI) of the feature.
     * @param state The requested state of the feature (true or false).
     *
     * @exception SAXNotRecognizedException If the
     *            requested feature is not known.
     * @exception SAXNotSupportedException If the
     *            requested feature is known, but the requested
     *            state is not supported.
     */
    public void setFeature(final String featureId, final boolean state)
        throws SAXNotRecognizedException, SAXNotSupportedException {

        try {
            parserConfiguration_.setFeature(featureId, state);
        }
        catch (final XMLConfigurationException e) {
            final String message = e.getMessage();
            if (e.getType() == XMLConfigurationException.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(message);
            }
            throw new SAXNotSupportedException(message);
        }
    }

    /**
     * Query the state of a feature.
     * <p>
     * Query the current state of any feature in a SAX2 parser.  The
     * parser might not recognize the feature.
     *
     * @param featureId The unique identifier (URI) of the feature
     *                  being set.
     * @return The current state of the feature.
     * @exception org.xml.sax.SAXNotRecognizedException If the
     *            requested feature is not known.
     * @exception SAXNotSupportedException If the
     *            requested feature is known but not supported.
     */
    public boolean getFeature(final String featureId) throws SAXNotRecognizedException, SAXNotSupportedException {

        try {
            return parserConfiguration_.getFeature(featureId);
        }
        catch (final XMLConfigurationException e) {
            final String message = e.getMessage();
            if (e.getType() == XMLConfigurationException.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(message);
            }
            throw new SAXNotSupportedException(message);
        }
    }

    /**
     * Set the value of any property in a SAX2 parser.  The parser
     * might not recognize the property, and if it does recognize
     * it, it might not support the requested value.
     *
     * @param propertyId The unique identifier (URI) of the property
     *                   being set.
     * @param value      The value to which the property is being set.
     *
     * @exception SAXNotRecognizedException If the
     *            requested property is not known.
     * @exception SAXNotSupportedException If the
     *            requested property is known, but the requested
     *            value is not supported.
     */
    public void setProperty(final String propertyId, final Object value)
        throws SAXNotRecognizedException, SAXNotSupportedException {

        try {
            parserConfiguration_.setProperty(propertyId, value);
        }
        catch (final XMLConfigurationException e) {
            final String message = e.getMessage();
            if (e.getType() == XMLConfigurationException.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(message);
            }
            throw new SAXNotSupportedException(message);
        }
    }

    //
    // XMLDocumentHandler methods
    //

    /** Sets the document source. */
    @Override
    public void setDocumentSource(final XMLDocumentSource source) {
        documentSource_ = source;
    }

    /** Returns the document source. */
    @Override
    public XMLDocumentSource getDocumentSource() {
        return documentSource_;
    }

    // since Xerces 2.2.0

    // Start document.
    @Override
    public void startDocument(final XMLLocator locator, final String encoding, final NamespaceContext nscontext, final Augmentations augs) throws XNIException {
        inCDATASection_ = false;
    }

    // XML declaration.
    @Override
    public void xmlDecl(final String version, final String encoding, final String standalone, final Augmentations augs)
        throws XNIException {
    }

    // Document type declaration.
    @Override
    public void doctypeDecl(final String root, final String pubid, final String sysid, final Augmentations augs) throws XNIException {
    }

    // Processing instruction.
    @Override
    public void processingInstruction(final String target, final XMLString data,
            final Augmentations augs)
        throws XNIException {

        final String s = data.toString();
        if (XMLChar.isValidName(s)) {
            final ProcessingInstruction pi = document_.createProcessingInstruction(target, s);
            currentNode_.appendChild(pi);
        }
    }

    // Comment.
    @Override
    public void comment(final XMLString text, final Augmentations augs)
        throws XNIException {
        final Comment comment = document_.createComment(text.toString());
        currentNode_.appendChild(comment);
    }

    // Start element.
    @Override
    public void startElement(final QName element, final XMLAttributes attrs, final Augmentations augs) throws XNIException {
        final Element elementNode = document_.createElement(element.rawname);

        if (attrs != null) {
            final int count = attrs.getLength();
            for (int i = 0; i < count; i++) {
                final String aname = attrs.getQName(i);
                final String avalue = attrs.getValue(i);
                if (XMLChar.isValidName(aname)) {
                    elementNode.setAttribute(aname, avalue);
                }
            }
        }
        currentNode_.appendChild(elementNode);
        currentNode_ = elementNode;
    }

    // Empty element.
    @Override
    public void emptyElement(final QName element, final XMLAttributes attrs, final Augmentations augs) throws XNIException {
        startElement(element, attrs, augs);
        endElement(element, augs);
    }

    // Characters.
    @Override
    public void characters(final XMLString text, final Augmentations augs)
        throws XNIException {

        if (inCDATASection_) {
            final Node node = currentNode_.getLastChild();
            if (node != null && node.getNodeType() == Node.CDATA_SECTION_NODE) {
                final CDATASection cdata = (CDATASection) node;
                cdata.appendData(text.toString());
            }
            else {
                final CDATASection cdata = document_.createCDATASection(text.toString());
                currentNode_.appendChild(cdata);
            }
        }
        else {
            final Node node = currentNode_.getLastChild();
            if (node != null && node.getNodeType() == Node.TEXT_NODE) {
                final Text textNode = (Text) node;
                textNode.appendData(text.toString());
            }
            else {
                final Text textNode = document_.createTextNode(text.toString());
                currentNode_.appendChild(textNode);
            }
        }

    }

    /** Ignorable whitespace. */
    @Override
    public void ignorableWhitespace(final XMLString text, final Augmentations augs)
        throws XNIException {
        characters(text, augs);
    }

    /** Start general entity. */
    @Override
    public void startGeneralEntity(final String name, final String encoding, final Augmentations augs)
        throws XNIException {
        final EntityReference entityRef = document_.createEntityReference(name);
        currentNode_.appendChild(entityRef);
        currentNode_ = entityRef;
    }

    /** Text declaration. */
    @Override
    public void textDecl(final String version, final String encoding, final Augmentations augs) throws XNIException {
    }

    /** End general entity. */
    @Override
    public void endGeneralEntity(final String name, final Augmentations augs)
        throws XNIException {
        currentNode_ = currentNode_.getParentNode();
    }

    /** Start CDATA section. */
    @Override
    public void startCDATA(final Augmentations augs) throws XNIException {
        inCDATASection_ = true;
    }

    /** End CDATA section. */
    @Override
    public void endCDATA(final Augmentations augs) throws XNIException {
        inCDATASection_ = false;
    }

    /** End element. */
    @Override
    public void endElement(final QName element, final Augmentations augs)
        throws XNIException {
        currentNode_ = currentNode_.getParentNode();
    }

    /** End document. */
    @Override
    public void endDocument(final Augmentations augs) throws XNIException {
    }

    /** getXMLParserConfiguration */
    public XMLParserConfiguration getXMLParserConfiguration() {
        return parserConfiguration_;
    }

    //
    // DEBUG
    //

    /*
    public static void print(Node node) {
        short type = node.getNodeType();
        switch (type) {
            case Node.ELEMENT_NODE: {
                System.out.print('<');
                System.out.print(node.getNodeName());
                org.w3c.dom.NamedNodeMap attrs = node.getAttributes();
                int attrCount = attrs != null ? attrs.getLength() : 0;
                for (int i = 0; i < attrCount; i++) {
                    Node attr = attrs.item(i);
                    System.out.print(' ');
                    System.out.print(attr.getNodeName());
                    System.out.print("='");
                    System.out.print(attr.getNodeValue());
                    System.out.print('\'');
                }
                System.out.print('>');
                break;
            }
            case Node.TEXT_NODE: {
                System.out.print(node.getNodeValue());
                break;
            }
        }
        Node child = node.getFirstChild();
        while (child != null) {
            print(child);
            child = child.getNextSibling();
        }
        if (type == Node.ELEMENT_NODE) {
            System.out.print("</");
            System.out.print(node.getNodeName());
            System.out.print('>');
        }
        else if (type == Node.DOCUMENT_NODE || type == Node.DOCUMENT_FRAGMENT_NODE) {
            System.out.println();
        }
        System.out.flush();
    }

    public static void main(String[] argv) throws Exception {
        DOMFragmentParser parser = new DOMFragmentParser();
        HTMLDocument document = new org.htmlunit.cyberneko.html.dom.HTMLDocumentImpl();
        for (int i = 0; i < argv.length; i++) {
            String sysid = argv[i];
            System.err.println("# "+sysid);
            DocumentFragment fragment = document.createDocumentFragment();
            parser.parse(sysid, fragment);
            print(fragment);
        }
    }
    */
}
