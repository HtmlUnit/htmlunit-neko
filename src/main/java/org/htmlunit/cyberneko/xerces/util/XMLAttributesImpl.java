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
package org.htmlunit.cyberneko.xerces.util;

import java.util.ArrayList;

import org.htmlunit.cyberneko.xerces.xni.QName;
import org.htmlunit.cyberneko.xerces.xni.XMLAttributes;

/**
 * The XMLAttributesImpl class is an implementation of the {@link XMLAttributes}
 * interface which defines a collection of attributes for an element. In the
 * parser, the document source scans the entire start element and collects
 * the attributes, communicating them to the document handler via the
 * {@code startElement} method.
 * <p>
 * The attributes are read-write so that subsequent stages in the document
 * pipeline can modify values or change attributes propagated to the next stage.
 * </p>
 *
 * @see org.htmlunit.cyberneko.xerces.xni.XMLDocumentHandler#startElement
 *
 * @author Andy Clark, IBM
 * @author Elena Litani, IBM
 * @author Michael Glavassevich, IBM
 * @author Ronald Brill
 */
public class XMLAttributesImpl implements XMLAttributes {

    /** Active attribute information. */
    private final ArrayList<Attribute> attributes_;

    /**
     * Constructs a default {@code XMLAttributesImpl} instance with an initial capacity.
     */
    public XMLAttributesImpl() {
        attributes_ = new ArrayList<>(4);
    }

    /**
     * Constructs a copy of the specified attribute collection.
     *
     * @param attributes the attribute collection to copy from
     */
    public XMLAttributesImpl(final XMLAttributesImpl attributes) {
        final ArrayList<Attribute> attribs = attributes.getAttributes();
        final int length = attribs.size();
        if (length > 0) {
            attributes_ = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                attributes_.add(attribs.get(i).clone());
            }
            return;
        }

        attributes_ = new ArrayList<>(4);
    }

    /**
     * Adds an attribute. The attribute's non-normalized value will
     * have the same value as the attribute value. Also, the added attribute will be
     * marked as specified in the XML instance document unless set otherwise using
     * the {@link #setSpecified} method.
     * <p>
     * <strong>Note:</strong> This implementation does <em>not</em> check whether an
     * attribute of the same name already exists. If duplicate prevention is required,
     * the caller must verify uniqueness before calling this method.
     * </p>
     *
     * @param name  the attribute name
     * @param type  the attribute type. The type name is determined by the type
     *              specified for this attribute in the DTD (e.g., "CDATA",
     *              "ID", "NMTOKEN", etc.). Enumeration types will have the type
     *              value specified as a pipe-separated list enclosed in parentheses,
     *              for example: "(true|false)".
     * @param value the attribute value
     * @return the zero-based attribute index
     * @see #setSpecified
     */
    @Override
    public int addAttribute(final QName name, final String type, final String value) {
        addAttribute(name, type, value, false);
        return attributes_.size() - 1;
    }

    /**
     * Adds an attribute. The attribute's non-normalized value will
     * have the same value as the attribute value. Also, the added attribute will be
     * marked as specified in the XML instance document unless set otherwise using
     * the {@link #setSpecified} method.
     * <p>
     * This method differs from other addAttribute variants in that it does not check
     * if an attribute of the same name already exists in the list before adding it.
     * To improve performance of namespace processing, this method allows
     * uniqueness checks to be deferred until all namespace information is
     * available after the entire attribute specification has been read.
     * </p>
     * <p>
     * <strong>Caution:</strong> If this method is called, it should not be mixed
     * with standard {@code addAttribute} calls unless it has been determined that
     * all attribute names are unique.
     * </p>
     *
     * @param name      the attribute name
     * @param type      the attribute type
     * @param value     the attribute value
     * @param specified {@code true} if the attribute was specified in the instance document
     */
    public void addAttribute(final QName name, final String type, final String value, final boolean specified) {
        final Attribute attribute = new Attribute();
        attribute.name_.setValues(name);
        attribute.type_ = type;
        attribute.value_ = value;
        attribute.specified_ = specified;

        attributes_.add(attribute);
    }

    /**
     * Adds an attribute together with its non-normalized (plain) value.
     * <p>
     * This variant stores both the normalized {@code value} and the original
     * {@code nonNormalizedValue} so that downstream consumers can access the
     * raw attribute text via {@link #getNonNormalizedValue(int)}.
     * </p>
     * <p>
     * Like other {@code addAttribute} overloads, this method does
     * <em>not</em> check for duplicate attribute names.
     * </p>
     *
     * @param name               the attribute name
     * @param type               the attribute type (e.g., "CDATA")
     * @param value              the normalized attribute value
     * @param nonNormalizedValue the original, non-normalized attribute value
     * @param specified          {@code true} if the attribute was specified in the instance document
     */
    public void addAttribute(final QName name, final String type, final String value,
                           final String nonNormalizedValue, final boolean specified) {
        final AttributeExt attribute = new AttributeExt();
        attribute.name_.setValues(name);
        attribute.type_ = type;
        attribute.value_ = value;
        attribute.nonNormalizedValue_ = nonNormalizedValue;
        attribute.specified_ = specified;

        attributes_.add(attribute);
    }

    /**
     * Removes all attributes from the collection. This method also removes all entities
     * associated with the attributes.
     */
    @Override
    public void removeAllAttributes() {
        attributes_.clear();
    }

    /**
     * Removes the attribute at the specified index.
     * <p>
     * <strong>Note:</strong> This operation shifts the indices of all attributes
     * following the specified index.
     * </p>
     *
     * @param attrIndex the zero-based attribute index
     */
    @Override
    public void removeAttributeAt(final int attrIndex) {
        attributes_.remove(attrIndex);
    }

    /**
     * Sets the name of the attribute at the specified index.
     *
     * @param attrIndex the zero-based attribute index
     * @param attrName  the new attribute name
     */
    @Override
    public void setName(final int attrIndex, final QName attrName) {
        attributes_.get(attrIndex).name_.setValues(attrName);
    }

    /**
     * Populates the fields of the given {@link QName} structure with the values of the attribute
     * name at the specified index.
     *
     * @param attrIndex the zero-based attribute index
     * @param attrName  the attribute name structure to fill in
     */
    @Override
    public void getName(final int attrIndex, final QName attrName) {
        attrName.setValues(attributes_.get(attrIndex).name_);
    }

    /**
     * Sets the value of the attribute at the specified index. This method also
     * overwrites the non-normalized value of the attribute.
     *
     * @param attrIndex the zero-based attribute index
     * @param attrValue the new attribute value
     */
    @Override
    public void setValue(final int attrIndex, final String attrValue) {
        attributes_.get(attrIndex).value_ = attrValue;
    }

    /**
     * Sets whether an attribute was specified in the instance document.
     *
     * @param attrIndex the zero-based attribute index
     * @param specified {@code true} if the attribute is specified in the instance document
     */
    @Override
    public void setSpecified(final int attrIndex, final boolean specified) {
        attributes_.get(attrIndex).specified_ = specified;
    }

    /**
     * Returns whether the attribute at the specified index was specified in the instance document.
     *
     * @param attrIndex the zero-based attribute index
     * @return {@code true} if specified, {@code false} otherwise
     */
    @Override
    public boolean isSpecified(final int attrIndex) {
        return attributes_.get(attrIndex).specified_;
    }

    /**
     * Returns the number of attributes in the list.
     * <p>
     * Once you know the number of attributes, you can iterate through the list.
     * </p>
     *
     * @return the number of attributes in the list
     */
    @Override
    public int getLength() {
        return attributes_.size();
    }

    /**
     * Looks up an attribute's type by index.
     * <p>
     * The attribute type is one of the strings "CDATA", "ID", "IDREF", "IDREFS",
     * "NMTOKEN", "NMTOKENS", "ENTITY", "ENTITIES", or "NOTATION" (always in upper case).
     * </p>
     * <p>
     * If the parser has not read a declaration for the attribute, or if it
     * does not report attribute types, it must return "CDATA" as stated in the
     * XML 1.0 Recommendation (clause 3.3.3, "Attribute-Value Normalization").
     * </p>
     *
     * @param index the zero-based attribute index
     * @return the attribute's type as a string, or {@code null} if out of range
     * @see #getLength
     */
    @Override
    public String getType(final int index) {
        if (index < 0 || index >= attributes_.size()) {
            return null;
        }
        return getReportableType(attributes_.get(index).type_);
    }

    /**
     * Looks up an attribute's type by XML 1.0 qualified name.
     * <p>
     * See {@link #getType(int)} for a description of possible types.
     * </p>
     *
     * @param qname the XML 1.0 qualified name
     * @return the attribute type as a string, or {@code null} if not found
     */
    @Override
    public String getType(final String qname) {
        final int index = getIndex(qname);
        return index != -1 ? getReportableType(attributes_.get(index).type_) : null;
    }

    /**
     * Looks up an attribute's value by index.
     * <p>
     * If the attribute value is a list of tokens (IDREFS, ENTITIES, or NMTOKENS),
     * tokens are concatenated into a single string separated by a single space.
     * </p>
     *
     * @param index the zero-based attribute index
     * @return the attribute's value as a string, or {@code null} if out of range
     * @see #getLength
     */
    @Override
    public String getValue(final int index) {
        if (index < 0 || index >= attributes_.size()) {
            return null;
        }
        return attributes_.get(index).value_;
    }

    /**
     * Looks up an attribute's value by XML 1.0 qualified name.
     * <p>
     * See {@link #getValue(int)} for a description of possible values.
     * </p>
     *
     * @param qname the XML 1.0 qualified name
     * @return the attribute value as a string, or {@code null} if not found
     */
    @Override
    public String getValue(final String qname) {
        final int index = getIndex(qname);
        return index != -1 ? attributes_.get(index).value_ : null;
    }

    /**
     * <span style="color:red">INTERNAL API - SUBJECT TO CHANGE AT ANY TIME - USE AT YOUR OWN RISK.</span>
     * <p>
     * Returns the raw (prefixed) name of the attribute at the given index.
     * This is a convenience shortcut equivalent to
     * {@code getName(index).getRawname()} with bounds checking.
     * </p>
     *
     * @param index the zero-based attribute index
     * @return the raw name of the indexed attribute, or {@code null} if out of range
     * @see #getName(int)
     * @see #getLength
     */
    public String getNameRawName(final int index) {
        if (index < 0 || index >= attributes_.size()) {
            return null;
        }
        return attributes_.get(index).name_.getRawname();
    }

    /**
     * Returns the full {@link QName} structure of the attribute at the specified index.
     *
     * @param index the zero-based attribute index
     * @return the attribute's full {@link QName}
     */
    @Override
    public QName getName(final int index) {
        return attributes_.get(index).name_;
    }

    /**
     * Looks up the index of an attribute by XML 1.0 qualified name.
     *
     * @param qName the qualified (prefixed) name
     * @return the attribute index, or {@code -1} if it does not appear in the list
     */
    @Override
    public int getIndex(final String qName) {
        final int length = attributes_.size();
        for (int i = 0; i < length; i++) {
            final String rawname = attributes_.get(i).name_.getRawname();
            if (rawname != null && rawname.equals(qName)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Looks up the index of an attribute by Namespace name.
     *
     * @param uri       the Namespace URI, or {@code null} if none
     * @param localPart the attribute's local name
     * @return the attribute index, or {@code -1} if it does not appear in the list
     */
    @Override
    public int getIndex(final String uri, final String localPart) {
        final int length = attributes_.size();
        for (int i = 0; i < length; i++) {
            final QName name = attributes_.get(i).name_;
            final String attrLocal = name.getLocalpart();
            if (attrLocal != null && attrLocal.equals(localPart)) {
                final String attrUri = name.getUri();
                if (uri == attrUri || (uri != null && uri.equals(attrUri))) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Looks up an attribute's local name by index.
     *
     * @param index the zero-based attribute index
     * @return the local name, empty string if namespaces are disabled, or {@code null} if out of range
     * @see #getLength
     */
    @Override
    public String getLocalName(final int index) {
        if (index < 0 || index >= attributes_.size()) {
            return null;
        }
        return attributes_.get(index).name_.getLocalpart();
    }

    /**
     * Looks up an attribute's XML 1.0 qualified name by index.
     *
     * @param index the zero-based attribute index
     * @return the qualified name, empty string if none available, or {@code null} if out of range
     * @see #getLength
     */
    @Override
    public String getQName(final int index) {
        if (index < 0 || index >= attributes_.size()) {
            return null;
        }
        final String rawname = attributes_.get(index).name_.getRawname();
        return rawname != null ? rawname : "";
    }

    /**
     * Looks up an attribute's type by Namespace name.
     * <p>
     * See {@link #getType(int)} for a description of possible types.
     * </p>
     *
     * @param uri       the Namespace URI, or {@code null} if none
     * @param localName the local name of the attribute
     * @return the attribute type, or {@code null} if not found
     */
    @Override
    public String getType(final String uri, final String localName) {
        final int index = getIndex(uri, localName);
        return index != -1 ? getReportableType(attributes_.get(index).type_) : null;
    }

    /**
     * Looks up an attribute's Namespace URI by index.
     *
     * @param index the zero-based attribute index
     * @return the Namespace URI, or {@code null} if out of range
     * @see #getLength
     */
    @Override
    public String getURI(final int index) {
        if (index < 0 || index >= attributes_.size()) {
            return null;
        }
        return attributes_.get(index).name_.getUri();
    }

    /**
     * Looks up an attribute's value by Namespace name.
     * <p>
     * See {@link #getValue(int)} for a description of possible values.
     * </p>
     *
     * @param uri       the Namespace URI, or {@code null} if none
     * @param localName the local name of the attribute
     * @return the attribute value, or {@code null} if not found
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
    public String getNonNormalizedValue(final int index) {
        if (index < 0 || index >= attributes_.size()) {
            return null;
        }
        return attributes_.get(index).getNonNormalizedValue();
    }

    // Attributes2 impl

    @Override
    public boolean isDeclared(final int index) {
        return false;
    }

    @Override
    public boolean isDeclared(final String qName) {
        return false;
    }

    @Override
    public boolean isDeclared(final String uri, final String localName) {
        return false;
    }

    @Override
    public boolean isSpecified(final String qName) {
        final int index = getIndex(qName);
        if (index == -1) {
            throw new IllegalArgumentException(qName);
        }
        return isSpecified(index);
    }

    @Override
    public boolean isSpecified(final String uri, final String localName) {
        final int index = getIndex(uri, localName);
        if (index == -1) {
            throw new IllegalArgumentException(localName);
        }
        return isSpecified(index);
    }

    // end Attributes2 impl

    /**
     * <span style="color:red">INTERNAL API - SUBJECT TO CHANGE AT ANY TIME - USE AT YOUR OWN RISK.</span>
     *
     * @return the internal attributes {@link ArrayList} to allow performance optimizations
     */
    public ArrayList<Attribute> getAttributes() {
        return attributes_;
    }

    /**
     * Returns the reportable type string, converting enumerated types to "NMTOKEN".
     *
     * @param type the raw attribute type
     * @return the reportable attribute type
     */
    private static String getReportableType(final String type) {
        if (type.charAt(0) == '(') {
            return "NMTOKEN";
        }
        return type;
    }

    /**
     * <span style="color:red">INTERNAL API - SUBJECT TO CHANGE AT ANY TIME - USE AT YOUR OWN RISK.</span>
     * <p>
     * Attribute information container.
     * Only public to allow certain performance optimizations.
     * </p>
     */
    public static class Attribute {
        /** Attribute qualified name. */
        final QName name_ = new QName();

        /**
         * Returns the attribute's qualified name structure.
         *
         * @return the qualified name
         */
        public QName getQName() {
            return name_;
        }

        /** Attribute type. */
        String type_;

        /** Attribute normalized value. */
        String value_;

        /**
         * Returns the attribute's value.
         *
         * @return the attribute value
         */
        public String getValue() {
            return value_;
        }

        /** Flag indicating whether the attribute was explicitly specified in the instance document. */
        boolean specified_;

        /**
         * Returns the non-normalized value of this attribute.
         *
         * @return the non-normalized attribute value
         */
        String getNonNormalizedValue() {
            return value_;
        }

        @Override
        public Attribute clone() {
            final Attribute clone = new Attribute();
            clone.name_.setValues(name_);
            clone.type_ = type_;
            clone.value_ = value_;
            clone.specified_ = specified_;
            return clone;
        }
    }

    /**
     * Extended attribute information container holding non-normalized text.
     */
    static class AttributeExt extends Attribute {
        /** Non-normalized attribute value. */
        String nonNormalizedValue_;

        @Override
        String getNonNormalizedValue() {
            return nonNormalizedValue_;
        }

        @Override
        public Attribute clone() {
            final AttributeExt clone = new AttributeExt();
            clone.name_.setValues(name_);
            clone.type_ = type_;
            clone.value_ = value_;
            clone.specified_ = specified_;
            clone.nonNormalizedValue_ = nonNormalizedValue_;
            return clone;
        }
    }

    /**
     * An immutable, empty {@link XMLAttributesImpl} used as a flyweight for
     * synthesized elements. Any attempt to mutate it throws
     * {@link UnsupportedOperationException} so that accidental downstream
     * modifications fail fast.
     */
    public static final class EmptyXMLAttributesImpl extends XMLAttributesImpl {

        /** Shared singleton instance. */
        public static final EmptyXMLAttributesImpl INSTANCE = new EmptyXMLAttributesImpl();

        private EmptyXMLAttributesImpl() {
            // zero attributes, no further setup needed
        }

        @Override
        public int addAttribute(final QName name, final String type, final String value) {
            throw mutation();
        }

        @Override
        public void addAttribute(final QName name, final String type, final String value, final boolean specified) {
            throw mutation();
        }

        @Override
        public void addAttribute(final QName name, final String type, final String value,
                final String nonNormalizedValue, final boolean specified) {
            throw mutation();
        }

        @Override
        public void removeAllAttributes() {
            throw mutation();
        }

        @Override
        public void removeAttributeAt(final int i) {
            throw mutation();
        }

        @Override
        public void setName(final int i, final QName q) {
            throw mutation();
        }

        @Override
        public void setValue(final int i, final String v) {
            throw mutation();
        }

        @Override
        public void setSpecified(final int i, final boolean s) {
            throw mutation();
        }

        private static UnsupportedOperationException mutation() {
            return new UnsupportedOperationException("EmptyAttributes is a shared immutable instance");
        }
    }
}