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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.htmlunit.cyberneko.HTMLElements.Element;
import org.htmlunit.cyberneko.util.FastHashMap;

/**
 * Collection of HTML element information. This is an immutable object
 * so it is safe to share between multiple threads. But one must not
 * continue to use any custom elements after adding it to this collection.
 * 
 * This class was originally in parts HtmlElements. To allow reuse and safe
 * use in a concurrent environment, it was split into this class and the new
 * HtmlElements. In parts to make the API less breaking.
 *
 * @author Andy Clark
 * @author Ahmed Ashour
 * @author Marc Guillemot
 * @author Ronald Brill
 * @author Rene Schwietzke
 */
public class HTMLElementsCollection {

    /** A default reusable instance */
    public static final HTMLElementsCollection DEFAULT = new HTMLElementsCollection();
    
    /** No such element. */
    public final Element NO_SUCH_ELEMENT = new Element(HTMLElements.UNKNOWN, "",  Element.CONTAINER, new short[]{HTMLElements.BODY}, null);

    // information

    // these fields became private to avoid exposing them for indirect modification
    // It cannot be final because we know only later the needed size.
    private Element[] elementsByCode_;

    // keep the list here for later modification
    private final HashMap<String, Element> elementsByNameForReference_ = new HashMap<>();

    // this is a optimized version which will be later queried, sparsely populated to avoid too many collisions
    private final FastHashMap<String, Element> elementsByNameOptimized_ = new FastHashMap<>(311, 0.50f);

    /**
     * Creates a new HTMLElements with all default objects only. If you need that,
     * use the static {@link #DEFAULT} instance.
     */
    private HTMLElementsCollection() {
        this(Collections.emptyList());
    }
    
    /**
     * Creates a new HTMLElements with one extra custom element.
     * 
     * @param customElement our custom element to add
     */
    public HTMLElementsCollection(Element customElement) {
        this(Collections.singletonList(customElement));
    }
    
    /**
     * Creates a new HTMLElements with all default objects plus our custom ones.
     */
    public HTMLElementsCollection(List<Element> customElements) {
        final Element[][] elementsArray = HTMLElements.setupDefaultHTMElements();
        
        // keep contiguous list of elements for lookups by name
        for (final Element[] elements : elementsArray) {
            if (elements != null) {
                for (final Element element : elements) {
                    this.elementsByNameForReference_.put(element.name, element);
                }
            }
        }
        // add our custom elements
        for (final Element customElement : customElements) {
            this.elementsByNameForReference_.put(customElement.name, customElement);
        }

        // setup an optimized versions with all references to parents and 
        // some optimized lookup structures
        setupOptimizedVersions();
    }
    
    private void setupOptimizedVersions() {
        // we got x amount of elements + 1 unknown
        // put that into an array instead of a map, that
        // is a faster look up and avoids equals
        
        // ATTENTION: Due to some HtmlUnit custom tag handling that overwrites our
        // list here, we might get a list with holes, so check the range first
        final int size = elementsByNameForReference_.values().stream().mapToInt(v -> v.code).max().getAsInt();
        
        elementsByCode_ = new Element[Math.max(size, NO_SUCH_ELEMENT.code) + 1];
        elementsByNameForReference_.values().forEach(v -> elementsByCode_[v.code] = v);
        elementsByCode_[NO_SUCH_ELEMENT.code] = NO_SUCH_ELEMENT;

        // initialize cross references to parent elements
        for (final Element element : elementsByCode_) {
            if (element != null) {
                defineParents(element);
            }
        }
        
        // get us a second version that is lowercase to
        // reduce lookup overhead
        for (final Element element : elementsByCode_) {
            // we might have holes due to HtmlUnitNekoHtmlParser
            if (element != null) {
                elementsByNameOptimized_.put(element.name.toLowerCase(Locale.ROOT), element);
            }
        }
    }

    private void defineParents(final Element element) {
        if (element.parentCodes_ != null) {
            element.parent = new Element[element.parentCodes_.length];
            
            for (int j = 0; j < element.parentCodes_.length; j++) {
                element.parent[j] = elementsByCode_[element.parentCodes_[j]];
            }
            
            element.parentCodes_ = null;
        }
    }

    /**
     * Lookup table for elements by code. There is no range check applied
     * for the sake of performance. Java will check that anyway, so we
     * don't have to.
     *
     * @param code The element code.
     * @return the element information for the specified element code.
     */
    final Element getElement(final short code) {
        return elementsByCode_[code];
    }

    /**
     * Lookup the element by name, returns null if not found.
     * 
     * @param ename the name of the element to lookup
     * @return the element or null if not found
     */
    final Element lookupElement(final String ename) {
        return elementsByNameOptimized_.get(ename);
    }
}
