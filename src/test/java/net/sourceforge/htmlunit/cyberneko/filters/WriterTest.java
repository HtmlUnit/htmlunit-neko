package net.sourceforge.htmlunit.cyberneko.filters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import net.sourceforge.htmlunit.cyberneko.HTMLConfiguration;

/**
 * Unit tests for {@link Writer}.
 *
 * @author Marc Guillemot
 */
public class WriterTest extends TestCase {

    /**
     * Regression test for bug: writer changed attribute value causing NPE in 2nd writer.
     * http://sourceforge.net/support/tracker.php?aid=2815779
     */
    public void testEmptyAttribute() throws Exception {
        
        final String content = "<html><head>"
            + "<meta name='COPYRIGHT' content='SOMEONE' />"
            + "</head><body></body></html>";

        try (InputStream inputStream = new ByteArrayInputStream(content.getBytes())) { 
            final XMLDocumentFilter[] filters = {
                new net.sourceforge.htmlunit.cyberneko.Writer(new ByteArrayOutputStream(), "UTF-8"),
                new net.sourceforge.htmlunit.cyberneko.Writer(new ByteArrayOutputStream(), "UTF-8")
            };

            // create HTML parser
            final XMLParserConfiguration parser = new HTMLConfiguration();
            parser.setProperty("http://cyberneko.org/html/properties/filters", filters);
    
            XMLInputSource source = new XMLInputSource(null, "currentUrl", null, inputStream, "UTF-8");

            parser.parse(source);
        }
    }
}
