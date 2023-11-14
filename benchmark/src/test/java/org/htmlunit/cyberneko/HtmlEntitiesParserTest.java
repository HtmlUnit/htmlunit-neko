package org.htmlunit.cyberneko;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

import org.htmlunit.cyberneko.util.HtmlEntities;
import org.htmlunit.cyberneko.util.HtmlEntities.Resolver;
import org.junit.jupiter.api.Test;

public class HtmlEntitiesParserTest
{
    @Test
    public void happyPath()
    {
        final Optional<String> r = HtmlEntities.get().lookup("gt");
        assertEquals(">", r.get());
    }

    @Test
    public void unknown()
    {
        final Optional<String> r = HtmlEntities.get().lookup("anything");
        assertFalse(r.isPresent());
    }

    @Test
    public void unicodeFind()
    {
        final Optional<String> r = HtmlEntities.get().lookup("dot;");
        assertEquals("\u02D9", r.get());
    }

    @Test
    public void existsButOnlyAsPiece()
    {
        final Optional<String> r = HtmlEntities.get().lookup("Agra");
        assertFalse(r.isPresent());
    }

    @Test
    public void existsInTwoVersions()
    {
        final Optional<String> r1 = HtmlEntities.get().lookup("Agrave");
        assertEquals("\u00C0", r1.get());

        final Optional<String> r2 = HtmlEntities.get().lookup("Agrave;");
        assertEquals("\u00C0", r2.get());
    }

    /**
     * Test all entities
     * @throws IOException
     */
    @Test
    public void allEntitiesInFull() throws IOException {
        final Properties props = new Properties();
        try (InputStream stream = HtmlEntitiesParserTest.class.getResourceAsStream("html_entities.properties")) {
            props.load(stream);
        }

        props.forEach((k, v) -> {
            String key = (String) k;
            String value = (String) v;

            // we might have an empty line in it
            if (key.trim().isEmpty()) {
                return;
            }

            final Optional<String> r = HtmlEntities.get().lookup(key);
            assertEquals(value, r.get());
        });
    }

    /**
     * Test all entities
     * @throws IOException
     */
    @Test
    public void allEntitiesByCharacter() throws IOException {
        final Properties props = new Properties();
        try (InputStream stream = HtmlEntitiesParserTest.class.getResourceAsStream("html_entities.properties")) {
            props.load(stream);
        }

        // touch me for faster debug loading
        HtmlEntities.get();

        props.forEach((k, v) -> {
            String key = (String) k;
            String value = (String) v;

            // we might have an empty line in it
            if (key.isEmpty()) {
                return;
            }

            final Resolver resolver = new HtmlEntities.Resolver();
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
