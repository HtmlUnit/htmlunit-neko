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

import org.w3c.dom.html.HTMLAreaElement;

/**
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLAreaElement
 * @see org.htmlunit.cyberneko.xerces.dom.ElementImpl
 */
public class HTMLAreaElementImpl
    extends HTMLElementImpl
    implements HTMLAreaElement
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
    public void setAccessKey(final String accessKey )
    {
        // Make sure that the access key is a single character.
        if ( accessKey != null && accessKey.length() > 1 )
            accessKey = accessKey.substring( 0, 1 );
        setAttribute("accesskey", accessKey );
    }


    @Override
    public String getAlt()
    {
        return getAttribute("alt");
    }


    @Override
    public void setAlt(final String alt )
    {
        setAttribute("alt", alt );
    }

    @Override
    public String getCoords()
    {
        return getAttribute("coords");
    }


    @Override
    public void setCoords(final String coords )
    {
        setAttribute("coords", coords );
    }


    @Override
    public String getHref()
    {
        return getAttribute("href");
    }


    @Override
    public void setHref(final String href )
    {
        setAttribute("href", href );
    }


    @Override
    public boolean getNoHref()
    {
        return getBinary("nohref");
    }


    @Override
    public void setNoHref( boolean noHref )
    {
        setAttribute("nohref", noHref );
    }


    @Override
    public String getShape()
    {
        return capitalize( getAttribute("shape"));
    }


    @Override
    public void setShape(final String shape )
    {
        setAttribute("shape", shape );
    }


    @Override
    public int getTabIndex()
    {
        return getInteger( getAttribute("tabindex"));
    }


    @Override
    public void setTabIndex( int tabIndex )
    {
        setAttribute("tabindex", String.valueOf( tabIndex ));
    }


    @Override
    public String getTarget()
    {
        return getAttribute("target");
    }


    @Override
    public void setTarget(final String target )
    {
        setAttribute("target", target );
    }


    /**
     * Constructor requires owner document.
     *
     * @param owner The owner HTML document
     */
    public HTMLAreaElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }

}

