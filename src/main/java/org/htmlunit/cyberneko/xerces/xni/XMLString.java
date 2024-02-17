/*
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
package org.htmlunit.cyberneko.xerces.xni;

import java.util.Arrays;
import java.util.Locale;

import org.htmlunit.cyberneko.util.FastHashMap;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * <p>This class is meant to replaces the old {@link XMLString} in all areas
 * where performance and memory-efficency is key. XMLString compatibility
 * remains in place in case one has used that in their own code.
 *
 * <p>This buffer is mutable and when you use it, make sure you work with
 * it responsibly. In many cases, we will reuse the buffer to avoid fresh
 * memory allocations, hence you have to pay attention to its usage pattern.
 * It is not meant to be a general String replacement.
 *
 * <p>This class avoids many of the standard runtime checks that will result
 * in a runtime or array exception anyway. Why check twice and raise the
 * same exception?
 *
 * @author René Schwietzke
 * @since 3.10.0
 */
public class XMLString implements CharSequence {
    // our data, can grow
    private char[] data_;

    // the current size of the string data
    private int length_;

    // the current size of the string data
    private final int growBy_;

    // how much do we grow if needed, half a cache line
    public static final int CAPACITY_GROWTH = 64 / 2;

    // what is our start size?
    // a cache line is 64 byte mostly, the overhead is mostly 24 bytes
    // a char is two bytes, let's use one cache lines
    public static final int INITIAL_CAPACITY = (64 - 24) / 2;

    // static empty version; DON'T MODIFY IT
    public static final XMLString EMPTY = new XMLString(0);

    // the � character
    private static final char REPLACEMENT_CHARACTER = '\uFFFD';

    /**
     * Constructs an XMLCharBuffer with a default size.
     */
    public XMLString() {
        data_ = new char[INITIAL_CAPACITY];
        length_ = 0;
        growBy_ = CAPACITY_GROWTH;
    }

    /**
     * Constructs an XMLCharBuffer with a desired size.
     *
     * @param startSize the size of the buffer to start with
     */
    public XMLString(final int startSize) {
        this(startSize, CAPACITY_GROWTH);
    }

    /**
     * Constructs an XMLCharBuffer with a desired size.
     *
     * @param startSize the size of the buffer to start with
     * @param growBy by how much do we want to grow when needed
     */
    public XMLString(final int startSize, final int growBy) {
        data_ = new char[startSize];
        length_ = 0;
        growBy_ = Math.max(1, growBy);
    }

    /**
     * Constructs an XMLCharBuffer from another buffer. Copies the data
     * over. The new buffer capacity matches the length of the source.
     *
     * @param src the source buffer to copy from
     */
    public XMLString(final XMLString src) {
        this(src, 0);
    }

    /**
     * Constructs an XMLCharBuffer from another buffer. Copies the data
     * over. You can add more capacity on top of the source length. If
     * you specify 0, the capacity will match the src length.
     *
     * @param src the source buffer to copy from
     * @param addCapacity how much capacity to add to origin length
     */
    public XMLString(final XMLString src, final int addCapacity) {
        data_ = Arrays.copyOf(src.data_, src.length_ + Math.max(0, addCapacity));
        length_ = src.length();
        growBy_ = Math.max(1, CAPACITY_GROWTH);
    }

    /**
     * Constructs an XMLCharBuffer from a string. To avoid
     * too much allocation, we just take the string array as is and
     * don't allocate extra space in the first place.
     *
     * @param src the string to copy from
     */
    public XMLString(final String src) {
        data_ = src.toCharArray();
        length_ = src.length();
        growBy_ = CAPACITY_GROWTH;
    }

    /**
     * Constructs an XMLString structure preset with the specified values.
     * There will not be any room to grow, if you need that, construct an
     * empty one and append.
     *
     * <p>There are not range checks performed. Make sure your data is correct.
     *
     * @param ch     The character array, must not be null
     * @param offset The offset into the character array.
     * @param length The length of characters from the offset.
     */
    public XMLString(final char[] ch, final int offset, final int length) {
        // just as big as we need it
        this(length);
        append(ch, offset, length);
    }

    /**
     * Check capacity and grow if needed automatically
     *
     * @param minimumCapacity how much space do we need at least
     */
    private void ensureCapacity(final int minimumCapacity) {
        if (minimumCapacity > data_.length) {
            final int newSize = Math.max(minimumCapacity + growBy_, (data_.length << 1) + 2);
            data_ = Arrays.copyOf(data_, newSize);
        }
    }

    /**
     * Returns the current max capacity without growth. Does not
     * indicate how much capacity is already in use. Use {@link #length()}
     * for that.
     *
     * @return the current capacity, not taken any usage into account
     */
    public int capacity() {
        return data_.length;
    }

    /**
     * Appends a single character to the buffer but growing it first without
     * checking if needed.
     *
     * @param c the character to append
     * @return this instance
     */
    private void growByAtLeastOne() {
        final int newSize = Math.max(growBy_, (data_.length << 1) + 2);
        data_ = Arrays.copyOf(data_, newSize);
    }

    /**
     * Appends a single character to the buffer.
     *
     * @param c the character to append
     * @return this instance
     */
    public XMLString append(final char c) {
        final int oldLength = length_;

        // ensureCapacity is too large, so we keep things small here and
        // also allow to keep the grow part external
        if (oldLength == data_.length) {
            growByAtLeastOne();
        }

        data_[oldLength] = c;
        length_++;

        return this;
    }

    /**
     * Append two characters at once, mainly to make
     * a codePoint add more efficient
     *
     * @param c1 the first character to append
     * @param c2 the second character to append
     * @return this instance
     */
    public XMLString append(final char c1, final char c2) {
        if (length_ + 1 < data_.length) {
            data_[length_++] = c1;
            data_[length_++] = c2;
        }
        else {
            // that part is less efficient but happens less often
            append(c1);
            append(c2);
        }

        return this;
    }

    /**
     * Append a string to this buffer without copying the string first.
     *
     * @param src the string to append
     * @return this instance
     */
    public XMLString append(final String src) {
        final int start = length_;
        length_ = length_ + src.length();
        ensureCapacity(length_);

        // copy char by char because we don't get a copy for free
        // from a string yet, this might change when immutable arrays
        // make it into Java, but that will not be very soon
        for (int i = 0; i < src.length(); i++) {
            data_[start + i] = src.charAt(i);
        }

        return this;
    }

    /**
     * Add another buffer to this one.
     *
     * @param src the buffer to append
     * @return this instance
     */
    public XMLString append(final XMLString src) {
        final int start = length_;
        length_ = length_ + src.length();
        ensureCapacity(length_);

        System.arraycopy(src.data_, 0, data_, start, src.length_);

        return this;
    }

    /**
     * Add data from a char array to this buffer with the ability to specify
     * a range to copy from
     *
     * @param src the source char array
     * @param offset the pos to start to copy from
     * @param length the length of the data to copy
     *
     * @return this instance
     */
    public XMLString append(final char[] src, final int offset, final int length) {
        final int start = length_;
        length_ = start + length;

        ensureCapacity(length_);

        System.arraycopy(src, offset, data_, start, length);

        return this;
    }

    /**
     * Inserts a character at the beginning
     *
     * @param c the char to insert at the beginning
     * @return this instance
     */
    public XMLString prepend(final char c) {
        final int oldLength = length_;

        // ensureCapacity is too large, so we keep things small here and
        // also allow to keep the grow part external
        if (oldLength == data_.length) {
            growByAtLeastOne();
        }

        // shift all to the right by one
        System.arraycopy(data_, 0, data_, 1, oldLength);
        data_[0] = c;
        length_++;

        return this;
    }

    /**
     * Returns the current length
     *
     * @return the length of the charbuffer data
     */
    @Override
    public int length() {
        return length_;
    }

    /**
     * Tell us how much the capacity grows if needed
     *
     * @return the value that determines how much we grow the backing
     *      array in case we have to
     */
    public int getGrowBy() {
        return growBy_;
    }

    /**
     * Resets the buffer to 0 length. It won't resize it to avoid memory
     * churn.
     *
     * @return this instance for fluid programming
     */
    public XMLString clear() {
        length_ = 0;

        return this;
    }

    /**
     * Resets the buffer to 0 length and sets the new data. This
     * is a little cheaper than clear().append(c) depending on
     * the where  and the inlining decisions.
     *
     * @param c the char to set
     * @return this instance for fluid programming
     */
    public XMLString clearAndAppend(final char c) {
        length_ = 0;

        if (data_.length > 0) {
            data_[length_] = c;
            length_++;
        }
        else {
            // the rare case when we don't have any buffer at hand
            append(c);
        }

        return this;
    }

    /**
     * Does this buffer end with this string? If we check for
     * the empty string, we get true. If we would support JDK 11, we could
     * use Arrays.mismatch and be way faster.
     *
     * @param s the string to check the end against
     * @return true of the end matches the buffer, false otherwise
     */
    public boolean endsWith(final String s) {
        // length does not match, cannot be the end
        if (length_ < s.length()) {
            return false;
        }

        // check the string by each char, avoids a copy of the string
        final int start = length_ - s.length();

        // change this to Arrays.mismatch when going JDK 11 or higher
        for (int i = 0; i < s.length(); i++) {
            if (data_[i + start] != s.charAt(i)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Reduces the buffer to the content between start and end marker when
     * only whitespaces are found before the startMarker as well as after the end marker.
     * If both strings overlap due to identical characters such as "foo" and "oof"
     * and the buffer is " foof ", we don't do anything.
     *
     * <p>If a marker is empty, it behaves like {@link java.lang.String#trim()} on that side.
     *
     * @param startMarker the start string to find, must not be null
     * @param endMarker the end string to find, must not be null
     * @return this instance
     *
     * @deprecated Use the new method {@link #trimToContent(String, String)} instead.
     */
    public XMLString reduceToContent(final String startMarker, final String endMarker) {
        return trimToContent(startMarker, endMarker);
    }

    /**
     * Reduces the buffer to the content between start and end marker when
     * only whitespaces are found before the startMarker as well as after the end marker.
     * If both strings overlap due to identical characters such as "foo" and "oof"
     * and the buffer is " foof ", we don't do anything.
     *
     * <p>If a marker is empty, it behaves like {@link java.lang.String#trim()} on that side.
     *
     * @param startMarker the start string to find, must not be null
     * @param endMarker the end string to find, must not be null
     * @return this instance
     */
    public XMLString trimToContent(final String startMarker, final String endMarker) {
        // if both are longer or same length than content, don't do anything
        final int markerLength = startMarker.length() + endMarker.length();
        if (markerLength >= length_) {
            return this;
        }

        // run over starting whitespaces
        int sPos = 0;
        for ( ; sPos < length_ - markerLength; sPos++) {
            if (!Character.isWhitespace(data_[sPos])) {
                break;
            }
        }

        // run over ending whitespaces
        int ePos = length_ - 1;
        for ( ; ePos > sPos - markerLength; ePos--) {
            if (!Character.isWhitespace(data_[ePos])) {
                break;
            }
        }

        // if we have less content than marker length, give up
        // this also helps when markers overlap such as
        // <!-- and --> and the string is " <!---> "
        if (ePos - sPos + 1 < markerLength) {
            return this;
        }

        // check the start
        for (int i = 0; i < startMarker.length(); i++) {
            if (startMarker.charAt(i) != data_[i + sPos]) {
                // no start match, stop and don't do anything
                return this;
            }
        }

        // check the end, ePos is when the first good char
        // occurred
        final int endStartCheckPos = ePos - endMarker.length() + 1;
        for (int i = 0; i < endMarker.length(); i++) {
            if (endMarker.charAt(i) != data_[endStartCheckPos + i]) {
                // no start match, stop and don't do anything
                return this;
            }
        }

        // shift left and cut length
        final int newLength = ePos - sPos + 1 - markerLength;
        System.arraycopy(data_,
                        sPos + startMarker.length(),
                        data_,
                        0, newLength);
        length_ = newLength;

        return this;
    }

    /**
     * Check if we have only whitespaces
     *
     * @return true if we have only whitespace, false otherwise
     */
    public boolean isWhitespace() {
        for (int i = 0; i < length_; i++) {
            if (!Character.isWhitespace(data_[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Trims the string similar to {@link java.lang.String#trim()}
     *
     * @return a string with removed whitespace at the beginning and the end
     */
    public XMLString trim() {
        // clean the end first, because it is cheap
        return trimTrailing().trimLeading();
    }

    /**
     * Removes all whitespace before the first non-whitespace char.
     * If all are whitespaces, we get an empty buffer
     *
     * @return this instance
     */
    public XMLString trimLeading() {
        // run over starting whitespace
        int sPos = 0;
        for ( ; sPos < length_; sPos++) {
            if (!Character.isWhitespace(data_[sPos])) {
                break;
            }
        }

        if (sPos == 0) {
            // nothing to do
            return this;
        }
        else if (sPos == length_) {
            // only whitespace
            length_ = 0;
            return this;
        }

        // shift left
        final int newLength = length_ - sPos;
        System.arraycopy(data_,
                        sPos,
                        data_,
                        0, newLength);
        length_ = newLength;

        return this;
    }

    /**
     * Removes all whitespace at the end.
     * If all are whitespace, we get an empty buffer
     *
     * @return this instance
     *
     * @deprecated Use {@link #trimTrailing()} instead.
     */
    public XMLString trimWhitespaceAtEnd() {
        return trimTrailing();
    }

    /**
     * Removes all whitespace at the end.
     * If all are whitespace, we get an empty buffer
     *
     * @return this instance
     */
    public XMLString trimTrailing() {
        // run over ending whitespaces
        int ePos = length_ - 1;
        for ( ; ePos >= 0; ePos--) {
            if (!Character.isWhitespace(data_[ePos])) {
                break;
            }
        }

        length_ = ePos + 1;

        return this;
    }

    /**
     * Shortens the buffer by that many positions. If the count is
     * larger than the length, we get just an empty buffer. If you pass in negative
     * values, we are failing, likely often silently. It is all about performance and
     * not a general all-purpose API.
     *
     * @param count a positive number, no runtime checks, if count is larger than
     *      length, we get length = 0
     * @return this instance
     */
    public XMLString shortenBy(final int count) {
        final int newLength = length_ - count;
        length_ = newLength < 0 ? 0 : newLength;

        return this;
    }

    /**
     * Get the characters as char array, this will be a copy!
     *
     * @return a copy of the underlying char darta
     */
    public char[] getChars() {
        return Arrays.copyOf(data_, length_);
    }

    /**
     * Returns a string representation of this buffer. This will be a copy
     * operation. If the buffer is empty, we get a constant empty String back
     * to avoid any overhead.
     *
     * @return a string of the content of this buffer
     */
    @Override
    public String toString() {
        if (length_ > 0) {
            return new String(data_, 0, length_);
        }
        return "";
    }

    /**
     * Returns a string representation of a buffer. This will be a copy
     * operation. If the buffer is empty, we get a constant empty String back
     * to avoid any overhead. Method exists to deliver null-safety.
     *
     * @return a string of the content of this buffer
     */
    public static String toString(final XMLString seq) {
        if (seq == null) {
            return null;
        }
        if (seq.length_ > 0) {
            return new String(seq.data_, 0, seq.length_);
        }
        return "";
    }

    /**
     * Returns a string representation of this buffer using a cache as
     * source to avoid duplicates. You have to make sure that the cache
     * support concurrency in case you use that in a concurrent context.
     *
     * <p> The cache will be filled with a copy of the XMLString to ensure
     * immutability. This copy is minimally sized.
     *
     * @param cache the cache to be used
     * @return a string of the content of this buffer, preferably taken from the cache
     *
     */
    public String toString(final FastHashMap<XMLString, String> cache) {
        String s = cache.get(this);
        if (s == null) {
            s = toString();
            // cache a copy of the string, because it would mutate otherwise
            cache.put(clone(), s);
        }
        return s;
    }

    /**
     * Returns a string representation of the buffer using a cache as
     * source to avoid duplicates. You have to make sure that the cache
     * support concurrency in case you use that in a concurrent context.
     *
     * <p> The cache will be filled with a copy of the XMLString to ensure
     * immutability. This copy is minimally sized.
     *
     * @param seq the XMLString to convert
     * @param cache the cache to be used
     * @return a string of the content of this buffer, preferably taken from the cache, null
     *         if seq was null
     *
     */
    public static String toString(final XMLString seq, final FastHashMap<XMLString, String> cache) {
        if (seq == null) {
            return null;
        }
        String s = cache.get(seq);
        if (s == null) {
            s = seq.toString();
            // cache a copy of the string, because it would mutate otherwise
            cache.put(seq.clone(), s);
        }
        return s;
    }

    /**
     * Returns the char a the given position. Will complain if
     * we try to read outside the range. We do a range check here
     * because we might not notice when we are within the buffer
     * but outside the current length.
     *
     * @param index the position to read from
     * @return the char at the position
     * @throws IndexOutOfBoundsException
     *      in case one tries to read outside of valid buffer range
     */
    @Override
    public char charAt(final int index) {
        if (index > length_ - 1 || index < 0) {
            throw new IndexOutOfBoundsException(
                            "Tried to read outside of the valid buffer data");
        }

        return data_[index];
    }

    /**
     * Returns the char at the given position. No checks are
     * performed. It is up to the caller to make sure we
     * read correctly. Reading outside of the array will
     * cause an {@link IndexOutOfBoundsException} but using an
     * incorrect position in the array (such as beyond length)
     * might stay unnoticed! This is a performance method,
     * use at your own risk.
     *
     * @param index the position to read from
     * @return the char at the position
     */
    public char unsafeCharAt(final int index) {
        return data_[index];
    }

    /**
     * Returns a content copy of this buffer
     *
     * @return a copy of this buffer, the capacity might differ
     */
    @Override
    public XMLString clone() {
        return new XMLString(this);
    }

    /**
     * Returns a <code>CharSequence</code> that is a subsequence of this sequence.
     * The subsequence starts with the <code>char</code> value at the specified index and
     * ends with the <code>char</code> value at index <tt>end - 1</tt>.  The length
     * (in <code>char</code>s) of the
     * returned sequence is <tt>end - start</tt>, so if <tt>start == end</tt>
     * then an empty sequence is returned.
     *
     * @param start the start index, inclusive
     * @param end the end index, exclusive
     * @return the specified subsequence
     * @throws IndexOutOfBoundsException
     *          if <tt>start</tt> or <tt>end</tt> are negative,
     *          if <tt>end</tt> is greater than <tt>length()</tt>,
     *          or if <tt>start</tt> is greater than <tt>end</tt>
     *
     * @return a charsequence of this buffer
     */
    @Override
    public CharSequence subSequence(final int start, final int end) {
        if (start < 0) {
            throw new StringIndexOutOfBoundsException(start);
        }
        if (end > length_) {
            throw new StringIndexOutOfBoundsException(end);
        }

        final int l = end - start;
        if (l < 0) {
            throw new StringIndexOutOfBoundsException(l);
        }

        return new String(data_, start, l);
    }

    /**
     * Two buffers are identical when the length and
     * the content of the backing array (only for the
     * data in view) are identical.
     *
     * @param o the object to compare with
     * @return true if length and array content match, false otherwise
     */
    @Override
    public boolean equals(final Object o) {
        if (o instanceof CharSequence) {
            final CharSequence ob = (CharSequence) o;

            if (ob.length() != length_) {
                return false;
            }

            // ok, in JDK 11 or up, we could use an
            // Arrays.mismatch, but we cannot do that
            // due to JDK 8 compatibility @TODO RS
            for (int i = 0; i < length_; i++) {
                if (ob.charAt(i) != data_[i]) {
                    return false;
                }
            }

            // length and content match, be happy
            return true;
        }

        return false;
    }

    /**
     * Compares a CharSequence with an XMLString in a null-safe manner.
     * For more, see {@link #equals(Object)}. The XMLString
     * can be null, but the CharSequence must not be null. This mimics the
     * typical use case "string".equalsIgnoreCase(null) which returns false
     * without raising an exception.
     *
     * @param sequence the sequence to compare to, null is permitted
     * @param s the XMLString to use for comparison
     * @return true if the sequence matches case-insensive, false otherwise
     */
    public static boolean equals(final CharSequence sequence, final XMLString s) {
        if (s == null) {
            return false;
        }
        return s.equals(sequence);
    }
    /**
     * We don't cache the hashcode because we mutate often. Don't use this in
     * hashmaps as key. But you can use that to look up in a hashmap against
     * a string using the CharSequence interface.
     *
     * @return the hashcode, similar to what a normal string would deliver
     */
    @Override
    public int hashCode() {
        int h = 0;

        for (int i = 0; i < length_; i++) {
            h = ((h << 5) - h) + data_[i];
        }

        return h;
    }

    /**
     * Append a character to an XMLCharBuffer. The character is an int value, and
     * can either be a single UTF-16 character or a supplementary character
     * represented by two UTF-16 code points.
     *
     * @param codePoint The character value.
     * @return this instance for fluid programming
     *
     * @throws IllegalArgumentException if the specified
     *          {@code codePoint} is not a valid Unicode code point.
     */
    public boolean appendCodePoint(final int codePoint) {
        if (Character.isBmpCodePoint(codePoint)) {
            append((char) codePoint);
        }
        else if (Character.isValidCodePoint(codePoint)) {
            // as seen in the JDK, avoid a char array in between
            append(Character.highSurrogate(codePoint), Character.lowSurrogate(codePoint));
        }
        else {
            // when value is not valid as UTF-16
            append(REPLACEMENT_CHARACTER);
            return false;
        }
        return true;
    }

    /**
     * This uppercases an XMLString in place and will likely not
     * consume extra memory unless the character might grow. This
     * conversion can be incorrect for certain characters from some
     * locales. See {@link String#toUpperCase()}.
     *
     * <p>We cannot correctly deal with ß for instance.
     *
     * <p>Note: We change the current XMLString and don't get a copy back
     * but this instance.
     *
     * @param locale the locale to use in case we have to bail out and convert
     *        using String, this also means, that the result is not perfect
     *        when comparing to {@link String#toLowerCase(Locale)}
     * @return this updated instance
     */
    public XMLString toUpperCase(final Locale locale) {
        // Ok, as soon as we something complicated, we bail out
        // and take the expensive route
        boolean gaveUp = false;
        for (int i = 0; i < length_; i++) {
            final char c = data_[i];

            if (Character.isHighSurrogate(c)) {
                // give up, we have UTF-16 here
                gaveUp = true;
                break;
            }
            // we know it is a unicode value and not a code point, so
            // char to int is safe
            final int upperCasePoint = Character.toUpperCase((int) c);
            data_[i] = (char) upperCasePoint;
        }

        // we converted inline and nicely
        if (!gaveUp) {
            return this;
        }

        // go expensive using String
        final String s = toString().toUpperCase(locale);

        // put the XMLString together again
        final int newLength = s.length();
        if (data_.length < newLength) {
            data_ = new char[newLength];
        }

        // copy everything and fix the length
        for (int i = 0; i < newLength; i++) {
            data_[i] = s.charAt(i);
        }
        length_ = newLength;

        return this;
    }

    /**
     * This lowercases an XMLString in place and will likely not
     * consume extra memory unless the character might grow. This
     * conversion can be incorrect for certain characters from some
     * locales. See {@link String#toUpperCase()}.
     *
     * <p>Note: We change the current XMLString and don't get a copy back
     * but this instance.
     *
     * @param locale the locale to use in case we have to bail out and convert
     *        using String, this also means, that the result is not perfect
     *        when comparing to {@link String#toLowerCase(Locale)}
     * @return this updated instance
     */
    public XMLString toLowerCase(final Locale locale) {
        // Ok, as soon as we something complicated, we bail out
        // and take the expensive route
        boolean gaveUp = false;
        for (int i = 0; i < length_; i++) {
            final char c = data_[i];

            if (Character.isHighSurrogate(c)) {
                // give up, we have UTF-16 here
                gaveUp = true;
                break;
            }
            // we know it is a unicode value and not a code point, so
            // char to int is safe
            final int lowerCasePoint = Character.toLowerCase((int) c);
            data_[i] = (char) lowerCasePoint;
        }

        // we converted inline and nicely
        if (!gaveUp) {
            return this;
        }

        // go expensive using String
        final String s = toString().toLowerCase(locale);

        // put the XMLString together again
        final int newLength = s.length();
        if (data_.length < newLength) {
            data_ = new char[newLength];
        }

        // copy everything and fix the length
        for (int i = 0; i < newLength; i++) {
            data_[i] = s.charAt(i);
        }
        length_ = newLength;

        return this;
    }

    /**
     * Compares a CharSequence with an XMLString in a null-safe manner.
     * For more, see {@link #equalsIgnoreCase(CharSequence)}. The XMLString
     * can be null, but the CharSequence must not be null. This mimic the
     * typical use case "string".equalsIgnoreCase(null) which returns false
     * without raising an exception.
     *
     * @param sequence the sequence to compare to, null is permitted
     * @param s the XMLString to use for comparison
     * @return true if the sequence matches case-insensive, false otherwise
     */
    public static boolean equalsIgnoreCase(final CharSequence sequence, final XMLString s) {
        if (s == null) {
            return false;
        }
        return s.equalsIgnoreCase(sequence);
    }

    /**
     * Compares this with a CharSequence in a case-insensitive manner.
     *
     * <p>This code might have subtle edge-case defects for some rare locales
     * and related characters. See {@link java.lang.String#toLowerCase(Locale)}.
     * The locales tr, at, lt and the extra letters GREEK CAPITAL LETTER SIGMA
     * and LATIN CAPITAL LETTER I WITH DOT ABOVE are our challengers. If the
     * input would match with {@link #equals(Object)}, everything is fine, just
     * in case we have to check for a casing difference, we might see a problem.
     *
     * <p>But this is for XML/HTML characters and we know what we compare, hence
     * this should not be any issue for us.
     *
     * @param s the sequence to compare to, null is permitted
     * @return true if the sequences match case-insensive, false otherwise
     */
    public boolean equalsIgnoreCase(final CharSequence s) {
        if (s == null || s.length() != length_) {
            return false;
        }

        // ok, in JDK 11 or up, we could use an
        // Arrays.mismatch, but we cannot do that
        // due to JDK 8 compatibility @TODO RS
        for (int i = 0; i < length_; i++) {
            final char c1 = data_[i];
            final char c2 = s.charAt(i);

            // if this compares nicely, we are good, if this does not
            // compare we lowercase and compare, if this fails, we
            // fall back to String and hence it becomes expensive
            if (c1 == c2) {
                continue;
            }

            // this behaves likes the JDK does, uppercase first, and later
            // we try lowercase again for the Georgian alphabet
            final char c1u = Character.toUpperCase(c1);
            final char c2u = Character.toUpperCase(c2);
            if (c1u == c2u) {
                continue;
            }

            final char c1l = Character.toLowerCase(c1);
            final char c2l = Character.toLowerCase(c2);
            if (c1l == c2l) {
                continue;
            }

            // does not match, stop here
            return false;
        }

        // length and content match, be happy
        return true;
    }

    /**
     * Code shared by String and StringBuffer to do searches. The
     * source is the character array being searched, and the target
     * is the string being searched for.
     *
     * @param source       the characters being searched.
     * @param sourceOffset offset of the source string.
     * @param sourceCount  count of the source string.
     * @param target       the characters being searched for.
     * @param targetOffset offset of the target string.
     * @param targetCount  count of the target string.
     * @param fromIndex    the index to begin searching from.
     *
     * @return the first position both array match
     */
    private static int indexOf(final char[] source, final int sourceOffset, final int sourceCount,
                               final char[] target, final int targetOffset, final int targetCount,
                               int fromIndex) {

        if (fromIndex >= sourceCount) {
            return targetCount == 0 ? sourceCount : -1;
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (targetCount == 0) {
            return fromIndex;
        }

        final char first = target[targetOffset];
        final int max = sourceOffset + (sourceCount - targetCount);

        for (int i = sourceOffset + fromIndex; i <= max; i++) {
            /* Look for first character. */
            if (source[i] != first) {
                while (++i <= max && source[i] != first) {
                    // empty
                }
            }

            /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                final int end = j + targetCount - 1;
                for (int k = targetOffset + 1; j < end && source[j] == target[k]; j++, k++) {
                    // empty
                }

                if (j == end) {
                    /* Found whole string. */
                    return i - sourceOffset;
                }
            }
        }
        return -1;
    }

    /**
     * Find the first occurrence of a char
     *
     * @param c the char to search
     * @return the position or -1 otherwise
     */
    public int indexOf(final char c) {
        for (int i = 0; i < length_; i++) {
            if (data_[i] == c) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Search for the first occurrence of another buffer in this buffer
     *
     * @param s the buffer to be search for
     * @return the first found position or -1 if not found
     */
    public int indexOf(final XMLString s) {
        return s != null ? indexOf(data_, 0, length_, s.data_, 0, s.length_, 0) : -1;
    }

    /**
     * See if this string contains the other
     *
     * @param s the XMLString to search and match
     * @return true if s is in this string or false otherwise
     */
    public boolean contains(final XMLString s) {
        return s != null ? indexOf(data_, 0, length_, s.data_, 0, s.length_, 0) > -1 : false;
    }

    // this stuff is here for performance reasons to avoid a copy
    // it has been taken from the old XMLString and because there was no JavaDoc
    // we still don't have any, legacy code
    public void characters(final ContentHandler contentHandler) throws SAXException {
        contentHandler.characters(data_, 0, length_);
    }

    public void ignorableWhitespace(final ContentHandler contentHandler) throws SAXException {
        contentHandler.ignorableWhitespace(data_, 0, length_);
    }

    public void comment(final LexicalHandler lexicalHandler) throws SAXException {
        lexicalHandler.comment(data_, 0, length_);
    }
}
