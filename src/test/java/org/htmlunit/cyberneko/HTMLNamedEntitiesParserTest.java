/*
 * Copyright (c) 2002-2009 Andy Clark, Marc Guillemot
 * Copyright (c) 2017-2024 Ronald Brill
 * Copyright 2023 René Schwietzke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.htmlunit.cyberneko;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.htmlunit.cyberneko.HTMLNamedEntitiesParser.State;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link HTMLNamedEntitiesParser}.
 *
 * @author René Schwietzke
 * @author Ronald Brill
 */
public class HTMLNamedEntitiesParserTest {
    @Test
    public void happyPath() {
        final State r = HTMLNamedEntitiesParser.INSTANCE.lookup("Beta;");
        assertTrue(r.isMatch_);
        assertTrue(r.endNode_);
        assertTrue(r.endsWithSemicolon_);

        assertEquals("\u0392", r.resolvedValue_);
        assertEquals("Beta;", r.entityOrFragment_);
        assertEquals(5, r.length_);
    }

    @Test
    public void happyPathOneCharDiff() {
        {
            final State r = HTMLNamedEntitiesParser.INSTANCE.lookup("Colon;");
            assertTrue(r.isMatch_);
            assertTrue(r.endNode_);
            assertTrue(r.endsWithSemicolon_);

            assertEquals("\u2237", r.resolvedValue_);
            assertEquals("Colon;", r.entityOrFragment_);
            assertEquals(6, r.length_);
        }
        {
            final State r = HTMLNamedEntitiesParser.INSTANCE.lookup("Colone;");
            assertTrue(r.isMatch_);
            assertTrue(r.endNode_);
            assertTrue(r.endsWithSemicolon_);

            assertEquals("\u2A74", r.resolvedValue_);
            assertEquals("Colone;", r.entityOrFragment_);
            assertEquals(7, r.length_);
        }
    }

    @Test
    public void happyPathTwoVersionEntity() {
        {
            final State r = HTMLNamedEntitiesParser.INSTANCE.lookup("gt");
            assertEquals("gt", r.entityOrFragment_);
            assertTrue(r.isMatch_);
            assertFalse(r.endNode_);
            assertFalse(r.endsWithSemicolon_);

            assertEquals(">", r.resolvedValue_);
            assertEquals(2, r.length_);
        }
        {
            final State r = HTMLNamedEntitiesParser.INSTANCE.lookup("gt;");
            assertEquals("gt;", r.entityOrFragment_);
            assertTrue(r.isMatch_);
            assertTrue(r.endNode_);
            assertTrue(r.endsWithSemicolon_);

            assertEquals(">", r.resolvedValue_);
            assertEquals(3, r.length_);
        }
    }

    @Test
    public void happyPathTwoVersionEntity2() {
        {
            final State r = HTMLNamedEntitiesParser.INSTANCE.lookup("ccedil");
            assertEquals("ccedil", r.entityOrFragment_);
            assertTrue(r.isMatch_);
            assertFalse(r.endNode_);
            assertFalse(r.endsWithSemicolon_);

            assertEquals("\u00E7", r.resolvedValue_);
            assertEquals(6, r.length_);
        }
        {
            final State r = HTMLNamedEntitiesParser.INSTANCE.lookup("ccedil;");
            assertEquals("ccedil;", r.entityOrFragment_);
            assertTrue(r.isMatch_);
            assertTrue(r.endNode_);
            assertTrue(r.endsWithSemicolon_);

            assertEquals("\u00E7", r.resolvedValue_);
            assertEquals(7, r.length_);
        }
    }

    @Test
    public void fullyUnknown() {
        {
            final State r = HTMLNamedEntitiesParser.INSTANCE.lookup("abc;");
            assertFalse(r.isMatch_);
            assertFalse(r.endNode_);
            assertFalse(r.endsWithSemicolon_);

            assertEquals(null, r.resolvedValue_);
            assertEquals("ab", r.entityOrFragment_);
            assertEquals(2, r.length_);
        }
    }

    /**
     * This must resolve to &not !!
     */
    @Test
    public void notit() {
        final State r = HTMLNamedEntitiesParser.INSTANCE.lookup("notit;");
        assertTrue(r.isMatch_);
        assertFalse(r.endNode_);
        assertFalse(r.endsWithSemicolon_);

        assertEquals("\u00AC", r.resolvedValue_);
        assertEquals("not", r.entityOrFragment_);
        assertEquals(3, r.length_);
    }

    /**
     * This resolve to &not;
     */
    @Test
    public void notSemicolon() {
        final State r = HTMLNamedEntitiesParser.INSTANCE.lookup("not;");
        assertTrue(r.isMatch_);
        assertTrue(r.endNode_);
        assertTrue(r.endsWithSemicolon_);

        assertEquals("\u00AC", r.resolvedValue_);
        assertEquals("not;", r.entityOrFragment_);
        assertEquals(4, r.length_);
    }

    /**
     * This resolve to &not
     */
    @Test
    public void notHash() {
        final State r = HTMLNamedEntitiesParser.INSTANCE.lookup("not#");
        assertTrue(r.isMatch_);
        assertFalse(r.endNode_);
        assertFalse(r.endsWithSemicolon_);

        assertEquals("\u00AC", r.resolvedValue_);
        assertEquals("not", r.entityOrFragment_);
        assertEquals(3, r.length_);
    }

    /**
     * See that we can handle something out of the range of indexed
     * chars
     */
    @Test
    public void smallerThanA() {
        final State r = HTMLNamedEntitiesParser.INSTANCE.lookup("9ot#");
        assertFalse(r.isMatch_);
        assertFalse(r.endNode_);
        assertFalse(r.endsWithSemicolon_);

        assertEquals(null, r.resolvedValue_);
        assertEquals("", r.entityOrFragment_);
        assertEquals(0, r.length_);
    }

    /**
     * See that we can handle something out of the range of indexed
     * chars
     */
    @Test
    public void largeThanLowercaseZ() {
        final State r = HTMLNamedEntitiesParser.INSTANCE.lookup("{any");
        assertFalse(r.isMatch_);
        assertFalse(r.endNode_);
        assertFalse(r.endsWithSemicolon_);

        assertEquals(null, r.resolvedValue_);
        assertEquals("", r.entityOrFragment_);
        assertEquals(0, r.length_);
    }

    /**
     * Handle chars that are in the holes of our look up on
     * the root level
     */
    @Test
    public void oneCharInAHoleWithoutNextLevel() {
        final State r = HTMLNamedEntitiesParser.INSTANCE.lookup("[any");
        assertFalse(r.isMatch_);
        assertFalse(r.endNode_);
        assertFalse(r.endsWithSemicolon_);

        assertEquals(null, r.resolvedValue_);
        assertEquals("", r.entityOrFragment_);
        assertEquals(0, r.length_);
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
            final String key = (String) k;
            final String value = (String) v;

            // we might have an empty line in it
            // we also don't want to test "old" entities at the moment aka no ; at the end
            if (key.trim().isEmpty()) {
                return;
            }

            final State r = HTMLNamedEntitiesParser.INSTANCE.lookup(key);
            assertTrue(r.isMatch_);

            if (key.endsWith(";")) {
                assertTrue(r.endNode_);
                assertTrue(r.endsWithSemicolon_);
            }
            else {
                // no ; means it is never and end node, because this
                // is for legacy entities
                assertFalse(r.endNode_);
                assertFalse(r.endsWithSemicolon_);
            }

            assertEquals(value, r.resolvedValue_);
            assertEquals(key, r.entityOrFragment_);
            assertEquals(key.length(), r.length_);
        });
    }

    /**
     * Test lookupEntityRefFor()
     *
     * @throws IOException
     */
    @Test
    public void lookupEntityRefFor() throws IOException {
        assertNull(HTMLNamedEntitiesParser.INSTANCE.lookupEntityRefFor("a"));

        assertEquals("&auml;", HTMLNamedEntitiesParser.INSTANCE.lookupEntityRefFor("ä"));
        assertEquals("&Ouml;", HTMLNamedEntitiesParser.INSTANCE.lookupEntityRefFor("Ö"));

        // make sure we return the entry with the semicolon at end
        assertEquals("&yacute;", HTMLNamedEntitiesParser.INSTANCE.lookupEntityRefFor("\u00FD"));

    }
}
