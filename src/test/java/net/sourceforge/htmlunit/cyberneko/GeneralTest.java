/**
 * Unit tests for the parser not done as {@link CanonicalTest}.
 *
 * @author Ronald Brill
 */
package net.sourceforge.htmlunit.cyberneko;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import net.sourceforge.htmlunit.cyberneko.parsers.DOMParser;
import net.sourceforge.htmlunit.xerces.xni.QName;
import net.sourceforge.htmlunit.xerces.xni.parser.XMLDocumentFilter;
import net.sourceforge.htmlunit.xerces.xni.parser.XMLInputSource;

/**
 * @author Ronald Brill
 */
public class GeneralTest {

    private static final String NL = System.lineSeparator();
    private static final String[] FEATURES = {"http://cyberneko.org/html/features/augmentations"};

    @Test
    public void newlineInAttributeCrLf() throws Exception {
        final String expected =
            "[synth](HTML" + NL
            + "[synth](head" + NL
            + "[synth])head" + NL
            + "[synth](body" + NL
            + "[1,1,0;1,11,10]\"some text " + NL
            + "[1,11,10;2,23,52](span" + NL
            + "Aclass value\\ncontaining a newline" + NL
            + "[2,23,52;2,34,63]\"spancontent" + NL
            + "[2,34,63;2,41,70])span" + NL
            + "[synth])body" + NL
            + "[synth])HTML" + NL;
        doTest("some text <span class='value\r\n"
                + "containing a newline'>spancontent</span>", new String[] { "html", "body" }, expected,
                FEATURES);
    }

    @Test
    public void newlineInAttributeLf() throws Exception {
        final String expected =
            "[synth](HTML" + NL
            + "[synth](head" + NL
            + "[synth])head" + NL
            + "[synth](body" + NL
            + "[1,1,0;1,11,10]\"some text " + NL
            + "[1,11,10;2,23,51](span" + NL
            + "Aclass value\\ncontaining a newline" + NL
            + "[2,23,51;2,34,62]\"spancontent" + NL
            + "[2,34,62;2,41,69])span" + NL
            + "[synth])body" + NL
            + "[synth])HTML" + NL;
        doTest("some text <span class='value\n"
                + "containing a newline'>spancontent</span>", new String[] { "html", "body" }, expected,
                FEATURES);
    }

    @Test
    public void newlineInPiCrLf() throws Exception {
        final String expected =
            "[synth](HTML" + NL
            + "[synth](head" + NL
            + "[synth])head" + NL
            + "[synth](body" + NL
            + "[1,1,0;1,11,10]\"some text " + NL
            + "[1,11,10;2,23,63]?instruct beforenl='content'\\n  afternl=\"content\" " + NL
            + "[2,23,63;3,1,74]\"more text\\n" + NL
            + "[synth])body" + NL
            + "[synth])HTML" + NL;
        doTest("some text <?instruct beforenl='content'\r\n"
                + "  afternl=\"content\" ?>more text\r\n", new String[] { "html", "body" }, expected,
                FEATURES);
    }

    @Test
    public void newlineInPiLf() throws Exception {
        final String expected =
            "[synth](HTML" + NL
            + "[synth](head" + NL
            + "[synth])head" + NL
            + "[synth](body" + NL
            + "[1,1,0;1,11,10]\"some text " + NL
            + "[1,11,10;2,23,62]?instruct beforenl='content'\\n  afternl=\"content\" " + NL
            + "[2,23,62;3,1,73]\"more text\\n" + NL
            + "[synth])body" + NL
            + "[synth])HTML" + NL;
        doTest("some text <?instruct beforenl='content'\n"
                + "  afternl=\"content\" ?>more text\r\n", new String[] { "html", "body" }, expected,
                FEATURES);
    }

    public static void doTest(final String html, final String[] contextStack,
            final String expected, String... features) throws Exception {
        final DOMParser parser = new DOMParser();
        for (String feature : features) {
            parser.setFeature(feature, true);
        }

        if (contextStack != null) {
            parser.setProperty("http://cyberneko.org/html/properties/balance-tags/fragment-context-stack", toQNames(contextStack));
        }

        final StringWriter out = new StringWriter();
        final XMLDocumentFilter[] filters = { new Writer(out) };
        parser.setProperty("http://cyberneko.org/html/properties/filters", filters);

        final StringReader sr = new StringReader(html);
        final XMLInputSource in = new XMLInputSource(null, "foo", null, sr, null);
        parser.parse(in);

        assertEquals(expected.trim(), out.toString().trim());
    }

    private static QName[] toQNames(final String[] tags) {
        final QName[] qnames = new QName[tags.length];
        for (int i = 0; i < tags.length; ++i) {
            qnames[i] = new QName(null, tags[i], null, null);
        }

        return qnames;
    }
}
