package com.github.vmorev.crawler.utils;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * User: Valentin_Morev
 * Date: 11.01.13
 *
 * This class is used to encapsulate Jackson implementation of Json parsing
 */
public class JsonHelper {

    private static ObjectMapper getMapper() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper;
    }

    public static <T> T parseJson(String json, Class<T> clazz) throws IOException {
        return getMapper().readValue(json, clazz);
    }

    public static <T> T parseJson(URL jsonResourceURL, Class<T> clazz) throws IOException{
        return getMapper().readValue(jsonResourceURL, clazz);
    }

    public static <T> T parseJson(File jsonFile, Class<T> clazz) throws IOException{
        return getMapper().readValue(jsonFile, clazz);
    }

    public static String parseObject(Object object) throws IOException {
        return getMapper().writeValueAsString(object);
    }
}
