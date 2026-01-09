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

import org.w3c.dom.html.HTMLInputElement;

/**
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLInputElement
 * @see org.htmlunit.cyberneko.xerces.dom.ElementImpl
 */
public class HTMLInputElementImpl extends HTMLElementImpl implements HTMLInputElement, HTMLFormControl {

    @Override
    public String getDefaultValue() {
        // ! NOT FULLY IMPLEMENTED !
        return getAttribute("defaultValue");
    }

    @Override
    public void setDefaultValue(final String defaultValue) {
        // ! NOT FULLY IMPLEMENTED !
        setAttribute("defaultValue", defaultValue);
    }

    @Override
    public boolean getDefaultChecked() {
        // ! NOT FULLY IMPLEMENTED !
        return getBinary("defaultChecked");
    }

    @Override
    public void setDefaultChecked(final boolean defaultChecked) {
        // ! NOT FULLY IMPLEMENTED !
        setAttribute("defaultChecked", defaultChecked);
    }

    @Override
    public String getAccept() {
        return getAttribute("accept");
    }

    @Override
    public void setAccept(final String accept) {
        setAttribute("accept", accept);
    }

    @Override
    public String getAccessKey() {
        // Make sure that the access key is a single character.
        String accessKey = getAttribute("accesskey");
        if (accessKey.length() > 1) {
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
    public String getAlign() {
        return capitalize(getAttribute("align"));
    }

    @Override
    public void setAlign(final String align) {
        setAttribute("align", align);
    }

    @Override
    public String getAlt() {
        return getAttribute("alt");
    }

    @Override
    public void setAlt(final String alt) {
        setAttribute("alt", alt);
    }

    @Override
    public boolean getChecked() {
        return getBinary("checked");
    }

    @Override
    public void setChecked(final boolean checked) {
        setAttribute("checked", checked);
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
    public int getMaxLength() {
        return getInteger(getAttribute("maxlength"));
    }

    @Override
    public void setMaxLength(final int maxLength) {
        setAttribute("maxlength", String.valueOf(maxLength));
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
    public String getSize() {
        return getAttribute("size");
    }

    @Override
    public void setSize(final String size) {
        setAttribute("size", size);
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
    public int getTabIndex() {
        try {
            return Integer.parseInt(getAttribute("tabindex"));
        }
        catch (final NumberFormatException expected) {
            return 0;
        }
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
    public String getUseMap() {
        return getAttribute("useMap");
    }

    @Override
    public void setUseMap(final String useMap) {
        setAttribute("useMap", useMap);
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

    @Override
    public void click() {
        // No scripting in server-side DOM. This method is moot.
    }

    /**
     * Constructor requires owner document.
     *
     * @param owner The owner HTML document
     */
    public HTMLInputElementImpl(final HTMLDocumentImpl owner, final String name) {
        super(owner, name);
    }
}

