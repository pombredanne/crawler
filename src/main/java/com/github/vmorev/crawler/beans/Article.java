package com.github.vmorev.crawler.beans;

import com.github.vmorev.crawler.utils.AWSHelper;
import com.github.vmorev.crawler.utils.HttpHelper;

/**
 * User: Valentin_Morev
 * Date: 10.01.13
 */
@org.codehaus.jackson.annotate.JsonIgnoreProperties(ignoreUnknown = true)
public class Article {
    private String url;
    private String text;
    private String title;
    private String cDate;
    private String author;
    private String siteId;
    private String articleCrawler;

    //TODO MINOR DIFFBOT check if next page param is required
    //private String nextPage;
    //private String numPages;

    public static String generateId(String url) {
        return HttpHelper.encode(url.replace("://",".").replace("/",".")) + AWSHelper.S3Service.S3_NAME_SUFFIX;
    }

    public String getArticleCrawler() {
        return articleCrawler;
    }

    public void setArticleCrawler(String articleCrawler) {
        this.articleCrawler = articleCrawler;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getcDate() {
        return cDate;
    }

    public void setcDate(String cDate) {
        this.cDate = cDate;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }
}
