package net.sourceforge.htmlunit.cyberneko;

import java.io.StringReader;
import java.io.StringWriter;

import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.apache.xerces.xni.parser.XMLInputSource;

import junit.framework.TestCase;
import net.sourceforge.htmlunit.cyberneko.parsers.DOMParser;

/**
 * Unit tests for {@link HTMLTagBalancer}'s property {@link HTMLTagBalancer#FRAGMENT_CONTEXT_STACK}.
 *
 * @author Marc Guillemot
 */
public class FragmentContextStackTest extends TestCase {

    private static final String NL = System.lineSeparator();

    public void testSimple() throws Exception {
        final String expected = "(div" + NL
            + "(span" + NL
            + "\"hello" + NL
            + ")span" + NL
            + ")div" + NL;
        doTest("<div><span>hello</span>", new String[] { "html", "body" }, expected);

        doTest("<div><span>hello</span>", new String[] { "html" }, expected);

        doTest("<div><span>hello</span>", new String[]{}, expected);
        doTest("<div><span>hello</span>", null, expected);
    }

    public void testTR() throws Exception {
        String expected = "(tr" + NL
            + "(td" + NL
            + "\"hello" + NL
            + ")td" + NL
            + ")tr" + NL;
        doTest("<tr><td>hello</td></tr>", new String[] { "html", "body", "table", "tbody" }, expected);
        expected = "(TBODY" + NL
            + expected
            + ")TBODY\n";
        doTest("<tr><td>hello</td></tr>", new String[] { "html", "body", "table" }, expected);
        doTest("<tr><td>hello</td></tr>", new String[] { "html", "body" }, "\"hello");
    }

    public void testFragmentShouldNotCloseContextStack() throws Exception {
        final String expected = "\"helloworld\n";
        doTest("hello</div>world", new String[] { "html", "body", "div" }, expected);
        doTest("hello</span>world", new String[] { "html", "body", "div", "span" }, expected);
    }

    public void testFragmentShouldNotCloseContextStackPHack() throws Exception {
        doTest("<p>hello world</p>", new String[] { "html", "body", "p" }, ")null" + NL + "(p" + NL + "\"hello world" + NL + ")p");
        doTest("<p>hello world", new String[] { "html", "body", "p" }, ")null" + NL + "(p" + NL + "\"hello world" + NL + ")p");

        doTest("<p>hello world</p>", new String[] { "html", "body", "p", "span"}, ")null" + NL + ")null" + NL + "(p" + NL + "\"hello world" + NL + ")p");
    }

    public void testFragmentShouldNotCloseSelect() throws Exception {
        doTest("<select><option>Two</option></select>", new String[] { "html", "body", "select" },
                ")null" + NL + "(select" + NL + "(option" + NL + "\"Two" + NL + ")option" + NL + ")select");
    }

    private static void doTest(final String html, final String[] contextStack,
            final String expected) throws Exception {
        final DOMParser parser = new DOMParser();
        parser.setFeature("http://cyberneko.org/html/features/balance-tags/document-fragment", true);
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