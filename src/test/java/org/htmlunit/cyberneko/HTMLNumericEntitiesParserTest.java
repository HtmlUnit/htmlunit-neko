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

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link HTMLNumericEntities}.
 * @author René Schwietzke
 */
public class HTMLNumericEntitiesParserTest {
    @Test
    public void parseEuro() {
        final HTMLUnicodeEntitiesParser parser = new HTMLUnicodeEntitiesParser();

        final String input = "x80;";
        int i = 0;
        while (parser.parseNumeric(input.charAt(i))) {
            i++;
        }

        assertEquals("\u20ac", parser.getMatch());
        assertEquals(0, parser.getRewindCount());
    }

    @Test
    public void parseEuroUppercase() {
        final HTMLUnicodeEntitiesParser parser = new HTMLUnicodeEntitiesParser();

        final String input = "X80;";
        int i = 0;
        while (parser.parseNumeric(input.charAt(i))) {
            i++;
        }

        assertEquals("\u20ac", parser.getMatch());
        assertEquals(0, parser.getRewindCount());
    }

    @Test
    public void parseBroken() {
        final HTMLUnicodeEntitiesParser parser = new HTMLUnicodeEntitiesParser();

        final String input = "A80;";
        int i = 0;
        while (parser.parseNumeric(input.charAt(i))) {
            i++;
        }

        assertEquals(null, parser.getMatch());
        assertEquals(1, parser.getRewindCount());
    }

    @Test
    public void parseLTAsDecimal() {
        final HTMLUnicodeEntitiesParser parser = new HTMLUnicodeEntitiesParser();

        final String input = "60;";
        int i = 0;
        while (parser.parseNumeric(input.charAt(i))) {
            i++;
        }

        assertEquals("<", parser.getMatch());
        assertEquals(0, parser.getRewindCount());
    }

    @Test
    public void parseLTAsDecimalBroken() {
        final HTMLUnicodeEntitiesParser parser = new HTMLUnicodeEntitiesParser();

        final String input = "60 ";
        int i = 0;
        while (parser.parseNumeric(input.charAt(i))) {
            i++;
        }

        assertEquals("<", parser.getMatch());
        assertEquals(1, parser.getRewindCount());
    }


    @Test
    public void parseEuroMissingSemicolon() {
        final HTMLUnicodeEntitiesParser parser = new HTMLUnicodeEntitiesParser();

        final String input = "x80<";
        int i = 0;
        while (parser.parseNumeric(input.charAt(i))) {
            i++;
        }

        assertEquals("\u20ac", parser.getMatch());
        assertEquals(1, parser.getRewindCount());
    }

    @Test
    public void parseNullChar() {
        final HTMLUnicodeEntitiesParser parser = new HTMLUnicodeEntitiesParser();

        final String input = "x00;";
        int i = 0;
        while (parser.parseNumeric(input.charAt(i))) {
            i++;
        }

        assertEquals("\uFFFD", parser.getMatch());
        assertEquals(0, parser.getRewindCount());
    }

    @Test
    public void parseOverflowRange() {
        final HTMLUnicodeEntitiesParser parser = new HTMLUnicodeEntitiesParser();

        final String input = "x11FFFF;";
        int i = 0;
        while (parser.parseNumeric(input.charAt(i))) {
            i++;
        }

        assertEquals("\uFFFD", parser.getMatch());
        assertEquals(0, parser.getRewindCount());
    }

    @Test
    public void parseSurrogate() {
        final HTMLUnicodeEntitiesParser parser = new HTMLUnicodeEntitiesParser();

        final String input = "xD800;";
        int i = 0;
        while (parser.parseNumeric(input.charAt(i))) {
            i++;
        }

        assertEquals("\uFFFD", parser.getMatch());
        assertEquals(6, parser.getRewindCount());
    }

    @Test
    public void parseNonCharacterLow() {
        final HTMLUnicodeEntitiesParser parser = new HTMLUnicodeEntitiesParser();

        final String input = "x80;";
        int i = 0;
        while (parser.parseNumeric(input.charAt(i))) {
            i++;
        }

        assertEquals("\u20AC", parser.getMatch());
        assertEquals(0, parser.getRewindCount());
    }

    @Test
    public void parseNonCharacterHighLowercase() {
        final HTMLUnicodeEntitiesParser parser = new HTMLUnicodeEntitiesParser();

        final String input = "x9f;";
        int i = 0;
        while (parser.parseNumeric(input.charAt(i))) {
            i++;
        }

        assertEquals("\u0178", parser.getMatch());
        assertEquals(0, parser.getRewindCount());
    }

    @Test
    public void parseNonCharacterHighUppercase() {
        final HTMLUnicodeEntitiesParser parser = new HTMLUnicodeEntitiesParser();

        final String input = "x9F;";
        int i = 0;
        while (parser.parseNumeric(input.charAt(i))) {
            i++;
        }

        assertEquals("\u0178", parser.getMatch());
        assertEquals(0, parser.getRewindCount());
    }
}


