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
package org.htmlunit.cyberneko.xerces.parsers;

import java.io.CharConversionException;
import java.io.IOException;
import java.util.Stack;

import org.htmlunit.cyberneko.xerces.dom.AttrImpl;
import org.htmlunit.cyberneko.xerces.dom.CoreDocumentImpl;
import org.htmlunit.cyberneko.xerces.dom.DOMMessageFormatter;
import org.htmlunit.cyberneko.xerces.dom.DocumentImpl;
import org.htmlunit.cyberneko.xerces.dom.EntityImpl;
import org.htmlunit.cyberneko.xerces.dom.TextImpl;
import org.htmlunit.cyberneko.xerces.util.ErrorHandlerWrapper;
import org.htmlunit.cyberneko.xerces.util.SAXMessageFormatter;
import org.htmlunit.cyberneko.xerces.xni.Augmentations;
import org.htmlunit.cyberneko.xerces.xni.NamespaceContext;
import org.htmlunit.cyberneko.xerces.xni.QName;
import org.htmlunit.cyberneko.xerces.xni.XMLAttributes;
import org.htmlunit.cyberneko.xerces.xni.XMLLocator;
import org.htmlunit.cyberneko.xerces.xni.XMLString;
import org.htmlunit.cyberneko.xerces.xni.XNIException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLConfigurationException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLErrorHandler;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLInputSource;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLParseException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLParserConfiguration;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;

/**
 * This is the base class of all DOM parsers. It implements the XNI callback
 * methods to create the DOM tree. After a successful parse of an XML document,
 * the DOM Document object can be queried using the <code>getDocument</code>
 * method. The actual pipeline is defined in parser configuration.
 *
 * @author Arnaud Le Hors, IBM
 * @author Andy Clark, IBM
 * @author Elena Litani, IBM
 */
public class AbstractDOMParser extends AbstractXMLDocumentParser {

    /** Feature id: namespace. */
    protected static final String NAMESPACES = Constants.SAX_FEATURE_PREFIX + Constants.NAMESPACES_FEATURE;

    /** Feature id: include comments. */
    protected static final String INCLUDE_COMMENTS_FEATURE = Constants.XERCES_FEATURE_PREFIX
            + Constants.INCLUDE_COMMENTS_FEATURE;

    /** Feature id: create cdata nodes. */
    protected static final String CREATE_CDATA_NODES_FEATURE = Constants.XERCES_FEATURE_PREFIX
            + Constants.CREATE_CDATA_NODES_FEATURE;

    /** Recognized features. */
    private static final String[] RECOGNIZED_FEATURES = {
        NAMESPACES,
        INCLUDE_COMMENTS_FEATURE,
        CREATE_CDATA_NODES_FEATURE};

    /** Recognized properties. */
    private static final String[] RECOGNIZED_PROPERTIES = {};

    private static final boolean DEBUG_EVENTS = false;

    /** Include Comments. */
    protected boolean fIncludeComments;

    /** Create cdata nodes. */
    protected boolean fCreateCDATANodes;

    /** The document. */
    protected Document fDocument;

    /** The default Xerces document implementation, if used. */
    protected CoreDocumentImpl fDocumentImpl;

    /** The document class to use. */
    protected Class<? extends DocumentImpl> fDocumentClass;

    /** The document type node. */
    protected DocumentType fDocumentType;

    /** Current node. */
    protected Node fCurrentNode;
    protected CDATASection fCurrentCDATASection;
    protected EntityImpl fCurrentEntityDecl;

    /** Character buffer */
    protected final XMLString fStringBuffer = new XMLString();

    protected boolean fNamespaceAware;

    /** True if inside CDATA section. */
    protected boolean fInCDATASection;

    /** True if saw the first chunk of characters */
    protected boolean fFirstChunk = false;

    // data

    /** Base uri stack */
    protected final Stack<String> fBaseURIStack = new Stack<>();

    /** Attribute QName. */
    private final QName fAttrQName = new QName();

    /** Document locator. */
    private XMLLocator fLocator;

    // Default constructor.
    protected AbstractDOMParser(final XMLParserConfiguration config, final Class<? extends DocumentImpl> documentClass) {
        super(config);

        // add recognized features
        parserConfiguration_.addRecognizedFeatures(RECOGNIZED_FEATURES);

        // set default values
        parserConfiguration_.setFeature(INCLUDE_COMMENTS_FEATURE, true);
        parserConfiguration_.setFeature(CREATE_CDATA_NODES_FEATURE, true);

        // add recognized properties
        parserConfiguration_.addRecognizedProperties(RECOGNIZED_PROPERTIES);

        setDocumentClass(documentClass);
    }

    /**
     * This method allows the programmer to decide which document factory to use
     * when constructing the DOM tree. However, doing so will lose the functionality
     * of the default factory. Also, a document class other than the default will
     * lose the ability to defer node expansion on the DOM tree produced.
     *
     * @param documentClass The document factory to use when constructing the DOM
     *                      tree.
     */
    protected void setDocumentClass(final Class<? extends DocumentImpl> documentClass) {
        fDocumentClass = documentClass;
    }

    /** @return the DOM document object. */
    public Document getDocument() {
        return fDocument;
    }

    /**
     * Resets the parser state.
     *
     * @throws XNIException Thrown on initialization error.
     */
    @Override
    public void reset() throws XNIException {
        super.reset();

        // get feature state
        fNamespaceAware = parserConfiguration_.getFeature(NAMESPACES);

        fIncludeComments = parserConfiguration_.getFeature(INCLUDE_COMMENTS_FEATURE);

        fCreateCDATANodes = parserConfiguration_.getFeature(CREATE_CDATA_NODES_FEATURE);

        // reset dom information
        fDocument = null;
        fDocumentImpl = null;
        fDocumentType = null;
        fCurrentNode = null;

        // reset string buffer
        fStringBuffer.clear();

        // reset state information
        fInCDATASection = false;
        fFirstChunk = false;
        fCurrentCDATASection = null;

        fBaseURIStack.removeAllElements();
    }

    /**
     * A comment.
     *
     * @param text The text in the comment.
     * @param augs Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by application to signal an error.
     */
    @Override
    public void comment(final XMLString text, final Augmentations augs) throws XNIException {
        if (!fIncludeComments) {
            return;
        }

        final Comment comment = fDocument.createComment(text.toString());
        setCharacterData(false);
        fCurrentNode.appendChild(comment);
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
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void processingInstruction(final String target, final XMLString data, final Augmentations augs) throws XNIException {
        if (DEBUG_EVENTS) {
            System.out.println("==>processingInstruction (" + target + ")");
        }

        final ProcessingInstruction pi = fDocument.createProcessingInstruction(target, data.toString());
        setCharacterData(false);
        fCurrentNode.appendChild(pi);
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

        fLocator = locator;
        if (fDocumentClass == null) {
            fDocument = new DocumentImpl();
            fDocumentImpl = (CoreDocumentImpl) fDocument;
            // REVISIT: when DOM Level 3 is REC rely on Document.support
            // instead of specific class
            // set DOM error checking off
            fDocumentImpl.setStrictErrorChecking(false);
            // set actual encoding
            fDocumentImpl.setInputEncoding(encoding);
            // set documentURI
            fDocumentImpl.setDocumentURI(locator.getExpandedSystemId());
        }
        else {
            // use specified document class
            try {
                fDocument = fDocumentClass.newInstance();
                fDocumentImpl = (CoreDocumentImpl) fDocument;

                // REVISIT: when DOM Level 3 is REC rely on
                // Document.support instead of specific class
                // set DOM error checking off
                fDocumentImpl.setStrictErrorChecking(false);
                // set actual encoding
                fDocumentImpl.setInputEncoding(encoding);
                // set documentURI
                if (locator != null) {
                    fDocumentImpl.setDocumentURI(locator.getExpandedSystemId());
                }
            }
            catch (final Exception e) {
                throw new RuntimeException(DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,
                        "CannotCreateDocumentClass", new Object[] {fDocumentClass.getSimpleName()}));
            }
        }
        fCurrentNode = fDocument;
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
        // REVISIT: when DOM Level 3 is REC rely on Document.support
        // instead of specific class
        if (fDocumentImpl != null) {
            if (version != null) {
                fDocumentImpl.setXmlVersion(version);
            }
            fDocumentImpl.setXmlEncoding(encoding);
            fDocumentImpl.setXmlStandalone("yes".equals(standalone));
        }
    }

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
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void doctypeDecl(final String rootElement, final String publicId, final String systemId, final Augmentations augs)
            throws XNIException {
        if (fDocumentImpl != null) {
            fDocumentType = fDocumentImpl.createDocumentType(rootElement, publicId, systemId);
            fCurrentNode.appendChild(fDocumentType);
        }
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
        if (DEBUG_EVENTS) {
            System.out.println("==>startElement (" + element.getRawname() + ")");
        }

        final Element el = createElementNode(element);
        final int attrCount = attributes.getLength();
        boolean seenSchemaDefault = false;
        for (int i = 0; i < attrCount; i++) {
            attributes.getName(i, fAttrQName);
            final Attr attr = createAttrNode(fAttrQName);

            final String attrValue = attributes.getValue(i);

            attr.setValue(attrValue);
            final boolean specified = attributes.isSpecified(i);
            // Take special care of schema defaulted attributes. Calling the
            // non-namespace aware setAttributeNode() method could overwrite
            // another attribute with the same local name.
            if (!specified && (seenSchemaDefault || (fAttrQName.getUri() != null
                    && fAttrQName.getUri() != NamespaceContext.XMLNS_URI && fAttrQName.getPrefix() == null))) {
                el.setAttributeNodeNS(attr);
                seenSchemaDefault = true;
            }
            else {
                el.setAttributeNode(attr);
            }
            // NOTE: The specified value MUST be set after you set
            // the node value because that turns the "specified"
            // flag to "true" which may overwrite a "false"
            // value from the attribute list. -Ac
            if (fDocumentImpl != null) {
                final AttrImpl attrImpl = (AttrImpl) attr;

                // DTD
                // For DOM Level 3 TypeInfo, the type name must
                // be null if this attribute has not been declared
                // in the DTD.
                attrImpl.setType(null);

                attrImpl.setSpecified(specified);
                // REVISIT: Handle entities in attribute value.
            }
        }
        setCharacterData(false);

        fCurrentNode.appendChild(el);
        fCurrentNode = el;
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
        if (DEBUG_EVENTS) {
            System.out.println("==>characters(): " + text.toString());
        }

        if (fInCDATASection && fCreateCDATANodes) {
            if (fCurrentCDATASection == null) {
                fCurrentCDATASection = fDocument.createCDATASection(text.toString());
                fCurrentNode.appendChild(fCurrentCDATASection);
                fCurrentNode = fCurrentCDATASection;
            }
            else {
                fCurrentCDATASection.appendData(text.toString());
            }
        }
        else {
            // if type is union (XML Schema) it is possible that we receive
            // character call with empty data
            if (text.length() == 0) {
                return;
            }

            final Node child = fCurrentNode.getLastChild();
            if (child != null && child.getNodeType() == Node.TEXT_NODE) {
                // collect all the data into the string buffer.
                if (fFirstChunk) {
                    if (fDocumentImpl != null) {
                        fStringBuffer.append(((TextImpl) child).removeData());
                    }
                    else {
                        fStringBuffer.append(((Text) child).getData());
                        child.setNodeValue(null);
                    }
                    fFirstChunk = false;
                }
                if (text.length() > 0) {
                    fStringBuffer.append(text);
                }
            }
            else {
                fFirstChunk = true;
                final Text textNode = fDocument.createTextNode(text.toString());
                fCurrentNode.appendChild(textNode);
            }

        }
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
        if (DEBUG_EVENTS) {
            System.out.println("==>endElement (" + element.getRawname() + ")");
        }
        setCharacterData(false);
        fCurrentNode = fCurrentNode.getParentNode();
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
        fInCDATASection = true;
        if (fCreateCDATANodes) {
            setCharacterData(false);
        }
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

        fInCDATASection = false;

        if (fCurrentCDATASection != null) {
            fCurrentNode = fCurrentNode.getParentNode();
            fCurrentCDATASection = null;
        }
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
        // REVISIT: when DOM Level 3 is REC rely on Document.support
        // instead of specific class
        // set the actual encoding and set DOM error checking back on
        if (fDocumentImpl != null) {
            if (fLocator != null) {
                fDocumentImpl.setInputEncoding(fLocator.getEncoding());
            }
            fDocumentImpl.setStrictErrorChecking(true);
        }
        fCurrentNode = null;
    }

    /**
     * Record baseURI information for the Element (by adding xml:base attribute) or
     * for the ProcessingInstruction (by setting a baseURI field) Non deferred DOM.
     *
     * @param node the node
     */
    protected final void handleBaseURI(final Node node) {
        if (fDocumentImpl != null) {
            // REVISIT: remove dependency on our implementation when
            // DOM L3 becomes REC

            final String baseURI;
            final short nodeType = node.getNodeType();

            if (nodeType == Node.ELEMENT_NODE) {
                // if an element already has xml:base attribute
                // do nothing
                if (fNamespaceAware) {
                    if (((Element) node).getAttributeNodeNS("http://www.w3.org/XML/1998/namespace", "base") != null) {
                        return;
                    }
                }
                else if (((Element) node).getAttributeNode("xml:base") != null) {
                    return;
                }
                // retrive the baseURI from the entity reference
                baseURI = fCurrentNode.getBaseURI();
                if (baseURI != null && !baseURI.equals(fDocumentImpl.getDocumentURI())) {
                    if (fNamespaceAware) {
                        ((Element) node).setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:base", baseURI);
                    }
                    else {
                        ((Element) node).setAttribute("xml:base", baseURI);
                    }
                }
            }
            else if (nodeType == Node.PROCESSING_INSTRUCTION_NODE) {
                baseURI = fCurrentNode.getBaseURI();
            }
        }
    }

    // method to create an element node.
    // subclasses can override this method to create element nodes in other ways.
    protected Element createElementNode(final QName element) {
        final Element el;

        if (fNamespaceAware) {
            // if we are using xerces DOM implementation, call our
            // own constructor to reuse the strings we have here.
            if (fDocumentImpl != null) {
                el = fDocumentImpl.createElementNS(element.getUri(), element.getRawname(), element.getLocalpart());
            }
            else {
                el = fDocument.createElementNS(element.getUri(), element.getRawname());
            }
        }
        else {
            el = fDocument.createElement(element.getRawname());
        }

        return el;
    }

    // method to create an attribute node.
    // subclasses can override this method to create attribute nodes in other ways.
    protected Attr createAttrNode(final QName attrQName) {
        final Attr attr;

        if (fNamespaceAware) {
            if (fDocumentImpl != null) {
                // if we are using xerces DOM implementation, call our
                // own constructor to reuse the strings we have here.
                attr = fDocumentImpl.createAttributeNS(attrQName.getUri(), attrQName.getRawname(), attrQName.getLocalpart());
            }
            else {
                attr = fDocument.createAttributeNS(attrQName.getUri(), attrQName.getRawname());
            }
        }
        else {
            attr = fDocument.createAttribute(attrQName.getRawname());
        }

        return attr;
    }

    /*
     * When the first characters() call is received, the data is stored in a new
     * Text node. If right after the first characters() we receive another chunk of
     * data, the data from the Text node, following the new characters are appended
     * to the fStringBuffer and the text node data is set to empty.
     *
     * This function is called when the state is changed and the data must be
     * appended to the current node.
     *
     * Note: if DOMFilter is set, you must make sure that if Node is skipped, or
     * removed fFistChunk must be set to true, otherwise some data can be lost.
     *
     */
    protected void setCharacterData(final boolean sawChars) {

        // handle character data
        fFirstChunk = sawChars;

        // if we have data in the buffer we must have created
        // a text node already.

        final Node child = fCurrentNode.getLastChild();
        if (child != null) {
            if (fStringBuffer.length() > 0) {
                // REVISIT: should this check be performed?
                if (child.getNodeType() == Node.TEXT_NODE) {
                    if (fDocumentImpl != null) {
                        ((TextImpl) child).replaceData(fStringBuffer.toString());
                    }
                    else {
                        ((Text) child).setData(fStringBuffer.toString());
                    }
                }
                // reset string buffer
                fStringBuffer.clear();
            }
        }
    }

    /**
     * Parses the input source specified by the given system identifier.
     * <p>
     * This method is equivalent to the following:
     *
     * <pre>
     * parse(new InputSource(systemId));
     * </pre>
     *
     * @param systemId The system identifier (URI).
     *
     * @exception org.xml.sax.SAXException Throws exception on SAX error.
     * @exception java.io.IOException      Throws exception on i/o error.
     */
    public void parse(final String systemId) throws SAXException, IOException {

        // parse document
        final XMLInputSource source = new XMLInputSource(null, systemId, null);
        try {
            parse(source);
        }

        // wrap XNI exceptions as SAX exceptions
        catch (final XMLParseException e) {
            final Exception ex = e.getException();
            if (ex == null || ex instanceof CharConversionException) {
                // must be a parser exception; mine it for locator info and throw
                // a SAXParseException
                final LocatorImpl locatorImpl = new LocatorImpl();
                locatorImpl.setPublicId(e.getPublicId());
                locatorImpl.setSystemId(e.getExpandedSystemId());
                locatorImpl.setLineNumber(e.getLineNumber());
                locatorImpl.setColumnNumber(e.getColumnNumber());
                throw (ex == null) ? new SAXParseException(e.getMessage(), locatorImpl)
                        : new SAXParseException(e.getMessage(), locatorImpl, ex);
            }
            if (ex instanceof SAXException) {
                // why did we create an XMLParseException?
                throw (SAXException) ex;
            }
            if (ex instanceof IOException) {
                throw (IOException) ex;
            }
            throw new SAXException(ex);
        }
        catch (final XNIException e) {
            e.printStackTrace();
            final Exception ex = e.getException();
            if (ex == null) {
                throw new SAXException(e.getMessage());
            }
            if (ex instanceof SAXException) {
                throw (SAXException) ex;
            }
            if (ex instanceof IOException) {
                throw (IOException) ex;
            }
            throw new SAXException(ex);
        }

    }

    /**
     * Parse.
     *
     * @param inputSource the input source
     *
     * @exception org.xml.sax.SAXException on error
     * @exception java.io.IOException      on error
     */
    public void parse(final InputSource inputSource) throws SAXException, IOException {

        // parse document
        try {
            final XMLInputSource xmlInputSource = new XMLInputSource(inputSource.getPublicId(), inputSource.getSystemId(), null);
            xmlInputSource.setByteStream(inputSource.getByteStream());
            xmlInputSource.setCharacterStream(inputSource.getCharacterStream());
            xmlInputSource.setEncoding(inputSource.getEncoding());
            parse(xmlInputSource);
        }

        // wrap XNI exceptions as SAX exceptions
        catch (final XMLParseException e) {
            final Exception ex = e.getException();
            if (ex == null || ex instanceof CharConversionException) {
                // must be a parser exception; mine it for locator info and throw
                // a SAXParseException
                final LocatorImpl locatorImpl = new LocatorImpl();
                locatorImpl.setPublicId(e.getPublicId());
                locatorImpl.setSystemId(e.getExpandedSystemId());
                locatorImpl.setLineNumber(e.getLineNumber());
                locatorImpl.setColumnNumber(e.getColumnNumber());
                throw (ex == null) ? new SAXParseException(e.getMessage(), locatorImpl)
                        : new SAXParseException(e.getMessage(), locatorImpl, ex);
            }
            if (ex instanceof SAXException) {
                // why did we create an XMLParseException?
                throw (SAXException) ex;
            }
            if (ex instanceof IOException) {
                throw (IOException) ex;
            }
            throw new SAXException(ex);
        }
        catch (final XNIException e) {
            final Exception ex = e.getException();
            if (ex == null) {
                throw new SAXException(e.getMessage());
            }
            if (ex instanceof SAXException) {
                throw (SAXException) ex;
            }
            if (ex instanceof IOException) {
                throw (IOException) ex;
            }
            throw new SAXException(ex);
        }

    }

    /**
     * Allow an application to register an error event handler.
     *
     * <p>
     * If the application does not register an error handler, all error events
     * reported by the SAX parser will be silently ignored; however, normal
     * processing may not continue. It is highly recommended that all SAX
     * applications implement an error handler to avoid unexpected bugs.
     * </p>
     *
     * <p>
     * Applications may register a new or different handler in the middle of a
     * parse, and the SAX parser must begin using the new handler immediately.
     * </p>
     *
     * @param errorHandler The error handler.
     * @exception java.lang.NullPointerException If the handler argument is null.
     * @see #getErrorHandler
     */
    public void setErrorHandler(final ErrorHandler errorHandler) {
        try {
            final XMLErrorHandler xeh = (XMLErrorHandler) parserConfiguration_.getProperty(ERROR_HANDLER);
            if (xeh instanceof ErrorHandlerWrapper) {
                final ErrorHandlerWrapper ehw = (ErrorHandlerWrapper) xeh;
                ehw.setErrorHandler(errorHandler);
            }
            else {
                parserConfiguration_.setProperty(ERROR_HANDLER, new ErrorHandlerWrapper(errorHandler));
            }
        }
        catch (final XMLConfigurationException e) {
            // do nothing
        }

    }

    /**
     * Return the current error handler.
     *
     * @return The current error handler, or null if none has been registered.
     * @see #setErrorHandler
     */
    public ErrorHandler getErrorHandler() {
        ErrorHandler errorHandler = null;
        try {
            final XMLErrorHandler xmlErrorHandler = (XMLErrorHandler) parserConfiguration_.getProperty(ERROR_HANDLER);
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
     * Set the state of any feature in a SAX2 parser. The parser might not recognize
     * the feature, and if it does recognize it, it might not be able to fulfill the
     * request.
     *
     * @param featureId The unique identifier (URI) of the feature.
     * @param state     The requested state of the feature (true or false).
     *
     * @exception SAXNotRecognizedException If the requested feature is not known.
     * @exception SAXNotSupportedException  If the requested feature is known, but
     *                                      the requested state is not supported.
     */
    public void setFeature(final String featureId, final boolean state) throws SAXNotRecognizedException, SAXNotSupportedException {
        try {
            parserConfiguration_.setFeature(featureId, state);
        }
        catch (final XMLConfigurationException e) {
            final String identifier = e.getIdentifier();
            if (e.getType() == XMLConfigurationException.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(SAXMessageFormatter.formatMessage("feature-not-recognized", new Object[] {identifier}));
            }

            throw new SAXNotSupportedException(
                    SAXMessageFormatter.formatMessage("feature-not-supported", new Object[] {identifier}));
        }
    }

    /**
     * Query the state of a feature.
     * <p>
     * Query the current state of any feature in a SAX2 parser. The parser might not
     * recognize the feature.
     *
     * @param featureId The unique identifier (URI) of the feature being set.
     * @return The current state of the feature.
     * @exception org.xml.sax.SAXNotRecognizedException If the requested feature is
     *                                                  not known.
     * @exception SAXNotSupportedException              If the requested feature is
     *                                                  known but not supported.
     */
    public boolean getFeature(final String featureId) throws SAXNotRecognizedException, SAXNotSupportedException {

        try {
            return parserConfiguration_.getFeature(featureId);
        }
        catch (final XMLConfigurationException e) {
            final String identifier = e.getIdentifier();
            if (e.getType() == XMLConfigurationException.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(SAXMessageFormatter.formatMessage("feature-not-recognized", new Object[] {identifier}));
            }

            throw new SAXNotSupportedException(SAXMessageFormatter.formatMessage("feature-not-supported", new Object[] {identifier}));
        }

    }

    /**
     * Set the value of any property in a SAX2 parser. The parser might not
     * recognize the property, and if it does recognize it, it might not support the
     * requested value.
     *
     * @param propertyId The unique identifier (URI) of the property being set.
     * @param value      The value to which the property is being set.
     *
     * @exception SAXNotRecognizedException If the requested property is not known.
     * @exception SAXNotSupportedException  If the requested property is known, but
     *                                      the requested value is not supported.
     */
    public void setProperty(final String propertyId, final Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {

        try {
            parserConfiguration_.setProperty(propertyId, value);
        }
        catch (final XMLConfigurationException e) {
            final String identifier = e.getIdentifier();
            if (e.getType() == XMLConfigurationException.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(SAXMessageFormatter.formatMessage("property-not-recognized", new Object[] {identifier}));
            }

            throw new SAXNotSupportedException(SAXMessageFormatter.formatMessage("property-not-supported", new Object[] {identifier}));
        }

    }

    /**
     * @return this parser's XMLParserConfiguration.
     */
    public XMLParserConfiguration getXMLParserConfiguration() {
        return parserConfiguration_;
    }
}
