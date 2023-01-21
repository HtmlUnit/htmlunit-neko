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

package net.sourceforge.htmlunit.xerces.util;

import java.util.ArrayList;

import net.sourceforge.htmlunit.xerces.xni.Augmentations;
import net.sourceforge.htmlunit.xerces.xni.QName;
import net.sourceforge.htmlunit.xerces.xni.XMLAttributes;

/**
 * The XMLAttributesImpl class is an implementation of the XMLAttributes
 * interface which defines a collection of attributes for an element.
 * In the parser, the document source would scan the entire start element
 * and collect the attributes. The attributes are communicated to the
 * document handler in the startElement method.
 * <p>
 * The attributes are read-write so that subsequent stages in the document
 * pipeline can modify the values or change the attributes that are
 * propogated to the next stage.
 *
 * @see net.sourceforge.htmlunit.xerces.xni.XMLDocumentHandler#startElement
 *
 * @author Andy Clark, IBM
 * @author Elena Litani, IBM
 * @author Michael Glavassevich, IBM
 */
public class XMLAttributesImpl
    implements XMLAttributes {

    /** Attribute information. */
    private ArrayList<Attribute> fAttributes = new ArrayList<>();

    /** Default constructor. */
    public XMLAttributesImpl() {
        fAttributes = new ArrayList<>();
    }

    //
    // XMLAttributes methods
    //

    /**
     * Adds an attribute. The attribute's non-normalized value of the
     * attribute will have the same value as the attribute value until
     * set using the <code>setNonNormalizedValue</code> method. Also,
     * the added attribute will be marked as specified in the XML instance
     * document unless set otherwise using the <code>setSpecified</code>
     * method.
     * <p>
     * <strong>Note:</strong> If an attribute of the same name already
     * exists, the old values for the attribute are replaced by the new
     * values.
     *
     * @param name  The attribute name.
     * @param type  The attribute type. The type name is determined by
     *                  the type specified for this attribute in the DTD.
     *                  For example: "CDATA", "ID", "NMTOKEN", etc. However,
     *                  attributes of type enumeration will have the type
     *                  value specified as the pipe ('|') separated list of
     *                  the enumeration values prefixed by an open
     *                  parenthesis and suffixed by a close parenthesis.
     *                  For example: "(true|false)".
     * @param value The attribute value.
     *
     * @return Returns the attribute index.
     *
     * @see #setNonNormalizedValue
     * @see #setSpecified
     */
    @Override
    public int addAttribute(QName name, String type, String value) {
        // set values
        Attribute attribute = new Attribute();
        attribute.name.setValues(name);
        attribute.type = type;
        attribute.value = value;
        attribute.nonNormalizedValue = value;
        attribute.specified = false;

        // clear augmentations
        attribute.augs.clear();

        fAttributes.add(attribute);
        return fAttributes.size() - 1;

    } // addAttribute(QName,String,XMLString)

    /**
     * Removes all of the attributes. This method will also remove all
     * entities associated to the attributes.
     */
    @Override
    public void removeAllAttributes() {
        fAttributes.clear();
    } // removeAllAttributes()

    /**
     * Removes the attribute at the specified index.
     * <p>
     * <strong>Note:</strong> This operation changes the indexes of all
     * attributes following the attribute at the specified index.
     *
     * @param attrIndex The attribute index.
     */
    @Override
    public void removeAttributeAt(int attrIndex) {
        fAttributes.remove(attrIndex);
    } // removeAttributeAt(int)

    /**
     * Sets the name of the attribute at the specified index.
     *
     * @param attrIndex The attribute index.
     * @param attrName  The new attribute name.
     */
    @Override
    public void setName(int attrIndex, QName attrName) {
        fAttributes.get(attrIndex).name.setValues(attrName);
    } // setName(int,QName)

    /**
     * Sets the fields in the given QName structure with the values
     * of the attribute name at the specified index.
     *
     * @param attrIndex The attribute index.
     * @param attrName  The attribute name structure to fill in.
     */
    @Override
    public void getName(int attrIndex, QName attrName) {
        attrName.setValues(fAttributes.get(attrIndex).name);
    } // getName(int,QName)

    /**
     * Sets the type of the attribute at the specified index.
     *
     * @param attrIndex The attribute index.
     * @param attrType  The attribute type. The type name is determined by
     *                  the type specified for this attribute in the DTD.
     *                  For example: "CDATA", "ID", "NMTOKEN", etc. However,
     *                  attributes of type enumeration will have the type
     *                  value specified as the pipe ('|') separated list of
     *                  the enumeration values prefixed by an open
     *                  parenthesis and suffixed by a close parenthesis.
     *                  For example: "(true|false)".
     */
    @Override
    public void setType(int attrIndex, String attrType) {
        fAttributes.get(attrIndex).type = attrType;
    } // setType(int,String)

    /**
     * Sets the value of the attribute at the specified index. This
     * method will overwrite the non-normalized value of the attribute.
     *
     * @param attrIndex The attribute index.
     * @param attrValue The new attribute value.
     *
     * @see #setNonNormalizedValue
     */
    @Override
    public void setValue(int attrIndex, String attrValue) {
        Attribute attribute = fAttributes.get(attrIndex);
        attribute.value = attrValue;
        attribute.nonNormalizedValue = attrValue;
    } // setValue(int,String)

    /**
     * Sets the non-normalized value of the attribute at the specified
     * index.
     *
     * @param attrIndex The attribute index.
     * @param attrValue The new non-normalized attribute value.
     */
    @Override
    public void setNonNormalizedValue(int attrIndex, String attrValue) {
        if (attrValue == null) {
            attrValue = fAttributes.get(attrIndex).value;
        }
        fAttributes.get(attrIndex).nonNormalizedValue = attrValue;
    } // setNonNormalizedValue(int,String)

    /**
     * Returns the non-normalized value of the attribute at the specified
     * index. If no non-normalized value is set, this method will return
     * the same value as the <code>getValue(int)</code> method.
     *
     * @param attrIndex The attribute index.
     */
    @Override
    public String getNonNormalizedValue(int attrIndex) {
        return fAttributes.get(attrIndex).nonNormalizedValue;
    } // getNonNormalizedValue(int):String

    /**
     * Sets whether an attribute is specified in the instance document
     * or not.
     *
     * @param attrIndex The attribute index.
     * @param specified True if the attribute is specified in the instance
     *                  document.
     */
    @Override
    public void setSpecified(int attrIndex, boolean specified) {
        fAttributes.get(attrIndex).specified = specified;
    } // setSpecified(int,boolean)

    /**
     * Returns true if the attribute is specified in the instance document.
     *
     * @param attrIndex The attribute index.
     */
    @Override
    public boolean isSpecified(int attrIndex) {
        return fAttributes.get(attrIndex).specified;
    } // isSpecified(int):boolean

    //
    // AttributeList and Attributes methods
    //

    /**
     * Return the number of attributes in the list.
     *
     * <p>Once you know the number of attributes, you can iterate
     * through the list.</p>
     *
     * @return The number of attributes in the list.
     */
    @Override
    public int getLength() {
        return fAttributes.size();
    } // getLength():int

    /**
     * Look up an attribute's type by index.
     *
     * <p>The attribute type is one of the strings "CDATA", "ID",
     * "IDREF", "IDREFS", "NMTOKEN", "NMTOKENS", "ENTITY", "ENTITIES",
     * or "NOTATION" (always in upper case).</p>
     *
     * <p>If the parser has not read a declaration for the attribute,
     * or if the parser does not report attribute types, then it must
     * return the value "CDATA" as stated in the XML 1.0 Recommentation
     * (clause 3.3.3, "Attribute-Value Normalization").</p>
     *
     * <p>For an enumerated attribute that is not a notation, the
     * parser will report the type as "NMTOKEN".</p>
     *
     * @param index The attribute index (zero-based).
     * @return The attribute's type as a string, or null if the
     *         index is out of range.
     * @see #getLength
     */
    @Override
    public String getType(int index) {
        if (index < 0 || index >= getLength()) {
            return null;
        }
        return getReportableType(fAttributes.get(index).type);
    } // getType(int):String

    /**
     * Look up an attribute's type by XML 1.0 qualified name.
     *
     * <p>See {@link #getType(int) getType(int)} for a description
     * of the possible types.</p>
     *
     * @param qname The XML 1.0 qualified name.
     * @return The attribute type as a string, or null if the
     *         attribute is not in the list or if qualified names
     *         are not available.
     */
    @Override
    public String getType(String qname) {
        int index = getIndex(qname);
        return index != -1 ? getReportableType(fAttributes.get(index).type) : null;
    } // getType(String):String

    /**
     * Look up an attribute's value by index.
     *
     * <p>If the attribute value is a list of tokens (IDREFS,
     * ENTITIES, or NMTOKENS), the tokens will be concatenated
     * into a single string with each token separated by a
     * single space.</p>
     *
     * @param index The attribute index (zero-based).
     * @return The attribute's value as a string, or null if the
     *         index is out of range.
     * @see #getLength
     */
    @Override
    public String getValue(int index) {
        if (index < 0 || index >= getLength()) {
            return null;
        }
        return fAttributes.get(index).value;
    } // getValue(int):String

    /**
     * Look up an attribute's value by XML 1.0 qualified name.
     *
     * <p>See {@link #getValue(int) getValue(int)} for a description
     * of the possible values.</p>
     *
     * @param qname The XML 1.0 qualified name.
     * @return The attribute value as a string, or null if the
     *         attribute is not in the list or if qualified names
     *         are not available.
     */
    @Override
    public String getValue(String qname) {
        int index = getIndex(qname);
        return index != -1 ? fAttributes.get(index).value : null;
    } // getValue(String):String

    //
    // AttributeList methods
    //

    /**
     * Return the name of an attribute in this list (by position).
     *
     * <p>The names must be unique: the SAX parser shall not include the
     * same attribute twice.  Attributes without values (those declared
     * #IMPLIED without a value specified in the start tag) will be
     * omitted from the list.</p>
     *
     * <p>If the attribute name has a namespace prefix, the prefix
     * will still be attached.</p>
     *
     * @param index The index of the attribute in the list (starting at 0).
     * @return The name of the indexed attribute, or null
     *         if the index is out of range.
     * @see #getLength
     */
    public String getName(int index) {
        if (index < 0 || index >= getLength()) {
            return null;
        }
        return fAttributes.get(index).name.rawname;
    } // getName(int):String

    //
    // Attributes methods
    //

    /**
     * Look up the index of an attribute by XML 1.0 qualified name.
     *
     * @param qName The qualified (prefixed) name.
     * @return The index of the attribute, or -1 if it does not
     *         appear in the list.
     */
    @Override
    public int getIndex(String qName) {
        for (int i = 0; i < getLength(); i++) {
            Attribute attribute = fAttributes.get(i);
            if (attribute.name.rawname != null &&
                attribute.name.rawname.equals(qName)) {
                return i;
            }
        }
        return -1;
    } // getIndex(String):int

    /**
     * Look up the index of an attribute by Namespace name.
     *
     * @param uri The Namespace URI, or null if
     *        the name has no Namespace URI.
     * @param localPart The attribute's local name.
     * @return The index of the attribute, or -1 if it does not
     *         appear in the list.
     */
    @Override
    public int getIndex(String uri, String localPart) {
        for (int i = 0; i < getLength(); i++) {
            Attribute attribute = fAttributes.get(i);
            if (attribute.name.localpart != null &&
                attribute.name.localpart.equals(localPart) &&
                ((uri==attribute.name.uri) ||
                (uri!=null && attribute.name.uri!=null && attribute.name.uri.equals(uri))))
            {
                return i;
            }
        }
        return -1;
    } // getIndex(String,String):int

    /**
     * Look up an attribute's local name by index.
     *
     * @param index The attribute index (zero-based).
     * @return The local name, or the empty string if Namespace
     *         processing is not being performed, or null
     *         if the index is out of range.
     * @see #getLength
     */
    @Override
    public String getLocalName(int index) {
        if (index < 0 || index >= getLength()) {
            return null;
        }
        return fAttributes.get(index).name.localpart;
    } // getLocalName(int):String

    /**
     * Look up an attribute's XML 1.0 qualified name by index.
     *
     * @param index The attribute index (zero-based).
     * @return The XML 1.0 qualified name, or the empty string
     *         if none is available, or null if the index
     *         is out of range.
     * @see #getLength
     */
    @Override
    public String getQName(int index) {
        if (index < 0 || index >= getLength()) {
            return null;
        }
        String rawname = fAttributes.get(index).name.rawname;
        return rawname != null ? rawname : "";
    } // getQName(int):String

    /**
     * Look up an attribute's type by Namespace name.
     *
     * <p>See {@link #getType(int) getType(int)} for a description
     * of the possible types.</p>
     *
     * @param uri The Namespace URI, or null if the
     *        name has no Namespace URI.
     * @param localName The local name of the attribute.
     * @return The attribute type as a string, or null if the
     *         attribute is not in the list or if Namespace
     *         processing is not being performed.
     */
    @Override
    public String getType(String uri, String localName) {
        int index = getIndex(uri, localName);
        return index != -1 ? getReportableType(fAttributes.get(index).type) : null;
    } // getType(String,String):String

    /**
     * Look up an attribute's Namespace URI by index.
     *
     * @param index The attribute index (zero-based).
     * @return The Namespace URI
     * @see #getLength
     */
    @Override
    public String getURI(int index) {
        if (index < 0 || index >= getLength()) {
            return null;
        }
        return fAttributes.get(index).name.uri;
    } // getURI(int):String

    /**
     * Look up an attribute's value by Namespace name.
     *
     * <p>See {@link #getValue(int) getValue(int)} for a description
     * of the possible values.</p>
     *
     * @param uri The Namespace URI, or null if the
     * @param localName The local name of the attribute.
     * @return The attribute value as a string, or null if the
     *         attribute is not in the list.
     */
    @Override
    public String getValue(String uri, String localName) {
        int index = getIndex(uri, localName);
        return index != -1 ? getValue(index) : null;
    } // getValue(String,String):String

    /**
     * Look up an augmentations by attributes index.
     *
     * @param attributeIndex The attribute index.
     * @return Augmentations
     */
    @Override
    public Augmentations getAugmentations (int attributeIndex){
        if (attributeIndex < 0 || attributeIndex >= getLength()) {
            return null;
        }
        return fAttributes.get(attributeIndex).augs;
    }

    // Implementation methods

    /**
     * Adds an attribute. The attribute's non-normalized value of the
     * attribute will have the same value as the attribute value until
     * set using the <code>setNonNormalizedValue</code> method. Also,
     * the added attribute will be marked as specified in the XML instance
     * document unless set otherwise using the <code>setSpecified</code>
     * method.
     * <p>
     * This method differs from <code>addAttribute</code> in that it
     * does not check if an attribute of the same name already exists
     * in the list before adding it. In order to improve performance
     * of namespace processing, this method allows uniqueness checks
     * to be deferred until all the namespace information is available
     * after the entire attribute specification has been read.
     * <p>
     * <strong>Caution:</strong> If this method is called it should
     * not be mixed with calls to <code>addAttribute</code> unless
     * it has been determined that all the attribute names are unique.
     *
     * @param name the attribute name
     * @param type the attribute type
     * @param value the attribute value
     *
     * @see #setNonNormalizedValue
     * @see #setSpecified
     * @see #checkDuplicatesNS
     */
    public void addAttributeNS(QName name, String type, String value) {
        // set values
        Attribute attribute = new Attribute();
        attribute.name.setValues(name);
        attribute.type = type;
        attribute.value = value;
        attribute.nonNormalizedValue = value;
        attribute.specified = false;

        // clear augmentations
        attribute.augs.clear();

        fAttributes.add(attribute);
    }

    /**
     * Returns the value passed in or NMTOKEN if it's an enumerated type.
     *
     * @param type attribute type
     * @return the value passed in or NMTOKEN if it's an enumerated type.
     */
    private String getReportableType(String type) {

        if (type.charAt(0) == '(') {
            return "NMTOKEN";
        }
        return type;
    }

    //
    // Classes
    //

    /**
     * Attribute information.
     *
     * @author Andy Clark, IBM
     */
    static final class Attribute {
        /** Name. */
        public final QName name = new QName();

        /** Type. */
        public String type;

        /** Value. */
        public String value;

        /** Non-normalized value. */
        public String nonNormalizedValue;

        /** Specified. */
        public boolean specified;

        /**
         * Augmentations information for this attribute.
         * XMLAttributes has no knowledge if any augmentations
         * were attached to Augmentations.
         */
        public Augmentations augs = new AugmentationsImpl();
    } // class Attribute

} // class XMLAttributesImpl
