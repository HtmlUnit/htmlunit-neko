/*
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
package org.htmlunit.cyberneko.xerces.xni;

import org.xml.sax.ext.Attributes2;

/**
 * The XMLAttributes interface defines a collection of attributes for an
 * element. In the parser, the document source would scan the entire start
 * element and collect the attributes. The attributes are communicated to the
 * document handler in the startElement method.
 * <p>
 * The attributes are read-write so that subsequent stages in the document
 * pipeline can modify the values or change the attributes that are propagated
 * to the next stage.
 *
 * @see XMLDocumentHandler#startElement
 *
 * @author Andy Clark, IBM
 */
public interface XMLAttributes extends Attributes2 {

    /**
     * Adds an attribute. The attribute's non-normalized value of the attribute will
     * have the same value as the attribute value until. Also, the added attribute
     * will be marked as specified in the XML instance document unless set otherwise
     * using the <code>setSpecified</code> method.
     * <p>
     * <strong>Note:</strong> If an attribute of the same name already exists, the
     * old values for the attribute are replaced by the new values.
     *
     * @param attrName  The attribute name.
     * @param attrType  The attribute type. The type name is determined by the type
     *                  specified for this attribute in the DTD. For example:
     *                  "CDATA", "ID", "NMTOKEN", etc. However, attributes of type
     *                  enumeration will have the type value specified as the pipe
     *                  ('|') separated list of the enumeration values prefixed by
     *                  an open parenthesis and suffixed by a close parenthesis. For
     *                  example: "(true|false)".
     * @param attrValue The attribute value.
     *
     * @return Returns the attribute index.
     *
     * @see #setSpecified(int, boolean)
     */
    int addAttribute(QName attrName, String attrType, String attrValue);

    /**
     * Removes all of the attributes. This method will also remove all entities
     * associated to the attributes.
     */
    void removeAllAttributes();

    /**
     * Removes the attribute at the specified index.
     * <p>
     * <strong>Note:</strong> This operation changes the indexes of all attributes
     * following the attribute at the specified index.
     *
     * @param attrIndex The attribute index.
     */
    void removeAttributeAt(int attrIndex);

    /**
     * Sets the name of the attribute at the specified index.
     *
     * @param attrIndex The attribute index.
     * @param attrName  The new attribute name.
     */
    void setName(int attrIndex, QName attrName);

    /**
     * Gets the fields in the given QName structure with the values of the attribute
     * name at the specified index.
     *
     * @param attrIndex The attribute index.
     * @param attrName  The attribute name structure to fill in.
     */
    void getName(int attrIndex, QName attrName);

    /**
     * Returns the QName structure of the name. Because QName is a modifiable
     * data structure, make sure you know what you do when you take this
     * shortcut route.
     *
     * @param attrIndex The attribute index.
     */
    QName getName(int attrIndex);

    /**
     * Sets the value of the attribute at the specified index. This method will
     * overwrite the non-normalized value of the attribute.
     *
     * @param attrIndex The attribute index.
     * @param attrValue The new attribute value.
     */
    void setValue(int attrIndex, String attrValue);

    /**
     * @return the non-normalized value of the attribute at the specified index. If
     *         no non-normalized value is set, this method will return the same
     *         value as the <code>getValue(int)</code> method.
     *
     * @param attrIndex The attribute index.
     */
    String getNonNormalizedValue(int attrIndex);

    /**
     * Sets whether an attribute is specified in the instance document or not.
     *
     * @param attrIndex The attribute index.
     * @param specified True if the attribute is specified in the instance document.
     */
    void setSpecified(int attrIndex, boolean specified);
}
