/*
 * Copyright (c) 2005-2024 René Schwietzke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.htmlunit.cyberneko.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link FastHashMap}.
 *
 * @author René Schwietzke
 */
public class FastHashMapTest {
    @Test
    public void happyPath() {
        final FastHashMap<String, Integer> f = new FastHashMap<>(3, 0.5f);
        f.put("a", 1);
        f.put("b", 2);
        f.put("c", 3);
        f.put("d", 4);
        f.put("e", 5);

        assertEquals(5, f.size());
        assertEquals(Integer.valueOf(1), f.get("a"));
        assertEquals(Integer.valueOf(4), f.get("d"));
        assertEquals(Integer.valueOf(3), f.get("c"));
        assertEquals(Integer.valueOf(5), f.get("e"));
        assertEquals(Integer.valueOf(2), f.get("b"));

        f.put("b", 20);
        assertEquals(5, f.size());
        assertEquals(Integer.valueOf(1), f.get("a"));
        assertEquals(Integer.valueOf(4), f.get("d"));
        assertEquals(Integer.valueOf(20), f.get("b"));
        assertEquals(Integer.valueOf(3), f.get("c"));
        assertEquals(Integer.valueOf(5), f.get("e"));
    }

    @Test
    public void keys() {
        final FastHashMap<String, Integer> f = new FastHashMap<>(3, 0.5f);
        f.put("aa", 1);
        f.put("bb", 2);
        f.put("cc", 3);
        f.put("dd", 4);
        f.put("ee", 5);

        {
            final List<String> k = f.keys();
            assertEquals(5, k.size());
            assertTrue(k.contains("aa"));
            assertTrue(k.contains("bb"));
            assertTrue(k.contains("cc"));
            assertTrue(k.contains("dd"));
            assertTrue(k.contains("ee"));
        }

        assertEquals(Integer.valueOf(3), f.remove("cc"));
        f.remove("c");
        {
            final List<String> k = f.keys();
            assertEquals(4, k.size());
            assertTrue(k.contains("aa"));
            assertTrue(k.contains("bb"));
            assertTrue(k.contains("dd"));
            assertTrue(k.contains("ee"));
        }

        f.put("zz", 10);
        f.remove("c");
        {
            final List<String> k = f.keys();
            assertEquals(5, k.size());
            assertTrue(k.contains("aa"));
            assertTrue(k.contains("bb"));
            assertTrue(k.contains("dd"));
            assertTrue(k.contains("ee"));
            assertTrue(k.contains("zz"));
        }

        // ask for something unknown
        assertNull(f.get("unknown"));
    }

    @Test
    public void values() {
        final FastHashMap<String, Integer> f = new FastHashMap<>(3, 0.5f);
        f.put("aa", 1);
        f.put("bb", 2);
        f.put("cc", 3);
        f.put("dd", 4);
        f.put("ee", 5);

        {
            final List<Integer> values = f.values();
            assertEquals(5, values.size());
            assertTrue(values.contains(1));
            assertTrue(values.contains(2));
            assertTrue(values.contains(3));
            assertTrue(values.contains(4));
            assertTrue(values.contains(5));
        }

        assertEquals(Integer.valueOf(3), f.remove("cc"));
        f.remove("c");
        {
            final List<Integer> values = f.values();
            assertEquals(4, values.size());
            assertTrue(values.contains(1));
            assertTrue(values.contains(2));
            assertTrue(values.contains(4));
            assertTrue(values.contains(5));
        }
    }

    @Test
    public void remove() {
        final FastHashMap<String, Integer> f = new FastHashMap<>(3, 0.5f);
        f.put("a", 1);
        f.put("b", 2);
        f.put("c", 3);
        f.put("d", 4);
        f.put("e", 5);

        f.remove("b");
        f.remove("d");

        assertEquals(3, f.size());
        assertEquals(Integer.valueOf(1), f.get("a"));
        assertEquals(Integer.valueOf(3), f.get("c"));
        assertEquals(Integer.valueOf(5), f.get("e"));
        assertNull(f.get("d"));
        assertNull(f.get("b"));

        // remove again
        assertNull(f.remove("b"));
        assertNull(f.remove("d"));

        f.put("d", 6);
        f.put("b", 7);
        assertEquals(Integer.valueOf(7), f.get("b"));
        assertEquals(Integer.valueOf(6), f.get("d"));
    }

    @Test
    public void clear() {
        final FastHashMap<String, Integer> m = new FastHashMap<String, Integer>();
        m.put("a", 1);
        assertEquals(1, m.size());

        m.clear();
        assertEquals(0, m.size());
        assertEquals(0, m.keys().size());
        assertEquals(0, m.values().size());
        assertNull(m.get("a"));

        m.put("b", 2);
        assertEquals(1, m.size());
        m.put("a", 3);
        assertEquals(2, m.size());

        m.clear();
        assertEquals(0, m.size());
        assertEquals(0, m.keys().size());
        assertEquals(0, m.values().size());

        m.put("a", 1);
        m.put("b", 2);
        m.put("c", 3);
        m.put("c", 3);
        assertEquals(3, m.size());
        assertEquals(3, m.keys().size());
        assertEquals(3, m.values().size());

        assertEquals(Integer.valueOf(1), m.get("a"));
        assertEquals(Integer.valueOf(2), m.get("b"));
        assertEquals(Integer.valueOf(3), m.get("c"));
    }

    @Test
    public void collision() {
        final FastHashMap<MockKey<String>, String> f = new FastHashMap<MockKey<String>, String>(13, 0.5f);
        IntStream.range(0, 15).forEach(i -> {
            f.put(new MockKey<String>(12, "k" + i), "v" + i);
        });

        assertEquals(15, f.size());

        IntStream.range(0, 15).forEach(i -> {
            assertEquals("v" + i, f.get(new MockKey<String>(12, "k" + i)));
        });

        // round 2
        IntStream.range(0, 20).forEach(i -> {
            f.put(new MockKey<String>(12, "k" + i), "v" + i);
        });

        assertEquals(20, f.size());

        IntStream.range(0, 20).forEach(i -> {
            assertEquals("v" + i, f.get(new MockKey<String>(12, "k" + i)));
        });

        // round 3
        IntStream.range(0, 10).forEach(i -> {
            assertEquals("v" + i, f.remove(new MockKey<String>(12, "k" + i)));
        });
        IntStream.range(10, 20).forEach(i -> {
            assertEquals("v" + i, f.get(new MockKey<String>(12, "k" + i)));
        });
    }

    /**
     * Overflow initial size with collision keys. Some hash code for all keys.
     */
    @Test
    public void overflow() {
        final FastHashMap<MockKey<String>, Integer> m = new FastHashMap<>(5, 0.5f);
        final Map<MockKey<String>, Integer> data = IntStream.range(0, 152)
                .mapToObj(Integer::valueOf)
                .collect(
                        Collectors.toMap(i -> new MockKey<String>(1, "k" + i),
                                i -> i));

        // add all
        data.forEach((k, v) -> m.put(k, v));

        // verify
        data.forEach((k, v) -> assertEquals(v, m.get(k)));
        assertEquals(152, m.size());
        assertEquals(152, m.keys().size());
        assertEquals(152, m.values().size());
    }

    /**
     * Try to test early growth and potential problems when growing. Based on
     * infinite loop observations.
     */
    @Test
    public void growFromSmall_InfiniteLoopIsssue() {
        for (int initSize = 1; initSize < 100; initSize++) {
            final FastHashMap<Integer, Integer> m = new FastHashMap<>(initSize, 0.7f);

            for (int i = 0; i < 300; i++) {
                // add one
                m.put(i, i);

                // ask for all
                for (int j = 0; j <= i; j++) {
                    assertEquals(Integer.valueOf(j), m.get(j));
                }

                // ask for something else
                for (int j = -1; j >= -100; j--) {
                    assertNull(m.get(j));
                }
            }
        }
    }

    /**
     * Test serialization, should work out of the box, just to
     * ensure nobody removes that.
     *
     * @throws IOException in case of error
     * @throws ClassNotFoundException in case of error
     */
    @Test
    public void serializable() throws IOException, ClassNotFoundException {
        final FastHashMap<String, Integer> src = new FastHashMap<>();

        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(buffer)) {
            objectOutputStream.writeObject(src);
            objectOutputStream.close();
        }

        try (ObjectInputStream objectInputStream =
                new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()))) {
            final FastHashMap<String, Integer> copy =
                    (FastHashMap<String, Integer>) objectInputStream.readObject();

            assertEquals(src.size(), copy.size());

            // clone still works
            copy.put("test", 1);
            assertEquals(1, copy.get("test"));
        }

    }

    /**
     * Test serialization, should work out of the box, just to
     * ensure nobody removes that.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Test
    public void serializable_notEmpty() throws IOException, ClassNotFoundException {
        final FastHashMap<String, Integer> src = new FastHashMap<>();
        src.put("a", 1);
        src.put("b", 2);
        src.put("c", 3);
        src.put("d", 4);
        src.remove("b");

        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(buffer)) {
            objectOutputStream.writeObject(src);
            objectOutputStream.close();
        }
        try (ObjectInputStream objectInputStream =
                new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()))) {
            final FastHashMap<String, Integer> copy = (FastHashMap<String, Integer>) objectInputStream.readObject();
            objectInputStream.close();

            assertEquals(src.size(), copy.size());
            assertEquals(1, copy.get("a"));
            assertEquals(3, copy.get("c"));
            assertEquals(4, copy.get("d"));
        }
    }

    /**
     * Try to hit all slots with bad hashcodes.
     */
    @Test
    public void hitEachSlot() {
        final FastHashMap<MockKey<String>, Integer> m = new FastHashMap<>(15, 0.9f);

        final Map<MockKey<String>, Integer> data = IntStream.range(0, 150)
                .mapToObj(Integer::valueOf)
                .collect(
                        Collectors.toMap(i -> new MockKey<String>(i, "k1" + i),
                                i -> i));

        // add the same hash codes again but other keys
        data.putAll(IntStream.range(0, 150)
                .mapToObj(Integer::valueOf)
                .collect(
                        Collectors.toMap(i -> new MockKey<String>(i, "k2" + i),
                                i -> i)));
        // add all
        data.forEach((k, v) -> m.put(k, v));
        // verify
        data.forEach((k, v) -> assertEquals(v, m.get(k)));
        assertEquals(300, m.size());
        assertEquals(300, m.keys().size());
        assertEquals(300, m.values().size());

        // remove all
        data.forEach((k, v) -> m.remove(k));
        // verify
        assertEquals(0, m.size());
        assertEquals(0, m.keys().size());
        assertEquals(0, m.values().size());

        // add all
        final List<MockKey<String>> keys = data.keySet().stream().collect(Collectors.toList());
        keys.stream().sorted().forEach(k -> m.put(k, data.get(k)));
        // put in different order
        Collections.shuffle(keys);
        keys.forEach(k -> m.put(k, data.get(k) + 42));

        // verify
        data.forEach((k, v) -> assertEquals(Integer.valueOf(v + 42), m.get(k)));
        assertEquals(300, m.size());
        assertEquals(300, m.keys().size());
        assertEquals(300, m.values().size());

        // remove in different order
        Collections.shuffle(keys);
        keys.forEach(k -> m.remove(k));

        // verify
        data.forEach((k, v) -> assertNull(m.get(k)));
        assertEquals(0, m.size());
        assertEquals(0, m.keys().size());
        assertEquals(0, m.values().size());
    }

    static class MockKey<T extends Comparable<T>> implements Comparable<MockKey<T>> {
        private final T key_;
        private final int hash_;

        MockKey(final int hash, final T key) {
            hash_ = hash;
            key_ = key;
        }

        @Override
        public int hashCode() {
            return hash_;
        }

        @Override
        public boolean equals(final Object o) {
            final MockKey<T> t = (MockKey<T>) o;
            return hash_ == o.hashCode() && key_.equals(t.key_);
        }

        @Override
        public String toString() {
            return "MockKey [key=" + key_ + ", hash=" + hash_ + "]";
        }

        @Override
        public int compareTo(final MockKey<T> o) {
            return o.key_.compareTo(this.key_);
        }
    }
}

