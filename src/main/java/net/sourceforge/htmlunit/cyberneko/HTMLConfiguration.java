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
 */

package net.sourceforge.htmlunit.cyberneko;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.sourceforge.htmlunit.cyberneko.filters.NamespaceBinder;
import net.sourceforge.htmlunit.xerces.util.ParserConfigurationSettings;
import net.sourceforge.htmlunit.xerces.xni.XMLDTDContentModelHandler;
import net.sourceforge.htmlunit.xerces.xni.XMLDTDHandler;
import net.sourceforge.htmlunit.xerces.xni.XMLDocumentHandler;
import net.sourceforge.htmlunit.xerces.xni.XNIException;
import net.sourceforge.htmlunit.xerces.xni.parser.XMLConfigurationException;
import net.sourceforge.htmlunit.xerces.xni.parser.XMLDocumentFilter;
import net.sourceforge.htmlunit.xerces.xni.parser.XMLDocumentSource;
import net.sourceforge.htmlunit.xerces.xni.parser.XMLEntityResolver;
import net.sourceforge.htmlunit.xerces.xni.parser.XMLErrorHandler;
import net.sourceforge.htmlunit.xerces.xni.parser.XMLInputSource;
import net.sourceforge.htmlunit.xerces.xni.parser.XMLParseException;
import net.sourceforge.htmlunit.xerces.xni.parser.XMLPullParserConfiguration;

/**
 * An XNI-based parser configuration that can be used to parse HTML
 * documents. This configuration can be used directly in order to
 * parse HTML documents or can be used in conjunction with any XNI
 * based tools, such as the Xerces2 implementation.
 * <p>
 * This configuration recognizes the following features:
 * <ul>
 * <li>http://cyberneko.org/html/features/augmentations
 * <li>http://cyberneko.org/html/features/report-errors
 * <li>http://cyberneko.org/html/features/report-errors/simple
 * <li><i>and</i>
 * <li>the features supported by the scanner and tag balancer components.
 * </ul>
 * <p>
 * This configuration recognizes the following properties:
 * <ul>
 * <li>http://cyberneko.org/html/properties/names/elems
 * <li>http://cyberneko.org/html/properties/names/attrs
 * <li>http://cyberneko.org/html/properties/filters
 * <li>http://cyberneko.org/html/properties/error-reporter
 * <li><i>and</i>
 * <li>the properties supported by the scanner and tag balancer.
 * </ul>
 * <p>
 * For complete usage information, refer to the documentation.
 *
 * @see HTMLScanner
 * @see HTMLTagBalancer
 * @see HTMLErrorReporter
 *
 * @author Andy Clark
 */
public class HTMLConfiguration
    extends ParserConfigurationSettings
    implements XMLPullParserConfiguration {

    //
    // Constants
    //

    // features

    /** Namespaces. */
    protected static final String NAMESPACES = "http://xml.org/sax/features/namespaces";

    /** Include infoset augmentations. */
    protected static final String AUGMENTATIONS = "http://cyberneko.org/html/features/augmentations";

    /** Report errors. */
    protected static final String REPORT_ERRORS = "http://cyberneko.org/html/features/report-errors";

    /** Simple report format. */
    protected static final String SIMPLE_ERROR_FORMAT = "http://cyberneko.org/html/features/report-errors/simple";

    // properties

    /** Modify HTML element names: { "upper", "lower", "default" }. */
    protected static final String NAMES_ELEMS = "http://cyberneko.org/html/properties/names/elems";

    /** Modify HTML attribute names: { "upper", "lower", "default" }. */
    protected static final String NAMES_ATTRS = "http://cyberneko.org/html/properties/names/attrs";

    /** Pipeline filters. */
    protected static final String FILTERS = "http://cyberneko.org/html/properties/filters";

    /** Error reporter. */
    protected static final String ERROR_REPORTER = "http://cyberneko.org/html/properties/error-reporter";

    // other

    /** Error domain. */
    protected static final String ERROR_DOMAIN = "http://cyberneko.org/html";

    // private

    //
    // Data
    //

    // handlers

    /** Document handler. */
    protected XMLDocumentHandler fDocumentHandler;

    /** DTD handler. */
    protected XMLDTDHandler fDTDHandler;

    /** DTD content model handler. */
    protected XMLDTDContentModelHandler fDTDContentModelHandler;

    /** Error handler. */
    protected XMLErrorHandler fErrorHandler;

    // other settings

    /** Entity resolver. */
    protected XMLEntityResolver fEntityResolver;

    /** Locale. */
    protected Locale fLocale = Locale.getDefault();

    // state

    /**
     * Stream opened by parser. Therefore, must close stream manually upon
     * termination of parsing.
     */
    protected boolean fCloseStream;

    // components

    /** Components. */
    protected final List<HTMLComponent> fHTMLComponents = new ArrayList<>(2);

    // pipeline

    /** Document scanner. */
    protected final HTMLScanner fDocumentScanner = createDocumentScanner();

    /** HTML tag balancer. */
    protected final HTMLTagBalancer fTagBalancer = new HTMLTagBalancer(this);

    /** Namespace binder. */
    protected final NamespaceBinder fNamespaceBinder = new NamespaceBinder(this);

    // other components

    /** Error reporter. */
    protected final HTMLErrorReporter fErrorReporter = new ErrorReporter();

    //
    // Static initializer
    //

    public final HTMLElements htmlElements_;
    //
    // Constructors
    //

    /** Default constructor. */
    public HTMLConfiguration() {
        this(new HTMLElements());
    }

    public HTMLConfiguration(HTMLElements htmlElements) {
        htmlElements_ = htmlElements;

        // add components
        addComponent(fDocumentScanner);
        addComponent(fTagBalancer);
        addComponent(fNamespaceBinder);

        //
        // features
        //

        // recognized features
        final String VALIDATION = "http://xml.org/sax/features/validation";
        final String[] recognizedFeatures = {
            AUGMENTATIONS,
            NAMESPACES,
            VALIDATION,
            REPORT_ERRORS,
            SIMPLE_ERROR_FORMAT,
        };
        addRecognizedFeatures(recognizedFeatures);
        setFeature(AUGMENTATIONS, false);
        setFeature(NAMESPACES, true);
        setFeature(VALIDATION, false);
        setFeature(REPORT_ERRORS, false);
        setFeature(SIMPLE_ERROR_FORMAT, false);

        //
        // properties
        //

        // recognized properties
        final String[] recognizedProperties = {
            NAMES_ELEMS,
            NAMES_ATTRS,
            FILTERS,
            ERROR_REPORTER,
        };
        addRecognizedProperties(recognizedProperties);
        setProperty(NAMES_ELEMS, "default");
        setProperty(NAMES_ATTRS, "lower");
        setProperty(ERROR_REPORTER, fErrorReporter);
    }

    protected HTMLScanner createDocumentScanner() {
        return new HTMLScanner(this);
    }

    //
    // Public methods
    //

    /**
     * Pushes an input source onto the current entity stack. This
     * enables the scanner to transparently scan new content (e.g.
     * the output written by an embedded script). At the end of the
     * current entity, the scanner returns where it left off at the
     * time this entity source was pushed.
     * <p>
     * <strong>Hint:</strong>
     * To use this feature to insert the output of &lt;SCRIPT&gt;
     * tags, remember to buffer the <em>entire</em> output of the
     * processed instructions before pushing a new input source.
     * Otherwise, events may appear out of sequence.
     *
     * @param inputSource The new input source to start scanning.
     * @see #evaluateInputSource(XMLInputSource)
     */
    public void pushInputSource(XMLInputSource inputSource) {
        fDocumentScanner.pushInputSource(inputSource);
    }

    /**
     * <span style="color: red">EXPERIMENTAL: may change in next release</span><br>
     * Immediately evaluates an input source and add the new content (e.g.
     * the output written by an embedded script).
     *
     * @param inputSource The new input source to start scanning.
     * @see #pushInputSource(XMLInputSource)
     */
    public void evaluateInputSource(XMLInputSource inputSource) {
        fDocumentScanner.evaluateInputSource(inputSource);
    }

    // XMLParserConfiguration methods
    //

    // Sets a feature.
    @Override
    public void setFeature(String featureId, boolean state)
        throws XMLConfigurationException {
        super.setFeature(featureId, state);
        final int size = fHTMLComponents.size();
        for (final HTMLComponent component : fHTMLComponents) {
            component.setFeature(featureId, state);
        }
    }

    // Sets a property.
    @Override
    public void setProperty(String propertyId, Object value)
        throws XMLConfigurationException {
        super.setProperty(propertyId, value);

        if (propertyId.equals(FILTERS)) {
            final XMLDocumentFilter[] filters = (XMLDocumentFilter[])getProperty(FILTERS);
            if (filters != null) {
                for (final XMLDocumentFilter filter : filters) {
                    if (filter instanceof HTMLComponent) {
                        addComponent((HTMLComponent)filter);
                    }
                }
            }
        }

        final int size = fHTMLComponents.size();
        for (final HTMLComponent component : fHTMLComponents) {
            component.setProperty(propertyId, value);
        }
    }

    // Sets the document handler.
    @Override
    public void setDocumentHandler(XMLDocumentHandler handler) {
        fDocumentHandler = handler;
        if (handler instanceof HTMLTagBalancingListener) {
            fTagBalancer.setTagBalancingListener((HTMLTagBalancingListener) handler);
        }
    }

    /** @return the document handler. */
    @Override
    public XMLDocumentHandler getDocumentHandler() {
        return fDocumentHandler;
    }

    // Sets the DTD handler.
    @Override
    public void setDTDHandler(XMLDTDHandler handler) {
        fDTDHandler = handler;
    }

    /** @return the DTD handler. */
    @Override
    public XMLDTDHandler getDTDHandler() {
        return fDTDHandler;
    }

    // Sets the DTD content model handler.
    @Override
    public void setDTDContentModelHandler(XMLDTDContentModelHandler handler) {
        fDTDContentModelHandler = handler;
    }

    /** @return the DTD content model handler. */
    @Override
    public XMLDTDContentModelHandler getDTDContentModelHandler() {
        return fDTDContentModelHandler;
    }

    // Sets the error handler.
    @Override
    public void setErrorHandler(XMLErrorHandler handler) {
        fErrorHandler = handler;
    }

    /** @return the error handler. */
    @Override
    public XMLErrorHandler getErrorHandler() {
        return fErrorHandler;
    }

    /** Sets the entity resolver. */
    @Override
    public void setEntityResolver(XMLEntityResolver resolver) {
        fEntityResolver = resolver;
    }

    /** Returns the entity resolver. */
    @Override
    public XMLEntityResolver getEntityResolver() {
        return fEntityResolver;
    }

    /** Sets the locale. */
    @Override
    public void setLocale(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        fLocale = locale;
    }

    /** Returns the locale. */
    @Override
    public Locale getLocale() {
        return fLocale;
    }

    /** Parses a document. */
    @Override
    public void parse(XMLInputSource source) throws XNIException, IOException {
        setInputSource(source);
        parse(true);
    }

    //
    // XMLPullParserConfiguration methods
    //

    // parsing

    /**
     * Sets the input source for the document to parse.
     *
     * @param inputSource The document's input source.
     *
     * @exception XMLConfigurationException Thrown if there is a
     *                        configuration error when initializing the
     *                        parser.
     * @exception IOException Thrown on I/O error.
     *
     * @see #parse(boolean)
     */
    @Override
    public void setInputSource(XMLInputSource inputSource)
        throws XMLConfigurationException, IOException {
        reset();
        fCloseStream = inputSource.getByteStream() == null &&
                       inputSource.getCharacterStream() == null;
        fDocumentScanner.setInputSource(inputSource);
    }

    /**
     * Parses the document in a pull parsing fashion.
     *
     * @param complete True if the pull parser should parse the
     *                 remaining document completely.
     *
     * @return True if there is more document to parse.
     *
     * @exception XNIException Any XNI exception, possibly wrapping
     *                         another exception.
     * @exception IOException  An IO exception from the parser, possibly
     *                         from a byte stream or character stream
     *                         supplied by the parser.
     *
     * @see #setInputSource
     */
    @Override
    public boolean parse(boolean complete) throws XNIException, IOException {
        try {
            final boolean more = fDocumentScanner.scanDocument(complete);
            if (!more) {
                cleanup();
            }
            return more;
        }
        catch (final XNIException e) {
            cleanup();
            throw e;
        }
        catch (final IOException e) {
            cleanup();
            throw e;
        }
    }

    /**
     * If the application decides to terminate parsing before the xml document
     * is fully parsed, the application should call this method to free any
     * resource allocated during parsing. For example, close all opened streams.
     */
    @Override
    public void cleanup() {
        fDocumentScanner.cleanup(fCloseStream);
    }

    //
    // Protected methods
    //

    // Adds a component.
    protected void addComponent(HTMLComponent component) {

        // add component to list
        fHTMLComponents.add(component);

        // add recognized features and set default states
        final String[] features = component.getRecognizedFeatures();
        addRecognizedFeatures(features);
        if (features != null) {
            final int featureCount = features.length;
            for (String feature : features) {
                final Boolean state = component.getFeatureDefault(feature);
                if (state != null) {
                    setFeature(feature, state.booleanValue());
                }
            }
        }

        // add recognized properties and set default values
        final String[] properties = component.getRecognizedProperties();
        addRecognizedProperties(properties);
        if (properties != null) {
            final int propertyCount = properties.length;
            for (String property : properties) {
                final Object value = component.getPropertyDefault(property);
                if (value != null) {
                    setProperty(property, value);
                }
            }
        }
    }

    /** Resets the parser configuration. */
    protected void reset() throws XMLConfigurationException {

        // reset components
        final int size = fHTMLComponents.size();
        for (final HTMLComponent component : fHTMLComponents) {
            component.reset(this);
        }

        // configure pipeline
        XMLDocumentSource lastSource = fDocumentScanner;
        if (getFeature(NAMESPACES)) {
            lastSource.setDocumentHandler(fNamespaceBinder);
            fNamespaceBinder.setDocumentSource(fTagBalancer);
            lastSource = fNamespaceBinder;
        }

        lastSource.setDocumentHandler(fTagBalancer);
        fTagBalancer.setDocumentSource(fDocumentScanner);
        lastSource = fTagBalancer;

        final XMLDocumentFilter[] filters = (XMLDocumentFilter[])getProperty(FILTERS);
        if (filters != null) {
            for (final XMLDocumentFilter filter : filters) {
                filter.setDocumentSource(lastSource);
                lastSource.setDocumentHandler(filter);
                lastSource = filter;
            }
        }
        lastSource.setDocumentHandler(fDocumentHandler);
    }

    //
    // Interfaces
    //

    /**
     * Defines an error reporter for reporting HTML errors. There is no such
     * thing as a fatal error in parsing HTML. I/O errors are fatal but should
     * throw an <code>IOException</code> directly instead of reporting an error.
     * <p>
     * When used in a configuration, the error reporter instance should be
     * set as a property with the following property identifier:
     * <pre>
     * "http://cyberneko.org/html/internal/error-reporter" in the
     * </pre>
     * Components in the configuration can query the error reporter using this
     * property identifier.
     * <p>
     * <strong>Note:</strong>
     * All reported errors are within the domain "http://cyberneko.org/html".
     *
     * @author Andy Clark
     */
    protected class ErrorReporter
        implements HTMLErrorReporter {

        //
        // Data
        //

        /** Last locale. */
        protected Locale fLastLocale;

        /** Error messages. */
        protected ResourceBundle fErrorMessages;

        //
        // HTMLErrorReporter methods
        //

        /** Format message without reporting error. */
        @Override
        public String formatMessage(String key, Object[] args) {
            if (!getFeature(SIMPLE_ERROR_FORMAT)) {
                if (!fLocale.equals(fLastLocale)) {
                    fErrorMessages = null;
                    fLastLocale = fLocale;
                }
                if (fErrorMessages == null) {
                    fErrorMessages =
                        ResourceBundle.getBundle("net/sourceforge/htmlunit/cyberneko/res/ErrorMessages",
                                                 fLocale);
                }
                try {
                    final String value = fErrorMessages.getString(key);
                    final String message = MessageFormat.format(value, args);
                    return message;
                }
                catch (final MissingResourceException e) {
                    // ignore and return a simple format
                }
            }
            return formatSimpleMessage(key, args);
        }

        /** Reports a warning. */
        @Override
        public void reportWarning(String key, Object[] args)
            throws XMLParseException {
            if (fErrorHandler != null) {
                fErrorHandler.warning(ERROR_DOMAIN, key, createException(key, args));
            }
        }

        /** Reports an error. */
        @Override
        public void reportError(String key, Object[] args)
            throws XMLParseException {
            if (fErrorHandler != null) {
                fErrorHandler.error(ERROR_DOMAIN, key, createException(key, args));
            }
        }

        //
        // Protected methods
        //

        // Creates parse exception.
        protected XMLParseException createException(String key, Object[] args) {
            final String message = formatMessage(key, args);
            return new XMLParseException(fDocumentScanner, message);
        }

        // Format simple message.
        protected String formatSimpleMessage(String key, Object[] args) {
            final StringBuilder str = new StringBuilder();
            str.append(ERROR_DOMAIN);
            str.append('#');
            str.append(key);
            if (args != null && args.length > 0) {
                str.append('\t');
                for (int i = 0; i < args.length; i++) {
                    if (i > 0) {
                        str.append('\t');
                    }
                    str.append(String.valueOf(args[i]));
                }
            }
            return str.toString();
        }
    }
}
