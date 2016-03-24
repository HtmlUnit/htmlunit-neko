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

package net.sourceforge.htmlunit.cyberneko;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.apache.xerces.util.XMLStringBuffer;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import net.sourceforge.htmlunit.cyberneko.filters.DefaultFilter;

/**
 * This class implements an filter to output "canonical" files for
 * regression testing.
 *
 * @author Andy Clark
 */
public class Writer
    extends DefaultFilter {

    //
    // Data
    //

    /** Writer. */
    protected PrintWriter out = new PrintWriter(System.out);

    // temp vars

    /** String buffer for collecting text content. */
    private final XMLStringBuffer fStringBuffer = new XMLStringBuffer();

    /** Are we currently in the middle of a block of characters? */
    private boolean fInCharacters = false;

    /**
     * Beginning line number of the current block of characters (which may be
     * reported in several characters chunks).  Will be -1 if the parser
     * isn't producing HTML augmentations.
     */
    private int fCharactersBeginLine = -1;

    /**
     * Beginning column number of the current block of characters (which may be
     * reported in several characters chunks).  Will be -1 if the parser
     * isn't producing HTML augmentations.
     */
    private int fCharactersBeginColumn = -1;

    /**
     * Beginning character offset of the current block of characters (which may
     * be reported in several characters chunks).  Will be -1 if the parser
     * isn't producing HTML augmentations.
     */
    private int fCharactersBeginCharacterOffset = -1;

    /**
     * Ending line number of the current block of characters (which may be
     * reported in several characters chunks).  Will be -1 if the parser
     * isn't producing HTML augmentations.
     */
    private int fCharactersEndLine = -1;

    /**
     * Ending column number of the current block of characters (which may be
     * reported in several characters chunks).  Will be -1 if the parser
     * isn't producing HTML augmentations.
     */
    private int fCharactersEndColumn = -1;

    /**
     * Ending character offset of the current block of characters (which may be
     * reported in several characters chunks).  Will be -1 if the parser isn't
     * producing HTML augmentations.
     */
    private int fCharactersEndCharacterOffset = -1;

    //
    // Constructors
    //

    /** 
     * Creates a writer to the standard output stream using UTF-8 
     * encoding. 
     */
    public Writer() {
        this(System.out);
    } // <init>()

    /** 
     * Creates a writer with the specified output stream using UTF-8 
     * encoding. 
     */
    public Writer(OutputStream stream) {
        this(stream, "UTF8");
    } // <init>(OutputStream)

    /** Creates a writer with the specified output stream and encoding. */
    public Writer(OutputStream stream, String encoding) {
        try {
            out = new PrintWriter(new OutputStreamWriter(stream, encoding), true);
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException("JVM must have "+encoding+" decoder");
        }
    } // <init>(OutputStream,String)

    /** Creates a writer with the specified Java Writer. */
    public Writer(java.io.Writer writer) {
        out = new PrintWriter(writer);
    } // <init>(java.io.Writer)

    //
    // XMLDocumentHandler methods
    //

    // since Xerces-J 2.2.0

    /** Start document. */
    public void startDocument(XMLLocator locator, String encoding, 
                              NamespaceContext nscontext, Augmentations augs) throws XNIException {
        fStringBuffer.clear();
    } // startDocument(XMLLocator,String,NamespaceContext,Augmentations)

    /** End document. */
    public void endDocument(Augmentations augs) throws XNIException {
        chars();
    }
    // old methods

    /** Start document. */
    public void startDocument(XMLLocator locator, String encoding, Augmentations augs) throws XNIException {
        startDocument(locator, encoding, null, augs);
    } // startDocument(XMLLocator,String,Augmentations)
    
    /** XML declaration. */
    public void xmlDecl(String version, String encoding, String standalone,
                        Augmentations augs) throws XNIException {
        doAugs(augs);
        if (version!=null) {
            out.print("xversion ");
            out.println(version);
        }
        if (encoding!=null) {
            out.print("xencoding ");
            out.println(encoding);
        }
        if (standalone!=null) {
            out.print("xstandalone ");
            out.println(standalone);
        }
        out.flush();
    } // xmlDecl(String,String,String,Augmentations)

    /** Doctype declaration. */
    public void doctypeDecl(String root, String pubid, String sysid, Augmentations augs) throws XNIException {
        chars();
        doAugs(augs);
        out.print('!');
        if (root != null) {
            out.print(root);
        }
        out.println();
        if (pubid != null) {
            out.print('p');
            out.print(pubid);
            out.println();
        }
        if (sysid != null) {
            out.print('s');
            out.print(sysid);
            out.println();
        }
        out.flush();
    } // doctypeDecl(String,String,String,Augmentations)

    /** Processing instruction. */
    public void processingInstruction(String target, XMLString data, Augmentations augs) throws XNIException {
        chars();
        doAugs(augs);
        out.print('?');
        out.print(target);
        if (data != null && data.length > 0) {
            out.print(' ');
            print(data.toString());
        }
        out.println();
        out.flush();
    } // processingInstruction(String,XMLString,Augmentations)

    /** Comment. */
    public void comment(XMLString text, Augmentations augs) throws XNIException {
        chars();
        doAugs(augs);
        out.print('#');
        print(text.toString());
        out.println();
        out.flush();
    } // comment(XMLString,Augmentations)

    /** Start element. */
    public void startElement(QName element, XMLAttributes attrs, Augmentations augs) throws XNIException {
        chars();
        doAugs(augs);
        out.print('(');
        out.print(element.rawname);
        int acount = attrs != null ? attrs.getLength() : 0;
        if (acount > 0) {
            String[] anames = new String[acount];
            String[] auris = new String[acount];
            sortAttrNames(attrs, anames, auris);
            for (int i = 0; i < acount; i++) {
                String aname = anames[i];
                out.println();
                out.flush();
                out.print('A');
                if (auris[i] != null) {
                    out.print('{');
                    out.print(auris[i]);
                    out.print('}');
                }
                out.print(aname);
                out.print(' ');
                print(attrs.getValue(aname));
            }
        }
        out.println();
        out.flush();
    } // startElement(QName,XMLAttributes,Augmentations)

    /** End element. */
    public void endElement(QName element, Augmentations augs) throws XNIException {
        chars();
        doAugs(augs);
        out.print(')');
        out.print(element.rawname);
        out.println();
        out.flush();
    } // endElement(QName,Augmentations)

    /** Empty element. */
    public void emptyElement(QName element, XMLAttributes attrs, Augmentations augs) throws XNIException {
        startElement(element, attrs, augs);
        endElement(element, augs);
    } // emptyElement(QName,XMLAttributes,Augmentations)

    /** Characters. */
    public void characters(XMLString text, Augmentations augs) throws XNIException {
        storeCharactersEnd(augs);
        if(!fInCharacters) {
            storeCharactersStart(augs);
        }
        fInCharacters = true;
        fStringBuffer.append(text);
    } // characters(XMLString,Augmentations)

    /** Ignorable whitespace. */
    public void ignorableWhitespace(XMLString text, Augmentations augs) throws XNIException {
        characters(text, augs);
    } // ignorableWhitespace(XMLString,Augmentations)

    
    public void startCDATA(Augmentations augs) throws XNIException {
        chars();
        doAugs(augs);
        out.println("((CDATA");
    }
    
    public void endCDATA(Augmentations augs) throws XNIException {
        chars();
        doAugs(augs);
        out.println("))CDATA");
        out.flush();
    }
    //
    // Protected methods
    //

    /** Prints collected characters. */
    protected void chars() {
        fInCharacters = false;
        if (fStringBuffer.length == 0) {
            return;
        }
        doCharactersAugs();
        out.print('"');
        print(fStringBuffer.toString());
        out.println();
        out.flush();
        fStringBuffer.clear();
    } // chars()

    /** Prints the specified string. */
    protected void print(String s) {
        int length = s != null ? s.length() : 0;
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\n': {
                    out.print("\\n");
                    break;
                }
                case '\r': {
                    out.print("\\r");
                    break;
                }
                case '\t': {
                    out.print("\\t");
                    break;
                }
                case '\\': {
                    out.print("\\\\");
                    break;
                }
                default: {
                    out.print(c);
                }
            }
        }
    } // print(String)

    /**
     * Print out the HTML augmentations for the given augs.  Prints nothing if
     * there are no HTML augmentations available.
     */
    protected void doAugs(Augmentations augs) {
        HTMLEventInfo evInfo = (augs == null) ? null : (HTMLEventInfo)augs
                .getItem("http://cyberneko.org/html/features/augmentations");
        if(evInfo != null) {
            if(evInfo.isSynthesized()) {
                out.print("[synth]");
            }
            else {
                out.print('[');
                out.print(evInfo.getBeginLineNumber());
                out.print(',');
                out.print(evInfo.getBeginColumnNumber());
                out.print(',');
                out.print(evInfo.getBeginCharacterOffset());
                out.print(';');
                out.print(evInfo.getEndLineNumber());
                out.print(',');
                out.print(evInfo.getEndColumnNumber());
                out.print(',');
                out.print(evInfo.getEndCharacterOffset());
                out.print(']');
            }
        }
    } // doAugs(Augmentations)

    /**
     * Store the HTML augmentations for the given augs in temporary variables
     * for the start of the current block of characters.  Does nothing if there
     * are no HTML augmentations available.
     */
    protected void storeCharactersStart(Augmentations augs) {
        HTMLEventInfo evInfo = (augs == null) ? null : (HTMLEventInfo)augs
                .getItem("http://cyberneko.org/html/features/augmentations");
        if(evInfo != null) {
            fCharactersBeginLine = evInfo.getBeginLineNumber();
            fCharactersBeginColumn = evInfo.getBeginColumnNumber();
            fCharactersBeginCharacterOffset = evInfo.getBeginCharacterOffset();
        }
    } // storeCharactersStart(Augmentations)

    /**
     * Store the HTML augmentations for the given augs in temporary variables
     * for the end of the current block of characters.  Does nothing if there
     * are no HTML augmentations available.
     */
    protected void storeCharactersEnd(Augmentations augs) {
        HTMLEventInfo evInfo = (augs == null) ? null : (HTMLEventInfo)augs
                .getItem("http://cyberneko.org/html/features/augmentations");
        if(evInfo != null) {
            fCharactersEndLine = evInfo.getEndLineNumber();
            fCharactersEndColumn = evInfo.getEndColumnNumber();
            fCharactersEndCharacterOffset = evInfo.getEndCharacterOffset();
        }
    } // storeCharactersEnd(Augmentations)

    /**
     * Print out the HTML augmentation values for the current block of
     * characters.  Prints nothing if there were no HTML augmentations
     * available.
     */
    protected void doCharactersAugs() {
        if(fCharactersBeginLine >= 0) {
            out.print('[');
            out.print(fCharactersBeginLine);
            out.print(',');
            out.print(fCharactersBeginColumn);
            out.print(',');
            out.print(fCharactersBeginCharacterOffset);
            out.print(';');
            out.print(fCharactersEndLine);
            out.print(',');
            out.print(fCharactersEndColumn);
            out.print(',');
            out.print(fCharactersEndCharacterOffset);
            out.print(']');
        }
    } // doCharactersAugs()

    //
    // Protected static methods
    //

    /** Sorts the attribute names. */
    protected static void sortAttrNames(XMLAttributes attrs, 
                                        String[] anames, String[] auris) {
        for (int i = 0; i < anames.length; i++) {
            anames[i] = attrs.getQName(i);
            auris[i] = attrs.getURI(i);
        }
        // NOTE: This is super inefficient but it doesn't really matter. -Ac
        for (int i = 0; i < anames.length - 1; i++) {
            int index = i;
            for (int j = i + 1; j < anames.length; j++) {
                if (anames[j].compareTo(anames[index]) < 0) {
                    index = j;
                }
            }
            if (index != i) {
                String tn = anames[i];
                anames[i] = anames[index];
                anames[index] = tn;
                String tu = auris[i];
                auris[i] = auris[index];
                auris[index] = tu;
            }
        }
    } // sortAttrNames(XMLAttributes,String[])

    //
    // MAIN
    //

    /** Main program. */
    public static void main(String[] argv) throws Exception {
        org.apache.xerces.xni.parser.XMLDocumentFilter[] filters = {
            new Writer(),
        };
        org.apache.xerces.xni.parser.XMLParserConfiguration parser = new HTMLConfiguration();
        parser.setProperty("http://cyberneko.org/html/properties/filters", filters);
        for (int i = 0; i < argv.length; i++) {
            org.apache.xerces.xni.parser.XMLInputSource source =
                new org.apache.xerces.xni.parser.XMLInputSource(null, argv[i], null);
            parser.parse(source);
        }
    } // main(String[])

} // class Writer

