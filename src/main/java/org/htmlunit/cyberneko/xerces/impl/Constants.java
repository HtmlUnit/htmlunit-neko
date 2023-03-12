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

package org.htmlunit.cyberneko.xerces.impl;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * Commonly used constants.
 * <p>
 *
 * @author Andy Clark, IBM
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

    //
    // DOM features
    //

    /** Comments feature ("include-comments"). */
    public static final String INCLUDE_COMMENTS_FEATURE = "include-comments";

    /** Create cdata nodes feature ("create-cdata-nodes"). */
    public static final String CREATE_CDATA_NODES_FEATURE = "create-cdata-nodes";

    // xerces features

    /** Xerces features prefix ("http://apache.org/xml/features/"). */
    public static final String XERCES_FEATURE_PREFIX = "http://apache.org/xml/features/";

    /** Continue after fatal error feature ("continue-after-fatal-error"). */
    public static final String CONTINUE_AFTER_FATAL_ERROR_FEATURE = "continue-after-fatal-error";

    /** Create entity reference nodes feature ("dom/create-entity-ref-nodes"). */
    public static final String CREATE_ENTITY_REF_NODES_FEATURE = "dom/create-entity-ref-nodes";

    /**
     * Include ignorable whitespace feature ("dom/include-ignorable-whitespace").
     */
    public static final String INCLUDE_IGNORABLE_WHITESPACE = "dom/include-ignorable-whitespace";

    /**
     * Notify built-in (&amp;amp;, etc.) references feature
     * (scanner/notify-builtin-refs").
     */
    public static final String NOTIFY_BUILTIN_REFS_FEATURE = "scanner/notify-builtin-refs";

    /** Standard URI conformant feature ("standard-uri-conformant"). */
    public static final String STANDARD_URI_CONFORMANT_FEATURE = "standard-uri-conformant";

    // xerces properties

    /** Xerces properties prefix ("http://apache.org/xml/properties/"). */
    public static final String XERCES_PROPERTY_PREFIX = "http://apache.org/xml/properties/";

    /** Error handler property ("internal/error-handler"). */
    public static final String ERROR_HANDLER_PROPERTY = "internal/error-handler";

    /** SAX properties. */
    private static final String[] fgSAXProperties = { LEXICAL_HANDLER_PROPERTY, };

    /** Xerces features. */
    private static final String[] fgXercesFeatures = { CONTINUE_AFTER_FATAL_ERROR_FEATURE,
            CREATE_ENTITY_REF_NODES_FEATURE, INCLUDE_IGNORABLE_WHITESPACE,
            NOTIFY_BUILTIN_REFS_FEATURE, STANDARD_URI_CONFORMANT_FEATURE, };

    /** Xerces properties. */
    private static final String[] fgXercesProperties = { ERROR_HANDLER_PROPERTY };

    /** Empty enumeration. */
    private static final Enumeration<Object> fgEmptyEnumeration = new ArrayEnumeration(new Object[] {});

    //
    // Constructors
    //

    /** This class cannot be instantiated. */
    private Constants() {
    }

    //
    // Public methods
    //

    /** @return an enumeration of the SAX properties. */
    public static Enumeration<Object> getSAXProperties() {
        return fgSAXProperties.length > 0 ? new ArrayEnumeration(fgSAXProperties) : fgEmptyEnumeration;
    }

    // xerces

    /** @return an enumeration of the Xerces features. */
    public static Enumeration<Object> getXercesFeatures() {
        return fgXercesFeatures.length > 0 ? new ArrayEnumeration(fgXercesFeatures) : fgEmptyEnumeration;
    }

    /** @return an enumeration of the Xerces properties. */
    public static Enumeration<Object> getXercesProperties() {
        return fgXercesProperties.length > 0 ? new ArrayEnumeration(fgXercesProperties) : fgEmptyEnumeration;
    }

    //
    // Classes
    //

    /**
     * An array enumeration.
     *
     * @author Andy Clark, IBM
     */
    static class ArrayEnumeration implements Enumeration<Object> {

        /** Array. */
        private final Object[] array;

        /** Index. */
        private int index;

        /** Constructs an array enumeration. */
        public ArrayEnumeration(Object[] array) {
            this.array = array;
        }

        /**
         * {@inheritDoc}
         *
         * Tests if this enumeration contains more elements.
         *
         * @return <code>true</code> if this enumeration contains more elements;
         *         <code>false</code> otherwise.
         */
        @Override
        public boolean hasMoreElements() {
            return index < array.length;
        } // hasMoreElement():boolean

        /**
         * {@inheritDoc}
         *
         * Returns the next element of this enumeration.
         *
         * @return the next element of this enumeration.
         * @exception NoSuchElementException if no more elements exist.
         */
        @Override
        public Object nextElement() {
            if (index < array.length) {
                return array[index++];
            }
            throw new NoSuchElementException();
        }

    }

    // Prints all of the constants to standard output.
    public static void main(String[] argv) {
        print("SAX properties:", SAX_PROPERTY_PREFIX, fgSAXProperties);
        print("Xerces features:", XERCES_FEATURE_PREFIX, fgXercesFeatures);
        print("Xerces properties:", XERCES_PROPERTY_PREFIX, fgXercesProperties);
    }

    // Prints a list of features/properties.
    private static void print(String header, String prefix, Object[] array) {
        System.out.print(header);
        if (array.length > 0) {
            System.out.println();
            for (Object o : array) {
                System.out.print("  ");
                System.out.print(prefix);
                System.out.println(o);
            }
        } else {
            System.out.println(" none.");
        }
    }
}
