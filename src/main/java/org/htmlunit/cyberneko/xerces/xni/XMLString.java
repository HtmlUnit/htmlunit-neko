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
package org.htmlunit.cyberneko.xerces.xni;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * This class is used as a structure to pass text contained in the underlying
 * character buffer of the scanner. The offset and length fields allow the
 * buffer to be re-used without creating new character arrays.
 * <p>
 * <strong>Note:</strong> Methods that are passed an XMLString structure should
 * consider the contents read-only and not make any modifications to the
 * contents of the buffer. The method receiving this structure should also not
 * modify the offset and length if this structure (or the values of this
 * structure) are passed to another method.
 * <p>
 * <strong>Note:</strong> Methods that are passed an XMLString structure are
 * required to copy the information out of the buffer if it is to be saved for
 * use beyond the scope of the method. The contents of the structure are
 * volatile and the contents of the character buffer cannot be assured once the
 * method that is passed this structure returns. Therefore, methods passed this
 * structure should not save any reference to the structure or the character
 * array contained in the structure.
 *
 * @author Eric Ye, IBM
 * @author Andy Clark, IBM
 * @author Ronald Brill
 */
public class XMLString {

    private final StringBuilder builder_;

    /**
     * Constructs an XMLString.
     */
    public XMLString() {
        builder_ = new StringBuilder();
    }

    /**
     * Constructs an XMLString structure preset with the specified values.
     *
     * @param ch     The character array.
     * @param offset The offset into the character array.
     * @param length The length of characters from the offset.
     */
    public XMLString(final char[] ch, final int offset, final int length) {
        this();
        builder_.append(ch, offset, length);
    }

    public void append(final char c) {
        builder_.append(c);
    }


    public void append(final String str) {
        builder_.append(str);
    }

    public void append(final XMLString xmlStr) {
        builder_.append(xmlStr.builder_);
    }

    public void append(final char[] str, final int offset, final int len) {
        builder_.append(str, offset, len);
    }

    public char charAt(final int index) {
        return builder_.charAt(index);
    }

    public int length() {
        return builder_.length();
    }

    public boolean endsWith(final String string) {
        final int l = string.length();
        if (builder_.length() < l) {
            return false;
        }

        return string.equals(builder_.substring(length() -l));
    }

    // Reduces the buffer to the content between start and end marker when
    // only whitespaces are found before the startMarker as well as after the end
    // marker
    public void reduceToContent(final String startMarker, final String endMarker) {
        int i = 0;
        int startContent = -1;

        final int startMarkerLength = startMarker.length();
        final int endMarkerLength = endMarker.length();

        while (i < builder_.length() - startMarkerLength - endMarkerLength) {
            final char c = builder_.charAt(i);
            if (Character.isWhitespace(c)) {
                ++i;
            }
            else if (c == startMarker.charAt(0) && startMarker.equals(builder_.substring(i, i + startMarkerLength))) {
                startContent = i + startMarkerLength;
                break;
            }
            else {
                return; // start marker not found
            }
        }
        if (startContent == -1) { // start marker not found
            return;
        }

        i = builder_.length() - 1;
        while (i > startContent + endMarkerLength) {
            final char c = builder_.charAt(i);
            if (Character.isWhitespace(c)) {
                --i;
            }
            else if (c == endMarker.charAt(endMarkerLength - 1)
                    && endMarker.equals(builder_.substring(i - endMarkerLength + 1, i + 1))) {

                builder_.delete(i - endMarkerLength + 1, builder_.length());
                if (startContent > 0) {
                    builder_.delete(0, startContent);
                }
                return;
            }
            else {
                return; // end marker not found
            }
        }
    }

    @Deprecated
    public char[] getChars() {
        char[] chars = new char[builder_.length()];
        builder_.getChars(0, builder_.length(), chars, 0);
        return chars;
    }

    @Override
    public XMLString clone() {
        XMLString clone = new XMLString();
        clone.builder_.append(builder_);
        return clone;
    }

    /**
     * Resets.
     * */
    public XMLString clear() {
        builder_.setLength(0);
        return this;
    }

    public boolean isWhitespace() {
        for (int i = 0; i < builder_.length(); i++) {
            if (!Character.isWhitespace(builder_.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public void trimWhitespaceAtEnd() {
        int i = builder_.length() - 1;

        while (i > -1) {
            if (!Character.isWhitespace(builder_.charAt(i))) {
                builder_.delete(i + 1, builder_.length());
                return;
            }
            i--;
        }
        clear();
    }

    public void appendTo(StringBuilder stringBuilder) {
        stringBuilder.append(builder_);
    }

    @Override
    public String toString() {
        return builder_.toString();
    }

    public void characters(final ContentHandler contentHandler) throws SAXException {
        contentHandler.characters(getChars(), 0, length());
    }

    public void ignorableWhitespace(final ContentHandler contentHandler) throws SAXException {
        contentHandler.ignorableWhitespace(getChars(), 0, length());
    }

    public void comment(final LexicalHandler lexicalHandler) throws SAXException {
        lexicalHandler.comment(getChars(), 0, length());
    }
}
