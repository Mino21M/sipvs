package com.example.sipvs;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.List;


@MultipartConfig(location = "/tmp", maxFileSize = 1024*1024, maxRequestSize = 2*1024*1024, fileSizeThreshold = 1024*1024)
@WebServlet(name="transformuj", value="/transformuj")
public class Transformuj extends HttpServlet {

    public class StreamSourceContainer {
        private final StreamSource streamSource1;
        private final StreamSource streamSource2;

        public StreamSourceContainer(StreamSource streamSource1, StreamSource streamSource2) {
            this.streamSource1 = streamSource1;
            this.streamSource2 = streamSource2;
        }

        public StreamSource getStreamSource1() {
            return streamSource1;
        }

        public StreamSource getStreamSource2() {
            return streamSource2;
        }
    }


    public StreamSourceContainer duplicate(InputStream data) throws IOException {
        // Assume originalSource is your original StreamSource
        StreamSource originalSource = new StreamSource(data); // for example

        // Read the content of the original StreamSource into a String
        StringBuilder contentBuilder = new StringBuilder();
        try (InputStream inputStream = originalSource.getInputStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                contentBuilder.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

        String content = contentBuilder.toString();

        // Now create new StreamSource instances for each read.
        return new StreamSourceContainer(
                new StreamSource(new StringReader(content)),
                new StreamSource(new StringReader(content))
        );
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String transformedHtml = "";
        Part xmlData = request.getPart("uploadedFile");
        StreamSourceContainer streamSourceContainer = duplicate(xmlData.getInputStream());
        boolean status = Extended.validuj(streamSourceContainer.getStreamSource1());
        if(status) {
            // Create a TransformerFactory
            TransformerFactory transformerFactory = TransformerFactory.newInstance();

            // Load the XSLT stylesheet from the provided path
            Source xslt = new StreamSource(new StringReader(Extended.xslt));

            // Create a Transformer for the stylesheet
            Transformer transformer = null;
            try {
                transformer = transformerFactory.newTransformer(xslt);
            } catch (TransformerConfigurationException e) {
                throw new RuntimeException(e);
            }

            // Create a StringWriter to capture the transformed output
            StringWriter outputWriter = new StringWriter();
            Result outputResult = new StreamResult(outputWriter);

            // Perform the transformation
            try {
                transformer.transform(streamSourceContainer.getStreamSource2(), outputResult);
            } catch (TransformerException e) {
                throw new RuntimeException(e);
            }

            // Get the transformed HTML as a string
            transformedHtml = outputWriter.toString();
        } else {
            transformedHtml = "Invalid XML";
        }

        PrintWriter out = response.getWriter();
        out.println(transformedHtml);
    }
}
