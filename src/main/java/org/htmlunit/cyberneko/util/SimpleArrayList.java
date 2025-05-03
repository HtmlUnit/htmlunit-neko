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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Inexpensive (partial) list implementation. Not fully implemented, just what is needed. As soon as iterators and other
 * things are involved, the memory savings we wanted are gone.
 * <p>
 * Minimal checks for data correctness!! This is tuned for speed not elegance or safety.
 *
 * @author Rene Schwietzke
 * @since 7.0.0
 */
public class SimpleArrayList<T> implements List<T> {
    private T[] data;
    private int size;

    /**
     * Creates a new list wrapper from an existing one.
     * This is not copying anything rather referencing it.
     * Make sure that you understand that!
     *
     * @param list the referencing list
     */
    SimpleArrayList(final SimpleArrayList<T> list) {
        data = list.data;
        size = list.size;
    }

    /**
     * Create a new list with a default capacity.
     *
     * @param capacity
     *            the capacity
     */
    @SuppressWarnings("unchecked")
    public SimpleArrayList(final int capacity) {
        data = (T[]) new Object[capacity];
    }

    /**
     * Create a new list with a default capacity.
     */
    @SuppressWarnings("unchecked")
    public SimpleArrayList() {
        data = (T[]) new Object[10];
    }

    /**
     * Add an element to the end of the list.
     *
     * @param element
     *            the element to add
     * @return true if added and for this impl it is always true
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean add(final T element) {
        final int length = this.data.length;
        if (this.size == length) {
            final T[] newData = (T[]) new Object[(length << 1) + 2];
            System.arraycopy(this.data, 0, newData, 0, length);
            this.data = newData;
        }

        this.data[this.size] = element;
        this.size++;

        return true;
    }

    @Override
    public void add(final int index, final T element) {
        final int length = this.data.length;
        if (this.size == length) {
            final T[] newData = (T[]) new Object[(length << 1) + 2];
            System.arraycopy(data, 0, newData, 0, length);
            data = newData;
        }

        // shift all to the right, if needed
        System.arraycopy(this.data, index, this.data, index + 1, this.size - index);

        this.data[index] = element;
        this.size++;
    }

    /**
     * Return an element at index. No range checks at all.
     *
     * @param index
     *            the position
     * @return the element at this position
     */
    @Override
    public T get(final int index) {
        return data[index];
    }

    /**
     * Returns the size of this list.
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Creates an array of the elements. This is a copy operation!
     *
     * @return an array of the elements
     */
    @Override
    public Object[] toArray() {
        return Arrays.copyOf(this.data, this.size);
    }

    /**
     * Creates an array of the elements. This is a copy operation!
     *
     * @return an array of the elements
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(final T[] array) {
        return (T[]) Arrays.copyOf(this.data, this.size, array.getClass());
    }

    /**
     * Clears the list by setting the size to zero. It does not release any elements for performance purposes.
     */
    @Override
    public void clear() {
        // are are not releasing any references, this is because of speed aka less memory
        // access needed
        this.size = 0;
    }

    /**
     * Returns view partitions on the underlying list. If the count is larger than size you get back the maximum
     * possible list number with one element each. If count is 0 or smaller, we correct it to 1.
     *
     * @param count
     *            how many list do we want
     * @return a list of lists
     */
    public List<List<T>> partition(final int count) {
        final int newCount;
        if (count > size) {
            newCount = size;
        }
        else {
            newCount = count <= 0 ? 1 : count;
        }

        final SimpleArrayList<List<T>> result = new SimpleArrayList<>(count);

        final int newSize = (int) Math.ceil((double) size / (double) newCount);
        for (int i = 0; i < newCount; i++) {
            final int from = i * newSize;
            int to = from + newSize - 1;
            if (to >= size) {
                to = size - 1;
            }
            result.add(new Partition<>(this, from, to));
        }

        return result;
    }

    static class Partition<K> extends SimpleArrayList<K> {
        private final int from_;
        private final int size_;

        Partition(final SimpleArrayList<K> list, final int from, final int to) {
            super(list);

            from_ = from;
            size_ = to - from + 1;
        }

        @Override
        public boolean add(final K o) {
            throw new UnsupportedOperationException("Cannot modify the partition");
        }

        @Override
        public K get(final int index) {
            return super.get(index + from_);
        }

        @Override
        public int size() {
            return size_;
        }

        @Override
        public K[] toArray() {
            throw new UnsupportedOperationException("unimplemented");
        }

    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public boolean contains(final Object o) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public Iterator<T> iterator() {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public boolean remove(final Object o) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public boolean addAll(final Collection<? extends T> c) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends T> c) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public T set(final int index, final T element) {
        this.data[index] = element;
        return element;
    }

    /**
     * Removes the data at index and shifts all data right of it.
     *
     * @param index the position to clear
     * @return the previous value at position index
     */
    @Override
    public T remove(final int index) {
        final T t = this.data[index];
        System.arraycopy(this.data, index + 1, this.data, index, this.size - index - 1);
        this.size--;

        return t;
    }

    @Override
    public int indexOf(final Object o) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public int lastIndexOf(final Object o) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public ListIterator<T> listIterator() {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public ListIterator<T> listIterator(final int index) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public List<T> subList(final int fromIndex, final int toIndex) {
        throw new UnsupportedOperationException("unimplemented");
    }
}
