package org.htmlunit.cyberneko;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.htmlunit.cyberneko.HTMLConfiguration;
import org.htmlunit.cyberneko.HTMLTagBalancingListener;
import org.htmlunit.cyberneko.xerces.parsers.AbstractSAXParser;
import org.htmlunit.cyberneko.xerces.xni.Augmentations;
import org.htmlunit.cyberneko.xerces.xni.QName;
import org.htmlunit.cyberneko.xerces.xni.XMLAttributes;
import org.htmlunit.cyberneko.xerces.xni.XNIException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLInputSource;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link HTMLTagBalancingListener}.
 * @author Marc Guillemot
 * @author Ronald Brill
 */
public class HTMLTagBalancingListenerTest {

   @Test
   public void ignoredTags() throws Exception {
       final String string = "<html><head><title>foo</title></head>"
           + "<body>"
           + "<body onload='alert(123)'>"
           + "<div>"
           + "<form action='foo'>"
           + "  <input name='text1'/>"
           + "</div>"
           + "</form>"
            + "</body></html>";

       final TestParser parser = new TestParser();
       final StringReader sr = new StringReader(string);
       final XMLInputSource in = new XMLInputSource(null, "foo", null, sr, null);

       parser.parse(in);

       final String[] expectedMessages = {"start html", "start head", "start title", "end title", "end head",
               "start body", "ignored start body",
               "start div", "start form", "start input", "end input", "end form",
               "end div", "ignored end form",
               "end body", "end html"};

       assertEquals(Arrays.asList(expectedMessages).toString(), parser.messages.toString());
    }

   /**
    * HTMLTagBalancer field fSeenFramesetElement was not correctly reset as of 1.19.17
    * @throws Exception on error
    */
   @Test
   public void reuse() throws Exception {
       final String string = "<head><title>title</title></head><body><div>hello</div></body>";

       final TestParser parser = new TestParser();
       final StringReader sr = new StringReader(string);
       final XMLInputSource in = new XMLInputSource(null, "foo", null, sr, null);

       parser.parse(in);

       final String[] expectedMessages = {"start HTML", "start head", "start title", "end title", "end head",
               "start body", "start div", "end div", "end body", "end HTML"};

       assertEquals(Arrays.asList(expectedMessages).toString(), parser.messages.toString());

       parser.messages.clear();
       parser.parse(new XMLInputSource(null, "foo", null, new StringReader(string), null));
       assertEquals(Arrays.asList(expectedMessages).toString(), parser.messages.toString());
    }
}

class TestParser extends AbstractSAXParser implements HTMLTagBalancingListener
{
    final List<String> messages = new ArrayList<>();
    TestParser() throws Exception
    {
        super(new HTMLConfiguration());
        setFeature("http://cyberneko.org/html/features/balance-tags/ignore-outside-content", true);
    }

    @Override
    public void startElement(QName element, XMLAttributes attributes,
            Augmentations augs) throws XNIException {

        messages.add("start " + element.rawname);
        super.startElement(element, attributes, augs);
    }

    @Override
    public void ignoredEndElement(QName element, Augmentations augs) {
        messages.add("ignored end " + element.rawname);
    }

    @Override
    public void ignoredStartElement(QName element, XMLAttributes attrs,
            Augmentations augs) {
        messages.add("ignored start " + element.rawname);
    }

    @Override
    public void endElement(QName element, Augmentations augs) throws XNIException {
        messages.add("end " + element.rawname);
        super.endElement(element, augs);
    }
}