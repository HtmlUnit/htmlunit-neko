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

import org.htmlunit.cyberneko.xerces.xni.Augmentations;

/**
 * Location infoset item.
 */
final class LocationItem implements Augmentations {

    /** Beginning line number. */
    private int beginLineNumber_;

    /** Beginning column number. */
    private int beginColumnNumber_;

    /** Beginning character offset. */
    private int beginCharacterOffset_;

    /** Ending line number. */
    private int endLineNumber_;

    /** Ending column number. */
    private int endColumnNumber_;

    /** Ending character offset. */
    private int endCharacterOffset_;

    public void setValues(final int beginLine, final int beginColumn, final int beginOffset,
            final int endLine, final int endColumn, final int endOffset) {
        beginLineNumber_ = beginLine;
        beginColumnNumber_ = beginColumn;
        beginCharacterOffset_ = beginOffset;
        endLineNumber_ = endLine;
        endColumnNumber_ = endColumn;
        endCharacterOffset_ = endOffset;
    }

    /**
     * We need a cloning way to keep reference. See the main interface.
     *
     * @return a copy of this state
     */
    @Override
    public Augmentations clone() {
        final LocationItem clone = new LocationItem();
        clone.setValues(beginLineNumber_, beginColumnNumber_, beginCharacterOffset_,
                endLineNumber_, endColumnNumber_, endCharacterOffset_);
        return clone;
    }

    /**
     * @return the line number of the beginning of this event.
     */
    @Override
    public int getBeginLineNumber() {
        return beginLineNumber_;
    }

    /**
     * @return the column number of the beginning of this event.
     */
    @Override
    public int getBeginColumnNumber() {
        return beginColumnNumber_;
    }

    /**
     * @return the character offset of the beginning of this event.
     */
    @Override
    public int getBeginCharacterOffset() {
        return beginCharacterOffset_;
    }

    /**
     * @return the line number of the end of this event.
     */
    @Override
    public int getEndLineNumber() {
        return endLineNumber_;
    }

    /**
     * @return the column number of the end of this event.
     */
    @Override
    public int getEndColumnNumber() {
        return endColumnNumber_;
    }

    /**
     * @return the character offset of the end of this event.
     */
    @Override
    public int getEndCharacterOffset() {
        return endCharacterOffset_;
    }

    // other information

    /**
     * @return true if this corresponding event was synthesized.
     */
    @Override
    public boolean isSynthesized() {
        return false;
    }

    /**
     * @return a string representation of this object.
     */
    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder();
        str.append(beginLineNumber_);
        str.append(':');
        str.append(beginColumnNumber_);
        str.append(':');
        str.append(beginCharacterOffset_);
        str.append(':');
        str.append(endLineNumber_);
        str.append(':');
        str.append(endColumnNumber_);
        str.append(':');
        str.append(endCharacterOffset_);
        return str.toString();
    }
}
