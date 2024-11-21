/*
 * Copyright (c) 2017-2024 Ronald Brill
 * Copyright 2023 René Schwietzke
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
package org.htmlunit.cyberneko.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * A playback input stream. This class has the ability to save the bytes read
 * from the underlying input stream and play the bytes back later. This class is
 * used by the HTML scanner to switch encodings when a &lt;meta&gt; tag is
 * detected that specifies a different encoding.
 * <p>
 * If the encoding is changed, then the scanner calls the <code>playback</code>
 * method and re-scans the beginning of the HTML document again. This should not
 * be too much of a performance problem because the &lt;meta&gt; tag appears at
 * the beginning of the document.
 * <p>
 * If the &lt;body&gt; tag is reached without playing back the bytes, then the
 * buffer can be cleared by calling the <code>clear</code> method. This stops
 * the buffering of bytes and allows the memory used by the buffer to be
 * reclaimed.
 * <p>
 * <strong>Note:</strong> If the buffer is never played back or cleared, this
 * input stream will continue to buffer the entire stream. Therefore, it is very
 * important to use this stream correctly.
 *
 * @author Andy Clark
 */
public final class PlaybackInputStream extends InputStream {

    /** Set to true to debug playback. */
    private static final boolean DEBUG_PLAYBACK = false;

    /** Playback mode. */
    private boolean playback_ = false;

    /** Buffer cleared. */
    private boolean cleared_ = false;

    /** Encoding detected. */
    private boolean detected_ = false;

    // buffer info

    /** Byte buffer. */
    private byte[] byteBuffer_ = new byte[1024];

    /** Offset into byte buffer during playback. */
    private int byteOffset_ = 0;

    /** Length of bytes read into byte buffer. */
    private int byteLength_ = 0;

    /** Pushback offset. */
    private int pushbackOffset_ = 0;

    /** Pushback length. */
    private int pushbackLength_ = 0;

    /** Our inputstream */
    private final InputStream in_;

    // Constructor.
    public PlaybackInputStream(final InputStream inputStream) {
        in_ = inputStream;
    }

    // Detect encoding.
    public void detectEncoding(final String[] encodings) throws IOException {
        if (detected_) {
            throw new IOException("Should not detect encoding twice.");
        }
        detected_ = true;

        final int b1 = read();
        if (b1 == -1) {
            return;
        }
        final int b2 = read();
        if (b2 == -1) {
            pushbackLength_ = 1;
            return;
        }
        // UTF-8 BOM: 0xEFBBBF
        if (b1 == 0xEF && b2 == 0xBB) {
            final int b3 = read();
            if (b3 == 0xBF) {
                pushbackOffset_ = 3;
                encodings[0] = "UTF-8";
                encodings[1] = "UTF8";
                return;
            }
            pushbackLength_ = 3;
        }

        // UTF-16 LE BOM: 0xFFFE
        else if (b1 == 0xFF && b2 == 0xFE) {
            pushbackOffset_ = 2;
            encodings[0] = "UTF-16";
            encodings[1] = "UnicodeLittleUnmarked";
            return;
        }

        // UTF-16 BE BOM: 0xFEFF
        else if (b1 == 0xFE && b2 == 0xFF) {
            pushbackOffset_ = 2;
            encodings[0] = "UTF-16";
            encodings[1] = "UnicodeBigUnmarked";
            return;
        }
        else {
            // unknown
            pushbackLength_ = 2;
        }
    }

    /** Playback buffer contents. */
    public void playback() {
        playback_ = true;
    }

    /**
     * Clears the buffer.
     * <p>
     * <strong>Note:</strong> The buffer cannot be cleared during playback.
     * Therefore, calling this method during playback will not do anything. However,
     * the buffer will be cleared automatically at the end of playback.
     */
    public void clear() {
        if (!playback_) {
            cleared_ = true;
            byteBuffer_ = null;
        }
    }

    /** Read a byte. */
    @Override
    public int read() throws IOException {
        if (DEBUG_PLAYBACK) {
            System.out.println("(read");
        }
        // this should be the normal state, hence we do that first
        if (cleared_) {
            return in_.read();
        }
        if (pushbackOffset_ < pushbackLength_) {
            return byteBuffer_[pushbackOffset_++];
        }
        if (playback_) {
            final int c = byteBuffer_[byteOffset_++];
            if (byteOffset_ == byteLength_) {
                cleared_ = true;
                byteBuffer_ = null;
            }
            if (DEBUG_PLAYBACK) {
                System.out.println(")read -> " + (char) c);
            }
            return c;
        }
        final int c = in_.read();
        if (c != -1) {
            if (byteLength_ == byteBuffer_.length) {
                final byte[] newarray = new byte[byteLength_ + 1024];
                System.arraycopy(byteBuffer_, 0, newarray, 0, byteLength_);
                byteBuffer_ = newarray;
            }
            byteBuffer_[byteLength_++] = (byte) c;
        }
        if (DEBUG_PLAYBACK) {
            System.out.println(")read -> " + (char) c);
        }
        return c;
    }

    /** Read an array of bytes. */
    @Override
    public int read(final byte[] array) throws IOException {
        return read(array, 0, array.length);
    }

    /** Read an array of bytes. */
    @Override
    public int read(final byte[] array, final int offset, int length) throws IOException {
        if (DEBUG_PLAYBACK) {
            System.out.println(")read(" + offset + ',' + length + ')');
        }
        // this should be the normal state, hence we do that first
        if (cleared_) {
            return in_.read(array, offset, length);
        }
        if (pushbackOffset_ < pushbackLength_) {
            int count = pushbackLength_ - pushbackOffset_;
            if (count > length) {
                count = length;
            }
            System.arraycopy(byteBuffer_, pushbackOffset_, array, offset, count);
            pushbackOffset_ += count;
            return count;
        }
        if (playback_) {
            if (byteOffset_ + length > byteLength_) {
                length = byteLength_ - byteOffset_;
            }
            System.arraycopy(byteBuffer_, byteOffset_, array, offset, length);
            byteOffset_ += length;
            if (byteOffset_ == byteLength_) {
                cleared_ = true;
                byteBuffer_ = null;
            }
            return length;
        }
        final int count = in_.read(array, offset, length);
        if (count != -1) {
            if (byteLength_ + count > byteBuffer_.length) {
                final byte[] newarray = new byte[byteLength_ + count + 512];
                System.arraycopy(byteBuffer_, 0, newarray, 0, byteLength_);
                byteBuffer_ = newarray;
            }
            System.arraycopy(array, offset, byteBuffer_, byteLength_, count);
            byteLength_ += count;
        }
        if (DEBUG_PLAYBACK) {
            System.out.println(")read(" + offset + ',' + length + ") -> " + count);
        }
        return count;
    }
}
