/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.htmlunit.html.dom;

import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLMapElement;

/**
 * @xerces.internal
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLMapElement
 * @see net.sourceforge.htmlunit.xerces.dom.ElementImpl
 */
public class HTMLMapElementImpl
    extends HTMLElementImpl
    implements HTMLMapElement
{

    private static final long serialVersionUID = 7520887584251976392L;

    @Override
    public HTMLCollection getAreas()
    {
        if ( _areas == null )
            _areas = new HTMLCollectionImpl( this, HTMLCollectionImpl.AREA );
        return _areas;
    }


      @Override
    public String getName()
    {
        return getAttribute( "name" );
    }


    @Override
    public void setName( String name )
    {
        setAttribute( "name", name );
    }

    /**
     * Explicit implementation of cloneNode() to ensure that cache used
     * for getAreas() gets cleared.
     */
    @Override
    public Node cloneNode( boolean deep )
    {
        HTMLMapElementImpl clonedNode = (HTMLMapElementImpl)super.cloneNode( deep );
        clonedNode._areas = null;
        return clonedNode;
    }

    /**
     * Constructor requires owner document.
     *
     * @param owner The owner HTML document
     */
    public HTMLMapElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }


    private HTMLCollection    _areas;


}

