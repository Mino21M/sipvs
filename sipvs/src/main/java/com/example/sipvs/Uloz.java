package com.example.sipvs;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import org.apache.commons.text.StringEscapeUtils;


import java.io.File;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jlibs.xml.sax.XMLDocument;
//import jlibs.xml.X
//import jlibs.xml.


import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Array;
import java.util.*;

@WebServlet(name = "uloz", value = "/uloz")
public class Uloz extends HttpServlet{

    static class Car {
        @JsonProperty("yearOfManufacture")
        int yearOfManufacture = 2000;
        @JsonProperty("brandName")
        String brandName = "";
        @JsonProperty("isCrashed")
        Boolean isCrashed = false;
        @JsonProperty("packages")
        ArrayList<Packages> packages = new ArrayList<>();

        static class Packages {
            @JsonProperty("name")
            String name = "";
            @JsonProperty("description")
            String description = "";
        }
    }

    /*public interface xsdtoxml {
        public static void main(String[] pArgs) {
            try {
                String filename = "out.xsd";
                // instance.

                final Document doc = loadXsdDocument(filename);

                //Find the docs root element and use it to find the targetNamespace
                final Element rootElem = doc.getDocumentElement();
                String targetNamespace = null;
                if (rootElem != null && rootElem.getNodeName().equals("xs:schema"))
                {
                    targetNamespace = rootElem.getAttribute("targetNamespace");
                }


                //Parse the file into an XSModel object
                org.apache.xerces.xs.XSModel xsModel = new XSParser().parse(filename);

                //Define defaults for the XML generation
                XSInstance instance = new XSInstance();
                instance.minimumElementsGenerated = 1;
                instance.maximumElementsGenerated = 1;
                instance.generateDefaultAttributes = true;
                instance.generateOptionalAttributes = true;
                instance.maximumRecursionDepth = 0;
                instance.generateAllChoices = true;
                instance.showContentModel = true;
                instance.generateOptionalElements = true;

                //Build the sample xml doc
                //Replace first param to XMLDoc with a file input stream to write to file
                QName rootElement = new QName(targetNamespace, "out");
                XMLDocument sampleXml = new XMLDocument(new StreamResult(System.out), true, 4, null);
                instance.generate(xsModel, rootElement, sampleXml);
            } catch (TransformerConfigurationException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public static Document loadXsdDocument(String inputName) {
            final String filename = inputName;

            final DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            factory.setValidating(false);
            factory.setIgnoringElementContentWhitespace(true);
            factory.setIgnoringComments(true);
            Document doc = null;

            try {
                final DocumentBuilder builder = factory.newDocumentBuilder();
                final File inputFile = new File(filename);
                doc = builder.parse(inputFile);
            } catch (final Exception e) {
                e.printStackTrace();
                // throw new ContentLoadException(msg);
            }

            return doc;
        }

    }*/

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        // Convert JSON to Java Object using Jackson
        ObjectMapper objectMapper = new ObjectMapper();
        Car car = objectMapper.readValue(sb.toString(), Car.class);



        String xmlData = "<cars>" +
                "<car isCrashed=\"" + (car.isCrashed != null ? "true" : "false") + "\">" +
                "<yearOfManufacture>" + StringEscapeUtils.escapeXml11(String.valueOf(car.yearOfManufacture)) + "</yearOfManufacture>" +
                "<brandName>" + StringEscapeUtils.escapeXml11(car.brandName) + "</brandName>";

        if (!car.packages.isEmpty()) {
            xmlData += "<packages>";
            for (Car.Packages packages: car.packages) {
                xmlData += "<package>" +
                        "<packageName>" + StringEscapeUtils.escapeXml11(packages.name) + "</packageName>" +
                        "<packageDescription>" + StringEscapeUtils.escapeXml11(packages.description) + "</packageDescription>" +
                        "</package>";
            }
            xmlData += "</packages>";
        }

        xmlData += "</car></cars>";

        response.setContentType("application/xml");
        PrintWriter out = response.getWriter();
        out.println(xmlData);
    }

}
