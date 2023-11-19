/*
 * Copyright 2002-2009 Andy Clark, Marc Guillemot
 * Copyright 2017-2023 Ronald Brill
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
package org.htmlunit.cyberneko.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

/**
 * This is a very specialized class for storing HTML entities with the ability
 * to look them up in stages. It is stateless and hence it use is memory friendly.
 * Additionally, it is not generated code rather it sets itself up from a file at
 * first use and stays fixed from now on.
 *
 * Because it is stateless, it delegates the state handling to the user in the
 * sense of how many characters one saw and when to stop doing things.
 *
 *
 *
 * @author René Schwietzke
 */
public class HTMLEntitiesParser
{
    // These are some benchmark results of a comparison old vs. new parser. onlyCommon is a test with just 7 out of
    // 2231 entities (most common such as lt gt and more). Random means, we are not feeding the parser the data
    // the test data in the same order, but vary them.
    //
    // As you can see, the new parser is up to 20x faster for common entities and 8x faster when checking all.
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
     * Our single instance of the parser
     */
    private final static HTMLEntitiesParser instance = new HTMLEntitiesParser();

    /*
     * Our starting point of the pseudo tree of entities. The root level is a little special, because of the size
     * it employs a different lookup on the characters (calculation rather comparison).
     */
    private RootLevel rootLevel = new RootLevel();

    /**
     * Constructor
     */
    private HTMLEntitiesParser()
    {
        // read the entities defined in the data taken from
        try (InputStream stream = HTMLEntitiesParser.class.getResourceAsStream("html_entities.properties"))
        {
            final Properties props = new Properties();
            props.load(stream);

            props.forEach((k, v) -> {
                String key = (String) k;
                String value = (String) v;

                // we might have an empty line in it
                if (key.isEmpty()) {
                    return;
                }

                this.rootLevel.add(key, value);
            });

            // make the root more efficient, rest stays simple
            this.rootLevel.optimize();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to initilaize the HTML entities from file");
        }
    }

    /**
     * Returns the singleton. The singleton is stateless and can safely be used in a multi-threaded
     * context. The
     */
    public static HTMLEntitiesParser get()
    {
        return instance;
    }

    public Level lookup(final String entityName)
    {
        Level lastResult = this.rootLevel;
        Level lastMatchingResult = null;

        for (int i = 0; i < entityName.length(); i++)
        {
            Level result = lastResult.lookup(entityName.charAt(i));

            if (result.endNode)
            {
                return result;
            }
            if (result == lastResult)
            {
                // nothing changed, more characters have not done anything
                return lastMatchingResult == null ? lastResult : lastMatchingResult;
            }
            if (result.isMatch)
            {
                lastMatchingResult = result;
            }
            lastResult = result;
        }

        return lastMatchingResult == null ? lastResult : lastMatchingResult;
    }

    public Level lookup(final int character, final Level level)
    {
        return level != null ? level.lookup(character) : rootLevel.lookup(character);
    }

    public static class RootLevel extends Level
    {
        private int offset = 0;

        @Override
        public Level lookup(int character)
        {
            // fastpath, just calculate the pos
            final int pos = character - offset;
            if (pos >=0 && pos < this.nextLevel.length)
            {
                return this.nextLevel[pos];
            }
            else
            {
                return this;
            }
        }

        /*
         * Optimizes the layout after creation but not for every level
         */
        protected void optimize()
        {
            // are we final already?
            if (offset > 0)
            {
                return;
            }

            // ok, smallest char is the start
            this.offset = this.characters[0];

            // get us new level array covering the smallest char in [0] and the largest in the last pos,
            // we might have holes, but not too many, hence this is faster than iterating or a binary search
            final Level[] newNextLevel = new Level[this.characters[this.characters.length - 1] - offset + 1];

            // arrange entry according to charactercode
            for (int i = 0; i < this.characters.length; i++)
            {
                final int c = this.characters[i];
                final Level level = this.nextLevel[i];

                newNextLevel[c - offset] = level;
            }

            // take it live
            this.nextLevel = newNextLevel;
            // free memory, because we not longer need that
            this.characters = null;
        }

    }

    public static class Level
    {
        private final int depth;
        int[] characters = new int[0];
        Level[] nextLevel = new Level[0];

        public final String entityOrFragment;
        public String resolvedValue;
        public final int length;
        public final boolean endsWithSemicolon;
        public boolean isMatch;
        public boolean endNode;

        public Level()
        {
            this.entityOrFragment = "";
            this.length = 0;
            this.depth = 0;
            this.endsWithSemicolon = false;
            this.isMatch = false;
            this.resolvedValue = null;
            this.endNode = false;
        }

        public Level(final int depth, final String entityFragment, final String resolvedValue)
        {
            if (depth == entityFragment.length())
            {
                // we are at the end
                this.entityOrFragment = entityFragment;
                this.length = entityFragment.length();
                this.depth = entityFragment.length();
                this.endsWithSemicolon = entityFragment.endsWith(";");
                this.isMatch = true;
                this.resolvedValue = resolvedValue;
                this.endNode = entityFragment.endsWith(";");
            }
            else
            {
                // intermediate state
                final String currentFragment = entityFragment.substring(0, depth);

                this.entityOrFragment = currentFragment;
                this.length = currentFragment.length();
                this.depth = depth;
                this.endsWithSemicolon = false;
                this.isMatch = false;
                this.resolvedValue = null;
                this.endNode = false;
            }
        }


        public void updateNonSemicolonEntity(final String entity, final String resolvedValue)
        {
            if (entity.endsWith(";"))
            {
                // nothing to do
                return;
            }
            if (entity.length() == this.depth)
            {
                // safety check
                if (!entity.equals(this.entityOrFragment))
                {
                    throw new RuntimeException("Illegal state reached");
                }

                this.isMatch = true;
                this.resolvedValue = resolvedValue;
            }
        }

        public void add(final String entity, final String resolvedValue)
        {
            // ok, any characters left?
            if (this.depth >= entity.length())
            {
                // no reason to go any further
                return;
            }

            // get me my character
            final char c = entity.charAt(this.depth);

            // do I already know it?
            final int pos = Arrays.binarySearch(characters, c);

            if (pos < 0)
            {
                // we don't know it, make the size bigger and get us the new pos
                this.nextLevel = Arrays.copyOf(this.nextLevel, this.nextLevel.length + 1);
                this.characters = Arrays.copyOf(this.characters, this.characters.length + 1);
                final int newPos = -(pos + 1);

                // move stuff first
                if (newPos != this.characters.length - 1)
                {
                    System.arraycopy(this.characters, newPos, this.characters, newPos + 1, this.characters.length - newPos - 1);
                    System.arraycopy(this.nextLevel, newPos, this.nextLevel, newPos + 1, this.nextLevel.length - newPos - 1);
                }
                else
                {
                    // we insert at the end, so no move needed
                }
                final Level newLevel = new Level(this.depth + 1, entity, resolvedValue);
                this.characters[newPos] = c;
                this.nextLevel[newPos] = newLevel;

                // update next level
                newLevel.add(entity, resolvedValue);
            }
            else
            {
                // ok, if this one is without a ; and we have the full entity, we
                // have a mismatch between one with and one without ;
                // change the level
                this.nextLevel[pos].updateNonSemicolonEntity(entity, resolvedValue);
                this.nextLevel[pos].add(entity, resolvedValue);
            }
        }

        public Level lookup(int character)
        {
            // because we have sorted arrays, we can be more efficient here
            final int length = this.characters.length;
            for (int i = 0; i < length; i++)
            {
                final int c = this.characters[i];
                if (c < character)
                {
                    continue;
                }
                if (c == character)
                {
                    // we are at position
                    return this.nextLevel[i];
                }
                else
                {
                    // ok, too far and have not found it, abort with current state
                    return this;
                }
            }

            return this;
        }
    }

}
