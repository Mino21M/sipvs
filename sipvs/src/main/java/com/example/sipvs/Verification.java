package com.example.sipvs;

import java.security.Security;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class Verification {

    protected Document document;

    protected Finder finder;

    public Verification(Document document) {

        this.document = document;
        this.finder = new Finder(document);

        org.apache.xml.security.Init.init();
        Security.addProvider(new BouncyCastleProvider());
    }

    protected boolean assertElementAttributeValue(Element element, String attribute, String expectedValue) {
        String actualValue = element.getAttribute(attribute);
        return actualValue.equals(expectedValue);
    }

    protected boolean assertElementAttributeValue(Element element, String attribute, List<String> expectedValues) {
        for (String expectedValue : expectedValues) {
            if (assertElementAttributeValue(element, attribute, expectedValue)) {
                return true;
            }
        }
        return false;
    }

    protected boolean assertElementAttributeValue(Element element, String attribute) {
        String actualValue = element.getAttribute(attribute);
        return !actualValue.isEmpty();
    }

}
