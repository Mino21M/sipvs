<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
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
    <form action="/transformuj" method="post" enctype="multipart/form-data">
        <label for="file">Upload a File:</label>
        <input type="file" id="file" name="uploadedFile"><br>

        <input type="submit" value="Transformuj">
    </form>
</div>
</body>
</html>
