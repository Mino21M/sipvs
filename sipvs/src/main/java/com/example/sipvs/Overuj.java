package com.example.sipvs;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.jcajce.provider.asymmetric.X509;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.bouncycastle.tsp.*;
import org.bouncycastle.cert.*;
import org.bouncycastle.x509.*;
import org.bouncycastle.cms.*;
import org.bouncycastle.*;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


@MultipartConfig(location = "/tmp", maxFileSize = 1024*1024, maxRequestSize = 2*1024*1024, fileSizeThreshold = 1024*1024)
@WebServlet(name = "check", value = "/check")
public class Overuj extends HttpServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Part xmlData = request.getPart("uploadedFile");
        PrintWriter out = response.getWriter();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlData.getInputStream());
            Element rootElement = document.getDocumentElement();

            // date envelope check
            boolean namespaceCheck = checkNamespace(rootElement);
            if (!namespaceCheck) {
                out.println("overenie datovej obalky: nespravny namespace");
            }

            // SignatureMethod & CanonicalizationMethod check
            String signatureMethod = getAttributeValue(rootElement, "ds:SignatureMethod", "Algorithm");
            String canonicalizationMethod = getAttributeValue(rootElement, "ds:CanonicalizationMethod", "Algorithm");
            List<String> allowedAlghoritms = List.of("http://www.w3.org/2000/09/xmldsig#dsa-sha1",
                    "http://www.w3.org/2000/09/xmldsig#rsa-sha1",
                    "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256",
                    "http://www.w3.org/2001/04/xmldsig-more#rsa-sha384",
                    "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512",
                    "http://www.w3.org/2000/09/xmldsig#sha1",
                    "http://www.w3.org/2001/04/xmldsig",
                    "http://www.w3.org/2001/04/xmlenc#sha256",
                    "http://www.w3.org/2001/04/xmldsig-more#sha384",
                    "http://www.w3.org/2001/04/xmlenc#sha512");

            if (!allowedAlghoritms.contains(signatureMethod) ||
                    !canonicalizationMethod.equals("http://www.w3.org/TR/2001/REC-xml-c14n-20010315")) {

                out.println("overenie xml signature: nespravny SignatureMethod alebo CanonicalizationMethod");
            }

            // overenie casovej peciatky
            String timestamp = getNodeValue(rootElement, "xades:EncapsulatedTimeStamp");

            try {
                byte[] timestampBytes = java.util.Base64.getDecoder().decode(timestamp);
                TimeStampResponse timeStampResponse = new TimeStampResponse(timestampBytes);
                //byte[] tsCert = getTimeStampSignatureCertificate(timestampBytes);

                out.println("ziskany tsCert!!!");


            } catch (Exception e) {
                out.println(e);
            }

            out.println("overenie prebehlo uspesne!");

        } catch (Exception e) {
            out.println(e);
        }

    }

    private static String getNodeValue(Element parentElement, String nodeName) {
        NodeList childNodes = parentElement.getChildNodes();
        String out = "";

        for (int i = 0; i < childNodes.getLength(); i++) {

            if (childNodes.item(i) instanceof Element) {
                Element childElement = (Element) childNodes.item(i);

                if (childElement.getNodeName().equals(nodeName)) {
                    Node dataNode = childElement.getFirstChild();
                    return dataNode.getTextContent();
                }

                if (childElement.hasChildNodes()) {
                    out = out.concat(getNodeValue(childElement, nodeName));
                }
            }
        }

        return out;
    }

    private static String getAttributeValue(Element parentElement, String nodeName, String attributeName) {
        NodeList childNodes = parentElement.getChildNodes();
        String out = "";

        for (int i = 0; i < childNodes.getLength(); i++) {

            if (childNodes.item(i) instanceof Element) {
                Element childElement = (Element) childNodes.item(i);

                if (childElement.getNodeName().equals(nodeName)) {
                    return childElement.getAttribute(attributeName);
                }

                if (childElement.hasChildNodes()) {
                    out = out.concat(getAttributeValue(childElement, nodeName, attributeName));
                }
            }
        }

        return out;
    }

    private static boolean checkNamespace(Element rootElement) {
        String xzep = "xmlns:xzep";
        String ds = "xmlns:ds";
        String xzep_ns = "http://www.ditec.sk/ep/signature_formats/xades_zep/v1.0";
        String ds_ns = "http://www.w3.org/2000/09/xmldsig#";

        boolean hasCorrectAttributes = rootElement.hasAttribute(xzep) &&
                rootElement.hasAttribute(ds);

        boolean hasCorrectNamespaces = xzep_ns.equals(rootElement.getAttribute(xzep)) &&
                ds_ns.equals(rootElement.getAttribute(ds));

        return hasCorrectAttributes && hasCorrectNamespaces;
    }

    private byte[] getTimeStampSignatureCertificate(byte[] tsResponse) {
        try {
            TimeStampResponse tsResp = new TimeStampResponse(tsResponse);
            TimeStampToken token = new TimeStampToken(
                    new org.bouncycastle.cms.CMSSignedData(tsResp.getTimeStampToken().getEncoded()));
            X509CertificateHolder signerCert = null;
            X509Store x509Certs = (X509Store) tsResp.getTimeStampToken().getCertificates();
            List<X509CertificateHolder> certs = new ArrayList<>(x509Certs.getMatches(null));

            // nájdenie podpisového certifikátu tokenu v kolekcii
            for (X509CertificateHolder cert : certs) {
                String cerIssuerName = cert.getIssuer().toString();
                String signerIssuerName = token.getSID().getIssuer().toString();

                // kontrola issuer name a seriového čísla
                if (cerIssuerName.equals(signerIssuerName) &&
                        cert.getSerialNumber().equals(token.getSID().getSerialNumber())) {
                    signerCert = cert;
                    break;
                }
            }

            return signerCert.getEncoded();
        } catch (IOException | CMSException | TSPException ex) {
            return null;
        }
    }
}