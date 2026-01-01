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

import java.io.IOException;
import java.io.StringReader;

import org.htmlunit.cyberneko.html.dom.HTMLDocumentImpl;
import org.htmlunit.cyberneko.parsers.DOMParser;
import org.htmlunit.cyberneko.parsers.SAXParser;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLInputSource;
import org.junit.jupiter.api.Test;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Unit tests for the code from the readme.
 *
 * @author Ronald Brill
 */
public class ReadmeTest {

    @Test
    public void domParser() throws Exception {
        final String html =
                """
                 <!DOCTYPE html>
                <html>
                <body>
                <h1>NekoHtml</h1>
                </body>
                </html>""";

        final StringReader sr = new StringReader(html);
        final XMLInputSource in = new XMLInputSource(null, "foo", null, sr, null);

        // use the provided simple DocumentImpl
        final DOMParser parser = new DOMParser(HTMLDocumentImpl.class);
        parser.parse(in);

        final HTMLDocumentImpl doc = (HTMLDocumentImpl) parser.getDocument();
        final NodeList headings = doc.getElementsByTagName("h1");

        System.out.println(headings.item(0));
    }

    @Test
    public void test() throws IOException {
        final String html =
                """
                 <!DOCTYPE html>
                <html>
                <body>
                <h1>NekoHtml</h1>
                </body>
                </html>""";

        final StringReader sr = new StringReader(html);
        final XMLInputSource in = new XMLInputSource(null, "foo", null, sr, null);

        final SAXParser parser = new SAXParser();

        final ContentHandler myContentHandler = new MyContentHandler();
        parser.setContentHandler(myContentHandler);

        parser.parse(in);
    }

    static class MyContentHandler implements ContentHandler {
        @Override
        public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
        }

        @Override
        public void startElement(final String uri, final String localName, final String qName,
                                    final Attributes atts) throws SAXException {
        }

        @Override
        public void startDocument() throws SAXException {
        }

        @Override
        public void skippedEntity(final String name) throws SAXException {
        }

        @Override
        public void setDocumentLocator(final Locator locator) {
        }

        @Override
        public void processingInstruction(final String target, final String data) throws SAXException {
        }

        @Override
        public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {
        }

        @Override
        public void endPrefixMapping(final String prefix) throws SAXException {
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        }

        @Override
        public void endDocument() throws SAXException {
        }

        @Override
        public void characters(final char[] ch, final int start, final int length) throws SAXException {
        }
    }
}
