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

/**
 * Represents an interface to query namespace information.
 * <p>
 * The prefix and namespace must be identical references for equal strings, thus
 * each string should be internalized ({@link String#intern()}).
 * </p>
 *
 * @author Andy Clark, IBM
 * @author Ronald Brill
 */
public interface NamespaceContext {

    /**
     * The XML Namespace ("http://www.w3.org/XML/1998/namespace"). This is the
     * Namespace URI that is automatically mapped to the "xml" prefix.
     */
    String XML_URI = "http://www.w3.org/XML/1998/namespace";

    /**
     * XML Information Set REC namespace attributes (including those named
     * {@code xmlns}, whose [prefix] property has no value) have a namespace URI of
     * "http://www.w3.org/2000/xmlns/".
     */
    String XMLNS_URI = "http://www.w3.org/2000/xmlns/";

    /**
     * Starts a new Namespace context.
     * <p>
     * A new context should be pushed at the beginning of each XML element: the new
     * context will automatically inherit the declarations of its parent context,
     * but it will also keep track of which declarations were made within this
     * context.
     * </p>
     *
     * @see #popContext
     */
    void pushContext();

    /**
     * Reverts to the previous Namespace context.
     * <p>
     * The context should be popped at the end of each XML element. After popping
     * the context, all Namespace prefix mappings that were previously in force are
     * restored.
     * </p>
     * <p>
     * Users must not attempt to declare additional Namespace prefixes after popping
     * a context unless another context is pushed first.
     * </p>
     *
     * @see #pushContext
     */
    void popContext();

    /**
     * Declares a Namespace prefix.
     * <p>
     * This method declares a prefix in the current Namespace context; the prefix
     * will remain in force until this context is popped, unless it is shadowed in a
     * descendant context.
     * </p>
     * <p>
     * Note that to declare a default Namespace, use the empty string. The prefixes
     * "xml" and "xmlns" cannot be rebound.
     * </p>
     *
     * @param prefix the prefix to declare, or {@code null} or empty string for the default namespace
     * @param uri    the Namespace URI to associate with the prefix
     * @return {@code true} if the prefix was legal, {@code false} otherwise
     * @see #getURI
     * @see #getDeclaredPrefixAt
     */
    boolean declarePrefix(String prefix, String uri);

    /**
     * Looks up a prefix and gets the currently-mapped Namespace URI.
     * <p>
     * This method looks up the prefix in the current context. If no mapping is
     * found, this method will continue lookup in the parent context(s). Use the
     * empty string ({@code ""}) for the default Namespace.
     * </p>
     *
     * @param prefix the prefix to look up
     * @return the associated Namespace URI, or {@code null} if the prefix is undeclared
     */
    String getURI(String prefix);

    /**
     * Returns a count of locally declared prefixes, including the default prefix if bound.
     *
     * @return the count of locally declared prefixes
     */
    int getDeclaredPrefixCount();

    /**
     * Returns the prefix at the specified index in the current context.
     *
     * @param index the zero-based index position of the declared prefix
     * @return the prefix at the specified index, or {@code null} if out of bounds
     */
    String getDeclaredPrefixAt(int index);

    /**
     * Resets this Namespace support object for reuse.
     * <p>
     * It is necessary to invoke this method before reusing the Namespace support
     * object for a new session.
     * </p>
     * <p>
     * Note that implementations of this method need to ensure that the declarations
     * of the prefixes "xmlns" and "xml" remain available.
     * </p>
     */
    void reset();
}
