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

import java.io.IOException;
import java.util.Locale;

import org.xml.sax.DTDHandler;
import org.xml.sax.DocumentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;

/**
 * A {@link Parser} based on our {@link org.htmlunit.cyberneko.parsers.SAXParser}.
 *
 * @author Ronald Brill
 */
public class NekoParserAdapter implements Parser {

    private final org.htmlunit.cyberneko.parsers.SAXParser parser_;

    public NekoParserAdapter() {
        parser_ = new org.htmlunit.cyberneko.parsers.SAXParser();
    }

    public org.htmlunit.cyberneko.parsers.SAXParser getSAXParser() {
        return parser_;
    }

    @Override
    public void parse(final InputSource source) throws SAXException, IOException {
        parser_.parse(source);
    }

    @Override
    public void parse(final String systemId) throws SAXException, IOException {
        parser_.parse(systemId);
    }

    @Override
    public void setLocale(final Locale locale) throws SAXException {
    }

    @Override
    public void setEntityResolver(final EntityResolver resolver) {
    }

    @Override
    public void setDTDHandler(final DTDHandler handler) {
    }

    @Override
    public void setDocumentHandler(final DocumentHandler handler) {
    }

    @Override
    public void setErrorHandler(final ErrorHandler handler) {
    }
}
