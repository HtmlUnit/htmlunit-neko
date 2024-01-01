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

import java.io.InputStream;
import java.io.Reader;

/**
 * This class represents an input source for an XML document. The basic
 * properties of an input source are the following:
 * <ul>
 * <li>public identifier</li>
 * <li>system identifier</li>
 * <li>byte stream or character stream</li>
 * <li>
 * </ul>
 *
 * @author Andy Clark, IBM
 */
public class XMLInputSource {

    /** Public identifier. */
    private String publicId_;

    /** System identifier. */
    private String systemId_;

    /** Base system identifier. */
    private String baseSystemId_;

    /** Byte stream. */
    private InputStream byteStream_;

    /** Character stream. */
    private Reader charStream_;

    /** Encoding. */
    private String encoding_;

    /**
     * Constructs an input source from just the public and system identifiers,
     * leaving resolution of the entity and opening of the input stream up to the
     * caller.
     *
     * @param publicId     The public identifier, if known.
     * @param systemId     The system identifier. This value should always be set,
     *                     if possible, and can be relative or absolute. If the
     *                     system identifier is relative, then the base system
     *                     identifier should be set.
     * @param baseSystemId The base system identifier. This value should always be
     *                     set to the fully expanded URI of the base system
     *                     identifier, if possible.
     */
    public XMLInputSource(final String publicId, final String systemId, final String baseSystemId) {
        publicId_ = publicId;
        systemId_ = systemId;
        baseSystemId_ = baseSystemId;
    }

    /**
     * Constructs an input source from a byte stream.
     *
     * @param publicId     The public identifier, if known.
     * @param systemId     The system identifier. This value should always be set,
     *                     if possible, and can be relative or absolute. If the
     *                     system identifier is relative, then the base system
     *                     identifier should be set.
     * @param baseSystemId The base system identifier. This value should always be
     *                     set to the fully expanded URI of the base system
     *                     identifier, if possible.
     * @param byteStream   The byte stream.
     * @param encoding     The encoding of the byte stream, if known.
     */
    public XMLInputSource(final String publicId, final String systemId, final String baseSystemId, final InputStream byteStream, final String encoding) {
        publicId_ = publicId;
        systemId_ = systemId;
        baseSystemId_ = baseSystemId;
        byteStream_ = byteStream;
        encoding_ = encoding;
    }

    /**
     * Constructs an input source from a character stream.
     *
     * @param publicId     The public identifier, if known.
     * @param systemId     The system identifier. This value should always be set,
     *                     if possible, and can be relative or absolute. If the
     *                     system identifier is relative, then the base system
     *                     identifier should be set.
     * @param baseSystemId The base system identifier. This value should always be
     *                     set to the fully expanded URI of the base system
     *                     identifier, if possible.
     * @param charStream   The character stream.
     * @param encoding     The original encoding of the byte stream used by the
     *                     reader, if known.
     */
    public XMLInputSource(final String publicId, final String systemId, final String baseSystemId, final Reader charStream, final String encoding) {
        publicId_ = publicId;
        systemId_ = systemId;
        baseSystemId_ = baseSystemId;
        charStream_ = charStream;
        encoding_ = encoding;
    }

    //
    // Public methods
    //

    /**
     * Sets the public identifier.
     *
     * @param publicId The new public identifier.
     */
    public void setPublicId(final String publicId) {
        publicId_ = publicId;
    }

    /** @return the public identifier. */
    public String getPublicId() {
        return publicId_;
    }

    /**
     * Sets the system identifier.
     *
     * @param systemId The new system identifier.
     */
    public void setSystemId(final String systemId) {
        systemId_ = systemId;
    }

    /** @return the system identifier. */
    public String getSystemId() {
        return systemId_;
    }

    /**
     * Sets the base system identifier.
     *
     * @param baseSystemId The new base system identifier.
     */
    public void setBaseSystemId(final String baseSystemId) {
        baseSystemId_ = baseSystemId;
    }

    /** @return the base system identifier. */
    public String getBaseSystemId() {
        return baseSystemId_;
    }

    /**
     * Sets the byte stream. If the byte stream is not already opened when this
     * object is instantiated, then the code that opens the stream should also set
     * the byte stream on this object. Also, if the encoding is auto-detected, then
     * the encoding should also be set on this object.
     *
     * @param byteStream The new byte stream.
     */
    public void setByteStream(final InputStream byteStream) {
        byteStream_ = byteStream;
    }

    /** @return the byte stream. */
    public InputStream getByteStream() {
        return byteStream_;
    }

    /**
     * Sets the character stream. If the character stream is not already opened when
     * this object is instantiated, then the code that opens the stream should also
     * set the character stream on this object. Also, the encoding of the byte
     * stream used by the reader should also be set on this object, if known.
     *
     * @param charStream The new character stream.
     *
     * @see #setEncoding(String)
     */
    public void setCharacterStream(final Reader charStream) {
        charStream_ = charStream;
    }

    /** @return the character stream. */
    public Reader getCharacterStream() {
        return charStream_;
    }

    /**
     * Sets the encoding of the stream.
     *
     * @param encoding The new encoding.
     */
    public void setEncoding(final String encoding) {
        encoding_ = encoding;
    }

    /** @return the encoding of the stream, or null if not known. */
    public String getEncoding() {
        return encoding_;
    }
}
