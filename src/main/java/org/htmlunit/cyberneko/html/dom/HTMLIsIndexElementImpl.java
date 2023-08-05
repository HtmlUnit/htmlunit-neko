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

import org.w3c.dom.html.HTMLIsIndexElement;

/**
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLIsIndexElement
 * @see org.htmlunit.cyberneko.xerces.dom.ElementImpl
 */
public class HTMLIsIndexElementImpl
    extends HTMLElementImpl
    implements HTMLIsIndexElement
{

    @Override
    public String getPrompt()
    {
        return getAttribute("prompt");
    }


    @Override
    public void setPrompt(final String prompt )
    {
        setAttribute("prompt", prompt );
    }


    /**
     * Constructor requires owner document.
     *
     * @param owner The owner HTML document
     */
    public HTMLIsIndexElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }


}

