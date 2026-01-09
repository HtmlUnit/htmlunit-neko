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
package org.htmlunit.cyberneko.xerces.util;

import org.htmlunit.cyberneko.xerces.xni.XNIException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLErrorHandler;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLParseException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class wraps a SAX error handler in an XNI error handler.
 *
 * @see ErrorHandler
 *
 * @author Andy Clark, IBM
 */
public class ErrorHandlerWrapper implements XMLErrorHandler {

    /** The SAX error handler. */
    private ErrorHandler fErrorHandler_;

    // Wraps the specified SAX error handler.
    public ErrorHandlerWrapper(final ErrorHandler errorHandler) {
        setErrorHandler(errorHandler);
    }

    // Sets the SAX error handler.
    public void setErrorHandler(final ErrorHandler errorHandler) {
        fErrorHandler_ = errorHandler;
    }

    /**
     * @return the SAX error handler.
     */
    public ErrorHandler getErrorHandler() {
        return fErrorHandler_;
    }

    /**
     * Reports a warning. Warnings are non-fatal and can be safely ignored by most
     * applications.
     *
     * @param domain    The domain of the warning. The domain can be any string but
     *                  is suggested to be a valid URI. The domain can be used to
     *                  conveniently specify a website location of the relevant
     *                  specification or document pertaining to this warning.
     * @param key       The warning key. This key can be any string and is
     *                  implementation dependent.
     * @param exception Exception.
     *
     * @throws XNIException Thrown to signal that the parser should stop parsing the
     *                      document.
     */
    @Override
    public void warning(final String domain, final String key, final XMLParseException exception) throws XNIException {

        if (fErrorHandler_ != null) {
            final SAXParseException saxException = createSAXParseException(exception);

            try {
                fErrorHandler_.warning(saxException);
            }
            catch (final SAXParseException e) {
                throw createXMLParseException(e);
            }
            catch (final SAXException e) {
                throw createXNIException(e);
            }
        }

    }

    /**
     * Reports an error. Errors are non-fatal and usually signify that the document
     * is invalid with respect to its grammar(s).
     *
     * @param domain    The domain of the error. The domain can be any string but is
     *                  suggested to be a valid URI. The domain can be used to
     *                  conveniently specify a website location of the relevant
     *                  specification or document pertaining to this error.
     * @param key       The error key. This key can be any string and is
     *                  implementation dependent.
     * @param exception Exception.
     *
     * @throws XNIException Thrown to signal that the parser should stop parsing the
     *                      document.
     */
    @Override
    public void error(final String domain, final String key, final XMLParseException exception) throws XNIException {

        if (fErrorHandler_ != null) {
            final SAXParseException saxException = createSAXParseException(exception);

            try {
                fErrorHandler_.error(saxException);
            }
            catch (final SAXParseException e) {
                throw createXMLParseException(e);
            }
            catch (final SAXException e) {
                throw createXNIException(e);
            }
        }

    }

    /**
     * Report a fatal error. Fatal errors usually occur when the document is not
     * well-formed and signifies that the parser cannot continue normal operation.
     * <p>
     * <strong>Note:</strong> The error handler should <em>always</em> throw an
     * <code>XNIException</code> from this method. This exception can either be the
     * same exception that is passed as a parameter to the method or a new XNI
     * exception object. If the registered error handler fails to throw an
     * exception, the continuing operation of the parser is undetermined.
     *
     * @param domain    The domain of the fatal error. The domain can be any string
     *                  but is suggested to be a valid URI. The domain can be used
     *                  to conveniently specify a website location of the relevant
     *                  specification or document pertaining to this fatal error.
     * @param key       The fatal error key. This key can be any string and is
     *                  implementation dependent.
     * @param exception Exception.
     *
     * @throws XNIException Thrown to signal that the parser should stop parsing the
     *                      document.
     */
    @Override
    public void fatalError(final String domain, final String key, final XMLParseException exception)
                    throws XNIException {

        if (fErrorHandler_ != null) {
            final SAXParseException saxException = createSAXParseException(exception);

            try {
                fErrorHandler_.fatalError(saxException);
            }
            catch (final SAXParseException e) {
                throw createXMLParseException(e);
            }
            catch (final SAXException e) {
                throw createXNIException(e);
            }
        }

    }

    // Creates a SAXParseException from an XMLParseException.
    protected static SAXParseException createSAXParseException(final XMLParseException exception) {
        return new SAXParseException(exception.getMessage(), exception.getPublicId(), exception.getSystemId(),
                exception.getLineNumber(), exception.getColumnNumber(), exception.getException());
    }

    // Creates an XMLParseException from a SAXParseException. */
    protected static XMLParseException createXMLParseException(final SAXParseException exception) {
        final XMLLocatorImpl location = new XMLLocatorImpl(
                exception.getPublicId(),
                exception.getSystemId(),
                exception.getLineNumber(),
                exception.getColumnNumber()
                );
        return new XMLParseException(location, exception.getMessage(), exception);
    }

    // Creates an XNIException from a SAXException.
    // NOTE: care should be taken *not* to call this with a SAXParseException; this
    // will lose information!!! */
    protected static XNIException createXNIException(final SAXException exception) {
        return new XNIException(exception.getMessage(), exception);
    }
}
