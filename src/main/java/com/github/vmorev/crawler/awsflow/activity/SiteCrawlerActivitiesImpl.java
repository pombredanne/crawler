package com.github.vmorev.crawler.awsflow.activity;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.github.vmorev.crawler.sitecrawler.SiteCrawler;
import com.github.vmorev.crawler.awsflow.AWSHelper;
import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.utils.HttpHelper;
import com.github.vmorev.crawler.utils.JsonHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class SiteCrawlerActivitiesImpl implements SiteCrawlerActivities {

    public long storeNewArticlesList(Site site) throws Exception {
        SiteCrawler crawler = (SiteCrawler) Class.forName(site.getNewArticlesCrawler()).newInstance();
        List<Article> articles = crawler.getNewArticles(site);
        //TODO MAJOR TEST crawler failure
        storeArticles(articles);
        return articles.size();
    }

    private void storeArticles(List<Article> articles) throws IOException {
        AWSHelper awsHelper = new AWSHelper();
        AmazonS3 s3 = awsHelper.createS3Client();
        if (!s3.doesBucketExist(awsHelper.getS3ArticleBucket()))
            s3.createBucket(awsHelper.getS3ArticleBucket());
        for (Article article : articles) {
            String key = Article.generateId(article.getSiteId(), article.getUrl());
            if (awsHelper.isS3RewriteAllowed()) {
                storeArticle(key, article);
            } else {
                if (s3.getObject(awsHelper.getS3ArticleBucket(), key) == null) {
                    //TODO MINOR AWS check to overwrite only empty fields
                    storeArticle(key, article);
                } else {
                    //TODO MEDIUM AWS log if already exist and is not saving
                }
            }
        }
    }

    private void storeArticle(String key, Article article) throws IOException {
        AWSHelper awsHelper = new AWSHelper();
        AmazonS3 s3 = awsHelper.createS3Client();

        ObjectMetadata metadata = new ObjectMetadata();
        //TODO MEDIUM AWS think on separation of metadata and content
        //metadata.setUserMetadata();
        s3.putObject(awsHelper.getS3ArticleBucket(), key, stringToInputStream(JsonHelper.parseObject(article)), metadata);
    }

    public long storeArchivedArticlesList(Site site) throws Exception {
        SiteCrawler crawler = (SiteCrawler) Class.forName(site.getOldArticlesCrawler()).newInstance();
        List<Article> articles = crawler.getArchivedArticles(site);
        storeArticles(articles);
        return articles.size();
    }

    //TODO MINOR LIBRARY move to library
    private InputStream stringToInputStream(String str) throws UnsupportedEncodingException {
        return stringToInputStream(str, "UTF8");
    }

    private InputStream stringToInputStream(String str, String encoding)
            throws UnsupportedEncodingException {
        return new ByteArrayInputStream(str.getBytes(encoding));
    }

}
