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

package net.sourceforge.htmlunit.xerces.util;

import java.util.MissingResourceException;

/**
 * This interface provides a generic message formatting mechanism and
 * is useful for producing messages that must be localized and/or formatted
 * with replacement text.
 *
 * @see net.sourceforge.htmlunit.xerces.impl.XMLErrorReporter
 *
 * @author Andy Clark
 */
public interface MessageFormatter {

    //
    // MessageFormatter methods
    //

    /**
     * Formats a message with the specified arguments.
     *
     * @param key       The message key.
     * @param arguments The message replacement text arguments. The order
     *                  of the arguments must match that of the placeholders
     *                  in the actual message.
     *
     * @return Returns the formatted message.
     *
     * @throws MissingResourceException Thrown if the message with the
     *                                  specified key cannot be found.
     */
    String formatMessage(String key, Object[] arguments) throws MissingResourceException;

}
