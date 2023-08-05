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

import org.w3c.dom.html.HTMLAnchorElement;

/**
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLAnchorElement
 * @see org.htmlunit.cyberneko.xerces.dom.ElementImpl
 */
public class HTMLAnchorElementImpl extends HTMLElementImpl implements HTMLAnchorElement {

    @Override
    public String getAccessKey() {
        String    accessKey;

        // Make sure that the access key is a single character.
        accessKey = getAttribute("accesskey");
        if (accessKey != null && accessKey.length() > 1) {
            accessKey = accessKey.substring(0, 1);
        }
        return accessKey;
    }

    @Override
    public void setAccessKey(String accessKey) {
        // Make sure that the access key is a single character.
        if (accessKey != null && accessKey.length() > 1) {
            accessKey = accessKey.substring(0, 1);
        }
        setAttribute("accesskey", accessKey);
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
    public String getCoords() {
        return getAttribute("coords");
    }

    @Override
    public void setCoords(final String coords) {
        setAttribute("coords", coords);
    }

    @Override
    public String getHref() {
        return getAttribute("href");
    }

    @Override
    public void setHref(final String href) {
        setAttribute("href", href);
    }

    @Override
    public String getHreflang() {
        return getAttribute("hreflang");
    }

    @Override
    public void setHreflang(final String hreflang) {
        setAttribute("hreflang", hreflang);
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
    public String getRel() {
        return getAttribute("rel");
    }

    @Override
    public void setRel(final String rel) {
        setAttribute("rel", rel);
    }

    @Override
    public String getRev() {
        return getAttribute("rev");
    }

    @Override
    public void setRev(final String rev) {
        setAttribute("rev", rev);
    }

    @Override
    public String getShape() {
        return capitalize(getAttribute("shape"));
    }

    @Override
    public void setShape(final String shape) {
        setAttribute("shape", shape);
    }

    @Override
    public int getTabIndex() {
        return this.getInteger(getAttribute("tabindex"));
    }

    @Override
    public void setTabIndex(final int tabIndex) {
        setAttribute("tabindex", String.valueOf(tabIndex));
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
    public String getType() {
        return getAttribute("type");
    }

    @Override
    public void setType(final String type) {
        setAttribute("type", type);
    }

    @Override
    public void blur() {
        // No scripting in server-side DOM. This method is moot.
    }

    @Override
    public void focus() {
        // No scripting in server-side DOM. This method is moot.
    }

    /**
     * Constructor requires owner document.
     *
     * @param owner The owner HTML document
     */
    public HTMLAnchorElementImpl(final HTMLDocumentImpl owner, final String name) {
        super(owner, name);
    }
}

