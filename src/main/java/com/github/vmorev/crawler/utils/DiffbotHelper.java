package com.github.vmorev.crawler.utils;

import com.github.vmorev.crawler.utils.ConfigStorage;

import java.io.IOException;
import java.util.Map;

/**
 * User: Valentin_Morev
 * Date: 21.01.13
 */
public class DiffbotHelper {
    private static final String CONFIG_FILE = "diffbot.json";
    private static final String TOKEN = "token";

    private Map<String, String> config;

    public DiffbotHelper() throws IOException {
        config = ConfigStorage.getInstance(CONFIG_FILE, Map.class, false);
    }

    public String getToken() {
        return config.get(TOKEN);
    }
}
