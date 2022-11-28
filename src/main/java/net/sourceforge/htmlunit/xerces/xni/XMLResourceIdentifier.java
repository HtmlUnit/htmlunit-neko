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

package net.sourceforge.htmlunit.xerces.xni;

/**
 * <p> This represents the basic physical description of the location of any
 * XML resource (a Schema grammar, a DTD, a general entity etc.) </p>
 *
 * @author Neil Graham, IBM
 * @version $Id$
 */

public interface XMLResourceIdentifier {
    
    /** Sets the public identifier. */
    void setPublicId(String publicId);

    /** Returns the public identifier. */
    String getPublicId();
    
    /** Sets the expanded system identifier. */
    void setExpandedSystemId(String systemId);
    
    /** Returns the expanded system identifier. */
    String getExpandedSystemId();
    
    /** Sets the literal system identifier. */
    void setLiteralSystemId(String systemId);
     
    /** Returns the literal system identifier. */
    String getLiteralSystemId();
    
    /** Sets the base URI against which the literal SystemId is to be 
        resolved.*/
    void setBaseSystemId(String systemId);
    
    /** <p> Returns the base URI against which the literal SystemId is to be 
        resolved. </p> */
    String getBaseSystemId();

    /** Sets the namespace of the resource. */
    void setNamespace(String namespace);
     
    /** Returns the namespace of the resource. */
    String getNamespace();
    
} // XMLResourceIdentifier
