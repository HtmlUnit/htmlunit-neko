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

import org.w3c.dom.html.HTMLButtonElement;

/**
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLButtonElement
 * @see org.htmlunit.cyberneko.xerces.dom.ElementImpl
 */
public class HTMLButtonElementImpl
    extends HTMLElementImpl
    implements HTMLButtonElement, HTMLFormControl
{

    @Override
    public String getAccessKey()
    {
        String    accessKey;

        // Make sure that the access key is a single character.
        accessKey = getAttribute("accesskey");
        if ( accessKey != null && accessKey.length() > 1 )
            accessKey = accessKey.substring( 0, 1 );
        return accessKey;
    }

    @Override
    public void setAccessKey(String accessKey ) {
        // Make sure that the access key is a single character.
        if (accessKey != null && accessKey.length() > 1) {
            accessKey = accessKey.substring(0, 1);
        }
        setAttribute("accesskey", accessKey );
    }

    @Override
    public boolean getDisabled() {
        return getBinary("disabled");
    }

    @Override
    public void setDisabled(final boolean disabled)
    {
        setAttribute("disabled", disabled );
    }


    @Override
    public String getName()
    {
        return getAttribute("name");
    }


    @Override
    public void setName(final String name)
    {
        setAttribute("name", name);
    }


    @Override
    public int getTabIndex()
    {
        try
        {
            return Integer.parseInt( getAttribute("tabindex"));
        }
        catch ( NumberFormatException except )
        {
            return 0;
        }
    }


    @Override
    public void setTabIndex(final int tabIndex)
    {
        setAttribute("tabindex", String.valueOf(tabIndex));
    }


    @Override
    public String getType()
    {
        return capitalize( getAttribute("type"));
    }


      @Override
    public String getValue()
    {
        return getAttribute("value");
    }


    @Override
    public void setValue(final String value )
    {
        setAttribute("value", value );
    }


    /**
     * Constructor requires owner document.
     *
     * @param owner The owner HTML document
     */
    public HTMLButtonElementImpl( HTMLDocumentImpl owner, String name)
    {
        super( owner, name);
    }


}

