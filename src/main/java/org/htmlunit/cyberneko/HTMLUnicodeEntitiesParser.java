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

    private static final int STATE_HEXADECIMAL_CHAR = -102;
    private static final int STATE_DECIMAL_CHAR = -104;
    private static final int STATE_HEXADECIMAL_START = -103;

    private static final int STATE_NUMERIC_CHAR_END_SEMICOLON_MISSING = -105;
    private static final int STATE_ABSENCE_OF_DIGITS_IN_NUMERIC_CHARACTER_REFERENCE = -106;

    private int state_;
    private int consumedCount_;
    private String match_;
    private int code_;
    private int matchLength_;

    public String getMatch() {
        return match_;
    }

    public int getRewindCount() {
        return consumedCount_ - matchLength_;
    }

    public HTMLUnicodeEntitiesParser() {
        state_ = STATE_START;
    }

    public void setMatchFromCode() {
        // If the number is 0x00, then this is a null-character-reference parse error. Set the character reference code to 0xFFFD.
        // If the number is greater than 0x10FFFF, then this is a character-reference-outside-unicode-range parse error. Set the character reference code to 0xFFFD.
        if ((0x00 == code_) || (code_ > 0x10FFFF)) {
            match_ = "\uFFFD";
            matchLength_ = consumedCount_;
            return;
        }

        // If the number is a surrogate, then this is a surrogate-character-reference parse error. Set the character reference code to 0xFFFD
        if (Character.isSurrogate((char) code_)) {
            match_ = "\uFFFD";
            return;
        }

        // If the number is a noncharacter, then this is a noncharacter-character-reference parse error.

        // If the number is 0x0D, or a control that's not ASCII whitespace, then this is a control-character-reference parse error.

        // If the number is one of the numbers in the first column of the following table, then find the row with that number in the first column,
        // and set the character reference code to the number in the second column of that row.
        switch (code_) {
            case 0x80:
                match_ = "\u20AC";
                matchLength_ = consumedCount_;
                return;

            case 0x82:
                match_ = "\u201A";
                matchLength_ = consumedCount_;
                return;

            case 0x83:
                match_ = "\u0192";
                matchLength_ = consumedCount_;
                return;

            case 0x84:
                match_ = "\u201E";
                matchLength_ = consumedCount_;
                return;

            case 0x85:
                match_ = "\u2026";
                matchLength_ = consumedCount_;
                return;

            case 0x86:
                match_ = "\u2020";
                matchLength_ = consumedCount_;
                return;

            case 0x87:
                match_ = "\u2021";
                matchLength_ = consumedCount_;
                return;

            case 0x88:
                match_ = "\u02C6";
                matchLength_ = consumedCount_;
                return;

            case 0x89:
                match_ = "\u2030";
                matchLength_ = consumedCount_;
                return;

            case 0x8A:
                match_ = "\u0160";
                matchLength_ = consumedCount_;
                return;

            case 0x8B:
                match_ = "\u2039";
                matchLength_ = consumedCount_;
                return;

            case 0x8C:
                match_ = "\u0152";
                matchLength_ = consumedCount_;
                return;

            case 0x8E:
                match_ = "\u017D";
                matchLength_ = consumedCount_;
                return;

            case 0x91:
                match_ = "\u2018";
                matchLength_ = consumedCount_;
                return;

            case 0x92:
                match_ = "\u2019";
                matchLength_ = consumedCount_;
                return;

            case 0x93:
                match_ = "\u201C";
                matchLength_ = consumedCount_;
                return;

            case 0x94:
                match_ = "\u201D";
                matchLength_ = consumedCount_;
                return;

            case 0x95:
                match_ = "\u2022";
                matchLength_ = consumedCount_;
                return;

            case 0x96:
                match_ = "\u2013";
                matchLength_ = consumedCount_;
                return;

            case 0x97:
                match_ = "\u2014";
                matchLength_ = consumedCount_;
                return;

            case 0x98:
                match_ = "\u20DC";
                matchLength_ = consumedCount_;
                return;

            case 0x99:
                match_ = "\u2122";
                matchLength_ = consumedCount_;
                return;

            case 0x9A:
                match_ = "\u0161";
                matchLength_ = consumedCount_;
                return;

            case 0x9B:
                match_ = "\u203A";
                matchLength_ = consumedCount_;
                return;

            case 0x9C:
                match_ = "\u0153";
                matchLength_ = consumedCount_;
                return;

            case 0x9E:
                match_ = "\u017E";
                matchLength_ = consumedCount_;
                return;

            case 0x9F:
                match_ = "\u0178";
                matchLength_ = consumedCount_;
                return;

            default:
                break;
        }
        match_ = new String(Character.toChars(code_));
        matchLength_ = consumedCount_;
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
        consumedCount_++;
        switch (state_) {
            case STATE_START:
                if ('X' == current || 'x' == current) {
                    // spec suggests a HEX START state
                    // 13.2.5.76 Hexadecimal character reference start state
                    state_ = STATE_HEXADECIMAL_START;
                    code_ = 0;
                    return true;
                }
                if ('0' <= current && current <= '9') {
                    state_ = STATE_DECIMAL_CHAR;
                    code_ = (code_ * 10) + current - 0x30;
                    return true;
                }
                break;
            case STATE_HEXADECIMAL_START:
                // this block addresses &#x and &#x; cases
                // Ideally we would just change state and reconsume,
                // but the parser impl does not permit that, hence
                // some duplicate code here
                if ('0' <= current && current <= '9') {
                    state_ = STATE_HEXADECIMAL_CHAR;
                    code_ = (code_ * 16) + current - 0x30;
                    return true;
                }
                if ('A' <= current && current <= 'F') {
                    state_ = STATE_HEXADECIMAL_CHAR;
                    code_ = (code_ * 16) + current - 0x37;
                    return true;
                }
                if ('a' <= current && current <= 'f') {
                    state_ = STATE_HEXADECIMAL_CHAR;
                    code_ = (code_ * 16) + current - 0x57;
                    return true;
                }

                state_ = STATE_ABSENCE_OF_DIGITS_IN_NUMERIC_CHARACTER_REFERENCE;
                break;
            case STATE_HEXADECIMAL_CHAR:
                if ('0' <= current && current <= '9') {
                    code_ = (code_ * 16) + current - 0x30;
                    return true;
                }
                if ('A' <= current && current <= 'F') {
                    code_ = (code_ * 16) + current - 0x37;
                    return true;
                }
                if ('a' <= current && current <= 'f') {
                    code_ = (code_ * 16) + current - 0x57;
                    return true;
                }
                if (';' == current) {
                    setMatchFromCode();
                    return false;
                }

                state_ = STATE_NUMERIC_CHAR_END_SEMICOLON_MISSING;
                setMatchFromCode();
                matchLength_ = consumedCount_ - 1;
                break;
            case STATE_DECIMAL_CHAR:
                if ('0' <= current && current <= '9') {
                    code_ = (code_ * 10) + current - 0x30;
                    return true;
                }
                if (';' == current) {
                    setMatchFromCode();
                    return false;
                }

                state_ = STATE_NUMERIC_CHAR_END_SEMICOLON_MISSING;
                setMatchFromCode();
                matchLength_ = consumedCount_ - 1;
                break;
        }
        return false;
    }

}
