/*
 * Copyright (c) 2002-2009 Andy Clark, Marc Guillemot
 * Copyright (c) 2017-2025 Ronald Brill
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
package org.htmlunit.cyberneko.xerces.dom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;

/**
 * Unit tests for {@link TextImpl}.
 *
 * @author Ronald Brill
 */
public class TextImplTest {

    @Test
    public void ctor() throws Exception {
        final DocumentImpl doc = new DocumentImpl();
        final TextImpl text = new TextImpl(doc, "Neko");

        assertEquals("Neko", text.getData());
    }

    @Test
    public void replaceWholeTextSimple() throws Exception {
        final DocumentImpl doc = new DocumentImpl();
        final TextImpl text = new TextImpl(doc, "Neko");

        text.replaceWholeText("new text");
        assertEquals("new text", text.getData());
    }

    @Test
    public void cloneNode() throws Exception {
        final DocumentImpl doc = new DocumentImpl();
        final TextImpl text = new TextImpl(doc, "Neko");

        final Node clone = text.cloneNode(true);
        assertEquals(3, clone.getNodeType());

        assertEquals("Neko", ((TextImpl) clone).getData());
    }
}
