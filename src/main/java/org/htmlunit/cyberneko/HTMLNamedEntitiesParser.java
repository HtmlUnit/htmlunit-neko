/*
 * Copyright (c) 2002-2009 Andy Clark, Marc Guillemot
 * Copyright (c) 2017-2024 Ronald Brill
 * Copyright 2023 René Schwietzke
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import org.htmlunit.cyberneko.util.FastHashMap;

/**
 * This is a very specialized class for recognizing HTML named entities with the ability
 * to look them up in stages. It is stateless and hence memory friendly.
 * Additionally, it is not generated code rather it sets itself up from a file at
 * first use and stays fixed from now on. Technically, it is not a parser anymore,
 * because it does not have a state that matches the HTML standard:
 * <a href="https://html.spec.whatwg.org/multipage/parsing.html#character-reference-state">
 * 12.2.5.72 Character reference state</a>
 *
 * <p>Because it is stateless, it delegates the state handling to the user in the
 * sense of how many characters one saw and when to stop doing things.
 *
 * @author René Schwietzke
 * @author Ronald Brill
 */
public final class HTMLNamedEntitiesParser {
    // These are some benchmark results of a comparison old vs. new parser. "onlyCommon" is a test with just 7 out of
    // 2231 entities (most common such as lt gt and more). Random means, we are not feeding the parser the data
    // the test data in the same order all the time, but vary it.
    //
    // As you can see, the new parser is up to 20x faster for common entities and 8x faster when checking all.
    //
    // T14s Gen 1 AMD, 32 GB memory, newParser4 is this implementation here
    //
    // Benchmark                               (onlyCommon)  (random)  Mode  Cnt        Score        Error  Units
    // HtmlEntitiesParserBenchmark.newParser4          true      true  avgt    3      135.647 ±     13.500  ns/op
    // HtmlEntitiesParserBenchmark.newParser4          true     false  avgt    3      132.972 ±      4.807  ns/op
    // HtmlEntitiesParserBenchmark.newParser4         false      true  avgt    3   240162.769 ±   3538.438  ns/op
    // HtmlEntitiesParserBenchmark.newParser4         false     false  avgt    3   206904.535 ±  53584.038  ns/op
    // HtmlEntitiesParserBenchmark.oldParser           true      true  avgt    3     3320.223 ±    178.501  ns/op
    // HtmlEntitiesParserBenchmark.oldParser           true     false  avgt    3     3097.086 ±     48.238  ns/op
    // HtmlEntitiesParserBenchmark.oldParser          false      true  avgt    3  1584678.257 ±  65965.438  ns/op
    // HtmlEntitiesParserBenchmark.oldParser          false     false  avgt    3  1604853.180 ±  73638.435  ns/op

    /*
     * Our single instance of the parser, we don't have state, so we are safe
     */
    public static final HTMLNamedEntitiesParser INSTANCE = new HTMLNamedEntitiesParser();

    /*
     * Our starting point of the pseudo tree of entities. The root level is a little special, because of the size,
     * it employs a different lookup on the characters (calculation rather comparison).
     */
    private RootState rootLevel_ = new RootState();

    /*
     * Support back mapping from char to entity.
     */
    private FastHashMap<String, String> entities_ = new FastHashMap<>();

    /**
     * Constructor. It builds the parser state from an entity defining properties file. This file has been taken
     * from https://html.spec.whatwg.org/multipage/named-characters.html (JSON version) and converted
     * appropriately.
     */
    private HTMLNamedEntitiesParser() {
        // read the entities defined in the data taken from
        try (InputStream stream = HTMLNamedEntitiesParser.class.getResourceAsStream("html_entities.properties")) {
            final Properties props = new Properties();
            props.load(stream);

            props.forEach((k, v) -> {
                String key = (String) k;
                final String value = (String) v;

                // we might have an empty line in it
                if (key.trim().isEmpty()) {
                    return;
                }

                rootLevel_.add(key, value);

                key = "&" + key;
                final String ref = entities_.get(value);
                if (ref == null
                        || ref.length() < key.length()
                        || (ref.length() == key.length() && ref.compareTo(key) < 1)) {
                    entities_.put(value, key);
                }
            });

            // make the root more efficient, rest stays simple
            rootLevel_.optimize();
        }
        catch (final IOException e) {
            // we are doomed and hence can break the entire setup due to some incorrect classpath
            // or build
            throw new RuntimeException("Unable to initilaize the HTML entities from file");
        }
    }

    /**
     * Utility method, mostly for testing, that allows us to look up and entity from a string
     * instead from single characters.
     *
     * @param entityName the entity to look up
     * @return a state that resembles the result, will never be null
     */
    public State lookup(final String entityName) {
        State lastResult = rootLevel_;
        State lastMatchingResult = null;

        for (int i = 0; i < entityName.length(); i++) {
            final State result = lastResult.lookup(entityName.charAt(i));

            if (result.endNode_) {
                // we found the last matching possible entity in the pseudo tree
                // we can finish here, there is nothing beyond that point
                return result;
            }
            if (result == lastResult) {
                // nothing changed, more characters have not done anything
                // in case we have see something that was a match before, return
                // to that state
                return lastMatchingResult == null ? lastResult : lastMatchingResult;
            }
            if (result.isMatch_) {
                // in case this is a match but not an endnode, we keep that state
                // for later, in case any further chars take us into the wrong direction
                // standard dictates to stop when we don't have a match and return
                // to the last known match, if any
                lastMatchingResult = result;
            }
            lastResult = result;
        }

        return lastMatchingResult == null ? lastResult : lastMatchingResult;
    }

    /**
     * Pseudo parses and entity character by character. We assume that we get
     * presented with the chars after the starting ampersand. This parser does
     * not supported unicode entities, hence this has to be handled differently.
     *
     * @param character the next character, should not be the ampersand ever
     * @param state the last known state or null in case we start to parse
     *
     * @return the current state, which might be a valid final result, see {@link State}
     */
    public State lookup(final int character, final State state) {
        return state != null ? state.lookup(character) : rootLevel_.lookup(character);
    }

    /**
     * @return the entity ref for the given key (usually a single char) or null
     */
    public String lookupEntityRefFor(final String key) {
        return entities_.get(key);
    }

    /**
     * Our "level" in the treeish structure that keeps its static state and the next level
     * underneath.
     */
    public static class State {
        // what is the current depth aka amount of characters seen
        private final int depth_;

        // The characters at this level
        // The state at the same position holds the matching result
        int[] characters_ = new int[0];

        // The matching states at this level
        // we intentionally have not build a unified data structure
        // between characters and state, keep it simple!
        State[] nextState_ = new State[0];

        // our current fragment or full entity, so for the entity "copy;"
        // you will have c, co, cop, copy, and copy; on each state level
        public final String entityOrFragment_;

        // what shall we resolve to? if we don't resolve, this is null!!!
        public String resolvedValue_;

        // the length of the entity fragment
        public final int length_;

        // tell us, if this is ending with a semicolon
        public final boolean endsWithSemicolon_;

        // does this entity fragment match a resolved value?
        public boolean isMatch_;

        // is this the end of the look up level structure, this the end
        // and hence it shall be a match
        public boolean endNode_;

        /**
         * Create the empty state
         */
        protected State() {
            entityOrFragment_ = "";
            length_ = 0;
            depth_ = 0;
            endsWithSemicolon_ = false;
            isMatch_ = false;
            resolvedValue_ = null;
            endNode_ = false;
        }

        /**
         * Create us a new state that describes itself nicely
         */
        protected State(final int depth, final String entityFragment, final String resolvedValue) {
            if (depth == entityFragment.length()) {
                // we are at the end
                entityOrFragment_ = entityFragment;
                length_ = entityFragment.length();
                depth_ = entityFragment.length();
                endsWithSemicolon_ = entityFragment.endsWith(";");
                isMatch_ = true;
                resolvedValue_ = resolvedValue;
                endNode_ = entityFragment.endsWith(";");
            }
            else {
                // intermediate state
                final String currentFragment = entityFragment.substring(0, depth);

                entityOrFragment_ = currentFragment;
                length_ = currentFragment.length();
                depth_ = depth;
                endsWithSemicolon_ = false;
                isMatch_ = false;
                resolvedValue_ = null;
                endNode_ = false;
            }
        }

        /**
         * We have a special in between state because some entities exist as correct
         * entity with a semicolon at the end and as legacy version without. We want
         * to look up both correctly, hence when we build the data set, we have to
         * unmark an existing one as final one and insert one more.
         *
         * @param entity the entity to look up
         * @param resolvedValue the value it will resolve to
         */
        protected void updateNonSemicolonEntity(final String entity, final String resolvedValue) {
            if (entity.endsWith(";")) {
                // nothing to do, perfect entity
                return;
            }

            // our entity is legacy (no ;) and so we have to see if we know the ; version already
            if (entity.length() == depth_) {
                // safety check, just for the initial programming and later updates then
                // for daily life
                if (!entity.equals(entityOrFragment_)) {
                    throw new RuntimeException("Illegal state reached");
                }

                // declare this an intermediate match
                endNode_ = false;
                isMatch_ = true;
                resolvedValue_ = resolvedValue;
            }
        }

        /**
         * Add a new entity to the pseudo-tree
         *
         * @param entity the entity to look for later
         * @param resolvedValue the value it resolves to
         */
        protected void add(final String entity, final String resolvedValue) {
            // ok, any characters left?
            if (depth_ >= entity.length()) {
                // no reason to go any further
                return;
            }

            // get me my character
            final char c = entity.charAt(depth_);

            // do I already know it?
            final int pos = Arrays.binarySearch(characters_, c);

            if (pos < 0) {
                // we don't know it, make the size bigger and get us the new pos
                nextState_ = Arrays.copyOf(nextState_, nextState_.length + 1);
                characters_ = Arrays.copyOf(characters_, characters_.length + 1);
                final int newPos = -(pos + 1);

                // move stuff first
                if (newPos != characters_.length - 1) {
                    System.arraycopy(characters_, newPos, characters_, newPos + 1, characters_.length - newPos - 1);
                    System.arraycopy(nextState_, newPos, nextState_, newPos + 1, nextState_.length - newPos - 1);
                }
                else {
                    // we insert at the end, so no move needed
                }
                final State newLevel = new State(depth_ + 1, entity, resolvedValue);
                characters_[newPos] = c;
                nextState_[newPos] = newLevel;

                // update next level
                newLevel.add(entity, resolvedValue);
            }
            else {
                // ok, if this one is without a ; and we have the full entity, we
                // have a mismatch between one with and one without ;
                // change the level
                nextState_[pos].updateNonSemicolonEntity(entity, resolvedValue);
                nextState_[pos].add(entity, resolvedValue);
            }
        }

        /**
         * Lookup the state by iterating over the chars at this state, should not be that
         * many and due to the small size of the array, should be cache only
         *
         * @param character the char to look up
         * @return the next state or the same in case the character was not found
         */
        protected State lookup(final int character) {
            // because we have sorted arrays, we can be more efficient here
            final int length = characters_.length;

            for (int i = 0; i < length; i++) {
                final int c = characters_[i];

                // are we still under, simply continue
                if (c < character) {
                    continue;
                }

                if (c == character) {
                    // we are at position
                    return nextState_[i];
                }
                // ok, too far and have not found it, abort with current state
                return this;
            }

            // nothing found, maybe array was empty
            return this;
        }
    }

    /**
     * This is our initial state and has a special optimization applied. We
     * don't iterate, we jump by character code to the position.
     */
    protected static class RootState extends State {
        // the smallest character determines this
        private int offset_ = 0;

        @Override
        public State lookup(final int character) {
            // fastpath, just calculate the pos
            final int pos = character - offset_;

            // in case we don't have a matching char, return
            // this state, if we end up in a hole with null,
            // we do the same
            if (pos >= 0 && pos < nextState_.length) {
                final State s = nextState_[pos];
                return s != null ? s : this;
            }
            return this;
        }

        /*
         * Optimizes the layout after creation. This is only applied to the root state
         * because it is a wider range of characters. It does not make sense for the substates,
         * because we would get arrays with large holes and that makes the cache go bust.
         */
        protected void optimize() {
            // are we final already?
            if (offset_ > 0) {
                // that is just for later to tell us that we don't understand our
                // own code anymore and called that incorrectly
                throw new RuntimeException("Optimiize was called twice");
            }

            // ok, smallest char is the start
            offset_ = characters_[0];

            // get us new a level array covering the smallest char in [0] and the largest in the last pos,
            // we might have holes, but not too many, hence this is faster than iterating or a binary search
            final State[] newNextLevel = new State[characters_[characters_.length - 1] - offset_ + 1];

            // arrange entries according to charactercode
            for (int i = 0; i < characters_.length; i++) {
                final int c = characters_[i];
                final State level = nextState_[i];

                newNextLevel[c - offset_] = level;
            }

            // take it live
            nextState_ = newNextLevel;

            // free memory, because we not longer need that, doesn't save a ton
            // but it might also help to discover programming mistakes
            characters_ = null;
        }
    }
}
