/*
 * Copyright (c) 2002-2009 Andy Clark, Marc Guillemot
 * Copyright (c) 2017-2024 Ronald Brill
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

import org.htmlunit.cyberneko.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.DOMException;
import org.w3c.dom.html.HTMLDOMImplementation;
import org.w3c.dom.html.HTMLDocument;

/**
 * Provides number of methods for performing operations that are independent
 * of any particular instance of the document object model. This class is
 * unconstructable, the only way to obtain an instance of a DOM implementation
 * is by calling the static method {@link #getDOMImplementation}.
 * <p>
 *
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.DOMImplementation
 */
public final class HTMLDOMImplementationImpl extends DOMImplementationImpl implements HTMLDOMImplementation {

    /**
     * Holds a reference to the single instance of the DOM implementation.
     * Only one instance is required since this class is multiple entry.
     */
    private static final HTMLDOMImplementation INSTANCE = new HTMLDOMImplementationImpl();

    /**
     * Private constructor assures that an object of this class cannot
     * be created. The only way to obtain an object is by calling {@link
     * #getDOMImplementation}.
     */
    private HTMLDOMImplementationImpl() {
    }

    /**
     * Create a new HTML document of the specified <TT>TITLE</TT> text.
     *
     * @param title The document title text
     * @return New HTML document
     */
    @Override
    public HTMLDocument createHTMLDocument(final String title) throws DOMException {
        if (title == null) {
            throw new NullPointerException("HTM014 Argument 'title' is null.");
        }

        final HTMLDocument doc = new HTMLDocumentImpl();
        doc.setTitle(title);
        return doc;
    }

    /**
     * Returns an instance of a {@link HTMLDOMImplementation} that can be
     * used to perform operations that are not specific to a particular
     * document instance, e.g. to create a new document.
     *
     * @return Reference to a valid DOM implementation
     */
    public static HTMLDOMImplementation getHTMLDOMImplementation() {
        return INSTANCE;
    }
}
