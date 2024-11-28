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
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.htmlunit.cyberneko.xerces.xni.Augmentations;
import org.htmlunit.cyberneko.xerces.xni.NamespaceContext;
import org.htmlunit.cyberneko.xerces.xni.QName;
import org.htmlunit.cyberneko.xerces.xni.XMLAttributes;
import org.htmlunit.cyberneko.xerces.xni.XMLDocumentHandler;
import org.htmlunit.cyberneko.xerces.xni.XMLLocator;
import org.htmlunit.cyberneko.xerces.xni.XMLString;
import org.htmlunit.cyberneko.xerces.xni.XNIException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLDocumentSource;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLInputSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;

/**
 * This test generates canonical result using the <code>XNI</code> interface
 * and compares it against the expected canonical output. Simple as that.
 *
 * @author Ronald Brill
 */
public class CanonicalXNITest extends AbstractCanonicalTest {

    @ParameterizedTest
    @MethodSource("testFiles")
    public void runTest(final File dataFile) throws Exception {
        final String domDataLines = getResult(dataFile);

        try {
            // prepare for future changes where canonical files are next to test file
            File canonicalFile = new File(dataFile.getParentFile(), dataFile.getName() + ".canonical-xni");
            if (!canonicalFile.exists()) {
                canonicalFile = new File(canonicalDir, dataFile.getName() + ".canonical-xni");
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
            String path = dataFile.getAbsolutePath();
            path = path.substring(path.indexOf("\\testfiles\\") + 11);
            final File output = new File(outputDir, path + ".canonical-xni");
            Files.createDirectories(Paths.get(output.getParentFile().getPath()));
            try (PrintWriter pw = new PrintWriter(Files.newOutputStream(output.toPath()))) {
                pw.print(domDataLines);
            }
            throw e;
        }
    }

    private static String getResult(final File infile) throws Exception {
        try (StringWriter out = new StringWriter()) {

            // parse
            final XNIParser parser = new XNIParser(out);

            HTMLConfiguration htmlConfiguration = new HTMLConfiguration();
            htmlConfiguration.setFeature(HTMLConfiguration.AUGMENTATIONS, true);
            htmlConfiguration.setDocumentHandler(parser);
            htmlConfiguration.parse(new XMLInputSource(null, infile.toString(), null));

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

    private static class XNIParser implements XMLDocumentHandler {
        private StringWriter out_;

        /** Default constructor. */
        XNIParser(StringWriter out) {
            out_ = out;
        }

        @Override
        public void startDocument(XMLLocator locator, String encoding, NamespaceContext namespaceContext, Augmentations augs) throws XNIException {
            out_.append("startDocument ");
            appendAugmentations(augs);
            out_.append('\n');
        }

        @Override
        public void xmlDecl(String version, String encoding, String standalone, Augmentations augs) throws XNIException {
            out_.append("xmlDecl ");
            appendAugmentations(augs);
            out_.append('\n');
        }

        @Override
        public void doctypeDecl(String rootElement, String publicId, String systemId, Augmentations augs) throws XNIException {
            out_.append("doctypeDecl ");
            appendAugmentations(augs);
            out_.append('\n');
        }

        @Override
        public void comment(XMLString text, Augmentations augs) throws XNIException {
            out_.append("comment '").append(text.toString()).append('\'');
            appendAugmentations(augs);
            out_.append('\n');
        }

        @Override
        public void processingInstruction(String target, XMLString data, Augmentations augs) throws XNIException {
            out_.append("processingInstruction ");
            appendAugmentations(augs);
            out_.append('\n');
        }

        @Override
        public void startElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException {
            out_.append("startElement (").append(element.toString()).append(") ");
            appendAugmentations(augs);
            out_.append('\n');
        }

        @Override
        public void emptyElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException {
            out_.append("emptyElement (").append(element.toString()).append(") ");
            appendAugmentations(augs);
            out_.append('\n');
        }

        @Override
        public void characters(XMLString text, Augmentations augs) throws XNIException {
            out_.append("characters '").append(text.toString()).append('\'');
            appendAugmentations(augs);
            out_.append('\n');
        }

        @Override
        public void endElement(QName element, Augmentations augs) throws XNIException {
            out_.append("endElement (").append(element.toString()).append(") ");
            appendAugmentations(augs);
            out_.append('\n');
        }

        @Override
        public void startCDATA(Augmentations augs) throws XNIException {
            out_.append("startCDATA ");
            appendAugmentations(augs);
            out_.append('\n');
        }

        @Override
        public void endCDATA(Augmentations augs) throws XNIException {
            out_.append("endCDATA ");
            appendAugmentations(augs);
            out_.append('\n');
        }

        @Override
        public void endDocument(Augmentations augs) throws XNIException {
            out_.append("endDocument ");
            appendAugmentations(augs);
            out_.append('\n');
        }

        @Override
        public void setDocumentSource(XMLDocumentSource source) {
        }

        @Override
        public XMLDocumentSource getDocumentSource() {
            return null;
        }

        private void appendAugmentations(final Augmentations augs) throws XNIException {
            if (augs == null) {
                out_.append("[no augs]");
                return;
            }

            out_
                .append("[(")
                .append(Integer.toString(augs.getBeginLineNumber()))
                .append(',')
                .append(Integer.toString(augs.getBeginColumnNumber()))
                .append(',')
                .append(Integer.toString(augs.getBeginCharacterOffset()))
                .append(") (")
                .append(Integer.toString(augs.getEndLineNumber()))
                .append(',')
                .append(Integer.toString(augs.getEndColumnNumber()))
                .append(',')
                .append(Integer.toString(augs.getEndCharacterOffset()))
                .append(") ")
                .append(Boolean.toString(augs.isSynthesized()))
                .append(']');
        }
    }
}
