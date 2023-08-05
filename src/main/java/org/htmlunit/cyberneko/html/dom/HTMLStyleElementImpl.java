/*
 * Copyright 2002-2009 Andy Clark, Marc Guillemot
 * Copyright 2017-2023 Ronald Brill
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

import org.w3c.dom.html.HTMLStyleElement;

/**
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLStyleElement
 * @see org.htmlunit.cyberneko.xerces.dom.ElementImpl
 */
public class HTMLStyleElementImpl extends HTMLElementImpl implements HTMLStyleElement {

    @Override
    public boolean getDisabled() {
        return getBinary("disabled");
    }

    @Override
    public void setDisabled(final boolean disabled) {
        setAttribute("disabled", disabled);
    }

    @Override
    public String getMedia() {
        return getAttribute("media");
    }

    @Override
    public void setMedia(final String media) {
        setAttribute("media", media);
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
    public HTMLStyleElementImpl(final HTMLDocumentImpl owner, final String name) {
        super(owner, name);
    }
}
