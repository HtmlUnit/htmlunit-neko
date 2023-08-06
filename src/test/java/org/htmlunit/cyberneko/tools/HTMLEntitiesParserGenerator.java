/*
 * Copyright 2002-2009 Andy Clark, Marc Guillemot
 * Copyright 2017-2023 Ronald Brill
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
package org.htmlunit.cyberneko.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.htmlunit.cyberneko.HTMLEntitiesParser;

/**
 * Generator for the parser HTMLEntitiesParser.
 *
 * @author Andy Clark
 * @author Ronald Brill
 */
public final class HTMLEntitiesParserGenerator {

    private static final class State {
        private static int IdGen_ = HTMLEntitiesParser.STATE_START;

        private int id_;
        private String switchCode_;
        private String ifCode_;
        private int branches_;

        State() {
            id_ = IdGen_++;
        }
    }

    private HTMLEntitiesParserGenerator() {
    }

    public static void main(final String[] args) {
        final Properties props = new Properties();

        load0(props, "html_entities.properties");

        final String[] entities = new String[props.size()];
        final String[] mapped = new String[props.size()];

        int i = 0;
        for (final Object key : props.keySet()) {
            entities[i++] = key.toString();
        }
        Arrays.sort(entities);
        for (i = 0; i < entities.length; i++) {
            mapped[i] = props.getProperty(entities[i]);
        }

        final String start = "";
        final List<State> states = new LinkedList<>();
        switchChar(entities, mapped, start, states);

        final int splitter = 1000;
        int count = 1;
        System.out.println("    private boolean parse" + count + "(final int current) {");
        System.out.println("        consumedCount++;");
        System.out.println("        switch (state) {");

        for (final State state : states) {
            if (state.id_ >= count * splitter) {
                System.out.println("        }");
                System.out.println("        return false;");
                System.out.println("    }");
                count++;
                System.out.println();
                System.out.println("    private boolean parse" + count + "(final int current) {");
                System.out.println("        consumedCount++;");
                System.out.println("        switch (state) {");
            }
            System.out.println("            case " + state.id_ + ":");
            if (state.branches_ < 3) {
                System.out.print(state.ifCode_);
            }
            else {
                System.out.print(state.switchCode_);
            }
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
        final State state = new State();
        states.add(state);

        state.switchCode_ = "                switch (current) {\n";
        state.ifCode_ = "";

        for (int i = 0; i < entities.length; i++) {
            final String entity = entities[i];
            if (entity.startsWith(start) && entity.length() > start.length()) {
                if (entity.charAt(start.length()) != c) {
                    c = entity.charAt(start.length());
                    if (entity.length() - start.length() > 1) {
                        final int stateId = switchChar(entities, mapped, start + (char) c, states);

                        state.switchCode_ += "                    case '" + (char) c + "':\n";
                        state.switchCode_ += "                        state = " + stateId + ";\n";
                        state.switchCode_ += "                        return true;\n";

                        if (state.branches_ > 0) {
                            state.ifCode_ += "                else if ('" + (char) c + "' == current) {\n";
                        }
                        else {
                            state.ifCode_ += "                if ('" + (char) c + "' == current) {\n";
                        }
                        state.ifCode_ += "                    state = " + stateId + ";\n";
                        state.ifCode_ += "                    return true;\n";
                        state.ifCode_ += "                }\n";

                        state.branches_++;
                    }
                    else {
                        state.switchCode_ += "                    case '" + (char) c + "': // " + entity + "\n";
                        state.switchCode_ += "                        match = \"" + escape(mapped[i]) + "\";\n";
                        state.switchCode_ += "                        matchLength = consumedCount;\n";

                        state.ifCode_ += "                // " + entity + "\n";
                        if (state.branches_ > 0) {
                            state.ifCode_ += "                else if ('" + (char) c + "' == current) {\n";
                        }
                        else {
                            state.ifCode_ += "                if ('" + (char) c + "' == current) {\n";
                        }
                        state.ifCode_ += "                    match = \"" + escape(mapped[i]) + "\";\n";
                        state.ifCode_ += "                    matchLength = consumedCount;\n";

                        // do we have to go on?
                        if (i + 1 < entities.length
                                && entities[i + 1].startsWith(start + (char) c)
                                && entities[i + 1].length() > start.length() + 1) {
                            final int stateId = switchChar(entities, mapped, start + (char) c, states);
                            state.switchCode_ += "                        state = " + stateId + ";\n";
                            state.switchCode_ += "                        return true;\n";

                            state.ifCode_ += "                    state = " + stateId + ";\n";
                            state.ifCode_ += "                    return true;\n";
                            state.ifCode_ += "                }\n";

                            state.branches_++;
                        }
                        else {
                            if (c == ';') {
                                state.switchCode_ += "                        state = STATE_ENDS_WITH_SEMICOLON;\n";
                                state.switchCode_ += "                        return false;\n";

                                state.ifCode_ += "                    state = STATE_ENDS_WITH_SEMICOLON;\n";
                                state.ifCode_ += "                    return false;\n";
                                state.ifCode_ += "                }\n";

                                state.branches_++;
                            }
                            else {
                                state.switchCode_ += "                        return false;\n";

                                state.ifCode_ += "                    return false;\n";
                                state.ifCode_ += "                }\n";

                                state.branches_++;
                            }
                        }
                    }
                }
            }
        }

        state.switchCode_ += "                }\n";

        return state.id_;
    }

    private static String escape(final String input) {
        final StringBuilder b = new StringBuilder();

        for (final char c : input.toCharArray()) {
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
            System.err.println("error: unable to load resource \"" + filename + "\" reson: " + e.getMessage());
        }
    }
}
