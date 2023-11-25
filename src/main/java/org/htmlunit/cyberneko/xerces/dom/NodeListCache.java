/*
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
package org.htmlunit.cyberneko.xerces.dom;

/**
 * This class is used, via a pool managed on CoreDocumentImpl, in ParentNode to
 * improve performance of the NodeList accessors, getLength() and item(i).
 * <p>
 *
 * @author Arnaud Le Hors, IBM
 */
class NodeListCache {

    /** Cached node list length. */
    int fLength = -1;

    /** Last requested node index. */
    int fChildIndex = -1;

    /** Last requested node. */
    ChildNode fChild;

    /** Owner of this cache */
    ParentNode fOwner;

    /**
     * Pointer to the next object on the list, only meaningful when actully stored
     * in the free list.
     */
    NodeListCache next;

    NodeListCache(final ParentNode owner) {
        fOwner = owner;
    }
}
