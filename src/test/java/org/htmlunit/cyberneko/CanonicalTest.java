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

import org.htmlunit.cyberneko.parsers.DOMParser;
import org.htmlunit.cyberneko.xerces.dom.CDATASectionImpl;
import org.htmlunit.cyberneko.xerces.dom.CommentImpl;
import org.htmlunit.cyberneko.xerces.dom.CoreDocumentImpl;
import org.htmlunit.cyberneko.xerces.dom.DocumentTypeImpl;
import org.htmlunit.cyberneko.xerces.dom.ElementNSImpl;
import org.htmlunit.cyberneko.xerces.dom.NodeImpl;
import org.htmlunit.cyberneko.xerces.dom.ProcessingInstructionImpl;
import org.htmlunit.cyberneko.xerces.dom.TextImpl;
import org.htmlunit.cyberneko.xerces.util.DefaultErrorHandler;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLDocumentFilter;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLInputSource;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLParserConfiguration;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.opentest4j.AssertionFailedError;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.webkit.dom.CharacterDataImpl;

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

    @TestFactory
    public Iterable<DynamicTest> suite() {
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

        final List<DynamicTest> tests = new ArrayList<>();
        for (final File dataFile : dataFiles) {
            tests.add(DynamicTest.dynamicTest(dataFile.getName(), () -> runTest(dataFile)));
            tests.add(DynamicTest.dynamicTest("[dom] "+ dataFile.getName(), () -> runDomTest(dataFile)));
        }
        return tests;
    }

    protected void runTest(final File dataFile) throws Exception {
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

            final File nyiFile = new File(dataFile.getParentFile(), dataFile.getName() + ".notyetimplemented");
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

    protected void runDomTest(final File dataFile) throws Exception {
        final String domDataLines = getDomResult(dataFile);

        try {
            // prepare for future changes where canonical files are next to test file
            File canonicalFile = new File(dataFile.getParentFile(), dataFile.getName() + ".canonical-dom");
            if (!canonicalFile.exists()) {
                canonicalFile = new File(dataFile.getParentFile(), dataFile.getName() + ".canonical");

                if (!canonicalFile.exists()) {
                    canonicalFile = new File(canonicalDir, dataFile.getName() + "-dom");

                    if (!canonicalFile.exists()) {
                        canonicalFile = new File(canonicalDir, dataFile.getName());
                    }
                }
            }

            if (!canonicalFile.exists()) {
                fail("Canonical file not found for input: " + dataFile.getAbsolutePath() + ": " + domDataLines);
            }

            final File nyiFile = new File(dataFile.getParentFile(), dataFile.getName() + ".notyetimplemented-dom");
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

            CoreDocumentImpl doc = (CoreDocumentImpl) parser.getDocument();

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

    private static void write(final StringBuilder out, final CoreDocumentImpl doc) {
        if (doc.getXmlEncoding() != null && doc.getXmlEncoding().length() > 0) {
            out.append("xencoding ");
            out.append(normalize(doc.getXmlEncoding()));
            out.append('\n');
        }

        NodeList childNodes = doc.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);

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
        out.append('(');
        out.append(node.getNodeName()).append("\n");

        // attributes
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);

                if (attribute instanceof Attr) {
                    write(out, (Attr) attribute);
                }
                else {
                    throw new RuntimeException("");
                }
            }
        }

        // child nodes
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);

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

    private static String normalize(final String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }
}
