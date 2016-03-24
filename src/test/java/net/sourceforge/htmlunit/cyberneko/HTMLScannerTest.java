package net.sourceforge.htmlunit.cyberneko;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

import org.apache.xerces.util.XMLStringBuffer;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import net.sourceforge.htmlunit.cyberneko.filters.DefaultFilter;

/**
 * Unit tests for {@link HTMLScanner}.
 * @author Marc Guillemot
 * @author Ahmed Ashour
 * @version $Id: HTMLScanner.java,v 1.19 2005/06/14 05:52:37 andyc Exp $
 */
public class HTMLScannerTest extends TestCase {

	public void testisEncodingCompatible() throws Exception {
		final HTMLScanner scanner = new HTMLScanner();
		assertTrue(scanner.isEncodingCompatible("ISO-8859-1","ISO-8859-1"));
		assertTrue(scanner.isEncodingCompatible("UTF-8","UTF-8"));
		assertTrue(scanner.isEncodingCompatible("UTF-16","UTF-16"));
		assertTrue(scanner.isEncodingCompatible("US-ASCII","ISO-8859-1"));
		assertTrue(scanner.isEncodingCompatible("UTF-8","ISO-8859-1"));

		assertFalse(scanner.isEncodingCompatible("UTF-8","UTF-16"));
		assertFalse(scanner.isEncodingCompatible("ISO-8859-1","UTF-16"));
		assertFalse(scanner.isEncodingCompatible("UTF-16","Cp1252"));
	}

	public void testEvaluateInputSource() throws Exception {
	    String string = "<html><head><title>foo</title></head>"
	        + "<body>"
	        + "<script id='myscript'>"
	        + "  document.write('<style type=\"text/css\" id=\"myStyle\">');"
            + "  document.write('  .nwr {white-space: nowrap;}');"
            + "  document.write('</style>');"
            + "  document.write('<div id=\"myDiv\"><span></span>');"
            + "  document.write('</div>');"
            + "</script>"
            + "<div><a/></div>"
            + "</body></html>";
        HTMLConfiguration parser = new HTMLConfiguration();
        EvaluateInputSourceFilter filter = new EvaluateInputSourceFilter(parser);
        parser.setProperty("http://cyberneko.org/html/properties/filters", new XMLDocumentFilter[] {filter});
        XMLInputSource source = new XMLInputSource(null, "myTest", null, new StringReader(string), "UTF-8");
        parser.parse(source);
        
        String[] expectedString = {"(HTML", "(HEAD", "(TITLE", ")TITLE", ")HEAD", "(BODY", "(SCRIPT",
            ")SCRIPT", "~inserting", "(STYLE", "~inserting", "~inserting", ")STYLE", "~inserting",
            "(DIV", "(SPAN", ")SPAN", "~inserting", ")DIV", "(DIV", "(A", ")A", ")DIV", ")BODY", ")HTML"};
        assertEquals(Arrays.asList(expectedString), filter.collectedStrings);
    }

	/**
	 * Ensure that the current locale doesn't affect the HTML tags.
	 * see issue https://sourceforge.net/tracker/?func=detail&atid=952178&aid=3544334&group_id=195122
	 * @throws Exception
	 */
	public void testLocale() throws Exception {
		final Locale originalLocale = Locale.getDefault();
		try {
			Locale.setDefault(new Locale("tr", "TR"));
		    String string = "<html><head><title>foo</title></head>"
		        + "<body>"
	            + "</body></html>";
	        HTMLConfiguration parser = new HTMLConfiguration();
	        EvaluateInputSourceFilter filter = new EvaluateInputSourceFilter(parser);
	        parser.setProperty("http://cyberneko.org/html/properties/filters", new XMLDocumentFilter[] {filter});
	        XMLInputSource source = new XMLInputSource(null, "myTest", null, new StringReader(string), "UTF-8");
	        parser.parse(source);
	        
	        String[] expectedString = {"(HTML", "(HEAD", "(TITLE", ")TITLE", ")HEAD", "(BODY", ")BODY", ")HTML"};
	        assertEquals(Arrays.asList(expectedString).toString(), filter.collectedStrings.toString());
		}
		finally {
			Locale.setDefault(originalLocale);
		}
    }

	/**
	 * Tests handling of xml declaration when used with Reader.
	 * Following test caused NPE with release 1.9.11.
	 * Regression test for [ 2503982 ] NPE when parsing from a CharacterStream
	 */
	public void testChangeEncodingWithReader() throws Exception {
	    String string = "<?xml version='1.0' encoding='UTF-8'?><html><head><title>foo</title></head>"
            + "</body></html>";

        XMLInputSource source = new XMLInputSource(null, "myTest", null, new StringReader(string), "ISO8859-1");
	    HTMLConfiguration parser = new HTMLConfiguration();
        parser.parse(source);
    }

	private static class EvaluateInputSourceFilter extends DefaultFilter {

       private List collectedStrings = new ArrayList();
       private static int counter = 1;
       protected HTMLConfiguration fConfiguration;

       public EvaluateInputSourceFilter(HTMLConfiguration config) {
           fConfiguration = config;
       }

       public void startElement(QName element, XMLAttributes attrs, Augmentations augs) throws XNIException {
           collectedStrings.add("(" + element.rawname);
       }

       public void endElement(QName element, Augmentations augs) throws XNIException {
           collectedStrings.add(")" + element.rawname);
           if (element.localpart.equals("SCRIPT")) {
        	   // act as if evaluation of document.write would insert the content
               insert("<style type=\"text/css\" id=\"myStyle\">");
               insert("  .nwr {white-space: nowrap;}");
               insert("</style>");
               insert("<div id=\"myDiv\"><span></span>");
               insert("</div>");
           }
       }

        private void insert(final String string) {
            collectedStrings.add("~inserting");
            XMLInputSource source = new XMLInputSource(null, "myTest" + counter++, null,
                                                      new StringReader(string), "UTF-8");
            fConfiguration.evaluateInputSource(source);
       }

   }

	public void testReduceToContent() throws Exception {
		XMLStringBuffer buffer = new XMLStringBuffer("<!-- hello-->");
		
		HTMLScanner.reduceToContent(buffer, "<!--", "-->");
		assertEquals(" hello", buffer.toString());

		buffer = new XMLStringBuffer("  \n <!-- hello-->\n");
		HTMLScanner.reduceToContent(buffer, "<!--", "-->");
		assertEquals(" hello", buffer.toString());

		buffer = new XMLStringBuffer("hello");
		HTMLScanner.reduceToContent(buffer, "<!--", "-->");
		assertEquals("hello", buffer.toString());

		buffer = new XMLStringBuffer("<!-- hello");
		HTMLScanner.reduceToContent(buffer, "<!--", "-->");
		assertEquals("<!-- hello", buffer.toString());

		buffer = new XMLStringBuffer("<!--->");
		HTMLScanner.reduceToContent(buffer, "<!--", "-->");
		assertEquals("<!--->", buffer.toString());
	}

	/**
	 * Regression test for bug 2933989.
	 * @throws Exception
	 */
    public void testInfiniteLoop() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<html>\n");
        for (int x = 0; x <= 2005; x++) {
            buffer.append((char) (x % 10 + '0'));
        }
        
        buffer.append("\n<noframes>- Generated in 1<1ms -->");

        XMLParserConfiguration parser = new HTMLConfiguration() {
            protected HTMLScanner createDocumentScanner() {
                return new InfiniteLoopScanner();
            }
        };
        XMLInputSource source = new XMLInputSource(null, "myTest", null, new StringReader(buffer.toString()), "UTF-8");
        parser.parse(source);
    }

    static class InfiniteLoopScanner extends HTMLScanner {
        InfiniteLoopScanner() {
            fContentScanner = new MyContentScanner();
        }

        class MyContentScanner extends HTMLScanner.ContentScanner {

            protected void scanComment() throws IOException {
            	// bug was here: calling nextContent() at the end of the buffer/input
            	nextContent(30);
            	super.scanComment();
            }
        }
    }
}
