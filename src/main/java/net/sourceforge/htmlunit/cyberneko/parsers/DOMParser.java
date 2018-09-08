/*
 * Copyright 2002-2009 Andy Clark, Marc Guillemot
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sourceforge.htmlunit.cyberneko.parsers;

import net.sourceforge.htmlunit.cyberneko.HTMLConfiguration;

/**
 * A DOM parser for HTML documents.
 *
 * @author Andy Clark
 *
 * @version $Id: DOMParser.java,v 1.5 2005/02/14 03:56:54 andyc Exp $
 */
public class DOMParser
    /***/
    extends org.apache.xerces.parsers.DOMParser {
    /***
    // NOTE: It would be better to extend from AbstractDOMParser but
    //       most users will find it easier if the API is just like the
    //       Xerces DOM parser. By extending directly from DOMParser,
    //       users can register SAX error handlers, entity resolvers,
    //       and the like. -Ac
    extends org.apache.xerces.parsers.AbstractDOMParser {
    /***/

    //
    // Constructors
    //

    /** Default constructor. */
    public DOMParser() {
        super(new HTMLConfiguration());
        /*** extending DOMParser ***/
        try {
            setProperty("http://apache.org/xml/properties/dom/document-class-name",
                                       "org.apache.html.dom.HTMLDocumentImpl");
        }
        catch (final org.xml.sax.SAXNotRecognizedException e) {
            throw new RuntimeException("http://apache.org/xml/properties/dom/document-class-name property not recognized");
        }
        catch (final org.xml.sax.SAXNotSupportedException e) {
            throw new RuntimeException("http://apache.org/xml/properties/dom/document-class-name property not supported");
        }
        /*** extending AbstractDOMParser ***
        fConfiguration.setProperty("http://apache.org/xml/properties/dom/document-class-name",
                                   "org.apache.html.dom.HTMLDocumentImpl");
        /***/
    } // <init>()

} // class DOMParser
