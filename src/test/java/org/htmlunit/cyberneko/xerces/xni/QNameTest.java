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
package org.htmlunit.cyberneko.xerces.xni;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link QName}.
 */
public class QNameTest {

    // ---- default constructor ----

    @Test
    public void defaultConstructorAllNull() {
        final QName q = new QName();
        assertNull(q.getPrefix());
        assertNull(q.getLocalpart());
        assertNull(q.getRawname());
        assertNull(q.getUri());
    }

    // ---- 4-arg constructor ----

    @Test
    public void fourArgConstructor() {
        final QName q = new QName("a", "foo", "a:foo", "urn:test");
        assertEquals("a", q.getPrefix());
        assertEquals("foo", q.getLocalpart());
        assertEquals("a:foo", q.getRawname());
        assertEquals("urn:test", q.getUri());
    }

    // ---- copy constructor ----

    @Test
    public void copyConstructor() {
        final QName original = new QName("p", "local", "p:local", "urn:ns");
        final QName copy = new QName(original);

        assertEquals("p", copy.getPrefix());
        assertEquals("local", copy.getLocalpart());
        assertEquals("p:local", copy.getRawname());
        assertEquals("urn:ns", copy.getUri());

        // verify independence: mutating original doesn't affect copy
        original.setPrefix("x");
        original.setRawname("x:local");
        original.setUri("urn:other");
        assertEquals("p", copy.getPrefix());
        assertEquals("p:local", copy.getRawname());
        assertEquals("urn:ns", copy.getUri());
    }

    // ---- setValues(QName) ----

    @Test
    public void setValuesFromQName() {
        final QName source = new QName("a", "b", "a:b", "urn:x");
        final QName target = new QName();

        target.setValues(source);

        assertEquals("a", target.getPrefix());
        assertEquals("b", target.getLocalpart());
        assertEquals("a:b", target.getRawname());
        assertEquals("urn:x", target.getUri());
    }

    // ---- setValues(4 strings) ----

    @Test
    public void setValuesFromStrings() {
        final QName q = new QName();
        q.setValues("p", "l", "p:l", "urn:ns");

        assertEquals("p", q.getPrefix());
        assertEquals("l", q.getLocalpart());
        assertEquals("p:l", q.getRawname());
        assertEquals("urn:ns", q.getUri());
    }

    // ---- individual setters ----

    @Test
    public void individualSetters() {
        final QName q = new QName();
        q.setPrefix("pre");
        q.setRawname("pre:name");
        q.setUri("urn:u");

        assertEquals("pre", q.getPrefix());
        assertEquals("pre:name", q.getRawname());
        assertEquals("urn:u", q.getUri());
    }

    // ---- splitQName ----

    @Test
    public void splitQNameWithPrefix() {
        final QName q = new QName(null, null, "ns:local", null);
        q.splitQName();

        assertEquals("ns", q.getPrefix());
        assertEquals("local", q.getLocalpart());
    }

    @Test
    public void splitQNameWithoutPrefix() {
        final QName q = new QName(null, null, "noprefix", null);
        q.splitQName();

        // no colon: prefix and localpart should remain unchanged
        assertNull(q.getPrefix());
        assertNull(q.getLocalpart());
    }

    @Test
    public void splitQNameReturnsSelf() {
        final QName q = new QName(null, null, "a:b", null);
        final QName result = q.splitQName();
        assertNotNull(result);
        assertEquals(q, result);
    }

    // ---- clone ----

    @Test
    public void cloneProducesEqualButDistinctInstance() {
        final QName q = new QName("a", "b", "a:b", "urn:x");
        final Object c = q.clone();

        assertNotSame(q, c);
        assertTrue(c instanceof QName);
        assertEquals(q, c);
    }

    // ---- equals ----

    @Test
    public void equalsWithUri() {
        // When URI is present, equals compares uri + localpart (identity for localpart)
        final String localpart = "name";
        final QName a = new QName(null, localpart, "name", "urn:x");
        final QName b = new QName(null, localpart, "name", "urn:x");

        // same localpart reference + same URI value
        assertTrue(a.equals(b));
    }

    @Test
    public void equalsWithDifferentUri() {
        final String localpart = "name";
        final QName a = new QName(null, localpart, "name", "urn:x");
        final QName b = new QName(null, localpart, "name", "urn:y");

        assertFalse(a.equals(b));
    }

    @Test
    public void equalsWithoutUri() {
        // Without URI, equals uses rawname identity (==)
        final String rawname = "div";
        final QName a = new QName(null, null, rawname, null);
        final QName b = new QName(null, null, rawname, null);

        // same String reference
        assertTrue(a.equals(b));
    }

    @Test
    public void equalsWithDifferentRawnameReferences() {
        // NOTE: This test documents that equals() uses == for rawname,
        // not .equals(). Two QNames with equal-but-distinct rawname
        // Strings are NOT considered equal when URI is null.
        final QName a = new QName(null, null, new String("div"), null);
        final QName b = new QName(null, null, new String("div"), null);

        assertFalse(a.equals(b));
    }

    @Test
    public void equalsWithNonQNameReturnsFalse() {
        final QName q = new QName(null, null, "div", null);
        assertFalse(q.equals("div"));
        assertFalse(q.equals(null));
    }

    @Test
    public void equalsWhenOneHasUriOtherDoesNot() {
        final QName withUri = new QName(null, "x", "x", "urn:x");
        final QName withoutUri = new QName(null, "x", "x", null);

        // a has URI, b doesn't: falls through to false
        assertFalse(withoutUri.equals(withUri));
        // a doesn't have URI, b has URI: a.uri_ == null but qname.uri_ != null
        assertFalse(withUri.equals(withoutUri));
    }

    // ---- hashCode ----

    @Test
    public void hashCodeConsistentWithEqualsForSameReference() {
        final String localpart = "name";
        final QName a = new QName(null, localpart, "name", "urn:x");
        final QName b = new QName(null, localpart, "name", "urn:x");

        if (a.equals(b)) {
            assertEquals(a.hashCode(), b.hashCode());
        }
    }

    @Test
    public void hashCodeWithNullUri() {
        final QName q = new QName(null, null, "div", null);
        // just verify no NPE
        assertEquals("div".hashCode(), q.hashCode());
    }

    @Test
    public void hashCodeWithNullEverything() {
        final QName q = new QName();
        assertEquals(0, q.hashCode());
    }

    // ---- toString ----

    @Test
    public void toStringAllFields() {
        final QName q = new QName("a", "b", "a:b", "urn:x");
        final String s = q.toString();

        assertTrue(s.contains("prefix=\"a\""));
        assertTrue(s.contains("localpart=\"b\""));
        assertTrue(s.contains("rawname=\"a:b\""));
        assertTrue(s.contains("uri=\"urn:x\""));
    }

    @Test
    public void toStringAllNull() {
        final QName q = new QName();
        assertEquals("", q.toString());
    }

    @Test
    public void toStringPartial() {
        final QName q = new QName(null, null, "div", null);
        final String s = q.toString();

        assertTrue(s.contains("rawname=\"div\""));
        assertFalse(s.contains("prefix="));
        assertFalse(s.contains("localpart="));
        assertFalse(s.contains("uri="));
    }
}