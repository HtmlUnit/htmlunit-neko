package org.htmlunit.cyberneko.filters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.htmlunit.cyberneko.HTMLConfiguration;
import org.htmlunit.cyberneko.Writer;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLDocumentFilter;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLInputSource;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLParserConfiguration;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Writer}.
 *
 * @author Marc Guillemot
 * @author Ronald Brill
 */
public class WriterTest {

    /**
     * Regression test for bug: writer changed attribute value causing NPE in 2nd writer.
     * http://sourceforge.net/support/tracker.php?aid=2815779
     */
    @Test
    public void emptyAttribute() throws Exception {

        final String content = "<html><head>"
            + "<meta name='COPYRIGHT' content='SOMEONE' />"
            + "</head><body></body></html>";

        try (InputStream inputStream = new ByteArrayInputStream(content.getBytes())) {
            final XMLDocumentFilter[] filters = {
                new org.htmlunit.cyberneko.Writer(new ByteArrayOutputStream(), "UTF-8"),
                new org.htmlunit.cyberneko.Writer(new ByteArrayOutputStream(), "UTF-8")
            };

            // create HTML parser
            final XMLParserConfiguration parser = new HTMLConfiguration();
            parser.setProperty("http://cyberneko.org/html/properties/filters", filters);

            final XMLInputSource source = new XMLInputSource(null, "currentUrl", null, inputStream, "UTF-8");

            parser.parse(source);
        }
    }
}
