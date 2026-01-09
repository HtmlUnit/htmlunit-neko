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
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLOptionElement;
import org.w3c.dom.html.HTMLSelectElement;

/**
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLSelectElement
 * @see org.htmlunit.cyberneko.xerces.dom.ElementImpl
 */
public class HTMLSelectElementImpl extends HTMLElementImpl implements HTMLSelectElement, HTMLFormControl {
    private HTMLCollection    options_;

    @Override
    public String getType() {
        return getAttribute("type");
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
    public int getSelectedIndex() {
        final NodeList options;
        int i;

        // Use getElementsByTagName() which creates a snapshot of all the
        // OPTION elements under this SELECT. Access to the returned NodeList
        // is very fast and the snapshot solves many synchronization problems.
        // Locate the first selected OPTION and return its index. Note that
        // the OPTION might be under an OPTGROUP.
        options = getElementsByTagName("OPTION");
        for (i = 0; i < options.getLength(); ++i) {
            if (((HTMLOptionElement) options.item(i)).getSelected()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void setSelectedIndex(final int selectedIndex) {
        final NodeList options;
        int i;

        // Use getElementsByTagName() which creates a snapshot of all the
        // OPTION elements under this SELECT. Access to the returned NodeList
        // is very fast and the snapshot solves many synchronization problems.
        // Change the select so all OPTIONs are off, except for the
        // selectIndex-th one.
        options = getElementsByTagName("OPTION");
        for (i = 0; i < options.getLength(); ++i) {
            ((HTMLOptionElementImpl) options.item(i)).setSelected(i == selectedIndex);
        }
    }

    @Override
    public HTMLCollection getOptions() {
        if (options_ == null) {
            options_ = new HTMLCollectionImpl(this, HTMLCollectionImpl.OPTION);
        }
        return options_;
    }

    @Override
    public int getLength() {
        return getOptions().getLength();
    }

    @Override
    public boolean getDisabled() {
        return getBinary("disabled");
    }

    @Override
    public void setDisabled(final boolean disabled) {
        setAttribute("disabled", disabled);
    }

    @Override
    public boolean getMultiple() {
        return getBinary("multiple");
    }

    @Override
    public void setMultiple(final boolean multiple) {
        setAttribute("multiple", multiple);
    }

    @Override
    public String getName() {
        return getAttribute("name");
    }

    @Override
    public void setName(final String name) {
        setAttribute("name", name);
    }

    @Override
    public int getSize() {
        return getInteger(getAttribute("size"));
    }

    @Override
    public void setSize(final int size) {
        setAttribute("size", String.valueOf(size));
    }

    @Override
    public int getTabIndex() {
        return getInteger(getAttribute("tabindex"));
    }

    @Override
    public void setTabIndex(final int tabIndex) {
        setAttribute("tabindex", String.valueOf(tabIndex));
    }

    @Override
    public void add(final HTMLElement element, final HTMLElement before) {
        insertBefore(element, before);
    }

    @Override
    public void remove(final int index) {
        final NodeList options;
        final Node removed;

        // Use getElementsByTagName() which creates a snapshot of all the
        // OPTION elements under this SELECT. Access to the returned NodeList
        // is very fast and the snapshot solves many synchronization problems.
        // Remove the indexed OPTION from it's parent, this might be this
        // SELECT or an OPTGROUP.
        options = getElementsByTagName("OPTION");
        removed = options.item(index);
        if (removed != null) {
            removed.getParentNode().removeChild(removed);
        }
    }

    @Override
    public void               blur() {
        // No scripting in server-side DOM. This method is moot.
    }

    @Override
    public void               focus() {
        // No scripting in server-side DOM. This method is moot.
    }

    /**
     * Explicit implementation of getChildNodes() to avoid problems with
     * overriding the getLength() method hidden in the super class.
     */
    @Override
    public NodeList getChildNodes() {
        return getChildNodesUnoptimized();
    }

    /**
     * Explicit implementation of cloneNode() to ensure that cache used
     * for getOptions() gets cleared.
     */
    @Override
    public Node cloneNode(final boolean deep) {
        final HTMLSelectElementImpl clonedNode = (HTMLSelectElementImpl) super.cloneNode(deep);
        clonedNode.options_ = null;
        return clonedNode;
    }

    /**
     * Constructor requires owner document.
     *
     * @param owner The owner HTML document
     */
    public HTMLSelectElementImpl(final HTMLDocumentImpl owner, final String name) {
        super(owner, name);
    }
}

