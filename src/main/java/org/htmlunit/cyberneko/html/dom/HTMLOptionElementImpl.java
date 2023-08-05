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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLOptionElement;
import org.w3c.dom.html.HTMLSelectElement;

/**
 * @author <a href="mailto:arkin@openxml.org">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLOptionElement
 * @see org.htmlunit.cyberneko.xerces.dom.ElementImpl
 */
public class HTMLOptionElementImpl extends HTMLElementImpl implements HTMLOptionElement {

    @Override
    public boolean getDefaultSelected() {
        // ! NOT FULLY IMPLEMENTED !
        return getBinary("default-selected");
    }

    @Override
    public void setDefaultSelected(final boolean defaultSelected) {
        // ! NOT FULLY IMPLEMENTED !
        setAttribute("default-selected", defaultSelected);
    }

    @Override
    public String getText() {
        Node child;
        final StringBuilder text = new StringBuilder();

        // Find the Text nodes contained within this element and return their
        // concatenated value. Required to go around comments, entities, etc.
        child = getFirstChild();
        while (child != null) {
            if (child instanceof Text) {
                text.append(((Text) child).getData());
            }
            child = child.getNextSibling();
        }
        return text.toString();
    }

    @Override
    public int getIndex() {
        Node parent;
        final NodeList options;
        int i;

        // Locate the parent SELECT. Note that this OPTION might be inside a
        // OPTGROUP inside the SELECT. Or it might not have a parent SELECT.
        // Everything is possible. If no parent is found, return -1.
        parent = getParentNode();
        while (parent != null && !(parent instanceof HTMLSelectElement)) {
            parent = parent.getParentNode();
        }
        if (parent != null) {
            // Use getElementsByTagName() which creates a snapshot of all the
            // OPTION elements under the SELECT. Access to the returned NodeList
            // is very fast and the snapshot solves many synchronization problems.
            options = ((HTMLElement) parent).getElementsByTagName("OPTION");
            for (i = 0; i < options.getLength(); ++i) {
                if (options.item(i) == this) {
                    return i;
                }
            }
        }
        return -1;
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
    public String getLabel() {
        return capitalize(getAttribute("label"));
    }

    @Override
    public void setLabel(final String label) {
        setAttribute("label", label);
    }

    @Override
    public boolean getSelected() {
        return getBinary("selected");
    }

    @Override
    public void setSelected(final boolean selected) {
        setAttribute("selected", selected);
    }

    @Override
    public String getValue() {
        return getAttribute("value");
    }

    @Override
    public void setValue(final String value) {
        setAttribute("value", value);
    }

    /**
     * Constructor requires owner document.
     *
     * @param owner The owner HTML document
     */
    public HTMLOptionElementImpl(final HTMLDocumentImpl owner, final String name) {
        super(owner, name);
    }
}
