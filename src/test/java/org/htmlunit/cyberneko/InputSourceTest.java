/*
 * Copyright (c) 2002-2009 Andy Clark, Marc Guillemot
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
package org.htmlunit.cyberneko;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.htmlunit.cyberneko.parsers.SAXParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

/**
 * Unit tests for {@link InputSource} handling.
 *
 * @author Ronald Brill
 */
public class InputSourceTest {

    /**
     * @throws Exception inc case of error
     */
    @Test
    public void inputStreamBomOverwrittenByEncoding() throws Exception {
        final byte[] bom = new byte[] {(byte) 0xef, (byte) 0xbb, (byte) 0xbf};
        final byte[] html = "<html><head></head><body>Neko</body></html>".getBytes(StandardCharsets.UTF_8);

        byte[] bytes = new byte[bom.length + html.length];
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.put(bom);
        buffer.put(html);
        bytes = buffer.array();

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            final InputSource source = new InputSource(bais);
            source.setEncoding("ASCII");

            final SAXParser parser = new SAXParser();
            try (StringWriter out = new StringWriter()) {
                // parse
                final SaxHandler saxHandler = new SaxHandler(out);
                parser.setContentHandler(saxHandler);
                parser.setProperty("http://xml.org/sax/properties/lexical-handler", saxHandler);
                parser.setErrorHandler(saxHandler);
                parser.parse(source);

                final StringBuilder sb = new StringBuilder();

                // first the error handler output
                final BufferedReader reader = new BufferedReader(new StringReader(out.toString()));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }

                Assertions.assertEquals(
                        "(html\n"
                        + "(head\n"
                        + ")head\n"
                        + "(body\n"
                        + "\"Neko\n"
                        + ")body\n"
                        + ")html\n",
                        sb.toString());
            }
        }
    }
}
