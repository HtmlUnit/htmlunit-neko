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
package org.htmlunit.cyberneko.xerces.parsers;

import java.io.IOException;

import org.htmlunit.cyberneko.xerces.impl.Constants;
import org.htmlunit.cyberneko.xerces.xni.XNIException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLInputSource;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLParserConfiguration;

/**
 * Base class of all XML-related parsers.
 * <p>
 * In addition to the features and properties recognized by the parser
 * configuration, this parser recognizes these additional features and
 * properties:
 * <ul>
 * <li>Properties
 * <ul>
 * <li>http://apache.org/xml/properties/internal/error-handler</li>
 * <li>http://apache.org/xml/properties/internal/entity-resolver</li>
 * </ul>
 * </ul>
 *
 * @author Arnaud Le Hors, IBM
 * @author Andy Clark, IBM
 */
public abstract class XMLParser {

    /** Property identifier: error handler. */
    protected static final String ERROR_HANDLER = Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_HANDLER_PROPERTY;

    /** Recognized properties. */
    private static final String[] RECOGNIZED_PROPERTIES = {ERROR_HANDLER};

    /** The parser configuration. */
    protected final XMLParserConfiguration fConfiguration;

    // Default Constructor.
    protected XMLParser(final XMLParserConfiguration config) {
        // save configuration
        fConfiguration = config;

        // add default recognized properties
        fConfiguration.addRecognizedProperties(RECOGNIZED_PROPERTIES);

    }

    /**
     * Parse.
     *
     * @param inputSource the input source
     *
     * @exception XNIException        on error
     * @exception java.io.IOException on error
     */
    public void parse(final XMLInputSource inputSource) throws XNIException, IOException {

        reset();
        fConfiguration.parse(inputSource);

    }

    /**
     * reset all components before parsing
     */
    protected void reset() throws XNIException {
    }
}
