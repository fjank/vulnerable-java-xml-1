package org.fjank.vulnerable_xml_1;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.StringReader;

public class Tester {
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, XMLStreamException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///C:\\Windows\\System32\\drivers\\etc\\\">]>" +
                "<root>" +
                "<test>&xxe;</test>" +
                "</root>";
        //System.out.println(parseDOM(xml));
        //System.out.println(parseSAX(xml));
        System.out.println(parseStAX(xml));
    }

    private static String parseStAX(String xml) throws XMLStreamException {
        boolean testElementActive = false;
        StringBuilder rv = new StringBuilder();
        XMLInputFactory f = XMLInputFactory.newInstance();
        f.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        XMLStreamReader r = f.createXMLStreamReader(new StringReader(xml));
        while (r.hasNext()) {
            r.next();
            if (r.getEventType() == XMLStreamConstants.START_ELEMENT && r.getName().getLocalPart().equals("test")) {
                testElementActive = true;
            }
            if (r.getEventType() == XMLStreamConstants.END_ELEMENT && r.getName().getLocalPart().equals("test")) {
                testElementActive = false;
            }
            if (r.getEventType()==XMLStreamConstants.CHARACTERS && testElementActive) {
                rv.append(r.getText());
            }
        }
        return rv.toString();
    }

    private static String parseSAX(String xml) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        SAXParser sp = spf.newSAXParser();
        XMLReader xmlReader = sp.getXMLReader();
        TestExtractor testExtractor = new TestExtractor();
        xmlReader.setContentHandler(testExtractor);
        xmlReader.parse(new InputSource(new StringReader(xml)));
        return testExtractor.getResult();
    }

    private static String parseDOM(String xml) throws ParserConfigurationException, IOException, SAXException {
        String rv = "Not found";
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new InputSource(new StringReader(xml)));
        NodeList list = doc.getElementsByTagName("test");
        for (int temp = 0; temp < list.getLength(); temp++) {
            Node node = list.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                // get test's attribute
                rv = element.getTextContent();
            }
        }
        return rv;

    }

    private static class TestExtractor extends DefaultHandler implements ContentHandler {
        private boolean testElementActive;
        private StringBuilder sb = new StringBuilder();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if ("test".equals(qName)) {
                testElementActive = true;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if ("test".equals(qName)) {
                testElementActive = false;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (!testElementActive) {
                return;
            }
            sb.append(ch, start, length);
        }

        String getResult() {
            return sb.toString();
        }
    }
}
