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
package org.htmlunit.cyberneko.xerces.parsers;

/**
 * Commonly used constants.
 * <p>
 *
 * @author Andy Clark, IBM
 * @author Ronald Brill
 */
public final class Constants {
    /** SAX feature prefix ("http://xml.org/sax/features/"). */
    public static final String SAX_FEATURE_PREFIX = "http://xml.org/sax/features/";

    /** Namespaces feature ("namespaces"). */
    public static final String NAMESPACES_FEATURE = "namespaces";

    /** Namespace prefixes feature ("namespace-prefixes"). */
    public static final String NAMESPACE_PREFIXES_FEATURE = "namespace-prefixes";

    /**
     * Lexical handler parameter entities feature
     * ("lexical-handler/parameter-entities").
     */
    public static final String LEXICAL_HANDLER_PARAMETER_ENTITIES_FEATURE = "lexical-handler/parameter-entities";

    /**
     * Unicode normalization checking feature ("unicode-normalization-checking").
     */
    public static final String UNICODE_NORMALIZATION_CHECKING_FEATURE = "unicode-normalization-checking";

    // sax properties

    /** SAX property prefix ("http://xml.org/sax/properties/"). */
    public static final String SAX_PROPERTY_PREFIX = "http://xml.org/sax/properties/";

    /** Lexical handler property ("lexical-handler"). */
    public static final String LEXICAL_HANDLER_PROPERTY = "lexical-handler";

    /** Document XML version property ("document-xml-version"). */
    public static final String DOCUMENT_XML_VERSION_PROPERTY = "document-xml-version";

    // DOM features

    /** Comments feature ("include-comments"). */
    public static final String INCLUDE_COMMENTS_FEATURE = "include-comments";

    /** Create cdata nodes feature ("create-cdata-nodes"). */
    public static final String CREATE_CDATA_NODES_FEATURE = "create-cdata-nodes";

    // xerces features

    /** Xerces features prefix ("http://apache.org/xml/features/"). */
    public static final String XERCES_FEATURE_PREFIX = "http://apache.org/xml/features/";

    // xerces properties

    /** Xerces properties prefix ("http://apache.org/xml/properties/"). */
    public static final String XERCES_PROPERTY_PREFIX = "http://apache.org/xml/properties/";

    /** Error handler property ("internal/error-handler"). */
    public static final String ERROR_HANDLER_PROPERTY = "internal/error-handler";

    private Constants() {
    }
}
