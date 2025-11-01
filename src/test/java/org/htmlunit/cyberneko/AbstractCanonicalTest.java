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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.htmlunit.cyberneko.util.TestUtils;
import org.htmlunit.cyberneko.xerces.dom.CDATASectionImpl;
import org.htmlunit.cyberneko.xerces.dom.CommentImpl;
import org.htmlunit.cyberneko.xerces.dom.CoreDocumentImpl;
import org.htmlunit.cyberneko.xerces.dom.DocumentFragmentImpl;
import org.htmlunit.cyberneko.xerces.dom.DocumentTypeImpl;
import org.htmlunit.cyberneko.xerces.dom.NodeImpl;
import org.htmlunit.cyberneko.xerces.dom.ProcessingInstructionImpl;
import org.htmlunit.cyberneko.xerces.dom.TextImpl;
import org.junit.jupiter.params.provider.Arguments;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This test generates canonical result using the <code>Writer</code> class
 * and compares it against the expected canonical output. Simple as that.
 *
 * @author Andy Clark
 * @author Marc Guillemot
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
public abstract class AbstractCanonicalTest {
    protected static final File DATA_DIR
                        = new File("src/test/resources/org/htmlunit/cyberneko/testfiles");
    protected static final File OUTOUT_DIR
                        = new File("target/data/output");

    protected static Stream<Arguments> testFiles() {
        OUTOUT_DIR.mkdirs();

        final List<File> dataFiles = new ArrayList<>();
        DATA_DIR.listFiles(new FileFilter() {
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

    protected static String getCanonical(final File infile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(infile), StandardCharsets.UTF_8))) {
            final StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }

    protected static void write(final StringBuilder out, final CoreDocumentImpl doc) {
        if (doc.getXmlEncoding() != null && doc.getXmlEncoding().length() > 0) {
            out.append("xencoding ");
            out.append(TestUtils.normalize(doc.getXmlEncoding()));
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

    protected static void write(final StringBuilder out, final DocumentFragmentImpl doc) {
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
            .append(TestUtils.normalize(text.getTextContent()))
            .append('\n');
    }

    private static void write(final StringBuilder out, final CDATASectionImpl cdata) {
        out.append("((CDATA\n\"")
            .append(TestUtils.normalize(cdata.getTextContent()))
            .append('\n')
            .append("))CDATA\n");
    }

    private static void write(final StringBuilder out, final CommentImpl comment) {
        out.append('#')
            .append(TestUtils.normalize(comment.getNodeValue()))
            .append('\n');
    }

    private static void write(final StringBuilder out, final ProcessingInstructionImpl processingInstruction) {
        out.append('?')
            .append(processingInstruction.getTarget());
        if (processingInstruction.getData() != null && processingInstruction.getData().length() > 0) {
            out.append(' ')
                .append(TestUtils.normalize(processingInstruction.getData()));
        }
        out.append('\n');
    }

    private static void write(final StringBuilder out, final DocumentTypeImpl documentType) {
        out.append('!');
        boolean addNl = true;
        if (documentType.getName() != null && documentType.getName().length() > 0) {
            out.append(TestUtils.normalize(documentType.getName()));
            out.append('\n');
            addNl = false;
        }
        if (documentType.getPublicId() != null && documentType.getPublicId().length() > 0) {
            out.append('p');
            out.append(TestUtils.normalize(documentType.getPublicId()));
            out.append('\n');
            addNl = false;
        }
        if (documentType.getSystemId() != null && documentType.getSystemId().length() > 0) {
            out.append('s');
            out.append(TestUtils.normalize(documentType.getSystemId()));
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

        out.append(TestUtils.normalize(attr.getName()))
            .append(' ')
            .append(TestUtils.normalize(attr.getValue()))
            .append('\n');
    }
}
