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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link StringUtils}.
 *
 * @author Ronald Brill
 */
public class StringUtilsTest {

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void isEmptyString() throws Exception {
        assertFalse(StringUtils.isEmptyString(null));
        assertTrue(StringUtils.isEmptyString(""));
        assertFalse(StringUtils.isEmptyString(" "));
        assertFalse(StringUtils.isEmptyString("\t"));
        assertFalse(StringUtils.isEmptyString("\r"));
        assertFalse(StringUtils.isEmptyString("\n"));
        assertFalse(StringUtils.isEmptyString("string"));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void equalsChar() throws Exception {
        assertFalse(StringUtils.equalsChar('#', null));
        assertFalse(StringUtils.equalsChar('#', ""));
        assertFalse(StringUtils.equalsChar('#', " "));
        assertTrue(StringUtils.equalsChar('#', "#"));
        assertFalse(StringUtils.equalsChar('#', "##"));
        assertFalse(StringUtils.equalsChar('#', " #"));
        assertFalse(StringUtils.equalsChar('#', "# "));
    }
}
