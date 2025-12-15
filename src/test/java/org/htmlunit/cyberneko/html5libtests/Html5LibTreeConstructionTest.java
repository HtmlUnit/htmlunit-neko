/*
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
package org.htmlunit.cyberneko.html5libtests;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * JUnit 5 test suite that runs all html5lib tree-construction tests.
 *
 * Usage:
 * 1. Clone html5lib-tests repository:
 *    git clone https://github.com/html5lib/html5lib-tests.git
 *
 * 2. Set the TEST_DATA_DIR system property or environment variable to point to
 *    the tree-construction directory:
 *    -Dhtml5lib.testdata.dir=/path/to/html5lib-tests/tree-construction
 *
 * @author Ronald Brill
 */
public class Html5LibTreeConstructionTest {

    private static String TEST_DATA_DIR_;

    @BeforeAll
    public static void setup() {
        // Try to get test data directory from system property or environment variable
        TEST_DATA_DIR_ = System.getProperty("html5lib.testdata.dir");
        if (TEST_DATA_DIR_ == null) {
            TEST_DATA_DIR_ = System.getenv("HTML5LIB_TESTDATA_DIR");
        }
        if (TEST_DATA_DIR_ == null) {
            // Default location relative to project root
            // testDataDir = "src/test/resources/html5lib-tests/tree-construction";
            TEST_DATA_DIR_ = "../html5lib-tests/tree-construction";
        }

        System.out.println("Html5lib test data directory: " + TEST_DATA_DIR_);
    }

    /**
     * Generates dynamic tests for all .dat files in the test directory.
     */
    @TestFactory
    public Collection<DynamicTest> html5libTreeConstructionTests() throws IOException {
        final List<DynamicTest> tests = new ArrayList<>();

        final Path testDir = Paths.get(TEST_DATA_DIR_);
        if (!Files.exists(testDir)) {
            System.out.println("WARNING: Test directory does not exist: " + testDir.toAbsolutePath());
            System.out.println("Please clone html5lib-tests and set html5lib.testdata.dir property");
            return tests;
        }

        // Find all .dat test files
        try (Stream<Path> paths = Files.walk(testDir)) {
            final List<Path> datFiles = paths
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".dat"))
                .sorted()
                .collect(Collectors.toList());

            System.out.println("Found " + datFiles.size() + " test files");

            for (final Path datFile : datFiles) {
                final String fileName = datFile.getFileName().toString();
                tests.addAll(createTestsForFile(datFile, fileName));
            }
        }

        return tests;
    }

    /**
     * Creates individual test cases for each test in a .dat file.
     */
    private List<DynamicTest> createTestsForFile(final Path filePath, final String fileName) {
        final List<DynamicTest> tests = new ArrayList<>();

        try {
            final String content = new String(Files.readAllBytes(filePath));
            final List<Html5LibTestParser.TestCase> testCases = Html5LibTestParser.parseTestFile(content);

            System.out.println(fileName + ": " + testCases.size() + " tests");

            for (int i = 0; i < testCases.size(); i++) {
                final Html5LibTestParser.TestCase testCase = testCases.get(i);
                final int testNumber = i + 1;

                // Create display name
                final String displayName = String.format("%s [%d]: %s",
                                                    fileName.replace(".dat", ""),
                                                    testNumber,
                                                    truncate(testCase.getData(), 50));

                final DynamicTest test = DynamicTest.dynamicTest(displayName, () -> {
                    runSingleTest(testCase, fileName, testNumber);
                });

                tests.add(test);
            }

        }
        catch (final IOException e) {
            // Create a failing test for this file
            tests.add(DynamicTest.dynamicTest(
                fileName + " - Failed to load",
                () -> fail("Could not read test file: " + e.getMessage())
            ));
        }

        return tests;
    }

    /**
     * Executes a single test case and asserts the results.
     */
    private void runSingleTest(final Html5LibTestParser.TestCase testCase,
                                final String fileName, final int testNumber) {
        // Run test with scripting enabled (or default)
        final Boolean scriptingEnabled = testCase.getScriptingEnabled();

        if (scriptingEnabled == null) {
            // Test should be run in both modes - for now just run with default
            runTestWithScripting(testCase, null, fileName, testNumber);
        }
        else {
            runTestWithScripting(testCase, scriptingEnabled, fileName, testNumber);
        }
    }

    private void runTestWithScripting(final Html5LibTestParser.TestCase testCase,
            final Boolean scriptingEnabled, final String fileName, final int testNumber) {
        final Html5LibTestRunner.TestResult result = Html5LibTestRunner.runTest(testCase);

        if (!result.isPassed()) {
            final StringBuilder msg = new StringBuilder();
            msg.append(String.format("%s [%d] FAILED: %s\n", fileName, testNumber, result.getMessage()));
            msg.append("\nInput HTML:\n");
            msg.append(testCase.getData());
            msg.append("\n\nExpected tree:\n");
            if (result.getExpectedOutput() != null) {
                for (final String line : result.getExpectedOutput()) {
                    msg.append(line).append("\n");
                }
            }
            msg.append("\nActual tree:\n");
            if (result.getActualOutput() != null) {
                for (final String line : result.getActualOutput()) {
                    msg.append(line).append("\n");
                }
            }

            fail(msg.toString());
        }
    }

    /**
     * Truncate string for display.
     */
    private String truncate(String str, final int maxLength) {
        if (str == null) {
            return "";
        }
        str = str.replace("\n", "\\n").replace("\r", "");
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}
