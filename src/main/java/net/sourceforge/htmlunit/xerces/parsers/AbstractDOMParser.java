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

import java.util.Locale;
import java.util.Stack;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMError;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import net.sourceforge.htmlunit.xerces.dom.AttrImpl;
import net.sourceforge.htmlunit.xerces.dom.CoreDocumentImpl;
import net.sourceforge.htmlunit.xerces.dom.DOMErrorImpl;
import net.sourceforge.htmlunit.xerces.dom.DOMMessageFormatter;
import net.sourceforge.htmlunit.xerces.dom.DocumentImpl;
import net.sourceforge.htmlunit.xerces.dom.ElementImpl;
import net.sourceforge.htmlunit.xerces.dom.EntityImpl;
import net.sourceforge.htmlunit.xerces.dom.EntityReferenceImpl;
import net.sourceforge.htmlunit.xerces.dom.NodeImpl;
import net.sourceforge.htmlunit.xerces.dom.TextImpl;
import net.sourceforge.htmlunit.xerces.impl.Constants;
import net.sourceforge.htmlunit.xerces.util.DOMErrorHandlerWrapper;
import net.sourceforge.htmlunit.xerces.util.ObjectFactory;
import net.sourceforge.htmlunit.xerces.xni.Augmentations;
import net.sourceforge.htmlunit.xerces.xni.NamespaceContext;
import net.sourceforge.htmlunit.xerces.xni.QName;
import net.sourceforge.htmlunit.xerces.xni.XMLAttributes;
import net.sourceforge.htmlunit.xerces.xni.XMLLocator;
import net.sourceforge.htmlunit.xerces.xni.XMLResourceIdentifier;
import net.sourceforge.htmlunit.xerces.xni.XMLString;
import net.sourceforge.htmlunit.xerces.xni.XNIException;
import net.sourceforge.htmlunit.xerces.xni.parser.XMLParserConfiguration;

/**
 * This is the base class of all DOM parsers. It implements the XNI
 * callback methods to create the DOM tree. After a successful parse of
 * an XML document, the DOM Document object can be queried using the
 * <code>getDocument</code> method. The actual pipeline is defined in
 * parser configuration.
 *
 * @author Arnaud Le Hors, IBM
 * @author Andy Clark, IBM
 * @author Elena Litani, IBM
 */
public class AbstractDOMParser extends AbstractXMLDocumentParser {

    /** Feature id: namespace. */
    protected static final String NAMESPACES =
    Constants.SAX_FEATURE_PREFIX+Constants.NAMESPACES_FEATURE;

    /** Feature id: create entity ref nodes. */
    protected static final String CREATE_ENTITY_REF_NODES =
    Constants.XERCES_FEATURE_PREFIX + Constants.CREATE_ENTITY_REF_NODES_FEATURE;

    /** Feature id: include comments. */
    protected static final String INCLUDE_COMMENTS_FEATURE =
    Constants.XERCES_FEATURE_PREFIX + Constants.INCLUDE_COMMENTS_FEATURE;

    /** Feature id: create cdata nodes. */
    protected static final String CREATE_CDATA_NODES_FEATURE =
    Constants.XERCES_FEATURE_PREFIX + Constants.CREATE_CDATA_NODES_FEATURE;

    /** Feature id: include ignorable whitespace. */
    protected static final String INCLUDE_IGNORABLE_WHITESPACE =
    Constants.XERCES_FEATURE_PREFIX + Constants.INCLUDE_IGNORABLE_WHITESPACE;


    /** Recognized features. */
    private static final String[] RECOGNIZED_FEATURES = {
        NAMESPACES,
        CREATE_ENTITY_REF_NODES,
        INCLUDE_COMMENTS_FEATURE,
        CREATE_CDATA_NODES_FEATURE,
        INCLUDE_IGNORABLE_WHITESPACE
    };

    /** Property id: document class name. */
    protected static final String DOCUMENT_CLASS_NAME =
    Constants.XERCES_PROPERTY_PREFIX + Constants.DOCUMENT_CLASS_NAME_PROPERTY;

    protected static final String  CURRENT_ELEMENT_NODE=
    Constants.XERCES_PROPERTY_PREFIX + Constants.CURRENT_ELEMENT_NODE_PROPERTY;

    /** Recognized properties. */
    private static final String[] RECOGNIZED_PROPERTIES = {
        DOCUMENT_CLASS_NAME,
        CURRENT_ELEMENT_NODE,
    };

    /** Default document class name. */
    protected static final String DEFAULT_DOCUMENT_CLASS_NAME =
    "net.sourceforge.htmlunit.xerces.dom.DocumentImpl";

    protected static final String CORE_DOCUMENT_CLASS_NAME =
    "net.sourceforge.htmlunit.xerces.dom.CoreDocumentImpl";

    private static final boolean DEBUG_EVENTS = false;
    private static final boolean DEBUG_BASEURI = false;

    /** DOM L3 error handler */
    protected final DOMErrorHandlerWrapper fErrorHandler = null;

    /** True if inside DTD. */
    protected boolean fInDTD;

    /** Create entity reference nodes. */
    protected boolean fCreateEntityRefNodes;

    /** Include ignorable whitespace. */
    protected boolean fIncludeIgnorableWhitespace;

    /** Include Comments. */
    protected boolean fIncludeComments;

    /** Create cdata nodes. */
    protected boolean fCreateCDATANodes;

    /** The document. */
    protected Document fDocument;

    /** The default Xerces document implementation, if used. */
    protected CoreDocumentImpl fDocumentImpl;

    /** The document class name to use. */
    protected String  fDocumentClassName;

    /** The document type node. */
    protected DocumentType fDocumentType;

    /** Current node. */
    protected Node fCurrentNode;
    protected CDATASection fCurrentCDATASection;
    protected EntityImpl fCurrentEntityDecl;
    protected int fDeferredEntityDecl;

    /** Character buffer */
    protected final StringBuffer fStringBuffer = new StringBuffer (50);

    // internal subset

    /** Internal subset buffer. */
    protected StringBuffer fInternalSubset;

    // deferred expansion data

    protected boolean              fNamespaceAware;
    protected int                  fDocumentIndex;
    protected int                  fDocumentTypeIndex;
    protected int                  fCurrentNodeIndex;
    protected int                  fCurrentCDATASectionIndex;

    // state

    /** True if inside DTD external subset. */
    protected boolean fInDTDExternalSubset;

    /** Root element node. */
    protected Node fRoot;

    /** True if inside CDATA section. */
    protected boolean fInCDATASection;

    /** True if saw the first chunk of characters*/
    protected boolean fFirstChunk = false;


    /** LSParserFilter: specifies that element with given QNAME and all its children
     * must be rejected */
    protected final boolean fFilterReject = false;

    // data

    /** Base uri stack*/
    protected final Stack<String> fBaseURIStack = new Stack<>();

    /** LSParserFilter: tracks the element depth within a rejected subtree. */
    protected int fRejectedElementDepth = 0;

    /** LSParserFilter: store depth of skipped elements */
    protected Stack<Boolean> fSkippedElemStack = null;

    /** LSParserFilter: true if inside entity reference */
    protected boolean fInEntityRef = false;

    /** Attribute QName. */
    private final QName fAttrQName = new QName();

    /** Document locator. */
    private XMLLocator fLocator;

    // Default constructor.
    protected AbstractDOMParser (XMLParserConfiguration config) {
        super (config);

        // add recognized features
        fConfiguration.addRecognizedFeatures (RECOGNIZED_FEATURES);

        // set default values
        fConfiguration.setFeature (CREATE_ENTITY_REF_NODES, true);
        fConfiguration.setFeature (INCLUDE_IGNORABLE_WHITESPACE, true);
        fConfiguration.setFeature (INCLUDE_COMMENTS_FEATURE, true);
        fConfiguration.setFeature (CREATE_CDATA_NODES_FEATURE, true);

        // add recognized properties
        fConfiguration.addRecognizedProperties (RECOGNIZED_PROPERTIES);

        // set default values
        fConfiguration.setProperty (DOCUMENT_CLASS_NAME,
        DEFAULT_DOCUMENT_CLASS_NAME);
    }

    /**
     * @return the name of current document class.
     */
    protected String getDocumentClassName () {
        return fDocumentClassName;
    }

    /**
     * This method allows the programmer to decide which document
     * factory to use when constructing the DOM tree. However, doing
     * so will lose the functionality of the default factory. Also,
     * a document class other than the default will lose the ability
     * to defer node expansion on the DOM tree produced.
     *
     * @param documentClassName The fully qualified class name of the
     *                      document factory to use when constructing
     *                      the DOM tree.
     *
     * @see #getDocumentClassName
     * @see #DEFAULT_DOCUMENT_CLASS_NAME
     */
    protected void setDocumentClassName (String documentClassName) {

        // normalize class name
        if (documentClassName == null) {
            documentClassName = DEFAULT_DOCUMENT_CLASS_NAME;
        }

        if (!documentClassName.equals(DEFAULT_DOCUMENT_CLASS_NAME)) {
            // verify that this class exists and is of the right type
            try {
                Class<?> _class = ObjectFactory.findProviderClass (documentClassName,
                ObjectFactory.findClassLoader (), true);
                //if (!_class.isAssignableFrom(Document.class)) {
                if (!Document.class.isAssignableFrom (_class)) {
                    throw new IllegalArgumentException (
                        DOMMessageFormatter.formatMessage(
                        DOMMessageFormatter.DOM_DOMAIN,
                        "InvalidDocumentClassName", new Object [] {documentClassName}));
                }
            }
            catch (ClassNotFoundException e) {
                throw new IllegalArgumentException (
                    DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    "MissingDocumentClassName", new Object [] {documentClassName}));
            }
        }

        // set document class name
        fDocumentClassName = documentClassName;
    }

    /** @return the DOM document object. */
    public Document getDocument () {
        return fDocument;
    }

    /**
     * Drops all references to the last DOM which was built by this parser.
     */
    public final void dropDocumentReferences() {
        fDocument = null;
        fDocumentImpl = null;
        fDocumentType = null;
        fCurrentNode = null;
        fCurrentCDATASection = null;
        fCurrentEntityDecl = null;
        fRoot = null;
    }

    /**
     * Resets the parser state.
     *
     * @throws XNIException Thrown on initialization error.
     */
    @Override
    public void reset () throws XNIException {
        super.reset ();


        // get feature state
        fCreateEntityRefNodes =
        fConfiguration.getFeature (CREATE_ENTITY_REF_NODES);

        fIncludeIgnorableWhitespace =
        fConfiguration.getFeature (INCLUDE_IGNORABLE_WHITESPACE);

        fNamespaceAware = fConfiguration.getFeature (NAMESPACES);

        fIncludeComments = fConfiguration.getFeature (INCLUDE_COMMENTS_FEATURE);

        fCreateCDATANodes = fConfiguration.getFeature (CREATE_CDATA_NODES_FEATURE);

        // get property
        setDocumentClassName ((String)
        fConfiguration.getProperty (DOCUMENT_CLASS_NAME));

        // reset dom information
        fDocument = null;
        fDocumentImpl = null;
        fDocumentType = null;
        fDocumentTypeIndex = -1;
        fCurrentNode = null;

        // reset string buffer
        fStringBuffer.setLength (0);

        // reset state information
        fRoot = null;
        fInDTD = false;
        fInDTDExternalSubset = false;
        fInCDATASection = false;
        fFirstChunk = false;
        fCurrentCDATASection = null;
        fCurrentCDATASectionIndex = -1;

        fBaseURIStack.removeAllElements ();


    } // reset()

    /**
     * Set the locale to use for messages.
     *
     * @param locale The locale object to use for localization of messages.
     *
     */
    public void setLocale (Locale locale) {
        fConfiguration.setLocale (locale);

    } // setLocale(Locale)

    //
    // XMLDocumentHandler methods
    //

    /**
     * This method notifies the start of a general entity.
     * <p>
     * <strong>Note:</strong> This method is not called for entity references
     * appearing as part of attribute values.
     *
     * @param name     The name of the general entity.
     * @param identifier The resource identifier.
     * @param encoding The auto-detected IANA encoding name of the entity
     *                 stream. This value will be null in those situations
     *                 where the entity encoding is not auto-detected (e.g.
     *                 internal entities or a document entity that is
     *                 parsed from a java.io.Reader).
     * @param augs     Additional information that may include infoset augmentations
     *
     * @exception XNIException Thrown by handler to signal an error.
     */
    @Override
    public void startGeneralEntity (String name,
    XMLResourceIdentifier identifier,
    String encoding, Augmentations augs)
    throws XNIException {
        if (DEBUG_EVENTS) {
            System.out.println ("==>startGeneralEntity ("+name+")");
            if (DEBUG_BASEURI) {
                System.out.println ("   expandedSystemId( **baseURI): "+identifier.getExpandedSystemId ());
                System.out.println ("   baseURI:"+ identifier.getBaseSystemId ());
            }
        }

        // Always create entity reference nodes to be able to recreate
        // entity as a part of doctype
        if (fFilterReject) {
            return;
        }
        setCharacterData (true);
        EntityReference er = fDocument.createEntityReference (name);
        if (fDocumentImpl != null) {
            // REVISIT: baseURI/actualEncoding
            //         remove dependency on our implementation when DOM L3 is REC
            //

            EntityReferenceImpl erImpl =(EntityReferenceImpl)er;

            // set base uri
            erImpl.setBaseURI (identifier.getExpandedSystemId ());
            if (fDocumentType != null) {
                // set actual encoding
                NamedNodeMap entities = fDocumentType.getEntities ();
                fCurrentEntityDecl = (EntityImpl) entities.getNamedItem (name);
                if (fCurrentEntityDecl != null) {
                    fCurrentEntityDecl.setInputEncoding (encoding);
                }

            }
            // we don't need synchronization now, because entity ref will be
            // expanded anyway. Synch only needed when user creates entityRef node
            erImpl.needsSyncChildren (false);
        }
        fInEntityRef = true;
        fCurrentNode.appendChild (er);
        fCurrentNode = er;
    }

    /**
     * Notifies of the presence of a TextDecl line in an entity. If present,
     * this method will be called immediately following the startEntity call.
     * <p>
     * <strong>Note:</strong> This method will never be called for the
     * document entity; it is only called for external general entities
     * referenced in document content.
     * <p>
     * <strong>Note:</strong> This method is not called for entity references
     * appearing as part of attribute values.
     *
     * @param version  The XML version, or null if not specified.
     * @param encoding The IANA encoding name of the entity.
     * @param augs       Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void textDecl (String version, String encoding, Augmentations augs) throws XNIException {
        if (fInDTD){
            return;
        }
        if (fCurrentEntityDecl != null && !fFilterReject) {
            fCurrentEntityDecl.setXmlEncoding (encoding);
            if (version != null)
                fCurrentEntityDecl.setXmlVersion (version);
        }
    }

    /**
     * A comment.
     *
     * @param text The text in the comment.
     * @param augs       Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by application to signal an error.
     */
    @Override
    public void comment (XMLString text, Augmentations augs) throws XNIException {
        if (fInDTD) {
            if (fInternalSubset != null && !fInDTDExternalSubset) {
                fInternalSubset.append ("<!--");
                if (text.length > 0) {
                    fInternalSubset.append (text.ch, text.offset, text.length);
                }
                fInternalSubset.append ("-->");
            }
            return;
        }
        if (!fIncludeComments || fFilterReject) {
            return;
        }
        Comment comment = fDocument.createComment (text.toString ());

        setCharacterData (false);
        fCurrentNode.appendChild (comment);
    }

    /**
     * A processing instruction. Processing instructions consist of a
     * target name and, optionally, text data. The data is only meaningful
     * to the application.
     * <p>
     * Typically, a processing instruction's data will contain a series
     * of pseudo-attributes. These pseudo-attributes follow the form of
     * element attributes but are <strong>not</strong> parsed or presented
     * to the application as anything other than text. The application is
     * responsible for parsing the data.
     *
     * @param target The target.
     * @param data   The data or null if none specified.
     * @param augs       Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void processingInstruction (String target, XMLString data, Augmentations augs)
    throws XNIException {

        if (fInDTD) {
            if (fInternalSubset != null && !fInDTDExternalSubset) {
                fInternalSubset.append ("<?");
                fInternalSubset.append (target);
                if (data.length > 0) {
                    fInternalSubset.append (' ').append (data.ch, data.offset, data.length);
                }
                fInternalSubset.append ("?>");
            }
            return;
        }

        if (DEBUG_EVENTS) {
            System.out.println ("==>processingInstruction ("+target+")");
        }
        if (fFilterReject) {
            return;
        }
        ProcessingInstruction pi =
        fDocument.createProcessingInstruction (target, data.toString ());


        setCharacterData (false);
        fCurrentNode.appendChild (pi);
    }

    /**
     * The start of the document.
     *
     * @param locator The system identifier of the entity if the entity
     *                 is external, null otherwise.
     * @param encoding The auto-detected IANA encoding name of the entity
     *                 stream. This value will be null in those situations
     *                 where the entity encoding is not auto-detected (e.g.
     *                 internal entities or a document entity that is
     *                 parsed from a java.io.Reader).
     * @param namespaceContext
     *                 The namespace context in effect at the
     *                 start of this document.
     *                 This object represents the current context.
     *                 Implementors of this class are responsible
     *                 for copying the namespace bindings from the
     *                 the current context (and its parent contexts)
     *                 if that information is important.
     * @param augs     Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void startDocument (XMLLocator locator, String encoding,
    NamespaceContext namespaceContext, Augmentations augs)
    throws XNIException {

        fLocator = locator;
        if (fDocumentClassName.equals (DEFAULT_DOCUMENT_CLASS_NAME)) {
            fDocument = new DocumentImpl ();
            fDocumentImpl = (CoreDocumentImpl)fDocument;
            // REVISIT: when DOM Level 3 is REC rely on Document.support
            //          instead of specific class
            // set DOM error checking off
            fDocumentImpl.setStrictErrorChecking (false);
            // set actual encoding
            fDocumentImpl.setInputEncoding (encoding);
            // set documentURI
            fDocumentImpl.setDocumentURI (locator.getExpandedSystemId ());
        }
        else {
            // use specified document class
            try {
                ClassLoader cl = ObjectFactory.findClassLoader();
                Class<?> documentClass = ObjectFactory.findProviderClass (fDocumentClassName,
                    cl, true);
                fDocument = (Document)documentClass.newInstance ();

                // if subclass of our own class that's cool too
                Class<?> defaultDocClass =
                ObjectFactory.findProviderClass (CORE_DOCUMENT_CLASS_NAME,
                    cl, true);
                if (defaultDocClass.isAssignableFrom (documentClass)) {
                    fDocumentImpl = (CoreDocumentImpl)fDocument;

                    // REVISIT: when DOM Level 3 is REC rely on
                    //          Document.support instead of specific class
                    // set DOM error checking off
                    fDocumentImpl.setStrictErrorChecking (false);
                    // set actual encoding
                    fDocumentImpl.setInputEncoding (encoding);
                    // set documentURI
                    if (locator != null) {
                        fDocumentImpl.setDocumentURI (locator.getExpandedSystemId ());
                    }
                }
            }
            catch (ClassNotFoundException e) {
                // won't happen we already checked that earlier
            }
            catch (Exception e) {
                throw new RuntimeException (
                    DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    "CannotCreateDocumentClass",
                    new Object [] {fDocumentClassName}));
            }
        }
        fCurrentNode = fDocument;
    } // startDocument(String,String)

    /**
     * Notifies of the presence of an XMLDecl line in the document. If
     * present, this method will be called immediately following the
     * startDocument call.
     *
     * @param version    The XML version.
     * @param encoding   The IANA encoding name of the document, or null if
     *                   not specified.
     * @param standalone The standalone value, or null if not specified.
     * @param augs       Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void xmlDecl (String version, String encoding, String standalone,
    Augmentations augs)
    throws XNIException {
        // REVISIT: when DOM Level 3 is REC rely on Document.support
        //          instead of specific class
        if (fDocumentImpl != null) {
            if (version != null)
                fDocumentImpl.setXmlVersion (version);
            fDocumentImpl.setXmlEncoding (encoding);
            fDocumentImpl.setXmlStandalone ("yes".equals (standalone));
        }
    }

    /**
     * Notifies of the presence of the DOCTYPE line in the document.
     *
     * @param rootElement The name of the root element.
     * @param publicId    The public identifier if an external DTD or null
     *                    if the external DTD is specified using SYSTEM.
     * @param systemId    The system identifier if an external DTD, null
     *                    otherwise.
     * @param augs     Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void doctypeDecl (String rootElement,
    String publicId, String systemId, Augmentations augs)
    throws XNIException {
        if (fDocumentImpl != null) {
            fDocumentType = fDocumentImpl.createDocumentType (
            rootElement, publicId, systemId);
            fCurrentNode.appendChild (fDocumentType);
        }
    }

    /**
     * The start of an element. If the document specifies the start element
     * by using an empty tag, then the startElement method will immediately
     * be followed by the endElement method, with no intervening methods.
     *
     * @param element    The name of the element.
     * @param attributes The element attributes.
     * @param augs     Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void startElement (QName element, XMLAttributes attributes, Augmentations augs)
    throws XNIException {
        if (DEBUG_EVENTS) {
            System.out.println ("==>startElement ("+element.rawname+")");
        }
        if (fFilterReject) {
            ++fRejectedElementDepth;
            return;
        }
        Element el = createElementNode (element);
        int attrCount = attributes.getLength ();
        boolean seenSchemaDefault = false;
        for (int i = 0; i < attrCount; i++) {
            attributes.getName (i, fAttrQName);
            Attr attr = createAttrNode (fAttrQName);

            String attrValue = attributes.getValue (i);

            attr.setValue (attrValue);
            boolean specified = attributes.isSpecified(i);
            // Take special care of schema defaulted attributes. Calling the
            // non-namespace aware setAttributeNode() method could overwrite
            // another attribute with the same local name.
            if (!specified && (seenSchemaDefault || (fAttrQName.uri != null &&
                fAttrQName.uri != NamespaceContext.XMLNS_URI && fAttrQName.prefix == null))) {
                el.setAttributeNodeNS(attr);
                seenSchemaDefault = true;
            }
            else {
                el.setAttributeNode(attr);
            }
            // NOTE: The specified value MUST be set after you set
            //       the node value because that turns the "specified"
            //       flag to "true" which may overwrite a "false"
            //       value from the attribute list. -Ac
            if (fDocumentImpl != null) {
                AttrImpl attrImpl = (AttrImpl) attr;
                Object type = null;
                boolean id = false;

                // DTD
                boolean isDeclared = Boolean.TRUE.equals (attributes.getAugmentations (i).getItem (Constants.ATTRIBUTE_DECLARED));
                // For DOM Level 3 TypeInfo, the type name must
                // be null if this attribute has not been declared
                // in the DTD.
                if (isDeclared) {
                    type = attributes.getType (i);
                    id = "ID".equals (type);
                }
                attrImpl.setType (type);

                if (id) {
                    ((ElementImpl) el).setIdAttributeNode (attr, true);
                }

                attrImpl.setSpecified (specified);
                // REVISIT: Handle entities in attribute value.
            }
        }
        setCharacterData (false);

        fCurrentNode.appendChild (el);
        fCurrentNode = el;
    }


    /**
     * An empty element.
     *
     * @param element    The name of the element.
     * @param attributes The element attributes.
     * @param augs   Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void emptyElement (QName element, XMLAttributes attributes, Augmentations augs)
    throws XNIException {

        startElement (element, attributes, augs);
        endElement (element, augs);

    } // emptyElement(QName,XMLAttributes)

    /**
     * Character content.
     *
     * @param text The content.
     * @param augs     Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void characters (XMLString text, Augmentations augs) throws XNIException {

        if (DEBUG_EVENTS) {
            System.out.println ("==>characters(): "+text.toString ());
        }

        if (fFilterReject) {
            return;
        }
        if (fInCDATASection && fCreateCDATANodes) {
            if (fCurrentCDATASection == null) {
                fCurrentCDATASection =
                fDocument.createCDATASection (text.toString ());
                fCurrentNode.appendChild (fCurrentCDATASection);
                fCurrentNode = fCurrentCDATASection;
            }
            else {
                fCurrentCDATASection.appendData (text.toString ());
            }
        }
        else if (!fInDTD) {
            // if type is union (XML Schema) it is possible that we receive
            // character call with empty data
            if (text.length == 0) {
                return;
            }

            Node child = fCurrentNode.getLastChild ();
            if (child != null && child.getNodeType () == Node.TEXT_NODE) {
                // collect all the data into the string buffer.
                if (fFirstChunk) {
                    if (fDocumentImpl != null) {
                        fStringBuffer.append (((TextImpl)child).removeData ());
                    } else {
                        fStringBuffer.append (((Text)child).getData ());
                        child.setNodeValue (null);
                    }
                    fFirstChunk = false;
                }
                if (text.length > 0) {
                    fStringBuffer.append (text.ch, text.offset, text.length);
                }
            }
            else {
                fFirstChunk = true;
                Text textNode = fDocument.createTextNode (text.toString());
                fCurrentNode.appendChild (textNode);
            }

        }
    }

    /**
     * Ignorable whitespace. For this method to be called, the document
     * source must have some way of determining that the text containing
     * only whitespace characters should be considered ignorable. For
     * example, the validator can determine if a length of whitespace
     * characters in the document are ignorable based on the element
     * content model.
     *
     * @param text The ignorable whitespace.
     * @param augs     Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void ignorableWhitespace (XMLString text, Augmentations augs) throws XNIException {

        if (!fIncludeIgnorableWhitespace || fFilterReject) {
            return;
        }
        Node child = fCurrentNode.getLastChild ();
        if (child != null && child.getNodeType () == Node.TEXT_NODE) {
            Text textNode = (Text)child;
            textNode.appendData (text.toString ());
        }
        else {
            Text textNode = fDocument.createTextNode (text.toString ());
            if (fDocumentImpl != null) {
                TextImpl textNodeImpl = (TextImpl)textNode;
                textNodeImpl.setIgnorableWhitespace (true);
            }
            fCurrentNode.appendChild (textNode);
        }
    }

    /**
     * The end of an element.
     *
     * @param element The name of the element.
     * @param augs     Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void endElement (QName element, Augmentations augs) throws XNIException {
        if (DEBUG_EVENTS) {
            System.out.println ("==>endElement ("+element.rawname+")");
        }
        setCharacterData (false);
        fCurrentNode = fCurrentNode.getParentNode ();
    }


    /**
     * The start of a CDATA section.
     * @param augs     Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void startCDATA (Augmentations augs) throws XNIException {

        fInCDATASection = true;
        if (fFilterReject) {
            return;
        }
        if (fCreateCDATANodes) {
            setCharacterData (false);
        }
    }

    /**
     * The end of a CDATA section.
     * @param augs     Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void endCDATA (Augmentations augs) throws XNIException {

        fInCDATASection = false;
        if (fFilterReject) {
            return;
        }

        if (fCurrentCDATASection !=null) {
            fCurrentNode = fCurrentNode.getParentNode ();
            fCurrentCDATASection = null;
        }
    }

    /**
     * The end of the document.
     * @param augs     Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void endDocument (Augmentations augs) throws XNIException {
        // REVISIT: when DOM Level 3 is REC rely on Document.support
        //          instead of specific class
        // set the actual encoding and set DOM error checking back on
        if (fDocumentImpl != null) {
            if (fLocator != null) {
                fDocumentImpl.setInputEncoding (fLocator.getEncoding());
            }
            fDocumentImpl.setStrictErrorChecking (true);
        }
        fCurrentNode = null;
    }

    /**
     * This method notifies the end of a general entity.
     * <p>
     * <strong>Note:</strong> This method is not called for entity references
     * appearing as part of attribute values.
     *
     * @param name   The name of the entity.
     * @param augs   Additional information that may include infoset augmentations
     *
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    @Override
    public void endGeneralEntity (String name, Augmentations augs) throws XNIException {
        if (DEBUG_EVENTS) {
            System.out.println ("==>endGeneralEntity: ("+name+")");
        }

        if (fFilterReject) {
            return;
        }
        setCharacterData (true);

        if (fDocumentType != null) {
            // get current entity declaration
            NamedNodeMap entities = fDocumentType.getEntities ();
            fCurrentEntityDecl = (EntityImpl) entities.getNamedItem (name);
            if (fCurrentEntityDecl != null) {
                if (fCurrentEntityDecl.getFirstChild () == null) {
                    fCurrentEntityDecl.setReadOnly (false, true);
                    Node child = fCurrentNode.getFirstChild ();
                    while (child != null) {
                        Node copy = child.cloneNode (true);
                        fCurrentEntityDecl.appendChild (copy);
                        child = child.getNextSibling ();
                    }
                    fCurrentEntityDecl.setReadOnly (true, true);

                    //entities.setNamedItem(fCurrentEntityDecl);
                }
                fCurrentEntityDecl = null;
            }

        }
        fInEntityRef = false;
        if (fCreateEntityRefNodes) {
            if (fDocumentImpl != null) {
                // Make entity ref node read only
                ((NodeImpl)fCurrentNode).setReadOnly (true, true);
            }
        }

        if (!fCreateEntityRefNodes) {
            // move entity reference children to the list of
            // siblings of its parent and remove entity reference
            NodeList children = fCurrentNode.getChildNodes ();
            Node parent = fCurrentNode.getParentNode ();
            int length = children.getLength ();
            if (length > 0) {

                // get previous sibling of the entity reference
                Node node = fCurrentNode.getPreviousSibling ();
                // normalize text nodes
                Node child = children.item (0);
                if (node != null && node.getNodeType () == Node.TEXT_NODE &&
                child.getNodeType () == Node.TEXT_NODE) {
                    ((Text)node).appendData (child.getNodeValue ());
                    fCurrentNode.removeChild (child);

                } else {
                    node = parent.insertBefore (child, fCurrentNode);
                    handleBaseURI (node);
                }

                for (int i=1;i <length;i++) {
                    node = parent.insertBefore (children.item (0), fCurrentNode);
                    handleBaseURI (node);
                }
            } // length > 0
            parent.removeChild (fCurrentNode);
            fCurrentNode = parent;
        }
    }


    /**
     * Record baseURI information for the Element (by adding xml:base attribute)
     * or for the ProcessingInstruction (by setting a baseURI field)
     * Non deferred DOM.
     *
     * @param node the node
     */
    protected final void handleBaseURI (Node node){
        if (fDocumentImpl != null) {
            // REVISIT: remove dependency on our implementation when
            //          DOM L3 becomes REC

            String baseURI;
            short nodeType = node.getNodeType ();

            if (nodeType == Node.ELEMENT_NODE) {
                // if an element already has xml:base attribute
                // do nothing
                if (fNamespaceAware) {
                    if (((Element)node).getAttributeNodeNS ("http://www.w3.org/XML/1998/namespace","base")!=null) {
                        return;
                    }
                } else if (((Element)node).getAttributeNode ("xml:base") != null) {
                    return;
                }
                // retrive the baseURI from the entity reference
                baseURI = fCurrentNode.getBaseURI ();
                if (baseURI !=null && !baseURI.equals (fDocumentImpl.getDocumentURI ())) {
                    if (fNamespaceAware) {
                        ((Element)node).setAttributeNS ("http://www.w3.org/XML/1998/namespace", "xml:base", baseURI);
                    } else {
                        ((Element)node).setAttribute ("xml:base", baseURI);
                    }
                }
            }
            else if (nodeType == Node.PROCESSING_INSTRUCTION_NODE) {

                baseURI = fCurrentNode.getBaseURI ();
                if (baseURI !=null && fErrorHandler != null) {
                    DOMErrorImpl error = new DOMErrorImpl ();
                    error.fType = "pi-base-uri-not-preserved";
                    error.fRelatedData = baseURI;
                    error.fSeverity = DOMError.SEVERITY_WARNING;
                    fErrorHandler.getErrorHandler ().handleError (error);
                }
            }
        }
    }

    // method to create an element node.
    // subclasses can override this method to create element nodes in other ways.
    protected Element createElementNode (QName element) {
        Element el;

        if (fNamespaceAware) {
            // if we are using xerces DOM implementation, call our
            // own constructor to reuse the strings we have here.
            if (fDocumentImpl != null) {
                el = fDocumentImpl.createElementNS (element.uri, element.rawname,
                element.localpart);
            }
            else {
                el = fDocument.createElementNS (element.uri, element.rawname);
            }
        }
        else {
            el = fDocument.createElement (element.rawname);
        }

        return el;
    }

    // method to create an attribute node.
    // subclasses can override this method to create attribute nodes in other ways.
    protected Attr createAttrNode (QName attrQName) {
        Attr attr;

        if (fNamespaceAware) {
            if (fDocumentImpl != null) {
                // if we are using xerces DOM implementation, call our
                // own constructor to reuse the strings we have here.
                attr = fDocumentImpl.createAttributeNS (attrQName.uri,
                attrQName.rawname,
                attrQName.localpart);
            }
            else {
                attr = fDocument.createAttributeNS (attrQName.uri,
                attrQName.rawname);
            }
        }
        else {
            attr = fDocument.createAttribute (attrQName.rawname);
        }

        return attr;
    }

    /*
     * When the first characters() call is received, the data is stored in
     * a new Text node. If right after the first characters() we receive another chunk of data,
     * the data from the Text node, following the new characters are appended
     * to the fStringBuffer and the text node data is set to empty.
     *
     * This function is called when the state is changed and the
     * data must be appended to the current node.
     *
     * Note: if DOMFilter is set, you must make sure that if Node is skipped,
     * or removed fFistChunk must be set to true, otherwise some data can be lost.
     *
     */
    protected void  setCharacterData (boolean sawChars){

        // handle character data
        fFirstChunk = sawChars;


        // if we have data in the buffer we must have created
        // a text node already.

        Node child = fCurrentNode.getLastChild ();
        if (child != null) {
            if (fStringBuffer.length () > 0) {
                // REVISIT: should this check be performed?
                if (child.getNodeType () == Node.TEXT_NODE) {
                    if (fDocumentImpl != null) {
                        ((TextImpl)child).replaceData (fStringBuffer.toString ());
                    }
                    else {
                        ((Text)child).setData (fStringBuffer.toString ());
                    }
                }
                // reset string buffer
                fStringBuffer.setLength (0);
            }

        } // end-if child !=null
    }
}
