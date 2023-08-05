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
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLFormElement;

/**
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLFormElement
 * @see org.htmlunit.cyberneko.xerces.dom.ElementImpl
 */
public class HTMLFormElementImpl extends HTMLElementImpl implements HTMLFormElement {
    /**
     * Collection of all elements contained in this FORM.
     */
    private HTMLCollectionImpl    elements_;

    @Override
    public HTMLCollection getElements() {
        if (elements_ == null) {
            elements_ = new HTMLCollectionImpl(this, HTMLCollectionImpl.ELEMENT);
        }
        return elements_;
    }

    @Override
    public int getLength() {
        return getElements().getLength();
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
    public String getAcceptCharset() {
        return getAttribute("accept-charset");
    }

    @Override
    public void setAcceptCharset(final String acceptCharset) {
        setAttribute("accept-charset", acceptCharset);
    }

    @Override
    public String getAction() {
        return getAttribute("action");
    }

    @Override
    public void setAction(final String action) {
        setAttribute("action", action);
    }

    @Override
    public String getEnctype() {
        return getAttribute("enctype");
    }

    @Override
    public void setEnctype(final String enctype) {
        setAttribute("enctype", enctype);
    }

    @Override
    public String getMethod() {
        return capitalize(getAttribute("method"));
    }

    @Override
    public void setMethod(final String method) {
        setAttribute("method", method);
    }

    @Override
    public String getTarget() {
        return getAttribute("target");
    }

    @Override
    public void setTarget(final String target) {
        setAttribute("target", target);
    }

    @Override
    public void submit() {
        // No scripting in server-side DOM. This method is moot.
    }

    @Override
    public void reset() {
        // No scripting in server-side DOM. This method is moot.
    }

    /*
     * Explicit implementation of getChildNodes() to avoid problems with
     * overriding the getLength() method hidden in the super class.
     */
    @Override
    public NodeList getChildNodes() {
        return getChildNodesUnoptimized();
    }

    /**
     * Explicit implementation of cloneNode() to ensure that cache used
     * for getElements() gets cleared.
     */
    @Override
    public Node cloneNode(final boolean deep) {
        final HTMLFormElementImpl clonedNode = (HTMLFormElementImpl) super.cloneNode(deep);
        clonedNode.elements_ = null;
        return clonedNode;
    }

    /**
     * Constructor requires owner document.
     *
     * @param owner The owner HTML document
     */
    public HTMLFormElementImpl(final HTMLDocumentImpl owner, final String name) {
        super(owner, name);
    }
}

