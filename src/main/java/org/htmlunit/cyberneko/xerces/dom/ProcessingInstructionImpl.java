/*
 * Copyright (c) 2017-2025 Ronald Brill
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

import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

/**
 * Processing Instructions (PIs) permit documents to carry processor-specific
 * information alongside their actual content. PIs are most common in XML, but
 * they are supported in HTML as well.
 * <p>
 * This class inherits from CharacterDataImpl to reuse its setNodeValue method.
 */
public class ProcessingInstructionImpl extends CharacterDataImpl implements ProcessingInstruction {

    private final String target_;

    // Factory constructor.
    public ProcessingInstructionImpl(final CoreDocumentImpl ownerDoc, final String target, final String data) {
        super(ownerDoc, data);
        this.target_ = target;
    }

    /**
     * {@inheritDoc}
     *
     * A short integer indicating what type of node this is. The named constants for
     * this value are defined in the org.w3c.dom.Node interface.
     */
    @Override
    public short getNodeType() {
        return Node.PROCESSING_INSTRUCTION_NODE;
    }

    /**
     * {@inheritDoc}
     *
     * Returns the target
     */
    @Override
    public String getNodeName() {
        return target_;
    }

    /**
     * {@inheritDoc}
     *
     * A PI's "target" states what processor channel the PI's data should be
     * directed to. It is defined differently in HTML and XML.
     * <p>
     * In XML, a PI's "target" is the first (whitespace-delimited) token following
     * the "&lt;?" token that begins the PI.
     * <p>
     * In HTML, target is always null.
     * <p>
     * Note that getNodeName is aliased to getTarget.
     */
    @Override
    public String getTarget() {
        return target_;

    }

    /**
     * {@inheritDoc}
     *
     * Returns the absolute base URI of this node or null if the implementation
     * wasn't able to obtain an absolute URI. Note: If the URI is malformed, a null
     * is returned.
     *
     * @return The absolute base URI of this node or null.
     */
    @Override
    public String getBaseURI() {
        return ownerNode_.getBaseURI();
    }
}
