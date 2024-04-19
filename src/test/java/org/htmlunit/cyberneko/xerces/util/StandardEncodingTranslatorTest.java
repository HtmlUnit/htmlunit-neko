/*
 * Copyright (c) 2024-2024 Atsushi Nakagawa
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.junit.jupiter.api.Test;

public class StandardEncodingTranslatorTest {

    @Test
    public void conversions() throws Exception {
        assertEquals("windows-1252", StandardEncodingTranslator.INSTANCE.encodingNameFromLabel("ascii"));
        assertEquals("utf-8", StandardEncodingTranslator.INSTANCE.encodingNameFromLabel("utf-8"));
        assertEquals("utf-8", StandardEncodingTranslator.INSTANCE.encodingNameFromLabel("utf8"));
        assertEquals("windows-874", StandardEncodingTranslator.INSTANCE.encodingNameFromLabel("windows-874"));
        assertEquals("koi8-u", StandardEncodingTranslator.INSTANCE.encodingNameFromLabel("koi8-u"));
        assertEquals("big5-hkscs", StandardEncodingTranslator.INSTANCE.encodingNameFromLabel("big5"));
        assertEquals("windows-949", StandardEncodingTranslator.INSTANCE.encodingNameFromLabel("euc-kr"));
        assertEquals("windows-949", StandardEncodingTranslator.INSTANCE.encodingNameFromLabel("windows-949"));
        assertEquals("windows-31j", StandardEncodingTranslator.INSTANCE.encodingNameFromLabel("shift_jis"));
        assertEquals("windows-31j", StandardEncodingTranslator.INSTANCE.encodingNameFromLabel("sjis"));
        assertEquals("windows-31j", StandardEncodingTranslator.INSTANCE.encodingNameFromLabel("windows-31j"));
        assertEquals("iso-8859-8", StandardEncodingTranslator.INSTANCE.encodingNameFromLabel("iso-8859-8-i"));
        assertEquals("utf-16le", StandardEncodingTranslator.INSTANCE.encodingNameFromLabel("utf-16le"));
        assertEquals("utf-16be", StandardEncodingTranslator.INSTANCE.encodingNameFromLabel("utf-16be"));

        assertEquals("x-MacRoman", StandardEncodingTranslator.INSTANCE.encodingNameFromLabel("macintosh"));
        assertEquals("x-MacUkraine", StandardEncodingTranslator.INSTANCE.encodingNameFromLabel("x-mac-cyrillic"));
        assertEquals("x-MacUkraine", StandardEncodingTranslator.INSTANCE.encodingNameFromLabel("x-mac-ukrainian"));

        // These are defined but not supported by reference Java 8
        // https://docs.oracle.com/javase/8/docs/technotes/guides/intl/encoding.doc.html
        assertEquals("iso-8859-10", StandardEncodingTranslator.INSTANCE.encodingNameFromLabel("iso-8859-10"));
        assertEquals("iso-8859-14", StandardEncodingTranslator.INSTANCE.encodingNameFromLabel("iso-8859-14"));
        // This one is added in Java 10 (https://bugs.openjdk.org/browse/JDK-8186751)
        assertEquals("iso-8859-16", StandardEncodingTranslator.INSTANCE.encodingNameFromLabel("iso-8859-16"));

        // Special WHATWG definitions
        assertEquals("replacement", StandardEncodingTranslator.INSTANCE.encodingNameFromLabel("replacement"));
        assertEquals("x-user-defined", StandardEncodingTranslator.INSTANCE.encodingNameFromLabel("x-user-defined"));

        assertEquals(null, StandardEncodingTranslator.INSTANCE.encodingNameFromLabel("foo"));
    }

    @Test
    void unsupportedCharsets() throws Exception {
        final Collection<String> unsupported = new LinkedHashSet<>();
        for (String encoding : new LinkedHashSet<>(StandardEncodingTranslator.ENCODING_FROM_LABEL.values())) {
            encoding = StandardEncodingTranslator.INSTANCE.encodingNameFromLabel(encoding);
            if (!Charset.isSupported(encoding)) {
                unsupported.add(encoding);
            }
        }
        // Added in Java 10 (https://bugs.openjdk.org/browse/JDK-8186751)
        unsupported.remove("iso-8859-16");

        assertEquals("[iso-8859-14, iso-8859-10, replacement, x-user-defined]", unsupported.toString());
    }
}
