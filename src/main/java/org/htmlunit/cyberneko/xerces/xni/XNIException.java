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
package org.htmlunit.cyberneko.xerces.xni;

/**
 * Base exception for all XNI exceptions. It can be constructed with an
 * error message, a wrapped cause, or both.
 * <p>
 * <strong>Note:</strong> By extending {@link RuntimeException}, XNI handlers
 * and components are not required to catch XNI exceptions explicitly, but
 * may choose to do so if desired.
 * </p>
 *
 * @author Andy Clark, IBM
 * @author Ronald Brill
 */
public class XNIException extends RuntimeException {

    /**
     * Constructs an XNI exception with the specified detail message.
     *
     * @param message the detail message
     */
    public XNIException(final String message) {
        super(message);
    }

    /**
     * Constructs an XNI exception with the specified wrapped cause.
     *
     * @param cause the wrapped cause exception
     */
    public XNIException(final Throwable cause) {
        super(cause != null ? cause.getMessage() : null, cause);
    }

    /**
     * Constructs an XNI exception with the specified detail message and wrapped cause.
     *
     * @param message the detail message
     <div></div>
     * @param cause   the wrapped cause exception
     */
    public XNIException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Returns the wrapped exception cause.
     *
     * @return the wrapped cause as an {@link Exception}, or {@code null} if none exists
     */
    public Exception getException() {
        final Throwable cause = getCause();
        if (cause instanceof Exception) {
            return (Exception) cause;
        }
        if (cause != null) {
            return new RuntimeException(cause);
        }
        return null;
    }
}
