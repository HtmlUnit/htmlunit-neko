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

    /** must match HTMLTagBalancer.MAX_CONTEXT_SEARCH_DEPTH */
    private static final int CONTEXT_WINDOW = 100;

    /** must match HTMLTagBalancer.MAX_ELEMENT_DEPTH */
    private static final int MAX_DEPTH = 512;

    // ------------------------------------------------------------------
    // 0. maximum element depth
    // ------------------------------------------------------------------

    /**
     * Unclosed unknown elements nest forever. The depth of the generated tree is limited so no
     * recursive consumer of the tree can be forced into a stack overflow; the elements beyond the
     * limit are still reported, as empty elements.
     */
    @Test
    public void nestingDepthIsLimited() {
        final int elements = 100_000;

        final Stats stats = parseWithTimeout(repeat("<s>", elements));

        assertBalanced(stats);
        assertEquals(elements, stats.count("s"));
        // the elements beyond the limit are reported as empty elements: one more level
        assertEquals(MAX_DEPTH + 1, stats.maxDepth());
    }

    /** Same for known block elements and with content: nothing is lost. */
    @Test
    public void contentBeyondMaxDepthIsKept() {
        final Stats stats = parseWithTimeout(repeat("<div>", MAX_DEPTH + 100) + "x");

        assertBalanced(stats);
        assertEquals(MAX_DEPTH, stats.lastCharacterDepth());
    }

    /** Documents up to the limit are not touched. */
    @Test
    public void nestingBelowTheLimitIsUnchanged() {
        // html and body take two levels
        final Stats stats = parseWithTimeout(repeat("<div>", MAX_DEPTH - 2) + "x");

        assertBalanced(stats);
        assertEquals(MAX_DEPTH - 2 + 2, stats.maxDepth());
        assertEquals(MAX_DEPTH, stats.lastCharacterDepth());
        assertEquals(MAX_DEPTH - 2, stats.count("div"));
    }

    /** The end tags of the elements beyond the limit are unmatched end tags and harmless. */
    @Test
    public void endTagsBeyondMaxDepthAreHarmless() {
        final int depth = MAX_DEPTH + 100;

        final Stats stats = parseWithTimeout(repeat("<div>", depth) + "x" + repeat("</div>", depth) + "y");

        assertBalanced(stats);
        assertEquals(depth, stats.count("div"));
        // the first end tags close the elements that are open, the text after them is at body level
        assertEquals(2, stats.lastCharacterDepth());
    }

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
        assertTrue(stats.maxDepth() <= MAX_DEPTH + 1, "depth " + stats.maxDepth());
    }

    /** Same attack with rows: every {@code <tr>} searches its {@code <tbody>}. */
    @Test
    public void deepStackBeforeManyRows() {
        final int depth = 100_000;
        final int rows = 50_000;

        final String html = "<table><tbody>" + repeat("<div>", depth) + repeat("<tr>x", rows);

        final Stats stats = parseWithTimeout(html);

        assertBalanced(stats);
        assertTrue(stats.maxDepth() <= MAX_DEPTH + 1, "depth " + stats.maxDepth());
    }

    /** Same attack with header cells. */
    @Test
    public void deepStackBeforeManyHeaderCells() {
        final int depth = 100_000;
        final int cells = 50_000;

        final String html = "<table><tr>" + repeat("<div>", depth) + repeat("<th>x", cells);

        final Stats stats = parseWithTimeout(html);

        assertBalanced(stats);
        assertTrue(stats.maxDepth() <= MAX_DEPTH + 1, "depth " + stats.maxDepth());
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
    @ValueSource(ints = {101, 200, 400})
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
    @ValueSource(ints = {CLOSE_WINDOW, CLOSE_WINDOW + 1, 150, 400})
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
    // 1c. table/form start context, end tag search, discarded elements
    // ------------------------------------------------------------------

    /**
     * Every {@code <table>} start tag has to find out whether it is inside table
     * structure. Under N open divs and without any table element open, the answer
     * is known from the open counters; an unbounded stack walk would cost N per tag.
     */
    @Test
    public void manyTablesBelowDeepStack() {
        final int depth = 100_000;
        final int tables = 50_000;

        final Stats stats = parseWithTimeout(repeat("<div>", depth) + repeat("<table></table>", tables));

        assertBalanced(stats);
        assertEquals(tables, stats.count("table"));
    }

    /** Same for {@code <form>}, which uses the same context check. */
    @Test
    public void manyFormsBelowDeepStack() {
        final int depth = 100_000;
        final int forms = 50_000;

        final Stats stats = parseWithTimeout(repeat("<div>", depth) + repeat("<form></form>", forms));

        assertBalanced(stats);
        assertEquals(forms, stats.count("form"));
    }

    /**
     * A table structure element is open at the bottom, so the counters cannot answer; only the
     * search window protects here. The cell at the bottom is out of reach, nested tables stay nested.
     */
    @Test
    public void manyTablesBelowDeepStackInsideCell() {
        final int depth = 100_000;
        final int tables = 50_000;

        final Stats stats = parseWithTimeout("<table><tr><td>" + repeat("<div>", depth)
                + repeat("<table></table>", tables));

        assertBalanced(stats);
        assertEquals(tables + 1, stats.count("table"));
    }

    /**
     * Unknown elements share one element code, so "is an unknown element open" cannot rule out
     * the search. An end tag without a matching start tag must not walk the whole stack.
     */
    @Test
    public void manyUnknownEndTagsBelowDeepStack() {
        final int depth = 100_000;
        final int endTags = 50_000;

        final Stats stats = parseWithTimeout("<x-a>" + repeat("<div>", depth) + repeat("</x-b>", endTags));

        assertBalanced(stats);
        assertEquals(1, stats.count("x-a"));
    }

    /**
     * {@code <li>} stops its search at the {@code <ul>} it finds first, so the end tags fail without
     * removing anything from the stack.
     */
    @Test
    public void manyEndTagsBehindParentBarrier() {
        final int depth = 100_000;
        final int endTags = 50_000;

        final Stats stats = parseWithTimeout("<ul><li><ul>" + repeat("<div>", depth) + repeat("</li>", endTags));

        assertBalanced(stats);
        assertEquals(depth, stats.count("div"));
    }

    /**
     * Discarded {@code <form>} start tags used to be remembered in a list that every
     * {@code </body>} end tag scanned.
     */
    @Test
    public void manyBodyEndTagsAfterDiscardedForms() {
        final int forms = 100_000;
        final int endTags = 50_000;

        final Stats stats = parseWithTimeout(repeat("<form>", forms) + repeat("</body>", endTags));

        assertBalanced(stats);
        assertEquals(1, stats.count("form"));
    }

    /** Discarded duplicate html/body start tags swallow exactly one end tag each. */
    @Test
    public void manyDiscardedHtmlAndBodyStartTags() {
        final int count = 100_000;

        final Stats stats = parseWithTimeout(repeat("<html>", count) + repeat("<body>", count) + "x"
                + repeat("</body>", count) + repeat("</html>", count));

        assertBalanced(stats);
        assertEquals(1, stats.count("html"));
        assertEquals(1, stats.count("body"));
        assertEquals(2, stats.lastCharacterDepth());
    }

    /** Same, with the end tags in the middle of the content and in mixed case. */
    @Test
    public void discardedHtmlAndBodyStartTagsSwallowTheirEndTags() {
        final Stats stats = parseWithTimeout("<html><html><body><body>x</body></body></html></html>y");

        assertBalanced(stats);
        assertEquals(1, stats.count("html"));
        assertEquals(1, stats.count("body"));
        assertEquals(2, stats.lastCharacterDepth());
    }

    /**
     * Within the window an end tag closes the unclosed elements above its start tag, and the text
     * after it lands behind the closed element (html/body = depth 2).
     */
    @ParameterizedTest
    @ValueSource(ints = {1, 50, CONTEXT_WINDOW - 1})
    public void endTagClosesUnclosedElementsWithinWindow(final int unclosed) {
        final Stats stats = parseWithTimeout("<div>" + repeat("<span>", unclosed) + "x</div>y");

        assertBalanced(stats);
        assertEquals(2, stats.lastCharacterDepth(), "unclosed " + unclosed);
    }

    /**
     * Beyond the window the end tag is treated like one without start tag and ignored: the text after
     * it stays inside all open elements (html, body, div and the spans).
     */
    @ParameterizedTest
    @ValueSource(ints = {CONTEXT_WINDOW, CONTEXT_WINDOW + 1, 150, 400})
    public void endTagIsIgnoredBeyondWindow(final int unclosed) {
        final Stats stats = parseWithTimeout("<div>" + repeat("<span>", unclosed) + "x</div>y");

        assertBalanced(stats);
        assertEquals(unclosed + 3, stats.lastCharacterDepth(), "unclosed " + unclosed);
    }

    /**
     * html and body are unique and sit at the bottom of the stack, they are exempt from the window:
     * with the same unclosed elements the result must not depend on the nesting depth.
     */
    @Test
    public void bodyAndHtmlEndTagsAreNotWindowed() {
        final Stats shallow = parseWithTimeout("<body>" + repeat("<span>", 5) + "x</body>y");
        final Stats deep = parseWithTimeout("<body>" + repeat("<span>", 500) + "x</body>y");

        assertBalanced(shallow);
        assertBalanced(deep);
        // the elements are all closed at the end of the document, never left behind
        assertEquals(shallow.count("body"), deep.count("body"));
        assertEquals(shallow.lastCharacterDepth() - 5, deep.lastCharacterDepth() - 500);
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
     * one (the stack is limited to MAX_DEPTH entries, the anchors beyond are reported as
     * empty elements). Must stay linear.
     * This is not about the adoption window but about the stack itself: it used
     * to be quadratic because the element stack (HTMLTagBalancer.InfoStack*) and
     * the NamespaceBinder context arrays grew by a fixed +10 entries, copying the
     * whole array on every resize (~48 GB of allocation for this input).
     */
    @Test
    public void manyDeepAnchors() {
        final int anchors = 2_000;
        final String html = repeat("<a href=\"x\">" + repeat("<div>", 150), anchors);

        final Stats stats = parseWithTimeout(html);

        assertBalanced(stats);
        assertTrue(stats.count("a") >= anchors, "a count: " + stats.count("a"));
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
        private int lastCharacterDepth;

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

        @Override
        public void characters(final char[] ch, final int start, final int length) {
            lastCharacterDepth = depth;
        }

        int count(final String name) {
            return startCounts_.getOrDefault(name, 0);
        }

        int maxDepth() {
            return maxDepth;
        }

        int lastCharacterDepth() {
            return lastCharacterDepth;
        }
    }
}