package net.sourceforge.htmlunit.cyberneko;


import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;
import net.sourceforge.htmlunit.cyberneko.parsers.SAXParser;

import org.apache.xerces.impl.Version;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.Locator2;

/**
 * Regression test for <a href="http://sourceforge.net/tracker/?func=detail&atid=952178&aid=3381270&group_id=195122">Bug 3381270</a>.
 * @author Marc Guillemot
 * @version $Revision: 291 $
 */
public class LocatorEncodingTest extends TestCase  {

    public void test() throws SAXException, IOException {
        if (Version.getVersion().startsWith("Xerces-J 2.2") || Version.getVersion().startsWith("Xerces-J 2.3")) {
            return; // this test makes sense only for more recent Xerces versions
        }
        
        final String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<html></html>";
        ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes("UTF-8"));
        SAXParser parser = new SAXParser();
        
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
