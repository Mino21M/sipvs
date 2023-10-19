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
                <xsl:output method="html" version="1.0" indent="no" omit-xml-declaration="yes" encoding="windows-1250"/>
             
                 <!-- Template to match the root and apply templates to each car element -->
                 <xsl:template match="/">
                     <html>
                         <head></head>
                         <body style="width: 100%; height: 100%; margin: 2;">
                             <div style="background: lightgray; background-color: lightgray;"> 
                                <div style="background: green; background-color: green; text-align: center; color: white; padding: 1em 0;">
                                     <h1>Car Information Form</h1>
                                </div>
                                <xsl:apply-templates select="//tns:car"/>
                             </div>
                         </body>
                     </html>
                 </xsl:template>
             
                 <!-- Template to match each car element and display its information -->
                 <xsl:template match="tns:car">
                    <div style="padding: 2em;background: white;margin: 2em 0;border-radius: 8px;box-shadow: 0 0 10px lightgray;">
                        <label for="year" style="display: block; margin-bottom: 1em;">Year of Manufacture:</label>
                        <input style="display: block; margin-bottom: 1em; width: 100%; border: solid 1px lightgray;" type="number" id="year" name="yearOfManufacture" read-only="true" disabled="true">
                          <xsl:attribute name="value">
                            <xsl:value-of select="tns:yearOfManufacture"/>
                          </xsl:attribute>
                        </input>
                
                        <label for="brand" style="display: block; margin-bottom: 1em;">Brand Name:</label>
                        <input style="display: block; margin-bottom: 1em; width: 100%; border: solid 1px lightgray;" type="text" id="brand" name="brandName" read-only="true" disabled="true">
                          <xsl:attribute name="value">
                            <xsl:value-of select="tns:brandName"/>
                          </xsl:attribute>
                        </input>
                
                        <label for="crash" style="display: block; margin-bottom: 1em;">Car Crashed:</label>
                        <input style="display: block; margin-bottom: 1em; border: solid 1px lightgray;" type="checkbox" id="crash" name="isCrashed" read-only="true" disabled="true">
                            <xsl:if test="contains(./@isCrashed,'true')">
                                <xsl:attribute name="checked">
                                </xsl:attribute>
                            </xsl:if>
                        </input>
                
                        <div id="packages-container" style="margin-bottom: 10px;">
                            <xsl:apply-templates select="tns:packages/tns:package"/>
                        </div>
                    </div>
                 </xsl:template>
             
                 <xsl:template match="tns:package">
                    <label for="packageName" style="display: inline-block; margin-bottom: 1em; width: 20%;">Package Name:</label>
                    <input style="display: inline-block; margin-bottom: 1em; width: 25%; border: solid 1px lightgray;" type="text" id="packageName" name="packageNames" read-only="true" disabled="true">
                      <xsl:attribute name="value">
                        <xsl:value-of select="tns:packageName"/>
                      </xsl:attribute>
                    </input>
                    <label for="packageDescription" style="display: inline-block; margin-bottom: 1em; width: 20%;">Package Description:</label>
                    <input style="display: inline-block; margin-bottom: 1em; width: 25%; border: solid 1px lightgray;" type="text" id="packageDescriptions" name="packageDescriptions" read-only="true" disabled="true">
                      <xsl:attribute name="value">
                        <xsl:value-of select="tns:packageDescription"/>
                      </xsl:attribute>
                    </input>
                 </xsl:template>
             </xsl:stylesheet>""";

    public static String pdf = "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KE1pY3Jvc29mdCBXb3JkIC0gRG9jdW1lbnQyKSAvUHJvZHVjZXIgKG1hY09TIFZlcnNpb24gMTQuMCBcKEJ1aWxkIDIzQTM0NFwpIFF1YXJ0eiBQREZDb250ZXh0KQovQ3JlYXRvciAoV29yZCkgL0NyZWF0aW9uRGF0ZSAoRDoyMDIzMTAxODE3NDcwMVowMCcwMCcpIC9Nb2REYXRlIChEOjIwMjMxMDE4MTc0NzAxWjAwJzAwJykKPj4KZW5kb2JqCnhyZWYKMCAxNAowMDAwMDAwMDAwIDY1NTM1IGYgCjAwMDAwMDAyNjAgMDAwMDAgbiAKMDAwMDAwMzIwOCAwMDAwMCBuIAowMDAwMDAwMDIyIDAwMDAwIG4gCjAwMDAwMDAzNjQgMDAwMDAgbiAKMDAwMDAwMzE3MyAwMDAwMCBuIAowMDAwMDAwMDAwIDAwMDAwIG4gCjAwMDAwMDMzNDAgMDAwMDAgbiAKMDAwMDAwMDQ2MSAwMDAwMCBuIAowMDAwMDAzMjkxIDAwMDAwIG4gCjAwMDAwMDM4NjggMDAwMDAgbiAKMDAwMDAwMzUyNiAwMDAwMCBuIAowMDAwMDA0MTA0IDAwMDAwIG4gCjAwMDAwMTI1ODEgMDAwMDAgbiAKdHJhaWxlcgo8PCAvU2l6ZSAxNCAvUm9vdCA5IDAgUiAvSW5mbyAxMyAwIFIgL0lEIFsgPDJhY2VlMmVkODhkMDk0MDAxYWZiMDZjMzFlZjg1ZGQ0Pgo8MmFjZWUyZWQ4OGQwOTQwMDFhZmIwNmMzMWVmODVkZDQ+IF0gPj4Kc3RhcnR4cmVmCjEyNzk2CiUlRU9GCg==";
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
