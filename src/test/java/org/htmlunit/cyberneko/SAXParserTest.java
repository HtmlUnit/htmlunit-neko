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
package org.htmlunit.cyberneko;

import java.io.StringReader;

import javax.xml.parsers.SAXParserFactory;

import org.htmlunit.cyberneko.parsers.SAXParser;
import org.htmlunit.cyberneko.sax.helpers.NekoSAXParserAdapter;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLInputSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Unit tests for {@link SAXParser}.
 *
 * @author Ronald Brill
 */
public class SAXParserTest {

    /**
     * @throws Exception in case of error
     */
    @Test
    public void parse() throws Exception {
        final String html = "<html><body>simple test</body></html>";

        final StringReader sr = new StringReader(html);
        final XMLInputSource source = new XMLInputSource(null, "foo", null, sr, null);

        final SAXParser parser = new SAXParser();
        parser.parse(source);
    }

    /**
     * @throws Exception in case of error
     */
    @Test
    public void xmlReaderFactory() throws Exception {
        final String html = "<html><body>simple test</body></html>";

        final StringReader sr = new StringReader(html);
        final InputSource source = new InputSource(sr);

        final String xmlReaderClass = "org.htmlunit.cyberneko.parsers.SAXParser";
        final XMLReader xmlReader = XMLReaderFactory.createXMLReader(xmlReaderClass);

        xmlReader.parse(source);
    }

    /**
     * @throws Exception in case of error
     */
    @Test
    public void saxParserFactorySystemProperty() throws Exception {
        final String html = "<html><body>simple test</body></html>";

        final StringReader sr = new StringReader(html);
        final InputSource source = new InputSource(sr);

        System.setProperty(
                "javax.xml.parsers.SAXParserFactory",
                "org.htmlunit.cyberneko.sax.helpers.NekoSAXParserFactory");
        final javax.xml.parsers.SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();

        Assertions.assertInstanceOf(NekoSAXParserAdapter.class, saxParser);
        saxParser.parse(source, (DefaultHandler) null);
    }

    /**
     * @throws Exception in case of error
     */
    @Test
    public void saxParserFactory() throws Exception {
        final String html = "<html><body>simple test</body></html>";

        final StringReader sr = new StringReader(html);
        final InputSource source = new InputSource(sr);

        final javax.xml.parsers.SAXParser saxParser =
                SAXParserFactory.newInstance(
                                    "org.htmlunit.cyberneko.sax.helpers.NekoSAXParserFactory", null)
                                .newSAXParser();

        Assertions.assertInstanceOf(NekoSAXParserAdapter.class, saxParser);
        saxParser.parse(source, (DefaultHandler) null);
    }
}
