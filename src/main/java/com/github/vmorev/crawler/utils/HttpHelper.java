package com.github.vmorev.crawler.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * User: Valentin_Morev
 * Date: 11.01.13
 *
 * This class handles all logic with GET and POST requests
 */
public class HttpHelper {
    private static final int BUFFER_SIZE = 65536;

    /**
     * Method to send GET request and receive response as a string
     * @param urlString - GET request
     * @return - response in a string format
     * @throws IOException in case of communication issues
     */
    public static String getResponse(String urlString) throws IOException {
        URL url = new URL(urlString);
        return inputStreamToString(url.openStream(), "UTF8");
    }

    /**
     * Method to send POST request and receive response as a string
     * @param urlString - url to send a request to
     * @param params - list of post parameters
     * @return - response in a string format
     * @throws IOException in case of communication issues
     */
    public static String postResponse(String urlString, String params) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("charset", "utf-8");
        connection.setRequestProperty("Content-Length", "" + Integer.toString(params.getBytes().length));
        connection.setUseCaches(false);
        DataOutputStream outStream = new DataOutputStream(connection.getOutputStream ());
        outStream.writeBytes(params);
        flush(outStream);
        close(outStream);
        return inputStreamToString(connection.getInputStream(), "UTF8");
    }

    //TODO MINOR LIBRARY this and all private methods to external library
    public static String inputStreamToString(InputStream inStream, String encoding)
            throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        StringBuilder outString = new StringBuilder();
        try {
            int bytesRead;
            while ((bytesRead = inStream.read(buffer)) != -1) {
                outString.append(new String(buffer, 0, bytesRead, encoding));
            }
        } finally {
            close(inStream);
        }
        return outString.toString();
    }

    private static void close(Closeable stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    private static void flush(Flushable stream) {
        try {
            if (stream != null) {
                stream.flush();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    public static String encode(String url) {
        String encodedUrl = url;
        try {
            encodedUrl = URLEncoder.encode(url, "UTF8");
        } catch (UnsupportedEncodingException e) {
        }
        return encodedUrl;
    }
}
