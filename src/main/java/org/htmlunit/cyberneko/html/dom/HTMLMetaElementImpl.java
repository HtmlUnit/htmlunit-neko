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

import org.w3c.dom.html.HTMLMetaElement;

/**
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLMetaElement
 * @see org.htmlunit.cyberneko.xerces.dom.ElementImpl
 */
public class HTMLMetaElementImpl
    extends HTMLElementImpl
    implements HTMLMetaElement
{

    @Override
    public String getContent()
    {
        return getAttribute("content");
    }


    @Override
    public void setContent(final String content)
    {
        setAttribute("content", content);
    }



      @Override
    public String getHttpEquiv()
    {
        return getAttribute("http-equiv");
    }


    @Override
    public void setHttpEquiv(final String httpEquiv)
    {
        setAttribute("http-equiv", httpEquiv);
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
    public String getScheme()
    {
        return getAttribute("scheme");
    }


    @Override
    public void setScheme(final String scheme)
    {
        setAttribute("scheme", scheme);
    }


    /**
     * Constructor requires owner document.
     *
     * @param owner The owner HTML document
     */
    public HTMLMetaElementImpl( HTMLDocumentImpl owner, String name)
    {
        super( owner, name);
    }


}

