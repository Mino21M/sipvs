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

@MultipartConfig(location = "/tmp", maxFileSize = 1024*1024, maxRequestSize = 2*1024*1024, fileSizeThreshold = 1024*1024)
@WebServlet(name = "timestamp", value = "/timestamp")
public class Timestamp extends HttpServlet {

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Part xmlData = request.getPart("uploadedFile");
        byte[] fileData = xmlData.getInputStream().readAllBytes();

        try {
            Digest digest = new SHA256Digest();
            digest.update(fileData, 0, fileData.length);

            byte[] signatureDigest = new byte[digest.getDigestSize()];
            int outOff = 0;
            digest.doFinal(signatureDigest, outOff);
            TimeStampRequestGenerator tsRequestGenerator = new TimeStampRequestGenerator();
            tsRequestGenerator.setCertReq(true);

            TimeStampRequest tsRequest = tsRequestGenerator.generate(TSPAlgorithms.SHA256, signatureDigest);

            TimestampHandler ts = new TimestampHandler();

            byte[] responseBytes = ts.getTimestamp(tsRequest.getEncoded(), "https://test.ditec.sk/TSAServer/tsa.aspx");
            TimeStampResponse tsResponse = new TimeStampResponse(responseBytes);
        } catch (TSPException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        PrintWriter out = response.getWriter();
        out.println(xmlData);
    }
}
