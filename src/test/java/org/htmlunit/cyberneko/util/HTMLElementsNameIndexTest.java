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
package org.htmlunit.cyberneko.util;

import org.htmlunit.cyberneko.HTMLElements.Element;
import org.htmlunit.cyberneko.HTMLElementsNameIndex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link HTMLElementsNameIndex}.
 *
 * @author Ronald Brill
 */
public class HTMLElementsNameIndexTest {

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void oneElement() throws Exception {
        HTMLElementsNameIndex.HTMLElementsNameIndexBuilder builder = new HTMLElementsNameIndex.HTMLElementsNameIndexBuilder();

        builder.register(new Element((short) 0, "ABCD", 0, (short) 0, new short[] {}));
        builder.register(new Element((short) 1, "ABBD", 0, (short) 0, new short[] {}));

        HTMLElementsNameIndex index = builder.build();

        Assertions.assertEquals("ABCD", index.getElement("ABCD", null).name);
        Assertions.assertEquals("ABCD", index.getElement("abcd", null).name);
        Assertions.assertEquals("ABBD", index.getElement("ABBD", null).name);
        Assertions.assertEquals("ABBD", index.getElement("abbd", null).name);
    }
}
