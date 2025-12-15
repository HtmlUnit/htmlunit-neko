/*
 * Copyright (c) 2002-2009 Andy Clark, Marc Guillemot
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
package org.htmlunit.cyberneko;

import java.io.StringWriter;
import java.util.ArrayList;

import org.htmlunit.cyberneko.util.TestUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;

/**
 * Helper.
 *
 * @author Andy Clark
 * @author Marc Guillemot
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
public final class SaxHandler implements ContentHandler, LexicalHandler, ErrorHandler {
    private final StringWriter out_;
    private boolean lastWasChar_;

    public SaxHandler(final StringWriter out) {
        out_ = out;
        lastWasChar_ = false;
    }

    @Override
    public void setDocumentLocator(final Locator locator) {
        characters();
    }

    @Override
    public void startDocument() throws SAXException {
        characters();
    }

    @Override
    public void endDocument() throws SAXException {
        characters();
    }

    @Override
    public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
        characters();
    }

    @Override
    public void endPrefixMapping(final String prefix) throws SAXException {
        characters();
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
            throws SAXException {
        characters();

        out_.append('(')
            .append(qName)
            .append("\n");

        final ArrayList<String> attNames = new ArrayList<>();
        for (int i = 0; i < atts.getLength(); i++) {
            attNames.add(atts.getQName(i));
        }

        for (final String attName : attNames) {
            out_.append('A');
            final int i = atts.getIndex(attName);
            if (atts.getURI(i) != null && atts.getURI(i).length() > 0) {
                out_.append('{')
                    .append(atts.getURI(i))
                    .append('}');
            }

            out_.append(TestUtils.normalize(atts.getQName(i)))
                .append(' ')
                .append(TestUtils.normalize(atts.getValue(i)))
                .append('\n');
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        characters();

        out_.append(')')
            .append(qName)
            .append("\n");
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        if (lastWasChar_) {
            out_.append(TestUtils.normalize(String.copyValueOf(ch, start, length)));
            return;
        }

        out_.append('"')
            .append(TestUtils.normalize(String.copyValueOf(ch, start, length)));
        lastWasChar_ = true;
    }

    @Override
    public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {
        characters();

        out_.append("# ignorableWhitespace\n");
    }

    @Override
    public void processingInstruction(final String target, final String data) throws SAXException {
        characters();

        out_.append('?')
            .append(target);
        if (data != null && data.length() > 0) {
            out_.append(' ')
                .append(TestUtils.normalize(data));
        }
        out_.append('\n');
    }

    @Override
    public void skippedEntity(final String name) throws SAXException {
        characters();

        out_.append("# skippedEntity\n");
    }

    @Override
    public void startDTD(final String name, final String publicId, final String systemId) throws SAXException {
        characters();

        out_.append('!');
        boolean addNl = true;
        if (name != null && name.length() > 0) {
            out_.append(TestUtils.normalize(name));
            out_.append('\n');
            addNl = false;
        }
        if (publicId != null && publicId.length() > 0) {
            out_.append('p');
            out_.append(TestUtils.normalize(publicId));
            out_.append('\n');
            addNl = false;
        }
        if (systemId != null && systemId.length() > 0) {
            out_.append('s');
            out_.append(TestUtils.normalize(systemId));
            out_.append('\n');
            addNl = false;
        }
        if (addNl) {
            out_.append('\n');
        }
    }

    @Override
    public void endDTD() throws SAXException {
        characters();

        out_.append("# endDTD\n");
    }

    @Override
    public void startEntity(final String name) throws SAXException {
        characters();

        out_.append("# startEntity\n");
    }

    @Override
    public void endEntity(final String name) throws SAXException {
        characters();

        out_.append("# endEntity\n");
    }

    @Override
    public void startCDATA() throws SAXException {
        characters();

        out_.append("((CDATA\n");
    }

    @Override
    public void endCDATA() throws SAXException {
        characters();

        out_.append("))CDATA\n");
    }

    @Override
    public void comment(final char[] ch, final int start, final int length) throws SAXException {
        characters();

        out_.append('#')
            .append(TestUtils.normalize(String.copyValueOf(ch, start, length)))
            .append('\n');
    }

    private void characters() {
        if (lastWasChar_) {
            out_.append('\n');
            lastWasChar_ = false;
        }
    }

    @Override
    public void warning(final SAXParseException exception) throws SAXException {
        out_.append("# warning\n");
    }

    @Override
    public void error(final SAXParseException exception) throws SAXException {
        out_.append("# error\n");
    }

    @Override
    public void fatalError(final SAXParseException exception) throws SAXException {
        out_.append("# fatalError\n");
    }
}
