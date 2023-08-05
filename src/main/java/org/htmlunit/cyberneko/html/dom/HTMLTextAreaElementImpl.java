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

import org.w3c.dom.html.HTMLTextAreaElement;

/**
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLTextAreaElement
 * @see org.htmlunit.cyberneko.xerces.dom.ElementImpl
 */
public class HTMLTextAreaElementImpl extends HTMLElementImpl implements HTMLTextAreaElement, HTMLFormControl {

    @Override
    public String getDefaultValue() {
        // ! NOT FULLY IMPLEMENTED !
        return getAttribute("default-value");
    }

    @Override
    public void setDefaultValue(final String defaultValue) {
        // ! NOT FULLY IMPLEMENTED !
        setAttribute("default-value", defaultValue);
    }

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
    public int getCols() {
        return getInteger(getAttribute("cols"));
    }

    @Override
    public void setCols(final int cols) {
        setAttribute("cols", String.valueOf(cols));
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
    public String getName() {
        return getAttribute("name");
    }

    @Override
    public void setName(final String name) {
        setAttribute("name", name);
    }

    @Override
    public boolean getReadOnly() {
        return getBinary("readonly");
    }

    @Override
    public void setReadOnly(final boolean readOnly) {
        setAttribute("readonly", readOnly);
    }

    @Override
    public int getRows() {
        return getInteger(getAttribute("rows"));
    }

    @Override
    public void setRows(final int rows) {
        setAttribute("rows", String.valueOf(rows));
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
    public void blur() {
        // No scripting in server-side DOM. This method is moot.
    }

    @Override
    public void focus() {
        // No scripting in server-side DOM. This method is moot.
    }

    @Override
    public void select() {
        // No scripting in server-side DOM. This method is moot.
    }

    /**
     * Constructor requires owner document.
     *
     * @param owner The owner HTML document
     */
    public HTMLTextAreaElementImpl(final HTMLDocumentImpl owner, final String name) {
        super(owner, name);
    }
}
