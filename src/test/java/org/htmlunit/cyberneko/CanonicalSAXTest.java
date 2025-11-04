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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.StringTokenizer;

import org.htmlunit.cyberneko.parsers.SAXParser;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLInputSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * This test generates canonical result using the <code>Writer</code> class
 * and compares it against the expected canonical output. Simple as that.
 *
 * @author Andy Clark
 * @author Marc Guillemot
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
public class CanonicalSAXTest extends AbstractCanonicalTest {

    @ParameterizedTest
    @MethodSource("testFiles")
    public void runTest(final File dataFile) throws Exception {
        final String infilename = dataFile.toString();

        final SAXParser parser = new SAXParser();
        setupParser(infilename, parser);

        String saxDataLines = getResult(parser, infilename);
        verify(dataFile, saxDataLines);

        // reset and run again
        parser.reset();
        saxDataLines = getResult(parser, infilename);
        verify(dataFile, saxDataLines);
    }

    @ParameterizedTest
    @MethodSource("testFiles")
    public void runTestWithCachedElementsProvider(final File dataFile) throws Exception {
        final String infilename = dataFile.toString();

        final SAXParser parser = new SAXParser(new HTMLElements.HTMLElementsWithCache(new HTMLElements()));
        setupParser(infilename, parser);

        String saxDataLines = getResult(parser, infilename);
        verify(dataFile, saxDataLines);

        // reset and run again
        parser.reset();
        saxDataLines = getResult(parser, infilename);
        verify(dataFile, saxDataLines);
    }

    private static void verify(final File dataFile, final String saxDataLines)
                throws IOException, AssertionFailedError {
        try {
            // prepare for future changes where canonical files are next to test file
            File canonicalFile = new File(dataFile.getParentFile(), dataFile.getName() + ".canonical-sax");
            if (!canonicalFile.exists()) {
                canonicalFile = new File(dataFile.getParentFile(), dataFile.getName() + ".canonical");
            }

            if (!canonicalFile.exists()) {
                fail("Canonical file not found for input: " + dataFile.getAbsolutePath() + ": " + saxDataLines);
            }

            final File nyiFile = new File(canonicalFile.getParentFile(),
                                            dataFile.getName() + ".canonical-sax.nyi");
            if (nyiFile.exists()) {
                try {
                    assertEquals(getCanonical(canonicalFile), saxDataLines, dataFile.toString());
                    fail("test " + dataFile.getName() + "is marked as not yet implemented but already works");
                }
                catch (final AssertionFailedError e) {
                    // expected
                }

                assertEquals(getCanonical(nyiFile), saxDataLines, "NYI: " + dataFile);
            }
            else {
                assertEquals(getCanonical(canonicalFile), saxDataLines, dataFile.toString());
            }
        }
        catch (final AssertionFailedError e) {
            String path = dataFile.getAbsolutePath();
            path = path.substring(path.indexOf("\\testfiles\\") + 11);
            final File output = new File(OUTOUT_DIR, path + ".canonical-sax");
            Files.createDirectories(Paths.get(output.getParentFile().getPath()));
            try (PrintWriter pw = new PrintWriter(Files.newOutputStream(output.toPath()))) {
                pw.print(saxDataLines);
            }
            throw e;
        }
    }

    private static String getResult(final SAXParser parser, final String infilename) throws Exception {
        try (StringWriter out = new StringWriter()) {
            // parse
            final SaxHandler saxHandler = new SaxHandler(out);
            parser.setContentHandler(saxHandler);
            parser.setProperty("http://xml.org/sax/properties/lexical-handler", saxHandler);
            parser.setErrorHandler(saxHandler);
            parser.parse(new XMLInputSource(null, infilename, null));

            final StringBuilder sb = new StringBuilder();

            // first the error handler output
            final BufferedReader reader = new BufferedReader(new StringReader(out.toString()));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            return sb.toString();
        }
    }

    private static void setupParser(final String infilename, final SAXParser parser)
            throws IOException, SAXNotRecognizedException, SAXNotSupportedException, FileNotFoundException {
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
                    }
                    else {
                        parser.setProperty(id, value);
                    }
                }
            }
        }
    }
}
