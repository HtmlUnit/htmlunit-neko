package org.htmlunit.cyberneko;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.htmlunit.benchmark.util.FastRandom;
import org.htmlunit.cyberneko.util.HtmlEntities1;
import org.htmlunit.cyberneko.util.HtmlEntities1.Resolver;
import org.htmlunit.cyberneko.util.HtmlEntities2.Level;
import org.junit.jupiter.api.Test;

public class HtmlEntities1ParserTest
{
    @Test
    public void happyPath()
    {
        final Optional<String> r = HtmlEntities1.get().lookup("gt");
        assertEquals(">", r.get());
    }

    @Test
    public void unknown()
    {
        final Optional<String> r = HtmlEntities1.get().lookup("anything");
        assertFalse(r.isPresent());
    }

    @Test
    public void unicodeFind()
    {
        final Optional<String> r = HtmlEntities1.get().lookup("dot;");
        assertEquals("\u02D9", r.get());
    }

    @Test
    public void existsButOnlyAsPiece()
    {
        final Optional<String> r = HtmlEntities1.get().lookup("Agra");
        assertFalse(r.isPresent());
    }

    @Test
    public void existsInTwoVersions()
    {
        final Optional<String> r1 = HtmlEntities1.get().lookup("Agrave");
        assertEquals("\u00C0", r1.get());

        final Optional<String> r2 = HtmlEntities1.get().lookup("Agrave;");
        assertEquals("\u00C0", r2.get());
    }

    /**
     * Test all entities
     * @throws IOException
     */
    @Test
    public void allEntitiesFullRandom() throws IOException {
        final Properties props = new Properties();
        try (InputStream stream = HtmlEntities1ParserTest.class.getResourceAsStream("html_entities.properties")) {
            props.load(stream);
        }

        final List<String> keys = new ArrayList<>();
        final List<String> values = new ArrayList<>();

        props.forEach((k, v) -> {
            String key = (String) k;
            String value = (String) v;

            // we might have an empty line in it
            if (key.isEmpty()) {
                return;
            }

            // we need randomness to avoid that the setup data looks identical to the quueried data
            FastRandom r = new FastRandom();
            int pos = r.nextInt(keys.size() + 1);

            keys.add(pos, key);
            values.add(pos, value);
        });


        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = values.get(i);

            // we might have an empty line in it
            // we also don't want to test "old" entities at the moment aka no ; at the end
            if (key.trim().isEmpty()) {
                return;
            }

            final Optional<String> r = HtmlEntities1.get().lookup(key);
            assertEquals(value, r.get());
        }
    }

    /**
     * Test all entities
     * @throws IOException
     */
    @Test
    public void allEntitiesByCharacter() throws IOException {
        final Properties props = new Properties();
        try (InputStream stream = HtmlEntities1ParserTest.class.getResourceAsStream("html_entities.properties")) {
            props.load(stream);
        }

        // touch me for faster debug loading
        HtmlEntities1.get();

        props.forEach((k, v) -> {
            String key = (String) k;
            String value = (String) v;

            // we might have an empty line in it
            if (key.isEmpty()) {
                return;
            }

            final Resolver resolver = new HtmlEntities1.Resolver();
            for (int i = 0; i < key.length(); i++)
            {
                final int c = key.charAt(i);
                boolean r = resolver.parse(c);

                // end when false and things should be complete
                if (r == false)
                {
                    break;
                }
            }

            assertEquals(value, resolver.getResolvedValue());
            assertEquals(key.length(), resolver.getMatchLength());
            assertEquals(0, resolver.getRewindCount());
            assertEquals(key.endsWith(";"), resolver.endsWithSemicolon());
        });
    }
}
