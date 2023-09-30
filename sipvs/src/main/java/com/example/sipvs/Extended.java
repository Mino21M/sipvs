package com.example.sipvs;

public class Extended {
    public static String xsd = """
<?xml version="1.0" encoding="UTF-8"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">

    <!-- Define the root element -->
    <xs:element name="car">
        <xs:complexType>
            <xs:sequence>
                <!-- Define elements for car information -->
                <xs:element name="yearOfManufacture" type="xs:int"/>
                <xs:element name="brandName" type="xs:string"/>
                <xs:element name="isCrashed" type="xs:boolean"/>

                <!-- Add an element for packages -->
                <xs:element name="packages">
                    <xs:complexType>
                        <xs:sequence>
                            <!-- Define elements for package information -->
                            <xs:element name="package">
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

</xs:schema>""";

    public static String xslt = """
<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template match="/car">
        <html>
            <head>
                <title>Car Information</title>
            </head>
            <body>
                <h1>Car Information</h1>
                <table border="1">
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
                        <td>
                            <xsl:apply-templates select="packages/package"/>
                        </td>
                    </tr>
                </table>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="package">
        <p>
            <strong>Name:</strong> <xsl:value-of select="packageName"/><br/>
            <strong>Description:</strong> <xsl:value-of select="packageDescription"/>
        </p>
    </xsl:template>

</xsl:stylesheet>""";
}
