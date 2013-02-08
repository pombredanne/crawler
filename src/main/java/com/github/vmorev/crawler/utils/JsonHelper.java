package com.github.vmorev.crawler.utils;

import com.github.vmorev.crawler.beans.Site;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * User: Valentin_Morev
 * Date: 11.01.13
 *
 * This class is used to encapsulate Jackson implementation of Json parsing
 */
public class JsonHelper {

    private static ObjectMapper getMapper() {
        return new ObjectMapper();
    }

    public static <T> T parseJson(String json, Class<T> clazz) throws IOException {
        return getMapper().readValue(json, clazz);
    }

    public static <T> T parseJson(String json, TypeReference typeReference) throws IOException {
        return getMapper().readValue(json, typeReference);
    }

    public static <T> T parseJson(URL jsonResourceURL, Class<T> clazz) throws IOException{
        return getMapper().readValue(jsonResourceURL, clazz);
    }

    public static <T> T parseJson(URL jsonResourceURL, TypeReference typeReference) throws IOException {
        return getMapper().readValue(jsonResourceURL, typeReference);
    }

    public static <T> T parseJson(File jsonFile, Class<T> clazz) throws IOException{
        return getMapper().readValue(jsonFile, clazz);
    }

    public static <T> T parseJson(File file, TypeReference typeReference) throws IOException {
        return getMapper().readValue(file, typeReference);
    }

    public static <T> T parseJson(InputStream inStream, Class<T> clazz) throws IOException{
        return getMapper().readValue(inStream, clazz);
    }

    public static <T> T parseJson(InputStream inStream, TypeReference typeReference) throws IOException {
        return getMapper().readValue(inStream, typeReference);
    }

    public static String parseObject(Object object) throws IOException {
        return getMapper().writeValueAsString(object);
    }
}
