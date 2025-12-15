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

import org.htmlunit.cyberneko.xerces.xni.parser.XMLParseException;

/**
 * Defines an error reporter for reporting HTML errors. There is no such
 * thing as a fatal error in parsing HTML. I/O errors are fatal but should
 * throw an <code>IOException</code> directly instead of reporting an error.
 * <p>
 * When used in a configuration, the error reporter instance should be
 * set as a property with the following property identifier:
 * <pre>
 * "http://cyberneko.org/html/internal/error-reporter" in the
 * </pre>
 * Components in the configuration can query the error reporter using this
 * property identifier.
 * <p>
 * <strong>Note:</strong>
 * All reported errors are within the domain "http://cyberneko.org/html".
 *
 * @author Andy Clark
 */
public interface HTMLErrorReporter {

    /**
     * Format message without reporting error.
     * @param key key
     * @param args args
     * @return string
     */
    String formatMessage(String key, Object[] args);

    /**
     * Reports a warning.
     * @param key key
     * @param args args
     */
    void reportWarning(String key, Object[] args) throws XMLParseException;

    /**
     * Reports an error.
     * @param key key
     * @param args args
     */
    void reportError(String key, Object[] args) throws XMLParseException;
}
