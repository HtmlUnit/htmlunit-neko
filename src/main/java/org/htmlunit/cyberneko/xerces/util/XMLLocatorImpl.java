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
package org.htmlunit.cyberneko.xerces.util;

import org.htmlunit.cyberneko.xerces.xni.XMLLocator;

/**
 * The XMLLocatorImpl class is an implementation of the XMLLocator
 * interface.
 *
 * @author Ronald Brill
 */
public class XMLLocatorImpl implements XMLLocator {

    private final String publicId_;
    private final String systemId_;
    private final int lineNumber_;
    private final int columnNumber_;

    public XMLLocatorImpl(final String publicId, final String systemId,
            final int lineNumber, final int columnNumber) {
        publicId_ = publicId;
        systemId_ = systemId;
        lineNumber_ = lineNumber;
        columnNumber_ = columnNumber;
    }

    @Override
    public String getXMLVersion() {
        return null;
    }

    @Override
    public String getEncoding() {
        return null;
    }

    @Override
    public String getPublicId() {
        return publicId_;
    }

    @Override
    public String getSystemId() {
        return systemId_;
    }

    @Override
    public int getLineNumber() {
        return lineNumber_;
    }

    @Override
    public int getColumnNumber() {
        return columnNumber_;
    }

    @Override
    public String getLiteralSystemId() {
        return null;
    }

    @Override
    public String getBaseSystemId() {
        return null;
    }

    @Override
    public int getCharacterOffset() {
        return -1;
    }
}
