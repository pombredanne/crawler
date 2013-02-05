package com.github.vmorev.crawler.awsflow.workflow;

import com.amazonaws.services.simpleworkflow.flow.annotations.Asynchronous;
import com.amazonaws.services.simpleworkflow.flow.core.Promise;
import com.github.vmorev.crawler.awsflow.activity.ArticleCrawlerActivitiesClient;
import com.github.vmorev.crawler.awsflow.activity.ArticleCrawlerActivitiesClientImpl;
import com.github.vmorev.crawler.beans.Article;

public class ArticleCrawlerWorkflowImpl implements ArticleCrawlerWorkflow {

    ArticleCrawlerActivitiesClient client = new ArticleCrawlerActivitiesClientImpl();

    public void startArticleProcessing(Article article) {
        Promise<Void> timer = storeContent(article);

        storeMetadata(article, timer);
    }

    @Asynchronous
    private Promise<Void> storeContent(Article article) {
        return client.storeContent(article);
    }

    @Asynchronous
    private Promise<Void> storeMetadata(Article article, Promise<?> timers) {
        return client.storeMetadata(article);
    }

}