package org.htmlunit.cyberneko;

import org.htmlunit.cyberneko.HTMLTagBalancer;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link HTMLTagBalancer}'s property {@link HTMLTagBalancer#FRAGMENT_CONTEXT_STACK}.
 *
 * @author Marc Guillemot
 * @author Ronald Brill
 */
public class FragmentContextStackTest {

    private static final String NL = System.lineSeparator();
    private static final String[] FEATURES = {"http://cyberneko.org/html/features/balance-tags/document-fragment"};

    @Test
    public void simple() throws Exception {
        final String expected = "(div" + NL
            + "(span" + NL
            + "\"hello" + NL
            + ")span" + NL
            + ")div" + NL;
        GeneralTest.doTest("<div><span>hello</span>", new String[] { "html", "body" }, expected, FEATURES);

        GeneralTest.doTest("<div><span>hello</span>", new String[] { "html" }, expected, FEATURES);

        GeneralTest.doTest("<div><span>hello</span>", new String[]{}, expected, FEATURES);
        GeneralTest.doTest("<div><span>hello</span>", null, expected, FEATURES);
    }

    @Test
    public void tr() throws Exception {
        String expected = "(tr" + NL
            + "(td" + NL
            + "\"hello" + NL
            + ")td" + NL
            + ")tr" + NL;
        GeneralTest.doTest("<tr><td>hello</td></tr>", new String[] { "html", "body", "table", "tbody" }, expected, FEATURES);
        expected = "(TBODY" + NL
            + expected
            + ")TBODY\n";
        GeneralTest.doTest("<tr><td>hello</td></tr>", new String[] { "html", "body", "table" }, expected, FEATURES);
        GeneralTest.doTest("<tr><td>hello</td></tr>", new String[] { "html", "body" }, "\"hello", FEATURES);
    }

    @Test
    public void fragmentShouldNotCloseContextStack() throws Exception {
        final String expected = "\"helloworld\n";
        GeneralTest.doTest("hello</div>world", new String[] { "html", "body", "div" }, expected, FEATURES);
        GeneralTest.doTest("hello</span>world", new String[] { "html", "body", "div", "span" }, expected, FEATURES);
    }

    @Test
    public void fragmentShouldNotCloseContextStackPHack() throws Exception {
        GeneralTest.doTest("<p>hello world</p>", new String[] { "html", "body", "p" }, ")null" + NL + "(p" + NL + "\"hello world" + NL + ")p", FEATURES);
        GeneralTest.doTest("<p>hello world", new String[] { "html", "body", "p" }, ")null" + NL + "(p" + NL + "\"hello world" + NL + ")p", FEATURES);

        GeneralTest.doTest("<p>hello world</p>", new String[] { "html", "body", "p", "span"}, ")null" + NL + ")null" + NL + "(p" + NL + "\"hello world" + NL + ")p", FEATURES);
    }

    @Test
    public void fragmentShouldNotCloseSelect() throws Exception {
        GeneralTest.doTest("<select><option>Two</option></select>", new String[] { "html", "body", "select" },
                ")null" + NL + "(select" + NL + "(option" + NL + "\"Two" + NL + ")option" + NL + ")select",
                FEATURES);
    }
}