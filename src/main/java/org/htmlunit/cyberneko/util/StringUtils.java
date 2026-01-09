/*
 * Copyright (c) 2002-2009 Andy Clark, Marc Guillemot
 * Copyright (c) 2017-2026 Ronald Brill
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
package org.htmlunit.cyberneko.util;

/**
 * String utilities class providing utility functions not covered by third party libraries.
 *
 * <p>This class contains static utility methods for common string operations used
 * throughout the HtmlUnit NekoHtml parser.  It focuses on lightweight, performance-oriented
 * string checks that avoid creating unnecessary String objects or using expensive operations.</p>
 *
 * @author Ronald Brill
 */
public final class StringUtils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private StringUtils() {
        // Empty - utility class should not be instantiated
    }

    /**
     * Checks if the provided character sequence is not null and has zero length.
     *
     * <p>This method differs from {@code org.apache.commons.lang3.StringUtils#isEmpty(CharSequence)}
     * in that it returns {@code false} if the provided string is {@code null}.</p>
     *
     * @param s the character sequence to check, may be null
     * @return {@code true} if the sequence is not null AND has length of zero; {@code false} otherwise
     */
    public static boolean isEmptyString(final CharSequence s) {
        return s != null && s.length() == 0;
    }

    /**
     * Checks if the provided character sequence consists of exactly one character that matches
     * the expected character.
     *
     * <p>This is an optimized equality check for single-character strings, avoiding the overhead
     * of full string comparison.  It's particularly useful during HTML parsing when checking for
     * single-character tokens or delimiters.</p>
     *
     * @param expected the character that we expect to match
     * @param s the character sequence to check, may be null
     * @return {@code true} if and only if the provided sequence is not null, has exactly one character,
     *         and that character equals the expected character; {@code false} otherwise
     */
    public static boolean equalsChar(final char expected, final CharSequence s) {
        return s != null && s.length() == 1 && expected == s.charAt(0);
    }
}
