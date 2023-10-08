package com.example.sipvs;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

@WebServlet(name = "uloz", value = "/uloz")
public class Uloz extends HttpServlet{
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        Car car = objectMapper.readValue(sb.toString(), Car.class);

        String xmlData = this.createXml(car);

        response.setContentType("application/xml");
        PrintWriter out = response.getWriter();
        out.println(xmlData);
    }

    private String createXml(Car car){
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root element with namespace
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElementNS("http://sipvs.uzasnyteam.fiit.stuba.sk/", "cars");
            doc.appendChild(rootElement);

            // car
            Element carElement = doc.createElement("car");
            // Attribute isCrashed
            carElement.setAttribute("isCrashed", String.valueOf(car.isCrashed));
            // yearOfManufacture
            Element yearOfManufacture = doc.createElement("yearOfManufacture");
            yearOfManufacture.appendChild(doc.createTextNode(String.valueOf(car.yearOfManufacture)));
            carElement.appendChild(yearOfManufacture);
            // brandName
            Element brandName = doc.createElement("brandName");
            brandName.appendChild(doc.createTextNode(car.brandName));
            carElement.appendChild(brandName);
            // packages
            Element packages = doc.createElement("packages");
            for(Car.Packages packagen : car.packages){
                Element packageElement = doc.createElement("package");
                Element name = doc.createElement("packageName");
                name.appendChild(doc.createTextNode(packagen.name));
                Element description = doc.createElement("packageDescription");
                description.appendChild(doc.createTextNode(packagen.description));

                packageElement.appendChild(name);
                packageElement.appendChild(description);

                packages.appendChild(packageElement);
            }

            carElement.appendChild(packages);
            rootElement.appendChild(carElement);

            // convert the document to string
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            return writer.getBuffer().toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
