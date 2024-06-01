/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.htmlunit.cyberneko.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SimpleArrayList}.
 *
 * @author René Schwietzke
 */
public class SimpleArrayListTest {
    @Test
    public void create() {
        {
            final SimpleArrayList<String> l = new SimpleArrayList<>();
            assertEquals(0, l.size());
        }
    }

    @Test
    public void createSized() {
        {
            final SimpleArrayList<String> l = new SimpleArrayList<>(5);
            assertEquals(0, l.size());
        }
        // empty start size is permitted
        {
            final SimpleArrayList<String> l = new SimpleArrayList<>(0);
            assertEquals(0, l.size());
        }
    }

    @Test
    public void fillZeroSize() {
        final SimpleArrayList<String> l = new SimpleArrayList<>(0);
        l.add("a");
        l.add("b");
        l.add("c");
        assertEquals(3, l.size());
        assertEquals("a", l.get(0));
        assertEquals("b", l.get(1));
        assertEquals("c", l.get(2));
    }

    @Test
    public void fill() {
        final SimpleArrayList<String> l = new SimpleArrayList<>(5);
        l.add("a");
        l.add("b");
        l.add("c");
        l.add("d");
        l.add("e");
        assertEquals(5, l.size());

        assertEquals("a", l.get(0));
        assertEquals("b", l.get(1));
        assertEquals("c", l.get(2));
        assertEquals("d", l.get(3));
        assertEquals("e", l.get(4));

        // no growth
        try {
            l.get(5);
            fail();
        }
        catch (final IndexOutOfBoundsException e) {
            // yeah... expected
        }
    }

    @Test
    public void fillAndGrow() {
        final SimpleArrayList<String> l = new SimpleArrayList<>(2);
        l.add("a");
        l.add("b");
        assertEquals(2, l.size());
        l.add("d"); // became 2 << 1 + 2
        l.add("e");
        assertEquals("d", l.get(2));
        assertEquals("e", l.get(3));
        assertEquals(4, l.size());

        // limited growth
        try {
            l.get(6);
            fail("Position exists");
        }
        catch (final IndexOutOfBoundsException e) {
            // yeah... expected
        }
    }

    @Test
    public void partitionHappy() {
        final SimpleArrayList<Integer> list = new SimpleArrayList<>(1);
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
        list.add(6);

        final List<List<Integer>> result = list.partition(3);
        assertEquals(3, result.size());

        assertEquals(2, result.get(0).size());
        assertEquals(Integer.valueOf(1), result.get(0).get(0));
        assertEquals(Integer.valueOf(2), result.get(0).get(1));

        assertEquals(2, result.get(1).size());
        assertEquals(Integer.valueOf(3), result.get(1).get(0));
        assertEquals(Integer.valueOf(4), result.get(1).get(1));

        assertEquals(2, result.get(2).size());
        assertEquals(Integer.valueOf(5), result.get(2).get(0));
        assertEquals(Integer.valueOf(6), result.get(2).get(1));
    }

    @Test
    public void partitionSize1() {
        final SimpleArrayList<Integer> list = new SimpleArrayList<>(1);
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
        list.add(6);

        final List<List<Integer>> result = list.partition(6);
        assertEquals(6, result.size());

        assertEquals(1, result.get(0).size());
        assertEquals(Integer.valueOf(1), result.get(0).get(0));

        assertEquals(1, result.get(1).size());
        assertEquals(Integer.valueOf(2), result.get(1).get(0));

        assertEquals(1, result.get(2).size());
        assertEquals(Integer.valueOf(3), result.get(2).get(0));

        assertEquals(1, result.get(3).size());
        assertEquals(Integer.valueOf(4), result.get(3).get(0));

        assertEquals(1, result.get(4).size());
        assertEquals(Integer.valueOf(5), result.get(4).get(0));

        assertEquals(1, result.get(5).size());
        assertEquals(Integer.valueOf(6), result.get(5).get(0));
    }

    @Test
    public void partitionEndBucketNotEven() {

        final SimpleArrayList<Integer> list = new SimpleArrayList<>(1);
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);

        final List<List<Integer>> result = list.partition(3);
        assertEquals(3, result.size());

        assertEquals(2, result.get(0).size());
        assertEquals(Integer.valueOf(1), result.get(0).get(0));
        assertEquals(Integer.valueOf(2), result.get(0).get(1));

        assertEquals(2, result.get(1).size());
        assertEquals(Integer.valueOf(3), result.get(1).get(0));
        assertEquals(Integer.valueOf(4), result.get(1).get(1));

        assertEquals(1, result.get(2).size());
        assertEquals(Integer.valueOf(5), result.get(2).get(0));
    }

    @Test
    public void partitionCountTooLarge() {
        final SimpleArrayList<Integer> list = new SimpleArrayList<>(1);
        list.add(1);
        list.add(2);
        list.add(3);

        final List<List<Integer>> result = list.partition(4);
        assertEquals(3, result.size());

        assertEquals(1, result.get(0).size());
        assertEquals(Integer.valueOf(1), result.get(0).get(0));

        assertEquals(1, result.get(1).size());
        assertEquals(Integer.valueOf(2), result.get(1).get(0));

        assertEquals(1, result.get(2).size());
        assertEquals(Integer.valueOf(3), result.get(2).get(0));

    }

    @Test
    public void clear() {
        final SimpleArrayList<String> l = new SimpleArrayList<>(5);
        l.add("a");
        l.add("b");
        l.add("c");
        l.add("d");
        l.add("e");
        assertEquals(5, l.size());

        assertEquals("a", l.get(0));
        assertEquals("b", l.get(1));
        assertEquals("c", l.get(2));
        assertEquals("d", l.get(3));
        assertEquals("e", l.get(4));

        l.clear();

        assertEquals(0, l.size());

        // we are not checking ranges or such... hence this now works!!!
        assertEquals("a", l.get(0));

        l.clear();
        assertEquals(0, l.size());
        l.add("e1");
        assertEquals(1, l.size());
        assertEquals("e1", l.get(0));

    }

    @Test
    public void toArray() {
        final SimpleArrayList<String> l = new SimpleArrayList<>(4);
        l.add("a");
        l.add("b");
        l.add("c");
        l.add("d");
        l.add("e");
        assertEquals(5, l.size());

        // we will get
        {
            final Object[] a = l.toArray();
            assertEquals(5, a.length);
            assertEquals("a", a[0]);
            assertEquals("b", a[1]);
            assertEquals("c", a[2]);
            assertEquals("d", a[3]);
            assertEquals("e", a[4]);
        }
        // we will get
        {
            final String[] a = l.toArray(new String[0]);
            assertEquals(5, a.length);
            assertEquals("a", a[0]);
            assertEquals("b", a[1]);
            assertEquals("c", a[2]);
            assertEquals("d", a[3]);
            assertEquals("e", a[4]);
        }
    }

    @Test
    public void remove() {
        // remove first
        {
            final SimpleArrayList<String> l = new SimpleArrayList<>(10);
            l.add("a");
            l.add("b");
            l.add("c");
            assertEquals(3, l.size());
            l.remove(0);
            assertEquals(2, l.size());
            assertEquals("b", l.get(0));
            assertEquals("c", l.get(1));
        }
        // remove  middle
        {
            final SimpleArrayList<String> l = new SimpleArrayList<>(10);
            l.add("a");
            l.add("b");
            l.add("c");
            assertEquals(3, l.size());
            l.remove(1);
            assertEquals(2, l.size());
            assertEquals("a", l.get(0));
            assertEquals("c", l.get(1));
        }
        // remove end
        {
            final SimpleArrayList<String> l = new SimpleArrayList<>(10);
            l.add("a");
            l.add("b");
            l.add("c");
            assertEquals(3, l.size());
            l.remove(2);
            assertEquals(2, l.size());
            assertEquals("a", l.get(0));
            assertEquals("b", l.get(1));
        }
        // remove last element
        {
            final SimpleArrayList<String> l = new SimpleArrayList<>(10);
            l.add("a");
            l.add("b");
            l.add("c");
            assertEquals(3, l.size());
            l.remove(0);
            l.remove(0);
            l.remove(0);
            assertEquals(0, l.size());
        }
    }

    @Test
    public void addAt() {
        // add to empty and no size
        {
            final SimpleArrayList<String> l = new SimpleArrayList<>(0);
            l.add(0, "a");
            assertEquals(1, l.size());
            assertEquals("a", l.get(0));
        }
        // add to empty
        {
            final SimpleArrayList<String> l = new SimpleArrayList<>();
            l.add(0, "a");
            assertEquals(1, l.size());
            assertEquals("a", l.get(0));
        }
        // add to end
        {
            final SimpleArrayList<String> l = new SimpleArrayList<>();
            l.add("a");
            l.add(1, "b");
            assertEquals(2, l.size());
            assertEquals("a", l.get(0));
            assertEquals("b", l.get(1));
            l.add(2, "c");
            assertEquals(3, l.size());
            assertEquals("a", l.get(0));
            assertEquals("b", l.get(1));
            assertEquals("c", l.get(2));
        }
        // add to start
        {
            final SimpleArrayList<String> l = new SimpleArrayList<>();
            l.add("a");
            l.add(0, "b");
            assertEquals(2, l.size());
            assertEquals("b", l.get(0));
            assertEquals("a", l.get(1));
            l.add(0, "c");
            assertEquals(3, l.size());
            assertEquals("a", l.get(2));
            assertEquals("b", l.get(1));
            assertEquals("c", l.get(0));
        }
        // add middle
        {
            final SimpleArrayList<String> l = new SimpleArrayList<>();
            l.add("a");
            l.add("c");
            l.add(1, "b");
            assertEquals(3, l.size());
            assertEquals("a", l.get(0));
            assertEquals("b", l.get(1));
            assertEquals("c", l.get(2));
        }
        // add and requires growth
        {
            final SimpleArrayList<String> l = new SimpleArrayList<>(2);
            l.add("a");
            l.add("c");
            l.add(1, "b");
            assertEquals(3, l.size());
            assertEquals("a", l.get(0));
            assertEquals("b", l.get(1));
            assertEquals("c", l.get(2));
        }
    }

    @Test
    public void set() {
        // set first
        {
            final SimpleArrayList<String> l = new SimpleArrayList<>(10);
            l.add("a");
            l.add("b");
            l.add("c");
            assertEquals(3, l.size());
            l.set(0, "x");
            assertEquals(3, l.size());
            assertEquals("x", l.get(0));
            assertEquals("b", l.get(1));
            assertEquals("c", l.get(2));
        }
        // set  middle
        {
            final SimpleArrayList<String> l = new SimpleArrayList<>(10);
            l.add("a");
            l.add("b");
            l.add("c");
            assertEquals(3, l.size());
            l.set(1, "x");
            assertEquals(3, l.size());
            assertEquals("a", l.get(0));
            assertEquals("x", l.get(1));
            assertEquals("c", l.get(2));
        }
        // set end
        {
            final SimpleArrayList<String> l = new SimpleArrayList<>(10);
            l.add("a");
            l.add("b");
            l.add("c");
            assertEquals(3, l.size());
            l.set(2, "x");
            assertEquals("a", l.get(0));
            assertEquals("b", l.get(1));
            assertEquals("x", l.get(2));
        }
    }
}
