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

import java.util.Iterator;
import java.util.NoSuchElementException;

import net.sourceforge.htmlunit.xerces.xni.NamespaceContext;

/**
 * Namespace support for XML document handlers. This class doesn't perform any
 * error checking and assumes that all strings passed as arguments to methods
 * are unique symbols. The SymbolTable class can be used for this purpose.
 *
 * @author Andy Clark, IBM
 */
public class NamespaceSupport implements NamespaceContext {

    /**
     * Namespace binding information. This array is composed of a series of tuples
     * containing the namespace binding information: &lt;prefix, uri&gt;. The
     * default size can be set to anything as long as it is a power of 2 greater
     * than 1.
     *
     * @see #fNamespaceSize
     * @see #fContext
     */
    private String[] fNamespace = new String[16 * 2];

    /** The top of the namespace information array. */
    private int fNamespaceSize;

    // NOTE: The constructor depends on the initial context size
    // being at least 1. -Ac

    /**
     * Context indexes. This array contains indexes into the namespace information
     * array. The index at the current context is the start index of declared
     * namespace bindings and runs to the size of the namespace information array.
     *
     * @see #fNamespaceSize
     */
    private int[] fContext = new int[8];

    /** The current context. */
    private int fCurrentContext;

    private String[] fPrefixes = new String[16];

    /** Default constructor. */
    public NamespaceSupport() {
    } // <init>()



    //
    // Public methods
    //

    /**
     * @see net.sourceforge.htmlunit.xerces.xni.NamespaceContext#reset()
     */
    @Override
    public void reset() {

        // reset namespace and context info
        fNamespaceSize = 0;
        fCurrentContext = 0;
        fContext[fCurrentContext] = fNamespaceSize;

        ++fCurrentContext;

    } // reset(SymbolTable)

    /**
     * @see net.sourceforge.htmlunit.xerces.xni.NamespaceContext#pushContext()
     */
    @Override
    public void pushContext() {

        // extend the array, if necessary
        if (fCurrentContext + 1 == fContext.length) {
            int[] contextarray = new int[fContext.length * 2];
            System.arraycopy(fContext, 0, contextarray, 0, fContext.length);
            fContext = contextarray;
        }

        // push context
        fContext[++fCurrentContext] = fNamespaceSize;

    } // pushContext()

    /**
     * @see net.sourceforge.htmlunit.xerces.xni.NamespaceContext#popContext()
     */
    @Override
    public void popContext() {
        fNamespaceSize = fContext[fCurrentContext--];
    } // popContext()

    /**
     * @see net.sourceforge.htmlunit.xerces.xni.NamespaceContext#declarePrefix(String,
     *      String)
     */
    @Override
    public boolean declarePrefix(String prefix, String uri) {
        // see if prefix already exists in current context
        for (int i = fNamespaceSize; i > fContext[fCurrentContext]; i -= 2) {
            if (fNamespace[i - 2] == prefix) {
                // REVISIT: [Q] Should the new binding override the
                // previously declared binding or should it
                // it be ignored? -Ac
                // NOTE: The SAX2 "NamespaceSupport" helper allows
                // re-bindings with the new binding overwriting
                // the previous binding. -Ac
                fNamespace[i - 1] = uri;
                return true;
            }
        }

        // resize array, if needed
        if (fNamespaceSize == fNamespace.length) {
            String[] namespacearray = new String[fNamespaceSize * 2];
            System.arraycopy(fNamespace, 0, namespacearray, 0, fNamespaceSize);
            fNamespace = namespacearray;
        }

        // bind prefix to uri in current context
        fNamespace[fNamespaceSize++] = prefix;
        fNamespace[fNamespaceSize++] = uri;

        return true;

    } // declarePrefix(String,String):boolean

    /**
     * @see net.sourceforge.htmlunit.xerces.xni.NamespaceContext#getURI(String)
     */
    @Override
    public String getURI(String prefix) {

        // find prefix in current context
        for (int i = fNamespaceSize; i > 0; i -= 2) {
            if (fNamespace[i - 2] == prefix) {
                return fNamespace[i - 1];
            }
        }

        // prefix not found
        return null;

    } // getURI(String):String

    /**
     * @see net.sourceforge.htmlunit.xerces.xni.NamespaceContext#getDeclaredPrefixCount()
     */
    @Override
    public int getDeclaredPrefixCount() {
        return (fNamespaceSize - fContext[fCurrentContext]) / 2;
    } // getDeclaredPrefixCount():int

    /**
     * @see net.sourceforge.htmlunit.xerces.xni.NamespaceContext#getDeclaredPrefixAt(int)
     */
    @Override
    public String getDeclaredPrefixAt(int index) {
        return fNamespace[fContext[fCurrentContext] + index * 2];
    } // getDeclaredPrefixAt(int):String

    /**
     * Checks whether a binding or unbinding for the given prefix exists in the
     * context.
     *
     * @param prefix The prefix to look up.
     *
     * @return true if the given prefix exists in the context
     */
    public boolean containsPrefix(String prefix) {

        // find prefix in current context
        for (int i = fNamespaceSize; i > 0; i -= 2) {
            if (fNamespace[i - 2] == prefix) {
                return true;
            }
        }

        // prefix not found
        return false;
    }

    protected final class Prefixes implements Iterator<String> {
        private final String[] prefixes;
        private int counter = 0;
        private final int size;

        public Prefixes(String[] prefixes, int size) {
            this.prefixes = prefixes;
            this.size = size;
        }

        @Override
        public boolean hasNext() {
            return (counter < size);
        }

        @Override
        public String next() {
            if (counter < size) {
                return fPrefixes[counter++];
            }
            throw new NoSuchElementException("Illegal access to Namespace prefixes enumeration.");
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < size; i++) {
                buf.append(prefixes[i]);
                buf.append(' ');
            }

            return buf.toString();
        }

    }
}
