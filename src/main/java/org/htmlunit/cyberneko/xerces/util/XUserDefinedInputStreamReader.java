/*
 * Copyright (c) 2017-2024 Ronald Brill
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
package org.htmlunit.cyberneko.xerces.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Reader backed by an InputStream that uses the x-user-defined encoding.
 * see https://encoding.spec.whatwg.org/#x-user-defined.
 *
 * @author Ronald Brill
 */
public final class XUserDefinedInputStreamReader extends Reader {

    /**
     * https://encoding.spec.whatwg.org/#x-user-defined
     */
    public static final String USER_DEFINED = "x-user-defined";

    private final InputStream inputStream_;

    public XUserDefinedInputStreamReader(final InputStream inputStream) {
        inputStream_ = inputStream;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        synchronized (lock) {
            final byte[] bytes = new byte[len];
            final int count = inputStream_.read(bytes, 0, len);

            int offset = off;
            for (int i = 0; i < count; i++) {
                final byte b = bytes[i];
                if (b >= 0) {
                    cbuf[offset] = (char) b;
                }
                else {
                    cbuf[offset] = (char) (0xF700 + (b & 0xFF));
                }
                offset++;
            }
            return count;
        }
    }

    @Override
    public void close() throws IOException {
        inputStream_.close();
    }
}
