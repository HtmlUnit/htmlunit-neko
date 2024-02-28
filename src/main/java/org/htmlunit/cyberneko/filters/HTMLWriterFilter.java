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
package org.htmlunit.cyberneko.filters;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.htmlunit.cyberneko.HTMLElements;
import org.htmlunit.cyberneko.HTMLNamedEntitiesParser;
import org.htmlunit.cyberneko.xerces.xni.Augmentations;
import org.htmlunit.cyberneko.xerces.xni.NamespaceContext;
import org.htmlunit.cyberneko.xerces.xni.QName;
import org.htmlunit.cyberneko.xerces.xni.XMLAttributes;
import org.htmlunit.cyberneko.xerces.xni.XMLLocator;
import org.htmlunit.cyberneko.xerces.xni.XMLString;
import org.htmlunit.cyberneko.xerces.xni.XNIException;

/**
 * An HTML writer written as a filter. Besides serializing the HTML
 * event stream, the writer also passes the document events to the next
 * stage in the pipeline. This allows applications to insert writer
 * filters between other custom filters for debugging purposes.
 * <p>
 * Since an HTML document may have specified its encoding using the
 * &lt;META&gt; tag and http-equiv/content attributes, the writer will
 * automatically change any character set specified in this tag to
 * match the encoding of the output stream. Therefore, the character
 * encoding name used to construct the writer should be an official
 * <a href='http://www.iana.org/assignments/character-sets'>IANA</a>
 * encoding name and not a Java encoding name.
 * <p>
 * <strong>Note:</strong>
 * The modified character set in the &lt;META&gt; tag is <em>not</em>
 * propagated to the next stage in the pipeline. The changed value is
 * only output to the stream; the original value is sent to the next
 * stage in the pipeline.
 *
 * @author Andy Clark
 * @author Ronald Brill
 */
public class HTMLWriterFilter extends DefaultFilter {

    /** The encoding. */
    protected String fEncoding;

    /**
     * The print writer used for serializing the document with the
     * appropriate character encoding.
     */
    protected PrintWriter fPrinter;

    /** Seen root element. */
    protected boolean fSeenRootElement;

    /** Seen http-equiv directive. */
    protected boolean fSeenHttpEquiv;

    /** Element depth. */
    protected int fElementDepth;

    /** Normalize character content. */
    protected boolean fNormalize;

    /** Print characters. */
    protected boolean fPrintChars;

    private final HTMLElements htmlElements_;

    public String getEncoding_() {
        return fEncoding;
    }

    public void setEncoding_(String encoding_) {
        this.fEncoding = encoding_;
    }

    public PrintWriter getPrinter_() {
        return fPrinter;
    }

    public void setPrinter_(PrintWriter printer_) {
        this.fPrinter = printer_;
    }

    public boolean isSeenRootElement_() {
        return fSeenRootElement;
    }

    public void setSeenRootElement_(boolean seenRootElement_) {
        this.fSeenRootElement = seenRootElement_;
    }

    /** Constructs a writer filter that prints to standard out. */
    public HTMLWriterFilter() {
        // Note: UTF-8 should *always* be a supported encoding. Although,
        //       I've heard of the old M$ JVM not supporting it! Amazing. -Ac
        try {
            fEncoding = "UTF-8";
            fPrinter = new PrintWriter(new OutputStreamWriter(System.out, fEncoding));
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage());
        }
        htmlElements_ = new HTMLElements();
    }

    /**
     * Constructs a writer filter using the specified output stream and
     * encoding.
     *
     * @param outputStream The output stream to write to.
     * @param encoding The encoding to be used for the output. The encoding name
     *                 should be an official IANA encoding name.
     */
    public HTMLWriterFilter(OutputStream outputStream, String encoding)
            throws UnsupportedEncodingException {
        this(new OutputStreamWriter(outputStream, encoding), encoding, new HTMLElements());
    }

    /**
     * Constructs a writer filter using the specified Java writer and
     * encoding.
     *
     * @param writer The Java writer to write to.
     * @param encoding The encoding to be used for the output. The encoding name
     *                 should be an official IANA encoding name.
     */
    public HTMLWriterFilter(java.io.Writer writer, String encoding, HTMLElements htmlElements) {
        fEncoding = encoding;
        if (writer instanceof PrintWriter) {
            fPrinter = (PrintWriter)writer;
        }
        else {
            fPrinter = new PrintWriter(writer);
        }
        htmlElements_ = htmlElements;
    }


    /** Start document. */
    @Override
    public void startDocument(XMLLocator locator, String encoding,
                              NamespaceContext nscontext, Augmentations augs)
            throws XNIException {
        fSeenRootElement = false;
        fSeenHttpEquiv = false;
        fElementDepth = 0;
        fNormalize = true;
        fPrintChars = true;
        super.startDocument(locator, encoding, nscontext, augs);
    }

    /** Comment. */
    @Override
    public void comment(XMLString text, Augmentations augs)
            throws XNIException {
        if (fSeenRootElement && fElementDepth <= 0) {
            fPrinter.println();
        }
        fPrinter.print("<!--");
        printCharacters(text, false);
        fPrinter.print("-->");
        if (!fSeenRootElement) {
            fPrinter.println();
        }
        fPrinter.flush();
    }

    /** Start element. */
    @Override
    public void startElement(QName element, XMLAttributes attributes, Augmentations augs)
            throws XNIException {
        fSeenRootElement = true;
        fElementDepth++;
        fNormalize = !htmlElements_.getElement(element.getRawname()).isSpecial();
        printStartElement(element, attributes);
        super.startElement(element, attributes, augs);
    }

    /** Empty element. */
    @Override
    public void emptyElement(QName element, XMLAttributes attributes, Augmentations augs)
            throws XNIException {
        fSeenRootElement = true;
        printStartElement(element, attributes);
        super.emptyElement(element, attributes, augs);
    }

    /** Characters. */
    @Override
    public void characters(XMLString text, Augmentations augs)
            throws XNIException {
        if (fPrintChars) {
            printCharacters(text, fNormalize);
        }
        super.characters(text, augs);
    }

    /** End element. */
    @Override
    public void endElement(QName element, Augmentations augs)
            throws XNIException {
        fElementDepth--;
        fNormalize = true;
        /***
         // NOTE: Not sure if this is what should be done in the case where
         //       the encoding is not explitly declared within the HEAD. So
         //       I'm leaving it commented out for now. -Ac
         if (element.rawname.equalsIgnoreCase("head") && !fSeenHttpEquiv) {
         boolean capitalize = Character.isUpperCase(element.rawname.charAt(0));
         String ename = capitalize ? "META" : "meta";
         QName qname = new QName(null, ename, ename, null);
         XMLAttributes attrs = new XMLAttributesImpl();
         QName aname = new QName(null, "http-equiv", "http-equiv", null);
         attrs.addAttribute(aname, "CDATA", "Content-Type");
         aname.setValues(null, "content", "content", null);
         attrs.addAttribute(aname, "CDATA", "text/html; charset="+fEncoding);
         super.emptyElement(qname, attrs, null);
         }
         /***/
        printEndElement(element);
        super.endElement(element, augs);
    }

    /** Print attribute value. */
    protected void printAttributeValue(String text) {
        int length = text.length();
        for (int j = 0; j < length; j++) {
            char c = text.charAt(j);
            if (c == '"') {
                fPrinter.print("&quot;");
            }
            else {
                fPrinter.print(c);
            }
        }
        fPrinter.flush();
    }

    /** Print characters. */
    protected void printCharacters(XMLString text, boolean normalize) {
        if (normalize) {
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c != '\n') {
                    String entity = HTMLNamedEntitiesParser.get().lookupEntityRefFor(Character.toString(c));
                    if (entity != null) {
                        fPrinter.print(entity);
                    }
                    else {
                        fPrinter.print(c);
                    }
                }
                else {
                    fPrinter.println();
                }
            }
        }
        else {
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                fPrinter.print(c);
            }
        }
        fPrinter.flush();
    }

    /** Print start element. */
    protected void printStartElement(QName element, XMLAttributes attributes) {

        // modify META[@http-equiv='content-type']/@content value
        int contentIndex = -1;
        String originalContent = null;
        if (element.getRawname().toLowerCase().equals("meta")) {
            String httpEquiv = null;
            int length = attributes.getLength();
            for (int i = 0; i < length; i++) {
                String aname = attributes.getQName(i).toLowerCase();
                if (aname.equals("http-equiv")) {
                    httpEquiv = attributes.getValue(i);
                }
                else if (aname.equals("content")) {
                    contentIndex = i;
                }
            }
            if (httpEquiv != null && httpEquiv.toLowerCase().equals("content-type")) {
                fSeenHttpEquiv = true;
                String content = null;
                if (contentIndex != -1) {
                    originalContent = attributes.getValue(contentIndex);
                    content = originalContent.toLowerCase();
                }
                if (content != null) {
                    int charsetIndex = content.indexOf("charset=");
                    if (charsetIndex != -1) {
                        content = content.substring(0, charsetIndex + 8);
                    }
                    else {
                        content += ";charset=";
                    }
                    content += fEncoding;
                    attributes.setValue(contentIndex, content);
                }
            }
        }

        // print element
        fPrinter.print('<');
        fPrinter.print(element.getRawname());
        int attrCount = attributes != null ? attributes.getLength() : 0;
        for (int i = 0; i < attrCount; i++) {
            String aname = attributes.getQName(i);
            String avalue = attributes.getValue(i);
            fPrinter.print(' ');
            fPrinter.print(aname);
            fPrinter.print("=\"");
            printAttributeValue(avalue);
            fPrinter.print('"');
        }
        fPrinter.print('>');
        fPrinter.flush();

        // return original META[@http-equiv]/@content value
        if (contentIndex != -1 && originalContent != null) {
            attributes.setValue(contentIndex, originalContent);
        }
    }

    /** Print end element. */
    protected void printEndElement(QName element) {
        fPrinter.print("</");
        fPrinter.print(element.getRawname());
        fPrinter.print('>');
        fPrinter.flush();
    }

//    /** Main. */
//    public static void main(String[] argv) throws Exception {
//        if (argv.length == 0) {
//            printUsage();
//            System.exit(1);
//        }
//        XMLParserConfiguration parser = new HTMLConfiguration();
//        String iencoding = null;
//        String oencoding = "Windows-1252";
//        for (int i = 0; i < argv.length; i++) {
//            String arg = argv[i];
//            if (arg.equals("-ie")) {
//                iencoding = argv[++i];
//                continue;
//            }
//            if (arg.equals("-e") || arg.equals("-oe")) {
//                oencoding = argv[++i];
//                continue;
//            }
//            if (arg.equals("-h")) {
//                printUsage();
//                System.exit(1);
//            }
//
//            java.util.Vector filtersVector = new java.util.Vector(2);
//            filtersVector.addElement(new HtmlWriterFilter(System.out, oencoding));
//            XMLDocumentFilter[] filters =
//                new XMLDocumentFilter[filtersVector.size()];
//            filtersVector.copyInto(filters);
//            parser.setProperty(HTMLConfiguration.FILTERS, filters);
//            XMLInputSource source = new XMLInputSource(null, arg, null);
//            source.setEncoding(iencoding);
//            parser.parse(source);
//        }
//    }
//
//    /** Print usage. */
//    private static void printUsage() {
//        System.err.println("usage: java "+HtmlWriterFilter.class.getName()+" (options) file ...");
//        System.err.println();
//        System.err.println("options:");
//        System.err.println("  -ie name  Specify IANA name of input encoding.");
//        System.err.println("  -oe name  Specify IANA name of output encoding.");
//        System.err.println("  -h        Display help screen.");
//        System.err.println();
//        System.err.println("notes:");
//        System.err.println("  The -i and -p options are mutually exclusive.");
//        System.err.println("  The -e option has been replaced with -oe.");
//    }
}