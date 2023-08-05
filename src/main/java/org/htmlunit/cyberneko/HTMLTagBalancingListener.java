/*
 * Copyright 2002-2009 Andy Clark, Marc Guillemot
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

package org.htmlunit.cyberneko;

import org.htmlunit.cyberneko.xerces.xni.Augmentations;
import org.htmlunit.cyberneko.xerces.xni.QName;
import org.htmlunit.cyberneko.xerces.xni.XMLAttributes;
import org.htmlunit.cyberneko.xerces.xni.XMLDocumentHandler;

/**
 * <span style="color: red">EXPERIMENTAL: may change in next release</span><br>
 * {@link XMLDocumentHandler} implementing this interface will get notified of elements discarded
 * by the tag balancer when they:
 * <ul>
 * <li>are configured using {@link HTMLConfiguration}
 * <li>activate the tag balancing feature
 * </ul>
 * @author Marc Guillemot
 */
public interface HTMLTagBalancingListener
{
    // Notifies that the start element has been ignored.
    void ignoredStartElement(QName elem, XMLAttributes attrs, Augmentations augs);

    // Notifies that the end element has been ignored.
    void ignoredEndElement(QName element, Augmentations augs);
}
