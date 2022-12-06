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

import net.sourceforge.htmlunit.xerces.xni.XMLResourceIdentifier;

/**
 * The XMLResourceIdentifierImpl class is an implementation of the
 * XMLResourceIdentifier interface which defines the location identity
 * of a resource.
 *
 * @author Andy Clark
 */
public class XMLResourceIdentifierImpl
    implements XMLResourceIdentifier {

    /** The public identifier. */
    protected String fPublicId;

    /** The literal system identifier. */
    protected String fLiteralSystemId;

    /** The base system identifier. */
    protected String fBaseSystemId;

    /** The expanded system identifier. */
    protected String fExpandedSystemId;

    /** The namespace of the resource. */
    protected String fNamespace;

    /** Constructs an empty resource identifier. */
    public XMLResourceIdentifierImpl() {}

    /**
     * Constructs a resource identifier.
     *
     * @param publicId The public identifier.
     * @param literalSystemId The literal system identifier.
     * @param baseSystemId The base system identifier.
     * @param expandedSystemId The expanded system identifier.
     */
    public XMLResourceIdentifierImpl(String publicId,
                                     String literalSystemId, String baseSystemId,
                                     String expandedSystemId) {
        setValues(publicId, literalSystemId, baseSystemId,
                  expandedSystemId, null);
    }

    // Sets the values of the resource identifier.
    public void setValues(String publicId, String literalSystemId,
                          String baseSystemId, String expandedSystemId) {
        setValues(publicId, literalSystemId, baseSystemId,
                  expandedSystemId, null);
    }

    // Sets the values of the resource identifier.
    public void setValues(String publicId, String literalSystemId,
                          String baseSystemId, String expandedSystemId,
                          String namespace) {
        fPublicId = publicId;
        fLiteralSystemId = literalSystemId;
        fBaseSystemId = baseSystemId;
        fExpandedSystemId = expandedSystemId;
        fNamespace = namespace;
    }

    /** Clears the values. */
    public void clear() {
        fPublicId = null;
        fLiteralSystemId = null;
        fBaseSystemId = null;
        fExpandedSystemId = null;
        fNamespace = null;
    } // clear()

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPublicId(String publicId) {
        fPublicId = publicId;
    }

    @Override
    public void setLiteralSystemId(String literalSystemId) {
        fLiteralSystemId = literalSystemId;
    }

    @Override
    public void setBaseSystemId(String baseSystemId) {
        fBaseSystemId = baseSystemId;
    }

    @Override
    public void setExpandedSystemId(String expandedSystemId) {
        fExpandedSystemId = expandedSystemId;
    }

    @Override
    public void setNamespace(String namespace) {
        fNamespace = namespace;
    }

    @Override
    public String getPublicId() {
        return fPublicId;
    }

    @Override
    public String getLiteralSystemId() {
        return fLiteralSystemId;
    }

    @Override
    public String getBaseSystemId() {
        return fBaseSystemId;
    }

    @Override
    public String getExpandedSystemId() {
        return fExpandedSystemId;
    }

    @Override
    public String getNamespace() {
        return fNamespace;
    }

    @Override
    public int hashCode() {
        int code = 0;
        if (fPublicId != null) {
            code += fPublicId.hashCode();
        }
        if (fLiteralSystemId != null) {
            code += fLiteralSystemId.hashCode();
        }
        if (fBaseSystemId != null) {
            code += fBaseSystemId.hashCode();
        }
        if (fExpandedSystemId != null) {
            code += fExpandedSystemId.hashCode();
        }
        if (fNamespace != null) {
            code += fNamespace.hashCode();
        }
        return code;
    } // hashCode():int

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        if (fPublicId != null) {
            str.append(fPublicId);
        }
        str.append(':');
        if (fLiteralSystemId != null) {
            str.append(fLiteralSystemId);
        }
        str.append(':');
        if (fBaseSystemId != null) {
            str.append(fBaseSystemId);
        }
        str.append(':');
        if (fExpandedSystemId != null) {
            str.append(fExpandedSystemId);
        }
        str.append(':');
        if (fNamespace != null) {
            str.append(fNamespace);
        }
        return str.toString();
    }
}
