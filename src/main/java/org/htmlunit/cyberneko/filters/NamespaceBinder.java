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

import java.util.Locale;

import org.htmlunit.cyberneko.HTMLConfiguration;
import org.htmlunit.cyberneko.xerces.xni.Augmentations;
import org.htmlunit.cyberneko.xerces.xni.NamespaceContext;
import org.htmlunit.cyberneko.xerces.xni.QName;
import org.htmlunit.cyberneko.xerces.xni.XMLAttributes;
import org.htmlunit.cyberneko.xerces.xni.XMLLocator;
import org.htmlunit.cyberneko.xerces.xni.XNIException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLComponentManager;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLConfigurationException;

/**
 * This filter binds namespaces if namespace processing is turned on
 * by setting the feature "http://xml.org/sax/features/namespaces" is
 * set to <code>true</code>.
 * <p>
 * This configuration recognizes the following features:
 * <ul>
 * <li>http://xml.org/sax/features/namespaces
 * </ul>
 *
 * @author Andy Clark
 */
public class NamespaceBinder extends DefaultFilter {

    /** XHTML 1.0 namespace URI (http://www.w3.org/1999/xhtml). */
    public static final String XHTML_1_0_URI = "http://www.w3.org/1999/xhtml";

    /** XML namespace URI (http://www.w3.org/XML/1998/namespace). */
    public static final String XML_URI = "http://www.w3.org/XML/1998/namespace";

    /** XMLNS namespace URI (http://www.w3.org/2000/xmlns/). */
    public static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";

    // features

    /** Namespaces. */
    private static final String NAMESPACES = "http://xml.org/sax/features/namespaces";

    /** Override namespace binding URI. */
    private static final String OVERRIDE_NAMESPACES = "http://cyberneko.org/html/features/override-namespaces";

    /** Insert namespace binding URIs. */
    private static final String INSERT_NAMESPACES = "http://cyberneko.org/html/features/insert-namespaces";

    /** Recognized features. */
    private static final String[] RECOGNIZED_FEATURES = {
        NAMESPACES,
        OVERRIDE_NAMESPACES,
        INSERT_NAMESPACES,
    };

    /** Feature defaults. */
    private static final Boolean[] FEATURE_DEFAULTS = {
        null,
        Boolean.FALSE,
        Boolean.FALSE,
    };

    /** Modify HTML element names: { "upper", "lower", "default" }. */
    private static final String NAMES_ELEMS = "http://cyberneko.org/html/properties/names/elems";

    /** Namespaces URI. */
    private static final String NAMESPACES_URI = "http://cyberneko.org/html/properties/namespaces-uri";

    /** Recognized properties. */
    private static final String[] RECOGNIZED_PROPERTIES = new String[] {
        NAMES_ELEMS,
        NAMESPACES_URI,
    };

    /** Property defaults. */
    private static final Object[] PROPERTY_DEFAULTS = {
        null,
        null,
        XHTML_1_0_URI,
    };

    // modify HTML names

    /** Don't modify HTML names. */
    private static final short NAMES_NO_CHANGE = 0;

    /** Uppercase HTML names. */
    private static final short NAMES_UPPERCASE = 1;

    /** Lowercase HTML names. */
    private static final short NAMES_LOWERCASE = 2;

    /** Namespaces. */
    private boolean fNamespaces_;

    /** Override namespaces. */
    private boolean fOverrideNamespaces_;

    /** Insert namespaces. */
    private boolean fInsertNamespaces_;

    // properties

    /** Modify HTML element names. */
    private short fNamesElems_;

    /** Namespaces URI. */
    private String fNamespacesURI_;

    /** Namespace context. */
    private final NamespaceSupport fNamespaceContext_ = new NamespaceSupport();

    /** QName. */
    private final QName fQName_ = new QName();

    private final HTMLConfiguration htmlConfiguration_;

    public NamespaceBinder(final HTMLConfiguration htmlConfiguration) {
        htmlConfiguration_ = htmlConfiguration;
    }

    /**
     * Returns a list of feature identifiers that are recognized by
     * this component. This method may return null if no features
     * are recognized by this component.
     */
    @Override
    public String[] getRecognizedFeatures() {
        return merge(super.getRecognizedFeatures(), RECOGNIZED_FEATURES);
    }

    /**
     * Returns the default state for a feature, or null if this
     * component does not want to report a default value for this
     * feature.
     */
    @Override
    public Boolean getFeatureDefault(final String featureId) {
        for (int i = 0; i < RECOGNIZED_FEATURES.length; i++) {
            if (RECOGNIZED_FEATURES[i].equals(featureId)) {
                return FEATURE_DEFAULTS[i];
            }
        }
        return super.getFeatureDefault(featureId);
    }

    /**
     * Returns a list of property identifiers that are recognized by
     * this component. This method may return null if no properties
     * are recognized by this component.
     */
    @Override
    public String[] getRecognizedProperties() {
        return merge(super.getRecognizedProperties(), RECOGNIZED_PROPERTIES);
    }

    /**
     * Returns the default value for a property, or null if this
     * component does not want to report a default value for this
     * property.
     */
    @Override
    public Object getPropertyDefault(final String propertyId) {
        for (int i = 0; i < RECOGNIZED_PROPERTIES.length; i++) {
            if (RECOGNIZED_PROPERTIES[i].equals(propertyId)) {
                return PROPERTY_DEFAULTS[i];
            }
        }
        return super.getPropertyDefault(propertyId);
    }

    /**
     * Resets the component. The component can query the component manager
     * about any features and properties that affect the operation of the
     * component.
     *
     * @param manager The component manager.
     *
     * @throws XNIException Thrown by component on initialization error.
     */
    @Override
    public void reset(final XMLComponentManager manager)
        throws XMLConfigurationException {
        super.reset(manager);

        // features
        fNamespaces_ = manager.getFeature(NAMESPACES);
        fOverrideNamespaces_ = manager.getFeature(OVERRIDE_NAMESPACES);
        fInsertNamespaces_ = manager.getFeature(INSERT_NAMESPACES);

        // get properties
        fNamesElems_ = getNamesValue(String.valueOf(manager.getProperty(NAMES_ELEMS)));
        fNamespacesURI_ = String.valueOf(manager.getProperty(NAMESPACES_URI));

        // initialize state
        fNamespaceContext_.reset();
    }

    /** Start document. */
    @Override
    public void startDocument(final XMLLocator locator, final String encoding, final NamespaceContext nscontext, final Augmentations augs)
        throws XNIException {

        // perform default handling
        // NOTE: using own namespace context
        super.startDocument(locator, encoding, fNamespaceContext_, augs);
    }

    /** Start element. */
    @Override
    public void startElement(final QName element, final XMLAttributes attrs, final Augmentations augs) throws XNIException {

        // bind namespaces, if needed
        if (fNamespaces_) {
            fNamespaceContext_.pushContext();
            bindNamespaces(element, attrs);
        }

        // perform default handling
        super.startElement(element, attrs, augs);
    }

    /** Empty element. */
    @Override
    public void emptyElement(final QName element, final XMLAttributes attrs, final Augmentations augs) throws XNIException {

        // bind namespaces, if needed
        if (fNamespaces_) {
            fNamespaceContext_.pushContext();
            bindNamespaces(element, attrs);
        }

        // perform default handling
        super.emptyElement(element, attrs, augs);

        // pop context
        if (fNamespaces_) {
            fNamespaceContext_.popContext();
        }
    }

    /** End element. */
    @Override
    public void endElement(final QName element, final Augmentations augs)
        throws XNIException {

        // bind namespaces, if needed
        if (fNamespaces_) {
            bindNamespaces(element, null);
        }

        // perform default handling
        super.endElement(element, augs);

        // pop context
        if (fNamespaces_) {
            fNamespaceContext_.popContext();
        }
    }

    //
    // Protected static methods
    //

    // Splits a qualified name.
    protected static void splitQName(final QName qname) {
        final int index = qname.rawname.indexOf(':');
        if (index != -1) {
            qname.prefix = qname.rawname.substring(0, index);
            qname.localpart  = qname.rawname.substring(index + 1);
        }
    }

    //
    // Converts HTML names string value to constant value.
    //
    // @see #NAMES_NO_CHANGE
    // @see #NAMES_LOWERCASE
    // @see #NAMES_UPPERCASE
    protected static short getNamesValue(final String value) {
        if ("lower".equals(value)) {
            return NAMES_LOWERCASE;
        }
        if ("upper".equals(value)) {
            return NAMES_UPPERCASE;
        }
        return NAMES_NO_CHANGE;
    }

    // Modifies the given name based on the specified mode.
    protected static String modifyName(final String name, final short mode) {
        switch (mode) {
            case NAMES_UPPERCASE: return name.toUpperCase(Locale.ROOT);
            case NAMES_LOWERCASE: return name.toLowerCase(Locale.ROOT);
        }
        return name;
    }

    //
    // Protected methods
    //

    // Binds namespaces.
    protected void bindNamespaces(final QName element, final XMLAttributes attrs) {

        // split element qname
        splitQName(element);

        // declare namespace prefixes
        if (attrs != null) {
            final int attrCount = attrs.getLength();
            for (int i = attrCount - 1; i >= 0; i--) {
                attrs.getName(i, fQName_);
                String rawname = fQName_.rawname;
                final String rawnameUC = rawname.toUpperCase(Locale.ROOT);
                if (rawnameUC.startsWith("XMLNS:") || "XMLNS".equals(rawnameUC)) {
                    final int anamelen = rawname.length();

                    // get parts
                    String aprefix = anamelen > 5 ? rawname.substring(0, 5) : null;
                    String alocal = anamelen > 5 ? rawname.substring(6) : rawname;
                    final String avalue = attrs.getValue(i);

                    // re-case parts and set them back into attributes
                    if (anamelen > 5) {
                        aprefix = modifyName(aprefix, NAMES_LOWERCASE);
                        alocal = modifyName(alocal, fNamesElems_);
                        rawname = aprefix + ':' + alocal;
                    }
                    else {
                        alocal = modifyName(alocal, NAMES_LOWERCASE);
                        rawname = alocal;
                    }
                    fQName_.setValues(aprefix, alocal, rawname, null);
                    attrs.setName(i, fQName_);

                    // declare prefix
                    final String prefix = alocal != rawname ? alocal : "";
                    String uri = avalue.length() > 0 ? avalue : null;
                    if (fOverrideNamespaces_ && prefix.equals(element.prefix)
                            && htmlConfiguration_.getHtmlElements().getElement(element.localpart, null) != null) {
                        uri = fNamespacesURI_;
                    }
                    fNamespaceContext_.declarePrefix(prefix, uri);
                }
            }
        }

        // bind element
        String prefix = element.prefix != null ? element.prefix : "";
        element.uri = fNamespaceContext_.getURI(prefix);
        // REVISIT: The prefix of a qualified element name that is
        //          bound to a namespace is passed (as recent as
        //          Xerces 2.4.0) as "" for start elements and null
        //          for end elements. Why? One of them is a bug,
        //          clearly. -Ac
        if (element.uri != null && element.prefix == null) {
            element.prefix = "";
        }

        // do we need to insert namespace bindings?
        if (fInsertNamespaces_ && attrs != null
                && htmlConfiguration_.getHtmlElements().getElement(element.localpart, null) != null) {
            if (element.prefix == null || fNamespaceContext_.getURI(element.prefix) == null) {
                final String xmlns = "xmlns" + ((element.prefix != null)
                             ? ":" + element.prefix : "");
                fQName_.setValues(null, xmlns, xmlns, null);
                attrs.addAttribute(fQName_, "CDATA", fNamespacesURI_);
                bindNamespaces(element, attrs);
                return;
            }
        }

        // bind attributes
        if (attrs != null) {
            final int attrCount = attrs.getLength();
            for (int i = 0; i < attrCount; i++) {
                attrs.getName(i, fQName_);
                splitQName(fQName_);
                prefix = !"xmlns".equals(fQName_.rawname)
                       ? (fQName_.prefix != null ? fQName_.prefix : "") : "xmlns";
                // PATCH: Joseph Walton
                if (!"".equals(prefix)) {
                    fQName_.uri = "xml".equals(prefix) ? XML_URI : fNamespaceContext_.getURI(prefix);
                }
                // NOTE: You would think the xmlns namespace would be handled
                //       by NamespaceSupport but it's not. -Ac
                if ("xmlns".equals(prefix) && fQName_.uri == null) {
                    fQName_.uri = XMLNS_URI;
                }
                attrs.setName(i, fQName_);
            }
        }
    }

    //
    // Classes
    //

    /**
     * This namespace context object implements the old and new XNI
     * <code>NamespaceContext</code> interface methods so that it can
     * be used across all versions of Xerces2.
     */
    public static class NamespaceSupport
        implements NamespaceContext {

        //
        // Data
        //

        /** Top of the levels list. */
        protected int fTop = 0;

        /** The levels of the entries. */
        protected int[] fLevels = new int[10];

        /** The entries. */
        protected Entry[] fEntries = new Entry[10];

        //
        // Constructors
        //

        /** Default constructor. */
        public NamespaceSupport() {
            pushContext();
            declarePrefix("xml", NamespaceContext.XML_URI);
            declarePrefix("xmlns", NamespaceContext.XMLNS_URI);
        }

        //
        // NamespaceContext methods
        //

        // since Xerces 2.0.0-beta2 (old XNI namespaces)

        /** Get URI. */
        @Override
        public String getURI(final String prefix) {
            for (int i = fLevels[fTop] - 1; i >= 0; i--) {
                final Entry entry = fEntries[i];
                if (entry.prefix.equals(prefix)) {
                    return entry.uri;
                }
            }
            return null;
        }

        /** Get declared prefix count. */
        @Override
        public int getDeclaredPrefixCount() {
            return fLevels[fTop] - fLevels[fTop - 1];
        }

        /** Get declared prefix at. */
        @Override
        public String getDeclaredPrefixAt(final int index) {
            return fEntries[fLevels[fTop - 1] + index].prefix;
        }

        // Get parent context.
        public NamespaceContext getParentContext() {
            return this;
        }

        // since Xerces #.#.# (new XNI namespaces)

        /** Reset. */
        @Override
        public void reset() {
            fLevels[fTop = 1] = fLevels[fTop - 1];
        }

        /** Push context. */
        @Override
        public void pushContext() {
            if (++fTop == fLevels.length) {
                final int[] iarray = new int[fLevels.length + 10];
                System.arraycopy(fLevels, 0, iarray, 0, fLevels.length);
                fLevels = iarray;
            }
            fLevels[fTop] = fLevels[fTop - 1];
        }

        /** Pop context. */
        @Override
        public void popContext() {
            if (fTop > 1) {
                fTop--;
            }
        }

        /** Declare prefix. */
        @Override
        public boolean declarePrefix(final String prefix, final String uri) {
            final int count = getDeclaredPrefixCount();
            for (int i = 0; i < count; i++) {
                final String dprefix = getDeclaredPrefixAt(i);
                if (dprefix.equals(prefix)) {
                    return false;
                }
            }
            final Entry entry = new Entry(prefix, uri);
            if (fLevels[fTop] == fEntries.length) {
                final Entry[] earray = new Entry[fEntries.length + 10];
                System.arraycopy(fEntries, 0, earray, 0, fEntries.length);
                fEntries = earray;
            }
            fEntries[fLevels[fTop]++] = entry;
            return true;
        }

        /** A namespace binding entry. */
        static final class Entry {

            //
            // Data
            //

            /** Prefix. */
            public final String prefix;

            /** URI. */
            public final String uri;

            //
            // Constructors
            //

            /** Constructs an entry. */
            Entry(final String prefix, final String uri) {
                this.prefix = prefix;
                this.uri = uri;
            }
        }
    }
}
