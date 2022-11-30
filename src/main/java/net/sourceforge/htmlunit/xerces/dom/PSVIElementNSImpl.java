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

package net.sourceforge.htmlunit.xerces.dom;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import net.sourceforge.htmlunit.xerces.impl.dv.ValidatedInfo;
import net.sourceforge.htmlunit.xerces.impl.xs.ElementPSVImpl;
import net.sourceforge.htmlunit.xerces.impl.xs.util.StringListImpl;
import net.sourceforge.htmlunit.xerces.xs.ElementPSVI;
import net.sourceforge.htmlunit.xerces.xs.ItemPSVI;
import net.sourceforge.htmlunit.xerces.xs.ShortList;
import net.sourceforge.htmlunit.xerces.xs.StringList;
import net.sourceforge.htmlunit.xerces.xs.XSComplexTypeDefinition;
import net.sourceforge.htmlunit.xerces.xs.XSElementDeclaration;
import net.sourceforge.htmlunit.xerces.xs.XSModel;
import net.sourceforge.htmlunit.xerces.xs.XSNotationDeclaration;
import net.sourceforge.htmlunit.xerces.xs.XSSimpleTypeDefinition;
import net.sourceforge.htmlunit.xerces.xs.XSTypeDefinition;
import net.sourceforge.htmlunit.xerces.xs.XSValue;

/**
 * Element namespace implementation; stores PSVI element items.
 *
 * @xerces.internal
 *
 * @author Sandy Gao, IBM
 *
 * @version $Id$
 */
public class PSVIElementNSImpl extends ElementNSImpl implements ElementPSVI {

    /** Serialization version. */
    static final long serialVersionUID = 6815489624636016068L;

    /**
     * Construct an element node.
     */
    public PSVIElementNSImpl(CoreDocumentImpl ownerDocument, String namespaceURI,
                             String qualifiedName, String localName) {
        super(ownerDocument, namespaceURI, qualifiedName, localName);
    }

    /**
     * Construct an element node.
     */
    public PSVIElementNSImpl(CoreDocumentImpl ownerDocument, String namespaceURI,
                             String qualifiedName) {
        super(ownerDocument, namespaceURI, qualifiedName);
    }

    /** element declaration */
    protected XSElementDeclaration fDeclaration = null;

    /** type of element, could be xsi:type */
    protected XSTypeDefinition fTypeDecl = null;

    /** true if clause 3.2 of Element Locally Valid (Element) (3.3.4)
      * is satisfied, otherwise false
      */
    protected boolean fNil = false;

    /** false if the element value was provided by the schema; true otherwise.
     */
    protected boolean fSpecified = true;

    /** Schema value */
    protected final ValidatedInfo fValue = new ValidatedInfo();

    /** http://www.w3.org/TR/xmlschema-1/#e-notation*/
    protected XSNotationDeclaration fNotation = null;

    /** validation attempted: none, partial, full */
    protected short fValidationAttempted = ElementPSVI.VALIDATION_NONE;

    /** validity: valid, invalid, unknown */
    protected short fValidity = ElementPSVI.VALIDITY_NOTKNOWN;

    /** error codes */
    protected StringList fErrorCodes = null;

    /** error messages */
    protected StringList fErrorMessages = null;

    /** validation context: could be QName or XPath expression*/
    protected String fValidationContext = null;

    /** the schema information property */
    protected XSModel fSchemaInformation = null;

    //
    // ElementPSVI methods
    //

    /* (non-Javadoc)
     * @see net.sourceforge.htmlunit.xerces.xs.ItemPSVI#constant()
     */
    @Override
    public ItemPSVI constant() {
        return new ElementPSVImpl(true, this);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.htmlunit.xerces.xs.ItemPSVI#isConstant()
     */
    @Override
    public boolean isConstant() {
        return false;
    }

    /**
     * [schema default]
     *
     * @return The canonical lexical representation of the declaration's {value constraint} value.
     * @see <a href="http://www.w3.org/TR/xmlschema-1/#e-schema_default>XML Schema Part 1: Structures [schema default]</a>
     */
    @Override
    public String getSchemaDefault() {
        return fDeclaration == null ? null : fDeclaration.getConstraintValue();
    }

    /**
     * [schema normalized value]
     *
     *
     * @see <a href="http://www.w3.org/TR/xmlschema-1/#e-schema_normalized_value>XML Schema Part 1: Structures [schema normalized value]</a>
     * @return the normalized value of this item after validation
     */
    @Override
    public String getSchemaNormalizedValue() {
        return fValue.getNormalizedValue();
    }

    /**
     * [schema specified]
     * @see <a href="http://www.w3.org/TR/xmlschema-1/#e-schema_specified">XML Schema Part 1: Structures [schema specified]</a>
     * @return false value was specified in schema, true value comes from the infoset
     */
    @Override
    public boolean getIsSchemaSpecified() {
        return fSpecified;
    }

    /**
     * Determines the extent to which the document has been validated
     *
     * @return return the [validation attempted] property. The possible values are
     *         NO_VALIDATION, PARTIAL_VALIDATION and FULL_VALIDATION
     */
    @Override
    public short getValidationAttempted() {
        return fValidationAttempted;
    }

    /**
     * Determine the validity of the node with respect
     * to the validation being attempted
     *
     * @return return the [validity] property. Possible values are:
     *         UNKNOWN_VALIDITY, INVALID_VALIDITY, VALID_VALIDITY
     */
    @Override
    public short getValidity() {
        return fValidity;
    }

    /**
     * A list of error codes generated from validation attempts.
     * Need to find all the possible subclause reports that need reporting
     *
     * @return Array of error codes
     */
    @Override
    public StringList getErrorCodes() {
        if (fErrorCodes != null) {
            return fErrorCodes;
        }
        return StringListImpl.EMPTY_LIST;
    }

    /**
     * A list of error messages generated from the validation attempt or
     * an empty <code>StringList</code> if no errors occurred during the
     * validation attempt. The indices of error messages in this list are
     * aligned with those in the <code>[schema error code]</code> list.
     */
    @Override
    public StringList getErrorMessages() {
        if (fErrorMessages != null) {
            return fErrorMessages;
        }
        return StringListImpl.EMPTY_LIST;
    }

    // This is the only information we can provide in a pipeline.
    @Override
    public String getValidationContext() {
        return fValidationContext;
    }

    /**
     * [nil]
     * @see <a href="http://www.w3.org/TR/xmlschema-1/#e-nil>XML Schema Part 1: Structures [nil]</a>
     * @return true if clause 3.2 of Element Locally Valid (Element) (3.3.4) above is satisfied, otherwise false
     */
    @Override
    public boolean getNil() {
        return fNil;
    }

    /**
     * [notation]
     * @see <a href="http://www.w3.org/TR/xmlschema-1/#e-notation>XML Schema Part 1: Structures [notation]</a>
     * @return The notation declaration.
     */
    @Override
    public XSNotationDeclaration getNotation() {
        return fNotation;
    }

    /**
     * An item isomorphic to the type definition used to validate this element.
     *
     * @return  a type declaration
     */
    @Override
    public XSTypeDefinition getTypeDefinition() {
        return fTypeDecl;
    }

    /**
     * If and only if that type definition is a simple type definition
     * with {variety} union, or a complex type definition whose {content type}
     * is a simple thype definition with {variety} union, then an item isomorphic
     * to that member of the union's {member type definitions} which actually
     * validated the element item's normalized value.
     *
     * @return  a simple type declaration
     */
    @Override
    public XSSimpleTypeDefinition getMemberTypeDefinition() {
        return fValue.getMemberTypeDefinition();
    }

    /**
     * An item isomorphic to the element declaration used to validate
     * this element.
     *
     * @return  an element declaration
     */
    @Override
    public XSElementDeclaration getElementDeclaration() {
        return fDeclaration;
    }

    /**
     * [schema information]
     * @see <a href="http://www.w3.org/TR/xmlschema-1/#e-schema_information">XML Schema Part 1: Structures [schema information]</a>
     * @return The schema information property if it's the validation root,
     *         null otherwise.
     */
    @Override
    public XSModel getSchemaInformation() {
        return fSchemaInformation;
    }

    /**
     * Copy PSVI properties from another psvi item.
     *
     * @param elem  the source of element PSVI items
     */
    public void setPSVI(ElementPSVI elem) {
        this.fDeclaration = elem.getElementDeclaration();
        this.fNotation = elem.getNotation();
        this.fValidationContext = elem.getValidationContext();
        this.fTypeDecl = elem.getTypeDefinition();
        this.fSchemaInformation = elem.getSchemaInformation();
        this.fValidity = elem.getValidity();
        this.fValidationAttempted = elem.getValidationAttempted();
        this.fErrorCodes = elem.getErrorCodes();
        this.fErrorMessages = elem.getErrorMessages();
        if (fTypeDecl instanceof XSSimpleTypeDefinition ||
                fTypeDecl instanceof XSComplexTypeDefinition &&
                ((XSComplexTypeDefinition)fTypeDecl).getContentType() == XSComplexTypeDefinition.CONTENTTYPE_SIMPLE) {
            this.fValue.copyFrom(elem.getSchemaValue());
        }
        else {
            this.fValue.reset();
        }
        this.fSpecified = elem.getIsSchemaSpecified();
        this.fNil = elem.getNil();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.htmlunit.xerces.xs.ItemPSVI#getActualNormalizedValue()
     */
    @Override
    public Object getActualNormalizedValue() {
        return fValue.getActualValue();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.htmlunit.xerces.xs.ItemPSVI#getActualNormalizedValueType()
     */
    @Override
    public short getActualNormalizedValueType() {
        return fValue.getActualValueType();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.htmlunit.xerces.xs.ItemPSVI#getItemValueTypes()
     */
    @Override
    public ShortList getItemValueTypes() {
        return fValue.getListValueTypes();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.htmlunit.xerces.xs.ItemPSVI#getSchemaValue()
     */
    @Override
    public XSValue getSchemaValue() {
        return fValue;
    }

    // REVISIT: Forbid serialization of PSVI DOM until
    // we support object serialization of grammars -- mrglavas

    private void writeObject(ObjectOutputStream out)
        throws IOException {
        throw new NotSerializableException(getClass().getName());
    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        throw new NotSerializableException(getClass().getName());
    }
}
