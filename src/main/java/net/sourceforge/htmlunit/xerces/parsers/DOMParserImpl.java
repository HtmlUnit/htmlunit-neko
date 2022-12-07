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

package net.sourceforge.htmlunit.xerces.parsers;

import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.ls.LSResourceResolver;

import net.sourceforge.htmlunit.xerces.dom.DOMMessageFormatter;
import net.sourceforge.htmlunit.xerces.dom.DOMStringListImpl;
import net.sourceforge.htmlunit.xerces.impl.Constants;
import net.sourceforge.htmlunit.xerces.util.DOMEntityResolverWrapper;
import net.sourceforge.htmlunit.xerces.util.DOMErrorHandlerWrapper;
import net.sourceforge.htmlunit.xerces.util.ObjectFactory;
import net.sourceforge.htmlunit.xerces.util.XMLSymbols;
import net.sourceforge.htmlunit.xerces.xni.Augmentations;
import net.sourceforge.htmlunit.xerces.xni.QName;
import net.sourceforge.htmlunit.xerces.xni.XMLAttributes;
import net.sourceforge.htmlunit.xerces.xni.XNIException;
import net.sourceforge.htmlunit.xerces.xni.parser.XMLConfigurationException;
import net.sourceforge.htmlunit.xerces.xni.parser.XMLEntityResolver;
import net.sourceforge.htmlunit.xerces.xni.parser.XMLParserConfiguration;

/**
 * This is Xerces DOM Builder class. It uses the abstract DOM
 * parser with a document scanner, a dtd scanner, and a validator, as
 * well as a grammar pool.
 *
 * @author Pavani Mukthipudi, Sun Microsystems Inc.
 * @author Elena Litani, IBM
 * @author Rahul Srivastava, Sun Microsystems Inc.
 */
public class DOMParserImpl
    extends AbstractDOMParser implements DOMConfiguration {

    // SAX & Xerces feature ids

    /** Feature identifier: namespaces. */
    protected static final String NAMESPACES =
        Constants.SAX_FEATURE_PREFIX + Constants.NAMESPACES_FEATURE;

    /** Feature id: validation. */
    protected static final String VALIDATION_FEATURE =
        Constants.SAX_FEATURE_PREFIX+Constants.VALIDATION_FEATURE;

    /** XML Schema validation */
    protected static final String XMLSCHEMA =
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_VALIDATION_FEATURE;

    /** Dynamic validation */
    protected static final String DYNAMIC_VALIDATION =
        Constants.XERCES_FEATURE_PREFIX + Constants.DYNAMIC_VALIDATION_FEATURE;

    /** Feature identifier: expose schema normalized value */
    protected static final String NORMALIZE_DATA =
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_NORMALIZED_VALUE;

    /** Feature identifier: disallow docType Decls. */
    protected static final String DISALLOW_DOCTYPE_DECL_FEATURE =
        Constants.XERCES_FEATURE_PREFIX + Constants.DISALLOW_DOCTYPE_DECL_FEATURE;

    /** Feature identifier: honour all schemaLocations */
    protected static final String HONOUR_ALL_SCHEMALOCATIONS =
        Constants.XERCES_FEATURE_PREFIX + Constants.HONOUR_ALL_SCHEMALOCATIONS_FEATURE;

    /** Feature identifier: namespace growth */
    protected static final String NAMESPACE_GROWTH =
        Constants.XERCES_FEATURE_PREFIX + Constants.NAMESPACE_GROWTH_FEATURE;

    /** Feature identifier: tolerate duplicates */
    protected static final String TOLERATE_DUPLICATES =
        Constants.XERCES_FEATURE_PREFIX + Constants.TOLERATE_DUPLICATES_FEATURE;

    // internal properties
    protected static final String SYMBOL_TABLE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;

    //
    // Data
    //

    /** Include namespace declaration attributes in the document. **/
    protected boolean fNamespaceDeclarations = true;

    // REVISIT: this value should be null by default and should be set during creation of
    //          LSParser
    protected String fSchemaType = null;

    protected boolean fBusy = false;

    protected final static boolean DEBUG = false;

    private String fSchemaLocation = null;
    private DOMStringList fRecognizedParameters;

    // Constructs a DOM Builder using the standard parser configuration.
    public DOMParserImpl (String configuration, String schemaType) {
        this (
        (XMLParserConfiguration) ObjectFactory.createObject (
        "net.sourceforge.htmlunit.xerces.xni.parser.XMLParserConfiguration",
        configuration));
        if (schemaType != null) {
            if (schemaType.equals (Constants.NS_DTD)) {
                //Schema validation is false by default and hence there is no
                //need to set it to false here.  Also, schema validation is
                //not a recognized feature for DTDConfiguration's and so
                //setting this feature here would result in a Configuration
                //Exception.
                fConfiguration.setProperty (
                Constants.JAXP_PROPERTY_PREFIX + Constants.SCHEMA_LANGUAGE,
                Constants.NS_DTD);
                fSchemaType = Constants.NS_DTD;
            }
            else if (schemaType.equals (Constants.NS_XMLSCHEMA)) {
                // XML Schem validation
                fConfiguration.setProperty (
                Constants.JAXP_PROPERTY_PREFIX + Constants.SCHEMA_LANGUAGE,
                Constants.NS_XMLSCHEMA);
            }
        }
    }

    // Constructs a DOM Builder using the specified parser configuration.
    public DOMParserImpl (XMLParserConfiguration config) {
        super (config);

        // add recognized features
        final String[] domRecognizedFeatures = {
            Constants.DOM_CANONICAL_FORM,
            Constants.DOM_CDATA_SECTIONS,
            Constants.DOM_CHARSET_OVERRIDES_XML_ENCODING,
            Constants.DOM_INFOSET,
            Constants.DOM_NAMESPACE_DECLARATIONS,
            Constants.DOM_SPLIT_CDATA,
            Constants.DOM_SUPPORTED_MEDIATYPES_ONLY,
            Constants.DOM_CERTIFIED,
            Constants.DOM_WELLFORMED,
            Constants.DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS,
        };

        fConfiguration.addRecognizedFeatures (domRecognizedFeatures);

        // Set values so that the value of the
        // infoset parameter is true (its default value).
        //
        // true: namespace-declarations, well-formed,
        // element-content-whitespace, comments, namespaces
        //
        // false: validate-if-schema, entities,
        // datatype-normalization, cdata-sections

        fConfiguration.setFeature(Constants.DOM_NAMESPACE_DECLARATIONS, true);
        fConfiguration.setFeature(Constants.DOM_WELLFORMED, true);
        fConfiguration.setFeature(INCLUDE_COMMENTS_FEATURE, true);
        fConfiguration.setFeature(INCLUDE_IGNORABLE_WHITESPACE, true);
        fConfiguration.setFeature(NAMESPACES, true);

        fConfiguration.setFeature(DYNAMIC_VALIDATION, false);
        fConfiguration.setFeature(CREATE_ENTITY_REF_NODES, false);
        fConfiguration.setFeature(CREATE_CDATA_NODES_FEATURE, false);

        // set other default values
        fConfiguration.setFeature (Constants.DOM_CANONICAL_FORM, false);
        fConfiguration.setFeature (Constants.DOM_CHARSET_OVERRIDES_XML_ENCODING, true);
        fConfiguration.setFeature (Constants.DOM_SPLIT_CDATA, true);
        fConfiguration.setFeature (Constants.DOM_SUPPORTED_MEDIATYPES_ONLY, false);
        fConfiguration.setFeature (Constants.DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS, true);

        // REVISIT: by default Xerces assumes that input is certified.
        //          default is different from the one specified in the DOM spec
        fConfiguration.setFeature (Constants.DOM_CERTIFIED, true);

        // Xerces datatype-normalization feature is on by default
        // This is a recognized feature only for XML Schemas. If the
        // configuration doesn't support this feature, ignore it.
        try {
            fConfiguration.setFeature ( NORMALIZE_DATA, false );
        }
        catch (XMLConfigurationException exc) {}

    } // <init>(XMLParserConfiguration)

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset () {
        super.reset();

        // get state of namespace-declarations parameter.
        fNamespaceDeclarations =
            fConfiguration.getFeature(Constants.DOM_NAMESPACE_DECLARATIONS);

        if (fSkippedElemStack != null) {
            fSkippedElemStack.removeAllElements();
        }
        fRejectedElementDepth = 0;
        fFilterReject = false;
        fSchemaType = null;

    } // reset()

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParameter (String name, Object value) throws DOMException {
        // set features

        if (value instanceof Boolean) {
            boolean state = ((Boolean)value).booleanValue();
            try {
                if (name.equalsIgnoreCase (Constants.DOM_COMMENTS)) {
                    fConfiguration.setFeature (INCLUDE_COMMENTS_FEATURE, state);
                }
                else if (name.equalsIgnoreCase (Constants.DOM_DATATYPE_NORMALIZATION)) {
                    fConfiguration.setFeature (NORMALIZE_DATA, state);
                }
                else if (name.equalsIgnoreCase (Constants.DOM_ENTITIES)) {
                    fConfiguration.setFeature (CREATE_ENTITY_REF_NODES, state);
                }
                else if (name.equalsIgnoreCase (Constants.DOM_DISALLOW_DOCTYPE)) {
                    fConfiguration.setFeature (DISALLOW_DOCTYPE_DECL_FEATURE, state);
                }
                else if (name.equalsIgnoreCase (Constants.DOM_SUPPORTED_MEDIATYPES_ONLY)
                || name.equalsIgnoreCase(Constants.DOM_NORMALIZE_CHARACTERS)
                || name.equalsIgnoreCase (Constants.DOM_CHECK_CHAR_NORMALIZATION)
                || name.equalsIgnoreCase (Constants.DOM_CANONICAL_FORM)) {
                    if (state) { // true is not supported
                        throw newFeatureNotSupportedError(name);
                    }
                    // setting those features to false is no-op
                }
                else if (name.equalsIgnoreCase (Constants.DOM_NAMESPACES)) {
                    fConfiguration.setFeature (NAMESPACES, state);
                }
                else if (name.equalsIgnoreCase (Constants.DOM_INFOSET)) {
                    // Setting false has no effect.
                    if (state) {
                        // true: namespaces, namespace-declarations,
                        // comments, element-content-whitespace
                        fConfiguration.setFeature(NAMESPACES, true);
                        fConfiguration.setFeature(Constants.DOM_NAMESPACE_DECLARATIONS, true);
                        fConfiguration.setFeature(INCLUDE_COMMENTS_FEATURE, true);
                        fConfiguration.setFeature(INCLUDE_IGNORABLE_WHITESPACE, true);

                        // false: validate-if-schema, entities,
                        // datatype-normalization, cdata-sections
                        fConfiguration.setFeature(DYNAMIC_VALIDATION, false);
                        fConfiguration.setFeature(CREATE_ENTITY_REF_NODES, false);
                        fConfiguration.setFeature(NORMALIZE_DATA, false);
                        fConfiguration.setFeature(CREATE_CDATA_NODES_FEATURE, false);
                    }
                }
                else if (name.equalsIgnoreCase(Constants.DOM_CDATA_SECTIONS)) {
                    fConfiguration.setFeature(CREATE_CDATA_NODES_FEATURE, state);
                }
                else if (name.equalsIgnoreCase (Constants.DOM_NAMESPACE_DECLARATIONS)) {
                    fConfiguration.setFeature(Constants.DOM_NAMESPACE_DECLARATIONS, state);
                }
                else if (name.equalsIgnoreCase (Constants.DOM_WELLFORMED)
                || name.equalsIgnoreCase (Constants.DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS)) {
                    if (!state) { // false is not supported
                        throw newFeatureNotSupportedError(name);
                    }
                    // setting these features to true is no-op
                    // REVISIT: implement "namespace-declaration" feature
                }
                else if (name.equalsIgnoreCase (Constants.DOM_VALIDATE)) {
                    fConfiguration.setFeature (VALIDATION_FEATURE, state);
                    if (fSchemaType != Constants.NS_DTD) {
                        fConfiguration.setFeature (XMLSCHEMA, state);
                    }
                    if (state){
                        fConfiguration.setFeature (DYNAMIC_VALIDATION, false);
                    }
                }
                else if (name.equalsIgnoreCase (Constants.DOM_ELEMENT_CONTENT_WHITESPACE)) {
                    fConfiguration.setFeature (INCLUDE_IGNORABLE_WHITESPACE, state);
                }
                else {
                    // Constants.DOM_CHARSET_OVERRIDES_XML_ENCODING feature,
                    // Constants.DOM_SPLIT_CDATA feature,
                    // or any Xerces feature
                    String normalizedName;
                    // The honour-all-schemaLocations feature is
                    // mixed case so requires special treatment.
                    if (name.equalsIgnoreCase(HONOUR_ALL_SCHEMALOCATIONS)) {
                        normalizedName = HONOUR_ALL_SCHEMALOCATIONS;
                    }
                    else if (name.equals(NAMESPACE_GROWTH)) {
                        normalizedName = NAMESPACE_GROWTH;
                    }
                    else if (name.equals(TOLERATE_DUPLICATES)) {
                        normalizedName = TOLERATE_DUPLICATES;
                    }
                    else {
                        normalizedName = name.toLowerCase(Locale.ENGLISH);
                    }
                    fConfiguration.setFeature(normalizedName, state);
                }

            }
            catch (XMLConfigurationException e) {
                throw newFeatureNotFoundError(name);
            }
        }
        else { // set properties
            if (name.equalsIgnoreCase (Constants.DOM_ERROR_HANDLER)) {
                if (value instanceof DOMErrorHandler || value == null) {
                    try {
                        fErrorHandler = new DOMErrorHandlerWrapper ((DOMErrorHandler) value);
                        fConfiguration.setProperty (ERROR_HANDLER, fErrorHandler);
                    }
                    catch (XMLConfigurationException e) {}
                }
                else {
                    throw newTypeMismatchError(name);
                }

            }
            else if (name.equalsIgnoreCase (Constants.DOM_RESOURCE_RESOLVER)) {
                if (value == null) {
                    try {
                        fConfiguration.setProperty (ENTITY_RESOLVER, new DOMEntityResolverWrapper (null));
                    }
                    catch (XMLConfigurationException e) {}
                }
                else {
                    throw newTypeMismatchError(name);
                }

            }
            else if (name.equalsIgnoreCase (Constants.DOM_SCHEMA_LOCATION)) {
                if (value instanceof String || value == null) {
                    try {
                        if (value == null) {
                            fSchemaLocation = null;
                            fConfiguration.setProperty (
                                Constants.JAXP_PROPERTY_PREFIX + Constants.SCHEMA_SOURCE,
                                null);
                        }
                        else {
                            fSchemaLocation = (String)value;
                            // map DOM schema-location to JAXP schemaSource property
                            // tokenize location string
                            StringTokenizer t = new StringTokenizer (fSchemaLocation, " \n\t\r");
                            if (t.hasMoreTokens()) {
                                ArrayList<String> locations = new ArrayList<>();
                                locations.add (t.nextToken());
                                while (t.hasMoreTokens()) {
                                    locations.add (t.nextToken());
                                }
                                fConfiguration.setProperty (
                                Constants.JAXP_PROPERTY_PREFIX + Constants.SCHEMA_SOURCE,
                                locations.toArray ());
                            }
                            else {
                                fConfiguration.setProperty (
                                Constants.JAXP_PROPERTY_PREFIX + Constants.SCHEMA_SOURCE,
                                value);
                            }
                        }
                    }
                    catch (XMLConfigurationException e) {}
                }
                else {
                    throw newTypeMismatchError(name);
                }

            }
            else if (name.equalsIgnoreCase (Constants.DOM_SCHEMA_TYPE)) {
                if (value instanceof String || value == null) {
                    try {
                        if (value == null) {
                            // turn off schema features
                            fConfiguration.setFeature (XMLSCHEMA, false);
                            // map to JAXP schemaLanguage
                            fConfiguration.setProperty ( Constants.JAXP_PROPERTY_PREFIX
                            + Constants.SCHEMA_LANGUAGE,
                            null);
                            fSchemaType = null;
                        }
                        else if (value.equals (Constants.NS_XMLSCHEMA)) {
                            // turn on schema features
                            fConfiguration.setFeature (XMLSCHEMA, true);
                            // map to JAXP schemaLanguage
                            fConfiguration.setProperty ( Constants.JAXP_PROPERTY_PREFIX
                            + Constants.SCHEMA_LANGUAGE,
                            Constants.NS_XMLSCHEMA);
                            fSchemaType = Constants.NS_XMLSCHEMA;
                        }
                        else if (value.equals (Constants.NS_DTD)) {
                            // turn off schema features
                            fConfiguration.setFeature (XMLSCHEMA, false);
                            // map to JAXP schemaLanguage
                            fConfiguration.setProperty ( Constants.JAXP_PROPERTY_PREFIX
                            + Constants.SCHEMA_LANGUAGE,
                            Constants.NS_DTD);
                            fSchemaType = Constants.NS_DTD;
                        }
                    }
                    catch (XMLConfigurationException e) {}
                }
                else {
                    throw newTypeMismatchError(name);
                }

            }
            else if (name.equalsIgnoreCase (DOCUMENT_CLASS_NAME)) {
                fConfiguration.setProperty (DOCUMENT_CLASS_NAME, value);
            }
            else {
                // Try to set the property.
                String normalizedName = name.toLowerCase(Locale.ENGLISH);
                try {
                    fConfiguration.setProperty(normalizedName, value);
                    return;
                }
                catch (XMLConfigurationException e) {}

                // If this is a boolean parameter a type mismatch should be thrown.
                try {
                    // The honour-all-schemaLocations feature is
                    // mixed case so requires special treatment.
                    if (name.equalsIgnoreCase(HONOUR_ALL_SCHEMALOCATIONS)) {
                        normalizedName = HONOUR_ALL_SCHEMALOCATIONS;
                    }
                    else if (name.equals(NAMESPACE_GROWTH)) {
                        normalizedName = NAMESPACE_GROWTH;
                    }
                    else if (name.equals(TOLERATE_DUPLICATES)) {
                        normalizedName = TOLERATE_DUPLICATES;
                    }
                    fConfiguration.getFeature(normalizedName);
                    throw newTypeMismatchError(name);

                }
                catch (XMLConfigurationException e) {}

                // Parameter is not recognized
                throw newFeatureNotFoundError(name);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Look up the value of a feature or a property.
     */
    @Override
    public Object getParameter (String name) throws DOMException {
        if (name.equalsIgnoreCase (Constants.DOM_COMMENTS)) {
            return (fConfiguration.getFeature (INCLUDE_COMMENTS_FEATURE))
            ? Boolean.TRUE
            : Boolean.FALSE;
        }
        else if (name.equalsIgnoreCase (Constants.DOM_DATATYPE_NORMALIZATION)) {
            return (fConfiguration.getFeature (NORMALIZE_DATA))
            ? Boolean.TRUE
            : Boolean.FALSE;
        }
        else if (name.equalsIgnoreCase (Constants.DOM_ENTITIES)) {
            return (fConfiguration.getFeature (CREATE_ENTITY_REF_NODES))
            ? Boolean.TRUE
            : Boolean.FALSE;
        }
        else if (name.equalsIgnoreCase (Constants.DOM_NAMESPACES)) {
            return (fConfiguration.getFeature (NAMESPACES))
            ? Boolean.TRUE
            : Boolean.FALSE;
        }
        else if (name.equalsIgnoreCase (Constants.DOM_VALIDATE)) {
            return (fConfiguration.getFeature (VALIDATION_FEATURE))
            ? Boolean.TRUE
            : Boolean.FALSE;
        }
        else if (name.equalsIgnoreCase (Constants.DOM_ELEMENT_CONTENT_WHITESPACE)) {
            return (fConfiguration.getFeature (INCLUDE_IGNORABLE_WHITESPACE))
            ? Boolean.TRUE
            : Boolean.FALSE;
        }
        else if (name.equalsIgnoreCase (Constants.DOM_DISALLOW_DOCTYPE)) {
            return (fConfiguration.getFeature (DISALLOW_DOCTYPE_DECL_FEATURE))
            ? Boolean.TRUE
            : Boolean.FALSE;
        }
        else if (name.equalsIgnoreCase (Constants.DOM_INFOSET)) {
            // REVISIT: This is somewhat expensive to compute
            // but it's possible that the user has a reference
            // to the configuration and is changing the values
            // of these features directly on it.
            boolean infoset = fConfiguration.getFeature(NAMESPACES) &&
                fConfiguration.getFeature(Constants.DOM_NAMESPACE_DECLARATIONS) &&
                fConfiguration.getFeature(INCLUDE_COMMENTS_FEATURE) &&
                fConfiguration.getFeature(INCLUDE_IGNORABLE_WHITESPACE) &&
                !fConfiguration.getFeature(DYNAMIC_VALIDATION) &&
                !fConfiguration.getFeature(CREATE_ENTITY_REF_NODES) &&
                !fConfiguration.getFeature(NORMALIZE_DATA) &&
                !fConfiguration.getFeature(CREATE_CDATA_NODES_FEATURE);
            return (infoset) ? Boolean.TRUE : Boolean.FALSE;
        }
        else if (name.equalsIgnoreCase(Constants.DOM_CDATA_SECTIONS)) {
            return (fConfiguration.getFeature(CREATE_CDATA_NODES_FEATURE))
                ? Boolean.TRUE : Boolean.FALSE;
        }
        else if (name.equalsIgnoreCase(Constants.DOM_CHECK_CHAR_NORMALIZATION ) ||
                 name.equalsIgnoreCase(Constants.DOM_NORMALIZE_CHARACTERS)) {
            return Boolean.FALSE;
        }
        else if (name.equalsIgnoreCase(Constants.DOM_NAMESPACE_DECLARATIONS)
        || name.equalsIgnoreCase (Constants.DOM_WELLFORMED)
        || name.equalsIgnoreCase (Constants.DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS)
        || name.equalsIgnoreCase (Constants.DOM_CANONICAL_FORM)
        || name.equalsIgnoreCase (Constants.DOM_SUPPORTED_MEDIATYPES_ONLY)
        || name.equalsIgnoreCase (Constants.DOM_SPLIT_CDATA)
        || name.equalsIgnoreCase (Constants.DOM_CHARSET_OVERRIDES_XML_ENCODING)) {
            return (fConfiguration.getFeature (name.toLowerCase(Locale.ENGLISH)))
            ? Boolean.TRUE
            : Boolean.FALSE;
        }
        else if (name.equalsIgnoreCase (Constants.DOM_ERROR_HANDLER)) {
            if (fErrorHandler != null) {
                return fErrorHandler.getErrorHandler ();
            }
            return null;
        }
        else if (name.equalsIgnoreCase (Constants.DOM_RESOURCE_RESOLVER)) {
            try {
                XMLEntityResolver entityResolver =
                (XMLEntityResolver) fConfiguration.getProperty (ENTITY_RESOLVER);
                if (entityResolver != null
                        && entityResolver instanceof DOMEntityResolverWrapper) {
                    return ((DOMEntityResolverWrapper) entityResolver).getEntityResolver();
                }
            }
            catch (XMLConfigurationException e) {}
            return null;
        }
        else if (name.equalsIgnoreCase (Constants.DOM_SCHEMA_TYPE)) {
            return fConfiguration.getProperty (
            Constants.JAXP_PROPERTY_PREFIX + Constants.SCHEMA_LANGUAGE);
        }
        else if (name.equalsIgnoreCase (Constants.DOM_SCHEMA_LOCATION)) {
            return fSchemaLocation;
        }
        else if (name.equalsIgnoreCase (SYMBOL_TABLE)) {
            return fConfiguration.getProperty (SYMBOL_TABLE);
        }
        else if (name.equalsIgnoreCase (DOCUMENT_CLASS_NAME)) {
            return fConfiguration.getProperty (DOCUMENT_CLASS_NAME);
        }
        else {
            // This could be a recognized feature or property.
            String normalizedName;

            // The honour-all-schemaLocations feature is
            // mixed case so requires special treatment.
            if (name.equalsIgnoreCase(HONOUR_ALL_SCHEMALOCATIONS)) {
                normalizedName = HONOUR_ALL_SCHEMALOCATIONS;
            }
            else if (name.equals(NAMESPACE_GROWTH)) {
                normalizedName = NAMESPACE_GROWTH;
            }
            else if (name.equals(TOLERATE_DUPLICATES)) {
                normalizedName = TOLERATE_DUPLICATES;
            }
            else {
                normalizedName = name.toLowerCase(Locale.ENGLISH);
            }
            try {
                return fConfiguration.getFeature(normalizedName)
                    ? Boolean.TRUE : Boolean.FALSE;
            }
            catch (XMLConfigurationException e) {}

            // This isn't a feature; perhaps it's a property
            try {
                return fConfiguration.getProperty(normalizedName);
            }
            catch (XMLConfigurationException e) {}

            throw newFeatureNotFoundError(name);
        }
    }

    @Override
    public boolean canSetParameter (String name, Object value) {
        if (value == null) {
            return true;
        }

        if (value instanceof Boolean) {
            boolean state = ((Boolean)value).booleanValue();
            if ( name.equalsIgnoreCase (Constants.DOM_SUPPORTED_MEDIATYPES_ONLY)
            || name.equalsIgnoreCase(Constants.DOM_NORMALIZE_CHARACTERS)
            || name.equalsIgnoreCase(Constants.DOM_CHECK_CHAR_NORMALIZATION )
            || name.equalsIgnoreCase (Constants.DOM_CANONICAL_FORM) ) {
                // true is not supported
                return (state) ? false : true;
            }
            else if (name.equalsIgnoreCase (Constants.DOM_WELLFORMED)
            || name.equalsIgnoreCase (Constants.DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS)) {
                // false is not supported
                return (state) ? true : false;
            }
            else if (name.equalsIgnoreCase (Constants.DOM_CDATA_SECTIONS)
            || name.equalsIgnoreCase (Constants.DOM_CHARSET_OVERRIDES_XML_ENCODING)
            || name.equalsIgnoreCase (Constants.DOM_COMMENTS)
            || name.equalsIgnoreCase (Constants.DOM_DATATYPE_NORMALIZATION)
            || name.equalsIgnoreCase (Constants.DOM_DISALLOW_DOCTYPE)
            || name.equalsIgnoreCase (Constants.DOM_ENTITIES)
            || name.equalsIgnoreCase (Constants.DOM_INFOSET)
            || name.equalsIgnoreCase (Constants.DOM_NAMESPACES)
            || name.equalsIgnoreCase (Constants.DOM_NAMESPACE_DECLARATIONS)
            || name.equalsIgnoreCase (Constants.DOM_VALIDATE)
            || name.equalsIgnoreCase (Constants.DOM_ELEMENT_CONTENT_WHITESPACE)
            || name.equalsIgnoreCase (Constants.DOM_XMLDECL)) {
                return true;
            }

            // Recognize Xerces features.
            try {
                String normalizedName;
                // The honour-all-schemaLocations feature is
                // mixed case so requires special treatment.
                if (name.equalsIgnoreCase(HONOUR_ALL_SCHEMALOCATIONS)) {
                    normalizedName = HONOUR_ALL_SCHEMALOCATIONS;
                }
                else if (name.equalsIgnoreCase(NAMESPACE_GROWTH)) {
                    normalizedName = NAMESPACE_GROWTH;
                }
                else if (name.equalsIgnoreCase(TOLERATE_DUPLICATES)) {
                    normalizedName = TOLERATE_DUPLICATES;
                }
                else {
                    normalizedName = name.toLowerCase(Locale.ENGLISH);
                }
                fConfiguration.getFeature(normalizedName);
                return true;
            }
            catch (XMLConfigurationException e) {
                return false;
            }
        }
        else { // check properties
            if (name.equalsIgnoreCase (Constants.DOM_ERROR_HANDLER)) {
                if (value instanceof DOMErrorHandler || value == null) {
                    return true;
                }
                return false;
            }
            else if (name.equalsIgnoreCase (Constants.DOM_RESOURCE_RESOLVER)) {
                if (value instanceof LSResourceResolver || value == null) {
                    return true;
                }
                return false;
            }
            else if (name.equalsIgnoreCase (Constants.DOM_SCHEMA_TYPE)) {
                if ((value instanceof String
                && (value.equals (Constants.NS_XMLSCHEMA)
                || value.equals (Constants.NS_DTD))) || value == null) {
                    return true;
                }
                return false;
            }
            else if (name.equalsIgnoreCase (Constants.DOM_SCHEMA_LOCATION)) {
                if (value instanceof String || value == null)
                    return true;
                return false;
            }
            else if (name.equalsIgnoreCase (DOCUMENT_CLASS_NAME)) {
                return true;
            }

            // Recognize Xerces properties.
            try {
                fConfiguration.getProperty(name.toLowerCase(Locale.ENGLISH));
                return true;
            }
            catch (XMLConfigurationException e) {
                return false;
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     *  DOM Level 3 CR - Experimental.
     * <p>
     *  The list of the parameters supported by this
     * <code>DOMConfiguration</code> object and for which at least one value
     * can be set by the application. Note that this list can also contain
     * parameter names defined outside this specification.
     */
    @Override
    public DOMStringList getParameterNames () {
        if (fRecognizedParameters == null){
            ArrayList<String> parameters = new ArrayList<>();

            // REVISIT: add Xerces recognized properties/features
            parameters.add(Constants.DOM_NAMESPACES);
            parameters.add(Constants.DOM_CDATA_SECTIONS);
            parameters.add(Constants.DOM_CANONICAL_FORM);
            parameters.add(Constants.DOM_NAMESPACE_DECLARATIONS);
            parameters.add(Constants.DOM_SPLIT_CDATA);

            parameters.add(Constants.DOM_ENTITIES);
            parameters.add(Constants.DOM_VALIDATE);
            parameters.add(Constants.DOM_DATATYPE_NORMALIZATION);

            parameters.add(Constants.DOM_CHARSET_OVERRIDES_XML_ENCODING);
            parameters.add(Constants.DOM_CHECK_CHAR_NORMALIZATION);
            parameters.add(Constants.DOM_SUPPORTED_MEDIATYPES_ONLY);
            parameters.add(Constants.DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS);

            parameters.add(Constants.DOM_NORMALIZE_CHARACTERS);
            parameters.add(Constants.DOM_WELLFORMED);
            parameters.add(Constants.DOM_INFOSET);
            parameters.add(Constants.DOM_DISALLOW_DOCTYPE);
            parameters.add(Constants.DOM_ELEMENT_CONTENT_WHITESPACE);
            parameters.add(Constants.DOM_COMMENTS);

            parameters.add(Constants.DOM_ERROR_HANDLER);
            parameters.add(Constants.DOM_RESOURCE_RESOLVER);
            parameters.add(Constants.DOM_SCHEMA_LOCATION);
            parameters.add(Constants.DOM_SCHEMA_TYPE);

            fRecognizedParameters = new DOMStringListImpl(parameters);

        }

        return fRecognizedParameters;
    }

    /**
     * The start of an element. If the document specifies the start element
     * by using an empty tag, then the startElement method will immediately
     * be followed by the endElement method, with no intervening methods.
     * Overriding the parent to handle DOM_NAMESPACE_DECLARATIONS=false.
     *
     * @param element    The name of the element.
     * @param attributes The element attributes.
     * @param augs     Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void startElement (QName element, XMLAttributes attributes, Augmentations augs) {
        // namespace declarations parameter has no effect if namespaces is false.
        if (!fNamespaceDeclarations && fNamespaceAware) {
            int len = attributes.getLength();
            for (int i = len - 1; i >= 0; --i) {
                if (XMLSymbols.PREFIX_XMLNS == attributes.getPrefix(i) ||
                    XMLSymbols.PREFIX_XMLNS == attributes.getQName(i)) {
                    attributes.removeAttributeAt(i);
                }
            }
        }
        super.startElement(element, attributes, augs);
    }

    private static DOMException newFeatureNotSupportedError(String name) {
        String msg =
            DOMMessageFormatter.formatMessage (
                    DOMMessageFormatter.DOM_DOMAIN,
                    "FEATURE_NOT_SUPPORTED",
                    new Object[] { name });
        return new DOMException (DOMException.NOT_SUPPORTED_ERR, msg);
    }

    private static DOMException newFeatureNotFoundError(String name) {
        String msg =
            DOMMessageFormatter.formatMessage (
                    DOMMessageFormatter.DOM_DOMAIN,
                    "FEATURE_NOT_FOUND",
                    new Object[] { name });
        return new DOMException (DOMException.NOT_FOUND_ERR, msg);
    }

    private static DOMException newTypeMismatchError(String name) {
        String msg =
            DOMMessageFormatter.formatMessage (
                    DOMMessageFormatter.DOM_DOMAIN,
                    "TYPE_MISMATCH_ERR",
                    new Object[] { name });
        return new DOMException (DOMException.TYPE_MISMATCH_ERR, msg);
    }
}
