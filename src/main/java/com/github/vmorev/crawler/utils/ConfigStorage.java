package com.github.vmorev.crawler.utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: Valentin_Morev
 * Date: 13.01.13
 */
public class ConfigStorage {
    private static volatile Map<String, Object> instances = new ConcurrentHashMap<>();

    /**
     * This method return bean filled with properties readed from JSON file
     * All beans are cached in this singleton
     *
     * @param configName - file to read JSON data from
     * @param clazz      - class to fill with JSON data
     * @return class with JSON data loaded
     * @throws IOException if file operations fail
     */
    public static <T> T getInstance(String configName, Class<T> clazz, boolean reload) throws IOException {
        T tmpInstance = (T) instances.get(configName);
        if (tmpInstance == null) {
            synchronized (ConfigStorage.class) {
                tmpInstance = (T) instances.get(configName);
                if (tmpInstance == null || reload) {
                    if (clazz.getName().equals(Map.class.getName()))
                        tmpInstance = loadMap(configName, clazz);
                    else
                        tmpInstance = load(configName, clazz);
                    instances.remove(configName);
                    instances.put(configName, tmpInstance);
                }
            }
        }
        return tmpInstance;
    }

    private static <T> T load(String configName, Class<T> clazz) throws IOException {
        return JsonHelper.parseJson(ClassLoader.getSystemResource(configName), clazz);
    }

    private static <T> T loadMap(String configName, Class<T> clazz) throws IOException {
        T config = JsonHelper.parseJson(ClassLoader.getSystemResource(configName), clazz);
        try {
            T localConfig = JsonHelper.parseJson(ClassLoader.getSystemResource(configName.replace(".json", ".local.json")), clazz);
            ((Map) config).putAll((Map) localConfig);
        } catch (Throwable e) {
            //just ignore
        }
        try {
            T localConfig = JsonHelper.parseJson(ClassLoader.getSystemResource(configName.replace(".json", ".test.json")), clazz);
            ((Map) config).putAll((Map) localConfig);
        } catch (Throwable e) {
            //just ignore
        }
        return config;
    }

    public static void updateLogger() {
        Properties props = new Properties();
        try {
            props.load(ClassLoader.getSystemResource("log4j.local.properties").openStream());
            if (!props.isEmpty()) {
                LogManager.resetConfiguration();
                PropertyConfigurator.configure(props);
            }
        } catch (IOException e) {
            //just skip, no local config exist
        }
        props = new Properties();
        try {
            props.load(ClassLoader.getSystemResource("log4j.test.properties").openStream());
            if (!props.isEmpty()) {
                LogManager.resetConfiguration();
                PropertyConfigurator.configure(props);
            }
        } catch (IOException e) {
            //just skip, no local config exist
        }
    }
}
