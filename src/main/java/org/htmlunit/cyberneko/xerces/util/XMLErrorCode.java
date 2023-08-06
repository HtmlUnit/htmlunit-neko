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
package org.htmlunit.cyberneko.xerces.util;

/**
 * <p>
 * A structure that represents an error code, characterized by a domain and a
 * message key.
 * </p>
 *
 * @author Naela Nissar, IBM
 */
final class XMLErrorCode {

    /** error domain */
    private String fDomain_;

    /** message key */
    private String fKey_;

    /**
     * <p>
     * Constructs an XMLErrorCode with the given domain and key.
     * </p>
     *
     * @param domain The error domain.
     * @param key    The key of the error message.
     */
    XMLErrorCode(final String domain, final String key) {
        fDomain_ = domain;
        fKey_ = key;
    }

    /**
     * <p>
     * Convenience method to set the values of an XMLErrorCode.
     * </p>
     *
     * @param domain The error domain.
     * @param key    The key of the error message.
     */
    public void setValues(final String domain, final String key) {
        fDomain_ = domain;
        fKey_ = key;
    }

    /**
     * <p>
     * Indicates whether some other object is equal to this XMLErrorCode.
     * </p>
     *
     * @param obj the object with which to compare.
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof XMLErrorCode)) {
            return false;
        }
        final XMLErrorCode err = (XMLErrorCode) obj;
        return fDomain_.equals(err.fDomain_) && fKey_.equals(err.fKey_);
    }

    /**
     * <p>
     * Returns a hash code value for this XMLErrorCode.
     * </p>
     *
     * @return a hash code value for this XMLErrorCode.
     */
    @Override
    public int hashCode() {
        return fDomain_.hashCode() + fKey_.hashCode();
    }
}
