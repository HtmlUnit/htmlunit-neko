/*
 * Copyright (c) 2002-2009 Andy Clark, Marc Guillemot
 * Copyright (c) 2017-2026 Ronald Brill
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.htmlunit.cyberneko.HTMLElements.Element;
import org.htmlunit.cyberneko.filters.NamespaceBinder;
import org.htmlunit.cyberneko.xerces.util.XMLAttributesImpl;
import org.htmlunit.cyberneko.xerces.util.XMLAttributesImpl.EmptyXMLAttributesImpl;
import org.htmlunit.cyberneko.xerces.xni.Augmentations;
import org.htmlunit.cyberneko.xerces.xni.NamespaceContext;
import org.htmlunit.cyberneko.xerces.xni.QName;
import org.htmlunit.cyberneko.xerces.xni.XMLAttributes;
import org.htmlunit.cyberneko.xerces.xni.XMLDocumentHandler;
import org.htmlunit.cyberneko.xerces.xni.XMLLocator;
import org.htmlunit.cyberneko.xerces.xni.XMLString;
import org.htmlunit.cyberneko.xerces.xni.XNIException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLConfigurationException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLDocumentFilter;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLDocumentSource;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLParserConfiguration;

/**
 * Balances tags in an HTML document. This component receives document events
 * and tries to correct many common mistakes that human (and computer) HTML
 * document authors make. This tag balancer can:
 * <ul>
 * <li>add missing parent elements;
 * <li>automatically close elements with optional end tags; and
 * <li>handle mis-matched inline element tags.
 * </ul>
 * <p>
 * This component recognizes the following features:
 * <ul>
 * <li>http://cyberneko.org/html/features/augmentations
 * <li>http://cyberneko.org/html/features/report-errors
 * <li>http://cyberneko.org/html/features/balance-tags/document-fragment
 * <li>http://cyberneko.org/html/features/balance-tags/ignore-outside-content
 * </ul>
 * <p>
 * This component recognizes the following properties:
 * <ul>
 * <li>http://cyberneko.org/html/properties/names/elems
 * <li>http://cyberneko.org/html/properties/names/attrs
 * <li>http://cyberneko.org/html/properties/error-reporter
 * <li>http://cyberneko.org/html/properties/balance-tags/current-stack
 * </ul>
 *
 * @see HTMLElements
 *
 * @author Andy Clark
 * @author Marc Guillemot
 * @author Ronald Brill
 */
public class HTMLTagBalancer
    implements XMLDocumentFilter, HTMLComponent {

    // features

    /** Namespaces. */
    protected static final String NAMESPACES = "http://xml.org/sax/features/namespaces";

    /** Include infoset augmentations. */
    protected static final String AUGMENTATIONS = "http://cyberneko.org/html/features/augmentations";

    /** Report errors. */
    protected static final String REPORT_ERRORS = "http://cyberneko.org/html/features/report-errors";

    /** Document fragment balancing only. */
    protected static final String DOCUMENT_FRAGMENT
                            = "http://cyberneko.org/html/features/balance-tags/document-fragment";

    /** Ignore outside content. */
    protected static final String IGNORE_OUTSIDE_CONTENT
                            = "http://cyberneko.org/html/features/balance-tags/ignore-outside-content";

    /** Recognized features. */
    private static final String[] RECOGNIZED_FEATURES = {
        NAMESPACES,
        AUGMENTATIONS,
        REPORT_ERRORS,
        DOCUMENT_FRAGMENT,
        IGNORE_OUTSIDE_CONTENT,
    };

    /** Recognized features defaults. */
    private static final Boolean[] RECOGNIZED_FEATURES_DEFAULTS = {
        null,
        null,
        null,
        Boolean.FALSE,
        Boolean.FALSE,
    };

    // properties

    /** Modify HTML element names: { "upper", "lower", "default" }. */
    protected static final String NAMES_ELEMS = "http://cyberneko.org/html/properties/names/elems";

    /** Modify HTML attribute names: { "upper", "lower", "default" }. */
    protected static final String NAMES_ATTRS = "http://cyberneko.org/html/properties/names/attrs";

    /** Error reporter. */
    protected static final String ERROR_REPORTER = "http://cyberneko.org/html/properties/error-reporter";

    /**
     * &lt;font color="red"&gt;EXPERIMENTAL: may change in next release&lt;/font&gt;&lt;br/&gt;
     * Name of the property holding the stack of elements in which context a document fragment should be parsed.
     */
    public static final String FRAGMENT_CONTEXT_STACK
                                = "http://cyberneko.org/html/properties/balance-tags/fragment-context-stack";

    /** Recognized properties. */
    private static final String[] RECOGNIZED_PROPERTIES = {
        NAMES_ELEMS,
        NAMES_ATTRS,
        ERROR_REPORTER,
        FRAGMENT_CONTEXT_STACK,
    };

    /** Recognized properties defaults. */
    private static final Object[] RECOGNIZED_PROPERTIES_DEFAULTS = {
        null,
        null,
        null,
        null,
    };

    /**
     * Maximum distance (in open elements) between the top of the stack and an
     * already open {@code <a>}/{@code <nobr>} for which the un-nesting
     * described in {@link #closeAndReopenThroughElement(int)} is still
     * performed. Beyond this distance the new element is simply nested as
     * before, which bounds the work (and the number of synthesized events)
     * caused by a single start tag on adversarial input.
     */
    private static final int MAX_ADOPTION_DEPTH = 100;

    /**
     * Maximum number of stack entries examined when searching for a parent
     * that is limited by a bounds element (see {@link #hasOpenParent}).
     * Table structures nested deeper than this are not meaningful in real
     * documents and are typically the signature of a denial-of-service input,
     * so the search is cut off instead of walking the whole stack for every
     * start tag.
     */
    private static final int MAX_PARENT_SEARCH_DEPTH = 100;

    /**
     * Maximum number of stack entries examined when searching for open elements
     * that a new start tag implicitly closes (e.g. a {@code <td>} closing the
     * previous {@code <td>}, an {@code <li>} closing the previous {@code <li>}).
     * <p>
     * The search normally ends quickly because it stops at block level elements
     * and at parents of the new element. Elements that are neither (like
     * {@code <div>}) do not stop it, so an input with many open elements followed
     * by many such start tags would otherwise cost one full stack walk per tag
     * (quadratic). Closing an element that is buried deeper than this limit is
     * not meaningful in real documents; in that case the new element is simply
     * nested instead.
     */
    private static final int MAX_CLOSE_SEARCH_DEPTH = 100;

    /**
     * Maximum number of stack entries examined by the "context" searches:
     * <ul>
     * <li>whether a {@code <table>}/{@code <form>} start tag is inside table structure but outside
     *     a cell (see {@link #isInTableStructureOutsideCell()}), and</li>
     * <li>finding the open element an end tag closes (see {@link #getElementDepth}).</li>
     * </ul>
     * Both walks can fail without removing anything from the stack, so on an input with many
     * open elements followed by many such tags every tag would cost a full stack walk (quadratic).
     * An end tag whose element is buried deeper than this limit is treated like an end tag
     * without a matching start tag.
     */
    private static final int MAX_CONTEXT_SEARCH_DEPTH = 100;

    /**
     * Maximum nesting depth of the elements in the generated document. Elements beyond this
     * depth do not nest any further: they are reported as empty elements and their content ends
     * up in the deepest open element. Browsers limit the depth of the DOM tree as well (Chrome 512);
     * without a limit a few kilobytes of unclosed tags produce trees that are
     * deep enough to overflow the stack of every recursive tree walker (DoS protection).
     */
    private static final int MAX_ELEMENT_DEPTH = 512;

    /** Stacks that grew beyond this capacity are shrunk back when they are cleared. */
    private static final int MAX_RETAINED_STACK_CAPACITY = 1 << 12;

    /**
     * Largest number of slots added at once when one of the element stacks has to
     * grow. Below this size the capacity doubles, above it the stack grows linearly
     * in steps of this size (262,144 entries, about 1 MB of references).
     */
    private static final int MAX_STACK_GROWTH = 1 << 18;

    /** Don't modify HTML names. */
    private static final short NAMES_NO_CHANGE = 0;

    /** Uppercase HTML names. */
    private static final short NAMES_UPPERCASE = 1;

    /** Lowercase HTML names. */
    private static final short NAMES_LOWERCASE = 2;

    /** Namespaces. */
    protected boolean fNamespaces;

    /** Include infoset augmentations. */
    protected boolean fAugmentations;

    /** Report errors. */
    protected boolean fReportErrors;

    /** Document fragment balancing only. */
    protected boolean fDocumentFragment;

    /** Template document fragment balancing only. */
    protected boolean fTemplateFragment;

    /** Ignore outside content. */
    protected boolean fIgnoreOutsideContent;

    /** Allows self closing iframe tags. */
    protected boolean fAllowSelfclosingIframe;

    /** Allows self closing script tags. */
    protected boolean fAllowSelfclosingScript;

    /** Allows self closing tags. */
    protected boolean fAllowSelfclosingTags;

    // properties

    /** Modify HTML element names. */
    protected short fNamesElems;

    /** Error reporter. */
    protected HTMLErrorReporter fErrorReporter;

    // connections

    private XMLDocumentSource documentSource_;

    /** The document handler. */
    private XMLDocumentHandler documentHandler_;

    // state

    /** The element stack. */
    protected final InfoStackWithOpenCounters fElementStack = new InfoStackWithOpenCounters(20);

    /** The inline stack. */
    protected final InfoStack fFormattingStack = new InfoStack(10);

    /** True if seen anything. Important for xml declaration. */
    protected boolean fSeenAnything;

    /** True if doctype has been seen. */
    protected boolean fSeenDoctype;

    /** True if root element has been seen. */
    protected boolean fSeenRootElement;

    /**
     * True if seen the end of the document element. In other words,
     * this variable is set to false <em>until</em> the end &lt;/HTML&gt;
     * tag is seen (or synthesized). This is used to ensure that
     * extraneous events after the end of the document element do not
     * make the document stream ill-formed.
     */
    protected boolean fSeenRootElementEnd;

    /** True if an explicit (non-synthesized) {@code <html>} element has been seen. */
    protected boolean fSeenRealHtmlElement;

    /** True if seen {@code <head>} element. */
    protected boolean fSeenHeadElement;

    /** True if seen {@code <body>} element. */
    protected boolean fSeenBodyElement;
    private boolean fSeenBodyElementEnd;

    /** True if seen {@code frameset} element. */
    private boolean fSeenFramesetElement;

    /** True if seen non whitespace characters. */
    private boolean fSeenCharacters;

    /**
     * True if a form is in the stack (allow to discard opening of nested forms).
     */
    protected boolean fOpenedForm;

    /**
     * True if a svg is in the stack (no parent checking takes place).
     */
    protected boolean fOpenedSvg;

    /**
     * True if a select is in the stack.
     */
    protected boolean fOpenedSelect;

    // temp vars

    /** A qualified name. */
    private final EmptyXMLAttributesImpl fEmptyXMLAttributes = XMLAttributesImpl.EmptyXMLAttributesImpl.INSTANCE;

    /** A qualified name. */
    private final QName fQName = new QName();

    protected HTMLTagBalancingListener tagBalancingListener;
    private final LostText lostText_ = new LostText();

    private boolean forcedStartElement_;
    private boolean forcedEndElement_;

    /**
     * Stack of elements determining the context in which a document fragment should be parsed
     */
    private QName[] fragmentContextStack_ = null;
    private int fragmentContextStackSize_ = 0; // not 0 only when a fragment is parsed and fragmentContextStack_ is set

    private final List<ElementEntry> endElementsBuffer_ = new ArrayList<>(4);
    /**
     * Number of discarded {@code <body>}/{@code <html>} start tags per raw name. Only these two are
     * ever looked up (their end tags are swallowed once), so nothing else is recorded; a list
     * would make every end tag a linear scan.
     */
    private final Map<String, Integer> discardedStartElements = new HashMap<>(4);

    private final HTMLConfiguration htmlConfiguration_;

    HTMLTagBalancer(final HTMLConfiguration htmlConfiguration) {
        htmlConfiguration_ = htmlConfiguration;
    }

    /** Returns the default state for a feature. */
    @Override
    public Boolean getFeatureDefault(final String featureId) {
        final int length = RECOGNIZED_FEATURES.length;
        for (int i = 0; i < length; i++) {
            if (RECOGNIZED_FEATURES[i].equals(featureId)) {
                return RECOGNIZED_FEATURES_DEFAULTS[i];
            }
        }
        return null;
    }

    /** Returns the default state for a property. */
    @Override
    public Object getPropertyDefault(final String propertyId) {
        final int length = RECOGNIZED_PROPERTIES.length;
        for (int i = 0; i < length; i++) {
            if (RECOGNIZED_PROPERTIES[i].equals(propertyId)) {
                return RECOGNIZED_PROPERTIES_DEFAULTS[i];
            }
        }
        return null;
    }

    /** Returns recognized features. */
    @Override
    public String[] getRecognizedFeatures() {
        return RECOGNIZED_FEATURES;
    }

    /** Returns recognized properties. */
    @Override
    public String[] getRecognizedProperties() {
        return RECOGNIZED_PROPERTIES;
    }

    /** Resets the component. */
    @Override
    public void reset(final XMLParserConfiguration xmlParserConfiguration) throws XMLConfigurationException {
        // get features
        fNamespaces = xmlParserConfiguration.getFeature(NAMESPACES);
        fAugmentations = xmlParserConfiguration.getFeature(AUGMENTATIONS);
        fReportErrors = xmlParserConfiguration.getFeature(REPORT_ERRORS);
        fDocumentFragment = xmlParserConfiguration.getFeature(DOCUMENT_FRAGMENT);
        fIgnoreOutsideContent = xmlParserConfiguration.getFeature(IGNORE_OUTSIDE_CONTENT);
        fAllowSelfclosingIframe = xmlParserConfiguration.getFeature(HTMLScanner.ALLOW_SELFCLOSING_IFRAME);
        fAllowSelfclosingScript = xmlParserConfiguration.getFeature(HTMLScanner.ALLOW_SELFCLOSING_SCRIPT);
        fAllowSelfclosingTags = xmlParserConfiguration.getFeature(HTMLScanner.ALLOW_SELFCLOSING_TAGS);

        // get properties
        fNamesElems = getNamesValue(String.valueOf(xmlParserConfiguration.getProperty(NAMES_ELEMS)));
        fErrorReporter = (HTMLErrorReporter) xmlParserConfiguration.getProperty(ERROR_REPORTER);

        fSeenAnything = false;
        fSeenDoctype = false;
        fSeenRootElement = false;
        fSeenRootElementEnd = false;
        fSeenRealHtmlElement = false;
        fSeenHeadElement = false;
        fSeenBodyElement = false;
        fSeenBodyElementEnd = false;
        fSeenFramesetElement = false;
        fSeenCharacters = false;
        fTemplateFragment = false;

        fOpenedForm = false;
        fOpenedSelect = false;
        fOpenedSvg = false;

        fragmentContextStack_ = (QName[]) xmlParserConfiguration.getProperty(FRAGMENT_CONTEXT_STACK);
        if (fragmentContextStack_ != null) {
            fSeenAnything = fragmentContextStack_.length > 0;
            // use indexed loop to avoid Iterator allocation
            for (int i = 0; i < fragmentContextStack_.length; i++) {
                final QName qname = fragmentContextStack_[i];
                final String qnameLocalpartLC = qname.getLocalpart().toLowerCase(Locale.ROOT);
                switch (qnameLocalpartLC) {
                    case "html" -> {
                        fSeenRootElement = true;
                        fSeenRealHtmlElement = true;
                    }
                    case "body" -> {
                        fSeenHeadElement = true;
                        fSeenBodyElement = true;
                    }
                    case "form" -> fOpenedForm = true;
                    case "select" -> fOpenedSelect = true;
                    case "svg" -> fOpenedSvg = true;
                }
            }
        }

        lostText_.clear();

        forcedStartElement_ = false;
        forcedEndElement_ = false;

        endElementsBuffer_.clear();
        discardedStartElements.clear();
    }

    /** Sets a feature. */
    @Override
    public void setFeature(final String featureId, final boolean state)
        throws XMLConfigurationException {

        switch (featureId) {
            case AUGMENTATIONS:
                fAugmentations = state;
                return;
            case REPORT_ERRORS:
                fReportErrors = state;
                return;
            case IGNORE_OUTSIDE_CONTENT:
                fIgnoreOutsideContent = state;
                return;
        }
    }

    /** Sets a property. */
    @Override
    public void setProperty(final String propertyId, final Object value)
        throws XMLConfigurationException {

        if (propertyId.equals(NAMES_ELEMS)) {
            fNamesElems = getNamesValue(String.valueOf(value));
            return;
        }
    }

    /** Sets the document handler. */
    @Override
    public void setDocumentHandler(final XMLDocumentHandler handler) {
        documentHandler_ = handler;
    }

    /** Returns the document handler. */
    @Override
    public XMLDocumentHandler getDocumentHandler() {
        return documentHandler_;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDocumentSource(final XMLDocumentSource source) {
        documentSource_ = source;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLDocumentSource getDocumentSource() {
        return documentSource_;
    }

    /** Start document. */
    @Override
    public void startDocument(final XMLLocator locator, final String encoding,
                    final NamespaceContext nscontext, final Augmentations augs)
        throws XNIException {

        // reset state
        fElementStack.clear();

        if (fragmentContextStack_ != null) {
            fragmentContextStackSize_ = fragmentContextStack_.length;
            // use indexed loop to avoid Iterator allocation
            for (int i = 0; i < fragmentContextStackSize_; i++) {
                final QName name = fragmentContextStack_[i];
                final Element elt = htmlConfiguration_.getHtmlElements().getElement(name.getLocalpart());
                fElementStack.push(new Info(elt, name));
            }

        }
        else {
            fragmentContextStackSize_ = 0;
        }

        // pass on event
        if (documentHandler_ != null) {
            documentHandler_.startDocument(locator, encoding, nscontext, augs);
        }

    }

    // old methods

    /** XML declaration. */
    @Override
    public void xmlDecl(final String version, final String encoding, final String standalone,
                    final Augmentations augs) throws XNIException {
        if (!fSeenAnything && documentHandler_ != null) {
            documentHandler_.xmlDecl(version, encoding, standalone, augs);
        }
    }

    /** Doctype declaration. */
    @Override
    public void doctypeDecl(final String rootElementName, final String publicId,
            final String systemId, final Augmentations augs) throws XNIException {
        fSeenAnything = true;
        if (fReportErrors) {
            if (fSeenRootElement) {
                fErrorReporter.reportError("HTML2010", null);
            }
            else if (fSeenDoctype) {
                fErrorReporter.reportError("HTML2011", null);
            }
        }
        if (!fSeenRootElement && !fSeenDoctype) {
            fSeenDoctype = true;
            if (documentHandler_ != null) {
                documentHandler_.doctypeDecl(rootElementName, publicId, systemId, augs);
            }
        }
    }

    /** End document. */
    @Override
    public void endDocument(final Augmentations augs) throws XNIException {

        // </body> and </html> have been buffered to consider outside content
        fIgnoreOutsideContent = true; // endElement should not ignore the elements passed from buffer
        consumeBufferedEndElements();

        // handle empty document
        if (!fSeenRootElement && !fDocumentFragment) {
            if (fReportErrors) {
                fErrorReporter.reportError("HTML2000", null);
            }
            if (documentHandler_ != null) {
                fSeenRootElementEnd = false;
                forceStartBody(); // will force <html> and <head></head>
                final String body = NAMES_UPPERCASE == fNamesElems ? "BODY" : "body";
                fQName.setValues(null, body, body, null);
                callEndElement(fQName, synthesizedAugs());

                final String ename = NAMES_UPPERCASE == fNamesElems ? "HTML" : "html";
                fQName.setValues(null, ename, ename, null);
                callEndElement(fQName, synthesizedAugs());
            }
        }

        // pop all remaining elements
        else {
            final int length = fElementStack.length - fragmentContextStackSize_;
            for (int i = 0; i < length; i++) {
                final Info info = fElementStack.pop();
                if (fReportErrors) {
                    final String ename = info.qname.getRawname();
                    fErrorReporter.reportWarning("HTML2001", new Object[]{ename});
                }
                if (documentHandler_ != null) {
                    addBodyIfNeeded(info.element.code);
                    callEndElement(info.qname, synthesizedAugs());
                }
            }
        }

        // call handler
        if (documentHandler_ != null) {
            documentHandler_.endDocument(augs);
        }

    }

    /**
     * Consumes end-element events that have been buffered (e.g.
     * {@code </head>}, {@code </body>}, {@code </html>}) so they can be
     * replayed at the correct point in the document — typically at the
     * very end, after all outside content has been processed.
     * <p>
     * The size-1 and size-2 fast paths avoid allocating a copy of the
     * buffer list. These cover the overwhelmingly common cases:
     * <ul>
     *   <li><b>size 1</b> — only {@code </head>} was buffered.</li>
     *   <li><b>size 2</b> — {@code </body>} and {@code </html>} were
     *       buffered (or {@code </head>} followed by {@code </body>}).</li>
     * </ul>
     * The general path (size &gt; 2) makes a defensive copy because
     * {@link #endElement} may re-enter this method or modify the buffer.
     */
    private void consumeBufferedEndElements() {
        final int s = endElementsBuffer_.size();
        if (s == 0) {
            return;
        }

        if (s == 1) {
            final ElementEntry entry = endElementsBuffer_.get(0);
            forcedEndElement_ = true;
            endElement(entry.name_, entry.augs_);
            endElementsBuffer_.clear();
            return;
        }

        if (s == 2) {
            final ElementEntry entry0 = endElementsBuffer_.get(0);
            final ElementEntry entry1 = endElementsBuffer_.get(1);
            forcedEndElement_ = true;
            endElement(entry0.name_, entry0.augs_);
            endElement(entry1.name_, entry1.augs_);
            endElementsBuffer_.clear();
            return;
        }

        final int bufferSize = endElementsBuffer_.size();
        for (int i = 0; i < bufferSize; i++) {
            final ElementEntry entry = endElementsBuffer_.get(i);
            forcedEndElement_ = true;
            endElement(entry.name_, entry.augs_);
        }
        endElementsBuffer_.subList(0, bufferSize).clear();
    }

    /** Comment. */
    @Override
    public void comment(final XMLString text, final Augmentations augs) throws XNIException {
        fSeenAnything = true;
        consumeEarlyTextIfNeeded();
        if (documentHandler_ != null) {
            documentHandler_.comment(text, augs);
        }
    }

    private void consumeEarlyTextIfNeeded() {
        if (!lostText_.isEmpty()) {
            if (!fSeenBodyElement) {
                forceStartBody();
            }
            lostText_.refeed(this);
        }
    }

    /** Processing instruction. */
    @Override
    public void processingInstruction(final String target, final XMLString data,
                    final Augmentations augs) throws XNIException {
        fSeenAnything = true;
        consumeEarlyTextIfNeeded();
        if (documentHandler_ != null) {
            documentHandler_.processingInstruction(target, data, augs);
        }
    }

    /** Start element. */
    @Override
    public void startElement(final QName elem, XMLAttributes attrs, final Augmentations augs)
        throws XNIException {
        fSeenAnything = true;

        final boolean isForcedCreation = forcedStartElement_;
        forcedStartElement_ = false;

        // check for end of document
        if (fSeenRootElementEnd) {
            notifyDiscardedStartElement(elem, attrs, augs);
            return;
        }

        // get element information
        final HTMLElements.Element element = getElement(elem);
        final short elementCode = element.code;

        // HTML5: a new <a> or <nobr> closes an open one of the same type, even across
        // intervening elements (see closeAndReopenThroughElement). Other formatting
        // elements (<b>, <i>, <u>, ...) have no such rule and nest normally.
        // findLastOpen() already respects marker elements and MAX_ADOPTION_DEPTH.
        if (elementCode == HTMLElements.A || elementCode == HTMLElements.NOBR) {
            final int pos = findLastOpen(elementCode);
            if (pos != -1) {
                closeAndReopenThroughElement(pos);
            }
        }

        reopenFormattingElements(null);

        if (elementCode == HTMLElements.TEMPLATE) {
            fTemplateFragment = true;
        }

        // the creation of some elements like TABLE or SELECT can't be forced. Any others?
        if (isForcedCreation && (elementCode == HTMLElements.TABLE || elementCode == HTMLElements.SELECT)) {
            return; // don't accept creation
        }

        // ignore multiple html, head, body elements
        if (fSeenRootElement && elementCode == HTMLElements.HTML && !fOpenedSvg) {
            notifyDiscardedStartElement(elem, attrs, augs);
            return;
        }
        // accept only frame and frameset within frameset
        if (fSeenFramesetElement
                && elementCode != HTMLElements.FRAME
                && elementCode != HTMLElements.FRAMESET
                && elementCode != HTMLElements.NOFRAMES) {
            notifyDiscardedStartElement(elem, attrs, augs);
            return;
        }

        if (!fTemplateFragment && fOpenedSelect) {
            if (elementCode == HTMLElements.SELECT) {
                final QName head = createQName(fNamesElems == NAMES_UPPERCASE ? "SELECT" : "select");
                endElement(head, synthesizedAugs());

                notifyDiscardedStartElement(elem, attrs, augs);
                return;
            }
        }

        if (elementCode == HTMLElements.HTML && !isForcedCreation) {
            fSeenRealHtmlElement = true;
        }

        if (elementCode == HTMLElements.HEAD) {
            if (fSeenHeadElement) {
                notifyDiscardedStartElement(elem, attrs, augs);
                return;
            }
            fSeenHeadElement = true;
        }
        else if (!fOpenedSvg && elementCode == HTMLElements.FRAMESET) {
            if (fSeenBodyElement && fSeenCharacters) {
                notifyDiscardedStartElement(elem, attrs, augs);
                return;
            }
            // create <head></head> if none was present
            if (!fSeenHeadElement) {
                final QName head = createQName(fNamesElems == NAMES_UPPERCASE ? "HEAD" : "head");
                forceStartElement(head, fEmptyXMLAttributes, synthesizedAugs());
                endElement(head, synthesizedAugs());
            }
            consumeBufferedEndElements(); // </head> (if any) has been buffered

            // maybe we had some text before...
            consumeEarlyTextIfNeeded();
            // check body again
            if (fSeenBodyElement) {
                notifyDiscardedStartElement(elem, attrs, augs);
                return;
            }

            fSeenFramesetElement = true;
        }
        else if (elementCode == HTMLElements.BODY) {
            // create <head></head> if none was present
            if (!fSeenHeadElement) {
                final QName head = createQName(fNamesElems == NAMES_UPPERCASE ? "HEAD" : "head");
                forceStartElement(head, fEmptyXMLAttributes, synthesizedAugs());
                endElement(head, synthesizedAugs());
            }
            consumeBufferedEndElements(); // </head> (if any) has been buffered

            if (fSeenBodyElement) {
                notifyDiscardedStartElement(elem, attrs, augs);
                return;
            }
            fSeenBodyElement = true;
        }
        else if (elementCode == HTMLElements.FORM) {
            if (fOpenedForm) {
                notifyDiscardedStartElement(elem, attrs, augs);
                return;
            }

            fOpenedForm = true;

            // check if inside a table
            //    forms are only valid inside td/th/caption
            //    otherwise close the form
            if (isInTableStructureOutsideCell()) {
                if (documentHandler_ != null) {
                    callStartElement(elem, attrs, augs);
                    callEndElement(createQName(fNamesElems == NAMES_UPPERCASE ? "FORM" : "form"),
                                        synthesizedAugs());
                }
                fOpenedForm = false;
                return;
            }
        }
        else if (fSeenHeadElement
                    && !fSeenFramesetElement
                    && !fOpenedSvg
                    && elementCode == HTMLElements.FRAME) {
            notifyDiscardedStartElement(elem, attrs, augs);
            return;
        }
        else if (elementCode == HTMLElements.UNKNOWN) {
            consumeBufferedEndElements();
        }
        else if (elementCode == HTMLElements.TABLE) {
            // check if inside another table
            //    tables are only valid inside td/th/caption
            //    otherwise close the surrounding table
            if (isInTableStructureOutsideCell()) {
                final QName table = createQName(fNamesElems == NAMES_UPPERCASE ? "TABLE" : "table");
                endElement(table, synthesizedAugs());
            }
        }

        // check proper parent
        if (element.parent != null && !fOpenedSvg) {
            final HTMLElements.Element preferedParent = element.parent[0];
            if (fDocumentFragment
                    && !fSeenRealHtmlElement
                    && (preferedParent.code == HTMLElements.HEAD || preferedParent.code == HTMLElements.BODY)) {
                // nothing, don't force HEAD or BODY creation for a document fragment
                // if there was no explicit head before
            }
            else if (fTemplateFragment
                        && fElementStack.length > 0
                        && fElementStack.data[fElementStack.length - 1].element.code == HTMLElements.TEMPLATE) {
                // nothing, don't force/check parent for the direct template children
            }
            else if (!fSeenRootElement && !fDocumentFragment) {
                final String pname = fNamesElems == NAMES_UPPERCASE
                                        ? preferedParent.name : preferedParent.lowercaseName;
                if (fReportErrors) {
                    final String ename = elem.getRawname();
                    fErrorReporter.reportWarning("HTML2002", new Object[]{ename, pname});
                }
                final QName qname = createQName(pname);
                final boolean parentCreated =
                        forceStartElementParentCreated(qname, fEmptyXMLAttributes, synthesizedAugs());
                if (!parentCreated) {
                    if (!isForcedCreation) {
                        notifyDiscardedStartElement(elem, attrs, augs);
                    }
                    return;
                }
            }
            else {
                if (preferedParent.code != HTMLElements.HEAD || (!fSeenBodyElement && !fDocumentFragment)) {
                    if (!hasOpenParent(element)) {
                        // no parent found
                        final String pname = fNamesElems == NAMES_UPPERCASE
                                                ? preferedParent.name : preferedParent.lowercaseName;
                        if (fReportErrors) {
                            final String ename = elem.getRawname();
                            fErrorReporter.reportWarning("HTML2004", new Object[]{ename, pname});
                        }

                        final QName qname = createQName(pname);
                        final boolean parentCreated =
                                        forceStartElementParentCreated(qname, fEmptyXMLAttributes, synthesizedAugs());
                        if (!parentCreated) {
                            if (!isForcedCreation) {
                                notifyDiscardedStartElement(elem, attrs, augs);
                            }
                            return;
                        }
                    }
                }
            }
        }

        if (elementCode == HTMLElements.SVG) {
            fOpenedSvg = true;
        }
        else if (!fTemplateFragment && elementCode == HTMLElements.SELECT) {
            fOpenedSelect = true;
        }

        // close previous elements
        // all elements close a <script>
        // in head, no element has children
        final int elementsLength = fElementStack.length;
        if ((elementsLength > 1
                && (fElementStack.peek().element.code == HTMLElements.SCRIPT))
                || elementsLength > 2
                && fElementStack.data[elementsLength - 2].element.code == HTMLElements.HEAD) {
            final Info info = fElementStack.pop();
            if (documentHandler_ != null) {
                callEndElement(info.qname, synthesizedAugs());
            }
        }

        if (element.closes != null) {
            int length = fElementStack.length;
            // never look further down than MAX_CLOSE_SEARCH_DEPTH entries (DoS protection)
            final int stop = Math.max(0, length - MAX_CLOSE_SEARCH_DEPTH);
            for (int i = length - 1; i >= stop; i--) {
                Info info = fElementStack.data[i];

                // html elements do not close the title element
                // see https://svgwg.org/svg2-draft/struct.html#DescriptionAndTitleElements
                if (fOpenedSvg && info.element.code == HTMLElements.TITLE) {
                    break;
                }

                // does it close the element we're looking at?
                if (element.closes(info.element.code)) {
                    if (fReportErrors) {
                        final String ename = elem.getRawname();
                        final String iname = info.qname.getRawname();
                        fErrorReporter.reportWarning("HTML2005", new Object[]{ename, iname});
                    }
                    for (int j = length - 1; j >= i; j--) {
                        info = fElementStack.pop();
                        if (j < fragmentContextStackSize_) {
                            fragmentContextStackSize_--;
                        }
                        if (documentHandler_ != null) {
                            // PATCH: Marc-André Morissette
                            callEndElement(info.qname, synthesizedAugs());
                        }
                    }
                    length = i;
                    continue;
                }

                // should we stop searching?
                if (info.element.code == HTMLElements.TEMPLATE
                        || info.element.isBlock() || element.isParent(info.element)) {
                    break;
                }
            }
        }

        // call handler
        fSeenRootElement = true;
        if (element.isEmpty() || fElementStack.length >= MAX_ELEMENT_DEPTH) {
            // elements beyond the maximum depth are reported as empty elements (like browsers
            // do, they do not nest any deeper): they are not pushed, the content goes to the
            // current parent and the end tag is handled like any other unmatched end tag
            if (attrs == null) {
                attrs = fEmptyXMLAttributes;
            }
            if (documentHandler_ != null) {
                documentHandler_.emptyElement(elem, attrs, augs);
            }
        }
        else {
            // Attributes are only needed later if this element may be re-opened:
            //  - formatting elements (reopenFormattingElements); <a> and <nobr> are formatting
            //    elements too, so they are covered and can be cloned by closeAndReopenThroughElement
            //  - any element pushed while an <a>/<nobr> is open (it may be closed and
            //    re-opened as an ancestor by closeAndReopenThroughElement)
            // Other inline elements (option, button, label, q, ...) are never re-opened.
            final boolean keepAttrs = element.isFormatting()
                    || fElementStack.isOpen(HTMLElements.A)
                    || fElementStack.isOpen(HTMLElements.NOBR);
            fElementStack.push(new Info(element, elem, keepAttrs ? attrs : null));

            if (attrs == null) {
                attrs = fEmptyXMLAttributes;
            }
            if (documentHandler_ != null) {
                callStartElement(elem, attrs, augs);
            }
        }

        if (elementCode == HTMLElements.BODY) {
            lostText_.refeed(this);
        }
    }

    /**
     * Forces an element start, taking care to set the information to allow startElement to "see" that's
     * the element has been forced.
     * @return <code>true</code> if creation could be done (TABLE's creation for instance can't be forced)
     */
    private boolean forceStartElementParentCreated(final QName elem, final XMLAttributes attrs,
                        final Augmentations augs) throws XNIException {
        forcedStartElement_ = true;
        startElement(elem, attrs, augs);

        return fElementStack.length > 0 && elem.equals(fElementStack.peek().qname);
    }

    /**
     * Forces an element start, taking care to set the information to allow startElement to "see" that's
     * the element has been forced.
     * @return <code>true</code> if creation could be done (TABLE's creation for instance can't be forced)
     */
    private void forceStartElement(final QName elem, final XMLAttributes attrs,
                        final Augmentations augs) throws XNIException {
        forcedStartElement_ = true;
        startElement(elem, attrs, augs);
    }

    private static QName createQName(final String tagName) {
        return new QName(null, tagName, tagName, NamespaceBinder.XHTML_1_0_URI);
    }

    /** Empty element. */
    @Override
    public void emptyElement(final QName element, final XMLAttributes attrs,
                    final Augmentations augs) throws XNIException {
        startElement(element, attrs, augs);
        // browser ignore the closing indication for non empty tags like <form .../> but not for unknown element
        final HTMLElements.Element elem = getElement(element);
        if (elem.isEmpty()
                || (fAllowSelfclosingTags && elem.code != HTMLElements.IFRAME && elem.code != HTMLElements.SCRIPT)
                || (elem.code == HTMLElements.IFRAME && fAllowSelfclosingIframe)
                || (elem.code == HTMLElements.SCRIPT && fAllowSelfclosingScript)
                || elem.code == HTMLElements.UNKNOWN) {
            endElement(element, augs);
        }
    }

    /**
     * Generates a missing <body> (which creates missing <head> when needed)
     */
    private void forceStartBody() {
        final QName body = createQName(fNamesElems == NAMES_UPPERCASE ? "BODY" : "body");
        if (fReportErrors) {
            fErrorReporter.reportWarning("HTML2006", new Object[]{body.getLocalpart()});
        }
        forceStartElement(body, fEmptyXMLAttributes, synthesizedAugs());
    }

    /** Start CDATA section. */
    @Override
    public void startCDATA(final Augmentations augs) throws XNIException {
        fSeenAnything = true;

        consumeEarlyTextIfNeeded();
        reopenFormattingElements(null);

        // check for end of document
        if (fSeenRootElementEnd) {
            return;
        }

        // call handler
        if (documentHandler_ != null) {
            documentHandler_.startCDATA(augs);
        }

    }

    /** End CDATA section. */
    @Override
    public void endCDATA(final Augmentations augs) throws XNIException {

        // check for end of document
        if (fSeenRootElementEnd) {
            return;
        }

        // call handler
        if (documentHandler_ != null) {
            documentHandler_.endCDATA(augs);
        }

    }

    /** Characters. */
    @Override
    public void characters(final XMLString text, final Augmentations augs) throws XNIException {
        // check for end of document
        if (fSeenRootElementEnd || fSeenBodyElementEnd) {
            return;
        }

        if (fElementStack.length == 0 && !fDocumentFragment) {
            // character before first opening tag
            lostText_.add(text, augs);
            return;
        }

        reopenFormattingElements(null);

        if (!fDocumentFragment) {
            // handle bare characters
            if (!fSeenRootElement) {
                forceStartBody();
            }

            // isWhitespace() can be an expensive opertation because, if you have many
            // whitespace at the beginning of an string
            // therefore we like to call it at late as possible - this leads to a bit
            // strange code but it is worth the price
            int whitespace = -1;
            if (fElementStack.length < 2 || endElementsBuffer_.size() == 1) {
                whitespace = text.isWhitespace() ? 1 : 0;
                if (whitespace == 1) {
                    // ignore spaces directly within <html>
                    return;
                }
            }

            // handle character content in head
            // NOTE: This frequently happens when the document looks like:
            //       <title>Title</title>
            //       And here's some text.
            if (text.length() > 0) {
                final Info info = fElementStack.peek();
                if (info.element.code == HTMLElements.HEAD || info.element.code == HTMLElements.HTML) {
                    if (whitespace == 0) {
                        if (fReportErrors) {
                            final String hname = NAMES_UPPERCASE == fNamesElems ? "HEAD" : "head";
                            final String bname = NAMES_UPPERCASE == fNamesElems ? "BODY" : "body";
                            fErrorReporter.reportWarning("HTML2009", new Object[]{hname, bname});
                        }
                        forceStartBody();
                    }
                    else if (whitespace == -1 && !text.isWhitespace()) {
                        if (fReportErrors) {
                            final String hname = NAMES_UPPERCASE == fNamesElems ? "HEAD" : "head";
                            final String bname = NAMES_UPPERCASE == fNamesElems ? "BODY" : "body";
                            fErrorReporter.reportWarning("HTML2009", new Object[]{hname, bname});
                        }
                        forceStartBody();
                    }
                }
            }
        }

        fSeenCharacters = fSeenCharacters || text.length() > 0;

        // call handler
        if (documentHandler_ != null) {
            documentHandler_.characters(text, augs);
        }
    }

    /** End element. */
    @Override
    public void endElement(final QName element, final Augmentations augs) throws XNIException {
        final boolean isForcedEndElement = forcedEndElement_;
        forcedEndElement_ = false;

        // is there anything to do?
        if (fSeenRootElementEnd) {
            notifyDiscardedEndElement(element, augs);
            return;
        }

        // get element information
        final HTMLElements.Element elem = getElement(element);
        final short elementCode = elem.code;

        if (reopenFormattingElements(elem)) {
            return;
        }

        if (!fTemplateFragment && fOpenedSelect) {
            if (elementCode == HTMLElements.SELECT) {
                fOpenedSelect = false;
            }
        }

        if (elementCode == HTMLElements.TEMPLATE) {
            fTemplateFragment = false;
        }

        // if we consider outside content, just buffer </body> and </html> to consider them at the very end
        if (!fIgnoreOutsideContent
                && (elementCode == HTMLElements.BODY || elementCode == HTMLElements.HTML)) {
            final String rawname = element.getRawname();
            final Integer pending = discardedStartElements.get(rawname);
            if (pending != null) {
                if (pending == 1) {
                    discardedStartElements.remove(rawname);
                }
                else {
                    discardedStartElements.put(rawname, pending - 1);
                }
                return;
            }

            // only add to buffer if the elements was discarded before
            endElementsBuffer_.add(new ElementEntry(element, augs));
            return;
        }

        // accept only frame and frameset within frameset
        if (fSeenFramesetElement && elementCode != HTMLElements.FRAME && elementCode != HTMLElements.FRAMESET) {
            notifyDiscardedEndElement(element, augs);
            return;
        }

        // check for end of document
        if (elementCode == HTMLElements.BR) {
            forceStartElement(element, fEmptyXMLAttributes, synthesizedAugs());
            return;
        }
        else if (fIgnoreOutsideContent) {
            if (elementCode == HTMLElements.HTML) {
                fSeenRootElementEnd = true;
            }
            else if (elementCode == HTMLElements.BODY) {
                fSeenBodyElementEnd = true;
            }
            else if (fSeenBodyElementEnd) {
                notifyDiscardedEndElement(element, augs);
                return;
            }
        }
        else if (elementCode == HTMLElements.FORM) {
            fOpenedForm = false;
        }
        else if (elementCode == HTMLElements.SVG) {
            fOpenedSvg = false;
        }
        else if (elementCode == HTMLElements.HEAD && !isForcedEndElement) {
            // consume </head> first when <body> is reached to retrieve content lost between </head> and <body>
            endElementsBuffer_.add(new ElementEntry(element, augs));
            return;
        }

        // empty element
        final int depth = getElementDepth(elem);

        // no matching tag found
        if (depth == -1) {
            if (elementCode == HTMLElements.P) {
                // A rejected synthesized start tag must not cause another retry.
                if (forceStartElementParentCreated(element, fEmptyXMLAttributes, synthesizedAugs())) {
                    endElement(element, augs);
                }
                else {
                    notifyDiscardedEndElement(element, augs);
                }
                return;
            }

            // Headings: if a heading end tag appears but there is no matching open tag,
            // close the nearest open heading (H1..H6).
            if (isHeading(elementCode)) {
                for (int i = fElementStack.length - 1; i >= fragmentContextStackSize_; i--) {
                    final Info info = fElementStack.data[i];
                    if (isHeading(info.element.code)) {
                        closeTopElements(element, fElementStack.length - i, augs);
                        return;
                    }
                }
            }

            if (!elem.isEmpty()) {
                notifyDiscardedEndElement(element, augs);
            }
            return;
        }

        // find unbalanced formatting elements
        if (depth > 1 && elem.isInline()) {
            final int size = fElementStack.length;
            fFormattingStack.length = 0;
            for (int i = 0; i < depth - 1; i++) {
                final Info info = fElementStack.data[size - i - 1];
                if (info.element.isFormatting()) {
                    // NOTE: I don't have to make a copy of the info because
                    //       it will just be popped off of the element stack
                    //       as soon as we close it, anyway.
                    fFormattingStack.push(info);
                }
            }
        }

        // close children up to appropriate element
        closeTopElements(element, depth, augs);
    }

    /**
     * Checks whether the top of the element stack is inside table structure ({@code tr}, {@code thead},
     * {@code tbody}, {@code tfoot} or {@code table}) <em>without</em> a table cell ({@code td},
     * {@code th}, {@code caption}) in between. A {@code <table>} or {@code <form>} start tag is only
     * valid inside a cell, so in this case the caller closes the surrounding table or the form.
     * <p>
     * Answered by the O(1) open counters when none of the structure elements is open; otherwise the
     * stack is scanned from the top, limited to {@link #MAX_CONTEXT_SEARCH_DEPTH} entries.
     *
     * @return {@code true} if table structure is reached before a table cell
     */
    private boolean isInTableStructureOutsideCell() {
        if (!(fElementStack.isOpen(HTMLElements.TR)
                || fElementStack.isOpen(HTMLElements.THEAD)
                || fElementStack.isOpen(HTMLElements.TBODY)
                || fElementStack.isOpen(HTMLElements.TFOOT)
                || fElementStack.isOpen(HTMLElements.TABLE))) {
            return false;
        }

        final Info[] data = fElementStack.data;
        final int top = fElementStack.length - 1;
        final int stop = Math.max(0, top - MAX_CONTEXT_SEARCH_DEPTH + 1);
        for (int i = top; i >= stop; i--) {
            final short code = data[i].element.code;
            if (code == HTMLElements.TD || code == HTMLElements.TH || code == HTMLElements.CAPTION) {
                return false;
            }
            if (code == HTMLElements.TR
                    || code == HTMLElements.THEAD
                    || code == HTMLElements.TBODY
                    || code == HTMLElements.TFOOT
                    || code == HTMLElements.TABLE) {
                return true;
            }
        }
        return false;
    }

    private static boolean isHeading(final short code) {
        return HTMLElements.H1 <= code && code <= HTMLElements.H6;
    }

    /**
     * Pops and closes the top {@code depth} elements from {@link #fElementStack}, emitting warnings
     * and endElement events exactly like the old inline loop in {@link #endElement(QName, Augmentations)}.
     *
     * @param triggeringElement the end tag that caused this close (used for warnings and for deciding
     *                          which Augmentations are "real" vs synthesized)
     * @param depth how many stack entries to close (must be > 0)
     * @param augs augmentations from the triggering end tag
     */
    private void closeTopElements(final QName triggeringElement, final int depth, final Augmentations augs) {
        for (int i = 0; i < depth; i++) {
            final Info info = fElementStack.pop();

            if (fReportErrors && i < depth - 1) {
                final String ename = triggeringElement.getRawname();
                final String iname = info.qname.getRawname();
                fErrorReporter.reportWarning("HTML2007", new Object[]{ename, iname});
            }

            if (documentHandler_ != null) {
                addBodyIfNeeded(info.element.code);
                // PATCH: Marc-André Morissette
                callEndElement(info.qname, i < depth - 1 ? synthesizedAugs() : augs);
            }
        }
    }

    /**
     * Elements that push a "marker" onto the list of active formatting
     * elements in the HTML5 parsing algorithm.
     * <p>
     * An {@code <a>}/{@code <nobr>} that was opened <em>before</em> one of
     * these elements is not reachable by an {@code <a>}/{@code <nobr>}
     * opened <em>inside</em> it, so {@link #findLastOpen(short)} stops
     * searching when it passes one of them. Example: in
     * {@code <a href="1">1<object><a href="2">2</a></object>} the second
     * anchor stays nested in the first one.
     * <p>
     * Note: {@code OBJECT}, {@code APPLET} and {@code MARQUEE} were verified
     * against browsers. {@code TD}, {@code TH} and {@code CAPTION} follow the
     * specification, but their effect is mixed up with table foster parenting,
     * which this balancer does not implement.
     */
    private static final short[] AFE_MARKER_ELEMENTS = {
        HTMLElements.OBJECT, HTMLElements.APPLET, HTMLElements.MARQUEE,
        HTMLElements.TD, HTMLElements.TH, HTMLElements.CAPTION,
    };

    /**
     * Checks whether the given element code is one of the
     * {@link #AFE_MARKER_ELEMENTS marker elements}.
     *
     * @param code the element code to test (see {@link HTMLElements.Element#code})
     * @return {@code true} if the element acts as a marker for the
     *         {@code <a>}/{@code <nobr>} un-nesting logic
     */
    private static boolean isMarkerElement(final short code) {
        for (final short marker : AFE_MARKER_ELEMENTS) {
            if (marker == code) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the topmost open element with the given code, limited to the
     * {@link #MAX_ADOPTION_DEPTH} topmost stack entries and bounded by
     * {@link #AFE_MARKER_ELEMENTS marker elements}.
     * <p>
     * The search runs from the top of the stack downwards and gives up
     * (returns {@code -1}) when
     * <ul>
     * <li>no element with this code is open anywhere (answered in O(1) by the
     *     open counters, without touching the stack),</li>
     * <li>a marker element is passed before a match is found, or</li>
     * <li>{@code MAX_ADOPTION_DEPTH} entries were examined without a match.</li>
     * </ul>
     * Because of the window, the returned index {@code pos} always satisfies
     * {@code fElementStack.length - pos <= MAX_ADOPTION_DEPTH}.
     *
     * @param elementCode the code of the element to look for
     *        (e.g. {@link HTMLElements#A} or {@link HTMLElements#NOBR})
     * @return the index of the element in {@link #fElementStack}, or
     *         {@code -1} if it is not open, not reachable (marker) or too deep
     */
    private int findLastOpen(final short elementCode) {
        if (!fElementStack.isOpen(elementCode)) {
            return -1;
        }

        final Info[] data = fElementStack.data;
        final int top = fElementStack.length - 1;
        final int stop = Math.max(0, top - MAX_ADOPTION_DEPTH + 1);
        for (int i = top; i >= stop; i--) {
            final short code = data[i].element.code;
            if (code == elementCode) {
                return i;
            }
            if (isMarkerElement(code)) {
                return -1;
            }
        }
        return -1;
    }

    /**
     * Approximates the HTML5 "adoption agency" behavior that browsers apply
     * when a start tag for an element that must not be nested in itself
     * ({@code <a>}, {@code <nobr>}) arrives while one of the same type is
     * still open &ndash; even if other elements are open in between.
     * <p>
     * Example: {@code <a href="1">1<div><a href="2">2</a></div>} is turned
     * into
     * <pre>
     * a[href=1] "1"
     * div
     *   a[href=1]        (empty clone)
     *   a[href=2] "2"
     * </pre>
     * instead of nesting the second anchor (and the div) inside the first one.
     * <p>
     * Procedure, with {@code popped[0]} being the element at {@code pos} and
     * {@code popped[1..n]} the elements above it (bottom to top):
     * <ol>
     * <li>report the {@code HTML2008} warning (same key as for re-opened
     *     formatting elements),</li>
     * <li>close all elements from the top of the stack down to and including
     *     {@code popped[0]},</li>
     * <li>re-open {@code popped[1..n]}, outermost first, as empty elements
     *     carrying their original attributes,</li>
     * <li>only if there were such elements ({@code n > 0}): re-open an empty
     *     clone of {@code popped[0]} as their innermost child and close it
     *     immediately. With direct nesting ({@code <a>1<a>2</a>}) no clone is
     *     created, the new element just becomes a sibling.</li>
     * </ol>
     * The caller then pushes the new start tag as usual, so it ends up inside
     * the innermost re-opened element.
     * <p>
     * This relies on {@link Info#attributes} being retained for every element
     * pushed while an {@code <a>}/{@code <nobr>} is open.
     * <p>
     * Limitations: this is the "single furthest block" case only. The full
     * algorithm (bookmark handling, one clone per intervening level, up to
     * eight outer iterations) is not implemented, e.g. for
     * {@code <a href="1">1<div><p><a href="2">2</a></p></div>} browsers also
     * leave an empty clone directly below the {@code div}, which is not
     * reproduced here.
     *
     * @param pos index in {@link #fElementStack} of the still open
     *        {@code <a>}/{@code <nobr>} to un-nest, as returned by
     *        {@link #findLastOpen(short)} (must be {@code >= 0})
     */
    private void closeAndReopenThroughElement(final int pos) {
        final int count = fElementStack.length - pos;
        final Info[] popped = new Info[count];
        for (int i = 0; i < count; i++) {
            popped[i] = fElementStack.data[pos + i];
        }

        // first special (block level) element above the anchor, -1 if there is none
        int firstSpecial = -1;
        for (int i = 1; i < count; i++) {
            if (popped[i].element.isHtml5Special()) {
                firstSpecial = i;
                break;
            }
        }

        if (fReportErrors) {
            fErrorReporter.reportWarning("HTML2008", new Object[]{popped[0].qname.getRawname()});
        }

        // close everything from the top of the stack down through the element
        for (int i = count - 1; i >= 0; i--) {
            final Info info = fElementStack.pop();
            if (documentHandler_ != null) {
                callEndElement(info.qname, synthesizedAugs());
            }
        }

        // between the anchor and the first special element only formatting
        // elements are reconstructed, plain ones (span, label, ...) stay closed
        final int plainEnd = firstSpecial == -1 ? count : firstSpecial;
        for (int i = 1; i < plainEnd; i++) {
            if (popped[i].element.isFormatting()) {
                forceStartElement(popped[i].qname, popped[i].attributes, synthesizedAugs());
            }
        }

        if (firstSpecial != -1) {
            // the first special element and everything above it, outermost first
            for (int i = firstSpecial; i < count; i++) {
                forceStartElement(popped[i].qname, popped[i].attributes, synthesizedAugs());
            }

            // empty clone of the anchor as innermost child, closed right away:
            // this is the empty "<a href=...></a>" browsers leave behind
            forceStartElement(popped[0].qname, popped[0].attributes, synthesizedAugs());
            endElement(popped[0].qname, synthesizedAugs());
        }
    }

    // re-open inline elements
    protected boolean reopenFormattingElements(final HTMLElements.Element element) {
        if (fFormattingStack.length == 0) {
            return false;
        }

        Info info = fFormattingStack.pop();
        XMLAttributes attributes = info.attributes;
        if (fReportErrors) {
            final String iname = info.qname.getRawname();
            fErrorReporter.reportWarning("HTML2008", new Object[]{iname});
        }

        if (element != null && info.element.code == element.code) {
            return true;
        }

        forceStartElement(info.qname, attributes, synthesizedAugs());

        while(fFormattingStack.length > 0) {
            info = fFormattingStack.pop();
            attributes = info.attributes;
            if (fReportErrors) {
                final String iname = info.qname.getRawname();
                fErrorReporter.reportWarning("HTML2008", new Object[]{iname});
            }

            forceStartElement(info.qname, attributes, synthesizedAugs());
        }
        return false;
    }

    // Returns an HTML element.
    protected HTMLElements.Element getElement(final QName elementName) {
        String name = elementName.getRawname();
        if (fNamespaces && NamespaceBinder.XHTML_1_0_URI.equals(elementName.getUri())) {
            final int index = name.indexOf(':');
            if (index != -1) {
                name = name.substring(index + 1);
            }
        }
        return htmlConfiguration_.getHtmlElements().getElement(name);
    }

    // Call document handler start element.
    protected final void callStartElement(final QName element, final XMLAttributes attrs, final Augmentations augs)
        throws XNIException {
        documentHandler_.startElement(element, attrs, augs);
    }

    private void addBodyIfNeeded(final short element) {
        if (!fDocumentFragment && !fSeenFramesetElement && element == HTMLElements.HTML) {
            if (!fSeenHeadElement) {
                final QName head = createQName(fNamesElems == NAMES_UPPERCASE ? "HEAD" : "head");
                callStartElement(head, fEmptyXMLAttributes, synthesizedAugs());
                callEndElement(head, synthesizedAugs());
            }
            if (!fSeenBodyElement) {
                final QName body = createQName(fNamesElems == NAMES_UPPERCASE ? "BODY" : "body");
                callStartElement(body, fEmptyXMLAttributes, synthesizedAugs());
                callEndElement(body, synthesizedAugs());
            }
        }
    }

    // Call document handler end element.
    protected final void callEndElement(final QName element, final Augmentations augs)
        throws XNIException {
        documentHandler_.endElement(element, augs);
    }

    /**
     * @return the depth of the open tag associated with the specified
     *      element name or -1 if no matching element is found.
     *
     * @param element The element.
     */
    protected final int getElementDepth(final HTMLElements.Element element) {
        final short elementCode = element.code;

        // fast path: this element type isn't open anywhere on the stack
        if (!fElementStack.isOpen(elementCode)) {
            return -1;
        }

        final boolean container = element.isContainer();
        final boolean tableBodyOrHtml = (elementCode == HTMLElements.TABLE)
            || (elementCode == HTMLElements.BODY) || (elementCode == HTMLElements.HTML);
        // html and body are unique and sit at the bottom of the stack, they have to stay reachable;
        // every other search is limited to MAX_CONTEXT_SEARCH_DEPTH entries (DoS protection)
        int lowest = fragmentContextStackSize_;
        if (elementCode != HTMLElements.HTML && elementCode != HTMLElements.BODY) {
            lowest = Math.max(lowest, fElementStack.length - MAX_CONTEXT_SEARCH_DEPTH);
        }

        int depth = -1;
        for (int i = fElementStack.length - 1; i >= lowest; i--) {
            final Info info = fElementStack.data[i];
            if (info.element.code == elementCode
                    && (elementCode != HTMLElements.UNKNOWN
                            || element.name.equals(info.element.name))) {
                depth = fElementStack.length - i;
                break;
            }
            if (!container && info.element.isBlock()) {
                break;
            }
            if (info.element.code == HTMLElements.TABLE && !tableBodyOrHtml) {
                return -1; // current element not allowed to close a table
            }
            if (element.isParent(info.element)) {
                break;
            }
        }
        return depth;
    }

    /**
     * Checks whether at least one of the parents required by the given
     * element is currently open, honoring the element's bounds.
     * <p>
     * The check is done in tiers, cheapest first:
     * <ol>
     * <li><b>No bounds</b> ({@code element.bounds == -1}, which is the case
     *     for all elements except the table row/cell family): answered by
     *     the O(1) open counters, the stack is not touched.</li>
     * <li><b>Bounds element not open</b>: it cannot cut the search off, so
     *     this is the same unbounded question as in tier 1.</li>
     * <li><b>Bounds open but no parent open</b>: the answer is {@code false},
     *     again without scanning.</li>
     * <li><b>Bounds and a parent open</b>: the order matters, so the stack is
     *     scanned from the top. {@code true} is returned if a parent is met
     *     before the bounds element, {@code false} if the bounds element is
     *     met first. At most {@link #MAX_PARENT_SEARCH_DEPTH} entries are
     *     examined; if neither is found in that window, {@code false} is
     *     returned.</li>
     * </ol>
     * Returning {@code false} makes the caller synthesize the preferred
     * parent (or discard the element if that parent cannot be created), so
     * hitting the depth limit degrades to bounded, well defined behavior
     * instead of an unbounded scan per start tag.
     * <p>
     * Example: for {@code <td>} (parent {@code TR}, bounds {@code TABLE}) in
     * {@code <table><tr><div>...<td>} the {@code TR} is found as long as it
     * is within the search window; in {@code <tr><table><td>} the inner
     * {@code TABLE} is hit first and no parent is found.
     *
     * @param element the element whose parents should be looked up
     * @return {@code true} if a required parent is open (and reachable
     *         within the bounds and the search window), {@code false} if the
     *         element has no required parents or none was found
     */
    protected boolean hasOpenParent(final HTMLElements.Element element) {
        final HTMLElements.Element[] parents = element.parent;
        if (parents == null || parents.length == 0) {
            return false;
        }

        final short bounds = element.bounds;

        // 1. hot path: no bounds
        if (bounds == -1) {
            return isAnyOpen(parents);
        }

        // 2. bounds element not on the stack -> cannot cut the search off
        if (!fElementStack.isOpen(bounds)) {
            return isAnyOpen(parents);
        }

        // 3. bounds open: only worth scanning if a parent is open at all
        if (!isAnyOpen(parents)) {
            return false;
        }

        // 4. ordered, depth-limited scan
        final Info[] data = fElementStack.data;
        final int top = fElementStack.length - 1;
        final int stop = Math.max(0, top - MAX_PARENT_SEARCH_DEPTH + 1);
        for (int i = top; i >= stop; i--) {
            final short code = data[i].element.code;
            if (code == bounds) {
                return false;
            }
            for (int j = 0; j < parents.length; j++) {
                if (code == parents[j].code) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks, using only the O(1) open counters, whether at least one of the
     * given elements is currently open anywhere on the element stack.
     * The position on the stack is not considered.
     *
     * @param elements the candidate elements (must not be {@code null})
     * @return {@code true} if any of them is open
     */
    private boolean isAnyOpen(final HTMLElements.Element[] elements) {
        for (int i = 0; i < elements.length; i++) {
            if (fElementStack.isOpen(elements[i].code)) {
                return true;
            }
        }
        return false;
    }

    // Returns an augmentations object with a synthesized item added.
    protected final Augmentations synthesizedAugs() {
        if (fAugmentations) {
            return SynthesizedItem.INSTANCE;
        }
        return null;
    }

     // Converts HTML names string value to constant value.
     //
     // @see #NAMES_NO_CHANGE
     // @see #NAMES_LOWERCASE
     // @see #NAMES_UPPERCASE
    protected static short getNamesValue(final String value) {
        if ("lower".equals(value)) {
            return NAMES_LOWERCASE;
        }
        if ("upper".equals(value)) {
            return NAMES_UPPERCASE;
        }
        return NAMES_NO_CHANGE;
    }

    /**
     * Element info for each start element. This information is used when
     * closing unbalanced inline elements. For example:
     * <pre>
     * &lt;i&gt;unbalanced &lt;b&gt;HTML&lt;/i&gt; content&lt;/b&gt;
     * </pre>
     * <p>
     * It seems that it is a waste of processing and memory to copy the
     * attributes for every start element even if there are no unbalanced
     * inline elements in the document. However, if the attributes are
     * <em>not</em> saved, then important attributes such as style
     * information would be lost.
     *
     * @author Andy Clark
     */
    public static class Info {

        /** The element. */
        public final HTMLElements.Element element;

        /** The element qualified name. */
        public final QName qname;

        /** The element attributes. */
        public XMLAttributes attributes;

        /**
         * Creates an element information object.
         * <p>
         * <strong>Note:</strong>
         * This constructor makes a copy of the element information.
         *
         * @param element The HTML element definition.
         * @param qname   The element's qualified name (will be deep-copied).
         */
        public Info(final HTMLElements.Element element, final QName qname) {
            this(element, qname, null);
        }

        /**
         * Creates an element information object.
         * <p>
         * <strong>Note:</strong>
         * This constructor makes a deep copy of the qualified name and,
         * for inline elements, of the attributes so that formatting
         * elements can be re-opened with their original attributes.
         *
         * @param element    The HTML element definition.
         * @param qname      The element's qualified name (will be deep-copied).
         * @param attributes The element attributes to copy, or {@code null} if
         *                   no attribute snapshot is needed.
         */
        public Info(final HTMLElements.Element element, final QName qname,
                final XMLAttributes attributes) {
            this.element = element;
            this.qname = new QName(qname);

            if (attributes != null) {
                final int length = attributes.getLength();
                if (length > 0) {
                    if (attributes instanceof XMLAttributesImpl impl) {
                        this.attributes = new XMLAttributesImpl(impl);
                        return;
                    }

                    final QName aqname = new QName();
                    final XMLAttributesImpl newattrs = new XMLAttributesImpl();
                    for (int i = 0; i < length; i++) {
                        attributes.getName(i, aqname);
                        newattrs.addAttribute(aqname, attributes.getType(i),
                                                attributes.getValue(i), attributes.isSpecified(i));
                    }
                    this.attributes = newattrs;
                }
            }
        }

        /**
         * Simple representation to make debugging easier.
         */
        @Override
        public String toString() {
            return super.toString() + qname;
        }
    }

    /** Unsynchronized stack of element information. */
    public static class InfoStack {

        /** The length of the stack. */
        public int length;

        /** The stack data. */
        public Info[] data;

        public InfoStack(final int initialSize) {
            data = new Info[initialSize];
        }

        // Pushes element information onto the stack.
        public void push(final Info info) {
            if (length == data.length) {
                final Info[] newarray = new Info[grownCapacity(length)];
                System.arraycopy(data, 0, newarray, 0, length);
                data = newarray;
            }
            data[length++] = info;
        }

        // Pops the top item off of the stack.
        public Info pop() {
            final Info info = data[--length];
            data[length] = null;
            return info;
        }

        // Simple representation to make debugging easier
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("InfoStack(");
            for (int i = length - 1; i >= 0; --i) {
                sb.append(data[i]);
                if (i != 0) {
                    sb.append(", ");
                }
            }
            sb.append(")");
            return sb.toString();
        }
    }

    /**
     * Calculates the capacity of a full element stack after growing.
     * <p>
     * A small fixed increment would copy the whole array on every resize, which
     * makes very deep documents quadratic. Pure doubling avoids that but can waste
     * up to half of a very large array. This uses doubling for small and medium
     * stacks and switches to large fixed steps once the stack is huge: the copy cost
     * stays negligible (a stack of 10 million entries needs about 40 resizes) while
     * the unused reserve never exceeds {@link #MAX_STACK_GROWTH} entries.
     *
     * @param current the current (full) capacity
     * @return the new capacity, always greater than {@code current}
     * @throws OutOfMemoryError if the capacity would overflow an int
     */
    private static int grownCapacity(final int current) {
        final int increment = Math.max(10, Math.min(current, MAX_STACK_GROWTH));
        final int grown = current + increment;
        if (grown < 0) {
            throw new OutOfMemoryError("Required element stack size too large");
        }
        return grown;
    }

    /** Unsynchronized stack of element information. */
    public static class InfoStackWithOpenCounters {

        /** The length of the stack. */
        public int length;

        /** The stack data. */
        public Info[] data;

        /** Count of currently-open elements per element code; index = HTMLElements code. */
        private final int[] openCounters;

        /** The capacity the stack starts with and is shrunk back to. */
        private final int initialSize;

        public InfoStackWithOpenCounters(final int initialSize) {
            this.initialSize = initialSize;
            data = new Info[initialSize];

            // hopefully noone will ever use more than 200 elements
            openCounters = new int[200];
        }

        // Pushes element information onto the stack.
        public void push(final Info info) {
            if (length == data.length) {
                final Info[] newarray = new Info[grownCapacity(length)];
                System.arraycopy(data, 0, newarray, 0, length);
                data = newarray;
            }
            data[length++] = info;
            openCounters[info.element.code]++;
        }

        // Peeks at the top of the stack.
        public Info peek() {
            return data[length - 1];
        }

        // Pops the top item off of the stack.
        public Info pop() {
            final Info info = data[--length];
            data[length] = null;
            openCounters[info.element.code]--;
            return info;
        }

        /**
         * Returns true if at least one element with this code is currently open.
         *
         * @return true if at least one element with this code is currently open.
         */
        public boolean isOpen(final short elementCode) {
            return openCounters[elementCode] > 0;
        }

        // Resets the stack and releases all Info references so they can be GC'd.
        // An array that grew huge for one deep document is not kept for the parser's lifetime.
        public void clear() {
            if (data.length > MAX_RETAINED_STACK_CAPACITY) {
                data = new Info[initialSize];
            }
            else {
                Arrays.fill(data, 0, length, null);
            }
            Arrays.fill(openCounters, 0);
            length = 0;
        }

        // Simple representation to make debugging easier
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("InfoStackWithOpenCounters(");
            for (int i = length - 1; i >= 0; --i) {
                sb.append(data[i]);
                if (i != 0) {
                    sb.append(", ");
                }
            }
            sb.append(")");
            return sb.toString();
        }
    }

    void setTagBalancingListener(final HTMLTagBalancingListener tagBalancingListener) {
        this.tagBalancingListener = tagBalancingListener;
    }

    /**
     * Notifies the tagBalancingListener (if any) of an ignored start element
     */
    private void notifyDiscardedStartElement(final QName elem, final XMLAttributes attrs, final Augmentations augs) {
        if (tagBalancingListener != null) {
            tagBalancingListener.ignoredStartElement(elem, attrs, augs);
        }
        final short code = getElement(elem).code;
        if (code == HTMLElements.BODY || code == HTMLElements.HTML) {
            discardedStartElements.merge(elem.getRawname(), 1, Integer::sum);
        }
    }

    /**
     * Notifies the tagBalancingListener (if any) of an ignored end element
     */
    private void notifyDiscardedEndElement(final QName element, final Augmentations augs) {
        if (tagBalancingListener != null) {
            tagBalancingListener.ignoredEndElement(element, augs);
        }
    }

    /**
     * Structure to hold information about an element placed in buffer to be consumed later
     */
    static class ElementEntry {
        final QName name_;
        final Augmentations augs_;

        ElementEntry(final QName element, final Augmentations augs) {
            name_ = new QName(element);
            augs_ = augs == null ? null : augs.clone();
        }
    }
}