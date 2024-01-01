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
package org.htmlunit.cyberneko.xerces.xni.parser;

/**
 * The component manager manages a parser configuration and the components that
 * make up that configuration. The manager notifies each component before
 * parsing to allow the components to initialize their state; and also any time
 * that a parser feature or property changes.
 * <p>
 * The methods of the component manager allow components to query features and
 * properties that affect the operation of the component.
 *
 * @see XMLComponent
 *
 * @author Andy Clark, IBM
 */
public interface XMLComponentManager {

    /**
     * @param featureId The feature identifier.
     * @return the state of a feature.
     *
     * @throws XMLConfigurationException Thrown on configuration error.
     */
    boolean getFeature(String featureId) throws XMLConfigurationException;

    /**
     * @param propertyId The property identifier.
     * @return the value of a property.
     *
     * @throws XMLConfigurationException Thrown on configuration error.
     */
    Object getProperty(String propertyId) throws XMLConfigurationException;

}
