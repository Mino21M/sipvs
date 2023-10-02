package com.example.sipvs;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;

@MultipartConfig(location = "/tmp", maxFileSize = 1024*1024, maxRequestSize = 2*1024*1024, fileSizeThreshold = 1024*1024)
@WebServlet(name = "validuj", value = "/validuj")
public class Validuj extends HttpServlet {

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Part xmlData = request.getPart("uploadedFile");
        boolean status = Extended.validuj(new StreamSource(xmlData.getInputStream()));
        PrintWriter out = response.getWriter();
        out.println(status);
    }
}
