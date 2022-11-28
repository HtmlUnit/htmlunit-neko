package net.sourceforge.htmlunit.cyberneko;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;

import net.sourceforge.htmlunit.xerces.parsers.AbstractSAXParser;
import org.junit.jupiter.api.Test;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Unit test for <a href="https://sourceforge.net/p/nekohtml/bugs/126/">Bug 126</a>.
 *
 * @author Charles Yates
 * @author Ronald Brill
 */
public class TableBodyNamespaceBugTest {

    /**
    * Ensure that the inserted tbody element has the right namespace
    */
    @Test
    public void headNamespace() throws Exception {
       final int[] nbTags = {0};
        final ContentHandler handler = new DefaultHandler() {
            @Override
            public void startElement(final String ns, final String name, final String qName, final Attributes atts) {
               assertEquals("http://www.w3.org/1999/xhtml:" + name, ns + ":" + name);
               ++nbTags[0];
            }
        };
        final InputSource source = new InputSource();
        source.setByteStream(new ByteArrayInputStream("<html xmlns='http://www.w3.org/1999/xhtml'><body><table><tr></tr></table></html>".getBytes()));
        final HTMLConfiguration conf = new HTMLConfiguration();
        conf.setFeature("http://cyberneko.org/html/features/insert-namespaces", true);
        final AbstractSAXParser parser = new AbstractSAXParser(conf){};
        parser.setContentHandler(handler);
        parser.parse(source);

        // to be sure that test doesn't pass just because handler has never been called
        assertEquals(6, nbTags[0]);
    }
}
