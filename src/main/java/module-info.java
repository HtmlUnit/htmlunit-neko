/*
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

/**
 * @author Ronald Brill
 * @since 5.0
 */
module org.htmlunit.cyberneko {
    requires java.xml;
    requires jdk.xml.dom;

    exports org.htmlunit.cyberneko;
    exports org.htmlunit.cyberneko.filters;
    exports org.htmlunit.cyberneko.html.dom;
    exports org.htmlunit.cyberneko.io;
    exports org.htmlunit.cyberneko.parsers;
    exports org.htmlunit.cyberneko.sax.helpers;
    exports org.htmlunit.cyberneko.util;
    exports org.htmlunit.cyberneko.xerces.dom;
    exports org.htmlunit.cyberneko.xerces.parsers;
    exports org.htmlunit.cyberneko.xerces.util;
    exports org.htmlunit.cyberneko.xerces.xni;
    exports org.htmlunit.cyberneko.xerces.xni.parser;
}