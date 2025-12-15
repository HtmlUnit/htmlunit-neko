/*
 * Copyright (c) 2002-2009 Andy Clark, Marc Guillemot
 * Copyright (c) 2017-2025 Ronald Brill
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
import org.htmlunit.cyberneko.parsers.DOMFragmentParser;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.html.HTMLDocument;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

/**
 * Unit tests for {@link DOMFragmentParser}.
 * @author Marc Guillemot
 * @author Ronald Brill
 *
 */
public class DOMFragmentParserTest {
    /**
     * See <a href="https://sourceforge.net/p/nekohtml/bugs/154/">Bug 154</a>.
     */
    @Test
    public void attrEndingWithCRAtEndOfStream() throws Exception {
        doTest("<a href=\"\r", "");
    }

    /**
     * See <a href="http://sourceforge.net/support/tracker.php?aid=2828553">Bug 2828553</a>.
     */
    @Test
    public void invalidProcessingInstruction() throws Exception {
        doTest("<html><?9 ?></html>", "<html/>");
    }

    /**
     * See <a href="http://sourceforge.net/support/tracker.php?aid=2828534">Bug 2828534</a>.
     */
    @Test
    public void invalidAttributeName() throws Exception {
        // doTest("<html 9='id'></html>", "<html/>");

        // changed in version 4.8.0 as this is an valid (html) attribute name
        // doTest("<html 9='id'></html>", "<html 9=\"id\"/>");

        // this fail on jdk8 because the DOMImplementationLS returns null if the dom is not xml
        // migrated to test-digit-attr-name
    }

    private static void doTest(final String html, final String expected) throws Exception {
        final DOMFragmentParser parser = new DOMFragmentParser();
        final HTMLDocument document = new HTMLDocumentImpl();

        final DocumentFragment fragment = document.createDocumentFragment();
        final InputSource source = new InputSource(new StringReader(html));
        parser.parse(source, fragment);
        final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();

        final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");

        final LSSerializer writer = impl.createLSSerializer();
        String str = writer.writeToString(fragment);
        if (str == null) {
            str = "";
        }
        str = str.replace("\r", "").replace("\n", "");

        final String xmlDecl = "<?xml version=\"1.0\" encoding=\"UTF-16\"?>";
        if (str.startsWith(xmlDecl)) {
            str = str.substring(xmlDecl.length());
        }

        assertEquals(expected, str);
    }

    public static void print(final Node node, final String indent) {
        System.out.println(indent + node.getClass().getName());
        Node child = node.getFirstChild();
        while (child != null) {
            print(child, indent + " ");
            child = child.getNextSibling();
        }
    }

    /**
     * HTMLTagBalancer field fSeenBodyElementEnd was not correctly reset as of 1.19.17.
     * @throws Exception on error
     */
    @Test
    public void instanceReuse() throws Exception {
        final String s = "<html><body><frame><frameset></frameset></html>";

        final DOMFragmentParser parser = new DOMFragmentParser();
        final HTMLDocument document = new HTMLDocumentImpl();

        final DocumentFragment fragment1 = document.createDocumentFragment();
        parser.parse(new InputSource(new StringReader(s)), fragment1);

        final DocumentFragment fragment2 = document.createDocumentFragment();
        parser.parse(new InputSource(new StringReader(s)), fragment2);

        final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        final DOMImplementationLS impl =
            (DOMImplementationLS) registry.getDOMImplementation("LS");

        final LSSerializer writer = impl.createLSSerializer();
        final String str1 = writer.writeToString(fragment1);
        final String str2 = writer.writeToString(fragment2);
        assertEquals(str1, str2);
    }
}

