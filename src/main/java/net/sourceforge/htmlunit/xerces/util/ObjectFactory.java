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

/**
 * This class is duplicated for each JAXP subpackage so keep it in sync.
 * It is package private and therefore is not exposed as part of the JAXP
 * API.
 * <p>
 * This code is designed to implement the JAXP 1.1 spec pluggability
 * feature and is designed to run on JDK version 1.1 and
 * later, and to compile on JDK 1.2 and onward.
 * The code also runs both as part of an unbundled jar file and
 * when bundled as part of the JDK.
 *
 */
public final class ObjectFactory {

    /** Set to true for debugging */
    private static final boolean DEBUG = isDebugEnabled();

    //
    // Private static methods
    //

    /** @return true if debug has been enabled. */
    private static boolean isDebugEnabled() {
        try {
            String val = SecuritySupport.getSystemProperty("xerces.debug");
            // Allow simply setting the prop to turn on debug
            return (val != null && (!"false".equals(val)));
        }
        catch (SecurityException se) {}
        return false;
    } // isDebugEnabled()

    /** Prints a message to standard error if debugging is enabled. */
    private static void debugPrintln(String msg) {
        if (DEBUG) {
            System.err.println("XERCES: " + msg);
        }
    } // debugPrintln(String)

    /**
     * Figure out which ClassLoader to use.  For JDK 1.2 and later use
     * the context ClassLoader.
     * @return the class loader
     * @throws ConfigurationError on error
     */
    public static ClassLoader findClassLoader()
        throws ConfigurationError
    {
        // Figure out which ClassLoader to use for loading the provider
        // class.  If there is a Context ClassLoader then use it.
        ClassLoader context = SecuritySupport.getContextClassLoader();
        ClassLoader system = SecuritySupport.getSystemClassLoader();

        ClassLoader chain = system;
        while (true) {
            if (context == chain) {
                // Assert: we are on JDK 1.1 or we have no Context ClassLoader
                // or any Context ClassLoader in chain of system classloader
                // (including extension ClassLoader) so extend to widest
                // ClassLoader (always look in system ClassLoader if Xerces
                // is in boot/extension/system classpath and in current
                // ClassLoader otherwise); normal classloaders delegate
                // back to system ClassLoader first so this widening doesn't
                // change the fact that context ClassLoader will be consulted
                ClassLoader current = ObjectFactory.class.getClassLoader();

                chain = system;
                while (true) {
                    if (current == chain) {
                        // Assert: Current ClassLoader in chain of
                        // boot/extension/system ClassLoaders
                        return system;
                    }
                    if (chain == null) {
                        break;
                    }
                    chain = SecuritySupport.getParentClassLoader(chain);
                }

                // Assert: Current ClassLoader not in chain of
                // boot/extension/system ClassLoaders
                return current;
            }

            if (chain == null) {
                // boot ClassLoader reached
                break;
            }

            // Check for any extension ClassLoaders in chain up to
            // boot ClassLoader
            chain = SecuritySupport.getParentClassLoader(chain);
        }

        // Assert: Context ClassLoader not in chain of
        // boot/extension/system ClassLoaders
        return context;
    } // findClassLoader():ClassLoader

     // Find a Class using the specified ClassLoader
    public static Class<?> findProviderClass(String className, ClassLoader cl, boolean doFallback)
        throws ClassNotFoundException, ConfigurationError
    {
        //throw security exception if the calling thread is not allowed to access the package
        //restrict the access to package as specified in java.security policy
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            final int lastDot = className.lastIndexOf('.');
            String packageName = className;
            if (lastDot != -1) packageName = className.substring(0, lastDot);
            security.checkPackageAccess(packageName);
        }
        Class<?> providerClass;
        if (cl == null) {
            // XXX Use the bootstrap ClassLoader.  There is no way to
            // load a class using the bootstrap ClassLoader that works
            // in both JDK 1.1 and Java 2.  However, this should still
            // work b/c the following should be true:
            //
            // (cl == null) iff current ClassLoader == null
            //
            // Thus Class.forName(String) will use the current
            // ClassLoader which will be the bootstrap ClassLoader.
            providerClass = Class.forName(className);
        } else {
            try {
                providerClass = cl.loadClass(className);
            } catch (ClassNotFoundException x) {
                if (doFallback) {
                    // Fall back to current classloader
                    ClassLoader current = ObjectFactory.class.getClassLoader();
                    if (current == null) {
                        providerClass = Class.forName(className);
                    } else if (cl != current) {
                        cl = current;
                        providerClass = cl.loadClass(className);
                    } else {
                        throw x;
                    }
                } else {
                    throw x;
                }
            }
        }

        return providerClass;
    }

    /**
     * A configuration error.
     */
    static final class ConfigurationError extends Error {

        private static final long serialVersionUID = 5666859090980261868L;

        /** Exception. */
        private final Exception exception;

        // Construct a new instance with the specified detail string and
        // exception.
        ConfigurationError(String msg, Exception x) {
            super(msg);
            this.exception = x;
        } // <init>(String,Exception)

        /** @return the exception associated to this error. */
        Exception getException() {
            return exception;
        } // getException():Exception

    } // class ConfigurationError

} // class ObjectFactory
