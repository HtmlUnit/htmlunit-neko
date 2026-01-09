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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringTokenizer;

import org.htmlunit.cyberneko.xerces.xni.parser.XMLDocumentFilter;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLInputSource;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLParserConfiguration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;

/**
 * This test generates canonical result using the <code>Writer</code> class
 * and compares it against the expected canonical output. Simple as that.
 *
 * @author Andy Clark
 * @author Marc Guillemot
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
public class CanonicalTest extends AbstractCanonicalTest {

    @ParameterizedTest
    @MethodSource("testFiles")
    public void runTest(final File dataFile) throws Exception {
        final String dataLines = getResult(dataFile, false);
        test(dataFile, dataLines);
    }

    @ParameterizedTest
    @MethodSource("testFiles")
    public void runTestWithCachedElementsProvider(final File dataFile) throws Exception {
        final String dataLines = getResult(dataFile, true);
        test(dataFile, dataLines);
    }

    private static void test(final File dataFile, final String dataLines) throws Exception {
        try {
            // prepare for future changes where canonical files are next to test file
            final File canonicalFile = new File(dataFile.getParentFile(), dataFile.getName() + ".canonical");
            if (!canonicalFile.exists()) {
                fail("Canonical file not found for input: " + dataFile.getAbsolutePath() + ": " + dataLines);
            }

            final File nyiFile = new File(canonicalFile.getParentFile(),
                                            dataFile.getName() + ".canonical.nyi");
            if (nyiFile.exists()) {
                try {
                    assertEquals(getCanonical(canonicalFile), dataLines, dataFile.toString());
                    fail("test " + dataFile.getName() + "is marked as not yet implemented but already works");
                }
                catch (final AssertionFailedError e) {
                    // expected
                }

                assertEquals(getCanonical(nyiFile), dataLines, "NYI: " + dataFile);
            }
            else {
                assertEquals(getCanonical(canonicalFile), dataLines, dataFile.toString());
            }
        }
        catch (final AssertionFailedError e) {
            String path = dataFile.getAbsolutePath();
            path = path.substring(path.indexOf("\\testfiles\\") + 11);
            final File output = new File(OUTOUT_DIR, path + ".canonical");
            Files.createDirectories(Path.of(output.getParentFile().getPath()));
            try (PrintWriter pw = new PrintWriter(Files.newOutputStream(output.toPath()))) {
                pw.print(dataLines);
            }
            throw e;
        }
    }

    private static String getResult(final File infile, final boolean cached) throws IOException {
        try (StringWriter out = new StringWriter()) {
            // create filters
            final XMLDocumentFilter[] filters = {new Writer(out)};

            // create parser
            final XMLParserConfiguration parser;
            if (cached) {
                parser = new HTMLConfiguration(new HTMLElements.HTMLElementsWithCache(new HTMLElements()));
            }
            else {
                parser = new HTMLConfiguration();
            }

            // parser settings
            parser.setProperty("http://cyberneko.org/html/properties/filters", filters);

            final String infilename = infile.toString();
            final File insettings = new File(infilename + ".settings");
            if (insettings.exists()) {
                try (BufferedReader settings = new BufferedReader(new FileReader(insettings))) {
                    String settingline;
                    while ((settingline = settings.readLine()) != null) {
                        final StringTokenizer tokenizer = new StringTokenizer(settingline);
                        final String type = tokenizer.nextToken();
                        final String id = tokenizer.nextToken();
                        final String value = tokenizer.nextToken();
                        if ("feature".equals(type)) {
                            parser.setFeature(id, "true".equals(value));
                            if (HTMLScanner.REPORT_ERRORS.equals(id)) {
                                parser.setErrorHandler(new HTMLErrorHandler(out));
                            }
                        }
                        else {
                            parser.setProperty(id, value);
                        }
                    }
                }
            }

            // parse
            parser.parse(new XMLInputSource(null, infilename, null));
            final BufferedReader reader = new BufferedReader(new StringReader(out.toString()));
            final StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }
}
