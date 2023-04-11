/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.htmlunit.cyberneko.xerces.impl.msg;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.htmlunit.cyberneko.xerces.util.MessageFormatter;

/**
 * XMLMessageFormatter provides error messages for the XML 1.0 Recommendation
 * and for the Namespaces Recommendation
 * <p>
 *
 * @author Eric Ye, IBM
 *
 */
public class XMLMessageFormatter implements MessageFormatter {
    /**
     * The domain of messages concerning the XML 1.0 specification.
     */
    public static final String XML_DOMAIN = "http://www.w3.org/TR/1998/REC-xml-19980210";
    public static final String XMLNS_DOMAIN = "http://www.w3.org/TR/1999/REC-xml-names-19990114";

    private ResourceBundle fResourceBundle = null;

    //
    // MessageFormatter methods
    //

    /**
     * Formats a message with the specified arguments.
     *
     * @param key       The message key.
     * @param arguments The message replacement text arguments. The order of the
     *                  arguments must match that of the placeholders in the actual
     *                  message.
     *
     * @return Returns the formatted message.
     *
     * @throws MissingResourceException Thrown if the message with the specified key
     *                                  cannot be found.
     */
    @Override
    public String formatMessage(String key, Object[] arguments) throws MissingResourceException {

        if (fResourceBundle == null) {
            fResourceBundle = ResourceBundle.getBundle("org.htmlunit.cyberneko.xerces.impl.msg.XMLMessages");
        }

        // format message
        String msg;
        try {
            msg = fResourceBundle.getString(key);
            if (arguments != null) {
                try {
                    msg = java.text.MessageFormat.format(msg, arguments);
                } catch (Exception e) {
                    msg = fResourceBundle.getString("FormatFailed");
                    msg += " " + fResourceBundle.getString(key);
                }
            }
        }

        // error
        catch (MissingResourceException e) {
            msg = fResourceBundle.getString("BadMessageKey");
            throw new MissingResourceException(key, msg, key);
        }

        // no message
        if (msg == null) {
            msg = key;
            if (arguments.length > 0) {
                StringBuilder str = new StringBuilder(msg);
                str.append('?');
                for (int i = 0; i < arguments.length; i++) {
                    if (i > 0) {
                        str.append('&');
                    }
                    str.append(arguments[i]);
                }
                msg = str.toString();
            }
        }

        return msg;
    }

}
