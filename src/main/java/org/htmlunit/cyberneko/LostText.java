/*
 * Copyright (c) 2002-2009 Andy Clark, Marc Guillemot
 * Copyright (c) 2017-2024 Ronald Brill
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

import java.util.ArrayList;
import java.util.List;

import org.htmlunit.cyberneko.xerces.xni.Augmentations;
import org.htmlunit.cyberneko.xerces.xni.XMLDocumentHandler;
import org.htmlunit.cyberneko.xerces.xni.XMLString;

/**
 * Container for text that should be hold and re-feed later like text before &lt;html&gt; that will be re-feed
 * in &lt;body&gt;
 * @author Marc Guillemot
 */
class LostText {
    /**
     * Pair of (text, augmentation)
     */
    private static final class Entry {
        final XMLString text_;
        final Augmentations augs_;

        Entry(final XMLString text, final Augmentations augs) {
            text_ = text.clone();
            augs_ = augs == null ? null : augs.clone();
        }
    }

    private final List<Entry> entries_ = new ArrayList<>();

    /**
     * Adds some text that need to be re-feed later. The information gets copied.
     */
    public void add(final XMLString text, final Augmentations augs) {
        if (!entries_.isEmpty() || !text.isWhitespace()) {
            entries_.add(new Entry(text, augs));
        }
    }

    /**
     * Pushes the characters into the {@link XMLDocumentHandler}
     * @param tagBalancer the tag balancer that will receive the events
     */
    public void refeed(final XMLDocumentHandler tagBalancer) {
        // Use indexed loop - no Iterator allocation
        final int size = entries_.size();
        for (int i = 0; i < size; i++) {
            final Entry entry = entries_.get(i);
            tagBalancer.characters(entry.text_, entry.augs_);
        }
        // not needed anymore once it has been used -> clear to free memory
        entries_.clear();
    }

    /**
     * Indicates if this container contains something
     * @return <code>true</code> if no lost text has been collected
     */
    public boolean isEmpty() {
        return entries_.isEmpty();
    }

    /**
     * Clears the list
     */
    public void clear() {
        entries_.clear();
    }
}
