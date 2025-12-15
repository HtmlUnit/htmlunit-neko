/*
 * Copyright (c) 2017-2025 Ronald Brill
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
 * Location information.
 *
 * @author Andy Clark, IBM
 */
public interface XMLLocator extends Locator2 {

    /**
     * @return the literal system identifier.
     */
    String getLiteralSystemId();

    /**
     * @return the base system identifier.
     */
    String getBaseSystemId();

    /**
     * @return the character offset, or <code>-1</code> if no character offset is
     *         available.
     */
    int getCharacterOffset();
}
