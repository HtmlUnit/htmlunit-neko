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

    /** Lexical handler parameter entities feature ("lexical-handler/parameter-entities"). */
    public static final String LEXICAL_HANDLER_PARAMETER_ENTITIES_FEATURE = "lexical-handler/parameter-entities";

    /** Unicode normalization checking feature ("unicode-normalization-checking"). */
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

    /** Feature id: load as infoset. */
    public static final String LOAD_AS_INFOSET = "load-as-infoset";


    //
    // Constants: DOM Level 3 feature ids
    //

    public static final String DOM_CANONICAL_FORM = "canonical-form";
    public static final String DOM_CDATA_SECTIONS ="cdata-sections";
    public static final String DOM_COMMENTS = "comments";

    // REVISIT: this feature seems to have no effect for Xerces
    public static final String DOM_CHARSET_OVERRIDES_XML_ENCODING =
        "charset-overrides-xml-encoding";

    public static final String DOM_DATATYPE_NORMALIZATION = "datatype-normalization";
    public static final String DOM_ENTITIES = "entities";
    public static final String DOM_INFOSET = "infoset";
    public static final String DOM_NAMESPACES = "namespaces";
    public static final String DOM_NAMESPACE_DECLARATIONS = "namespace-declarations";
    public static final String DOM_SUPPORTED_MEDIATYPES_ONLY =
        "supported-media-types-only";

    public static final String DOM_VALIDATE = "validate";
    public static final String DOM_ELEMENT_CONTENT_WHITESPACE =
        "element-content-whitespace";

    // DOM Level 3 features defined in Core:
    public static final String DOM_DISCARD_DEFAULT_CONTENT = "discard-default-content";
    public static final String DOM_NORMALIZE_CHARACTERS    = "normalize-characters";
    public static final String DOM_CHECK_CHAR_NORMALIZATION  = "check-character-normalization";
    public static final String DOM_WELLFORMED  = "well-formed";
    public static final String DOM_SPLIT_CDATA = "split-cdata-sections";

    // Load and Save
    public static final String DOM_FORMAT_PRETTY_PRINT = "format-pretty-print";
    public static final String DOM_XMLDECL = "xml-declaration";
    public static final String DOM_UNKNOWNCHARS = "unknown-characters";
    public static final String DOM_DISALLOW_DOCTYPE =  "disallow-doctype";
    public static final String DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS =  "ignore-unknown-character-denormalizations";

    // DOM Properties
    public static final String DOM_ERROR_HANDLER = "error-handler";


    // xerces features

    /** Xerces features prefix ("http://apache.org/xml/features/"). */
    public static final String XERCES_FEATURE_PREFIX = "http://apache.org/xml/features/";

    /** Warn on duplicate entity declaration feature ("warn-on-duplicate-entitydef"). */
    public static final String WARN_ON_DUPLICATE_ENTITYDEF_FEATURE = "warn-on-duplicate-entitydef";

    /** Allow Java encoding names feature ("allow-java-encodings"). */
    public static final String ALLOW_JAVA_ENCODINGS_FEATURE = "allow-java-encodings";

    /** Disallow DOCTYPE declaration feature ("disallow-doctype-decl"). */
    public static final String DISALLOW_DOCTYPE_DECL_FEATURE = "disallow-doctype-decl";

    /** Continue after fatal error feature ("continue-after-fatal-error"). */
    public static final String CONTINUE_AFTER_FATAL_ERROR_FEATURE = "continue-after-fatal-error";

    /** Load dtd grammar when nonvalidating feature ("nonvalidating/load-dtd-grammar"). */
    public static final String LOAD_DTD_GRAMMAR_FEATURE = "nonvalidating/load-dtd-grammar";

    /** Load external dtd when nonvalidating feature ("nonvalidating/load-external-dtd"). */
    public static final String LOAD_EXTERNAL_DTD_FEATURE = "nonvalidating/load-external-dtd";

    /** Defer node expansion feature ("dom/defer-node-expansion"). */
    public static final String DEFER_NODE_EXPANSION_FEATURE = "dom/defer-node-expansion";

    /** Create entity reference nodes feature ("dom/create-entity-ref-nodes"). */
    public static final String CREATE_ENTITY_REF_NODES_FEATURE = "dom/create-entity-ref-nodes";

    /** Include ignorable whitespace feature ("dom/include-ignorable-whitespace"). */
    public static final String INCLUDE_IGNORABLE_WHITESPACE = "dom/include-ignorable-whitespace";

    /** Default attribute values feature ("validation/default-attribute-values"). */
    public static final String DEFAULT_ATTRIBUTE_VALUES_FEATURE = "validation/default-attribute-values";

    /** Validate content models feature ("validation/validate-content-models"). */
    public static final String VALIDATE_CONTENT_MODELS_FEATURE = "validation/validate-content-models";

    /** Validate datatypes feature ("validation/validate-datatypes"). */
    public static final String VALIDATE_DATATYPES_FEATURE = "validation/validate-datatypes";

    /** Balance syntax trees feature ("validation/balance-syntax-trees"). */
    public static final String BALANCE_SYNTAX_TREES = "validation/balance-syntax-trees";

    /** Notify character references feature (scanner/notify-char-refs"). */
    public static final String NOTIFY_CHAR_REFS_FEATURE = "scanner/notify-char-refs";

    /** Notify built-in (&amp;amp;, etc.) references feature (scanner/notify-builtin-refs"). */
    public static final String NOTIFY_BUILTIN_REFS_FEATURE = "scanner/notify-builtin-refs";

    /** Standard URI conformant feature ("standard-uri-conformant"). */
    public static final String STANDARD_URI_CONFORMANT_FEATURE = "standard-uri-conformant";

    /** Generate synthetic annotations feature ("generate-synthetic-annotations"). */
    public static final String GENERATE_SYNTHETIC_ANNOTATIONS_FEATURE = "generate-synthetic-annotations";

    /** Validate annotations feature ("validate-annotations"). */
    public static final String VALIDATE_ANNOTATIONS_FEATURE = "validate-annotations";

    /** Honour all schemaLocations feature ("honour-all-schemaLocations"). */
    public static final String HONOUR_ALL_SCHEMALOCATIONS_FEATURE = "honour-all-schemaLocations";

    /** Namespace growth feature ("namespace-growth"). */
    public static final String NAMESPACE_GROWTH_FEATURE = "namespace-growth";

    /** Tolerate duplicates feature ("internal/tolerate-duplicates"). */
    public static final String TOLERATE_DUPLICATES_FEATURE = "internal/tolerate-duplicates";

    /** String interned feature ("internal/strings-interned"). */
    public static final String STRINGS_INTERNED_FEATURE = "internal/strings-interned";

    /** XInclude processing feature ("xinclude"). */
    public static final String XINCLUDE_FEATURE = "xinclude";

    /** XInclude fixup base URIs feature ("xinclude/fixup-base-uris"). */
    public static final String XINCLUDE_FIXUP_BASE_URIS_FEATURE = "xinclude/fixup-base-uris";

    /** XInclude fixup language feature ("xinclude/fixup-language"). */
    public static final String XINCLUDE_FIXUP_LANGUAGE_FEATURE = "xinclude/fixup-language";

    /**
     * Feature to ignore xsi:type attributes on elements during validation,
     * until a global element declaration is found. ("validation/schema/ignore-xsi-type-until-elemdecl")
     * If this feature is on when validating a document, then beginning at the validation root
     * element, xsi:type attributes are ignored until a global element declaration is
     * found for an element.  Once a global element declaration has been found, xsi:type
     * attributes will start being processed for the sub-tree beginning at the element for
     * which the declaration was found.
     * <p>
     * Suppose an element A has two element children, B and C.
     * <p>
     * If a global element declaration is found for A, xsi:type attributes on A, B and C,
     * and all of B and C's descendents, will be processed.
     * <p>
     * If no global element declaration is found for A or B, but one is found for C,
     * then xsi:type attributes will be ignored on A and B (and any descendents of B,
     * until a global element declaration is found), but xsi:type attributes will be
     * processed for C and all of C's descendents.
     * <p>
     * Once xsi:type attributes stop being ignored for a subtree, they do not start
     * being ignored again, even if more elements are encountered for which no global
     * element declaration can be found.
     */
    public static final String IGNORE_XSI_TYPE_FEATURE = "validation/schema/ignore-xsi-type-until-elemdecl";

    /** Perform checking of ID/IDREFs ("validation/id-idref-checking") */
    public static final String ID_IDREF_CHECKING_FEATURE = "validation/id-idref-checking";

    /** Feature to ignore errors caused by identity constraints ("validation/identity-constraint-checking") */
    public static final String IDC_CHECKING_FEATURE = "validation/identity-constraint-checking";

    /** Feature to ignore errors caused by unparsed entities ("validation/unparsed-entity-checking") */
    public static final String UNPARSED_ENTITY_CHECKING_FEATURE = "validation/unparsed-entity-checking";

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

    /** Namespace context property ("internal/namespace-context"). */
    public static final String NAMESPACE_CONTEXT_PROPERTY = "internal/namespace-context";

    /** Schema type for the root element in a document ("validation/schema/root-type-definition"). */
    public static final String ROOT_TYPE_DEFINITION_PROPERTY = "validation/schema/root-type-definition";

    /** Schema element declaration for the root element in a document ("validation/schema/root-element-declaration"). */
    public static final String ROOT_ELEMENT_DECLARATION_PROPERTY = "validation/schema/root-element-declaration";

    // general constants

    /**
     * Boolean indicating whether an attribute is declared in the DTD is stored
     * in augmentations using the string "ATTRIBUTE_DECLARED". The absence of this
     * augmentation indicates that the attribute was not declared in the DTD.
     */
    public final static String ATTRIBUTE_DECLARED = "ATTRIBUTE_DECLARED";

    /**
     * Boolean indicating whether an entity referenced in the document has
     * not been read is stored in augmentations using the string "ENTITY_SKIPPED".
     * The absence of this augmentation indicates that the entity had a
     * declaration and was expanded.
     */
    public final static String ENTITY_SKIPPED = "ENTITY_SKIPPED";

    /**
     * Boolean indicating whether a character is a probable white space
     * character (ch &lt;= 0x20) that was the replacement text of a character
     * reference is stored in augmentations using the string "CHAR_REF_PROBABLE_WS".
     * The absence of this augmentation indicates that the character is not
     * probable white space and/or was not included from a character reference.
     */
    public final static String CHAR_REF_PROBABLE_WS = "CHAR_REF_PROBABLE_WS";

    // XML version constants
    public final static short XML_VERSION_ERROR = -1;
    public final static short XML_VERSION_1_0 = 1;
    public final static short XML_VERSION_1_1 = 2;

    // private

    /** SAX features. */
    private static final String[] fgSAXFeatures = {
            NAMESPACES_FEATURE,
            NAMESPACE_PREFIXES_FEATURE,
    };

    /** SAX properties. */
    private static final String[] fgSAXProperties = {
            LEXICAL_HANDLER_PROPERTY,
    };

    /** Xerces features. */
    private static final String[] fgXercesFeatures = {
            ALLOW_JAVA_ENCODINGS_FEATURE,
            CONTINUE_AFTER_FATAL_ERROR_FEATURE,
            LOAD_DTD_GRAMMAR_FEATURE,
            LOAD_EXTERNAL_DTD_FEATURE,
            CREATE_ENTITY_REF_NODES_FEATURE,
            INCLUDE_IGNORABLE_WHITESPACE,
            DEFAULT_ATTRIBUTE_VALUES_FEATURE,
            VALIDATE_CONTENT_MODELS_FEATURE,
            VALIDATE_DATATYPES_FEATURE,
            BALANCE_SYNTAX_TREES,
            NOTIFY_CHAR_REFS_FEATURE,
            NOTIFY_BUILTIN_REFS_FEATURE,
            DISALLOW_DOCTYPE_DECL_FEATURE,
            STANDARD_URI_CONFORMANT_FEATURE,
            GENERATE_SYNTHETIC_ANNOTATIONS_FEATURE,
            VALIDATE_ANNOTATIONS_FEATURE,
            HONOUR_ALL_SCHEMALOCATIONS_FEATURE,
            XINCLUDE_FEATURE,
            XINCLUDE_FIXUP_BASE_URIS_FEATURE,
            XINCLUDE_FIXUP_LANGUAGE_FEATURE,
            IGNORE_XSI_TYPE_FEATURE,
            ID_IDREF_CHECKING_FEATURE,
            IDC_CHECKING_FEATURE,
            UNPARSED_ENTITY_CHECKING_FEATURE,
            NAMESPACE_GROWTH_FEATURE,
            TOLERATE_DUPLICATES_FEATURE,
            STRINGS_INTERNED_FEATURE,
    };

    /** Xerces properties. */
    private static final String[] fgXercesProperties = {
            SYMBOL_TABLE_PROPERTY,
            ERROR_HANDLER_PROPERTY,
            ERROR_REPORTER_PROPERTY,
            ENTITY_MANAGER_PROPERTY,
            DOCUMENT_SCANNER_PROPERTY,
            BUFFER_SIZE_PROPERTY,
            ROOT_TYPE_DEFINITION_PROPERTY,
            ROOT_ELEMENT_DECLARATION_PROPERTY
    };

    /** Empty enumeration. */
    private static final Enumeration<Object> fgEmptyEnumeration = new ArrayEnumeration(new Object[] {});

    //
    // Constructors
    //

    /** This class cannot be instantiated. */
    private Constants() {}

    //
    // Public methods
    //

    // sax

    /** @return an enumeration of the SAX features. */
    public static Enumeration<Object> getSAXFeatures() {
        return fgSAXFeatures.length > 0
        ? new ArrayEnumeration(fgSAXFeatures) : fgEmptyEnumeration;
    }

    /** @return an enumeration of the SAX properties. */
    public static Enumeration<Object> getSAXProperties() {
        return fgSAXProperties.length > 0
        ? new ArrayEnumeration(fgSAXProperties) : fgEmptyEnumeration;
    }

    // xerces

    /** @return an enumeration of the Xerces features. */
    public static Enumeration<Object> getXercesFeatures() {
        return fgXercesFeatures.length > 0
        ? new ArrayEnumeration(fgXercesFeatures) : fgEmptyEnumeration;
    }

    /** @return an enumeration of the Xerces properties. */
    public static Enumeration<Object> getXercesProperties() {
        return fgXercesProperties.length > 0
        ? new ArrayEnumeration(fgXercesProperties) : fgEmptyEnumeration;
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
         * @return  <code>true</code> if this enumeration contains more elements;
         *          <code>false</code> otherwise.
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
         * @return     the next element of this enumeration.
         * @exception  NoSuchElementException  if no more elements exist.
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
        }
        else {
            System.out.println(" none.");
        }
    }
}
