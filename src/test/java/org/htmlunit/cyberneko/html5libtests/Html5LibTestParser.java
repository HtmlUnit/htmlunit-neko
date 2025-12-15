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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser for html5lib tree-construction test files.
 *
 * Test file format: - Tests separated by two newlines - Each test starts with
 * #data - Sections: #data, #errors, #new-errors (optional),
 * #script-off/#script-on (optional), #document-fragment (optional), #document
 *
 * @author Ronald Brill
 */
public final class Html5LibTestParser {

    public static class TestCase {
        private String data_;
        private int expectedErrors_;
        private int newErrors_;
        private Boolean scriptingEnabled_; // null = test both modes
        private String fragmentContext_;
        private List<String> expectedTree_;
        private String testName_;

        public TestCase() {
            expectedTree_ = new ArrayList<>();
            expectedErrors_ = 0;
            newErrors_ = 0;
        }

        public String getData() {
            return data_;
        }

        public void setData(final String data) {
            data_ = data;
        }

        public int getExpectedErrors() {
            return expectedErrors_;
        }

        public void setExpectedErrors(final int expectedErrors) {
            expectedErrors_ = expectedErrors;
        }

        public int getNewErrors() {
            return newErrors_;
        }

        public void setNewErrors(final int newErrors) {
            newErrors_ = newErrors;
        }

        public Boolean getScriptingEnabled() {
            return scriptingEnabled_;
        }

        public void setScriptingEnabled(final Boolean scriptingEnabled) {
            scriptingEnabled_ = scriptingEnabled;
        }

        public String getFragmentContext() {
            return fragmentContext_;
        }

        public void setFragmentContext(final String fragmentContext) {
            fragmentContext_ = fragmentContext;
        }

        public List<String> getExpectedTree() {
            return expectedTree_;
        }

        public void addTreeLine(final String line) {
            expectedTree_.add(line);
        }

        public String getTestName() {
            return testName_;
        }

        public void setTestName(final String testName) {
            testName_ = testName;
        }

        public int getTotalExpectedErrors() {
            return expectedErrors_ + newErrors_;
        }
    }

    /**
     * Parse all tests from an html5lib test file.
     */
    public static List<TestCase> parseTestFile(final String content) throws IOException {
        final List<TestCase> tests = new ArrayList<>();
        final BufferedReader reader = new BufferedReader(new StringReader(content));

        TestCase currentTest = null;
        String line;
        String section = null;
        int errorCount = 0;
        int testNumber = 0;

        while ((line = reader.readLine()) != null) {
            // Start of new test
            if ("#data".equals(line)) {
                if (currentTest != null) {
                    tests.add(currentTest);
                }
                currentTest = new TestCase();
                currentTest.setTestName("Test " + (++testNumber));
                section = "data";
                continue;
            }

            if (currentTest == null) {
                continue;
            }

            // Section markers
            if ("#errors".equals(line)) {
                section = "errors";
                errorCount = 0;
                continue;
            }

            if ("#new-errors".equals(line)) {
                currentTest.setExpectedErrors(errorCount);
                section = "new-errors";
                errorCount = 0;
                continue;
            }

            if ("#document-fragment".equals(line)) {
                section = "fragment";
                continue;
            }

            if ("#document".equals(line)) {
                if ("errors".equals(section)) {
                    currentTest.setExpectedErrors(errorCount);
                }
                else if ("new-errors".equals(section)) {
                    currentTest.setNewErrors(errorCount);
                }
                section = "document";
                continue;
            }

            if ("#script-off".equals(line)) {
                currentTest.setScriptingEnabled(false);
                continue;
            }

            if ("#script-on".equals(line)) {
                currentTest.setScriptingEnabled(true);
                continue;
            }

            // Process section content
            switch (section) {
                case "data":
                    if (currentTest.getData() == null) {
                        currentTest.setData(line);
                    }
                    else {
                        currentTest.setData(currentTest.getData() + "\n" + line);
                    }
                    break;

                case "errors":
                case "new-errors":
                    if (!line.isEmpty()) {
                        errorCount++;
                    }
                    break;

                case "fragment":
                    currentTest.setFragmentContext(line);
                    break;

                case "document":
                    if (!line.isEmpty()) {
                        currentTest.addTreeLine(line);
                    }
                    break;
            }
        }

        // Add last test
        if (currentTest != null) {
            tests.add(currentTest);
        }

        return tests;
    }

    /**
     * Parse expected tree structure from test case. Returns a normalized tree
     * structure for comparison.
     */
    public static class TreeNode {
        public enum NodeType {
            DOCUMENT, DOCTYPE, COMMENT, ELEMENT, TEXT
        }

        private NodeType type_;
        private String value_;
        private List<TreeNode> children_;
        private List<Attribute> attributes_;
        private int depth_;

        public TreeNode(final NodeType type, final String value, final int depth) {
            type_ = type;
            value_ = value;
            depth_ = depth;
            children_ = new ArrayList<>();
            attributes_ = new ArrayList<>();
        }

        public NodeType getType() {
            return type_;
        }

        public String getValue() {
            return value_;
        }

        public List<TreeNode> getChildren() {
            return children_;
        }

        public List<Attribute> getAttributes() {
            return attributes_;
        }

        public int getDepth() {
            return depth_;
        }

        public void addChild(final TreeNode child) {
            children_.add(child);
        }

        public void addAttribute(final Attribute attr) {
            attributes_.add(attr);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < depth_; i++) {
                sb.append("  ");
            }
            sb.append("| ");

            switch (type_) {
                case DOCTYPE:
                    sb.append("<!DOCTYPE ").append(value_).append(">");
                    break;
                case COMMENT:
                    sb.append("<!-- ").append(value_).append(" -->");
                    break;
                case ELEMENT:
                    sb.append("<").append(value_).append(">");
                    break;
                case TEXT:
                    sb.append("\"").append(value_).append("\"");
                    break;
            }

            return sb.toString();
        }
    }

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

    /**
     * Parse the expected tree structure from test output lines.
     */
    public static List<TreeNode> parseExpectedTree(final List<String> treeLines) {
        final List<TreeNode> nodes = new ArrayList<>();
        TreeNode lastNode = null;

        for (final String line : treeLines) {
            if (!line.startsWith("| ")) {
                continue;
            }

            // Calculate depth based on leading spaces after "| "
            String content = line.substring(2);
            int depth = 0;
            while (content.startsWith("  ")) {
                depth++;
                content = content.substring(2);
            }

            TreeNode node = null;

            // Parse different node types
            if (content.startsWith("<!DOCTYPE ")) {
                final String doctype = content.substring(10, content.length() - 1);
                node = new TreeNode(TreeNode.NodeType.DOCTYPE, doctype, depth);
            }
            else if (content.startsWith("<!-- ")) {
                final String comment = content.substring(5, content.length() - 4);
                node = new TreeNode(TreeNode.NodeType.COMMENT, comment, depth);
            }
            else if (content.startsWith("<")) {
                final int endPos = content.indexOf('>');
                final String tagName = content.substring(1, endPos);
                node = new TreeNode(TreeNode.NodeType.ELEMENT, tagName, depth);
            }
            else if (content.startsWith("\"")) {
                final String text = content.substring(1, content.length() - 1);
                node = new TreeNode(TreeNode.NodeType.TEXT, text, depth);
            }
            else if (content.contains("=\"")) {
                // Attribute line
                if (lastNode != null && lastNode.getType() == TreeNode.NodeType.ELEMENT) {
                    final int eqPos = content.indexOf('=');
                    final String attrName = content.substring(0, eqPos);
                    final String attrValue = content.substring(eqPos + 2, content.length() - 1);
                    lastNode.addAttribute(new Attribute(attrName, attrValue));
                }
                continue;
            }

            if (node != null) {
                nodes.add(node);
                lastNode = node;
            }
        }

        return nodes;
    }

    private Html5LibTestParser() {
    }
}
