package org.htmlunit.cyberneko;

import java.io.IOException;
import java.io.Reader;

import org.htmlunit.cyberneko.xerces.xni.XMLString;

/**
 * Current entity.
 */
final class HTMLScannerBufferedReader {

    /** Character stream. */
    private Reader reader_;

    /** Encoding. */
    String encoding_;

    /** Line number. */
    private int lineNumber_ = 1;

    /** Column number. */
    private int columnNumber_ = 1;

    /** Character offset in the file. */
    private int characterOffset_ = 0;

    // buffer

    /** Character buffer. */
    char[] buffer_;

    /** Offset into character buffer. */
    int offset_ = 0;

    /** Length of characters read into character buffer. */
    int length_ = 0;

    private boolean endReached_ = false;

    // Constructs an entity from the specified stream.
    HTMLScannerBufferedReader(final Reader reader, final int readerBufferSize, final String encoding) {
        reader_ = reader;
        buffer_ = new char[readerBufferSize];
        encoding_ = encoding;
    }

    char getCurrentChar() {
        return buffer_[offset_];
    }

    /**
     * @return the current character and moves to next one.
     */
    char getNextChar() {
        characterOffset_++;
        columnNumber_++;
        return buffer_[offset_++];
    }

    void closeQuietly() {
        try {
            reader_.close();
        }
        catch (final IOException e) {
            // ignore
        }
    }

    /**
     * Indicates if there are characters left.
     */
    boolean hasNext() {
        return offset_ < length_;
    }

    /**
     * Loads a new chunk of data into the buffer and returns the number of
     * characters loaded or -1 if no additional characters were loaded.
     *
     * @param loadOffset The offset at which new characters should be loaded.
     * @return count
     * @throws IOException in case of io problems
     */
    int load(final int loadOffset) throws IOException {
        if (HTMLScanner.DEBUG_BUFFER) {
            debugBufferIfNeeded("(load: ");
        }
        // resize buffer, if needed
        if (loadOffset == buffer_.length) {
            final int adjust = buffer_.length / 4;
            final char[] array = new char[buffer_.length + adjust];
            System.arraycopy(buffer_, 0, array, 0, length_);
            buffer_ = array;
        }
        // read a block of characters
        final int count = reader_.read(buffer_, loadOffset, buffer_.length - loadOffset);
        if (count == -1) {
            length_ = loadOffset;
            endReached_ = true;
        }
        else {
            length_ = count + loadOffset;
        }
        offset_ = loadOffset;
        if (HTMLScanner.DEBUG_BUFFER) {
            debugBufferIfNeeded(")load: ", " -> " + count);
        }
        return count;
    }

    int loadWholeBuffer() throws IOException {
        if (HTMLScanner.DEBUG_BUFFER) {
            debugBufferIfNeeded("(loadWholeBuffer: ");
        }
        // read a block of characters
        final int count = reader_.read(buffer_, 0, buffer_.length);
        if (count == -1) {
            length_ = 0;
            endReached_ = true;
        }
        else {
            length_ = count;
        }
        offset_ = 0;
        if (HTMLScanner.DEBUG_BUFFER) {
            debugBufferIfNeeded(")loadWholeBuffer: ", " -> " + count);
        }
        return count;
    }

    // Reads a single character.
    int read() throws IOException {
        if (HTMLScanner.DEBUG_BUFFER) {
            debugBufferIfNeeded("(read: ");
        }

        if (offset_ == length_) {
            if (endReached_) {
                return -1;
            }
            if (loadWholeBuffer() == -1) {
                if (HTMLScanner.DEBUG_BUFFER) {
                    System.out.println(")read: -> -1");
                }
                return -1;
            }
        }
        final char c = buffer_[offset_];
        offset_++;
        characterOffset_++;
        columnNumber_++;

        if (HTMLScanner.DEBUG_BUFFER) {
            debugBufferIfNeeded(")read: ", " -> " + c);
        }

        return c;
    }

    /**
     * Reads the next characters WITHOUT impacting the buffer content up to current
     * offset.
     *
     * @param len the number of characters to read
     * @return the read string (length may be smaller if EOF is encountered)
     * @throws IOException in case of io problems
     */
    protected String nextContent(final int len) throws IOException {
        final int originalOffset = offset_;
        final int originalColumnNumber = getColumnNumber();
        final int originalCharacterOffset = getCharacterOffset();

        final char[] buff = new char[len];
        int nbRead;
        for (nbRead = 0; nbRead < len; ++nbRead) {
            // load(length_) should not clear the buffer
            if (offset_ == length_) {
                if (load(length_) == -1) {
                    break;
                }
            }

            final int c = read();
            if (c == -1) {
                break;
            }
            buff[nbRead] = (char) c;
        }

        // restore position
        offset_ = originalOffset;
        columnNumber_ = originalColumnNumber;
        characterOffset_ = originalCharacterOffset;

        return new String(buff, 0, nbRead);
    }

    // Reads a single character, preserving the old buffer content
    protected int readPreservingBufferContent() throws IOException {
        if (HTMLScanner.DEBUG_BUFFER) {
            debugBufferIfNeeded("(readPreserving: ");
        }
        if (offset_ == length_) {
            if (load(length_) == -1) {
                if (HTMLScanner.DEBUG_BUFFER) {
                    System.out.println(")readPreserving: -> -1");
                }
                return -1;
            }
        }
        final char c = getNextChar();
        if (HTMLScanner.DEBUG_BUFFER) {
            debugBufferIfNeeded(")readPreserving: ", " -> " + c);
        }
        return c;
    }

    /** Prints the contents of the character buffer to standard out. */
    void debugBufferIfNeeded(final String prefix) {
        debugBufferIfNeeded(prefix, "");
    }

    /** Prints the contents of the character buffer to standard out. */
    void debugBufferIfNeeded(final String prefix, final String suffix) {
        System.out.print(prefix);
        System.out.print('[');
        System.out.print(length_);
        System.out.print(' ');
        System.out.print(offset_);
        if (length_ > 0) {
            System.out.print(" \"");
            for (int i = 0; i < length_; i++) {
                if (i == offset_) {
                    System.out.print('^');
                }
                final char c = buffer_[i];
                switch (c) {
                    case '\r':
                        System.out.print("\\r");
                        break;
                    case '\n':
                        System.out.print("\\n");
                        break;
                    case '\t':
                        System.out.print("\\t");
                        break;
                    case '"':
                        System.out.print("\\\"");
                        break;
                    default:
                        System.out.print(c);
                }
            }
            if (offset_ == length_) {
                System.out.print('^');
            }
            System.out.print('"');
        }
        System.out.print(']');
        System.out.print(suffix);
        System.out.println();
    }

    void setStream(final Reader inputStreamReader, final String encoding) {
        reader_ = inputStreamReader;
        offset_ = 0;
        length_ = 0;
        characterOffset_ = 0;
        lineNumber_ = 1;
        columnNumber_ = 1;
        encoding_ = encoding;
    }

    /**
     * Goes back, canceling the effect of the previous read() call.
     */
    void rewind() {
        offset_--;
        characterOffset_--;
        columnNumber_--;
    }

    void rewind(final int i) {
        offset_ -= i;
        characterOffset_ -= i;
        columnNumber_ -= i;
    }

    void incLine() {
        lineNumber_++;
        columnNumber_ = 1;
    }

    void incLine(final int nbLines) {
        lineNumber_ += nbLines;
        columnNumber_ = 1;
    }

    public int getLineNumber() {
        return lineNumber_;
    }

    void resetBuffer(final XMLString xmlBuffer, final int lineNumber, final int columnNumber,
            final int characterOffset) {
        lineNumber_ = lineNumber;
        columnNumber_ = columnNumber;
        characterOffset_ = characterOffset;

        // TODO RBRi
        buffer_ = xmlBuffer.getChars();
        offset_ = 0;
        length_ = xmlBuffer.length();
    }

    int getColumnNumber() {
        return columnNumber_;
    }

    int getCharacterOffset() {
        return characterOffset_;
    }

    // Returns true if the specified text is present (case-insensitive) and is skipped.
    // for performance reasons you have to provide the specified text in uppercase
    protected boolean skip(final String expectedInUpperCase) throws IOException {
        final int length = expectedInUpperCase.length();
        for (int i = 0; i < length; i++) {
            if (offset_ == length_) {
                // preserve for rewind
                System.arraycopy(buffer_, offset_ - i, buffer_, 0, i);
                if (load(i) == -1) {
                    offset_ = 0;
                    return false;
                }
            }
            final char c0 = expectedInUpperCase.charAt(i);
            final char c1 = Character.toUpperCase(getNextChar());
            if (c0 != c1) {
                rewind(i + 1);
                return false;
            }
        }
        return true;
    }

    // Skips markup.
    protected boolean skipMarkup(final boolean balance) throws IOException {
        if (HTMLScanner.DEBUG_BUFFER) {
            debugBufferIfNeeded("(skipMarkup: ");
        }
        int depth = 1;
        boolean slashgt = false;
        OUTER: while (true) {
            if (offset_ == length_) {
                if (loadWholeBuffer() == -1) {
                    break OUTER;
                }
            }
            while (hasNext()) {
                char c = getNextChar();
                if (balance && c == '<') {
                    depth++;
                }
                else if (c == '>') {
                    depth--;
                    if (depth == 0) {
                        break OUTER;
                    }
                }
                else if (c == '/') {
                    if (offset_ == length_) {
                        if (loadWholeBuffer() == -1) {
                            break OUTER;
                        }
                    }
                    c = getNextChar();
                    if (c == '>') {
                        slashgt = true;
                        depth--;
                        if (depth == 0) {
                            break OUTER;
                        }
                    }
                    else {
                        rewind();
                    }
                }
                else if (c == '\r' || c == '\n') {
                    rewind();
                    skipNewlines();
                }
            }
        }
        if (HTMLScanner.DEBUG_BUFFER) {
            debugBufferIfNeeded(")skipMarkup: ", " -> " + slashgt);
        }
        return slashgt;
    }

    // Skips whitespace.
    protected boolean skipSpaces() throws IOException {
        if (HTMLScanner.DEBUG_BUFFER) {
            debugBufferIfNeeded("(skipSpaces: ");
        }
        boolean spaces = false;
        while (true) {
            if (offset_ == length_) {
                if (loadWholeBuffer() == -1) {
                    break;
                }
            }

            final char c = getNextChar();
            // compare against the usual suspects first before going
            // the expensive route
            // unix \n might dominate
            if (c == '\n' || c == '\r') {
                spaces = true;
                rewind();
                skipNewlines();
            }
            else if (Character.isWhitespace(c)) {
                spaces = true;
            }
            else {
                rewind();
                break;
            }
        }
        if (HTMLScanner.DEBUG_BUFFER) {
            debugBufferIfNeeded(")skipSpaces: ", " -> " + spaces);
        }
        return spaces;
    }

    // Skips newlines and returns the number of newlines skipped.
    protected int skipNewlines() throws IOException {
        if (HTMLScanner.DEBUG_BUFFER) {
            debugBufferIfNeeded("(skipNewlines: ");
        }

        if (offset_ == length_) {
            if (loadWholeBuffer() == -1) {
                return 0;
            }
        }

        char c = getCurrentChar();
        int newlines = 0;
        if (c == '\n' || c == '\r') {
            do {
                c = getNextChar();
                if (c == '\n') {
                    newlines++;
                    if (offset_ == length_) {
                        offset_ = newlines;
                        if (load(newlines) == -1) {
                            break;
                        }
                    }
                }
                else if (c == '\r') {
                    newlines++;
                    if (offset_ == length_) {
                        offset_ = newlines;
                        if (load(newlines) == -1) {
                            break;
                        }
                    }
                    if (getCurrentChar() == '\n') {
                        offset_++;
                        characterOffset_++;
                    }
                }
                else {
                    rewind();
                    break;
                }
            }
            while (offset_ < length_ - 1);
            incLine(newlines);
        }
        if (HTMLScanner.DEBUG_BUFFER) {
            debugBufferIfNeeded(")skipNewlines: ", " -> " + newlines);
        }
        return newlines;
    }
}