package com.example.sipvs;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;


@MultipartConfig(location = "/tmp", maxFileSize = 1024*1024, maxRequestSize = 2*1024*1024, fileSizeThreshold = 1024*1024)
@WebServlet(name="transformuj", value="/transformuj")
public class Transformuj extends HttpServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Part xmlData = request.getPart("uploadedFile");
            // Create a TransformerFactory
            TransformerFactory transformerFactory = TransformerFactory.newInstance();

            // Load the XSLT stylesheet from the provided path
            Source xslt = new StreamSource(new StringReader(Extended.xslt));

            // Create a Transformer for the stylesheet
            Transformer transformer = transformerFactory.newTransformer(xslt);

            // Create a Source for the XML data
            Source xml = new StreamSource(xmlData.getInputStream());

            // Create a StringWriter to capture the transformed output
            StringWriter outputWriter = new StringWriter();
            Result outputResult = new StreamResult(outputWriter);

            // Perform the transformation
            transformer.transform(xml, outputResult);

            // Get the transformed HTML as a string
            String transformedHtml = outputWriter.toString();
            PrintWriter out = response.getWriter();
            out.println(transformedHtml);
        } catch (Exception e) {
            // Transformation failed; log the error (e.getMessage()) or handle it as needed.
            // You can also throw an exception if you want to handle it higher up the call stack.
            e.printStackTrace();
            PrintWriter out = response.getWriter();
            out.println("Error occurred");
        }
    }
}
