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

package org.htmlunit.cyberneko.xerces.xni;

/**
 * Location information.
 *
 * @author Andy Clark, IBM
 */
public interface XMLLocator {

    /** @return the public identifier. */
    String getPublicId();

    /** @return the literal system identifier. */
    String getLiteralSystemId();

    /** @return the base system identifier. */
    String getBaseSystemId();

    /** @return the expanded system identifier. */
    String getExpandedSystemId();

    /**
     * @return the line number, or <code>-1</code> if no line number is available.
     */
    int getLineNumber();

    /**
     * @return the column number, or <code>-1</code> if no column number is
     *         available.
     */
    int getColumnNumber();

    /**
     * @return the character offset, or <code>-1</code> if no character offset is
     *         available.
     */
    int getCharacterOffset();

    /**
     * @return the encoding of the current entity. Note that, for a given entity,
     *         this value can only be considered final once the encoding declaration
     *         has been read (or once it has been determined that there is no such
     *         declaration) since, no encoding having been specified on the
     *         XMLInputSource, the parser will make an initial "guess" which could
     *         be in error.
     */
    String getEncoding();

    /**
     * @return the XML version of the current entity. This will normally be the
     *         value from the XML or text declaration or defaulted by the parser.
     *         Note that that this value may be different than the version of the
     *         processing rules applied to the current entity. For instance, an XML
     *         1.1 document may refer to XML 1.0 entities. In such a case the rules
     *         of XML 1.1 are applied to the entire document. Also note that, for a
     *         given entity, this value can only be considered final once the XML or
     *         text declaration has been read or once it has been determined that
     *         there is no such declaration.
     */
    String getXMLVersion();
}
