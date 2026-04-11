/*
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
package org.htmlunit.cyberneko;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.htmlunit.cyberneko.HTMLElements.Element;
import org.htmlunit.cyberneko.HTMLTagBalancer.Info;
import org.htmlunit.cyberneko.xerces.util.XMLAttributesImpl;
import org.htmlunit.cyberneko.xerces.xni.QName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link HTMLTagBalancer.Info}.
 */
public class InfoTest {

    private final HTMLElements elements = new HTMLElements();

    // ---- 2-arg constructor ----

    @Test
    public void twoArgConstructorDeepCopiesQName() {
        final QName original = new QName(null, "div", "div", null);
        final Element elem = elements.getElement("div");

        final Info info = new Info(elem, original);

        assertNotSame(original, info.qname);
        assertEquals("div", info.qname.getRawname());

        // mutating original doesn't affect info
        original.setRawname("span");
        assertEquals("div", info.qname.getRawname());
    }

    @Test
    public void twoArgConstructorHasNullAttributes() {
        final Info info = new Info(
                elements.getElement("p"),
                new QName(null, "p", "p", null));

        assertNull(info.attributes);
    }

    // ---- 3-arg constructor with XMLAttributesImpl ----

    @Test
    public void threeArgConstructorDeepCopiesXMLAttributesImpl() {
        final XMLAttributesImpl attrs = new XMLAttributesImpl();
        attrs.addAttribute(new QName(null, "class", "class", null), "CDATA", "myClass", true);
        attrs.addAttribute(new QName(null, "id", "id", null), "CDATA", "myId", true);

        final Info info = new Info(
                elements.getElement("div"),
                new QName(null, "div", "div", null),
                attrs);

        assertNotNull(info.attributes);
        assertNotSame(attrs, info.attributes);
        assertEquals(2, info.attributes.getLength());
        assertEquals("myClass", info.attributes.getValue(0));

        // mutating original doesn't affect info's copy
        attrs.setValue(0, "changed");
        assertEquals("myClass", info.attributes.getValue(0));
    }

    // ---- 3-arg constructor with null attributes ----

    @Test
    public void threeArgConstructorWithNullAttributes() {
        final Info info = new Info(
                elements.getElement("span"),
                new QName(null, "span", "span", null),
                null);

        assertNull(info.attributes);
    }

    // ---- 3-arg constructor with empty attributes ----

    @Test
    public void threeArgConstructorWithEmptyAttributes() {
        final XMLAttributesImpl attrs = new XMLAttributesImpl();

        final Info info = new Info(
                elements.getElement("br"),
                new QName(null, "br", "br", null),
                attrs);

        // empty attributes: length == 0, so the copy is skipped
        assertNull(info.attributes);
    }

    // ---- element reference is shared, not copied ----

    @Test
    public void elementReferenceIsShared() {
        final Element elem = elements.getElement("div");
        final Info info = new Info(elem, new QName(null, "div", "div", null));

        assertSame(elem, info.element);
    }

    // ---- toString doesn't throw ----

    @Test
    public void toStringIncludesQName() {
        final Info info = new Info(
                elements.getElement("div"),
                new QName(null, "div", "div", null));
        final String s = info.toString();

        assertNotNull(s);
        assertTrue(s.contains("div"), "toString should include the rawname");
    }

    private static void assertTrue(final boolean condition, final String message) {
        org.junit.jupiter.api.Assertions.assertTrue(condition, message);
    }
}