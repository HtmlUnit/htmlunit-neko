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

import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.htmlunit.cyberneko.filters.NamespaceBinder;
import org.htmlunit.cyberneko.util.FastHashMap;
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
public class HTMLDocumentImpl extends DocumentImpl implements HTMLDocument {

    /**
     * All valid HTML5 tags without deprecated elements
     * <a href="https://www.tutorialrepublic.com/html-reference/html5-tags.php">...</a>
     */
    private static final String[] HTML5ELEMENTS = {
        "A", "ABBR", "ADDRESS", "AREA", "ARTICLE", "ASIDE", "AUDIO",
        "B", "BASE", "BDI", "BDO", "BLOCKQUOTE", "BODY", "BR", "BUTTON",
        "CANVAS", "CAPTION", "CITE", "CODE", "COL", "COLGROUP", "DATA",
        "DATALIST", "DD", "DEL", "DETAILS", "DFN", "DIALOG", "DIV",
        "DL", "DT", "EM", "EMBED", "FIELDSET", "FIGCAPTION", "FIGURE",
        "FOOTER", "FORM", "HEAD", "HEADER", "HGROUP", "H1", "HR", "HTML",
        "I", "IFRAME", "IMG", "INPUT", "INS", "KBD", "KEYGEN", "LABEL",
        "LEGEND", "LI", "LINK", "MAIN", "MAP", "MARK", "MENU", "MENUITEM",
        "META", "METER", "NAV", "NOSCRIPT", "OBJECT", "OL", "OPTGROUP",
        "OPTION", "OUTPUT", "P", "PARAM", "PICTURE", "PRE", "PROGRESS",
        "Q", "RP", "RT", "RUBY", "S", "SAMP", "SCRIPT", "SECTION",
        "SELECT", "SMALL", "SOURCE", "SPAN", "STRONG", "STYLE", "SUB",
        "SUMMARY", "SUP", "SVG", "TABLE", "TBODY", "TD", "TEMPLATE",
        "TEXTAREA", "TFOOT", "TH", "THEAD", "TIME", "TITLE", "TR",
        "TRACK", "U", "UL", "VAR", "VIDEO", "WBR"
    };

    /**
     * Holds <code>HTMLCollectionImpl</code> object with live collection of all
     * anchors in document. This reference is on demand only once.
     */
    private HTMLCollectionImpl    anchors_;

    /**
     * Holds <code>HTMLCollectionImpl</code> object with live collection of all
     * forms in document. This reference is on demand only once.
     */
    private HTMLCollectionImpl    forms_;

    /**
     * Holds <code>HTMLCollectionImpl</code> object with live collection of all
     * images in document. This reference is on demand only once.
     */
    private HTMLCollectionImpl    images_;

    /**
     * Holds <code>HTMLCollectionImpl</code> object with live collection of all
     * links in document. This reference is on demand only once.
     */
    private HTMLCollectionImpl    links_;

    /**
     * Holds <code>HTMLCollectionImpl</code> object with live collection of all
     * applets in document. This reference is on demand only once.
     */
    private HTMLCollectionImpl    applets_;

    /**
     * Holds string writer used by direct manipulation operation ({@link #open}.
     * {@link #write}, etc) to write new contents into the document and parse
     * that text into a document tree.
     */
    private StringWriter        writer_;

    /**
     * Holds names and classes of HTML element types. When an element with a
     * particular tag name is created, the matching {@link java.lang.Class}
     * is used to create the element object. For example, &lt;A&gt; matches
     * {@link HTMLAnchorElementImpl}. This static table is shared across all
     * HTML documents.
     *
     * @see #createElement
     */
    private static final FastHashMap<String, ElementTypesHTMLHolder> ELEMENT_TYPES_HTML_LOWER
                            = new FastHashMap<>(11, 0.5f);
    private static final FastHashMap<String, ElementTypesHTMLHolder> ELEMENT_TYPES_HTML_UPPER
                            = new FastHashMap<>(11, 0.5f);

    /**
     * Signature used to locate constructor of HTML element classes. This
     * static array is shared across all HTML documents.
     *
     * @see #createElement
     */
    private static final Class<?>[] ELEMENT_CLASS_CTOR_SIGNATURE = new Class[] {HTMLDocumentImpl.class, String.class};

    static {
        final Map<String, Class<? extends HTMLElementImpl>> tMap = new HashMap<>();

        // register all HTML5 elements that are not deprecated as simple
        // HTMLElementImpl first and overwrite them later
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element
        Arrays.stream(HTML5ELEMENTS).forEach(t -> tMap.put(t, HTMLElementImpl.class));

        tMap.put("A", HTMLAnchorElementImpl.class);
        tMap.put("APPLET", HTMLAppletElementImpl.class);
        tMap.put("AREA", HTMLAreaElementImpl.class);
        tMap.put("BASE",  HTMLBaseElementImpl.class);
        tMap.put("BASEFONT", HTMLBaseFontElementImpl.class);
        tMap.put("BLOCKQUOTE", HTMLQuoteElementImpl.class);
        tMap.put("BODY", HTMLBodyElementImpl.class);
        tMap.put("BR", HTMLBRElementImpl.class);
        tMap.put("BUTTON", HTMLButtonElementImpl.class);
        tMap.put("DEL", HTMLModElementImpl.class);
        tMap.put("DIR", HTMLDirectoryElementImpl.class);
        tMap.put("DIV",  HTMLDivElementImpl.class);
        tMap.put("DL", HTMLDListElementImpl.class);
        tMap.put("FIELDSET", HTMLFieldSetElementImpl.class);
        tMap.put("FONT", HTMLFontElementImpl.class);
        tMap.put("FORM", HTMLFormElementImpl.class);
        tMap.put("FRAME", HTMLFrameElementImpl.class);
        tMap.put("FRAMESET", HTMLFrameSetElementImpl.class);
        tMap.put("HEAD", HTMLHeadElementImpl.class);
        tMap.put("H1", HTMLHeadingElementImpl.class);
        tMap.put("H2", HTMLHeadingElementImpl.class);
        tMap.put("H3", HTMLHeadingElementImpl.class);
        tMap.put("H4", HTMLHeadingElementImpl.class);
        tMap.put("H5", HTMLHeadingElementImpl.class);
        tMap.put("H6", HTMLHeadingElementImpl.class);
        tMap.put("HR", HTMLHRElementImpl.class);
        tMap.put("HTML", HTMLHtmlElementImpl.class);
        tMap.put("IFRAME", HTMLIFrameElementImpl.class);
        tMap.put("IMG", HTMLImageElementImpl.class);
        tMap.put("INPUT", HTMLInputElementImpl.class);
        tMap.put("INS", HTMLModElementImpl.class);
        tMap.put("ISINDEX", HTMLIsIndexElementImpl.class);
        tMap.put("LABEL", HTMLLabelElementImpl.class);
        tMap.put("LEGEND", HTMLLegendElementImpl.class);
        tMap.put("LI", HTMLLIElementImpl.class);
        tMap.put("LINK", HTMLLinkElementImpl.class);
        tMap.put("MAP", HTMLMapElementImpl.class);
        tMap.put("MENU", HTMLMenuElementImpl.class);
        tMap.put("META", HTMLMetaElementImpl.class);
        tMap.put("OBJECT", HTMLObjectElementImpl.class);
        tMap.put("OL", HTMLOListElementImpl.class);
        tMap.put("OPTGROUP", HTMLOptGroupElementImpl.class);
        tMap.put("OPTION", HTMLOptionElementImpl.class);
        tMap.put("P", HTMLParagraphElementImpl.class);
        tMap.put("PARAM", HTMLParamElementImpl.class);
        tMap.put("PRE", HTMLPreElementImpl.class);
        tMap.put("Q", HTMLQuoteElementImpl.class);
        tMap.put("SCRIPT", HTMLScriptElementImpl.class);
        tMap.put("SELECT", HTMLSelectElementImpl.class);
        tMap.put("STYLE", HTMLStyleElementImpl.class);
        tMap.put("TABLE", HTMLTableElementImpl.class);
        tMap.put("CAPTION", HTMLTableCaptionElementImpl.class);
        tMap.put("TD", HTMLTableCellElementImpl.class);
        tMap.put("TH", HTMLTableCellElementImpl.class);
        tMap.put("COL", HTMLTableColElementImpl.class);
        tMap.put("COLGROUP", HTMLTableColElementImpl.class);
        tMap.put("TR", HTMLTableRowElementImpl.class);
        tMap.put("TBODY", HTMLTableSectionElementImpl.class);
        tMap.put("THEAD", HTMLTableSectionElementImpl.class);
        tMap.put("TFOOT", HTMLTableSectionElementImpl.class);
        tMap.put("TEXTAREA", HTMLTextAreaElementImpl.class);
        tMap.put("TITLE", HTMLTitleElementImpl.class);
        tMap.put("UL", HTMLUListElementImpl.class);

        // also put all lowercase versions here to safe on lookup
        tMap.forEach((key, value) -> {

            final String uKey = key.toUpperCase(Locale.ENGLISH);
            final String lKey = key.toLowerCase(Locale.ENGLISH);

            try {
                final Constructor<? extends HTMLElementImpl> ctr = value.getConstructor(ELEMENT_CLASS_CTOR_SIGNATURE);

                final ElementTypesHTMLHolder holder = new ElementTypesHTMLHolder(uKey, ctr);
                ELEMENT_TYPES_HTML_UPPER.put(uKey, holder);
                ELEMENT_TYPES_HTML_LOWER.put(lKey, holder);
            }
            catch (NoSuchMethodException | SecurityException ex) {
                throw new IllegalStateException(
                        "HTM15 Tag '" + key
                            + "' associated with an Element class that failed to construct.\n" + key, ex);
            }
        });
    }

    static class ElementTypesHTMLHolder {
        public final String tagName_;
        public final Constructor<? extends HTMLElementImpl> ctr_;

        ElementTypesHTMLHolder(final String tagName, final Constructor<? extends HTMLElementImpl> ctr) {
            tagName_ = tagName;
            ctr_ = ctr;
        }
    }

    /**
     */
    public HTMLDocumentImpl() {
        super();
    }

    @Override
    public synchronized Element getDocumentElement() {
        // The document element is the top-level HTML element of the HTML
        // document. Only this element should exist at the top level.
        // If the HTML element is found, all other elements that might
        // precede it are placed inside the HTML element.
        Node html = getFirstChild();
        while (html != null) {
            if (html instanceof HTMLHtmlElement) {
                return (HTMLElement) html;
            }
            html = html.getNextSibling();
        }

        // HTML element must exist. Create a new element and dump the
        // entire contents of the document into it in the same order as
        // they appear now.
        html = new HTMLHtmlElementImpl(this, "HTML");
        Node child = getFirstChild();
        while (child != null) {
            final Node next = child.getNextSibling();
            html.appendChild(child);
            child = next;
        }
        appendChild(html);
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
    public synchronized HTMLElement getHead() {
        Node head;
        final Node html;
        Node child;
        Node next;

        // Call getDocumentElement() to get the HTML element that is also the
        // top-level element in the document. Get the first element in the
        // document that is called HEAD. Work with that.
        html = getDocumentElement();
        synchronized (html) {
            head = html.getFirstChild();
            while (head != null && !(head instanceof HTMLHeadElement)) {
                head = head.getNextSibling();
            }

            // HEAD exists but might not be first element in HTML: make sure
            // it is and return it.
            if (head != null) {
                synchronized (head) {
                    child = html.getFirstChild();
                    while (child != null && child != head) {
                        next = child.getNextSibling();
                        head.insertBefore(child, head.getFirstChild());
                        child = next;
                    }
                }
                return (HTMLElement) head;
            }

            // Head does not exist, create a new one, place it at the top of the
            // HTML element and return it.
            head = new HTMLHeadElementImpl(this, "HEAD");
            html.insertBefore(head, html.getFirstChild());
        }
        return (HTMLElement) head;
    }

    @Override
    public synchronized String getTitle() {
        final HTMLElement head;
        final NodeList list;
        final Node title;

        // Get the HEAD element and look for the TITLE element within.
        // When found, make sure the TITLE is a direct child of HEAD,
        // and return the title's text (the Text node contained within).
        head = getHead();
        list = head.getElementsByTagName("TITLE");
        if (list.getLength() > 0) {
            title = list.item(0);
            return ((HTMLTitleElement) title).getText();
        }
        // No TITLE found, return an empty string.
        return "";
    }

    @Override
    public synchronized void setTitle(final String newTitle) {
        final HTMLElement head;
        final NodeList list;
        final Node title;

        // Get the HEAD element and look for the TITLE element within.
        // When found, make sure the TITLE is a direct child of HEAD,
        // and set the title's text (the Text node contained within).
        head = getHead();
        list = head.getElementsByTagName("TITLE");
        if (list.getLength() > 0) {
            title = list.item(0);
            if (title.getParentNode() != head) {
                head.appendChild(title);
            }
            ((HTMLTitleElement) title).setText(newTitle);
        }
        else {
            // No TITLE found, create a new element and place it at the end
            // of the HEAD element.
            title = new HTMLTitleElementImpl(this, "TITLE");
            ((HTMLTitleElement) title).setText(newTitle);
            head.appendChild(title);
        }
    }

    @Override
    public synchronized HTMLElement getBody() {
        Node body;

        // Call getDocumentElement() to get the HTML element that is also the
        // top-level element in the document. Get the first element in the
        // document that is called BODY. Work with that.
        final Element html = getDocumentElement();
        final HTMLElement head = getHead();
        synchronized (html) {
            body = head.getNextSibling();
            while (body != null && !(body instanceof HTMLBodyElement)
                    && !(body instanceof HTMLFrameSetElement)) {
                body = body.getNextSibling();
            }

            // BODY/FRAMESET exists but might not be second element in HTML
            // (after HEAD): make sure it is and return it.
            if (body != null) {
                synchronized (body) {
                    Node child = head.getNextSibling();
                    while (child != null && child != body) {
                        final Node next = child.getNextSibling();
                        body.insertBefore(child, body.getFirstChild());
                        child = next;
                    }
                }
                return (HTMLElement) body;
            }

            // BODY does not exist, create a new one, place it in the HTML element
            // right after the HEAD and return it.
            body = new HTMLBodyElementImpl(this, "BODY");
            html.appendChild(body);
        }
        return (HTMLElement) body;
    }

    @Override
    public synchronized void setBody(final HTMLElement newBody) {
        synchronized (newBody) {
            // Call getDocumentElement() to get the HTML element that is also the
            // top-level element in the document. Get the first element in the
            // document that is called BODY. Work with that.
            final Element html = getDocumentElement();
            final HTMLElement head = getHead();
            synchronized (html) {
                final NodeList list = this.getElementsByTagName("BODY");
                if (list.getLength() > 0) {
                    // BODY exists but might not follow HEAD in HTML. If not,
                    // make it so and replace it. Start with the HEAD and make
                    // sure the BODY is the first element after the HEAD.
                    final Node body = list.item(0);
                    synchronized (body) {
                        Node child = head;
                        while (child != null) {
                            if (child instanceof Element) {
                                if (child != body) {
                                    html.insertBefore(newBody, child);
                                }
                                else {
                                    html.replaceChild(newBody, body);
                                }
                                return;
                            }
                            child = child.getNextSibling();
                        }
                        html.appendChild(newBody);
                    }
                    return;
                }
                // BODY does not exist, place it in the HTML element
                // right after the HEAD.
                html.appendChild(newBody);
            }
        }
    }

    @Override
    public synchronized Element getElementById(final String elementId) {
        final Element idElement = super.getElementById(elementId);
        if (idElement != null) {
            return idElement;
        }
        return getElementById(elementId, this);
    }

    @Override
    public NodeList getElementsByName(final String elementname) {
        return new NameNodeListImpl(this, elementname);
    }

    @Override
    public final NodeList getElementsByTagName(final String tagName) {
        return super.getElementsByTagName(tagName.toUpperCase(Locale.ENGLISH));
    }

    @Override
    public final NodeList getElementsByTagNameNS(final String namespaceURI, final String localName) {
        if (namespaceURI != null && !namespaceURI.isEmpty()) {
            return super.getElementsByTagNameNS(namespaceURI, localName.toUpperCase(Locale.ENGLISH));
        }
        return super.getElementsByTagName(localName.toUpperCase(Locale.ENGLISH));
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
    public Element createElementNS(final String namespaceURI, final String qualifiedName,
                                    final String localpart) throws DOMException {
        return createElementNS(namespaceURI, qualifiedName);
    }

    @Override
    public Element createElementNS(final String namespaceURI, final String qualifiedname) {
        if (namespaceURI == null
                || namespaceURI.isEmpty()) {
            return createElement(qualifiedname);
        }

        if (NamespaceBinder.XHTML_1_0_URI.equals(namespaceURI)) {
            final int index = qualifiedname.indexOf(':');
            if (index != -1) {
                return createElement(qualifiedname.substring(index + 1));
            }
            return createElement(qualifiedname);
        }

        return super.createElementNS(namespaceURI, qualifiedname);
    }

    @Override
    public Element createElement(final String tagName) throws DOMException {
        // First, make sure tag name is all upper case, next get the associated
        // element class. If no class is found, generate a generic HTML element.
        // Do so also if an unexpected exception occurs.
        ElementTypesHTMLHolder htmlHolder = ELEMENT_TYPES_HTML_LOWER.get(tagName);
        if (htmlHolder == null) {
            // try uppercase but only if needed and don't use this string to create
            // the element but the stored one to keep the memory usage low when the
            // tree is kept in memory longer
            htmlHolder = ELEMENT_TYPES_HTML_UPPER.get(tagName.toUpperCase(Locale.ENGLISH));
        }

        if (htmlHolder != null) {
            // Get the constructor for the element. The signature specifies an
            // owner document and a tag name. Use the constructor to instantiate
            // a new object and return it.
            try {
                return htmlHolder.ctr_.newInstance(this, tagName);
            }
            catch (final Exception e) {
                throw new IllegalStateException(
                        "HTM15 Tag '" + tagName + "' associated with an Element class that failed to construct.", e);
            }
        }
        return new HTMLElementImpl(this, tagName);
    }

    /**
     * Creates an Attribute having this Document as its OwnerDoc.
     * Overrides {@link DocumentImpl#createAttribute} and returns
     * and attribute whose name is lower case.
     *
     * @param name The name of the attribute
     * @return An attribute whose name is all lower case
     * @throws DOMException INVALID_NAME_ERR if the attribute name
     *       is not acceptable
     */
    @Override
    public Attr createAttribute(final String name) throws DOMException {
        return super.createAttribute(name.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public String getReferrer() {
        // Information not available on server side.
        return null;
    }

    @Override
    public String getDomain() {
        // Information not available on server side.
        return null;
    }

    @Override
    public String getURL() {
        // Information not available on server side.
        return null;
    }

    @Override
    public String getCookie() {
        // Information not available on server side.
        return null;
    }

    @Override
    public void setCookie(final String cookie) {
        // Information not available on server side.
    }

    @Override
    public HTMLCollection getImages() {
        // For more information see HTMLCollection#collectionMatch
        if (images_ == null) {
            images_ = new HTMLCollectionImpl(getBody(), HTMLCollectionImpl.IMAGE);
        }
        return images_;
    }

    @Override
    public HTMLCollection getApplets() {
        // For more information see HTMLCollection#collectionMatch
        if (applets_ == null) {
            applets_ = new HTMLCollectionImpl(getBody(), HTMLCollectionImpl.APPLET);
        }
        return applets_;
    }

    @Override
    public HTMLCollection getLinks() {
        // For more information see HTMLCollection#collectionMatch
        if (links_ == null) {
            links_ = new HTMLCollectionImpl(getBody(), HTMLCollectionImpl.LINK);
        }
        return links_;
    }

    @Override
    public HTMLCollection getForms() {
        // For more information see HTMLCollection#collectionMatch
        if (forms_ == null) {
            forms_ = new HTMLCollectionImpl(getBody(), HTMLCollectionImpl.FORM);
        }
        return forms_;
    }

    @Override
    public HTMLCollection getAnchors() {
        // For more information see HTMLCollection#collectionMatch
        if (anchors_ == null) {
            anchors_ = new HTMLCollectionImpl(getBody(), HTMLCollectionImpl.ANCHOR);
        }
        return anchors_;
    }

    @Override
    public void open() {
        // When called an in-memory is prepared. The document tree is still
        // accessible the old way, until this writer is closed.
        if (writer_ == null) {
            writer_ = new StringWriter();
        }
    }

    @Override
    public void close() {
        // ! NOT IMPLEMENTED, REQUIRES PARSER !
        if (writer_ != null) {
            writer_ = null;
        }
    }

    @Override
    public void write(final String text) {
        // Write a string into the in-memory writer.
        if (writer_ != null) {
            writer_.write(text);
        }
    }

    @Override
    public void writeln(final String text) {
        // Write a line into the in-memory writer.
        if (writer_ != null) {
            writer_.write(text + "\n");
        }
    }

    @Override
    public Node cloneNode(final boolean deep) {
        final HTMLDocumentImpl newdoc = new HTMLDocumentImpl();
        cloneNode(newdoc, deep);
        return newdoc;
    }

    /* (non-Javadoc)
     * @see CoreDocumentImpl#canRenameElements()
     */
    @Override
    protected boolean canRenameElements(final String newNamespaceURI, final String newNodeName, final ElementImpl el) {
        if (el.getNamespaceURI() != null) {
            // element is not HTML:
            // can be renamed if not changed to HTML
            return newNamespaceURI != null;
        }

        // check whether a class change is required
        final Constructor<?> newCtr = ELEMENT_TYPES_HTML_UPPER.get(newNodeName.toUpperCase(Locale.ENGLISH)).ctr_;
        final Constructor<?> oldCtr = ELEMENT_TYPES_HTML_UPPER.get(el.getTagName().toUpperCase(Locale.ENGLISH)).ctr_;
        return newCtr == oldCtr;
    }

    /**
     * Recursive method retreives an element by its <code>id</code> attribute.
     * Called by {@link #getElementById(String)}.
     *
     * @param elementId The <code>id</code> value to look for
     * @return The node in which to look for
     */
    private Element getElementById(final String elementId, final Node node) {
        Node    child;
        Element    result;

        child = node.getFirstChild();
        while (child != null) {
            if (child instanceof Element element) {
                if (elementId.equals(element.getAttribute("id"))) {
                    return element;
                }
                result = getElementById(elementId, child);
                if (result != null) {
                    return result;
                }
            }
            child = child.getNextSibling();
        }
        return null;
    }
}

