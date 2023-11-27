package org.htmlunit.cyberneko.util;

import java.util.Arrays;

/**
 * Extremely light stack implementation. Perfect for inlining.
 *
 */
public class MiniStack<E> {

    private Object[] elements_;
    private int pos_ = -1;

    public MiniStack() {
        elements_ = new Object[8];
    }

    public void clear() {
        pos_ = -1;

        // drop all references to ensure GC
        // this is not most efficient
        Arrays.fill(this.elements_, null);
    }

    /**
     * Resets the stack to zero but does not
     * clean it
     */
    public void reset() {
        pos_ = -1;
    }

    @SuppressWarnings("unchecked")
    public E pop() {
        if ( pos_ >= 0) {
            final E e = (E) this.elements_[pos_];
            // ensure ref is clean
            this.elements_[pos_] = null;
            pos_--;

            return e;
        }
        else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public E peek() {
        if ( pos_ >= 0) {
            return (E) this.elements_[pos_];
        }
        else {
            return null;
        }
    }

    public void push(E element) {
        pos_++;

        if (pos_ == this.elements_.length) {
            this.elements_ = Arrays.copyOf(this.elements_, this.elements_.length + 8);
        }

        this.elements_[pos_] = element;
    }

    public boolean isEmpty() {
        return pos_ < 0;
    }

    public int size() {
        return pos_ + 1;
    }
}
