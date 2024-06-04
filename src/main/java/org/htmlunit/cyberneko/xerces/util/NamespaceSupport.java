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

import java.util.Objects;

import org.htmlunit.cyberneko.xerces.xni.NamespaceContext;

/**
 * Namespace support for XML document handlers. This class doesn't perform any
 * error checking and assumes that all strings passed as arguments to methods
 * are unique symbols.
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
     * @see #fNamespaceSize_
     * @see #fContext_
     */
    private String[] fNamespace_ = new String[16 * 2];

    /** The top of the namespace information array. */
    private int fNamespaceSize_;

    // NOTE: The constructor depends on the initial context size
    // being at least 1. -Ac

    /**
     * Context indexes. This array contains indexes into the namespace information
     * array. The index at the current context is the start index of declared
     * namespace bindings and runs to the size of the namespace information array.
     *
     * @see #fNamespaceSize_
     */
    private int[] fContext_ = new int[8];

    /** The current context. */
    private int fCurrentContext_;

    /** Default constructor. */
    public NamespaceSupport() {
    }

    /**
     * @see org.htmlunit.cyberneko.xerces.xni.NamespaceContext#reset()
     */
    @Override
    public void reset() {

        // reset namespace and context info
        fNamespaceSize_ = 0;
        fCurrentContext_ = 0;
        fContext_[fCurrentContext_] = fNamespaceSize_;

        ++fCurrentContext_;
    }

    /**
     * @see org.htmlunit.cyberneko.xerces.xni.NamespaceContext#pushContext()
     */
    @Override
    public void pushContext() {

        // extend the array, if necessary
        if (fCurrentContext_ + 1 == fContext_.length) {
            final int[] contextarray = new int[fContext_.length * 2];
            System.arraycopy(fContext_, 0, contextarray, 0, fContext_.length);
            fContext_ = contextarray;
        }

        // push context
        fContext_[++fCurrentContext_] = fNamespaceSize_;
    }

    /**
     * @see org.htmlunit.cyberneko.xerces.xni.NamespaceContext#popContext()
     */
    @Override
    public void popContext() {
        fNamespaceSize_ = fContext_[fCurrentContext_--];
    }

    /**
     * @see org.htmlunit.cyberneko.xerces.xni.NamespaceContext#declarePrefix(String,
     *      String)
     */
    @Override
    public boolean declarePrefix(final String prefix, final String uri) {
        // see if prefix already exists in current context
        for (int i = fNamespaceSize_; i > fContext_[fCurrentContext_]; i -= 2) {
            if (Objects.equals(prefix, fNamespace_[i - 2])) {
                // REVISIT: [Q] Should the new binding override the
                // previously declared binding or should it
                // it be ignored? -Ac
                // NOTE: The SAX2 "NamespaceSupport" helper allows
                // re-bindings with the new binding overwriting
                // the previous binding. -Ac
                fNamespace_[i - 1] = uri;
                return true;
            }
        }

        // resize array, if needed
        if (fNamespaceSize_ == fNamespace_.length) {
            final String[] namespacearray = new String[fNamespaceSize_ * 2];
            System.arraycopy(fNamespace_, 0, namespacearray, 0, fNamespaceSize_);
            fNamespace_ = namespacearray;
        }

        // bind prefix to uri in current context
        fNamespace_[fNamespaceSize_++] = prefix;
        fNamespace_[fNamespaceSize_++] = uri;

        return true;
    }

    /**
     * @see org.htmlunit.cyberneko.xerces.xni.NamespaceContext#getURI(String)
     */
    @Override
    public String getURI(final String prefix) {

        // find prefix in current context
        for (int i = fNamespaceSize_; i > 0; i -= 2) {
            if (Objects.equals(prefix, fNamespace_[i - 2])) {
                return fNamespace_[i - 1];
            }
        }

        // prefix not found
        return null;
    }

    /**
     * @see org.htmlunit.cyberneko.xerces.xni.NamespaceContext#getDeclaredPrefixCount()
     */
    @Override
    public int getDeclaredPrefixCount() {
        return (fNamespaceSize_ - fContext_[fCurrentContext_]) / 2;
    }

    /**
     * @see org.htmlunit.cyberneko.xerces.xni.NamespaceContext#getDeclaredPrefixAt(int)
     */
    @Override
    public String getDeclaredPrefixAt(final int index) {
        return fNamespace_[fContext_[fCurrentContext_] + index * 2];
    }
}
