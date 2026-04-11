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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.htmlunit.cyberneko.xerces.util.XMLAttributesImpl.EmptyXMLAttributesImpl;
import org.htmlunit.cyberneko.xerces.xni.QName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link EmptyXMLAttributesImpl}.
 */
public class EmptyXMLAttributesImplTest {

    private final EmptyXMLAttributesImpl empty = EmptyXMLAttributesImpl.INSTANCE;

    // ---- singleton ----

    @Test
    public void singletonInstance() {
        assertSame(EmptyXMLAttributesImpl.INSTANCE, EmptyXMLAttributesImpl.INSTANCE);
    }

    // ---- read-only queries on empty instance ----

    @Test
    public void lengthIsZero() {
        assertEquals(0, empty.getLength());
    }

    @Test
    public void gettersReturnNullOrNegativeOne() {
        assertNull(empty.getType(0));
        assertNull(empty.getValue(0));
        assertNull(empty.getLocalName(0));
        assertNull(empty.getQName(0));
        assertNull(empty.getURI(0));
        assertNull(empty.getNonNormalizedValue(0));
        assertNull(empty.getNameRawName(0));

        assertEquals(-1, empty.getIndex("anything"));
        assertEquals(-1, empty.getIndex("urn:x", "anything"));

        assertNull(empty.getType("anything"));
        assertNull(empty.getValue("anything"));
        assertNull(empty.getType("urn:x", "anything"));
        assertNull(empty.getValue("urn:x", "anything"));
    }

    // ---- all mutators must throw ----

    @Test
    public void addAttribute3ArgThrows() {
        assertThrows(UnsupportedOperationException.class,
                () -> empty.addAttribute(new QName(), "CDATA", "v"));
    }

    @Test
    public void addAttribute4ArgThrows() {
        // This test catches bug 1.5: the 4-arg overload was missing.
        // If the override is not present, this test will PASS silently
        // (no exception) — revealing the bug.
        assertThrows(UnsupportedOperationException.class,
                () -> empty.addAttribute(new QName(), "CDATA", "v", true));
    }

    @Test
    public void addAttribute5ArgThrows() {
        // This test catches bug 1.5: the 5-arg overload was missing.
        assertThrows(UnsupportedOperationException.class,
                () -> empty.addAttribute(new QName(), "CDATA", "v", "raw", true));
    }

    @Test
    public void removeAllAttributesThrows() {
        assertThrows(UnsupportedOperationException.class, () -> empty.removeAllAttributes());
    }

    @Test
    public void removeAttributeAtThrows() {
        assertThrows(UnsupportedOperationException.class, () -> empty.removeAttributeAt(0));
    }

    @Test
    public void setNameThrows() {
        assertThrows(UnsupportedOperationException.class, () -> empty.setName(0, new QName()));
    }

    @Test
    public void setValueThrows() {
        assertThrows(UnsupportedOperationException.class, () -> empty.setValue(0, "v"));
    }

    @Test
    public void setSpecifiedThrows() {
        assertThrows(UnsupportedOperationException.class, () -> empty.setSpecified(0, true));
    }

    // ---- verify instance is still empty after failed mutations ----

    @Test
    public void instanceRemainsEmptyAfterFailedMutation() {
        try {
            empty.addAttribute(new QName(), "CDATA", "v");
        }
        catch (final UnsupportedOperationException ignored) {
            // expected
        }
        assertEquals(0, empty.getLength());
    }
}