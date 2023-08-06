/*
 * Copyright 2017-2023 Ronald Brill
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

    private static final long serialVersionUID = -1306660736099956209L;

    /** Public identifier. */
    private String fPublicId_;

    /** literal System identifier. */
    private String fLiteralSystemId_;

    /** expanded System identifier. */
    private String fExpandedSystemId_;

    /** Base system identifier. */
    private String fBaseSystemId_;

    /** Line number. */
    private int fLineNumber_ = -1;

    /** Column number. */
    private int fColumnNumber_ = -1;

    /** Character offset. */
    private int fCharacterOffset_ = -1;

    // Constructs a parse exception.
    public XMLParseException(final XMLLocator locator, final String message) {
        super(message);
        if (locator != null) {
            fPublicId_ = locator.getPublicId();
            fLiteralSystemId_ = locator.getLiteralSystemId();
            fExpandedSystemId_ = locator.getExpandedSystemId();
            fBaseSystemId_ = locator.getBaseSystemId();
            fLineNumber_ = locator.getLineNumber();
            fColumnNumber_ = locator.getColumnNumber();
            fCharacterOffset_ = locator.getCharacterOffset();
        }
    }

    // Constructs a parse exception.
    public XMLParseException(final XMLLocator locator, final String message, final Exception exception) {
        super(message, exception);
        if (locator != null) {
            fPublicId_ = locator.getPublicId();
            fLiteralSystemId_ = locator.getLiteralSystemId();
            fExpandedSystemId_ = locator.getExpandedSystemId();
            fBaseSystemId_ = locator.getBaseSystemId();
            fLineNumber_ = locator.getLineNumber();
            fColumnNumber_ = locator.getColumnNumber();
            fCharacterOffset_ = locator.getCharacterOffset();
        }
    }

    /** @return the public identifier. */
    public String getPublicId() {
        return fPublicId_;
    }

    /** @return the expanded system identifier. */
    public String getExpandedSystemId() {
        return fExpandedSystemId_;
    }

    /** @return the literal system identifier. */
    public String getLiteralSystemId() {
        return fLiteralSystemId_;
    }

    /** @return the base system identifier. */
    public String getBaseSystemId() {
        return fBaseSystemId_;
    }

    /** @return the line number. */
    public int getLineNumber() {
        return fLineNumber_;
    }

    /** @return the row number. */
    public int getColumnNumber() {
        return fColumnNumber_;
    }

    /** @return the character offset. */
    public int getCharacterOffset() {
        return fCharacterOffset_;
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder();
        if (fPublicId_ != null) {
            str.append(fPublicId_);
        }
        str.append(':');
        if (fLiteralSystemId_ != null) {
            str.append(fLiteralSystemId_);
        }
        str.append(':');
        if (fExpandedSystemId_ != null) {
            str.append(fExpandedSystemId_);
        }
        str.append(':');
        if (fBaseSystemId_ != null) {
            str.append(fBaseSystemId_);
        }
        str.append(':');
        str.append(fLineNumber_);
        str.append(':');
        str.append(fColumnNumber_);
        str.append(':');
        str.append(fCharacterOffset_);
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
