/*
 * Copyright 2002-2008 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sourceforge.htmlunit.cyberneko;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * This class is duplicated for each JAXP subpackage so keep it in sync.
 * It is package private and therefore is not exposed as part of the JAXP
 * API.
 *
 * Security related methods that only work on J2SE 1.2 and newer.
 */
class SecuritySupport12 extends SecuritySupport {

    @Override
    ClassLoader getContextClassLoader() {
    return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
    @Override
    public ClassLoader run() {
    ClassLoader cl = null;
    try {
        cl = Thread.currentThread().getContextClassLoader();
    } catch (SecurityException ex) { }
    return cl;
    }
   });
    }

    @Override
    ClassLoader getSystemClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                ClassLoader cl = null;
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (SecurityException ex) {}
                return cl;
            }
        });
    }

    @Override
    ClassLoader getParentClassLoader(final ClassLoader cl) {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                ClassLoader parent = null;
                try {
                    parent = cl.getParent();
                } catch (SecurityException ex) {}

                // eliminate loops in case of the boot
                // ClassLoader returning itself as a parent
                return (parent == cl) ? null : parent;
            }
        });
    }

    @Override
    String getSystemProperty(final String propName) {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return System.getProperty(propName);
            }
        });
    }

    @Override
    FileInputStream getFileInputStream(final File file)
        throws FileNotFoundException
    {
        try {
                return AccessController.doPrivileged(new PrivilegedExceptionAction<FileInputStream>() {
                    @Override
                    public FileInputStream run() throws FileNotFoundException {
                        return new FileInputStream(file);
                    }
                });
        } catch (PrivilegedActionException e) {
            throw (FileNotFoundException)e.getException();
        }
    }

    @Override
    InputStream getResourceAsStream(final ClassLoader cl,
                                           final String name)
    {
        return AccessController.doPrivileged(new PrivilegedAction<InputStream>() {
            @Override
            public InputStream run() {
                InputStream ris;
                if (cl == null) {
                    ris = ClassLoader.getSystemResourceAsStream(name);
                } else {
                    ris = cl.getResourceAsStream(name);
                }
                return ris;
            }
        });
    }

    @Override
    boolean getFileExists(final File f) {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            @Override
            public Boolean run() {
                return Boolean.valueOf(f.exists());
            }
        }).booleanValue();
        }

    @Override
    long getLastModified(final File f) {
        return AccessController.doPrivileged(new PrivilegedAction<Long>() {
            @Override
            public Long run() {
                return Long.valueOf(f.lastModified());
            }
        }).longValue();
    }

}
