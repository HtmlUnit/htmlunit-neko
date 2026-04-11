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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.htmlunit.cyberneko.xerces.xni.QName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link HTMLTagBalancer.ElementEntry}.
 */
public class ElementEntryTest {

    // ---- QName is deep-copied ----

    @Test
    public void qnameIsDeepCopied() {
        final QName original = new QName(null, "div", "div", null);

        final HTMLTagBalancer.ElementEntry entry =
                new HTMLTagBalancer.ElementEntry(original, null);

        assertNotSame(original, entry.name_);
        assertEquals("div", entry.name_.getRawname());

        // mutating original doesn't affect entry
        original.setRawname("span");
        assertEquals("div", entry.name_.getRawname());
    }

    // ---- null augmentations ----

    @Test
    public void nullAugmentations() {
        final HTMLTagBalancer.ElementEntry entry =
                new HTMLTagBalancer.ElementEntry(
                        new QName(null, "p", "p", null), null);

        assertNull(entry.augs_);
    }

    // ---- non-null augmentations are cloned ----

    @Test
    public void augmentationsAreCloned() {
        final LocationItem augs = new LocationItem();
        augs.setValues(1, 1, 0, 1, 10, 9);

        final HTMLTagBalancer.ElementEntry entry =
                new HTMLTagBalancer.ElementEntry(
                        new QName(null, "div", "div", null), augs);

        assertNotNull(entry.augs_);
        assertNotSame(augs, entry.augs_);
        assertEquals(1, entry.augs_.getBeginLineNumber());
        assertEquals(10, entry.augs_.getEndColumnNumber());
    }
}