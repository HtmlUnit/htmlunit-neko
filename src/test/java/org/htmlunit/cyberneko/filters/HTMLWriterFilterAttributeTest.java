/*
 * Copyright (c) 2017-2026 Ronald Brill
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.htmlunit.cyberneko.HTMLElements;
import org.htmlunit.cyberneko.html.dom.HTMLDocumentImpl;
import org.htmlunit.cyberneko.parsers.DOMParser;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLDocumentFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * The attribute values of a serialized document have to be the same as the ones of the parsed input:
 * the scanner decodes character references in attribute values, so the writer has to encode them again.
 * Otherwise parsing the output decodes a second time, e.g. {@code href="&amp;#106;avascript:alert(1)"}
 * (value {@code &#106;avascript:alert(1)}) would turn into {@code javascript:alert(1)}.
 *
 * @author Ronald Brill
 */
public class HTMLWriterFilterAttributeTest {

    private static Document parse(final String html) throws Exception {
        final DOMParser parser = new DOMParser(HTMLDocumentImpl.class);
        parser.parse(new InputSource(new StringReader(html)));
        return parser.getDocument();
    }

    private static String write(final String html) throws Exception {
        final StringWriter out = new StringWriter();
        final DOMParser parser = new DOMParser(HTMLDocumentImpl.class);
        parser.setProperty("http://cyberneko.org/html/properties/filters",
                new XMLDocumentFilter[] {new HTMLWriterFilter(out, "UTF-8", new HTMLElements())});
        parser.parse(new InputSource(new StringReader(html)));
        return out.toString();
    }

    private static List<String> attributes(final Node node, final List<String> result) {
        if (node instanceof Element element) {
            final NamedNodeMap attributes = element.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                final Node attribute = attributes.item(i);
                result.add(element.getTagName().toLowerCase(Locale.ROOT) + "@" + attribute.getNodeName()
                        + "=" + attribute.getNodeValue());
            }
        }
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            attributes(child, result);
        }
        return result;
    }

    private static void assertRoundTrip(final String html) throws Exception {
        final List<String> expected = attributes(parse(html), new ArrayList<>());
        final String serialized = write(html);
        assertEquals(expected, attributes(parse(serialized), new ArrayList<>()), html + " -> " + serialized);
    }

    /** The case from the report. */
    @Test
    public void encodedCharacterReferenceStaysText() throws Exception {
        final String html = "<a href=\"&amp;#106;avascript:alert(1)\">x</a>";

        assertEquals("&#106;avascript:alert(1)",
                ((Element) parse(html).getElementsByTagName("a").item(0)).getAttribute("href"));

        final String serialized = write(html);
        assertTrue(serialized.contains("href=\"&amp;#106;avascript:alert(1)\""), serialized);
        assertEquals("&#106;avascript:alert(1)",
                ((Element) parse(serialized).getElementsByTagName("a").item(0)).getAttribute("href"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "<a href=\"&amp;#106;avascript:alert(1)\">x</a>",
        "<a href=\"&amp;#x6a;avascript:alert(1)\">x</a>",
        "<a title=\"&amp;lt;img src=x onerror=alert(1)&amp;gt;\">x</a>",
        "<a title='&amp;quot; onmouseover=alert(1) x=&amp;quot;'>x</a>",
        "<a title=\"&amp;amp;amp;\">x</a>",
        "<a title=\"a&amp;b\">x</a>",
        "<a title=\"a&b\">x</a>",
        "<a href=\"x?a=1&amp;not=2\">x</a>",
        "<a href=\"x&notin;y&amp;not\">x</a>",
        "<a title=\"&quot;q&quot; &#39;s&#39; &lt;&gt; &nbsp;x\">x</a>",
        "<a title=\"\">x</a>",
        "<a title=x&amp;amp;y>x</a>",
        "<img alt='&amp;#60;script&amp;#62;'>",
    })
    public void attributeValuesAreStable(final String html) throws Exception {
        assertRoundTrip(html);
    }

    /** Random mixtures of everything that has a meaning inside an attribute value. */
    @Test
    public void randomAttributeValuesAreStable() throws Exception {
        final String[] parts = {"&", "&amp;", "&#", "&#106;", "&#x6a;", "#", ";", "amp", "lt", "gt", "quot", "not",
            "notin", "\"", "'", "<", ">", "=", "a", "1", " ", "&amp;#106;", "&lt;", "&quot;", "\u00a0"};
        final Random random = new Random(42);
        for (int i = 0; i < 2_000; i++) {
            final StringBuilder value = new StringBuilder();
            final int count = 1 + random.nextInt(8);
            for (int j = 0; j < count; j++) {
                value.append(parts[random.nextInt(parts.length)]);
            }
            assertRoundTrip("<a title=\"" + value.toString().replace("\"", "&quot;") + "\">x</a>");
        }
    }
}