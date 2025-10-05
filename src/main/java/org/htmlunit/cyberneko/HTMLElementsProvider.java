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

import org.htmlunit.cyberneko.HTMLElements.Element;

/**
 * Interface to support different {@link Element}'s providers.
 *
 * @author Ronald Brill
 */
public interface HTMLElementsProvider {

    /**
     * @return the element information for the specified element name.
     *
     * @param ename the element name.
     */
    Element getElement(String ename);

    /**
     * @return the element information for the specified element name.
     *
     * @param ename the element name.
     * @param elementIfNotFound the default element to return if not found.
     */
    Element getElement(String ename, Element elementIfNotFound);

    /**
     * @return the element information for the specified element name.
     *
     * @param enameLC the element name in lower case
     * @param elementIfNotFound the default element to return if not found.
     */
    Element getElementLC(String enameLC, Element elementIfNotFound);
}
