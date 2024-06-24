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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.htmlunit.cyberneko.filters.DefaultFilter;
import org.htmlunit.cyberneko.xerces.xni.Augmentations;
import org.htmlunit.cyberneko.xerces.xni.QName;
import org.htmlunit.cyberneko.xerces.xni.XMLAttributes;
import org.htmlunit.cyberneko.xerces.xni.XNIException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLDocumentFilter;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLInputSource;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLParserConfiguration;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link HTMLScanner}.
 * @author Marc Guillemot
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
public class HTMLScannerTest {

    @Test
    public void isEncodingCompatible() {
        assertTrue(HTMLScanner.isEncodingCompatible("ISO-8859-1", "ISO-8859-1"));
        assertTrue(HTMLScanner.isEncodingCompatible("UTF-8", "UTF-8"));
        assertTrue(HTMLScanner.isEncodingCompatible("UTF-16", "UTF-16"));
        assertTrue(HTMLScanner.isEncodingCompatible("US-ASCII", "ISO-8859-1"));
        assertTrue(HTMLScanner.isEncodingCompatible("UTF-8", "ISO-8859-1"));

        assertFalse(HTMLScanner.isEncodingCompatible("UTF-8", "UTF-16"));
        assertFalse(HTMLScanner.isEncodingCompatible("ISO-8859-1", "UTF-16"));
        assertFalse(HTMLScanner.isEncodingCompatible("UTF-16", "Cp1252"));
    }

    @Test
    public void evaluateInputSource() throws Exception {
        final String string = "<html><head><title>foo</title></head>"
            + "<body>"
            + "<script id='myscript'>"
            + "  document.write('<style type=\"text/css\" id=\"myStyle\">');"
            + "  document.write('  .nwr {white-space: nowrap;}');"
            + "  document.write('</style>');"
            + "  document.write('<div id=\"myDiv\"><span></span>');"
            + "  document.write('</div>');"
            + "</script>"
            + "<div><a/></div>"
            + "</body></html>";
        final HTMLConfiguration parser = new HTMLConfiguration();
        final EvaluateInputSourceFilter filter = new EvaluateInputSourceFilter(parser);
        parser.setProperty("http://cyberneko.org/html/properties/filters", new XMLDocumentFilter[] {filter});
        final XMLInputSource source = new XMLInputSource(null, "myTest", null, new StringReader(string), "UTF-8");
        parser.parse(source);

        final String[] expectedString = {"(html", "(head", "(title", ")title", ")head", "(body", "(script",
            ")script", "~inserting", "(style", "~inserting", "~inserting", ")style", "~inserting",
            "(div", "(span", ")span", "~inserting", ")div", "(div", "(a", ")a", ")div", ")body", ")html"};
        assertEquals(Arrays.asList(expectedString), filter.collectedStrings_);
    }

    /**
     * Ensure that the current locale doesn't affect the HTML tags.
     * see issue https://sourceforge.net/tracker/?func=detail&atid=952178&aid=3544334&group_id=195122
     * @throws Exception on error
     */
    @Test
    public void locale() throws Exception {
        final Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));
            final String string = "<html><head><title>foo</title></head>"
                + "<body>"
                + "</body></html>";
            final HTMLConfiguration parser = new HTMLConfiguration();
            final EvaluateInputSourceFilter filter = new EvaluateInputSourceFilter(parser);
            parser.setProperty("http://cyberneko.org/html/properties/filters", new XMLDocumentFilter[] {filter});
            final XMLInputSource source = new XMLInputSource(null, "myTest", null, new StringReader(string), "UTF-8");
            parser.parse(source);

            final String[] expectedString = {"(html", "(head", "(title", ")title", ")head", "(body", ")body", ")html"};
            assertEquals(Arrays.asList(expectedString).toString(), filter.collectedStrings_.toString());
        }
        finally {
            Locale.setDefault(originalLocale);
        }
    }

    /**
     * Tests handling of xml declaration when used with Reader.
     * Following test caused NPE with release 1.9.11.
     * Regression test for [ 2503982 ] NPE when parsing from a CharacterStream
     */
    @Test
    public void changeEncodingWithReader() throws Exception {
        final String string = "<?xml version='1.0' encoding='UTF-8'?><html><head><title>foo</title></head>"
            + "</body></html>";

        final XMLInputSource source = new XMLInputSource(null, "myTest", null, new StringReader(string), "ISO8859-1");
        final HTMLConfiguration parser = new HTMLConfiguration();
        parser.parse(source);
    }

    private static class EvaluateInputSourceFilter extends DefaultFilter {

        private static int Counter_ = 1;

        final List<String> collectedStrings_ = new ArrayList<>();
        private final HTMLConfiguration configuration_;

        EvaluateInputSourceFilter(final HTMLConfiguration config) {
            configuration_ = config;
        }

        @Override
        public void startElement(final QName element, final XMLAttributes attrs, final Augmentations augs) throws XNIException {
            collectedStrings_.add("(" + element.getRawname());
        }

        @Override
        public void endElement(final QName element, final Augmentations augs) throws XNIException {
            collectedStrings_.add(")" + element.getRawname());
            if ("SCRIPT".equalsIgnoreCase(element.getLocalpart())) {
                // act as if evaluation of document.write would insert the content
                insert("<style type=\"text/css\" id=\"myStyle\">");
                insert("  .nwr {white-space: nowrap;}");
                insert("</style>");
                insert("<div id=\"myDiv\"><span></span>");
                insert("</div>");
            }
        }

        private void insert(final String string) {
            collectedStrings_.add("~inserting");
            final XMLInputSource source = new XMLInputSource(null, "myTest" + Counter_++, null,
                                                      new StringReader(string), "UTF-8");
            configuration_.evaluateInputSource(source);
        }
    }

    /**
     * Regression test for bug 2933989.
     * @throws Exception on error
     */
    @Test
    public void infiniteLoop() throws Exception {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("<html>\n");
        for (int x = 0; x <= 2005; x++) {
            buffer.append((char) (x % 10 + '0'));
        }

        buffer.append("\n<noframes>- Generated in 1<1ms -->");

        final XMLParserConfiguration parser = new HTMLConfiguration() {
            @Override
            protected HTMLScanner createDocumentScanner() {
                return new InfiniteLoopScanner();
            }
        };
        final XMLInputSource source = new XMLInputSource(null, "myTest", null, new StringReader(buffer.toString()), "UTF-8");
        parser.parse(source);
    }

    static class InfiniteLoopScanner extends HTMLScanner {
        InfiniteLoopScanner() {
            super(new HTMLConfiguration());
            fContentScanner = new MyContentScanner();
        }

        class MyContentScanner extends HTMLScanner.ContentScanner {

            @Override
            protected void scanComment() throws IOException {
                // bug was here: calling nextContent() at the end of the buffer/input
                nextContent(30);
                super.scanComment();
            }
        }
    }

    /**
     * @throws Exception on error
     */
    @Test
    public void elementNameNormalization() throws Exception {
        // not set
        final String string = "<HTML><Head><tiTLE>foo</tiTLE></hEaD><Body></BOdy></htMl>";

        HTMLConfiguration parser = new HTMLConfiguration();
        EvaluateInputSourceFilter filter = new EvaluateInputSourceFilter(parser);
        parser.setProperty("http://cyberneko.org/html/properties/filters", new XMLDocumentFilter[] {filter});
        XMLInputSource source = new XMLInputSource(null, "myTest", null, new StringReader(string), "UTF-8");
        parser.parse(source);

        final String[] expectedString = {"(HTML", "(Head", "(tiTLE", ")tiTLE", ")Head", "(Body", ")Body", ")HTML"};
        assertEquals(Arrays.asList(expectedString).toString(), filter.collectedStrings_.toString());

        // upper
        parser = new HTMLConfiguration();
        filter = new EvaluateInputSourceFilter(parser);
        parser.setProperty("http://cyberneko.org/html/properties/names/elems", "upper");
        parser.setProperty("http://cyberneko.org/html/properties/filters", new XMLDocumentFilter[] {filter});
        source = new XMLInputSource(null, "myTest", null, new StringReader(string), "UTF-8");
        parser.parse(source);

        final String[] expectedStringUpper = {"(HTML", "(HEAD", "(TITLE", ")TITLE", ")HEAD", "(BODY", ")BODY", ")HTML"};
        assertEquals(Arrays.asList(expectedStringUpper).toString(), filter.collectedStrings_.toString());

        // upper
        parser = new HTMLConfiguration();
        filter = new EvaluateInputSourceFilter(parser);
        parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
        parser.setProperty("http://cyberneko.org/html/properties/filters", new XMLDocumentFilter[] {filter});
        source = new XMLInputSource(null, "myTest", null, new StringReader(string), "UTF-8");
        parser.parse(source);

        final String[] expectedStringLower = {"(html", "(head", "(title", ")title", ")head", "(body", ")body", ")html"};
        assertEquals(Arrays.asList(expectedStringLower).toString(), filter.collectedStrings_.toString());
    }

    /**
     * Regression test for an oom exception in versions < 2.60.
     * @throws Exception on error
     */
    @Test
    public void invalidProcessingInstruction() throws Exception {
        final String string = "<!--?><?a/";

        final HTMLConfiguration parser = new HTMLConfiguration();
        final EvaluateInputSourceFilter filter = new EvaluateInputSourceFilter(parser);
        parser.setProperty("http://cyberneko.org/html/properties/filters", new XMLDocumentFilter[] {filter});
        final XMLInputSource source = new XMLInputSource(null, "myTest", null, new StringReader(string), "UTF-8");
        parser.parse(source);

        final String[] expected = {"(HTML", "(head", ")head", "(body", ")body", ")html"};
        assertEquals(Arrays.asList(expected).toString(), filter.collectedStrings_.toString());
    }

    /**
     * Regression test for an index out of bounds exception in versions < 2.60.
     * @throws Exception on error
     */
    @Test
    public void invalidProcessingInstruction2() throws Exception {
        final String string = "<?ax\r";

        final HTMLConfiguration parser = new HTMLConfiguration();
        final EvaluateInputSourceFilter filter = new EvaluateInputSourceFilter(parser);
        parser.setProperty("http://cyberneko.org/html/properties/filters", new XMLDocumentFilter[] {filter});
        final XMLInputSource source = new XMLInputSource(null, "myTest", null, new StringReader(string), "UTF-8");
        parser.parse(source);

        final String[] expected = {"(HTML", "(head", ")head", "(body", ")body", ")html"};
        assertEquals(Arrays.asList(expected).toString(), filter.collectedStrings_.toString());
    }

    /**
     * Regression test for an index out of bounds exception in versions < 2.60.
     * @throws Exception on error
     */
    @Test
    public void invalidProcessingInstruction3() throws Exception {
        final String string = "<?a x\r";

        final HTMLConfiguration parser = new HTMLConfiguration();
        final EvaluateInputSourceFilter filter = new EvaluateInputSourceFilter(parser);
        parser.setProperty("http://cyberneko.org/html/properties/filters", new XMLDocumentFilter[] {filter});
        final XMLInputSource source = new XMLInputSource(null, "myTest", null, new StringReader(string), "UTF-8");
        parser.parse(source);

        final String[] expected = {"(HTML", "(head", ")head", "(body", ")body", ")html"};
        assertEquals(Arrays.asList(expected).toString(), filter.collectedStrings_.toString());
    }

    /**
     * Regression test https://github.com/HtmlUnit/htmlunit-neko/pull/98.
     * @throws Exception on error
     */
    @Test
    public void reader() throws Exception {
        final String string = "<html><body>"
                + "<script type='text/javascript'>//<!-- /* <![CDATA[ */ function foo() {} /* ]]> */ // --> </script>"
                + "</body></html>";

        final String[] expected = {
                "(html",
                "(head",
                ")head",
                "(body",
                "(script",
                "Atype text/javascript",
                "\"//<!-- /* <![CDATA[ */ function foo() {} /* ]]> */ // --> ",
                ")script",
                ")body",
                ")html"
                };

        try (StringWriter out = new StringWriter()) {
            final HTMLConfiguration parser = new HTMLConfiguration();
            final Writer filter = new Writer(out);
            parser.setProperty("http://cyberneko.org/html/properties/filters", new XMLDocumentFilter[] {filter});

            final StringReader testReader = new StringReader(string) {
                @Override
                public int read(final char[] cbuf, final int off, final int len) throws IOException {
                    // this simulates the return of a smaller buffer
                    return super.read(cbuf, off, 1);
                }
            };

            final XMLInputSource source = new XMLInputSource(null, "myTest", null, testReader, "UTF-8");
            parser.parse(source);

            assertEquals(String.join(System.lineSeparator(), expected), out.toString().trim());
        }
    }
}
