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

import java.io.StringReader;

import org.htmlunit.cyberneko.html.dom.HTMLDocumentImpl;
import org.htmlunit.cyberneko.parsers.DOMParser;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLInputSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;

/**
 * Unit tests for {@link ParentNode}.
 *
 * @author Ronald Brill
 */
public class ParentNodeTest {

    @Test
    public void getTextContent() throws Exception {
        final String html =
                "<html>"
                + "<body>"
                + "<span>Hello</span>"
                + "<p><span>Hello</span>World</p>"
                + "<h1><span>Hello</span> <span>World</span></h1>"
                + "</body></html>";

        final StringReader sr = new StringReader(html);
        final XMLInputSource source = new XMLInputSource(null, "foo", null, sr, null);

        final DOMParser parser = new DOMParser(HTMLDocumentImpl.class);
        parser.parse(source);

        final HTMLDocumentImpl doc = (HTMLDocumentImpl) parser.getDocument();

        Node node = doc.getElementsByTagName("span").item(0);
        Assertions.assertEquals("Hello", node.getTextContent());

        node = doc.getElementsByTagName("p").item(0);
        Assertions.assertEquals("HelloWorld", node.getTextContent());

        node = doc.getElementsByTagName("h1").item(0);
        Assertions.assertEquals("Hello World", node.getTextContent());
    }
}
