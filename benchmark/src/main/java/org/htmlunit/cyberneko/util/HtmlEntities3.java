package org.htmlunit.cyberneko.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

import javax.management.RuntimeErrorException;

import org.htmlunit.cyberneko.HtmlEntitiesParserBenchmark;

/**
 * This is a very specialized tree class for storing HTML entities
 * with the ability to look them up in stages. It is driven by an
 * char (presented as int) and results in finding a String result at
 * the end. We return the last tree node as result, so we can keep
 * that as state for the next iterations. The tree itself does not
 * keep an active state when being used.
 */
public class HtmlEntities3
{
    private final static HtmlEntities3 instance = new HtmlEntities3();

    private Level rootLevel = new Level();

    /**
     * Constructor
     */
    private HtmlEntities3()
    {
        // read the entities defined in the data taken from
        try (InputStream stream = HtmlEntitiesParserBenchmark.class.getResourceAsStream("html_entities.properties"))
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
    public static HtmlEntities3 get()
    {
        return instance;
    }

    public Optional<Level> lookup(final String entityName)
    {
        Level lastResult = this.rootLevel;
        for (int i = 0; i < entityName.length(); i++)
        {
            Level result = lastResult.lookup(entityName.charAt(i));

            if (result.endNode)
            {
                lastResult = result;
                break;
            }
            if (result == lastResult)
            {
                // nothing changed, more characters have not done anything
                break;
            }
            lastResult = result;
        }

        return Optional.of(lastResult);
    }

    public Level lookup(final int character, final Level level)
    {
        return level != null ? level.lookup(character) : rootLevel.lookup(character);
    }

    public static class Level
    {
        private final int depth;
        private int[] characters = new int[0];
        private Level[] nextLevel = new Level[0];

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
                    // we are over position
                    return this.nextLevel[i];
                }
                else
                {
                    return this;
                }
            }

            return this;
        }
    }

}
