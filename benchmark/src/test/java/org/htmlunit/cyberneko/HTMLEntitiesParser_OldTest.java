/*
 * Copyright 2002-2009 Andy Clark, Marc Guillemot
 * Copyright 2017-2023 Ronald Brill
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

import org.htmlunit.cyberneko.HTMLEntitiesParser_Old;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link HTMLEntitiesParser_OldGenerator}.
 * @author Ronald Brill
 */
public class HTMLEntitiesParser_OldTest {

    @Test
    public void parseEuml() {
        final HTMLEntitiesParser_Old parser = new HTMLEntitiesParser_Old();

        final String input = "Euml ";
        int i = 0;
        while (parser.parse(input.charAt(i))) {
            i++;
        }
        assertEquals("\u00CB", parser.getMatch());
        assertEquals(1, parser.getRewindCount());
        assertFalse(parser.endsWithSemicolon());
    }

    @Test
    public void parseEuml_() {
        final HTMLEntitiesParser_Old parser = new HTMLEntitiesParser_Old();

        final String input = "Euml; ";
        int i = 0;
        while (parser.parse(input.charAt(i))) {
            i++;
        }
        assertEquals("\u00CB", parser.getMatch());
        assertEquals(0, parser.getRewindCount());
        assertTrue(parser.endsWithSemicolon());
    }

    @Test
    public void parseEumlX() {
        final HTMLEntitiesParser_Old parser = new HTMLEntitiesParser_Old();

        final String input = "EumlX";
        int i = 0;
        while (parser.parse(input.charAt(i))) {
            i++;
        }

        // valid without semicolon at end
        assertEquals("\u00CB", parser.getMatch());
        assertEquals(1, parser.getRewindCount());
        assertFalse(parser.endsWithSemicolon());
    }

    @Test
    public void parseEumX() {
        final HTMLEntitiesParser_Old parser = new HTMLEntitiesParser_Old();

        final String input = "EumX";
        int i = 0;
        while (parser.parse(input.charAt(i))) {
            i++;
        }
        assertNull(parser.getMatch());
        assertEquals(4, parser.getRewindCount());
        assertFalse(parser.endsWithSemicolon());
    }

    @Test
    public void parseEuroLt() {
        final HTMLEntitiesParser_Old parser = new HTMLEntitiesParser_Old();

        final String input = "euro<";
        int i = 0;
        while (parser.parse(input.charAt(i))) {
            i++;
        }

        // not valid without semicolon at end
        assertNull(parser.getMatch());
        assertEquals(5, parser.getRewindCount());
        assertFalse(parser.endsWithSemicolon());
    }

    @Test
    public void parseEuro() {
        final HTMLEntitiesParser_Old parser = new HTMLEntitiesParser_Old();

        final String input = "x80;";
        int i = 0;
        while (parser.parseNumeric(input.charAt(i))) {
            i++;
        }

        assertEquals("\u20ac", parser.getMatch());
        assertEquals(0, parser.getRewindCount());
    }

    @Test
    public void parseEuroMissingSemicolon() {
        final HTMLEntitiesParser_Old parser = new HTMLEntitiesParser_Old();

        final String input = "x80<";
        int i = 0;
        while (parser.parseNumeric(input.charAt(i))) {
            i++;
        }

        assertEquals("\u20ac", parser.getMatch());
        assertEquals(1, parser.getRewindCount());
    }

    /**
     * Test all entities
     * @throws IOException
     */
    @Test
    public void allEntities() throws IOException {
        final Properties props = new Properties();
        try (InputStream stream = HTMLEntitiesParser_OldTest.class.getResourceAsStream("html_entities.properties")) {
            props.load(stream);
        }

        props.forEach((k, v) -> {
            String key = (String) k;
            String value = (String) v;

            // we might have an empty line in it
            if (key.isEmpty()) {
                return;
            }

            final HTMLEntitiesParser_Old parser = new HTMLEntitiesParser_Old();

            int i = 0;
            String parserInput = key + " ";
            while (parser.parse(parserInput.charAt(i))) {
                i++;
            }

            assertEquals(value, parser.getMatch());
            assertEquals(key.endsWith(";") ? 0 : 1, parser.getRewindCount());
            assertEquals(key.endsWith(";"), parser.endsWithSemicolon());
        });
    }
}


