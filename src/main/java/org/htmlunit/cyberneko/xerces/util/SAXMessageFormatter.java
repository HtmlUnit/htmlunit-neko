/*
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
package org.htmlunit.cyberneko.xerces.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Used to format SAX error messages.
 *
 * @author Michael Glavassevich, IBM
 */
public final class SAXMessageFormatter {

    private static final ResourceBundle SAXResourceBundle_ = ResourceBundle.getBundle("org.htmlunit.cyberneko.res.SAXMessages");

    private SAXMessageFormatter() {
    }

    /**
     * Formats a message with the specified arguments.
     *
     * @param key       The message key.
     * @param arguments The message replacement text arguments. The order of the
     *                  arguments must match that of the placeholders in the actual
     *                  message.
     *
     * @return the formatted message.
     *
     * @throws MissingResourceException Thrown if the message with the specified key
     *                                  cannot be found.
     */
    public static String formatMessage(final String key, final Object[] arguments) throws MissingResourceException {
        try {
            String msg = SAXResourceBundle_.getString(key);
            if (arguments != null) {
                try {
                    msg = java.text.MessageFormat.format(msg, arguments);
                }
                catch (final Exception e) {
                    msg = SAXResourceBundle_.getString("FormatFailed");
                    msg += " " + SAXResourceBundle_.getString(key);
                }
            }

            return msg;
        }
        catch (final MissingResourceException e) {
            MissingResourceException mre = new MissingResourceException(key, SAXResourceBundle_.getString("BadMessageKey"), key);
            mre.initCause(e);
            throw mre;
        }
    }
}
