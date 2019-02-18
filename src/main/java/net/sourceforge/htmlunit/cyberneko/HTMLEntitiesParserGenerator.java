/*
 * Copyright 2002-2009 Andy Clark, Marc Guillemot
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sourceforge.htmlunit.cyberneko;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Generator for the parser HTMLNamedEntitiesParser
 *
 * @author Andy Clark
 * @author Ronald Brill
 */
public class HTMLEntitiesParserGenerator {

    private static final class State {
        private static int idGen = HTMLEntitiesParser.STATE_START;

        int id;
        String switchCode;

        public State() {
            id = idGen++;
        }
    }

    public static void main(String[] args) {
        final Properties props = new Properties();

        load0(props, "res/html_entities.properties");

        final String[] entities = new String[props.size()];
        final String[] mapped = new String[props.size()];

        int i = 0;
        for (Object key : props.keySet()) {
            entities[i++] = key.toString();
        }
        Arrays.sort(entities);
        for (i= 0; i < entities.length; i++) {
            mapped[i] = props.getProperty(entities[i]).toString();
        }

        String start = "";
        List<State> states = new LinkedList<HTMLEntitiesParserGenerator.State>();
        switchChar(entities, mapped, start, states);

        int splitter = 1000;
        int count = 1;
        System.out.println("    private boolean parse" + count + "(final int current) {");
        System.out.println("        consumedCount++;");
        System.out.println("        switch (state) {");

        for (State state : states) {
            if (state.id >= count * splitter) {
                System.out.println("        }");
                System.out.println("        return false;");
                System.out.println("    }");
                count++;
                System.out.println();
                System.out.println("    private boolean parse" + count + "(final int current) {");
                System.out.println("        consumedCount++;");
                System.out.println("        switch (state) {");
            }
            System.out.println("            case " + state.id +":");
            System.out.print(state.switchCode);
            System.out.println("                break;");
        }

        System.out.println("        }");
        System.out.println("        return false;");
        System.out.println("    }");

        System.out.println();
        System.out.println("    public boolean parse(final int current) {");
        for (int j = 1; j <= count; j++) {
            System.out.println("        if (state < " + (j * splitter) + ") {");
            System.out.println("            return parse" + j + "(current);");
            System.out.println("        }");
        }

        System.out.println("        return false;");
        System.out.println("    }");
    }

    private static int switchChar(final String[] entities, final String[] mapped, final String start, final List<State> states) {
        int c = -1;
        State state = new State();
        states.add(state);
        state.switchCode = "                switch (current) {\n";

        for (int i = 0; i < entities.length; i++) {
            String entity = entities[i];
            if (entity.startsWith(start) && entity.length() > start.length()) {
                if (entity.charAt(start.length()) != c) {
                    c = entity.charAt(start.length());
                    if (entity.length() - start.length() > 1) {
                        state.switchCode += "                    case '" + (char) c +"' :\n";
                        int stateId = switchChar(entities, mapped, start + (char) c, states);
                        state.switchCode += "                        state = " + stateId + ";\n";
                        state.switchCode += "                        return true;\n";
                    } else {
                        state.switchCode += "                    case '" + (char) c + "' : // " + entity + "\n";
                        state.switchCode += "                        match = \"" + escape(mapped[i]) + "\";\n";
                        state.switchCode += "                        matchLength = consumedCount;\n";
                        // do we have to go on?
                        if (i+1 < entities.length
                                && entities[i+1].startsWith(start + (char)c)
                                && entities[i+1].length() > start.length()+ 1) {
                            int stateId = switchChar(entities, mapped, start + (char) c, states);
                            state.switchCode += "                        state = " + stateId + ";\n";
                            state.switchCode += "                        return true;\n";
                        } else {
                            if (c == ';') {
                                state.switchCode += "                        state = STATE_ENDS_WITH_SEMICOLON;\n";
                                state.switchCode += "                        return false;\n";
                            } else {
                                state.switchCode += "                        return false;\n";
                            }
                        }
                    }
                }
            }
        }
        state.switchCode += "                }\n";
        return state.id;
    }

    private static String escape(String input) {
        StringBuilder b = new StringBuilder();

        for (char c : input.toCharArray()) {
            if ('\n' == c) {
                b.append("\\n");
            }
            else if (c == '"') {
                b.append("\\\"");
            }
            else if (c == '\\') {
                b.append("\\\\");
            }
            else if (c >= 127 || c <= 32) {
                b.append("\\u").append(String.format("%04X", (int) c));
            }
            else {
                b.append(c);
            }
        }

        return b.toString();
    }

    /** Loads the entity values in the specified resource. */
    private static void load0(final Properties props, final String filename) {
        try (InputStream stream = HTMLEntitiesParserGenerator.class.getResourceAsStream(filename)) {
            props.load(stream);
        }
        catch (final IOException e) {
            System.err.println("error: unable to load resource \""+filename+"\"");
        }
    }
}
