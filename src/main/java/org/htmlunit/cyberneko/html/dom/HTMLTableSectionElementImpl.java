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
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLTableRowElement;
import org.w3c.dom.html.HTMLTableSectionElement;

/**
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLTableSectionElement
 * @see org.htmlunit.cyberneko.xerces.dom.ElementImpl
 */
public class HTMLTableSectionElementImpl extends HTMLElementImpl implements HTMLTableSectionElement {

    private HTMLCollectionImpl rows_;

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
        // Make sure that the access key is a single character.
        String ch = getAttribute("char");
        if (ch.length() > 1) {
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
    public String getVAlign() {
        return capitalize(getAttribute("valign"));
    }

    @Override
    public void setVAlign(final String vAlign) {
        setAttribute("valign", vAlign);
    }

    @Override
    public HTMLCollection getRows() {
        if (rows_ == null) {
            rows_ = new HTMLCollectionImpl(this, HTMLCollectionImpl.ROW);
        }
        return rows_;
    }

    @Override
    public HTMLElement insertRow(final int index) {
        final HTMLTableRowElementImpl newRow;

        newRow = new HTMLTableRowElementImpl((HTMLDocumentImpl) getOwnerDocument(), "TR");
        newRow.insertCell(0);
        if (insertRowX(index, newRow) >= 0) {
            appendChild(newRow);
        }
        return newRow;
    }

    int insertRowX(int index, final HTMLTableRowElementImpl newRow) {
        Node child;

        child = getFirstChild();
        while (child != null) {
            if (child instanceof HTMLTableRowElement) {
                if (index == 0) {
                    insertBefore(newRow, child);
                    return -1;
                }
                --index;
            }
            child = child.getNextSibling();
        }
        return index;
    }

    @Override
    public void deleteRow(final int index) {
        deleteRowX(index);
    }

    int deleteRowX(int index) {
        Node    child;

        child = getFirstChild();
        while (child != null) {
            if (child instanceof HTMLTableRowElement) {
                if (index == 0) {
                    removeChild(child);
                    return -1;
                }
                --index;
            }
            child = child.getNextSibling();
        }
        return index;
    }

    /**
     * Explicit implementation of cloneNode() to ensure that cache used
     * for getRows() gets cleared.
     */
    @Override
    public Node cloneNode(final boolean deep) {
        final HTMLTableSectionElementImpl clonedNode = (HTMLTableSectionElementImpl) super.cloneNode(deep);
        clonedNode.rows_ = null;
        return clonedNode;
    }

    /**
     * Constructor requires owner document.
     *
     * @param owner The owner HTML document
     */
    public HTMLTableSectionElementImpl(final HTMLDocumentImpl owner, final String name) {
        super(owner, name);
    }
}
