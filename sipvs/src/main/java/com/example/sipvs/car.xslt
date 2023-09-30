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

</xsl:stylesheet>
