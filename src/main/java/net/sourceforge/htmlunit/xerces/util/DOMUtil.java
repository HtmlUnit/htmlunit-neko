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

import org.w3c.dom.ls.LSException;

/**
 * Some useful utility methods.
 * This class was modified in Xerces2 with a view to abstracting as
 * much as possible away from the representation of the underlying
 * parsed structure (i.e., the DOM).  This was done so that, if Xerces
 * ever adopts an in-memory representation more efficient than the DOM
 * (such as a DTM), we should easily be able to convert our schema
 * parsing to utilize it.
 */
public final class DOMUtil {

    //
    // Constructors
    //

    /** This class cannot be instantiated. */
    private DOMUtil() {}

    /**
     * Creates an LSException. On J2SE 1.4 and above the cause for the exception will be set.
     */
    public static LSException createLSException(short code, Throwable cause) {
        LSException lse = new LSException(code, cause != null ? cause.getMessage() : null);
        if (cause != null && ThrowableMethods.fgThrowableMethodsAvailable) {
            try {
                ThrowableMethods.fgThrowableInitCauseMethod.invoke(lse, new Object [] {cause});
            }
            // Something went wrong. There's not much we can do about it.
            catch (Exception e) {}
        }
        return lse;
    }

    /**
     * Holder of methods from java.lang.Throwable.
     */
    static class ThrowableMethods {

        // Method: java.lang.Throwable.initCause(java.lang.Throwable)
        private static java.lang.reflect.Method fgThrowableInitCauseMethod = null;

        // Flag indicating whether or not Throwable methods available.
        private static boolean fgThrowableMethodsAvailable = false;

        private ThrowableMethods() {}

        // Attempt to get methods for java.lang.Throwable on class initialization.
        static {
            try {
                fgThrowableInitCauseMethod = Throwable.class.getMethod("initCause", new Class [] {Throwable.class});
                fgThrowableMethodsAvailable = true;
            }
            // ClassNotFoundException, NoSuchMethodException or SecurityException
            // Whatever the case, we cannot use java.lang.Throwable.initCause(java.lang.Throwable).
            catch (Exception exc) {
                fgThrowableInitCauseMethod = null;
                fgThrowableMethodsAvailable = false;
            }
        }
    }

} // class DOMUtil
