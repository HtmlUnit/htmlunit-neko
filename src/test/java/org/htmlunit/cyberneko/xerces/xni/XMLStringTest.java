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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link XMLString}.
 * @author Ronald Brill
 */
public class XMLStringTest {

    @Test
    public void reduceToContent() {
        XMLString buffer = new XMLString();

        buffer.clear().append("<!-- hello-->");
        buffer.reduceToContent("<!--", "-->");
        assertEquals(" hello", buffer.toString());

        buffer.clear().append("  \n <!-- hello-->\n");
        buffer.reduceToContent("<!--", "-->");
        assertEquals(" hello", buffer.toString());

        buffer.clear().append("hello");
        buffer.reduceToContent("<!--", "-->");
        assertEquals("hello", buffer.toString());

        buffer.clear().append("<!-- hello");
        buffer.reduceToContent("<!--", "-->");
        assertEquals("<!-- hello", buffer.toString());

        buffer.clear().append("<!--->");
        buffer.reduceToContent("<!--", "-->");
        assertEquals("<!--->", buffer.toString());
    }

    @Test
    public void trimWhitespaceAtEnd() {
        XMLString buffer = new XMLString();

        buffer.clear().append("");
        buffer.trimWhitespaceAtEnd();
        assertEquals("", buffer.toString());

        buffer.clear().append("a");
        buffer.trimWhitespaceAtEnd();
        assertEquals("a", buffer.toString());

        buffer.clear().append(" a");
        buffer.trimWhitespaceAtEnd();
        assertEquals(" a", buffer.toString());

        buffer.clear().append("a b");
        buffer.trimWhitespaceAtEnd();
        assertEquals("a b", buffer.toString());

        buffer.clear().append("a ");
        buffer.trimWhitespaceAtEnd();
        assertEquals("a", buffer.toString());

        buffer.clear().append("a  ");
        buffer.trimWhitespaceAtEnd();
        assertEquals("a", buffer.toString());

        buffer.clear().append(" ");
        buffer.trimWhitespaceAtEnd();
        assertEquals("", buffer.toString());

        buffer.clear().append("  ");
        buffer.trimWhitespaceAtEnd();
        assertEquals("", buffer.toString());
    }
}
