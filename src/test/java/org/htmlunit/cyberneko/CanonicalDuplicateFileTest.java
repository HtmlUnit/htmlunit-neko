/*
 * Copyright (c) 2002-2009 Andy Clark, Marc Guillemot
 * Copyright (c) 2017-2026 Ronald Brill
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

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * This test checks for duplicate expectation files.
 *
 * @author Ronald Brill
 */
public class CanonicalDuplicateFileTest extends AbstractCanonicalTest {

    @ParameterizedTest
    @MethodSource("testFiles")
    public void runTest(final File dataFile) throws Exception {
        test(dataFile);
    }

    private static void test(final File dataFile) throws Exception {
        final File canonicalFile = new File(dataFile.getParentFile(), dataFile.getName() + ".canonical");
        final File canonicalDomFile = new File(dataFile.getParentFile(), dataFile.getName() + ".canonical-dom");

        // CanonicalCustomSAXParserTest
        File candidate = new File(dataFile.getParentFile(), dataFile.getName() + ".canonical-sax-cust");
        assertNotSame(candidate, canonicalFile);

        // CanonicalDomFragmentTest
        candidate = new File(dataFile.getParentFile(), dataFile.getName() + ".canonical-frg");
        assertNotSame(candidate, canonicalFile);
        assertNotSame(candidate, canonicalDomFile);

        // CanonicalDomHtmlDocumentTest
        candidate = new File(dataFile.getParentFile(), dataFile.getName() + ".canonical-domhtml");
        assertNotSame(candidate, canonicalFile);
        assertNotSame(candidate, canonicalDomFile);

        // CanonicalDomTest
        candidate = new File(dataFile.getParentFile(), dataFile.getName() + ".canonical-dom");
        assertNotSame(candidate, canonicalFile);

        // CanonicalHtmlWriterTest

        // CanonicalSAXTest
        candidate = new File(dataFile.getParentFile(), dataFile.getName() + ".canonical-sax");
        assertNotSame(candidate, canonicalFile);

        // CanonicalTest

        // CanonicalXNITest
    }

    private static void assertNotSame(final File candidate, final File reference) throws IOException {
        if (reference.exists() && candidate.exists()) {
            final String referenceContent = getCanonical(reference);
            final String candidateContent = getCanonical(candidate);
            if (referenceContent.equals(candidateContent)) {
                fail("File '" + candidate.getAbsolutePath()
                        + "' is obsolete (has the same content as the fallback file '"
                        + reference.getAbsolutePath() + ".");
            }
        }
    }
}
