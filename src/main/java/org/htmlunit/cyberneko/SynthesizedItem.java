/*
 * Copyright (c) 2002-2009 Andy Clark, Marc Guillemot
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

import org.htmlunit.cyberneko.xerces.xni.Augmentations;

/**
 * Synthesized infoset item.
 *
 * @author Andy Clark
 * @author Ronald Brill
 */
final class SynthesizedItem implements Augmentations {

    public static final SynthesizedItem INSTANCE = new SynthesizedItem();

    /**
     * We only like to have one instance.
     */
    private SynthesizedItem() {
    }

    /**
     * @return the line number of the beginning of this event.
     */
    @Override
    public int getBeginLineNumber() {
        return -1;
    }

    /**
     * @return the column number of the beginning of this event.
     */
    @Override
    public int getBeginColumnNumber() {
        return -1;
    }

    /**
     * @return the character offset of the beginning of this event.
     */
    @Override
    public int getBeginCharacterOffset() {
        return -1;
    }

    /**
     * @return the line number of the end of this event.
     */
    @Override
    public int getEndLineNumber() {
        return -1;
    }

    /**
     * @return the column number of the end of this event.
     */
    @Override
    public int getEndColumnNumber() {
        return -1;
    }

    /**
     * @return the character offset of the end of this event.
     */
    @Override
    public int getEndCharacterOffset() {
        return -1;
    }

    /**
     * @return true if this corresponding event was synthesized.
     */
    @Override
    public boolean isSynthesized() {
        return true;
    }

    /**
     * Save to return this instance because it does not have state
     *
     * @return this instance
     */
    @Override
    public Augmentations clone() {
        return this;
    }

    /**
     * @return a string representation of this object.
     */
    @Override
    public String toString() {
        return "synthesized";
    }
}
