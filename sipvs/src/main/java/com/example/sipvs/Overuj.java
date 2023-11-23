package com.example.sipvs;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;


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

            boolean namespaceCheck = checkNamespace(rootElement);
            if (!namespaceCheck) {
                out.println("overenie datovej obalky: nespravny namespace");
            }

            out.println("overenie prebehlo uspesne!");


        } catch (Exception e) {
            out.println("Error encountered");
        }

    }

    private static boolean checkNamespace(Element rootElement) {
        String xzep = "xmlns:xzep";
        String ds = "xmlns:ds";
        String xzep_ns = "http://www.ditec.sk/ep/signature_formats/xades_zep/v1.0";
        String ds_ns = "http://www.w3.org/2000/09/xmldsig#";

        String temp1 = rootElement.getAttribute(xzep);
        String temp2 = rootElement.getAttribute(ds);

        boolean hasCorrectAttributes = rootElement.hasAttribute(xzep) &&
                rootElement.hasAttribute(ds);

        boolean hasCorrectNamespaces = xzep_ns.equals(rootElement.getAttribute(xzep)) &&
                ds_ns.equals(rootElement.getAttribute(ds));

        return hasCorrectAttributes && hasCorrectNamespaces;
    }

}