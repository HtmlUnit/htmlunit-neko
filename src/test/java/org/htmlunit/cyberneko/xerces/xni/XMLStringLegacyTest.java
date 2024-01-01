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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link XMLString}. These tests are here to ensure
 * compatibility with the old XMLString implementation before version
 * 3.10.0
 *
 * @author Ronald Brill
 */
public class XMLStringLegacyTest {

    @Test
    public void reduceToContent() {
        final XMLString xmlString = new XMLString();

        xmlString.clear().append("<!-- hello-->");
        xmlString.reduceToContent("<!--", "-->");
        assertEquals(" hello", xmlString.toString());

        xmlString.clear().append("  \n <!-- hello-->\n");
        xmlString.reduceToContent("<!--", "-->");
        assertEquals(" hello", xmlString.toString());

        xmlString.clear().append("hello");
        xmlString.reduceToContent("<!--", "-->");
        assertEquals("hello", xmlString.toString());

        xmlString.clear().append("<!-- hello");
        xmlString.reduceToContent("<!--", "-->");
        assertEquals("<!-- hello", xmlString.toString());

        xmlString.clear().append("<!--->");
        xmlString.reduceToContent("<!--", "-->");
        assertEquals("<!--->", xmlString.toString());
    }

    @Test
    public void trimWhitespaceAtEnd() {
        final XMLString xmlString = new XMLString();

        xmlString.clear().append("");
        xmlString.trimWhitespaceAtEnd();
        assertEquals("", xmlString.toString());

        xmlString.clear().append("a");
        xmlString.trimWhitespaceAtEnd();
        assertEquals("a", xmlString.toString());

        xmlString.clear().append(" a");
        xmlString.trimWhitespaceAtEnd();
        assertEquals(" a", xmlString.toString());

        xmlString.clear().append("a b");
        xmlString.trimWhitespaceAtEnd();
        assertEquals("a b", xmlString.toString());

        xmlString.clear().append("a ");
        xmlString.trimWhitespaceAtEnd();
        assertEquals("a", xmlString.toString());

        xmlString.clear().append("a  ");
        xmlString.trimWhitespaceAtEnd();
        assertEquals("a", xmlString.toString());

        xmlString.clear().append(" ");
        xmlString.trimWhitespaceAtEnd();
        assertEquals("", xmlString.toString());

        xmlString.clear().append("  ");
        xmlString.trimWhitespaceAtEnd();
        assertEquals("", xmlString.toString());
    }
}
