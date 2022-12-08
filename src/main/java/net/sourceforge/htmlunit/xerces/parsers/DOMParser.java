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

package net.sourceforge.htmlunit.xerces.parsers;

import java.io.CharConversionException;
import java.io.IOException;

import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;

import net.sourceforge.htmlunit.xerces.util.ErrorHandlerWrapper;
import net.sourceforge.htmlunit.xerces.util.SAXMessageFormatter;
import net.sourceforge.htmlunit.xerces.xni.XNIException;
import net.sourceforge.htmlunit.xerces.xni.parser.XMLConfigurationException;
import net.sourceforge.htmlunit.xerces.xni.parser.XMLErrorHandler;
import net.sourceforge.htmlunit.xerces.xni.parser.XMLInputSource;
import net.sourceforge.htmlunit.xerces.xni.parser.XMLParseException;
import net.sourceforge.htmlunit.xerces.xni.parser.XMLParserConfiguration;

/**
 * This is the main Xerces DOM parser class. It uses the abstract DOM
 * parser with a document scanner, a dtd scanner, and a validator, as
 * well as a grammar pool.
 *
 * @author Arnaud  Le Hors, IBM
 * @author Andy Clark, IBM
 */
public class DOMParser extends AbstractDOMParser {

    // Constructs a DOM parser using the specified parser configuration.
    public DOMParser(XMLParserConfiguration config) {
        super(config);
    }

    /**
     * Parses the input source specified by the given system identifier.
     * <p>
     * This method is equivalent to the following:
     * <pre>
     *     parse(new InputSource(systemId));
     * </pre>
     *
     * @param systemId The system identifier (URI).
     *
     * @exception org.xml.sax.SAXException Throws exception on SAX error.
     * @exception java.io.IOException Throws exception on i/o error.
     */
    public void parse(String systemId) throws SAXException, IOException {

        // parse document
        XMLInputSource source = new XMLInputSource(null, systemId, null);
        try {
            parse(source);
        }

        // wrap XNI exceptions as SAX exceptions
        catch (XMLParseException e) {
            Exception ex = e.getException();
            if (ex == null || ex instanceof CharConversionException) {
                // must be a parser exception; mine it for locator info and throw
                // a SAXParseException
                LocatorImpl locatorImpl = new LocatorImpl();
                locatorImpl.setPublicId(e.getPublicId());
                locatorImpl.setSystemId(e.getExpandedSystemId());
                locatorImpl.setLineNumber(e.getLineNumber());
                locatorImpl.setColumnNumber(e.getColumnNumber());
                throw (ex == null) ?
                        new SAXParseException(e.getMessage(), locatorImpl) :
                        new SAXParseException(e.getMessage(), locatorImpl, ex);
            }
            if (ex instanceof SAXException) {
                // why did we create an XMLParseException?
                throw (SAXException)ex;
            }
            if (ex instanceof IOException) {
                throw (IOException)ex;
            }
            throw new SAXException(ex);
        }
        catch (XNIException e) {
            e.printStackTrace();
            Exception ex = e.getException();
            if (ex == null) {
                throw new SAXException(e.getMessage());
            }
            if (ex instanceof SAXException) {
                throw (SAXException)ex;
            }
            if (ex instanceof IOException) {
                throw (IOException)ex;
            }
            throw new SAXException(ex);
        }

    }

    /**
     * Parse.
     *
     * @param inputSource the input source
     *
     * @exception org.xml.sax.SAXException on error
     * @exception java.io.IOException on error
     */
    public void parse(InputSource inputSource)
        throws SAXException, IOException {

        // parse document
        try {
            XMLInputSource xmlInputSource =
                new XMLInputSource(inputSource.getPublicId(),
                                   inputSource.getSystemId(),
                                   null);
            xmlInputSource.setByteStream(inputSource.getByteStream());
            xmlInputSource.setCharacterStream(inputSource.getCharacterStream());
            xmlInputSource.setEncoding(inputSource.getEncoding());
            parse(xmlInputSource);
        }

        // wrap XNI exceptions as SAX exceptions
        catch (XMLParseException e) {
            Exception ex = e.getException();
            if (ex == null || ex instanceof CharConversionException) {
                // must be a parser exception; mine it for locator info and throw
                // a SAXParseException
                LocatorImpl locatorImpl = new LocatorImpl();
                locatorImpl.setPublicId(e.getPublicId());
                locatorImpl.setSystemId(e.getExpandedSystemId());
                locatorImpl.setLineNumber(e.getLineNumber());
                locatorImpl.setColumnNumber(e.getColumnNumber());
                throw (ex == null) ?
                        new SAXParseException(e.getMessage(), locatorImpl) :
                        new SAXParseException(e.getMessage(), locatorImpl, ex);
            }
            if (ex instanceof SAXException) {
                // why did we create an XMLParseException?
                throw (SAXException)ex;
            }
            if (ex instanceof IOException) {
                throw (IOException)ex;
            }
            throw new SAXException(ex);
        }
        catch (XNIException e) {
            Exception ex = e.getException();
            if (ex == null) {
                throw new SAXException(e.getMessage());
            }
            if (ex instanceof SAXException) {
                throw (SAXException)ex;
            }
            if (ex instanceof IOException) {
                throw (IOException)ex;
            }
            throw new SAXException(ex);
        }

    }

    /**
     * Allow an application to register an error event handler.
     *
     * <p>If the application does not register an error handler, all
     * error events reported by the SAX parser will be silently
     * ignored; however, normal processing may not continue.  It is
     * highly recommended that all SAX applications implement an
     * error handler to avoid unexpected bugs.</p>
     *
     * <p>Applications may register a new or different handler in the
     * middle of a parse, and the SAX parser must begin using the new
     * handler immediately.</p>
     *
     * @param errorHandler The error handler.
     * @exception java.lang.NullPointerException If the handler
     *            argument is null.
     * @see #getErrorHandler
     */
    public void setErrorHandler(ErrorHandler errorHandler) {

        try {
            XMLErrorHandler xeh = (XMLErrorHandler) fConfiguration.getProperty(ERROR_HANDLER);
            if (xeh instanceof ErrorHandlerWrapper) {
                ErrorHandlerWrapper ehw = (ErrorHandlerWrapper) xeh;
                ehw.setErrorHandler(errorHandler);
            }
            else {
                fConfiguration.setProperty(ERROR_HANDLER,
                        new ErrorHandlerWrapper(errorHandler));
            }
        }
        catch (XMLConfigurationException e) {
            // do nothing
        }

    }

    /**
     * Return the current error handler.
     *
     * @return The current error handler, or null if none
     *         has been registered.
     * @see #setErrorHandler
     */
    public ErrorHandler getErrorHandler() {

        ErrorHandler errorHandler = null;
        try {
            XMLErrorHandler xmlErrorHandler =
                (XMLErrorHandler)fConfiguration.getProperty(ERROR_HANDLER);
            if (xmlErrorHandler != null &&
                xmlErrorHandler instanceof ErrorHandlerWrapper) {
                errorHandler = ((ErrorHandlerWrapper)xmlErrorHandler).getErrorHandler();
            }
        }
        catch (XMLConfigurationException e) {
            // do nothing
        }
        return errorHandler;

    }

    /**
     * Set the state of any feature in a SAX2 parser.  The parser
     * might not recognize the feature, and if it does recognize
     * it, it might not be able to fulfill the request.
     *
     * @param featureId The unique identifier (URI) of the feature.
     * @param state The requested state of the feature (true or false).
     *
     * @exception SAXNotRecognizedException If the
     *            requested feature is not known.
     * @exception SAXNotSupportedException If the
     *            requested feature is known, but the requested
     *            state is not supported.
     */
    public void setFeature(String featureId, boolean state)
        throws SAXNotRecognizedException, SAXNotSupportedException {

        try {
            fConfiguration.setFeature(featureId, state);
        }
        catch (XMLConfigurationException e) {
            String identifier = e.getIdentifier();
            if (e.getType() == XMLConfigurationException.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(
                    SAXMessageFormatter.formatMessage("feature-not-recognized", new Object [] {identifier}));
            }
            else {
                throw new SAXNotSupportedException(
                    SAXMessageFormatter.formatMessage("feature-not-supported", new Object [] {identifier}));
            }
        }

    }

    /**
     * Query the state of a feature.
     * <p>
     * Query the current state of any feature in a SAX2 parser.  The
     * parser might not recognize the feature.
     *
     * @param featureId The unique identifier (URI) of the feature
     *                  being set.
     * @return The current state of the feature.
     * @exception org.xml.sax.SAXNotRecognizedException If the
     *            requested feature is not known.
     * @exception SAXNotSupportedException If the
     *            requested feature is known but not supported.
     */
    public boolean getFeature(String featureId)
        throws SAXNotRecognizedException, SAXNotSupportedException {

        try {
            return fConfiguration.getFeature(featureId);
        }
        catch (XMLConfigurationException e) {
            String identifier = e.getIdentifier();
            if (e.getType() == XMLConfigurationException.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(
                    SAXMessageFormatter.formatMessage("feature-not-recognized", new Object [] {identifier}));
            }
            else {
                throw new SAXNotSupportedException(
                    SAXMessageFormatter.formatMessage("feature-not-supported", new Object [] {identifier}));
            }
        }

    }

    /**
     * Set the value of any property in a SAX2 parser.  The parser
     * might not recognize the property, and if it does recognize
     * it, it might not support the requested value.
     *
     * @param propertyId The unique identifier (URI) of the property
     *                   being set.
     * @param value The value to which the property is being set.
     *
     * @exception SAXNotRecognizedException If the
     *            requested property is not known.
     * @exception SAXNotSupportedException If the
     *            requested property is known, but the requested
     *            value is not supported.
     */
    public void setProperty(String propertyId, Object value)
        throws SAXNotRecognizedException, SAXNotSupportedException {

        try {
            fConfiguration.setProperty(propertyId, value);
        }
        catch (XMLConfigurationException e) {
            String identifier = e.getIdentifier();
            if (e.getType() == XMLConfigurationException.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(
                    SAXMessageFormatter.formatMessage("property-not-recognized", new Object [] {identifier}));
            }
            else {
                throw new SAXNotSupportedException(
                    SAXMessageFormatter.formatMessage("property-not-supported", new Object [] {identifier}));
            }
        }

    }

    /**
     * Query the value of a property.
     * <p>
     * Return the current value of a property in a SAX2 parser.
     * The parser might not recognize the property.
     *
     * @param propertyId The unique identifier (URI) of the property
     *                   being set.
     * @return The current value of the property.
     * @exception org.xml.sax.SAXNotRecognizedException If the
     *            requested property is not known.
     * @exception SAXNotSupportedException If the
     *            requested property is known but not supported.
     */
    public Object getProperty(String propertyId)
        throws SAXNotRecognizedException, SAXNotSupportedException {

       if (propertyId.equals(CURRENT_ELEMENT_NODE)) {
           return (fCurrentNode!=null &&
                   fCurrentNode.getNodeType() == Node.ELEMENT_NODE)? fCurrentNode:null;
       }

        try {
            return fConfiguration.getProperty(propertyId);
        }
        catch (XMLConfigurationException e) {
            String identifier = e.getIdentifier();
            if (e.getType() == XMLConfigurationException.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(
                    SAXMessageFormatter.formatMessage("property-not-recognized", new Object [] {identifier}));
            }
            else {
                throw new SAXNotSupportedException(
                    SAXMessageFormatter.formatMessage("property-not-supported", new Object [] {identifier}));
            }
        }

    }

    /**
     * @return this parser's XMLParserConfiguration.
     */
    public XMLParserConfiguration getXMLParserConfiguration() {
        return fConfiguration;
    }

}
