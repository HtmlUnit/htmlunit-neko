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
package org.htmlunit.cyberneko.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link HTMLScannerBufferedReader}.
 *
 * @author Ronald Brill
 */
public class HTMLScannerBufferedReaderTest {

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void loadWholeBuffer() throws Exception {
        HTMLScannerBufferedReader reader = new HTMLScannerBufferedReader(new StringReader("Neko"), 20, "UTF-8");

        assertEquals(0, reader.length_);
        assertEquals(0, reader.offset_);
        assertEquals(0, reader.getCharacterOffset());
        // strange but the buffer is not filled at start
        assertFalse(reader.hasNext());

        int bytes = reader.loadWholeBuffer();
        assertEquals(4, bytes);
        assertEquals(4, reader.length_);
        assertEquals(0, reader.offset_);
        assertEquals(0, reader.getCharacterOffset());

        assertTrue(reader.hasNext());

        bytes = reader.loadWholeBuffer();
        assertEquals(-1, bytes);
        assertEquals(0, reader.length_);
        assertEquals(0, reader.offset_);
        assertEquals(0, reader.getCharacterOffset());

        assertFalse(reader.hasNext());
    }
}
