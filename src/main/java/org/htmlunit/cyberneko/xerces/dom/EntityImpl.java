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
package org.htmlunit.cyberneko.xerces.dom;

import org.w3c.dom.Entity;
import org.w3c.dom.Node;

/**
 * Entity nodes hold the reference data for an XML Entity -- either parsed or
 * unparsed. The nodeName (inherited from Node) will contain the name (if any)
 * of the Entity. Its data will be contained in the Entity's children, in
 * exactly the structure which an EntityReference to this name will present
 * within the document's body.
 * <P>
 * Note that this object models the actual entity, _not_ the entity declaration
 * or the entity reference.
 * <P>
 * An XML processor may choose to completely expand entities before the
 * structure model is passed to the DOM; in this case, there will be no
 * EntityReferences in the DOM tree.
 * <P>
 * Quoting the 10/01 DOM Proposal, <BLOCKQUOTE> "The DOM Level 1 does not
 * support editing Entity nodes; if a user wants to make changes to the contents
 * of an Entity, every related EntityReference node has to be replaced in the
 * structure model by a clone of the Entity's contents, and then the desired
 * changes must be made to each of those clones instead. All the descendants of
 * an Entity node are readonly." </BLOCKQUOTE> I'm interpreting this as: It is
 * the parser's responsibilty to call the non-DOM operation
 * setReadOnly(true,true) after it constructs the Entity. Since the DOM
 * explicitly decided not to deal with this, _any_ answer will involve a non-DOM
 * operation, and this is the simplest solution.
 * <p>
 *
 * @author Elena Litani, IBM
 */
public class EntityImpl extends ParentNode implements Entity {

    /** Entity name. */
    private final String name_;

    /** Public identifier. */
    private String publicId_;

    /** System identifier. */
    private String systemId_;

    /** Encoding */
    private String encoding_;

    /** Input Encoding */
    private String inputEncoding_;

    /** Version */
    private String version_;

    /** Notation name. */
    private String notationName_;

    /** base uri */
    private String baseURI_;

    // Factory constructor.
    public EntityImpl(final CoreDocumentImpl ownerDoc, final String name) {
        super(ownerDoc);
        name_ = name;
    }

    /**
     * {@inheritDoc}
     *
     * A short integer indicating what type of node this is. The named constants for
     * this value are defined in the org.w3c.dom.Node interface.
     */
    @Override
    public short getNodeType() {
        return Node.ENTITY_NODE;
    }

    /**
     * {@inheritDoc}
     *
     * Returns the entity name
     */
    @Override
    public String getNodeName() {
        return name_;
    }

    /**
     * {@inheritDoc}
     *
     * Clone node.
     */
    @Override
    public Node cloneNode(final boolean deep) {
        final EntityImpl newentity = (EntityImpl) super.cloneNode(deep);
        return newentity;
    }

    /**
     * {@inheritDoc}
     *
     * The public identifier associated with the entity. If not specified, this will
     * be null.
     */
    @Override
    public String getPublicId() {
        return publicId_;

    }

    /**
     * {@inheritDoc}
     *
     * The system identifier associated with the entity. If not specified, this will
     * be null.
     */
    @Override
    public String getSystemId() {
        return systemId_;

    }

    /**
     * {@inheritDoc}
     *
     * DOM Level 3 WD - experimental the version number of this entity, when it is
     * an external parsed entity.
     */
    @Override
    public String getXmlVersion() {
        return version_;

    }

    /**
     * {@inheritDoc}
     *
     * DOM Level 3 WD - experimental the encoding of this entity, when it is an
     * external parsed entity.
     */
    @Override
    public String getXmlEncoding() {
        return encoding_;

    }

    /**
     * {@inheritDoc}
     *
     * Unparsed entities -- which contain non-XML data -- have a "notation name"
     * which tells applications how to deal with them. Parsed entities, which
     * <em>are</em> in XML format, don't need this and set it to null.
     */
    @Override
    public String getNotationName() {
        return notationName_;

    }

    /**
     * DOM Level 2: The public identifier associated with the entity. If not
     * specified, this will be null.
     *
     * @param id the id
     */
    public void setPublicId(final String id) {
        publicId_ = id;

    }

    /**
     * NON-DOM encoding - An attribute specifying, as part of the text declaration,
     * the encoding of this entity, when it is an external parsed entity. This is
     * null otherwise
     *
     */
    public void setXmlEncoding(final String value) {
        encoding_ = value;
    }

    /**
     * {@inheritDoc}
     *
     * An attribute specifying the encoding used for this entity at the tiome of
     * parsing, when it is an external parsed entity. This is <code>null</code> if
     * it an entity from the internal subset or if it is not known..
     */
    @Override
    public String getInputEncoding() {
        return inputEncoding_;
    }

    // NON-DOM, used to set the input encoding.
    public void setInputEncoding(final String inputEncoding) {
        inputEncoding_ = inputEncoding;
    }

    // NON-DOM
    // version - An attribute specifying, as part of the text declaration,
    // the version number of this entity, when it is an external parsed entity.
    // This is null otherwise
    public void setXmlVersion(final String value) {
        version_ = value;
    }

    /**
     * DOM Level 2: The system identifier associated with the entity. If not
     * specified, this will be null.
     *
     * @param id the id
     */
    public void setSystemId(final String id) {
        systemId_ = id;

    }

    /**
     * DOM Level 2: Unparsed entities -- which contain non-XML data -- have a
     * "notation name" which tells applications how to deal with them. Parsed
     * entities, which <em>are</em> in XML format, don't need this and set it to
     * null.
     *
     * @param name the name
     */
    public void setNotationName(final String name) {
        notationName_ = name;

    }

    /**
     * {@inheritDoc}
     *
     * Returns the absolute base URI of this node or null if the implementation
     * wasn't able to obtain an absolute URI. Note: If the URI is malformed, a null
     * is returned.
     *
     * @return The absolute base URI of this node or null.
     */
    @Override
    public String getBaseURI() {
        return (baseURI_ != null) ? baseURI_ : getOwnerDocument().getBaseURI();
    }

    // NON-DOM: set base uri
    public void setBaseURI(final String uri) {
        baseURI_ = uri;
    }
}
