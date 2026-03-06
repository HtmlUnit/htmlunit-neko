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

import java.util.Arrays;
import java.util.HashMap;

/**
 * A cache that interns strings from char[] buffer regions.
 * <p>
 * On cache hits, the same {@code String} instance is returned,
 * avoiding repeated allocation for frequently occurring names
 * (e.g., HTML tag names and attribute names).
 *
 * <p>The lookup key points directly into the caller's buffer
 * (zero-copy), and only when a new entry is added does the key
 * data get copied into an independent array.
 *
 * @author Ronald Brill
 * @since 5.0.0
 */
public class StringCache {
    // HTML has ~100 distinct tag names + ~50 common attribute names
    // At 0.75 load factor, capacity 256 avoids some rehash
    private final HashMap<CharBufferKey, String> cache_ = new HashMap<>(256);

    private final CharBufferKey lookupKey_ = new CharBufferKey();

    /**
     * Returns a cached {@code String} for the given char buffer region.
     * If no cached entry exists, a new {@code String} is created, cached, and returned.
     *
     * @param ch     the character array (may be a shared/reused buffer)
     * @param offset the start offset of the name in {@code ch}
     * @param length the number of characters
     * @return the cached string
     */
    public String get(final char[] ch, final int offset, final int length) {
        lookupKey_.update(ch, offset, length);
        String val = cache_.get(lookupKey_);

        if (val == null) {
            val = new String(ch, offset, length);
            cache_.put(lookupKey_.detach(), val);
        }

        return val;
    }

    /**
     * A lightweight key that wraps a region of a {@code char[]} for use
     * as a {@link HashMap} lookup key. The {@link #update} method points
     * the key at a caller-owned buffer (zero-copy); {@link #detach}
     * creates an independent copy suitable for long-term storage in the map.
     */
    static final class CharBufferKey {
        private char[] data_;
        private int offset_;
        private int length_;
        private int hash_;

        /**
         * Points this key at a region of an external char array.
         * No copy is made; the caller must not mutate the region
         * while this key is used for a lookup.
         *
         * @param ch     the character array
         * @param offset the start offset
         * @param length the number of characters
         */
        void update(final char[] ch, final int offset, final int length) {
            data_ = ch;
            offset_ = offset;
            length_ = length;

            int h = 0;
            for (int i = offset; i < offset + length; i++) {
                h = ((h << 5) - h) + ch[i];
            }
            hash_ = h;
        }

        /**
         * Creates an independent copy of this key whose data is
         * not shared with any external buffer. The copy is suitable
         * for storing as a long-lived map key.
         *
         * @return a detached copy of this key
         */
        CharBufferKey detach() {
            final CharBufferKey detached = new CharBufferKey();
            detached.data_ = new char[length_];
            System.arraycopy(data_, offset_, detached.data_, 0, length_);
            detached.offset_ = 0;
            detached.length_ = length_;
            detached.hash_ = hash_;
            return detached;
        }

        @Override
        public int hashCode() {
            return hash_;
        }

        @Override
        public boolean equals(final Object o) {
            if (o instanceof CharBufferKey ob) {
                if (ob.length_ != length_) {
                    return false;
                }
                return Arrays.mismatch(
                        data_, offset_, offset_ + length_,
                        ob.data_, ob.offset_, ob.offset_ + ob.length_) < 0;
            }
            return false;
        }

        @Override
        public String toString() {
            return new String(data_, offset_, length_);
        }
    }
}