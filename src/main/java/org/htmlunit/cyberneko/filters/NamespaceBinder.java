/*
 * Copyright (c) 2002-2009 Andy Clark, Marc Guillemot
 * Copyright (c) 2017-2024 Ronald Brill
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
    private boolean namespaces_;

    /** Override namespaces. */
    private boolean overrideNamespaces_;

    /** Insert namespaces. */
    private boolean insertNamespaces_;

    /** Modify HTML element names. */
    private short namesElems_;

    /** Namespaces URI. */
    private String namespacesURI_;

    /** Namespace context. */
    private final NamespaceSupport namespaceContext_ = new NamespaceSupport();

    /** QName. */
    private final QName qName_ = new QName();

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
        namespaces_ = manager.getFeature(NAMESPACES);
        overrideNamespaces_ = manager.getFeature(OVERRIDE_NAMESPACES);
        insertNamespaces_ = manager.getFeature(INSERT_NAMESPACES);

        // get properties
        namesElems_ = getNamesValue(String.valueOf(manager.getProperty(NAMES_ELEMS)));
        namespacesURI_ = String.valueOf(manager.getProperty(NAMESPACES_URI));

        // initialize state
        namespaceContext_.reset();
    }

    /** Start document. */
    @Override
    public void startDocument(final XMLLocator locator, final String encoding,
                    final NamespaceContext nscontext, final Augmentations augs) throws XNIException {
        // perform default handling
        // NOTE: using own namespace context
        super.startDocument(locator, encoding, namespaceContext_, augs);
    }

    /** Start element. */
    @Override
    public void startElement(final QName element, final XMLAttributes attrs,
                    final Augmentations augs) throws XNIException {
        // bind namespaces, if needed
        if (namespaces_) {
            namespaceContext_.pushContext();
            bindNamespaces(element, attrs);
        }

        // perform default handling
        super.startElement(element, attrs, augs);
    }

    /** Empty element. */
    @Override
    public void emptyElement(final QName element, final XMLAttributes attrs,
                    final Augmentations augs) throws XNIException {
        // bind namespaces, if needed
        if (namespaces_) {
            namespaceContext_.pushContext();
            bindNamespaces(element, attrs);
        }

        // perform default handling
        super.emptyElement(element, attrs, augs);

        // pop context
        if (namespaces_) {
            namespaceContext_.popContext();
        }
    }

    /** End element. */
    @Override
    public void endElement(final QName element, final Augmentations augs)
        throws XNIException {

        // bind namespaces, if needed
        if (namespaces_) {
            bindNamespaces(element, null);
        }

        // perform default handling
        super.endElement(element, augs);

        // pop context
        if (namespaces_) {
            namespaceContext_.popContext();
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

    // Binds namespaces.
    protected void bindNamespaces(final QName element, final XMLAttributes attrs) {
        // split element qname
        element.splitQName();

        // declare namespace prefixes
        if (attrs != null) {
            final int attrCount = attrs.getLength();

            for (int i = attrCount - 1; i >= 0; i--) {
                attrs.getName(i, qName_);
                String rawname = qName_.getRawname();
                // we don't uppercase anymore, instead we lowercase, because most
                // of the time, we are lowercase already and save on memory because
                // we don't convert it
                final String rawnameUC = rawname.toLowerCase(Locale.ROOT);

                if ("xmlns".equals(rawnameUC) || rawnameUC.startsWith("xmlns:")) {
                    final int anamelen = rawname.length();

                    // get parts
                    String aprefix = anamelen > 5 ? rawname.substring(0, 5) : null;
                    String alocal = anamelen > 5 ? rawname.substring(6) : rawname;
                    final String avalue = attrs.getValue(i);

                    // re-case parts and set them back into attributes
                    final String prefix;
                    if (anamelen > 5) {
                        aprefix = modifyName(aprefix, NAMES_LOWERCASE);
                        alocal = modifyName(alocal, namesElems_);
                        rawname = aprefix + ':' + alocal;
                        prefix = alocal;
                    }
                    else {
                        alocal = modifyName(alocal, NAMES_LOWERCASE);
                        rawname = alocal;
                        prefix = "";
                    }
                    qName_.setValues(aprefix, alocal, rawname, null);
                    attrs.setName(i, qName_);

                    // declare prefix, this is moved up to avoid
                    // another if
                    // final String prefix = alocal != rawname ? alocal : "";
                    String uri = avalue.length() > 0 ? avalue : null;
                    if (overrideNamespaces_ && prefix.equals(element.getPrefix())
                            && htmlConfiguration_.getHtmlElements().getElement(element.getLocalpart(), null) != null) {
                        uri = namespacesURI_;
                    }
                    namespaceContext_.declarePrefix(prefix, uri);
                }
            }
        }

        // bind element
        element.setUri(namespaceContext_.getURI(element.getPrefix() != null ? element.getPrefix() : ""));
        // REVISIT: The prefix of a qualified element name that is
        //          bound to a namespace is passed (as recent as
        //          Xerces 2.4.0) as "" for start elements and null
        //          for end elements. Why? One of them is a bug,
        //          clearly. -Ac
        if (element.getUri() != null && element.getPrefix() == null) {
            element.setPrefix("");
        }

        // do we need to insert namespace bindings?
        if (insertNamespaces_ && attrs != null
                && htmlConfiguration_.getHtmlElements().getElement(element.getLocalpart(), null) != null) {
            if (element.getPrefix() == null || namespaceContext_.getURI(element.getPrefix()) == null) {
                final String xmlns = "xmlns" + ((element.getPrefix() != null)
                             ? ":" + element.getPrefix() : "");
                qName_.setValues(null, xmlns, xmlns, null);
                attrs.addAttribute(qName_, "CDATA", namespacesURI_);
                bindNamespaces(element, attrs);
                return;
            }
        }

        // bind attributes
        if (attrs != null) {
            final int attrCount = attrs.getLength();
            for (int i = 0; i < attrCount; i++) {
                final QName qName = attrs.getName(i).splitQName();

                final String prefix = !"xmlns".equals(qName.getRawname())
                       ? (qName.getPrefix() != null ? qName.getPrefix() : "") : "xmlns";
                // PATCH: Joseph Walton, if we have a non-empty prefix
                if (prefix.length() > 0) {
                    qName.setUri("xml".equals(prefix) ? XML_URI : namespaceContext_.getURI(prefix));
                }
                // NOTE: You would think the xmlns namespace would be handled
                //       by NamespaceSupport but it's not. -Ac
                if (qName.getUri() == null && "xmlns".equals(prefix)) {
                    qName.setUri(XMLNS_URI);
                }
                // no need to set it, we already worked on the reference,
                // less copying of things make it more efficient
                // attrs.setName(i, fQName_);
            }
        }
    }

    /**
     * This namespace context object implements the old and new XNI
     * <code>NamespaceContext</code> interface methods so that it can
     * be used across all versions of Xerces2.
     */
    public static class NamespaceSupport implements NamespaceContext {

        /** Top of the levels list. */
        private int top_ = 0;

        /** The levels of the entries. */
        private int[] levels_ = new int[10];

        /** The entries. */
        private Entry[] entries_ = new Entry[10];

        /** Default constructor. */
        public NamespaceSupport() {
            pushContext();
            declarePrefix("xml", NamespaceContext.XML_URI);
            declarePrefix("xmlns", NamespaceContext.XMLNS_URI);
        }

        // since Xerces 2.0.0-beta2 (old XNI namespaces)

        /** Get URI. */
        @Override
        public String getURI(final String prefix) {
            for (int i = levels_[top_] - 1; i >= 0; i--) {
                final Entry entry = entries_[i];
                if (entry.prefix_.equals(prefix)) {
                    return entry.uri_;
                }
            }
            return null;
        }

        /** Get declared prefix count. */
        @Override
        public int getDeclaredPrefixCount() {
            return levels_[top_] - levels_[top_ - 1];
        }

        /** Get declared prefix at. */
        @Override
        public String getDeclaredPrefixAt(final int index) {
            return entries_[levels_[top_ - 1] + index].prefix_;
        }

        // Get parent context.
        public NamespaceContext getParentContext() {
            return this;
        }

        // since Xerces #.#.# (new XNI namespaces)

        /** Reset. */
        @Override
        public void reset() {
            levels_[top_ = 1] = levels_[top_ - 1];
        }

        /** Push context. */
        @Override
        public void pushContext() {
            if (++top_ == levels_.length) {
                final int[] iarray = new int[levels_.length + 10];
                System.arraycopy(levels_, 0, iarray, 0, levels_.length);
                levels_ = iarray;
            }
            levels_[top_] = levels_[top_ - 1];
        }

        /** Pop context. */
        @Override
        public void popContext() {
            if (top_ > 1) {
                top_--;
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
            if (levels_[top_] == entries_.length) {
                final Entry[] earray = new Entry[entries_.length + 10];
                System.arraycopy(entries_, 0, earray, 0, entries_.length);
                entries_ = earray;
            }
            entries_[levels_[top_]++] = entry;
            return true;
        }

        /** A namespace binding entry. */
        static final class Entry {

            /** Prefix. */
            public final String prefix_;

            /** URI. */
            public final String uri_;

            /** Constructs an entry. */
            Entry(final String prefix, final String uri) {
                prefix_ = prefix;
                uri_ = uri;
            }
        }
    }
}
