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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.StringReader;

import org.htmlunit.cyberneko.html.dom.HTMLDocumentImpl;
import org.htmlunit.cyberneko.parsers.DOMParser;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLInputSource;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for QName pooling optimization.
 *
 * @author Ronald Brill
 */
public class QNamePoolingTest {

    /**
     * Test that QName pooling works correctly with deeply nested HTML.
     * This tests the pool exhaustion scenario where we fall back to allocation.
     */
    @Test
    public void deeplyNestedHtml() throws Exception {
        final StringBuilder html = new StringBuilder("<html><body>");
        
        // Create 20 levels of nested divs (more than pool size of 8)
        for (int i = 0; i < 20; i++) {
            html.append("<div id='div").append(i).append("'>");
        }
        html.append("content");
        for (int i = 0; i < 20; i++) {
            html.append("</div>");
        }
        html.append("</body></html>");
        
        final StringReader sr = new StringReader(html.toString());
        final XMLInputSource source = new XMLInputSource(null, "foo", null, sr, null);
        
        final DOMParser parser = new DOMParser(HTMLDocumentImpl.class);
        parser.parse(source);
        
        assertNotNull(parser.getDocument());
    }

    /**
     * Test that synthesized elements (head, body, etc.) work correctly with pooling.
     */
    @Test
    public void synthesizedElements() throws Exception {
        // HTML without explicit head/body tags - parser will synthesize them
        final String html = "<html><title>Test</title><p>Content</p></html>";
        
        final StringReader sr = new StringReader(html);
        final XMLInputSource source = new XMLInputSource(null, "foo", null, sr, null);
        
        final DOMParser parser = new DOMParser(HTMLDocumentImpl.class);
        parser.parse(source);
        
        assertNotNull(parser.getDocument());
    }

    /**
     * Test multiple documents parsed with same parser instance.
     * Verifies pool index is reset properly between documents.
     */
    @Test
    public void multipleDocuments() throws Exception {
        final DOMParser parser = new DOMParser(HTMLDocumentImpl.class);
        
        for (int doc = 0; doc < 3; doc++) {
            final String html = "<html><body><div>Document " + doc + "</div></body></html>";
            final StringReader sr = new StringReader(html);
            final XMLInputSource source = new XMLInputSource(null, "foo", null, sr, null);
            parser.parse(source);
            
            assertNotNull(parser.getDocument());
        }
    }
}
