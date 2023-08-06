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

import org.w3c.dom.Comment;
import org.w3c.dom.Node;

/**
 * Represents an XML (or HTML) comment.
 */
public class CommentImpl extends CharacterDataImpl implements Comment {

    /**
     * Factory constructor.
     *
     * @param ownerDoc the owner document
     * @param data     the data
     */
    public CommentImpl(CoreDocumentImpl ownerDoc, String data) {
        super(ownerDoc, data);
    }

    /**
     * {@inheritDoc}
     *
     * A short integer indicating what type of node this is. The named constants for
     * this value are defined in the org.w3c.dom.Node interface.
     */
    @Override
    public short getNodeType() {
        return Node.COMMENT_NODE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNodeName() {
        return "#comment";
    }
}
