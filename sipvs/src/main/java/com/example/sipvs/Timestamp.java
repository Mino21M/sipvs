package com.example.sipvs;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import jakarta.servlet.http.Part;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.tsp.*;


@WebServlet(name = "timestamp", value = "/timestamp")
@MultipartConfig
public class Timestamp extends HttpServlet {

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        PrintWriter out = response.getWriter();
        out.println("test");

        Part xmlData = request.getPart("uploadedFile");
        byte[] fileData = xmlData.getInputStream().readAllBytes();

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
        try {
            TimeStampResponse tsResponse = new TimeStampResponse(responseBytes);
        } catch (TSPException e) {
            throw new RuntimeException(e);
        }
    }
}
