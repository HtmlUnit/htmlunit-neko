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

import java.util.HashMap;
import java.util.Locale;

import org.htmlunit.cyberneko.util.FastHashMap;

/**
 * Collection of HTML element information.
 *
 * @author Andy Clark
 * @author Ahmed Ashour
 * @author Marc Guillemot
 * @author Ronald Brill
 */
public class HTMLElements {

    // element codes

    // NOTE: The element codes *must* start with 0 and increment in
    //       sequence. The parent and closes references depends on
    //       this assumption. -Ac

    public static final short A = 0;
    public static final short ABBR = A + 1;
    public static final short ACRONYM = ABBR + 1;
    public static final short ADDRESS = ACRONYM + 1;
    public static final short APPLET = ADDRESS + 1;
    public static final short AREA = APPLET + 1;
    public static final short ARTICLE = AREA + 1;
    public static final short ASIDE = ARTICLE + 1;
    public static final short AUDIO = ASIDE + 1;
    public static final short B = AUDIO + 1;
    public static final short BASE = B + 1;
    public static final short BASEFONT = BASE + 1;
    public static final short BDI = BASEFONT + 1;
    public static final short BDO = BDI + 1;
    public static final short BGSOUND = BDO + 1;
    public static final short BIG = BGSOUND + 1;
    public static final short BLINK = BIG + 1;
    public static final short BLOCKQUOTE = BLINK + 1;
    public static final short BODY = BLOCKQUOTE + 1;
    public static final short BR = BODY + 1;
    public static final short BUTTON = BR + 1;
    public static final short CANVAS = BUTTON + 1;
    public static final short CAPTION = CANVAS + 1;
    public static final short CENTER = CAPTION + 1;
    public static final short CITE = CENTER + 1;
    public static final short CODE = CITE + 1;
    public static final short COL = CODE + 1;
    public static final short COLGROUP = COL + 1;
    public static final short COMMENT = COLGROUP + 1;
    public static final short DATA = COMMENT + 1;
    public static final short DATALIST = DATA + 1;
    public static final short DEL = DATALIST + 1;
    public static final short DETAILS = DEL + 1;
    public static final short DFN = DETAILS + 1;
    public static final short DIALOG = DFN + 1;
    public static final short DIR = DIALOG + 1;
    public static final short DIV = DIR + 1;
    public static final short DD = DIV + 1;
    public static final short DL = DD + 1;
    public static final short DT = DL + 1;
    public static final short EM = DT + 1;
    public static final short EMBED = EM + 1;
    public static final short FIELDSET = EMBED + 1;
    public static final short FIGCAPTION = FIELDSET + 1;
    public static final short FIGURE = FIGCAPTION + 1;
    public static final short FONT = FIGURE + 1;
    public static final short FOOTER = FONT + 1;
    public static final short FORM = FOOTER + 1;
    public static final short FRAME = FORM + 1;
    public static final short FRAMESET = FRAME + 1;
    public static final short H1 = FRAMESET + 1;
    public static final short H2 = H1 + 1;
    public static final short H3 = H2 + 1;
    public static final short H4 = H3 + 1;
    public static final short H5 = H4 + 1;
    public static final short H6 = H5 + 1;
    public static final short HEAD = H6 + 1;
    public static final short HEADER = HEAD + 1;
    public static final short HR = HEADER + 1;
    public static final short HTML = HR + 1;
    public static final short I = HTML + 1;
    public static final short IFRAME = I + 1;
    public static final short ILAYER = IFRAME + 1;
    public static final short IMG = ILAYER + 1;
    public static final short IMAGE = IMG + 1;
    public static final short INPUT = IMAGE + 1;
    public static final short INS = INPUT + 1;
    public static final short KBD = INS + 1;
    public static final short KEYGEN = KBD + 1;
    public static final short LABEL = KEYGEN + 1;
    public static final short LAYER = LABEL + 1;
    public static final short LEGEND = LAYER + 1;
    public static final short LI = LEGEND + 1;
    public static final short LINK = LI + 1;
    public static final short LISTING = LINK + 1;
    public static final short MAIN = LISTING + 1;
    public static final short MAP = MAIN + 1;
    public static final short MARK = MAP + 1;
    public static final short MARQUEE = MARK + 1;
    public static final short MENU = MARQUEE + 1;
    public static final short META = MENU + 1;
    public static final short METER = META + 1;
    public static final short MULTICOL = METER + 1;
    public static final short NAV = MULTICOL + 1;
    public static final short NEXTID = NAV + 1;
    public static final short NOBR = NEXTID + 1;
    public static final short NOEMBED = NOBR + 1;
    public static final short NOFRAMES = NOEMBED + 1;
    public static final short NOLAYER = NOFRAMES + 1;
    public static final short NOSCRIPT = NOLAYER + 1;
    public static final short OBJECT = NOSCRIPT + 1;
    public static final short OL = OBJECT + 1;
    public static final short OPTGROUP = OL + 1;
    public static final short OPTION = OPTGROUP + 1;
    public static final short P = OPTION + 1;
    public static final short PARAM = P + 1;
    public static final short PICTURE = PARAM + 1;
    public static final short PLAINTEXT = PICTURE + 1;
    public static final short PRE = PLAINTEXT + 1;
    public static final short PROGRESS = PRE + 1;
    public static final short Q = PROGRESS + 1;
    public static final short RB = Q + 1;
    public static final short RBC = RB + 1;
    public static final short RP = RBC + 1;
    public static final short RT = RP + 1;
    public static final short RTC = RT + 1;
    public static final short RUBY = RTC + 1;
    public static final short S = RUBY + 1;
    public static final short SAMP = S + 1;
    public static final short SCRIPT = SAMP + 1;
    public static final short SECTION = SCRIPT + 1;
    public static final short SELECT = SECTION + 1;
    public static final short SLOT = SELECT + 1;
    public static final short SMALL = SLOT + 1;
    public static final short SOUND = SMALL + 1;
    public static final short SOURCE = SOUND + 1;
    public static final short SPACER = SOURCE + 1;
    public static final short SPAN = SPACER + 1;
    public static final short STRIKE = SPAN + 1;
    public static final short STRONG = STRIKE + 1;
    public static final short STYLE = STRONG + 1;
    public static final short SUB = STYLE + 1;
    public static final short SUMMARY = SUB + 1;
    public static final short SUP = SUMMARY + 1;
    public static final short SVG = SUP + 1;
    public static final short TABLE = SVG + 1;
    public static final short TBODY = TABLE + 1;
    public static final short TD = TBODY + 1;
    public static final short TEMPLATE = TD + 1;
    public static final short TEXTAREA = TEMPLATE + 1;
    public static final short TFOOT = TEXTAREA + 1;
    public static final short TH = TFOOT + 1;
    public static final short THEAD = TH + 1;
    public static final short TIME = THEAD + 1;
    public static final short TITLE = TIME + 1;
    public static final short TR = TITLE + 1;
    public static final short TRACK = TR + 1;
    public static final short TT = TRACK + 1;
    public static final short OUTPUT = TT + 1;
    public static final short U = OUTPUT + 1;
    public static final short UL = U + 1;
    public static final short VAR = UL + 1;
    public static final short VIDEO = VAR + 1;
    public static final short WBR = VIDEO + 1;
    public static final short XML = WBR + 1;
    public static final short XMP = XML + 1;
    public static final short UNKNOWN = XMP + 1;

    // information

    /** No such element. */
    public final Element NO_SUCH_ELEMENT = new Element(UNKNOWN, "",  Element.CONTAINER, new short[]{BODY}, null);

    // these fields became private to avoid exposing them for indirect modification
    // this cannot be final because HtmlUnit might add to that
    private Element[] elementsByCode_;

    // keep the list here for later modification
    private final HashMap<String, Element> elementsByNameForReference_ = new HashMap<>();

    // this is a optimized version which will be later queried
    private final FastHashMap<String, Element> elementsByNameOptimized_ = new FastHashMap<>(311, 0.70f);

    // this map helps us to know what elements we don't have and speed things up
    private final FastHashMap<String, Boolean> unknownElements_ = new FastHashMap<>(11, 0.70f);

    public HTMLElements() {
        final Element[][] elementsArray = new Element[26][];
        // <!ENTITY % heading "H1|H2|H3|H4|H5|H6">
        // <!ENTITY % fontstyle "TT | I | B | BIG | SMALL">
        // <!ENTITY % phrase "EM | STRONG | DFN | CODE | SAMP | KBD | VAR | CITE | ABBR | ACRONYM" >
        // <!ENTITY % special "A | IMG | OBJECT | BR | SCRIPT | MAP | Q | SUB | SUP | SPAN | BDO">
        // <!ENTITY % formctrl "INPUT | SELECT | TEXTAREA | LABEL | BUTTON">
        // <!ENTITY % inline "#PCDATA | %fontstyle; | %phrase; | %special; | %formctrl;">
        // <!ENTITY % block "P | %heading; | %list; | %preformatted; | DL | DIV
        //                     | NOSCRIPT | BLOCKQUOTE | FORM | HR | TABLE | FIELDSET | ADDRESS">
        // <!ENTITY % flow "%block; | %inline;">

        // initialize array of element information
        elementsArray['A' - 'A'] = new Element[] {
            // A - - (%inline;)* -(A)
            new Element(A, "A", Element.CONTAINER, BODY, new short[] {A}),
            // ABBR - - (%inline;)*
            new Element(ABBR, "ABBR", Element.INLINE, BODY, null),
            // ACRONYM - - (%inline;)*
            new Element(ACRONYM, "ACRONYM", Element.INLINE, BODY, null),
            // ADDRESS - - (%inline;)*
            new Element(ADDRESS, "ADDRESS", Element.BLOCK, BODY, new short[] {P}),
            // APPLET
            new Element(APPLET, "APPLET", Element.CONTAINER, BODY, null),
            // AREA - O EMPTY
            new Element(AREA, "AREA", Element.EMPTY, BODY, null),

            new Element(ARTICLE, "ARTICLE", Element.BLOCK, BODY, new short[] {P}),

            new Element(ASIDE, "ASIDE", Element.BLOCK, BODY, new short[] {P}),

            new Element(AUDIO, "AUDIO", Element.CONTAINER, BODY, null),
        };
        elementsArray['B' - 'A'] = new Element[] {
            // B - - (%inline;)*
            new Element(B, "B", Element.INLINE, BODY, new short[] {SVG}),
            // BASE - O EMPTY
            new Element(BASE, "BASE", Element.EMPTY, HEAD, null),
            // BASEFONT
            new Element(BASEFONT, "BASEFONT", Element.EMPTY, HEAD, null),

            new Element(BDI, "BDI", Element.INLINE, BODY, null),
            // BDO - - (%inline;)*
            new Element(BDO, "BDO", Element.INLINE, BODY, null),
            // BGSOUND
            new Element(BGSOUND, "BGSOUND", Element.EMPTY, HEAD, null),
            // BIG - - (%inline;)*
            new Element(BIG, "BIG", Element.INLINE, BODY, new short[]{SVG}),
            // BLINK
            new Element(BLINK, "BLINK", Element.INLINE, BODY, null),
            // BLOCKQUOTE - - (%block;|SCRIPT)+
            new Element(BLOCKQUOTE, "BLOCKQUOTE", Element.BLOCK, BODY, new short[]{P, SVG}),
            // BODY O O (%block;|SCRIPT)+ +(INS|DEL)
            new Element(BODY, "BODY", Element.CONTAINER, HTML, new short[]{HEAD, SVG}),
            // BR - O EMPTY
            new Element(BR, "BR", Element.EMPTY, BODY, new short[]{SVG}),
            // BUTTON - - (%flow;)* -(A|%formctrl;|FORM|FIELDSET)
            new Element(BUTTON, "BUTTON", Element.INLINE | Element.BLOCK, BODY, new short[]{BUTTON}),
        };
        elementsArray['C' - 'A'] = new Element[] {
            new Element(CANVAS, "CANVAS",  Element.CONTAINER, BODY, null),
            // CAPTION - - (%inline;)*
            new Element(CAPTION, "CAPTION", Element.INLINE, TABLE, null),
            // CENTER,
            new Element(CENTER, "CENTER", Element.CONTAINER, BODY, new short[] {P, SVG}),
            // CITE - - (%inline;)*
            new Element(CITE, "CITE", Element.INLINE, BODY, null),
            // CODE - - (%inline;)*
            new Element(CODE, "CODE", Element.INLINE, BODY, new short[]{SVG}),
            // COL - O EMPTY
            new Element(COL, "COL", Element.EMPTY, COLGROUP, null),
            // COLGROUP - O (COL)*
            new Element(COLGROUP, "COLGROUP", Element.CONTAINER, TABLE, new short[]{COL, COLGROUP}),
            // COMMENT
            new Element(COMMENT, "COMMENT", Element.SPECIAL, HTML, null),
        };
        elementsArray['D' - 'A'] = new Element[] {
            new Element(DATA, "DATA",  Element.CONTAINER, BODY, null),

            new Element(DATALIST, "DATALIST",  Element.CONTAINER, BODY, null),

            // DEL - - (%flow;)*
            new Element(DEL, "DEL", Element.INLINE, BODY, null),

            new Element(DETAILS, "DETAILS", Element.BLOCK, BODY, new short[] {P}),
            // DFN - - (%inline;)*
            new Element(DFN, "DFN", Element.INLINE, BODY, null),

            new Element(DIALOG, "DIALOG",  Element.CONTAINER, BODY, new short[] {P}),
            // DIR
            new Element(DIR, "DIR", Element.CONTAINER, BODY, new short[] {P}),
            // DIV - - (%flow;)*
            new Element(DIV, "DIV", Element.CONTAINER, BODY, new short[]{P, SVG}),
            // DD - O (%flow;)*
            new Element(DD, "DD", Element.BLOCK, BODY, new short[]{DT, DD, P, SVG}),
            // DL - - (DT|DD)+
            new Element(DL, "DL", Element.BLOCK | Element.CONTAINER, BODY, new short[] {P, SVG}),
            // DT - O (%inline;)*
            new Element(DT, "DT", Element.BLOCK, BODY, new short[]{DT, DD, P, SVG}),
        };
        elementsArray['E' - 'A'] = new Element[] {
            // EM - - (%inline;)*
            new Element(EM, "EM", Element.INLINE, BODY, new short[]{SVG}),
            // EMBED
            new Element(EMBED, "EMBED", Element.EMPTY, BODY, new short[]{SVG}),
        };
        elementsArray['F' - 'A'] = new Element[] {
            // FIELDSET - - (#PCDATA,LEGEND,(%flow;)*)
            new Element(FIELDSET, "FIELDSET", Element.CONTAINER, BODY, new short[] {P}),

            new Element(FIGCAPTION, "FIGCAPTION", Element.BLOCK, BODY, new short[] {P}),

            new Element(FIGURE, "FIGURE", Element.BLOCK, BODY, new short[] {P}),
            // FONT
            new Element(FONT, "FONT", Element.CONTAINER, BODY, null),

            new Element(FOOTER, "FOOTER", Element.BLOCK, BODY, new short[] {P}),

            // FORM - - (%block;|SCRIPT)+ -(FORM)
            new Element(FORM, "FORM", Element.CONTAINER, new short[]{BODY, TD, DIV}, new short[]{P}),
            // FRAME - O EMPTY
            new Element(FRAME, "FRAME", Element.EMPTY, FRAMESET, null),
            // FRAMESET - - ((FRAMESET|FRAME)+ & NOFRAMES?)
            new Element(FRAMESET, "FRAMESET", Element.CONTAINER, HTML, new short[]{HEAD}),
        };
        elementsArray['H' - 'A'] = new Element[] {
            // (H1|H2|H3|H4|H5|H6) - - (%inline;)*
            new Element(H1, "H1", Element.BLOCK, new short[]{BODY, A}, new short[]{H1, H2, H3, H4, H5, H6, P, SVG}),
            new Element(H2, "H2", Element.BLOCK, new short[]{BODY, A}, new short[]{H1, H2, H3, H4, H5, H6, P, SVG}),
            new Element(H3, "H3", Element.BLOCK, new short[]{BODY, A}, new short[]{H1, H2, H3, H4, H5, H6, P, SVG}),
            new Element(H4, "H4", Element.BLOCK, new short[]{BODY, A}, new short[]{H1, H2, H3, H4, H5, H6, P, SVG}),
            new Element(H5, "H5", Element.BLOCK, new short[]{BODY, A}, new short[]{H1, H2, H3, H4, H5, H6, P, SVG}),
            new Element(H6, "H6", Element.BLOCK, new short[]{BODY, A}, new short[]{H1, H2, H3, H4, H5, H6, P, SVG}),
            // HEAD O O (%head.content;) +(%head.misc;)
            new Element(HEAD, "HEAD", 0, HTML, null),

            new Element(HEADER, "HEADER", Element.BLOCK, BODY, new short[] {P}),

            // HR - O EMPTY
            new Element(HR, "HR", Element.EMPTY, new short[]{BODY, SELECT}, new short[]{P, SVG}),
            // HTML O O (%html.content;)
            new Element(HTML, "HTML", 0, null, null),
        };
        elementsArray['I' - 'A'] = new Element[] {
            // I - - (%inline;)*
            new Element(I, "I", Element.INLINE, BODY, new short[]{SVG}),
            // IFRAME
            new Element(IFRAME, "IFRAME", Element.BLOCK, BODY, null),
            // ILAYER
            new Element(ILAYER, "ILAYER", Element.BLOCK, BODY, null),
            // IMG - O EMPTY
            new Element(IMG, "IMG", Element.EMPTY, BODY, new short[]{SVG}),

            new Element(IMAGE, "IMAGE", Element.EMPTY, BODY, null),
            // INPUT - O EMPTY
            new Element(INPUT, "INPUT", Element.EMPTY, BODY, null),
            // INS - - (%flow;)*
            new Element(INS, "INS", Element.INLINE, BODY, null),
        };
        elementsArray['K' - 'A'] = new Element[] {
            // KBD - - (%inline;)*
            new Element(KBD, "KBD", Element.INLINE, BODY, null),
            // KEYGEN
            new Element(KEYGEN, "KEYGEN", Element.EMPTY, BODY, null),
        };
        elementsArray['L' - 'A'] = new Element[] {
            // LABEL - - (%inline;)* -(LABEL)
            new Element(LABEL, "LABEL", Element.INLINE, BODY, null),
            // LAYER
            new Element(LAYER, "LAYER", Element.BLOCK, BODY, null),
            // LEGEND - - (%inline;)*
            new Element(LEGEND, "LEGEND", Element.INLINE, BODY, null),
            // LI - O (%flow;)*
            new Element(LI, "LI", Element.CONTAINER, new short[]{BODY, UL, OL, MENU}, new short[]{LI, P, SVG}),
            // LINK - O EMPTY
            new Element(LINK, "LINK", Element.EMPTY, HEAD, null),
            // LISTING
            new Element(LISTING, "LISTING", Element.BLOCK, BODY, new short[] {P, SVG}),
        };
        elementsArray['M' - 'A'] = new Element[] {
            new Element(MAIN, "MAIN", Element.BLOCK, BODY, new short[] {P}),
            // MAP - - ((%block;) | AREA)+
            new Element(MAP, "MAP", Element.INLINE, BODY, null),

            new Element(MARK, "MARK",  Element.CONTAINER, BODY, null),
            // MARQUEE
            new Element(MARQUEE, "MARQUEE", Element.CONTAINER, BODY, null),
            // MENU
            new Element(MENU, "MENU", Element.CONTAINER, BODY, new short[] {P, SVG}),

            new Element(METER, "METER",  Element.CONTAINER, BODY, null),
            // META - O EMPTY
            new Element(META, "META", Element.EMPTY, HEAD, new short[]{STYLE, TITLE, SVG}),
            // MULTICOL
            new Element(MULTICOL, "MULTICOL", Element.CONTAINER, BODY, null),
        };
        elementsArray['N' - 'A'] = new Element[] {
            new Element(NAV, "NAV", Element.BLOCK, BODY, new short[] {P}),

            // NEXTID
            new Element(NEXTID, "NEXTID", Element.INLINE, BODY, null),
            // NOBR
            new Element(NOBR, "NOBR", Element.INLINE, BODY, new short[]{NOBR, SVG}),
            // NOEMBED
            new Element(NOEMBED, "NOEMBED", Element.CONTAINER, BODY, null),
            // NOFRAMES - - (BODY) -(NOFRAMES)
            new Element(NOFRAMES, "NOFRAMES", Element.CONTAINER, null, null),
            // NOLAYER
            new Element(NOLAYER, "NOLAYER", Element.CONTAINER, BODY, null),
            // NOSCRIPT - - (%block;)+
            new Element(NOSCRIPT, "NOSCRIPT", Element.CONTAINER, new short[]{HEAD, BODY}, null),
        };
        elementsArray['O' - 'A'] = new Element[] {
            // OBJECT - - (PARAM | %flow;)*
            new Element(OBJECT, "OBJECT", Element.CONTAINER, BODY, null),
            // OL - - (LI)+
            new Element(OL, "OL", Element.BLOCK, BODY, new short[] {P, SVG}),
            // OPTGROUP - - (OPTION)+
            new Element(OPTGROUP, "OPTGROUP", Element.INLINE, BODY, new short[]{OPTION}),
            // OPTION - O (#PCDATA)
            new Element(OPTION, "OPTION", Element.INLINE, BODY, new short[]{OPTION}),

            new Element(OUTPUT, "OUTPUT",  Element.CONTAINER, BODY, null),
        };
        elementsArray['P' - 'A'] = new Element[] {
            // P - O (%inline;)*
            new Element(P, "P", Element.CONTAINER, BODY, new short[]{P, SVG}),
            // PARAM - O EMPTY
            new Element(PARAM, "PARAM", Element.EMPTY, BODY, null),

            new Element(PICTURE, "PICTURE",  Element.CONTAINER, BODY, null),
            // PLAINTEXT
            new Element(PLAINTEXT, "PLAINTEXT", Element.SPECIAL, BODY, new short[]{P}),
            // PRE - - (%inline;)* -(%pre.exclusion;)
            new Element(PRE, "PRE", Element.BLOCK, BODY, new short[] {P, SVG}),

            new Element(PROGRESS, "PROGRESS",  Element.CONTAINER, BODY, null),
        };
        elementsArray['Q' - 'A'] = new Element[] {
            // Q - - (%inline;)*
            new Element(Q, "Q", Element.INLINE, BODY, null),
        };
        elementsArray['R' - 'A'] = new Element[] {
            // RB
            new Element(RB, "RB", Element.INLINE, BODY, null),
            // RBC
            new Element(RBC, "RBC", 0, BODY, null),
            // RP
            new Element(RP, "RP", Element.INLINE, BODY, null),
            // RT
            new Element(RT, "RT", Element.INLINE, BODY, null),
            // RTC
            new Element(RTC, "RTC", Element.INLINE, BODY, null),
            // RUBY
            new Element(RUBY, "RUBY", Element.CONTAINER, BODY, new short[]{SVG}),
        };
        elementsArray['S' - 'A'] = new Element[] {
            // S
            new Element(S, "S", Element.INLINE, BODY, new short[]{SVG}),
            // SAMP - - (%inline;)*
            new Element(SAMP, "SAMP", Element.INLINE, BODY, null),
            // SCRIPT - - %Script;
            new Element(SCRIPT, "SCRIPT", Element.SPECIAL | Element.SCRIPT_SUPPORTING,
                            new short[]{HEAD, BODY}, null),

            new Element(SECTION, "SECTION", Element.BLOCK, BODY, new short[]{SELECT, P}),
            // SELECT - - (OPTGROUP|OPTION)+
            new Element(SELECT, "SELECT", Element.CONTAINER, BODY, new short[]{SELECT}),

            new Element(SLOT, "SLOT",  Element.CONTAINER, BODY, null),
            // SMALL - - (%inline;)*
            new Element(SMALL, "SMALL", Element.INLINE, BODY, new short[]{SVG}),
            // SOUND
            new Element(SOUND, "SOUND", Element.EMPTY, HEAD, null),

            new Element(SOURCE, "SOURCE", Element.EMPTY, BODY, null),
            // SPACER
            new Element(SPACER, "SPACER", Element.INLINE, BODY, null),
            // SPAN - - (%inline;)*
            new Element(SPAN, "SPAN", Element.CONTAINER, BODY, new short[]{SVG}),
            // STRIKE
            new Element(STRIKE, "STRIKE", Element.INLINE, BODY, new short[]{SVG}),
            // STRONG - - (%inline;)*
            new Element(STRONG, "STRONG", Element.INLINE, BODY, new short[]{SVG}),
            // STYLE - - %StyleSheet;
            new Element(STYLE, "STYLE", Element.SPECIAL, new short[]{HEAD, BODY}, new short[]{STYLE, TITLE, META}),
            // SUB - - (%inline;)*
            new Element(SUB, "SUB", Element.INLINE, BODY, new short[]{SVG}),

            new Element(SUMMARY, "SUMMARY", Element.BLOCK, BODY, new short[] {P}),
            // SUP - - (%inline;)*
            new Element(SUP, "SUP", Element.INLINE, BODY, new short[]{SVG}),

            // SVG - - (%SVG;)*
            new Element(SVG, "SVG", Element.CONTAINER, BODY, null),
        };
        elementsArray['T' - 'A'] = new Element[] {
            // TABLE - - (CAPTION?, (COL*|COLGROUP*), THEAD?, TFOOT?, TBODY+)
            new Element(TABLE, "TABLE", Element.BLOCK | Element.CONTAINER, BODY, new short[]{SVG}),
            // TBODY O O (TR)+
            new Element(TBODY, "TBODY", 0, TABLE, new short[]{FORM, THEAD, TBODY, TFOOT, TD, TH, TR, COLGROUP}),
            // TD - O (%flow;)*
            new Element(TD, "TD", Element.CONTAINER, TR, TABLE, new short[]{TD, TH}),

            new Element(TEMPLATE, "TEMPLATE", Element.CONTAINER | Element.SCRIPT_SUPPORTING,
                            new short[]{HEAD, BODY}, null),
            // TEXTAREA - - (#PCDATA)
            new Element(TEXTAREA, "TEXTAREA", Element.SPECIAL, BODY, null),
            // TFOOT - O (TR)+
            new Element(TFOOT, "TFOOT", 0, TABLE, new short[]{THEAD, TBODY, TFOOT, TD, TH, TR}),
            // TH - O (%flow;)*
            new Element(TH, "TH", Element.CONTAINER, TR, TABLE, new short[]{TD, TH}),
            // THEAD - O (TR)+
            new Element(THEAD, "THEAD", 0, TABLE, new short[]{THEAD, TBODY, TFOOT, TD, TH, TR, COLGROUP}),

            new Element(TIME, "TIME",  Element.CONTAINER, BODY, null),
            // TITLE - - (#PCDATA) -(%head.misc;)
            new Element(TITLE, "TITLE", Element.SPECIAL, new short[]{HEAD, BODY}, null),
            // TR - O (TH|TD)+
            new Element(TR, "TR", Element.BLOCK, new short[]{TBODY, THEAD, TFOOT}, TABLE,
                    new short[]{FORM, TD, TH, TR, COLGROUP, DIV}),

            new Element(TRACK, "TRACK", Element.EMPTY, BODY, null),
            // TT - - (%inline;)*
            new Element(TT, "TT", Element.INLINE, BODY, new short[]{SVG}),
        };
        elementsArray['U' - 'A'] = new Element[] {
            // U,
            new Element(U, "U", Element.INLINE, BODY, new short[]{SVG}),
            // UL - - (LI)+
            new Element(UL, "UL", Element.CONTAINER, BODY, new short[] {P, SVG}),
        };
        elementsArray['V' - 'A'] = new Element[] {
            // VAR - - (%inline;)*
            new Element(VAR, "VAR", Element.INLINE, BODY, new short[]{SVG}),

            new Element(VIDEO, "VIDEO", Element.CONTAINER, BODY, null),
        };
        elementsArray['W' - 'A'] = new Element[] {
            // WBR
            new Element(WBR, "WBR", Element.EMPTY, BODY, null),
        };
        elementsArray['X' - 'A'] = new Element[] {
            // XML
            new Element(XML, "XML", 0, BODY, null),
            // XMP
            new Element(XMP, "XMP", Element.SPECIAL, BODY, new short[] {P}),
        };

        // keep contiguous list of elements for lookups by code
        for (final Element[] elements : elementsArray) {
            if (elements != null) {
                for (final Element element : elements) {
                    this.elementsByNameForReference_.put(element.name, element);
                }
            }
        }

        // setup optimized versions
        setupOptimizedVersions();
    }

    public void setElement(final Element element) {
        this.elementsByNameForReference_.put(element.name, element);

        // rebuild the information "trees"
        setupOptimizedVersions();
    }

    private void setupOptimizedVersions() {
        // we got x amount of elements + 1 unknown
        // put that into an array instead of a map, that
        // is a faster look up and avoids equals
        // ATTENTION: Due to some HtmlUnit custom tag handling that overwrites our
        // list here, we might get a list with holes, so check the range first
        final int size = elementsByNameForReference_.values().stream().mapToInt(v -> v.code).max().getAsInt();
        elementsByCode_ = new Element[Math.max(size, NO_SUCH_ELEMENT.code) + 1];
        elementsByNameForReference_.values().forEach(v -> elementsByCode_[v.code] = v);
        elementsByCode_[NO_SUCH_ELEMENT.code] = NO_SUCH_ELEMENT;

        // initialize cross references to parent elements
        for (final Element element : elementsByCode_) {
            if (element != null) {
                defineParents(element);
            }
        }
        // get us a second version that is lowercase stringified to
        // reduce lookup overhead
        for (final Element element : elementsByCode_) {
            // we might have holes due to HtmlUnitNekoHtmlParser
            if (element != null) {
                elementsByNameOptimized_.put(element.name.toLowerCase(Locale.ROOT), element);
            }
        }
    }

    private void defineParents(final Element element) {
        if (element.parentCodes_ != null) {
            element.parent = new Element[element.parentCodes_.length];
            for (int j = 0; j < element.parentCodes_.length; j++) {
                element.parent[j] = elementsByCode_[element.parentCodes_[j]];
            }
            element.parentCodes_ = null;
        }
    }

    /**
     * @return the element information for the specified element code.
     *
     * @param code The element code.
     */
    public final Element getElement(final short code) {
        return elementsByCode_[code];
    }

    /**
     * @return the element information for the specified element name.
     *
     * @param ename The element name.
     */
    public final Element getElement(final String ename) {
        Element element = getElement(ename, NO_SUCH_ELEMENT);
        if (element == NO_SUCH_ELEMENT) {
            element = new Element(UNKNOWN, ename.toUpperCase(Locale.ROOT),
                                NO_SUCH_ELEMENT.flags, NO_SUCH_ELEMENT.parentCodes_, NO_SUCH_ELEMENT.closes);
            element.parent = NO_SUCH_ELEMENT.parent;
            element.parentCodes_ = NO_SUCH_ELEMENT.parentCodes_;
        }
        return element;
    }

    /**
     * @return the element information for the specified element name.
     *
     * @param ename The element name.
     * @param element The default element to return if not found.
     */
    public final Element getElement(final String ename, final Element element) {
        // check the current form casing first, which is mostly lowercase only
        Element r = elementsByNameOptimized_.get(ename);
        if (r == null) {
            // check first if we know that we don't know and avoid the
            // lowercasing later
            if (unknownElements_.get(ename) != null) {
                // we added it to the cache, so we know it has been
                // queried once unsuccessfully before
                return element;
            }

            // we have not found it in its current form, might be uppercase
            // or mixed case, so try all lowercase for sanity, we speculated that
            // good HTML is mostly all lowercase in the first place so this is the
            // fallback for atypical HTML
            // we also have not seen that element missing yet
            r = elementsByNameOptimized_.get(ename.toLowerCase(Locale.ROOT));

            // remember that we had a miss
            if (r == null) {
                unknownElements_.put(ename, Boolean.TRUE);
                return element;
            }
        }
        return r;
    }

    /**
     * Element information.
     *
     * @author Andy Clark
     */
    public static class Element {

        /** Inline element. */
        public static final int INLINE = 0x01;

        /** Block element. */
        public static final int BLOCK = 0x02;

        /** Empty element. */
        public static final int EMPTY = 0x04;

        /** Container element. */
        public static final int CONTAINER = 0x08;

        /** Special element. */
        public static final int SPECIAL = 0x10;

        /**
         * Script-supporting elements.
         * <a href='https://html.spec.whatwg.org/#script-supporting-elements'>Script-supporting elements</a>
         */
        public static final int SCRIPT_SUPPORTING = 0x20;

        /** The element code. */
        public final short code;

        /** The element name. */
        public final String name;

        /** The element name. */
        public final String lowercaseName;

        /** Informational flags. */
        public final int flags;

        /** Parent elements. */
        public Element[] parent;

        /** The bounding element code. */
        public final short bounds;

        /** List of elements this element can close. */
        public final short[] closes;

        /** Parent elements. */
        short[] parentCodes_;

        /**
         * Constructs an element object.
         *
         * @param code The element code.
         * @param name The element name.
         * @param flags Informational flags
         * @param parent Natural closing parent name.
         * @param closes List of elements this element can close.
         */
        public Element(final short code, final String name, final int flags,
                final short parent, final short[] closes) {
            this(code, name, flags, new short[]{parent}, (short) -1, closes);
        }

        /**
         * Constructs an element object.
         *
         * @param code The element code.
         * @param name The element name.
         * @param flags Informational flags
         * @param parent Natural closing parent name.
         * @param bounds bounds
         * @param closes List of elements this element can close.
         */
        public Element(final short code, final String name, final int flags,
                final short parent, final short bounds, final short[] closes) {
            this(code, name, flags, new short[]{parent}, bounds, closes);
        }

        /**
         * Constructs an element object.
         *
         * @param code The element code.
         * @param name The element name.
         * @param flags Informational flags
         * @param parents Natural closing parent names.
         * @param closes List of elements this element can close.
         */
        public Element(final short code, final String name, final int flags,
                final short[] parents, final short[] closes) {
            this(code, name, flags, parents, (short) -1, closes);
        }

        /**
         * Constructs an element object.
         *
         * @param code The element code.
         * @param name The element name.
         * @param flags Informational flags
         * @param parents Natural closing parent names.
         * @param bounds bounds
         * @param closes List of elements this element can close.
         */
        public Element(final short code, final String name, final int flags,
                final short[] parents, final short bounds, final short[] closes) {
            this.code = code;
            this.name = name;
            this.lowercaseName = name.toLowerCase(Locale.ROOT);
            this.flags = flags;
            this.parentCodes_ = parents;
            this.parent = null;
            this.bounds = bounds;
            this.closes = closes;
        }

        /**
         * @return true if this element is an inline element.
         */
        public final boolean isInline() {
            return (flags & INLINE) != 0;
        }

        /**
         * @return true if this element is a block element.
         */
        public final boolean isBlock() {
            return (flags & BLOCK) != 0;
        }

        /**
         * @return true if this element is an empty element.
         */
        public final boolean isEmpty() {
            return (flags & EMPTY) != 0;
        }

        /**
         * @return true if this element is a container element.
         */
        public final boolean isContainer() {
            return (flags & CONTAINER) != 0;
        }

        /**
         * @return true if this element is special -- if its content
         *     should be parsed ignoring markup.
         */
        public final boolean isSpecial() {
            return (flags & SPECIAL) != 0;
        }

        /**
         * @return true if this element is a script-supporting one.
         */
        public final boolean isScriptSupporting() {
            return (flags & SCRIPT_SUPPORTING) != 0;
        }

        /**
         * @return true if this element can close the specified Element.
         *
         * @param tag The element.
         */
        public boolean closes(final short tag) {
            if (closes != null) {
                for (final short close : closes) {
                    if (close == tag) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * @return a hash code for this object.
         */
        @Override
        public int hashCode() {
            return name.hashCode();
        }

        /**
         * @return true if the objects are equal.
         */
        @Override
        public boolean equals(final Object o) {
            if (o instanceof Element) {
                final Element e = (Element) o;
                return lowercaseName.equals(e.name) || name.equals(e.name);
            }
            return false;
        }

        /**
         * @return a simple representation to make debugging easier
         */
        @Override
        public String toString() {
            return super.toString() + "(name=" + name + ")";
        }

        /**
         * Indicates if the provided element is an accepted parent of current element.
         * @param element the element to test for "paternity"
         * @return <code>true</code> if <code>element</code> belongs to the {@link #parent}
         */
        public boolean isParent(final Element element) {
            if (parent == null) {
                return false;
            }
            for (final Element element2 : parent) {
                if (element.code == element2.code) {
                    return true;
                }
            }
            return false;
        }
    }
}
