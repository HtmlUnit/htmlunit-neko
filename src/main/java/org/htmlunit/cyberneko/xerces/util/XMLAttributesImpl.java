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

import org.htmlunit.cyberneko.xerces.xni.QName;
import org.htmlunit.cyberneko.xerces.xni.XMLAttributes;

/**
 * The XMLAttributesImpl class is an implementation of the XMLAttributes
 * interface which defines a collection of attributes for an element. In the
 * parser, the document source would scan the entire start element and collect
 * the attributes. The attributes are communicated to the document handler in
 * the startElement method.
 * <p>
 * The attributes are read-write so that subsequent stages in the document
 * pipeline can modify the values or change the attributes that are propogated
 * to the next stage.
 *
 * @see org.htmlunit.cyberneko.xerces.xni.XMLDocumentHandler#startElement
 *
 * @author Andy Clark, IBM
 * @author Elena Litani, IBM
 * @author Michael Glavassevich, IBM
 */
public class XMLAttributesImpl implements XMLAttributes {

    /** Attribute information. */
    private final ArrayList<Attribute> attributes_;

    /** Default constructor. */
    public XMLAttributesImpl() {
        attributes_ = new ArrayList<>();
    }

    /**
     * Adds an attribute. The attribute's non-normalized value of the attribute will
     * have the same value as the attribute value. Also, the added attribute will be
     * marked as specified in the XML instance document unless set otherwise using
     * the <code>setSpecified</code> method.
     * <p>
     * <strong>Note:</strong> If an attribute of the same name already exists, the
     * old values for the attribute are replaced by the new values.
     *
     * @param name  The attribute name.
     * @param type  The attribute type. The type name is determined by the type
     *              specified for this attribute in the DTD. For example: "CDATA",
     *              "ID", "NMTOKEN", etc. However, attributes of type enumeration
     *              will have the type value specified as the pipe ('|') separated
     *              list of the enumeration values prefixed by an open parenthesis
     *              and suffixed by a close parenthesis. For example:
     *              "(true|false)".
     * @param value The attribute value.
     *
     * @return Returns the attribute index.
     * @see #setSpecified
     */
    @Override
    public int addAttribute(final QName name, final String type, final String value) {
        addAttribute(name, type, value, false);
        return attributes_.size() - 1;
    }

    /**
     * Adds an attribute. The attribute's non-normalized value of the attribute will
     * have the same value as the attribute value. Also, the added attribute will be
     * marked as specified in the XML instance document unless set otherwise using
     * the <code>setSpecified</code> method.
     * <p>
     * This method differs from <code>addAttribute</code> in that it does not check
     * if an attribute of the same name already exists in the list before adding it.
     * In order to improve performance of namespace processing, this method allows
     * uniqueness checks to be deferred until all the namespace information is
     * available after the entire attribute specification has been read.
     * <p>
     * <strong>Caution:</strong> If this method is called it should not be mixed
     * with calls to <code>addAttribute</code> unless it has been determined that
     * all the attribute names are unique.
     *
     * @param name  the attribute name
     * @param type  the attribute type
     * @param value the attribute value
     * @param specified the specified attribute value
     */
    public void addAttribute(final QName name, final String type, final String value, final boolean specified) {
        // set values
        final Attribute attribute = new Attribute();
        attribute.name_.setValues(name);
        attribute.type_ = type;
        attribute.value_ = value;
        attribute.specified_ = specified;

        attributes_.add(attribute);
    }

    public void addAttribute(final QName name, final String type, final String value, final String nonNormalizedValue, final boolean specified) {
        final AttributeExt attribute = new AttributeExt();
        attribute.name_.setValues(name);
        attribute.type_ = type;
        attribute.value_ = value;
        attribute.nonNormalizedValue_ = nonNormalizedValue;
        attribute.specified_ = specified;

        attributes_.add(attribute);
    }

    /**
     * Removes all of the attributes. This method will also remove all entities
     * associated to the attributes.
     */
    @Override
    public void removeAllAttributes() {
        attributes_.clear();
    }

    /**
     * Removes the attribute at the specified index.
     * <p>
     * <strong>Note:</strong> This operation changes the indexes of all attributes
     * following the attribute at the specified index.
     *
     * @param attrIndex The attribute index.
     */
    @Override
    public void removeAttributeAt(final int attrIndex) {
        attributes_.remove(attrIndex);
    }

    /**
     * Sets the name of the attribute at the specified index.
     *
     * @param attrIndex The attribute index.
     * @param attrName  The new attribute name.
     */
    @Override
    public void setName(final int attrIndex, final QName attrName) {
        attributes_.get(attrIndex).name_.setValues(attrName);
    }

    /**
     * Sets the fields in the given QName structure with the values of the attribute
     * name at the specified index.
     *
     * @param attrIndex The attribute index.
     * @param attrName  The attribute name structure to fill in.
     */
    @Override
    public void getName(final int attrIndex, final QName attrName) {
        attrName.setValues(attributes_.get(attrIndex).name_);
    }

    /**
     * Sets the type of the attribute at the specified index.
     *
     * @param attrIndex The attribute index.
     * @param attrType  The attribute type. The type name is determined by the type
     *                  specified for this attribute in the DTD. For example:
     *                  "CDATA", "ID", "NMTOKEN", etc. However, attributes of type
     *                  enumeration will have the type value specified as the pipe
     *                  ('|') separated list of the enumeration values prefixed by
     *                  an open parenthesis and suffixed by a close parenthesis. For
     *                  example: "(true|false)".
     */
    @Override
    public void setType(final int attrIndex, final String attrType) {
        attributes_.get(attrIndex).type_ = attrType;
    }

    /**
     * Sets the value of the attribute at the specified index. This method will
     * overwrite the non-normalized value of the attribute.
     *
     * @param attrIndex The attribute index.
     * @param attrValue The new attribute value.
     */
    @Override
    public void setValue(final int attrIndex, final String attrValue) {
        final Attribute attribute = attributes_.get(attrIndex);
        attribute.value_ = attrValue;
    }

    /**
     * Sets whether an attribute is specified in the instance document or not.
     *
     * @param attrIndex The attribute index.
     * @param specified True if the attribute is specified in the instance document.
     */
    @Override
    public void setSpecified(final int attrIndex, final boolean specified) {
        attributes_.get(attrIndex).specified_ = specified;
    }

    /**
     * Returns true if the attribute is specified in the instance document.
     *
     * @param attrIndex The attribute index.
     */
    @Override
    public boolean isSpecified(final int attrIndex) {
        return attributes_.get(attrIndex).specified_;
    }

    /**
     * Return the number of attributes in the list.
     *
     * <p>
     * Once you know the number of attributes, you can iterate through the list.
     * </p>
     *
     * @return The number of attributes in the list.
     */
    @Override
    public int getLength() {
        return attributes_.size();
    }

    /**
     * Look up an attribute's type by index.
     *
     * <p>
     * The attribute type is one of the strings "CDATA", "ID", "IDREF", "IDREFS",
     * "NMTOKEN", "NMTOKENS", "ENTITY", "ENTITIES", or "NOTATION" (always in upper
     * case).
     * </p>
     *
     * <p>
     * If the parser has not read a declaration for the attribute, or if the parser
     * does not report attribute types, then it must return the value "CDATA" as
     * stated in the XML 1.0 Recommentation (clause 3.3.3, "Attribute-Value
     * Normalization").
     * </p>
     *
     * <p>
     * For an enumerated attribute that is not a notation, the parser will report
     * the type as "NMTOKEN".
     * </p>
     *
     * @param index The attribute index (zero-based).
     * @return The attribute's type as a string, or null if the index is out of
     *         range.
     * @see #getLength
     */
    @Override
    public String getType(final int index) {
        if (index < 0 || index >= getLength()) {
            return null;
        }
        return getReportableType(attributes_.get(index).type_);
    }

    /**
     * Look up an attribute's type by XML 1.0 qualified name.
     *
     * <p>
     * See {@link #getType(int) getType(int)} for a description of the possible
     * types.
     * </p>
     *
     * @param qname The XML 1.0 qualified name.
     * @return The attribute type as a string, or null if the attribute is not in
     *         the list or if qualified names are not available.
     */
    @Override
    public String getType(final String qname) {
        final int index = getIndex(qname);
        return index != -1 ? getReportableType(attributes_.get(index).type_) : null;
    }

    /**
     * Look up an attribute's value by index.
     *
     * <p>
     * If the attribute value is a list of tokens (IDREFS, ENTITIES, or NMTOKENS),
     * the tokens will be concatenated into a single string with each token
     * separated by a single space.
     * </p>
     *
     * @param index The attribute index (zero-based).
     * @return The attribute's value as a string, or null if the index is out of
     *         range.
     * @see #getLength
     */
    @Override
    public String getValue(final int index) {
        if (index < 0 || index >= getLength()) {
            return null;
        }
        return attributes_.get(index).value_;
    }

    /**
     * Look up an attribute's value by XML 1.0 qualified name.
     *
     * <p>
     * See {@link #getValue(int) getValue(int)} for a description of the possible
     * values.
     * </p>
     *
     * @param qname The XML 1.0 qualified name.
     * @return The attribute value as a string, or null if the attribute is not in
     *         the list or if qualified names are not available.
     */
    @Override
    public String getValue(final String qname) {
        final int index = getIndex(qname);
        return index != -1 ? attributes_.get(index).value_ : null;
    }

    /**
     * Return the name of an attribute in this list (by position).
     *
     * <p>
     * The names must be unique: the SAX parser shall not include the same attribute
     * twice. Attributes without values (those declared #IMPLIED without a value
     * specified in the start tag) will be omitted from the list.
     * </p>
     *
     * <p>
     * If the attribute name has a namespace prefix, the prefix will still be
     * attached.
     * </p>
     *
     * @param index The index of the attribute in the list (starting at 0).
     * @return The name of the indexed attribute, or null if the index is out of
     *         range.
     * @see #getLength
     */
    public String getNameRawName(final int index) {
        if (index < 0 || index >= getLength()) {
            return null;
        }
        return attributes_.get(index).name_.getRawname();
    }

    /**
     * Returns the full QName of the name of this attribute.
     */
    @Override
    public QName getName(final int index) {
        return attributes_.get(index).name_;
    }

    /**
     * Look up the index of an attribute by XML 1.0 qualified name.
     *
     * @param qName The qualified (prefixed) name.
     * @return The index of the attribute, or -1 if it does not appear in the list.
     */
    @Override
    public int getIndex(final String qName) {
        for (int i = 0; i < getLength(); i++) {
            final Attribute attribute = attributes_.get(i);
            if (attribute.name_.getRawname() != null && attribute.name_.getRawname().equals(qName)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Look up the index of an attribute by Namespace name.
     *
     * @param uri       The Namespace URI, or null if the name has no Namespace URI.
     * @param localPart The attribute's local name.
     * @return The index of the attribute, or -1 if it does not appear in the list.
     */
    @Override
    public int getIndex(final String uri, final String localPart) {
        for (int i = 0; i < getLength(); i++) {
            final Attribute attribute = attributes_.get(i);
            if (attribute.name_.getLocalpart() != null && attribute.name_.getLocalpart().equals(localPart)
                    && ((uri == attribute.name_.getUri())
                            || (uri != null && attribute.name_.getUri() != null && attribute.name_.getUri().equals(uri)))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Look up an attribute's local name by index.
     *
     * @param index The attribute index (zero-based).
     * @return The local name, or the empty string if Namespace processing is not
     *         being performed, or null if the index is out of range.
     * @see #getLength
     */
    @Override
    public String getLocalName(final int index) {
        if (index < 0 || index >= getLength()) {
            return null;
        }
        return attributes_.get(index).name_.getLocalpart();
    }

    /**
     * Look up an attribute's XML 1.0 qualified name by index.
     *
     * @param index The attribute index (zero-based).
     * @return The XML 1.0 qualified name, or the empty string if none is available,
     *         or null if the index is out of range.
     * @see #getLength
     */
    @Override
    public String getQName(final int index) {
        if (index < 0 || index >= getLength()) {
            return null;
        }
        final String rawname = attributes_.get(index).name_.getRawname();
        return rawname != null ? rawname : "";
    }

    /**
     * Look up an attribute's type by Namespace name.
     *
     * <p>
     * See {@link #getType(int) getType(int)} for a description of the possible
     * types.
     * </p>
     *
     * @param uri       The Namespace URI, or null if the name has no Namespace URI.
     * @param localName The local name of the attribute.
     * @return The attribute type as a string, or null if the attribute is not in
     *         the list or if Namespace processing is not being performed.
     */
    @Override
    public String getType(final String uri, final String localName) {
        final int index = getIndex(uri, localName);
        return index != -1 ? getReportableType(attributes_.get(index).type_) : null;
    }

    /**
     * Look up an attribute's Namespace URI by index.
     *
     * @param index The attribute index (zero-based).
     * @return The Namespace URI
     * @see #getLength
     */
    @Override
    public String getURI(final int index) {
        if (index < 0 || index >= getLength()) {
            return null;
        }
        return attributes_.get(index).name_.getUri();
    }

    /**
     * Look up an attribute's value by Namespace name.
     *
     * <p>
     * See {@link #getValue(int) getValue(int)} for a description of the possible
     * values.
     * </p>
     *
     * @param uri       The Namespace URI, or null if the
     * @param localName The local name of the attribute.
     * @return The attribute value as a string, or null if the attribute is not in
     *         the list.
     */
    @Override
    public String getValue(final String uri, final String localName) {
        final int index = getIndex(uri, localName);
        return index != -1 ? getValue(index) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNonNormalizedValue(int index) {
        if (index < 0 || index >= getLength()) {
            return null;
        }
        return attributes_.get(index).getNonNormalizedValue();
    }

    /**
     * Returns the value passed in or NMTOKEN if it's an enumerated type.
     *
     * @param type attribute type
     * @return the value passed in or NMTOKEN if it's an enumerated type.
     */
    private static String getReportableType(final String type) {

        if (type.charAt(0) == '(') {
            return "NMTOKEN";
        }
        return type;
    }

    /**
     * Attribute information.
     */
    static class Attribute {
        /** Name. */
        final QName name_ = new QName();

        /** Type. */
        String type_;

        /** Value. */
        String value_;

        /** Specified. */
        boolean specified_;

        String getNonNormalizedValue() {
            return value_;
        }
    }

    /**
     * Attribute information.
     */
    static class AttributeExt extends Attribute {
        String nonNormalizedValue_;

        @Override
        String getNonNormalizedValue() {
            return nonNormalizedValue_;
        }
    }
}
