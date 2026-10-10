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
package org.htmlunit.cyberneko.xerces.xni;

/**
 * The Augmentations interface defines a table of additional data that may be
 * passed along the document pipeline. The information can contain extra
 * arguments or infoset augmentations, for example PSVI. This additional
 * information is identified by a String key.
 * <p>
 * <strong>Note:</strong> Methods that receive Augmentations are required to
 * copy the information if it is to be saved for use beyond the scope of the
 * method. The Augmentations content is volatile, and may be modified by any
 * method in any component in the pipeline. Therefore, methods passed this
 * structure should not save any reference to the structure.
 * </p>
 *
 * @author Elena Litani, IBM
 * @author Ronald Brill
 */
public interface Augmentations {
    // location information

    /**
     * Returns the line number of the beginning of this event.
     *
     * @return the one-based begin line number, or {@code -1} if not available
     */
    int getBeginLineNumber();

    /**
     * Returns the column number of the beginning of this event.
     *
     * @return the one-based begin column number, or {@code -1} if not available
     */
    int getBeginColumnNumber();

    /**
     * Returns the character offset of the beginning of this event.
     *
     * @return the zero-based begin character offset, or {@code -1} if not available
     */
    int getBeginCharacterOffset();

    /**
     * Returns the line number of the end of this event.
     *
     * @return the one-based end line number, or {@code -1} if not available
     */
    int getEndLineNumber();

    /**
     * Returns the column number of the end of this event.
     *
     * @return the one-based end column number, or {@code -1} if not available
     */
    int getEndColumnNumber();

    /**
     * Returns the character offset of the end of this event.
     *
     * @return the zero-based end character offset, or {@code -1} if not available
     */
    int getEndCharacterOffset();

    // other information

    /**
     * Returns whether the corresponding event was synthesized.
     *
     * @return {@code true} if the event was synthesized, {@code false} otherwise
     */
    boolean isSynthesized();

    /**
     * Clones this Augmentations instance in case a reference needs to be retained.
     * Storing the original reference directly is illegal due to its volatile nature.
     *
     * @return a full copy of this augmentations holder
     */
    Augmentations clone();
}
