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
import org.w3c.dom.Text;
import org.w3c.dom.html.HTMLScriptElement;

/**
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLScriptElement
 * @see org.htmlunit.cyberneko.xerces.dom.ElementImpl
 */
public class HTMLScriptElementImpl extends HTMLElementImpl implements HTMLScriptElement {

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
    public void setText(final String text) {
        Node    child;
        Node    next;

        // Delete all the nodes and replace them with a single Text node.
        // This is the only approach that can handle comments and other nodes.
        child = getFirstChild();
        while (child != null) {
            next = child.getNextSibling();
            removeChild(child);
            child = next;
        }
        insertBefore(getOwnerDocument().createTextNode(text), getFirstChild());
    }

    @Override
    public String getHtmlFor() {
        return getAttribute("for");
    }

    @Override
    public void setHtmlFor(final String htmlFor) {
        setAttribute("for", htmlFor);
    }

    @Override
    public String getEvent() {
        return getAttribute("event");
    }

    @Override
    public void setEvent(final String event) {
        setAttribute("event", event);
    }

    @Override
    public String getCharset() {
        return getAttribute("charset");
    }

    @Override
    public void setCharset(final String charset) {
        setAttribute("charset", charset);
    }

    @Override
    public boolean getDefer() {
        return getBinary("defer");
    }

    @Override
    public void setDefer(final boolean defer) {
        setAttribute("defer", defer);
    }

    @Override
    public String getSrc() {
        return getAttribute("src");
    }

    @Override
    public void setSrc(final String src) {
        setAttribute("src", src);
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
    public HTMLScriptElementImpl(final HTMLDocumentImpl owner, final String name) {
        super(owner, name);
    }
}
