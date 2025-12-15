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

import org.w3c.dom.html.HTMLOListElement;

/**
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLOListElement
 * @see org.htmlunit.cyberneko.xerces.dom.ElementImpl
 */
public class HTMLOListElementImpl extends HTMLElementImpl implements HTMLOListElement {

    @Override
    public boolean getCompact() {
        return getBinary("compact");
    }

    @Override
    public void setCompact(final boolean compact) {
        setAttribute("compact", compact);
    }

    @Override
    public int getStart() {
        return getInteger(getAttribute("start"));
    }

    @Override
    public void setStart(final int start) {
        setAttribute("start", String.valueOf(start));
    }

    @Override
    public String getType() {
        return getAttribute("type");
    }

    @Override
    public void setType(final String type) {
        setAttribute("type", type);
    }

    /**
     * Constructor requires owner document.
     *
     * @param owner The owner HTML document
     */
    public HTMLOListElementImpl(final HTMLDocumentImpl owner, final String name) {
        super(owner, name);
    }
}
