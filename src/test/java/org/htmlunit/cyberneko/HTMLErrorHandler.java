/*
 * Copyright 2002-2009 Andy Clark, Marc Guillemot
 * Copyright 2017-2023 Ronald Brill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.htmlunit.cyberneko;

import java.io.IOException;

import org.htmlunit.cyberneko.xerces.util.DefaultErrorHandler;
import org.htmlunit.cyberneko.xerces.xni.XNIException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLParseException;

/**
 * Error handler for test purposes: just logs the errors to the provided PrintWriter.
 * @author Marc Guillemot
 */
class HTMLErrorHandler extends DefaultErrorHandler {
    private final java.io.Writer out_;

    public HTMLErrorHandler(final java.io.Writer out) {
        out_ = out;
    }


    /** @see DefaultErrorHandler#error(String,String,XMLParseException) */
    @Override
    public void error(final String domain, final String key,
            final XMLParseException exception) throws XNIException {
        println("Err", key, exception);
    }

    private void println(final String type, String key, XMLParseException exception) throws XNIException {
        try {
            out_.append("[").append(type).append("] ").append(key).append(" ").append(exception.getMessage()).append("\n");
        }
        catch (final IOException e) {
            throw new XNIException(e);
        }
    }

    /** @see DefaultErrorHandler#warning(String,String,XMLParseException) */
    @Override
    public void warning(final String domain, final String key,
            final XMLParseException exception) throws XNIException {
        println("Warn", key, exception);
    }
}
