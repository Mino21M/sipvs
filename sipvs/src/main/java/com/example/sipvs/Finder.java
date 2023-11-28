package com.example.sipvs;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.util.encoders.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpressionException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class Finder {
    private Document document;
    public Finder(Document document) {
        this.document = document;
    }

    public Element findByAttributeValue(String elementType, String attributeName, String attributeValue) {
        NodeList elements = this.document.getElementsByTagName(elementType);
        for (int i=0; i<elements.getLength(); i++) {
            Element element = (Element) elements.item(i);
            if (element.hasAttribute(attributeName) && element.getAttribute(attributeName).equals(attributeValue)) {
                return element;
            }
        }
        return null;
    }

    public X509Certificate getCertificate() throws XPathExpressionException, DocumentVerificationException {

        Element keyInfoElement = (Element) document.getElementsByTagName("ds:KeyInfo").item(0);

        if (keyInfoElement == null) {
            throw new DocumentVerificationException("Chyba pri ziskavani certifikatu: Dokument neobsahuje element ds:KeyInfo");
        }

        Element x509DataElement = (Element) keyInfoElement.getElementsByTagName("ds:X509Data").item(0);

        if (x509DataElement == null) {
            throw new DocumentVerificationException("Chyba pri ziskavani certifikatu: Dokument neobsahuje element ds:X509Data");
        }

        Element x509Certificate = (Element) x509DataElement.getElementsByTagName("ds:X509Certificate").item(0);

        if (x509Certificate == null) {
            throw new DocumentVerificationException("Chyba pri ziskavani certifikatu: Dokument neobsahuje element ds:X509Certificate");
        }

        X509Certificate certObject = null;
        // ASN1InputStream inputStream = null;

        try {
            //inputStream = new ASN1InputStream(new ByteArrayInputStream(Base64.decode(x509Certificate.getTextContent())));
            //ASN1Sequence sequence = (ASN1Sequence) inputStream.readObject();

            String base64Certificate = x509Certificate.getTextContent();
            byte[] certificateBytes = java.util.Base64.getDecoder().decode(base64Certificate);

            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            certObject = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certificateBytes));

        } catch (java.security.cert.CertificateParsingException e) {

            throw new DocumentVerificationException("Certifikát nebolo možné načítať", e);

        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }

        return certObject;
    }


    private void closeQuietly(ASN1InputStream inputStream) {

        if (inputStream == null) {
            return;
        }

        try {
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}