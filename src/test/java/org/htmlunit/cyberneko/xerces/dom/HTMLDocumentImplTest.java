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
package org.htmlunit.cyberneko.xerces.dom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;

import org.htmlunit.cyberneko.html.dom.HTMLDocumentImpl;
import org.htmlunit.cyberneko.parsers.DOMParser;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Unit tests for {@link HTMLDocumentImpl}.
 *
 * @author Ronald Brill
 */
public class HTMLDocumentImplTest {

    @Test
    public void tagName() throws Exception {
        final String html = "<HTML><head></head>"
                + "<bODy>"
                + "<DIv>abc</DIv>"
                + "</bodY></HTML>";

        DOMParser parser = new DOMParser(HTMLDocumentImpl.class);
        parser.parse(new InputSource(new StringReader(html)));
        Document doc = parser.getDocument();

        Element htmlElem = doc.getDocumentElement();
        assertEquals("HTML", htmlElem.getTagName());

        Element headElem = (Element) htmlElem.getChildNodes().item(0);
        assertEquals("head", headElem.getTagName());

        Element bodyElem = (Element) htmlElem.getChildNodes().item(1);
        assertEquals("bODy", bodyElem.getTagName());

        Element divElem = (Element) bodyElem.getChildNodes().item(0);
        assertEquals("DIv", divElem.getTagName());

        assertEquals(bodyElem, doc.getElementsByTagName("bODy").item(0));
        assertEquals(bodyElem, doc.getElementsByTagName("body").item(0));
        assertEquals(bodyElem, doc.getElementsByTagName("BODY").item(0));
        assertEquals(bodyElem, doc.getElementsByTagName("Body").item(0));

        assertEquals(divElem, doc.getElementsByTagName("DIv").item(0));
        assertEquals(divElem, doc.getElementsByTagName("div").item(0));
        assertEquals(divElem, doc.getElementsByTagName("DIV").item(0));
        assertEquals(divElem, doc.getElementsByTagName("diV").item(0));
    }

    @Test
    public void tagNameLower() throws Exception {
        final String html = "<HTML><head></head>"
                + "<bODy>"
                + "<DIv>abc</DIv>"
                + "</bodY></HTML>";

        DOMParser parser = new DOMParser(HTMLDocumentImpl.class);
        parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");

        parser.parse(new InputSource(new StringReader(html)));
        Document doc = parser.getDocument();

        Element htmlElem = doc.getDocumentElement();
        assertEquals("html", htmlElem.getTagName());

        Element headElem = (Element) htmlElem.getChildNodes().item(0);
        assertEquals("head", headElem.getTagName());

        Element bodyElem = (Element) htmlElem.getChildNodes().item(1);
        assertEquals("body", bodyElem.getTagName());

        Element divElem = (Element) bodyElem.getChildNodes().item(0);
        assertEquals("div", divElem.getTagName());

        assertEquals(bodyElem, doc.getElementsByTagName("bODy").item(0));
        assertEquals(bodyElem, doc.getElementsByTagName("body").item(0));
        assertEquals(bodyElem, doc.getElementsByTagName("BODY").item(0));
        assertEquals(bodyElem, doc.getElementsByTagName("Body").item(0));

        assertEquals(divElem, doc.getElementsByTagName("DIv").item(0));
        assertEquals(divElem, doc.getElementsByTagName("div").item(0));
        assertEquals(divElem, doc.getElementsByTagName("DIV").item(0));
        assertEquals(divElem, doc.getElementsByTagName("diV").item(0));
    }

    @Test
    public void tagNameUpper() throws Exception {
        final String html = "<HTML><head></head>"
                + "<bODy>"
                + "<DIv>abc</DIv>"
                + "</bodY></HTML>";

        DOMParser parser = new DOMParser(HTMLDocumentImpl.class);
        parser.setProperty("http://cyberneko.org/html/properties/names/elems", "upper");

        parser.parse(new InputSource(new StringReader(html)));
        Document doc = parser.getDocument();

        Element htmlElem = doc.getDocumentElement();
        assertEquals("HTML", htmlElem.getTagName());

        Element headElem = (Element) htmlElem.getChildNodes().item(0);
        assertEquals("HEAD", headElem.getTagName());

        Element bodyElem = (Element) htmlElem.getChildNodes().item(1);
        assertEquals("BODY", bodyElem.getTagName());

        Element divElem = (Element) bodyElem.getChildNodes().item(0);
        assertEquals("DIV", divElem.getTagName());

        assertEquals(bodyElem, doc.getElementsByTagName("bODy").item(0));
        assertEquals(bodyElem, doc.getElementsByTagName("body").item(0));
        assertEquals(bodyElem, doc.getElementsByTagName("BODY").item(0));
        assertEquals(bodyElem, doc.getElementsByTagName("Body").item(0));

        assertEquals(divElem, doc.getElementsByTagName("DIv").item(0));
        assertEquals(divElem, doc.getElementsByTagName("div").item(0));
        assertEquals(divElem, doc.getElementsByTagName("DIV").item(0));
        assertEquals(divElem, doc.getElementsByTagName("diV").item(0));
    }
}
