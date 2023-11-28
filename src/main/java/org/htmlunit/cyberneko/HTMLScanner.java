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

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Locale;
import java.util.Stack;

import org.htmlunit.cyberneko.xerces.util.EncodingMap;
import org.htmlunit.cyberneko.xerces.util.NamespaceSupport;
import org.htmlunit.cyberneko.xerces.util.URI;
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
import org.htmlunit.cyberneko.xerces.xni.parser.XMLDocumentScanner;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLInputSource;

/**
 * A simple HTML scanner. This scanner makes no attempt to balance tags or fix
 * other problems in the source document &mdash; it just scans what it can and
 * generates XNI document "events", ignoring errors of all kinds.
 * <p>
 * This component recognizes the following features:
 * <ul>
 * <li>http://cyberneko.org/html/features/augmentations
 * <li>http://cyberneko.org/html/features/report-errors
 * <li>http://apache.org/xml/features/scanner/notify-builtin-refs
 * <li>http://cyberneko.org/html/features/scanner/notify-builtin-refs
 * <li>http://cyberneko.org/html/features/scanner/script/strip-cdata-delims
 * <li>http://cyberneko.org/html/features/scanner/script/strip-comment-delims
 * <li>http://cyberneko.org/html/features/scanner/style/strip-cdata-delims
 * <li>http://cyberneko.org/html/features/scanner/style/strip-comment-delims
 * <li>http://cyberneko.org/html/features/scanner/ignore-specified-charset
 * <li>http://cyberneko.org/html/features/scanner/cdata-sections
 * <li>http://cyberneko.org/html/features/override-doctype
 * <li>http://cyberneko.org/html/features/insert-doctype
 * <li>http://cyberneko.org/html/features/parse-noscript-content
 * <li>http://cyberneko.org/html/features/scanner/allow-selfclosing-iframe
 * <li>http://cyberneko.org/html/features/scanner/allow-selfclosing-tags
 * </ul>
 * <p>
 * This component recognizes the following properties:
 * <ul>
 * <li>http://cyberneko.org/html/properties/names/elems
 * <li>http://cyberneko.org/html/properties/names/attrs
 * <li>http://cyberneko.org/html/properties/default-encoding
 * <li>http://cyberneko.org/html/properties/error-reporter
 * <li>http://cyberneko.org/html/properties/doctype/pubid
 * <li>http://cyberneko.org/html/properties/doctype/sysid
 * </ul>
 *
 * @see HTMLElements
 *
 * @author Andy Clark
 * @author Marc Guillemot
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
public class HTMLScanner implements XMLDocumentScanner, XMLLocator, HTMLComponent {

    //
    // Constants
    //

    // doctype info: HTML 4.01 strict

    /** HTML 4.01 strict public identifier ("-//W3C//DTD HTML 4.01//EN"). */
    public static final String HTML_4_01_STRICT_PUBID = "-//W3C//DTD HTML 4.01//EN";

    /**
     * HTML 4.01 strict system identifier ("http://www.w3.org/TR/html4/strict.dtd").
     */
    public static final String HTML_4_01_STRICT_SYSID = "http://www.w3.org/TR/html4/strict.dtd";

    // doctype info: HTML 4.01 loose

    /**
     * HTML 4.01 transitional public identifier ("-//W3C//DTD HTML 4.01
     * Transitional//EN").
     */
    public static final String HTML_4_01_TRANSITIONAL_PUBID = "-//W3C//DTD HTML 4.01 Transitional//EN";

    /**
     * HTML 4.01 transitional system identifier
     * ("http://www.w3.org/TR/html4/loose.dtd").
     */
    public static final String HTML_4_01_TRANSITIONAL_SYSID = "http://www.w3.org/TR/html4/loose.dtd";

    // doctype info: HTML 4.01 frameset

    /**
     * HTML 4.01 frameset public identifier ("-//W3C//DTD HTML 4.01 Frameset//EN").
     */
    public static final String HTML_4_01_FRAMESET_PUBID = "-//W3C//DTD HTML 4.01 Frameset//EN";

    /**
     * HTML 4.01 frameset system identifier
     * ("http://www.w3.org/TR/html4/frameset.dtd").
     */
    public static final String HTML_4_01_FRAMESET_SYSID = "http://www.w3.org/TR/html4/frameset.dtd";

    // features

    /** Include infoset augmentations. */
    protected static final String AUGMENTATIONS = "http://cyberneko.org/html/features/augmentations";

    /** Report errors. */
    protected static final String REPORT_ERRORS = "http://cyberneko.org/html/features/report-errors";

    /**
     * Strip HTML comment delimiters ("&lt;!&minus;&minus;" and
     * "&minus;&minus;&gt;") from SCRIPT tag contents.
     */
    public static final String SCRIPT_STRIP_COMMENT_DELIMS = "http://cyberneko.org/html/features/scanner/script/strip-comment-delims";

    /**
     * Strip XHTML CDATA delimiters ("&lt;![CDATA[" and "]]&gt;") from SCRIPT tag
     * contents.
     */
    public static final String SCRIPT_STRIP_CDATA_DELIMS = "http://cyberneko.org/html/features/scanner/script/strip-cdata-delims";

    /**
     * Strip HTML comment delimiters ("&lt;!&minus;&minus;" and
     * "&minus;&minus;&gt;") from STYLE tag contents.
     */
    public static final String STYLE_STRIP_COMMENT_DELIMS = "http://cyberneko.org/html/features/scanner/style/strip-comment-delims";

    /**
     * Strip XHTML CDATA delimiters ("&lt;![CDATA[" and "]]&gt;") from STYLE tag
     * contents.
     */
    public static final String STYLE_STRIP_CDATA_DELIMS = "http://cyberneko.org/html/features/scanner/style/strip-cdata-delims";

    /**
     * Ignore specified charset found in the &lt;meta equiv='Content-Type'
     * content='text/html;charset=&hellip;'&gt; tag or in the &lt;?xml &hellip;
     * encoding='&hellip;'&gt; processing instruction
     */
    public static final String IGNORE_SPECIFIED_CHARSET = "http://cyberneko.org/html/features/scanner/ignore-specified-charset";

    /** Scan CDATA sections. */
    public static final String CDATA_SECTIONS = "http://cyberneko.org/html/features/scanner/cdata-sections";

    /** Override doctype declaration public and system identifiers. */
    public static final String OVERRIDE_DOCTYPE = "http://cyberneko.org/html/features/override-doctype";

    /** Insert document type declaration. */
    public static final String INSERT_DOCTYPE = "http://cyberneko.org/html/features/insert-doctype";

    /** Parse &lt;noscript&gt;...&lt;/noscript&gt; content */
    public static final String PARSE_NOSCRIPT_CONTENT = "http://cyberneko.org/html/features/parse-noscript-content";

    /** Allows self closing &lt;iframe/&gt; tag */
    public static final String ALLOW_SELFCLOSING_IFRAME = "http://cyberneko.org/html/features/scanner/allow-selfclosing-iframe";

    /** Allows self closing tags e.g. &lt;div/&gt; (XHTML) */
    public static final String ALLOW_SELFCLOSING_TAGS = "http://cyberneko.org/html/features/scanner/allow-selfclosing-tags";

    /** Normalize attribute values. */
    protected static final String NORMALIZE_ATTRIBUTES = "http://cyberneko.org/html/features/scanner/normalize-attrs";

    /** Recognized features. */
    private static final String[] RECOGNIZED_FEATURES = {
        AUGMENTATIONS,
        REPORT_ERRORS,
        SCRIPT_STRIP_CDATA_DELIMS,
        SCRIPT_STRIP_COMMENT_DELIMS,
        STYLE_STRIP_CDATA_DELIMS,
        STYLE_STRIP_COMMENT_DELIMS,
        IGNORE_SPECIFIED_CHARSET,
        CDATA_SECTIONS,
        OVERRIDE_DOCTYPE,
        INSERT_DOCTYPE,
        NORMALIZE_ATTRIBUTES,
        PARSE_NOSCRIPT_CONTENT,
        ALLOW_SELFCLOSING_IFRAME,
        ALLOW_SELFCLOSING_TAGS, };

    /** Recognized features defaults. */
    private static final Boolean[] RECOGNIZED_FEATURES_DEFAULTS = {
        null,
        null,
        Boolean.FALSE,
        Boolean.FALSE,
        Boolean.FALSE,
        Boolean.FALSE,
        Boolean.FALSE,
        Boolean.FALSE,
        Boolean.FALSE,
        Boolean.FALSE,
        Boolean.FALSE,
        Boolean.TRUE,
        Boolean.FALSE,
        Boolean.FALSE, };

    // properties

    /** Modify HTML element names: { "upper", "lower", "default" }. */
    protected static final String NAMES_ELEMS = "http://cyberneko.org/html/properties/names/elems";

    /** Modify HTML attribute names: { "upper", "lower", "default" }. */
    protected static final String NAMES_ATTRS = "http://cyberneko.org/html/properties/names/attrs";

    /** Default encoding. */
    protected static final String DEFAULT_ENCODING = "http://cyberneko.org/html/properties/default-encoding";

    /** Error reporter. */
    protected static final String ERROR_REPORTER = "http://cyberneko.org/html/properties/error-reporter";

    /** Doctype declaration public identifier. */
    protected static final String DOCTYPE_PUBID = "http://cyberneko.org/html/properties/doctype/pubid";

    /** Doctype declaration system identifier. */
    protected static final String DOCTYPE_SYSID = "http://cyberneko.org/html/properties/doctype/sysid";

    /** Recognized properties. */
    private static final String[] RECOGNIZED_PROPERTIES = {
        NAMES_ELEMS,
        NAMES_ATTRS,
        DEFAULT_ENCODING,
        ERROR_REPORTER,
        DOCTYPE_PUBID,
        DOCTYPE_SYSID};

    /** Recognized properties defaults. */
    private static final Object[] RECOGNIZED_PROPERTIES_DEFAULTS = {
        null,
        null,
        "Windows-1252",
        null,
        HTML_4_01_TRANSITIONAL_PUBID,
        HTML_4_01_TRANSITIONAL_SYSID};

    // states

    private static final char REPLACEMENT_CHARACTER = '\uFFFD'; // the � character

    /** State: content. */
    protected static final short STATE_CONTENT = 0;

    /** State: markup bracket. */
    protected static final short STATE_MARKUP_BRACKET = 1;

    /** State: start document. */
    protected static final short STATE_START_DOCUMENT = 10;

    /** State: end document. */
    protected static final short STATE_END_DOCUMENT = 11;

    // modify HTML names

    /** Don't modify HTML names. */
    protected static final short NAMES_NO_CHANGE = 0;

    /** Uppercase HTML names. */
    protected static final short NAMES_UPPERCASE = 1;

    /** Lowercase HTML names. */
    protected static final short NAMES_LOWERCASE = 2;

    // defaults

    /** Default buffer size. */
    protected static final int DEFAULT_BUFFER_SIZE = 2048;

    // debugging

    /** Set to true to debug changes in the scanner. */
    private static final boolean DEBUG_SCANNER = false;

    /** Set to true to debug changes in the scanner state. */
    private static final boolean DEBUG_SCANNER_STATE = false;

    /** Set to true to debug the buffer. */
    private static final boolean DEBUG_BUFFER = false;

    /** Set to true to debug character encoding handling. */
    private static final boolean DEBUG_CHARSET = false;

    /** Set to true to debug callbacks. */
    protected static final boolean DEBUG_CALLBACKS = false;

    // static vars

    /** Synthesized event info item. */
    protected static final HTMLEventInfo SYNTHESIZED_ITEM = new HTMLEventInfo.SynthesizedItem();

    // features

    /** Augmentations. */
    private boolean fAugmentations_;

    /** Report errors. */
    private boolean fReportErrors_;

    /** Strip CDATA delimiters from SCRIPT tags. */
    private boolean fScriptStripCDATADelims_;

    /** Strip comment delimiters from SCRIPT tags. */
    private boolean fScriptStripCommentDelims_;

    /** Strip CDATA delimiters from STYLE tags. */
    private boolean fStyleStripCDATADelims_;

    /** Strip comment delimiters from STYLE tags. */
    private boolean fStyleStripCommentDelims_;

    /** Ignore specified character set. */
    private boolean fIgnoreSpecifiedCharset_;

    /** CDATA sections. */
    private boolean fCDATASections_;

    /** Override doctype declaration public and system identifiers. */
    private boolean fOverrideDoctype_;

    /** Insert document type declaration. */
    private boolean fInsertDoctype_;

    /** Normalize attribute values. */
    private boolean fNormalizeAttributes_;

    /** Parse noscript content. */
    private boolean fParseNoScriptContent_;

    /** Allows self closing iframe tags. */
    private boolean fAllowSelfclosingIframe_;

    /** Allows self closing tags. */
    private boolean fAllowSelfclosingTags_;

    // properties

    /** Modify HTML element names. */
    protected short fNamesElems;

    /** Modify HTML attribute names. */
    protected short fNamesAttrs;

    /** Default encoding. */
    protected String fDefaultIANAEncoding;

    /** Error reporter. */
    protected HTMLErrorReporter fErrorReporter;

    /** Doctype declaration public identifier. */
    protected String fDoctypePubid;

    /** Doctype declaration system identifier. */
    protected String fDoctypeSysid;

    // boundary locator information

    /** Beginning line number. */
    protected int fBeginLineNumber;

    /** Beginning column number. */
    protected int fBeginColumnNumber;

    /** Beginning character offset in the file. */
    protected int fBeginCharacterOffset;

    /** Ending line number. */
    protected int fEndLineNumber;

    /** Ending column number. */
    protected int fEndColumnNumber;

    /** Ending character offset in the file. */
    protected int fEndCharacterOffset;

    // state

    /** The playback byte stream. */
    protected PlaybackInputStream fByteStream;

    /** Current entity. */
    protected CurrentEntity fCurrentEntity;

    /** The current entity stack. */
    protected final Stack<CurrentEntity> fCurrentEntityStack = new Stack<>();

    /** The current scanner. */
    protected Scanner fScanner;

    /** The current scanner state. */
    protected short fScannerState;

    /** The document handler. */
    protected XMLDocumentHandler fDocumentHandler;

    /** Auto-detected IANA encoding. */
    protected String fIANAEncoding;

    /** Auto-detected Java encoding. */
    protected String fJavaEncoding;

    /** Element count. */
    protected int fElementCount;

    /** Element depth. */
    protected int fElementDepth;

    // scanners

    /** Content scanner. */
    protected Scanner fContentScanner = new ContentScanner();

    /**
     * Special scanner used for elements whose content needs to be scanned as plain
     * text, ignoring markup such as elements and entity references. For example:
     * &lt;SCRIPT&gt; and &lt;COMMENT&gt;.
     */
    protected final SpecialScanner fSpecialScanner = new SpecialScanner();

    // temp vars

    /** String buffer. */
    protected final XMLString fStringBuffer = new XMLString();

    /** String buffer. */
    private final XMLString fStringBuffer2 = new XMLString();

    /** Single boolean array. */
    private final boolean[] fSingleBoolean = {false};

    private final HTMLConfiguration htmlConfiguration_;

    HTMLScanner(final HTMLConfiguration htmlConfiguration) {
        htmlConfiguration_ = htmlConfiguration;
    }

    /**
     * Pushes an input source onto the current entity stack. This enables the
     * scanner to transparently scan new content (e.g. the output written by an
     * embedded script). At the end of the current entity, the scanner returns where
     * it left off at the time this entity source was pushed.
     * <p>
     * <strong>Note:</strong> This functionality is experimental at this time and is
     * subject to change in future releases of NekoHTML.
     *
     * @param inputSource The new input source to start scanning.
     * @see #evaluateInputSource(XMLInputSource)
     */
    public void pushInputSource(final XMLInputSource inputSource) {
        final Reader reader = getReader(inputSource);

        fCurrentEntityStack.push(fCurrentEntity);
        final String encoding = inputSource.getEncoding();
        final String publicId = inputSource.getPublicId();
        final String baseSystemId = inputSource.getBaseSystemId();
        final String literalSystemId = inputSource.getSystemId();
        final String expandedSystemId = expandSystemId(literalSystemId, baseSystemId);
        fCurrentEntity = new CurrentEntity(reader, encoding, publicId, baseSystemId, literalSystemId, expandedSystemId);
    }

    private Reader getReader(final XMLInputSource inputSource) {
        final Reader reader = inputSource.getCharacterStream();
        if (reader == null) {
            try {
                return new InputStreamReader(inputSource.getByteStream(), fJavaEncoding);
            }
            catch (final UnsupportedEncodingException e) {
                // should not happen as this encoding is already used to parse the "main" source
            }
        }
        return reader;
    }

    /**
     * Immediately evaluates an input source and add the new content (e.g. the
     * output written by an embedded script).
     *
     * @param inputSource The new input source to start evaluating.
     * @see #pushInputSource(XMLInputSource)
     */
    public void evaluateInputSource(final XMLInputSource inputSource) {
        final Scanner previousScanner = fScanner;
        final short previousScannerState = fScannerState;
        final CurrentEntity previousEntity = fCurrentEntity;
        final Reader reader = getReader(inputSource);

        final String encoding = inputSource.getEncoding();
        final String publicId = inputSource.getPublicId();
        final String baseSystemId = inputSource.getBaseSystemId();
        final String literalSystemId = inputSource.getSystemId();
        final String expandedSystemId = expandSystemId(literalSystemId, baseSystemId);
        fCurrentEntity = new CurrentEntity(reader, encoding, publicId, baseSystemId, literalSystemId, expandedSystemId);
        setScanner(fContentScanner);
        setScannerState(STATE_CONTENT);
        try {
            do {
                fScanner.scan(false);
            }
            while (fScannerState != STATE_END_DOCUMENT);
        }
        catch (final IOException e) {
            // ignore
        }
        setScanner(previousScanner);
        setScannerState(previousScannerState);
        fCurrentEntity = previousEntity;
    }

    /**
     * Cleans up used resources. For example, if scanning is terminated early, then
     * this method ensures all remaining open streams are closed.
     *
     * @param closeall Close all streams, including the original. This is used in
     *                 cases when the application has opened the original document
     *                 stream and should be responsible for closing it.
     */
    public void cleanup(final boolean closeall) {
        final int size = fCurrentEntityStack.size();
        if (size > 0) {
            // current entity is not the original, so close it
            if (fCurrentEntity != null) {
                fCurrentEntity.closeQuietly();
            }
            // close remaining streams
            for (int i = closeall ? 0 : 1; i < size; i++) {
                fCurrentEntity = fCurrentEntityStack.pop();
                fCurrentEntity.closeQuietly();
            }
        }
        else if (closeall && fCurrentEntity != null) {
            fCurrentEntity.closeQuietly();
        }
    }

    /** Returns the encoding. */
    @Override
    public String getEncoding() {
        return fCurrentEntity != null ? fCurrentEntity.encoding_ : null;
    }

    /** Returns the public identifier. */
    @Override
    public String getPublicId() {
        return fCurrentEntity != null ? fCurrentEntity.publicId : null;
    }

    /** Returns the base system identifier. */
    @Override
    public String getBaseSystemId() {
        return fCurrentEntity != null ? fCurrentEntity.baseSystemId : null;
    }

    /** Returns the literal system identifier. */
    @Override
    public String getLiteralSystemId() {
        return fCurrentEntity != null ? fCurrentEntity.literalSystemId : null;
    }

    /** Returns the expanded system identifier. */
    @Override
    public String getExpandedSystemId() {
        return fCurrentEntity != null ? fCurrentEntity.expandedSystemId : null;
    }

    /** Returns the current line number. */
    @Override
    public int getLineNumber() {
        return fCurrentEntity != null ? fCurrentEntity.getLineNumber() : -1;
    }

    /** Returns the current column number. */
    @Override
    public int getColumnNumber() {
        return fCurrentEntity != null ? fCurrentEntity.getColumnNumber() : -1;
    }

    /** Returns the XML version. */
    @Override
    public String getXMLVersion() {
        return fCurrentEntity != null ? fCurrentEntity.version : null;
    }

    /** Returns the character offset. */
    @Override
    public int getCharacterOffset() {
        return fCurrentEntity != null ? fCurrentEntity.getCharacterOffset() : -1;
    }

    //
    // HTMLComponent methods
    //

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
    public void reset(final XMLComponentManager manager) throws XMLConfigurationException {

        // get features
        fAugmentations_ = manager.getFeature(AUGMENTATIONS);
        fReportErrors_ = manager.getFeature(REPORT_ERRORS);
        fScriptStripCDATADelims_ = manager.getFeature(SCRIPT_STRIP_CDATA_DELIMS);
        fScriptStripCommentDelims_ = manager.getFeature(SCRIPT_STRIP_COMMENT_DELIMS);
        fStyleStripCDATADelims_ = manager.getFeature(STYLE_STRIP_CDATA_DELIMS);
        fStyleStripCommentDelims_ = manager.getFeature(STYLE_STRIP_COMMENT_DELIMS);
        fIgnoreSpecifiedCharset_ = manager.getFeature(IGNORE_SPECIFIED_CHARSET);
        fCDATASections_ = manager.getFeature(CDATA_SECTIONS);
        fOverrideDoctype_ = manager.getFeature(OVERRIDE_DOCTYPE);
        fInsertDoctype_ = manager.getFeature(INSERT_DOCTYPE);
        fNormalizeAttributes_ = manager.getFeature(NORMALIZE_ATTRIBUTES);
        fParseNoScriptContent_ = manager.getFeature(PARSE_NOSCRIPT_CONTENT);
        fAllowSelfclosingIframe_ = manager.getFeature(ALLOW_SELFCLOSING_IFRAME);
        fAllowSelfclosingTags_ = manager.getFeature(ALLOW_SELFCLOSING_TAGS);

        // get properties
        fNamesElems = getNamesValue(String.valueOf(manager.getProperty(NAMES_ELEMS)));
        fNamesAttrs = getNamesValue(String.valueOf(manager.getProperty(NAMES_ATTRS)));
        fDefaultIANAEncoding = String.valueOf(manager.getProperty(DEFAULT_ENCODING));
        fErrorReporter = (HTMLErrorReporter) manager.getProperty(ERROR_REPORTER);
        fDoctypePubid = String.valueOf(manager.getProperty(DOCTYPE_PUBID));
        fDoctypeSysid = String.valueOf(manager.getProperty(DOCTYPE_SYSID));
    }

    /** Sets a feature. */
    @Override
    public void setFeature(final String featureId, final boolean state) {

        if (featureId.equals(AUGMENTATIONS)) {
            fAugmentations_ = state;
        }
        else if (featureId.equals(IGNORE_SPECIFIED_CHARSET)) {
            fIgnoreSpecifiedCharset_ = state;
        }
        else if (featureId.equals(SCRIPT_STRIP_CDATA_DELIMS)) {
            fScriptStripCDATADelims_ = state;
        }
        else if (featureId.equals(SCRIPT_STRIP_COMMENT_DELIMS)) {
            fScriptStripCommentDelims_ = state;
        }
        else if (featureId.equals(STYLE_STRIP_CDATA_DELIMS)) {
            fStyleStripCDATADelims_ = state;
        }
        else if (featureId.equals(STYLE_STRIP_COMMENT_DELIMS)) {
            fStyleStripCommentDelims_ = state;
        }
        else if (featureId.equals(PARSE_NOSCRIPT_CONTENT)) {
            fParseNoScriptContent_ = state;
        }
        else if (featureId.equals(ALLOW_SELFCLOSING_IFRAME)) {
            fAllowSelfclosingIframe_ = state;
        }
        else if (featureId.equals(ALLOW_SELFCLOSING_TAGS)) {
            fAllowSelfclosingTags_ = state;
        }
    }

    /** Sets a property. */
    @Override
    public void setProperty(final String propertyId, final Object value) throws XMLConfigurationException {

        if (propertyId.equals(NAMES_ELEMS)) {
            fNamesElems = getNamesValue(String.valueOf(value));
            return;
        }

        if (propertyId.equals(NAMES_ATTRS)) {
            fNamesAttrs = getNamesValue(String.valueOf(value));
            return;
        }

        if (propertyId.equals(DEFAULT_ENCODING)) {
            fDefaultIANAEncoding = String.valueOf(value);
            return;
        }
    }

    //
    // XMLDocumentScanner methods
    //

    /** Sets the input source. */
    @Override
    public void setInputSource(final XMLInputSource source) throws IOException {

        // reset state
        fElementCount = 0;
        fElementDepth = -1;
        fByteStream = null;
        fCurrentEntityStack.removeAllElements();

        fBeginLineNumber = 1;
        fBeginColumnNumber = 1;
        fBeginCharacterOffset = 0;
        fEndLineNumber = fBeginLineNumber;
        fEndColumnNumber = fBeginColumnNumber;
        fEndCharacterOffset = fBeginCharacterOffset;

        // reset encoding information
        fIANAEncoding = fDefaultIANAEncoding;
        fJavaEncoding = fIANAEncoding;

        // get location information
        String encoding = source.getEncoding();
        final String publicId = source.getPublicId();
        final String baseSystemId = source.getBaseSystemId();
        final String literalSystemId = source.getSystemId();
        final String expandedSystemId = expandSystemId(literalSystemId, baseSystemId);

        // open stream
        Reader reader = source.getCharacterStream();
        if (reader == null) {
            InputStream inputStream = source.getByteStream();
            if (inputStream == null) {
                final URL url = new URL(expandedSystemId);
                inputStream = url.openStream();
            }
            fByteStream = new PlaybackInputStream(inputStream);
            final String[] encodings = new String[2];
            if (encoding == null) {
                fByteStream.detectEncoding(encodings);
            }
            else {
                encodings[0] = encoding;
            }
            if (encodings[0] == null) {
                encodings[0] = fDefaultIANAEncoding;
                if (fReportErrors_) {
                    fErrorReporter.reportWarning("HTML1000", null);
                }
            }
            if (encodings[1] == null) {
                encodings[1] = EncodingMap.getIANA2JavaMapping(encodings[0].toUpperCase(Locale.ROOT));
                if (encodings[1] == null) {
                    encodings[1] = encodings[0];
                    if (fReportErrors_) {
                        fErrorReporter.reportWarning("HTML1001", new Object[] {encodings[0]});
                    }
                }
            }
            fIANAEncoding = encodings[0];
            fJavaEncoding = encodings[1];
            encoding = fIANAEncoding;
            reader = new InputStreamReader(fByteStream, fJavaEncoding);
        }
        fCurrentEntity = new CurrentEntity(reader, encoding, publicId, baseSystemId, literalSystemId, expandedSystemId);

        // set scanner and state
        setScanner(fContentScanner);
        setScannerState(STATE_START_DOCUMENT);
    }

    /** Scans the document. */
    @Override
    public boolean scanDocument(final boolean complete) throws XNIException, IOException {
        do {
            if (!fScanner.scan(complete)) {
                return false;
            }
        }
        while (complete);

        return true;
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

    //
    // Protected static methods
    //

    // Returns the value of the specified attribute, ignoring case.
    protected static String getValue(final XMLAttributes attrs, final String aname) {
        if (attrs != null) {
            final int length = attrs.getLength();
            for (int i = 0; i < length; i++) {
                if (attrs.getQName(i).equalsIgnoreCase(aname)) {
                    return attrs.getValue(i);
                }
            }
        }
        return null;
    }

    /**
     * Expands a system id and returns the system id as a URI, if it can be
     * expanded. A return value of null means that the identifier is already
     * expanded. An exception thrown indicates a failure to expand the id.
     *
     * @param systemId     The systemId to be expanded.
     * @param baseSystemId baseSystemId
     *
     * @return Returns the URI string representing the expanded system identifier. A
     *         null value indicates that the given system identifier is already
     *         expanded.
     *
     */
    @SuppressWarnings("unused")
    public static String expandSystemId(final String systemId, final String baseSystemId) {

        // check for bad parameters id
        if (systemId == null || systemId.length() == 0) {
            return systemId;
        }
        // if id already expanded, return
        try {
            new URI(systemId);
            return systemId;
        }
        catch (final URI.MalformedURIException e) {
            // continue on...
        }
        // normalize id
        final String id = fixURI(systemId);

        // normalize base
        URI base;
        URI uri = null;
        try {
            if (baseSystemId == null || baseSystemId.length() == 0 || baseSystemId.equals(systemId)) {

                String dir;
                try {
                    dir = fixURI(System.getProperty("user.dir"))
                            // deal with blanks in paths; maybe we have to do better uri encoding here
                            .replaceAll(" ", "%20");

                }
                catch (final SecurityException se) {
                    dir = "";
                }
                if (!dir.endsWith("/")) {
                    dir = dir + "/";
                }
                base = new URI("file", "", dir, null, null);
            }
            else {
                try {
                    base = new URI(fixURI(baseSystemId));
                }
                catch (final URI.MalformedURIException e) {
                    String dir;
                    try {
                        dir = fixURI(System.getProperty("user.dir"))
                                // deal with blanks in paths; maybe we have to do better uri encoding here
                                .replaceAll(" ", "%20");
                    }
                    catch (final SecurityException se) {
                        dir = "";
                    }
                    if (baseSystemId.indexOf(':') != -1) {
                        // for xml schemas we might have baseURI with
                        // a specified drive
                        base = new URI("file", "", fixURI(baseSystemId), null, null);
                    }
                    else {
                        if (!dir.endsWith("/")) {
                            dir = dir + "/";
                        }
                        dir = dir + fixURI(baseSystemId);
                        base = new URI("file", "", dir, null, null);
                    }
                }
            }

            // expand id
            uri = new URI(base, id);
        }
        catch (final URI.MalformedURIException e) {
            // let it go through
        }

        if (uri == null) {
            return systemId;
        }
        return uri.toString();
    }

    /**
     * Fixes a platform dependent filename to standard URI form.
     *
     * @param str The string to fix.
     *
     * @return Returns the fixed URI string.
     */
    protected static String fixURI(String str) {

        // handle platform dependent strings
        str = str.replace(java.io.File.separatorChar, '/');

        // Windows fix
        if (str.length() >= 2) {
            final char ch1 = str.charAt(1);
            // change "C:blah" to "/C:blah"
            if (ch1 == ':') {
                final char ch0 = String.valueOf(str.charAt(0)).toUpperCase(Locale.ROOT).charAt(0);
                if (ch0 >= 'A' && ch0 <= 'Z') {
                    str = "/" + str;
                }
            }
            // change "//blah" to "file://blah"
            else if (ch1 == '/' && str.charAt(0) == '/') {
                str = "file:" + str;
            }
        }

        // done
        return str;
    }

    // Modifies the given name based on the specified mode.
    protected static String modifyName(final String name, final short mode) {
        switch (mode) {
            case NAMES_UPPERCASE:
                return name.toUpperCase(Locale.ROOT);
            case NAMES_LOWERCASE:
                return name.toLowerCase(Locale.ROOT);
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
    // debugging

    // Sets the scanner.
    protected void setScanner(final Scanner scanner) {
        fScanner = scanner;
        if (DEBUG_SCANNER) {
            System.out.print("$$$ setScanner(");
            System.out.print(scanner != null ? scanner.getClass().getName() : "null");
            System.out.println(");");
        }
    }

    // Sets the scanner state.
    protected void setScannerState(final short state) {
        fScannerState = state;
        if (DEBUG_SCANNER_STATE) {
            System.out.print("$$$ setScannerState(");
            switch (fScannerState) {
                case STATE_CONTENT: {
                    System.out.print("STATE_CONTENT");
                    break;
                }
                case STATE_MARKUP_BRACKET: {
                    System.out.print("STATE_MARKUP_BRACKET");
                    break;
                }
                case STATE_START_DOCUMENT: {
                    System.out.print("STATE_START_DOCUMENT");
                    break;
                }
                case STATE_END_DOCUMENT: {
                    System.out.print("STATE_END_DOCUMENT");
                    break;
                }
            }
            System.out.println(");");
        }
    }

    // scanning

    // Scans a DOCTYPE line.
    protected void scanDoctype() throws IOException {
        String root = null;
        String pubid = null;
        String sysid = null;

        if (skipSpaces()) {
            root = scanName(true);
            if (root == null) {
                if (fReportErrors_) {
                    fErrorReporter.reportError("HTML1014", null);
                }
            }
            else {
                root = modifyName(root, fNamesElems);
            }
            if (skipSpaces()) {
                if (skip("PUBLIC", false)) {
                    skipSpaces();
                    pubid = scanLiteral();
                    if (skipSpaces()) {
                        sysid = scanLiteral();
                    }
                }
                else if (skip("SYSTEM", false)) {
                    skipSpaces();
                    sysid = scanLiteral();
                }
            }
        }
        int c;
        while ((c = fCurrentEntity.read()) != -1) {
            if (c == '<') {
                fCurrentEntity.rewind();
                break;
            }
            if (c == '>') {
                break;
            }
            if (c == '[') {
                skipMarkup(true);
                break;
            }
        }

        if (fDocumentHandler != null) {
            if (fOverrideDoctype_) {
                pubid = fDoctypePubid;
                sysid = fDoctypeSysid;
            }
            fEndLineNumber = fCurrentEntity.getLineNumber();
            fEndColumnNumber = fCurrentEntity.getColumnNumber();
            fEndCharacterOffset = fCurrentEntity.getCharacterOffset();
            fDocumentHandler.doctypeDecl(root, pubid, sysid, locationAugs());
        }
    }

    // Scans a quoted literal.
    protected String scanLiteral() throws IOException {
        final int quote = fCurrentEntity.read();
        if (quote == '\'' || quote == '"') {
            final StringBuilder str = new StringBuilder();
            int c;
            while ((c = fCurrentEntity.read()) != -1) {
                if (c == quote) {
                    break;
                }
                if (c == '\r' || c == '\n') {
                    fCurrentEntity.rewind();
                    // NOTE: This collapses newlines to a single space.
                    // [Q] Is this the right thing to do here? -Ac
                    skipNewlines();
                    str.append(' ');
                }
                else if (c == '<') {
                    fCurrentEntity.rewind();
                    break;
                }
                else {
                    appendChar(str, c, null);
                }
            }
            if (c == -1) {
                if (fReportErrors_) {
                    fErrorReporter.reportError("HTML1007", null);
                }
                throw new EOFException();
            }
            return str.toString();
        }
        fCurrentEntity.rewind();
        return null;
    }

    // Scans a name.
    protected String scanName(final boolean strict) throws IOException {
        if (DEBUG_BUFFER) {
            fCurrentEntity.debugBufferIfNeeded("(scanName: ");
        }
        if (fCurrentEntity.offset_ == fCurrentEntity.length_) {
            if (fCurrentEntity.load(0) == -1) {
                if (DEBUG_BUFFER) {
                    fCurrentEntity.debugBufferIfNeeded(")scanName: ");
                }
                return null;
            }
        }
        int offset = fCurrentEntity.offset_;
        while (true) {
            while (fCurrentEntity.hasNext()) {
                final char c = fCurrentEntity.getNextChar();
                if ((strict && (!Character.isLetterOrDigit(c) && c != '-' && c != '.' && c != ':' && c != '_'))
                        || (!strict && (Character.isWhitespace(c) || c == '=' || c == '/' || c == '>'))) {
                    fCurrentEntity.rewind();
                    break;
                }
            }
            if (fCurrentEntity.offset_ == fCurrentEntity.length_) {
                final int length = fCurrentEntity.length_ - offset;
                System.arraycopy(fCurrentEntity.buffer_, offset, fCurrentEntity.buffer_, 0, length);
                final int count = fCurrentEntity.load(length);
                offset = 0;
                if (count == -1) {
                    break;
                }
            }
            else {
                break;
            }
        }
        final int length = fCurrentEntity.offset_ - offset;
        final String name = length > 0 ? new String(fCurrentEntity.buffer_, offset, length) : null;
        if (DEBUG_BUFFER) {
            fCurrentEntity.debugBufferIfNeeded(")scanName: ", " -> \"" + name + '"');
        }
        return name;
    }

    // Scans an entity reference.
    protected int scanEntityRef(final XMLString str, final boolean content) throws IOException {
        str.clear();
        str.append('&');

        // use readPreservingBufferContent inside this method to be sure we can rewind

        int nextChar = readPreservingBufferContent();
        if (nextChar == -1) {
            return returnEntityRefString(str, content);
        }
        str.append((char) nextChar);

        if ('#' == nextChar) {
            final HTMLUnicodeEntitiesParser parser = new HTMLUnicodeEntitiesParser();

            nextChar = readPreservingBufferContent();
            if (nextChar != -1) {
                str.append((char) nextChar);
            }

            while (nextChar != -1 && parser.parseNumeric(nextChar)) {
                nextChar = readPreservingBufferContent();
                if (nextChar != -1) {
                    str.append((char) nextChar);
                }
            }

            final String match = parser.getMatch();
            if (match == null) {
                final String consumed = str.toString();
                fCurrentEntity.rewind(consumed.length() - 1);
                str.clear();
                str.append('&');
            }
            else {
                fCurrentEntity.rewind(parser.getRewindCount());
                str.clear();
                str.append(match);
            }
            return returnEntityRefString(str, content);
        }

        // we read regular entities such as &lt; here
        int readCount = 1;
        // this will be our state of the parsing, we have to feed that back to the parser
        HTMLNamedEntitiesParser.State result = null;
        // in case of incorrect entities such as &notin where we are supposed to recognize
        // &not, we have to keep the last matching state, so we can fall back to it
        HTMLNamedEntitiesParser.State lastMatchingResult = null;

        while (nextChar != -1) {
            HTMLNamedEntitiesParser.State intermediateResult = HTMLNamedEntitiesParser.get().lookup(nextChar, result);

            if (intermediateResult.endNode) {
                result = intermediateResult;
                break;
            }
            if (intermediateResult == result) {
                // nothing changed, more characters have not done anything
                break;
            }
            if (intermediateResult.isMatch) {
                lastMatchingResult = intermediateResult;
            }
            result = intermediateResult;

            nextChar = readPreservingBufferContent();
            if (nextChar != -1) {
                str.append((char) nextChar);
                readCount++;
            }
        }

        // it might happen that we read &lta but need just &lt so
        // we have to go back to the last match
        if (!result.isMatch && lastMatchingResult != null) {
            result = lastMatchingResult;
        }

        // hopefully, we got something, otherwise we have to go
        // the error route
        if (result.isMatch) {
            // in case we overran because the entity was broken or
            // not terminated by a ;, we have to reset the char
            // position because we read one more char than the entity has
            fCurrentEntity.rewind(readCount - result.length);

            // if we have a correct character that is terminate by ;
            // we can keep things simple
            if (result.endsWithSemicolon) {
                str.clear();
                str.append(result.resolvedValue);
            }
            else {
                if (fReportErrors_) {
                    fErrorReporter.reportWarning("HTML1004", null);
                }

                // If there is a match
                // {
                //      If the character reference was consumed as part of an attribute, and the last character matched is not
                //      a U+003B SEMICOLON character (;), and the next input character is either a U+003D EQUALS SIGN character (=)
                //      or an ASCII alphanumeric,
                //      then, for historical reasons, flush code points consumed as a character reference and switch to the return state.

                //      Otherwise:
                //      1. If the last character matched is not a U+003B SEMICOLON character (;), then this is a missing-semicolon-after-character-reference parse error.
                //      2. Set the temporary buffer to the empty string. Append one or two characters corresponding to the character reference name
                //      (as given by the second column of the named character references table) to the temporary buffer.
                //      3. Flush code points consumed as a character reference. Switch to the return state.
                // }
                // Otherwise
                // {
                //      Flush code points consumed as a character reference. Switch to the ambiguous ampersand state.
                // }
                if (content) {
                    str.clear();
                    str.append(result.resolvedValue);
                }
                else {
                    // look ahead
                    // 13.2.5.73
                    final int matchLength = result.length + 1;
                    if (matchLength < str.length()) {
                        nextChar = str.charAt(matchLength);
                        if ('=' == nextChar || '0' <= nextChar && nextChar <= '9' || 'A' <= nextChar && nextChar <= 'Z'
                                || 'a' <= nextChar && nextChar <= 'z') {
                            // we just shorten our temp str instead of copying stuff around
                            str.delete(result.length + 1, str.length() + 1);
                        }
                        else {
                            str.clear();
                            str.append(result.resolvedValue);
                        }
                    }
                    else {
                        str.clear();
                        str.append(result.resolvedValue);
                    }
                }
            }
        }
        else {
            // Entity not found, rewind and continue
            // broken from here, aka keeping everything
            fCurrentEntity.rewind(readCount);
            str.clear();
            str.append('&');
        }

        return returnEntityRefString(str, content);
    }

    private int returnEntityRefString(final XMLString str, final boolean content) {
        if (content && fDocumentHandler != null && fElementCount >= fElementDepth) {
            fEndLineNumber = fCurrentEntity.getLineNumber();
            fEndColumnNumber = fCurrentEntity.getColumnNumber();
            fEndCharacterOffset = fCurrentEntity.getCharacterOffset();
            fDocumentHandler.characters(str, locationAugs());
        }
        return -1;
    }

    // Returns true if the specified text is present and is skipped.
    protected boolean skip(final String s, final boolean caseSensitive) throws IOException {
        final int length = s != null ? s.length() : 0;
        for (int i = 0; i < length; i++) {
            if (fCurrentEntity.offset_ == fCurrentEntity.length_) {
                System.arraycopy(fCurrentEntity.buffer_, fCurrentEntity.offset_ - i, fCurrentEntity.buffer_, 0, i);
                if (fCurrentEntity.load(i) == -1) {
                    fCurrentEntity.offset_ = 0;
                    return false;
                }
            }
            char c0 = s.charAt(i);
            char c1 = fCurrentEntity.getNextChar();
            if (!caseSensitive) {
                c0 = String.valueOf(c0).toUpperCase(Locale.ROOT).charAt(0);
                c1 = String.valueOf(c1).toUpperCase(Locale.ROOT).charAt(0);
            }
            if (c0 != c1) {
                fCurrentEntity.rewind(i + 1);
                return false;
            }
        }
        return true;
    }

    // Skips markup.
    protected boolean skipMarkup(final boolean balance) throws IOException {
        if (DEBUG_BUFFER) {
            fCurrentEntity.debugBufferIfNeeded("(skipMarkup: ");
        }
        int depth = 1;
        boolean slashgt = false;
        OUTER: while (true) {
            if (fCurrentEntity.offset_ == fCurrentEntity.length_) {
                if (fCurrentEntity.load(0) == -1) {
                    break OUTER;
                }
            }
            while (fCurrentEntity.hasNext()) {
                char c = fCurrentEntity.getNextChar();
                if (balance && c == '<') {
                    depth++;
                }
                else if (c == '>') {
                    depth--;
                    if (depth == 0) {
                        break OUTER;
                    }
                }
                else if (c == '/') {
                    if (fCurrentEntity.offset_ == fCurrentEntity.length_) {
                        if (fCurrentEntity.load(0) == -1) {
                            break OUTER;
                        }
                    }
                    c = fCurrentEntity.getNextChar();
                    if (c == '>') {
                        slashgt = true;
                        depth--;
                        if (depth == 0) {
                            break OUTER;
                        }
                    }
                    else {
                        fCurrentEntity.rewind();
                    }
                }
                else if (c == '\r' || c == '\n') {
                    fCurrentEntity.rewind();
                    skipNewlines();
                }
            }
        }
        if (DEBUG_BUFFER) {
            fCurrentEntity.debugBufferIfNeeded(")skipMarkup: ", " -> " + slashgt);
        }
        return slashgt;
    }

    // Skips whitespace.
    protected boolean skipSpaces() throws IOException {
        if (DEBUG_BUFFER) {
            fCurrentEntity.debugBufferIfNeeded("(skipSpaces: ");
        }
        boolean spaces = false;
        while (true) {
            if (fCurrentEntity.offset_ == fCurrentEntity.length_) {
                if (fCurrentEntity.load(0) == -1) {
                    break;
                }
            }
            final char c = fCurrentEntity.getNextChar();
            if (!Character.isWhitespace(c)) {
                fCurrentEntity.rewind();
                break;
            }
            spaces = true;
            if (c == '\r' || c == '\n') {
                fCurrentEntity.rewind();
                skipNewlines();
                continue;
            }
        }
        if (DEBUG_BUFFER) {
            fCurrentEntity.debugBufferIfNeeded(")skipSpaces: ", " -> " + spaces);
        }
        return spaces;
    }

    // Skips newlines and returns the number of newlines skipped.
    protected int skipNewlines() throws IOException {
        if (DEBUG_BUFFER) {
            fCurrentEntity.debugBufferIfNeeded("(skipNewlines: ");
        }

        if (!fCurrentEntity.hasNext()) {
            if (fCurrentEntity.load(0) == -1) {
                if (DEBUG_BUFFER) {
                    fCurrentEntity.debugBufferIfNeeded(")skipNewlines: ");
                }
                return 0;
            }
        }
        char c = fCurrentEntity.getCurrentChar();
        int newlines = 0;
        if (c == '\n' || c == '\r') {
            do {
                c = fCurrentEntity.getNextChar();
                if (c == '\r') {
                    newlines++;
                    if (fCurrentEntity.offset_ == fCurrentEntity.length_) {
                        fCurrentEntity.offset_ = newlines;
                        if (fCurrentEntity.load(newlines) == -1) {
                            break;
                        }
                    }
                    if (fCurrentEntity.getCurrentChar() == '\n') {
                        fCurrentEntity.offset_++;
                        fCurrentEntity.characterOffset_++;
                    }
                }
                else if (c == '\n') {
                    newlines++;
                    if (fCurrentEntity.offset_ == fCurrentEntity.length_) {
                        fCurrentEntity.offset_ = newlines;
                        if (fCurrentEntity.load(newlines) == -1) {
                            break;
                        }
                    }
                }
                else {
                    fCurrentEntity.rewind();
                    break;
                }
            }
            while (fCurrentEntity.offset_ < fCurrentEntity.length_ - 1);
            fCurrentEntity.incLine(newlines);
        }
        if (DEBUG_BUFFER) {
            fCurrentEntity.debugBufferIfNeeded(")skipNewlines: ", " -> " + newlines);
        }
        return newlines;
    }

    // infoset utility methods

    // Returns an augmentations object with a location item added.
    protected final Augmentations locationAugs() {
        if (fAugmentations_) {
            return new LocationItem(fBeginLineNumber, fBeginColumnNumber, fBeginCharacterOffset, fEndLineNumber,
                    fEndColumnNumber, fEndCharacterOffset);
        }
        return null;
    }

    // Returns an augmentations object with a synthesized item added.
    protected final Augmentations synthesizedAugs() {
        if (fAugmentations_) {
            return SYNTHESIZED_ITEM;
        }
        return null;
    }

    //
    // Protected static methods
    //

    // Returns true if the name is a built-in XML general entity reference.
    protected static boolean builtinXmlRef(final String name) {
        return "amp".equals(name) || "lt".equals(name) || "gt".equals(name) || "quot".equals(name)
                || "apos".equals(name);
    }

    //
    // Private methods
    //

    /**
     * Append a character to an XMLStringBuffer. The character is an int value, and
     * can either be a single UTF-16 character or a supplementary character
     * represented by two UTF-16 code points.
     *
     * @param str   The XMLStringBuffer to append to.
     * @param value The character value.
     * @param name  to be used for error reporting
     */
    private void appendChar(final XMLString str, final int value, String name) {
        if (value > Character.MAX_VALUE) {
            try {
                final char[] chars = Character.toChars(value);
                str.append(chars, 0, chars.length);
            }
            catch (final IllegalArgumentException e) { // when value is not valid as UTF-16
                if (fReportErrors_) {
                    if (name == null) {
                        name = "&#" + value + ';';
                    }
                    fErrorReporter.reportError("HTML1005", new Object[] {name});
                }
                str.append(REPLACEMENT_CHARACTER);
            }
        }
        else {
            str.append((char) value);
        }
    }

    /**
     * Append a character to a StringBuilder. The character is an int value, and can
     * either be a single UTF-16 character or a supplementary character represented
     * by two UTF-16 code points.
     *
     * @param str   The StringBuilder to append to.
     * @param value The character value.
     * @param name  to be used for error reporting
     */
    private void appendChar(final StringBuilder str, final int value, String name) {
        if (value > Character.MAX_VALUE) {
            try {
                final char[] chars = Character.toChars(value);
                str.append(chars, 0, chars.length);
            }
            catch (final IllegalArgumentException e) { // when value is not valid as UTF-16
                if (fReportErrors_) {
                    if (name == null) {
                        name = "&#" + value + ';';
                    }
                    fErrorReporter.reportError("HTML1005", new Object[] {name});
                }
                str.append(REPLACEMENT_CHARACTER);
            }
        }
        else {
            str.append((char) value);
        }
    }

    //
    // Interfaces
    //

    /**
     * Basic scanner interface.
     *
     * @author Andy Clark
     */
    public interface Scanner {

        //
        // Scanner methods
        //

        /**
         * Scans part of the document. This interface allows scanning to be performed in
         * a pulling manner.
         *
         * @param complete True if the scanner should not return until scanning is
         *                 complete.
         *
         * @return True if additional scanning is required.
         *
         * @throws IOException Thrown if I/O error occurs.
         */
        boolean scan(boolean complete) throws IOException;
    }

    //
    // Classes
    //

    /**
     * Current entity.
     *
     * @author Andy Clark
     */
    public static final class CurrentEntity {

        /** Character stream. */
        private Reader stream_;

        /** Encoding. */
        private String encoding_;

        /** Public identifier. */
        public final String publicId;

        /** Base system identifier. */
        public final String baseSystemId;

        /** Literal system identifier. */
        public final String literalSystemId;

        /** Expanded system identifier. */
        public final String expandedSystemId;

        /** XML version. */
        public final String version = "1.0";

        /** Line number. */
        private int lineNumber_ = 1;

        /** Column number. */
        private int columnNumber_ = 1;

        /** Character offset in the file. */
        private int characterOffset_ = 0;

        // buffer

        /** Character buffer. */
        private char[] buffer_ = new char[DEFAULT_BUFFER_SIZE];

        /** Offset into character buffer. */
        private int offset_ = 0;

        /** Length of characters read into character buffer. */
        private int length_ = 0;

        private boolean endReached_ = false;

        // Constructs an entity from the specified stream.
        public CurrentEntity(final Reader stream, final String encoding, final String publicId, final String baseSystemId, final String literalSystemId, final String expandedSystemId) {
            stream_ = stream;
            this.encoding_ = encoding;
            this.publicId = publicId;
            this.baseSystemId = baseSystemId;
            this.literalSystemId = literalSystemId;
            this.expandedSystemId = expandedSystemId;
        }

        private char getCurrentChar() {
            return buffer_[offset_];
        }

        /**
         * @return the current character and moves to next one.
         */
        private char getNextChar() {
            characterOffset_++;
            columnNumber_++;
            return buffer_[offset_++];
        }

        private void closeQuietly() {
            try {
                stream_.close();
            }
            catch (final IOException e) {
                // ignore
            }
        }

        /**
         * Indicates if there are characters left.
         */
        boolean hasNext() {
            return offset_ < length_;
        }

        /**
         * Loads a new chunk of data into the buffer and returns the number of
         * characters loaded or -1 if no additional characters were loaded.
         *
         * @param loadOffset The offset at which new characters should be loaded.
         * @return count
         * @throws IOException in case of io problems
         */
        protected int load(final int loadOffset) throws IOException {
            if (DEBUG_BUFFER) {
                debugBufferIfNeeded("(load: ");
            }
            // resize buffer, if needed
            if (loadOffset == buffer_.length) {
                final int adjust = buffer_.length / 4;
                final char[] array = new char[buffer_.length + adjust];
                System.arraycopy(buffer_, 0, array, 0, length_);
                buffer_ = array;
            }
            // read a block of characters
            final int count = stream_.read(buffer_, loadOffset, buffer_.length - loadOffset);
            if (count == -1) {
                endReached_ = true;
            }
            length_ = count != -1 ? count + loadOffset : loadOffset;
            this.offset_ = loadOffset;
            if (DEBUG_BUFFER) {
                debugBufferIfNeeded(")load: ", " -> " + count);
            }
            return count;
        }

        // Reads a single character.
        protected int read() throws IOException {
            if (DEBUG_BUFFER) {
                debugBufferIfNeeded("(read: ");
            }
            if (offset_ == length_) {
                if (endReached_) {
                    return -1;
                }
                if (load(0) == -1) {
                    if (DEBUG_BUFFER) {
                        System.out.println(")read: -> -1");
                    }
                    return -1;
                }
            }
            final char c = buffer_[offset_++];
            characterOffset_++;
            columnNumber_++;

            if (DEBUG_BUFFER) {
                debugBufferIfNeeded(")read: ", " -> " + c);
            }
            return c;
        }

        /** Prints the contents of the character buffer to standard out. */
        private void debugBufferIfNeeded(final String prefix) {
            debugBufferIfNeeded(prefix, "");
        }

        /** Prints the contents of the character buffer to standard out. */
        private void debugBufferIfNeeded(final String prefix, final String suffix) {
            if (DEBUG_BUFFER) {
                System.out.print(prefix);
                System.out.print('[');
                System.out.print(length_);
                System.out.print(' ');
                System.out.print(offset_);
                if (length_ > 0) {
                    System.out.print(" \"");
                    for (int i = 0; i < length_; i++) {
                        if (i == offset_) {
                            System.out.print('^');
                        }
                        final char c = buffer_[i];
                        switch (c) {
                            case '\r':
                                System.out.print("\\r");
                                break;
                            case '\n':
                                System.out.print("\\n");
                                break;
                            case '\t':
                                System.out.print("\\t");
                                break;
                            case '"':
                                System.out.print("\\\"");
                                break;
                            default:
                                System.out.print(c);
                        }
                    }
                    if (offset_ == length_) {
                        System.out.print('^');
                    }
                    System.out.print('"');
                }
                System.out.print(']');
                System.out.print(suffix);
                System.out.println();
            }
        }

        private void setStream(final InputStreamReader inputStreamReader) {
            stream_ = inputStreamReader;
            offset_ = 0;
            length_ = 0;
            characterOffset_ = 0;
            lineNumber_ = 1;
            columnNumber_ = 1;
            encoding_ = inputStreamReader.getEncoding();
        }

        /**
         * Goes back, cancelling the effect of the previous read() call.
         */
        private void rewind() {
            offset_--;
            characterOffset_--;
            columnNumber_--;
        }

        private void rewind(final int i) {
            offset_ -= i;
            characterOffset_ -= i;
            columnNumber_ -= i;
        }

        private void incLine() {
            lineNumber_++;
            columnNumber_ = 1;
        }

        private void incLine(final int nbLines) {
            lineNumber_ += nbLines;
            columnNumber_ = 1;
        }

        public int getLineNumber() {
            return lineNumber_;
        }

        private void resetBuffer(final XMLString xmlBuffer, final int lineNumber, final int columnNumber,
                final int characterOffset) {
            lineNumber_ = lineNumber;
            columnNumber_ = columnNumber;
            this.characterOffset_ = characterOffset;

            // TODO RBRi
            this.buffer_ = xmlBuffer.getChars();
            this.offset_ = 0;
            this.length_ = xmlBuffer.length();
        }

        private int getColumnNumber() {
            return columnNumber_;
        }

        private void restorePosition(final int originalOffset, final int originalColumnNumber, final int originalCharacterOffset) {
            this.offset_ = originalOffset;
            this.columnNumber_ = originalColumnNumber;
            this.characterOffset_ = originalCharacterOffset;
        }

        private int getCharacterOffset() {
            return characterOffset_;
        }
    }

    /**
     * The primary HTML document scanner.
     *
     * @author Andy Clark
     */
    public class ContentScanner implements Scanner {

        /** A qualified name. */
        private final QName qName_ = new QName();

        /** Attributes. */
        private final XMLAttributesImpl attributes_ = new XMLAttributesImpl();

        /** Scan. */
        @Override
        public boolean scan(final boolean complete) throws IOException {
            boolean next;
            do {
                try {
                    next = false;
                    switch (fScannerState) {
                        case STATE_CONTENT: {
                            fBeginLineNumber = fCurrentEntity.getLineNumber();
                            fBeginColumnNumber = fCurrentEntity.getColumnNumber();
                            fBeginCharacterOffset = fCurrentEntity.getCharacterOffset();
                            final int c = fCurrentEntity.read();
                            if (c == -1) {
                                throw new EOFException();
                            }
                            if (c == '<') {
                                setScannerState(STATE_MARKUP_BRACKET);
                                next = true;
                            }
                            else if (c == '&') {
                                scanEntityRef(fStringBuffer, true);
                            }
                            else {
                                fCurrentEntity.rewind();
                                scanCharacters();
                            }
                            break;
                        }
                        case STATE_MARKUP_BRACKET: {
                            final int c = fCurrentEntity.read();
                            if (c == -1) {
                                if (fReportErrors_) {
                                    fErrorReporter.reportError("HTML1003", null);
                                }
                                if (fDocumentHandler != null && fElementCount >= fElementDepth) {
                                    fStringBuffer.clear();
                                    fStringBuffer.append('<');
                                    fDocumentHandler.characters(fStringBuffer, null);
                                }
                                throw new EOFException();
                            }
                            if (c == '!') {
                                // process some strange self closing comments first
                                if (skip("--->", false) || skip("-->", false) || skip("->", false) || skip(">", false)) {
                                    fEndLineNumber = fCurrentEntity.getLineNumber();
                                    fEndColumnNumber = fCurrentEntity.getColumnNumber();
                                    fEndCharacterOffset = fCurrentEntity.getCharacterOffset();
                                    fDocumentHandler.comment(new XMLString(), locationAugs());
                                }
                                else if (skip("--", false)) {
                                    scanComment();
                                }
                                else if (skip("[CDATA[", false)) {
                                    scanCDATA();
                                }
                                else if (skip("DOCTYPE", false)) {
                                    scanDoctype();
                                }
                                else {
                                    if (fReportErrors_) {
                                        fErrorReporter.reportError("HTML1002", null);
                                    }
                                    skipMarkup(true);
                                }
                            }
                            else if (c == '?') {
                                scanPI();
                            }
                            else if (c == '/') {
                                scanEndElement();
                            }
                            else {
                                fCurrentEntity.rewind();
                                fElementCount++;
                                fSingleBoolean[0] = false;

                                final String ename = scanStartElement(fSingleBoolean);
                                final String enameLC = ename == null ? null : ename.toLowerCase(Locale.ROOT);

                                fBeginLineNumber = fCurrentEntity.getLineNumber();
                                fBeginColumnNumber = fCurrentEntity.getColumnNumber();
                                fBeginCharacterOffset = fCurrentEntity.getCharacterOffset();

                                if ("script".equals(enameLC)) {
                                    scanScriptContent();
                                }
                                else if (!fAllowSelfclosingTags_ && !fAllowSelfclosingIframe_ && "iframe".equals(enameLC)) {
                                    scanUntilEndTag("iframe");
                                }
                                else if (!fParseNoScriptContent_ && "noscript".equals(enameLC)) {
                                    scanUntilEndTag("noscript");
                                }
                                else if ("noframes".equals(enameLC)) {
                                    scanUntilEndTag("noframes");
                                }
                                else if ("noembed".equals(enameLC)) {
                                    scanUntilEndTag("noembed");
                                }
                                else if (ename != null && htmlConfiguration_.getHtmlElements().getElement(enameLC).isSpecial()
                                        && (!"title".equals(enameLC) || isEnded(enameLC))) {
                                    if ("plaintext".equals(enameLC)) {
                                        setScanner(new PlainTextScanner());
                                    }
                                    else {
                                        setScanner(fSpecialScanner.setElementName(ename));
                                        setScannerState(STATE_CONTENT);
                                    }
                                    return true;
                                }
                            }
                            setScannerState(STATE_CONTENT);
                            break;
                        }
                        case STATE_START_DOCUMENT: {
                            if (fDocumentHandler != null && fElementCount >= fElementDepth) {
                                if (DEBUG_CALLBACKS) {
                                    System.out.println("startDocument()");
                                }
                                final XMLLocator locator = HTMLScanner.this;
                                final String encoding = fIANAEncoding;
                                final Augmentations augs = locationAugs();
                                final NamespaceContext nscontext = new NamespaceSupport();
                                fDocumentHandler.startDocument(locator, encoding, nscontext, augs);
                            }
                            if (fInsertDoctype_ && fDocumentHandler != null) {
                                String root = htmlConfiguration_.getHtmlElements().getElement(HTMLElements.HTML).name;
                                root = modifyName(root, fNamesElems);
                                final String pubid = fDoctypePubid;
                                final String sysid = fDoctypeSysid;
                                fDocumentHandler.doctypeDecl(root, pubid, sysid, synthesizedAugs());
                            }
                            setScannerState(STATE_CONTENT);
                            break;
                        }
                        case STATE_END_DOCUMENT: {
                            if (fDocumentHandler != null && fElementCount >= fElementDepth && complete) {
                                if (DEBUG_CALLBACKS) {
                                    System.out.println("endDocument()");
                                }
                                fEndLineNumber = fCurrentEntity.getLineNumber();
                                fEndColumnNumber = fCurrentEntity.getColumnNumber();
                                fEndCharacterOffset = fCurrentEntity.getCharacterOffset();
                                fDocumentHandler.endDocument(locationAugs());
                            }
                            return false;
                        }
                        default: {
                            throw new RuntimeException("unknown scanner state: " + fScannerState);
                        }
                    }
                }
                catch (final EOFException e) {
                    if (fCurrentEntityStack.empty()) {
                        setScannerState(STATE_END_DOCUMENT);
                    }
                    else {
                        fCurrentEntity = fCurrentEntityStack.pop();
                    }
                    next = true;
                }
            }
            while (next || complete);
            return true;
        }

        /**
         * Scans the content of <noscript>: it doesn't get parsed but is considered as
         * plain text when feature {@link HTMLScanner#PARSE_NOSCRIPT_CONTENT} is set to
         * false.
         *
         * @param tagName the tag for which content is scanned (one of "noscript",
         *                "noframes", "iframe")
         * @throws IOException on error
         */
        private void scanUntilEndTag(final String tagName) throws IOException {
            final XMLString xmlString = new XMLString();
            final String end = "/" + tagName;
            final int lengthToScan = tagName.length() + 2;

            while (true) {
                final int c = fCurrentEntity.read();
                if (c == -1) {
                    break;
                }
                if (c == '<') {
                    final String next = nextContent(lengthToScan) + " ";
                    if (next.length() >= lengthToScan && end.equalsIgnoreCase(next.substring(0, end.length()))
                            && ('>' == next.charAt(lengthToScan - 1)
                                    || Character.isWhitespace(next.charAt(lengthToScan - 1)))) {
                        fCurrentEntity.rewind();
                        break;
                    }
                }
                if (c == '\r' || c == '\n') {
                    fCurrentEntity.rewind();
                    final int newlines = skipNewlines();
                    for (int i = 0; i < newlines; i++) {
                        xmlString.append('\n');
                    }
                }
                else {
                    appendChar(xmlString, c, null);
                }
            }
            if (xmlString.length() > 0 && fDocumentHandler != null) {
                fEndLineNumber = fCurrentEntity.getLineNumber();
                fEndColumnNumber = fCurrentEntity.getColumnNumber();
                fEndCharacterOffset = fCurrentEntity.getCharacterOffset();
                fDocumentHandler.characters(xmlString, locationAugs());
            }
        }

        private void scanScriptContent() throws IOException {
            final XMLString xmlString = new XMLString();
            boolean waitForEndComment = false;
            boolean invalidComment = false;
            while (true) {
                final int c = fCurrentEntity.read();
                if (c == -1) {
                    break;
                }
                else if (c == '-' && xmlString.endsWith("<!-")) {
                    waitForEndComment = endCommentAvailable();
                }
                else if (!waitForEndComment && c == '<') {
                    final String next = nextContent(8) + " ";
                    if (next.length() >= 8 && "/script".equalsIgnoreCase(next.substring(0, 7))
                            && ('>' == next.charAt(7) || Character.isWhitespace(next.charAt(7)))) {
                        fCurrentEntity.rewind();
                        break;
                    }
                }
                else if (c == '>') {
                    if (xmlString.endsWith("--")) {
                        waitForEndComment = false;
                    }
                    if (xmlString.endsWith("--!")) {
                        invalidComment = true;
                        waitForEndComment = false;
                    }
                }

                if (c == '\r' || c == '\n') {
                    fCurrentEntity.rewind();
                    final int newlines = skipNewlines();
                    for (int i = 0; i < newlines; i++) {
                        xmlString.append('\n');
                    }
                }
                else {
                    appendChar(xmlString, c, null);
                }
            }

            if (fScriptStripCommentDelims_) {
                if (invalidComment) {
                    xmlString.reduceToContent("<!--", "--!>");
                }
                else {
                    xmlString.reduceToContent("<!--", "-->");
                }
            }
            if (fScriptStripCDATADelims_) {
                xmlString.reduceToContent("<![CDATA[", "]]>");
            }

            if (xmlString.length() > 0 && fDocumentHandler != null && fElementCount >= fElementDepth) {
                if (DEBUG_CALLBACKS) {
                    System.out.println("characters(" + xmlString + ")");
                }
                fEndLineNumber = fCurrentEntity.getLineNumber();
                fEndColumnNumber = fCurrentEntity.getColumnNumber();
                fEndCharacterOffset = fCurrentEntity.getCharacterOffset();
                fDocumentHandler.characters(xmlString, locationAugs());
            }
        }

        /**
         * Reads the next characters WITHOUT impacting the buffer content up to current
         * offset.
         *
         * @param len the number of characters to read
         * @return the read string (length may be smaller if EOF is encountered)
         * @throws IOException in case of io problems
         */
        protected String nextContent(final int len) throws IOException {
            final int originalOffset = fCurrentEntity.offset_;
            final int originalColumnNumber = fCurrentEntity.getColumnNumber();
            final int originalCharacterOffset = fCurrentEntity.getCharacterOffset();

            final char[] buff = new char[len];
            int nbRead;
            for (nbRead = 0; nbRead < len; ++nbRead) {
                // read() should not clear the buffer
                if (fCurrentEntity.offset_ == fCurrentEntity.length_) {
                    if (fCurrentEntity.length_ == fCurrentEntity.buffer_.length) {
                        fCurrentEntity.load(fCurrentEntity.buffer_.length);
                    }
                    else { // everything was already loaded
                        break;
                    }
                }

                final int c = fCurrentEntity.read();
                if (c == -1) {
                    break;
                }
                buff[nbRead] = (char) c;
            }
            fCurrentEntity.restorePosition(originalOffset, originalColumnNumber, originalCharacterOffset);
            return new String(buff, 0, nbRead);
        }

        //
        // Protected methods
        //

        // Scans characters.
        protected void scanCharacters() throws IOException {
            if (DEBUG_BUFFER) {
                fCurrentEntity.debugBufferIfNeeded("(scanCharacters: ");
            }
            fStringBuffer.clear();
            while (true) {
                final int newlines = skipNewlines();
                if (newlines == 0 && fCurrentEntity.offset_ == fCurrentEntity.length_) {
                    if (DEBUG_BUFFER) {
                        fCurrentEntity.debugBufferIfNeeded(")scanCharacters: ");
                    }
                    break;
                }
                char c;
                final int offset = fCurrentEntity.offset_ - newlines;
                for (int i = offset; i < fCurrentEntity.offset_; i++) {
                    fCurrentEntity.buffer_[i] = '\n';
                }
                while (fCurrentEntity.hasNext()) {
                    c = fCurrentEntity.getNextChar();
                    if (c == '<' || c == '&' || c == '\n' || c == '\r') {
                        fCurrentEntity.rewind();
                        break;
                    }
                }
                if (fCurrentEntity.offset_ > offset && fDocumentHandler != null && fElementCount >= fElementDepth) {
                    if (DEBUG_CALLBACKS) {
                        final XMLString xmlString = new XMLString(fCurrentEntity.buffer_, offset,
                                fCurrentEntity.offset_ - offset);
                        System.out.println("characters(" + xmlString + ")");
                    }
                    fEndLineNumber = fCurrentEntity.getLineNumber();
                    fEndColumnNumber = fCurrentEntity.getColumnNumber();
                    fEndCharacterOffset = fCurrentEntity.getCharacterOffset();
                    fStringBuffer.append(fCurrentEntity.buffer_, offset, fCurrentEntity.offset_ - offset);
                }
                if (DEBUG_BUFFER) {
                    fCurrentEntity.debugBufferIfNeeded(")scanCharacters: ");
                }

                final boolean hasNext = fCurrentEntity.offset_ < fCurrentEntity.buffer_.length;
                final int next = hasNext ? fCurrentEntity.getCurrentChar() : -1;

                if (next == '&' || next == '<' || next == -1) {
                    break;
                }

            } // end while

            if (fStringBuffer.length() != 0) {
                fDocumentHandler.characters(fStringBuffer, locationAugs());
            }
        }

        // Scans a CDATA section.
        protected void scanCDATA() throws IOException {
            if (DEBUG_BUFFER) {
                fCurrentEntity.debugBufferIfNeeded("(scanCDATA: ");
            }
            fStringBuffer.clear();
            if (fCDATASections_) {
                if (fDocumentHandler != null && fElementCount >= fElementDepth) {
                    fEndLineNumber = fCurrentEntity.getLineNumber();
                    fEndColumnNumber = fCurrentEntity.getColumnNumber();
                    fEndCharacterOffset = fCurrentEntity.getCharacterOffset();
                    if (DEBUG_CALLBACKS) {
                        System.out.println("startCDATA()");
                    }
                    fDocumentHandler.startCDATA(locationAugs());
                }
            }
            else {
                fStringBuffer.append("[CDATA[");
            }
            final boolean eof = scanMarkupContent(fStringBuffer, ']');
            if (!fCDATASections_) {
                fStringBuffer.append("]]");
            }
            if (fDocumentHandler != null && fElementCount >= fElementDepth) {
                fEndLineNumber = fCurrentEntity.getLineNumber();
                fEndColumnNumber = fCurrentEntity.getColumnNumber();
                fEndCharacterOffset = fCurrentEntity.getCharacterOffset();
                if (fCDATASections_) {
                    if (DEBUG_CALLBACKS) {
                        System.out.println("characters(" + fStringBuffer + ")");
                    }
                    fDocumentHandler.characters(fStringBuffer, locationAugs());
                    if (DEBUG_CALLBACKS) {
                        System.out.println("endCDATA()");
                    }
                    fDocumentHandler.endCDATA(locationAugs());
                }
                else {
                    if (DEBUG_CALLBACKS) {
                        System.out.println("comment(" + fStringBuffer + ")");
                    }
                    fDocumentHandler.comment(fStringBuffer, locationAugs());
                }
            }
            if (DEBUG_BUFFER) {
                fCurrentEntity.debugBufferIfNeeded(")scanCDATA: ");
            }
            if (eof) {
                throw new EOFException();
            }
        }

        // Scans a comment.
        protected void scanComment() throws IOException {
            if (DEBUG_BUFFER) {
                fCurrentEntity.debugBufferIfNeeded("(scanComment: ");
            }
            fEndLineNumber = fCurrentEntity.getLineNumber();
            fEndColumnNumber = fCurrentEntity.getColumnNumber();
            fEndCharacterOffset = fCurrentEntity.getCharacterOffset();
            XMLString xmlString = new XMLString();
            boolean eof = scanMarkupContent(xmlString, '-');
            // no --> found, comment with end only with >
            if (eof) {
                fCurrentEntity.resetBuffer(xmlString, fEndLineNumber, fEndColumnNumber, fEndCharacterOffset);
                xmlString = new XMLString(); // take a new one to avoid interactions
                while (true) {
                    final int c = fCurrentEntity.read();
                    if (c == -1) {
                        if (fReportErrors_) {
                            fErrorReporter.reportError("HTML1007", null);
                        }
                        eof = true;
                        break;
                    }
                    else if (c == '\n' || c == '\r') {
                        fCurrentEntity.rewind();
                        final int newlines = skipNewlines();
                        for (int i = 0; i < newlines; i++) {
                            xmlString.append('\n');
                        }
                        continue;
                    }
                    else if (c != '>') {
                        appendChar(xmlString, c, null);
                        continue;
                    }
                    eof = false;
                    break;
                }
            }
            if (fDocumentHandler != null && fElementCount >= fElementDepth) {
                if (DEBUG_CALLBACKS) {
                    System.out.println("comment(" + xmlString + ")");
                }
                fEndLineNumber = fCurrentEntity.getLineNumber();
                fEndColumnNumber = fCurrentEntity.getColumnNumber();
                fEndCharacterOffset = fCurrentEntity.getCharacterOffset();
                fDocumentHandler.comment(xmlString, locationAugs());
            }
            if (DEBUG_BUFFER) {
                fCurrentEntity.debugBufferIfNeeded(")scanComment: ");
            }
            if (eof) {
                throw new EOFException();
            }
        }

        // Scans markup content.
        protected boolean scanMarkupContent(final XMLString xmlString, final char cend) throws IOException {
            int c;
            OUTER: while (true) {
                c = fCurrentEntity.read();
                if (c == -1) {
                    if (fReportErrors_) {
                        fErrorReporter.reportError("HTML1007", null);
                    }
                    break;
                }
                else if (c == cend) {
                    int count = 1;
                    while (true) {
                        c = fCurrentEntity.read();
                        if (c == cend) {
                            count++;
                            continue;
                        }
                        break;
                    }
                    if (c == -1) {
                        if (fReportErrors_) {
                            fErrorReporter.reportError("HTML1007", null);
                        }
                        break OUTER;
                    }
                    if (count < 2) {
                        xmlString.append(cend);
                        // if (c != -1) {
                        fCurrentEntity.rewind();
                        // }
                        continue;
                    }
                    if (c != '>') {
                        for (int i = 0; i < count; i++) {
                            xmlString.append(cend);
                        }
                        fCurrentEntity.rewind();
                        continue;
                    }
                    for (int i = 0; i < count - 2; i++) {
                        xmlString.append(cend);
                    }
                    break;
                }
                else if (c == '\n' || c == '\r') {
                    fCurrentEntity.rewind();
                    final int newlines = skipNewlines();
                    for (int i = 0; i < newlines; i++) {
                        xmlString.append('\n');
                    }
                    continue;
                }
                appendChar(xmlString, c, null);
            }
            return c == -1;
        }

        // Scans a processing instruction.
        protected void scanPI() throws IOException {
            if (DEBUG_BUFFER) {
                fCurrentEntity.debugBufferIfNeeded("(scanPI: ");
            }
            if (fReportErrors_) {
                fErrorReporter.reportWarning("HTML1008", null);
            }

            // scan processing instruction
            final String target = scanName(true);
            if (target != null && !"xml".equalsIgnoreCase(target)) {
                while (true) {
                    int c = fCurrentEntity.read();
                    if (c == -1) {
                        break;
                    }
                    if (c == '\r' || c == '\n') {
                        if (c == '\r') {
                            c = fCurrentEntity.read();
                            if (c == -1) {
                                break;
                            }
                            if (c != '\n') {
                                fCurrentEntity.offset_--;
                                fCurrentEntity.characterOffset_--;
                            }
                        }
                        fCurrentEntity.incLine();
                        continue;
                    }
                    if (c != ' ' && c != '\t') {
                        fCurrentEntity.rewind();
                        break;
                    }
                }
                fStringBuffer.clear();
                while (true) {
                    int c = fCurrentEntity.read();
                    if (c == -1) {
                        break;
                    }
                    if (c == '?' || c == '/') {
                        final char c0 = (char) c;
                        c = fCurrentEntity.read();
                        if (c == -1 || c == '>') {
                            break;
                        }
                        fStringBuffer.append(c0);
                        fCurrentEntity.rewind();
                        continue;
                    }
                    else if (c == '\r' || c == '\n') {
                        fStringBuffer.append('\n');
                        if (c == '\r') {
                            c = fCurrentEntity.read();
                            if (c == -1) {
                                break;
                            }
                            if (c != '\n') {
                                fCurrentEntity.offset_--;
                                fCurrentEntity.characterOffset_--;
                            }
                        }
                        fCurrentEntity.incLine();
                        continue;
                    }
                    else if (c == '>') {
                        // invalid procession instruction, handle as comment
                        if (fDocumentHandler != null) {
                            fStringBuffer.append(target);
                            fEndLineNumber = fCurrentEntity.getLineNumber();
                            fEndColumnNumber = fCurrentEntity.getColumnNumber();
                            fEndCharacterOffset = fCurrentEntity.getCharacterOffset();
                            fDocumentHandler.comment(fStringBuffer, locationAugs());
                        }
                        return;
                    }
                    else {
                        appendChar(fStringBuffer, c, null);
                    }
                }
                if (fDocumentHandler != null) {
                    fEndLineNumber = fCurrentEntity.getLineNumber();
                    fEndColumnNumber = fCurrentEntity.getColumnNumber();
                    fEndCharacterOffset = fCurrentEntity.getCharacterOffset();
                    fDocumentHandler.processingInstruction(target, fStringBuffer, locationAugs());
                }
            }

            // scan xml/text declaration
            else {
                final int beginLineNumber = fBeginLineNumber;
                final int beginColumnNumber = fBeginColumnNumber;
                final int beginCharacterOffset = fBeginCharacterOffset;
                attributes_.removeAllAttributes();
                int aindex = 0;
                while (scanPseudoAttribute(attributes_)) {
                    // if we haven't scanned a value, remove the entry as values have special
                    // signification
                    if (attributes_.getValue(aindex).length() == 0) {
                        attributes_.removeAttributeAt(aindex);
                    }
                    else {
                        attributes_.getName(aindex, qName_);
                        qName_.rawname = qName_.rawname.toLowerCase(Locale.ROOT);
                        attributes_.setName(aindex, qName_);
                        aindex++;
                    }
                }
                if (fDocumentHandler != null) {
                    final String version = attributes_.getValue("version");
                    final String encoding = attributes_.getValue("encoding");
                    final String standalone = attributes_.getValue("standalone");

                    // if the encoding is successfully changed, the stream will be processed again
                    // with the right encoding an we will come here again but without need to change
                    // the encoding
                    final boolean xmlDeclNow = fIgnoreSpecifiedCharset_ || !changeEncoding(encoding);
                    if (xmlDeclNow) {
                        fBeginLineNumber = beginLineNumber;
                        fBeginColumnNumber = beginColumnNumber;
                        fBeginCharacterOffset = beginCharacterOffset;
                        fEndLineNumber = fCurrentEntity.getLineNumber();
                        fEndColumnNumber = fCurrentEntity.getColumnNumber();
                        fEndCharacterOffset = fCurrentEntity.getCharacterOffset();
                        fDocumentHandler.xmlDecl(version, encoding, standalone, locationAugs());
                    }
                }
            }

            if (DEBUG_BUFFER) {
                fCurrentEntity.debugBufferIfNeeded(")scanPI: ");
            }
        }

        /**
         * Scans a start element.
         *
         * @param empty Is used for a second return value to indicate whether the start
         *              element tag is empty (e.g. "/&gt;").
         * @return ename
         * @throws IOException in case of io problems
         */
        protected String scanStartElement(final boolean[] empty) throws IOException {
            String ename = scanName(true);
            final int length = ename != null ? ename.length() : 0;
            final int c = length > 0 ? ename.charAt(0) : -1;
            if (length == 0 || !((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) {
                if (fReportErrors_) {
                    fErrorReporter.reportError("HTML1009", null);
                }
                if (fDocumentHandler != null && fElementCount >= fElementDepth) {
                    fStringBuffer.clear();
                    fStringBuffer.append('<');
                    if (length > 0) {
                        fStringBuffer.append(ename);
                    }
                    fDocumentHandler.characters(fStringBuffer, null);
                }
                return null;
            }
            ename = modifyName(ename, fNamesElems);
            attributes_.removeAllAttributes();
            final int beginLineNumber = fBeginLineNumber;
            final int beginColumnNumber = fBeginColumnNumber;
            final int beginCharacterOffset = fBeginCharacterOffset;
            while (scanAttribute(attributes_, empty)) {
                // do nothing
            }
            fBeginLineNumber = beginLineNumber;
            fBeginColumnNumber = beginColumnNumber;
            fBeginCharacterOffset = beginCharacterOffset;
            if (fByteStream != null && fElementDepth == -1) {
                if ("META".equalsIgnoreCase(ename) && !fIgnoreSpecifiedCharset_) {
                    if (DEBUG_CHARSET) {
                        System.out.println("+++ <META>");
                    }
                    final String httpEquiv = getValue(attributes_, "http-equiv");
                    if (httpEquiv != null && "content-type".equalsIgnoreCase(httpEquiv)) {
                        if (DEBUG_CHARSET) {
                            System.out.println("+++ @content-type: \"" + httpEquiv + '"');
                        }
                        String content = getValue(attributes_, "content");
                        if (content != null) {
                            content = removeSpaces(content);
                            final int index1 = content.toLowerCase(Locale.ROOT).indexOf("charset=");
                            if (index1 != -1) {
                                final int index2 = content.indexOf(';', index1);
                                final String charset = index2 != -1 ? content.substring(index1 + 8, index2)
                                        : content.substring(index1 + 8);
                                changeEncoding(charset);
                            }
                        }
                    }
                    else {
                        final String metaCharset = getValue(attributes_, "charset");
                        if (metaCharset != null) {
                            changeEncoding(metaCharset);
                        }
                    }
                }
                else if ("BODY".equalsIgnoreCase(ename)) {
                    fByteStream.clear();
                    fByteStream = null;
                }
                else {
                    final HTMLElements.Element element = htmlConfiguration_.getHtmlElements().getElement(ename);
                    if (element.parent != null && element.parent.length > 0) {
                        if (element.parent[0].code == HTMLElements.BODY) {
                            fByteStream.clear();
                            fByteStream = null;
                        }
                    }
                }
            }
            if (fDocumentHandler != null && fElementCount >= fElementDepth) {
                qName_.setValues(null, ename, ename, null);
                if (DEBUG_CALLBACKS) {
                    System.out.println("startElement(" + qName_ + ',' + attributes_ + ")");
                }
                fEndLineNumber = fCurrentEntity.getLineNumber();
                fEndColumnNumber = fCurrentEntity.getColumnNumber();
                fEndCharacterOffset = fCurrentEntity.getCharacterOffset();
                if (empty[0] && !"BR".equalsIgnoreCase(ename)) {
                    fDocumentHandler.emptyElement(qName_, attributes_, locationAugs());
                }
                else {
                    fDocumentHandler.startElement(qName_, attributes_, locationAugs());
                }
            }
            return ename;
        }

        /**
         * Removes all spaces for the string (remember: JDK 1.3!)
         */
        private String removeSpaces(final String content) {
            StringBuilder sb = null;
            for (int i = content.length() - 1; i >= 0; --i) {
                if (Character.isWhitespace(content.charAt(i))) {
                    if (sb == null) {
                        sb = new StringBuilder(content);
                    }
                    sb.deleteCharAt(i);
                }
            }
            return (sb == null) ? content : sb.toString();
        }

        /**
         * Tries to change the encoding used to read the input stream to the specified
         * one
         *
         * @param charset the charset that should be used
         * @return <code>true</code> when the encoding has been changed
         */
        private boolean changeEncoding(String charset) {
            if (charset == null || fByteStream == null) {
                return false;
            }
            charset = charset.trim();
            boolean encodingChanged = false;
            try {
                String javaEncoding = EncodingMap.getIANA2JavaMapping(charset.toUpperCase(Locale.ROOT));
                if (DEBUG_CHARSET) {
                    System.out.println("+++ ianaEncoding: " + charset);
                    System.out.println("+++ javaEncoding: " + javaEncoding);
                }
                if (javaEncoding == null) {
                    javaEncoding = charset;
                    if (fReportErrors_) {
                        fErrorReporter.reportError("HTML1001", new Object[] {charset});
                    }
                }
                // patch: Marc Guillemot
                if (!javaEncoding.equals(fJavaEncoding)) {
                    if (!isEncodingCompatible(javaEncoding, fJavaEncoding)) {
                        if (fReportErrors_) {
                            fErrorReporter.reportError("HTML1015", new Object[] {javaEncoding, fJavaEncoding});
                        }
                    }
                    // change the charset
                    else {
                        fJavaEncoding = javaEncoding;
                        fCurrentEntity.setStream(new InputStreamReader(fByteStream, javaEncoding));
                        fByteStream.playback();
                        fElementDepth = fElementCount;
                        fElementCount = 0;
                        encodingChanged = true;
                    }
                }
            }
            catch (final UnsupportedEncodingException e) {
                if (fReportErrors_) {
                    fErrorReporter.reportError("HTML1010", new Object[] {charset});
                }
                // NOTE: If the encoding change doesn't work,
                // then there's no point in continuing to
                // buffer the input stream.
                fByteStream.clear();
                fByteStream = null;
            }
            return encodingChanged;
        }

        /**
         * Scans a real attribute.
         *
         * @param attributes The list of attributes.
         * @param empty      Is used for a second return value to indicate whether the
         *                   start element tag is empty (e.g. "/&gt;").
         * @return success
         * @throws IOException in case of io problems
         */
        protected boolean scanAttribute(final XMLAttributesImpl attributes, final boolean[] empty) throws IOException {
            return scanAttribute(attributes, empty, '/');
        }

        /**
         * Scans a pseudo attribute.
         *
         * @param attributes The list of attributes.
         * @return success
         * @throws IOException in case of io problems
         */
        protected boolean scanPseudoAttribute(final XMLAttributesImpl attributes) throws IOException {
            return scanAttribute(attributes, fSingleBoolean, '?');
        }

        /**
         * Scans an attribute, pseudo or real.
         *
         * @param attributes The list of attributes.
         * @param empty      Is used for a second return value to indicate whether the
         *                   start element tag is empty (e.g. "/&gt;").
         * @param endc       The end character that appears before the closing angle
         *                   bracket ('&gt;').
         * @return success
         * @throws IOException in case of io problems
         */
        protected boolean scanAttribute(final XMLAttributesImpl attributes, final boolean[] empty, final char endc) throws IOException {
            final boolean skippedSpaces = skipSpaces();
            fBeginLineNumber = fCurrentEntity.getLineNumber();
            fBeginColumnNumber = fCurrentEntity.getColumnNumber();
            fBeginCharacterOffset = fCurrentEntity.getCharacterOffset();
            int c = fCurrentEntity.read();
            if (c == -1) {
                if (fReportErrors_) {
                    fErrorReporter.reportError("HTML1007", null);
                }
                return false;
            }
            else if (c == '>') {
                return false;
            }
            else if (c == '<') {
                fCurrentEntity.rewind();
                if (fReportErrors_) {
                    fErrorReporter.reportError("HTML1016", null);
                }
                return false;
            }
            fCurrentEntity.rewind();
            String aname = scanName(false);
            if (aname == null) {
                if (fReportErrors_) {
                    fErrorReporter.reportError("HTML1011", null);
                }

                // check if the next char is '=' and handle this according to the spec
                skipSpaces();
                if (!fCurrentEntity.hasNext() || '=' != fCurrentEntity.getNextChar()) {
                    fCurrentEntity.rewind();
                    empty[0] = skipMarkup(false);
                    return false;
                }
                aname = '=' + scanName(false);
            }
            if (!skippedSpaces && fReportErrors_) {
                fErrorReporter.reportError("HTML1013", new Object[] {aname});
            }
            aname = modifyName(aname, fNamesAttrs);
            skipSpaces();
            c = fCurrentEntity.read();
            if (c == -1) {
                if (fReportErrors_) {
                    fErrorReporter.reportError("HTML1007", null);
                }
                throw new EOFException();
            }
            if (c == '/' || c == '>') {
                qName_.setValues(null, aname, aname, null);
                attributes.addAttribute(qName_, "CDATA", "");
                attributes.setSpecified(attributes.getLength() - 1, true);
                if (c == '/') {
                    fCurrentEntity.rewind();
                    empty[0] = skipMarkup(false);
                }
                return false;
            }
            if (c == '=') {
                skipSpaces();
                c = fCurrentEntity.read();
                if (c == -1) {
                    if (fReportErrors_) {
                        fErrorReporter.reportError("HTML1007", null);
                    }
                    throw new EOFException();
                }
                // Xiaowei/Ac: Fix for <a href=/cgi-bin/myscript>...</a>
                if (c == '>') {
                    qName_.setValues(null, aname, aname, null);
                    attributes.addAttribute(qName_, "CDATA", "");
                    attributes.setSpecified(attributes.getLength() - 1, true);
                    return false;
                }
                fStringBuffer.clear();
                if (c != '\'' && c != '"') {
                    fCurrentEntity.rewind();
                    while (true) {
                        c = fCurrentEntity.read();
                        if (c == -1) {
                            if (fReportErrors_) {
                                fErrorReporter.reportError("HTML1007", null);
                            }
                            throw new EOFException();
                        }
                        // Xiaowei/Ac: Fix for <a href=/broken/>...</a>
                        if (Character.isWhitespace((char) c) || c == '>') {
                            // fCharOffset--;
                            fCurrentEntity.rewind();
                            break;
                        }
                        if (c == '&') {
                            scanEntityRef(fStringBuffer2, false);
                            fStringBuffer.append(fStringBuffer2);
                        }
                        else {
                            appendChar(fStringBuffer, c, null);
                        }
                    }
                    qName_.setValues(null, aname, aname, null);
                    final String avalue = fStringBuffer.toString();
                    attributes.addAttribute(qName_, "CDATA", avalue);

                    final int lastattr = attributes.getLength() - 1;
                    attributes.setSpecified(lastattr, true);
                    return true;
                }
                final char quote = (char) c;
                boolean isStart = true;
                boolean prevSpace = false;
                do {
                    final boolean acceptSpace = !fNormalizeAttributes_ || (!isStart && !prevSpace);
                    c = fCurrentEntity.read();
                    if (c == -1) {
                        if (fReportErrors_) {
                            fErrorReporter.reportError("HTML1007", null);
                        }
                        throw new EOFException();
                    }
                    if (c == '&') {
                        isStart = false;
                        final int ce = scanEntityRef(fStringBuffer2, false);
                        if (ce != -1) {
                            appendChar(fStringBuffer, ce, null);
                        }
                        else {
                            fStringBuffer.append(fStringBuffer2);
                        }
                    }
                    else if (c == ' ' || c == '\t') {
                        if (acceptSpace) {
                            fStringBuffer.append(fNormalizeAttributes_ ? ' ' : (char) c);
                        }
                    }
                    else if (c == '\r' || c == '\n') {
                        if (c == '\r') {
                            final int c2 = fCurrentEntity.read();
                            if (c2 == '\n') {
                                c = c2;
                            }
                            else if (c2 != -1) {
                                fCurrentEntity.rewind();
                            }
                        }
                        if (acceptSpace) {
                            fStringBuffer.append(fNormalizeAttributes_ ? ' ' : '\n');
                        }
                        fCurrentEntity.incLine();
                    }
                    else if (c != quote) {
                        isStart = false;
                        appendChar(fStringBuffer, c, null);
                    }
                    prevSpace = c == ' ' || c == '\t' || c == '\r' || c == '\n';
                    isStart = isStart && prevSpace;
                }
                while (c != quote);

                if (fNormalizeAttributes_ && fStringBuffer.length() > 0) {
                    // trailing whitespace already normalized to single space
                    fStringBuffer.trimWhitespaceAtEnd();
                }

                qName_.setValues(null, aname, aname, null);
                final String avalue = fStringBuffer.toString();
                attributes.addAttribute(qName_, "CDATA", avalue);

                final int lastattr = attributes.getLength() - 1;
                attributes.setSpecified(lastattr, true);
            }
            else {
                qName_.setValues(null, aname, aname, null);
                attributes.addAttribute(qName_, "CDATA", "");
                attributes.setSpecified(attributes.getLength() - 1, true);
                fCurrentEntity.rewind();
            }
            return true;
        }

        // Scans an end element.
        protected void scanEndElement() throws IOException {
            String ename = scanName(true);
            if (fReportErrors_ && ename == null) {
                fErrorReporter.reportError("HTML1012", null);
            }
            skipMarkup(false);
            if (ename != null) {
                ename = modifyName(ename, fNamesElems);
                if (fDocumentHandler != null && fElementCount >= fElementDepth) {
                    qName_.setValues(null, ename, ename, null);
                    if (DEBUG_CALLBACKS) {
                        System.out.println("endElement(" + qName_ + ")");
                    }
                    fEndLineNumber = fCurrentEntity.getLineNumber();
                    fEndColumnNumber = fCurrentEntity.getColumnNumber();
                    fEndCharacterOffset = fCurrentEntity.getCharacterOffset();
                    fDocumentHandler.endElement(qName_, locationAugs());
                }
            }
        }

        /**
         * Returns true if the given element has an end-tag.
         */
        private boolean isEnded(final String ename) {
            final String content = new String(fCurrentEntity.buffer_, fCurrentEntity.offset_,
                    fCurrentEntity.length_ - fCurrentEntity.offset_);
            return content.toLowerCase(Locale.ROOT).contains("</" + ename.toLowerCase(Locale.ROOT) + ">");
        }
    }

    /**
     * Special scanner used for elements whose content needs to be scanned as plain
     * text, ignoring markup such as elements and entity references. For example:
     * &lt;SCRIPT&gt; and &lt;COMMENT&gt;.
     *
     * @author Andy Clark
     */
    public class SpecialScanner implements Scanner {

        //
        // Data
        //

        /** Name of element whose content needs to be scanned as text. */
        protected String fElementName;

        /** True if &lt;style&gt; element. */
        protected boolean fStyle;

        /** True if &lt;textarea&gt; element. */
        protected boolean fTextarea;

        /** True if &lt;title&gt; element. */
        protected boolean fTitle;

        // temp vars

        /** A qualified name. */
        private final QName fQName_ = new QName();

        /** A string buffer. */
        private final XMLString xmlString_ = new XMLString();

        //
        // Public methods
        //

        // Sets the element name.
        public Scanner setElementName(final String ename) {
            fElementName = ename;
            fStyle = "STYLE".equalsIgnoreCase(fElementName);
            fTextarea = "TEXTAREA".equalsIgnoreCase(fElementName);
            fTitle = "TITLE".equalsIgnoreCase(fElementName);
            return this;
        }

        //
        // Scanner methods
        //

        /** Scan. */
        @Override
        public boolean scan(final boolean complete) throws IOException {
            boolean next;
            do {
                try {
                    next = false;
                    switch (fScannerState) {
                        case STATE_CONTENT: {
                            fBeginLineNumber = fCurrentEntity.getLineNumber();
                            fBeginColumnNumber = fCurrentEntity.getColumnNumber();
                            fBeginCharacterOffset = fCurrentEntity.getCharacterOffset();
                            final int c = fCurrentEntity.read();
                            if (c == -1) {
                                if (fReportErrors_) {
                                    fErrorReporter.reportError("HTML1007", null);
                                }
                                throw new EOFException();
                            }
                            if (c == '<') {
                                setScannerState(STATE_MARKUP_BRACKET);
                                continue;
                            }
                            if (c == '&') {
                                if (fTextarea || fTitle) {
                                    scanEntityRef(xmlString_, true);
                                    continue;
                                }
                                xmlString_.clear();
                                xmlString_.append('&');
                            }
                            else {
                                fCurrentEntity.rewind();
                                xmlString_.clear();
                            }
                            scanCharacters(xmlString_, -1);
                            break;
                        }
                        case STATE_MARKUP_BRACKET: {
                            final int delimiter = -1;
                            final int c = fCurrentEntity.read();
                            if (c == '/') {
                                String ename = scanName(true);
                                if (ename != null) {
                                    if (ename.equalsIgnoreCase(fElementName)) {
                                        if (fCurrentEntity.read() == '>') {
                                            ename = modifyName(ename, fNamesElems);
                                            if (fDocumentHandler != null && fElementCount >= fElementDepth) {
                                                fQName_.setValues(null, ename, ename, null);
                                                if (DEBUG_CALLBACKS) {
                                                    System.out.println("endElement(" + fQName_ + ")");
                                                }
                                                fEndLineNumber = fCurrentEntity.getLineNumber();
                                                fEndColumnNumber = fCurrentEntity.getColumnNumber();
                                                fEndCharacterOffset = fCurrentEntity.getCharacterOffset();
                                                fDocumentHandler.endElement(fQName_, locationAugs());
                                            }
                                            setScanner(fContentScanner);
                                            setScannerState(STATE_CONTENT);
                                            return true;
                                        }
                                        fCurrentEntity.rewind();
                                    }
                                    xmlString_.clear();
                                    xmlString_.append("</");
                                    xmlString_.append(ename);
                                }
                                else {
                                    xmlString_.clear();
                                    xmlString_.append("</");
                                }
                            }
                            else {
                                xmlString_.clear();
                                xmlString_.append('<');
                                appendChar(xmlString_, c, null);
                            }
                            scanCharacters(xmlString_, delimiter);
                            setScannerState(STATE_CONTENT);
                            break;
                        }
                    }
                }
                catch (final EOFException e) {
                    setScanner(fContentScanner);
                    if (fCurrentEntityStack.empty()) {
                        setScannerState(STATE_END_DOCUMENT);
                    }
                    else {
                        fCurrentEntity = fCurrentEntityStack.pop();
                        setScannerState(STATE_CONTENT);
                    }
                    return true;
                }
            }
            while (next || complete);
            return true;
        }

        // Scan characters.
        protected void scanCharacters(final XMLString buffer, final int delimiter) throws IOException {
            if (DEBUG_BUFFER) {
                fCurrentEntity.debugBufferIfNeeded("(scanCharacters, delimiter=" + delimiter + ": ");
            }

            while (true) {
                final int c = fCurrentEntity.read();

                if (c == -1 || (c == '<' || c == '&')) {
                    if (c != -1) {
                        fCurrentEntity.rewind();
                    }
                    break;
                }
                // Patch supplied by Jonathan Baxter
                else if (c == '\r' || c == '\n') {
                    fCurrentEntity.rewind();
                    final int newlines = skipNewlines();
                    for (int i = 0; i < newlines; i++) {
                        buffer.append('\n');
                    }
                }
                else {
                    appendChar(buffer, c, null);
                }
            }

            if (fStyle) {
                if (fStyleStripCommentDelims_) {
                    buffer.reduceToContent("<!--", "-->");
                }
                if (fStyleStripCDATADelims_) {
                    buffer.reduceToContent("<![CDATA[", "]]>");
                }
            }

            if (buffer.length() > 0 && fDocumentHandler != null && fElementCount >= fElementDepth) {
                if (DEBUG_CALLBACKS) {
                    System.out.println("characters(" + buffer + ")");
                }
                fEndLineNumber = fCurrentEntity.getLineNumber();
                fEndColumnNumber = fCurrentEntity.getColumnNumber();
                fEndCharacterOffset = fCurrentEntity.getCharacterOffset();
                fDocumentHandler.characters(buffer, locationAugs());
            }
            if (DEBUG_BUFFER) {
                fCurrentEntity.debugBufferIfNeeded(")scanCharacters: ");
            }
        }
    }

    /**
     * Special scanner used for {@code PLAINTEXT}
     */
    public class PlainTextScanner implements Scanner {

        /** A string buffer. */
        private final XMLString xmlString_ = new XMLString();

        @Override
        public boolean scan(final boolean complete) throws IOException {
            scanCharacters(xmlString_);
            return false;
        }

        protected void scanCharacters(final XMLString buffer) throws IOException {
            while (true) {
                final int c = fCurrentEntity.read();

                if (c == -1) {
                    break;
                }
                appendChar(buffer, c, null);
                if (c == '\n') {
                    fCurrentEntity.incLine();
                }
            }

            if (buffer.length() > 0 && fDocumentHandler != null && fElementCount >= fElementDepth) {
                if (DEBUG_CALLBACKS) {
                    System.out.println("characters(" + buffer + ")");
                }
                fEndLineNumber = fCurrentEntity.getLineNumber();
                fEndColumnNumber = fCurrentEntity.getColumnNumber();
                fEndCharacterOffset = fCurrentEntity.getCharacterOffset();
                fDocumentHandler.characters(buffer, locationAugs());
                fDocumentHandler.endDocument(locationAugs());
            }
            if (DEBUG_BUFFER) {
                fCurrentEntity.debugBufferIfNeeded(")scanCharacters: ");
            }
        }
    }

    /**
     * A playback input stream. This class has the ability to save the bytes read
     * from the underlying input stream and play the bytes back later. This class is
     * used by the HTML scanner to switch encodings when a &lt;meta&gt; tag is
     * detected that specifies a different encoding.
     * <p>
     * If the encoding is changed, then the scanner calls the <code>playback</code>
     * method and re-scans the beginning of the HTML document again. This should not
     * be too much of a performance problem because the &lt;meta&gt; tag appears at
     * the beginning of the document.
     * <p>
     * If the &lt;body&gt; tag is reached without playing back the bytes, then the
     * buffer can be cleared by calling the <code>clear</code> method. This stops
     * the buffering of bytes and allows the memory used by the buffer to be
     * reclaimed.
     * <p>
     * <strong>Note:</strong> If the buffer is never played back or cleared, this
     * input stream will continue to buffer the entire stream. Therefore, it is very
     * important to use this stream correctly.
     *
     * @author Andy Clark
     */
    private static final class PlaybackInputStream extends FilterInputStream {

        /** Set to true to debug playback. */
        private static final boolean DEBUG_PLAYBACK = false;

        /** Playback mode. */
        private boolean playback_ = false;

        /** Buffer cleared. */
        private boolean cleared_ = false;

        /** Encoding detected. */
        private boolean detected_ = false;

        // buffer info

        /** Byte buffer. */
        private byte[] byteBuffer_ = new byte[1024];

        /** Offset into byte buffer during playback. */
        private int byteOffset_ = 0;

        /** Length of bytes read into byte buffer. */
        private int byteLength_ = 0;

        /** Pushback offset. */
        private int pushbackOffset_ = 0;

        /** Pushback length. */
        private int pushbackLength_ = 0;

        // Constructor.
        PlaybackInputStream(final InputStream in) {
            super(in);
        }

        // Detect encoding.
        public void detectEncoding(final String[] encodings) throws IOException {
            if (detected_) {
                throw new IOException("Should not detect encoding twice.");
            }
            detected_ = true;
            final int b1 = read();
            if (b1 == -1) {
                return;
            }
            final int b2 = read();
            if (b2 == -1) {
                pushbackLength_ = 1;
                return;
            }
            // UTF-8 BOM: 0xEFBBBF
            if (b1 == 0xEF && b2 == 0xBB) {
                final int b3 = read();
                if (b3 == 0xBF) {
                    pushbackOffset_ = 3;
                    encodings[0] = "UTF-8";
                    encodings[1] = "UTF8";
                    return;
                }
                pushbackLength_ = 3;
            }
            // UTF-16 LE BOM: 0xFFFE
            if (b1 == 0xFF && b2 == 0xFE) {
                encodings[0] = "UTF-16";
                encodings[1] = "UnicodeLittleUnmarked";
                return;
            }
            // UTF-16 BE BOM: 0xFEFF
            else if (b1 == 0xFE && b2 == 0xFF) {
                encodings[0] = "UTF-16";
                encodings[1] = "UnicodeBigUnmarked";
                return;
            }
            // unknown
            pushbackLength_ = 2;
        }

        /** Playback buffer contents. */
        public void playback() {
            playback_ = true;
        }

        /**
         * Clears the buffer.
         * <p>
         * <strong>Note:</strong> The buffer cannot be cleared during playback.
         * Therefore, calling this method during playback will not do anything. However,
         * the buffer will be cleared automatically at the end of playback.
         */
        public void clear() {
            if (!playback_) {
                cleared_ = true;
                byteBuffer_ = null;
            }
        }

        //
        // InputStream methods
        //

        /** Read a byte. */
        @Override
        public int read() throws IOException {
            if (DEBUG_PLAYBACK) {
                System.out.println("(read");
            }
            if (pushbackOffset_ < pushbackLength_) {
                return byteBuffer_[pushbackOffset_++];
            }
            if (cleared_) {
                return in.read();
            }
            if (playback_) {
                final int c = byteBuffer_[byteOffset_++];
                if (byteOffset_ == byteLength_) {
                    cleared_ = true;
                    byteBuffer_ = null;
                }
                if (DEBUG_PLAYBACK) {
                    System.out.println(")read -> " + (char) c);
                }
                return c;
            }
            final int c = in.read();
            if (c != -1) {
                if (byteLength_ == byteBuffer_.length) {
                    final byte[] newarray = new byte[byteLength_ + 1024];
                    System.arraycopy(byteBuffer_, 0, newarray, 0, byteLength_);
                    byteBuffer_ = newarray;
                }
                byteBuffer_[byteLength_++] = (byte) c;
            }
            if (DEBUG_PLAYBACK) {
                System.out.println(")read -> " + (char) c);
            }
            return c;
        }

        /** Read an array of bytes. */
        @Override
        public int read(final byte[] array) throws IOException {
            return read(array, 0, array.length);
        }

        /** Read an array of bytes. */
        @Override
        public int read(final byte[] array, final int offset, int length) throws IOException {
            if (DEBUG_PLAYBACK) {
                System.out.println(")read(" + offset + ',' + length + ')');
            }
            if (pushbackOffset_ < pushbackLength_) {
                int count = pushbackLength_ - pushbackOffset_;
                if (count > length) {
                    count = length;
                }
                System.arraycopy(byteBuffer_, pushbackOffset_, array, offset, count);
                pushbackOffset_ += count;
                return count;
            }
            if (cleared_) {
                return in.read(array, offset, length);
            }
            if (playback_) {
                if (byteOffset_ + length > byteLength_) {
                    length = byteLength_ - byteOffset_;
                }
                System.arraycopy(byteBuffer_, byteOffset_, array, offset, length);
                byteOffset_ += length;
                if (byteOffset_ == byteLength_) {
                    cleared_ = true;
                    byteBuffer_ = null;
                }
                return length;
            }
            final int count = in.read(array, offset, length);
            if (count != -1) {
                if (byteLength_ + count > byteBuffer_.length) {
                    final byte[] newarray = new byte[byteLength_ + count + 512];
                    System.arraycopy(byteBuffer_, 0, newarray, 0, byteLength_);
                    byteBuffer_ = newarray;
                }
                System.arraycopy(array, offset, byteBuffer_, byteLength_, count);
                byteLength_ += count;
            }
            if (DEBUG_PLAYBACK) {
                System.out.println(")read(" + offset + ',' + length + ") -> " + count);
            }
            return count;
        }
    }

    /**
     * Location infoset item.
     *
     * @author Andy Clark
     */
    private static final class LocationItem implements HTMLEventInfo {

        /** Beginning line number. */
        private final int beginLineNumber_;

        /** Beginning column number. */
        private final int beginColumnNumber_;

        /** Beginning character offset. */
        private final int beginCharacterOffset_;

        /** Ending line number. */
        private final int endLineNumber_;

        /** Ending column number. */
        private final int endColumnNumber_;

        /** Ending character offset. */
        private final int endCharacterOffset_;

        LocationItem(final int beginLine, final int beginColumn, final int beginOffset, final int endLine, final int endColumn, final int endOffset) {
            beginLineNumber_ = beginLine;
            beginColumnNumber_ = beginColumn;
            beginCharacterOffset_ = beginOffset;
            endLineNumber_ = endLine;
            endColumnNumber_ = endColumn;
            endCharacterOffset_ = endOffset;
        }

        /**
         * @return the line number of the beginning of this event.
         */
        @Override
        public int getBeginLineNumber() {
            return beginLineNumber_;
        }

        /**
         * @return the column number of the beginning of this event.
         */
        @Override
        public int getBeginColumnNumber() {
            return beginColumnNumber_;
        }

        /**
         * @return the character offset of the beginning of this event.
         */
        @Override
        public int getBeginCharacterOffset() {
            return beginCharacterOffset_;
        }

        /**
         * @return the line number of the end of this event.
         */
        @Override
        public int getEndLineNumber() {
            return endLineNumber_;
        }

        /**
         * @return the column number of the end of this event.
         */
        @Override
        public int getEndColumnNumber() {
            return endColumnNumber_;
        }

        /**
         * @return the character offset of the end of this event.
         */
        @Override
        public int getEndCharacterOffset() {
            return endCharacterOffset_;
        }

        // other information

        /**
         * @return true if this corresponding event was synthesized.
         */
        @Override
        public boolean isSynthesized() {
            return false;
        }

        /**
         * @return a string representation of this object.
         */
        @Override
        public String toString() {
            final StringBuilder str = new StringBuilder();
            str.append(beginLineNumber_);
            str.append(':');
            str.append(beginColumnNumber_);
            str.append(':');
            str.append(beginCharacterOffset_);
            str.append(':');
            str.append(endLineNumber_);
            str.append(':');
            str.append(endColumnNumber_);
            str.append(':');
            str.append(endCharacterOffset_);
            return str.toString();
        }
    }

    /**
     * To detect if 2 encoding are compatible, both must be able to read the meta
     * tag specifying the new encoding. This means that the byte representation of
     * some minimal html markup must be the same in both encodings
     */
    boolean isEncodingCompatible(final String encoding1, final String encoding2) {
        try {
            try {
                return canRoundtrip(encoding1, encoding2);
            }
            catch (final UnsupportedOperationException e) {
                // if encoding1 only supports decode, we can test it the other way to only
                // decode with it
                try {
                    return canRoundtrip(encoding2, encoding1);
                }
                catch (final UnsupportedOperationException e1) {
                    // encoding2 only supports decode too. Time to give up.
                    return false;
                }
            }
        }
        catch (final UnsupportedEncodingException e) {
            return false;
        }
    }

    private static boolean canRoundtrip(final String encodeCharset, final String decodeCharset)
            throws UnsupportedEncodingException {
        final String reference = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html;charset=";
        final byte[] bytesEncoding1 = reference.getBytes(encodeCharset);
        final String referenceWithEncoding2 = new String(bytesEncoding1, decodeCharset);
        return reference.equals(referenceWithEncoding2);
    }

    // Reads a single character, preserving the old buffer content
    protected int readPreservingBufferContent() throws IOException {
        if (DEBUG_BUFFER) {
            fCurrentEntity.debugBufferIfNeeded("(read: ");
        }
        if (fCurrentEntity.offset_ == fCurrentEntity.length_) {
            if (fCurrentEntity.load(fCurrentEntity.length_) < 1) {
                if (DEBUG_BUFFER) {
                    System.out.println(")read: -> -1");
                }
                return -1;
            }
        }
        final char c = fCurrentEntity.getNextChar();
        if (DEBUG_BUFFER) {
            fCurrentEntity.debugBufferIfNeeded(")read: ", " -> " + c);
        }
        return c;
    }

    // Indicates if the end comment --> (or --!>) is available,
    // loading further data if needed, without to reset the buffer
    private boolean endCommentAvailable() throws IOException {
        int nbCaret = 0;
        final int originalOffset = fCurrentEntity.offset_;
        final int originalColumnNumber = fCurrentEntity.getColumnNumber();
        final int originalCharacterOffset = fCurrentEntity.getCharacterOffset();

        while (true) {
            final int c = readPreservingBufferContent();
            if (c == -1) {
                fCurrentEntity.restorePosition(originalOffset, originalColumnNumber, originalCharacterOffset);
                return false;
            }
            else if (c == '>' && nbCaret >= 2) {
                fCurrentEntity.restorePosition(originalOffset, originalColumnNumber, originalCharacterOffset);
                return true;
            }
            else if (c == '!' && nbCaret >= 2) {
                // ignore to support --!> also
                // maybe we have to emit a warning
            }
            else if (c == '-') {
                nbCaret++;
            }
            else {
                nbCaret = 0;
            }
        }
    }
}
