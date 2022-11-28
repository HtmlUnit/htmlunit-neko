/*
 * Copyright 2002-2009 Andy Clark, Marc Guillemot
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

package net.sourceforge.htmlunit.cyberneko;

/**
 * This interface is used to pass augmentated information to the
 * application through the XNI pipeline.
 *
 * @author Andy Clark
 */
public interface HTMLEventInfo {

    //
    // HTMLEventInfo methods
    //

    // location information

    /**
     * @return the line number of the beginning of this event.
     */
    int getBeginLineNumber();

    /**
     * @return the column number of the beginning of this event.
     */
    int getBeginColumnNumber();

    /**
     * @return the character offset of the beginning of this event.
     */
    int getBeginCharacterOffset();

    /**
     * @return the line number of the end of this event.
     */
    int getEndLineNumber();

    /**
     * @return the column number of the end of this event.
     */
    int getEndColumnNumber();

    /**
     * @return the character offset of the end of this event.
     */
    int getEndCharacterOffset();

    // other information

    /**
     * @return true if this corresponding event was synthesized.
     */
    boolean isSynthesized();

    /**
     * Synthesized infoset item.
     *
     * @author Andy Clark
     */
    class SynthesizedItem
        implements HTMLEventInfo {

        //
        // HTMLEventInfo methods
        //

        // location information

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

        // other information

        /**
         * @return true if this corresponding event was synthesized.
         */
        @Override
        public boolean isSynthesized() {
            return true;
        }

        //
        // Object methods
        //

        /**
         * @return a string representation of this object.
         */
        @Override
        public String toString() {
            return "synthesized";
        }
    }
}
