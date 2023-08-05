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

import org.w3c.dom.html.HTMLParamElement;

/**
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLParamElement
 * @see org.htmlunit.cyberneko.xerces.dom.ElementImpl
 */
public class HTMLParamElementImpl extends HTMLElementImpl implements HTMLParamElement {

    @Override
    public String getName() {
        return getAttribute("name");
    }

    @Override
    public void setName(final String name) {
        setAttribute("name", name);
    }

    @Override
    public String getType() {
        return getAttribute("type");
    }

    @Override
    public void setType(final String type) {
        setAttribute("type", type);
    }

    @Override
    public String getValue() {
        return getAttribute("value");
    }

    @Override
    public void setValue(final String value) {
        setAttribute("value", value);
    }

    @Override
    public String getValueType() {
        return capitalize(getAttribute("valuetype"));
    }

    @Override
    public void setValueType(final String valuetype) {
        setAttribute("valuetype", valuetype);
    }

    /**
     * Constructor requires owner document.
     *
     * @param owner The owner HTML document
     */
    public HTMLParamElementImpl(final HTMLDocumentImpl owner, final String name) {
        super(owner, name);
    }
}
