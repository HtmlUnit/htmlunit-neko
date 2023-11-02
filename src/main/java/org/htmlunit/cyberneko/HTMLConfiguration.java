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
package org.htmlunit.cyberneko;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.htmlunit.cyberneko.filters.NamespaceBinder;
import org.htmlunit.cyberneko.xerces.util.ParserConfigurationSettings;
import org.htmlunit.cyberneko.xerces.xni.XMLDocumentHandler;
import org.htmlunit.cyberneko.xerces.xni.XNIException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLConfigurationException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLDocumentFilter;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLDocumentSource;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLErrorHandler;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLInputSource;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLParseException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLParserConfiguration;

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
public class HTMLConfiguration extends ParserConfigurationSettings implements XMLParserConfiguration {

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

    /** Document handler. */
    private XMLDocumentHandler documentHandler_;

    /** Error handler. */
    private XMLErrorHandler errorHandler_;

    /**
     * Stream opened by parser. Therefore, must close stream manually upon
     * termination of parsing.
     */
    private boolean closeStream_;

    /** Components. */
    private final List<HTMLComponent> htmlComponents_ = new ArrayList<>(2);

    /** Document scanner. */
    private final HTMLScanner documentScanner_ = createDocumentScanner();

    /** HTML tag balancer. */
    private final HTMLTagBalancer tagBalancer_ = new HTMLTagBalancer(this);

    /** Namespace binder. */
    private final NamespaceBinder namespaceBinder_ = new NamespaceBinder(this);

    public final HTMLElements htmlElements_;

    /** Default constructor. */
    public HTMLConfiguration() {
        this(new HTMLElements());
    }

    public HTMLConfiguration(final HTMLElements htmlElements) {
        htmlElements_ = htmlElements;

        // add components
        addComponent(documentScanner_);
        addComponent(tagBalancer_);
        addComponent(namespaceBinder_);

        // recognized features
        final String[] recognizedFeatures = {
            AUGMENTATIONS,
            NAMESPACES,
            REPORT_ERRORS,
            SIMPLE_ERROR_FORMAT,
        };
        addRecognizedFeatures(recognizedFeatures);
        setFeature(AUGMENTATIONS, false);
        setFeature(NAMESPACES, true);
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
        setProperty(ERROR_REPORTER, new ErrorReporter());
    }

    protected HTMLScanner createDocumentScanner() {
        return new HTMLScanner(this);
    }

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
    public void pushInputSource(final XMLInputSource inputSource) {
        documentScanner_.pushInputSource(inputSource);
    }

    /**
     * <span style="color: red">EXPERIMENTAL: may change in next release</span><br>
     * Immediately evaluates an input source and add the new content (e.g.
     * the output written by an embedded script).
     *
     * @param inputSource The new input source to start scanning.
     * @see #pushInputSource(XMLInputSource)
     */
    public void evaluateInputSource(final XMLInputSource inputSource) {
        documentScanner_.evaluateInputSource(inputSource);
    }

    // Sets a feature.
    @Override
    public void setFeature(final String featureId, final boolean state)
        throws XMLConfigurationException {
        super.setFeature(featureId, state);
        for (final HTMLComponent component : htmlComponents_) {
            component.setFeature(featureId, state);
        }
    }

    // Sets a property.
    @Override
    public void setProperty(final String propertyId, final Object value)
        throws XMLConfigurationException {
        super.setProperty(propertyId, value);

        if (propertyId.equals(FILTERS)) {
            final XMLDocumentFilter[] filters = (XMLDocumentFilter[]) getProperty(FILTERS);
            if (filters != null) {
                for (final XMLDocumentFilter filter : filters) {
                    if (filter instanceof HTMLComponent) {
                        addComponent((HTMLComponent) filter);
                    }
                }
            }
        }

        for (final HTMLComponent component : htmlComponents_) {
            component.setProperty(propertyId, value);
        }
    }

    // Sets the document handler.
    @Override
    public void setDocumentHandler(final XMLDocumentHandler handler) {
        documentHandler_ = handler;
        if (handler instanceof HTMLTagBalancingListener) {
            tagBalancer_.setTagBalancingListener((HTMLTagBalancingListener) handler);
        }
    }

    /** @return the document handler. */
    @Override
    public XMLDocumentHandler getDocumentHandler() {
        return documentHandler_;
    }

    // Sets the error handler.
    @Override
    public void setErrorHandler(final XMLErrorHandler handler) {
        errorHandler_ = handler;
    }

    /** @return the error handler. */
    @Override
    public XMLErrorHandler getErrorHandler() {
        return errorHandler_;
    }

    /** Parses a document. */
    @Override
    public void parse(final XMLInputSource source) throws XNIException, IOException {
        setInputSource(source);
        parse(true);
    }

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
    public void setInputSource(final XMLInputSource inputSource)
        throws XMLConfigurationException, IOException {
        reset();
        closeStream_ = inputSource.getByteStream() == null && inputSource.getCharacterStream() == null;
        documentScanner_.setInputSource(inputSource);
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
    public boolean parse(final boolean complete) throws XNIException, IOException {
        try {
            final boolean more = documentScanner_.scanDocument(complete);
            if (!more) {
                cleanup();
            }
            return more;
        }
        catch (final XNIException | IOException e) {
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
        documentScanner_.cleanup(closeStream_);
    }

    // Adds a component.
    protected void addComponent(final HTMLComponent component) {

        // add component to list
        htmlComponents_.add(component);

        // add recognized features and set default states
        final String[] features = component.getRecognizedFeatures();
        addRecognizedFeatures(features);
        if (features != null) {
            for (final String feature : features) {
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
            for (final String property : properties) {
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
        for (final HTMLComponent component : htmlComponents_) {
            component.reset(this);
        }

        // configure pipeline
        XMLDocumentSource lastSource = documentScanner_;
        if (getFeature(NAMESPACES)) {
            lastSource.setDocumentHandler(namespaceBinder_);
            namespaceBinder_.setDocumentSource(tagBalancer_);
            lastSource = namespaceBinder_;
        }

        lastSource.setDocumentHandler(tagBalancer_);
        tagBalancer_.setDocumentSource(documentScanner_);
        lastSource = tagBalancer_;

        final XMLDocumentFilter[] filters = (XMLDocumentFilter[]) getProperty(FILTERS);
        if (filters != null) {
            for (final XMLDocumentFilter filter : filters) {
                filter.setDocumentSource(lastSource);
                lastSource.setDocumentHandler(filter);
                lastSource = filter;
            }
        }
        lastSource.setDocumentHandler(documentHandler_);
    }

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
    protected class ErrorReporter implements HTMLErrorReporter {

        /** Error messages. */
        private ResourceBundle errorMessages_;

        /** Format message without reporting error. */
        @Override
        public String formatMessage(final String key, final Object[] args) {
            if (!getFeature(SIMPLE_ERROR_FORMAT)) {
                if (errorMessages_ == null) {
                    errorMessages_ =
                        ResourceBundle.getBundle("org/htmlunit/cyberneko/res/ErrorMessages");
                }
                try {
                    final String value = errorMessages_.getString(key);
                    return MessageFormat.format(value, args);
                }
                catch (final MissingResourceException e) {
                    // ignore and return a simple format
                }
            }
            return formatSimpleMessage(key, args);
        }

        /** Reports a warning. */
        @Override
        public void reportWarning(final String key, final Object[] args)
            throws XMLParseException {
            if (errorHandler_ != null) {
                errorHandler_.warning(ERROR_DOMAIN, key, createException(key, args));
            }
        }

        /** Reports an error. */
        @Override
        public void reportError(final String key, final Object[] args)
            throws XMLParseException {
            if (errorHandler_ != null) {
                errorHandler_.error(ERROR_DOMAIN, key, createException(key, args));
            }
        }

        // Creates parse exception.
        protected XMLParseException createException(final String key, final Object[] args) {
            final String message = formatMessage(key, args);
            return new XMLParseException(documentScanner_, message);
        }

        // Format simple message.
        protected String formatSimpleMessage(final String key, final Object[] args) {
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
                    str.append(args[i]);
                }
            }
            return str.toString();
        }
    }

    /** getHtmlComponents_ */
    public List<HTMLComponent> getHtmlComponents_() {
        return htmlComponents_;
    }

    /** getDocumentScanner_ */
    public HTMLScanner getDocumentScanner_() {
        return documentScanner_;
    }

    /** getTagBalancer_ */
    public HTMLTagBalancer getTagBalancer_() {
        return tagBalancer_;
    }

    /** getNamespaceBinder_ */
    public NamespaceBinder getNamespaceBinder_() {
        return namespaceBinder_;
    }
}
