package net.sourceforge.htmlunit.cyberneko;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;

import org.junit.jupiter.api.Test;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.html.HTMLDocument;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import net.sourceforge.htmlunit.cyberneko.html.dom.HTMLDocumentImpl;
import net.sourceforge.htmlunit.cyberneko.parsers.DOMFragmentParser;

/**
 * Unit tests for {@link DOMFragmentParser}.
 * @author Marc Guillemot
 * @author Ronald Brill
 *
 */
public class DOMFragmentParserTest {
    /**
     * See <a href="https://sourceforge.net/p/nekohtml/bugs/154/">Bug 154</a>.
     */
    @Test
    public void attrEndingWithCRAtEndOfStream() {
        // TODO doTest("<a href=\"\r", "<A href=\"&#xa;\"/>");
    }

    /**
     * See <a href="http://sourceforge.net/support/tracker.php?aid=2828553">Bug 2828553</a>.
     */
    @Test
    public void invalidProcessingInstruction() throws Exception {
        doTest("<html><?9 ?></html>", "<HTML/>");
    }

    /**
     * See <a href="http://sourceforge.net/support/tracker.php?aid=2828534">Bug 2828534</a>.
     */
    @Test
    public void invalidAttributeName() throws Exception {
        doTest("<html 9='id'></html>", "<HTML/>");
    }

    private static void doTest(final String html, final String expected) throws Exception {
        final DOMFragmentParser parser = new DOMFragmentParser();
        final HTMLDocument document = new HTMLDocumentImpl();

        final DocumentFragment fragment = document.createDocumentFragment();
        final InputSource source = new InputSource(new StringReader(html));
        parser.parse(source, fragment);
//        final OutputFormat of = new OutputFormat();
//        of.setOmitXMLDeclaration(true);
//        XMLSerializer s = new XMLSerializer(of);
//        StringWriter sw = new StringWriter();
//        s.setOutputCharStream(sw);
//        s.serialize(fragment);
        final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();

        final DOMImplementationLS impl =
            (DOMImplementationLS)registry.getDOMImplementation("LS");

        final LSSerializer writer = impl.createLSSerializer();
        String str = writer.writeToString(fragment);
        str = str.replace("\r", "").replace("\n", "");

        final String xmlDecl = "<?xml version=\"1.0\" encoding=\"UTF-16\"?>";
        assertEquals(xmlDecl + expected, str);
    }

    public static void print(Node node, String indent) {
        System.out.println(indent+node.getClass().getName());
        Node child = node.getFirstChild();
        while (child != null) {
            print(child, indent+" ");
            child = child.getNextSibling();
        }
    }

    /**
     * HTMLTagBalancer field fSeenBodyElementEnd was not correctly reset as of 1.19.17
     * @throws Exception on error
     */
    @Test
    public void instanceReuse() throws Exception {
        final String s = "<html><body><frame><frameset></frameset></html>";

        final DOMFragmentParser parser = new DOMFragmentParser();
        final HTMLDocument document = new HTMLDocumentImpl();

        final DocumentFragment fragment1 = document.createDocumentFragment();
        parser.parse(new InputSource(new StringReader(s)), fragment1);

        final DocumentFragment fragment2 = document.createDocumentFragment();
        parser.parse(new InputSource(new StringReader(s)), fragment2);

        final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        final DOMImplementationLS impl =
            (DOMImplementationLS)registry.getDOMImplementation("LS");

        final LSSerializer writer = impl.createLSSerializer();
        final String str1 = writer.writeToString(fragment1);
        final String str2 = writer.writeToString(fragment2);
        assertEquals(str1, str2);
    }
}

