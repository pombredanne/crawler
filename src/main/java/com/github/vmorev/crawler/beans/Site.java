package com.github.vmorev.crawler.beans;

import com.github.vmorev.crawler.utils.HttpHelper;

/**
 * User: Valentin_Morev
 * Date: 10.01.13
 */
@org.codehaus.jackson.annotate.JsonIgnoreProperties(ignoreUnknown = true)
public class Site {
    public static final String VAR_S3_BUCKET = "site.s3";
    public static final String VAR_SQS_QUEUE = "site.sqs";
    public static final String VAR_SDB_DOMAIN = "site.sdb";

    private String url;
    private String externalId;
    private String newArticlesCrawler;
    private String oldArticlesCrawler;
    private long lastCheckDate;
    private long checkInterval;
    private boolean archiveStored;

    public static String generateId(String url) {
        String safeUrl = url.replaceAll("[^A-Za-z0-9]", "-").replaceAll("(-)\\1+", "$1");
        safeUrl = safeUrl.endsWith("-") ? safeUrl.substring(0, safeUrl.length() - 1) : safeUrl;
        return HttpHelper.encode(safeUrl);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getLastCheckDate() {
        return lastCheckDate;
    }

    public void setLastCheckDate(long lastCheckDate) {
        this.lastCheckDate = lastCheckDate;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public boolean getArchiveStored() {
        return archiveStored;
    }

    public void setArchiveStored(boolean archiveStored) {
        this.archiveStored = archiveStored;
    }

    public String getNewArticlesCrawler() {
        return newArticlesCrawler;
    }

    public void setNewArticlesCrawler(String newArticlesCrawler) {
        this.newArticlesCrawler = newArticlesCrawler;
    }

    public String getOldArticlesCrawler() {
        return oldArticlesCrawler;
    }

    public void setOldArticlesCrawler(String oldArticlesCrawler) {
        this.oldArticlesCrawler = oldArticlesCrawler;
    }

    public long getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(long checkInterval) {
        this.checkInterval = checkInterval;
    }

}
