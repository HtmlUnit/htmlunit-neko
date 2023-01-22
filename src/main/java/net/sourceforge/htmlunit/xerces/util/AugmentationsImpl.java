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

import java.util.HashMap;
import java.util.Set;

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

    private final HashMap<String, Object> fAugmentationsContainer = new HashMap<>();

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
    public Object put(String key, Object item){
        return fAugmentationsContainer.put(key, item);
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
    public Object get(String key){
        return fAugmentationsContainer.get(key);
    }

    /**
     * Returns an enumeration of the keys in the Augmentations structure
     *
     */
    @Override
    public Set<String> keys(){
        return fAugmentationsContainer.keySet();
    }

    /**
     * Remove all objects from the Augmentations structure.
     */
    @Override
    public void clear() {
        fAugmentationsContainer.clear();
    }

    @Override
    public String toString() {
        return fAugmentationsContainer.toString();
    }
}
