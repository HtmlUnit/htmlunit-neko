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
package org.htmlunit.cyberneko.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PlaybackInputStream}.
 *
 * @author Ronald Brill
 */
public class PlaybackInputStreamTest {

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void detectEncodingOneByte() throws Exception {
        final byte[] bytes = new byte[] {(byte) 0x20};

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                PlaybackInputStream pbis = new PlaybackInputStream(bais)) {

            final String[] encoding = new String[2];
            pbis.detectBomEncoding(encoding);

            assertNull(encoding[0]);
            assertNull(encoding[1]);

            assertEquals(32, pbis.read());
            assertEquals(-1, pbis.read());
        }
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void detectEncodingTwoBytes() throws Exception {
        final byte[] bytes = new byte[] {(byte) 0x20, (byte) 0x21};

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                PlaybackInputStream pbis = new PlaybackInputStream(bais)) {

            final String[] encoding = new String[2];
            pbis.detectBomEncoding(encoding);

            assertNull(encoding[0]);
            assertNull(encoding[1]);

            assertEquals(32, pbis.read());
            assertEquals(33, pbis.read());
            assertEquals(-1, pbis.read());
        }
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void detectEncodingThreeBytes() throws Exception {
        final byte[] bytes = new byte[] {(byte) 0x20, (byte) 0x21, (byte) 0x22};

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                PlaybackInputStream pbis = new PlaybackInputStream(bais)) {

            final String[] encoding = new String[2];
            pbis.detectBomEncoding(encoding);

            assertNull(encoding[0]);
            assertNull(encoding[1]);

            assertEquals(32, pbis.read());
            assertEquals(33, pbis.read());
            assertEquals(34, pbis.read());
            assertEquals(-1, pbis.read());
        }
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void detectEncodingUtf8() throws Exception {
        final byte[] bytes = new byte[] {(byte) 0xef, (byte) 0xbb, (byte) 0xbf, (byte) 0x20};

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                PlaybackInputStream pbis = new PlaybackInputStream(bais)) {

            final String[] encoding = new String[2];
            pbis.detectBomEncoding(encoding);

            assertEquals("UTF-8", encoding[0]);
            assertEquals("UTF8", encoding[1]);

            assertEquals(32, pbis.read());
            assertEquals(-1, pbis.read());

            // FIXME
            // pbis.playback();
            // assertEquals(32, pbis.read());
            // assertEquals(-1, pbis.read());
        }
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void detectEncodingUtf8Part() throws Exception {
        final byte[] bytes = new byte[] {(byte) 0xef, (byte) 0xbb, (byte) 0x20};

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                PlaybackInputStream pbis = new PlaybackInputStream(bais)) {

            final String[] encoding = new String[2];
            pbis.detectBomEncoding(encoding);

            assertNull(encoding[0]);
            assertNull(encoding[1]);

            assertEquals(-17, pbis.read());
            assertEquals(-69, pbis.read());
            assertEquals(32, pbis.read());
            assertEquals(-1, pbis.read());

            pbis.playback();
            assertEquals(-17, pbis.read());
            assertEquals(-69, pbis.read());
            assertEquals(32, pbis.read());
            assertEquals(-1, pbis.read());
        }
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void detectEncodingUtf8PartBufer() throws Exception {
        final byte[] bytes = new byte[] {(byte) 0xef, (byte) 0xbb, (byte) 0x20};

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                PlaybackInputStream pbis = new PlaybackInputStream(bais)) {

            final String[] encoding = new String[2];
            pbis.detectBomEncoding(encoding);

            assertNull(encoding[0]);
            assertNull(encoding[1]);

            final byte[] read = new byte[1024];
            int count = pbis.read(read);

            assertEquals(3, count);
            assertEquals(-17, read[0]);
            assertEquals(-69, read[1]);
            assertEquals(32, read[2]);
            assertEquals(0, read[3]);
            assertEquals(-1, pbis.read(read));

            pbis.playback();
            count = pbis.read(read);

            assertEquals(3, count);
            assertEquals(-17, read[0]);
            assertEquals(-69, read[1]);
            assertEquals(32, read[2]);
            assertEquals(0, read[3]);

            assertEquals(-1, pbis.read(read));
        }
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void detectEncodingUtf16LE() throws Exception {
        final byte[] bytes = new byte[] {(byte) 0xff, (byte) 0xfe, (byte) 0x20};

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                PlaybackInputStream pbis = new PlaybackInputStream(bais)) {

            final String[] encoding = new String[2];
            pbis.detectBomEncoding(encoding);

            assertEquals("UTF-16", encoding[0]);
            assertEquals("UnicodeLittleUnmarked", encoding[1]);

            assertEquals(32, pbis.read());
            assertEquals(-1, pbis.read());
        }
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void detectEncodingUtf16LEPart() throws Exception {
        final byte[] bytes = new byte[] {(byte) 0xff, (byte) 0x20};

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                PlaybackInputStream pbis = new PlaybackInputStream(bais)) {

            final String[] encoding = new String[2];
            pbis.detectBomEncoding(encoding);

            assertNull(encoding[0]);
            assertNull(encoding[1]);

            assertEquals(-1, pbis.read());
            assertEquals(32, pbis.read());
            assertEquals(-1, pbis.read());
        }
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void detectEncodingUtf16BE() throws Exception {
        final byte[] bytes = new byte[] {(byte) 0xfe, (byte) 0xff, (byte) 0x20};

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                PlaybackInputStream pbis = new PlaybackInputStream(bais)) {

            final String[] encoding = new String[2];
            pbis.detectBomEncoding(encoding);

            assertEquals("UTF-16", encoding[0]);
            assertEquals("UnicodeBigUnmarked", encoding[1]);

            assertEquals(32, pbis.read());
            assertEquals(-1, pbis.read());
        }
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void detectEncodingUtf16BEPart() throws Exception {
        final byte[] bytes = new byte[] {(byte) 0xfe, (byte) 0x20};

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                PlaybackInputStream pbis = new PlaybackInputStream(bais)) {

            final String[] encoding = new String[2];
            pbis.detectBomEncoding(encoding);

            assertNull(encoding[0]);
            assertNull(encoding[1]);

            assertEquals(-2, pbis.read());
            assertEquals(32, pbis.read());
            assertEquals(-1, pbis.read());
        }
    }
}
