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
package org.htmlunit.cyberneko.xerces.xni;

/**
 * This exception is the base exception of all XNI exceptions. It can be
 * constructed with an error message or used to wrap another exception object.
 * <p>
 * <strong>Note:</strong> By extending the Java <code>RuntimeException</code>,
 * XNI handlers and components are not required to catch XNI exceptions but may
 * explicitly catch them, if so desired.
 *
 * @author Andy Clark, IBM
 * @author Ronald Brill
 */
public class XNIException extends RuntimeException {

    private static final long serialVersionUID = 7447489736019161121L;

    /**
     * Constructs an XNI exception with a message.
     *
     * @param message The exception message.
     */
    public XNIException(final String message) {
        super(message);
    }

    /**
     * Constructs an XNI exception with a wrapped exception.
     *
     * @param exception The wrapped exception.
     */
    public XNIException(final Exception exception) {
        super(exception.getMessage(), exception);
    }

    /**
     * Constructs an XNI exception with a message and wrapped exception.
     *
     * @param message   The exception message.
     * @param exception The wrapped exception.
     */
    public XNIException(final String message, final Exception exception) {
        super(message, exception);
    }

    /** @return the wrapped exception. */
    public Exception getException() {
        return (Exception) getCause();
    }
}
