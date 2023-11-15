package org.htmlunit.cyberneko.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

import org.htmlunit.cyberneko.HtmlEntitiesParserBenchmark;

/**
 * This is a very specialized tree class for storing HTML entities
 * with the ability to look them up in stages. It is driven by an
 * char (presented as int) and results in finding a String result at
 * the end. We return the last tree node as result, so we can keep
 * that as state for the next iterations. The tree itself does not
 * keep an active state when being used.
 */
public class HtmlEntities1
{
    private final static HtmlEntities1 instance = new HtmlEntities1();

    private String[] entities = new String[0];
    private ValueNode[] values = new ValueNode[0];

    /**
     * Constructor
     */
    private HtmlEntities1()
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

                this.add(key, value);
            });

            // enumerate all
            this.enumerate();
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
    public static HtmlEntities1 get()
    {
        return instance;
    }

    /**
     * Returns the intermediate state as position in the data set
     * Returns >= 0 if still matching, -1 if no match or no match
     * anymore. You can use the last positive number to retrieve
     * the last valid result.
     *
     * @param
     * @param
     * @return
     */
    public ValueNode lookup(final String entityName, int previousStep)
    {
        if (entityName.length() <= 2)
        {
            int low = previousStep;
            int high = this.entities.length - 1;

            while (low <= high) {
                int mid = (low + high) >>> 1;
                String midVal = this.entities[mid];
                int cmp = midVal.compareTo(entityName);

                if (cmp < 0) {
                    low = mid + 1;
                }
                else if (cmp > 0) {
                    high = mid - 1;
                }
                else {
                    return this.values[mid]; // key found
                }
            }
        }
        else
        {
            for (int i = previousStep + 1; i < this.entities.length; i++)
            {
                final int r = this.entities[i].compareTo(entityName);
                if (r < 0)
                {
                    continue;
                }

                if (r == 0)
                {
                    return this.values[i];
                }
                else if (r > 0)
                {
                    return null;
                }
            }
        }

        return null;
    }

    /**
     * Allows to directly lookup an entity without going the incrementally.
     * We go the Java 8 way and avoid null here. With Valhalla, this will become
     * memory efficient, up to that point, we might use this for testing only
     * I guess.
     *
     * @param entityName
     * @return
     */
    public Optional<String> lookup(final String entityName)
    {
        final int pos = Arrays.binarySearch(this.entities, entityName);
        if (pos >= 0)
        {
            return Optional.ofNullable(this.values[pos].resolvedValue);
        }
        else
        {
            return Optional.empty();
        }
    }

    public ValueNode lookup(final int pos)
    {
        return this.values[pos];
    }

    private void add(ValueNode node)
    {
        // find position
        final int pos = Arrays.binarySearch(this.entities, node.entity);
        if (pos >= 0)
        {
            // when this is a non match but we need a mtch to store, overridde
            // this covers things like a valid "gt" and a later valid "gt;"
            if (node.isMatch() && !this.values[pos].isMatch())
            {
                this.values[pos] = node;
            }
            return;
        }

        // we assume clean data such as that we don't have things twice
        // increase data size
        this.entities = Arrays.copyOf(this.entities, this.entities.length + 1);
        this.values = Arrays.copyOf(this.values, this.values.length + 1);

        // move all to the right
        final int p = -(pos + 1);
        if (p < this.entities.length - 1)
        {
            System.arraycopy(this.entities, p, this.entities, p + 1, this.entities.length - p - 1);
            System.arraycopy(this.values, p, this.values, p + 1, this.values.length - p - 1);
        }

        this.entities[p] = node.entity;
        this.values[p] = node;;
    }

    private void add(final String entityName, final String resolvedValue)
    {
        for (int i = 1; i < entityName.length(); i++)
        {
            String s = entityName.substring(0, i);
            add(new ValueNode(s));
        }
        add(new ValueNode(entityName, resolvedValue));
    }

    private void enumerate()
    {
        for (int i = 0; i < this.values.length; i++)
        {
            this.values[i].position = i;
        }
    }

    public static class Resolver
    {
        private int consumedCount;
        private ValueNode state = ValueNode.EMPTY;
        private StringBuilder data = new StringBuilder(8);

        public void reset()
        {
            this.consumedCount = 0;
            this.state = ValueNode.EMPTY;
            this.data.delete(0, this.data.length());
        }

        public String getResolvedValue()
        {
            return state.resolvedValue;
        }

        public int getMatchLength()
        {
            return state.length;
        }

        public int getRewindCount()
        {
            return consumedCount - getMatchLength();
        }

        public boolean endsWithSemicolon()
        {
            return state.endsWithSemicolon;
        }

        public boolean parse(final int character)
        {
            this.consumedCount++;
            this.data.append((char) character);

            ValueNode newState = HtmlEntities1.get().lookup(data.toString(), state.position);

            if (newState == null)
            {
                return false;
            }
            else
            {
                this.state = newState;
                return true;
            }
        }
    }

    public static class ValueNode
    {
        public static ValueNode EMPTY = new ValueNode("");

        public int position;
        public String entity;
        public String resolvedValue;
        public int length;
        public boolean endsWithSemicolon;
        public boolean isMatch;

        public ValueNode(final String entity)
        {
            this.entity = entity;
            this.resolvedValue = null;
            this.length = entity.length();
            this.endsWithSemicolon = false;
            this.isMatch = false;
        }

        public ValueNode(final String entity, final String resolvedValue)
        {
            this.entity = entity;
            this.resolvedValue = resolvedValue;
            this.length = entity.length();
            this.endsWithSemicolon = entity.endsWith(";");
            this.isMatch = true;
        }

        public boolean isMatch()
        {
            return this.isMatch;
        }

        public String matched()
        {
            return this.entity;
        }

        public String resolvedTo()
        {
            return resolvedValue;
        }
    }
}
