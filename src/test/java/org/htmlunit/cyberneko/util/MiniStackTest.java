/*
 * Copyright (c) 2017-2024 Ronald Brill
 * Copyright 2023 René Schwietzke
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


/**
 * Unit tests for {@link MiniStack}.
 *
 * @author René Schwietzke
 */
public class MiniStackTest {
    @Test
    public void ctr() {
        final MiniStack<String> m = new MiniStack<>();
        assertEquals(0, m.size());
        assertNull(m.pop());
    }

    @Test
    public void pop() {
        // empty
        {
            final MiniStack<String> m = new MiniStack<>();
            assertNull(m.pop());
        }
        // full
        {
            final MiniStack<String> m = new MiniStack<>();
            m.push("foo");
            assertEquals("foo", m.pop());
            assertNull(m.pop());
            assertEquals(0, m.size());
        }
        // two elements
        {
            final MiniStack<String> m = new MiniStack<>();
            m.push("1");
            m.push("2");
            assertEquals("2", m.pop());
            assertEquals(1, m.size());

            assertEquals("1", m.pop());
            assertEquals(0, m.size());
            assertNull(m.pop());
        }
    }

    @Test
    public void push() {
        final String a = new String("a");

        // empty
        {
            final MiniStack<String> m = new MiniStack<>();
            m.push("a");
            assertEquals(1, m.size());
        }
        // two
        {
            final MiniStack<String> m = new MiniStack<>();
            m.push("foo");
            m.push(a);
            m.push(a);
            assertEquals(3, m.size());
        }
        // grow
        {
            final MiniStack<Integer> m = new MiniStack<>();
            for (int i = 0; i < 20; i++) {
                m.push(i);
                assertEquals(i + 1, m.size());
            }
            assertEquals(20, m.size());
        }
    }

    @Test
    public void peek() {
        // empty
        {
            final MiniStack<String> m = new MiniStack<>();
            assertNull(m.peek());
        }
        // more
        {
            final MiniStack<String> m = new MiniStack<>();
            m.push("foo");
            assertEquals("foo", m.peek());
            m.push("2");
            assertEquals("2", m.peek());
            m.push("1");
            assertEquals("1", m.peek());
        }
    }

    @Test
    public void isEmpty() {
        // empty
        {
            final MiniStack<String> m = new MiniStack<>();
            assertTrue(m.isEmpty());
        }
        // more
        {
            final MiniStack<String> m = new MiniStack<>();
            m.push("foo");
            assertFalse(m.isEmpty());
        }
    }

    @Test
    public void size() {
        // empty
        {
            final MiniStack<String> m = new MiniStack<>();
            assertEquals(0, m.size());
        }
        // one
        {
            final MiniStack<String> m = new MiniStack<>();
            m.push("foo");
            assertEquals(1, m.size());
        }
        // more
        {
            final MiniStack<String> m = new MiniStack<>();
            m.push("foo");
            m.push("foo");
            m.push("foo");
            m.push("foo");
            m.push("foo");
            assertEquals(5, m.size());
        }
    }

    @Test
    public void clear() {
        // empty
        {
            final MiniStack<String> m = new MiniStack<>();
            m.clear();
            assertEquals(0, m.size());
        }
        // one
        {
            final MiniStack<String> m = new MiniStack<>();
            m.push("foo");
            assertEquals(1, m.size());
            m.clear();
            assertEquals(0, m.size());
            assertTrue(m.isEmpty());
            assertNull(m.peek());
            assertNull(m.pop());
        }

        // we might have to open the object for a check of the backing array
    }

    @Test
    public void reset() {
        // empty
        {
            final MiniStack<String> m = new MiniStack<>();
            m.clear();
            assertEquals(0, m.size());
        }
        // one
        {
            final MiniStack<String> m = new MiniStack<>();
            m.push("foo");
            assertEquals(1, m.size());
            m.clear();
            assertEquals(0, m.size());
            assertTrue(m.isEmpty());
            assertNull(m.peek());
            assertNull(m.pop());
        }
    }

}
