package com.example.sipvs;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.StringReader;
import java.util.ArrayList;


class Car {
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

public class Extended {

    public static String xsd = """
            <?xml version="1.0" encoding="UTF-8"?>
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
                  targetNamespace="http://sipvs.uzasnyteam.fiit.stuba.sk/"
                  xmlns:tns="http://sipvs.uzasnyteam.fiit.stuba.sk/"
                  elementFormDefault="qualified">

                <!-- Define a new root element that can contain multiple car elements -->
                <xs:element name="cars">
                    <xs:complexType>
                        <xs:sequence>
                            <xs:element maxOccurs="unbounded" ref="tns:car"/>
                        </xs:sequence>
                    </xs:complexType>
                </xs:element>

                <!-- Define the car element -->
                <xs:element name="car">
                    <xs:complexType>
                        <xs:sequence>
                            <xs:element name="yearOfManufacture" type="xs:int"/>
                            <xs:element name="brandName" type="xs:string"/>
                            <xs:element name="packages">
                                <xs:complexType>
                                    <xs:sequence>
                                        <xs:element maxOccurs="unbounded" name="package">
                                            <xs:complexType>
                                                <xs:sequence>
                                                    <xs:element name="packageName" type="xs:string"/>
                                                    <xs:element name="packageDescription" type="xs:string"/>
                                                </xs:sequence>
                                            </xs:complexType>
                                        </xs:element>
                                    </xs:sequence>
                                </xs:complexType>
                            </xs:element>
                        </xs:sequence>
                      <xs:attribute name="isCrashed"  type="xs:boolean"/>
                    </xs:complexType>
                </xs:element>

            </xs:schema>""";

    public static String xslt = """
            <?xml version="1.0" encoding="UTF-8"?>
             <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                version="1.0" 
                xmlns:tns="http://sipvs.uzasnyteam.fiit.stuba.sk/">
             
                 <!-- Template to match the root and apply templates to each car element -->
                 <xsl:template match="/">
                     <html>
                         <head>
                             <title>Cars Information2</title>
                             <style>
                                 body {
                                     font-family: 'Arial', sans-serif;
                                     background-color: #f4f4f4;
                                     margin: 0;
                                     padding: 0;
                                     width: 100%;
                                 }
                         
                                 header {
                                     background: #50b3a2;
                                     color: white;
                                     text-align: center;
                                     padding: 1em 0;
                                 }
                         
                                 container {
                                     width: 50%;
                                     margin: auto;
                                 }
                         
                                 #uloz {
                                     padding: 2em;
                                     background: #fff;
                                     margin: 2em 0;
                                     border-radius: 8px;
                                     box-shadow: 0 0 10px #ccc;
                                 }
                         
                                 label, input {
                                     display: block;
                                     margin-bottom: 1em;
                                 }
                         
                                 input[type="text"],
                                 input[type="number"],
                                 input[type="file"] {
                                     width: calc(100% - 22px);
                                     padding: 10px;
                                     border: solid 1px #ddd;
                                     border-radius: 5px;
                                 }
                         
                                 a {
                                     width: 100%;
                                     padding: 10px;
                                     border: none;
                                     color: #fff;
                                     background: #28a745;
                                     cursor: pointer;
                                     border-radius: 5px;
                                     display: block;
                                     margin-bottom: 1em;
                                     text-decoration: none;
                                     text-align: center;
                                 }
                         
                                 a:hover {
                                     background: #218838;
                                 }
                                 /* [other styles] */
                         
                                 .package-row {
                                     margin-bottom: 10px;
                                 }
                         
                                 .package-row label, .package-row input {
                                     display: inline-block;
                                 }
                         
                                 .package-row label {
                                     width: 20%;
                                 }
                         
                                 .package-row input {
                                     width: 25%;
                                 }
                         
                                 #add-package {
                                     cursor: pointer;
                                     background-color: #007bff;
                                     color: white;
                                     padding: 5px 10px;
                                     border: none;
                                     border-radius: 5px;
                                     margin-bottom: 5px;
                                     width: 100%;
                                 }
                         
                                 #add-package:hover {
                                     background-color: #0056b3;
                                 }
                             </style>
                         </head>
                         <body>
                             <header>
                                 <h1>Car Information Form</h1>
                             </header>
                             <xsl:apply-templates select="//tns:car"/>
                         </body>
                     </html>
                 </xsl:template>
             
                 <!-- Template to match each car element and display its information -->
                 <xsl:template match="tns:car">
                    <div id="uloz">
                        <label for="year">Year of Manufacture:</label>
                        <input type="number" id="year" name="yearOfManufacture" read-only="true" disabled="true">
                          <xsl:attribute name="value">
                            <xsl:value-of select="tns:yearOfManufacture"/>
                          </xsl:attribute>
                        </input>
                
                        <label for="brand">Brand Name:</label>
                        <input type="text" id="brand" name="brandName" read-only="true" disabled="true">
                          <xsl:attribute name="value">
                            <xsl:value-of select="tns:brandName"/>
                          </xsl:attribute>
                        </input>
                
                        <label for="crash">Car Crashed:</label>
                        <input type="checkbox" id="crash" name="isCrashed" read-only="true" disabled="true">
                            <xsl:if test="contains(./@isCrashed,'true')">
                                <xsl:attribute name="checked">
                                </xsl:attribute>
                            </xsl:if>
                        </input>
                
                        <div id="packages-container" class="package-row">
                            <xsl:apply-templates select="tns:packages/tns:package"/>
                        </div>
                    </div>
                 </xsl:template>
             
                 <xsl:template match="tns:package">
                    <label for="packageName">Package Name:</label>
                    <input type="text" id="packageName" name="packageNames" read-only="true" disabled="true">
                      <xsl:attribute name="value">
                        <xsl:value-of select="tns:packageName"/>
                      </xsl:attribute>
                    </input>
                    <label for="packageDescription">Package Description:</label>
                    <input type="text" id="packageDescriptions" name="packageDescriptions" read-only="true" disabled="true">
                      <xsl:attribute name="value">
                        <xsl:value-of select="tns:packageDescription"/>
                      </xsl:attribute>
                    </input>
                 </xsl:template>
             </xsl:stylesheet>""";

    static public String validuj(StreamSource source) {
        try {
            // Create a SchemaFactory and specify the XML schema language (XSD)
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // Load the XSD schema from the provided path
            Schema schema = schemaFactory.newSchema(new StreamSource(new StringReader(Extended.xsd)));

            // Create a validator from the schema
            Validator validator = schema.newValidator();

            // Validate the XML data by parsing it
            validator.validate(source);

            return "1";
        } catch (Exception e) {
            // Validation failed; log the error (e.getMessage()) or handle it as needed.
            // You can also throw an exception if you want to handle it higher up the call stack.
            e.printStackTrace();
            return e.toString();
        }
    }
}
