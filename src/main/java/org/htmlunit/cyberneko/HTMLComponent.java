/*
 * Copyright 2004-2008 Andy Clark, Marc Guillemot
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.htmlunit.cyberneko;

import org.htmlunit.xerces.xni.parser.XMLComponent;

/**
 * This interface extends the XNI <code>XMLComponent</code> interface
 * to add methods that allow the preferred default values for features
 * and properties to be queried.
 *
 * @author Andy Clark
 */
public interface HTMLComponent
    extends XMLComponent {

    //
    // HTMLComponent methods
    //

    /**
     * Returns the default state for a feature, or null if this
     * component does not want to report a default value for this
     * feature.
     */
    @Override
    Boolean getFeatureDefault(String featureId);

    /**
     * Returns the default state for a property, or null if this
     * component does not want to report a default value for this
     * property.
     */
    @Override
    Object getPropertyDefault(String propertyId);
}
