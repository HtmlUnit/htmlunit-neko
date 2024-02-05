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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Stream;

import org.htmlunit.cyberneko.parsers.DOMFragmentParser;
import org.htmlunit.cyberneko.parsers.DOMParser;
import org.htmlunit.cyberneko.parsers.SAXParser;
import org.htmlunit.cyberneko.xerces.dom.CDATASectionImpl;
import org.htmlunit.cyberneko.xerces.dom.CommentImpl;
import org.htmlunit.cyberneko.xerces.dom.CoreDocumentImpl;
import org.htmlunit.cyberneko.xerces.dom.DocumentFragmentImpl;
import org.htmlunit.cyberneko.xerces.dom.DocumentTypeImpl;
import org.htmlunit.cyberneko.xerces.dom.NodeImpl;
import org.htmlunit.cyberneko.xerces.dom.ProcessingInstructionImpl;
import org.htmlunit.cyberneko.xerces.dom.TextImpl;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLDocumentFilter;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLInputSource;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLParserConfiguration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;

/**
 * This test generates canonical result using the <code>Writer</code> class
 * and compares it against the expected canonical output. Simple as that.
 *
 * @author Andy Clark
 * @author Marc Guillemot
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
public class CanonicalTest {
    private static final File dataDir = new File("src/test/resources/org/htmlunit/cyberneko/testfiles");
    private static final File canonicalDir = new File("src/test/resources/org/htmlunit/cyberneko/testfiles/canonical");
    private static final File outputDir = new File("target/data/output");

    private static Stream<Arguments> testFiles() {
        // System.out.println(canonicalDir.getAbsolutePath());
        outputDir.mkdirs();

        final List<File> dataFiles = new ArrayList<>();
        dataDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File file) {
                final String name = file.getName();
                if (file.isDirectory() && !"canonical".equals(name)) {
                    file.listFiles(this);
                }
                else if (name.startsWith("test") && name.endsWith(".html")) {
                    dataFiles.add(file);
                }
                return false; // we don't care to listFiles' result
            }
        });
        Collections.sort(dataFiles);
        return dataFiles.stream().map(f -> Arguments.of(f));
    }

    @ParameterizedTest
    @MethodSource("testFiles")
    public void runTest(final File dataFile) throws Exception {
        final String dataLines = getResult(dataFile);

        try {
            // prepare for future changes where canonical files are next to test file
            File canonicalFile = new File(dataFile.getParentFile(), dataFile.getName() + ".canonical");
            if (!canonicalFile.exists()) {
                canonicalFile = new File(canonicalDir, dataFile.getName());
            }
            if (!canonicalFile.exists()) {
                fail("Canonical file not found for input: " + dataFile.getAbsolutePath() + ": " + dataLines);
            }

            final File nyiFile = new File(canonicalFile.getParentFile(), canonicalFile.getName() + ".nyi");
            if (nyiFile.exists()) {
                try {
                    assertEquals(getCanonical(canonicalFile), dataLines, dataFile.toString());
                    fail("test " + dataFile.getName() + "is marked as not yet implemented but already works");
                }
                catch (final AssertionFailedError e) {
                    // expected
                }

                assertEquals(getCanonical(nyiFile), dataLines, "NYI: " + dataFile);
            }
            else {
                assertEquals(getCanonical(canonicalFile), dataLines, dataFile.toString());
            }
        }
        catch (final AssertionFailedError e) {
            final File output = new File(outputDir, dataFile.getName());
            try (PrintWriter pw = new PrintWriter(Files.newOutputStream(output.toPath()))) {
                pw.print(dataLines);
            }
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("testFiles")
    public void runDomTest(final File dataFile) throws Exception {
        final String domDataLines = getDomResult(dataFile);

        try {
            // prepare for future changes where canonical files are next to test file
            File canonicalFile = new File(dataFile.getParentFile(), dataFile.getName() + ".canonical-dom");
            if (!canonicalFile.exists()) {
                canonicalFile = new File(dataFile.getParentFile(), dataFile.getName() + ".canonical");

                if (!canonicalFile.exists()) {
                    canonicalFile = new File(canonicalDir, dataFile.getName() + ".canonical-dom");

                    if (!canonicalFile.exists()) {
                        canonicalFile = new File(canonicalDir, dataFile.getName());
                    }
                }
            }

            if (!canonicalFile.exists()) {
                fail("Canonical file not found for input: " + dataFile.getAbsolutePath() + ": " + domDataLines);
            }

            final File nyiFile = new File(canonicalFile.getParentFile(), canonicalFile.getName() + ".nyi");
            if (nyiFile.exists()) {
                try {
                    assertEquals(getCanonical(canonicalFile), domDataLines, dataFile.toString());
                    fail("test " + dataFile.getName() + "is marked as not yet implemented but already works");
                }
                catch (final AssertionFailedError e) {
                    // expected
                }

                assertEquals(getCanonical(nyiFile), domDataLines, "NYI: " + dataFile);
            }
            else {
                assertEquals(getCanonical(canonicalFile), domDataLines, dataFile.toString());
            }
        }
        catch (final AssertionFailedError e) {
            final File output = new File(outputDir, dataFile.getName());
            try (PrintWriter pw = new PrintWriter(Files.newOutputStream(output.toPath()))) {
                pw.print(domDataLines);
            }
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("testFiles")
    public void runDomFragmentTest(final File dataFile) throws Exception {
        final String domDataLines = getDomFragmentResult(dataFile);

        try {
            // prepare for future changes where canonical files are next to test file
            File canonicalFile = new File(dataFile.getParentFile(), dataFile.getName() + ".canonical-frg");
            if (!canonicalFile.exists()) {
                canonicalFile = new File(dataFile.getParentFile(), dataFile.getName() + ".canonical-dom");

                if (!canonicalFile.exists()) {
                    canonicalFile = new File(dataFile.getParentFile(), dataFile.getName() + ".canonical");

                    if (!canonicalFile.exists()) {
                        canonicalFile = new File(canonicalDir, dataFile.getName() + ".canonical-frg");

                        if (!canonicalFile.exists()) {
                            canonicalFile = new File(canonicalDir, dataFile.getName() + ".canonical-dom");

                            if (!canonicalFile.exists()) {
                                canonicalFile = new File(canonicalDir, dataFile.getName());
                            }
                        }
                    }
                }
            }

            if (!canonicalFile.exists()) {
                fail("Canonical file not found for input: " + dataFile.getAbsolutePath() + ": " + domDataLines);
            }

            final File nyiFile = new File(canonicalFile.getParentFile(), canonicalFile.getName() + ".nyi");
            if (nyiFile.exists()) {
                try {
                    assertEquals(getCanonical(canonicalFile), domDataLines, dataFile.toString());
                    fail("test " + dataFile.getName() + "is marked as not yet implemented but already works");
                }
                catch (final AssertionFailedError e) {
                    // expected
                }

                assertEquals(getCanonical(nyiFile), domDataLines, "NYI: " + dataFile);
            }
            else {
                assertEquals(getCanonical(canonicalFile), domDataLines, dataFile.toString());
            }
        }
        catch (final AssertionFailedError e) {
            final File output = new File(outputDir, dataFile.getName());
            try (PrintWriter pw = new PrintWriter(Files.newOutputStream(output.toPath()))) {
                pw.print(domDataLines);
            }
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("testFiles")
    public void runSaxTest(final File dataFile) throws Exception {
        final String domDataLines = getSaxResult(dataFile);

        try {
            // prepare for future changes where canonical files are next to test file
            File canonicalFile = new File(dataFile.getParentFile(), dataFile.getName() + ".canonical-sax");
            if (!canonicalFile.exists()) {
                canonicalFile = new File(dataFile.getParentFile(), dataFile.getName() + ".canonical");

                if (!canonicalFile.exists()) {
                    canonicalFile = new File(canonicalDir, dataFile.getName() + ".canonical-sax");

                    if (!canonicalFile.exists()) {
                        canonicalFile = new File(canonicalDir, dataFile.getName());
                    }
                }
            }

            if (!canonicalFile.exists()) {
                fail("Canonical file not found for input: " + dataFile.getAbsolutePath() + ": " + domDataLines);
            }

            final File nyiFile = new File(canonicalFile.getParentFile(), canonicalFile.getName() + ".nyi");
            if (nyiFile.exists()) {
                try {
                    assertEquals(getCanonical(canonicalFile), domDataLines, dataFile.toString());
                    fail("test " + dataFile.getName() + "is marked as not yet implemented but already works");
                }
                catch (final AssertionFailedError e) {
                    // expected
                }

                assertEquals(getCanonical(nyiFile), domDataLines, "NYI: " + dataFile);
            }
            else {
                assertEquals(getCanonical(canonicalFile), domDataLines, dataFile.toString());
            }
        }
        catch (final AssertionFailedError e) {
            final File output = new File(outputDir, dataFile.getName());
            try (PrintWriter pw = new PrintWriter(Files.newOutputStream(output.toPath()))) {
                pw.print(domDataLines);
            }
            throw e;
        }
    }

    private static String getCanonical(final File infile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new UTF8BOMSkipper(new FileInputStream(infile)), StandardCharsets.UTF_8))) {
            final StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }

    private static String getResult(final File infile) throws IOException {
        try (StringWriter out = new StringWriter()) {
            // create filters
            final XMLDocumentFilter[] filters = {new Writer(out)};

            // create parser
            final XMLParserConfiguration parser = new HTMLConfiguration();

            // parser settings
            parser.setProperty("http://cyberneko.org/html/properties/filters", filters);

            final String infilename = infile.toString();
            final File insettings = new File(infilename + ".settings");
            if (insettings.exists()) {
                try (BufferedReader settings = new BufferedReader(new FileReader(insettings))) {
                    String settingline;
                    while ((settingline = settings.readLine()) != null) {
                        final StringTokenizer tokenizer = new StringTokenizer(settingline);
                        final String type = tokenizer.nextToken();
                        final String id = tokenizer.nextToken();
                        final String value = tokenizer.nextToken();
                        if ("feature".equals(type)) {
                            parser.setFeature(id, "true".equals(value));
                            if (HTMLScanner.REPORT_ERRORS.equals(id)) {
                                parser.setErrorHandler(new HTMLErrorHandler(out));
                            }
                        }
                        else {
                            parser.setProperty(id, value);
                        }
                    }
                }
            }

            // parse
            parser.parse(new XMLInputSource(null, infilename, null));
            final BufferedReader reader = new BufferedReader(new StringReader(out.toString()));
            final StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }

    private static String getDomResult(final File infile) throws Exception {
        try (StringWriter out = new StringWriter()) {
            final DOMParser parser = new DOMParser(null);

            final String infilename = infile.toString();
            final File insettings = new File(infilename + ".settings");
            if (insettings.exists()) {
                try (BufferedReader settings = new BufferedReader(new FileReader(insettings))) {
                    String settingline;
                    while ((settingline = settings.readLine()) != null) {
                        final StringTokenizer tokenizer = new StringTokenizer(settingline);
                        final String type = tokenizer.nextToken();
                        final String id = tokenizer.nextToken();
                        final String value = tokenizer.nextToken();
                        if ("feature".equals(type)) {
                            parser.setFeature(id, "true".equals(value));
                            /* feature not implemented
                            if (HTMLScanner.REPORT_ERRORS.equals(id)) {
                                parser.setErrorHandler(new ErrorHandler() {
                                    @Override
                                    public void warning(SAXParseException exception) throws SAXException {
                                        out.append("[warning]").append(exception.getMessage());
                                    }

                                    @Override
                                    public void fatalError(SAXParseException exception) throws SAXException {
                                        out.append("[error]").append(exception.getMessage());
                                    }

                                    @Override
                                    public void error(SAXParseException exception) throws SAXException {
                                        out.append("[error]").append(exception.getMessage());
                                    }
                                });
                            }
                            */
                        }
                        else {
                            parser.setProperty(id, value);
                        }
                    }
                }
            }

            // parse
            parser.parse(new XMLInputSource(null, infilename, null));

            final CoreDocumentImpl doc = (CoreDocumentImpl) parser.getDocument();

            final StringBuilder sb = new StringBuilder();

            // first the error handler output
            final BufferedReader reader = new BufferedReader(new StringReader(out.toString()));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            write(sb, doc);

            return sb.toString();
        }
    }

    private static String getDomFragmentResult(final File infile) throws Exception {
        try (StringWriter out = new StringWriter()) {
            final DOMFragmentParser parser = new DOMFragmentParser();

            final CoreDocumentImpl document = new CoreDocumentImpl();
            final DocumentFragmentImpl fragment = (DocumentFragmentImpl) document.createDocumentFragment();

            final String infilename = infile.toString();
            final File insettings = new File(infilename + ".settings");
            if (insettings.exists()) {
                try (BufferedReader settings = new BufferedReader(new FileReader(insettings))) {
                    String settingline;
                    while ((settingline = settings.readLine()) != null) {
                        final StringTokenizer tokenizer = new StringTokenizer(settingline);
                        final String type = tokenizer.nextToken();
                        final String id = tokenizer.nextToken();
                        final String value = tokenizer.nextToken();
                        if ("feature".equals(type)) {
                            parser.setFeature(id, "true".equals(value));
                            /* feature not implemented
                            if (HTMLScanner.REPORT_ERRORS.equals(id)) {
                                parser.setErrorHandler(new ErrorHandler() {
                                    @Override
                                    public void warning(SAXParseException exception) throws SAXException {
                                        out.append("[warning]").append(exception.getMessage());
                                    }

                                    @Override
                                    public void fatalError(SAXParseException exception) throws SAXException {
                                        out.append("[error]").append(exception.getMessage());
                                    }

                                    @Override
                                    public void error(SAXParseException exception) throws SAXException {
                                        out.append("[error]").append(exception.getMessage());
                                    }
                                });
                            }
                            */
                        }
                        else {
                            parser.setProperty(id, value);
                        }
                    }
                }
            }

            // parse
            parser.parse(infilename, fragment);

            final StringBuilder sb = new StringBuilder();

            // first the error handler output
            final BufferedReader reader = new BufferedReader(new StringReader(out.toString()));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            write(sb, fragment);

            return sb.toString();
        }
    }

    private static void write(final StringBuilder out, final CoreDocumentImpl doc) {
        if (doc.getXmlEncoding() != null && doc.getXmlEncoding().length() > 0) {
            out.append("xencoding ");
            out.append(normalize(doc.getXmlEncoding()));
            out.append('\n');
        }

        final NodeList childNodes = doc.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node childNode = childNodes.item(i);

            if (childNode instanceof CDATASectionImpl) {
                write(out, (CDATASectionImpl) childNode);
            }
            else if (childNode instanceof TextImpl) {
                write(out, (TextImpl) childNode);
            }
            else if (childNode instanceof CommentImpl) {
                write(out, (CommentImpl) childNode);
            }
            else if (childNode instanceof DocumentTypeImpl) {
                write(out, (DocumentTypeImpl) childNode);
            }
            else if (childNode instanceof ProcessingInstructionImpl) {
                write(out, (ProcessingInstructionImpl) childNode);
            }
            else if (childNode instanceof NodeImpl) {
                write(out, (NodeImpl) childNode);
            }
        }
    }

    private static void write(final StringBuilder out, final DocumentFragmentImpl doc) {
        final NodeList childNodes = doc.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node childNode = childNodes.item(i);

            if (childNode instanceof CDATASectionImpl) {
                write(out, (CDATASectionImpl) childNode);
            }
            else if (childNode instanceof TextImpl) {
                write(out, (TextImpl) childNode);
            }
            else if (childNode instanceof CommentImpl) {
                write(out, (CommentImpl) childNode);
            }
            else if (childNode instanceof DocumentTypeImpl) {
                write(out, (DocumentTypeImpl) childNode);
            }
            else if (childNode instanceof ProcessingInstructionImpl) {
                write(out, (ProcessingInstructionImpl) childNode);
            }
            else if (childNode instanceof NodeImpl) {
                write(out, (NodeImpl) childNode);
            }
        }
    }

    private static void write(final StringBuilder out, final NodeImpl node) {
        out.append('(')
            .append(node.getNodeName())
            .append("\n");

        // attributes
        final NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                final Node attribute = attributes.item(i);

                if (attribute instanceof Attr) {
                    write(out, (Attr) attribute);
                }
                else {
                    throw new RuntimeException("");
                }
            }
        }

        // child nodes
        final NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node childNode = childNodes.item(i);

            if (childNode instanceof CDATASectionImpl) {
                write(out, (CDATASectionImpl) childNode);
            }
            else if (childNode instanceof TextImpl) {
                write(out, (TextImpl) childNode);
            }
            else if (childNode instanceof CommentImpl) {
                write(out, (CommentImpl) childNode);
            }
            else if (childNode instanceof DocumentTypeImpl) {
                write(out, (DocumentTypeImpl) childNode);
            }
            else if (childNode instanceof ProcessingInstructionImpl) {
                write(out, (ProcessingInstructionImpl) childNode);
            }
            else if (childNode instanceof NodeImpl) {
                write(out, (NodeImpl) childNode);
            }
            else {
                throw new RuntimeException("");
            }
        }

        out.append(')')
            .append(node.getNodeName())
            .append('\n');
    }

    private static void write(final StringBuilder out, final TextImpl text) {
        out.append('"')
            .append(normalize(text.getTextContent()))
            .append('\n');
    }

    private static void write(final StringBuilder out, final CDATASectionImpl cdata) {
        out.append("((CDATA\n\"")
            .append(normalize(cdata.getTextContent()))
            .append('\n')
            .append("))CDATA\n");
    }

    private static void write(final StringBuilder out, final CommentImpl comment) {
        out.append('#')
            .append(normalize(comment.getNodeValue()))
            .append('\n');
    }

    private static void write(final StringBuilder out, final ProcessingInstructionImpl processingInstruction) {
        out.append('?')
            .append(processingInstruction.getTarget());
        if (processingInstruction.getData() != null && processingInstruction.getData().length() > 0) {
            out.append(' ')
                .append(normalize(processingInstruction.getData()));
        }
        out.append('\n');
    }

    private static void write(final StringBuilder out, final DocumentTypeImpl documentType) {
        out.append('!');
        boolean addNl = true;
        if (documentType.getName() != null && documentType.getName().length() > 0) {
            out.append(normalize(documentType.getName()));
            out.append('\n');
            addNl = false;
        }
        if (documentType.getPublicId() != null && documentType.getPublicId().length() > 0) {
            out.append('p');
            out.append(normalize(documentType.getPublicId()));
            out.append('\n');
            addNl = false;
        }
        if (documentType.getSystemId() != null && documentType.getSystemId().length() > 0) {
            out.append('s');
            out.append(normalize(documentType.getSystemId()));
            out.append('\n');
            addNl = false;
        }
        if (addNl) {
            out.append('\n');
        }
    }

    private static void write(final StringBuilder out, final Attr attr) {
        out.append('A');
        if (attr.getNamespaceURI() != null && attr.getNamespaceURI().length() > 0) {
            out.append('{')
                .append(attr.getNamespaceURI())
                .append('}');
        }

        out.append(normalize(attr.getName()))
            .append(' ')
            .append(normalize(attr.getValue()))
            .append('\n');
    }

    static String normalize(final String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private static String getSaxResult(final File infile) throws Exception {
        try (StringWriter out = new StringWriter()) {
            final SAXParser parser = new SAXParser();

            final String infilename = infile.toString();
            final File insettings = new File(infilename + ".settings");
            if (insettings.exists()) {
                try (BufferedReader settings = new BufferedReader(new FileReader(insettings))) {
                    String settingline;
                    while ((settingline = settings.readLine()) != null) {
                        final StringTokenizer tokenizer = new StringTokenizer(settingline);
                        final String type = tokenizer.nextToken();
                        final String id = tokenizer.nextToken();
                        final String value = tokenizer.nextToken();
                        if ("feature".equals(type)) {
                            parser.setFeature(id, "true".equals(value));
                        }
                        else {
                            parser.setProperty(id, value);
                        }
                    }
                }
            }

            // parse
            final SaxHandler saxHandler = new SaxHandler(out);
            parser.setContentHandler(saxHandler);
            parser.setProperty("http://xml.org/sax/properties/lexical-handler", saxHandler);
            parser.setErrorHandler(saxHandler);
            parser.parse(new XMLInputSource(null, infilename, null));

            final StringBuilder sb = new StringBuilder();

            // first the error handler output
            final BufferedReader reader = new BufferedReader(new StringReader(out.toString()));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            return sb.toString();
        }
    }

    public static final class SaxHandler implements ContentHandler, LexicalHandler, ErrorHandler {
        private final StringWriter out_;
        private boolean lastWasChar_;

        public SaxHandler(final StringWriter out) {
            out_ = out;
            lastWasChar_ = false;
        }

        @Override
        public void setDocumentLocator(final Locator locator) {
            characters();
        }

        @Override
        public void startDocument() throws SAXException {
            characters();
        }

        @Override
        public void endDocument() throws SAXException {
            characters();
        }

        @Override
        public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
            characters();
        }

        @Override
        public void endPrefixMapping(final String prefix) throws SAXException {
            characters();
        }

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
                throws SAXException {
            characters();

            out_.append('(')
                .append(qName)
                .append("\n");

            final ArrayList<String> attNames = new ArrayList<>();
            for (int i = 0; i < atts.getLength(); i++) {
                attNames.add(atts.getQName(i));
            }

            Collections.sort(attNames);

            for (final String attName : attNames) {
                out_.append('A');
                final int i = atts.getIndex(attName);
                if (atts.getURI(i) != null && atts.getURI(i).length() > 0) {
                    out_.append('{')
                        .append(atts.getURI(i))
                        .append('}');
                }

                out_.append(normalize(atts.getQName(i)))
                    .append(' ')
                    .append(normalize(atts.getValue(i)))
                    .append('\n');
            }
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            characters();

            out_.append(')')
                .append(qName)
                .append("\n");
        }

        @Override
        public void characters(final char[] ch, final int start, final int length) throws SAXException {
            if (lastWasChar_) {
                out_.append(normalize(String.copyValueOf(ch, start, length)));
                return;
            }

            out_.append('"')
                .append(normalize(String.copyValueOf(ch, start, length)));
            lastWasChar_ = true;
        }

        @Override
        public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {
            characters();

            out_.append("# ignorableWhitespace\n");
        }

        @Override
        public void processingInstruction(final String target, final String data) throws SAXException {
            characters();

            out_.append('?')
                .append(target);
            if (data != null && data.length() > 0) {
                out_.append(' ')
                    .append(normalize(data));
            }
            out_.append('\n');
        }

        @Override
        public void skippedEntity(final String name) throws SAXException {
            characters();

            out_.append("# skippedEntity\n");
        }

        @Override
        public void startDTD(final String name, final String publicId, final String systemId) throws SAXException {
            characters();

            out_.append('!');
            boolean addNl = true;
            if (name != null && name.length() > 0) {
                out_.append(normalize(name));
                out_.append('\n');
                addNl = false;
            }
            if (publicId != null && publicId.length() > 0) {
                out_.append('p');
                out_.append(normalize(publicId));
                out_.append('\n');
                addNl = false;
            }
            if (systemId != null && systemId.length() > 0) {
                out_.append('s');
                out_.append(normalize(systemId));
                out_.append('\n');
                addNl = false;
            }
            if (addNl) {
                out_.append('\n');
            }
        }

        @Override
        public void endDTD() throws SAXException {
            characters();

            out_.append("# endDTD\n");
        }

        @Override
        public void startEntity(final String name) throws SAXException {
            characters();

            out_.append("# startEntity\n");
        }

        @Override
        public void endEntity(final String name) throws SAXException {
            characters();

            out_.append("# endEntity\n");
        }

        @Override
        public void startCDATA() throws SAXException {
            characters();

            out_.append("((CDATA\n");
        }

        @Override
        public void endCDATA() throws SAXException {
            characters();

            out_.append("))CDATA\n");
        }

        @Override
        public void comment(final char[] ch, final int start, final int length) throws SAXException {
            characters();

            out_.append('#')
                .append(normalize(String.copyValueOf(ch, start, length)))
                .append('\n');
        }

        private void characters() {
            if (lastWasChar_) {
                out_.append('\n');
                lastWasChar_ = false;
            }
        }

        @Override
        public void warning(final SAXParseException exception) throws SAXException {
            out_.append("# warning\n");
        }

        @Override
        public void error(final SAXParseException exception) throws SAXException {
            out_.append("# error\n");
        }

        @Override
        public void fatalError(final SAXParseException exception) throws SAXException {
            out_.append("# fatalError\n");
        }
    }
}
