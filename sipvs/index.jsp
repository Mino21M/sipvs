<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <script type="text/javascript" src="/config.js"></script>
    <script type="text/javascript" src="/dCommon.min.js"></script>
    <script type="text/javascript" src="/dSigXades.min.js"></script>
    <title>Car Information Form</title>
    <style>
        body {
            font-family: 'Arial', sans-serif;
            background-color: #f4f4f4;
            margin: 0;
            padding: 0;
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

        form {
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

        input[type="submit"] {
            width: 100%;
            padding: 10px;
            border: none;
            color: #fff;
            background: #28a745;
            cursor: pointer;
            border-radius: 5px;
        }

        input[type="submit"]:hover {
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
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            document.getElementById('uloz-form').addEventListener('submit', function(event) {
                event.preventDefault();

                let form = document.getElementById('uloz-form');
                let formData = new FormData(form);


                let json = {
                    yearOfManufacture: Number(formData.get('yearOfManufacture')),
                    brandName: formData.get('brandName'),
                    isCrashed: !!formData.get('isCrashed'),
                    packages: []
                }

                let names = formData.getAll('packageNames');
                let descriptions = formData.getAll('packageDescriptions');

                for(let i = 0; i < names.length; i++) {
                    json.packages.push({ name: names[i], description: descriptions[i] });
                }
                console.log(json);

                fetch(form.action, {
                    method: 'POST',
                    body: JSON.stringify(json),
                    headers: {
                        'Content-Type': 'application/json'
                    },
                })
                    .then(response => {
                        if(response.ok) {
                            return response.text(); // assuming the response is in text format
                        } else {
                            throw new Error('Network response was not ok');
                        }
                    })
                    .then(data => {
                        // Create a Blob from the text
                        var blob = new Blob([data], { type: 'application/xml' });

                        // Create a link element
                        var link = document.createElement('a');

                        // Set the download attribute with a value (the name of the desired file)
                        link.download = 'car.xml';

                        // Create a URL to the Blob and set it as the href attribute
                        link.href = window.URL.createObjectURL(blob);

                        // Append the link to the body
                        document.body.appendChild(link);

                        // Trigger the download
                        link.click();

                        // Remove the link from the document
                        document.body.removeChild(link);
                    })
                    .catch(error => {
                        console.error('There was a problem with the fetch operation:', error);
                        alert('Failure: Could not validate. Please try again.');
                    });
            });

            document.getElementById('validuj-form').addEventListener('submit', function(event) {
                event.preventDefault();

                let form = event.target;
                let formData = new FormData(form);

                fetch(form.action, {
                    method: 'POST',
                    body: formData
                })
                    .then(response => {
                        if(response.ok) {
                            return response.text(); // or response.json() if the response is in JSON format
                        } else {
                            throw new Error('Network response was not ok');
                        }
                    })
                    .then(data => {
                        alert('Success: ' + data);
                    })
                    .catch(error => {
                        console.error('There was a problem with the fetch operation:', error);
                        alert('Failure: Could not validate. Please try again.');
                    });
            });

            document.getElementById('podpisuj-form').addEventListener('submit', function(event) {
                event.preventDefault();

                let reader = new FileReader();
                reader.onload = function(e)
                {
                    let xsd = `<?xml version="1.0" encoding="UTF-8"?>
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

            </xs:schema>`;
                    let xslt = `<?xml version="1.0" encoding="UTF-8"?>
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
             </xsl:stylesheet>`;
                    let namespace = "http://sipvs.uzasnyteam.fiit.stuba.sk/";
                    let xs_ref = "http://www.w3.org/2001/XMLSchema";
                    let xslt_ref = "http://www.w3.org/1999/XSL/Transform";
                    ditec.dSigXadesJs.deploy(null, new Callback(function(){

                        ditec.dSigXadesJs.initialize(new Callback(function(){

                            ditec.dSigXadesJs.addXmlObject("xml1", "xmlDesc", e.target.result, xsd, namespace, xs_ref, xslt, xslt_ref, new Callback(function(){

                                ditec.dSigXadesJs.sign20("signatureId", "http://www.w3.org/2001/04/xmlenc#sha256", "urn:oid:1.3.158.36061701.1.2.3", "dataEnvelopeId", "http://dataenvelopeuri/", "dataEnvelopeDescr", new Callback(function(){

                                    ditec.dSigXadesJs.getSignedXmlWithEnvelope(new Callback(function(ret) {
                                        alert(ret);
                                    }));
                                }));
                            }));
                        }));
                    }));
                };
                reader.readAsText(document.getElementById("podpisuj").files[0]);
        });

        function addPackageRow() {
            var container = document.getElementById('packages-container');
            var newRow = document.createElement('div');
            newRow.className = 'package-row';

            newRow.innerHTML = '<label for="packageName">Package Name:</label>' +
                '<input type="text" id="packageName" name="packageNames" required>' +
                '<label for="packageDescription">Package Description:</label>' +
                '<input type="text" id="packageDescription" name="packageDescriptions" required>';

            container.appendChild(newRow);
        }})

        function Callback(onSuccess) {
            this.onSuccess = onSuccess;
            this.onError = function(e) {
                alert("Chyba: " + e);
            }
        }
    </script>
</head>
<body>
<header>
    <h1>Car Information Form</h1>
</header>
<div class="container">
    <!-- Form for saving cars -->
    <form id="uloz-form" action="/uloz" method="post" content="multipart">
        <label for="year">Year of Manufacture:</label>
        <input type="number" id="year" name="yearOfManufacture" required><br>

        <label for="brand">Brand Name:</label>
        <input type="text" id="brand" name="brandName" required><br>

        <label for="crash">Car Crashed:</label>
        <input type="checkbox" id="crash" name="isCrashed"><br>

        <div id="packages-container" class="package-row">
            <label for="packageName">Package Name:</label>
            <input type="text" id="packageName" name="packageNames" required>
            <label for="packageDescription">Package Description:</label>
            <input type="text" id="packageDescription" name="packageDescriptions" required>
        </div>
        <button type="button" id="add-package" onclick="addPackageRow()">Add Package</button><br>

        <input type="submit" value="Uloz">
    </form>

    <!-- Form for validation -->
    <form  id="validuj-form" action="/validuj" method="post" enctype="multipart/form-data">
        <label for="validuj">Upload a File:</label>
        <input type="file" id="validuj" name="uploadedFile"><br>

        <input type="submit" value="Validuj">
    </form>

    <!-- Form for displaying -->
    <form action="/transformuj" method="post" enctype="multipart/form-data" >
        <label for="file">Upload a File:</label>
        <input type="file" id="file" name="uploadedFile"><br>

        <input type="submit" value="Transformuj">
    </form>

    <!-- Form for signature -->
    <form id="podpisuj-form">
        <label for="file">Upload a File:</label>
        <input type="file" id="podpisuj" name="uploadedFile"><br>

        <input type="submit" value="Podpisuj">
    </form>
</div>
</body>
</html>
