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

import org.htmlunit.cyberneko.parsers.DOMParser;
import org.htmlunit.cyberneko.xerces.dom.CoreDocumentImpl;
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
public class CanonicalDomTest extends AbstractCanonicalTest {

    @ParameterizedTest
    @MethodSource("testFiles")
    public void runTest(final File dataFile) throws Exception {
        final String infilename = dataFile.toString();

        final DOMParser parser = new DOMParser(null);
        setupParser(infilename, parser);

        String domDataLines = getResult(parser, infilename);
        verify(dataFile, domDataLines);

        // reset and run again
        parser.reset();
        domDataLines = getResult(parser, infilename);
        verify(dataFile, domDataLines);
    }

    private static void verify(final File dataFile, final String domDataLines) throws IOException, AssertionFailedError {
        try {
            // prepare for future changes where canonical files are next to test file
            File canonicalFile = new File(dataFile.getParentFile(), dataFile.getName() + ".canonical-dom");
            if (!canonicalFile.exists()) {
                canonicalFile = new File(dataFile.getParentFile(), dataFile.getName() + ".canonical");

                if (!canonicalFile.exists()) {
                    canonicalFile = new File(canonicalDir, dataFile.getName() + ".canonical-dom");

                    if (!canonicalFile.exists()) {
                        canonicalFile = new File(canonicalDir, dataFile.getName());
                    }
                }
            }

            if (!canonicalFile.exists()) {
                fail("Canonical file not found for input: " + dataFile.getAbsolutePath() + ": " + domDataLines);
            }

            final File nyiFile = new File(canonicalFile.getParentFile(), canonicalFile.getName() + ".nyi");
            if (nyiFile.exists()) {
                try {
                    assertEquals(getCanonical(canonicalFile), domDataLines, dataFile.toString());
                    fail("test " + dataFile.getName() + "is marked as not yet implemented but already works");
                }
                catch (final AssertionFailedError e) {
                    // expected
                }

                assertEquals(getCanonical(nyiFile), domDataLines, "NYI: " + dataFile);
            }
            else {
                assertEquals(getCanonical(canonicalFile), domDataLines, dataFile.toString());
            }
        }
        catch (final AssertionFailedError e) {
            String path = dataFile.getAbsolutePath();
            path = path.substring(path.indexOf("\\testfiles\\") + 11);
            final File output = new File(outputDir, path + ".canonical-dom");
            Files.createDirectories(Paths.get(output.getParentFile().getPath()));
            try (PrintWriter pw = new PrintWriter(Files.newOutputStream(output.toPath()))) {
                pw.print(domDataLines);
            }
            throw e;
        }
    }

    private static String getResult(final DOMParser parser, final String infilename) throws Exception {
        try (StringWriter out = new StringWriter()) {
            // parse
            parser.parse(new XMLInputSource(null, infilename, null));

            final CoreDocumentImpl doc = (CoreDocumentImpl) parser.getDocument();

            final StringBuilder sb = new StringBuilder();

            // first the error handler output
            final BufferedReader reader = new BufferedReader(new StringReader(out.toString()));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            write(sb, doc);

            return sb.toString();
        }
    }

    private static void setupParser(final String infilename, final DOMParser parser)
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
                        /* feature not implemented
                        if (HTMLScanner.REPORT_ERRORS.equals(id)) {
                            parser.setErrorHandler(new ErrorHandler() {
                                @Override
                                public void warning(SAXParseException exception) throws SAXException {
                                    out.append("[warning]").append(exception.getMessage());
                                }

                                @Override
                                public void fatalError(SAXParseException exception) throws SAXException {
                                    out.append("[error]").append(exception.getMessage());
                                }

                                @Override
                                public void error(SAXParseException exception) throws SAXException {
                                    out.append("[error]").append(exception.getMessage());
                                }
                            });
                        }
                        */
                    }
                    else {
                        parser.setProperty(id, value);
                    }
                }
            }
        }
    }
}
