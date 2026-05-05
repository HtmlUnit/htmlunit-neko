/*
 * Copyright (c) 2002-2009 Andy Clark, Marc Guillemot
 * Copyright (c) 2017-2026 Ronald Brill
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

import org.htmlunit.cyberneko.xerces.xni.XNIException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLConfigurationException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLParserConfiguration;

/**
 * This interface extends the XNI <code>XMLComponent</code> interface
 * to add methods that allow the preferred default values for features
 * and properties to be queried.
 *
 * @author Andy Clark
 */
public interface HTMLComponent {
    /**
     * Resets the component. The component can query the component manager about any
     * features and properties that affect the operation of the component.
     *
     * @param xmlParserConfiguration The {@link XMLParserConfiguration}.
     *
     * @throws XNIException Thrown by component on initialization error.
     */
    void reset(XMLParserConfiguration xmlParserConfiguration) throws XMLConfigurationException;

    /**
     * @return an array of feature identifiers that are recognized by this
     *         component. This method may return null if no features are recognized
     *         by this component.
     */
    String[] getRecognizedFeatures();

    /**
     * Sets the state of a feature. This method is called by the component manager
     * any time after reset when a feature changes state.
     * <p>
     * <strong>Note:</strong> Components should silently ignore features that do not
     * affect the operation of the component.
     *
     * @param featureId The feature identifier.
     * @param state     The state of the feature.
     *
     * @throws XMLConfigurationException Thrown for configuration error. In general,
     *                                   components should only throw this exception
     *                                   if it is <strong>really</strong> a critical
     *                                   error.
     */
    void setFeature(String featureId, boolean state) throws XMLConfigurationException;

    /**
     * @return an array of property identifiers that are recognized by this
     *         component. This method may return null if no properties are
     *         recognized by this component.
     */
    String[] getRecognizedProperties();

    /**
     * Sets the value of a property. This method is called by the component manager
     * any time after reset when a property changes value.
     * <p>
     * <strong>Note:</strong> Components should silently ignore properties that do
     * not affect the operation of the component.
     *
     * @param propertyId The property identifier.
     * @param value      The value of the property.
     *
     * @throws XMLConfigurationException Thrown for configuration error. In general,
     *                                   components should only throw this exception
     *                                   if it is <strong>really</strong> a critical
     *                                   error.
     */
    void setProperty(String propertyId, Object value) throws XMLConfigurationException;

    /**
     * Returns the default state for a feature, or null if this
     * component does not want to report a default value for this
     * feature.
     *
     * @param featureId The feature identifier.
     * @return the default state for a feature, or null if this component does not
     *         want to report a default value for this feature.
     */
    Boolean getFeatureDefault(String featureId);

    /**
     * Returns the default state for a property, or null if this
     * component does not want to report a default value for this
     * property.
     *
     * @param propertyId The property identifier.
     * @return the default state for a property, or null if this component does not
     *         want to report a default value for this property
     */
    Object getPropertyDefault(String propertyId);
}
