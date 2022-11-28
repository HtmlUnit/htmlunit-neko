/*
 * Copyright 2002-2009 Andy Clark, Marc Guillemot
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sourceforge.htmlunit.cyberneko.filters;

import net.sourceforge.htmlunit.xerces.xni.Augmentations;
import net.sourceforge.htmlunit.xerces.xni.NamespaceContext;
import net.sourceforge.htmlunit.xerces.xni.QName;
import net.sourceforge.htmlunit.xerces.xni.XMLAttributes;
import net.sourceforge.htmlunit.xerces.xni.XMLDocumentHandler;
import net.sourceforge.htmlunit.xerces.xni.XMLLocator;
import net.sourceforge.htmlunit.xerces.xni.XMLResourceIdentifier;
import net.sourceforge.htmlunit.xerces.xni.XMLString;
import net.sourceforge.htmlunit.xerces.xni.XNIException;
import net.sourceforge.htmlunit.xerces.xni.parser.XMLComponentManager;
import net.sourceforge.htmlunit.xerces.xni.parser.XMLConfigurationException;
import net.sourceforge.htmlunit.xerces.xni.parser.XMLDocumentFilter;
import net.sourceforge.htmlunit.xerces.xni.parser.XMLDocumentSource;

import net.sourceforge.htmlunit.cyberneko.HTMLComponent;

/**
 * This class implements a filter that simply passes document
 * events to the next handler. It can be used as a base class to
 * simplify the development of new document filters.
 *
 * @author Andy Clark
 */
public class DefaultFilter
    implements XMLDocumentFilter, HTMLComponent {

    //
    // Data
    //

    /** Document handler. */
    protected XMLDocumentHandler fDocumentHandler;

    /** Document source. */
    protected XMLDocumentSource fDocumentSource;

    //
    // XMLDocumentSource methods
    //

    /** Sets the document handler. */
    @Override
    public void setDocumentHandler(XMLDocumentHandler handler) {
        fDocumentHandler = handler;
    }

    /** Returns the document handler. */
    @Override
    public XMLDocumentHandler getDocumentHandler() {
        return fDocumentHandler;
    }

    /** Sets the document source. */
    @Override
    public void setDocumentSource(XMLDocumentSource source) {
        fDocumentSource = source;
    }

    /** Returns the document source. */
    @Override
    public XMLDocumentSource getDocumentSource() {
        return fDocumentSource;
    }

    //
    // XMLDocumentHandler methods
    //

    /** Start document. */
    @Override
    public void startDocument(XMLLocator locator, String encoding,
                              NamespaceContext nscontext, Augmentations augs)
        throws XNIException {
        if (fDocumentHandler != null) {
            fDocumentHandler.startDocument(locator, encoding, nscontext, augs);
        }
    }

    // old methods

    /** XML declaration. */
    @Override
    public void xmlDecl(String version, String encoding, String standalone, Augmentations augs)
        throws XNIException {
        if (fDocumentHandler != null) {
            fDocumentHandler.xmlDecl(version, encoding, standalone, augs);
        }
    }

    /** Doctype declaration. */
    @Override
    public void doctypeDecl(String root, String publicId, String systemId, Augmentations augs)
        throws XNIException {
        if (fDocumentHandler != null) {
            fDocumentHandler.doctypeDecl(root, publicId, systemId, augs);
        }
    }

    /** Comment. */
    @Override
    public void comment(XMLString text, Augmentations augs)
        throws XNIException {
        if (fDocumentHandler != null) {
            fDocumentHandler.comment(text, augs);
        }
    }

    /** Processing instruction. */
    @Override
    public void processingInstruction(String target, XMLString data, Augmentations augs)
        throws XNIException {
        if (fDocumentHandler != null) {
            fDocumentHandler.processingInstruction(target, data, augs);
        }
    }

    /** Start element. */
    @Override
    public void startElement(QName element, XMLAttributes attributes, Augmentations augs)
        throws XNIException {
        if (fDocumentHandler != null) {
            fDocumentHandler.startElement(element, attributes, augs);
        }
    }

    /** Empty element. */
    @Override
    public void emptyElement(QName element, XMLAttributes attributes, Augmentations augs)
        throws XNIException {
        if (fDocumentHandler != null) {
            fDocumentHandler.emptyElement(element, attributes, augs);
        }
    }

    /** Characters. */
    @Override
    public void characters(XMLString text, Augmentations augs)
        throws XNIException {
        if (fDocumentHandler != null) {
            fDocumentHandler.characters(text, augs);
        }
    }

    /** Ignorable whitespace. */
    @Override
    public void ignorableWhitespace(XMLString text, Augmentations augs)
        throws XNIException {
        if (fDocumentHandler != null) {
            fDocumentHandler.ignorableWhitespace(text, augs);
        }
    }

    /** Start general entity. */
    @Override
    public void startGeneralEntity(String name, XMLResourceIdentifier id, String encoding, Augmentations augs)
        throws XNIException {
        if (fDocumentHandler != null) {
            fDocumentHandler.startGeneralEntity(name, id, encoding, augs);
        }
    }

    /** Text declaration. */
    @Override
    public void textDecl(String version, String encoding, Augmentations augs)
        throws XNIException {
        if (fDocumentHandler != null) {
            fDocumentHandler.textDecl(version, encoding, augs);
        }
    }

    /** End general entity. */
    @Override
    public void endGeneralEntity(String name, Augmentations augs)
        throws XNIException {
        if (fDocumentHandler != null) {
            fDocumentHandler.endGeneralEntity(name, augs);
        }
    }

    /** Start CDATA section. */
    @Override
    public void startCDATA(Augmentations augs) throws XNIException {
        if (fDocumentHandler != null) {
            fDocumentHandler.startCDATA(augs);
        }
    }

    /** End CDATA section. */
    @Override
    public void endCDATA(Augmentations augs) throws XNIException {
        if (fDocumentHandler != null) {
            fDocumentHandler.endCDATA(augs);
        }
    }

    /** End element. */
    @Override
    public void endElement(QName element, Augmentations augs)
        throws XNIException {
        if (fDocumentHandler != null) {
            fDocumentHandler.endElement(element, augs);
        }
    }

    /** End document. */
    @Override
    public void endDocument(Augmentations augs) throws XNIException {
        if (fDocumentHandler != null) {
            fDocumentHandler.endDocument(augs);
        }
    }

    //
    // HTMLComponent methods
    //

    /**
     * Returns a list of feature identifiers that are recognized by
     * this component. This method may return null if no features
     * are recognized by this component.
     */
    @Override
    public String[] getRecognizedFeatures() {
        return null;
    }

    /**
     * Returns the default state for a feature, or null if this
     * component does not want to report a default value for this
     * feature.
     */
    @Override
    public Boolean getFeatureDefault(String featureId) {
        return null;
    }

    /**
     * Returns a list of property identifiers that are recognized by
     * this component. This method may return null if no properties
     * are recognized by this component.
     */
    @Override
    public String[] getRecognizedProperties() {
        return null;
    }

    /**
     * Returns the default state for a property, or null if this
     * component does not want to report a default value for this
     * property.
     */
    @Override
    public Object getPropertyDefault(String propertyId) {
        return null;
    }

    /**
     * Resets the component. The component can query the component manager
     * about any features and properties that affect the operation of the
     * component.
     *
     * @param componentManager The component manager.
     *
     * @throws XNIException Thrown by component on initialization error.
     */
    @Override
    public void reset(XMLComponentManager componentManager)
        throws XMLConfigurationException {
    }

    /**
     * Sets the state of a feature. This method is called by the component
     * manager any time after reset when a feature changes state.
     * <p>
     * <strong>Note:</strong> Components should silently ignore features
     * that do not affect the operation of the component.
     *
     * @param featureId The feature identifier.
     * @param state     The state of the feature.
     *
     * @throws XMLConfigurationException Thrown for configuration error.
     *                                   In general, components should
     *                                   only throw this exception if
     *                                   it is <strong>really</strong>
     *                                   a critical error.
     */
    @Override
    public void setFeature(String featureId, boolean state)
        throws XMLConfigurationException {
    }

    /**
     * Sets the value of a property. This method is called by the component
     * manager any time after reset when a property changes value.
     * <p>
     * <strong>Note:</strong> Components should silently ignore properties
     * that do not affect the operation of the component.
     *
     * @param propertyId The property identifier.
     * @param value      The value of the property.
     *
     * @throws XMLConfigurationException Thrown for configuration error.
     *                                   In general, components should
     *                                   only throw this exception if
     *                                   it is <strong>really</strong>
     *                                   a critical error.
     */
    @Override
    public void setProperty(String propertyId, Object value)
        throws XMLConfigurationException {
    }

    //
    // Protected static methods
    //

    /**
     * Utility method for merging string arrays for recognized features
     * and recognized properties.
     * @param array1 array1
     * @param array2 array2
     * @return the merged array
     */
    protected static String[] merge(String[] array1, String[] array2) {

        // shortcut merge
        if (array1 == array2) {
            return array1;
        }
        if (array1 == null) {
            return array2;
        }
        if (array2 == null) {
            return array1;
        }

        // full merge
        final String[] array3 = new String[array1.length + array2.length];
        System.arraycopy(array1, 0, array3, 0, array1.length);
        System.arraycopy(array2, 0, array3, array1.length, array2.length);

        return array3;
    }
}
