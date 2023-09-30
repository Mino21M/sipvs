<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Car Information Form</title>
    </head>
    <body>
        <h1>Car Information Form</h1>

        <!-- Formular pre ulozenie auta -->
        <form action="/uloz" method="post">
            <label for="year">Year of Manufacture:</label>
            <input type="number" id="year" name="yearOfManufacture" required><br>

            <label for="brand">Brand Name:</label>
            <input type="text" id="brand" name="brandName" required><br>

            <label for="crash">Car Crashed:</label>
            <input type="checkbox" id="crash" name="isCrashed"><br>

            <label for="packages">Select Packages:</label><br>
            <input type="checkbox" id="safety" name="packages" value="Safety Package">
            <label for="safety">Safety Package</label><br>
            <input type="checkbox" id="entertainment" name="packages" value="Entertainment Package">
            <label for="entertainment">Entertainment Package</label><br>

            <input type="submit" value="Uloz">
        </form>

        <!-- formular pre validaciu -->
        <form action="/validuj" method="post" enctype="multipart/form-data">
            <label for="file">Upload a File:</label>
            <input type="file" id="file" name="uploadedFile"><br>

            <input type="submit" value="Validuj">
        </form>

        <!-- Formular pre zobrazenie -->
        <form action="/transformuj" method="post" enctype="multipart/form-data">
            <label for="file">Upload a File:</label>
            <input type="file" id="file" name="uploadedFile"><br>

            <input type="submit" value="Transformuj">
        </form>
    </body>
</html>

