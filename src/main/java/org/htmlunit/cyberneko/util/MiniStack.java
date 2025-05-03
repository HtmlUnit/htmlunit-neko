/*
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

import java.util.Arrays;

/**
 * Extremely light stack implementation. Perfect for inlining.
 *
 * @author Ren&eacute; Schwietzke
 * @since 3.10.0
 */
public class MiniStack<E> {
    // our data
    private Object[] elements_;

    // our current position in the stack
    private int pos_ = -1;

    /**
     * Create a new empty stack with 8 elements capacity.
     *
     * <p>A reference are typically (less than 32 GB memory)
     * 4 bytes. The overhead of any object is about 24 bytes,
     * give or take, hence 8*4 + 24 fits a cache line of 64 bytes.</p>
     */
    public MiniStack() {
        elements_ = new Object[8];
    }

    /**
     * Empties the stack. It will not only reset the pointer but
     * empty out the backing array to ensure we are not holding
     * old references.
     */
    public void clear() {
        pos_ = -1;

        // drop all references to ensure GC
        // keeping would be more efficient but
        // also has sideeffects.
        Arrays.fill(this.elements_, null);
    }

    /**
     * Resets the stack to zero but does not
     * clean it. Use with caution to avoid holding
     * old objects and preventing them from GC.
     */
    public void reset() {
        pos_ = -1;
    }

    /**
     * Removes and returns the last element.
     *
     * @return the last element or null of none
     */
    @SuppressWarnings("unchecked")
    public E pop() {
        if (pos_ >= 0) {
            final E e = (E) this.elements_[pos_];
            // ensure ref is clean to allow GC
            this.elements_[pos_] = null;
            this.pos_--;

            return e;
        }
        return null;
    }

    /**
     * Returns the top/last element without removing
     * it. Will return null if we don't have a last
     * element.
     *
     * @return the top/last element if any, null otherwise
     */
    @SuppressWarnings("unchecked")
    public E peek() {
        return pos_ >= 0 ? (E) this.elements_[pos_] :  null;
    }

    /**
     * Add a new element on top of the stack. You can add null
     * but the return value of pop and peek will become ambiguous.
     *
     * @param element the element to add
     */
    public void push(final E element) {
        pos_++;

        // check if we need room, grow by about a cache line
        if (pos_ == this.elements_.length) {
            this.elements_ = Arrays.copyOf(this.elements_, this.elements_.length + 8);
        }

        this.elements_[pos_] = element;
    }

    /**
     * Checks if we have any element at all on the stack.
     *
     * @return true if the stack holds elements, false otherwise
     */
    public boolean isEmpty() {
        return pos_ < 0;
    }

    /**
     * Returns the current size of the stack.
     *
     * @return the size of the stack, always &gt;= 0
     */
    public int size() {
        return pos_ + 1;
    }
}
