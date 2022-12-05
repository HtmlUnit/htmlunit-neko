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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.opentest4j.AssertionFailedError;

import net.sourceforge.htmlunit.xerces.xni.parser.XMLDocumentFilter;
import net.sourceforge.htmlunit.xerces.xni.parser.XMLInputSource;
import net.sourceforge.htmlunit.xerces.xni.parser.XMLParserConfiguration;

/**
 * This test generates canonical result using the <code>Writer</code> class
 * and compares it against the expected canonical output. Simple as that.
 *
 * @author Andy Clark
 * @author Marc Guillemot
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
public class CanonicalTest {

    private static final File dataDir = new File("src/test/resources");
    private static final File canonicalDir = new File("src/test/resources/canonical");
    private static final File outputDir = new File("target/data/output");

    @TestFactory
    public Iterable<DynamicTest> suite() throws Exception {
        // System.out.println(canonicalDir.getAbsolutePath());
        outputDir.mkdirs();

        final List<File> dataFiles = new ArrayList<>();
        dataDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File file) {
                final String name = file.getName();
                if (file.isDirectory() && !"canonical".equals(name)) {
                    file.listFiles(this);
                }
                else if (name.startsWith("test") && name.endsWith(".html")) {
                    dataFiles.add(file);
                }
                return false; // we don't care to listFiles' result
            }
        });
        Collections.sort(dataFiles);

        List<DynamicTest> tests = new ArrayList<>();
        for (int i=0; i < dataFiles.size(); i++) {
            // suite.addTest(new CanonicalTest(dataFiles.get(i)));
            final File dataFile = dataFiles.get(i);
            tests.add(DynamicTest.dynamicTest(dataFile.getName(), () -> runTest(dataFile)));
        }
        return tests;
    }

    protected void runTest(File dataFile) throws Exception {
        final String dataLines = getResult(dataFile);
        try
        {
            // prepare for future changes where canonical files are next to test file
            File canonicalFile = new File(dataFile.getParentFile(), dataFile.getName() + ".canonical");
            if (!canonicalFile.exists()) {
                canonicalFile = new File(canonicalDir, dataFile.getName());
            }
            if (!canonicalFile.exists()) {
                fail("Canonical file not found for input: " + dataFile.getAbsolutePath() + ": " + dataLines);
            }

            File nyiFile = new File(dataFile.getParentFile(), dataFile.getName() + ".notyetimplemented");
            if (nyiFile.exists()) {
                try {
                    assertEquals(getCanonical(canonicalFile), dataLines, dataFile.toString());
                    fail("test " + dataFile.getName() + "is marked as not yet implemented but already works");
                }
                catch (final AssertionFailedError e) {
                    // expected
                }
                assertEquals(getCanonical(nyiFile), dataLines, "NYI: " + dataFile);
            } else {
                assertEquals(getCanonical(canonicalFile), dataLines, dataFile.toString());
            }
        }
        catch (final AssertionFailedError e) {
            final File output = new File(outputDir, dataFile.getName());
            try (final PrintWriter pw = new PrintWriter(new FileOutputStream(output))) {
                pw.print(dataLines);
            }
            throw e;
        }
    }

    private static String getCanonical(final File infile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new UTF8BOMSkipper(new FileInputStream(infile)), StandardCharsets.UTF_8))) {
            final StringBuffer sb = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }

    private static String getResult(final File infile) throws IOException {
        try (StringWriter out = new StringWriter()) {
            // create filters
            final XMLDocumentFilter[] filters = { new Writer(out) };

            // create parser
            final XMLParserConfiguration parser = new HTMLConfiguration();

            // parser settings
            parser.setProperty("http://cyberneko.org/html/properties/filters", filters);
            final String infilename = infile.toString();
            final File insettings = new File(infilename+".settings");
            if (insettings.exists()) {
                try (BufferedReader settings = new BufferedReader(new FileReader(insettings))) {
                    String settingline;
                    while ((settingline = settings.readLine()) != null) {
                        final StringTokenizer tokenizer = new StringTokenizer(settingline);
                        final String type = tokenizer.nextToken();
                        final String id = tokenizer.nextToken();
                        final String value = tokenizer.nextToken();
                        if (type.equals("feature")) {
                            parser.setFeature(id, value.equals("true"));
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
            final StringBuffer sb = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }
}
