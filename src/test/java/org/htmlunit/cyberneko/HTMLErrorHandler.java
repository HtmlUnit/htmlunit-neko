package org.htmlunit.cyberneko;

import java.io.IOException;

import org.htmlunit.cyberneko.xerces.util.DefaultErrorHandler;
import org.htmlunit.cyberneko.xerces.xni.XNIException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLParseException;

/**
 * Error handler for test purposes: just logs the errors to the provided PrintWriter.
 * @author Marc Guillemot
 */
class HTMLErrorHandler extends DefaultErrorHandler {
    private final java.io.Writer out_;

    public HTMLErrorHandler(final java.io.Writer out) {
        out_ = out;
    }


    /** @see DefaultErrorHandler#error(String,String,XMLParseException) */
    @Override
    public void error(final String domain, final String key,
            final XMLParseException exception) throws XNIException {
        println("Err", key, exception);
    }

    private void println(final String type, String key, XMLParseException exception) throws XNIException {
        try {
            out_.append("[").append(type).append("] ").append(key).append(" ").append(exception.getMessage()).append("\n");
        }
        catch (final IOException e) {
            throw new XNIException(e);
        }
    }

    /** @see DefaultErrorHandler#warning(String,String,XMLParseException) */
    @Override
    public void warning(final String domain, final String key,
            final XMLParseException exception) throws XNIException {
        println("Warn", key, exception);
    }
}
