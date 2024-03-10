/*
 * Copyright (c) 2002-2009 Andy Clark, Marc Guillemot
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
package org.htmlunit.cyberneko;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.htmlunit.cyberneko.HTMLElements.Element;
import org.htmlunit.cyberneko.filters.NamespaceBinder;
import org.htmlunit.cyberneko.xerces.util.XMLAttributesImpl;
import org.htmlunit.cyberneko.xerces.xni.Augmentations;
import org.htmlunit.cyberneko.xerces.xni.NamespaceContext;
import org.htmlunit.cyberneko.xerces.xni.QName;
import org.htmlunit.cyberneko.xerces.xni.XMLAttributes;
import org.htmlunit.cyberneko.xerces.xni.XMLDocumentHandler;
import org.htmlunit.cyberneko.xerces.xni.XMLLocator;
import org.htmlunit.cyberneko.xerces.xni.XMLString;
import org.htmlunit.cyberneko.xerces.xni.XNIException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLComponentManager;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLConfigurationException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLDocumentFilter;

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
    protected static final String DOCUMENT_FRAGMENT = "http://cyberneko.org/html/features/balance-tags/document-fragment";

    /** Ignore outside content. */
    protected static final String IGNORE_OUTSIDE_CONTENT = "http://cyberneko.org/html/features/balance-tags/ignore-outside-content";

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
    public static final String FRAGMENT_CONTEXT_STACK = "http://cyberneko.org/html/properties/balance-tags/fragment-context-stack";

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

    /** Don't modify HTML names. */
    private static final short NAMES_NO_CHANGE = 0;

    /** Uppercase HTML names. */
    private static final short NAMES_UPPERCASE = 1;

    /** Lowercase HTML names. */
    private static final short NAMES_LOWERCASE = 2;

    // static vars

    /** Synthesized event info item. */
    private static final HTMLEventInfo SYNTHESIZED_ITEM = new HTMLEventInfo.SynthesizedItem();

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

    /** Allows self closing tags. */
    protected boolean fAllowSelfclosingTags;

    // properties

    /** Modify HTML element names. */
    protected short fNamesElems;

    /** Error reporter. */
    protected HTMLErrorReporter fErrorReporter;

    // connections

    /** The document handler. */
    protected XMLDocumentHandler fDocumentHandler;

    // state

    /** The element stack. */
    protected final InfoStack fElementStack = new InfoStack();

    /** The inline stack. */
    protected final InfoStack fInlineStack = new InfoStack();

    /** True if seen anything. Important for xml declaration. */
    protected boolean fSeenAnything;

    /** True if root element has been seen. */
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

    /** True if seen {@code head} element. */
    protected boolean fSeenHeadElement;

    /** True if seen {@code body} element. */
    protected boolean fSeenBodyElement;
    private boolean fSeenBodyElementEnd;

    /** True if seen {@code frameset} element. */
    private boolean fSeenFramesetElement;

    /** True if seen non whitespace characters. */
    private boolean fSeenCharacters;

    /** True if a form is in the stack (allow to discard opening of nested forms) */
    protected boolean fOpenedForm;

    /** True if a svg is in the stack (no parent checking takes place) */
    protected boolean fOpenedSvg;

    /** True if a select is in the stack */
    protected boolean fOpenedSelect;

    // temp vars

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

    private final List<ElementEntry> endElementsBuffer_ = new ArrayList<>();
    private final List<String> discardedStartElements = new ArrayList<>();

    private final HTMLConfiguration htmlConfiguration_;

    HTMLTagBalancer(final HTMLConfiguration htmlConfiguration) {
        htmlConfiguration_ = htmlConfiguration;
    }

    /** Returns the default state for a feature. */
    @Override
    public Boolean getFeatureDefault(final String featureId) {
        final int length = RECOGNIZED_FEATURES != null ? RECOGNIZED_FEATURES.length : 0;
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
        final int length = RECOGNIZED_PROPERTIES != null ? RECOGNIZED_PROPERTIES.length : 0;
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
    public void reset(final XMLComponentManager manager)
        throws XMLConfigurationException {

        // get features
        fNamespaces = manager.getFeature(NAMESPACES);
        fAugmentations = manager.getFeature(AUGMENTATIONS);
        fReportErrors = manager.getFeature(REPORT_ERRORS);
        fDocumentFragment = manager.getFeature(DOCUMENT_FRAGMENT);
        fIgnoreOutsideContent = manager.getFeature(IGNORE_OUTSIDE_CONTENT);
        fAllowSelfclosingIframe = manager.getFeature(HTMLScanner.ALLOW_SELFCLOSING_IFRAME);
        fAllowSelfclosingTags = manager.getFeature(HTMLScanner.ALLOW_SELFCLOSING_TAGS);

        // get properties
        fNamesElems = getNamesValue(String.valueOf(manager.getProperty(NAMES_ELEMS)));
        fErrorReporter = (HTMLErrorReporter) manager.getProperty(ERROR_REPORTER);

        fragmentContextStack_ = (QName[]) manager.getProperty(FRAGMENT_CONTEXT_STACK);
        fSeenAnything = false;
        fSeenDoctype = false;
        fSeenRootElement = false;
        fSeenRootElementEnd = false;
        fSeenHeadElement = false;
        fSeenBodyElement = false;
        fSeenBodyElementEnd = false;
        fSeenFramesetElement = false;
        fSeenCharacters = false;
        fTemplateFragment = false;

        fOpenedForm = false;
        fOpenedSvg = false;
        fOpenedSelect = false;

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

        if (featureId.equals(AUGMENTATIONS)) {
            fAugmentations = state;
            return;
        }
        if (featureId.equals(REPORT_ERRORS)) {
            fReportErrors = state;
            return;
        }
        if (featureId.equals(IGNORE_OUTSIDE_CONTENT)) {
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
        fDocumentHandler = handler;
    }

    /** Returns the document handler. */
    @Override
    public XMLDocumentHandler getDocumentHandler() {
        return fDocumentHandler;
    }

    /** Start document. */
    @Override
    public void startDocument(final XMLLocator locator, final String encoding, final NamespaceContext nscontext, final Augmentations augs)
        throws XNIException {

        // reset state
        fElementStack.top = 0;
        if (fragmentContextStack_ != null) {
            fragmentContextStackSize_ = fragmentContextStack_.length;
            for (final QName name : fragmentContextStack_) {
                final Element elt = htmlConfiguration_.getHtmlElements().getElement(name.getLocalpart());
                fElementStack.push(new Info(elt, name));
            }

        }
        else {
            fragmentContextStackSize_ = 0;
        }

        // pass on event
        if (fDocumentHandler != null) {
            fDocumentHandler.startDocument(locator, encoding, nscontext, augs);
        }

    }

    // old methods

    /** XML declaration. */
    @Override
    public void xmlDecl(final String version, final String encoding, final String standalone, final Augmentations augs) throws XNIException {
        if (!fSeenAnything && fDocumentHandler != null) {
            fDocumentHandler.xmlDecl(version, encoding, standalone, augs);
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
            if (fDocumentHandler != null) {
                fDocumentHandler.doctypeDecl(rootElementName, publicId, systemId, augs);
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
            if (fDocumentHandler != null) {
                fSeenRootElementEnd = false;
                forceStartBody(); // will force <html> and <head></head>
                final String body = modifyName("body", fNamesElems);
                fQName.setValues(null, body, body, null);
                callEndElement(fQName, synthesizedAugs());

                final String ename = modifyName("html", fNamesElems);
                fQName.setValues(null, ename, ename, null);
                callEndElement(fQName, synthesizedAugs());
            }
        }

        // pop all remaining elements
        else {
            final int length = fElementStack.top - fragmentContextStackSize_;
            for (int i = 0; i < length; i++) {
                final Info info = fElementStack.pop();
                if (fReportErrors) {
                    final String ename = info.qname.getRawname();
                    fErrorReporter.reportWarning("HTML2001", new Object[]{ename});
                }
                if (fDocumentHandler != null) {
                    addBodyIfNeeded(info.element.code);
                    callEndElement(info.qname, synthesizedAugs());
                }
            }
        }

        // call handler
        if (fDocumentHandler != null) {
            fDocumentHandler.endDocument(augs);
        }

    }

    /**
     * Consume elements that have been buffered, like </body></html> that are first consumed
     * at the end of document
     */
    private void consumeBufferedEndElements() {
        if (endElementsBuffer_.isEmpty()) {
            return;
        }

        final List<ElementEntry> toConsume = new ArrayList<>(endElementsBuffer_);
        endElementsBuffer_.clear();
        for (final ElementEntry entry : toConsume) {
            forcedEndElement_ = true;
            endElement(entry.name_, entry.augs_);
        }
        endElementsBuffer_.clear();
    }

    /** Comment. */
    @Override
    public void comment(final XMLString text, final Augmentations augs) throws XNIException {
        fSeenAnything = true;
        consumeEarlyTextIfNeeded();
        if (fDocumentHandler != null) {
            fDocumentHandler.comment(text, augs);
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
    public void processingInstruction(final String target, final XMLString data, final Augmentations augs) throws XNIException {
        fSeenAnything = true;
        consumeEarlyTextIfNeeded();
        if (fDocumentHandler != null) {
            fDocumentHandler.processingInstruction(target, data, augs);
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
        if (fSeenFramesetElement && elementCode != HTMLElements.FRAME && elementCode != HTMLElements.FRAMESET && elementCode != HTMLElements.NOFRAMES) {
            notifyDiscardedStartElement(elem, attrs, augs);
            return;
        }

        if (!fTemplateFragment && fOpenedSelect) {
            if (elementCode == HTMLElements.SELECT) {
                final QName head = createQName("SELECT");
                endElement(head, synthesizedAugs());

                notifyDiscardedStartElement(elem, attrs, augs);
                return;
            }
            else if (elementCode != HTMLElements.OPTION
                        && elementCode != HTMLElements.OPTGROUP
                        && elementCode != HTMLElements.SCRIPT
                        && elementCode != HTMLElements.HR) {
                notifyDiscardedStartElement(elem, attrs, augs);
                return;
            }
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
                final QName head = createQName("head");
                forceStartElement(head, new XMLAttributesImpl(), synthesizedAugs());
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
                final QName head = createQName("head");
                forceStartElement(head, new XMLAttributesImpl(), synthesizedAugs());
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
            for (int i = fElementStack.top - 1; i >= 0; i--) {
                final Info info = fElementStack.data[i];
                if (info.element.code == HTMLElements.TD
                        || info.element.code == HTMLElements.TH
                        || info.element.code == HTMLElements.CAPTION) {
                    break;
                }
                if (info.element.code == HTMLElements.TR
                        || info.element.code == HTMLElements.THEAD
                        || info.element.code == HTMLElements.TBODY
                        || info.element.code == HTMLElements.TFOOT
                        || info.element.code == HTMLElements.TABLE) {
                    if (fDocumentHandler != null) {
                        callStartElement(elem, attrs, augs);
                        callEndElement(createQName("form"), synthesizedAugs());
                    }
                    fOpenedForm = false;
                    return;
                }
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
            for (int i = fElementStack.top - 1; i >= 0; i--) {
                final Info info = fElementStack.data[i];
                if (info.element.code == HTMLElements.TD
                        || info.element.code == HTMLElements.TH
                        || info.element.code == HTMLElements.CAPTION) {
                    break;
                }
                if (info.element.code == HTMLElements.TR
                        || info.element.code == HTMLElements.THEAD
                        || info.element.code == HTMLElements.TBODY
                        || info.element.code == HTMLElements.TFOOT
                        || info.element.code == HTMLElements.TABLE) {
                    final QName table = createQName("table");
                    endElement(table, synthesizedAugs());
                    break;
                }
            }
        }

        // check proper parent
        if (element.parent != null && !fOpenedSvg) {
            final HTMLElements.Element preferedParent = element.parent[0];
            if (fDocumentFragment && (preferedParent.code == HTMLElements.HEAD || preferedParent.code == HTMLElements.BODY)) {
                // nothing, don't force HEAD or BODY creation for a document fragment
            }
            else if (fTemplateFragment
                        && fElementStack.top > 0
                        && fElementStack.data[fElementStack.top - 1].element.code == HTMLElements.TEMPLATE) {
                // nothing, don't force/check parent for the direct template children
            }
            else if (!fSeenRootElement && !fDocumentFragment) {
                String pname = preferedParent.name;
                pname = modifyName(pname, fNamesElems);
                if (fReportErrors) {
                    final String ename = elem.getRawname();
                    fErrorReporter.reportWarning("HTML2002", new Object[]{ename, pname});
                }
                final QName qname = createQName(pname);
                final boolean parentCreated = forceStartElement(qname, new XMLAttributesImpl(), synthesizedAugs());
                if (!parentCreated) {
                    if (!isForcedCreation) {
                        notifyDiscardedStartElement(elem, attrs, augs);
                    }
                    return;
                }
            }
            else {
                if (preferedParent.code != HTMLElements.HEAD || (!fSeenBodyElement && !fDocumentFragment)) {
                    final int depth = getParentDepth(element.parent, element.bounds);
                    if (depth == -1) { // no parent found
                        final String pname = modifyName(preferedParent.name, fNamesElems);
                        final QName qname = createQName(pname);
                        if (fReportErrors) {
                            final String ename = elem.getRawname();
                            fErrorReporter.reportWarning("HTML2004", new Object[]{ename, pname});
                        }
                        final boolean parentCreated = forceStartElement(qname, new XMLAttributesImpl(), synthesizedAugs());
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

        // if block element, save immediate parent inline elements
        int depth = 0;
        if (element.flags == 0) {
            final int length = fElementStack.top;
            fInlineStack.top = 0;
            for (int i = length - 1; i >= 0; i--) {
                final Info info = fElementStack.data[i];
                if (!info.element.isInline()) {
                    break;
                }
                fInlineStack.push(info);
                endElement(info.qname, synthesizedAugs());
            }
            depth = fInlineStack.top;
        }

        // close previous elements
        // all elements close a <script>
        // in head, no element has children
        if ((fElementStack.top > 1
                && (fElementStack.peek().element.code == HTMLElements.SCRIPT))
                || fElementStack.top > 2 && fElementStack.data[fElementStack.top - 2].element.code == HTMLElements.HEAD) {
            final Info info = fElementStack.pop();
            if (fDocumentHandler != null) {
                callEndElement(info.qname, synthesizedAugs());
            }
        }

        if (element.closes != null) {
            int length = fElementStack.top;
            for (int i = length - 1; i >= 0; i--) {
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
                        if (fDocumentHandler != null) {
                            // PATCH: Marc-Andr� Morissette
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
        if (element.isEmpty()) {
            if (attrs == null) {
                attrs = new XMLAttributesImpl();
            }
            if (fDocumentHandler != null) {
                fDocumentHandler.emptyElement(elem, attrs, augs);
            }
        }
        else {
            final boolean inline = element.isInline();
            fElementStack.push(new Info(element, elem, inline ? attrs : null));
            if (attrs == null) {
                attrs = new XMLAttributesImpl();
            }
            if (fDocumentHandler != null) {
                callStartElement(elem, attrs, augs);
            }
        }

        // re-open inline elements
        for (int i = 0; i < depth; i++) {
            final Info info = fInlineStack.pop();
            forceStartElement(info.qname, info.attributes, synthesizedAugs());
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
    private boolean forceStartElement(final QName elem, final XMLAttributes attrs, final Augmentations augs) throws XNIException {

        forcedStartElement_ = true;
        startElement(elem, attrs, augs);

        return fElementStack.top > 0 && elem.equals(fElementStack.peek().qname);
    }

    private QName createQName(String tagName) {
        tagName = modifyName(tagName, fNamesElems);
        return new QName(null, tagName, tagName, NamespaceBinder.XHTML_1_0_URI);
    }

    /** Empty element. */
    @Override
    public void emptyElement(final QName element, final XMLAttributes attrs, final Augmentations augs) throws XNIException {
        startElement(element, attrs, augs);
        // browser ignore the closing indication for non empty tags like <form .../> but not for unknown element
        final HTMLElements.Element elem = getElement(element);
        if (elem.isEmpty()
                || fAllowSelfclosingTags
                || elem.code == HTMLElements.UNKNOWN
                || (elem.code == HTMLElements.IFRAME && fAllowSelfclosingIframe)) {
            endElement(element, augs);
        }
    }

    /**
     * Generates a missing <body> (which creates missing <head> when needed)
     */
    private void forceStartBody() {
        final QName body = createQName("body");
        if (fReportErrors) {
            fErrorReporter.reportWarning("HTML2006", new Object[]{body.getLocalpart()});
        }
        forceStartElement(body, new XMLAttributesImpl(), synthesizedAugs());
    }

    /** Start CDATA section. */
    @Override
    public void startCDATA(final Augmentations augs) throws XNIException {
        fSeenAnything = true;

        consumeEarlyTextIfNeeded();

        // check for end of document
        if (fSeenRootElementEnd) {
            return;
        }

        // call handler
        if (fDocumentHandler != null) {
            fDocumentHandler.startCDATA(augs);
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
        if (fDocumentHandler != null) {
            fDocumentHandler.endCDATA(augs);
        }

    }

    /** Characters. */
    @Override
    public void characters(final XMLString text, final Augmentations augs) throws XNIException {
        // check for end of document
        if (fSeenRootElementEnd || fSeenBodyElementEnd) {
            return;
        }

        if (fElementStack.top == 0 && !fDocumentFragment) {
            // character before first opening tag
            lostText_.add(text, augs);
            return;
        }

        // is this text whitespace?
        final boolean whitespace = text.isWhitespace();
        if (!fDocumentFragment) {
            // handle bare characters
            if (!fSeenRootElement) {
                forceStartBody();
            }

            if (whitespace && (fElementStack.top < 2 || endElementsBuffer_.size() == 1)) {
                // ignore spaces directly within <html>
                return;
            }

            // handle character content in head
            // NOTE: This frequently happens when the document looks like:
            //       <title>Title</title>
            //       And here's some text.
            else if (!whitespace) {
                final Info info = fElementStack.peek();
                if (info.element.code == HTMLElements.HEAD || info.element.code == HTMLElements.HTML) {
                    if (fReportErrors) {
                        final String hname = modifyName("head", fNamesElems);
                        final String bname = modifyName("body", fNamesElems);
                        fErrorReporter.reportWarning("HTML2009", new Object[]{hname, bname});
                    }
                    forceStartBody();
                }
            }
        }

        fSeenCharacters = fSeenCharacters || !whitespace;

        // call handler
        if (fDocumentHandler != null) {
            fDocumentHandler.characters(text, augs);
        }

    }

    /** End element. */
    @Override
    public void endElement(final QName element, final Augmentations augs) throws XNIException {
        final boolean forcedEndElement = forcedEndElement_;
        // is there anything to do?
        if (fSeenRootElementEnd) {
            notifyDiscardedEndElement(element, augs);
            return;
        }

        // get element information
        final HTMLElements.Element elem = getElement(element);
        final short elementCode = elem.code;

        if (!fTemplateFragment && fOpenedSelect) {
            if (elementCode == HTMLElements.SELECT) {
                fOpenedSelect = false;
            }
            else if (elementCode != HTMLElements.OPTION
                        && elementCode != HTMLElements.OPTGROUP
                        && elementCode != HTMLElements.SCRIPT) {
                notifyDiscardedEndElement(element, augs);
                return;
            }
        }

        if (elementCode == HTMLElements.TEMPLATE) {
            fTemplateFragment = false;
        }

        // if we consider outside content, just buffer </body> and </html> to consider them at the very end
        if (!fIgnoreOutsideContent
                && (elementCode == HTMLElements.BODY || elementCode == HTMLElements.HTML)) {
            for (final Iterator<String> it = discardedStartElements.iterator(); it.hasNext();) {
                if (element.getRawname().equals(it.next())) {
                    it.remove();
                    return;
                }
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
        if (elementCode == HTMLElements.HTML) {
            fSeenRootElementEnd = true;
        }
        else if (fIgnoreOutsideContent) {
            if (elementCode == HTMLElements.BODY) {
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
        else if (elementCode == HTMLElements.HEAD && !forcedEndElement) {
            // consume </head> first when <body> is reached to retrieve content lost between </head> and <body>
            endElementsBuffer_.add(new ElementEntry(element, augs));
            return;
        }

        // empty element
        final int depth = getElementDepth(elem);
        if (depth == -1) {
            if (elementCode == HTMLElements.P) {
                forceStartElement(element, new XMLAttributesImpl(), synthesizedAugs());
                endElement(element, augs);
            }
            else if (elementCode == HTMLElements.BR) {
                forceStartElement(element, new XMLAttributesImpl(), synthesizedAugs());
            }
            else if (!elem.isEmpty()) {
                notifyDiscardedEndElement(element, augs);
            }
            return;
        }

        // find unbalanced inline elements
        if (depth > 1 && elem.isInline()) {
            final int size = fElementStack.top;
            fInlineStack.top = 0;
            for (int i = 0; i < depth - 1; i++) {
                final Info info = fElementStack.data[size - i - 1];
                final HTMLElements.Element pelem = info.element;
                if (pelem.isInline() || pelem.code == HTMLElements.FONT) { // TODO: investigate if only FONT
                    // NOTE: I don't have to make a copy of the info because
                    //       it will just be popped off of the element stack
                    //       as soon as we close it, anyway.
                    fInlineStack.push(info);
                }
            }
        }

        // close children up to appropriate element
        for (int i = 0; i < depth; i++) {
            final Info info = fElementStack.pop();
            if (fReportErrors && i < depth - 1) {
                final String ename = modifyName(element.getRawname(), fNamesElems);
                final String iname = info.qname.getRawname();
                fErrorReporter.reportWarning("HTML2007", new Object[]{ename, iname});
            }
            if (fDocumentHandler != null) {
                addBodyIfNeeded(info.element.code);
                // PATCH: Marc-Andr� Morissette
                callEndElement(info.qname, i < depth - 1 ? synthesizedAugs() : augs);
            }
        }

        // re-open inline elements
        if (depth > 1) {
            final int size = fInlineStack.top;
            for (int i = 0; i < size; i++) {
                final Info info = fInlineStack.pop();
                final XMLAttributes attributes = info.attributes;
                if (fReportErrors) {
                    final String iname = info.qname.getRawname();
                    fErrorReporter.reportWarning("HTML2008", new Object[]{iname});
                }
                forceStartElement(info.qname, attributes, synthesizedAugs());
            }
        }

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
        fDocumentHandler.startElement(element, attrs, augs);
    }

    private void addBodyIfNeeded(final short element) {
        if (!fDocumentFragment && !fSeenFramesetElement && element == HTMLElements.HTML) {
            if (!fSeenHeadElement) {
                final QName head = createQName("head");
                callStartElement(head, new XMLAttributesImpl(), synthesizedAugs());
                callEndElement(head, synthesizedAugs());
            }
            if (!fSeenBodyElement) {
                final QName body = createQName("body");
                callStartElement(body, new XMLAttributesImpl(), synthesizedAugs());
                callEndElement(body, synthesizedAugs());
            }
        }
    }

    // Call document handler end element.
    protected final void callEndElement(final QName element, final Augmentations augs)
        throws XNIException {
        fDocumentHandler.endElement(element, augs);
    }

    /**
     * @return the depth of the open tag associated with the specified
     * element name or -1 if no matching element is found.
     *
     * @param element The element.
     */
    protected final int getElementDepth(final HTMLElements.Element element) {
        final boolean container = element.isContainer();
        final short elementCode = element.code;
        final boolean tableBodyOrHtml = (elementCode == HTMLElements.TABLE)
            || (elementCode == HTMLElements.BODY) || (elementCode == HTMLElements.HTML);
        int depth = -1;
        for (int i = fElementStack.top - 1; i >= fragmentContextStackSize_; i--) {
            final Info info = fElementStack.data[i];
            if (info.element.code == element.code
                    && (elementCode != HTMLElements.UNKNOWN || (elementCode == HTMLElements.UNKNOWN && element.name.equals(info.element.name)))) {
                depth = fElementStack.top - i;
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
     * @return the depth of the open tag associated with the specified
     * element parent names or -1 if no matching element is found.
     *
     * @param parents The parent elements.
     * @param bounds bounds
     */
    protected int getParentDepth(final HTMLElements.Element[] parents, final short bounds) {
        if (parents != null) {
            for (int i = fElementStack.top - 1; i >= 0; i--) {
                final Info info = fElementStack.data[i];
                if (info.element.code == bounds) {
                    break;
                }
                for (final Element parent : parents) {
                    if (info.element.code == parent.code) {
                        return fElementStack.top - i;
                    }
                }
            }
        }
        return -1;
    }

    // Returns an augmentations object with a synthesized item added.
    protected final Augmentations synthesizedAugs() {
        if (fAugmentations) {
            return SYNTHESIZED_ITEM;
        }
        return null;
    }

    // Modifies the given name based on the specified mode.
    protected static String modifyName(final String name, final short mode) {
        switch (mode) {
            case NAMES_UPPERCASE: return name.toUpperCase(Locale.ROOT);
            case NAMES_LOWERCASE: return name.toLowerCase(Locale.ROOT);
        }
        return name;
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
         * @param element The element qualified name.
         * @param qname qname
         */
        public Info(final HTMLElements.Element element, final QName qname) {
            this(element, qname, null);
        }

        /**
         * Creates an element information object.
         * <p>
         * <strong>Note:</strong>
         * This constructor makes a copy of the element information.
         *
         * @param element The element qualified name.
         * @param attributes The element attributes.
         * @param qname qname
         */
        public Info(final HTMLElements.Element element, final QName qname, final XMLAttributes attributes) {
            this.element = element;
            this.qname = new QName(qname);
            if (attributes != null) {
                final int length = attributes.getLength();
                if (length > 0) {
                    final QName aqname = new QName();
                    final XMLAttributesImpl newattrs = new XMLAttributesImpl();
                    for (int i = 0; i < length; i++) {
                        attributes.getName(i, aqname);
                        newattrs.addAttribute(aqname, attributes.getType(i), attributes.getValue(i), attributes.isSpecified(i));
                    }
                    this.attributes = newattrs;
                }
            }
        }

        /**
         * Simple representation to make debugging easier
         */
        @Override
        public String toString() {
            return super.toString() + qname;
        }
    }

    /** Unsynchronized stack of element information. */
    public static class InfoStack {

        /** The top of the stack. */
        public int top;

        /** The stack data. */
        public Info[] data = new Info[10];

        // Pushes element information onto the stack.
        public void push(final Info info) {
            if (top == data.length) {
                final Info[] newarray = new Info[top + 10];
                System.arraycopy(data, 0, newarray, 0, top);
                data = newarray;
            }
            data[top++] = info;
        }

        // Peeks at the top of the stack.
        public Info peek() {
            return data[top - 1];
        }

        // Pops the top item off of the stack.
        public Info pop() {
            return data[--top];
        }

        // Simple representation to make debugging easier
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("InfoStack(");
            for (int i = top - 1; i >= 0; --i) {
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
        discardedStartElements.add(elem.getRawname());
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
     * Structure to hold information about an element placed in buffer to be comsumed later
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
