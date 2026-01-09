/*
 * Copyright (c) 2017-2026 Ronald Brill
 * Copyright 2023 René Schwietzke
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
package org.htmlunit.cyberneko.xerces.xni;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import java.util.Random;
import java.util.function.BiFunction;

import org.htmlunit.cyberneko.util.FastHashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for the new {@link XMLString} which is technically a
 * CharBuffer.
 *
 * @author Ronald Brill
 * @author Ren&eacute; Schwietzke
 */
public class XMLStringTest {
    public static final String CHAR5  = "ABCDE";
    public static final String CHAR10 = "ABCDEFGHIJ";
    public static final String CHAR15 = "ABCDEFGHIJKLMNO";
    public static final String CHAR20 = "ABCDEFGHIJKLMNOPQRST";
    public static final String CHAR25 = "ABCDEFGHIJKLMNOPQRSTUVWXY";

    @Test
    public void ctrEmpty() {
        final XMLString b = new XMLString();
        assertEquals(0, b.length());
        assertEquals(XMLString.INITIAL_CAPACITY, b.capacity());
        assertEquals("", b.toString());
    }

    @Test
    public void ctr_int() {
        // standard
        {
            final XMLString b = new XMLString(42);
            assertEquals(0, b.length());
            assertEquals(42, b.capacity());
            assertEquals(XMLString.CAPACITY_GROWTH, b.getGrowBy());
            assertEquals("", b.toString());
        }
        // 0
        {
            final XMLString b = new XMLString(0);
            assertEquals(0, b.length());
            assertEquals(0, b.capacity());
            assertEquals(XMLString.CAPACITY_GROWTH, b.getGrowBy());
            assertEquals("", b.toString());
        }
    }

    @Test
    public void ctr_int_int() {
        // standard
        {
            final XMLString b = new XMLString(42, 11);
            assertEquals(0, b.length());
            assertEquals(42, b.capacity());
            assertEquals(11, b.getGrowBy());
            assertEquals("", b.toString());
        }
        // zero is silently ignored
        {
            final XMLString b = new XMLString(42, 0);
            assertEquals(0, b.length());
            assertEquals(42, b.capacity());
            assertEquals(1, b.getGrowBy());
            assertEquals("", b.toString());
        }
        // 0 size is possible
        {
            final XMLString b = new XMLString(0, 3);
            assertEquals(0, b.length());
            assertEquals(0, b.capacity());
            assertEquals(3, b.getGrowBy());
            assertEquals("", b.toString());
        }
    }

    @Test
    public void ctr_Buffer() {
        // standard
        {
            final XMLString a = new XMLString(new XMLString(CHAR25));
            assertEquals(CHAR25, a.toString());
            assertEquals(25, a.length());
            assertEquals(25, a.capacity());
            assertEquals(XMLString.CAPACITY_GROWTH, a.getGrowBy());
        }
        // empty
        {
            final XMLString a = new XMLString(new XMLString());
            assertEquals("", a.toString());
            assertEquals(0, a.length());
            assertEquals(0, a.capacity());
            assertEquals(XMLString.CAPACITY_GROWTH, a.getGrowBy());
        }
    }

    @Test
    public void ctr_xmlcharuffer_int() {
        // standard
        {
            final XMLString a = new XMLString(new XMLString("foo"), 11);
            assertEquals("foo", a.toString());
            assertEquals(3, a.length());
            assertEquals(14, a.capacity());
            assertEquals(XMLString.CAPACITY_GROWTH, a.getGrowBy());
        }
        // empty
        {
            final XMLString a = new XMLString(new XMLString(), 5);
            assertEquals("", a.toString());
            assertEquals(0, a.length());
            assertEquals(5, a.capacity());
            assertEquals(XMLString.CAPACITY_GROWTH, a.getGrowBy());
        }
    }

    @Test
    public void ctr_string() {
        // empty
        {
            final XMLString a = new XMLString("");
            assertEquals("", a.toString());
            assertEquals(0, a.length());
            assertEquals(0, a.capacity());
            assertEquals(XMLString.CAPACITY_GROWTH, a.getGrowBy());
        }
        // standard
        {
            final XMLString a = new XMLString("1234");
            assertEquals("1234", a.toString());
            assertEquals(4, a.length());
            assertEquals(4, a.capacity());
            assertEquals(XMLString.CAPACITY_GROWTH, a.getGrowBy());
        }
    }

    @Test
    public void ctr_char_int_int() {
        // empty
        {
            final XMLString a = new XMLString("".toCharArray(), 0, 0);
            assertEquals("", a.toString());
            assertEquals(0, a.length());
            assertEquals(0, a.capacity());
            assertEquals(XMLString.CAPACITY_GROWTH, a.getGrowBy());
        }
        // standard
        {
            final XMLString a = new XMLString("foo".toCharArray(), 0, 3);
            assertEquals("foo", a.toString());
            assertEquals(3, a.length());
            assertEquals(3, a.capacity());
            assertEquals(XMLString.CAPACITY_GROWTH, a.getGrowBy());
        }
        // standard
        {
            final XMLString a = new XMLString("foobar2".toCharArray(), 3, 3);
            assertEquals("bar", a.toString());
            assertEquals(3, a.length());
            assertEquals(3, a.capacity());
            assertEquals(XMLString.CAPACITY_GROWTH, a.getGrowBy());
        }
    }

    /*
     * toString()
     */
    @Test
    public void toStringTest() {
        assertEquals("", new XMLString().toString());
        assertEquals("", new XMLString(10).toString());
        assertEquals("foo", new XMLString("foobar".toCharArray(), 0, 3).toString());
        assertEquals("foobar", new XMLString("foobar".toCharArray(), 0, 6).toString());

        final XMLString b1 = new XMLString();
        b1.append("foo");
        assertEquals("foo", b1.toString());
        b1.clear().append("bar");
        assertEquals("bar", b1.toString());
        b1.append("more than anything today in the world 0123456");
        assertEquals("barmore than anything today in the world 0123456", b1.toString());
    }

    /*
     * toString(FastHashMap)
     */
    @Test
    public void toStringCacheTest() {
        final FastHashMap<XMLString, String> cache = new FastHashMap<>(10, 0.5f);

        final XMLString x1 = new XMLString("abc");
        final XMLString x2 = new XMLString("abc");
        final XMLString x3 = new XMLString("12");

        final String s1 = x1.toString(cache);
        final String s2 = x2.toString(cache);
        final String s3 = x3.toString(cache);

        assertSame(s1, s2);
        assertNotSame(s2, s3);
    }

    /*
     * Just the base length checks, the rest comes with append and such
     * tests.
     */
    @Test
    public void length() {
        assertEquals(0, new XMLString().length());
        assertEquals(0, new XMLString(10).length());

        assertEquals(1, new XMLString().append('f').length());
    }

    @Test
    public void capacity() {
        assertEquals(20, new XMLString().capacity());
        assertEquals(20, new XMLString().append('f').capacity());

        // let's grow and see
        assertEquals(20, new XMLString().append("01234567890123456789").capacity());
    }

    @Test
    public void append_char_new() {
        final XMLString b = new XMLString(5);
        assertEquals(0, b.length());
        assertEquals(5, b.capacity());

        b.append('a');
        assertEquals(1, b.length());
        assertEquals(5, b.capacity());
        assertEquals("a", b.toString());

        b.append('b');
        assertEquals(2, b.length());
        assertEquals(5, b.capacity());

        b.append('c');
        assertEquals(3, b.length());
        assertEquals(5, b.capacity());

        b.append('d');
        assertEquals(4, b.length());
        assertEquals(5, b.capacity());

        b.append('e');
        assertEquals(5, b.length());
        assertEquals(5, b.capacity());

        assertEquals("abcde", b.toString());

        // our source of correctness
        final StringBuilder sb = new StringBuilder(b.toString());
        final Random r = new Random();

        for (int i = 0; i < 100; i++) {
            final char c = CHAR25.charAt(r.nextInt(CHAR25.length()));
            sb.append(c);
            b.append(c);

            assertEquals(sb.toString(), b.toString());
        }
    }

    @Test
    public void append_charbuffer_new() {
        final XMLString c0 = new XMLString();
        final XMLString c5 = new XMLString("01234");

        // empty
        {
            final XMLString a = new XMLString();
            a.append(c0);
            assertEquals("", a.toString());
            assertEquals(0, a.length());
        }
        // self empty
        {
            final XMLString a = new XMLString();
            a.append(a);
            assertEquals("", a.toString());
            assertEquals(0, a.length());
        }
        // standard
        {
            final XMLString a = new XMLString();
            a.append(c5);
            assertEquals(c5.toString(), a.toString());
            assertEquals(5, a.length());
        }
        // self standard
        {
            final XMLString a = new XMLString("ABCDE");
            a.append(a);
            assertEquals("ABCDEABCDE", a.toString());
            assertEquals(10, a.length());
        }

    }

    @Test
    public void append_charbuffer_noResize() {
        final XMLString c0 = new XMLString();
        final XMLString c5 = new XMLString("01234");

        {
            // we start sized for the data we got
            final XMLString a = new XMLString();
            assertEquals(XMLString.INITIAL_CAPACITY, a.capacity());
            a.append("ABC");
            assertEquals("ABC", a.toString());

            // no data, no change
            a.append(c0);
            assertEquals(XMLString.INITIAL_CAPACITY, a.capacity());
            assertEquals("ABC", a.toString());

            // we add 5 chars, should grow once for the current
            // growth
            a.append(c5);
            assertEquals(XMLString.INITIAL_CAPACITY, a.capacity());
            assertEquals("ABC01234", a.toString());
            assertEquals(8, a.length());
        }
    }

    @Test
    public void append_charbuffer_resize() {
        final XMLString c5 = new XMLString("a-b-c");
        final XMLString c25 = new XMLString(CHAR25);

        {
            final XMLString a = new XMLString();
            a.append(c5);
            assertEquals(XMLString.INITIAL_CAPACITY, a.capacity());
            assertEquals("a-b-c", a.toString());
            assertEquals(5, a.length());
            assertEquals(20, a.capacity());
            a.append(c25);
            assertEquals("a-b-c" + CHAR25, a.toString());
            //            assertEquals(52, a.capacity());
            assertEquals(30, a.length());
        }
    }

    @Test
    public void append2_noResize() {
        final XMLString c1 = new XMLString();
        c1.append('0', '2');
        assertEquals(2, c1.length());
        assertEquals("02", c1.toString());

        c1.append('4', 'A');
        assertEquals(4, c1.length());
        assertEquals("024A", c1.toString());
    }

    @Test
    public void append2_resize() {
        final XMLString c1 = new XMLString();
        final XMLString c2 = new XMLString().append('0');

        final StringBuilder a1 = new StringBuilder();
        final StringBuilder a2 = new StringBuilder("0");

        {
            for (int i = 0; i < 64; i++) {
                c1.append('0', '1');
                c2.append('1', '0');
                a1.append('0').append('1');
                a2.append('1').append('0');
            }
            assertEquals(a1.toString(), c1.toString());
            assertEquals(a2.toString(), c2.toString());
        }
    }

    @Test
    public void append_string() {
        // new, no resize
        {
            final XMLString a = new XMLString();
            a.append(CHAR5);
            assertEquals(XMLString.INITIAL_CAPACITY, a.capacity());
            assertEquals(CHAR5, a.toString());
            assertEquals(5, a.length());
        }
        // new, resize at once
        {
            final XMLString a = new XMLString();
            assertEquals(XMLString.INITIAL_CAPACITY, a.capacity());
            a.append(CHAR5 + CHAR25);
            //            assertEquals(52, a.capacity());
            assertEquals(CHAR5 + CHAR25, a.toString());
            assertEquals(30, a.length());
        }
        // resize later
        {
            final XMLString a = new XMLString();
            assertEquals(XMLString.INITIAL_CAPACITY, a.capacity());
            a.append(CHAR5);
            a.append(CHAR25);
            //            assertEquals(52, a.capacity());
            assertEquals(CHAR5 + CHAR25, a.toString());
            assertEquals(30, a.length());
        }
        // empty string
        {
            final XMLString a = new XMLString();
            assertEquals(XMLString.INITIAL_CAPACITY, a.capacity());
            a.append("");
            a.append("");
            a.append(CHAR25);
            a.append("");
            //            assertEquals(52, a.capacity());
            assertEquals(CHAR25, a.toString());
            assertEquals(25, a.length());
        }
    }

    @Test
    public void append_char_int_int_new() {
        // empty
        {
            final XMLString a = new XMLString();
            a.append("ABC-DEF-GHU".toCharArray(), 0, 0);
            assertEquals("", a.toString());
            assertEquals(0, a.length());
        }
        // standard
        {
            final XMLString a = new XMLString();
            a.append("ABC-DEF-GHU".toCharArray(), 0, 11);
            assertEquals("ABC-DEF-GHU", a.toString());
            assertEquals(11, a.length());
        }
        // standard
        {
            final XMLString a = new XMLString();
            a.append("ABC-DEF-GHU".toCharArray(), 1, 10);
            assertEquals("BC-DEF-GHU", a.toString());
            assertEquals(10, a.length());
        }
        // requires growth
        {
            final XMLString a = new XMLString();
            assertEquals(XMLString.INITIAL_CAPACITY, a.capacity());
            a.append(CHAR25.toCharArray(), 0, 25);
            //            assertEquals(52, a.capacity());

            a.append(CHAR25.toCharArray(), 0, 25);
            //            assertEquals(52, a.capacity()); // still fits
            assertEquals(CHAR25 + CHAR25, a.toString());
            assertEquals(50, a.length());

            a.append(CHAR25.toCharArray(), 0, 5);
            //            assertEquals(84, a.capacity()); // still fits
            assertEquals(CHAR25 + CHAR25 + CHAR5, a.toString());
            assertEquals(55, a.length());
        }
        // append several times
        {
            final XMLString a = new XMLString();
            a.append("abc".toCharArray(), 0, 3);
            a.append("1234".toCharArray(), 0, 4);
            a.append("".toCharArray(), 0, 0);
            a.append("0987654321".toCharArray(), 0, 10);
            a.append("FOO--OO".toCharArray(), 3, 2);
            assertEquals("abc12340987654321--", a.toString());
            assertEquals(10 + 0 + 4 + 3 + 2, a.length());
        }
    }

    @Test
    public void charAt() {
        {
            final XMLString a = new XMLString("a");
            assertEquals('a', a.charAt(0));
        }
        {
            final XMLString a = new XMLString("abc");
            assertEquals('a', a.charAt(0));
            assertEquals('b', a.charAt(1));
            assertEquals('c', a.charAt(2));
        }
    }

    @Test
    public void charAt_Errors() {
        // empty
        final XMLString a1 = new XMLString();
        assertThrows(IndexOutOfBoundsException.class, () -> a1.charAt(0));

        final XMLString a2 = new XMLString("");
        assertThrows(IndexOutOfBoundsException.class, () -> a2.charAt(0));

        final XMLString b = new XMLString("foo");
        assertThrows(IndexOutOfBoundsException.class, () -> b.charAt(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> b.charAt(3));
        assertThrows(IndexOutOfBoundsException.class, () -> b.charAt(4));
    }

    @Test
    public void unsafeCharAt() {
        {
            final XMLString a = new XMLString("a");
            assertEquals('a', a.unsafeCharAt(0));
        }
        {
            final XMLString a = new XMLString("abc");
            assertEquals('a', a.unsafeCharAt(0));
            assertEquals('b', a.unsafeCharAt(1));
            assertEquals('c', a.unsafeCharAt(2));
        }
    }

    @Test
    public void unsafeCharAt_Errors() {
        // empty
        final XMLString a1 = new XMLString();
        // can read any garbage
        a1.unsafeCharAt(0);

        // cannot read outside the array of course
        assertThrows(IndexOutOfBoundsException.class, () -> a1.unsafeCharAt(XMLString.INITIAL_CAPACITY));
    }

    @Test
    public void endsWidth() {
        // no size match, string to large
        assertFalse(new XMLString("foo").endsWith("foobar"));

        // empty string
        assertTrue(new XMLString("foobar").endsWith(""));

        // both empty
        assertTrue(new XMLString("").endsWith(""));

        // both are the same size and don't match
        assertFalse(new XMLString("foo").endsWith("bar"));

        // both are the same size and match
        assertTrue(new XMLString("foo").endsWith("foo"));

        // first char mismatch
        assertFalse(new XMLString("foobar").endsWith("car"));

        // last char mismatch
        assertFalse(new XMLString("foobar").endsWith("baa"));

        // full match
        assertTrue(new XMLString("foobar").endsWith("bar"));
    }

    @Test
    public void clear() {
        // empty
        {
            final XMLString a = new XMLString();
            a.clear();
            assertEquals(XMLString.INITIAL_CAPACITY, a.capacity());
            assertEquals("", a.toString());
            assertEquals(0, a.length());
        }
        // some stuff in it
        {
            final XMLString a = new XMLString();
            a.append(CHAR5);
            a.clear();
            assertEquals(XMLString.INITIAL_CAPACITY, a.capacity());
            assertEquals("", a.toString());
            assertEquals(0, a.length());
        }
        // repeated clear
        {
            final XMLString a = new XMLString();
            a.append(CHAR5);
            a.clear();
            a.clear();
            assertEquals(XMLString.INITIAL_CAPACITY, a.capacity());
            assertEquals("", a.toString());
            assertEquals(0, a.length());
        }
        // clear append
        {
            final XMLString a = new XMLString();
            a.append(CHAR5);
            a.clear();
            a.append(CHAR5);
            assertEquals(XMLString.INITIAL_CAPACITY, a.capacity());
            assertEquals(CHAR5, a.toString());
            assertEquals(5, a.length());
        }
    }

    @Test
    public void clearAndAppend() {
        // empty
        {
            final XMLString a = new XMLString();
            a.clearAndAppend('a');
            assertEquals(XMLString.INITIAL_CAPACITY, a.capacity());
            assertEquals("a", a.toString());
            assertEquals(1, a.length());
        }
        // some stuff in it
        {
            final XMLString a = new XMLString();
            a.append(CHAR5);
            a.clearAndAppend('a');
            assertEquals(XMLString.INITIAL_CAPACITY, a.capacity());
            assertEquals("a", a.toString());
            assertEquals(1, a.length());
        }
        // repeated clear
        {
            final XMLString a = new XMLString();
            a.append(CHAR5);
            a.clearAndAppend('a');
            a.clearAndAppend('a');
            assertEquals(XMLString.INITIAL_CAPACITY, a.capacity());
            assertEquals("a", a.toString());
            assertEquals(1, a.length());
        }
        // clear append
        {
            final XMLString a = new XMLString();
            a.append(CHAR5);
            a.clearAndAppend('a');
            a.append(CHAR5);
            assertEquals(XMLString.INITIAL_CAPACITY, a.capacity());
            assertEquals("a" + CHAR5, a.toString());
            assertEquals(6, a.length());
        }
        // clear append on total empty
        {
            final XMLString a = new XMLString();
            a.clearAndAppend('a');
            assertEquals(XMLString.INITIAL_CAPACITY, a.capacity());
            assertEquals("a", a.toString());
            assertEquals(1, a.length());
        }
    }

    @Test
    public void prepend() {
        final XMLString x = new XMLString();
        x.prepend('x');
        assertEquals("x", x.toString());

        final XMLString a = new XMLString("foo");
        a.prepend('=');
        assertEquals("=foo", a.toString());

        // just push something into the string to see if we have growth pain
        final XMLString b = new XMLString("foo");
        final StringBuilder exB = new StringBuilder("foo");
        final String src = "This is a test of the prepending";
        for (int i = 0; i < src.length(); i++) {
            final char c = src.charAt(i);
            exB.insert(0, c);
            b.prepend(c);
        }
        assertEquals(exB.toString(), b.toString());
    }

    @Test
    public void reduceToContent() {
        final XMLString x = new XMLString();

        // buffer shorter than markers
        x.clear().append("foo");
        x.trimToContent("<!--", "-->");
        assertEquals(3, x.length());
        assertEquals("foo", x.toString());

        // only start
        x.clear().append("Sfoo");
        x.trimToContent("S", "-->");
        assertEquals(4, x.length());
        assertEquals("Sfoo", x.toString());

        // only end
        x.clear().append("fooE");
        x.trimToContent("S", "E");
        assertEquals(4, x.length());
        assertEquals("fooE", x.toString());

        // start and end, no whitespace
        x.clear().append("SfooE");
        x.trimToContent("S", "E");
        assertEquals(3, x.length());
        assertEquals("foo", x.toString());

        // start and end, only start with whitespace
        x.clear().append(" SfooE");
        x.trimToContent("S", "E");
        assertEquals(3, x.length());
        assertEquals("foo", x.toString());

        // start and end, only end with whitespace
        x.clear().append("SfooE \n");
        x.trimToContent("S", "E");
        assertEquals(3, x.length());
        assertEquals("foo", x.toString());

        // start and end, both with whitespace
        x.clear().append("     SfooE \n");
        x.trimToContent("S", "E");
        assertEquals(3, x.length());
        assertEquals("foo", x.toString());

        // start and end, only one whitespace each
        x.clear().append(" SfooE ");
        x.trimToContent("S", "E");
        assertEquals(3, x.length());
        assertEquals("foo", x.toString());

        // start and end, both with whitespace
        // no content but markers
        x.clear().append("     SE \n");
        x.trimToContent("S", "E");
        assertEquals(0, x.length());
        assertEquals("", x.toString());

        x.clear().append(" 1234 ");
        x.trimToContent("12", "34");
        assertEquals(0, x.length());
        assertEquals("", x.toString());

        // start and end, both with whitespace
        // one char content
        x.clear().append(" 1234 ");
        x.trimToContent("12", "4");
        assertEquals(1, x.length());
        assertEquals("3", x.toString());

        // start and end, both with whitespace
        // content, just whitespace
        x.clear().append(" 12 4 ");
        x.trimToContent("12", "4");
        assertEquals(1, x.length());
        assertEquals(" ", x.toString());

        // start and end, both with whitespace
        // content, just chars

        // start and end, both with whitespace
        // content, content with whitespaces

        // one char start and end

        // two chars start and end

        // empty end marker

        // empty start marker

        // empty start and end, behaves like trim, with whitespace

        // empty start and end, behaves like trim, without

        // some non ASCII stuff

        // markers are doubled

        // gives us some fancy whitespaces

        // original tests from XmlString
        x.clear().append("<!-- hello-->");
        x.trimToContent("<!--", "-->");
        assertEquals(" hello", x.toString());

        x.clear().append("  \n <!-- hello-->\n");
        x.trimToContent("<!--", "-->");
        assertEquals(" hello", x.toString());

        x.clear().append("hello");
        x.trimToContent("<!--", "-->");
        assertEquals("hello", x.toString());

        x.clear().append("<!-- hello");
        x.trimToContent("<!--", "-->");
        assertEquals("<!-- hello", x.toString());

        x.clear().append("<!--->");
        x.trimToContent("<!--", "-->");
        assertEquals("<!--->", x.toString());

        x.clear().append(" <!--->\n");
        x.trimToContent("<!--", "-->");
        assertEquals(" <!--->\n", x.toString());
    }

    @Test
    public void trimLeading() {
        final XMLString xmlString = new XMLString();

        xmlString.clear().append("");
        xmlString.trimLeading();
        assertEquals("", xmlString.toString());

        xmlString.clear().append("a");
        xmlString.trimLeading();
        assertEquals("a", xmlString.toString());

        xmlString.clear().append("a ");
        xmlString.trimLeading();
        assertEquals("a ", xmlString.toString());

        xmlString.clear().append("a b");
        xmlString.trimLeading();
        assertEquals("a b", xmlString.toString());

        xmlString.clear().append(" a");
        xmlString.trimLeading();
        assertEquals("a", xmlString.toString());

        xmlString.clear().append(" abckasd");
        xmlString.trimLeading();
        assertEquals("abckasd", xmlString.toString());

        xmlString.clear().append("      a");
        xmlString.trimLeading();
        assertEquals("a", xmlString.toString());

        xmlString.clear().append(" ");
        xmlString.trimLeading();
        assertEquals("", xmlString.toString());

        xmlString.clear().append("  ");
        xmlString.trimLeading();
        assertEquals("", xmlString.toString());
    }

    @Test
    public void trimTrailing() {
        final XMLString xmlString = new XMLString();

        xmlString.clear().append("");
        xmlString.trimTrailing();
        assertEquals("", xmlString.toString());

        xmlString.clear().append("a");
        xmlString.trimTrailing();
        assertEquals("a", xmlString.toString());

        xmlString.clear().append(" a");
        xmlString.trimTrailing();
        assertEquals(" a", xmlString.toString());

        xmlString.clear().append("a b");
        xmlString.trimTrailing();
        assertEquals("a b", xmlString.toString());

        xmlString.clear().append("a ");
        xmlString.trimTrailing();
        assertEquals("a", xmlString.toString());

        xmlString.clear().append("a  ");
        xmlString.trimTrailing();
        assertEquals("a", xmlString.toString());

        xmlString.clear().append(" ");
        xmlString.trimTrailing();
        assertEquals("", xmlString.toString());

        xmlString.clear().append("  ");
        xmlString.trimTrailing();
        assertEquals("", xmlString.toString());
    }

    @Test
    public void trim() {
        final XMLString xmlString = new XMLString();

        xmlString.clear().append("");
        xmlString.trim();
        assertEquals("", xmlString.toString());

        xmlString.clear().append("a");
        xmlString.trim();
        assertEquals("a", xmlString.toString());

        xmlString.clear().append(" a");
        xmlString.trim();
        assertEquals("a", xmlString.toString());

        xmlString.clear().append("a b");
        xmlString.trim();
        assertEquals("a b", xmlString.toString());

        xmlString.clear().append("a ");
        xmlString.trim();
        assertEquals("a", xmlString.toString());

        xmlString.clear().append("a  ");
        xmlString.trim();
        assertEquals("a", xmlString.toString());

        xmlString.clear().append(" a  ");
        xmlString.trim();
        assertEquals("a", xmlString.toString());

        xmlString.clear().append(" ");
        xmlString.trim();
        assertEquals("", xmlString.toString());

        xmlString.clear().append("  ");
        xmlString.trim();
        assertEquals("", xmlString.toString());
    }

    @Test
    public void shortenBy() {
        // shorten empty
        {
            final XMLString a = new XMLString("");
            a.shortenBy(12);
            assertEquals(0, a.length());
            assertEquals("", a.toString());
        }
        // shorten by 0
        {
            final XMLString a = new XMLString("ab");
            a.shortenBy(0);
            assertEquals(2, a.length());
            assertEquals("ab", a.toString());
        }
        // shorten by larger
        {
            final XMLString a = new XMLString("ab");
            a.shortenBy(4);
            assertEquals(0, a.length());
            assertEquals("", a.toString());
        }
        // shorten standard
        {
            final XMLString a = new XMLString("abcd");
            a.shortenBy(1);
            assertEquals(3, a.length());
            assertEquals("abc", a.toString());
        }
        // shorten standard
        {
            final XMLString a = new XMLString("abcd123");
            a.shortenBy(3);
            assertEquals(4, a.length());
            assertEquals("abcd", a.toString());
        }
    }

    @Test
    public void cloneTest() {
        {
            final XMLString o = new XMLString("");
            final XMLString a = o.clone();
            assertEquals(0, a.length());
            assertEquals("", a.toString());
            assertNotSame(o, a);
        }
        {
            final XMLString o = new XMLString("abc");
            final XMLString a = o.clone();
            assertEquals(3, a.length());
            assertEquals("abc", a.toString());
            assertNotSame(o, a);
        }
        {
            final XMLString o = new XMLString(" 817 ");
            final XMLString a = o.clone();
            final XMLString b = a.clone();
            final XMLString c = b.clone();
            assertEquals(5, c.length());
            assertEquals(" 817 ", c.toString());
            assertNotSame(o, c);
        }
    }

    @Test
    public void subSequence() {
        {
            final CharSequence o = new XMLString("").subSequence(0, 0);
            assertEquals(0, o.length());
            assertEquals("", o.toString());
        }
        {
            final CharSequence o = new XMLString("a").subSequence(0, 1);
            assertEquals(1, o.length());
            assertEquals("a", o.toString());
        }
        {
            final CharSequence o = new XMLString("abc").subSequence(0, 3);
            assertEquals(3, o.length());
            assertEquals("abc", o.toString());
        }
        {
            final CharSequence o = new XMLString("abc").subSequence(1, 3);
            assertEquals(2, o.length());
            assertEquals("bc", o.toString());
        }
        {
            final CharSequence o = new XMLString("abc").subSequence(0, 2);
            assertEquals(2, o.length());
            assertEquals("ab", o.toString());
        }
        {
            final CharSequence o = new XMLString("abc").subSequence(1, 2);
            assertEquals(1, o.length());
            assertEquals("b", o.toString());
        }
    }

    @Test
    public void subSequenceErrors() {
        {
            final CharSequence o = new XMLString("");
            assertThrows(IndexOutOfBoundsException.class, () -> o.subSequence(-1, 2));
            assertThrows(IndexOutOfBoundsException.class, () -> o.subSequence(0, 1));
        }
        {
            final CharSequence o = new XMLString("12345");
            assertThrows(IndexOutOfBoundsException.class, () -> o.subSequence(-2, 3));
            assertThrows(IndexOutOfBoundsException.class, () -> o.subSequence(0, 6));
            assertThrows(IndexOutOfBoundsException.class, () -> o.subSequence(1, 0));
        }
    }

    @Test
    public void equalsTest() {
        // different types
        {
            final Integer a = Integer.valueOf(192);
            final XMLString b = new XMLString();
            assertNotEquals(a, b);
            assertNotEquals(b, a);
        }
        // both empty
        {
            final XMLString a = new XMLString();
            final XMLString b = new XMLString();
            assertEquals(a, b);
            assertEquals(b, a);
        }
        // self
        {
            final XMLString a = new XMLString();
            final XMLString b = new XMLString("abc");
            assertEquals(a, a);
            assertEquals(b, b);
        }
        // different length
        {
            final XMLString a = new XMLString("a");
            final XMLString b = new XMLString("ab");
            assertNotEquals(a, b);
            assertNotEquals(b, a);
        }
        // different content, same length
        {
            final XMLString a = new XMLString("ac");
            final XMLString b = new XMLString("ab");
            assertNotEquals(a, b);
            assertNotEquals(b, a);
        }
        // length 1, identical
        {
            final XMLString a = new XMLString("a");
            final XMLString b = new XMLString("a");
            assertEquals(a, b);
            assertEquals(b, a);
        }
        // length 1, different
        {
            final XMLString a = new XMLString("b");
            final XMLString b = new XMLString("b");
            assertEquals(a, b);
            assertEquals(b, a);
        }
        // length > 1, same
        {
            final XMLString a = new XMLString("81hal/&%$");
            final XMLString b = new XMLString("81hal/&%$");
            assertEquals(a, b);
            assertEquals(b, a);
        }
        // length > 1, different
        {
            final XMLString a = new XMLString("b091872983");
            final XMLString b = new XMLString("b09187298_");
            assertNotEquals(a, b);
            assertNotEquals(b, a);
        }

        // CharSequence works too
        {
            final String a = new String("b091872983");
            final XMLString b = new XMLString("b091872983");
            assertNotEquals(a, b); // a string is not so open :)
            assertEquals(b, a);
        }
        // CharSequence works too
        {
            final String a = new String("983");
            final XMLString b = new XMLString("b09187298_");
            assertNotEquals(a, b);
            assertNotEquals(b, a);
        }
    }

    @Test
    public void isWhitespace() {
        {
            final XMLString a = new XMLString("");
            assertTrue(a.isWhitespace());
            assertTrue(a.isWhitespace());
        }
        {
            final XMLString a = new XMLString(" ");
            assertTrue(a.isWhitespace());
            assertTrue(a.isWhitespace());
        }
        {
            final XMLString a = new XMLString("a");
            assertFalse(a.isWhitespace());
            assertFalse(a.isWhitespace());
        }
        {
            final XMLString a = new XMLString("a ");
            assertFalse(a.isWhitespace());
            assertFalse(a.isWhitespace());
        }
        {
            final XMLString a = new XMLString("a  ");
            assertFalse(a.isWhitespace());
            assertFalse(a.isWhitespace());
        }
        {
            final XMLString a = new XMLString(" a");
            assertFalse(a.isWhitespace());
            assertFalse(a.isWhitespace());
        }
        {
            final XMLString a = new XMLString("  a");
            assertFalse(a.isWhitespace());
            assertFalse(a.isWhitespace());
        }
        {
            final XMLString a = new XMLString("a     \n");
            assertFalse(a.isWhitespace());
            assertFalse(a.isWhitespace());
        }
        {
            final XMLString a = new XMLString("     \na");
            assertFalse(a.isWhitespace());
            assertFalse(a.isWhitespace());
        }
        {
            final XMLString a = new XMLString("      \n");
            assertTrue(a.isWhitespace());
            assertTrue(a.isWhitespace());
        }
    }

    @Test
    public void getChars() {
        // empty
        {
            final XMLString a = new XMLString("");
            assertArrayEquals(new char[0], a.getChars());
        }
        // normal
        {
            final XMLString a = new XMLString("abc");
            assertArrayEquals("abc".toCharArray(), a.getChars());
        }
        // grown
        {
            final XMLString a = new XMLString("abc");
            a.append(CHAR25);
            assertArrayEquals(("abc" + CHAR25).toCharArray(), a.getChars());
        }
    }

    @Test
    public void hashCodeTest() {
        // empty
        {
            final XMLString a = new XMLString();
            assertEquals("".hashCode(), a.hashCode());
        }
        // mutate
        {
            final XMLString a = new XMLString();
            assertEquals("".hashCode(), a.hashCode());
            a.append('a');
            assertEquals("a".hashCode(), a.hashCode());
            a.append("bc");
            assertEquals("abc".hashCode(), a.hashCode());
        }
    }

    @Test
    public void appendCodePoint() {
        // regular code point
        {
            final XMLString a = new XMLString();
            assertTrue(a.appendCodePoint('a'));
            assertEquals(1, a.length());
            assertEquals("a", a.toString());
        }
        // two char code point
        {
            final XMLString a = new XMLString();
            assertTrue(a.appendCodePoint(0x30CA));
            assertEquals(1, a.length());
            assertEquals("ナ", a.toString());
        }
        // invalid code point
        {
            final XMLString a = new XMLString();
            assertFalse(a.appendCodePoint(0x10FFFF + 42));
        }
    }

    /**
     * @see XMLString#equalsIgnoreCase(CharSequence)
     */
    @Test
    public void equalsIgnoreCase_Self() {
        final XMLString a = new XMLString();
        assertTrue(a.equalsIgnoreCase(a));

        final XMLString b = new XMLString("text");
        assertTrue(b.equalsIgnoreCase(b));

        final XMLString c = new XMLString("tEXt");
        assertTrue(c.equalsIgnoreCase(c));
    }

    @Test
    public void equalsIgnoreCase_Null() {
        final XMLString a = new XMLString();
        assertFalse(a.equalsIgnoreCase(null));

        final XMLString b = new XMLString();
        assertFalse(b.equalsIgnoreCase(null));
    }

    @Test
    public void equalsIgnoreCase_Empty() {
        final XMLString a = new XMLString("");
        final XMLString b = new XMLString();
        assertTrue(a.equalsIgnoreCase(b));
        assertTrue(b.equalsIgnoreCase(a));
        assertTrue(a.equalsIgnoreCase(""));
        assertTrue(b.equalsIgnoreCase(""));
    }

    @Test
    public void equalsIgnoreCase_LengthDifference() {
        final XMLString a = new XMLString("ab");
        final XMLString b = new XMLString("abc");
        assertFalse(a.equalsIgnoreCase(b));
        assertFalse(b.equalsIgnoreCase(a));
    }

    @Test
    public void equalsIgnoreCase_Same() {
        final XMLString a = new XMLString("a");
        final XMLString aUC = new XMLString("A");
        assertTrue(a.equalsIgnoreCase(aUC));
        assertTrue(aUC.equalsIgnoreCase("a"));
        assertTrue(a.equalsIgnoreCase("A"));
        assertTrue(a.equalsIgnoreCase("a"));
        assertTrue(aUC.equalsIgnoreCase("A"));

        final XMLString a1 = new XMLString("iasDJ7(/&%$§%&/()1726781aaA");
        final XMLString a2 = new XMLString("iasDj7(/&%$§%&/()1726781aaa");
        assertTrue(a1.equalsIgnoreCase(a2));
        assertTrue(a2.equalsIgnoreCase(a1));
        assertTrue(a1.equalsIgnoreCase("iasDJ7(/&%$§%&/()1726781aaA"));
        assertTrue(a2.equalsIgnoreCase("iasDj7(/&%$§%&/()1726781aaa"));

        final XMLString a3 = new XMLString("a92 3   u2wquherqhw herjqhw Ö");
        final XMLString b3 = new XMLString("a92 3   u2wquherqhw herjqhw ö");
        assertTrue(a3.equalsIgnoreCase(b3));
        assertTrue(b3.equalsIgnoreCase(a3));
        assertTrue(a3.equalsIgnoreCase("a92 3   u2wquherqhw herjqhw ö"));
        assertTrue(b3.equalsIgnoreCase("a92 3   u2wquherqhw herjqhw Ö"));
    }

    @Test
    public void equalsIgnoreCase_Difference() {
        final XMLString a = new XMLString("a");
        final XMLString b = new XMLString("b");
        assertFalse(a.equalsIgnoreCase(b));
        assertFalse(a.equalsIgnoreCase("b"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "éäöü€", "Ganyu (贛語 / 赣语)", "duże i małe litery", "большой и нижний регистр" })
    public void equalsIgnoreCase_MoreChallengingLocales(final String s) {
        final String upper = s.toUpperCase(Locale.ROOT);
        final String lower = s.toLowerCase(Locale.ROOT);

        final XMLString a = new XMLString(s);
        final XMLString l = new XMLString(upper);
        final XMLString u = new XMLString(lower);

        assertTrue(a.equalsIgnoreCase(l));
        assertTrue(a.equalsIgnoreCase(u));
        assertTrue(l.equalsIgnoreCase(a));
        assertTrue(l.equalsIgnoreCase(u));
        assertTrue(u.equalsIgnoreCase(a));
        assertTrue(u.equalsIgnoreCase(l));
        assertTrue(a.equalsIgnoreCase(upper));
        assertTrue(a.equalsIgnoreCase(lower));
    }

    @Test
    public void equalsIgnoreCase_NoMatch() {
        // even the JDK says no to that! ß becomes double-S
        final XMLString a = new XMLString("Straße");
        assertFalse(a.equalsIgnoreCase("STRASSE"));
        assertNotEquals("Straße", "Straße".toUpperCase());
    }

    /**
     * @see XMLString#toLowerCase(Locale)
     */
    @ParameterizedTest
    @ValueSource(strings = {
        // "𘀀", // UTF-16
        "A", "a", ""
        // "HtmlUnit", "éäöü€",
        // "Ganyu (贛語 / 赣语)", "duże i małe litery", "большой и нижний регистр", // some unicode
        // "Foo\uD840" // supplemental char aka UTF-16
    })
    public void toLowerCase(final String s) {
        final XMLString a = new XMLString(s);
        assertEquals(s.toLowerCase(Locale.ROOT), a.toLowerCase(Locale.ROOT).toString());
    }

    /**
     * @see XMLString#toUpperCase(Locale)
     */
    @ParameterizedTest
    @ValueSource(strings = {
        "𘀀", // UTF-16
        "", "A", "a",
        "HtmlUnit", "éäöü€",
        "Ganyu (贛語 / 赣语)", "duże i małe litery", "большой и нижний регистр", // some unicode
        "Foo\uD840" // supplemental char aka UTF-16
    })
    public void toUpperCase(final String s) {
        final XMLString a = new XMLString(s);
        assertEquals(s.toUpperCase(Locale.ROOT), a.toUpperCase(Locale.ROOT).toString());
    }

    @Test
    public void indexOf_char() {
        assertEquals(-1, new XMLString().indexOf('c'));
        assertEquals(-1, new XMLString("").indexOf('c'));
        assertEquals(0, new XMLString("a").indexOf('a'));
        assertEquals(0, new XMLString("aa").indexOf('a'));
        assertEquals(1, new XMLString("abc").indexOf('b'));
        assertEquals(2, new XMLString("abc").indexOf('c'));
    }

    @Test
    public void indexOf_XMLString() {
        final BiFunction<String, String, Integer> t = (s1, s2)
                -> new XMLString(s1).indexOf(s2 != null ? new XMLString(s2) : null);

        assertEquals(0, t.apply("", ""));
        assertEquals(-1, t.apply("", null));
        assertEquals(-1, t.apply("", "a"));
        assertEquals(0, t.apply("a", ""));
        assertEquals(-1, t.apply("abc", "d"));
        assertEquals(-1, t.apply("abc", "abcd"));
        assertEquals(0, t.apply("abc", "abc"));
        assertEquals(-1, t.apply("abc", "abb"));
        assertEquals(0, t.apply("abc", "a"));
        assertEquals(1, t.apply("abc", "b"));
        assertEquals(2, t.apply("abc", "c"));
        assertEquals(1, t.apply("abc", "bc"));
        assertEquals(0, t.apply("abcabc", "abc"));
    }

    @Test
    public void contains_XMLString() {
        final BiFunction<String, String, Boolean> t = (s1, s2)
                -> new XMLString(s1).contains(s2 != null ? new XMLString(s2) : null);

        assertEquals(true, t.apply("", ""));
        assertEquals(false, t.apply("", null));
        assertEquals(false, t.apply("", "a"));
        assertEquals(true, t.apply("a", ""));
        assertEquals(false, t.apply("abc", "d"));
        assertEquals(false, t.apply("abc", "abcd"));
        assertEquals(true, t.apply("abc", "abc"));
        assertEquals(true, t.apply("abc", "a"));
        assertEquals(true, t.apply("abc", "b"));
        assertEquals(true, t.apply("abc", "c"));
        assertEquals(true, t.apply("abc", "bc"));
        assertEquals(true, t.apply("abcabc", "abc"));
    }
}
