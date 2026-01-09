/*
 * Copyright (c) 2002-2009 Andy Clark, Marc Guillemot
 * Copyright (c) 2017-2026 Ronald Brill
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
import org.w3c.dom.html.HTMLTableCaptionElement;
import org.w3c.dom.html.HTMLTableElement;
import org.w3c.dom.html.HTMLTableRowElement;
import org.w3c.dom.html.HTMLTableSectionElement;

/**
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLAnchorElement
 * @see org.htmlunit.cyberneko.xerces.dom.ElementImpl
 */
public class HTMLTableElementImpl extends HTMLElementImpl implements HTMLTableElement {
    private HTMLCollectionImpl rows_;
    private HTMLCollectionImpl bodies_;

    @Override
    public synchronized HTMLTableCaptionElement getCaption() {
        Node    child;

        child = getFirstChild();
        while (child != null) {
            if (child instanceof HTMLTableCaptionElement element && child.getNodeName().equals("CAPTION")) {
                return element;
            }
            child = child.getNextSibling();
        }
        return null;
    }

    @Override
    public synchronized void setCaption(final HTMLTableCaptionElement caption) {
        if (caption != null && !"CAPTION".equalsIgnoreCase(caption.getTagName())) {
            throw new IllegalArgumentException("HTM016 Argument 'caption' is not an element of type <CAPTION>.");
        }

        deleteCaption();
        if (caption != null) {
            appendChild(caption);
        }
    }

    @Override
    public synchronized HTMLElement createCaption() {
        HTMLElement    section;

        section = getCaption();
        if (section != null) {
            return section;
        }

        section = new HTMLTableCaptionElementImpl((HTMLDocumentImpl) getOwnerDocument(), "CAPTION");
        appendChild(section);
        return section;
    }

    @Override
    public synchronized void deleteCaption() {
        final Node old;

        old = getCaption();
        if (old != null) {
            removeChild(old);
        }
    }

    @Override
    public synchronized HTMLTableSectionElement getTHead() {
        Node    child;

        child = getFirstChild();
        while (child != null) {
            if (child instanceof HTMLTableSectionElement element && child.getNodeName().equals("THEAD")) {
                return element;
            }
            child = child.getNextSibling();
        }
        return null;
    }

    @Override
    public synchronized void setTHead(final HTMLTableSectionElement tHead) {
        if (tHead != null && !"THEAD".equalsIgnoreCase(tHead.getTagName())) {
            throw new IllegalArgumentException("HTM017 Argument 'tHead' is not an element of type <THEAD>.");
        }

        deleteTHead();
        if (tHead != null) {
            appendChild(tHead);
        }
    }

    @Override
    public synchronized HTMLElement createTHead() {
        HTMLElement section;

        section = getTHead();
        if (section != null) {
            return section;
        }
        section = new HTMLTableSectionElementImpl((HTMLDocumentImpl) getOwnerDocument(), "THEAD");
        appendChild(section);
        return section;
    }

    @Override
    public synchronized void deleteTHead() {
        final Node old;

        old = getTHead();
        if (old != null) {
            removeChild(old);
        }
    }

    @Override
    public synchronized HTMLTableSectionElement getTFoot() {
        Node    child;

        child = getFirstChild();
        while (child != null) {
            if (child instanceof HTMLTableSectionElement element && child.getNodeName().equals("TFOOT")) {
                return element;
            }
            child = child.getNextSibling();
        }
        return null;
    }

    @Override
    public synchronized void setTFoot(final HTMLTableSectionElement tFoot) {
        if (tFoot != null && !"TFOOT".equalsIgnoreCase(tFoot.getTagName())) {
            throw new IllegalArgumentException("HTM018 Argument 'tFoot' is not an element of type <TFOOT>.");
        }

        deleteTFoot();
        if (tFoot != null) {
            appendChild(tFoot);
        }
    }

    @Override
    public synchronized HTMLElement createTFoot() {
        HTMLElement    section;

        section = getTFoot();
        if (section != null) {
            return section;
        }

        section = new HTMLTableSectionElementImpl((HTMLDocumentImpl) getOwnerDocument(), "TFOOT");
        appendChild(section);
        return section;
    }

    @Override
    public synchronized void deleteTFoot() {
        final Node old;

        old = getTFoot();
        if (old != null) {
            removeChild(old);
        }
    }

    @Override
    public HTMLCollection getRows() {
        if (rows_ == null) {
            rows_ = new HTMLCollectionImpl(this, HTMLCollectionImpl.ROW);
        }
        return rows_;
    }

    @Override
    public HTMLCollection getTBodies() {
        if (bodies_ == null) {
            bodies_ = new HTMLCollectionImpl(this, HTMLCollectionImpl.TBODY);
        }
        return bodies_;
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
    public String getBorder() {
        return getAttribute("border");
    }

    @Override
    public void setBorder(final String border) {
        setAttribute("border", border);
    }

    @Override
    public String getCellPadding() {
        return getAttribute("cellpadding");
    }

    @Override
    public void setCellPadding(final String cellPadding) {
        setAttribute("cellpadding", cellPadding);
    }

    @Override
    public String getCellSpacing() {
        return getAttribute("cellspacing");
    }

    @Override
    public void setCellSpacing(final String cellSpacing) {
        setAttribute("cellspacing", cellSpacing);
    }

    @Override
    public String getFrame() {
        return capitalize(getAttribute("frame"));
    }

    @Override
    public void setFrame(final String frame) {
        setAttribute("frame", frame);
    }

    @Override
    public String getRules() {
        return capitalize(getAttribute("rules"));
    }

    @Override
    public void setRules(final String rules) {
        setAttribute("rules", rules);
    }

    @Override
    public String getSummary() {
        return getAttribute("summary");
    }

    @Override
    public void setSummary(final String summary) {
        setAttribute("summary", summary);
    }

    @Override
    public String getWidth() {
        return getAttribute("width");
    }

    @Override
    public void setWidth(final String width) {
        setAttribute("width", width);
    }

    @Override
    public HTMLElement insertRow(final int index) {
        final HTMLTableRowElementImpl newRow;

        newRow = new HTMLTableRowElementImpl((HTMLDocumentImpl) getOwnerDocument(), "TR");
        //newRow.insertCell(0);
        insertRowX(index, newRow);
        return newRow;
    }

    void insertRowX(int index, final HTMLTableRowElementImpl newRow) {
        Node child;
        Node lastSection = null;

        child = getFirstChild();
        while (child != null) {
            if (child instanceof HTMLTableRowElement) {
                if (index == 0) {
                    insertBefore(newRow, child);
                    return;
                }
            }
            else {
                if (child instanceof HTMLTableSectionElementImpl impl) {
                    lastSection = child;
                    index = impl.insertRowX(index, newRow);
                    if (index < 0) {
                        return;
                    }
                }
            }
            child = child.getNextSibling();
        }
        if (lastSection != null) {
            lastSection.appendChild(newRow);
        }
        else {
            appendChild(newRow);
        }
    }

    @Override
    public synchronized void deleteRow(int index) {
        Node child;

        child = getFirstChild();
        while (child != null) {
            if (child instanceof HTMLTableRowElement) {
                if (index == 0) {
                    removeChild(child);
                    return;
                }
                --index;
            }
            else {
                if (child instanceof HTMLTableSectionElementImpl impl) {
                    index = impl.deleteRowX(index);
                    if (index < 0) {
                        return;
                    }
                }
            }
            child = child.getNextSibling();
        }
    }

    /**
     * Explicit implementation of cloneNode() to ensure that cache used
     * for getRows() and getTBodies() gets cleared.
     */
    @Override
    public Node cloneNode(final boolean deep) {
        final HTMLTableElementImpl clonedNode = (HTMLTableElementImpl) super.cloneNode(deep);
        clonedNode.rows_ = null;
        clonedNode.bodies_ = null;
        return clonedNode;
    }

    /**
     * Constructor requires owner document.
     *
     * @param owner The owner HTML document
     */
    public HTMLTableElementImpl(final HTMLDocumentImpl owner, final String name) {
        super(owner, name);
    }
}

