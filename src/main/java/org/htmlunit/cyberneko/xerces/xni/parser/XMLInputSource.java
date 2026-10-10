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
package org.htmlunit.cyberneko.xerces.xni.parser;

import java.io.InputStream;
import java.io.Reader;

/**
 * Represents an input source for an XML document. The basic properties of an input source are:
 * <ul>
 *   <li>Public identifier</li>
 *   <li>System identifier</li>
 *   <li>Base system identifier</li>
 *   <li>Byte stream or character stream</li>
 *   <li>Encoding</li>
 * </ul>
 *
 * @author Andy Clark, IBM
 * @author Ronald Brill
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
     * leaving resolution of the entity and opening of the input stream up to the caller.
     *
     * @param publicId     the public identifier, if known
     * @param systemId     the system identifier (can be relative or absolute)
     * @param baseSystemId the base system identifier as a fully expanded URI, if known
     */
    public XMLInputSource(final String publicId, final String systemId, final String baseSystemId) {
        publicId_ = publicId;
        systemId_ = systemId;
        baseSystemId_ = baseSystemId;
    }

    /**
     * Constructs an input source from a byte stream.
     *
     * @param publicId     the public identifier, if known
     * @param systemId     the system identifier (can be relative or absolute)
     * @param baseSystemId the base system identifier as a fully expanded URI, if known
     * @param byteStream   the byte stream
     * @param encoding     the encoding of the byte stream, if known
     */
    public XMLInputSource(final String publicId, final String systemId, final String baseSystemId,
                          final InputStream byteStream, final String encoding) {
        publicId_ = publicId;
        systemId_ = systemId;
        baseSystemId_ = baseSystemId;
        byteStream_ = byteStream;
        encoding_ = encoding;
    }

    /**
     * Constructs an input source from a character stream.
     *
     * @param publicId     the public identifier, if known
     * @param systemId     the system identifier (can be relative or absolute)
     * @param baseSystemId the base system identifier as a fully expanded URI, if known
     * @param charStream   the character stream
     * @param encoding     the original encoding of the byte stream used by the reader, if known
     */
    public XMLInputSource(final String publicId, final String systemId, final String baseSystemId,
                          final Reader charStream, final String encoding) {
        publicId_ = publicId;
        systemId_ = systemId;
        baseSystemId_ = baseSystemId;
        charStream_ = charStream;
        encoding_ = encoding;
    }

    /**
     * Sets the public identifier.
     *
     * @param publicId the new public identifier
     */
    public void setPublicId(final String publicId) {
        publicId_ = publicId;
    }

    /**
     * Returns the public identifier.
     *
     * @return the public identifier, or {@code null} if not specified
     */
    public String getPublicId() {
        return publicId_;
    }

    /**
     * Sets the system identifier.
     *
     * @param systemId the new system identifier
     */
    public void setSystemId(final String systemId) {
        systemId_ = systemId;
    }

    /**
     * Returns the system identifier.
     *
     * @return the system identifier, or {@code null} if not specified
     */
    public String getSystemId() {
        return systemId_;
    }

    /**
     * Sets the base system identifier.
     *
     * @param baseSystemId the new base system identifier
     */
    public void setBaseSystemId(final String baseSystemId) {
        baseSystemId_ = baseSystemId;
    }

    /**
     * Returns the base system identifier.
     *
     * @return the base system identifier, or {@code null} if not specified
     */
    public String getBaseSystemId() {
        return baseSystemId_;
    }

    /**
     * Sets the byte stream. If the byte stream is not already opened when this
     * object is instantiated, the code opening the stream should set the byte stream here.
     *
     * @param byteStream the new byte stream
     */
    public void setByteStream(final InputStream byteStream) {
        byteStream_ = byteStream;
    }

    /**
     * Returns the byte stream.
     *
     * @return the byte stream, or {@code null} if none is set
     */
    public InputStream getByteStream() {
        return byteStream_;
    }

    /**
     * Sets the character stream. If the character stream is not already opened when
     * this object is instantiated, the code opening the stream should set the character stream here.
     *
     * @param charStream the new character stream
     * @see #setEncoding(String)
     */
    public void setCharacterStream(final Reader charStream) {
        charStream_ = charStream;
    }

    /**
     * Returns the character stream.
     *
     * @return the character stream, or {@code null} if none is set
     */
    public Reader getCharacterStream() {
        return charStream_;
    }

    /**
     * Sets the encoding of the stream.
     *
     * @param encoding the new encoding
     */
    public void setEncoding(final String encoding) {
        encoding_ = encoding;
    }

    /**
     * Returns the encoding of the stream.
     *
     * @return the encoding, or {@code null} if not known
     */
    public String getEncoding() {
        return encoding_;
    }
}
