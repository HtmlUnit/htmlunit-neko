/*
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
package org.htmlunit.cyberneko.xerces.dom;

import org.w3c.dom.DOMLocator;
import org.w3c.dom.Node;

/**
 * <code>DOMLocatorImpl</code> is an implementaion that describes a location
 * (e.g. where an error occured).
 * <p>
 * See also the
 * <a href='http://www.w3.org/TR/2001/WD-DOM-Level-3-Core-20010913'>Document
 * Object Model (DOM) Level 3 Core Specification</a>.
 * <p>
 *
 * @author Gopal Sharma, SUN Microsystems Inc.
 */

public class DOMLocatorImpl implements DOMLocator {

    //
    // Data
    //

    /**
     * The column number where the error occured, or -1 if there is no column number
     * available.
     */
    public int fColumnNumber = -1;

    /**
     * The line number where the error occured, or -1 if there is no line number
     * available.
     */
    public int fLineNumber = -1;

    /** related data node */
    public final Node fRelatedNode = null;

    /**
     * The URI where the error occured, or null if there is no URI available.
     */
    public String fUri = null;

    /**
     * The byte offset into the input source this locator is pointing to or -1 if
     * there is no byte offset available
     */
    public final int fByteOffset = -1;

    /**
     * The UTF-16, as defined in [Unicode] and Amendment 1 of [ISO/IEC 10646],
     * offset into the input source this locator is pointing to or -1 if there is no
     * UTF-16 offset available.
     */
    public int fUtf16Offset = -1;

    //
    // Constructors
    //

    public DOMLocatorImpl() {
    }

    public DOMLocatorImpl(final int lineNumber, final int columnNumber, final int utf16Offset, final String uri) {
        fLineNumber = lineNumber;
        fColumnNumber = columnNumber;
        fUri = uri;
        fUtf16Offset = utf16Offset;
    }

    /**
     * The line number where the error occured, or -1 if there is no line number
     * available.
     */
    @Override
    public int getLineNumber() {
        return fLineNumber;
    }

    /**
     * The column number where the error occured, or -1 if there is no column number
     * available.
     */
    @Override
    public int getColumnNumber() {
        return fColumnNumber;
    }

    /**
     * The URI where the error occured, or null if there is no URI available.
     */
    @Override
    public String getUri() {
        return fUri;
    }

    @Override
    public Node getRelatedNode() {
        return fRelatedNode;
    }

    /**
     * The byte offset into the input source this locator is pointing to or -1 if
     * there is no byte offset available
     */
    @Override
    public int getByteOffset() {
        return fByteOffset;
    }

    /**
     * The UTF-16, as defined in [Unicode] and Amendment 1 of [ISO/IEC 10646],
     * offset into the input source this locator is pointing to or -1 if there is no
     * UTF-16 offset available.
     */
    @Override
    public int getUtf16Offset() {
        return fUtf16Offset;
    }

}
