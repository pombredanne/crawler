package com.github.vmorev.crawler.utils.amazon;

import com.github.vmorev.crawler.utils.ConfigStorage;

import java.util.Map;

/**
 * User: Valentin_Morev
 * Date: 14.02.13
 */
public class AmazonConfig {
    private static final String ARTICLE = "article";
    private static final String SITE = "site";
    private static final String LOGS = "logs";
    private static final String LOGS_STAT = "logsStat";
    private static final String CONFIG_FILE = "aws.json";
    private static final String ACCESS_KEY = "accessKey";
    private static final String SECRET_KEY = "secretKey";

    private Map<String, String> configStorage;

    public AmazonConfig() {
        configStorage = ConfigStorage.getInstance(CONFIG_FILE, Map.class, false);
    }

    protected String getAccessKey() {
        return configStorage.get(ACCESS_KEY);
    }

    protected String getSecretKey() {
        return configStorage.get(SECRET_KEY);
    }

    public String getLogs() {
        return configStorage.get(LOGS);
    }

    public String getArticle() {
        return configStorage.get(ARTICLE);
    }

    public String getSite() {
        return configStorage.get(SITE);
    }

    public String getLogsStat() {
        return configStorage.get(LOGS_STAT);
    }
}
