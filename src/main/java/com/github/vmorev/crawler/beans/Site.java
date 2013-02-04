package com.github.vmorev.crawler.beans;

import com.github.vmorev.crawler.awsflow.AWSHelper;
import com.github.vmorev.crawler.utils.HttpHelper;

/**
 * User: Valentin_Morev
 * Date: 10.01.13
 */
@org.codehaus.jackson.annotate.JsonIgnoreProperties(ignoreUnknown = true)
public class Site {
    private String url;
    private String externalId;
    private String newArticlesCrawler;
    private String oldArticlesCrawler;
    private long lcDate;
    private boolean isArchiveStored;

    public static String generateId(String url) {
        return HttpHelper.encode(url) + AWSHelper.S3_NAME_SUFFIX;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getLcDate() {
        return lcDate;
    }

    public void setLcDate(long lcDate) {
        this.lcDate = lcDate;
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
}
