/*
 * Copyright (c) 2002-2009 Andy Clark, Marc Guillemot
 * Copyright (c) 2017-2024 Ronald Brill
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

/**
 * Parser for the Pre-defined named HTML entities.
 * <a href="https://html.spec.whatwg.org/multipage/parsing.html#character-reference-state">12.2.5.72 Character reference state</a>
 * <p>
 * From the spec:<br>
 * Consume the maximum number of characters possible, with the consumed characters matching
 * one of the identifiers in the first column of the named character references table
 * (in a case-sensitive manner).
 * Append each character to the temporary buffer when it's consumed.
 *
 * @author Ronald Brill
 */
public class HTMLUnicodeEntitiesParser {
    public static final int STATE_START = 0;
    private static final int STATE_ENDS_WITH_SEMICOLON = -2;

    private static final int STATE_HEXADECIMAL_CHAR = -102;
    private static final int STATE_DECIMAL_CHAR = -104;
    private static final int STATE_HEXADECIMAL_START = -103;

    private static final int STATE_NUMERIC_CHAR_END_SEMICOLON_MISSING = -105;
    private static final int STATE_ABSENCE_OF_DIGITS_IN_NUMERIC_CHARACTER_REFERENCE = -106;

    private int state;
    private int consumedCount;
    private String match;
    private int code;
    private int matchLength;

    public String getMatch() {
        return match;
    }

    public int getMatchLength() {
        return matchLength;
    }

    public int getRewindCount() {
        return consumedCount - matchLength;
    }

    public boolean endsWithSemicolon() {
        return STATE_ENDS_WITH_SEMICOLON == state;
    }

    public HTMLUnicodeEntitiesParser() {
        state = STATE_START;
    }

    public void setMatchFromCode() {
        // If the number is 0x00, then this is a null-character-reference parse error. Set the character reference code to 0xFFFD.
        // If the number is greater than 0x10FFFF, then this is a character-reference-outside-unicode-range parse error. Set the character reference code to 0xFFFD.
        if ((0x00 == code) || (code > 0x10FFFF)) {
            match = "\uFFFD";
            matchLength = consumedCount;
            return;
        }

        // If the number is a surrogate, then this is a surrogate-character-reference parse error. Set the character reference code to 0xFFFD
        if (Character.isSurrogate((char) code)) {
            match = "\uFFFD";
            return;
        }

        // If the number is a noncharacter, then this is a noncharacter-character-reference parse error.

        // If the number is 0x0D, or a control that's not ASCII whitespace, then this is a control-character-reference parse error.

        // If the number is one of the numbers in the first column of the following table, then find the row with that number in the first column,
        // and set the character reference code to the number in the second column of that row.
        switch (code) {
            case 0x80:
                match = "\u20AC";
                matchLength = consumedCount;
                return;

            case 0x82:
                match = "\u201A";
                matchLength = consumedCount;
                return;

            case 0x83:
                match = "\u0192";
                matchLength = consumedCount;
                return;

            case 0x84:
                match = "\u201E";
                matchLength = consumedCount;
                return;

            case 0x85:
                match = "\u2026";
                matchLength = consumedCount;
                return;

            case 0x86:
                match = "\u2020";
                matchLength = consumedCount;
                return;

            case 0x87:
                match = "\u2021";
                matchLength = consumedCount;
                return;

            case 0x88:
                match = "\u02C6";
                matchLength = consumedCount;
                return;

            case 0x89:
                match = "\u2030";
                matchLength = consumedCount;
                return;

            case 0x8A:
                match = "\u0160";
                matchLength = consumedCount;
                return;

            case 0x8B:
                match = "\u2039";
                matchLength = consumedCount;
                return;

            case 0x8C:
                match = "\u0152";
                matchLength = consumedCount;
                return;

            case 0x8E:
                match = "\u017D";
                matchLength = consumedCount;
                return;

            case 0x91:
                match = "\u2018";
                matchLength = consumedCount;
                return;

            case 0x92:
                match = "\u2019";
                matchLength = consumedCount;
                return;

            case 0x93:
                match = "\u201C";
                matchLength = consumedCount;
                return;

            case 0x94:
                match = "\u201D";
                matchLength = consumedCount;
                return;

            case 0x95:
                match = "\u2022";
                matchLength = consumedCount;
                return;

            case 0x96:
                match = "\u2013";
                matchLength = consumedCount;
                return;

            case 0x97:
                match = "\u2014";
                matchLength = consumedCount;
                return;

            case 0x98:
                match = "\u20DC";
                matchLength = consumedCount;
                return;

            case 0x99:
                match = "\u2122";
                matchLength = consumedCount;
                return;

            case 0x9A:
                match = "\u0161";
                matchLength = consumedCount;
                return;

            case 0x9B:
                match = "\u203A";
                matchLength = consumedCount;
                return;

            case 0x9C:
                match = "\u0153";
                matchLength = consumedCount;
                return;

            case 0x9E:
                match = "\u017E";
                matchLength = consumedCount;
                return;

            case 0x9F:
                match = "\u0178";
                matchLength = consumedCount;
                return;

            default:
                break;
        }
        match = new String(Character.toChars(code));
        matchLength = consumedCount;
    }

    /**
     * Parses a numeric entity such as #x64; or #42; The
     * ampersand must not be presented.
     *
     * @param current the next character to check
     *
     * @return if we have reached the end of the parsing
     */
    public boolean parseNumeric(final int current) {
        consumedCount++;
        switch (state) {
            case STATE_START:
                if ('X' == current || 'x' == current) {
                    // spec suggests a HEX START state
                    // 13.2.5.76 Hexadecimal character reference start state
                    state = STATE_HEXADECIMAL_START;
                    code = 0;
                    return true;
                }
                if ('0' <= current && current <= '9') {
                    state = STATE_DECIMAL_CHAR;
                    code = (code * 10) + current - 0x30;
                    return true;
                }
                break;
            case STATE_HEXADECIMAL_START:
                // this block addresses &#x and &#x; cases
                // Ideally we would just change state and reconsume,
                // but the parser impl does not permit that, hence
                // some duplicate code here
                if ('0' <= current && current <= '9') {
                    state = STATE_HEXADECIMAL_CHAR;
                    code = (code * 16) + current - 0x30;
                    return true;
                }
                if ('A' <= current && current <= 'F') {
                    state = STATE_HEXADECIMAL_CHAR;
                    code = (code * 16) + current - 0x37;
                    return true;
                }
                if ('a' <= current && current <= 'f') {
                    state = STATE_HEXADECIMAL_CHAR;
                    code = (code * 16) + current - 0x57;
                    return true;
                }

                state = STATE_ABSENCE_OF_DIGITS_IN_NUMERIC_CHARACTER_REFERENCE;
                break;
            case STATE_HEXADECIMAL_CHAR:
                if ('0' <= current && current <= '9') {
                    code = (code * 16) + current - 0x30;
                    return true;
                }
                if ('A' <= current && current <= 'F') {
                    code = (code * 16) + current - 0x37;
                    return true;
                }
                if ('a' <= current && current <= 'f') {
                    code = (code * 16) + current - 0x57;
                    return true;
                }
                if (';' == current) {
                    setMatchFromCode();
                    return false;
                }

                state = STATE_NUMERIC_CHAR_END_SEMICOLON_MISSING;
                setMatchFromCode();
                matchLength = consumedCount - 1;
                break;
            case STATE_DECIMAL_CHAR:
                if ('0' <= current && current <= '9') {
                    code = (code * 10) + current - 0x30;
                    return true;
                }
                if (';' == current) {
                    setMatchFromCode();
                    return false;
                }

                state = STATE_NUMERIC_CHAR_END_SEMICOLON_MISSING;
                setMatchFromCode();
                matchLength = consumedCount - 1;
                break;
        }
        return false;
    }

}
