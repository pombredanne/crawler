package com.github.vmorev.crawler.awsflow.activity;

import com.github.vmorev.crawler.awsflow.AWSHelper;
import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.sitecrawler.SiteCrawler;
import com.github.vmorev.crawler.sitecrawler.diffbot.DiffbotSiteCrawler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArticleCrawlerActivitiesImpl implements ArticleCrawlerActivities {
    private static final Logger log = LoggerFactory.getLogger(SiteCrawlerActivitiesImpl.class);

    public void storeContent(Article article) throws Exception {
        String key = Article.generateId(article.getSiteId(), article.getUrl());
        try {
            SiteCrawler crawler = new DiffbotSiteCrawler();
            Article updatedArticle = crawler.getArticle(article);
            AWSHelper awsHelper = new AWSHelper();
            awsHelper.saveS3Object(awsHelper.getS3ArticleBucket(), key, updatedArticle);
        } catch (Exception e) {
            log.info("FAIL. GET ARTICLE CONTENT. KEY=" + key, e);
            throw e;
        }
    }

    public void storeMetadata(Article article) {
        //TODO MAJOR implement storing data to graph
    }
}
