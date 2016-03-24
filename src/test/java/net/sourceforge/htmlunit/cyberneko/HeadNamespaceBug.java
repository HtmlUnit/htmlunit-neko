package net.sourceforge.htmlunit.cyberneko;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.apache.xerces.parsers.AbstractSAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Unit test for <a href="http://sourceforge.net/support/tracker.php?aid=2799585">Bug 2799585</a>.
 * @author Charles Yates
 * @author Marc Guillemot
 *
 */
public class HeadNamespaceBug extends TestCase {

	/**
	 * Ensure that the inserted head element has the right namespace
	 */
    public void testHeadNamespace() throws Exception {
    	final int[] nbTags = {0};
        final ContentHandler handler = new DefaultHandler() {
            public void startElement(final String ns, final String name, final String qName, final Attributes atts) {
            	assertEquals("http://www.w3.org/1999/xhtml:" + name, ns + ":" + name);
            	++nbTags[0];
            }
        };
        InputSource source = new InputSource();
        source.setByteStream(new ByteArrayInputStream("<html xmlns='http://www.w3.org/1999/xhtml'><body/></html>".getBytes()));
        HTMLConfiguration conf = new HTMLConfiguration();
        conf.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
        conf.setFeature("http://cyberneko.org/html/features/insert-namespaces", true);
        AbstractSAXParser parser = new AbstractSAXParser(conf){};
        parser.setContentHandler(handler);
        parser.parse(source);
        
        // to be sure that test doesn't pass just because handler has never been called
        assertEquals(5, nbTags[0]);
    }
}

 	  	 