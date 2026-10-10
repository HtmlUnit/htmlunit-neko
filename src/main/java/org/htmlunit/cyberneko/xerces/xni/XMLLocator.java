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

import org.xml.sax.ext.Locator2;

/**
 * Interface for tracking location information during document parsing.
 * <p>
 * Extends SAX {@link Locator2} to provide additional system identifiers
 * and character offsets for precise error reporting and source tracking.
 * </p>
 *
 * @author Andy Clark, IBM
 */
public interface XMLLocator extends Locator2 {

    /**
     * Returns the literal system identifier.
     *
     * @return the literal system identifier, or {@code null} if none is available
     */
    String getLiteralSystemId();

    /**
     * Returns the base system identifier.
     *
     * @return the base system identifier, or {@code null} if none is available
     */
    String getBaseSystemId();

    /**
     * Returns the character offset within the input stream or character stream.
     *
     * @return the character offset, or {@code -1} if no character offset is available
     */
    int getCharacterOffset();
}
