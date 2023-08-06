/*
 * Copyright 2017-2023 Ronald Brill
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
package org.htmlunit.cyberneko.xerces.util;

import org.htmlunit.cyberneko.xerces.xni.XMLString;

/**
 * XMLString is a structure used to pass character arrays. However,
 * XMLStringBuffer is a buffer in which characters can be appended and extends
 * XMLString so that it can be passed to methods expecting an XMLString object.
 * This is a safe operation because it is assumed that any callee will
 * <strong>not</strong> modify the contents of the XMLString structure.
 * <p>
 * The contents of the string are managed by the string buffer. As characters
 * are appended, the string buffer will grow as needed.
 * <p>
 * <strong>Note:</strong> Never set the <code>ch</code>, <code>offset</code>,
 * and <code>length</code> fields directly. These fields are managed by the
 * string buffer. In order to reset the buffer, call <code>clear()</code>.
 *
 * @author Andy Clark, IBM
 * @author Eric Ye, IBM
 */
public class XMLStringBuffer extends XMLString {

    /** Default buffer size (32). */
    public static final int DEFAULT_SIZE = 32;

    public XMLStringBuffer() {
        this(DEFAULT_SIZE);
    }

    public XMLStringBuffer(final int size) {
        ch = new char[size];
    }

    public XMLStringBuffer(final String s) {
        this(s.length());
        append(s);
    }

    /** Clears the string buffer. */
    @Override
    public void clear() {
        offset = 0;
        length = 0;
    }

    /**
     * append
     *
     * @param c thechar to append
     */
    public void append(final char c) {
        if (this.length + 1 > this.ch.length) {
            int newLength = this.ch.length * 2;
            if (newLength < this.ch.length + DEFAULT_SIZE) {
                newLength = this.ch.length + DEFAULT_SIZE;
            }
            final char[] newch = new char[newLength];
            System.arraycopy(this.ch, 0, newch, 0, this.length);
            this.ch = newch;
        }
        this.ch[this.length] = c;
        this.length++;
    }

    /**
     * append
     *
     * @param s the string to append
     */
    public void append(final String s) {
        final int length = s.length();
        if (this.length + length > this.ch.length) {
            int newLength = this.ch.length * 2;
            if (newLength < this.length + length + DEFAULT_SIZE) {
                newLength = this.ch.length + length + DEFAULT_SIZE;
            }
            final char[] newch = new char[newLength];
            System.arraycopy(this.ch, 0, newch, 0, this.length);
            this.ch = newch;
        }
        s.getChars(0, length, this.ch, this.length);
        this.length += length;
    }

    /**
     * append
     *
     * @param ch     char array
     * @param offset start
     * @param length length
     */
    public void append(final char[] ch, final int offset, final int length) {
        if (this.length + length > this.ch.length) {
            int newLength = this.ch.length * 2;
            if (newLength < this.length + length + DEFAULT_SIZE) {
                newLength = this.ch.length + length + DEFAULT_SIZE;
            }
            final char[] newch = new char[newLength];
            System.arraycopy(this.ch, 0, newch, 0, this.length);
            this.ch = newch;
        }
        System.arraycopy(ch, offset, this.ch, this.length, length);
        this.length += length;
    }

    /**
     * append
     *
     * @param s xmlstring
     */
    public void append(final XMLString s) {
        append(s.ch, s.offset, s.length);
    }
}
