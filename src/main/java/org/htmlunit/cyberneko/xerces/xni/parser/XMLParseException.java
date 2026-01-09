/*
 * Copyright (c) 2017-2026 Ronald Brill
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
package org.htmlunit.cyberneko.xerces.xni.parser;

import org.htmlunit.cyberneko.xerces.xni.XMLLocator;
import org.htmlunit.cyberneko.xerces.xni.XNIException;

/**
 * A parsing exception. This exception is different from the standard XNI
 * exception in that it stores the location in the document (or its entities)
 * where the exception occurred.
 *
 * @author Andy Clark, IBM
 */
public class XMLParseException extends XNIException {

    /** Public identifier. */
    private String publicId_;

    /** System identifier. */
    private String systemId_;

    /** literal System identifier. */
    private String literalSystemId_;

    /** Base system identifier. */
    private String baseSystemId_;

    /** Line number. */
    private int lineNumber_ = -1;

    /** Column number. */
    private int columnNumber_ = -1;

    /** Character offset. */
    private int characterOffset_ = -1;

    // Constructs a parse exception.
    public XMLParseException(final XMLLocator locator, final String message) {
        super(message);
        if (locator != null) {
            publicId_ = locator.getPublicId();
            literalSystemId_ = locator.getLiteralSystemId();
            systemId_ = locator.getSystemId();
            baseSystemId_ = locator.getBaseSystemId();
            lineNumber_ = locator.getLineNumber();
            columnNumber_ = locator.getColumnNumber();
            characterOffset_ = locator.getCharacterOffset();
        }
    }

    // Constructs a parse exception.
    public XMLParseException(final XMLLocator locator, final String message, final Exception exception) {
        super(message, exception);
        if (locator != null) {
            publicId_ = locator.getPublicId();
            literalSystemId_ = locator.getLiteralSystemId();
            systemId_ = locator.getSystemId();
            baseSystemId_ = locator.getBaseSystemId();
            lineNumber_ = locator.getLineNumber();
            columnNumber_ = locator.getColumnNumber();
            characterOffset_ = locator.getCharacterOffset();
        }
    }

    /**
     * @return the public identifier.
     */
    public String getPublicId() {
        return publicId_;
    }

    /**
     * @return the expanded system identifier.
     */
    public String getSystemId() {
        return systemId_;
    }

    /**
     * @return the literal system identifier.
     */
    public String getLiteralSystemId() {
        return literalSystemId_;
    }

    /**
     * return the base system identifier.
     */
    public String getBaseSystemId() {
        return baseSystemId_;
    }

    /**
     * @return the line number.
     */
    public int getLineNumber() {
        return lineNumber_;
    }

    /**
     * @return the row number.
     */
    public int getColumnNumber() {
        return columnNumber_;
    }

    /**
     * @return the character offset.
     */
    public int getCharacterOffset() {
        return characterOffset_;
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder();
        if (publicId_ != null) {
            str.append(publicId_);
        }
        str.append(':');
        if (literalSystemId_ != null) {
            str.append(literalSystemId_);
        }
        str.append(':');
        if (systemId_ != null) {
            str.append(systemId_);
        }
        str.append(':');
        if (baseSystemId_ != null) {
            str.append(baseSystemId_);
        }
        str.append(':');
        str.append(lineNumber_);
        str.append(':');
        str.append(columnNumber_);
        str.append(':');
        str.append(characterOffset_);
        str.append(':');
        String message = getMessage();
        if (message == null) {
            final Exception exception = getException();
            if (exception != null) {
                message = exception.getMessage();
            }
        }
        if (message != null) {
            str.append(message);
        }
        return str.toString();
    }
}
