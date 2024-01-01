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
package org.htmlunit.cyberneko.xerces.xni.parser;

import org.htmlunit.cyberneko.xerces.xni.XNIException;

/**
 * An XNI parser configuration exception. This exception class extends
 * <code>XNIException</code> in order to differentiate between general parsing
 * errors and configuration errors.
 *
 * @author Andy Clark, IBM
 */
public class XMLConfigurationException extends XNIException {

    private static final long serialVersionUID = 8987025467104000713L;

    /** Exception type: identifier not recognized. */
    public static final short NOT_RECOGNIZED = 0;

    /** Exception type: identifier not supported. */
    public static final short NOT_SUPPORTED = 1;

    /** Exception type. */
    private final short type_;

    /** Identifier. */
    private final String identifier_;

    /**
     * Constructs a configuration exception with the specified type and
     * feature/property identifier.
     *
     * @param type       The type of the exception.
     * @param identifier The feature or property identifier.
     *
     * @see #NOT_RECOGNIZED
     * @see #NOT_SUPPORTED
     */
    public XMLConfigurationException(final short type, final String identifier) {
        super(identifier);
        type_ = type;
        identifier_ = identifier;
    }

    /**
     * Constructs a configuration exception with the specified type,
     * feature/property identifier, and error message
     *
     * @param type       The type of the exception.
     * @param identifier The feature or property identifier.
     * @param message    The error message.
     *
     * @see #NOT_RECOGNIZED
     * @see #NOT_SUPPORTED
     */
    public XMLConfigurationException(final short type, final String identifier, final String message) {
        super(message);
        type_ = type;
        identifier_ = identifier;
    }

    /**
     * @return the exception type.
     *
     * @see #NOT_RECOGNIZED
     * @see #NOT_SUPPORTED
     */
    public short getType() {
        return type_;
    }

    /** @return the feature or property identifier. */
    public String getIdentifier() {
        return identifier_;
    }
}
