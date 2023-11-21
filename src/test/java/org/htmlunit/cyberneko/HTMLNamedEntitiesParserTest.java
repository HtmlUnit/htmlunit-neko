package org.htmlunit.cyberneko;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.htmlunit.cyberneko.HTMLNamedEntitiesParser.State;
import org.junit.jupiter.api.Test;

public class HTMLNamedEntitiesParserTest
{
    @Test
    public void happyPath() {
        final State r = HTMLNamedEntitiesParser.get().lookup("Beta;");
        assertTrue(r.isMatch);
        assertTrue(r.endNode);
        assertTrue(r.endsWithSemicolon);

        assertEquals("\u0392", r.resolvedValue);
        assertEquals("Beta;", r.entityOrFragment);
        assertEquals(5, r.length);
    }

    @Test
    public void happyPathOneCharDiff() {
        {
            final State r = HTMLNamedEntitiesParser.get().lookup("Colon;");
            assertTrue(r.isMatch);
            assertTrue(r.endNode);
            assertTrue(r.endsWithSemicolon);

            assertEquals("\u2237", r.resolvedValue);
            assertEquals("Colon;", r.entityOrFragment);
            assertEquals(6, r.length);
        }
        {
            final State r = HTMLNamedEntitiesParser.get().lookup("Colone;");
            assertTrue(r.isMatch);
            assertTrue(r.endNode);
            assertTrue(r.endsWithSemicolon);

            assertEquals("\u2A74", r.resolvedValue);
            assertEquals("Colone;", r.entityOrFragment);
            assertEquals(7, r.length);
        }
    }

    @Test
    public void happyPathTwoVersionEntity() {
        {
            final State r = HTMLNamedEntitiesParser.get().lookup("gt");
            assertEquals("gt", r.entityOrFragment);
            assertTrue(r.isMatch);
            assertFalse(r.endNode);
            assertFalse(r.endsWithSemicolon);

            assertEquals(">", r.resolvedValue);
            assertEquals(2, r.length);
        }
        {
            final State r = HTMLNamedEntitiesParser.get().lookup("gt;");
            assertEquals("gt;", r.entityOrFragment);
            assertTrue(r.isMatch);
            assertTrue(r.endNode);
            assertTrue(r.endsWithSemicolon);

            assertEquals(">", r.resolvedValue);
            assertEquals(3, r.length);
        }
    }

    @Test
    public void happyPathTwoVersionEntity2() {
        {
            final State r = HTMLNamedEntitiesParser.get().lookup("ccedil");
            assertEquals("ccedil", r.entityOrFragment);
            assertTrue(r.isMatch);
            assertFalse(r.endNode);
            assertFalse(r.endsWithSemicolon);

            assertEquals("\u00E7", r.resolvedValue);
            assertEquals(6, r.length);
        }
        {
            final State r = HTMLNamedEntitiesParser.get().lookup("ccedil;");
            assertEquals("ccedil;", r.entityOrFragment);
            assertTrue(r.isMatch);
            assertTrue(r.endNode);
            assertTrue(r.endsWithSemicolon);

            assertEquals("\u00E7", r.resolvedValue);
            assertEquals(7, r.length);
        }
    }

    @Test
    public void fullyUnknown() {
        {
            final State r = HTMLNamedEntitiesParser.get().lookup("abc;");
            assertFalse(r.isMatch);
            assertFalse(r.endNode);
            assertFalse(r.endsWithSemicolon);

            assertEquals(null, r.resolvedValue);
            assertEquals("ab", r.entityOrFragment);
            assertEquals(2, r.length);
        }
    }

    /**
     * This must resolve to &not !!
     */
    @Test
    public void notit() {
        final State r = HTMLNamedEntitiesParser.get().lookup("notit;");
        assertTrue(r.isMatch);
        assertFalse(r.endNode);
        assertFalse(r.endsWithSemicolon);

        assertEquals("\u00AC", r.resolvedValue);
        assertEquals("not", r.entityOrFragment);
        assertEquals(3, r.length);
    }

    /**
     * This resolve to &not;
     */
    @Test
    public void notSemicolon() {
        final State r = HTMLNamedEntitiesParser.get().lookup("not;");
        assertTrue(r.isMatch);
        assertTrue(r.endNode);
        assertTrue(r.endsWithSemicolon);

        assertEquals("\u00AC", r.resolvedValue);
        assertEquals("not;", r.entityOrFragment);
        assertEquals(4, r.length);
    }

    /**
     * This resolve to &not
     */
    @Test
    public void notHash() {
        final State r = HTMLNamedEntitiesParser.get().lookup("not#");
        assertTrue(r.isMatch);
        assertFalse(r.endNode);
        assertFalse(r.endsWithSemicolon);

        assertEquals("\u00AC", r.resolvedValue);
        assertEquals("not", r.entityOrFragment);
        assertEquals(3, r.length);
    }

    /**
     * See that we can handle something out of the range of indexed
     * chars
     */
    @Test
    public void smallerThanA() {
        final State r = HTMLNamedEntitiesParser.get().lookup("9ot#");
        assertFalse(r.isMatch);
        assertFalse(r.endNode);
        assertFalse(r.endsWithSemicolon);

        assertEquals(null, r.resolvedValue);
        assertEquals("", r.entityOrFragment);
        assertEquals(0, r.length);
    }

    /**
     * See that we can handle something out of the range of indexed
     * chars
     */
    @Test
    public void largeThanLowercaseZ() {
        final State r = HTMLNamedEntitiesParser.get().lookup("{any");
        assertFalse(r.isMatch);
        assertFalse(r.endNode);
        assertFalse(r.endsWithSemicolon);

        assertEquals(null, r.resolvedValue);
        assertEquals("", r.entityOrFragment);
        assertEquals(0, r.length);
    }

    /**
     * Handle chars that are in the holes of our look up on
     * the root level
     */
    @Test
    public void oneCharInAHoleWithoutNextLevel() {
        final State r = HTMLNamedEntitiesParser.get().lookup("[any");
        assertFalse(r.isMatch);
        assertFalse(r.endNode);
        assertFalse(r.endsWithSemicolon);

        assertEquals(null, r.resolvedValue);
        assertEquals("", r.entityOrFragment);
        assertEquals(0, r.length);
    }

    /**
     * Test all entities
     *
     * @throws IOException
     */
    @Test
    public void allEntitiesWithSemicolonFull() throws IOException {
        final Properties props = new Properties();
        try (InputStream stream = HTMLNamedEntitiesParserTest.class.getResourceAsStream("html_entities.properties")) {
            props.load(stream);
        }

        props.forEach((k, v) -> {
            String key = (String) k;
            String value = (String) v;

            // we might have an empty line in it
            // we also don't want to test "old" entities at the moment aka no ; at the end
            if (key.trim().isEmpty()) {
                return;
            }

            final State r = HTMLNamedEntitiesParser.get().lookup(key);
            assertTrue(r.isMatch);

            if (key.endsWith(";")) {
                assertTrue(r.endNode);
                assertTrue(r.endsWithSemicolon);
            }
            else {
                // no ; means it is never and end node, because this
                // is for legacy entities
                assertFalse(r.endNode);
                assertFalse(r.endsWithSemicolon);
            }

            assertEquals(value, r.resolvedValue);
            assertEquals(key, r.entityOrFragment);
            assertEquals(key.length(), r.length);
        });
    }
}
