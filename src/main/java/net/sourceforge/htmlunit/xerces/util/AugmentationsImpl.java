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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.htmlunit.xerces.xni.Augmentations;

/**
 * This class provides an implementation for Augmentations interface.
 * Augmentations interface defines a hashtable of additional data that could
 * be passed along the document pipeline. The information can contain extra
 * arguments or infoset augmentations, for example PSVI. This additional
 * information is identified by a String key.
 * <p>
 *
 * @author Elena Litani, IBM
 */
public class AugmentationsImpl implements Augmentations {

    private AugmentationsItemsContainer fAugmentationsContainer = new SmallContainer();

    /**
     * Add additional information identified by a key to the Augmentations structure.
     *
     * @param key    Identifier, can't be <code>null</code>
     * @param item   Additional information
     *
     * @return the previous value of the specified key in the Augmentations strucutre,
     *         or <code>null</code> if it did not have one.
     */
    @Override
    public Object putItem (String key, Object item){
        Object oldValue = fAugmentationsContainer.putItem(key, item);

        if (oldValue == null && fAugmentationsContainer.isFull()) {
            fAugmentationsContainer = fAugmentationsContainer.expand();
        }

        return oldValue;
    }

    /**
     * Get information identified by a key from the Augmentations structure
     *
     * @param key    Identifier, can't be <code>null</code>
     *
     * @return the value to which the key is mapped in the Augmentations structure;
     *         <code>null</code> if the key is not mapped to any value.
     */
    @Override
    public Object getItem(String key){
        return fAugmentationsContainer.getItem(key);
    }

    /**
     * Remove additional info from the Augmentations structure
     *
     * @param key    Identifier, can't be <code>null</code>
     */
    @Override
    public Object removeItem (String key){
        return fAugmentationsContainer.removeItem(key);
    }

    /**
     * Returns an enumeration of the keys in the Augmentations structure
     *
     */
    @Override
    public Enumeration<String> keys (){
        return fAugmentationsContainer.keys();
    }

    /**
     * Remove all objects from the Augmentations structure.
     */
    @Override
    public void removeAllItems() {
        fAugmentationsContainer.clear();
    }

    @Override
    public String toString() {
        return fAugmentationsContainer.toString();
    }

    static abstract class AugmentationsItemsContainer {
        abstract public Object putItem(String key, Object item);
        abstract public Object getItem(String key);
        abstract public Object removeItem(String key);
        abstract public Enumeration<String> keys();
        abstract public void clear();
        abstract public boolean isFull();
        abstract public AugmentationsItemsContainer expand();
    }

    final static class SmallContainer extends AugmentationsItemsContainer {

        final static int SIZE_LIMIT = 10;
        final Object[] fAugmentations = new Object[SIZE_LIMIT*2];
        int fNumEntries = 0;

        @Override
        public Enumeration<String> keys() {
            return new SmallContainerKeyEnumeration();
        }

        @Override
        public Object getItem(String key) {
            for (int i = 0; i < fNumEntries*2; i = i + 2) {
                if (fAugmentations[i].equals(key)) {
                    return fAugmentations[i+1];
                }
            }

            return null;
        }

        @Override
        public Object putItem(String key, Object item) {
            for (int i = 0; i < fNumEntries*2; i = i + 2) {
                if (fAugmentations[i].equals(key)) {
                    Object oldValue = fAugmentations[i+1];
                    fAugmentations[i+1] = item;

                    return oldValue;
                }
            }

            fAugmentations[fNumEntries*2] = key;
            fAugmentations[fNumEntries*2+1] = item;
            fNumEntries++;

            return null;
        }


        @Override
        public Object removeItem(String key) {
            for (int i = 0; i < fNumEntries*2; i = i + 2) {
                if (fAugmentations[i].equals(key)) {
                    Object oldValue = fAugmentations[i+1];

                    for (int j = i; j < fNumEntries*2 - 2; j = j + 2) {
                        fAugmentations[j] = fAugmentations[j+2];
                        fAugmentations[j+1] = fAugmentations[j+3];
                    }

                    fAugmentations[fNumEntries*2-2] = null;
                    fAugmentations[fNumEntries*2-1] = null;
                    fNumEntries--;

                    return oldValue;
                }
            }

            return null;
        }

        @Override
        public void clear() {
            for (int i = 0; i < fNumEntries*2; i = i + 2) {
                fAugmentations[i] = null;
                fAugmentations[i+1] = null;
            }

            fNumEntries = 0;
        }

        @Override
        public boolean isFull() {
            return (fNumEntries == SIZE_LIMIT);
        }

        @Override
        public AugmentationsItemsContainer expand() {
            LargeContainer expandedContainer = new LargeContainer();

            for (int i = 0; i < fNumEntries*2; i = i + 2) {
                expandedContainer.putItem((String)fAugmentations[i],
                                          fAugmentations[i+1]);
            }

            return expandedContainer;
        }

        @Override
        public String toString() {
            StringBuilder buff = new StringBuilder();
            buff.append("SmallContainer - fNumEntries == ").append(fNumEntries);

            for (int i = 0; i < SIZE_LIMIT*2; i=i+2) {
                buff.append("\nfAugmentations[");
                buff.append(i);
                buff.append("] == ");
                buff.append(fAugmentations[i]);
                buff.append("; fAugmentations[");
                buff.append(i+1);
                buff.append("] == ");
                buff.append(fAugmentations[i+1]);
            }

            return buff.toString();
        }

        final class SmallContainerKeyEnumeration implements Enumeration<String> {

            final String[] enumArray = new String[fNumEntries];
            int next = 0;

            SmallContainerKeyEnumeration() {
                for (int i = 0; i < fNumEntries; i++) {
                    enumArray[i] = (String)fAugmentations[i*2];
                }
            }

            @Override
            public boolean hasMoreElements() {
                return next < enumArray.length;
            }

            @Override
            public String nextElement() {
                if (next >= enumArray.length) {
                    throw new java.util.NoSuchElementException();
                }

                Object nextVal = enumArray[next];
                enumArray[next] = null;
                next++;

                return (String) nextVal;
            }
        }
    }

    final static class LargeContainer extends AugmentationsItemsContainer {

        private final HashMap<String, Object> fAugmentations = new HashMap<>();

        @Override
        public Object getItem(String key) {
            return fAugmentations.get(key);
        }

        @Override
        public Object putItem(String key, Object item) {
            return fAugmentations.put(key, item);
        }

        @Override
        public Object removeItem(String key) {
            return fAugmentations.remove(key);
        }

        @Override
        public Enumeration<String> keys() {
            return Collections.enumeration(fAugmentations.keySet());
        }

        @Override
        public void clear() {
            fAugmentations.clear();
        }

        @Override
        public boolean isFull() {
            return false;
        }

        @Override
        public AugmentationsItemsContainer expand() {
            return this;
        }

        @Override
        public String toString() {
            StringBuilder buff = new StringBuilder();
            buff.append("LargeContainer");
            for (Map.Entry<String, Object> entry : fAugmentations.entrySet()) {
                buff.append("\nkey == ");
                buff.append(entry.getKey());
                buff.append("; value == ");
                buff.append(entry.getValue());
            }
            return buff.toString();
        }
    }
}
