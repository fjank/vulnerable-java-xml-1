package org.fjank.vulnerable_xml_1;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS;

public class Tester {
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, XMLStreamException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///C:\\Windows\\System32\\drivers\\etc\\\">]>" +
                "<root>\n" +
                "<test>&xxe;</test>\n" +
                "</root>\n";
        //System.out.println(parseDOM(xml));
        //System.out.println(parseSAX(xml));
        //System.out.println(parseStAX(xml));
        String schema = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "<xs:element name=\"root\">" +
                "<xs:complexType>\n" +
                "    <xs:sequence>\n" +
                "      <xs:element name=\"test\" type=\"xs:string\"/>" +
                "    </xs:sequence>\n" +
                "  </xs:complexType>\n" +
                "</xs:element>" +
                "</xs:schema>";

//        System.out.println(validate(schema, xml));
        String xslt = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<xsl:stylesheet version=\"1.0\"\n" +
                "xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n" +
                "<xsl:output method=\"text\" indent=\"no\" encoding=\"UTF-8\" omit-xml-declaration=\"yes\" />" +
                "<xsl:template match=\"/root/test\">\n" +
                "<xsl:value-of select=\".\"/>" +
                "</xsl:template>\n" +
                "</xsl:stylesheet>";
        //System.out.println(parseXSLT(xslt, xml));
//        String xpath = "(substring((doc-available('file:///C:\\Windows\\System32\\drivers\\etc\\hosts')/*[1]/*[1]/text()[1]),3,1))) < 127";
//        System.out.println(parseXPath(xpath, xml));
        System.out.println(parseDOMLS(xml));
    }

    private static String parseDOMLS(String xml) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        DOMImplementationRegistry dir = DOMImplementationRegistry.newInstance();
        DOMImplementationLS impl = (DOMImplementationLS) dir.getDOMImplementation("LS");
        LSParser lsParser = impl.createLSParser(MODE_SYNCHRONOUS, "http://www.w3.org/2001/XMLSchema");
        LSInput lsInput = impl.createLSInput();
        lsInput.setCharacterStream(new StringReader(xml));
        Document doc = lsParser.parse(lsInput);
        String rv = "";
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

    private static String parseXPath(String expression, String xml) throws XPathExpressionException, XPathFactoryConfigurationException, ParserConfigurationException, IOException, SAXException {
        XPathFactory xpf = XPathFactory.newDefaultInstance();
        // Skru på FSP, men den skrur kun av eksterne xpath funksjoner. XXE via xml er fremdeles mulig!
        //xpf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        XPath xPath = xpf.newXPath();
        XPathExpression xpe = xPath.compile(expression);
        // Do not use xpe.evaluate(inputSource, XPathConstants.STRING), as that uses an unsafe DocumentBuilderFactory
        // Compliant example:
        // Make sure to use a safe DocumentBuilderBuilderFactory, then retrieve the org.w3c.dom.Document and pass that to evaluate.
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
//        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
//        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
//        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
//        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
//        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
//        dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
//        dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
//        dbf.setXIncludeAware(false);
//        dbf.setExpandEntityReferences(false);
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document document = db.parse(new InputSource(new StringReader(xml)));
        return (String) xpe.evaluate(document, XPathConstants.STRING);
    }

    private static String parseXSLT(String xslt, String xml) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newDefaultInstance();
//        tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
//        tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
//        tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        StringWriter writer = new StringWriter();
        Result result = new StreamResult(writer);
        Transformer t = tf.newTransformer(new StreamSource(new StringReader(xslt)));
        t.transform(new StreamSource(new StringReader(xml)), result);
        return writer.toString();
    }

    private static String parseSchema(String xml) throws SAXException, IOException, ParserConfigurationException {
        SchemaFactory sf = SchemaFactory.newDefaultInstance();
        sf.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        Schema schema = sf.newSchema(new StreamSource(new StringReader(xml)));
        return null;
    }

    private static String validate(String xml, String xml2) throws SAXException, IOException, ParserConfigurationException {
        SchemaFactory sf = SchemaFactory.newDefaultInstance();
        sf.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        Schema schema = sf.newSchema(new StreamSource(new StringReader(xml)));
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(new StringReader(xml2)));
        return null;
    }


    private static String parseStAX(String xml) throws XMLStreamException {
        boolean testElementActive = false;
        StringBuilder rv = new StringBuilder();
        XMLInputFactory f = XMLInputFactory.newInstance();
        XMLStreamReader r = f.createXMLStreamReader(new StringReader(xml));
        while (r.hasNext()) {
            r.next();
            if (r.getEventType() == XMLStreamConstants.START_ELEMENT && r.getName().getLocalPart().equals("test")) {
                testElementActive = true;
            }
            if (r.getEventType() == XMLStreamConstants.END_ELEMENT && r.getName().getLocalPart().equals("test")) {
                testElementActive = false;
            }
            if (r.getEventType() == XMLStreamConstants.CHARACTERS && testElementActive) {
                rv.append(r.getText());
            }
        }
        return rv.toString();
    }

    private static String parseSAX(String xml) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
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
        dbf.setExpandEntityReferences(false);
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
