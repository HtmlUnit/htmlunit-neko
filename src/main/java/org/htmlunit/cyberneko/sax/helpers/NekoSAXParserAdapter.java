/*
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
package org.htmlunit.cyberneko.sax.helpers;

import javax.xml.parsers.SAXParser;

import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * A {@link SAXParser} based on our {@link org.htmlunit.cyberneko.parsers.SAXParser}.
 *
 * @author Ronald Brill
 */
public class NekoSAXParserAdapter extends SAXParser {

    private final NekoParserAdapter parser_;

    public NekoSAXParserAdapter() {
        parser_ = new NekoParserAdapter();
    }

    @Override
    public Parser getParser() throws SAXException {
        return parser_;
    }

    @Override
    public XMLReader getXMLReader() throws SAXException {
        return parser_.getSAXParser();
    }

    @Override
    public boolean isNamespaceAware() {
        return false;
    }

    @Override
    public boolean isValidating() {
        return false;
    }

    @Override
    public void setProperty(final String name, final Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    @Override
    public Object getProperty(final String name)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        return null;
    }
}
