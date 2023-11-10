package com.example.sipvs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TimestampHandler {
    private String errorMessage = "OK";
    private static final String TS_QUERY_MIME_TYPE = "application/timestamp-query";
    private static final String TS_REPLY_MIME_TYPE = "application/timestamp-reply";

    public byte[] getTimestamp(byte[] tsRequest, String tsUrl) {
        try {
            URL url = new URL(tsUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", TS_QUERY_MIME_TYPE);
            connection.setDoOutput(true);

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(tsRequest);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String contentType = connection.getContentType();
                if (contentType != null && contentType.toLowerCase().equals(TS_REPLY_MIME_TYPE.toLowerCase())) {
                    try (InputStream inputStream = connection.getInputStream();
                         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            byteArrayOutputStream.write(buffer, 0, bytesRead);
                        }
                        return byteArrayOutputStream.toByteArray();
                    }
                } else {
                    throw new Exception("Incorrect response mimetype: " + contentType);
                }
            } else {
                throw new Exception("HTTP error code: " + responseCode);
            }
        } catch (IOException ex) {
            ex.printStackTrace(); // Print the exception for debugging
            this.errorMessage = ex.toString();
            return null;
        } catch (Exception ex) {
            ex.printStackTrace(); // Print the exception for debugging
            this.errorMessage = ex.toString();
            return null;
        }
    }


    public String getErrorMessage() {
        return this.errorMessage;
    }

}
