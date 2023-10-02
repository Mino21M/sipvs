package com.example.sipvs;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;


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

        String xmlData = "<cars><car>" +
                "<yearOfManufacture>" + car.yearOfManufacture + "</yearOfManufacture>" +
                "<brandName>" + car.brandName + "</brandName>" +
                "<isCrashed>" + (car.isCrashed != null ? "true" : "false") + "</isCrashed>";

        if (!car.packages.isEmpty()) {
            xmlData += "<packages>";
            for (Car.Packages packages: car.packages) {
                xmlData += "<package>" +
                        "<packageName>" + packages.name + "</packageName>" +
                        "<packageDescription>" + packages.description + "</packageDescription>" +
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
