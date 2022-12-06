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

package net.sourceforge.htmlunit.xerces.dom;

/**
 * XML provides the CDATA markup to allow a region of text in which
 * most of the XML delimiter recognition does not take place. This is
 * intended to ease the task of quoting XML fragments and other
 * programmatic information in a document's text without needing to
 * escape these special characters. It's primarily a convenience feature
 * for those who are hand-editing XML.
 * <P>
 * CDATASection is an Extended DOM feature, and is not used in HTML
 * contexts.
 * <P>
 * Within the DOM, CDATASections are treated essentially as Text
 * blocks. Their distinct type is retained in order to allow us to
 * properly recreate the XML syntax when we write them out.
 * <P>
 * Reminder: CDATA IS NOT A COMPLETELY GENERAL SOLUTION; it can't
 * quote its own end-of-block marking. If you need to write out a
 * CDATA that contains the ]]&gt; sequence, it's your responsibility to
 * split that string over two successive CDATAs at that time.
 * <P>
 * CDATA does not participate in Element.normalize() processing.
 */
public class DeferredCDATASectionImpl
    extends CDATASectionImpl
    implements DeferredNode {

    /** Serialization version. */
    static final long serialVersionUID = 1983580632355645726L;

    /** Node index. */
    protected final transient int fNodeIndex;

    /**
     * This is the deferred constructor. Only the fNodeIndex is given here. All other data,
     * can be requested from the ownerDocument via the index.
     * @param ownerDocument the ownerDocument
     * @param nodeIndex the index
     */
    DeferredCDATASectionImpl(DeferredDocumentImpl ownerDocument, int nodeIndex) {
        super(ownerDocument, null);

        fNodeIndex = nodeIndex;
        needsSyncData(true);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNodeIndex() {
        return fNodeIndex;
    }

    /**
     * {@inheritDoc}
     *
     *  Synchronizes the data (name and value) for fast nodes.
     */
    @Override
    protected void synchronizeData() {

        // no need to sync in the future
        needsSyncData(false);

        // fluff data
        DeferredDocumentImpl ownerDocument =
            (DeferredDocumentImpl) this.ownerDocument();
        data = ownerDocument.getNodeValueString(fNodeIndex);
    }
}
