/*
 * Copyright (c) 2002-2009 Andy Clark, Marc Guillemot
 * Copyright (c) 2017-2025 Ronald Brill
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Locale;

import org.htmlunit.cyberneko.HTMLElements.Element;
import org.htmlunit.cyberneko.io.PlaybackInputStream;
import org.htmlunit.cyberneko.util.MiniStack;
import org.htmlunit.cyberneko.xerces.util.EncodingTranslator;
import org.htmlunit.cyberneko.xerces.util.NamespaceSupport;
import org.htmlunit.cyberneko.xerces.util.StandardEncodingTranslator;
import org.htmlunit.cyberneko.xerces.util.URI;
import org.htmlunit.cyberneko.xerces.util.XMLAttributesImpl;
import org.htmlunit.cyberneko.xerces.xni.Augmentations;
import org.htmlunit.cyberneko.xerces.xni.QName;
import org.htmlunit.cyberneko.xerces.xni.XMLAttributes;
import org.htmlunit.cyberneko.xerces.xni.XMLDocumentHandler;
import org.htmlunit.cyberneko.xerces.xni.XMLLocator;
import org.htmlunit.cyberneko.xerces.xni.XMLString;
import org.htmlunit.cyberneko.xerces.xni.XNIException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLComponentManager;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLConfigurationException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLDocumentSource;
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
 * <li>http://cyberneko.org/html/features/scanner/script/strip-cdata-delims
 * <li>http://cyberneko.org/html/features/scanner/script/strip-comment-delims
 * <li>http://cyberneko.org/html/features/scanner/style/strip-cdata-delims
 * <li>http://cyberneko.org/html/features/scanner/style/strip-comment-delims
 * <li>http://cyberneko.org/html/features/scanner/ignore-specified-charset
 * <li>http://cyberneko.org/html/features/scanner/cdata-sections
 * <li>http://cyberneko.org/html/features/scanner/cdata-early-closing
 * <li>http://cyberneko.org/html/features/override-doctype
 * <li>http://cyberneko.org/html/features/insert-doctype
 * <li>http://cyberneko.org/html/features/parse-noscript-content
 * <li>http://cyberneko.org/html/features/scanner/allow-selfclosing-iframe
 * <li>http://cyberneko.org/html/features/scanner/allow-selfclosing-tags
 * <li>http://cyberneko.org/html/features/scanner/normalize-attrs
 * <li>http://cyberneko.org/html/features/scanner/plain-attr-values
 * </ul>
 * <p>
 * This component recognizes the following properties:
 * <ul>
 * <li>http://cyberneko.org/html/properties/names/elems
 * <li>http://cyberneko.org/html/properties/names/attrs
 * <li>http://cyberneko.org/html/properties/default-encoding
 * <li>http://cyberneko.org/html/properties/error-reporter
 * <li>http://cyberneko.org/html/properties/encoding-translator
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
 * @author Ren&eacute; Schwietzke
 */
public class HTMLScanner implements XMLDocumentSource, XMLLocator, HTMLComponent {

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
    public static final String AUGMENTATIONS = "http://cyberneko.org/html/features/augmentations";

    /** Report errors. */
    public static final String REPORT_ERRORS = "http://cyberneko.org/html/features/report-errors";

    /**
     * Strip HTML comment delimiters ("&lt;!&minus;&minus;" and
     * "&minus;&minus;&gt;") from SCRIPT tag contents.
     */
    public static final String SCRIPT_STRIP_COMMENT_DELIMS
                                    = "http://cyberneko.org/html/features/scanner/script/strip-comment-delims";

    /**
     * Strip XHTML CDATA delimiters ("&lt;![CDATA[" and "]]&gt;") from SCRIPT tag
     * contents.
     */
    public static final String SCRIPT_STRIP_CDATA_DELIMS
                                    = "http://cyberneko.org/html/features/scanner/script/strip-cdata-delims";

    /**
     * Strip HTML comment delimiters ("&lt;!&minus;&minus;" and
     * "&minus;&minus;&gt;") from STYLE tag contents.
     */
    public static final String STYLE_STRIP_COMMENT_DELIMS
                                    = "http://cyberneko.org/html/features/scanner/style/strip-comment-delims";

    /**
     * Strip XHTML CDATA delimiters ("&lt;![CDATA[" and "]]&gt;") from STYLE tag
     * contents.
     */
    public static final String STYLE_STRIP_CDATA_DELIMS
                                    = "http://cyberneko.org/html/features/scanner/style/strip-cdata-delims";

    /**
     * Ignore specified charset found in the &lt;meta equiv='Content-Type'
     * content='text/html;charset='&hellip;'&gt; tag or in the &lt;?xml &hellip;
     * encoding='&hellip;'&gt; processing instruction.
     */
    public static final String IGNORE_SPECIFIED_CHARSET
                                    = "http://cyberneko.org/html/features/scanner/ignore-specified-charset";

    /** Whether CDATA sections (&lt;![CDATA[...]]>) are treated as proper XML CDATA sections or as HTML comments. */
    public static final String CDATA_SECTIONS = "http://cyberneko.org/html/features/scanner/cdata-sections";

    /** '>' closes the cdata section (see html spec). */
    public static final String CDATA_EARLY_CLOSING = "http://cyberneko.org/html/features/scanner/cdata-early-closing";

    /** Override doctype declaration public and system identifiers. */
    public static final String OVERRIDE_DOCTYPE = "http://cyberneko.org/html/features/override-doctype";

    /** Insert document type declaration. */
    public static final String INSERT_DOCTYPE = "http://cyberneko.org/html/features/insert-doctype";

    /** Parse &lt;noscript&gt;...&lt;/noscript&gt; content. */
    public static final String PARSE_NOSCRIPT_CONTENT = "http://cyberneko.org/html/features/parse-noscript-content";

    /** Allows self closing &lt;iframe/&gt; tag. */
    public static final String ALLOW_SELFCLOSING_IFRAME
                                    = "http://cyberneko.org/html/features/scanner/allow-selfclosing-iframe";

    /** Allows self closing &lt;script/&gt; tag. */
    public static final String ALLOW_SELFCLOSING_SCRIPT
                                    = "http://cyberneko.org/html/features/scanner/allow-selfclosing-script";

    /** Allows self closing tags e.g. &lt;div/&gt; (XHTML) */
    public static final String ALLOW_SELFCLOSING_TAGS
                                    = "http://cyberneko.org/html/features/scanner/allow-selfclosing-tags";

    /** Normalize attribute values. */
    public static final String NORMALIZE_ATTRIBUTES = "http://cyberneko.org/html/features/scanner/normalize-attrs";

    /** Store the plain attribute values also. */
    public static final String PLAIN_ATTRIBUTE_VALUES = "http://cyberneko.org/html/features/scanner/plain-attr-values";

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
        CDATA_EARLY_CLOSING,
        OVERRIDE_DOCTYPE,
        INSERT_DOCTYPE,
        NORMALIZE_ATTRIBUTES,
        PLAIN_ATTRIBUTE_VALUES,
        PARSE_NOSCRIPT_CONTENT,
        ALLOW_SELFCLOSING_IFRAME,
        ALLOW_SELFCLOSING_SCRIPT,
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
        Boolean.TRUE,
        Boolean.FALSE,
        Boolean.FALSE,
        Boolean.FALSE,
        Boolean.FALSE,
        Boolean.TRUE,
        Boolean.FALSE,
        Boolean.FALSE,
        Boolean.FALSE, };

    // properties

    /** Modify HTML element names: { "upper", "lower", "default" }. */
    public static final String NAMES_ELEMS = "http://cyberneko.org/html/properties/names/elems";

    /** Modify HTML attribute names: { "upper", "lower", "default" }. */
    public static final String NAMES_ATTRS = "http://cyberneko.org/html/properties/names/attrs";

    /** Default encoding. */
    public static final String DEFAULT_ENCODING = "http://cyberneko.org/html/properties/default-encoding";

    /** Error reporter. */
    public static final String ERROR_REPORTER = "http://cyberneko.org/html/properties/error-reporter";

    /** Encoding translator. */
    public static final String ENCODING_TRANSLATOR = "http://cyberneko.org/html/properties/encoding-translator";

    /** Doctype declaration public identifier. */
    public static final String DOCTYPE_PUBID = "http://cyberneko.org/html/properties/doctype/pubid";

    /** Doctype declaration system identifier. */
    public static final String DOCTYPE_SYSID = "http://cyberneko.org/html/properties/doctype/sysid";

    /** Reader buffer size. */
    public static final String READER_BUFFER_SIZE = "http://cyberneko.org/html/properties/reader-buffer-size";

    /** Recognized properties. */
    private static final String[] RECOGNIZED_PROPERTIES = {
        NAMES_ELEMS,
        NAMES_ATTRS,
        DEFAULT_ENCODING,
        ERROR_REPORTER,
        ENCODING_TRANSLATOR,
        DOCTYPE_PUBID,
        DOCTYPE_SYSID,
        READER_BUFFER_SIZE};

    /** Recognized properties defaults. */
    private static final Object[] RECOGNIZED_PROPERTIES_DEFAULTS = {
        null,
        null,
        "Windows-1252",
        null,
        StandardEncodingTranslator.INSTANCE,
        HTML_4_01_TRANSITIONAL_PUBID,
        HTML_4_01_TRANSITIONAL_SYSID,

        /* Default buffer size, 10 cache lines minus overhead
         * A smaller buffer creates less cache misses compared
         * to 2048 bytes or more.
         * Xerces org: (10 * 64) - 24
         */
        (10 * 64) - 24};

    // states

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

    // scan return codes

    /** Scan return code: operation completed normally. */
    private static final int SCAN_TRUE = 0;

    /** Scan return code: end of entity reached (EOF). */
    private static final int SCAN_EOF = 1;

    /** Scan return code: continue scanning (state transition). */
    private static final int SCAN_FALSE = 2;

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

    // features

    /** Augmentations. */
    private boolean fAugmentations_;

    /** Report errors. */
    boolean fReportErrors_;

    /** Strip CDATA delimiters from SCRIPT tags. */
    boolean fScriptStripCDATADelims_;

    /** Strip comment delimiters from SCRIPT tags. */
    boolean fScriptStripCommentDelims_;

    /** Strip CDATA delimiters from STYLE tags. */
    boolean fStyleStripCDATADelims_;

    /** Strip comment delimiters from STYLE tags. */
    boolean fStyleStripCommentDelims_;

    /** Ignore specified character set. */
    boolean fIgnoreSpecifiedCharset_;

    /** CDATA sections. */
    boolean fCDATASections_;

    /** CDATA early closing. */
    boolean fCDATAEarlyClosing_;

    /** Override doctype declaration public and system identifiers. */
    private boolean fOverrideDoctype_;

    /** Insert document type declaration. */
    boolean fInsertDoctype_;

    /** Normalize attribute values. */
    boolean fNormalizeAttributes_;

    /** Store the plain attribute values also. */
    boolean fPlainAttributeValues_;

    /** Parse noscript content. */
    boolean fParseNoScriptContent_;

    /** Allows self closing iframe tags. */
    boolean fAllowSelfclosingIframe_;

    /** Allows self closing script tags. */
    boolean fAllowSelfclosingScript_;

    /** Allows self closing tags. */
    boolean fAllowSelfclosingTags_;

    // properties

    /** Modify HTML element names. */
    protected short fNamesElems;

    /** Modify HTML attribute names. */
    protected short fNamesAttrs;

    /** Default encoding. */
    protected String fDefaultIANAEncoding;

    /** Error reporter. */
    protected HTMLErrorReporter fErrorReporter;

    /** Error reporter. */
    protected EncodingTranslator fEncodingTranslator;

    /** Doctype declaration public identifier. */
    protected String fDoctypePubid;

    /** Doctype declaration system identifier. */
    protected String fDoctypeSysid;

    private int fReaderBufferSize;

    // boundary locator information

    /** Beginning line number. */
    protected int fBeginLineNumber;

    /** Beginning column number. */
    protected int fBeginColumnNumber;

    /** Beginning character offset in the file. */
    protected int fBeginCharacterOffset;

    // state

    /** The playback byte stream. */
    protected PlaybackInputStream fByteStream;

    /** Current entity. */
    protected CurrentEntity fCurrentEntity;

    /** The current entity stack. */
    protected final MiniStack<CurrentEntity> fCurrentEntityStack = new MiniStack<>();

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

    /** if not empty this is the name of the tag, that requires the special scanner. */
    private String fFragmentSpecialScannerTag_;

    // scanners

    /** Content scanner. */
    protected Scanner fContentScanner = new ContentScanner();

    /**
     * Special scanner used for elements whose content needs to be scanned as plain
     * text, ignoring markup such as elements and entity references. For example:
     * &lt;SCRIPT&gt; and &lt;COMMENT&gt;.
     */
    protected final SpecialScanner fSpecialScanner = new SpecialScanner();

    /**
     * Special scanner used script tags.
     */
    protected final ScriptScanner fScriptScanner = new ScriptScanner();

    // temp vars

    /** String buffer. */
    protected final XMLString fStringBuffer = new XMLString();

    /** String buffer used when resolving entity refs. */
    final XMLString fStringBufferEntityRef = new XMLString();
    final XMLString fStringBufferPlainAttribValue = new XMLString();

    final XMLString fScanUntilEndTag = new XMLString();

    final XMLString fScanComment = new XMLString();

    private final XMLString fScanLiteral = new XMLString();

    /**
     * Reusable single-element boolean array used as an out-parameter.
     * <p>
     * Performance optimization: This array is reused across method calls to avoid
     * allocating a new single-element array on every invocation of methods like
     * {@code scanStartElement} and {@code scanAttribute}. The array is reset before
     * each use to ensure correctness.
     * <p>
     * Thread safety: Safe because scanner instances are single-threaded.
     */    final boolean[] fSingleBoolean = {false};

    final HTMLConfiguration htmlConfiguration_;

    /**
     * Our location item, to be reused because {@link Augmentations}
     * says so, so let's save on memory
     */
    private final LocationItem fLocationItem = new LocationItem();

    /**
     * Creates a new HTMLScanner with the given configuration
     *
     * @param htmlConfiguration the configuration to use
     */
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
        final String systemId = systemId(literalSystemId, baseSystemId);
        fCurrentEntity = new CurrentEntity(reader, fReaderBufferSize, encoding,
                                    publicId, baseSystemId, literalSystemId, systemId);
    }

    private Reader getReader(final XMLInputSource inputSource) {
        final Reader reader = inputSource.getCharacterStream();
        if (reader == null) {
            try {
                if (StandardEncodingTranslator.REPLACEMENT.equalsIgnoreCase(fJavaEncoding)) {
                    return new StringReader("\uFFFD");
                }
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
        final String systemId = systemId(literalSystemId, baseSystemId);
        fCurrentEntity = new CurrentEntity(reader, fReaderBufferSize, encoding,
                                            publicId, baseSystemId, literalSystemId, systemId);
        setScanner(fContentScanner);
        setScannerState(STATE_CONTENT);
        try {
            int scan;
            do {
                scan = fScanner.scan(false);
            }
            while (SCAN_TRUE == scan);
        }
        catch (final IOException e) {
            // ignore
        }
        // preserve the plaintext scanning process
        setScanner(fScanner instanceof PlainTextScanner ? new PlainTextScanner() : previousScanner);
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
    public String getSystemId() {
        return fCurrentEntity != null ? fCurrentEntity.systemId : null;
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
        fCDATAEarlyClosing_ = manager.getFeature(CDATA_EARLY_CLOSING);
        fOverrideDoctype_ = manager.getFeature(OVERRIDE_DOCTYPE);
        fInsertDoctype_ = manager.getFeature(INSERT_DOCTYPE);
        fNormalizeAttributes_ = manager.getFeature(NORMALIZE_ATTRIBUTES);
        fPlainAttributeValues_ = manager.getFeature(PLAIN_ATTRIBUTE_VALUES);
        fParseNoScriptContent_ = manager.getFeature(PARSE_NOSCRIPT_CONTENT);
        fAllowSelfclosingIframe_ = manager.getFeature(ALLOW_SELFCLOSING_IFRAME);
        fAllowSelfclosingScript_ =  manager.getFeature(ALLOW_SELFCLOSING_SCRIPT);
        fAllowSelfclosingTags_ = manager.getFeature(ALLOW_SELFCLOSING_TAGS);

        // get properties
        fNamesElems = getNamesValue(String.valueOf(manager.getProperty(NAMES_ELEMS)));
        fNamesAttrs = getNamesValue(String.valueOf(manager.getProperty(NAMES_ATTRS)));
        fDefaultIANAEncoding = String.valueOf(manager.getProperty(DEFAULT_ENCODING));
        fErrorReporter = (HTMLErrorReporter) manager.getProperty(ERROR_REPORTER);
        fEncodingTranslator = (EncodingTranslator) manager.getProperty(ENCODING_TRANSLATOR);
        fDoctypePubid = String.valueOf(manager.getProperty(DOCTYPE_PUBID));
        fDoctypeSysid = String.valueOf(manager.getProperty(DOCTYPE_SYSID));
        fReaderBufferSize = Integer.parseInt(String.valueOf(manager.getProperty(READER_BUFFER_SIZE)));

        final QName[] fragmentContextStack = (QName[]) manager.getProperty(HTMLTagBalancer.FRAGMENT_CONTEXT_STACK);
        if (fragmentContextStack != null) {
            final int length = fragmentContextStack.length;
            if (length > 0) {
                final QName lastQname = fragmentContextStack[length - 1];
                final String name = lastQname.getLocalpart();
                final String nameLC = name.toLowerCase(Locale.ROOT);
                final Element elem = htmlConfiguration_.getHtmlElements().getElementLC(nameLC, null);
                if (elem != null && elem.isSpecial()) {
                    fFragmentSpecialScannerTag_ = name;
                }
            }
        }
    }

    /** Sets a feature. */
    @Override
    public void setFeature(final String featureId, final boolean state) {
        if (featureId.equals(AUGMENTATIONS)) {
            fAugmentations_ = state;
        }
        else if (featureId.equals(REPORT_ERRORS)) {
            fReportErrors_ = state;
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
        else if (featureId.equals(CDATA_SECTIONS)) {
            fCDATASections_ = state;
        }
        else if (featureId.equals(CDATA_EARLY_CLOSING)) {
            fCDATAEarlyClosing_ = state;
        }
        else if (featureId.equals(OVERRIDE_DOCTYPE)) {
            fOverrideDoctype_ = state;
        }
        else if (featureId.equals(INSERT_DOCTYPE)) {
            fInsertDoctype_ = state;
        }
        else if (featureId.equals(NORMALIZE_ATTRIBUTES)) {
            fNormalizeAttributes_ = state;
        }
        else if (featureId.equals(PLAIN_ATTRIBUTE_VALUES)) {
            fPlainAttributeValues_ = state;
        }
        else if (featureId.equals(PARSE_NOSCRIPT_CONTENT)) {
            fParseNoScriptContent_ = state;
        }
        else if (featureId.equals(ALLOW_SELFCLOSING_IFRAME)) {
            fAllowSelfclosingIframe_ = state;
        }
        else if (featureId.equals(ALLOW_SELFCLOSING_SCRIPT)) {
            fAllowSelfclosingScript_ = state;
        }
        else if (featureId.equals(ALLOW_SELFCLOSING_TAGS)) {
            fAllowSelfclosingTags_ = state;
        }
    }

    /** Sets a property. */
    @Override
    public void setProperty(final String propertyId, final Object value) throws XMLConfigurationException {
        switch (propertyId) {
            case NAMES_ELEMS:
                fNamesElems = getNamesValue(String.valueOf(value));
                break;
            case NAMES_ATTRS:
                fNamesAttrs = getNamesValue(String.valueOf(value));
                break;
            case DEFAULT_ENCODING:
                fDefaultIANAEncoding = String.valueOf(value);
                break;
            case ERROR_REPORTER:
                fErrorReporter = (HTMLErrorReporter) value;
                break;
            case ENCODING_TRANSLATOR:
                fEncodingTranslator = (EncodingTranslator) value;
                break;
            case DOCTYPE_PUBID:
                fDoctypePubid = String.valueOf(value);
                break;
            case DOCTYPE_SYSID:
                fDoctypeSysid = String.valueOf(value);
                break;
            case READER_BUFFER_SIZE:
                fReaderBufferSize = Integer.parseInt(String.valueOf(value));
                break;
        }
    }

    /**
     * Sets the input source.
     *
     * @param source The input source.
     *
     * @throws IOException Thrown on i/o error.
     */
    public void setInputSource(final XMLInputSource source) throws IOException {
        // reset state
        fElementCount = 0;
        fElementDepth = -1;
        fByteStream = null;
        fCurrentEntityStack.clear();

        fBeginLineNumber = 1;
        fBeginColumnNumber = 1;
        fBeginCharacterOffset = 0;

        // reset encoding information
        fIANAEncoding = fDefaultIANAEncoding;
        fJavaEncoding = fIANAEncoding;

        // get location information
        final String encoding = source.getEncoding();
        final String publicId = source.getPublicId();
        final String baseSystemId = source.getBaseSystemId();
        final String literalSystemId = source.getSystemId();
        final String systemId = systemId(literalSystemId, baseSystemId);

        // open stream
        Reader reader = source.getCharacterStream();
        if (reader == null) {
            InputStream inputStream = source.getByteStream();
            if (inputStream == null) {
                final URL url = new URL(systemId);
                inputStream = url.openStream();
            }

            final String[] encodings = new String[2];
            fByteStream = new PlaybackInputStream(inputStream);
            // always call detectBomEncoding() to skip bom in case
            // we got an input stream with bom and an encoding
            fByteStream.detectBomEncoding(encodings);
            if (encoding != null) {
                encodings[0] = encoding;
                encodings[1] = null;
            }

            if (encodings[0] == null) {
                encodings[0] = fDefaultIANAEncoding;
                if (fReportErrors_) {
                    fErrorReporter.reportWarning("HTML1000", null);
                }
            }
            if (encodings[1] == null) {
                encodings[1] = fEncodingTranslator.encodingNameFromLabel(encodings[0]);
                if (encodings[1] == null
                        || (!StandardEncodingTranslator.REPLACEMENT.equalsIgnoreCase(encodings[1])
                                && !Charset.isSupported(encodings[1]))) {
                    encodings[1] = encodings[0];
                    if (fReportErrors_) {
                        fErrorReporter.reportWarning("HTML1001", new Object[] {encodings[0]});
                    }
                }
            }
            fIANAEncoding = encodings[0];
            fJavaEncoding = encodings[1];

            if (StandardEncodingTranslator.REPLACEMENT.equalsIgnoreCase(fJavaEncoding)) {
                reader = new StringReader("\uFFFD");
            }
            else {
                reader = new InputStreamReader(fByteStream, fJavaEncoding);
            }
        }
        fCurrentEntity = new CurrentEntity(reader, fReaderBufferSize, fIANAEncoding,
                                            publicId, baseSystemId, literalSystemId, systemId);

        // set scanner and state
        if (fFragmentSpecialScannerTag_ != null) {
            final String scannerTagLC = fFragmentSpecialScannerTag_.toLowerCase(Locale.ROOT);
            if ("script".equals(scannerTagLC)) {
                setScanner(fScriptScanner);
            }
            else if ("plaintext".equals(scannerTagLC)) {
                setScanner(new PlainTextScanner());
            }
            else {
                setScanner(fSpecialScanner.setElementName(fFragmentSpecialScannerTag_, scannerTagLC));
                setScannerState(STATE_CONTENT);
            }

        }
        else {
            setScanner(fContentScanner);
            setScannerState(STATE_START_DOCUMENT);
        }
    }

    /**
     * Scans a document.
     *
     * @param complete True if the scanner should scan the document completely,
     *                 pushing all events to the registered document handler. A
     *                 value of false indicates that the scanner should only
     *                 scan the next portion of the document and return. A scanner
     *                 instance is permitted to completely scan a document if it
     *                 does not support this "pull" scanning model.
     *
     * @return True if there is more to scan, false otherwise.
     * @throws IOException  Thrown on i/o error.
     * @throws XNIException on error.
     */
    public boolean scanDocument(final boolean complete) throws XNIException, IOException {
        do {
            final int scan = fScanner.scan(complete);
            if (SCAN_FALSE == scan) {
                return false;
            }
        }
        while (complete);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDocumentHandler(final XMLDocumentHandler handler) {
        if (handler == null) {
            throw new NullPointerException("HTMLScanner always requires a non null document handler");
        }
        fDocumentHandler = handler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLDocumentHandler getDocumentHandler() {
        return fDocumentHandler;
    }

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
    public static String systemId(final String systemId, final String baseSystemId) {

        // check for bad parameters id
        if (systemId == null || systemId.isEmpty()) {
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
            if (baseSystemId == null || baseSystemId.isEmpty() || baseSystemId.equals(systemId)) {

                String dir;
                try {
                    dir = fixURI(System.getProperty("user.dir"))
                            // deal with blanks in paths; maybe we have to do better uri encoding here
                            .replaceAll(" ", "%20");

                }
                catch (final SecurityException ignored) {
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
                    catch (final SecurityException ignored) {
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

    // Scans a DOCTYPE
    protected int scanDoctype() throws IOException {
        String root = null;
        String pubid = null;
        String sysid = null;

        if (fCurrentEntity.skipSpaces()) {
            root = scanName(true, fNamesElems);
            if (root == null) {
                if (fReportErrors_) {
                    fErrorReporter.reportError("HTML1014", null);
                }
            }
            if (fCurrentEntity.skipSpaces()) {
                if (fCurrentEntity.skip("PUBLIC")) {
                    fCurrentEntity.skipSpaces();
                    int scanLiteral = scanLiteral();
                    if (SCAN_EOF == scanLiteral) {
                        return SCAN_EOF;
                    }
                    if (SCAN_TRUE == scanLiteral) {
                        pubid = fScanLiteral.toString();
                    }
                    if (fCurrentEntity.skipSpaces()) {
                        scanLiteral = scanLiteral();
                        if (SCAN_EOF == scanLiteral) {
                            return SCAN_EOF;
                        }
                        if (SCAN_TRUE == scanLiteral) {
                            sysid = fScanLiteral.toString();
                        }
                    }
                }
                else if (fCurrentEntity.skip("SYSTEM")) {
                    fCurrentEntity.skipSpaces();
                    final int scanLiteral = scanLiteral();
                    if (SCAN_EOF == scanLiteral) {
                        return SCAN_EOF;
                    }
                    if (SCAN_TRUE == scanLiteral) {
                        sysid = fScanLiteral.toString();
                    }
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
                fCurrentEntity.skipMarkup(true);
                break;
            }
        }

        if (fOverrideDoctype_) {
            pubid = fDoctypePubid;
            sysid = fDoctypeSysid;
        }
        fDocumentHandler.doctypeDecl(root, pubid, sysid, locationAugs(fCurrentEntity));

        return c == -1 ? SCAN_EOF : SCAN_TRUE;
    }

    // Scans a quoted literal.
    protected int scanLiteral() throws IOException {
        final int quote = fCurrentEntity.read();

        if (quote == '"' || quote == '\'') {
            final XMLString str = fScanLiteral.clear();
            int c;
            while ((c = fCurrentEntity.read()) != -1) {
                if (c == quote) {
                    break;
                }
                if (c == '\n' || c == '\r') {
                    fCurrentEntity.rewind();
                    // NOTE: This collapses newlines to a single space.
                    // [Q] Is this the right thing to do here? -Ac
                    fCurrentEntity.skipNewlines();
                    str.append(' ');
                }
                else if (c == '<') {
                    fCurrentEntity.rewind();
                    break;
                }
                else {
                    if (!str.appendCodePoint(c)) {
                        if (fReportErrors_) {
                            fErrorReporter.reportError("HTML1005", new Object[] {"&#" + c + ';'});
                        }
                    }
                }
            }
            if (c == -1) {
                if (fReportErrors_) {
                    fErrorReporter.reportError("HTML1007", null);
                }
                return SCAN_EOF;
            }
            return SCAN_TRUE;
        }
        fCurrentEntity.rewind();
        return SCAN_FALSE;
    }

    // Scan a name.
    protected String scanName(final boolean strict, final short mode) throws IOException {
        if (DEBUG_BUFFER) {
            fCurrentEntity.debugBufferIfNeeded("(scanName: ");
        }
        if (fCurrentEntity.offset_ == fCurrentEntity.length_) {
            if (fCurrentEntity.loadWholeBuffer() == -1) {
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

                // this has been split up to cater to the needs of branch prediction
                if (strict && (!Character.isLetterOrDigit(c) && c != '-' && c != '.' && c != ':' && c != '_')) {
                    fCurrentEntity.rewind();
                    break;
                }
                // we check for the regular space first because isWhitespace is no inlineable and hence expensive
                // regular space should be the norm as well as newlines
                else if (!strict
                            && (c == ' '
                                    || c == '\n'
                                    || c == '='
                                    || c == '/'
                                    || c == '>'
                                    || Character.isWhitespace(c))) {
                    fCurrentEntity.rewind();
                    break;
                }

                if (NAMES_NO_CHANGE == mode) {
                    // nothing to do
                }
                else if (NAMES_UPPERCASE == mode && !Character.isUpperCase(c)) {
                    fCurrentEntity.buffer_[fCurrentEntity.offset_ - 1] = Character.toUpperCase(c);
                }
                else if (NAMES_LOWERCASE == mode && !Character.isLowerCase(c)) {
                    fCurrentEntity.buffer_[fCurrentEntity.offset_ - 1] = Character.toLowerCase(c);
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

    // Scan a tag name.
    protected String scanTagName() throws IOException {
        if (DEBUG_BUFFER) {
            fCurrentEntity.debugBufferIfNeeded("(scanTagName: ");
        }
        if (fCurrentEntity.offset_ == fCurrentEntity.length_) {
            if (fCurrentEntity.loadWholeBuffer() == -1) {
                if (DEBUG_BUFFER) {
                    fCurrentEntity.debugBufferIfNeeded(")scanTagName: ");
                }
                return null;
            }
        }
        int offset = fCurrentEntity.offset_;
        boolean isFirst = true;

        while (true) {
            while (fCurrentEntity.hasNext()) {
                final char c = fCurrentEntity.getNextChar();

                if (isFirst) {
                    isFirst = false;

                    // first char has to be ASCII alpha
                    if (!('A' <= c && c <= 'Z' || 'a' <= c && c <= 'z')) {
                        fCurrentEntity.rewind();
                        break;
                    }
                }
                else {
                    if (c == '\t' || c == '\r' || c == '\n' || c == ' ' || c == 0
                            || c == '/' || c == '>') {
                        fCurrentEntity.rewind();
                        break;
                    }
                }

                if (NAMES_NO_CHANGE == fNamesElems) {
                    // nothing to do
                }
                else if (NAMES_UPPERCASE == fNamesElems && !Character.isUpperCase(c)) {
                    fCurrentEntity.buffer_[fCurrentEntity.offset_ - 1] = Character.toUpperCase(c);
                }
                else if (NAMES_LOWERCASE == fNamesElems && !Character.isLowerCase(c)) {
                    fCurrentEntity.buffer_[fCurrentEntity.offset_ - 1] = Character.toLowerCase(c);
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
    protected int scanEntityRef(final XMLString str, final XMLString plainValue, final boolean content)
                        throws IOException {
        str.clearAndAppend('&');

        // use readPreservingBufferContent inside this method to be sure we can rewind

        int nextChar = fCurrentEntity.readPreservingBufferContent();
        if (nextChar == -1) {
            if (plainValue != null) {
                plainValue.append(str);
            }
            return returnEntityRefString(str, content);
        }
        str.append((char) nextChar);

        if ('#' == nextChar) {
            final HTMLUnicodeEntitiesParser parser = new HTMLUnicodeEntitiesParser();

            do {
                nextChar = fCurrentEntity.readPreservingBufferContent();
                if (nextChar != -1) {
                    str.append((char) nextChar);
                }
            }
            while (nextChar != -1 && parser.parseNumeric(nextChar));

            final String match = parser.getMatch();
            if (match == null) {
                fCurrentEntity.rewind(str.length() - 1);
                if (plainValue != null) {
                    plainValue.append('&');
                }
                str.clearAndAppend('&');
            }
            else {
                fCurrentEntity.rewind(parser.getRewindCount());
                if (plainValue != null) {
                    plainValue.append(str);
                }
                str.clear().append(match);
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
            final HTMLNamedEntitiesParser.State intermediateResult
                        = HTMLNamedEntitiesParser.get().lookup(nextChar, result);

            if (intermediateResult.endNode_) {
                result = intermediateResult;
                break;
            }
            if (intermediateResult == result) {
                // nothing changed, more characters have not done anything
                break;
            }
            if (intermediateResult.isMatch_) {
                lastMatchingResult = intermediateResult;
            }
            result = intermediateResult;

            nextChar = fCurrentEntity.readPreservingBufferContent();
            if (nextChar != -1) {
                str.append((char) nextChar);
                readCount++;
            }
        }

        // it might happen that we read &lta but need just &lt so
        // we have to go back to the last match
        if (!result.isMatch_ && lastMatchingResult != null) {
            result = lastMatchingResult;
        }

        // hopefully, we got something, otherwise we have to go
        // the error route
        if (result.isMatch_) {
            // in case we overrun because the entity was broken or
            // not terminated by a ';', we have to reset the char
            // position because we read one more char than the entity has
            fCurrentEntity.rewind(readCount - result.length_);

            // if we have a correct character that is terminated by ';'
            // we can keep things simple
            if (result.endsWithSemicolon_) {
                if (plainValue != null) {
                    plainValue.append(str);
                }
                str.clear().append(result.resolvedValue_);
            }
            else {
                if (fReportErrors_) {
                    fErrorReporter.reportWarning("HTML1004", null);
                }

                // If there is a match
                // {
                //      If the character reference was consumed as part of an attribute,
                //      and the last character matched is not
                //      a U+003B SEMICOLON character (;), and the next input character
                //      is either a U+003D EQUALS SIGN character (=)
                //      or an ASCII alphanumeric,
                //      then, for historical reasons, flush code points consumed as
                //      a character reference and switch to the return state.

                //      Otherwise:
                //      1. If the last character matched is not a U+003B SEMICOLON character (;),
                //         then this is a missing-semicolon-after-character-reference parse error.
                //      2. Set the temporary buffer to the empty string. Append one or two characters
                //         corresponding to the character reference name
                //         (as given by the second column of the named character references table)
                //         to the temporary buffer.
                //      3. Flush code points consumed as a character reference. Switch to the return state.
                // }
                // Otherwise
                // {
                //      Flush code points consumed as a character reference. Switch to the ambiguous ampersand state.
                // }
                if (content) {
                    if (plainValue != null) {
                        plainValue.append(str);
                    }
                    str.clear().append(result.resolvedValue_);
                }
                else {
                    // look ahead
                    // 13.2.5.73
                    final int matchLength = result.length_ + 1;
                    if (matchLength < str.length()) {
                        nextChar = str.charAt(matchLength);
                        if ('=' == nextChar || '0' <= nextChar && nextChar <= '9' || 'A' <= nextChar && nextChar <= 'Z'
                                || 'a' <= nextChar && nextChar <= 'z') {
                            // we just shorten our temp str instead of copying stuff around
                            str.shortenBy(str.length() - result.length_ - 1);
                            if (plainValue != null) {
                                plainValue.append(str);
                            }
                        }
                        else {
                            if (plainValue != null) {
                                plainValue.append(str);
                            }
                            str.clear().append(result.resolvedValue_);
                        }
                    }
                    else {
                        if (plainValue != null) {
                            plainValue.append(str);
                        }
                        str.clear().append(result.resolvedValue_);
                    }
                }
            }
        }
        else {
            // Entity not found, rewind and continue
            // broken from here, aka keeping everything
            fCurrentEntity.rewind(readCount);
            if (plainValue != null) {
                plainValue.append('&');
            }
            str.clearAndAppend('&');
        }

        return returnEntityRefString(str, content);
    }

    private int returnEntityRefString(final XMLString str, final boolean content) {
        if (content && fElementCount >= fElementDepth) {
            fDocumentHandler.characters(str, locationAugs(fCurrentEntity));
        }
        return -1;
    }

    // infoset utility methods

    // Returns an augmentations object with a location item added.
    protected final Augmentations locationAugs(final CurrentEntity currentEntity) {
        // we don't have to create a new LocationItem all the time, because the interface says:
        // Methods that receive Augmentations are required to copy the information
        // if it is to be saved for use beyond the scope of the method.
        if (fAugmentations_) {
            fLocationItem.setValues(fBeginLineNumber, fBeginColumnNumber, fBeginCharacterOffset,
                    currentEntity.getLineNumber(),
                    currentEntity.getColumnNumber(),
                    currentEntity.getCharacterOffset());
            return fLocationItem;
        }
        return null;
    }

    // Returns an augmentations object with a synthesized item added.
    protected final Augmentations synthesizedAugs() {
        if (fAugmentations_) {
            return SynthesizedItem.INSTANCE;
        }
        return null;
    }

    /**
     * Basic scanner interface.
     */
    public interface Scanner {

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
        int scan(boolean complete) throws IOException;
    }

    /**
     * Current entity.
     */
    static final class CurrentEntity {

        /** Character stream. */
        private Reader reader_;

        /** Encoding. */
        String encoding_;

        /** Public identifier. */
        public final String publicId;

        /** Base system identifier. */
        public final String baseSystemId;

        /** Literal system identifier. */
        public final String literalSystemId;

        /** Expanded system identifier. */
        final String systemId;

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
        char[] buffer_;

        /** Offset into character buffer. */
        int offset_ = 0;

        /** Length of characters read into character buffer. */
        int length_ = 0;

        private boolean endReached_ = false;

        // Constructs an entity from the specified stream.
        CurrentEntity(final Reader reader, final int readerBufferSize, final String encoding,
                final String publicId, final String baseSystemId,
                final String literalSystemId, final String systemId) {
            reader_ = reader;
            buffer_ = new char[readerBufferSize];
            encoding_ = encoding;

            this.publicId = publicId;
            this.baseSystemId = baseSystemId;
            this.literalSystemId = literalSystemId;
            this.systemId = systemId;

        }

        char getCurrentChar() {
            return buffer_[offset_];
        }

        /**
         * @return the current character and moves to next one.
         */
        char getNextChar() {
            characterOffset_++;
            columnNumber_++;
            return buffer_[offset_++];
        }

        void closeQuietly() {
            try {
                reader_.close();
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
        int load(final int loadOffset) throws IOException {
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
            final int count = reader_.read(buffer_, loadOffset, buffer_.length - loadOffset);
            if (count == -1) {
                length_ = loadOffset;
                endReached_ = true;
            }
            else {
                length_ = count + loadOffset;
            }
            offset_ = loadOffset;
            if (DEBUG_BUFFER) {
                debugBufferIfNeeded(")load: ", " -> " + count);
            }
            return count;
        }

        int loadWholeBuffer() throws IOException {
            if (DEBUG_BUFFER) {
                debugBufferIfNeeded("(loadWholeBuffer: ");
            }
            // read a block of characters
            final int count = reader_.read(buffer_, 0, buffer_.length);
            if (count == -1) {
                length_ = 0;
                endReached_ = true;
            }
            else {
                length_ = count;
            }
            offset_ = 0;
            if (DEBUG_BUFFER) {
                debugBufferIfNeeded(")loadWholeBuffer: ", " -> " + count);
            }
            return count;
        }

        // Reads a single character.
        int read() throws IOException {
            if (DEBUG_BUFFER) {
                debugBufferIfNeeded("(read: ");
            }

            if (offset_ == length_) {
                if (endReached_) {
                    return -1;
                }
                if (loadWholeBuffer() == -1) {
                    if (DEBUG_BUFFER) {
                        System.out.println(")read: -> -1");
                    }
                    return -1;
                }
            }
            final char c = buffer_[offset_];
            offset_++;
            characterOffset_++;
            columnNumber_++;

            if (DEBUG_BUFFER) {
                debugBufferIfNeeded(")read: ", " -> " + c);
            }

            return c;
        }

        /**
         * Reads the next characters WITHOUT impacting the buffer content up to current
         * offset.
         *
         * @param len the number of characters to read
         * @return the read string (length may be smaller if EOF is encountered)
         * @throws IOException in case of io problems
         */
        String nextContent(final int len) throws IOException {
            final int originalOffset = offset_;
            final int originalColumnNumber = getColumnNumber();
            final int originalCharacterOffset = getCharacterOffset();

            final char[] buff = new char[len];
            int nbRead;
            for (nbRead = 0; nbRead < len; ++nbRead) {
                // load(length_) should not clear the buffer
                if (offset_ == length_) {
                    if (load(length_) == -1) {
                        break;
                    }
                }

                final int c = read();
                if (c == -1) {
                    break;
                }
                buff[nbRead] = (char) c;
            }

            // restore position
            offset_ = originalOffset;
            columnNumber_ = originalColumnNumber;
            characterOffset_ = originalCharacterOffset;

            return new String(buff, 0, nbRead);
        }

        // Reads a single character, preserving the old buffer content
        int readPreservingBufferContent() throws IOException {
            if (DEBUG_BUFFER) {
                debugBufferIfNeeded("(readPreserving: ");
            }
            if (offset_ == length_) {
                if (load(length_) == -1) {
                    if (DEBUG_BUFFER) {
                        System.out.println(")readPreserving: -> -1");
                    }
                    return -1;
                }
            }
            final char c = getNextChar();
            if (DEBUG_BUFFER) {
                debugBufferIfNeeded(")readPreserving: ", " -> " + c);
            }
            return c;
        }

        /** Prints the contents of the character buffer to standard out. */
        private void debugBufferIfNeeded(final String prefix) {
            debugBufferIfNeeded(prefix, "");
        }

        /** Prints the contents of the character buffer to standard out. */
        private void debugBufferIfNeeded(final String prefix, final String suffix) {
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

        void setStream(final Reader inputStreamReader, final String encoding) {
            reader_ = inputStreamReader;
            offset_ = 0;
            length_ = 0;
            characterOffset_ = 0;
            lineNumber_ = 1;
            columnNumber_ = 1;
            encoding_ = encoding;
        }

        /**
         * Goes back, canceling the effect of the previous read() call.
         */
        void rewind() {
            offset_--;
            characterOffset_--;
            columnNumber_--;
        }

        void rewind(final int i) {
            offset_ -= i;
            characterOffset_ -= i;
            columnNumber_ -= i;
        }

        void incLine() {
            lineNumber_++;
            columnNumber_ = 1;
        }

        void incLine(final int nbLines) {
            lineNumber_ += nbLines;
            columnNumber_ = 1;
        }

        public int getLineNumber() {
            return lineNumber_;
        }

        void resetBuffer(final XMLString xmlBuffer, final int lineNumber, final int columnNumber,
                final int characterOffset) {
            lineNumber_ = lineNumber;
            columnNumber_ = columnNumber;
            characterOffset_ = characterOffset;

            // TODO RBRi
            buffer_ = xmlBuffer.getChars();
            offset_ = 0;
            length_ = xmlBuffer.length();
        }

        int getColumnNumber() {
            return columnNumber_;
        }

        int getCharacterOffset() {
            return characterOffset_;
        }

        // Returns true if the specified text is present (case-insensitive) and is skipped.
        // for performance reasons you have to provide the specified text in uppercase
        boolean skip(final String expectedInUpperCase) throws IOException {
            final int length = expectedInUpperCase != null ? expectedInUpperCase.length() : 0;
            for (int i = 0; i < length; i++) {
                if (offset_ == length_) {
                    System.arraycopy(buffer_, offset_ - i, buffer_, 0, i);
                    if (load(i) == -1) {
                        offset_ = 0;
                        return false;
                    }
                }
                final char c0 = expectedInUpperCase.charAt(i);
                final char c1 = Character.toUpperCase(getNextChar());
                if (c0 != c1) {
                    rewind(i + 1);
                    return false;
                }
            }
            return true;
        }

        // Skips markup.
        boolean skipMarkup(final boolean balance) throws IOException {
            if (DEBUG_BUFFER) {
                debugBufferIfNeeded("(skipMarkup: ");
            }
            int depth = 1;
            boolean slashgt = false;
            OUTER: while (true) {
                if (offset_ == length_) {
                    if (loadWholeBuffer() == -1) {
                        break OUTER;
                    }
                }
                while (hasNext()) {
                    char c = getNextChar();
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
                        if (offset_ == length_) {
                            if (loadWholeBuffer() == -1) {
                                break OUTER;
                            }
                        }
                        c = getNextChar();
                        if (c == '>') {
                            slashgt = true;
                            depth--;
                            if (depth == 0) {
                                break OUTER;
                            }
                        }
                        else {
                            rewind();
                        }
                    }
                    else if (c == '\r' || c == '\n') {
                        rewind();
                        skipNewlines();
                    }
                }
            }
            if (DEBUG_BUFFER) {
                debugBufferIfNeeded(")skipMarkup: ", " -> " + slashgt);
            }
            return slashgt;
        }

        // Skips whitespace.
        boolean skipSpaces() throws IOException {
            if (DEBUG_BUFFER) {
                debugBufferIfNeeded("(skipSpaces: ");
            }
            boolean spaces = false;
            while (true) {
                if (offset_ == length_) {
                    if (loadWholeBuffer() == -1) {
                        break;
                    }
                }

                final char c = getNextChar();
                // compare against the usual suspects first before going
                // the expensive route
                // unix \n might dominate
                if (c == '\n' || c == '\r') {
                    spaces = true;
                    rewind();
                    skipNewlines();
                }
                else if (Character.isWhitespace(c)) {
                    spaces = true;
                }
                else {
                    rewind();
                    break;
                }
            }
            if (DEBUG_BUFFER) {
                debugBufferIfNeeded(")skipSpaces: ", " -> " + spaces);
            }
            return spaces;
        }

        // Skips newlines and returns the number of newlines skipped.
        int skipNewlines() throws IOException {
            if (DEBUG_BUFFER) {
                debugBufferIfNeeded("(skipNewlines: ");
            }

            if (offset_ == length_) {
                if (loadWholeBuffer() == -1) {
                    if (DEBUG_BUFFER) {
                        debugBufferIfNeeded(")skipNewlines: ");
                    }
                    return 0;
                }
            }
            char c = getCurrentChar();
            int newlines = 0;
            if (c == '\n' || c == '\r') {
                do {
                    c = getNextChar();
                    if (c == '\n') {
                        newlines++;
                        if (offset_ == length_) {
                            offset_ = newlines;
                            if (load(newlines) == -1) {
                                break;
                            }
                        }
                    }
                    else if (c == '\r') {
                        newlines++;
                        if (offset_ == length_) {
                            offset_ = newlines;
                            if (load(newlines) == -1) {
                                break;
                            }
                        }
                        if (getCurrentChar() == '\n') {
                            offset_++;
                            characterOffset_++;
                        }
                    }
                    else {
                        rewind();
                        break;
                    }
                }
                while (offset_ < length_ - 1);
                incLine(newlines);
            }
            if (DEBUG_BUFFER) {
                debugBufferIfNeeded(")skipNewlines: ", " -> " + newlines);
            }
            return newlines;
        }
    }

    /*
     * Script parsing states based on https://html.spec.whatwg.org/multipage/parsing.html#script-data-state
     */
    private enum ScanScriptState {
        /** Script data state */
        DATA,
        /** Script data escaped state */
        ESCAPED,
        /** Script data escaped less-than sign state */
        ESCAPED_LT,
        /** Script data double escaped state */
        DOUBLE_ESCAPED,
        /** Script data double escaped less-than sign state */
        DOUBLE_ESCAPED_LT,
    }

    /**
     * The primary HTML document scanner.
     */
    public class ContentScanner implements Scanner {

        /** A qualified name. */
        private final QName qName_ = new QName();

        /** Attributes. */
        private final XMLAttributesImpl attributes_ = new XMLAttributesImpl();
        private String scanStartElement_;

        /** Scan. */
        @Override
        public int scan(final boolean complete) throws IOException {
            boolean next;
            do {
                next = false;
                switch (fScannerState) {
                    case STATE_CONTENT: {
                        fBeginLineNumber = fCurrentEntity.getLineNumber();
                        fBeginColumnNumber = fCurrentEntity.getColumnNumber();
                        fBeginCharacterOffset = fCurrentEntity.getCharacterOffset();
                        final int c = fCurrentEntity.read();
                        if (c == -1) {
                            eof();
                            continue;
                        }
                        if (c == '<') {
                            setScannerState(STATE_MARKUP_BRACKET);
                            next = true;
                        }
                        else if (c == '&') {
                            scanEntityRef(fStringBuffer, null, true);
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
                            if (fElementCount >= fElementDepth) {
                                fStringBuffer.clearAndAppend('<');
                                fDocumentHandler.characters(fStringBuffer, null);
                            }
                            eof();
                            continue;
                        }
                        if (c == '!') {
                            // process some strange self closing comments first
                            if (fCurrentEntity.skip("--->")
                                    || fCurrentEntity.skip("-->")
                                    || fCurrentEntity.skip("->")
                                    || fCurrentEntity.skip(">")) {
                                // using EMPTY here is slightly dangerous but a review showed
                                // that all implementations of comment() only read the data
                                // never do anything else with it, so safe for now
                                fDocumentHandler.comment(XMLString.EMPTY, locationAugs(fCurrentEntity));
                            }
                            else if (fCurrentEntity.skip("-!>")) {
                                final XMLString str = new XMLString();
                                str.append("-!");
                                fDocumentHandler.comment(str, locationAugs(fCurrentEntity));
                            }
                            else if (fCurrentEntity.skip("--")) {
                                if (SCAN_EOF == scanComment()) {
                                    eof();
                                    continue;
                                }
                            }
                            else if (fCurrentEntity.skip("[CDATA[")) {
                                if (SCAN_EOF == scanCDATA()) {
                                    eof();
                                    continue;
                                }
                            }
                            else if (fCurrentEntity.skip("DOCTYPE")) {
                                if (SCAN_EOF == scanDoctype()) {
                                    eof();
                                    continue;
                                }
                            }
                            else {
                                if (fReportErrors_) {
                                    fErrorReporter.reportError("HTML1002", null);
                                }
                                fCurrentEntity.skipMarkup(true);
                            }
                        }
                        else if (c == '?') {
                            if (SCAN_EOF == scanPI()) {
                                eof();
                                continue;
                            }
                        }
                        else if (c == '/') {
                            scanEndElement();
                        }
                        else {
                            fCurrentEntity.rewind();
                            fElementCount++;
                            fSingleBoolean[0] = false;

                            final int scanStartElement = scanStartElement(fSingleBoolean);
                            if (SCAN_EOF == scanStartElement) {
                                eof();
                                continue;
                            }

                            final String ename;
                            final String enameLC;
                            if (SCAN_TRUE == scanStartElement) {
                                ename = scanStartElement_;
                                enameLC = ename.toLowerCase(Locale.ROOT);
                            }
                            else {
                                ename = null;
                                enameLC = null;
                            }

                            fBeginLineNumber = fCurrentEntity.getLineNumber();
                            fBeginColumnNumber = fCurrentEntity.getColumnNumber();
                            fBeginCharacterOffset = fCurrentEntity.getCharacterOffset();

                            if ("script".equals(enameLC)) {
                                if (!fAllowSelfclosingScript_) {
                                    setScanner(fScriptScanner);
                                    setScannerState(STATE_CONTENT);
                                    return SCAN_TRUE;
                                }
                            }
                            else if (!fAllowSelfclosingTags_
                                        && !fAllowSelfclosingIframe_
                                        && "iframe".equals(enameLC)) {
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
                            // title inside svg
                            else if ("title".equals(enameLC)
                                    && htmlConfiguration_.getTagBalancer().fOpenedSvg) {
                                setScannerState(STATE_CONTENT);
                            }
                            else if ("plaintext".equals(enameLC)) {
                                setScanner(new PlainTextScanner());
                            }
                            else if (ename != null) {
                                final Element elem =
                                        htmlConfiguration_.getHtmlElements().getElementLC(enameLC, null);
                                if (elem != null && elem.isSpecial()) {
                                    setScanner(fSpecialScanner.setElementName(ename, enameLC));
                                    setScannerState(STATE_CONTENT);
                                    return SCAN_TRUE;
                                }
                            }
                        }
                        setScannerState(STATE_CONTENT);
                        break;
                    }
                    case STATE_START_DOCUMENT: {
                        if (fElementCount >= fElementDepth) {
                            if (DEBUG_CALLBACKS) {
                                System.out.println("startDocument()");
                            }
                            fDocumentHandler.startDocument(HTMLScanner.this,
                                                            fIANAEncoding,
                                                            new NamespaceSupport(),
                                                            locationAugs(fCurrentEntity));
                        }
                        if (fInsertDoctype_) {
                            fDocumentHandler.doctypeDecl(
                                                NAMES_UPPERCASE == fNamesElems ? "HTML" : "html",
                                                fDoctypePubid,
                                                fDoctypeSysid,
                                                synthesizedAugs());
                        }
                        setScannerState(STATE_CONTENT);
                        break;
                    }
                    case STATE_END_DOCUMENT: {
                        if (fElementCount >= fElementDepth && complete) {
                            if (DEBUG_CALLBACKS) {
                                System.out.println("endDocument()");
                            }
                            fDocumentHandler.endDocument(locationAugs(fCurrentEntity));
                        }
                        return SCAN_FALSE;
                    }
                    default: {
                        throw new RuntimeException("unknown scanner state: " + fScannerState);
                    }
                }

                if (fScanner instanceof PlainTextScanner) {
                    return SCAN_TRUE;
                }
            }
            while (next || complete);
            return SCAN_TRUE;
        }

        private void eof() {
            if (fCurrentEntityStack.isEmpty()) {
                setScannerState(STATE_END_DOCUMENT);
            }
            else {
                fCurrentEntity = fCurrentEntityStack.pop();
            }
        }

        /**
         * Scans the content of &lt;noscript&gr;: it doesn't get parsed but is considered as
         * plain text when feature {@link HTMLScanner#PARSE_NOSCRIPT_CONTENT} is set to
         * false.
         *
         * @param tagName the tag for which content is scanned (one of "noscript",
         *                "noframes", "iframe")
         * @throws IOException on error
         */
        private void scanUntilEndTag(final String tagName) throws IOException {
            fScanUntilEndTag.clear();

            final String end = "/" + tagName;
            final int lengthToScan = tagName.length() + 2;

            while (true) {
                final int c = fCurrentEntity.read();
                if (c == -1) {
                    break;
                }
                if (c == '<') {
                    final String next = fCurrentEntity.nextContent(lengthToScan) + " ";
                    if (next.length() >= lengthToScan
                            && end.equalsIgnoreCase(next.substring(0, end.length()))
                            && ('>' == next.charAt(lengthToScan - 1)
                                    || Character.isWhitespace(next.charAt(lengthToScan - 1)))) {
                        fCurrentEntity.rewind();
                        break;
                    }
                }
                if (c == '\n' || c == '\r') {
                    fCurrentEntity.rewind();
                    final int newlines = fCurrentEntity.skipNewlines();
                    for (int i = 0; i < newlines; i++) {
                        fScanUntilEndTag.append('\n');
                    }
                }
                else {
                    if (!fScanUntilEndTag.appendCodePoint(c)) {
                        if (fReportErrors_) {
                            fErrorReporter.reportError("HTML1005", new Object[] {"&#" + c + ';'});
                        }
                    }
                }
            }
            if (fScanUntilEndTag.length() > 0) {
                fDocumentHandler.characters(fScanUntilEndTag, locationAugs(fCurrentEntity));
            }
        }

        // Scans characters.
        protected void scanCharacters() throws IOException {
            if (DEBUG_BUFFER) {
                fCurrentEntity.debugBufferIfNeeded("(scanCharacters: ");
            }
            fStringBuffer.clear();
            while (true) {
                final int newlines = fCurrentEntity.skipNewlines();
                if (newlines == 0 && fCurrentEntity.offset_ == fCurrentEntity.length_) {
                    if (DEBUG_BUFFER) {
                        fCurrentEntity.debugBufferIfNeeded(")scanCharacters: ");
                    }
                    break;
                }
                final int offset = fCurrentEntity.offset_ - newlines;
                for (int i = offset; i < fCurrentEntity.offset_; i++) {
                    fCurrentEntity.buffer_[i] = '\n';
                }
                while (fCurrentEntity.hasNext()) {
                    final char c = fCurrentEntity.getNextChar();
                    if (c == '<' || c == '&' || c == '\n' || c == '\r') {
                        fCurrentEntity.rewind();
                        break;
                    }
                }
                if (fCurrentEntity.offset_ > offset && fElementCount >= fElementDepth) {
                    if (DEBUG_CALLBACKS) {
                        final XMLString xmlString = new XMLString(fCurrentEntity.buffer_, offset,
                                fCurrentEntity.offset_ - offset);
                        System.out.println("characters(" + xmlString + ")");
                    }
                    fStringBuffer.append(fCurrentEntity.buffer_, offset, fCurrentEntity.offset_ - offset);
                }
                if (DEBUG_BUFFER) {
                    fCurrentEntity.debugBufferIfNeeded(")scanCharacters: ");
                }

                if (fCurrentEntity.offset_ < fCurrentEntity.buffer_.length) {
                    final int next = fCurrentEntity.getCurrentChar();
                    if (next == '&' || next == '<' || next == -1) {
                        break;
                    }
                }
                else {
                    break;
                }
            }

            if (fStringBuffer.length() != 0) {
                fDocumentHandler.characters(fStringBuffer, locationAugs(fCurrentEntity));
            }
        }

        // Scans a CDATA section
        protected int scanCDATA() throws IOException {
            if (DEBUG_BUFFER) {
                fCurrentEntity.debugBufferIfNeeded("(scanCDATA: ");
            }
            fStringBuffer.clear();
            if (fCDATASections_) {
                if (fElementCount >= fElementDepth) {
                    if (DEBUG_CALLBACKS) {
                        System.out.println("startCDATA()");
                    }
                    fDocumentHandler.startCDATA(locationAugs(fCurrentEntity));
                }
            }
            else {
                fStringBuffer.append("[CDATA[");
            }
            final int scanCData = scanCDataContent(fStringBuffer);

            if (fElementCount >= fElementDepth) {
                if (fCDATASections_) {
                    if (DEBUG_CALLBACKS) {
                        System.out.println("characters(" + fStringBuffer + ")");
                    }
                    fDocumentHandler.characters(fStringBuffer, locationAugs(fCurrentEntity));
                    if (DEBUG_CALLBACKS) {
                        System.out.println("endCDATA()");
                    }
                    fDocumentHandler.endCDATA(locationAugs(fCurrentEntity));
                }
                else {
                    if (DEBUG_CALLBACKS) {
                        System.out.println("comment(" + fStringBuffer + ")");
                    }
                    fDocumentHandler.comment(fStringBuffer, locationAugs(fCurrentEntity));
                }
            }
            if (DEBUG_BUFFER) {
                fCurrentEntity.debugBufferIfNeeded(")scanCDATA: ");
            }

            return scanCData;
        }

        // Scans a comment
        protected int scanComment() throws IOException {
            if (DEBUG_BUFFER) {
                fCurrentEntity.debugBufferIfNeeded("(scanComment: ");
            }

            fScanComment.clear();
            final int scanComment = scanCommentContent(fScanComment);
            if (fElementCount >= fElementDepth) {
                if (DEBUG_CALLBACKS) {
                    System.out.println("comment(" + fScanComment + ")");
                }
                fDocumentHandler.comment(fScanComment, locationAugs(fCurrentEntity));
            }
            if (DEBUG_BUFFER) {
                fCurrentEntity.debugBufferIfNeeded(")scanComment: ");
            }

            return scanComment;
        }

        // Scans markup content.
        protected int scanCommentContent(final XMLString buffer) throws IOException {
            int c;
            while (true) {
                c = fCurrentEntity.read();
                if (c == -1) {
                    if (fReportErrors_) {
                        fErrorReporter.reportError("HTML1007", null);
                    }
                    return SCAN_EOF;
                }

                if (c == '-') {
                    int count = 1;
                    while (true) {
                        c = fCurrentEntity.read();
                        if (c == '-') {
                            count++;
                            continue;
                        }
                        break;
                    }
                    if (c == -1) {
                        if (fReportErrors_) {
                            fErrorReporter.reportError("HTML1007", null);
                        }
                        break;
                    }
                    if (count < 2) {
                        buffer.append('-');
                        fCurrentEntity.rewind();
                        continue;
                    }

                    if (c == '!') {
                        c = fCurrentEntity.read();
                        if (c == -1) {
                            if (fReportErrors_) {
                                fErrorReporter.reportError("HTML1007", null);
                            }
                            return SCAN_EOF;
                        }

                        if (c == '>') {
                            for (int i = 0; i < count - 2; i++) {
                                buffer.append('-');
                            }
                            break;
                        }

                        for (int i = 0; i < count; i++) {
                            buffer.append('-');
                        }
                        buffer.append('!');
                        fCurrentEntity.rewind();
                        continue;
                    }

                    if (c == '>') {
                        for (int i = 0; i < count - 2; i++) {
                            buffer.append('-');
                        }
                        break;
                    }

                    for (int i = 0; i < count; i++) {
                        buffer.append('-');
                    }
                    fCurrentEntity.rewind();
                    continue;
                }
                else if (c == '\n' || c == '\r') {
                    fCurrentEntity.rewind();
                    final int newlines = fCurrentEntity.skipNewlines();
                    for (int i = 0; i < newlines; i++) {
                        buffer.append('\n');
                    }
                    continue;
                }
                if (!buffer.appendCodePoint(c)) {
                    if (fReportErrors_) {
                        fErrorReporter.reportError("HTML1005", new Object[] {"&#" + c + ';'});
                    }
                }
            }

            return c == -1 ? SCAN_EOF : SCAN_TRUE;
        }

        // Scans cdata content.
        protected int scanCDataContent(final XMLString xmlString) throws IOException {
            int c;
            while (true) {
                c = fCurrentEntity.read();
                if (c == -1) {
                    if (fReportErrors_) {
                        fErrorReporter.reportError("HTML1007", null);
                    }
                    break;
                }

                if (c == ']') {
                    int count = 1;
                    while (true) {
                        c = fCurrentEntity.read();
                        if (c == ']') {
                            count++;
                            continue;
                        }
                        break;
                    }
                    if (c == -1) {
                        if (fReportErrors_) {
                            fErrorReporter.reportError("HTML1007", null);
                        }
                        break;
                    }
                    if (count < 2) {
                        xmlString.append(']');
                        fCurrentEntity.rewind();
                        continue;
                    }
                    if (c != '>') {
                        for (int i = 0; i < count; i++) {
                            xmlString.append(']');
                        }
                        fCurrentEntity.rewind();
                        continue;
                    }
                    for (int i = 0; i < count - 2; i++) {
                        xmlString.append(']');
                    }
                    break;
                }
                else if (fCDATAEarlyClosing_ && c == '>') {
                    // don't add the ]] to the buffer
                    return SCAN_FALSE;
                }
                else if (c == '\n' || c == '\r') {
                    fCurrentEntity.rewind();
                    final int newlines = fCurrentEntity.skipNewlines();
                    for (int i = 0; i < newlines; i++) {
                        xmlString.append('\n');
                    }
                    continue;
                }
                if (!xmlString.appendCodePoint(c)) {
                    if (fReportErrors_) {
                        fErrorReporter.reportError("HTML1005", new Object[] {"&#" + c + ';'});
                    }
                }
            }

            if (!fCDATASections_) {
                fStringBuffer.append("]]");
            }

            return c == -1 ? SCAN_EOF : SCAN_FALSE;
        }

        // Scans a processing instruction.
        protected int scanPI() throws IOException {
            if (DEBUG_BUFFER) {
                fCurrentEntity.debugBufferIfNeeded("(scanPI: ");
            }
            if (fReportErrors_) {
                fErrorReporter.reportWarning("HTML1008", null);
            }

            // scan processing instruction
            final String target = scanName(true, fNamesElems);
            if (target != null && !"xml".equalsIgnoreCase(target)) {
                int c;
                while (true) {
                    c = fCurrentEntity.read();
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
                                fCurrentEntity.rewind();
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
                    c = fCurrentEntity.read();
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
                                fCurrentEntity.rewind();
                            }
                        }
                        fCurrentEntity.incLine();
                        continue;
                    }
                    else if (c == '>') {
                        // invalid procession instruction, handle as comment
                        fStringBuffer.append(target);
                        fDocumentHandler.comment(fStringBuffer, locationAugs(fCurrentEntity));
                        return SCAN_TRUE;
                    }
                    else {
                        if (!fStringBuffer.appendCodePoint(c)) {
                            if (fReportErrors_) {
                                fErrorReporter.reportError("HTML1005", new Object[] {"&#" + c + ';'});
                            }
                        }
                    }
                }
                fDocumentHandler.processingInstruction(target, fStringBuffer, locationAugs(fCurrentEntity));

                return c == -1 ? SCAN_EOF : SCAN_TRUE;
            }

            // scan xml/text declaration
            final int beginLineNumber = fBeginLineNumber;
            final int beginColumnNumber = fBeginColumnNumber;
            final int beginCharacterOffset = fBeginCharacterOffset;
            attributes_.removeAllAttributes();
            int aindex = 0;

            int scanAttribute = scanAttribute(attributes_, fSingleBoolean);
            while (SCAN_TRUE == scanAttribute) {
                // if we haven't scanned a value, remove the entry as values have special
                // signification
                if (attributes_.getValue(aindex).isEmpty()) {
                    attributes_.removeAttributeAt(aindex);
                }
                else {
                    attributes_.getName(aindex, qName_);
                    qName_.setRawname(qName_.getRawname().toLowerCase(Locale.ROOT));
                    attributes_.setName(aindex, qName_);
                    aindex++;
                }
                scanAttribute = scanAttribute(attributes_, fSingleBoolean);
            }

            if (SCAN_EOF == scanAttribute) {
                return SCAN_EOF;
            }

            final String version = attributes_.getValue("version");
            final String encoding = attributes_.getValue("encoding");
            final String standalone = attributes_.getValue("standalone");

            // if the encoding is successfully changed, the stream will be processed again
            // with the right encoding and we will come here again but without need to change
            // the encoding
            final boolean xmlDeclNow = fIgnoreSpecifiedCharset_ || !changeEncoding(encoding);
            if (xmlDeclNow) {
                fBeginLineNumber = beginLineNumber;
                fBeginColumnNumber = beginColumnNumber;
                fBeginCharacterOffset = beginCharacterOffset;
                fDocumentHandler.xmlDecl(version, encoding, standalone, locationAugs(fCurrentEntity));
            }

            if (DEBUG_BUFFER) {
                fCurrentEntity.debugBufferIfNeeded(")scanPI: ");
            }

            return SCAN_TRUE;
        }

        /**
         * Scans a start element.
         *
         * @param empty Is used for a second return value to indicate whether the start
         *              element tag is empty (e.g. "/&gt;").
         * @return ename
         * @throws IOException in case of io problems
         */
        protected int scanStartElement(final boolean[] empty) throws IOException {
            scanStartElement_ = scanTagName();
            final int length = scanStartElement_ != null ? scanStartElement_.length() : 0;
            if (length == 0) {
                if (fReportErrors_) {
                    fErrorReporter.reportError("HTML1009", null);
                }
                if (fElementCount >= fElementDepth) {
                    fStringBuffer.clearAndAppend('<');
                    fDocumentHandler.characters(fStringBuffer, null);
                }
                return SCAN_FALSE;
            }
            if (attributes_.getLength() != 0) {
                attributes_.removeAllAttributes();
            }
            final int beginLineNumber = fBeginLineNumber;
            final int beginColumnNumber = fBeginColumnNumber;
            final int beginCharacterOffset = fBeginCharacterOffset;

            int scanAttribute = scanAttribute(attributes_, empty);
            while (SCAN_TRUE == scanAttribute) {
                scanAttribute = scanAttribute(attributes_, empty);
            }

            if (SCAN_EOF == scanAttribute) {
                return SCAN_EOF;
            }

            fBeginLineNumber = beginLineNumber;
            fBeginColumnNumber = beginColumnNumber;
            fBeginCharacterOffset = beginCharacterOffset;
            if (fElementDepth == -1) {
                if (fByteStream != null) {
                    final String enameLC = scanStartElement_.toLowerCase(Locale.ROOT);

                    if (!fIgnoreSpecifiedCharset_ && "meta".equals(enameLC)) {
                        if (DEBUG_CHARSET) {
                            System.out.println("+++ <META>");
                        }
                        final String httpEquiv = getValue(attributes_, "http-equiv");
                        if ("content-type".equalsIgnoreCase(httpEquiv)) {
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
                    else if ("body".equals(enameLC)) {
                        fByteStream.clear();
                        fByteStream = null;
                    }
                    else {
                        final HTMLElements.Element element =
                                htmlConfiguration_.getHtmlElements().getElementLC(enameLC, null);
                        if (element == null // unknown has body as parent
                                || (element.parent != null
                                    && element.parent.length > 0
                                    && element.parent[0].code == HTMLElements.BODY)) {
                            fByteStream.clear();
                            fByteStream = null;
                        }
                    }
                }
            }

            if (fElementCount >= fElementDepth) {
                qName_.setValues(null, scanStartElement_, scanStartElement_, null);
                if (DEBUG_CALLBACKS) {
                    System.out.println("startElement(" + qName_ + ',' + attributes_ + ")");
                }
                if (empty[0] && !"br".equalsIgnoreCase(scanStartElement_)) {
                    fDocumentHandler.emptyElement(qName_, attributes_, locationAugs(fCurrentEntity));
                }
                else {
                    fDocumentHandler.startElement(qName_, attributes_, locationAugs(fCurrentEntity));
                }
            }
            return SCAN_TRUE;
        }

        /**
         * Removes all whitespaces from the string
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
                String javaEncoding = fEncodingTranslator.encodingNameFromLabel(charset);
                if (DEBUG_CHARSET) {
                    System.out.println("+++ ianaEncoding: " + charset);
                    System.out.println("+++ javaEncoding: " + javaEncoding);
                }
                if (javaEncoding == null
                        || (!StandardEncodingTranslator.REPLACEMENT.equalsIgnoreCase(javaEncoding)
                                && !Charset.isSupported(javaEncoding))) {
                    javaEncoding = charset;
                    if (fReportErrors_) {
                        fErrorReporter.reportError("HTML1001", new Object[] {charset});
                    }
                }

                if (!javaEncoding.equals(fJavaEncoding)) {
                    if (StandardEncodingTranslator.REPLACEMENT.equalsIgnoreCase(javaEncoding)) {
                        fJavaEncoding = javaEncoding;
                        // use a simple string reader to implement the charset
                        fCurrentEntity.setStream(new StringReader("\uFFFD"), javaEncoding);
                        fByteStream.playback();
                        // start from the beginning, without re-using the nodes already parsed
                        fElementDepth = 0;
                        fElementCount = 0;
                        encodingChanged = true;
                    }
                    else if (!isEncodingCompatible(javaEncoding, fJavaEncoding)) {
                        if (fReportErrors_) {
                            fErrorReporter.reportError("HTML1015", new Object[] {javaEncoding, fJavaEncoding});
                        }
                    }
                    // change the charset
                    else {
                        fJavaEncoding = javaEncoding;
                        fCurrentEntity.setStream(new InputStreamReader(fByteStream, javaEncoding), javaEncoding);
                        fByteStream.playback();
                        // skip the already parsed elements
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
        protected int scanAttribute(final XMLAttributesImpl attributes, final boolean[] empty) throws IOException {
            final boolean skippedSpaces = fCurrentEntity.skipSpaces();

            fBeginLineNumber = fCurrentEntity.getLineNumber();
            fBeginColumnNumber = fCurrentEntity.getColumnNumber();
            fBeginCharacterOffset = fCurrentEntity.getCharacterOffset();

            int c = fCurrentEntity.read();

            // try to jump over that at once
            if (c <= '>') {
                if (c == -1) {
                    if (fReportErrors_) {
                        fErrorReporter.reportError("HTML1007", null);
                    }
                    return SCAN_EOF;
                }
                if (c == '>') {
                    return SCAN_FALSE;
                }
                if (c == '/') {
                    if (fCurrentEntity.hasNext()) {
                        c = fCurrentEntity.getNextChar();
                        if (c == '>') {
                            empty[0] = true;
                            return SCAN_FALSE;
                        }
                        fCurrentEntity.rewind();
                    }
                }

                // https://html.spec.whatwg.org/multipage/parsing.html
                //                    #parse-error-unexpected-character-in-attribute-name
                if (c == '<') {
                    if (fReportErrors_) {
                        fErrorReporter.reportError("HTML1016", null);
                    }
                    // report but process as part of the attrib name
                }
            }

            fCurrentEntity.rewind();
            String aname = scanName(false, fNamesAttrs);
            if (aname == null) {
                if (fReportErrors_) {
                    fErrorReporter.reportError("HTML1011", null);
                }

                // check if the next char is '=' and handle this according to the spec
                fCurrentEntity.skipSpaces();
                if (!fCurrentEntity.hasNext() || '=' != fCurrentEntity.getNextChar()) {
                    fCurrentEntity.rewind();
                    empty[0] = fCurrentEntity.skipMarkup(false);
                    return SCAN_FALSE;
                }
                aname = '=' + scanName(false, fNamesElems);
            }
            if (fReportErrors_ && !skippedSpaces) {
                fErrorReporter.reportError("HTML1013", new Object[] {aname});
            }
            fCurrentEntity.skipSpaces();
            c = fCurrentEntity.read();
            if (c == -1) {
                if (fReportErrors_) {
                    fErrorReporter.reportError("HTML1007", null);
                }
                return SCAN_EOF;
            }
            if (c == '/') {
                qName_.setValues(null, aname, aname, null);
                attributes.addAttribute(qName_, "CDATA", "", true);
                return SCAN_TRUE;
            }
            if (c == '>') {
                qName_.setValues(null, aname, aname, null);
                attributes.addAttribute(qName_, "CDATA", "", true);
                return SCAN_FALSE;
            }
            if (c == '=') {
                fCurrentEntity.skipSpaces();
                c = fCurrentEntity.read();

                // check for the good case first, before we deal with error handling
                if (c == '"' || c == '\'') {
                    // scan the attribute value, basically the stuff between quotes
                    final XMLString attribValue = fStringBuffer.clear();

                    if (fPlainAttributeValues_) {
                        final XMLString plainAttribValue = fStringBufferPlainAttribValue.clear();
                        if (SCAN_EOF == scanAttributeQuotedValue(c, fCurrentEntity, attribValue,
                                                    plainAttribValue, fNormalizeAttributes_)) {
                            return SCAN_EOF;
                        }

                        if (fNormalizeAttributes_ && attribValue.length() > 0) {
                            // trailing whitespace already normalized to single space
                            attribValue.trimTrailing();
                        }

                        qName_.setValues(null, aname, aname, null);
                        attributes.addAttribute(qName_, "CDATA", attribValue.toString(),
                                                    plainAttribValue.toString(), true);
                    }
                    else {
                        if (SCAN_EOF == scanAttributeQuotedValue(c, fCurrentEntity,
                                                    attribValue, null, fNormalizeAttributes_)) {
                            return SCAN_EOF;
                        }

                        if (fNormalizeAttributes_ && attribValue.length() > 0) {
                            // trailing whitespace already normalized to single space
                            attribValue.trimTrailing();
                        }

                        qName_.setValues(null, aname, aname, null);
                        attributes.addAttribute(qName_, "CDATA", attribValue.toString(), true);
                    }

                    return SCAN_TRUE;
                }

                // no data?
                if (c == -1) {
                    if (fReportErrors_) {
                        fErrorReporter.reportError("HTML1007", null);
                    }
                    return SCAN_EOF;
                }

                // we don't have any quotes, deal with that

                // Xiaowei/Ac: Fix for <a href=/cgi-bin/myscript>...</a>
                if (c == '>') {
                    qName_.setValues(null, aname, aname, null);
                    attributes.addAttribute(qName_, "CDATA", "", true);
                    return SCAN_FALSE;
                }

                fCurrentEntity.rewind();

                final XMLString attribValue = fStringBuffer.clear();
                if (fPlainAttributeValues_) {
                    final XMLString nonNormalizedAttribValue = fStringBufferPlainAttribValue.clear();
                    if (SCAN_EOF == scanAttributeUnquotedValue(fCurrentEntity, attribValue, nonNormalizedAttribValue)) {
                        return SCAN_EOF;
                    }

                    qName_.setValues(null, aname, aname, null);
                    attributes.addAttribute(qName_, "CDATA",
                                                attribValue.toString(),
                                                nonNormalizedAttribValue.toString(), true);
                }
                else {
                    if (SCAN_EOF == scanAttributeUnquotedValue(fCurrentEntity, attribValue, null)) {
                        return SCAN_EOF;
                    }

                    qName_.setValues(null, aname, aname, null);
                    attributes.addAttribute(qName_, "CDATA", attribValue.toString(), true);
                }

                return SCAN_TRUE;
            }

            qName_.setValues(null, aname, aname, null);
            attributes.addAttribute(qName_, "CDATA", "", true);

            fCurrentEntity.rewind();
            return SCAN_TRUE;
        }

        protected int scanAttributeUnquotedValue(
                final CurrentEntity currentEntity,
                final XMLString attribValue,
                final XMLString plainAttribValue) throws IOException {
            while (true) {
                final int c = currentEntity.read();

                if (c == -1) {
                    if (fReportErrors_) {
                        fErrorReporter.reportError("HTML1007", null);
                    }
                    return SCAN_EOF;
                }

                // Xiaowei/Ac: Fix for <a href=/broken/>...</a>
                if (c == '>' || Character.isWhitespace(c)) {
                    // fCharOffset--;
                    currentEntity.rewind();
                    break;
                }

                if (c == '&') {
                    scanEntityRef(fStringBufferEntityRef, plainAttribValue, false);
                    attribValue.append(fStringBufferEntityRef);
                }
                else {
                    if (!attribValue.appendCodePoint(c)) {
                        if (fReportErrors_) {
                            fErrorReporter.reportError("HTML1005", new Object[] {"&#" + c + ';'});
                        }
                    }
                    if (plainAttribValue != null) {
                        plainAttribValue.appendCodePoint(c);
                    }
                }
            }
            return SCAN_TRUE;
        }

        protected int scanAttributeQuotedValue(
                final int currentQuote,
                final CurrentEntity currentEntity,
                final XMLString attribValue,
                final XMLString plainAttribValue,
                final boolean normalizeAttributes) throws IOException {

            boolean isStart = true;
            boolean prevSpace = false;

            while (true) {
                final boolean acceptSpace = !normalizeAttributes || (!isStart && !prevSpace);
                final int c = currentEntity.read();

                if (c == -1) {
                    if (fReportErrors_) {
                        fErrorReporter.reportError("HTML1007", null);
                    }
                    return SCAN_EOF;
                }
                if (c == ' ' || c == '\t') {
                    if (acceptSpace) {
                        attribValue.append(normalizeAttributes ? ' ' : (char) c);
                    }
                    if (plainAttribValue != null) {
                        plainAttribValue.append((char) c);
                    }
                    prevSpace = true;
                }
                else if (c == '\n') {
                    if (acceptSpace) {
                        attribValue.append(normalizeAttributes ? ' ' : '\n');
                    }
                    if (plainAttribValue != null) {
                        plainAttribValue.append('\n');
                    }
                    currentEntity.incLine();
                    prevSpace = true;
                }
                else if (c == '\r') {
                    final int c2 = currentEntity.read();
                    if (c2 == '\n') {
                        // c = c2;
                    }
                    else if (c2 != -1) {
                        currentEntity.rewind();
                    }
                    if (acceptSpace) {
                        attribValue.append(normalizeAttributes ? ' ' : '\n');
                    }
                    if (plainAttribValue != null) {
                        plainAttribValue.append('\n');
                    }
                    currentEntity.incLine();
                    prevSpace = true;
                }
                else if (c == '&') {
                    isStart = false;
                    final int ce = scanEntityRef(fStringBufferEntityRef, plainAttribValue, false);
                    if (ce != -1) {
                        if (!attribValue.appendCodePoint(ce)) {
                            if (fReportErrors_) {
                                fErrorReporter.reportError("HTML1005", new Object[] {"&#" + ce + ';'});
                            }
                        }
                    }
                    else {
                        attribValue.append(fStringBufferEntityRef);
                    }
                    prevSpace = false;
                }
                else if (c != currentQuote) {
                    isStart = false;
                    if (!attribValue.appendCodePoint(c)) {
                        if (fReportErrors_) {
                            fErrorReporter.reportError("HTML1005", new Object[] {"&#" + c + ';'});
                        }
                    }
                    if (plainAttribValue != null) {
                        plainAttribValue.appendCodePoint(c);
                    }
                    prevSpace = false;
                }
                else {
                    // ok, we finally have found the matching quote and can end here
                    break;
                }
            }
            return SCAN_TRUE;
        }

        // Scans an end element.
        protected void scanEndElement() throws IOException {
            final String ename = scanTagName();
            if (fReportErrors_ && ename == null) {
                fErrorReporter.reportError("HTML1012", null);
            }
            fCurrentEntity.skipMarkup(false);
            if (ename != null) {
                if (fElementCount >= fElementDepth) {
                    qName_.setValues(null, ename, ename, null);
                    if (DEBUG_CALLBACKS) {
                        System.out.println("endElement(" + qName_ + ")");
                    }
                    fDocumentHandler.endElement(qName_, locationAugs(fCurrentEntity));
                }
            }
        }
    }

    /**
     * Special scanner used for elements whose content needs to be scanned as plain
     * text, ignoring markup such as elements and entity references. For example:
     * &lt;SCRIPT&gt; and &lt;COMMENT&gt;.
     */
    public class SpecialScanner implements Scanner {

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
        private final XMLString charBuffer_ = new XMLString();

        // Sets the element name.
        public Scanner setElementName(final String ename, final String enameLC) {
            fElementName = ename;
            fStyle = "style".equals(enameLC);
            fTextarea = "textarea".equals(enameLC);
            fTitle = "title".equals(enameLC);
            return this;
        }

        /** Scan. */
        @Override
        public int scan(final boolean complete) throws IOException {
            do {
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

                            setScanner(fContentScanner);
                            if (fCurrentEntityStack.isEmpty()) {
                                setScannerState(STATE_END_DOCUMENT);
                            }
                            else {
                                fCurrentEntity = fCurrentEntityStack.pop();
                                setScannerState(STATE_CONTENT);
                            }
                            return SCAN_TRUE;
                        }
                        if (c == '<') {
                            setScannerState(STATE_MARKUP_BRACKET);
                            continue;
                        }
                        if (c == '&') {
                            if (fTextarea || fTitle) {
                                scanEntityRef(charBuffer_, null, true);
                                continue;
                            }
                            charBuffer_.clearAndAppend('&');
                        }
                        else {
                            fCurrentEntity.rewind();
                            charBuffer_.clear();
                        }
                        scanCharacters(charBuffer_);
                        break;
                    }
                    case STATE_MARKUP_BRACKET: {
                        final int c = fCurrentEntity.read();
                        if (c == '/') {
                            final String ename = scanName(true, fNamesElems);
                            if (ename != null) {
                                fCurrentEntity.skipSpaces();

                                if (ename.equalsIgnoreCase(fElementName)) {
                                    if (fCurrentEntity.read() == '>') {
                                        if (fElementCount >= fElementDepth) {
                                            fQName_.setValues(null, ename, ename, null);
                                            if (DEBUG_CALLBACKS) {
                                                System.out.println("endElement(" + fQName_ + ")");
                                            }
                                            fDocumentHandler.endElement(fQName_, locationAugs(fCurrentEntity));
                                        }
                                        setScanner(fContentScanner);
                                        setScannerState(STATE_CONTENT);
                                        return SCAN_TRUE;
                                    }
                                    fCurrentEntity.rewind();
                                }
                                charBuffer_.clear().append("</").append(ename);
                            }
                            else {
                                charBuffer_.clear().append("</");
                            }
                        }
                        else {
                            charBuffer_.clearAndAppend('<');
                            if (!charBuffer_.appendCodePoint(c)) {
                                if (fReportErrors_) {
                                    fErrorReporter.reportError("HTML1005", new Object[] {"&#" + c + ';'});
                                }
                            }
                        }
                        scanCharacters(charBuffer_);
                        setScannerState(STATE_CONTENT);
                        break;
                    }
                }
            }
            while (complete);
            return SCAN_TRUE;
        }

        // Scan characters.
        protected void scanCharacters(final XMLString buffer) throws IOException {
            if (DEBUG_BUFFER) {
                fCurrentEntity.debugBufferIfNeeded("(scanCharacters");
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
                    final int newlines = fCurrentEntity.skipNewlines();
                    for (int i = 0; i < newlines; i++) {
                        buffer.append('\n');
                    }
                }
                else {
                    if (!buffer.appendCodePoint(c)) {
                        if (fReportErrors_) {
                            fErrorReporter.reportError("HTML1005", new Object[] {"&#" + c + ';'});
                        }
                    }
                }
            }

            if (fStyle) {
                if (fStyleStripCommentDelims_) {
                    buffer.trimToContent("<!--", "-->");
                }
                if (fStyleStripCDATADelims_) {
                    buffer.trimToContent("<![CDATA[", "]]>");
                }
            }

            if (buffer.length() > 0 && fElementCount >= fElementDepth) {
                if (DEBUG_CALLBACKS) {
                    System.out.println("characters(" + buffer + ")");
                }
                fDocumentHandler.characters(buffer, locationAugs(fCurrentEntity));
            }
            if (DEBUG_BUFFER) {
                fCurrentEntity.debugBufferIfNeeded(")scanCharacters: ");
            }
        }
    }

    /**
     * Special scanner used for {@code PLAINTEXT}.
     */
    public class PlainTextScanner implements Scanner {

        /** A string buffer. */
        private final XMLString xmlString_ = new XMLString();

        @Override
        public int scan(final boolean complete) throws IOException {
            scanCharacters(xmlString_, complete);
            return SCAN_FALSE;
        }

        protected void scanCharacters(final XMLString buffer, final boolean complete) throws IOException {
            while (true) {
                final int c = fCurrentEntity.read();

                if (c == -1) {
                    break;
                }

                if (!buffer.appendCodePoint(c)) {
                    if (fReportErrors_) {
                        fErrorReporter.reportError("HTML1005", new Object[] {"&#" + c + ';'});
                    }
                }

                if (c == '\n') {
                    fCurrentEntity.incLine();
                }
            }

            if (buffer.length() > 0 && fElementCount >= fElementDepth) {
                if (DEBUG_CALLBACKS) {
                    System.out.println("characters(" + buffer + ")");
                }
                fDocumentHandler.characters(buffer, locationAugs(fCurrentEntity));
            }
            if (complete) {
                fDocumentHandler.endDocument(locationAugs(fCurrentEntity));
            }
            if (DEBUG_BUFFER) {
                fCurrentEntity.debugBufferIfNeeded(")scanCharacters: ");
            }
        }
    }

    /**
     * Special scanner used for {@code script}'s.
     */
    public class ScriptScanner implements Scanner {

        /** String buffer, larger because scripts areas are larger */
        final XMLString fScanScriptContent = new XMLString(128);

        @Override
        public int scan(final boolean complete) throws IOException {
            fScanScriptContent.clear();

            ScanScriptState state = ScanScriptState.DATA;
            int closeIndex = 0;
            int openIndex = 0;
            boolean invalidComment = false;

            OUTER:
                while (true) {
                    final int c = fCurrentEntity.read();
                    if (c == -1) {
                        break OUTER;
                    }

                    switch (state) {
                    case DATA:
                        if (c == '-' && fScanScriptContent.endsWith("<!-")) {
                            state = ScanScriptState.ESCAPED;
                        }
                        else if (c == '<') {
                            final String next = fCurrentEntity.nextContent(8) + " ";
                            if (next.length() >= 8 && "/script".equalsIgnoreCase(next.substring(0, 7))
                                    && ('>' == next.charAt(7) || Character.isWhitespace(next.charAt(7)))) {
                                fCurrentEntity.rewind();
                                break OUTER;
                            }
                        }
                        break;
                    case ESCAPED:
                        if (c == '>') {
                            if (fScanScriptContent.endsWith("--")) {
                                state = ScanScriptState.DATA;
                            }
                            else if (fScanScriptContent.endsWith("--!")) {
                                state = ScanScriptState.DATA;
                                invalidComment = true;
                            }
                        }
                        else if (c == '<') {
                            final String next = fCurrentEntity.nextContent(8) + " ";
                            if (next.length() >= 8 && "/script".equalsIgnoreCase(next.substring(0, 7))
                                    && ('>' == next.charAt(7) || Character.isWhitespace(next.charAt(7)))) {
                                fCurrentEntity.rewind();
                                break OUTER;
                            }
                            openIndex = 0;
                            state = ScanScriptState.ESCAPED_LT;
                        }
                        break;
                    case ESCAPED_LT:
                        if (openIndex < 6) {
                            if (Character.toLowerCase(c) == "script".charAt(openIndex)) {
                                openIndex++;
                            }
                            else {
                                state = ScanScriptState.ESCAPED;
                            }
                        }
                        else if (openIndex == 6) {
                            if (Character.isWhitespace(c)) {
                                openIndex++;
                            }
                            else if (c == '>') {
                                // buffer must not end with "--"
                                state = ScanScriptState.DOUBLE_ESCAPED;
                            }
                            else {
                                state = ScanScriptState.ESCAPED;
                            }
                        }
                        else {
                            if (c == '>') {
                                if (fScanScriptContent.endsWith("--")) {
                                    state = ScanScriptState.DATA;
                                }
                                else {
                                    state = ScanScriptState.DOUBLE_ESCAPED;
                                }
                            }
                        }
                        break;
                    case DOUBLE_ESCAPED:
                        if (c == '>' && fScanScriptContent.endsWith("--")) {
                            state = ScanScriptState.DATA;
                        }
                        else if (c == '<') {
                            state = ScanScriptState.DOUBLE_ESCAPED_LT;
                        }
                        break;
                    case DOUBLE_ESCAPED_LT:
                        if (closeIndex < 7) {
                            if (Character.toLowerCase(c) == "/script".charAt(closeIndex)) {
                                closeIndex++;
                            }
                            else if (c == '<') {
                                closeIndex = 0;
                            }
                            else {
                                state = ScanScriptState.DOUBLE_ESCAPED;
                            }
                        }
                        else {
                            if (c == '>') {
                                state = ScanScriptState.ESCAPED;
                            }
                            else if (Character.isWhitespace(c)) {
                                // skip white space
                                // </script(\s)*>
                                //         ^^^^^
                            }
                            else if (c == '<') {
                                closeIndex = 0;
                            }
                            else {
                                state = ScanScriptState.DOUBLE_ESCAPED;
                            }
                        }
                        break;
                    }

                    if (c == '\r' || c == '\n') {
                        fCurrentEntity.rewind();
                        final int newlines = fCurrentEntity.skipNewlines();
                        for (int i = 0; i < newlines; i++) {
                            fScanScriptContent.append('\n');
                        }
                    }
                    else {
                        if (!fScanScriptContent.appendCodePoint(c)) {
                            if (fReportErrors_) {
                                fErrorReporter.reportError("HTML1005", new Object[] {"&#" + c + ';'});
                            }
                        }
                    }
                }

            if (fScriptStripCommentDelims_) {
                if (invalidComment) {
                    fScanScriptContent.trimToContent("<!--", "--!>");
                }
                else {
                    fScanScriptContent.trimToContent("<!--", "-->");
                }
            }
            if (fScriptStripCDATADelims_) {
                fScanScriptContent.trimToContent("<![CDATA[", "]]>");
            }

            if (fScanScriptContent.length() > 0 && fElementCount >= fElementDepth) {
                if (DEBUG_CALLBACKS) {
                    System.out.println("characters(" + fScanScriptContent + ")");
                }
                fDocumentHandler.characters(fScanScriptContent, locationAugs(fCurrentEntity));
            }

            setScanner(fContentScanner);
            setScannerState(STATE_CONTENT);

            return SCAN_TRUE;
        }
    }

    /**
     * To detect if 2 encoding are compatible, both must be able to read the meta
     * tag specifying the new encoding. This means that the byte representation of
     * some minimal html markup must be the same in both encodings
     */
    static boolean isEncodingCompatible(final String encoding1, final String encoding2) {
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
                catch (final UnsupportedOperationException ignored) {
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
}
