package net.sourceforge.htmlunit.cyberneko.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import net.sourceforge.htmlunit.cyberneko.HTMLConfiguration;
import net.sourceforge.htmlunit.cyberneko.HTMLEntitiesParser;
import net.sourceforge.htmlunit.xerces.xni.parser.XMLInputSource;

/**
 * Unit tests for {@link HTMLEntitiesParserGenerator}.
 * @author Ronald Brill
 */
public class HTMLEntitiesTest {

    @Test
    public void parseEuml() {
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

    @Test
    public void parseEuml_() {
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

    @Test
    public void parseEumlX() {
        HTMLEntitiesParser parser = new HTMLEntitiesParser();

        String input = "EumlX";
        int i = 0;
        while(parser.parse(input.charAt(i))) {
            i++;
        }

        // valid without semicolon at end
        assertEquals("\u00CB", parser.getMatch());
        assertEquals(1, parser.getRewindCount());
        assertFalse(parser.endsWithSemicolon());
    }

    @Test
    public void parseEumX() {
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

    @Test
    public void parseEuroLt() {
        HTMLEntitiesParser parser = new HTMLEntitiesParser();

        String input = "euro<";
        int i = 0;
        while(parser.parse(input.charAt(i))) {
            i++;
        }

        // not valid without semicolon at end
        assertNull(parser.getMatch());
        assertEquals(5, parser.getRewindCount());
        assertFalse(parser.endsWithSemicolon());
    }

    @Test
    public void parseEuro() {
        HTMLEntitiesParser parser = new HTMLEntitiesParser();

        String input = "x80;";
        int i = 0;
        while(parser.parseNumeric(input.charAt(i))) {
            i++;
        }

        assertEquals("\u20ac", parser.getMatch());
        assertEquals(0, parser.getRewindCount());
    }

    @Test
    public void parseEuroMissingSemicolon() {
        HTMLEntitiesParser parser = new HTMLEntitiesParser();

        String input = "x80<";
        int i = 0;
        while(parser.parseNumeric(input.charAt(i))) {
            i++;
        }

        assertEquals("\u20ac", parser.getMatch());
        assertEquals(1, parser.getRewindCount());
    }

    @Test
    public void rewind() throws Exception {
        HTMLConfiguration htmlConfiguration = new HTMLConfiguration();
        String content = "<html blah=\"" +
            "funfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfun" +
            "funfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfun" +
            "funfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfun" +
            "funfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfun" +
            "funfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfun" +
            "funfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfun" +
            "funfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfun" +
            "funfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfun" +
            "funfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfun" +
            "funfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfun" +
            "funfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfun" +
            "funfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfun" +
            "funfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfun" +
            "funfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfun" +
            "funfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfun" +
            "funfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfun" +
            "funfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfun" +
            "funfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfun" +
            "funfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfun" +
            "funfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfun" +
            "funfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfun" +
            "funfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfun" +
            "funfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfun" +
            "funfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfun" +
            "funfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfun" +
            "funfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfun" +
            "funfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfunfun" +
            "funfunfun&fin\"></html>";
        InputStream byteStream = new ByteArrayInputStream(content.getBytes());
        XMLInputSource inputSource = new XMLInputSource("", "", "", byteStream, "UTF-8");
        htmlConfiguration.parse(inputSource);
    }
}
