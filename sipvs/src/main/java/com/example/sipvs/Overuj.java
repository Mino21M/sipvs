package com.example.sipvs;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.bouncycastle.asn1.x509.CertificateList;
import org.bouncycastle.jce.provider.X509CRLParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.rmi.ServerException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.*;
import java.util.*;
import java.security.cert.X509Certificate;

import org.bouncycastle.tsp.*;
import org.bouncycastle.cert.*;
import org.bouncycastle.cms.*;
import org.bouncycastle.util.Store;

import java.net.URL;

import org.bouncycastle.cert.jcajce.JcaX509CRLConverter;
import java.security.Provider;
import java.security.Security;


import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


@MultipartConfig(location = "/tmp", maxFileSize = 1024*1024, maxRequestSize = 2*1024*1024, fileSizeThreshold = 1024*1024)
@WebServlet(name = "check", value = "/check")
public class Overuj extends HttpServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Part xmlData = request.getPart("uploadedFile");
        String fileName = xmlData.getSubmittedFileName();
        PrintWriter out = response.getWriter();
        out.println(fileName);

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlData.getInputStream());
            Element rootElement = document.getDocumentElement();

            // date envelope check
            boolean namespaceCheck = checkNamespace(rootElement);
            if (!namespaceCheck) {
                out.println("overenie datovej obalky: nespravny namespace\n");
                return;
            }

            // xml signature check
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

                out.println("overenie xml signature: nespravny SignatureMethod alebo CanonicalizationMethod\n");
                return;
            }

            SignatureValidation signatureValidator = new SignatureValidation(document);

            try {
                //Elementy ds:Transforms a ds:DigestMethod
                signatureValidator.verifyTransformsAndDigestMethod();

                //Core validation
                signatureValidator.verifyCoreReferencesAndDigestValue();
                signatureValidator.verifyCoreSignatureValue();

                //Element ds:Signature:
                signatureValidator.verifySignature();

                //Element ds:SignatureValue
                signatureValidator.verifySignatureValueId();

                //Referencie v Signedinfo
                signatureValidator.verifySignedInfoReferencesAndAttributeValues();

                //Element ds:KeyInfo
                signatureValidator.verifyKeyInfoContent();

                //Element ds:SignatureProperties
                signatureValidator.verifySignaturePropertiesContent();

                //Elementy ds:Manifest
                signatureValidator.verifyManifestElements();

                //Referencie ds:Manifest elementov
                signatureValidator.verifyManifestElementsReferences();

            } catch (Exception e) {
                out.println(e);
                return;
            }

            // overenie casovej peciatky
            String timestamp = getNodeValue(rootElement, "xades:EncapsulatedTimeStamp");

            try {
                byte[] timestampBytes = java.util.Base64.getDecoder().decode(timestamp);
                X509CertificateHolder tsCert = getTimeStampSignatureCertificate(timestampBytes);

                if (tsCert == null){
                    out.println("Overenie casovej peciatky: casova peciatka nebola najdena\n");
                    return;
                }

                if (!tsCert.isValidOn(new Date())){
                    out.println("Overenie casovej peciatky: neplatny cas voci aktualnemu datumu.\n");
                    return;
                }

                X509CRL crl = getCRL("http://test.ditec.sk/TSAServer/crl/dtctsa.crl");

                if (crl.getRevokedCertificate(tsCert.getSerialNumber()) != null){
                    out.println("Overenie casovej peciatky: neplatny certifikat podla CRL\n");
                    return;
                }

                String messageImprintCheck = checkMessageImprint(timestampBytes, rootElement);
                if (!messageImprintCheck.equals("uspech")) {
                    out.println(messageImprintCheck);
                    out.println();
                    return;
                }

            } catch (Exception e) {
                out.println(e);
                return;
            }

            //overenie podpisoveho certifikatu
            String certValue = getNodeValue(rootElement, "ds:X509Certificate");
            X509Certificate cert = getCert(certValue);

            if (cert == null) {
                out.println("Overenie podpisoveho certifikatu: chyba pri citani certifikatu\n");
                return;
            }

            byte[] timestampBytes = java.util.Base64.getDecoder().decode(timestamp);
            TimeStampToken tsToken = new TimeStampToken(
                    new org.bouncycastle.cms.CMSSignedData(timestampBytes));

            try {
                cert.checkValidity(tsToken.getTimeStampInfo().getGenTime());
            } catch (CertificateExpiredException e) {
                out.println("Overenie podpisoveho certifikatu: certifikat expirovany\n");
            } catch (CertificateNotYetValidException e) {
                out.println("Overenie podpisoveho certifikatu: certifikat este nebol platny v case podpisu\n");
            }

            X509CRL crl = getCRL("http://test.ditec.sk/DTCCACrl/DTCCACrl.crl");
            X509CRLEntry entry = crl.getRevokedCertificate(cert.getSerialNumber());
            if (entry != null && entry.getRevocationDate().before(tsToken.getTimeStampInfo().getGenTime())) {
                out.println("Overenie podpisoveho certifikatu: certifikat bol neplatny v case podpisu\n");
                return;
            }

            out.println("Overenie preblehlo uspesne!\n");


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

    private X509CRL getCRL(String url) throws IOException {

        ByteArrayInputStream crlData = getDataFromUrl(url);

        if (crlData == null){
            throw new ServerException("Nepodarilo sa stiahnut CRL zo stranky.");
        }

        try {
            X509CRLHolder crlHolder = new X509CRLHolder(crlData);
            return new JcaX509CRLConverter().getCRL(crlHolder);

        } catch (CRLException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private ByteArrayInputStream getDataFromUrl(String url) {

        URL urlHandler = null;
        try {
            urlHandler = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = null;
        try {
            is = urlHandler.openStream();
            byte[] byteChunk = new byte[4096];
            int n;

            while ( (n = is.read(byteChunk)) > 0 ) {
                baos.write(byteChunk, 0, n);
            }
        }
        catch (IOException e) {
            System.err.printf ("Failed while reading bytes from %s: %s", urlHandler.toExternalForm(), e.getMessage());
            return null;
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return new ByteArrayInputStream(baos.toByteArray());
    }

    public String checkMessageImprint(byte[] tsResponse, Element root) throws ServletException, CMSException, TSPException, IOException {
        try {
            TimeStampToken token = new TimeStampToken(
                    new org.bouncycastle.cms.CMSSignedData(tsResponse));

            byte[] MI = token.getTimeStampInfo().getMessageImprintDigest();
            String alghoritm = token.getTimeStampInfo().getHashAlgorithm().getAlgorithm().getId();
            String signatureValue = getNodeValue(root, "ds:SignatureValue");

            if (signatureValue.isEmpty()){
                return "Overenie casovej peciatky: SignatureValue nebol najdeny";
            }

            byte[] signatureValueBytes = java.util.Base64.getDecoder().decode(signatureValue.getBytes());

            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

            MessageDigest messageDigest = null;
            try {
                messageDigest = MessageDigest.getInstance(alghoritm, "BC");
            } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
                return "Overenie casovej peciatky: neznamy algoritmus v MI.";
            }

            if (!Arrays.equals(MI, messageDigest.digest(signatureValueBytes))){
                return "Overenie casovej peciatky: MI z casovej peciatky a podpis v SignatureValue sa nezhoduju.";
            }

            return "uspech";

        } catch (IOException | CMSException | TSPException ex) {
            return null;
        }

    }

    private X509CertificateHolder getTimeStampSignatureCertificate(byte[] tsResponse) {
        try {
            TimeStampToken token = new TimeStampToken(
                    new org.bouncycastle.cms.CMSSignedData(tsResponse));
            X509CertificateHolder signerCert = null;

            Store<X509CertificateHolder> x509Certs = token.getCertificates();
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

            return signerCert;
        } catch (IOException | CMSException | TSPException ex) {
            return null;
        }
    }

    private X509Certificate getCert(String cert) {

        try {
            byte[] certificateBytes = java.util.Base64.getDecoder().decode(cert);

            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

            return (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certificateBytes));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}