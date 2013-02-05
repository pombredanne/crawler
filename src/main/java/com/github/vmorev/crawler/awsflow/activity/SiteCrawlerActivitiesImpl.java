package com.github.vmorev.crawler.awsflow.activity;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.simpleworkflow.flow.ActivityExecutionContext;
import com.amazonaws.services.simpleworkflow.flow.ActivityExecutionContextProvider;
import com.amazonaws.services.simpleworkflow.flow.ActivityExecutionContextProviderImpl;
import com.github.vmorev.crawler.awsflow.AWSHelper;
import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.sitecrawler.SiteCrawler;
import com.github.vmorev.crawler.utils.JsonHelper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CancellationException;

public class SiteCrawlerActivitiesImpl implements SiteCrawlerActivities {

    public Site getUpdatedSite(Site site) throws IOException {
        AWSHelper awsHelper = new AWSHelper();
        AmazonS3 s3 = awsHelper.createS3Client();
        return JsonHelper.parseJson(s3.getObject(awsHelper.getS3SiteBucket(), Site.generateId(site.getUrl())).getObjectContent(), Site.class);
    }

    public long storeNewArticlesList(Site site) throws Exception {
        SiteCrawler crawler = (SiteCrawler) Class.forName(site.getNewArticlesCrawler()).newInstance();
        List<Article> articles = crawler.getNewArticles(site);
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
            if (awsHelper.getS3Object(awsHelper.getS3ArticleBucket(), key) == null) {
                awsHelper.saveS3Object(awsHelper.getS3ArticleBucket(), key, article);
            } else {
                //TODO MINOR overwrite only empty fields
            }
        }
    }

    public long storeArchivedArticlesList(Site site) throws Exception {
        //TODO MAJOR add heartbeat
        /*
        try {
            ActivityExecutionContextProvider provider = new ActivityExecutionContextProviderImpl();
            ActivityExecutionContext context = provider.getActivityExecutionContext();
            while (true) {
                Thread.sleep(1000);
                context.recordActivityHeartbeat(currentArticle);
            }
        } catch(CancellationException ex) {
            throw ex;
        }
        */
        SiteCrawler crawler = (SiteCrawler) Class.forName(site.getOldArticlesCrawler()).newInstance();
        List<Article> articles = crawler.getArchivedArticles(site);
        if (articles.size() > 0) {
            storeArticles(articles);

            site.setArchiveStored(true);
            AWSHelper awsHelper = new AWSHelper();
            awsHelper.saveS3Object(awsHelper.getS3SiteBucket(), Site.generateId(site.getUrl()), site);
        }
        return articles.size();
    }

}
