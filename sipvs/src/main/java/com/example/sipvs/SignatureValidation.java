package com.example.sipvs;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
*/

import org.w3c.dom.NodeList;

import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.util.encoders.Base64;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.xpath.XPathExpressionException;
import it.svario.xpathapi.jaxp.XPathAPI;

public class SignatureValidation extends Verification {
    private List<String> manifestTransformMethods = new ArrayList<String>(Arrays.asList(
            new String[] {
                    "http://www.w3.org/TR/2001/REC-xml-c14n-20010315",
                    "http://www.w3.org/2000/09/xmldsig#base64"
            }
    ));

    private List<String> digestMethods = new ArrayList<String>(Arrays.asList(
            new String[] {
                    "http://www.w3.org/2000/09/xmldsig#sha1",
                    "http://www.w3.org/2001/04/xmldsig-more#sha224",
                    "http://www.w3.org/2001/04/xmlenc#sha256",
                    "http://www.w3.org/2001/04/xmldsig-more#sha384",
                    "http://www.w3.org/2001/04/xmlenc#sha512"
            }
    ));

    public SignatureValidation(Document document) {
        super(document);
        // constructor code for SignatureValidation
    }

    /*
     Element ds:Signature:
     - musí mať Id atribút,
     - musí mať špecifikovaný namespace xmlns:ds
     */
    public boolean verifySignature() throws DocumentVerificationException {
        Element signatureElement = (Element) document.getElementsByTagName("ds:Signature").item(0);
        if (signatureElement == null) {
            throw new DocumentVerificationException(
                    "Element ds:Signature sa nenašiel");
        }
        if (!signatureElement.hasAttribute("Id")) {
            throw new DocumentVerificationException(
                    "Element ds:Signature neobsahuje atribút Id");
        }
/*        if (!assertElementAttributeValue(signatureElement)) {
            throw new DocumentVerificationException(
                    "Atribút Id elementu ds:Signature neobsahuje žiadnu hodnotu");
        }*/
        if (!assertElementAttributeValue(signatureElement, "xmlns:ds", "http://www.w3.org/2000/09/xmldsig#")) {
            throw new DocumentVerificationException(
                    "Element ds:Signature nemá nastavený namespace xmlns:ds");
        }
        return true;
    }

    /*
     * Element ds:SignatureValue
     * 	– musí mať Id atribút
     */
    public boolean verifySignatureValueId() throws DocumentVerificationException {
        Element signatureValueElement = (Element) document.getElementsByTagName("ds:SignatureValue").item(0);

        if (signatureValueElement == null) {
            throw new DocumentVerificationException("Element ds:SignatureValue sa nenašiel");
        }
        if (!signatureValueElement.hasAttribute("Id")) {
            throw new DocumentVerificationException("Element ds:SignatureValue neobsahuje atribút Id");
        }

        return true;
    }

    public boolean verifyKeyInfoContent() throws DocumentVerificationException {
        Element keyInfoElement = (Element) document.getElementsByTagName("ds:KeyInfo").item(0);

        if (keyInfoElement == null) {
            throw new DocumentVerificationException("Element ds:Signature sa nenašiel");
        }

        if (!keyInfoElement.hasAttribute("Id")) {
            throw new DocumentVerificationException("Element ds:Signature neobsahuje atribút Id");
        }

        if (!assertElementAttributeValue(keyInfoElement, "Id")) {
            throw new DocumentVerificationException( "Atribút Id elementu ds:Signature neobsahuje žiadnu hodnotu");
        }

        Element xDataElement = (Element) keyInfoElement.getElementsByTagName("ds:X509Data").item(0);
        if (xDataElement == null) {throw new DocumentVerificationException("Element ds:KeyInfo neobsahuje element ds:X509Data");
        }

        Element xCertificateElement = (Element) xDataElement.getElementsByTagName("ds:X509Certificate").item(0);
        Element xIssuerSerialElement = (Element) xDataElement.getElementsByTagName("ds:X509IssuerSerial").item(0);
        Element xSubjectNameElement = (Element) xDataElement.getElementsByTagName("ds:X509SubjectName").item(0);

        if (xCertificateElement == null) {
            throw new DocumentVerificationException( "Element ds:X509Data neobsahuje element ds:X509Certificate");
        }

        if (xIssuerSerialElement == null) {
            throw new DocumentVerificationException("Element ds:X509Data neobsahuje element ds:X509IssuerSerial");
        }

        if (xSubjectNameElement == null) {
            throw new DocumentVerificationException( "Element ds:X509Data neobsahuje element ds:X509SubjectName");
        }

        Element xIssuerNameElement = (Element) xIssuerSerialElement.getElementsByTagName("ds:X509IssuerName").item(0);
        Element xSerialNumberElement = (Element) xIssuerSerialElement.getElementsByTagName("ds:X509SerialNumber").item(0);

        if (xIssuerNameElement == null) {
            throw new DocumentVerificationException("Element ds:X509IssuerSerial neobsahuje element ds:X509IssuerName");
        }

        if (xSerialNumberElement == null) {
            throw new DocumentVerificationException( "Element ds:X509IssuerSerial neobsahuje element ds:X509SerialNumber");
        }

        X509CertificateObject certificate = null;

        try {
            certificate = this.finder.getCertificate();

        } catch (XPathExpressionException e) {
            throw new DocumentVerificationException(
                    "X509 certifikát sa v dokumente nepodarilo nájsť", e);
        }

        String certifIssuerName = certificate.getIssuerX500Principal().toString().replaceAll("ST=", "S=");
        String certifSerialNumber = certificate.getSerialNumber().toString();
        String certifSubjectName = certificate.getSubjectX500Principal().toString();

        if (!xIssuerNameElement.getTextContent().equals(certifIssuerName)) {
            throw new DocumentVerificationException("Element ds:X509IssuerName sa nezhoduje s hodnotou na certifikáte");
        }
        if (!xSerialNumberElement.getTextContent().equals(certifSerialNumber))
            throw new DocumentVerificationException("Element ds:X509SerialNumber sa nezhoduje s hodnotou na certifikáte");

        if (!xSubjectNameElement.getTextContent().equals(certifSubjectName)) {
            throw new DocumentVerificationException("Element ds:X509SubjectName neobsahuje element ds:X509SerialNumber");
        }
        return true;
    }

    /*
     * Overenie obsahu ds:SignatureProperties:
     * 	- musí mať Id atribút,
     * 	- musí obsahovať dva elementy ds:SignatureProperty pre xzep:SignatureVersion a xzep:ProductInfos,
     * 	- obidva ds:SignatureProperty musia mať atribút Target nastavený na ds:Signature
     */
    public boolean verifySignaturePropertiesContent() throws DocumentVerificationException {
        Element signaturePropertiesElement = (Element) document.getElementsByTagName("ds:SignatureProperties").item(0);

        if (signaturePropertiesElement == null) {
            throw new DocumentVerificationException("Element ds:SignatureProperties sa nenašiel");
        }

        if (!signaturePropertiesElement.hasAttribute("Id")) {
            throw new DocumentVerificationException("Element ds:SignatureProperties neobsahuje atribút Id");
        }

        if (!assertElementAttributeValue(signaturePropertiesElement, "Id")) {
            throw new DocumentVerificationException("Atribút Id elementu ds:SignatureProperties neobsahuje žiadnu hodnotu");
        }

        Element signatureVersionElement = null;
        Element productInfosElement = null;

        /*
        It iterates over the elements obtained from getElementsByTagName("ds:SignatureProperty"),
         for each element, it looks for child elements with the tags "xzep:SignatureVersion" and
         "xzep:ProductInfos". Depending on whether "xzep:SignatureVersion" is found or not, it updates
         either signatureVersionElement or productInfosElement accordingly.
         */
        NodeList signatureProperties = signaturePropertiesElement.getElementsByTagName("ds:SignatureProperty");
        for (int i = 0; i < signatureProperties.getLength(); i++) {
            Element tempElement = (Element) signatureProperties.item(i);
            Element tempElement2 = (Element) tempElement.getElementsByTagName("xzep:SignatureVersion").item(0);

            if (tempElement2 == null) {
                tempElement2 = (Element) tempElement.getElementsByTagName("xzep:ProductInfos").item(0);
                productInfosElement = tempElement2;
            } else {
                signatureVersionElement = tempElement2;
            }
        }

        if (signatureVersionElement == null) {
            throw new DocumentVerificationException("ds:SignatureProperties neobsahuje taký element ds:SignatureProperty, ktorý by obsahoval element xzep:SignatureVersion");
        }

        if (productInfosElement == null) {
            throw new DocumentVerificationException("ds:SignatureProperties neobsahuje taký element ds:SignatureProperty, ktorý by obsahoval element xzep:ProductInfos");
        }

        Element signature = (Element) document.getElementsByTagName("ds:Signature").item(0);

        if (signature == null) {
            throw new DocumentVerificationException("Element ds:Signature sa nenašiel");
        }

        String signatureId = signature.getAttribute("Id");

        Element sigVerParentElement = (Element) signatureVersionElement.getParentNode();
        Element pInfoParentElement = (Element) productInfosElement.getParentNode();

        String targetSigVer = sigVerParentElement.getAttribute("Target");
        String targetPInfo = pInfoParentElement.getAttribute("Target");

        if (!targetSigVer.equals("#" + signatureId)) {
            throw new DocumentVerificationException("Atribút Target elementu xzep:SignatureVersion sa neodkazuje na daný ds:Signature");
        }

        if(!targetPInfo.equals("#" + signatureId)) {
            throw new DocumentVerificationException("Atribút Target elementu xzep:ProductInfos sa neodkazuje na daný ds:Signature");
        }
        return true;
    }

    /*
     * Overenie ds:Manifest elementov:
     * 	- každý ds:Manifest element musí mať Id atribút,
     * 	- ds:Transforms musí byť z množiny podporovaných algoritmov pre daný element podľa profilu XAdES_ZEP,
     * 	- ds:DigestMethod – musí obsahovať URI niektorého z podporovaných algoritmov podľa profilu XAdES_ZEP,
     * 	- overenie hodnoty Type atribútu voči profilu XAdES_ZEP,
     * 	- každý ds:Manifest element musí obsahovať práve jednu referenciu na ds:Object
     */
    public boolean verifyManifestElements() throws DocumentVerificationException {
        NodeList manifestElements = null;
        try {
            manifestElements = XPathAPI.selectNodeList(document.getDocumentElement(), "//ds:Signature/ds:Object/ds:Manifest");

        } catch (XPathException e) {
            throw new DocumentVerificationException("Chyba pri hladanie ds:Manifest elementov v dokumente", e);
        }

        for (int i = 0; i < manifestElements.getLength(); i++) {
            Element manifestElement = (Element) manifestElements.item(i);

            if (!manifestElement.hasAttribute("Id")) {
                throw new DocumentVerificationException("Element ds:Manifest nema atribut Id");
            }

            try {
                NodeList referenceElements = XPathAPI.selectNodeList(manifestElement, "ds:Reference");

                if (referenceElements.getLength() != 1) {
                    throw new DocumentVerificationException("ds:Manifest element neobsahuje prave jednu referenciu na objekt");
                }
            } catch (XPathException e) {
                throw new DocumentVerificationException("Chyba pri hladanii ds:Reference elementov v ds:Manifest elemente", e);
            }
        }

        NodeList referenceElements = null;
        try {
            referenceElements = XPathAPI.selectNodeList(document.getDocumentElement(), "//ds:Signature/ds:Object/ds:Manifest/ds:Reference");
        } catch (XPathException e) {

            throw new DocumentVerificationException("Chyba pri hladanii ds:Reference elementov v dokumente", e);
        }

        for (int i = 0; i < referenceElements.getLength(); i++) {
            Element referenceElement = (Element) referenceElements.item(i);

            try {
                NodeList transformsElements = XPathAPI.selectNodeList(referenceElement, "ds:Transforms/ds:Transform");

                for (int j = 0; j < transformsElements.getLength(); j++) {
                    Element transformElement = (Element) transformsElements.item(j);
                    if (!manifestTransformMethods.contains(transformElement.getAttribute("Algorithm"))) {
                        throw new DocumentVerificationException("Element ds:Transform obsahuje nepovoleny typ algoritmu");
                    }
                }

                Element digestMethodElement = (Element) XPathAPI.selectSingleNode(referenceElement, "ds:DigestMethod");
                if (!digestMethods.contains(digestMethodElement.getAttribute("Algorithm"))) {
                    throw new DocumentVerificationException("Atribút Algorithm elementu ds:DigestMethod neobsahuje URI niektorého z podporovaných algoritmov");
                }

                if (!"http://www.w3.org/2000/09/xmldsig#Object".equals(referenceElement.getAttribute("Type"))) {
                    throw new DocumentVerificationException("Atribút Type elementu ds:Reference neobsahuje hodnotu http://www.w3.org/2000/09/xmldsig#Object");
                }
            } catch (XPathException e) {
                throw new DocumentVerificationException("Chyba pri hladanie elementov v dokumente", e);
            }
        }

        return true;
    }
}
