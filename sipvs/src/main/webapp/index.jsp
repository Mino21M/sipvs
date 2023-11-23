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
            background-color: lightgrey;
            margin: 0;
            padding: 0;
        }

        header {
            background: green;
            color: white;
            text-align: center;
            padding: 1em 0;
        }

        form {
            padding: 2em;
            background: white;
            margin: 2em 0;
            box-shadow: 0 0 10px lightgray;
        }

        label, input, select {
            display: block;
            margin-bottom: 1em;
        }

        input[type="text"],
        input[type="number"],
        input[type="file"], select {
            width: 100%;
            padding: 10px;
            border: solid 1px lightgray;
        }

        input[type="submit"] {
            width: 100%;
            padding: 10px;
            border: none;
            color: white;
            background: green;
            cursor: pointer;
        }

        input[type="submit"]:hover {
            background: darkgreen;
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
            background-color: blue;
            color: white;
            padding: 5px 10px;
            border: none;
            border-radius: 5px;
            margin-bottom: 5px;
            width: 100%;
        }

        #add-package:hover {
            background-color: darkblue;
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

            function createRequest (e, xslt, xsd, pdf) {
                let xml = e.target.result;
                let namespace = "http://sipvs.uzasnyteam.fiit.stuba.sk/";
                let xs_ref = "http://www.w3.org/2001/XMLSchema";
                let xslt_ref = "http://www.w3.org/1999/XSL/Transform";
                ditec.dSigXadesJs.deploy(null, new Callback(function(){

                    ditec.dSigXadesJs.initialize(new Callback(function(){

                        ditec.dSigXadesJs.addXmlObject2("xml1", "Formular", xml, xsd, namespace, xs_ref, xslt, xslt_ref, "HTML", new Callback(function(){

                            ditec.dSigXadesJs.addPdfObject("pdf1", "FormularVisual", pdf, "", "http://aljksdlkasjdkl", 2, true, new Callback(function () {

                                ditec.dSigXadesJs.sign20("signatureId", "http://www.w3.org/2001/04/xmlenc#sha256", "urn:oid:1.3.158.36061701.1.2.3", "dataEnvelopeId", "http://dataenvelopeuri/", "dataEnvelopeDescr", new Callback(function(){

                                    ditec.dSigXadesJs.getSignedXmlWithEnvelope(new Callback(function(ret) {
                                        let textToSave = ret;
                                        let hiddenElement = document.createElement('a');
                                        let binaryData = [];
                                        
                                        binaryData.push(textToSave);
                                        hiddenElement.href = window.URL.createObjectURL(new Blob(binaryData, {type: "data:text/xml"}))
                                        hiddenElement.target = '_blank';
                                        hiddenElement.download = 'car_podpisane.xml';
                                        hiddenElement.click();
                                    }));
                                }));
                            }));
                        }));
                    }));
                }));
            }

            document.getElementById('podpisuj-form').addEventListener('submit', async function(event) {
                event.preventDefault();
                let input = document.getElementById("podpisuj");

                let data = new FormData();
                data.append('uploadedFile', input.files[0]);

                let xslt_request = await fetch('/xslt');
                let xsd_request = await fetch('/xsd');
                let pdf_request = await fetch('/pdf', {method: 'POST', body: data})

                let xslt = await xslt_request.text();
                let xsd = await xsd_request.text();
                let pdf = await pdf_request.text();


                let reader = new FileReader();
                reader.onload = (e) => createRequest(e, xslt, xsd, pdf);
                reader.readAsText(input.files[0]);
            });


            document.getElementById('timestamp-form').addEventListener('submit', async function(event) {
                event.preventDefault();
                let input = document.getElementById("timestamp");
                let whichData = document.getElementById("whichData");

                let data = new FormData();
                data.append('whichData', whichData.value);
                data.append('uploadedFile', input.files[0]);

                let timestamp_request = await fetch('/timestamp', {method: 'POST', body: data})

                let textToSave = await timestamp_request.text();
                let hiddenElement = document.createElement('a');
                let binaryData = [];

                binaryData.push(textToSave);
                hiddenElement.href = window.URL.createObjectURL(new Blob(binaryData, {type: "data:text/xml"}))
                hiddenElement.target = '_blank';
                hiddenElement.download = 'car_timestamp.xml';
                hiddenElement.click();
            });

            document.getElementById('overuj-form').addEventListener('submit', async function(event) {
                event.preventDefault();
                let input = document.getElementById("check");

                let data = new FormData();
                data.append('uploadedFile', input.files[0]);

                let timestamp_request = await fetch('/check', {method: 'POST', body: data})

                let textToSave = await timestamp_request.text();
                alert(textToSave);
            });

        })

        function Callback(onSuccess) {
            this.onSuccess = onSuccess;
            this.onError = function(e) {
                alert("Chyba: " + e);
            }
        }

        function addPackageRow() {
            var container = document.getElementById('packages-container');
            var newRow = document.createElement('div');
            newRow.className = 'package-row';

            newRow.innerHTML = '<label for="packageName">Package Name:</label>' +
                '<input type="text" id="packageName" name="packageNames" required>' +
                '<label for="packageDescription">Package Description:</label>' +
                '<input type="text" id="packageDescription" name="packageDescriptions" required>';

            container.appendChild(newRow);
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
    <form action="/transformuj" method="post" enctype="multipart/form-data" >
        <label for="file">Upload a File:</label>
        <input type="file" id="file" name="uploadedFile"><br>

        <input type="submit" value="Transformuj">
    </form>

    <!-- Form for displaying -->
    <form  id="validuj-form" action="/validuj" method="post" enctype="multipart/form-data">
        <label for="validuj">Upload a File:</label>
        <input type="file" id="validuj" name="uploadedFile"><br>

        <input type="submit" value="Validuj">
    </form>

    <!-- Form for signature -->
    <form id="podpisuj-form">
        <label for="file">Upload a File:</label>
        <input type="file" id="podpisuj" name="uploadedFile"><br>

        <input type="submit" value="Podpisuj">
    </form>

    <form id="timestamp-form">
        <label for="file">Upload a File:</label>
        <input type="file" id="timestamp" name="uploadedFile"><br>
        <label for="whichData">Ktore data: </label>
        <select id="whichData" name="whichData">
            <option value="1">Iba signature extrahovany z XML a zahashovany</option>
            <option value="3">Cely subor zahashovany</option>
        </select>

        <input type="submit" value="Timestamp">
    </form>

    <form id="overuj-form">
        <label for="file">Upload a File:</label>
        <input type="file" id="check" name="uploadedFile"><br>
        <input type="submit" value="Overuj">
    </form>
</div>
</body>
</html>
