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

package net.sourceforge.htmlunit.xerces.impl;

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

    /** Notify character references feature (scanner/notify-char-refs"). */
    public static final String NOTIFY_CHAR_REFS_FEATURE = "scanner/notify-char-refs";

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

    /** Symbol table property ("internal/symbol-table"). */
    public static final String SYMBOL_TABLE_PROPERTY = "internal/symbol-table";

    /** Error reporter property ("internal/error-reporter"). */
    public static final String ERROR_REPORTER_PROPERTY = "internal/error-reporter";

    /** Error handler property ("internal/error-handler"). */
    public static final String ERROR_HANDLER_PROPERTY = "internal/error-handler";

    /** Entity manager property ("internal/entity-manager"). */
    public static final String ENTITY_MANAGER_PROPERTY = "internal/entity-manager";

    /** Input buffer size property ("input-buffer-size"). */
    public static final String BUFFER_SIZE_PROPERTY = "input-buffer-size";

    /** Document scanner property ("internal/document-scanner"). */
    public static final String DOCUMENT_SCANNER_PROPERTY = "internal/document-scanner";

    /** Namespace binder property ("internal/namespace-binder"). */
    public static final String NAMESPACE_BINDER_PROPERTY = "internal/namespace-binder";

    // general constants

    /**
     * Boolean indicating whether an attribute is declared in the DTD is stored in
     * augmentations using the string "ATTRIBUTE_DECLARED". The absence of this
     * augmentation indicates that the attribute was not declared in the DTD.
     */
    public final static String ATTRIBUTE_DECLARED = "ATTRIBUTE_DECLARED";

    /**
     * Boolean indicating whether an entity referenced in the document has not been
     * read is stored in augmentations using the string "ENTITY_SKIPPED". The
     * absence of this augmentation indicates that the entity had a declaration and
     * was expanded.
     */
    public final static String ENTITY_SKIPPED = "ENTITY_SKIPPED";

    /**
     * Boolean indicating whether a character is a probable white space character
     * (ch &lt;= 0x20) that was the replacement text of a character reference is
     * stored in augmentations using the string "CHAR_REF_PROBABLE_WS". The absence
     * of this augmentation indicates that the character is not probable white space
     * and/or was not included from a character reference.
     */
    public final static String CHAR_REF_PROBABLE_WS = "CHAR_REF_PROBABLE_WS";

    // XML version constants
    public final static short XML_VERSION_ERROR = -1;
    public final static short XML_VERSION_1_0 = 1;
    public final static short XML_VERSION_1_1 = 2;

    // private

    /** SAX features. */
    private static final String[] fgSAXFeatures = { NAMESPACES_FEATURE, NAMESPACE_PREFIXES_FEATURE, };

    /** SAX properties. */
    private static final String[] fgSAXProperties = { LEXICAL_HANDLER_PROPERTY, };

    /** Xerces features. */
    private static final String[] fgXercesFeatures = { CONTINUE_AFTER_FATAL_ERROR_FEATURE,
            CREATE_ENTITY_REF_NODES_FEATURE, INCLUDE_IGNORABLE_WHITESPACE, NOTIFY_CHAR_REFS_FEATURE,
            NOTIFY_BUILTIN_REFS_FEATURE, STANDARD_URI_CONFORMANT_FEATURE, };

    /** Xerces properties. */
    private static final String[] fgXercesProperties = { SYMBOL_TABLE_PROPERTY, ERROR_HANDLER_PROPERTY,
            ERROR_REPORTER_PROPERTY, ENTITY_MANAGER_PROPERTY, DOCUMENT_SCANNER_PROPERTY, BUFFER_SIZE_PROPERTY, };

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

    // sax

    /** @return an enumeration of the SAX features. */
    public static Enumeration<Object> getSAXFeatures() {
        return fgSAXFeatures.length > 0 ? new ArrayEnumeration(fgSAXFeatures) : fgEmptyEnumeration;
    }

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

        print("SAX features:", SAX_FEATURE_PREFIX, fgSAXFeatures);
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
