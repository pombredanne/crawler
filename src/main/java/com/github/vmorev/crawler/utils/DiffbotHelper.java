package com.github.vmorev.crawler.utils;

import com.github.vmorev.amazon.utils.ConfigStorage;

import java.util.Map;

/**
 * User: Valentin_Morev
 * Date: 21.01.13
 */
public class DiffbotHelper {
    private static final String CONFIG_FILE = "diffbot.json";
    private static final String TOKEN = "token";

    private Map<String, String> config;

    public DiffbotHelper() {
        config = ConfigStorage.loadMap(CONFIG_FILE, true);
    }

    public String getToken() {
        return config.get(TOKEN);
    }
}
