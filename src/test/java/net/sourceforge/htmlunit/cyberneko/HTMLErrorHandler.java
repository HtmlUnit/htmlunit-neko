package net.sourceforge.htmlunit.cyberneko;

import java.io.IOException;

import org.apache.xerces.util.DefaultErrorHandler;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLParseException;

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
    public void error(final String domain, final String key,
            final XMLParseException exception) throws XNIException {
    	println("Err", key, exception);
    }

    private void println(final String type, String key, XMLParseException exception) throws XNIException {
    	try {
			out_.append("[" + type + "] "
					+ key + " " + exception.getMessage() + "\n");
		}
    	catch (final IOException e) {
    		throw new XNIException(e);
		}
	}

	/** @see DefaultErrorHandler#warning(String,String,XMLParseException) */
    public void warning(final String domain, final String key,
            final XMLParseException exception) throws XNIException {
    	println("Warn", key, exception);
    }
}
