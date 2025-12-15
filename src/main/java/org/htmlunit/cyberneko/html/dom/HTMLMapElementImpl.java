/*
 * Copyright (c) 2002-2009 Andy Clark, Marc Guillemot
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
package org.htmlunit.cyberneko.html.dom;

import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLMapElement;

/**
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLMapElement
 * @see org.htmlunit.cyberneko.xerces.dom.ElementImpl
 */
public class HTMLMapElementImpl extends HTMLElementImpl implements HTMLMapElement {
    private HTMLCollection areas_;

    @Override
    public HTMLCollection getAreas() {
        if (areas_ == null) {
            areas_ = new HTMLCollectionImpl(this, HTMLCollectionImpl.AREA);
        }
        return areas_;
    }

    @Override
    public String getName() {
        return getAttribute("name");
    }

    @Override
    public void setName(final String name) {
        setAttribute("name", name);
    }

    /**
     * Explicit implementation of cloneNode() to ensure that cache used
     * for getAreas() gets cleared.
     */
    @Override
    public Node cloneNode(final boolean deep) {
        final HTMLMapElementImpl clonedNode = (HTMLMapElementImpl) super.cloneNode(deep);
        clonedNode.areas_ = null;
        return clonedNode;
    }

    /**
     * Constructor requires owner document.
     *
     * @param owner The owner HTML document
     */
    public HTMLMapElementImpl(final HTMLDocumentImpl owner, final String name) {
        super(owner, name);
    }
}

