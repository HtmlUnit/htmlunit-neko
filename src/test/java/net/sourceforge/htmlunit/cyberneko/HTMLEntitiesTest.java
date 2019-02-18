package net.sourceforge.htmlunit.cyberneko;

import junit.framework.TestCase;

/**
 * Unit tests for {@link HTMLEntitiesParserGenerator}.
 * @author Ronald Brill
 */
public class HTMLEntitiesTest extends TestCase {

    public void testParseEuml() throws Exception {
        HTMLEntitiesParser parser = new HTMLEntitiesParser();

        String input = "Euml ";
        int i = 0;
        while(parser.parse(input.charAt(i))) {
            i++;
        }
        assertEquals("\u00CB", parser.getMatch());
        assertEquals(1, parser.getRewindCount());
        assertFalse(parser.endsWithSemicolon());
    }

    public void testParseEuml_() throws Exception {
        HTMLEntitiesParser parser = new HTMLEntitiesParser();

        String input = "Euml; ";
        int i = 0;
        while(parser.parse(input.charAt(i))) {
            i++;
        }
        assertEquals("\u00CB", parser.getMatch());
        assertEquals(0, parser.getRewindCount());
        assertTrue(parser.endsWithSemicolon());
    }

    public void testParseEumlX() throws Exception {
        HTMLEntitiesParser parser = new HTMLEntitiesParser();

        String input = "EumlX";
        int i = 0;
        while(parser.parse(input.charAt(i))) {
            i++;
        }
        assertEquals("\u00CB", parser.getMatch());
        assertEquals(1, parser.getRewindCount());
        assertFalse(parser.endsWithSemicolon());
    }

    public void testParseEumX() throws Exception {
        HTMLEntitiesParser parser = new HTMLEntitiesParser();

        String input = "EumX";
        int i = 0;
        while(parser.parse(input.charAt(i))) {
            i++;
        }
        assertNull(parser.getMatch());
        assertEquals(4, parser.getRewindCount());
        assertFalse(parser.endsWithSemicolon());
    }

    public void testParseEuro() throws Exception {
        HTMLEntitiesParser parser = new HTMLEntitiesParser();

        String input = "x80;";
        int i = 0;
        while(parser.parseNumeric(input.charAt(i))) {
            i++;
        }

        assertEquals("\u20ac", parser.getMatch());
        assertEquals(0, parser.getRewindCount());
    }
}
