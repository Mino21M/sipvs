package com.example.sipvs;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet(name = "uloz", value = "/uloz")
public class Uloz extends HttpServlet{
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Collect form data from the request
        String yearOfManufacture = request.getParameter("yearOfManufacture");
        String brandName = request.getParameter("brandName");
        String isCrashed = request.getParameter("isCrashed");

        // Extract packages
        List<String[]> selectedPackages = new ArrayList<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("packages[")) {
                int index = Integer.parseInt(key.substring(9, key.indexOf(']', 9)));
                if (selectedPackages.size() <= index) {
                    selectedPackages.add(new String[2]); // Each package is a String array with 2 elements: name and description
                }
                if (key.endsWith("[name]")) {
                    selectedPackages.get(index)[0] = entry.getValue()[0];
                } else if (key.endsWith("[description]")) {
                    selectedPackages.get(index)[1] = entry.getValue()[0];
                }
            }
        }

        // Create an XML document based on the XSD schema
        String xmlData = "<car>" +
                "<yearOfManufacture>" + yearOfManufacture + "</yearOfManufacture>" +
                "<brandName>" + brandName + "</brandName>" +
                "<isCrashed>" + (isCrashed != null ? "true" : "false") + "</isCrashed>";

        if (!selectedPackages.isEmpty()) {
            xmlData += "<packages>";
            for (String[] packageValue : selectedPackages) {
                xmlData += "<package>" +
                        "<packageName>" + packageValue[0] + "</packageName>" +
                        "<packageDescription>" + packageValue[1] + "</packageDescription>" +
                        "</package>";
            }
            xmlData += "</packages>";
        }

        xmlData += "</car>";

        response.setContentType("application/xml");
        PrintWriter out = response.getWriter();
        out.println(xmlData);
    }

}
