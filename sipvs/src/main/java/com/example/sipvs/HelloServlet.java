package com.example.sipvs;

import java.io.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;

public class HelloServlet extends HttpServlet {
    private String message;

    public void init() {
        message = "Hello World!";
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");

        // Hello
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>" + message + "</h1>");
        out.println("</body></html>");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Get the XML data from the request
        String xmlData = request.getParameter("xmlData");

        try {
            // Validate the XML against the XSD (you can use libraries like javax.xml.validation.SchemaFactory)
            //boolean isValid = validateXML(xmlData, "car.xsd"); // Provide the path to your XSD file.

            /*if (isValid) {
                // Perform XSLT transformation
                String transformedHtml = transformXML(xmlData, "car.xsl"); // Provide the path to your XSL file.

                // Set the transformed HTML as the response content
                response.setContentType("text/html");
                PrintWriter out = response.getWriter();
                out.println(transformedHtml);
            } else {
                // Handle validation errors
                response.setContentType("text/plain");
                PrintWriter out = response.getWriter();
                out.println("XML validation failed.");
            }*/
        } catch (Exception e) {
            // Handle exceptions (e.g., parsing, transformation errors)
            response.setContentType("text/plain");
            PrintWriter out = response.getWriter();
            out.println("An error occurred: " + e.getMessage());
        }
    }


    private String transformXML(String xmlData, String xslPath) {
        try {
            // Create a TransformerFactory
            TransformerFactory transformerFactory = TransformerFactory.newInstance();

            // Load the XSLT stylesheet from the provided path
            Source xslt = new StreamSource(xslPath);

            // Create a Transformer for the stylesheet
            Transformer transformer = transformerFactory.newTransformer(xslt);

            // Create a Source for the XML data
            Source xml = new StreamSource(new StringReader(xmlData));

            // Create a StringWriter to capture the transformed output
            StringWriter outputWriter = new StringWriter();
            Result outputResult = new StreamResult(outputWriter);

            // Perform the transformation
            transformer.transform(xml, outputResult);

            // Get the transformed HTML as a string
            String transformedHtml = outputWriter.toString();

            return transformedHtml;
        } catch (Exception e) {
            // Transformation failed; log the error (e.getMessage()) or handle it as needed.
            // You can also throw an exception if you want to handle it higher up the call stack.
            e.printStackTrace();
            return "<html><body>Transformation error occurred</body></html>";
        }
    }


    public void destroy() {
    }
}