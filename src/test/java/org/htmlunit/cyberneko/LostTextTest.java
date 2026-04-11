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
package org.htmlunit.cyberneko;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.htmlunit.cyberneko.xerces.xni.Augmentations;
import org.htmlunit.cyberneko.xerces.xni.NamespaceContext;
import org.htmlunit.cyberneko.xerces.xni.QName;
import org.htmlunit.cyberneko.xerces.xni.XMLAttributes;
import org.htmlunit.cyberneko.xerces.xni.XMLDocumentHandler;
import org.htmlunit.cyberneko.xerces.xni.XMLLocator;
import org.htmlunit.cyberneko.xerces.xni.XMLString;
import org.htmlunit.cyberneko.xerces.xni.XNIException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLDocumentSource;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link LostText}.
 */
public class LostTextTest {

    // ---- initially empty ----

    @Test
    public void initiallyEmpty() {
        final LostText lt = new LostText();
        assertTrue(lt.isEmpty());
    }

    // ---- whitespace-only text is ignored when container is empty ----

    @Test
    public void whitespaceOnlyIgnoredWhenEmpty() {
        final LostText lt = new LostText();
        lt.add(new XMLString("   ".toCharArray(), 0, 3), null);

        assertTrue(lt.isEmpty());
    }

    // ---- non-whitespace text is kept ----

    @Test
    public void nonWhitespaceTextIsKept() {
        final LostText lt = new LostText();
        lt.add(new XMLString("hello".toCharArray(), 0, 5), null);

        assertFalse(lt.isEmpty());
    }

    // ---- whitespace IS kept after non-whitespace ----

    @Test
    public void whitespaceKeptAfterNonWhitespace() {
        final LostText lt = new LostText();
        lt.add(new XMLString("hi".toCharArray(), 0, 2), null);
        lt.add(new XMLString("  ".toCharArray(), 0, 2), null);

        assertFalse(lt.isEmpty());
    }

    // ---- refeed delivers all entries and clears ----

    @Test
    public void refeedDeliversAndClears() {
        final LostText lt = new LostText();
        lt.add(new XMLString("abc".toCharArray(), 0, 3), null);
        lt.add(new XMLString("def".toCharArray(), 0, 3), null);

        final CollectingHandler handler = new CollectingHandler();
        lt.refeed(handler);

        assertEquals(2, handler.texts.size());
        assertEquals("abc", handler.texts.get(0));
        assertEquals("def", handler.texts.get(1));
        assertTrue(lt.isEmpty());
    }

    // ---- clear discards all entries ----

    @Test
    public void clearDiscardsEntries() {
        final LostText lt = new LostText();
        lt.add(new XMLString("x".toCharArray(), 0, 1), null);
        lt.clear();

        assertTrue(lt.isEmpty());
    }

    // ---- text is deep-copied on add ----

    @Test
    public void textIsDeepCopiedOnAdd() {
        final LostText lt = new LostText();
        final char[] buf = "hello".toCharArray();
        lt.add(new XMLString(buf, 0, 5), null);

        // mutate original buffer
        buf[0] = 'X';

        final CollectingHandler handler = new CollectingHandler();
        lt.refeed(handler);
        assertEquals("hello", handler.texts.get(0));
    }

    /**
     * Minimal handler that just collects character text.
     */
    private static class CollectingHandler implements XMLDocumentHandler {
        final List<String> texts = new ArrayList<>();

        @Override
        public void characters(final XMLString text, final Augmentations augs) throws XNIException {
            texts.add(text.toString());
        }

        // -- remaining methods are no-ops --

        @Override public void startDocument(XMLLocator l, String e, NamespaceContext n, Augmentations a) { }
        @Override public void xmlDecl(String v, String e, String s, Augmentations a) { }
        @Override public void doctypeDecl(String r, String p, String s, Augmentations a) { }
        @Override public void comment(XMLString t, Augmentations a) { }
        @Override public void processingInstruction(String t, XMLString d, Augmentations a) { }
        @Override public void startElement(QName e, XMLAttributes a, Augmentations aug) { }
        @Override public void emptyElement(QName e, XMLAttributes a, Augmentations aug) { }
        @Override public void endElement(QName e, Augmentations a) { }
        @Override public void startCDATA(Augmentations a) { }
        @Override public void endCDATA(Augmentations a) { }
        @Override public void endDocument(Augmentations a) { }
        @Override public void setDocumentSource(XMLDocumentSource s) { }
        @Override public XMLDocumentSource getDocumentSource() { return null; }
    }
}