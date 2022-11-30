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

package net.sourceforge.htmlunit.xerces.impl.xs.opti;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


/**
 * @xerces.internal
 *
 * @author Rahul Srivastava, Sun Microsystems Inc.
 *
 * @version $Id$
 */
public class NamedNodeMapImpl implements NamedNodeMap {

    final Attr[] attrs;

    public NamedNodeMapImpl(Attr[] attrs) {
        this.attrs = attrs;
    }

    @Override
    public Node getNamedItem(String name) {
        for (Attr attr : attrs) {
            if (attr.getName().equals(name)) {
                return attr;
            }
        }
            return null;
    }

    @Override
    public Node item(int index) {
        if (index < 0 && index > getLength()) {
            return null;
        }
        return attrs[index];
    }

    @Override
    public int getLength() {
        return attrs.length;
    }

    @Override
    public Node getNamedItemNS(String namespaceURI, String localName) {
        for (Attr attr : attrs) {
            if (attr.getName().equals(localName) && attr.getNamespaceURI().equals(namespaceURI)) {
                return attr;
            }
        }
            return null;
    }

    @Override
    public Node setNamedItemNS(Node arg) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Method not supported");
    }

    @Override
    public Node setNamedItem(Node arg) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Method not supported");
    }

    @Override
    public Node removeNamedItem(String name) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Method not supported");
    }

    @Override
    public Node removeNamedItemNS(String namespaceURI, String localName) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Method not supported");
    }
}