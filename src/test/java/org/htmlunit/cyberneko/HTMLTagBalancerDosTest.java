/*
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
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.htmlunit.cyberneko.parsers.SAXParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parser level DoS tests. All inputs are generated; nothing recurses over the
 * resulting tree (a SAX handler is used instead of a DOM) so that very deep
 * documents cannot blow the test's own stack.
 *
 * @author Ronald Brill
 */
public class HTMLTagBalancerDosTest {

    /**
     * Generous upper bound. The attack inputs take many seconds (or minutes)
     * on the unprotected code and well below a second on the protected one.
     * Increase the sizes below if your machine is fast enough to hide the
     * regression.
     */
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    /** must match HTMLTagBalancer.MAX_ADOPTION_DEPTH */
    private static final int ADOPTION_WINDOW = 100;

    /** must match HTMLTagBalancer.MAX_CLOSE_SEARCH_DEPTH */
    private static final int CLOSE_WINDOW = 100;

    // ------------------------------------------------------------------
    // 1. hasOpenParent(): quadratic scan for table row/cell elements
    // ------------------------------------------------------------------

    /**
     * {@code <table><tr>} + N open divs + M x {@code <td>}: every {@code <td>}
     * has to look for its {@code <tr>} below N divs. Unbounded this is N*M
     * stack entries (5 * 10^9 here).
     */
    @Test
    public void deepStackBeforeManyCells() {
        final int depth = 100_000;
        final int cells = 50_000;

        final String html = "<table><tr>" + repeat("<div>", depth) + repeat("<td>x", cells);

        final Stats stats = parseWithTimeout(html);

        assertBalanced(stats);
        assertEquals(cells, stats.count("td"));
    }

    /** Same attack with rows: every {@code <tr>} searches its {@code <tbody>}. */
    @Test
    public void deepStackBeforeManyRows() {
        final int depth = 100_000;
        final int rows = 50_000;

        final String html = "<table><tbody>" + repeat("<div>", depth) + repeat("<tr>x", rows);

        final Stats stats = parseWithTimeout(html);

        assertBalanced(stats);
        // synthesized parents may add more, but none of the rows may get lost
        assertTrue(stats.count("tr") >= rows, "tr count: " + stats.count("tr"));
    }

    /** Same attack with header cells. */
    @Test
    public void deepStackBeforeManyHeaderCells() {
        final int depth = 100_000;
        final int cells = 50_000;

        final String html = "<table><tr>" + repeat("<div>", depth) + repeat("<th>x", cells);

        final Stats stats = parseWithTimeout(html);

        assertBalanced(stats);
        assertEquals(cells, stats.count("th"));
    }

    /**
     * Regression guard for the window: a {@code <tr>} that is within the
     * search window is found, so no second row is synthesized.
     */
    @Test
    public void cellParentWithinWindowIsFound() {
        final String html = "<table><tr>" + repeat("<div>", 50) + "<td>x</td></table>";

        final Stats stats = parseWithTimeout(html);

        assertBalanced(stats);
        assertEquals(1, stats.count("tr"));
        assertEquals(1, stats.count("td"));
    }

    /**
     * Beyond the window the parent is treated as not found. The result is
     * not required to match browsers, but the parser has to terminate with a
     * balanced document and must not lose the cell.
     */
    @ParameterizedTest
    @ValueSource(ints = {101, 200, 5_000})
    public void cellParentBeyondWindowDegradesGracefully(final int depth) {
        final String html = "<table><tr>" + repeat("<div>", depth) + "<td>x</td></table>";

        final Stats stats = parseWithTimeout(html);

        assertBalanced(stats);
        assertEquals(1, stats.count("td"));
        assertTrue(stats.count("tr") >= 1);
    }

    /**
     * A nested table is a bounds element: the inner cell must never see the
     * outer row, however many elements are in between.
     */
    @Test
    public void boundsElementStillStopsTheSearch() {
        final String html = "<table><tr><td>"
                + "<table>" + repeat("<div>", 50) + "<td>x</td></table>"
                + "</td></tr></table>";

        final Stats stats = parseWithTimeout(html);

        assertBalanced(stats);
        // the inner <td> did not attach to the outer <tr>: it got its own row
        assertTrue(stats.count("tr") >= 2, "tr count: " + stats.count("tr"));
    }

    // ------------------------------------------------------------------
    // 1b. "close previous elements" loop (implicit end tags)
    // ------------------------------------------------------------------

    /**
     * Within the window the second {@code <li>} closes the first one and
     * becomes its sibling: the deepest nesting is html/body/ul/li + the divs.
     */
    @ParameterizedTest
    @ValueSource(ints = {1, 50, CLOSE_WINDOW - 1})
    public void implicitCloseWithinWindow(final int depth) {
        final String html = "<ul><li>a" + repeat("<div>", depth) + "<li>b</ul>";

        final Stats stats = parseWithTimeout(html);

        assertBalanced(stats);
        assertEquals(4 + depth, stats.maxDepth(), "depth " + depth);
    }

    /**
     * Beyond the window the first {@code <li>} is out of reach: the second one
     * is nested (one level deeper than the divs) instead of closing it.
     */
    @ParameterizedTest
    @ValueSource(ints = {CLOSE_WINDOW, CLOSE_WINDOW + 1, 150, 5_000})
    public void implicitCloseBeyondWindow(final int depth) {
        final String html = "<ul><li>a" + repeat("<div>", depth) + "<li>b</ul>";

        final Stats stats = parseWithTimeout(html);

        assertBalanced(stats);
        assertEquals(4 + depth + 1, stats.maxDepth(), "depth " + depth);
    }

    /** Many implicit closes below a deep stack of non-block elements must stay linear. */
    @Test
    public void manyImplicitClosesBelowDeepStack() {
        final int depth = 100_000;
        final int items = 50_000;

        final String html = "<ul><li>a" + repeat("<span>", depth) + repeat("<li>x", items);

        final Stats stats = parseWithTimeout(html);

        assertBalanced(stats);
        assertTrue(stats.count("li") >= items, "li count: " + stats.count("li"));
    }

    // ------------------------------------------------------------------
    // 2. <a>/<nobr> un-nesting: depth window
    // ------------------------------------------------------------------

    /**
     * Within the window the second anchor un-nests the first one: original
     * {@code <a>}, the empty clone inside the re-opened div, and the new
     * {@code <a>} = 3 start events.
     */
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 50, ADOPTION_WINDOW - 1})
    public void anchorIsUnNestedWithinWindow(final int depth) {
        final String html = "<a href=\"1\">1" + repeat("<div>", depth) + "<a href=\"2\">2</a>";

        final Stats stats = parseWithTimeout(html);

        assertBalanced(stats);
        assertEquals(3, stats.count("a"), "depth " + depth);
    }

    /**
     * Beyond the window the old behavior is kept: plain nesting, no clone,
     * so only the two anchors from the input.
     */
    @ParameterizedTest
    @ValueSource(ints = {ADOPTION_WINDOW, ADOPTION_WINDOW + 1, 200, 5_000, 100_000})
    public void anchorIsNotUnNestedBeyondWindow(final int depth) {
        final String html = "<a href=\"1\">1" + repeat("<div>", depth) + "<a href=\"2\">2</a>";

        final Stats stats = parseWithTimeout(html);

        assertBalanced(stats);
        assertEquals(2, stats.count("a"), "depth " + depth);
    }

    /** Same window for {@code <nobr>}. */
    @Test
    public void nobrWindow() {
        Stats stats = parseWithTimeout("<nobr>1" + repeat("<div>", 50) + "<nobr>2</nobr>");
        assertBalanced(stats);
        assertEquals(3, stats.count("nobr"));

        stats = parseWithTimeout("<nobr>1" + repeat("<div>", 5_000) + "<nobr>2</nobr>");
        assertBalanced(stats);
        assertEquals(2, stats.count("nobr"));
    }

    /**
     * Many deep anchors: every anchor sits beyond the window of the previous
     * one. Must stay linear and must not synthesize anything.
     * (Sanity guard; this input is not quadratic even without the window,
     * the window mainly limits amplification caused by a single start tag.)
     */
    @Test
    public void manyDeepAnchors() {
        final int anchors = 2_000;
        final String html = repeat("<a href=\"x\">" + repeat("<div>", 150), anchors);

        final Stats stats = parseWithTimeout(html);

        assertBalanced(stats);
        assertEquals(anchors, stats.count("a"));
    }

    /**
     * Many shallow anchors (the legitimate misnesting case): each one un-nests
     * the previous one, work stays linear.
     */
    @Test
    public void manyShallowAnchors() {
        final int anchors = 50_000;
        final String html = repeat("<a href=\"x\">1<div>", anchors);

        final Stats stats = parseWithTimeout(html);

        assertBalanced(stats);
        assertTrue(stats.count("a") >= anchors, "a count: " + stats.count("a"));
    }

    /**
     * An anchor outside an {@code <object>} is not reachable from inside it
     * (marker), so the search must not walk past the marker.
     */
    @Test
    public void markerStopsTheSearch() {
        final Stats stats = parseWithTimeout("<a href=\"1\">1<object>" + repeat("<div>", 10)
                + "<a href=\"2\">2</a></object>");

        assertBalanced(stats);
        assertEquals(2, stats.count("a"));
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private static Stats parseWithTimeout(final String html) {
        return assertTimeoutPreemptively(TIMEOUT, () -> {
            final SAXParser parser = new SAXParser();
            final Stats stats = new Stats();
            parser.setContentHandler(stats);
            parser.parse(new InputSource(new StringReader(html)));
            return stats;
        });
    }

    private static void assertBalanced(final Stats stats) {
        assertEquals(stats.starts, stats.ends, "start/end events are not balanced");
        assertEquals(0, stats.depth, "document not fully closed");
    }

    private static String repeat(final String s, final int count) {
        final StringBuilder sb = new StringBuilder(s.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    /** Counts start elements per name, total start/end events and the maximum depth. */
    private static final class Stats extends DefaultHandler {
        private final Map<String, Integer> startCounts_ = new HashMap<>();
        private int starts;
        private int ends;
        private int depth;
        private int maxDepth;

        @Override
        public void startElement(final String uri, final String localName, final String qName,
                final Attributes attributes) {
            starts++;
            depth++;
            maxDepth = Math.max(maxDepth, depth);
            startCounts_.merge(qName.toLowerCase(Locale.ROOT), 1, Integer::sum);
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) {
            ends++;
            depth--;
        }

        int count(final String name) {
            return startCounts_.getOrDefault(name, 0);
        }

        int maxDepth() {
            return maxDepth;
        }
    }
}
