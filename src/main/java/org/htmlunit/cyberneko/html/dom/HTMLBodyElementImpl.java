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

import org.w3c.dom.html.HTMLBodyElement;

/**
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLBodyElement
 * @see org.htmlunit.cyberneko.xerces.dom.ElementImpl
 */
public class HTMLBodyElementImpl
    extends HTMLElementImpl
    implements HTMLBodyElement
{

    @Override
    public String getALink()
    {
        return getAttribute( "alink" );
    }


    @Override
    public void setALink(String aLink)
    {
        setAttribute( "alink", aLink );
    }


    @Override
    public String getBackground()
    {
        return getAttribute( "background" );
    }


    @Override
    public void setBackground( String background )
    {
        setAttribute( "background", background );
    }


    @Override
    public String getBgColor()
    {
        return getAttribute( "bgcolor" );
    }


    @Override
    public void setBgColor(String bgColor)
    {
        setAttribute( "bgcolor", bgColor );
    }


    @Override
    public String getLink()
    {
        return getAttribute( "link" );
    }


    @Override
    public void setLink(String link)
    {
        setAttribute( "link", link );
    }


    @Override
    public String getText()
    {
        return getAttribute( "text" );
    }


    @Override
    public void setText(String text)
    {
        setAttribute( "text", text );
    }


    @Override
    public String getVLink()
    {
        return getAttribute( "vlink" );
    }


    @Override
    public void  setVLink(String vLink)
    {
        setAttribute( "vlink", vLink );
    }


      /**
     * Constructor requires owner document.
     *
     * @param owner The owner HTML document
     */
    public HTMLBodyElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }


}

