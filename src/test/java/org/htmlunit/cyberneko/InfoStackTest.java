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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.htmlunit.cyberneko.HTMLTagBalancer.Info;
import org.htmlunit.cyberneko.HTMLTagBalancer.InfoStack;
import org.htmlunit.cyberneko.xerces.xni.QName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link InfoStack}.
 */
public class InfoStackTest {

    private static Info newInfo(final String name) {
        final HTMLElements elements = new HTMLElements();
        final QName qname = new QName(null, name, name, null);
        return new Info(elements.getElement(name), qname);
    }

    // ---- basic push / peek / pop ----

    @Test
    public void pushPeekPop() {
        final InfoStack stack = new InfoStack(4);
        final Info info = newInfo("div");

        stack.push(info);

        assertEquals(1, stack.length);
        assertSame(info, stack.peek());
        assertSame(info, stack.pop());
        assertEquals(0, stack.length);
    }

    @Test
    public void lifoOrder() {
        final InfoStack stack = new InfoStack(4);
        final Info a = newInfo("div");
        final Info b = newInfo("span");
        final Info c = newInfo("p");

        stack.push(a);
        stack.push(b);
        stack.push(c);

        assertSame(c, stack.pop());
        assertSame(b, stack.pop());
        assertSame(a, stack.pop());
    }

    // ---- growth beyond initial capacity ----

    @Test
    public void pushBeyondInitialCapacityTriggersGrowth() {
        final InfoStack stack = new InfoStack(2);

        for (int i = 0; i < 50; i++) {
            stack.push(newInfo("div"));
        }

        assertEquals(50, stack.length);

        // verify we can pop all 50
        for (int i = 0; i < 50; i++) {
            assertNotNull(stack.pop());
        }
        assertEquals(0, stack.length);
    }

    // ---- clear ----

    @Test
    public void clearResetsLengthAndNullsReferences() {
        final InfoStack stack = new InfoStack(4);
        stack.push(newInfo("div"));
        stack.push(newInfo("span"));

        stack.clear();

        assertEquals(0, stack.length);
        // verify references are nulled out to allow GC
        assertNull(stack.data[0]);
        assertNull(stack.data[1]);
    }

    // ---- pop on empty (documents current behavior) ----

    @Test
    public void popOnEmptyThrowsArrayIndexOutOfBounds() {
        final InfoStack stack = new InfoStack(4);
        assertThrows(ArrayIndexOutOfBoundsException.class, stack::pop);
    }

    @Test
    public void popNullsVacatedSlot() {
        // This test documents bug 1.1: after pop(), data[length] still
        // holds a reference. If the bug is fixed, change assertNotNull
        // to assertNull.
        final InfoStack stack = new InfoStack(4);
        final Info info = newInfo("div");
        stack.push(info);

        stack.pop();

        assertNull(stack.data[0]);
    }

    // ---- toString ----

    @Test
    public void toStringEmpty() {
        final InfoStack stack = new InfoStack(4);
        assertEquals("InfoStack()", stack.toString());
    }

    @Test
    public void toStringNonEmpty() {
        final InfoStack stack = new InfoStack(4);
        stack.push(newInfo("div"));
        final String s = stack.toString();
        assertNotNull(s);
        // just verify it doesn't throw and contains the wrapper
        assertEquals("InfoStack(", s.substring(0, 10));
    }
}