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

import java.util.Locale;

import org.htmlunit.cyberneko.xerces.dom.ElementImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLFormElement;

/**
 * Implements an HTML-specific element, an {@link org.w3c.dom.Element} that
 * will only appear inside HTML documents. This element extends {@link
 * org.htmlunit.cyberneko.xerces.dom.ElementImpl} by adding methods for directly
 * manipulating HTML-specific attributes. All HTML elements gain access to
 * the <code>id</code>, <code>title</code>, <code>lang</code>,
 * <code>dir</code> and <code>class</code> attributes. Other elements
 * add their own specific attributes.
 * <p>
 *
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLElement
 */
public class HTMLElementImpl extends ElementImpl implements HTMLElement {

    /**
     * Constructor required owner document and element tag name. Will be called
     * by the constructor of specific element types but with a known tag name.
     * Assures that the owner document is an HTML element.
     *
     * @param owner The owner HTML document
     * @param tagName The element's tag name
     */
    public HTMLElementImpl(final HTMLDocumentImpl owner, final String tagName) {
        super(owner, tagName.toUpperCase(Locale.ENGLISH));
    }

    @Override
    public String getId() {
        return getAttribute("id");
    }

    @Override
    public void setId(final String id) {
        setAttribute("id", id);
    }

    @Override
    public String getTitle() {
        return getAttribute("title");
    }

    @Override
    public void setTitle(final String title) {
        setAttribute("title", title);
    }

    @Override
    public String getLang() {
        return getAttribute("lang");
    }

    @Override
    public void setLang(final String lang) {
        setAttribute("lang", lang);
    }

    @Override
    public String getDir() {
        return getAttribute("dir");
    }

    @Override
    public void setDir(final String dir) {
        setAttribute("dir", dir);
    }

    @Override
    public String getClassName() {
        return getAttribute("class");
    }

    @Override
    public void setClassName(final String classname) {
        setAttribute("class", classname);
    }

    /**
     * Convenience method used to translate an attribute value into an integer
     * value. Returns the integer value or zero if the attribute is not a
     * valid numeric string.
     *
     * @param value The value of the attribute
     * @return The integer value, or zero if not a valid numeric string
     */
    int getInteger(final String value) {
        try {
            return Integer.parseInt(value);
        }
        catch (final NumberFormatException except) {
            return 0;
        }
    }

    /**
     * Convenience method used to translate an attribute value into a boolean
     * value. If the attribute has an associated value (even an empty string),
     * it is set and true is returned. If the attribute does not exist, false
     * is returned.
     *
     * @param attrName The name of the attribute
     * @return True or false depending on whether the attribute has been set
     */
    boolean getBinary(final String attrName) {
        return getAttributeNode(attrName) != null;
    }

    /**
     * Convenience method used to set a boolean attribute. If the value is true,
     * the attribute is set to an empty string. If the value is false, the attribute
     * is removed. HTML 4.0 understands empty strings as set attributes.
     *
     * @param name The name of the attribute
     * @param value The value of the attribute
     */
    void setAttribute(final String name, final boolean value) {
        if (value) {
            setAttribute(name, name);
        }
        else {
            removeAttribute(name);
        }
    }

    @Override
    public Attr getAttributeNode(final String attrName) {
        return super.getAttributeNode(attrName.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public Attr getAttributeNodeNS(final String namespaceURI, final String localName) {
        if (namespaceURI != null && namespaceURI.length() > 0) {
            return super.getAttributeNodeNS(namespaceURI, localName);
        }
        return super.getAttributeNode(localName.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public String getAttribute(final String attrName) {
        return super.getAttribute(attrName.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public String getAttributeNS(final String namespaceURI, final String localName) {
        if (namespaceURI != null && namespaceURI.length() > 0) {
            return super.getAttributeNS(namespaceURI, localName);
        }
        return super.getAttribute(localName.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public final NodeList getElementsByTagName(final String tagName) {
        return super.getElementsByTagName(tagName.toUpperCase(Locale.ENGLISH));
    }

    @Override
    public final NodeList getElementsByTagNameNS(final String namespaceURI, final String localName) {
        if (namespaceURI != null && namespaceURI.length() > 0) {
            return super.getElementsByTagNameNS(namespaceURI, localName.toUpperCase(Locale.ENGLISH));
        }
        return super.getElementsByTagName(localName.toUpperCase(Locale.ENGLISH));
    }

    /**
     * Convenience method used to capitalize a one-off attribute value before it
     * is returned. For example, the align values "LEFT" and "left" will both
     * return as "Left".
     *
     * @param value The value of the attribute
     * @return The capitalized value
     */
    String capitalize(final String value) {
        final char[] chars;
        int i;

        // Convert string to charactares. Convert the first one to upper case,
        // the other characters to lower case, and return the converted string.
        chars = value.toCharArray();
        if (chars.length > 0) {
            chars[ 0 ] = Character.toUpperCase(chars[ 0 ]);
            for (i = 1; i < chars.length; ++i) {
                chars[ i ] = Character.toLowerCase(chars[ i ]);
            }
            return String.valueOf(chars);
        }
        return value;
    }

    /**
     * Convenience method used to capitalize a one-off attribute value before it
     * is returned. For example, the align values "LEFT" and "left" will both
     * return as "Left".
     *
     * @param attrname The name of the attribute
     * @return The capitalized value
     */
    String getCapitalized(final String attrname) {
        final String value;
        final char[] chars;
        int i;

        value = getAttribute(attrname);
        if (value != null) {
            // Convert string to charactares. Convert the first one to upper case,
            // the other characters to lower case, and return the converted string.
            chars = value.toCharArray();
            if (chars.length > 0) {
                chars[ 0 ] = Character.toUpperCase(chars[ 0 ]);
                for (i = 1; i < chars.length; ++i) {
                    chars[ i ] = Character.toLowerCase(chars[ i ]);
                }
                return String.valueOf(chars);
            }
        }
        return value;
    }

    /**
     * Convenience method returns the form in which this form element is contained.
     * This method is exposed for form elements through the DOM API, but other
     * elements have no access to it through the API.
     */
    public HTMLFormElement getForm() {
        Node parent = getParentNode();
        while (parent != null) {
            if (parent instanceof HTMLFormElement) {
                return (HTMLFormElement) parent;
            }
            parent = parent.getParentNode();
        }
        return null;
    }
}
