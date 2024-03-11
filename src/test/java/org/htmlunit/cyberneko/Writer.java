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
package org.htmlunit.cyberneko;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.htmlunit.cyberneko.filters.DefaultFilter;
import org.htmlunit.cyberneko.xerces.xni.Augmentations;
import org.htmlunit.cyberneko.xerces.xni.NamespaceContext;
import org.htmlunit.cyberneko.xerces.xni.QName;
import org.htmlunit.cyberneko.xerces.xni.XMLAttributes;
import org.htmlunit.cyberneko.xerces.xni.XMLLocator;
import org.htmlunit.cyberneko.xerces.xni.XMLString;
import org.htmlunit.cyberneko.xerces.xni.XNIException;

/**
 * This class implements an filter to output "canonical" files for
 * regression testing.
 *
 * @author Andy Clark
 */
public class Writer extends DefaultFilter {

    /** Writer. */
    private final PrintWriter out_;

    /** String buffer for collecting text content. */
    private final XMLString stringBuffer_ = new XMLString();

    /** Are we currently in the middle of a block of characters? */
    private boolean inCharacters_ = false;

    /**
     * Beginning line number of the current block of characters (which may be
     * reported in several characters chunks).  Will be -1 if the parser
     * isn't producing HTML augmentations.
     */
    private int charactersBeginLine_ = -1;

    /**
     * Beginning column number of the current block of characters (which may be
     * reported in several characters chunks).  Will be -1 if the parser
     * isn't producing HTML augmentations.
     */
    private int charactersBeginColumn_ = -1;

    /**
     * Beginning character offset of the current block of characters (which may
     * be reported in several characters chunks).  Will be -1 if the parser
     * isn't producing HTML augmentations.
     */
    private int charactersBeginCharacterOffset_ = -1;

    /**
     * Ending line number of the current block of characters (which may be
     * reported in several characters chunks).  Will be -1 if the parser
     * isn't producing HTML augmentations.
     */
    private int charactersEndLine_ = -1;

    /**
     * Ending column number of the current block of characters (which may be
     * reported in several characters chunks).  Will be -1 if the parser
     * isn't producing HTML augmentations.
     */
    private int charactersEndColumn_ = -1;

    /**
     * Ending character offset of the current block of characters (which may be
     * reported in several characters chunks).  Will be -1 if the parser isn't
     * producing HTML augmentations.
     */
    private int charactersEndCharacterOffset_ = -1;

    /** Creates a writer with the specified output stream and encoding. */
    public Writer(final OutputStream stream, final String encoding) {
        try {
            out_ = new PrintWriter(new OutputStreamWriter(stream, encoding), true);
        }
        catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("JVM must have " + encoding + " decoder");
        }
    }

    /** Creates a writer with the specified Java Writer. */
    public Writer(final java.io.Writer writer) {
        out_ = new PrintWriter(writer);
    }

    //
    // XMLDocumentHandler methods
    //

    // since Xerces-J 2.2.0

    /** Start document. */
    @Override
    public void startDocument(final XMLLocator locator, final String encoding, final NamespaceContext nscontext, final Augmentations augs) throws XNIException {
        stringBuffer_.clear();
    }

    /** End document. */
    @Override
    public void endDocument(final Augmentations augs) throws XNIException {
        chars();
    }

    /** XML declaration. */
    @Override
    public void xmlDecl(final String version, final String encoding, final String standalone, final Augmentations augs) throws XNIException {
        doAugs(augs);
        if (version != null) {
            out_.print("xversion ");
            out_.println(version);
        }
        if (encoding != null) {
            out_.print("xencoding ");
            out_.println(encoding);
        }
        if (standalone != null) {
            out_.print("xstandalone ");
            out_.println(standalone);
        }
        out_.flush();
    }

    /** Doctype declaration. */
    @Override
    public void doctypeDecl(final String root, final String pubid, final String sysid, final Augmentations augs) throws XNIException {
        chars();
        doAugs(augs);
        out_.print('!');
        if (root != null) {
            out_.print(root);
        }
        out_.println();
        if (pubid != null) {
            out_.print('p');
            out_.print(pubid);
            out_.println();
        }
        if (sysid != null) {
            out_.print('s');
            out_.print(sysid);
            out_.println();
        }
        out_.flush();
    }

    /** Processing instruction. */
    @Override
    public void processingInstruction(final String target, final XMLString data, final Augmentations augs) throws XNIException {
        chars();
        doAugs(augs);
        out_.print('?');
        out_.print(target);
        if (data != null && data.length() > 0) {
            out_.print(' ');
            print(data.toString());
        }
        out_.println();
        out_.flush();
    }

    /** Comment. */
    @Override
    public void comment(final XMLString text, final Augmentations augs) throws XNIException {
        chars();
        doAugs(augs);
        out_.print('#');
        print(text.toString());
        out_.println();
        out_.flush();
    }

    /** Start element. */
    @Override
    public void startElement(final QName element, final XMLAttributes attrs, final Augmentations augs) throws XNIException {
        chars();
        doAugs(augs);
        out_.print('(');
        out_.print(element.getRawname());
        if (attrs != null) {
            final int acount = attrs.getLength();
            if (acount > 0) {
                final String[] anames = new String[acount];
                final String[] anamesNonNormalized = new String[acount];
                final String[] auris = new String[acount];
                sortAttrNames(attrs, anames, anamesNonNormalized, auris);
                for (int i = 0; i < acount; i++) {
                    final String aname = anames[i];
                    out_.println();
                    out_.flush();
                    out_.print('A');
                    if (auris[i] != null) {
                        out_.print('{');
                        out_.print(auris[i]);
                        out_.print('}');
                    }
                    out_.print(aname);
                    out_.print(' ');
                    print(attrs.getValue(aname));

                    if(anamesNonNormalized[i] != null && !anamesNonNormalized[i].equals(attrs.getValue(aname))) {
                        out_.print(" / ");
                        out_.print(anamesNonNormalized[i]);
                    }
                }
            }
        }
        out_.println();
        out_.flush();
    }

    /** End element. */
    @Override
    public void endElement(final QName element, final Augmentations augs) throws XNIException {
        chars();
        doAugs(augs);
        out_.print(')');
        out_.print(element.getRawname());
        out_.println();
        out_.flush();
    }

    /** Empty element. */
    @Override
    public void emptyElement(final QName element, final XMLAttributes attrs, final Augmentations augs) throws XNIException {
        startElement(element, attrs, augs);
        endElement(element, augs);
    }

    /** Characters. */
    @Override
    public void characters(final XMLString text, final Augmentations augs) throws XNIException {
        storeCharactersEnd(augs);
        if (!inCharacters_) {
            storeCharactersStart(augs);
        }
        inCharacters_ = true;
        stringBuffer_.append(text);
    }

    @Override
    public void startCDATA(final Augmentations augs) throws XNIException {
        chars();
        doAugs(augs);
        out_.println("((CDATA");
    }

    @Override
    public void endCDATA(final Augmentations augs) throws XNIException {
        chars();
        doAugs(augs);
        out_.println("))CDATA");
        out_.flush();
    }

    /** Prints collected characters. */
    protected void chars() {
        inCharacters_ = false;
        if (stringBuffer_.length() == 0) {
            return;
        }
        doCharactersAugs();
        out_.print('"');
        print(stringBuffer_.toString());
        out_.println();
        out_.flush();
        stringBuffer_.clear();
    }

    /** Prints the specified string. */
    protected void print(final String s) {
        if (s != null) {
            final int length = s.length();
            for (int i = 0; i < length; i++) {
                final char c = s.charAt(i);
                switch (c) {
                    case '\n':
                        out_.print("\\n");
                        break;
                    case '\r':
                        out_.print("\\r");
                        break;
                    case '\t':
                        out_.print("\\t");
                        break;
                    case '\\':
                        out_.print("\\\\");
                        break;
                    default:
                        out_.print(c);
                }
            }
        }
    }

    /**
     * Print out the HTML augmentations for the given augs.  Prints nothing if
     * there are no HTML augmentations available.
     */
    protected void doAugs(final Augmentations augs) {
        final HTMLEventInfo evInfo = (augs == null) ? null : (HTMLEventInfo) augs;
        if (evInfo != null) {
            if (evInfo.isSynthesized()) {
                out_.print("[synth]");
            }
            else {
                out_.print('[');
                out_.print(evInfo.getBeginLineNumber());
                out_.print(',');
                out_.print(evInfo.getBeginColumnNumber());
                out_.print(',');
                out_.print(evInfo.getBeginCharacterOffset());
                out_.print(';');
                out_.print(evInfo.getEndLineNumber());
                out_.print(',');
                out_.print(evInfo.getEndColumnNumber());
                out_.print(',');
                out_.print(evInfo.getEndCharacterOffset());
                out_.print(']');
            }
        }
    }

    /**
     * Store the HTML augmentations for the given augs in temporary variables
     * for the start of the current block of characters.  Does nothing if there
     * are no HTML augmentations available.
     */
    protected void storeCharactersStart(final Augmentations augs) {
        final HTMLEventInfo evInfo = (augs == null) ? null : (HTMLEventInfo) augs;
        if (evInfo != null) {
            charactersBeginLine_ = evInfo.getBeginLineNumber();
            charactersBeginColumn_ = evInfo.getBeginColumnNumber();
            charactersBeginCharacterOffset_ = evInfo.getBeginCharacterOffset();
        }
    }

    /**
     * Store the HTML augmentations for the given augs in temporary variables
     * for the end of the current block of characters.  Does nothing if there
     * are no HTML augmentations available.
     */
    protected void storeCharactersEnd(final Augmentations augs) {
        final HTMLEventInfo evInfo = (augs == null) ? null : (HTMLEventInfo) augs;
        if (evInfo != null) {
            charactersEndLine_ = evInfo.getEndLineNumber();
            charactersEndColumn_ = evInfo.getEndColumnNumber();
            charactersEndCharacterOffset_ = evInfo.getEndCharacterOffset();
        }
    }

    /**
     * Print out the HTML augmentation values for the current block of
     * characters.  Prints nothing if there were no HTML augmentations
     * available.
     */
    protected void doCharactersAugs() {
        if (charactersBeginLine_ >= 0) {
            out_.print('[');
            out_.print(charactersBeginLine_);
            out_.print(',');
            out_.print(charactersBeginColumn_);
            out_.print(',');
            out_.print(charactersBeginCharacterOffset_);
            out_.print(';');
            out_.print(charactersEndLine_);
            out_.print(',');
            out_.print(charactersEndColumn_);
            out_.print(',');
            out_.print(charactersEndCharacterOffset_);
            out_.print(']');
        }
    }

    /** Sorts the attribute names. */
    protected static void sortAttrNames(final XMLAttributes attrs, final String[] anames, final String[] anamesNonNormalized, final String[] auris) {
        for (int i = 0; i < anames.length; i++) {
            anames[i] = attrs.getQName(i);
            anamesNonNormalized[i] = attrs.getNonNormalizedValue(i);
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
                final String tn = anames[i];
                anames[i] = anames[index];
                anames[index] = tn;

                final String tnn = anamesNonNormalized[i];
                anamesNonNormalized[i] = anamesNonNormalized[index];
                anamesNonNormalized[index] = tnn;

                final String tu = auris[i];
                auris[i] = auris[index];
                auris[index] = tu;
            }
        }
    }
}
