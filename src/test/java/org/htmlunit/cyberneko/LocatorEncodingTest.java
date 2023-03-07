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
    public void test() throws SAXException, IOException {
        final String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<html></html>";
        final ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        final SAXParser parser = new SAXParser();

        final Locator[] locators = { null };

        final ContentHandler contentHandler = new ContentHandler() {
            @Override
            public void startPrefixMapping(String prefix, String uri) throws SAXException {
            }

            @Override
            public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            }

            @Override
            public void startDocument() throws SAXException {
            }

            @Override
            public void skippedEntity(String name) throws SAXException {
            }

            @Override
            public void setDocumentLocator(Locator locator) {
                locators[0] = locator;
            }

            @Override
            public void processingInstruction(String target, String data)
                    throws SAXException {
            }

            @Override
            public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            }

            @Override
            public void endPrefixMapping(String prefix) throws SAXException {
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
            }

            @Override
            public void endDocument() throws SAXException {
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
            }
        };
        parser.setContentHandler(contentHandler);
        parser.parse(new InputSource(input));
        assertEquals("UTF8", ((Locator2) locators[0]).getEncoding());
    }
}
