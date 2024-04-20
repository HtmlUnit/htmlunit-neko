package org.htmlunit.cyberneko.xerces.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class XUserDefinedInputStreamReaderTest {

    @Test
    void ascii() throws IOException {
        final byte[] ascii = "abX!".getBytes(StandardCharsets.US_ASCII);
        try (Reader reader = new XUserDefinedInputStreamReader(new ByteArrayInputStream(ascii))) {
            Assertions.assertEquals('a', reader.read());
            Assertions.assertEquals('b', reader.read());
            Assertions.assertEquals('X', reader.read());
            Assertions.assertEquals('!', reader.read());
        }
    }

    @Test
    void asciiBuffer() throws IOException {
        final byte[] ascii = "abX!".getBytes(StandardCharsets.US_ASCII);
        try (Reader reader = new XUserDefinedInputStreamReader(new ByteArrayInputStream(ascii))) {
            final char[] chars = new char[10];
            Assertions.assertEquals(4, reader.read(chars));
            Assertions.assertEquals('a', chars[0]);
            Assertions.assertEquals('b', chars[1]);
            Assertions.assertEquals('X', chars[2]);
            Assertions.assertEquals('!', chars[3]);
        }
    }

    @Test
    void asciiBufferOffset() throws IOException {
        final byte[] ascii = "abX!".getBytes(StandardCharsets.US_ASCII);
        try (Reader reader = new XUserDefinedInputStreamReader(new ByteArrayInputStream(ascii))) {
            final char[] chars = new char[10];
            Assertions.assertEquals(4, reader.read(chars, 2, 5));
            Assertions.assertEquals(0, chars[0]);
            Assertions.assertEquals(0, chars[1]);
            Assertions.assertEquals('a', chars[2]);
            Assertions.assertEquals('b', chars[3]);
            Assertions.assertEquals('X', chars[4]);
            Assertions.assertEquals('!', chars[5]);
            Assertions.assertEquals(0, chars[6]);
        }
    }


    @Test
    void asciiBufferOffsetBorder() throws IOException {
        final byte[] ascii = "abX!".getBytes(StandardCharsets.US_ASCII);
        try (Reader reader = new XUserDefinedInputStreamReader(new ByteArrayInputStream(ascii))) {
            final char[] chars = new char[7];
            Assertions.assertEquals(4, reader.read(chars, 2, 5));
            Assertions.assertEquals(0, chars[0]);
            Assertions.assertEquals(0, chars[1]);
            Assertions.assertEquals('a', chars[2]);
            Assertions.assertEquals('b', chars[3]);
            Assertions.assertEquals('X', chars[4]);
            Assertions.assertEquals('!', chars[5]);
            Assertions.assertEquals(0, chars[6]);
        }
    }

    @Test
    void nonPrintable() throws IOException {
        final byte[] bytes = new byte[]{0, 1, 2, 3};
        try (Reader reader = new XUserDefinedInputStreamReader(new ByteArrayInputStream(bytes))) {
            Assertions.assertEquals('\u0000', reader.read());
            Assertions.assertEquals('\u0001', reader.read());
            Assertions.assertEquals(2, reader.read());
            Assertions.assertEquals(3, reader.read());
        }
    }


    @Test
    void privateUseArea() throws IOException {
        final byte[] bytes = new byte[]{(byte) 0x80, (byte) 0x90, (byte) 0xF0, (byte) 0xFF};
        try (Reader reader = new XUserDefinedInputStreamReader(new ByteArrayInputStream(bytes))) {
            Assertions.assertEquals('\uF780', reader.read());
            Assertions.assertEquals('\uF790', reader.read());
            Assertions.assertEquals('\uF7F0', reader.read());
            Assertions.assertEquals('\uF7FF', reader.read());
        }
    }
}
