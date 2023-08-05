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

import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Locale;

import org.htmlunit.cyberneko.xerces.dom.DocumentImpl;
import org.htmlunit.cyberneko.xerces.dom.ElementImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLBodyElement;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLDocument;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLFrameSetElement;
import org.w3c.dom.html.HTMLHeadElement;
import org.w3c.dom.html.HTMLHtmlElement;
import org.w3c.dom.html.HTMLTitleElement;

/**
 * Implements an HTML document. Provides access to the top level element in the
 * document, its body and title.
 * <P>
 * Several methods create new nodes of all basic types (comment, text, element,
 * etc.). These methods create new nodes but do not place them in the document
 * tree. The nodes may be placed in the document tree using {@link
 * org.w3c.dom.Node#appendChild} or {@link org.w3c.dom.Node#insertBefore}, or
 * they may be placed in some other document tree.
 * <P>
 * Note: &lt;FRAMESET&gt; documents are not supported at the moment, neither
 * are direct document writing ({@link #open}, {@link #write}) and HTTP attribute
 * methods ({@link #getURL}, {@link #getCookie}).
 * <p>
 *
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLDocument
 */
public class HTMLDocumentImpl
    extends DocumentImpl
    implements HTMLDocument
{

    /**
     * Holds <code>HTMLCollectionImpl</code> object with live collection of all
     * anchors in document. This reference is on demand only once.
     */
    private HTMLCollectionImpl    _anchors;


    /**
     * Holds <code>HTMLCollectionImpl</code> object with live collection of all
     * forms in document. This reference is on demand only once.
     */
    private HTMLCollectionImpl    _forms;


    /**
     * Holds <code>HTMLCollectionImpl</code> object with live collection of all
     * images in document. This reference is on demand only once.
     */
    private HTMLCollectionImpl    _images;


    /**
     * Holds <code>HTMLCollectionImpl</code> object with live collection of all
     * links in document. This reference is on demand only once.
     */
    private HTMLCollectionImpl    _links;


    /**
     * Holds <code>HTMLCollectionImpl</code> object with live collection of all
     * applets in document. This reference is on demand only once.
     */
    private HTMLCollectionImpl    _applets;


    /**
     * Holds string writer used by direct manipulation operation ({@link #open}.
     * {@link #write}, etc) to write new contents into the document and parse
     * that text into a document tree.
     */
    private StringWriter        _writer;


    /**
     * Holds names and classes of HTML element types. When an element with a
     * particular tag name is created, the matching {@link java.lang.Class}
     * is used to create the element object. For example, &lt;A&gt; matches
     * {@link HTMLAnchorElementImpl}. This static table is shared across all
     * HTML documents.
     *
     * @see #createElement
     */
    private static final HashMap<String, Class<? extends HTMLElementImpl>> _elementTypesHTML = new HashMap<>();


    /**
     * Signature used to locate constructor of HTML element classes. This
     * static array is shared across all HTML documents.
     *
     * @see #createElement
     */
    private static final Class<?>[]    _elemClassSigHTML =
                new Class[] { HTMLDocumentImpl.class, String.class };

    static {
        _elementTypesHTML.put("A", HTMLAnchorElementImpl.class);
        _elementTypesHTML.put("APPLET", HTMLAppletElementImpl.class);
        _elementTypesHTML.put("AREA", HTMLAreaElementImpl.class);
        _elementTypesHTML.put("BASE",  HTMLBaseElementImpl.class);
        _elementTypesHTML.put("BASEFONT", HTMLBaseFontElementImpl.class);
        _elementTypesHTML.put("BLOCKQUOTE", HTMLQuoteElementImpl.class);
        _elementTypesHTML.put("BODY", HTMLBodyElementImpl.class);
        _elementTypesHTML.put("BR", HTMLBRElementImpl.class);
        _elementTypesHTML.put("BUTTON", HTMLButtonElementImpl.class);
        _elementTypesHTML.put("DEL", HTMLModElementImpl.class);
        _elementTypesHTML.put("DIR", HTMLDirectoryElementImpl.class);
        _elementTypesHTML.put("DIV",  HTMLDivElementImpl.class);
        _elementTypesHTML.put("DL", HTMLDListElementImpl.class);
        _elementTypesHTML.put("FIELDSET", HTMLFieldSetElementImpl.class);
        _elementTypesHTML.put("FONT", HTMLFontElementImpl.class);
        _elementTypesHTML.put("FORM", HTMLFormElementImpl.class);
        _elementTypesHTML.put("FRAME", HTMLFrameElementImpl.class);
        _elementTypesHTML.put("FRAMESET", HTMLFrameSetElementImpl.class);
        _elementTypesHTML.put("HEAD", HTMLHeadElementImpl.class);
        _elementTypesHTML.put("H1", HTMLHeadingElementImpl.class);
        _elementTypesHTML.put("H2", HTMLHeadingElementImpl.class);
        _elementTypesHTML.put("H3", HTMLHeadingElementImpl.class);
        _elementTypesHTML.put("H4", HTMLHeadingElementImpl.class);
        _elementTypesHTML.put("H5", HTMLHeadingElementImpl.class);
        _elementTypesHTML.put("H6", HTMLHeadingElementImpl.class);
        _elementTypesHTML.put("HR", HTMLHRElementImpl.class);
        _elementTypesHTML.put("HTML", HTMLHtmlElementImpl.class);
        _elementTypesHTML.put("IFRAME", HTMLIFrameElementImpl.class);
        _elementTypesHTML.put("IMG", HTMLImageElementImpl.class);
        _elementTypesHTML.put("INPUT", HTMLInputElementImpl.class);
        _elementTypesHTML.put("INS", HTMLModElementImpl.class);
        _elementTypesHTML.put("ISINDEX", HTMLIsIndexElementImpl.class);
        _elementTypesHTML.put("LABEL", HTMLLabelElementImpl.class);
        _elementTypesHTML.put("LEGEND", HTMLLegendElementImpl.class);
        _elementTypesHTML.put("LI", HTMLLIElementImpl.class);
        _elementTypesHTML.put("LINK", HTMLLinkElementImpl.class);
        _elementTypesHTML.put("MAP", HTMLMapElementImpl.class);
        _elementTypesHTML.put("MENU", HTMLMenuElementImpl.class);
        _elementTypesHTML.put("META", HTMLMetaElementImpl.class);
        _elementTypesHTML.put("OBJECT", HTMLObjectElementImpl.class);
        _elementTypesHTML.put("OL", HTMLOListElementImpl.class);
        _elementTypesHTML.put("OPTGROUP", HTMLOptGroupElementImpl.class);
        _elementTypesHTML.put("OPTION", HTMLOptionElementImpl.class);
        _elementTypesHTML.put("P", HTMLParagraphElementImpl.class);
        _elementTypesHTML.put("PARAM", HTMLParamElementImpl.class);
        _elementTypesHTML.put("PRE", HTMLPreElementImpl.class);
        _elementTypesHTML.put("Q", HTMLQuoteElementImpl.class);
        _elementTypesHTML.put("SCRIPT", HTMLScriptElementImpl.class);
        _elementTypesHTML.put("SELECT", HTMLSelectElementImpl.class);
        _elementTypesHTML.put("STYLE", HTMLStyleElementImpl.class);
        _elementTypesHTML.put("TABLE", HTMLTableElementImpl.class);
        _elementTypesHTML.put("CAPTION", HTMLTableCaptionElementImpl.class);
        _elementTypesHTML.put("TD", HTMLTableCellElementImpl.class);
        _elementTypesHTML.put("TH", HTMLTableCellElementImpl.class);
        _elementTypesHTML.put("COL", HTMLTableColElementImpl.class);
        _elementTypesHTML.put("COLGROUP", HTMLTableColElementImpl.class);
        _elementTypesHTML.put("TR", HTMLTableRowElementImpl.class);
        _elementTypesHTML.put("TBODY", HTMLTableSectionElementImpl.class);
        _elementTypesHTML.put("THEAD", HTMLTableSectionElementImpl.class);
        _elementTypesHTML.put("TFOOT", HTMLTableSectionElementImpl.class);
        _elementTypesHTML.put("TEXTAREA", HTMLTextAreaElementImpl.class);
        _elementTypesHTML.put("TITLE", HTMLTitleElementImpl.class);
        _elementTypesHTML.put("UL", HTMLUListElementImpl.class);
    }

    /**
     */
    public HTMLDocumentImpl()
    {
        super();
    }


    @Override
    public synchronized Element getDocumentElement()
    {
        Node    html;
        Node    child;
        Node    next;

        // The document element is the top-level HTML element of the HTML
        // document. Only this element should exist at the top level.
        // If the HTML element is found, all other elements that might
        // precede it are placed inside the HTML element.
        html = getFirstChild();
        while ( html != null )
        {
            if ( html instanceof HTMLHtmlElement )
            {
                return (HTMLElement) html;
            }
            html = html.getNextSibling();
        }

        // HTML element must exist. Create a new element and dump the
        // entire contents of the document into it in the same order as
        // they appear now.
        html = new HTMLHtmlElementImpl( this, "HTML");
        child = getFirstChild();
        while ( child != null )
        {
            next = child.getNextSibling();
            html.appendChild( child );
            child = next;
        }
        appendChild( html );
        return (HTMLElement) html;
    }


    /**
     * Obtains the &lt;HEAD&gt; element in the document, creating one if does
     * not exist before. The &lt;HEAD&gt; element is the first element in the
     * &lt;HTML&gt; in the document. The &lt;HTML&gt; element is obtained by
     * calling {@link #getDocumentElement}. If the element does not exist, one
     * is created.
     * <P>
     * Called by {@link #getTitle}, {@link #setTitle}, {@link #getBody} and
     * {@link #setBody} to assure the document has the &lt;HEAD&gt; element
     * correctly placed.
     *
     * @return The &lt;HEAD&gt; element
     */
    public synchronized HTMLElement getHead()
    {
        Node    head;
        Node    html;
        Node    child;
        Node    next;

        // Call getDocumentElement() to get the HTML element that is also the
        // top-level element in the document. Get the first element in the
        // document that is called HEAD. Work with that.
        html = getDocumentElement();
        synchronized ( html )
        {
            head = html.getFirstChild();
            while ( head != null && ! ( head instanceof HTMLHeadElement ))
                head = head.getNextSibling();
            // HEAD exists but might not be first element in HTML: make sure
            // it is and return it.
            if ( head != null )
            {
                synchronized ( head )
                {
                    child = html.getFirstChild();
                    while ( child != null && child != head )
                    {
                        next = child.getNextSibling();
                        head.insertBefore( child, head.getFirstChild());
                        child = next;
                    }
                }
                return (HTMLElement) head;
            }

            // Head does not exist, create a new one, place it at the top of the
            // HTML element and return it.
            head = new HTMLHeadElementImpl( this, "HEAD");
            html.insertBefore( head, html.getFirstChild());
        }
        return (HTMLElement) head;
    }


    @Override
    public synchronized String getTitle()
    {
        HTMLElement head;
        NodeList    list;
        Node        title;

        // Get the HEAD element and look for the TITLE element within.
        // When found, make sure the TITLE is a direct child of HEAD,
        // and return the title's text (the Text node contained within).
        head = getHead();
        list = head.getElementsByTagName("TITLE");
        if ( list.getLength() > 0 ) {
            title = list.item( 0 );
            return ( (HTMLTitleElement) title ).getText();
        }
        // No TITLE found, return an empty string.
        return "";
    }


    @Override
    public synchronized void setTitle(final String newTitle )
    {
        HTMLElement head;
        NodeList    list;
        Node        title;

        // Get the HEAD element and look for the TITLE element within.
        // When found, make sure the TITLE is a direct child of HEAD,
        // and set the title's text (the Text node contained within).
        head = getHead();
        list = head.getElementsByTagName("TITLE");
        if ( list.getLength() > 0 ) {
            title = list.item( 0 );
            if ( title.getParentNode() != head )
                head.appendChild( title );
            ( (HTMLTitleElement) title ).setText( newTitle );
        }
        else
        {
            // No TITLE found, create a new element and place it at the end
            // of the HEAD element.
            title = new HTMLTitleElementImpl( this, "TITLE");
            ( (HTMLTitleElement) title ).setText( newTitle );
            head.appendChild( title );
        }
    }


    @Override
    public synchronized HTMLElement getBody()
    {
        Node    html;
        Node    head;
        Node    body;
        Node    child;
        Node    next;

        // Call getDocumentElement() to get the HTML element that is also the
        // top-level element in the document. Get the first element in the
        // document that is called BODY. Work with that.
        html = getDocumentElement();
        head = getHead();
        synchronized ( html )
        {
            body = head.getNextSibling();
            while ( body != null && ! ( body instanceof HTMLBodyElement )
                    && ! ( body instanceof HTMLFrameSetElement ))
                body = body.getNextSibling();

            // BODY/FRAMESET exists but might not be second element in HTML
            // (after HEAD): make sure it is and return it.
            if ( body != null )
            {
                synchronized ( body )
                {
                    child = head.getNextSibling();
                    while ( child != null && child != body )
                    {
                        next = child.getNextSibling();
                        body.insertBefore( child, body.getFirstChild());
                        child = next;
                    }
                }
                return (HTMLElement) body;
            }

            // BODY does not exist, create a new one, place it in the HTML element
            // right after the HEAD and return it.
            body = new HTMLBodyElementImpl( this, "BODY");
            html.appendChild( body );
        }
        return (HTMLElement) body;
    }


    @Override
    public synchronized void setBody( HTMLElement newBody )
    {
        Node    html;
        Node    body;
        Node    head;
        Node    child;
        NodeList list;

        synchronized ( newBody )
        {
            // Call getDocumentElement() to get the HTML element that is also the
            // top-level element in the document. Get the first element in the
            // document that is called BODY. Work with that.
            html = getDocumentElement();
            head = getHead();
            synchronized ( html )
            {
                list = this.getElementsByTagName("BODY");
                if ( list.getLength() > 0 ) {
                    // BODY exists but might not follow HEAD in HTML. If not,
                    // make it so and replce it. Start with the HEAD and make
                    // sure the BODY is the first element after the HEAD.
                    body = list.item( 0 );
                    synchronized ( body )
                    {
                        child = head;
                        while ( child != null )
                        {
                            if ( child instanceof Element )
                            {
                                if ( child != body )
                                    html.insertBefore( newBody, child );
                                else
                                    html.replaceChild( newBody, body );
                                return;
                            }
                            child = child.getNextSibling();
                        }
                        html.appendChild( newBody );
                    }
                    return;
                }
                // BODY does not exist, place it in the HTML element
                // right after the HEAD.
                html.appendChild( newBody );
            }
        }
    }


    @Override
    public synchronized Element getElementById(final String elementId )
    {
        Element idElement = super.getElementById(elementId);
        if (idElement != null) {
            return idElement;
        }
        return getElementById( elementId, this );
    }


    @Override
    public NodeList getElementsByName(final String elementName )
    {
        return new NameNodeListImpl( this, elementName );
    }


    @Override
    public final NodeList getElementsByTagName(final String tagName )
    {
        return super.getElementsByTagName( tagName.toUpperCase(Locale.ENGLISH));
    }


    @Override
    public final NodeList getElementsByTagNameNS(final String namespaceURI,
                                                  String localName )
    {
        if ( namespaceURI != null && namespaceURI.length() > 0 ) {
            return super.getElementsByTagNameNS( namespaceURI, localName.toUpperCase(Locale.ENGLISH));
        }
        return super.getElementsByTagName( localName.toUpperCase(Locale.ENGLISH));
    }


    /**
     * Xerces-specific constructor. "localName" is passed in, so we don't need
     * to create a new String for it.
     *
     * @param namespaceURI The namespace URI of the element to
     *                     create.
     * @param qualifiedName The qualified name of the element type to
     *                      instantiate.
     * @param localpart     The local name of the element to instantiate.
     * @return Element A new Element object with the following attributes:
     * @throws DOMException INVALID_CHARACTER_ERR: Raised if the specified
     *                      name contains an invalid character.
     */
    @Override
    public Element createElementNS(String namespaceURI, String qualifiedName,
                                   String localpart)
        throws DOMException
    {
        return createElementNS(namespaceURI, qualifiedName);
    }

    @Override
    public Element createElementNS(final String namespaceURI, String qualifiedName )
    {
        if ( namespaceURI == null || namespaceURI.length() == 0 ) {
            return createElement( qualifiedName );
        }
        return super.createElementNS( namespaceURI, qualifiedName );
    }


    @Override
    public Element createElement(final String tagName )
        throws DOMException
    {
        Class<?> elemClass;
        Constructor<?> cnst;

        // First, make sure tag name is all upper case, next get the associated
        // element class. If no class is found, generate a generic HTML element.
        // Do so also if an unexpected exception occurs.
        tagName = tagName.toUpperCase(Locale.ENGLISH);
        elemClass = _elementTypesHTML.get( tagName );
        if ( elemClass != null )
        {
            // Get the constructor for the element. The signature specifies an
            // owner document and a tag name. Use the constructor to instantiate
            // a new object and return it.
            try
            {
                cnst = elemClass.getConstructor( _elemClassSigHTML );
                return (Element) cnst.newInstance( new Object[] { this, tagName } );
            }
            catch (Exception e)
            {
                throw new IllegalStateException("HTM15 Tag '" + tagName + "' associated with an Element class that failed to construct.\n" + tagName, e);
            }
        }
        return new HTMLElementImpl( this, tagName );
    }


    /**
     * Creates an Attribute having this Document as its OwnerDoc.
     * Overrides {@link DocumentImpl#createAttribute} and returns
     * and attribute whose name is lower case.
     *
     * @param name The name of the attribute
     * @return An attribute whose name is all lower case
     * @throws DOMException INVALID_NAME_ERR if the attribute name
     *   is not acceptable
     */
    @Override
    public Attr createAttribute(final String name )
        throws DOMException
    {
        return super.createAttribute( name.toLowerCase(Locale.ENGLISH));
    }


    @Override
    public String getReferrer()
    {
        // Information not available on server side.
        return null;
    }


    @Override
    public String getDomain()
    {
        // Information not available on server side.
        return null;
    }


    @Override
    public String getURL()
    {
        // Information not available on server side.
        return null;
    }


    @Override
    public String getCookie()
    {
        // Information not available on server side.
        return null;
    }


    @Override
    public void setCookie(final String cookie )
    {
        // Information not available on server side.
    }


    @Override
    public HTMLCollection getImages()
    {
        // For more information see HTMLCollection#collectionMatch
        if ( _images == null )
            _images = new HTMLCollectionImpl( getBody(), HTMLCollectionImpl.IMAGE );
        return _images;
    }


    @Override
    public HTMLCollection getApplets()
    {
        // For more information see HTMLCollection#collectionMatch
        if ( _applets == null )
            _applets = new HTMLCollectionImpl( getBody(), HTMLCollectionImpl.APPLET );
        return _applets;
    }


    @Override
    public HTMLCollection getLinks()
    {
        // For more information see HTMLCollection#collectionMatch
        if ( _links == null )
            _links = new HTMLCollectionImpl( getBody(), HTMLCollectionImpl.LINK );
        return _links;
    }


    @Override
    public HTMLCollection getForms()
    {
        // For more information see HTMLCollection#collectionMatch
        if ( _forms == null )
            _forms = new HTMLCollectionImpl( getBody(), HTMLCollectionImpl.FORM );
        return _forms;
    }


    @Override
    public HTMLCollection getAnchors()
    {
        // For more information see HTMLCollection#collectionMatch
        if ( _anchors == null )
            _anchors = new HTMLCollectionImpl( getBody(), HTMLCollectionImpl.ANCHOR );
        return _anchors;
    }


    @Override
    public void open()
    {
        // When called an in-memory is prepared. The document tree is still
        // accessible the old way, until this writer is closed.
        if ( _writer == null )
            _writer = new StringWriter();
    }


    @Override
    public void close()
    {
        // ! NOT IMPLEMENTED, REQUIRES PARSER !
        if ( _writer != null )
        {
            _writer = null;
        }
    }


    @Override
    public void write(final String text )
    {
        // Write a string into the in-memory writer.
        if ( _writer != null )
            _writer.write( text );
    }


    @Override
    public void writeln(final String text )
    {
        // Write a line into the in-memory writer.
        if ( _writer != null )
            _writer.write( text + "\n");
    }


    @Override
    public Node cloneNode( boolean deep )
    {
        HTMLDocumentImpl newdoc = new HTMLDocumentImpl();
        cloneNode(newdoc, deep);
        return newdoc;
    }


    /* (non-Javadoc)
     * @see CoreDocumentImpl#canRenameElements()
     */
    @Override
    protected boolean canRenameElements(String newNamespaceURI, String newNodeName, ElementImpl el) {
        if (el.getNamespaceURI() != null) {
            // element is not HTML:
            // can be renamed if not changed to HTML
            return newNamespaceURI != null;
        }

        // check whether a class change is required
        Class<?> newClass = _elementTypesHTML.get(newNodeName.toUpperCase(Locale.ENGLISH));
        Class<?> oldClass = _elementTypesHTML.get(el.getTagName());
        return newClass == oldClass;
    }


    /**
     * Recursive method retreives an element by its <code>id</code> attribute.
     * Called by {@link #getElementById(String)}.
     *
     * @param elementId The <code>id</code> value to look for
     * @return The node in which to look for
     */
    private Element getElementById(final String elementId, Node node )
    {
        Node    child;
        Element    result;

        child = node.getFirstChild();
        while ( child != null )
        {
            if ( child instanceof Element )
            {
                if ( elementId.equals( ( (Element) child ).getAttribute("id")))
                    return (Element) child;
                result = getElementById( elementId, child );
                if ( result != null )
                    return result;
            }
            child = child.getNextSibling();
        }
        return null;
    }
}

