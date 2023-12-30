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
package org.htmlunit.cyberneko.xerces.xni;

/**
 * A structure that holds the components of an XML Namespaces qualified name.
 * <p>
 * To be used correctly, the strings must be identical references for equal
 * strings. Within the parser, these values are considered symbols and should
 * always be retrieved from the <code>SymbolTable</code>.
 *
 * @see <a href=
 *      "../../../../../xerces2/org/apache/xerces/util/SymbolTable.html">org.htmlunit.cyberneko.xerces.util.SymbolTable</a>
 *
 * @author Andy Clark, IBM
 */
public class QName implements Cloneable {

    /**
     * The qname prefix. For example, the prefix for the qname "a:foo" is "a".
     */
    public String prefix;

    /**
     * The qname localpart. For example, the localpart for the qname "a:foo" is
     * "foo".
     */
    public String localpart;

    /**
     * The qname rawname. For example, the rawname for the qname "a:foo" is "a:foo".
     */
    public String rawname;

    /**
     * The URI to which the qname prefix is bound. This binding must be performed by
     * a XML Namespaces aware processor.
     */
    public String uri;

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

    /**
     * Convenience method to set the values of the qname components.
     *
     * @param qname The qualified name to be copied.
     */
    public void setValues(final QName qname) {
        prefix = qname.prefix;
        localpart = qname.localpart;
        rawname = qname.rawname;
        uri = qname.uri;
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
        this.prefix = prefix;
        this.localpart = localpart;
        this.rawname = rawname;
        this.uri = uri;
    }

    // Splits a qualified name.
    public QName splitQName() {
        final int index = this.rawname.indexOf(':');
        if (index != -1) {
            this.prefix = this.rawname.substring(0, index);
            this.localpart  = this.rawname.substring(index + 1);
        }
        return this;
    }

    @Override
    public Object clone() {
        return new QName(this);
    }

    @Override
    public int hashCode() {
        if (uri != null) {
            return uri.hashCode() + ((localpart != null) ? localpart.hashCode() : 0);
        }
        return (rawname != null) ? rawname.hashCode() : 0;
    }

    @Override
    public boolean equals(final Object object) {
        if (object instanceof QName) {
            final QName qname = (QName) object;
            if (qname.uri != null) {
                return qname.uri.equals(uri) && localpart == qname.localpart;
            }
            else if (uri == null) {
                return rawname == qname.rawname;
            }
            // fall through and return not equal
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder();
        boolean comma = false;
        if (prefix != null) {
            str.append("prefix=\"").append(prefix).append('"');
            comma = true;
        }
        if (localpart != null) {
            if (comma) {
                str.append(',');
            }
            str.append("localpart=\"").append(localpart).append('"');
            comma = true;
        }
        if (rawname != null) {
            if (comma) {
                str.append(',');
            }
            str.append("rawname=\"").append(rawname).append('"');
            comma = true;
        }
        if (uri != null) {
            if (comma) {
                str.append(',');
            }
            str.append("uri=\"").append(uri).append('"');
        }
        return str.toString();
    }
}
