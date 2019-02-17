package net.sourceforge.htmlunit.cyberneko;

import junit.framework.TestCase;

/**
 * Unit tests for {@link HTMLNamedEntitiesParserGenerator}.
 * @author Ronald Brill
 */
public class HTMLEntitiesTest extends TestCase {

    public void testParseEuml() throws Exception {
        HTMLNamedEntitiesParser parser = new HTMLNamedEntitiesParser();

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
        HTMLNamedEntitiesParser parser = new HTMLNamedEntitiesParser();

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
        HTMLNamedEntitiesParser parser = new HTMLNamedEntitiesParser();

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
        HTMLNamedEntitiesParser parser = new HTMLNamedEntitiesParser();

        String input = "EumX";
        int i = 0;
        while(parser.parse(input.charAt(i))) {
            i++;
        }
        assertNull(parser.getMatch());
        assertEquals(4, parser.getRewindCount());
        assertFalse(parser.endsWithSemicolon());
    }
}
