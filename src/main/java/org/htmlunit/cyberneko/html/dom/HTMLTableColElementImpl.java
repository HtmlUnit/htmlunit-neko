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

import org.w3c.dom.html.HTMLTableColElement;

/**
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLTableColElement
 * @see org.htmlunit.cyberneko.xerces.dom.ElementImpl
 */
public class HTMLTableColElementImpl extends HTMLElementImpl implements HTMLTableColElement {

    @Override
    public String getAlign() {
        return capitalize(getAttribute("align"));
    }

    @Override
    public void setAlign(final String align) {
        setAttribute("align", align);
    }

    @Override
    public String getCh() {
        String ch;

        // Make sure that the access key is a single character.
        ch = getAttribute("char");
        if (ch != null && ch.length() > 1) {
            ch = ch.substring(0, 1);
        }
        return ch;
    }

    @Override
    public void setCh(String ch) {
        // Make sure that the char is a single character.
        if (ch != null && ch.length() > 1) {
            ch = ch.substring(0, 1);
        }
        setAttribute("char", ch);
    }

    @Override
    public String getChOff() {
        return getAttribute("charoff");
    }

    @Override
    public void setChOff(final String chOff) {
        setAttribute("charoff", chOff);
    }

    @Override
    public int getSpan() {
        return getInteger(getAttribute("span"));
    }

    @Override
    public void setSpan(final int span) {
        setAttribute("span", String.valueOf(span));
    }

    @Override
    public String getVAlign() {
        return capitalize(getAttribute("valign"));
    }

    @Override
    public void setVAlign(final String vAlign) {
        setAttribute("valign", vAlign);
    }

    @Override
    public String getWidth() {
        return getAttribute("width");
    }

    @Override
    public void setWidth(final String width) {
        setAttribute("width", width);
    }

    /**
     * Constructor requires owner document.
     *
     * @param owner The owner HTML document
     */
    public HTMLTableColElementImpl(final HTMLDocumentImpl owner, final String name) {
        super(owner, name);
    }
}
