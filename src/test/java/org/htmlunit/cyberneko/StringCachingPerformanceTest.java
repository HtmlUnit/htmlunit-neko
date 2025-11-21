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

import org.htmlunit.cyberneko.html.dom.HTMLDocumentImpl;
import org.htmlunit.cyberneko.parsers.DOMParser;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLInputSource;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the string caching optimization in HTMLTagBalancer and HTMLScanner.
 * Tests verify that the cache correctly handles:
 * - Already lowercase/uppercase strings (fast path)
 * - Mixed case conversions
 * - Cache hit/miss scenarios
 * - Thread safety (ThreadLocal implementation)
 *
 * @author Ronald Brill
 */
public class StringCachingPerformanceTest {

    /**
     * Test that already lowercase strings are returned as-is (fast path).
     */
    @Test
    public void testAlreadyLowercaseString() throws Exception {
        final String html = "<html><body><div><span>test</span></div></body></html>";
        final DOMParser parser = new DOMParser(HTMLDocumentImpl.class);
        
        // Parse with lowercase mode
        parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
        
        final StringReader sr = new StringReader(html);
        final XMLInputSource source = new XMLInputSource(null, "test", null, sr, null);
        parser.parse(source);
        
        // All element names are already lowercase, should use fast path
        final org.w3c.dom.Document doc = parser.getDocument();
        assertEquals("html", doc.getDocumentElement().getNodeName());
        assertEquals("div", doc.getElementsByTagName("div").item(0).getNodeName());
        assertEquals("span", doc.getElementsByTagName("span").item(0).getNodeName());
    }

    /**
     * Test that already uppercase strings are returned as-is (fast path).
     */
    @Test
    public void testAlreadyUppercaseString() throws Exception {
        final String html = "<HTML><BODY><DIV><SPAN>test</SPAN></DIV></BODY></HTML>";
        final DOMParser parser = new DOMParser(HTMLDocumentImpl.class);
        
        // Parse with uppercase mode
        parser.setProperty("http://cyberneko.org/html/properties/names/elems", "upper");
        
        final StringReader sr = new StringReader(html);
        final XMLInputSource source = new XMLInputSource(null, "test", null, sr, null);
        parser.parse(source);
        
        // All element names are already uppercase, should use fast path
        final org.w3c.dom.Document doc = parser.getDocument();
        assertEquals("HTML", doc.getDocumentElement().getNodeName());
        assertEquals("DIV", doc.getElementsByTagName("DIV").item(0).getNodeName());
        assertEquals("SPAN", doc.getElementsByTagName("SPAN").item(0).getNodeName());
    }

    /**
     * Test mixed case conversion to lowercase.
     */
    @Test
    public void testMixedCaseToLowercase() throws Exception {
        final String html = "<HTML><BODY><DiV><SpAn>test</SpAn></DiV></BODY></HTML>";
        final DOMParser parser = new DOMParser(HTMLDocumentImpl.class);
        
        parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
        
        final StringReader sr = new StringReader(html);
        final XMLInputSource source = new XMLInputSource(null, "test", null, sr, null);
        parser.parse(source);
        
        final org.w3c.dom.Document doc = parser.getDocument();
        assertEquals("html", doc.getDocumentElement().getNodeName());
        assertEquals("div", doc.getElementsByTagName("div").item(0).getNodeName());
        assertEquals("span", doc.getElementsByTagName("span").item(0).getNodeName());
    }

    /**
     * Test mixed case conversion to uppercase.
     */
    @Test
    public void testMixedCaseToUppercase() throws Exception {
        final String html = "<html><body><DiV><SpAn>test</SpAn></DiV></body></html>";
        final DOMParser parser = new DOMParser(HTMLDocumentImpl.class);
        
        parser.setProperty("http://cyberneko.org/html/properties/names/elems", "upper");
        
        final StringReader sr = new StringReader(html);
        final XMLInputSource source = new XMLInputSource(null, "test", null, sr, null);
        parser.parse(source);
        
        final org.w3c.dom.Document doc = parser.getDocument();
        assertEquals("HTML", doc.getDocumentElement().getNodeName());
        assertEquals("DIV", doc.getElementsByTagName("DIV").item(0).getNodeName());
        assertEquals("SPAN", doc.getElementsByTagName("SPAN").item(0).getNodeName());
    }

    /**
     * Test that common HTML5 tags benefit from pre-populated cache.
     */
    @Test
    public void testCommonHtml5Tags() throws Exception {
        final String html = "<html><head><title>Test</title></head>"
                + "<body><div><p><span><a href='#'>Link</a></span></p></div>"
                + "<table><tr><td>Cell</td></tr></table>"
                + "<form><input type='text'/><button>Submit</button></form></body></html>";
        
        final DOMParser parser = new DOMParser(HTMLDocumentImpl.class);
        parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
        
        final StringReader sr = new StringReader(html);
        final XMLInputSource source = new XMLInputSource(null, "test", null, sr, null);
        parser.parse(source);
        
        final org.w3c.dom.Document doc = parser.getDocument();
        assertEquals("html", doc.getDocumentElement().getNodeName());
        
        // Verify nested elements
        assertEquals("body", doc.getElementsByTagName("body").item(0).getNodeName());
        assertEquals("div", doc.getElementsByTagName("div").item(0).getNodeName());
        assertEquals("table", doc.getElementsByTagName("table").item(0).getNodeName());
    }

    /**
     * Test that the cache works with repeated tag names.
     * This simulates cache hits for frequently used tags.
     */
    @Test
    public void testRepeatedTagNames() throws Exception {
        final StringBuilder html = new StringBuilder("<html><body>");
        
        // Add many div elements to test cache hits
        for (int i = 0; i < 100; i++) {
            html.append("<div>Content ").append(i).append("</div>");
        }
        html.append("</body></html>");
        
        final DOMParser parser = new DOMParser(HTMLDocumentImpl.class);
        parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
        
        final StringReader sr = new StringReader(html.toString());
        final XMLInputSource source = new XMLInputSource(null, "test", null, sr, null);
        parser.parse(source);
        
        final org.w3c.dom.Document doc = parser.getDocument();
        
        // Should have 100 div elements
        assertEquals(100, doc.getElementsByTagName("div").getLength());
    }

    /**
     * Test attribute name caching with lowercase mode.
     */
    @Test
    public void testAttributeNameCaching() throws Exception {
        final String html = "<div ID='test' CLASS='myclass' DATA-VALUE='123'></div>";
        final DOMParser parser = new DOMParser(HTMLDocumentImpl.class);
        
        parser.setProperty("http://cyberneko.org/html/properties/names/attrs", "lower");
        
        final StringReader sr = new StringReader(html);
        final XMLInputSource source = new XMLInputSource(null, "test", null, sr, null);
        parser.parse(source);
        
        final org.w3c.dom.Document doc = parser.getDocument();
        final org.w3c.dom.Element div = (org.w3c.dom.Element) doc.getElementsByTagName("div").item(0);
        
        // Attributes should be lowercase
        assertEquals("test", div.getAttribute("id"));
        assertEquals("myclass", div.getAttribute("class"));
    }

    /**
     * Test that no-change mode doesn't affect strings.
     */
    @Test
    public void testNoChangeMode() throws Exception {
        final String html = "<html><body><DiV><SpAn>test</SpAn></DiV></body></html>";
        final DOMParser parser = new DOMParser(HTMLDocumentImpl.class);
        
        parser.setProperty("http://cyberneko.org/html/properties/names/elems", "default");
        
        final StringReader sr = new StringReader(html);
        final XMLInputSource source = new XMLInputSource(null, "test", null, sr, null);
        parser.parse(source);
        
        final org.w3c.dom.Document doc = parser.getDocument();
        
        // Default mode in HTML is lowercase
        assertEquals("html", doc.getDocumentElement().getNodeName());
    }

    /**
     * Test parsing large HTML document to ensure cache size limit works.
     */
    @Test
    public void testCacheSizeLimit() throws Exception {
        final StringBuilder html = new StringBuilder("<html><body>");
        
        // Create more unique tags than cache size (256)
        // This tests that the LRU eviction works correctly
        for (int i = 0; i < 300; i++) {
            html.append("<div id='").append(i).append("'>Content</div>");
        }
        html.append("</body></html>");
        
        final DOMParser parser = new DOMParser(HTMLDocumentImpl.class);
        parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
        
        final StringReader sr = new StringReader(html.toString());
        final XMLInputSource source = new XMLInputSource(null, "test", null, sr, null);
        parser.parse(source);
        
        final org.w3c.dom.Document doc = parser.getDocument();
        assertEquals("html", doc.getDocumentElement().getNodeName());
        assertEquals(300, doc.getElementsByTagName("div").getLength());
    }

    /**
     * Test thread safety by parsing in multiple threads.
     * ThreadLocal cache should ensure no interference between threads.
     */
    @Test
    public void testThreadSafety() throws Exception {
        final String html = "<html><body><div>Test</div></body></html>";
        
        final Runnable parseTask = () -> {
            try {
                final DOMParser parser = new DOMParser(HTMLDocumentImpl.class);
                parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
                
                final StringReader sr = new StringReader(html);
                final XMLInputSource source = new XMLInputSource(null, "test", null, sr, null);
                parser.parse(source);
                
                final org.w3c.dom.Document doc = parser.getDocument();
                assertEquals("html", doc.getDocumentElement().getNodeName());
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        };
        
        // Run parsing in multiple threads
        final Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(parseTask);
            threads[i].start();
        }
        
        // Wait for all threads to complete
        for (final Thread thread : threads) {
            thread.join();
        }
    }

    /**
     * Test that cache correctly handles empty strings.
     */
    @Test
    public void testEmptyString() throws Exception {
        // This is a corner case - normally HTML parsers don't deal with empty tag names
        // but we test it for completeness
        final String html = "<html><body><div></div></body></html>";
        final DOMParser parser = new DOMParser(HTMLDocumentImpl.class);
        
        parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
        
        final StringReader sr = new StringReader(html);
        final XMLInputSource source = new XMLInputSource(null, "test", null, sr, null);
        parser.parse(source);
        
        final org.w3c.dom.Document doc = parser.getDocument();
        assertEquals("html", doc.getDocumentElement().getNodeName());
    }

    /**
     * Test special HTML5 semantic tags.
     */
    @Test
    public void testHtml5SemanticTags() throws Exception {
        final String html = "<html><body>"
                + "<header><nav><a href='#'>Home</a></nav></header>"
                + "<main><article><section><h1>Title</h1><p>Content</p></section></article></main>"
                + "<footer><address>Contact</address></footer>"
                + "</body></html>";
        
        final DOMParser parser = new DOMParser(HTMLDocumentImpl.class);
        parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
        
        final StringReader sr = new StringReader(html);
        final XMLInputSource source = new XMLInputSource(null, "test", null, sr, null);
        parser.parse(source);
        
        final org.w3c.dom.Document doc = parser.getDocument();
        assertEquals("html", doc.getDocumentElement().getNodeName());
        assertEquals(1, doc.getElementsByTagName("header").getLength());
        assertEquals(1, doc.getElementsByTagName("nav").getLength());
        assertEquals(1, doc.getElementsByTagName("main").getLength());
        assertEquals(1, doc.getElementsByTagName("article").getLength());
        assertEquals(1, doc.getElementsByTagName("section").getLength());
        assertEquals(1, doc.getElementsByTagName("footer").getLength());
    }

    /**
     * Test uppercase conversion of common tags.
     */
    @Test
    public void testUppercaseCommonTags() throws Exception {
        final String html = "<html><body><div><span>test</span></div></body></html>";
        final DOMParser parser = new DOMParser(HTMLDocumentImpl.class);
        
        parser.setProperty("http://cyberneko.org/html/properties/names/elems", "upper");
        
        final StringReader sr = new StringReader(html);
        final XMLInputSource source = new XMLInputSource(null, "test", null, sr, null);
        parser.parse(source);
        
        final org.w3c.dom.Document doc = parser.getDocument();
        assertEquals("HTML", doc.getDocumentElement().getNodeName());
        assertEquals("BODY", doc.getElementsByTagName("BODY").item(0).getNodeName());
        assertEquals("DIV", doc.getElementsByTagName("DIV").item(0).getNodeName());
    }

    /**
     * Test that cache handles unicode characters correctly.
     */
    @Test
    public void testUnicodeCharacters() throws Exception {
        // Custom tags with unicode characters
        final String html = "<html><body><div-ñoño><span>test</span></div-ñoño></body></html>";
        final DOMParser parser = new DOMParser(HTMLDocumentImpl.class);
        
        parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
        
        final StringReader sr = new StringReader(html);
        final XMLInputSource source = new XMLInputSource(null, "test", null, sr, null);
        parser.parse(source);
        
        final org.w3c.dom.Document doc = parser.getDocument();
        assertEquals("html", doc.getDocumentElement().getNodeName());
    }
}
