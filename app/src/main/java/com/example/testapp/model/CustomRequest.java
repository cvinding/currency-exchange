package com.example.testapp.model;

import com.example.testapp.listener.RequestListener;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;

/**
 *
 * @author Christian Vinding Rasmussen
 */
public class CustomRequest {

    private String BASE_URL;

    private boolean CONVERT_XML = false;

    /**
     * CustomRequest() is used for setting a base URL
     * @param BASE_URL a string for the base url of the API
     */
    public CustomRequest(String BASE_URL){
        this.BASE_URL = BASE_URL;
    }

    /**
     * CustomRequest() is used for setting a base URL & telling the class that the API returns XML
     * @param BASE_URL a string for the base url of the API
     * @param CONVERT_XML a boolean for API endpoints which return XML, true if API use XML & false if not
     */
    public CustomRequest(String BASE_URL, boolean CONVERT_XML) {
        this.BASE_URL = BASE_URL;
        this.CONVERT_XML = CONVERT_XML;
    }

    /**
     * sendRequest() is used for sending API requests, it needs to be called on a separate thread of main and is asynchronous
     * @param endpoint is a string for specifying the endpoint, in which to send a request to
     * @param method is a string for specifying the request method, e.g. GET or POST
     * @param listener is a callback interface
     */
    public void sendRequest(String endpoint, String method, RequestListener listener) {
        try {

            // Create the URL for the API
            URL API_URL = new URL(this.BASE_URL + endpoint);

            // Open the HTTP connection
            HttpURLConnection conn = (HttpURLConnection) API_URL.openConnection();

            // Set request method
            conn.setRequestMethod(method);

            // If response code is not 200 OR 304 return an error
            if(conn.getResponseCode() != 200 && conn.getResponseCode() != 304){
                listener.onFailure("Failure: " + conn.getResponseCode(), conn.getResponseCode());
                return;
            }

            // Call onSuccess callback, and return data as JSON ;=)
            listener.onSuccess((this.CONVERT_XML) ? convertXML(new BufferedInputStream(conn.getInputStream())) : createString(new BufferedInputStream(conn.getInputStream())));

            // Disconnect connection
            conn.disconnect();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    private String convertXML(InputStream inputStream) throws IOException {
        XmlToJson xmlToJson = new XmlToJson.Builder(inputStream, null).build();
        inputStream.close();

        return xmlToJson.toString();
    }

    /**
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    private String createString(InputStream inputStream) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));

        // Create a string output
        StringBuilder newString = new StringBuilder();
        for (String line; (line = r.readLine()) != null; ) {
            newString.append(line).append('\n');
        }

        inputStream.close();

        return newString.toString();
    }

}
