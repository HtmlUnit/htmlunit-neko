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
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.Attributes2;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.ext.Locator2;
import org.xml.sax.ext.Locator2Impl;

/**
 * This is the base class of all SAX parsers. It implements both the SAX1 and
 * SAX2 parser functionality, while the actual pipeline is defined in the parser
 * configuration.
 *
 * @author Arnaud Le Hors, IBM
 * @author Andy Clark, IBM
 */
public abstract class AbstractSAXParser extends AbstractXMLDocumentParser implements XMLReader { // SAX2

    // features

    /** Feature identifier: namespaces. */
    protected static final String NAMESPACES = Constants.SAX_FEATURE_PREFIX + Constants.NAMESPACES_FEATURE;

    /** Recognized features. */
    private static final String[] RECOGNIZED_FEATURES = {NAMESPACES};

    // properties

    /** Property id: lexical handler. */
    protected static final String LEXICAL_HANDLER = Constants.SAX_PROPERTY_PREFIX + Constants.LEXICAL_HANDLER_PROPERTY;

    /** Recognized properties. */
    private static final String[] RECOGNIZED_PROPERTIES = {LEXICAL_HANDLER};

    // features

    /** Namespaces. */
    protected boolean fNamespaces;

    /** Namespace prefixes. */
    protected boolean fNamespacePrefixes = false;

    /** Lexical handler parameter entities. */
    protected boolean fLexicalHandlerParameterEntities = true;

    /** Standalone document declaration. */
    protected boolean fStandalone;

    /** Use EntityResolver2. */
    protected boolean fUseEntityResolver2 = true;

    // parser handlers

    /** Content handler. */
    protected ContentHandler fContentHandler;

    /** Namespace context */
    protected NamespaceContext fNamespaceContext;

    /** DTD handler. */
    protected org.xml.sax.DTDHandler fDTDHandler;

    /** Lexical handler. */
    protected LexicalHandler fLexicalHandler;

    // state

    /**
     * True if a parse is in progress. This state is needed because some
     * features/properties cannot be set while parsing (e.g. validation and
     * namespaces).
     */
    protected final boolean fParseInProgress = false;

    // track the version of the document being parsed
    protected String fVersion;

    // temp vars
    private final AttributesProxy fAttributesProxy = new AttributesProxy();

    // Default constructor.
    protected AbstractSAXParser(final XMLParserConfiguration config) {
        super(config);

        config.addRecognizedFeatures(RECOGNIZED_FEATURES);
        config.addRecognizedProperties(RECOGNIZED_PROPERTIES);
    }

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
     * @param augs             Additional information that may include infoset
     *                         augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void startDocument(final XMLLocator locator, final String encoding, final NamespaceContext namespaceContext, final Augmentations augs) throws XNIException {

        fNamespaceContext = namespaceContext;

        try {
            // SAX2
            if (fContentHandler != null) {
                if (locator != null) {
                    fContentHandler.setDocumentLocator(new LocatorProxy(locator));
                }
                // The application may have set the ContentHandler to null
                // within setDocumentLocator() so we need to check again.
                if (fContentHandler != null) {
                    fContentHandler.startDocument();
                }
            }
        }
        catch (final SAXException e) {
            throw new XNIException(e);
        }
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
        // the version need only be set once; if
        // document's XML 1.0|1.1, that's how it'll stay
        fVersion = version;
        fStandalone = "yes".equals(standalone);
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

        try {
            // SAX2 extension
            if (fLexicalHandler != null) {
                fLexicalHandler.startDTD(rootElement, publicId, systemId);
            }
        }
        catch (final SAXException e) {
            throw new XNIException(e);
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

        try {
            // SAX2
            if (fContentHandler != null) {

                if (fNamespaces) {
                    // send prefix mapping events
                    startNamespaceMapping();
                }

                final String uri = element.getUri() != null ? element.getUri() : "";
                final String localpart = fNamespaces ? element.getLocalpart() : "";
                fAttributesProxy.setAttributes(attributes);
                fContentHandler.startElement(uri, localpart, element.getRawname(), fAttributesProxy);
            }
        }
        catch (final SAXException e) {
            throw new XNIException(e);
        }
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

        // if type is union (XML Schema) it is possible that we receive
        // character call with empty data
        if (text.length() == 0) {
            return;
        }

        try {
            // SAX2
            if (fContentHandler != null) {
                text.characters(fContentHandler);
            }
        }
        catch (final SAXException e) {
            throw new XNIException(e);
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

        try {
            // SAX2
            if (fContentHandler != null) {
                final String uri = element.getUri() != null ? element.getUri() : "";
                final String localpart = fNamespaces ? element.getLocalpart() : "";
                fContentHandler.endElement(uri, localpart, element.getRawname());
                if (fNamespaces) {
                    endNamespaceMapping();
                }
            }
        }
        catch (final SAXException e) {
            throw new XNIException(e);
        }
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

        try {
            // SAX2 extension
            if (fLexicalHandler != null) {
                fLexicalHandler.startCDATA();
            }
        }
        catch (final SAXException e) {
            throw new XNIException(e);
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

        try {
            // SAX2 extension
            if (fLexicalHandler != null) {
                fLexicalHandler.endCDATA();
            }
        }
        catch (final SAXException e) {
            throw new XNIException(e);
        }
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

        try {
            // SAX2 extension
            if (fLexicalHandler != null) {
                text.comment(fLexicalHandler);
            }
        }
        catch (final SAXException e) {
            throw new XNIException(e);
        }
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

        // REVISIT - I keep running into SAX apps that expect
        // null data to be an empty string, which is contrary
        // to the comment for this method in the SAX API.

        try {
            // SAX2
            if (fContentHandler != null) {
                fContentHandler.processingInstruction(target, data.toString());
            }
        }
        catch (final SAXException e) {
            throw new XNIException(e);
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

        try {
            // SAX2
            if (fContentHandler != null) {
                fContentHandler.endDocument();
            }
        }
        catch (final SAXException e) {
            throw new XNIException(e);
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
    @Override
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
                // must be a parser exception; mine it for locator info
                // and throw a SAXParseException
                final Locator2Impl locatorImpl = new Locator2Impl();
                // since XMLParseExceptions know nothing about encoding,
                // we cannot return anything meaningful in this context.
                // We *could* consult the LocatorProxy, but the
                // application can do this itself if it wishes to possibly
                // be mislead.
                locatorImpl.setXMLVersion(fVersion);
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
     * {@inheritDoc}
     */
    @Override
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
                // must be a parser exception; mine it for locator info
                // and throw a SAXParseException
                final Locator2Impl locatorImpl = new Locator2Impl();
                // since XMLParseExceptions know nothing about encoding,
                // we cannot return anything meaningful in this context.
                // We *could* consult the LocatorProxy, but the
                // application can do this itself if it wishes to possibly
                // be mislead.
                locatorImpl.setXMLVersion(fVersion);
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
     * {@inheritDoc}
     */
    @Override
    public void setEntityResolver(final EntityResolver resolver) {
    }

    /**
     * Return the current entity resolver.
     *
     * @return The current entity resolver, or null if none has been registered.
     * @see #setEntityResolver
     */
    @Override
    public EntityResolver getEntityResolver() {
        return null;
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
     * @see #getErrorHandler
     */
    @Override
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
    @Override
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
     * Allow an application to register a DTD event handler.
     * <p>
     * If the application does not register a DTD handler, all DTD events reported
     * by the SAX parser will be silently ignored.
     * <p>
     * Applications may register a new or different handler in the middle of a
     * parse, and the SAX parser must begin using the new handler immediately.
     *
     * @param dtdHandler The DTD handler.
     * @see #getDTDHandler
     */
    @Override
    public void setDTDHandler(final DTDHandler dtdHandler) {
        fDTDHandler = dtdHandler;
    }

    /**
     * Allow an application to register a content event handler.
     * <p>
     * If the application does not register a content handler, all content events
     * reported by the SAX parser will be silently ignored.
     * <p>
     * Applications may register a new or different handler in the middle of a
     * parse, and the SAX parser must begin using the new handler immediately.
     *
     * @param contentHandler The content handler.
     *
     * @see #getContentHandler
     */
    @Override
    public void setContentHandler(final ContentHandler contentHandler) {
        fContentHandler = contentHandler;
    }

    /**
     * Return the current content handler.
     *
     * @return The current content handler, or null if none has been registered.
     *
     * @see #setContentHandler
     */
    @Override
    public ContentHandler getContentHandler() {
        return fContentHandler;
    }

    /**
     * Return the current DTD handler.
     *
     * @return The current DTD handler, or null if none has been registered.
     * @see #setDTDHandler
     */
    @Override
    public DTDHandler getDTDHandler() {
        return fDTDHandler;
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
    @Override
    public void setFeature(final String featureId, final boolean state) throws SAXNotRecognizedException, SAXNotSupportedException {

        try {
            if (featureId.startsWith(Constants.SAX_FEATURE_PREFIX)) {
                final int suffixLength = featureId.length() - Constants.SAX_FEATURE_PREFIX.length();

                // http://xml.org/sax/features/namespaces
                if (suffixLength == Constants.NAMESPACES_FEATURE.length()
                        && featureId.endsWith(Constants.NAMESPACES_FEATURE)) {
                    parserConfiguration_.setFeature(featureId, state);
                    fNamespaces = state;
                    return;
                }

                // http://xml.org/sax/features/namespace-prefixes
                // controls the reporting of raw prefixed names and Namespace
                // declarations (xmlns* attributes): when this feature is false
                // (the default), raw prefixed names may optionally be reported,
                // and xmlns* attributes must not be reported.
                //
                if (suffixLength == Constants.NAMESPACE_PREFIXES_FEATURE.length()
                        && featureId.endsWith(Constants.NAMESPACE_PREFIXES_FEATURE)) {
                    fNamespacePrefixes = state;
                    return;
                }

                // http://xml.org/sax/features/lexical-handler/parameter-entities
                // controls whether the beginning and end of parameter entities
                // will be reported to the LexicalHandler.
                //
                if (suffixLength == Constants.LEXICAL_HANDLER_PARAMETER_ENTITIES_FEATURE.length()
                        && featureId.endsWith(Constants.LEXICAL_HANDLER_PARAMETER_ENTITIES_FEATURE)) {
                    fLexicalHandlerParameterEntities = state;
                    return;
                }

                // http://xml.org/sax/features/unicode-normalization-checking
                // controls whether Unicode normalization checking is performed
                // as per Appendix B of the XML 1.1 specification
                if (suffixLength == Constants.UNICODE_NORMALIZATION_CHECKING_FEATURE.length()
                        && featureId.endsWith(Constants.UNICODE_NORMALIZATION_CHECKING_FEATURE)) {
                    // REVISIT: Allow this feature to be set once Unicode normalization
                    // checking is supported -- mrglavas.
                    if (state) {
                        throw new SAXNotSupportedException(
                                SAXMessageFormatter.formatMessage("true-not-supported", new Object[] {featureId}));
                    }
                    return;
                }
            }

            parserConfiguration_.setFeature(featureId, state);
        }
        catch (final XMLConfigurationException e) {
            final String identifier = e.getIdentifier();
            if (e.getType() == XMLConfigurationException.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(
                        SAXMessageFormatter.formatMessage("feature-not-recognized", new Object[] {identifier}));
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
    @Override
    public boolean getFeature(final String featureId) throws SAXNotRecognizedException, SAXNotSupportedException {

        try {
            if (featureId.startsWith(Constants.SAX_FEATURE_PREFIX)) {
                final int suffixLength = featureId.length() - Constants.SAX_FEATURE_PREFIX.length();

                // http://xml.org/sax/features/namespace-prefixes
                // controls the reporting of raw prefixed names and Namespace
                // declarations (xmlns* attributes): when this feature is false
                // (the default), raw prefixed names may optionally be reported,
                // and xmlns* attributes must not be reported.
                //
                if (suffixLength == Constants.NAMESPACE_PREFIXES_FEATURE.length()
                        && featureId.endsWith(Constants.NAMESPACE_PREFIXES_FEATURE)) {
                    return fNamespacePrefixes;
                }

                // http://xml.org/sax/features/lexical-handler/parameter-entities
                // controls whether the beginning and end of parameter entities
                // will be reported to the LexicalHandler.
                //
                if (suffixLength == Constants.LEXICAL_HANDLER_PARAMETER_ENTITIES_FEATURE.length()
                        && featureId.endsWith(Constants.LEXICAL_HANDLER_PARAMETER_ENTITIES_FEATURE)) {
                    return fLexicalHandlerParameterEntities;
                }

                // http://xml.org/sax/features/unicode-normalization-checking
                // controls whether Unicode normalization checking is performed
                // as per Appendix B of the XML 1.1 specification
                //
                if (suffixLength == Constants.UNICODE_NORMALIZATION_CHECKING_FEATURE.length()
                        && featureId.endsWith(Constants.UNICODE_NORMALIZATION_CHECKING_FEATURE)) {
                    // REVISIT: Allow this feature to be set once Unicode normalization
                    // checking is supported -- mrglavas.
                    return false;
                }
            }

            return parserConfiguration_.getFeature(featureId);
        }
        catch (final XMLConfigurationException e) {
            final String identifier = e.getIdentifier();
            if (e.getType() == XMLConfigurationException.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(
                        SAXMessageFormatter.formatMessage("feature-not-recognized", new Object[] {identifier}));
            }
            throw new SAXNotSupportedException(
                    SAXMessageFormatter.formatMessage("feature-not-supported", new Object[] {identifier}));
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
    @Override
    public void setProperty(final String propertyId, final Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {

        try {
            if (propertyId.startsWith(Constants.SAX_PROPERTY_PREFIX)) {
                final int suffixLength = propertyId.length() - Constants.SAX_PROPERTY_PREFIX.length();

                //
                // http://xml.org/sax/properties/lexical-handler
                // Value type: org.xml.sax.ext.LexicalHandler
                // Access: read/write, pre-parse only
                // Set the lexical event handler.
                //
                if (suffixLength == Constants.LEXICAL_HANDLER_PROPERTY.length()
                        && propertyId.endsWith(Constants.LEXICAL_HANDLER_PROPERTY)) {
                    try {
                        setLexicalHandler((LexicalHandler) value);
                    }
                    catch (final ClassCastException e) {
                        throw new SAXNotSupportedException(SAXMessageFormatter.formatMessage("incompatible-class",
                                new Object[] {propertyId, "org.xml.sax.ext.LexicalHandler"}));
                    }
                    return;
                }
            }

            parserConfiguration_.setProperty(propertyId, value);
        }
        catch (final XMLConfigurationException e) {
            final String identifier = e.getIdentifier();
            if (e.getType() == XMLConfigurationException.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(
                        SAXMessageFormatter.formatMessage("property-not-recognized", new Object[] {identifier}));
            }
            throw new SAXNotSupportedException(
                    SAXMessageFormatter.formatMessage("property-not-supported", new Object[] {identifier}));
        }
    }

    /**
     * Query the value of a property.
     * <p>
     * Return the current value of a property in a SAX2 parser. The parser might not
     * recognize the property.
     *
     * @param propertyId The unique identifier (URI) of the property being set.
     * @return The current value of the property.
     * @exception org.xml.sax.SAXNotRecognizedException If the requested property is
     *                                                  not known.
     * @exception SAXNotSupportedException              If the requested property is
     *                                                  known but not supported.
     */
    @Override
    public Object getProperty(final String propertyId) throws SAXNotRecognizedException, SAXNotSupportedException {

        try {
            //
            // SAX2 core properties
            //

            if (propertyId.startsWith(Constants.SAX_PROPERTY_PREFIX)) {
                final int suffixLength = propertyId.length() - Constants.SAX_PROPERTY_PREFIX.length();

                //
                // http://xml.org/sax/properties/document-xml-version
                // Value type: java.lang.String
                // Access: read-only
                // The literal string describing the actual XML version of the document.
                //
                if (suffixLength == Constants.DOCUMENT_XML_VERSION_PROPERTY.length()
                        && propertyId.endsWith(Constants.DOCUMENT_XML_VERSION_PROPERTY)) {
                    return fVersion;
                }

                //
                // http://xml.org/sax/properties/lexical-handler
                // Value type: org.xml.sax.ext.LexicalHandler
                // Access: read/write, pre-parse only
                // Set the lexical event handler.
                //
                if (suffixLength == Constants.LEXICAL_HANDLER_PROPERTY.length()
                        && propertyId.endsWith(Constants.LEXICAL_HANDLER_PROPERTY)) {
                    return getLexicalHandler();
                }
            }

            return parserConfiguration_.getProperty(propertyId);
        }
        catch (final XMLConfigurationException e) {
            final String identifier = e.getIdentifier();
            if (e.getType() == XMLConfigurationException.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(
                        SAXMessageFormatter.formatMessage("property-not-recognized", new Object[] {identifier}));
            }
            throw new SAXNotSupportedException(
                    SAXMessageFormatter.formatMessage("property-not-supported", new Object[] {identifier}));
        }

    }

    // SAX2 core properties

    /**
     * Set the lexical event handler.
     * <p>
     * This method is the equivalent to the property:
     *
     * <pre>
     * http://xml.org/sax/properties/lexical-handler
     * </pre>
     *
     * @param handler lexical event handler
     * @throws SAXNotSupportedException on error
     *
     * @see #getLexicalHandler()
     * @see #setProperty(String, Object)
     */
    protected void setLexicalHandler(final LexicalHandler handler) throws SAXNotSupportedException {

        if (fParseInProgress) {
            throw new SAXNotSupportedException(SAXMessageFormatter.formatMessage("property-not-parsing-supported",
                    new Object[] {"http://xml.org/sax/properties/lexical-handler"}));
        }
        fLexicalHandler = handler;

    }

    /**
     * @return the lexical handler.
     *
     * @see #setLexicalHandler(LexicalHandler)
     */
    protected LexicalHandler getLexicalHandler() {
        return fLexicalHandler;
    }

    /**
     * Send startPrefixMapping events
     *
     * @throws SAXException on error
     */
    protected final void startNamespaceMapping() throws SAXException {
        final int count = fNamespaceContext.getDeclaredPrefixCount();
        if (count > 0) {
            String prefix;
            String uri;
            for (int i = 0; i < count; i++) {
                prefix = fNamespaceContext.getDeclaredPrefixAt(i);
                uri = fNamespaceContext.getURI(prefix);
                fContentHandler.startPrefixMapping(prefix, (uri == null) ? "" : uri);
            }
        }
    }

    /**
     * Send endPrefixMapping events
     *
     * @throws SAXException on error
     */
    protected final void endNamespaceMapping() throws SAXException {
        final int count = fNamespaceContext.getDeclaredPrefixCount();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                fContentHandler.endPrefixMapping(fNamespaceContext.getDeclaredPrefixAt(i));
            }
        }
    }

    /**
     * Reset all components before parsing.
     *
     * @throws XNIException Thrown if an error occurs during initialization.
     */
    @Override
    public void reset() throws XNIException {
        super.reset();

        // reset state
        fVersion = "1.0";
        fStandalone = false;

        // features
        fNamespaces = parserConfiguration_.getFeature(NAMESPACES);
    }

    protected static final class LocatorProxy implements Locator2 {

        /** XML locator. */
        private final XMLLocator fLocator;

        // Constructs an XML locator proxy.
        public LocatorProxy(final XMLLocator locator) {
            fLocator = locator;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getPublicId() {
            return fLocator.getPublicId();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSystemId() {
            return fLocator.getExpandedSystemId();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getLineNumber() {
            return fLocator.getLineNumber();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getColumnNumber() {
            return fLocator.getColumnNumber();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getXMLVersion() {
            return fLocator.getXMLVersion();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getEncoding() {
            return fLocator.getEncoding();
        }

    }

    protected static final class AttributesProxy implements Attributes2 {

        /** XML attributes. */
        private XMLAttributes fAttributes;

        // Sets the XML attributes.
        public void setAttributes(final XMLAttributes attributes) {
            fAttributes = attributes;
        }

        @Override
        public int getLength() {
            return fAttributes.getLength();
        }

        @Override
        public String getQName(final int index) {
            return fAttributes.getQName(index);
        }

        @Override
        public String getURI(final int index) {
            // REVISIT: this hides the fact that internally we use
            // null instead of empty string
            // SAX requires URI to be a string or an empty string
            final String uri = fAttributes.getURI(index);
            return uri != null ? uri : "";
        }

        @Override
        public String getLocalName(final int index) {
            return fAttributes.getLocalName(index);
        }

        @Override
        public String getType(final int i) {
            return fAttributes.getType(i);
        }

        @Override
        public String getType(final String name) {
            return fAttributes.getType(name);
        }

        @Override
        public String getType(final String uri, final String localName) {
            return uri.length() == 0 ? fAttributes.getType(null, localName) : fAttributes.getType(uri, localName);
        }

        @Override
        public String getValue(final int i) {
            return fAttributes.getValue(i);
        }

        @Override
        public String getValue(final String name) {
            return fAttributes.getValue(name);
        }

        @Override
        public String getValue(final String uri, final String localName) {
            return uri.length() == 0 ? fAttributes.getValue(null, localName) : fAttributes.getValue(uri, localName);
        }

        @Override
        public int getIndex(final String qName) {
            return fAttributes.getIndex(qName);
        }

        @Override
        public int getIndex(final String uri, final String localPart) {
            return uri.length() == 0 ? fAttributes.getIndex(null, localPart) : fAttributes.getIndex(uri, localPart);
        }

        @Override
        public boolean isDeclared(final int index) {
            return false;
        }

        @Override
        public boolean isDeclared(final String qName) {
            return false;
        }

        @Override
        public boolean isDeclared(final String uri, final String localName) {
            return false;
        }

        @Override
        public boolean isSpecified(final int index) {
            if (index < 0 || index >= fAttributes.getLength()) {
                throw new ArrayIndexOutOfBoundsException(index);
            }
            return fAttributes.isSpecified(index);
        }

        @Override
        public boolean isSpecified(final String qName) {
            final int index = getIndex(qName);
            if (index == -1) {
                throw new IllegalArgumentException(qName);
            }
            return fAttributes.isSpecified(index);
        }

        @Override
        public boolean isSpecified(final String uri, final String localName) {
            final int index = getIndex(uri, localName);
            if (index == -1) {
                throw new IllegalArgumentException(localName);
            }
            return fAttributes.isSpecified(index);
        }

    }
}
