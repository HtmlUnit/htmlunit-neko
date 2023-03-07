/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.htmlunit.xerces.xni;

/**
 * This exception is the base exception of all XNI exceptions. It can be
 * constructed with an error message or used to wrap another exception object.
 * <p>
 * <strong>Note:</strong> By extending the Java <code>RuntimeException</code>,
 * XNI handlers and components are not required to catch XNI exceptions but may
 * explicitly catch them, if so desired.
 *
 * @author Andy Clark, IBM
 */
public class XNIException extends RuntimeException {

    private static final long serialVersionUID = 7447489736019161121L;

    /** The wrapped exception. */
    private Exception fException = this;

    /**
     * Constructs an XNI exception with a message.
     *
     * @param message The exception message.
     */
    public XNIException(String message) {
        super(message);
    }

    /**
     * Constructs an XNI exception with a wrapped exception.
     *
     * @param exception The wrapped exception.
     */
    public XNIException(Exception exception) {
        super(exception.getMessage());
        fException = exception;
    }

    /**
     * Constructs an XNI exception with a message and wrapped exception.
     *
     * @param message   The exception message.
     * @param exception The wrapped exception.
     */
    public XNIException(String message, Exception exception) {
        super(message);
        fException = exception;
    }

    /** @return the wrapped exception. */
    public Exception getException() {
        return fException != this ? fException : null;
    }

    /**
     * Initializes the cause of this <code>XNIException</code>. The value must be an
     * instance of <code>Exception</code> or <code>null</code>.
     *
     * @param throwable the cause
     * @return this exception
     *
     * @throws IllegalStateException    if a cause has already been set
     * @throws IllegalArgumentException if the cause is this exception
     * @throws ClassCastException       if the cause is not assignable to
     *                                  <code>Exception</code>
     */
    @Override
    public synchronized Throwable initCause(Throwable throwable) {
        if (fException != this) {
            // TODO: Add error message.
            throw new IllegalStateException();
        }
        if (throwable == this) {
            // TODO: Add error message.
            throw new IllegalArgumentException();
        }
        fException = (Exception) throwable;
        return this;
    }

    /** @return the cause of this <code>XNIException</code>. */
    @Override
    public Throwable getCause() {
        return getException();
    }
}
