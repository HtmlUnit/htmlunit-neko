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

import java.io.PrintWriter;

import org.htmlunit.cyberneko.xerces.xni.XNIException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLErrorHandler;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLParseException;

/**
 * Default error handler.
 *
 * @author Andy Clark, IBM
 */
public class DefaultErrorHandler implements XMLErrorHandler {

    /** Print writer. */
    private final PrintWriter fOut_;

    /**
     * Constructs an error handler that prints error messages to
     * <code>System.err</code>.
     */
    public DefaultErrorHandler() {
        this(new PrintWriter(System.err));
    }

    // Constructs an error handler that prints error messages to the
    // specified <code>PrintWriter</code>.
    public DefaultErrorHandler(final PrintWriter out) {
        fOut_ = out;
    }

    /** Warning. */
    @Override
    public void warning(final String domain, final String key, final XMLParseException ex) throws XNIException {
        printError("Warning", ex);
    }

    /** Error. */
    @Override
    public void error(final String domain, final String key, final XMLParseException ex) throws XNIException {
        printError("Error", ex);
    }

    /** Fatal error. */
    @Override
    public void fatalError(final String domain, final String key, final XMLParseException ex) throws XNIException {
        printError("Fatal Error", ex);
        throw ex;
    }

    /** Prints the error message. */
    private void printError(final String type, final XMLParseException ex) {
        fOut_.print("[");
        fOut_.print(type);
        fOut_.print("] ");
        String systemId = ex.getExpandedSystemId();
        if (systemId != null) {
            final int index = systemId.lastIndexOf('/');
            if (index != -1) {
                systemId = systemId.substring(index + 1);
            }
            fOut_.print(systemId);
        }
        fOut_.print(':');
        fOut_.print(ex.getLineNumber());
        fOut_.print(':');
        fOut_.print(ex.getColumnNumber());
        fOut_.print(": ");
        fOut_.print(ex.getMessage());
        fOut_.println();
        fOut_.flush();
    }
}
