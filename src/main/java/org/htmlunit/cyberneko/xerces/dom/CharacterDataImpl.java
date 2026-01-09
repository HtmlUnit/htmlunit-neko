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
package org.htmlunit.cyberneko.xerces.dom;

import org.htmlunit.cyberneko.xerces.util.DOMMessageFormatter;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * CharacterData is an abstract Node that can carry character data as its Value.
 * It provides shared behavior for Text, CData, and possibly other node types.
 * All offsets are 0-based.
 * <p>
 * Since ProcessingInstructionImpl inherits from this class to reuse the
 * setNodeValue method, this class isn't declared as implementing the interface
 * CharacterData. This is done by relevant subclasses (TexImpl, CommentImpl).
 * <p>
 * This class doesn't directly support mutation events, however, it notifies the
 * document when mutations are performed so that the document class do so.
 */
public abstract class CharacterDataImpl extends ChildNode {

    protected String data_;

    /** Empty child nodes. */
    private static final NodeList EMPTY_NODE_LIST_SINGLETON = new NodeList() {
        @Override
        public Node item(final int index) {
            return null;
        }

        @Override
        public int getLength() {
            return 0;
        }
    };

    /**
     * Factory constructor.
     *
     * @param ownerDocument the owner document
     * @param data          the data
     */
    protected CharacterDataImpl(final CoreDocumentImpl ownerDocument, final String data) {
        super(ownerDocument);
        data_ = data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeList getChildNodes() {
        return EMPTY_NODE_LIST_SINGLETON;
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public String getNodeValue() {
        return data_;
    }

    /**
     * Convenience wrapper for calling setNodeValueInternal when we are not
     * performing a replacement operation.
     *
     * @param value the value
     */
    protected void setNodeValueInternal(final String value) {
        setNodeValueInternal(value, false);
    }

    /**
     * This function added so that we can distinguish whether setNodeValue has been
     * called from some other DOM functions. or by the client.
     * <p>
     * This is important, because we do one type of Range fix-up, from the
     * high-level functions in CharacterData, and another type if the client simply
     * calls setNodeValue(value).
     *
     * @param value   the value
     * @param replace flag to signal replace
     */
    protected void setNodeValueInternal(final String value, final boolean replace) {

        final CoreDocumentImpl ownerDocument = ownerDocument();
        // revisit: may want to set the value in ownerDocument.
        // Default behavior, overridden in some subclasses

        // keep old value for document notification
        final String oldvalue = this.data_;

        // notify document
        ownerDocument.modifyingCharacterData(this, replace);

        this.data_ = value;

        // notify document
        ownerDocument.modifiedCharacterData(this, oldvalue, value, replace);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the content, possibly firing related events, and updating ranges (via
     * notification to the document)
     */
    @Override
    public void setNodeValue(final String value) {

        setNodeValueInternal(value);

        // notify document
        ownerDocument().replacedText(this);
    }

    /**
     * @return character data currently stored in this node.
     */
    public String getData() {
        return data_;
    }

    /**
     * {@inheritDoc}
     *
     * Report number of characters currently stored in this node's data. It may be
     * 0, meaning that the value is an empty string.
     */
    @Override
    public int getLength() {
        return data_.length();
    }

    /**
     * Concatenate additional characters onto the end of the data stored in this
     * node. Note that this, and insert(), are the paths by which a DOM could wind
     * up accumulating more data than the language's strings can easily handle. (See
     * above discussion.)
     *
     * @param data the data
     */
    public void appendData(final String data) {
        if (data == null) {
            return;
        }

        setNodeValue(this.data_ + data);
    }

    /**
     * Remove a range of characters from the node's value. Throws a DOMException if
     * the offset is beyond the end of the string. However, a deletion _count_ that
     * exceeds the available data is accepted as a delete-to-end request.
     *
     * @param offset the offser
     * @param count  the count
     *
     * @throws DOMException INDEX_SIZE_ERR if offset is negative or greater than
     *                      length, or if count is negative.
     */
    public void deleteData(final int offset, final int count) throws DOMException {

        internalDeleteData(offset, count, false);
    }

    // NON-DOM INTERNAL: Within DOM actions, we sometimes need to be able
    // to control which mutation events are spawned. This version of the
    // deleteData operation allows us to do so. It is not intended
    // for use by application programs.
    void internalDeleteData(final int offset, final int count, final boolean replace) throws DOMException {

        final CoreDocumentImpl ownerDocument = ownerDocument();
        if (ownerDocument.errorChecking) {
            if (count < 0) {
                final String msg = DOMMessageFormatter.formatMessage("INDEX_SIZE_ERR", null);
                throw new DOMException(DOMException.INDEX_SIZE_ERR, msg);
            }
        }

        final int tailLength = Math.max(data_.length() - count - offset, 0);
        try {
            final String value = data_.substring(0, offset)
                    + (tailLength > 0 ? data_.substring(offset + count, offset + count + tailLength) : "");

            setNodeValueInternal(value, replace);

            // notify document
            ownerDocument.deletedText(this, offset, count);
        }
        catch (final StringIndexOutOfBoundsException e) {
            final String msg = DOMMessageFormatter.formatMessage("INDEX_SIZE_ERR", null);
            throw new DOMException(DOMException.INDEX_SIZE_ERR, msg);
        }
    }

    /**
     * Insert additional characters into the data stored in this node, at the offset
     * specified.
     *
     * @param offset the offset
     * @param data   the data
     *
     * @throws DOMException INDEX_SIZE_ERR if offset is negative or greater than
     *                      length.
     */
    public void insertData(final int offset, final String data) throws DOMException {

        internalInsertData(offset, data, false);
    }

    // NON-DOM INTERNAL: Within DOM actions, we sometimes need to be able
    // to control which mutation events are spawned. This version of the
    // insertData operation allows us to do so. It is not intended
    // for use by application programs.
    void internalInsertData(final int offset, final String data, final boolean replace) throws DOMException {

        final CoreDocumentImpl ownerDocument = ownerDocument();
        try {
            final String value = new StringBuilder(this.data_).insert(offset, data).toString();

            setNodeValueInternal(value, replace);

            // notify document
            ownerDocument.insertedText(this, offset, data.length());
        }
        catch (final StringIndexOutOfBoundsException e) {
            final String msg = DOMMessageFormatter.formatMessage("INDEX_SIZE_ERR", null);
            throw new DOMException(DOMException.INDEX_SIZE_ERR, msg);
        }

    }

    /**
     * Replace a series of characters at the specified (zero-based) offset with a
     * new string, NOT necessarily of the same length. Convenience method,
     * equivalent to a delete followed by an insert. Throws a DOMException if the
     * specified offset is beyond the end of the existing data.
     *
     * @param offset The offset at which to begin replacing.
     *
     * @param count  The number of characters to remove, interpreted as in the
     *               delete() method.
     *
     * @param data   The new string to be inserted at offset in place of the removed
     *               data. Note that the entire string will be inserted -- the count
     *               parameter does not affect insertion, and the new data may be
     *               longer or shorter than the substring it replaces.
     *
     * @throws DOMException INDEX_SIZE_ERR if offset is negative or greater than
     *                      length, or if count is negative.
     */
    public void replaceData(final int offset, final int count, final String data) throws DOMException {

        final CoreDocumentImpl ownerDocument = ownerDocument();

        // notify document
        ownerDocument.replacingData(this);

        // keep old value for document notification
        final String oldvalue = this.data_;

        internalDeleteData(offset, count, true);
        internalInsertData(offset, data, true);

        ownerDocument.replacedCharacterData(this, oldvalue, this.data_);

    }

    /**
     * Store character data into this node.
     *
     * @param value the value
     */
    public void setData(final String value) throws DOMException {
        setNodeValue(value);
    }

    /**
     * Substring is more than a convenience function. In some implementations of the
     * DOM, where the stored data may exceed the length that can be returned in a
     * single string, the only way to read it all is to extract it in chunks via
     * this method.
     *
     * @param offset Zero-based offset of first character to retrieve.
     * @param count  Number of characters to retrieve.
     *               <p>
     *               If the sum of offset and count exceeds the length, all
     *               characters to end of data are returned.
     * @return the substring
     *
     * @throws DOMException INDEX_SIZE_ERR if offset is negative or greater than
     *                      length, or if count is negative.
     *
     * @throws DOMException WSTRING_SIZE_ERR In some implementations, count may
     *                      exceed the permitted length of strings. If so,
     *                      substring() will throw this DOMException advising the
     *                      user to instead retrieve the data in smaller chunks.
     */
    public String substringData(final int offset, final int count) throws DOMException {
        final int length = data_.length();
        if (count < 0 || offset < 0 || offset > length - 1) {
            final String msg = DOMMessageFormatter.formatMessage("INDEX_SIZE_ERR", null);
            throw new DOMException(DOMException.INDEX_SIZE_ERR, msg);
        }

        final int tailIndex = Math.min(offset + count, length);

        return data_.substring(offset, tailIndex);

    }
}
