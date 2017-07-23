package com.in2bits.shims;

/**
 * Created by Tim on 7/3/17.
 */

public class XDocument {
    public XElement xPathSelectElement(String ok) {
        throw new RuntimeException("Not Implemented: com.in2bits.shims.XDocument.XPathSelectElement");
    }

    public static XDocument parse(String xml) throws XmlException {
        throw new RuntimeException("Not Implemented: com.in2bits.shims.XDocument.parse");
    }
}
