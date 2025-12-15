/*
 * Copyright (c) 2002-2009 Andy Clark, Marc Guillemot
 * Copyright (c) 2017-2025 Ronald Brill
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
 * String utilities class for utility functions not covered by third party libraries.
 *
 * @author Ronald Brill
 */
public final class StringUtils {

    /**
     * Disallow instantiation of this class.
     */
    private StringUtils() {
        // Empty.
    }

    /**
     * Returns true if the param is not null and empty. This is different from
     * org.apache.commons.lang3.StringUtils#isEmpty(CharSequence) because
     * this returns false if the provided string is null.
     *
     * @param s the string to check
     * @return true if the param is not null and empty
     */
    public static boolean isEmptyString(final CharSequence s) {
        return s != null && s.length() == 0;
    }

    /**
     * @param expected the char that we expect
     * @param s the string to check
     * @return true if the provided string has only one char and this matches the expectation
     */
    public static boolean equalsChar(final char expected, final CharSequence s) {
        return s != null && s.length() == 1 && expected == s.charAt(0);
    }
}
