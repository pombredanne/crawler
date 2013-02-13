package com.github.vmorev.crawler.beans;

import com.github.vmorev.crawler.utils.AWSHelper;
import com.github.vmorev.crawler.utils.HttpHelper;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * User: Valentin_Morev
 * Date: 10.01.13
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Article {
    @JsonIgnore
    private String text;

    private String url;
    private String title;
    private long date;
    private String author;
    private String siteId;
    private String articleCrawler;
    private List<String> tags;
    private String summary;
    private List<Map> media;
    private String xpath;
    private double spamScore;
    private double staticRank;
    private double fresh;

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

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<Map> getMedia() {
        return media;
    }

    public void setMedia(List<Map> media) {
        this.media = media;
    }

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public double getSpamScore() {
        return spamScore;
    }

    public void setSpamScore(double spamScore) {
        this.spamScore = spamScore;
    }

    public double getStaticRank() {
        return staticRank;
    }

    public void setStaticRank(double staticRank) {
        this.staticRank = staticRank;
    }

    public double getFresh() {
        return fresh;
    }

    public void setFresh(double fresh) {
        this.fresh = fresh;
    }
}
