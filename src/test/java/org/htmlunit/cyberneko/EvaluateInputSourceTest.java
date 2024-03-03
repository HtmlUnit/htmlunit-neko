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

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.htmlunit.cyberneko.html.dom.HTMLDocumentImpl;
import org.htmlunit.cyberneko.parsers.DOMParser;
import org.htmlunit.cyberneko.xerces.xni.Augmentations;
import org.htmlunit.cyberneko.xerces.xni.QName;
import org.htmlunit.cyberneko.xerces.xni.XNIException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLDocumentFilter;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLInputSource;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the parser evaluateInputSource() support.
 *
 * @author Ronald Brill
 */
public class EvaluateInputSourceTest {
    private static final String NL = System.lineSeparator();

    @Test
    public void simpleTags() throws Exception {
        final String html =
                "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<title>Test</title>"
                + "</head>"
                + "<body>"
                + "  <script></script>"
                + "  bar"
                + "</body>"
                + "</html>";

        final String htmlEval =
                "<p>paragraph</p>";

        final String expected =
                "!html" + NL
                + "(html" + NL
                + "(head" + NL
                + "(title" + NL
                + "\"Test" + NL
                + ")title" + NL
                + ")head" + NL
                + "(body" + NL
                + "\"  " + NL
                + "(script" + NL
                + ")script" + NL
                + "(p" + NL
                + "\"paragraph" + NL
                + ")p" + NL
                + "\"  bar" + NL
                + ")body" + NL
                + ")html";

        doTest(html, htmlEval, expected);
    }

    @Test
    public void plaintext() throws Exception {
        final String html =
                "<html>"
                + "<head>"
                + "<script>"
                + "</script>"
                + "<title>Test</title>"
                + "</head>"
                + "<body>"
                + "  bar"
                + "</body>"
                + "</html>";

        final String htmlEval =
                "<plaintext>foo";

        final String expected =
                "(html" + NL
                + "(head" + NL
                + "(script" + NL
                + ")script" + NL
                + ")head" + NL
                + "(BODY" + NL
                + "(plaintext" + NL
                + "\"foo<title>Test</title></head><body>  bar</body></html>" + NL
                + ")plaintext" + NL
                + ")BODY" + NL
                + ")html";

        doTest(html, htmlEval, expected);
    }

    public static void doTest(final String html, final String htmlEval, final String expected) throws Exception {
        final DOMParser parser = new DOMParser(HTMLDocumentImpl.class);

        final StringWriter out = new StringWriter();
        final Writer writer = new Writer(out) {
            @Override
            public void endElement(final QName element, final Augmentations augs) throws XNIException {
                super.endElement(element, augs);

                if ("script".equals(element.getRawname())) {
                    final XMLInputSource in = new XMLInputSource(null, null, null, new StringReader(htmlEval), StandardCharsets.UTF_8.name());
                    ((HTMLConfiguration) parser.getXMLParserConfiguration()).evaluateInputSource(in);
                }
            }
        };
        final XMLDocumentFilter[] filters = {writer};
        parser.setProperty("http://cyberneko.org/html/properties/filters", filters);

        final StringReader sr = new StringReader(html);
        final XMLInputSource in = new XMLInputSource(null, "foo", null, sr, null);
        parser.parse(in);

        assertEquals(expected.trim(), out.toString().trim());
    }
}
