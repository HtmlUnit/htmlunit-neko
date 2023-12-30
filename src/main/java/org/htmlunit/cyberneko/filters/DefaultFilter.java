/*
 * Copyright 2002-2009 Andy Clark, Marc Guillemot
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
package org.htmlunit.cyberneko.filters;

import org.htmlunit.cyberneko.HTMLComponent;
import org.htmlunit.cyberneko.xerces.xni.Augmentations;
import org.htmlunit.cyberneko.xerces.xni.NamespaceContext;
import org.htmlunit.cyberneko.xerces.xni.QName;
import org.htmlunit.cyberneko.xerces.xni.XMLAttributes;
import org.htmlunit.cyberneko.xerces.xni.XMLString;
import org.htmlunit.cyberneko.xerces.xni.XMLDocumentHandler;
import org.htmlunit.cyberneko.xerces.xni.XMLLocator;
import org.htmlunit.cyberneko.xerces.xni.XNIException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLComponentManager;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLConfigurationException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLDocumentFilter;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLDocumentSource;

/**
 * This class implements a filter that simply passes document
 * events to the next handler. It can be used as a base class to
 * simplify the development of new document filters.
 *
 * @author Andy Clark
 */
public class DefaultFilter
    implements XMLDocumentFilter, HTMLComponent {

    /** Document handler. */
    private XMLDocumentHandler fDocumentHandler_;

    /** Document source. */
    private XMLDocumentSource fDocumentSource;

    /** Sets the document handler. */
    @Override
    public void setDocumentHandler(final XMLDocumentHandler handler) {
        fDocumentHandler_ = handler;
    }

    /** Returns the document handler. */
    @Override
    public XMLDocumentHandler getDocumentHandler() {
        return fDocumentHandler_;
    }

    /** Sets the document source. */
    @Override
    public void setDocumentSource(final XMLDocumentSource source) {
        fDocumentSource = source;
    }

    /** Returns the document source. */
    @Override
    public XMLDocumentSource getDocumentSource() {
        return fDocumentSource;
    }

    /** Start document. */
    @Override
    public void startDocument(final XMLLocator locator, final String encoding, final NamespaceContext nscontext, final Augmentations augs)
        throws XNIException {
        if (fDocumentHandler_ != null) {
            fDocumentHandler_.startDocument(locator, encoding, nscontext, augs);
        }
    }

    /** XML declaration. */
    @Override
    public void xmlDecl(final String version, final String encoding, final String standalone, final Augmentations augs)
        throws XNIException {
        if (fDocumentHandler_ != null) {
            fDocumentHandler_.xmlDecl(version, encoding, standalone, augs);
        }
    }

    /** Doctype declaration. */
    @Override
    public void doctypeDecl(final String root, final String publicId, final String systemId, final Augmentations augs)
        throws XNIException {
        if (fDocumentHandler_ != null) {
            fDocumentHandler_.doctypeDecl(root, publicId, systemId, augs);
        }
    }

    /** Comment. */
    @Override
    public void comment(final XMLString text, final Augmentations augs)
        throws XNIException {
        if (fDocumentHandler_ != null) {
            fDocumentHandler_.comment(text, augs);
        }
    }

    /** Processing instruction. */
    @Override
    public void processingInstruction(final String target, final XMLString data, final Augmentations augs)
        throws XNIException {
        if (fDocumentHandler_ != null) {
            fDocumentHandler_.processingInstruction(target, data, augs);
        }
    }

    /** Start element. */
    @Override
    public void startElement(final QName element, final XMLAttributes attributes, final Augmentations augs)
        throws XNIException {
        if (fDocumentHandler_ != null) {
            fDocumentHandler_.startElement(element, attributes, augs);
        }
    }

    /** Empty element. */
    @Override
    public void emptyElement(final QName element, final XMLAttributes attributes, final Augmentations augs)
        throws XNIException {
        if (fDocumentHandler_ != null) {
            fDocumentHandler_.emptyElement(element, attributes, augs);
        }
    }

    /** Characters. */
    @Override
    public void characters(final XMLString text, final Augmentations augs)
        throws XNIException {
        if (fDocumentHandler_ != null) {
            fDocumentHandler_.characters(text, augs);
        }
    }

    /** Ignorable whitespace. */
    @Override
    public void ignorableWhitespace(final XMLString text, final Augmentations augs)
        throws XNIException {
        if (fDocumentHandler_ != null) {
            fDocumentHandler_.ignorableWhitespace(text, augs);
        }
    }

    /** Start general entity. */
    @Override
    public void startGeneralEntity(final String name, final String encoding, final Augmentations augs)
        throws XNIException {
        if (fDocumentHandler_ != null) {
            fDocumentHandler_.startGeneralEntity(name, encoding, augs);
        }
    }

    /** Text declaration. */
    @Override
    public void textDecl(final String version, final String encoding, final Augmentations augs)
        throws XNIException {
        if (fDocumentHandler_ != null) {
            fDocumentHandler_.textDecl(version, encoding, augs);
        }
    }

    /** End general entity. */
    @Override
    public void endGeneralEntity(final String name, final Augmentations augs)
        throws XNIException {
        if (fDocumentHandler_ != null) {
            fDocumentHandler_.endGeneralEntity(name, augs);
        }
    }

    /** Start CDATA section. */
    @Override
    public void startCDATA(final Augmentations augs) throws XNIException {
        if (fDocumentHandler_ != null) {
            fDocumentHandler_.startCDATA(augs);
        }
    }

    /** End CDATA section. */
    @Override
    public void endCDATA(final Augmentations augs) throws XNIException {
        if (fDocumentHandler_ != null) {
            fDocumentHandler_.endCDATA(augs);
        }
    }

    /** End element. */
    @Override
    public void endElement(final QName element, final Augmentations augs)
        throws XNIException {
        if (fDocumentHandler_ != null) {
            fDocumentHandler_.endElement(element, augs);
        }
    }

    /** End document. */
    @Override
    public void endDocument(final Augmentations augs) throws XNIException {
        if (fDocumentHandler_ != null) {
            fDocumentHandler_.endDocument(augs);
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
    public Boolean getFeatureDefault(final String featureId) {
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
    public Object getPropertyDefault(final String propertyId) {
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
    public void reset(final XMLComponentManager componentManager)
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
    public void setFeature(final String featureId, final boolean state)
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
    public void setProperty(final String propertyId, final Object value) throws XMLConfigurationException {
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
    protected static String[] merge(final String[] array1, final String[] array2) {

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
