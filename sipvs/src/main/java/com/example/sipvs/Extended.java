package com.example.sipvs;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.StringReader;

public class Extended {
    public static String xsd = """
<?xml version="1.0" encoding="UTF-8"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">

    <!-- Define a new root element that can contain multiple car elements -->
    <xs:element name="cars">
        <xs:complexType>
            <xs:sequence>
                <xs:element maxOccurs="unbounded" ref="car"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>

    <!-- Define the car element -->
    <xs:element name="car">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="yearOfManufacture" type="xs:int"/>
                <xs:element name="brandName" type="xs:string"/>
                <xs:element name="isCrashed" type="xs:boolean"/>
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
        </xs:complexType>
    </xs:element>

</xs:schema>
""";

    public static String xslt = """
<?xml version="1.0" encoding="UTF-8"?>
 <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
 
     <!-- Template to match the root and apply templates to each car element -->
     <xsl:template match="/">
         <html>
             <head>
                 <title>Cars Information</title>
                 <style>
                     body {
                         font-family: 'Arial', sans-serif;
                         background-color: #f4f4f4;
                         margin: 0;
                         padding: 0;
                     }
                     h1 {
                         text-align: center;
                         color: #333;
                     }
                     table {
                         width: 50%;
                         margin: 20px auto;
                         border-collapse: collapse;
                     }
                     th, td {
                         border: 1px solid #ddd;
                         padding: 8px;
                         text-align: left;
                     }
                     th {
                         background-color: #4CAF50;
                         color: white;
                     }
                     p {
                         margin: 0;
                     }
                     .back-button {
                         display: block;
                         width: 100px;
                         margin: 20px auto;
                         padding: 10px;
                         background-color: #4CAF50;
                         color: white;
                         text-align: center;
                         cursor: pointer;
                         text-decoration: none;
                     }
                 </style>
             </head>
             <body>
                 <h1>Cars Information</h1>
                 <xsl:apply-templates select="//car"/>
                 <a class="back-button" href="/">Go Back</a>
             </body>
         </html>
     </xsl:template>
 
     <!-- Template to match each car element and display its information -->
     <xsl:template match="car">
         <table>
             <tr>
                 <th>Year of Manufacture</th>
                 <td><xsl:value-of select="yearOfManufacture"/></td>
             </tr>
             <tr>
                 <th>Brand Name</th>
                 <td><xsl:value-of select="brandName"/></td>
             </tr>
             <tr>
                 <th>Crashed</th>
                 <td><xsl:value-of select="isCrashed"/></td>
             </tr>
             <tr>
                 <th>Packages</th>
                 <td><xsl:apply-templates select="packages/package"/></td>
             </tr>
         </table>
     </xsl:template>
 
     <xsl:template match="package">
         <p>
             <strong>Name:</strong> <xsl:value-of select="packageName"/><br/>
             <strong>Description:</strong> <xsl:value-of select="packageDescription"/>
         </p>
     </xsl:template>
 </xsl:stylesheet>""";

    static public boolean validuj(StreamSource source) {
        try {
            // Create a SchemaFactory and specify the XML schema language (XSD)
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // Load the XSD schema from the provided path
            Schema schema = schemaFactory.newSchema(new StreamSource(new StringReader(Extended.xsd)));

            // Create a validator from the schema
            Validator validator = schema.newValidator();

            // Validate the XML data by parsing it
            validator.validate(source);

            return true;
        } catch (Exception e) {
            // Validation failed; log the error (e.getMessage()) or handle it as needed.
            // You can also throw an exception if you want to handle it higher up the call stack.
            e.printStackTrace();
            return false;
        }
    }
}
