package com.example.sipvs;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;

@WebServlet(name = "validuj", value = "/validuj")
public class Validuj extends HttpServlet {

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String xmlData = request.getParameter("fileUpload");
            String xsdPath = "./car.xsd";
            // Create a SchemaFactory and specify the XML schema language (XSD)
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // Load the XSD schema from the provided path
            Schema schema = schemaFactory.newSchema(new StreamSource(xsdPath));

            // Create a validator from the schema
            Validator validator = schema.newValidator();

            // Validate the XML data by parsing it
            validator.validate(new StreamSource(new StringReader(xmlData)));

            // If validation succeeds, return true

        } catch (Exception e) {
            // Validation failed; log the error (e.getMessage()) or handle it as needed.
            // You can also throw an exception if you want to handle it higher up the call stack.
            e.printStackTrace();

        }
    }
}
