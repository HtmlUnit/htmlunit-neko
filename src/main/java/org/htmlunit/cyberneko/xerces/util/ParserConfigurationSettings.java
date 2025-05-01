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

import java.util.ArrayList;
import java.util.HashMap;

import org.htmlunit.cyberneko.xerces.xni.parser.XMLComponentManager;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLConfigurationException;

/**
 * This class implements the basic operations for managing parser configuration
 * features and properties. This utility class can be used as a base class for
 * parser configurations or separately to encapsulate a number of parser
 * settings as a component manager.
 * <p>
 * This class can be constructed with a "parent" settings object (in the form of
 * an <code>XMLComponentManager</code>) that allows parser configuration
 * settings to be "chained" together.
 *
 * @author Andy Clark, IBM
 */
public class ParserConfigurationSettings implements XMLComponentManager {

    /** Recognized properties. */
    private final ArrayList<String> fRecognizedProperties_;

    /** Properties. */
    private final HashMap<String, Object> fProperties_;

    /** Recognized features. */
    private final ArrayList<String> fRecognizedFeatures_;

    /** Features. */
    private final HashMap<String, Boolean> fFeatures_;

    // Constructs a parser configuration settings object with a
    // parent settings object.
    public ParserConfigurationSettings() {
        // create storage for recognized features and properties
        fRecognizedFeatures_ = new ArrayList<>();
        fRecognizedProperties_ = new ArrayList<>();

        // create table for features and properties
        fFeatures_ = new HashMap<>();
        fProperties_ = new HashMap<>();
    }

    /**
     * Allows a parser to add parser specific features to be recognized and managed
     * by the parser configuration.
     *
     * @param featureIds An array of the additional feature identifiers to be
     *                   recognized.
     */
    public void addRecognizedFeatures(final String[] featureIds) {
        // add recognized features
        final int featureIdsCount = featureIds != null ? featureIds.length : 0;
        for (int i = 0; i < featureIdsCount; i++) {
            final String featureId = featureIds[i];
            if (!fRecognizedFeatures_.contains(featureId)) {
                fRecognizedFeatures_.add(featureId);
            }
        }
    }

    /**
     * Set the state of a feature.
     * <p>
     * Set the state of any feature in a SAX2 parser. The parser might not recognize
     * the feature, and if it does recognize it, it might not be able to fulfill the
     * request.
     *
     * @param featureId The unique identifier (URI) of the feature.
     * @param state     The requested state of the feature (true or false).
     *
     * @exception org.htmlunit.cyberneko.xerces.xni.parser.XMLConfigurationException If
     *            the requested feature is not known.
     */
    public void setFeature(final String featureId, final boolean state) throws XMLConfigurationException {
        // check and store
        checkFeature(featureId);

        fFeatures_.put(featureId, state ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Allows a parser to add parser specific properties to be recognized and
     * managed by the parser configuration.
     *
     * @param propertyIds An array of the additional property identifiers to be
     *                    recognized.
     */
    public void addRecognizedProperties(final String[] propertyIds) {
        // add recognizedProperties
        final int propertyIdsCount = propertyIds != null ? propertyIds.length : 0;
        for (int i = 0; i < propertyIdsCount; i++) {
            final String propertyId = propertyIds[i];
            if (!fRecognizedProperties_.contains(propertyId)) {
                fRecognizedProperties_.add(propertyId);
            }
        }
    }

    /**
     * setProperty.
     *
     * @param propertyId the property id
     * @param value      the value
     * @exception org.htmlunit.cyberneko.xerces.xni.parser.XMLConfigurationException If
     *            the requested feature is not known.
     */
    public void setProperty(final String propertyId, final Object value) throws XMLConfigurationException {
        // check and store
        checkProperty(propertyId);
        fProperties_.put(propertyId, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getFeature(final String featureId) throws XMLConfigurationException {
        final Boolean state = fFeatures_.get(featureId);

        if (state == null) {
            checkFeature(featureId);
            return false;
        }
        return state.booleanValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getProperty(final String propertyId) throws XMLConfigurationException {
        final Object propertyValue = fProperties_.get(propertyId);

        if (propertyValue == null) {
            checkProperty(propertyId);
        }

        return propertyValue;
    }

    /**
     * Check a feature. If feature is known and supported, this method simply
     * returns. Otherwise, the appropriate exception is thrown.
     *
     * @param featureId The unique identifier (URI) of the feature.
     *
     * @exception org.htmlunit.cyberneko.xerces.xni.parser.XMLConfigurationException If
     *            the requested feature is not known.
     */
    protected void checkFeature(final String featureId) throws XMLConfigurationException {
        // check feature
        if (!fRecognizedFeatures_.contains(featureId)) {
            final short type = XMLConfigurationException.NOT_RECOGNIZED;
            throw new XMLConfigurationException(type, featureId);
        }
    }

    /**
     * Check a property. If the property is known and supported, this method simply
     * returns. Otherwise, the appropriate exception is thrown.
     *
     * @param propertyId The unique identifier (URI) of the property being set.
     * @exception org.htmlunit.cyberneko.xerces.xni.parser.XMLConfigurationException If
     *            the requested feature is not known.
     */
    protected void checkProperty(final String propertyId) throws XMLConfigurationException {
        // check property
        if (!fRecognizedProperties_.contains(propertyId)) {
            final short type = XMLConfigurationException.NOT_RECOGNIZED;
            throw new XMLConfigurationException(type, propertyId);
        }
    }
}
