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
import java.io.Writer;

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
    protected String encoding_;

    /**
     * The print writer used for serializing the document with the
     * appropriate character encoding.
     */
    protected PrintWriter printer_;

    /** Seen root element. */
    protected boolean seenRootElement_;

    /** Element depth. */
    protected int elementDepth_;

    /** Normalize character content. */
    protected boolean normalize_;

    /** Print characters. */
    protected boolean printChars_;

    protected final HTMLElements htmlElements_;

    /** Constructs a writer filter that prints to standard out. */
    public HTMLWriterFilter() {
        // Note: UTF-8 should *always* be a supported encoding. Although,
        //       I've heard of the old M$ JVM not supporting it! Amazing. -Ac
        try {
            encoding_ = "UTF-8";
            printer_ = new PrintWriter(new OutputStreamWriter(System.out, encoding_));
        }
        catch (final UnsupportedEncodingException e) {
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
    public HTMLWriterFilter(final OutputStream outputStream, final String encoding)
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
    public HTMLWriterFilter(final Writer writer, final String encoding, final HTMLElements htmlElements) {
        encoding_ = encoding;
        if (writer instanceof PrintWriter) {
            printer_ = (PrintWriter) writer;
        }
        else {
            printer_ = new PrintWriter(writer);
        }
        htmlElements_ = htmlElements;
    }

    /** Start document. */
    @Override
    public void startDocument(final XMLLocator locator, final String encoding,
            final NamespaceContext nscontext, final Augmentations augs)
        throws XNIException {
        seenRootElement_ = false;
        elementDepth_ = 0;
        normalize_ = true;
        printChars_ = true;
        super.startDocument(locator, encoding, nscontext, augs);
    }

    /** Comment. */
    @Override
    public void comment(final XMLString text, final Augmentations augs)
        throws XNIException {
        if (seenRootElement_ && elementDepth_ <= 0) {
            printer_.println();
        }
        printer_.print("<!--");
        printCharacters(text, false);
        printer_.print("-->");
        if (!seenRootElement_) {
            printer_.println();
        }
        printer_.flush();
    }

    /** Start element. */
    @Override
    public void startElement(final QName element, final XMLAttributes attributes, final Augmentations augs)
        throws XNIException {
        seenRootElement_ = true;
        elementDepth_++;
        normalize_ = !htmlElements_.getElement(element.getRawname()).isSpecial();
        printStartElement(element, attributes);
        super.startElement(element, attributes, augs);
    }

    /** Empty element. */
    @Override
    public void emptyElement(final QName element, final XMLAttributes attributes, final Augmentations augs)
        throws XNIException {
        seenRootElement_ = true;
        printStartElement(element, attributes);
        super.emptyElement(element, attributes, augs);
    }

    /** Characters. */
    @Override
    public void characters(final XMLString text, final Augmentations augs)
        throws XNIException {
        if (printChars_) {
            printCharacters(text, normalize_);
        }
        super.characters(text, augs);
    }

    /** End element. */
    @Override
    public void endElement(final QName element, final Augmentations augs)
        throws XNIException {
        elementDepth_--;
        normalize_ = true;
        printEndElement(element);
        super.endElement(element, augs);
    }

    /** Print attribute value. */
    protected void printAttributeValue(final String text) {
        final int length = text.length();
        for (int j = 0; j < length; j++) {
            final char c = text.charAt(j);
            if (c == '"') {
                printer_.print("&quot;");
            }
            else {
                printer_.print(c);
            }
        }
        printer_.flush();
    }

    /** Print characters. */
    protected void printCharacters(final XMLString text, final boolean normalize) {
        if (normalize) {
            for (int i = 0; i < text.length(); i++) {
                final char c = text.charAt(i);
                if (c != '\n') {
                    final String entity = HTMLNamedEntitiesParser.get().lookupEntityRefFor(Character.toString(c));
                    if (entity != null) {
                        printer_.print(entity);
                    }
                    else {
                        printer_.print(c);
                    }
                }
                else {
                    printer_.println();
                }
            }
        }
        else {
            for (int i = 0; i < text.length(); i++) {
                final char c = text.charAt(i);
                printer_.print(c);
            }
        }
        printer_.flush();
    }

    /** Print start element. */
    protected void printStartElement(final QName element, final XMLAttributes attributes) {

        // modify META[@http-equiv='content-type']/@content value
        int contentIndex = -1;
        String originalContent = null;
        if (element.getRawname().toLowerCase().equals("meta")) {
            String httpEquiv = null;
            final int length = attributes.getLength();
            for (int i = 0; i < length; i++) {
                final String aname = attributes.getQName(i).toLowerCase();
                if ("http-equiv".equals(aname)) {
                    httpEquiv = attributes.getValue(i);
                }
                else if ("content".equals(aname)) {
                    contentIndex = i;
                }
            }
            if (httpEquiv != null && httpEquiv.toLowerCase().equals("content-type")) {
                String content = null;
                if (contentIndex != -1) {
                    originalContent = attributes.getValue(contentIndex);
                    content = originalContent.toLowerCase();
                }
                if (content != null) {
                    final int charsetIndex = content.indexOf("charset=");
                    if (charsetIndex != -1) {
                        content = content.substring(0, charsetIndex + 8);
                    }
                    else {
                        content += ";charset=";
                    }
                    content += encoding_;
                    attributes.setValue(contentIndex, content);
                }
            }
        }

        // print element
        printer_.print('<');
        printer_.print(element.getRawname());
        final int attrCount = attributes != null ? attributes.getLength() : 0;
        for (int i = 0; i < attrCount; i++) {
            final String aname = attributes.getQName(i);
            final String avalue = attributes.getValue(i);
            printer_.print(' ');
            printer_.print(aname);
            printer_.print("=\"");
            printAttributeValue(avalue);
            printer_.print('"');
        }
        printer_.print('>');
        printer_.flush();

        // return original META[@http-equiv]/@content value
        if (contentIndex != -1 && originalContent != null) {
            attributes.setValue(contentIndex, originalContent);
        }
    }

    /** Print end element. */
    protected void printEndElement(final QName element) {
        printer_.print("</");
        printer_.print(element.getRawname());
        printer_.print('>');
        printer_.flush();
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
