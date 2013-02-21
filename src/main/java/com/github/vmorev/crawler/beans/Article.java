package com.github.vmorev.crawler.beans;

import com.github.vmorev.crawler.utils.HttpHelper;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

/**
 * User: Valentin_Morev
 * Date: 10.01.13
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Article {
    public static final String VAR_S3_BUCKET = "article.s3";
    public static final String VAR_SQS_QUEUE = "article.sqs";
    public static final String VAR_SDB_DOMAIN = "article.sdb";

    private String text;
    private String url;
    private String title;
    private String date;
    private String author;
    private String siteId;
    private String articleCrawler;
    private List<String> tags;
    private String summary;
    private List<ArticleMedia> media;
    private String xpath;
    private double spamScore;
    private double staticRank;
    private double fresh;
    private long generatedDate;

    public static String generateId(String url) {
        String safeUrl = url.replaceAll("[^A-Za-z0-9]", "-").replaceAll("(-)\\1+", "$1");
        safeUrl = safeUrl.endsWith("-") ? safeUrl.substring(0, safeUrl.length() - 1) : safeUrl;
        return HttpHelper.encode(safeUrl);
    }

    public Article() {
        this.generatedDate = System.currentTimeMillis();
    }

    public long getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(long generatedDate) {
        this.generatedDate = generatedDate;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
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

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<ArticleMedia> getMedia() {
        return media;
    }

    public void setMedia(List<ArticleMedia> media) {
        this.media = media;
    }
}
