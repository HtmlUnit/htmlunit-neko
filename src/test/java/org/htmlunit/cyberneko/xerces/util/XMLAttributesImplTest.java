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
package org.htmlunit.cyberneko.xerces.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.htmlunit.cyberneko.xerces.util.XMLAttributesImpl.Attribute;
import org.htmlunit.cyberneko.xerces.xni.QName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link XMLAttributesImpl}.
 */
public class XMLAttributesImplTest {

    // ---- basic add / retrieve ----

    @Test
    public void addAndRetrieveByIndex() {
        final XMLAttributesImpl attrs = new XMLAttributesImpl();
        final QName name = new QName(null, "class", "class", null);

        final int idx = attrs.addAttribute(name, "CDATA", "foo");

        assertEquals(0, idx);
        assertEquals(1, attrs.getLength());
        assertEquals("foo", attrs.getValue(0));
        assertEquals("CDATA", attrs.getType(0));
        assertEquals("class", attrs.getQName(0));
        assertEquals("class", attrs.getLocalName(0));
    }

    @Test
    public void addMultipleAndRetrieveByQName() {
        final XMLAttributesImpl attrs = new XMLAttributesImpl();
        attrs.addAttribute(new QName(null, "id", "id", null), "CDATA", "a");
        attrs.addAttribute(new QName(null, "class", "class", null), "CDATA", "b");
        attrs.addAttribute(new QName(null, "style", "style", null), "CDATA", "c");

        assertEquals(3, attrs.getLength());
        assertEquals("b", attrs.getValue("class"));
        assertEquals(1, attrs.getIndex("class"));
        assertNull(attrs.getValue("missing"));
        assertEquals(-1, attrs.getIndex("missing"));
    }

    @Test
    public void addByFourArgOverload() {
        final XMLAttributesImpl attrs = new XMLAttributesImpl();
        final QName name = new QName(null, "href", "href", null);

        attrs.addAttribute(name, "CDATA", "http://example.com", true);

        assertEquals(1, attrs.getLength());
        assertEquals("http://example.com", attrs.getValue(0));
        assertTrue(attrs.isSpecified(0));
    }

    @Test
    public void addByFiveArgOverload() {
        final XMLAttributesImpl attrs = new XMLAttributesImpl();
        final QName name = new QName(null, "value", "value", null);

        attrs.addAttribute(name, "CDATA", "normalized", "  raw  ", true);

        assertEquals(1, attrs.getLength());
        assertEquals("normalized", attrs.getValue(0));
        assertEquals("  raw  ", attrs.getNonNormalizedValue(0));
    }

    // ---- getIndex with namespace ----

    @Test
    public void getIndexByNamespace() {
        final XMLAttributesImpl attrs = new XMLAttributesImpl();
        attrs.addAttribute(
                new QName("xml", "lang", "xml:lang", "http://www.w3.org/XML/1998/namespace"),
                "CDATA", "en", true);
        attrs.addAttribute(
                new QName(null, "class", "class", null),
                "CDATA", "main", true);

        assertEquals(0, attrs.getIndex("http://www.w3.org/XML/1998/namespace", "lang"));
        assertEquals(-1, attrs.getIndex("http://wrong", "lang"));
        assertEquals(-1, attrs.getIndex(null, "missing"));
    }

    @Test
    public void getIndexByQNameWithNullRawname() {
        final XMLAttributesImpl attrs = new XMLAttributesImpl();
        // rawname is null — should not match any lookup
        attrs.addAttribute(new QName(null, "test", null, null), "CDATA", "val", true);

        assertEquals(-1, attrs.getIndex("test"));
        assertEquals(0, attrs.getIndex(null, "test"));
    }

    // ---- remove ----

    @Test
    public void removeAllAttributes() {
        final XMLAttributesImpl attrs = new XMLAttributesImpl();
        attrs.addAttribute(new QName(null, "a", "a", null), "CDATA", "1");
        attrs.addAttribute(new QName(null, "b", "b", null), "CDATA", "2");

        attrs.removeAllAttributes();

        assertEquals(0, attrs.getLength());
    }

    @Test
    public void removeAttributeAtShiftsIndexes() {
        final XMLAttributesImpl attrs = new XMLAttributesImpl();
        attrs.addAttribute(new QName(null, "a", "a", null), "CDATA", "1");
        attrs.addAttribute(new QName(null, "b", "b", null), "CDATA", "2");
        attrs.addAttribute(new QName(null, "c", "c", null), "CDATA", "3");

        attrs.removeAttributeAt(1);

        assertEquals(2, attrs.getLength());
        assertEquals("a", attrs.getQName(0));
        assertEquals("c", attrs.getQName(1));
    }

    // ---- setters ----

    @Test
    public void setValueOverwrites() {
        final XMLAttributesImpl attrs = new XMLAttributesImpl();
        attrs.addAttribute(new QName(null, "x", "x", null), "CDATA", "old");
        attrs.setValue(0, "new");

        assertEquals("new", attrs.getValue(0));
    }

    @Test
    public void setNameOverwrites() {
        final XMLAttributesImpl attrs = new XMLAttributesImpl();
        attrs.addAttribute(new QName(null, "old", "old", null), "CDATA", "val");

        attrs.setName(0, new QName(null, "fresh", "fresh", null));
        assertEquals("fresh", attrs.getQName(0));
    }

    @Test
    public void setSpecified() {
        final XMLAttributesImpl attrs = new XMLAttributesImpl();
        attrs.addAttribute(new QName(null, "a", "a", null), "CDATA", "v");

        assertFalse(attrs.isSpecified(0));
        attrs.setSpecified(0, true);
        assertTrue(attrs.isSpecified(0));
    }

    // ---- getName into output QName ----

    @Test
    public void getNameCopiesIntoTarget() {
        final XMLAttributesImpl attrs = new XMLAttributesImpl();
        attrs.addAttribute(new QName("p", "local", "p:local", "urn:test"), "CDATA", "v", true);

        final QName target = new QName();
        attrs.getName(0, target);

        assertEquals("p", target.getPrefix());
        assertEquals("local", target.getLocalpart());
        assertEquals("p:local", target.getRawname());
        assertEquals("urn:test", target.getUri());
    }

    // ---- out-of-range returns null, not exception ----

    @Test
    public void outOfRangeReturnsNull() {
        final XMLAttributesImpl attrs = new XMLAttributesImpl();

        assertNull(attrs.getType(-1));
        assertNull(attrs.getType(0));
        assertNull(attrs.getValue(-1));
        assertNull(attrs.getValue(0));
        assertNull(attrs.getLocalName(-1));
        assertNull(attrs.getQName(-1));
        assertNull(attrs.getURI(-1));
        assertNull(attrs.getNonNormalizedValue(-1));
        assertNull(attrs.getNameRawName(-1));
    }

    // ---- getType returns NMTOKEN for enumerated ----

    @Test
    public void getTypeReturnsNMTOKENForEnumerated() {
        final XMLAttributesImpl attrs = new XMLAttributesImpl();
        attrs.addAttribute(new QName(null, "a", "a", null), "(yes|no)", "yes");

        assertEquals("NMTOKEN", attrs.getType(0));
        assertEquals("NMTOKEN", attrs.getType("a"));
    }

    // ---- getType with namespace ----

    @Test
    public void getTypeByNamespace() {
        final XMLAttributesImpl attrs = new XMLAttributesImpl();
        attrs.addAttribute(new QName(null, "a", "a", "urn:x"), "ID", "x", true);

        assertEquals("ID", attrs.getType("urn:x", "a"));
        assertNull(attrs.getType("urn:wrong", "a"));
    }

    // ---- getURI ----

    @Test
    public void getURIReturnsUri() {
        final XMLAttributesImpl attrs = new XMLAttributesImpl();
        attrs.addAttribute(new QName(null, "a", "a", "urn:ns"), "CDATA", "v", true);

        assertEquals("urn:ns", attrs.getURI(0));
    }

    // ---- nonNormalized: Attribute vs AttributeExt ----

    @Test
    public void nonNormalizedValueFallsBackToValueForRegularAttribute() {
        final XMLAttributesImpl attrs = new XMLAttributesImpl();
        attrs.addAttribute(new QName(null, "a", "a", null), "CDATA", "val", true);

        // regular Attribute: getNonNormalizedValue returns value_
        assertEquals("val", attrs.getNonNormalizedValue(0));
    }

    @Test
    public void nonNormalizedValueReturnsSeparateValueForAttributeExt() {
        final XMLAttributesImpl attrs = new XMLAttributesImpl();
        attrs.addAttribute(new QName(null, "a", "a", null), "CDATA", "norm", "  raw  ", true);

        assertEquals("  raw  ", attrs.getNonNormalizedValue(0));
        assertEquals("norm", attrs.getValue(0));
    }

    // ---- copy constructor ----

    @Test
    public void copyConstructorDeepCopies() {
        final XMLAttributesImpl original = new XMLAttributesImpl();
        original.addAttribute(new QName(null, "a", "a", null), "CDATA", "val", true);

        final XMLAttributesImpl copy = new XMLAttributesImpl(original);

        assertEquals(1, copy.getLength());
        assertEquals("val", copy.getValue(0));

        // mutating copy does not affect original
        copy.setValue(0, "changed");
        assertEquals("val", original.getValue(0));
        assertEquals("changed", copy.getValue(0));
    }

    @Test
    public void copyConstructorWithEmptySource() {
        final XMLAttributesImpl original = new XMLAttributesImpl();
        final XMLAttributesImpl copy = new XMLAttributesImpl(original);

        assertEquals(0, copy.getLength());
    }

    // ---- Attribute.clone() ----

    @Test
    public void attributeCloneIsIndependent() {
        final Attribute attr = new Attribute();
        attr.getQName().setValues(null, "a", "a", null);
        attr.value_ = "v";
        attr.type_ = "CDATA";
        attr.specified_ = true;

        final Attribute cloned = attr.clone();

        assertNotSame(attr, cloned);
        assertNotSame(attr.getQName(), cloned.getQName());
        assertEquals("a", cloned.getQName().getRawname());
        assertEquals("v", cloned.getValue());

        // mutating clone doesn't affect original
        cloned.value_ = "changed";
        assertEquals("v", attr.value_);
    }

    // ---- Attributes2 isDeclared always false ----

    @Test
    public void isDeclaredAlwaysReturnsFalse() {
        final XMLAttributesImpl attrs = new XMLAttributesImpl();
        attrs.addAttribute(new QName(null, "a", "a", null), "CDATA", "v");

        assertFalse(attrs.isDeclared(0));
        assertFalse(attrs.isDeclared("a"));
        assertFalse(attrs.isDeclared(null, "a"));
    }

    // ---- isSpecified by name throws for unknown ----

    @Test
    public void isSpecifiedByQNameThrowsForUnknown() {
        final XMLAttributesImpl attrs = new XMLAttributesImpl();
        assertThrows(IllegalArgumentException.class, () -> attrs.isSpecified("missing"));
    }

    @Test
    public void isSpecifiedByNamespaceThrowsForUnknown() {
        final XMLAttributesImpl attrs = new XMLAttributesImpl();
        assertThrows(IllegalArgumentException.class, () -> attrs.isSpecified("urn:x", "missing"));
    }

    // ---- getAttributes exposes internal list ----

    @Test
    public void getAttributesReturnsMutableList() {
        final XMLAttributesImpl attrs = new XMLAttributesImpl();
        attrs.addAttribute(new QName(null, "a", "a", null), "CDATA", "v");

        assertNotNull(attrs.getAttributes());
        assertEquals(1, attrs.getAttributes().size());
    }

    // ---- getQName returns "" for null rawname ----

    @Test
    public void getQNameReturnsEmptyStringForNullRawname() {
        final XMLAttributesImpl attrs = new XMLAttributesImpl();
        attrs.addAttribute(new QName(null, "local", null, null), "CDATA", "v", true);

        assertEquals("", attrs.getQName(0));
    }
}