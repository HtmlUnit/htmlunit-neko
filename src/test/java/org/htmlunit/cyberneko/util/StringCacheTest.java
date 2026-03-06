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
package org.htmlunit.cyberneko.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link StringCache}.
 *
 * @author Ronald Brill
 */
public class StringCacheTest {

    // ================================================================
    // StringCache.get()
    // ================================================================

    @Test
    public void getMiss() {
        final StringCache cache = new StringCache();
        final String result = cache.get("div".toCharArray(), 0, 3);
        assertEquals("div", result);
    }

    @Test
    public void getHit() {
        final StringCache cache = new StringCache();

        final String first = cache.get("div".toCharArray(), 0, 3);
        final String second = cache.get("div".toCharArray(), 0, 3);

        assertEquals("div", first);
        assertSame(first, second);
    }

    @Test
    public void getHitFromDifferentBuffer() {
        final StringCache cache = new StringCache();

        // first call caches it
        final String first = cache.get("div".toCharArray(), 0, 3);

        // second call uses a completely different char array with same content
        final char[] otherBuffer = "div".toCharArray();
        final String second = cache.get(otherBuffer, 0, 3);

        assertEquals("div", first);
        assertSame(first, second);
    }

    @Test
    public void getWithOffset() {
        final StringCache cache = new StringCache();

        // name is in the middle of a larger buffer, simulating fCurrentEntity.buffer_
        final char[] buffer = "<div class=\"x\">".toCharArray();
        // "div" is at offset 1, length 3
        final String result = cache.get(buffer, 1, 3);
        assertEquals("div", result);
    }

    @Test
    public void getWithOffsetHit() {
        final StringCache cache = new StringCache();

        // cache from one buffer position
        final char[] buffer1 = "<div>".toCharArray();
        final String first = cache.get(buffer1, 1, 3);

        // hit from a different buffer at a different offset
        final char[] buffer2 = "xxdivyy".toCharArray();
        final String second = cache.get(buffer2, 2, 3);

        assertEquals("div", first);
        assertSame(first, second);
    }

    @Test
    public void getDistinctNames() {
        final StringCache cache = new StringCache();

        final String div = cache.get("div".toCharArray(), 0, 3);
        final String span = cache.get("span".toCharArray(), 0, 4);
        final String a = cache.get("a".toCharArray(), 0, 1);

        assertEquals("div", div);
        assertEquals("span", span);
        assertEquals("a", a);
        assertNotSame(div, span);
        assertNotSame(div, a);
        assertNotSame(span, a);
    }

    @Test
    public void getSingleChar() {
        final StringCache cache = new StringCache();

        final String first = cache.get("a".toCharArray(), 0, 1);
        final String second = cache.get("a".toCharArray(), 0, 1);

        assertEquals("a", first);
        assertSame(first, second);
    }

    @Test
    public void getEmptyString() {
        final StringCache cache = new StringCache();

        final String first = cache.get("abc".toCharArray(), 0, 0);
        final String second = cache.get("xyz".toCharArray(), 1, 0);

        assertEquals("", first);
        assertSame(first, second);
    }

    @Test
    public void getManyEntries() {
        final StringCache cache = new StringCache();

        // simulate many distinct HTML tag/attribute names
        final String[] names = {
            "div", "span", "a", "p", "li", "ul", "ol", "img", "input",
            "table", "tr", "td", "th", "form", "label", "button", "select",
            "option", "textarea", "h1", "h2", "h3", "h4", "h5", "h6",
            "class", "id", "style", "href", "src", "type", "name", "value"
        };

        // first pass: populate cache
        final String[] firstResults = new String[names.length];
        for (int i = 0; i < names.length; i++) {
            firstResults[i] = cache.get(names[i].toCharArray(), 0, names[i].length());
            assertEquals(names[i], firstResults[i]);
        }

        // second pass: all should be cache hits returning same instances
        for (int i = 0; i < names.length; i++) {
            final String second = cache.get(names[i].toCharArray(), 0, names[i].length());
            assertSame(firstResults[i], second);
        }
    }

    @Test
    public void getBufferMutationAfterCacheDoesNotCorruptEntry() {
        final StringCache cache = new StringCache();

        final char[] buffer = "div".toCharArray();
        final String first = cache.get(buffer, 0, 3);
        assertEquals("div", first);

        // mutate the original buffer
        buffer[0] = 'x';
        buffer[1] = 'x';
        buffer[2] = 'x';

        // cached string should still be "div"
        final String second = cache.get("div".toCharArray(), 0, 3);
        assertSame(first, second);
        assertEquals("div", second);
    }

    @Test
    public void getSamePrefixDifferentLength() {
        final StringCache cache = new StringCache();

        final char[] buffer = "class".toCharArray();
        final String cls = cache.get(buffer, 0, 5);
        final String cla = cache.get(buffer, 0, 3);

        assertEquals("class", cls);
        assertEquals("cla", cla);
        assertNotSame(cls, cla);
    }

    // ================================================================
    // CharBufferKey.update() / hashCode()
    // ================================================================

    @Test
    public void keyHashCodeConsistentWithContent() {
        final StringCache.CharBufferKey key = new StringCache.CharBufferKey();

        key.update("div".toCharArray(), 0, 3);
        final int hash1 = key.hashCode();

        // same content from different array
        key.update("div".toCharArray(), 0, 3);
        final int hash2 = key.hashCode();

        assertEquals(hash1, hash2);
    }

    @Test
    public void keyHashCodeWithOffset() {
        final StringCache.CharBufferKey key1 = new StringCache.CharBufferKey();
        final StringCache.CharBufferKey key2 = new StringCache.CharBufferKey();

        key1.update("div".toCharArray(), 0, 3);
        key2.update("xxdivyy".toCharArray(), 2, 3);

        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    public void keyHashCodeDifferentContent() {
        final StringCache.CharBufferKey key = new StringCache.CharBufferKey();

        key.update("div".toCharArray(), 0, 3);
        final int hash1 = key.hashCode();

        key.update("span".toCharArray(), 0, 4);
        final int hash2 = key.hashCode();

        // could theoretically collide, but very unlikely for these strings
        assertNotEquals(hash1, hash2);
    }

    @Test
    public void keyHashCodeEmptyLength() {
        final StringCache.CharBufferKey key = new StringCache.CharBufferKey();
        key.update("abc".toCharArray(), 0, 0);
        assertEquals(0, key.hashCode());
    }

    // ================================================================
    // CharBufferKey.equals()
    // ================================================================

    @Test
    public void keyEqualsSameContent() {
        final StringCache.CharBufferKey key1 = new StringCache.CharBufferKey();
        final StringCache.CharBufferKey key2 = new StringCache.CharBufferKey();

        key1.update("div".toCharArray(), 0, 3);
        key2.update("div".toCharArray(), 0, 3);

        assertTrue(key1.equals(key2));
        assertTrue(key2.equals(key1));
    }

    @Test
    public void keyEqualsDifferentOffsets() {
        final StringCache.CharBufferKey key1 = new StringCache.CharBufferKey();
        final StringCache.CharBufferKey key2 = new StringCache.CharBufferKey();

        key1.update("div".toCharArray(), 0, 3);
        key2.update("xxdivyy".toCharArray(), 2, 3);

        assertTrue(key1.equals(key2));
        assertTrue(key2.equals(key1));
    }

    @Test
    public void keyEqualsDetachedVsLive() {
        final StringCache.CharBufferKey live = new StringCache.CharBufferKey();
        live.update("span".toCharArray(), 0, 4);

        final StringCache.CharBufferKey detached = live.detach();

        // update live to point at different content
        live.update("span".toCharArray(), 0, 4);

        assertTrue(live.equals(detached));
        assertTrue(detached.equals(live));
    }

    @Test
    public void keyNotEqualsDifferentContent() {
        final StringCache.CharBufferKey key1 = new StringCache.CharBufferKey();
        final StringCache.CharBufferKey key2 = new StringCache.CharBufferKey();

        key1.update("div".toCharArray(), 0, 3);
        key2.update("dix".toCharArray(), 0, 3);

        assertFalse(key1.equals(key2));
        assertFalse(key2.equals(key1));
    }

    @Test
    public void keyNotEqualsDifferentLength() {
        final StringCache.CharBufferKey key1 = new StringCache.CharBufferKey();
        final StringCache.CharBufferKey key2 = new StringCache.CharBufferKey();

        key1.update("div".toCharArray(), 0, 3);
        key2.update("di".toCharArray(), 0, 2);

        assertFalse(key1.equals(key2));
        assertFalse(key2.equals(key1));
    }

    @Test
    public void keyNotEqualsNull() {
        final StringCache.CharBufferKey key = new StringCache.CharBufferKey();
        key.update("div".toCharArray(), 0, 3);

        assertFalse(key.equals(null));
    }

    @Test
    public void keyNotEqualsWrongType() {
        final StringCache.CharBufferKey key = new StringCache.CharBufferKey();
        key.update("div".toCharArray(), 0, 3);

        assertFalse(key.equals("div"));
        assertFalse(key.equals(Integer.valueOf(42)));
    }

    @Test
    public void keyEqualsReflexive() {
        final StringCache.CharBufferKey key = new StringCache.CharBufferKey();
        key.update("div".toCharArray(), 0, 3);

        assertTrue(key.equals(key));
    }

    @Test
    public void keyEqualsEmpty() {
        final StringCache.CharBufferKey key1 = new StringCache.CharBufferKey();
        final StringCache.CharBufferKey key2 = new StringCache.CharBufferKey();

        key1.update("abc".toCharArray(), 0, 0);
        key2.update("xyz".toCharArray(), 1, 0);

        assertTrue(key1.equals(key2));
    }

    // ================================================================
    // CharBufferKey.detach()
    // ================================================================

    @Test
    public void detachCreatesIndependentCopy() {
        final StringCache.CharBufferKey original = new StringCache.CharBufferKey();
        final char[] buffer = "div".toCharArray();
        original.update(buffer, 0, 3);

        final StringCache.CharBufferKey detached = original.detach();

        // they should be equal
        assertTrue(original.equals(detached));
        assertEquals(original.hashCode(), detached.hashCode());

        // but detach should survive buffer mutation
        buffer[0] = 'z';
        buffer[1] = 'z';
        buffer[2] = 'z';

        // detached still has the original content
        assertEquals("div", detached.toString());
    }

    @Test
    public void detachWithOffset() {
        final StringCache.CharBufferKey original = new StringCache.CharBufferKey();
        original.update("xxdivyy".toCharArray(), 2, 3);

        final StringCache.CharBufferKey detached = original.detach();

        assertEquals("div", detached.toString());
        assertEquals(original.hashCode(), detached.hashCode());
        assertTrue(original.equals(detached));
    }

    @Test
    public void detachPreservesHash() {
        final StringCache.CharBufferKey original = new StringCache.CharBufferKey();
        original.update("span".toCharArray(), 0, 4);

        final int originalHash = original.hashCode();
        final StringCache.CharBufferKey detached = original.detach();

        assertEquals(originalHash, detached.hashCode());
    }

    @Test
    public void detachIsSeparateInstance() {
        final StringCache.CharBufferKey original = new StringCache.CharBufferKey();
        original.update("div".toCharArray(), 0, 3);

        final StringCache.CharBufferKey detached = original.detach();
        assertNotSame(original, detached);
    }

    // ================================================================
    // CharBufferKey.toString()
    // ================================================================

    @Test
    public void keyToString() {
        final StringCache.CharBufferKey key = new StringCache.CharBufferKey();
        key.update("hello world".toCharArray(), 6, 5);

        assertEquals("world", key.toString());
    }

    @Test
    public void keyToStringFull() {
        final StringCache.CharBufferKey key = new StringCache.CharBufferKey();
        key.update("div".toCharArray(), 0, 3);

        assertEquals("div", key.toString());
    }

    @Test
    public void keyToStringEmpty() {
        final StringCache.CharBufferKey key = new StringCache.CharBufferKey();
        key.update("abc".toCharArray(), 1, 0);

        assertEquals("", key.toString());
    }

    @Test
    public void keyToStringDetached() {
        final StringCache.CharBufferKey key = new StringCache.CharBufferKey();
        key.update("xxspanyy".toCharArray(), 2, 4);

        final StringCache.CharBufferKey detached = key.detach();
        assertEquals("span", detached.toString());
    }

    // ================================================================
    // CharBufferKey.update() reuse
    // ================================================================

    @Test
    public void keyUpdateOverwritesPrevious() {
        final StringCache.CharBufferKey key = new StringCache.CharBufferKey();

        key.update("div".toCharArray(), 0, 3);
        assertEquals("div", key.toString());

        key.update("span".toCharArray(), 0, 4);
        assertEquals("span", key.toString());

        // hash should reflect the new content
        final StringCache.CharBufferKey other = new StringCache.CharBufferKey();
        other.update("span".toCharArray(), 0, 4);
        assertEquals(other.hashCode(), key.hashCode());
        assertTrue(key.equals(other));
    }
}