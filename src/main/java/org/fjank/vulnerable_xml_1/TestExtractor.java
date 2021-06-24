package org.fjank.vulnerable_xml_1;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

class TestExtractor extends DefaultHandler implements ContentHandler {
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
