package com.github.vmorev.crawler.awsflow.activity;

import com.github.vmorev.crawler.awsflow.AWSHelper;
import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.sitecrawler.SiteCrawler;
import com.github.vmorev.crawler.sitecrawler.diffbot.DiffbotSiteCrawler;

public class ArticleCrawlerActivitiesImpl implements ArticleCrawlerActivities {

    public void storeContent(Article article) throws Exception {
        SiteCrawler crawler = new DiffbotSiteCrawler();
        Article updatedArticle = crawler.getArticle(article);
        AWSHelper awsHelper = new AWSHelper();
        awsHelper.saveS3Object(awsHelper.getS3ArticleBucket(), Article.generateId(article.getSiteId(), article.getUrl()), updatedArticle);
    }

    public void storeMetadata(Article article) {
        //TODO MAJOR implement storing data to graph
    }
}
