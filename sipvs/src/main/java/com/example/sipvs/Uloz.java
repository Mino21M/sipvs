package com.example.sipvs;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;

import java.io.IOException;

@WebServlet(name = "uloz", value = "/uloz")
public class Uloz extends HttpServlet{
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
        response.setContentType("text/html");

        // Hello
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html><html><head><title>Car Information Form</title></head><body><h1>Car Information Form</h1><form action=\"uloz\" method=\"post\"><label for=\"year\">Year of Manufacture:</label><input type=\"number\" id=\"year\" name=\"yearOfManufacture\" required><br><label for=\"brand\">Brand Name:</label><input type=\"text\" id=\"brand\" name=\"brandName\" required><br><label for=\"crash\">Car Crashed:</label><input type=\"checkbox\" id=\"crash\" name=\"isCrashed\"><br><label for=\"packages\">Select Packages:</label><br><input type=\"checkbox\" id=\"safety\" name=\"packages\" value=\"Safety Package\"><label for=\"safety\">Safety Package</label><br><input type=\"checkbox\" id=\"entertainment\" name=\"packages\" value=\"Entertainment Package\"><label for=\"entertainment\">Entertainment Package</label><br><input type=\"submit\" value=\"Submit\"></form></body></html>");
    }
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Collect form data from the request
        String yearOfManufacture = request.getParameter("yearOfManufacture");
        String brandName = request.getParameter("brandName");
        String isCrashed = request.getParameter("isCrashed");
        String[] selectedPackages = request.getParameterValues("packages");

        // Create an XML document based on the XSD schema
        String xmlData = "<car>" +
                "<yearOfManufacture>" + yearOfManufacture + "</yearOfManufacture>" +
                "<brandName>" + brandName + "</brandName>" +
                "<isCrashed>" + (isCrashed != null ? "true" : "false") + "</isCrashed>";

        if (selectedPackages != null) {
            for (String packageValue : selectedPackages) {
                xmlData += "<packages><package>" +
                        "<packageName>" + packageValue + "</packageName>" +
                        "<packageDescription>Package Description Goes Here</packageDescription>" +
                        "</package></packages>";
            }
        }

        xmlData += "</car>";

        response.setContentType("application/xml");
        PrintWriter out = response.getWriter();
        out.println(xmlData);
    }
}
