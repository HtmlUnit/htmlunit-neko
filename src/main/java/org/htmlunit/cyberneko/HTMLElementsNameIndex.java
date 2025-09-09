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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.htmlunit.cyberneko.HTMLElements.Element;

/**
 * A special index impl for {@link HTMLElements.Element}'s.
 *
 * @author Ronald Brill
 */
public class HTMLElementsNameIndex {
    int maxLength_;
    IndexEntry[] indexEntries_;

    public static final class HTMLElementsNameIndexBuilder {
        private int maxLength_;
        private ArrayList<Map<String, Element>> elementsByLength_;

        public HTMLElementsNameIndexBuilder() {
            maxLength_ = 0;
            elementsByLength_ = new ArrayList<>();
        }

        /**
         * Add the provided element to the index.
         *
         * @param element the {@link Element} to add
         */
        public void register(final Element element) {
            int length = element.name.length();
            maxLength_ = Math.max(maxLength_, length);

            while (elementsByLength_.size() < length) {
                elementsByLength_.add(new HashMap<>());
            }
            Map<String, Element> elements = elementsByLength_.get(length - 1);
            elements.put(element.name, element);
        }

        /**
         * @return the index
         */
        public HTMLElementsNameIndex build() {
            HTMLElementsNameIndex index = new HTMLElementsNameIndex();
            index.maxLength_ = maxLength_;

            index.indexEntries_ = new IndexEntry[maxLength_];
            int i = 0;
            for (final Map<String, Element> elements : elementsByLength_) {
                index.indexEntries_[i] = new IndexEntry(elements);
                i++;
            }
            return index;
        }
    }

    public static final class IndexEntry {
        private Element[] elements_;
        private List<char[]> names_;

        public IndexEntry(Map<String, Element> elements) {
            List<String> names = new ArrayList<>(elements.keySet());
            Collections.sort(names);

            elements_ = new Element[names.size()];
            names_ = new ArrayList<>(names.size());
            int i = 0;
            for (String name : names) {
                elements_[i] = elements.get(name);
                // todo check for ascii
                names_.add(name.toUpperCase().toCharArray());
                i++;
            }
        }

        public Element getElement(final String ename) {
            char[] enameChars = ename.toCharArray();
            int length = enameChars.length;

            int converted = 0;
            int foundIdx = -1;

            for (char[] nameChars : names_) {
                outer: {
                    foundIdx++;
                    int i = 0;

                    for (; i < converted; i++) {
                        char expected = nameChars[i];
                        char c = enameChars[i];
                        if (c < expected) {
                            return null;
                        }

                        if (c > expected) {
                            break outer;
                        }
                    }
                    for (; i < length; i++) {
                        char c = Character.toUpperCase(enameChars[i]);
                        enameChars[i] = c;
                        converted++;

                        char expected = nameChars[i];
                        if (c < expected) {
                            return null;
                        }

                        if (c > expected) {
                            break outer;
                        }
                    }

                    if (i == length) {
                        return elements_[foundIdx];
                    }
                }
            }

            return null;
        }
    }

    /**
     * @return the element information for the specified element name.
     *
     * @param ename The element name.
     * @param elementIfNotFound The default element to return if not found.
     */
    public Element getElement(final String ename, final Element elementIfNotFound) {
        int length = ename.length();
        if (length > maxLength_) {
            return elementIfNotFound;
        }

        Element element = indexEntries_[length - 1].getElement(ename);
        if (element != null) {
            return element;
        }

        return elementIfNotFound;
    }
}
