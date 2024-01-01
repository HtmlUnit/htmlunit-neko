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
package org.htmlunit.cyberneko.html.dom;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLTableCellElement;
import org.w3c.dom.html.HTMLTableElement;
import org.w3c.dom.html.HTMLTableRowElement;
import org.w3c.dom.html.HTMLTableSectionElement;

/**
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLTableRowElement
 * @see org.htmlunit.cyberneko.xerces.dom.ElementImpl
 */
public class HTMLTableRowElementImpl extends HTMLElementImpl implements HTMLTableRowElement {
    private HTMLCollection cells_;

    @Override
    public int getRowIndex() {
        Node    parent;

        parent = getParentNode();
        if (parent instanceof HTMLTableSectionElement) {
            parent = parent.getParentNode();
        }
        if (parent instanceof HTMLTableElement) {
            return getRowIndex(parent);
        }
        return -1;
    }

    @Override
    public int getSectionRowIndex() {
        final Node parent;

        parent = getParentNode();
        if (parent instanceof HTMLTableSectionElement) {
            return getRowIndex(parent);
        }
        return -1;
    }

    int getRowIndex(final Node parent) {
        final NodeList rows;
        int i;

        // Use getElementsByTagName() which creates a snapshot of all the
        // TR elements under the TABLE/section. Access to the returned NodeList
        // is very fast and the snapshot solves many synchronization problems.
        rows = ((HTMLElement) parent).getElementsByTagName("TR");
        for (i = 0; i < rows.getLength(); ++i) {
            if (rows.item(i) == this) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public HTMLCollection  getCells() {
        if (cells_ == null) {
            cells_ = new HTMLCollectionImpl(this, HTMLCollectionImpl.CELL);
        }
        return cells_;
    }

    @Override
    public HTMLElement insertCell(int index) {
        Node child;
        final HTMLElement newCell;

        newCell = new HTMLTableCellElementImpl((HTMLDocumentImpl) getOwnerDocument(), "TD");
        child = getFirstChild();
        while (child != null) {
            if (child instanceof HTMLTableCellElement) {
                if (index == 0) {
                    insertBefore(newCell, child);
                    return newCell;
                }
                --index;
            }
            child = child.getNextSibling();
        }
        appendChild(newCell);
        return newCell;
    }

    @Override
    public void deleteCell(int index) {
        Node    child;

        child = getFirstChild();
        while (child != null) {
            if (child instanceof HTMLTableCellElement) {
                if (index == 0) {
                    removeChild(child);
                    return;
                }
                --index;
            }
            child = child.getNextSibling();
        }
    }

    @Override
    public String getAlign() {
        return capitalize(getAttribute("align"));
    }

    @Override
    public void setAlign(final String align) {
        setAttribute("align", align);
    }

    @Override
    public String getBgColor() {
        return getAttribute("bgcolor");
    }

    @Override
    public void setBgColor(final String bgColor) {
        setAttribute("bgcolor", bgColor);
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

    /**
     * Explicit implementation of cloneNode() to ensure that cache used
     * for getCells() gets cleared.
     */
    @Override
    public Node cloneNode(final boolean deep) {
        final HTMLTableRowElementImpl clonedNode = (HTMLTableRowElementImpl) super.cloneNode(deep);
        clonedNode.cells_ = null;
        return clonedNode;
    }

    /**
     * Constructor requires owner document.
     *
     * @param owner The owner HTML document
     */
    public HTMLTableRowElementImpl(final HTMLDocumentImpl owner, final String name) {
        super(owner, name);
    }
}

