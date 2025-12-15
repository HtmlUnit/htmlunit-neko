/*
 * Copyright (c) 2017-2025 Ronald Brill
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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * A {@link SAXParserFactory} for our {@link org.htmlunit.cyberneko.parsers.SAXParser}.
 *
 * @author Ronald Brill
 */
public class NekoSAXParserFactory extends SAXParserFactory {

    @Override
    public SAXParser newSAXParser() throws ParserConfigurationException, SAXException {
        final NekoSAXParserAdapter saxParser = new NekoSAXParserAdapter();
        // todo - set features
        return saxParser;
    }

    @Override
    public void setFeature(final String name, final boolean value)
            throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
    }

    @Override
    public boolean getFeature(final String name)
            throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        return false;
    }

    @Override
    public boolean isValidating() {
        return false;
    }

    @Override
    public boolean isXIncludeAware() {
        return false;
    }
}
