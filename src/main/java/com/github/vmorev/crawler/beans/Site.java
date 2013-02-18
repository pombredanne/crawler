package com.github.vmorev.crawler.beans;

import com.github.vmorev.crawler.utils.HttpHelper;

/**
 * User: Valentin_Morev
 * Date: 10.01.13
 */
@org.codehaus.jackson.annotate.JsonIgnoreProperties(ignoreUnknown = true)
public class Site extends SDBItem {
    private String url;
    private String externalId;
    private String newArticlesCrawler;
    private String oldArticlesCrawler;
    private long lastCheckDate;
    private long latestArticleDate;
    private long checkInterval;
    private boolean isArchiveStored;

    public static String generateId(String url) {
        return HttpHelper.encode(url.replace("://",".").replace("/","."));
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

    public boolean isArchiveStored() {
        return isArchiveStored;
    }

    public void setArchiveStored(boolean archiveStored) {
        isArchiveStored = archiveStored;
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

    public long getLatestArticleDate() {
        return latestArticleDate;
    }

    public void setLatestArticleDate(long latestArticleDate) {
        this.latestArticleDate = latestArticleDate;
    }
}
