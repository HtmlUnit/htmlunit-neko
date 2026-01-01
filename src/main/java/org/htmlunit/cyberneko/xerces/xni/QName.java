/*
 * Copyright (c) 2017-2025 Ronald Brill
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
 * A structure that holds the components of an XML Namespaces qualified name.
 * <p>
 * To be used correctly, the strings must be identical references for equal
 * strings.
 *
 * @author Andy Clark, IBM
 */
public class QName implements Cloneable {

    /**
     * The qname prefix. For example, the prefix for the qname "a:foo" is "a".
     */
    private String prefix_;

    /**
     * The qname localpart. For example, the localpart for the qname "a:foo" is
     * "foo".
     */
    private String localpart_;

    /**
     * The qname rawname. For example, the rawname for the qname "a:foo" is "a:foo".
     */
    private String rawname_;

    /**
     * The URI to which the qname prefix is bound. This binding must be performed by
     * a XML Namespaces aware processor.
     */
    private String uri_;

    /** Default constructor. */
    public QName() {
    }

    // Constructs a QName with the specified values.
    public QName(final String prefix, final String localpart, final String rawname, final String uri) {
        setValues(prefix, localpart, rawname, uri);
    }

    // Constructs a copy of the specified QName.
    public QName(final QName qname) {
        setValues(qname);
    }

    public String getPrefix() {
        return prefix_;
    }

    public void setPrefix(final String prefix) {
        prefix_ = prefix;
    }

    public String getLocalpart() {
        return localpart_;
    }

    public String getRawname() {
        return rawname_;
    }

    public void setRawname(final String rawname) {
        rawname_ = rawname;
    }

    public String getUri() {
        return uri_;
    }

    public void setUri(final String uri) {
        uri_ = uri;
    }

    /**
     * Convenience method to set the values of the qname components.
     *
     * @param qname The qualified name to be copied.
     */
    public void setValues(final QName qname) {
        prefix_ = qname.prefix_;
        localpart_ = qname.localpart_;
        rawname_ = qname.rawname_;
        uri_ = qname.uri_;
    }

    /**
     * Convenience method to set the values of the qname components.
     *
     * @param prefix    The qname prefix. (e.g. "a")
     * @param localpart The qname localpart. (e.g. "foo")
     * @param rawname   The qname rawname. (e.g. "a:foo")
     * @param uri       The URI binding. (e.g. "http://foo.com/mybinding")
     */
    public void setValues(final String prefix, final String localpart, final String rawname, final String uri) {
        prefix_ = prefix;
        localpart_ = localpart;
        rawname_ = rawname;
        uri_ = uri;
    }

    // Splits a qualified name.
    public QName splitQName() {
        final int index = rawname_.indexOf(':');
        if (index != -1) {
            prefix_ = rawname_.substring(0, index);
            localpart_  = rawname_.substring(index + 1);
        }
        return this;
    }

    @Override
    public Object clone() {
        return new QName(this);
    }

    @Override
    public int hashCode() {
        if (uri_ != null) {
            return uri_.hashCode() + ((localpart_ != null) ? localpart_.hashCode() : 0);
        }
        return (rawname_ != null) ? rawname_.hashCode() : 0;
    }

    @Override
    public boolean equals(final Object object) {
        if (object instanceof QName qname) {
            if (qname.uri_ != null) {
                return qname.uri_.equals(uri_) && localpart_ == qname.localpart_;
            }
            else if (uri_ == null) {
                return rawname_ == qname.rawname_;
            }
            // fall through and return not equal
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder();
        boolean comma = false;
        if (prefix_ != null) {
            str.append("prefix=\"").append(prefix_).append('"');
            comma = true;
        }
        if (localpart_ != null) {
            if (comma) {
                str.append(',');
            }
            str.append("localpart=\"").append(localpart_).append('"');
            comma = true;
        }
        if (rawname_ != null) {
            if (comma) {
                str.append(',');
            }
            str.append("rawname=\"").append(rawname_).append('"');
            comma = true;
        }
        if (uri_ != null) {
            if (comma) {
                str.append(',');
            }
            str.append("uri=\"").append(uri_).append('"');
        }
        return str.toString();
    }
}
