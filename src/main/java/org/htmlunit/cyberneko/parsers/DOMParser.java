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
package org.htmlunit.cyberneko.parsers;

import org.htmlunit.cyberneko.HTMLConfiguration;
import org.htmlunit.cyberneko.xerces.dom.DocumentImpl;
import org.htmlunit.cyberneko.xerces.parsers.AbstractDOMParser;

/**
 * A DOM parser for HTML documents.
 *
 * @author Andy Clark
 */
public class DOMParser extends AbstractDOMParser {

    /** Default constructor. */
    public DOMParser(final Class<? extends DocumentImpl> documentClass) {
        super(new HTMLConfiguration(), documentClass);
    }
}
