package com.example.sipvs;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.Part;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.tsp.*;

import java.io.PrintWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@MultipartConfig(location = "/tmp", maxFileSize = 1024*1024, maxRequestSize = 2*1024*1024, fileSizeThreshold = 1024*1024)
@WebServlet(name = "timestamp", value = "/timestamp")
public class Timestamp extends HttpServlet {
    String initMatch = "<ds:SignatureValue Id=\"signatureIdSignatureValue\" xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:xzep=\"http://www.ditec.sk/ep/signature_formats/xades_zep/v1.1\">";
    int initOffset = initMatch.length();
    String lastMatch = "</ds:SignatureValue>";
    String funnyMatch = "</xades:SignedProperties>";
    int funnyOffset = funnyMatch.length();
    String funnyPre = "<xades:UnsignedProperties><xades:UnsignedSignatureProperties><xades:SignatureTimeStamp Id=\"signatureIdSignatureTimeStamp\"><xades:EncapsulatedTimeStamp>";

    String funnyPost = "</xades:EncapsulatedTimeStamp></xades:SignatureTimeStamp></xades:UnsignedSignatureProperties></xades:UnsignedProperties>";

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        int whichData = Integer.parseInt(new String(request.getPart("whichData").getInputStream().readAllBytes()));
        Part xmlData = request.getPart("uploadedFile");
        String xmlString = new String(xmlData.getInputStream().readAllBytes());

        byte[] signatureDigest;
        PrintWriter out = response.getWriter();
        
        if(whichData == 1){
            // extrahovanie len dat ktore treba opeciatkovat
            String base64signature = xmlString.substring(xmlString.indexOf(this.initMatch) + this.initOffset, xmlString.indexOf(this.lastMatch));
            
            Digest digest = new SHA256Digest();
            digest.update(base64signature.getBytes(), 0, base64signature.length());

            signatureDigest = new byte[digest.getDigestSize()];
            int outOff = 0;
            digest.doFinal(signatureDigest, outOff);
        } else if (whichData == 2) {
            // extrahovanie len dat ktore treba opeciatkovat
            signatureDigest = xmlString.substring(xmlString.indexOf(this.initMatch) + this.initOffset, xmlString.indexOf(this.lastMatch)).getBytes();
        } else {
            Digest digest = new SHA256Digest();
            digest.update(xmlString.getBytes(), 0, xmlString.length());

            signatureDigest = new byte[digest.getDigestSize()];
            int outOff = 0;
            digest.doFinal(signatureDigest, outOff);
        }
        
        TimeStampRequestGenerator tsRequestGenerator = new TimeStampRequestGenerator();
        tsRequestGenerator.setCertReq(true);

        TimeStampRequest tsRequest = tsRequestGenerator.generate(TSPAlgorithms.SHA256, signatureDigest);
        TimestampHandler ts = new TimestampHandler();

        byte[] responseBytes = ts.getTimestamp(tsRequest.getEncoded(), "http://test.ditec.sk/TSAServer/tsa.aspx");
        try {
            TimeStampResponse tsResponse = new TimeStampResponse(responseBytes);
            // extrahovanie podpisanych dat a casovej peciatky
            String timestampB64 = Base64.getEncoder().encodeToString(tsResponse.getTimeStampToken().getEncoded());

            // pridanie XADES-T do XADES-EPES
            int funny = xmlString.indexOf(this.funnyMatch);
            String updated = xmlString.substring(0, funny+this.funnyOffset);
            updated += this.funnyPre;
            updated += timestampB64;
            updated += this.funnyPost;
            updated += xmlString.substring(funny + this.funnyOffset);

            out.println(updated);
        } catch (Exception e) {
            out.println("Error encountered");
        }
    }
}
