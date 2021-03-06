/*
 * Copyright 2002-2009 Andy Clark, Marc Guillemot
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ==============================================================
 * This file contains some code from Apache Xerces-J which is
 * used in accordance with the Apache license.
 */

package net.sourceforge.htmlunit.cyberneko.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.ErrorHandlerWrapper;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParseException;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
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

import net.sourceforge.htmlunit.cyberneko.HTMLConfiguration;

/**
 * A DOM parser for HTML fragments.
 *
 * @author Andy Clark
 */
public class DOMFragmentParser
    implements XMLDocumentHandler {

    //
    // Constants
    //

    // features

    /** Document fragment balancing only. */
    protected static final String DOCUMENT_FRAGMENT =
        "http://cyberneko.org/html/features/document-fragment";

    /** Recognized features. */
    protected static final String[] RECOGNIZED_FEATURES = {
        DOCUMENT_FRAGMENT,
    };

    // properties

    /** Property identifier: error handler. */
    protected static final String ERROR_HANDLER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_HANDLER_PROPERTY;

    /** Current element node. */
    protected static final String CURRENT_ELEMENT_NODE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.CURRENT_ELEMENT_NODE_PROPERTY;

    /** Recognized properties. */
    protected static final String[] RECOGNIZED_PROPERTIES = {
        ERROR_HANDLER,
        CURRENT_ELEMENT_NODE,
    };

    //
    // Data
    //

    /** Parser configuration. */
    protected XMLParserConfiguration fParserConfiguration;

    /** Document source. */
    protected XMLDocumentSource fDocumentSource;

    /** DOM document fragment. */
    protected DocumentFragment fDocumentFragment;

    /** Document. */
    protected Document fDocument;

    /** Current node. */
    protected Node fCurrentNode;

    /** True if within a CDATA section. */
    protected boolean fInCDATASection;

    //
    // Constructors
    //

    /** Default constructor. */
    public DOMFragmentParser() {
        fParserConfiguration = new HTMLConfiguration();
        fParserConfiguration.addRecognizedFeatures(RECOGNIZED_FEATURES);
        fParserConfiguration.addRecognizedProperties(RECOGNIZED_PROPERTIES);
        fParserConfiguration.setFeature(DOCUMENT_FRAGMENT, true);
        fParserConfiguration.setDocumentHandler(this);
    }

    //
    // Public methods
    //

    /**
     * Parses a document fragment
     * @param systemId systemId
     * @param fragment fragment
     * @throws SAXException in case of sax error
     * @throws IOException in case of io problems
     */
    public void parse(String systemId, DocumentFragment fragment)
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
    public void parse(InputSource source, DocumentFragment fragment)
        throws SAXException, IOException {

        fCurrentNode = fDocumentFragment = fragment;
        fDocument = fDocumentFragment.getOwnerDocument();

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

            fParserConfiguration.parse(inputSource);
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
    public void setErrorHandler(ErrorHandler errorHandler) {
        fParserConfiguration.setErrorHandler(new ErrorHandlerWrapper(errorHandler));
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
                (XMLErrorHandler)fParserConfiguration.getProperty(ERROR_HANDLER);
            if (xmlErrorHandler != null &&
                xmlErrorHandler instanceof ErrorHandlerWrapper) {
                errorHandler = ((ErrorHandlerWrapper)xmlErrorHandler).getErrorHandler();
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
    public void setFeature(String featureId, boolean state)
        throws SAXNotRecognizedException, SAXNotSupportedException {

        try {
            fParserConfiguration.setFeature(featureId, state);
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
     *
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
    public boolean getFeature(String featureId)
        throws SAXNotRecognizedException, SAXNotSupportedException {

        try {
            return fParserConfiguration.getFeature(featureId);
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
    public void setProperty(String propertyId, Object value)
        throws SAXNotRecognizedException, SAXNotSupportedException {

        try {
            fParserConfiguration.setProperty(propertyId, value);
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
     * Query the value of a property.
     *
     * Return the current value of a property in a SAX2 parser.
     * The parser might not recognize the property.
     *
     * @param propertyId The unique identifier (URI) of the property
     *                   being set.
     * @return The current value of the property.
     * @exception org.xml.sax.SAXNotRecognizedException If the
     *            requested property is not known.
     * @exception SAXNotSupportedException If the
     *            requested property is known but not supported.
     */
    public Object getProperty(String propertyId)
        throws SAXNotRecognizedException, SAXNotSupportedException {

        if (propertyId.equals(CURRENT_ELEMENT_NODE)) {
            return (fCurrentNode!=null &&
                    fCurrentNode.getNodeType() == Node.ELEMENT_NODE)? fCurrentNode:null;
        }

        try {
            return fParserConfiguration.getProperty(propertyId);
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
    public void setDocumentSource(XMLDocumentSource source) {
        fDocumentSource = source;
    }

    /** Returns the document source. */
    @Override
    public XMLDocumentSource getDocumentSource() {
        return fDocumentSource;
    }

    // Start document.
    public void startDocument(XMLLocator locator, String encoding,
                              Augmentations augs) throws XNIException {
        startDocument(locator, encoding, null, augs);
    }

    // since Xerces 2.2.0

    // Start document.
    @Override
    public void startDocument(XMLLocator locator, String encoding,
                              NamespaceContext nscontext,
                              Augmentations augs) throws XNIException {
        fInCDATASection = false;
    }

    // XML declaration.
    @Override
    public void xmlDecl(String version, String encoding,
                        String standalone, Augmentations augs)
        throws XNIException {
    }

    // Document type declaration.
    @Override
    public void doctypeDecl(String root, String pubid, String sysid,
                            Augmentations augs) throws XNIException {
    }

    // Processing instruction.
    @Override
    public void processingInstruction(final String target, final XMLString data,
            final Augmentations augs)
        throws XNIException {

        final String s = data.toString();
        if (XMLChar.isValidName(s)) {
            final ProcessingInstruction pi = fDocument.createProcessingInstruction(target, s);
            fCurrentNode.appendChild(pi);
        }
    }

    // Comment.
    @Override
    public void comment(XMLString text, Augmentations augs)
        throws XNIException {
        final Comment comment = fDocument.createComment(text.toString());
        fCurrentNode.appendChild(comment);
    }

    // Start element.
    @Override
    public void startElement(QName element, XMLAttributes attrs,
                             Augmentations augs) throws XNIException {
        final Element elementNode = fDocument.createElement(element.rawname);

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
        fCurrentNode.appendChild(elementNode);
        fCurrentNode = elementNode;
    }

    // Empty element.
    @Override
    public void emptyElement(QName element, XMLAttributes attrs,
                             Augmentations augs) throws XNIException {
        startElement(element, attrs, augs);
        endElement(element, augs);
    }

    // Characters.
    @Override
    public void characters(XMLString text, Augmentations augs)
        throws XNIException {

        if (fInCDATASection) {
            final Node node = fCurrentNode.getLastChild();
            if (node != null && node.getNodeType() == Node.CDATA_SECTION_NODE) {
                final CDATASection cdata = (CDATASection)node;
                cdata.appendData(text.toString());
            }
            else {
                final CDATASection cdata = fDocument.createCDATASection(text.toString());
                fCurrentNode.appendChild(cdata);
            }
        }
        else {
            final Node node = fCurrentNode.getLastChild();
            if (node != null && node.getNodeType() == Node.TEXT_NODE) {
                final Text textNode = (Text)node;
                textNode.appendData(text.toString());
            }
            else {
                final Text textNode = fDocument.createTextNode(text.toString());
                fCurrentNode.appendChild(textNode);
            }
        }

    }

    /** Ignorable whitespace. */
    @Override
    public void ignorableWhitespace(XMLString text, Augmentations augs)
        throws XNIException {
        characters(text, augs);
    }

    /** Start general entity. */
    @Override
    public void startGeneralEntity(String name, XMLResourceIdentifier id,
                                   String encoding, Augmentations augs)
        throws XNIException {
        final EntityReference entityRef = fDocument.createEntityReference(name);
        fCurrentNode.appendChild(entityRef);
        fCurrentNode = entityRef;
    }

    /** Text declaration. */
    @Override
    public void textDecl(String version, String encoding,
                         Augmentations augs) throws XNIException {
    }

    /** End general entity. */
    @Override
    public void endGeneralEntity(String name, Augmentations augs)
        throws XNIException {
        fCurrentNode = fCurrentNode.getParentNode();
    }

    /** Start CDATA section. */
    @Override
    public void startCDATA(Augmentations augs) throws XNIException {
        fInCDATASection = true;
    }

    /** End CDATA section. */
    @Override
    public void endCDATA(Augmentations augs) throws XNIException {
        fInCDATASection = false;
    }

    /** End element. */
    @Override
    public void endElement(QName element, Augmentations augs)
        throws XNIException {
        fCurrentNode = fCurrentNode.getParentNode();
    }

    /** End document. */
    @Override
    public void endDocument(Augmentations augs) throws XNIException {
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
        HTMLDocument document = new org.apache.html.dom.HTMLDocumentImpl();
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
