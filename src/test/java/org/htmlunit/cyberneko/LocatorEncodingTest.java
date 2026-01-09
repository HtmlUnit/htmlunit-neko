/*
 * Copyright (c) 2002-2009 Andy Clark, Marc Guillemot
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.htmlunit.cyberneko.parsers.SAXParser;
import org.junit.jupiter.api.Test;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.Locator2;

/**
 * Regression test for <a href="http://sourceforge.net/tracker/?func=detail&atid=952178&aid=3381270&group_id=195122">Bug 3381270</a>.
 * @author Marc Guillemot
 * @author Ronald Brill
 */
public class LocatorEncodingTest {

    @Test
    public void provided() throws SAXException, IOException {
        final String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<html></html>";
        final ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        final SAXParser parser = new SAXParser();

        final Locator[] locators = {null};

        parser.setContentHandler(new TestContentHandler(locators));
        parser.parse(new InputSource(input));
        assertEquals("utf-8", ((Locator2) locators[0]).getEncoding());
    }

    @Test
    public void guess() throws SAXException, IOException {
        final String content = "<?xml version=\"1.0\"?>\n<html></html>";
        final ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        final SAXParser parser = new SAXParser();

        final Locator[] locators = {null};

        parser.setContentHandler(new TestContentHandler(locators));
        parser.parse(new InputSource(input));
        assertEquals("Windows-1252", ((Locator2) locators[0]).getEncoding());
    }

    static final class TestContentHandler implements ContentHandler {
        private final Locator[] locators_;

        TestContentHandler(final Locator[] locators) {
            locators_ = locators;
        }

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
            locators_[0] = locator;
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
