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

package net.sourceforge.htmlunit.xerces.dom;

import org.w3c.dom.DOMError;
import org.w3c.dom.DOMLocator;

import net.sourceforge.htmlunit.xerces.xni.parser.XMLParseException;

/**
 * <code>DOMErrorImpl</code> is an implementation that describes an error.
 * <strong>Note:</strong> The error object that describes the error might be
 * reused by Xerces implementation, across multiple calls to the handleEvent
 * method on DOMErrorHandler interface.
 *
 * <p>
 * See also the
 * <a href='http://www.w3.org/TR/2001/WD-DOM-Level-3-Core-20010913'>Document
 * Object Model (DOM) Level 3 Core Specification</a>.
 * <p>
 *
 * @author Gopal Sharma, SUN Microsystems Inc.
 * @author Elena Litani, IBM
 */

// REVISIT: the implementation of ErrorReporter.
//          we probably should not pass XMLParseException
//

public class DOMErrorImpl implements DOMError {

    private short fSeverity = DOMError.SEVERITY_WARNING;
    private final String fMessage = null;
    private DOMLocatorImpl fLocator = new DOMLocatorImpl();
    private Exception fException = null;
    private String fType;
    private Object fRelatedData;

    /** Default constructor. */
    public DOMErrorImpl() {
    }

    /**
     * Extracts information from XMLParserException
     *
     * @param severity  the severity
     * @param exception the exception
     */
    public DOMErrorImpl(short severity, XMLParseException exception) {
        fSeverity = severity;
        fException = exception;
        fLocator = createDOMLocator(exception);
    }

    /**
     * {@inheritDoc}
     *
     * The severity of the error, either <code>SEVERITY_WARNING</code>,
     * <code>SEVERITY_ERROR</code>, or <code>SEVERITY_FATAL_ERROR</code>.
     */
    @Override
    public short getSeverity() {
        return fSeverity;
    }

    /**
     * {@inheritDoc}
     *
     * An implementation specific string describing the error that occured.
     */
    @Override
    public String getMessage() {
        return fMessage;
    }

    /**
     * {@inheritDoc}
     *
     * The location of the error.
     */
    @Override
    public DOMLocator getLocation() {
        return fLocator;
    }

    // method to get the DOMLocator Object
    private DOMLocatorImpl createDOMLocator(XMLParseException exception) {
        // assuming DOMLocator wants the *expanded*, not the literal, URI of the doc...
        // - neilg
        return new DOMLocatorImpl(exception.getLineNumber(), exception.getColumnNumber(),
                exception.getCharacterOffset(), exception.getExpandedSystemId());
    }

    /**
     * {@inheritDoc}
     *
     * The related platform dependent exception if any.exception is a reserved word,
     * we need to rename it.Change to "relatedException". (F2F 26 Sep 2001)
     */
    @Override
    public Object getRelatedException() {
        return fException;
    }

    public void reset() {
        fSeverity = DOMError.SEVERITY_WARNING;
        fException = null;
    }

    @Override
    public String getType() {
        return fType;
    }

    @Override
    public Object getRelatedData() {
        return fRelatedData;
    }
}
