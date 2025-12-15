/*
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
package org.htmlunit.cyberneko.html5libtests;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.htmlunit.cyberneko.HTMLScanner;
import org.htmlunit.cyberneko.parsers.SAXParser;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLInputSource;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * Test runner that executes html5lib tests against htmlunit-neko SAX parser.
 *
 * @author Ronald Brill
 */
public class Html5LibTestRunner {

    /**
     * SAX ContentHandler that builds a tree structure compatible with html5lib
     * output format.
     */
    public static class TreeBuildingHandler implements ContentHandler, LexicalHandler {
        private List<TreeNode> nodes_ = new ArrayList<>();
        private List<TreeNode> stack_ = new ArrayList<>();
        private int errorCount_ = 0;
        private StringBuilder currentText = null;

        public TreeBuildingHandler() {
            // Document root
            final TreeNode doc = new TreeNode(TreeNode.NodeType.DOCUMENT, "#document", 0);
            nodes_.add(doc);
            stack_.add(doc);
        }

        public List<TreeNode> getNodes() {
            return nodes_;
        }

        public int getErrorCount() {
            return errorCount_;
        }

        private int getCurrentDepth() {
            return stack_.size() - 1;
        }

        private void flushText() {
            if (currentText != null && currentText.length() > 0) {
                final TreeNode textNode =
                        new TreeNode(TreeNode.NodeType.TEXT, currentText.toString(), getCurrentDepth());
                nodes_.add(textNode);
                currentText = null;
            }
        }

        @Override
        public void startElement(final String uri, final String localName,
                        final String qName, final Attributes atts) throws SAXException {
            flushText();

            final TreeNode element = new TreeNode(TreeNode.NodeType.ELEMENT,
                                        localName.isEmpty() ? qName : localName, getCurrentDepth());

            // Add attributes sorted lexicographically
            final List<TreeNode.Attribute> attributes = new ArrayList<>();
            for (int i = 0; i < atts.getLength(); i++) {
                String attrName = atts.getLocalName(i);
                if (attrName == null || attrName.isEmpty()) {
                    attrName = atts.getQName(i);
                }
                String attrValue = atts.getValue(i);
                if (attrValue == null) {
                    attrValue = "";
                }
                attributes.add(new TreeNode.Attribute(attrName, attrValue));
            }

            // Sort attributes by name (lexicographically)
            Collections.sort(attributes, new Comparator<TreeNode.Attribute>() {
                @Override
                public int compare(final TreeNode.Attribute a1, final TreeNode.Attribute a2) {
                    return a1.getName().compareTo(a2.getName());
                }
            });

            for (final TreeNode.Attribute attr : attributes) {
                element.addAttribute(attr);
            }

            nodes_.add(element);
            stack_.add(element);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            flushText();
            if (!stack_.isEmpty() && stack_.size() > 1) { // Don't remove document root
                stack_.remove(stack_.size() - 1);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String text = new String(ch, start, length);
            if (!text.isEmpty()) {
                if (currentText == null) {
                    currentText = new StringBuilder();
                }
                currentText.append(text);
            }
        }

        @Override
        public void comment(char[] ch, int start, int length) throws SAXException {
            flushText();
            String comment = new String(ch, start, length);
            TreeNode commentNode = new TreeNode(TreeNode.NodeType.COMMENT, comment, getCurrentDepth());
            nodes_.add(commentNode);
        }

        @Override
        public void startDTD(String name, String publicId, String systemId) throws SAXException {
            StringBuilder doctype = new StringBuilder();
            if (name != null && !name.isEmpty()) {
                doctype.append(name);
            }

            // Format: name "publicId" "systemId"
            if (publicId != null && !publicId.isEmpty()) {
                if (doctype.length() > 0) {
                    doctype.append(" ");
                }
                doctype.append("\"").append(publicId).append("\"");

                if (systemId != null && !systemId.isEmpty()) {
                    doctype.append(" \"").append(systemId).append("\"");
                }
            }
            else if (systemId != null && !systemId.isEmpty()) {
                if (doctype.length() > 0) {
                    doctype.append(" ");
                }
                // Empty publicId before systemId
                doctype.append("\"\" \"").append(systemId).append("\"");
            }

            TreeNode doctypeNode = new TreeNode(TreeNode.NodeType.DOCTYPE, doctype.toString(), 0);
            nodes_.add(doctypeNode);
        }

        @Override
        public void endDTD() throws SAXException {
            // Nothing to do
        }

        @Override
        public void startCDATA() throws SAXException {
            // CDATA sections are treated as text
        }

        @Override
        public void endCDATA() throws SAXException {
            // CDATA sections are treated as text
        }

        @Override
        public void startEntity(String name) throws SAXException {
            // Not needed for tree construction
        }

        @Override
        public void endEntity(String name) throws SAXException {
            // Not needed for tree construction
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            // Not needed for basic tree construction
        }

        @Override
        public void startDocument() throws SAXException {
            // Already initialized in constructor
        }

        @Override
        public void endDocument() throws SAXException {
            flushText();
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            // Not needed for basic tree construction
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
            // Not needed for basic tree construction
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            // Treat as regular characters for html5lib compatibility
            characters(ch, start, length);
        }

        @Override
        public void processingInstruction(final String target, final String data) throws SAXException {
            // Processing instructions are rare in HTML but could be added if needed
        }

        @Override
        public void skippedEntity(final String name) throws SAXException {
            // Not typically needed for HTML parsing
        }
    }

    /**
     * Tree node for building the parsed structure.
     */
    public static class TreeNode {
        public enum NodeType {
            DOCUMENT, DOCTYPE, COMMENT, ELEMENT, TEXT
        }

        private NodeType type_;
        private String value_;
        private List<Attribute> attributes_;
        private int depth_;

        public TreeNode(final NodeType type, final String value, final int depth) {
            type_ = type;
            value_ = value;
            depth_ = depth;
            attributes_ = new ArrayList<>();
        }

        public NodeType getType() {
            return type_;
        }

        public String getValue() {
            return value_;
        }

        public List<Attribute> getAttributes() {
            return attributes_;
        }

        public int getDepth() {
            return depth_;
        }

        public void addAttribute(final Attribute attr) {
            attributes_.add(attr);
        }

        /**
         * Convert to html5lib output format. Format: "| " + (depth * " ") + content
         */
        public List<String> toOutputLines() {
            final List<String> lines = new ArrayList<>();

            switch (type_) {
                case DOCUMENT:
                    // Document node is implicit in html5lib format, not printed
                    return lines;

                case DOCTYPE:
                    lines.add(formatLine("<!DOCTYPE " + value_ + ">"));
                    break;

                case COMMENT:
                    lines.add(formatLine("<!-- " + value_ + " -->"));
                    break;

                case ELEMENT:
                    lines.add(formatLine("<" + value_ + ">"));

                    // Add attribute lines (one level deeper)
                    for (final Attribute attr : attributes_) {
                        lines.add(formatLine(attr.getName() + "=\"" + attr.getValue() + "\"", depth_ + 1));
                    }
                    break;

                case TEXT:
                    lines.add(formatLine("\"" + value_ + "\""));
                    break;
            }

            return lines;
        }

        private String formatLine(final String content) {
            return formatLine(content, this.depth_);
        }

        private String formatLine(final String content, final int lineDepth) {
            final StringBuilder sb = new StringBuilder("| ");
            for (int i = 0; i < lineDepth; i++) {
                sb.append("  ");
            }
            sb.append(content);
            return sb.toString();
        }

        @Override
        public String toString() {
            return toOutputLines().isEmpty() ? "" : toOutputLines().get(0);
        }

        /**
         * Attribute of an element.
         */
        public static class Attribute {
            private String name_;
            private String value_;

            public Attribute(final String name, final String value) {
                name_ = name;
                value_ = value;
            }

            public String getName() {
                return name_;
            }

            public String getValue() {
                return value_;
            }

            @Override
            public String toString() {
                return name_ + "=\"" + value_ + "\"";
            }
        }

    }

    /**
     * Result of running a single test.
     */
    public static class TestResult {
        private boolean passed_;
        private String message_;
        private List<String> expectedOutput_;
        private List<String> actualOutput_;
        private Exception exception_;

        public TestResult(final boolean passed, final String message) {
            passed_ = passed;
            message_ = message;
        }

        public boolean isPassed() {
            return passed_;
        }

        public String getMessage() {
            return message_;
        }

        public void setExpectedOutput(final List<String> expected) {
            expectedOutput_ = expected;
        }

        public void setActualOutput(final List<String> actual) {
            actualOutput_ = actual;
        }

        public List<String> getExpectedOutput() {
            return expectedOutput_;
        }

        public List<String> getActualOutput() {
            return actualOutput_;
        }

        public void setException(final Exception e) {
            exception_ = e;
        }

        public Exception getException() {
            return exception_;
        }
    }

    /**
     * Run a single test case.
     */
    public static TestResult runTest(final Html5LibTestParser.TestCase test) {
        try {
            final SAXParser parser = new SAXParser();
            final TreeBuildingHandler handler = new TreeBuildingHandler();

            // Set up handlers
            parser.setContentHandler(handler);
            parser.setProperty(HTMLScanner.NAMES_ELEMS, "lower");
            parser.setProperty("http://xml.org/sax/properties/lexical-handler", handler);

            if (test.getScriptingEnabled() != null) {
                final boolean scriptingEnabled = test.getScriptingEnabled();
                parser.setFeature(HTMLScanner.PARSE_NOSCRIPT_CONTENT, !scriptingEnabled);
            }

            // parser.setFeature("http://cyberneko.org/html/features/balance-tags/document-fragment", true);

            // Parse the HTML
            String html = test.getData();
            if (html == null) {
                html = "";
            }

            final StringReader reader = new StringReader(html);
            final XMLInputSource source = new XMLInputSource(null, "test", null, reader, null);
            parser.parse(source);

            // Build output from parsed tree
            final List<String> actualOutput = new ArrayList<>();
            for (final TreeNode node : handler.getNodes()) {
                actualOutput.addAll(node.toOutputLines());
            }

            // Get expected output
            final List<String> expectedOutput = test.getExpectedTree();

            // Compare tree structures
            return compareTreeOutput(expectedOutput, actualOutput);

        }
        catch (final Exception e) {
            final TestResult result = new TestResult(false, "Exception during parsing: " + e.getMessage());
            result.setException(e);
            return result;
        }
    }

    /**
     * Compare expected and actual tree output.
     */
    private static TestResult compareTreeOutput(final List<String> expected, final List<String> actual) {
        // Check size first
        if (actual.size() != expected.size()) {
            final TestResult result = new TestResult(false, String
                    .format("Tree size mismatch: expected %d lines, got %d lines", expected.size(), actual.size()));
            result.setExpectedOutput(expected);
            result.setActualOutput(actual);
            return result;
        }

        // Compare line by line
        for (int i = 0; i < expected.size(); i++) {
            final String expectedLine = expected.get(i);
            final String actualLine = actual.get(i);

            if (!expectedLine.equals(actualLine)) {
                final TestResult result = new TestResult(false, String
                        .format("Line %d mismatch:%n  Expected: %s%n  Actual:   %s", i + 1, expectedLine, actualLine));
                result.setExpectedOutput(expected);
                result.setActualOutput(actual);
                return result;
            }
        }

        // All lines match
        final TestResult result = new TestResult(true, "Test passed");
        result.setExpectedOutput(expected);
        result.setActualOutput(actual);
        return result;
    }

    /**
     * Run a test with detailed output for debugging.
     */
    public static TestResult runTestVerbose(Html5LibTestParser.TestCase test) {
        System.out.println("=== Running Test ===");
        System.out.println("Input HTML:");
        System.out.println(test.getData());
        System.out.println();

        TestResult result = runTest(test);

        System.out.println("Expected Output:");
        if (result.getExpectedOutput() != null) {
            for (String line : result.getExpectedOutput()) {
                System.out.println(line);
            }
        }
        System.out.println();

        System.out.println("Actual Output:");
        if (result.getActualOutput() != null) {
            for (String line : result.getActualOutput()) {
                System.out.println(line);
            }
        }
        System.out.println();

        System.out.println("Result: " + (result.isPassed() ? "PASSED" : "FAILED"));
        if (!result.isPassed()) {
            System.out.println("Reason: " + result.getMessage());
            if (result.getException() != null) {
                result.getException().printStackTrace();
            }
        }
        System.out.println();

        return result;
    }
}
