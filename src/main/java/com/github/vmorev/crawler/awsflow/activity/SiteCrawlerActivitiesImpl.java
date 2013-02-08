package com.github.vmorev.crawler.awsflow.activity;

import com.amazonaws.services.s3.AmazonS3;
import com.github.vmorev.crawler.awsflow.AWSHelper;
import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.sitecrawler.SiteCrawler;
import com.github.vmorev.crawler.utils.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class SiteCrawlerActivitiesImpl implements SiteCrawlerActivities {
    private static final Logger log = LoggerFactory.getLogger(SiteCrawlerActivitiesImpl.class);

    public Site getUpdatedSite(Site site) throws IOException {
        AWSHelper awsHelper = new AWSHelper();
        AmazonS3 s3 = awsHelper.createS3Client();
        return JsonHelper.parseJson(s3.getObject(awsHelper.getS3SiteBucket(), Site.generateId(site.getUrl())).getObjectContent(), Site.class);
    }

    public long storeNewArticlesList(Site site) throws Exception {
        List<Article> articles;
        try {
            SiteCrawler crawler = (SiteCrawler) Class.forName(site.getNewArticlesCrawler()).newInstance();
            //TODO MAJOR add heartbeat
            articles = crawler.getNewArticles(site);
            storeArticles(articles);
        } catch (Exception e) {
            log.info("FAIL. GET NEW ARTICLES. SITE=" + Site.generateId(site.getUrl()), e);
            throw e;
        }
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
                log.info("SUCCESS. ADD ARTICLE. KEY=" + key);
                awsHelper.saveS3Object(awsHelper.getS3ArticleBucket(), key, article);
            } else {
                log.info("SUCCESS. OVERWRITE ARTICLE. KEY=" + key);
                //TODO MINOR overwrite only empty fields
            }
        }
    }

    public long storeArchivedArticlesList(Site site) throws Exception {
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
        List<Article> articles;
        try {
            SiteCrawler crawler = (SiteCrawler) Class.forName(site.getOldArticlesCrawler()).newInstance();
            //TODO MAJOR add heartbeat
            articles = crawler.getArchivedArticles(site);
            if (articles.size() > 0) {
                storeArticles(articles);

                site.setArchiveStored(true);
                AWSHelper awsHelper = new AWSHelper();
                awsHelper.saveS3Object(awsHelper.getS3SiteBucket(), Site.generateId(site.getUrl()), site);
            }
        } catch (Exception e) {
            log.info("FAIL. GET OLD ARTICLES. SITE=" + Site.generateId(site.getUrl()), e);
            throw e;
        }
        return articles.size();
    }

}
