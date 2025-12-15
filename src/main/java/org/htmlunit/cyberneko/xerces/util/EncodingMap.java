/*
 * Copyright (c) 2017-2025 Ronald Brill
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

import java.util.HashMap;
import java.util.Locale;

/**
 * EncodingMap is a convenience class which handles conversions between IANA
 * encoding names and Java encoding names, and vice versa. The encoding names
 * used in XML instance documents <strong>must</strong> be the IANA encoding
 * names specified or one of the aliases for those names which IANA defines.
 * <TABLE BORDER="0" WIDTH="100%">
 * <TR>
 * <TD WIDTH="33%">
 * <P ALIGN="CENTER">
 * <B>Common Name</B></TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * <B>Use this name in XML files</B></TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * <B>Name Type</B></TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * <B>Xerces converts to this Java Encoder Name</B></TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">8 bit Unicode</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * UTF-8</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * IANA</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * UTF8</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">ISO Latin 1</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * ISO-8859-1</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * MIME</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * ISO-8859-1</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">ISO Latin 2</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * ISO-8859-2</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * MIME</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * ISO-8859-2</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">ISO Latin 3</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * ISO-8859-3</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * MIME</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * ISO-8859-3</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">ISO Latin 4</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * ISO-8859-4</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * MIME</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * ISO-8859-4</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">ISO Latin Cyrillic</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * ISO-8859-5</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * MIME</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * ISO-8859-5</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">ISO Latin Arabic</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * ISO-8859-6</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * MIME</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * ISO-8859-6</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">ISO Latin Greek</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * ISO-8859-7</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * MIME</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * ISO-8859-7</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">ISO Latin Hebrew</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * ISO-8859-8</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * MIME</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * ISO-8859-8</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">ISO Latin 5</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * ISO-8859-9</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * MIME</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * ISO-8859-9</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">EBCDIC: US</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * ebcdic-cp-us</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * IANA</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * cp037</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">EBCDIC: Canada</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * ebcdic-cp-ca</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * IANA</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * cp037</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">EBCDIC: Netherlands</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * ebcdic-cp-nl</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * IANA</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * cp037</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">EBCDIC: Denmark</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * ebcdic-cp-dk</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * IANA</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * cp277</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">EBCDIC: Norway</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * ebcdic-cp-no</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * IANA</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * cp277</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">EBCDIC: Finland</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * ebcdic-cp-fi</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * IANA</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * cp278</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">EBCDIC: Sweden</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * ebcdic-cp-se</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * IANA</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * cp278</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">EBCDIC: Italy</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * ebcdic-cp-it</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * IANA</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * cp280</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">EBCDIC: Spain, Latin America</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * ebcdic-cp-es</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * IANA</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * cp284</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">EBCDIC: Great Britain</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * ebcdic-cp-gb</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * IANA</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * cp285</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">EBCDIC: France</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * ebcdic-cp-fr</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * IANA</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * cp297</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">EBCDIC: Arabic</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * ebcdic-cp-ar1</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * IANA</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * cp420</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">EBCDIC: Hebrew</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * ebcdic-cp-he</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * IANA</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * cp424</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">EBCDIC: Switzerland</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * ebcdic-cp-ch</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * IANA</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * cp500</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">EBCDIC: Roece</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * ebcdic-cp-roece</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * IANA</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * cp870</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">EBCDIC: Yugoslavia</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * ebcdic-cp-yu</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * IANA</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * cp870</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">EBCDIC: Iceland</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * ebcdic-cp-is</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * IANA</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * cp871</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">EBCDIC: Urdu</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * ebcdic-cp-ar2</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * IANA</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * cp918</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">Chinese for PRC, mixed 1/2 byte</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * gb2312</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * MIME</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * GB2312</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">Extended Unix Code, packed for Japanese</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * euc-jp</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * MIME</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * eucjis</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">Japanese: iso-2022-jp</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * iso-2020-jp</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * MIME</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * JIS</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">Japanese: Shift JIS</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * Shift_JIS</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * MIME</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * SJIS</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">Chinese: Big5</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * Big5</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * MIME</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * Big5</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">Extended Unix Code, packed for Korean</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * euc-kr</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * MIME</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * iso2022kr</TD>
 * </TR>
 * <TR>
 * <TD WIDTH="33%">Cyrillic</TD>
 * <TD WIDTH="15%">
 * <P ALIGN="CENTER">
 * koi8-r</TD>
 * <TD WIDTH="12%">
 * <P ALIGN="CENTER">
 * MIME</TD>
 * <TD WIDTH="31%">
 * <P ALIGN="CENTER">
 * koi8-r</TD>
 * </TR>
 * </TABLE>
 *
 * @author TAMURA Kent, IBM
 * @author Andy Clark, IBM
 *
 * @author Ronald Brill
 */
public final class EncodingMap implements EncodingTranslator {

    /**
     * Singleton.
     */
    public static final EncodingMap INSTANCE = new EncodingMap();

    /** fIANA2JavaMap */
    private static final HashMap<String, String> IANA_2_JAVA_MAP = new HashMap<>();

    static {
        // add IANA to Java encoding mappings.
        IANA_2_JAVA_MAP.put("BIG5", "Big5");
        IANA_2_JAVA_MAP.put("CSBIG5", "Big5");
        IANA_2_JAVA_MAP.put("CP037", "CP037");
        IANA_2_JAVA_MAP.put("IBM037", "CP037");
        IANA_2_JAVA_MAP.put("CSIBM037", "CP037");
        IANA_2_JAVA_MAP.put("EBCDIC-CP-US", "CP037");
        IANA_2_JAVA_MAP.put("EBCDIC-CP-CA", "CP037");
        IANA_2_JAVA_MAP.put("EBCDIC-CP-NL", "CP037");
        IANA_2_JAVA_MAP.put("EBCDIC-CP-WT", "CP037");
        IANA_2_JAVA_MAP.put("IBM273", "CP273");
        IANA_2_JAVA_MAP.put("CP273", "CP273");
        IANA_2_JAVA_MAP.put("CSIBM273", "CP273");
        IANA_2_JAVA_MAP.put("IBM277", "CP277");
        IANA_2_JAVA_MAP.put("CP277", "CP277");
        IANA_2_JAVA_MAP.put("CSIBM277", "CP277");
        IANA_2_JAVA_MAP.put("EBCDIC-CP-DK", "CP277");
        IANA_2_JAVA_MAP.put("EBCDIC-CP-NO", "CP277");
        IANA_2_JAVA_MAP.put("IBM278", "CP278");
        IANA_2_JAVA_MAP.put("CP278", "CP278");
        IANA_2_JAVA_MAP.put("CSIBM278", "CP278");
        IANA_2_JAVA_MAP.put("EBCDIC-CP-FI", "CP278");
        IANA_2_JAVA_MAP.put("EBCDIC-CP-SE", "CP278");
        IANA_2_JAVA_MAP.put("IBM280", "CP280");
        IANA_2_JAVA_MAP.put("CP280", "CP280");
        IANA_2_JAVA_MAP.put("CSIBM280", "CP280");
        IANA_2_JAVA_MAP.put("EBCDIC-CP-IT", "CP280");
        IANA_2_JAVA_MAP.put("IBM284", "CP284");
        IANA_2_JAVA_MAP.put("CP284", "CP284");
        IANA_2_JAVA_MAP.put("CSIBM284", "CP284");
        IANA_2_JAVA_MAP.put("EBCDIC-CP-ES", "CP284");
        IANA_2_JAVA_MAP.put("EBCDIC-CP-GB", "CP285");
        IANA_2_JAVA_MAP.put("IBM285", "CP285");
        IANA_2_JAVA_MAP.put("CP285", "CP285");
        IANA_2_JAVA_MAP.put("CSIBM285", "CP285");
        IANA_2_JAVA_MAP.put("EBCDIC-JP-KANA", "CP290");
        IANA_2_JAVA_MAP.put("IBM290", "CP290");
        IANA_2_JAVA_MAP.put("CP290", "CP290");
        IANA_2_JAVA_MAP.put("CSIBM290", "CP290");
        IANA_2_JAVA_MAP.put("EBCDIC-CP-FR", "CP297");
        IANA_2_JAVA_MAP.put("IBM297", "CP297");
        IANA_2_JAVA_MAP.put("CP297", "CP297");
        IANA_2_JAVA_MAP.put("CSIBM297", "CP297");
        IANA_2_JAVA_MAP.put("EBCDIC-CP-AR1", "CP420");
        IANA_2_JAVA_MAP.put("IBM420", "CP420");
        IANA_2_JAVA_MAP.put("CP420", "CP420");
        IANA_2_JAVA_MAP.put("CSIBM420", "CP420");
        IANA_2_JAVA_MAP.put("EBCDIC-CP-HE", "CP424");
        IANA_2_JAVA_MAP.put("IBM424", "CP424");
        IANA_2_JAVA_MAP.put("CP424", "CP424");
        IANA_2_JAVA_MAP.put("CSIBM424", "CP424");
        IANA_2_JAVA_MAP.put("IBM437", "CP437");
        IANA_2_JAVA_MAP.put("437", "CP437");
        IANA_2_JAVA_MAP.put("CP437", "CP437");
        IANA_2_JAVA_MAP.put("CSPC8CODEPAGE437", "CP437");
        IANA_2_JAVA_MAP.put("IBM500", "CP500");
        IANA_2_JAVA_MAP.put("CP500", "CP500");
        IANA_2_JAVA_MAP.put("CSIBM500", "CP500");
        IANA_2_JAVA_MAP.put("EBCDIC-CP-CH", "CP500");
        IANA_2_JAVA_MAP.put("EBCDIC-CP-BE", "CP500");
        IANA_2_JAVA_MAP.put("IBM775", "CP775");
        IANA_2_JAVA_MAP.put("CP775", "CP775");
        IANA_2_JAVA_MAP.put("CSPC775BALTIC", "CP775");
        IANA_2_JAVA_MAP.put("IBM850", "CP850");
        IANA_2_JAVA_MAP.put("850", "CP850");
        IANA_2_JAVA_MAP.put("CP850", "CP850");
        IANA_2_JAVA_MAP.put("CSPC850MULTILINGUAL", "CP850");
        IANA_2_JAVA_MAP.put("IBM852", "CP852");
        IANA_2_JAVA_MAP.put("852", "CP852");
        IANA_2_JAVA_MAP.put("CP852", "CP852");
        IANA_2_JAVA_MAP.put("CSPCP852", "CP852");
        IANA_2_JAVA_MAP.put("IBM855", "CP855");
        IANA_2_JAVA_MAP.put("855", "CP855");
        IANA_2_JAVA_MAP.put("CP855", "CP855");
        IANA_2_JAVA_MAP.put("CSIBM855", "CP855");
        IANA_2_JAVA_MAP.put("IBM857", "CP857");
        IANA_2_JAVA_MAP.put("857", "CP857");
        IANA_2_JAVA_MAP.put("CP857", "CP857");
        IANA_2_JAVA_MAP.put("CSIBM857", "CP857");
        IANA_2_JAVA_MAP.put("IBM00858", "CP858");
        IANA_2_JAVA_MAP.put("CP00858", "CP858");
        IANA_2_JAVA_MAP.put("CCSID00858", "CP858");
        IANA_2_JAVA_MAP.put("IBM860", "CP860");
        IANA_2_JAVA_MAP.put("860", "CP860");
        IANA_2_JAVA_MAP.put("CP860", "CP860");
        IANA_2_JAVA_MAP.put("CSIBM860", "CP860");
        IANA_2_JAVA_MAP.put("IBM861", "CP861");
        IANA_2_JAVA_MAP.put("861", "CP861");
        IANA_2_JAVA_MAP.put("CP861", "CP861");
        IANA_2_JAVA_MAP.put("CP-IS", "CP861");
        IANA_2_JAVA_MAP.put("CSIBM861", "CP861");
        IANA_2_JAVA_MAP.put("IBM862", "CP862");
        IANA_2_JAVA_MAP.put("862", "CP862");
        IANA_2_JAVA_MAP.put("CP862", "CP862");
        IANA_2_JAVA_MAP.put("CSPC862LATINHEBREW", "CP862");
        IANA_2_JAVA_MAP.put("IBM863", "CP863");
        IANA_2_JAVA_MAP.put("863", "CP863");
        IANA_2_JAVA_MAP.put("CP863", "CP863");
        IANA_2_JAVA_MAP.put("CSIBM863", "CP863");
        IANA_2_JAVA_MAP.put("IBM864", "CP864");
        IANA_2_JAVA_MAP.put("CP864", "CP864");
        IANA_2_JAVA_MAP.put("CSIBM864", "CP864");
        IANA_2_JAVA_MAP.put("IBM865", "CP865");
        IANA_2_JAVA_MAP.put("865", "CP865");
        IANA_2_JAVA_MAP.put("CP865", "CP865");
        IANA_2_JAVA_MAP.put("CSIBM865", "CP865");
        IANA_2_JAVA_MAP.put("IBM866", "CP866");
        IANA_2_JAVA_MAP.put("866", "CP866");
        IANA_2_JAVA_MAP.put("CP866", "CP866");
        IANA_2_JAVA_MAP.put("CSIBM866", "CP866");
        IANA_2_JAVA_MAP.put("IBM868", "CP868");
        IANA_2_JAVA_MAP.put("CP868", "CP868");
        IANA_2_JAVA_MAP.put("CSIBM868", "CP868");
        IANA_2_JAVA_MAP.put("CP-AR", "CP868");
        IANA_2_JAVA_MAP.put("IBM869", "CP869");
        IANA_2_JAVA_MAP.put("CP869", "CP869");
        IANA_2_JAVA_MAP.put("CSIBM869", "CP869");
        IANA_2_JAVA_MAP.put("CP-GR", "CP869");
        IANA_2_JAVA_MAP.put("IBM870", "CP870");
        IANA_2_JAVA_MAP.put("CP870", "CP870");
        IANA_2_JAVA_MAP.put("CSIBM870", "CP870");
        IANA_2_JAVA_MAP.put("EBCDIC-CP-ROECE", "CP870");
        IANA_2_JAVA_MAP.put("EBCDIC-CP-YU", "CP870");
        IANA_2_JAVA_MAP.put("IBM871", "CP871");
        IANA_2_JAVA_MAP.put("CP871", "CP871");
        IANA_2_JAVA_MAP.put("CSIBM871", "CP871");
        IANA_2_JAVA_MAP.put("EBCDIC-CP-IS", "CP871");
        IANA_2_JAVA_MAP.put("IBM918", "CP918");
        IANA_2_JAVA_MAP.put("CP918", "CP918");
        IANA_2_JAVA_MAP.put("CSIBM918", "CP918");
        IANA_2_JAVA_MAP.put("EBCDIC-CP-AR2", "CP918");
        IANA_2_JAVA_MAP.put("IBM00924", "CP924");
        IANA_2_JAVA_MAP.put("CP00924", "CP924");
        IANA_2_JAVA_MAP.put("CCSID00924", "CP924");
        // is this an error???
        IANA_2_JAVA_MAP.put("EBCDIC-LATIN9--EURO", "CP924");
        IANA_2_JAVA_MAP.put("IBM1026", "CP1026");
        IANA_2_JAVA_MAP.put("CP1026", "CP1026");
        IANA_2_JAVA_MAP.put("CSIBM1026", "CP1026");
        IANA_2_JAVA_MAP.put("IBM01140", "Cp1140");
        IANA_2_JAVA_MAP.put("CP01140", "Cp1140");
        IANA_2_JAVA_MAP.put("CCSID01140", "Cp1140");
        IANA_2_JAVA_MAP.put("IBM01141", "Cp1141");
        IANA_2_JAVA_MAP.put("CP01141", "Cp1141");
        IANA_2_JAVA_MAP.put("CCSID01141", "Cp1141");
        IANA_2_JAVA_MAP.put("IBM01142", "Cp1142");
        IANA_2_JAVA_MAP.put("CP01142", "Cp1142");
        IANA_2_JAVA_MAP.put("CCSID01142", "Cp1142");
        IANA_2_JAVA_MAP.put("IBM01143", "Cp1143");
        IANA_2_JAVA_MAP.put("CP01143", "Cp1143");
        IANA_2_JAVA_MAP.put("CCSID01143", "Cp1143");
        IANA_2_JAVA_MAP.put("IBM01144", "Cp1144");
        IANA_2_JAVA_MAP.put("CP01144", "Cp1144");
        IANA_2_JAVA_MAP.put("CCSID01144", "Cp1144");
        IANA_2_JAVA_MAP.put("IBM01145", "Cp1145");
        IANA_2_JAVA_MAP.put("CP01145", "Cp1145");
        IANA_2_JAVA_MAP.put("CCSID01145", "Cp1145");
        IANA_2_JAVA_MAP.put("IBM01146", "Cp1146");
        IANA_2_JAVA_MAP.put("CP01146", "Cp1146");
        IANA_2_JAVA_MAP.put("CCSID01146", "Cp1146");
        IANA_2_JAVA_MAP.put("IBM01147", "Cp1147");
        IANA_2_JAVA_MAP.put("CP01147", "Cp1147");
        IANA_2_JAVA_MAP.put("CCSID01147", "Cp1147");
        IANA_2_JAVA_MAP.put("IBM01148", "Cp1148");
        IANA_2_JAVA_MAP.put("CP01148", "Cp1148");
        IANA_2_JAVA_MAP.put("CCSID01148", "Cp1148");
        IANA_2_JAVA_MAP.put("IBM01149", "Cp1149");
        IANA_2_JAVA_MAP.put("CP01149", "Cp1149");
        IANA_2_JAVA_MAP.put("CCSID01149", "Cp1149");
        IANA_2_JAVA_MAP.put("EUC-JP", "EUCJIS");
        IANA_2_JAVA_MAP.put("CSEUCPKDFMTJAPANESE", "EUCJIS");
        IANA_2_JAVA_MAP.put("EXTENDED_UNIX_CODE_PACKED_FORMAT_FOR_JAPANESE", "EUCJIS");
        IANA_2_JAVA_MAP.put("EUC-KR", "KSC5601");
        IANA_2_JAVA_MAP.put("CSEUCKR", "KSC5601");
        IANA_2_JAVA_MAP.put("KS_C_5601-1987", "KS_C_5601-1987");
        IANA_2_JAVA_MAP.put("ISO-IR-149", "KS_C_5601-1987");
        IANA_2_JAVA_MAP.put("KS_C_5601-1989", "KS_C_5601-1987");
        IANA_2_JAVA_MAP.put("KSC_5601", "KS_C_5601-1987");
        IANA_2_JAVA_MAP.put("KOREAN", "KS_C_5601-1987");
        IANA_2_JAVA_MAP.put("CSKSC56011987", "KS_C_5601-1987");
        IANA_2_JAVA_MAP.put("GB2312", "GB2312");
        IANA_2_JAVA_MAP.put("CSGB2312", "GB2312");
        IANA_2_JAVA_MAP.put("ISO-2022-JP", "JIS");
        IANA_2_JAVA_MAP.put("CSISO2022JP", "JIS");
        IANA_2_JAVA_MAP.put("ISO-2022-KR", "ISO2022KR");
        IANA_2_JAVA_MAP.put("CSISO2022KR", "ISO2022KR");
        IANA_2_JAVA_MAP.put("ISO-2022-CN", "ISO2022CN");

        IANA_2_JAVA_MAP.put("X0201", "JIS0201");
        IANA_2_JAVA_MAP.put("CSISO13JISC6220JP", "JIS0201");
        IANA_2_JAVA_MAP.put("X0208", "JIS0208");
        IANA_2_JAVA_MAP.put("ISO-IR-87", "JIS0208");
        IANA_2_JAVA_MAP.put("X0208dbiJIS_X0208-1983", "JIS0208");
        IANA_2_JAVA_MAP.put("CSISO87JISX0208", "JIS0208");
        IANA_2_JAVA_MAP.put("X0212", "JIS0212");
        IANA_2_JAVA_MAP.put("ISO-IR-159", "JIS0212");
        IANA_2_JAVA_MAP.put("CSISO159JISX02121990", "JIS0212");
        IANA_2_JAVA_MAP.put("GB18030", "GB18030");
        IANA_2_JAVA_MAP.put("GBK", "GBK");
        IANA_2_JAVA_MAP.put("CP936", "GBK");
        IANA_2_JAVA_MAP.put("MS936", "GBK");
        IANA_2_JAVA_MAP.put("WINDOWS-936", "GBK");
        IANA_2_JAVA_MAP.put("SHIFT_JIS", "SJIS");
        IANA_2_JAVA_MAP.put("CSSHIFTJIS", "SJIS");
        IANA_2_JAVA_MAP.put("MS_KANJI", "SJIS");
        IANA_2_JAVA_MAP.put("WINDOWS-31J", "MS932");
        IANA_2_JAVA_MAP.put("CSWINDOWS31J", "MS932");

        // Add support for Cp1252 and its friends
        IANA_2_JAVA_MAP.put("WINDOWS-1250", "Cp1250");
        IANA_2_JAVA_MAP.put("WINDOWS-1251", "Cp1251");
        IANA_2_JAVA_MAP.put("WINDOWS-1252", "Cp1252");
        IANA_2_JAVA_MAP.put("WINDOWS-1253", "Cp1253");
        IANA_2_JAVA_MAP.put("WINDOWS-1254", "Cp1254");
        IANA_2_JAVA_MAP.put("WINDOWS-1255", "Cp1255");
        IANA_2_JAVA_MAP.put("WINDOWS-1256", "Cp1256");
        IANA_2_JAVA_MAP.put("WINDOWS-1257", "Cp1257");
        IANA_2_JAVA_MAP.put("WINDOWS-1258", "Cp1258");
        IANA_2_JAVA_MAP.put("TIS-620", "TIS620");

        IANA_2_JAVA_MAP.put("ISO-8859-1", "ISO8859_1");
        IANA_2_JAVA_MAP.put("ISO-IR-100", "ISO8859_1");
        IANA_2_JAVA_MAP.put("ISO_8859-1", "ISO8859_1");
        IANA_2_JAVA_MAP.put("LATIN1", "ISO8859_1");
        IANA_2_JAVA_MAP.put("CSISOLATIN1", "ISO8859_1");
        IANA_2_JAVA_MAP.put("L1", "ISO8859_1");
        IANA_2_JAVA_MAP.put("IBM819", "ISO8859_1");
        IANA_2_JAVA_MAP.put("CP819", "ISO8859_1");

        IANA_2_JAVA_MAP.put("ISO-8859-2", "ISO8859_2");
        IANA_2_JAVA_MAP.put("ISO-IR-101", "ISO8859_2");
        IANA_2_JAVA_MAP.put("ISO_8859-2", "ISO8859_2");
        IANA_2_JAVA_MAP.put("LATIN2", "ISO8859_2");
        IANA_2_JAVA_MAP.put("CSISOLATIN2", "ISO8859_2");
        IANA_2_JAVA_MAP.put("L2", "ISO8859_2");

        IANA_2_JAVA_MAP.put("ISO-8859-3", "ISO8859_3");
        IANA_2_JAVA_MAP.put("ISO-IR-109", "ISO8859_3");
        IANA_2_JAVA_MAP.put("ISO_8859-3", "ISO8859_3");
        IANA_2_JAVA_MAP.put("LATIN3", "ISO8859_3");
        IANA_2_JAVA_MAP.put("CSISOLATIN3", "ISO8859_3");
        IANA_2_JAVA_MAP.put("L3", "ISO8859_3");

        IANA_2_JAVA_MAP.put("ISO-8859-4", "ISO8859_4");
        IANA_2_JAVA_MAP.put("ISO-IR-110", "ISO8859_4");
        IANA_2_JAVA_MAP.put("ISO_8859-4", "ISO8859_4");
        IANA_2_JAVA_MAP.put("LATIN4", "ISO8859_4");
        IANA_2_JAVA_MAP.put("CSISOLATIN4", "ISO8859_4");
        IANA_2_JAVA_MAP.put("L4", "ISO8859_4");

        IANA_2_JAVA_MAP.put("ISO-8859-5", "ISO8859_5");
        IANA_2_JAVA_MAP.put("ISO-IR-144", "ISO8859_5");
        IANA_2_JAVA_MAP.put("ISO_8859-5", "ISO8859_5");
        IANA_2_JAVA_MAP.put("CYRILLIC", "ISO8859_5");
        IANA_2_JAVA_MAP.put("CSISOLATINCYRILLIC", "ISO8859_5");

        IANA_2_JAVA_MAP.put("ISO-8859-6", "ISO8859_6");
        IANA_2_JAVA_MAP.put("ISO-IR-127", "ISO8859_6");
        IANA_2_JAVA_MAP.put("ISO_8859-6", "ISO8859_6");
        IANA_2_JAVA_MAP.put("ECMA-114", "ISO8859_6");
        IANA_2_JAVA_MAP.put("ASMO-708", "ISO8859_6");
        IANA_2_JAVA_MAP.put("ARABIC", "ISO8859_6");
        IANA_2_JAVA_MAP.put("CSISOLATINARABIC", "ISO8859_6");

        IANA_2_JAVA_MAP.put("ISO-8859-7", "ISO8859_7");
        IANA_2_JAVA_MAP.put("ISO-IR-126", "ISO8859_7");
        IANA_2_JAVA_MAP.put("ISO_8859-7", "ISO8859_7");
        IANA_2_JAVA_MAP.put("ELOT_928", "ISO8859_7");
        IANA_2_JAVA_MAP.put("ECMA-118", "ISO8859_7");
        IANA_2_JAVA_MAP.put("GREEK", "ISO8859_7");
        IANA_2_JAVA_MAP.put("CSISOLATINGREEK", "ISO8859_7");
        IANA_2_JAVA_MAP.put("GREEK8", "ISO8859_7");

        IANA_2_JAVA_MAP.put("ISO-8859-8", "ISO8859_8");
        IANA_2_JAVA_MAP.put("ISO-8859-8-I", "ISO8859_8"); // added since this encoding only differs w.r.t. presentation
        IANA_2_JAVA_MAP.put("ISO-IR-138", "ISO8859_8");
        IANA_2_JAVA_MAP.put("ISO_8859-8", "ISO8859_8");
        IANA_2_JAVA_MAP.put("HEBREW", "ISO8859_8");
        IANA_2_JAVA_MAP.put("CSISOLATINHEBREW", "ISO8859_8");

        IANA_2_JAVA_MAP.put("ISO-8859-9", "ISO8859_9");
        IANA_2_JAVA_MAP.put("ISO-IR-148", "ISO8859_9");
        IANA_2_JAVA_MAP.put("ISO_8859-9", "ISO8859_9");
        IANA_2_JAVA_MAP.put("LATIN5", "ISO8859_9");
        IANA_2_JAVA_MAP.put("CSISOLATIN5", "ISO8859_9");
        IANA_2_JAVA_MAP.put("L5", "ISO8859_9");

        IANA_2_JAVA_MAP.put("ISO-8859-13", "ISO8859_13");

        IANA_2_JAVA_MAP.put("ISO-8859-15", "ISO8859_15_FDIS");
        IANA_2_JAVA_MAP.put("ISO_8859-15", "ISO8859_15_FDIS");
        IANA_2_JAVA_MAP.put("LATIN-9", "ISO8859_15_FDIS");

        IANA_2_JAVA_MAP.put("KOI8-R", "KOI8_R");
        IANA_2_JAVA_MAP.put("CSKOI8R", "KOI8_R");
        IANA_2_JAVA_MAP.put("US-ASCII", "ASCII");
        IANA_2_JAVA_MAP.put("ISO-IR-6", "ASCII");
        IANA_2_JAVA_MAP.put("ANSI_X3.4-1968", "ASCII");
        IANA_2_JAVA_MAP.put("ANSI_X3.4-1986", "ASCII");
        IANA_2_JAVA_MAP.put("ISO_646.IRV:1991", "ASCII");
        IANA_2_JAVA_MAP.put("ASCII", "ASCII");
        IANA_2_JAVA_MAP.put("CSASCII", "ASCII");
        IANA_2_JAVA_MAP.put("ISO646-US", "ASCII");
        IANA_2_JAVA_MAP.put("US", "ASCII");
        IANA_2_JAVA_MAP.put("IBM367", "ASCII");
        IANA_2_JAVA_MAP.put("CP367", "ASCII");
        IANA_2_JAVA_MAP.put("UTF-8", "UTF-8");
        IANA_2_JAVA_MAP.put("UTF-16", "UTF-16");
        IANA_2_JAVA_MAP.put("UTF-16BE", "UnicodeBig");
        IANA_2_JAVA_MAP.put("UTF-16LE", "UnicodeLittle");

        // support for 1047, as proposed to be added to the
        // IANA registry in
        // http://lists.w3.org/Archives/Public/ietf-charset/2002JulSep/0049.html
        IANA_2_JAVA_MAP.put("IBM-1047", "Cp1047");
        IANA_2_JAVA_MAP.put("IBM1047", "Cp1047");
        IANA_2_JAVA_MAP.put("CP1047", "Cp1047");

        // Adding new aliases as proposed in
        // http://lists.w3.org/Archives/Public/ietf-charset/2002JulSep/0058.html
        IANA_2_JAVA_MAP.put("IBM-37", "CP037");
        IANA_2_JAVA_MAP.put("IBM-273", "CP273");
        IANA_2_JAVA_MAP.put("IBM-277", "CP277");
        IANA_2_JAVA_MAP.put("IBM-278", "CP278");
        IANA_2_JAVA_MAP.put("IBM-280", "CP280");
        IANA_2_JAVA_MAP.put("IBM-284", "CP284");
        IANA_2_JAVA_MAP.put("IBM-285", "CP285");
        IANA_2_JAVA_MAP.put("IBM-290", "CP290");
        IANA_2_JAVA_MAP.put("IBM-297", "CP297");
        IANA_2_JAVA_MAP.put("IBM-420", "CP420");
        IANA_2_JAVA_MAP.put("IBM-424", "CP424");
        IANA_2_JAVA_MAP.put("IBM-437", "CP437");
        IANA_2_JAVA_MAP.put("IBM-500", "CP500");
        IANA_2_JAVA_MAP.put("IBM-775", "CP775");
        IANA_2_JAVA_MAP.put("IBM-850", "CP850");
        IANA_2_JAVA_MAP.put("IBM-852", "CP852");
        IANA_2_JAVA_MAP.put("IBM-855", "CP855");
        IANA_2_JAVA_MAP.put("IBM-857", "CP857");
        IANA_2_JAVA_MAP.put("IBM-858", "CP858");
        IANA_2_JAVA_MAP.put("IBM-860", "CP860");
        IANA_2_JAVA_MAP.put("IBM-861", "CP861");
        IANA_2_JAVA_MAP.put("IBM-862", "CP862");
        IANA_2_JAVA_MAP.put("IBM-863", "CP863");
        IANA_2_JAVA_MAP.put("IBM-864", "CP864");
        IANA_2_JAVA_MAP.put("IBM-865", "CP865");
        IANA_2_JAVA_MAP.put("IBM-866", "CP866");
        IANA_2_JAVA_MAP.put("IBM-868", "CP868");
        IANA_2_JAVA_MAP.put("IBM-869", "CP869");
        IANA_2_JAVA_MAP.put("IBM-870", "CP870");
        IANA_2_JAVA_MAP.put("IBM-871", "CP871");
        IANA_2_JAVA_MAP.put("IBM-918", "CP918");
        IANA_2_JAVA_MAP.put("IBM-924", "CP924");
        IANA_2_JAVA_MAP.put("IBM-1026", "CP1026");
        IANA_2_JAVA_MAP.put("IBM-1140", "Cp1140");
        IANA_2_JAVA_MAP.put("IBM-1141", "Cp1141");
        IANA_2_JAVA_MAP.put("IBM-1142", "Cp1142");
        IANA_2_JAVA_MAP.put("IBM-1143", "Cp1143");
        IANA_2_JAVA_MAP.put("IBM-1144", "Cp1144");
        IANA_2_JAVA_MAP.put("IBM-1145", "Cp1145");
        IANA_2_JAVA_MAP.put("IBM-1146", "Cp1146");
        IANA_2_JAVA_MAP.put("IBM-1147", "Cp1147");
        IANA_2_JAVA_MAP.put("IBM-1148", "Cp1148");
        IANA_2_JAVA_MAP.put("IBM-1149", "Cp1149");
        IANA_2_JAVA_MAP.put("IBM-819", "ISO8859_1");
        IANA_2_JAVA_MAP.put("IBM-367", "ASCII");
    }

    private EncodingMap() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String encodingNameFromLabel(final String charsetLabel) {
        if (charsetLabel == null || charsetLabel.length() < 2) {
            return null;
        }

        final String label = charsetLabel.trim().toUpperCase(Locale.ROOT);
        return IANA_2_JAVA_MAP.get(label);
    }
}
