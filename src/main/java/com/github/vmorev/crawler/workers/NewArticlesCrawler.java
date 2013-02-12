package com.github.vmorev.crawler.workers;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.sitecrawler.SiteCrawler;
import com.github.vmorev.crawler.utils.AWSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NewArticlesCrawler extends AbstractWorker {
    private static final Logger log = LoggerFactory.getLogger(NewArticlesCrawler.class);
    protected String articleSQSName;
    protected String siteSQSName;
    protected String siteS3Name;
    protected boolean isTest;

    protected void performWork() throws InterruptedException, ExecutionFailureException {
        AWSHelper helper;
        ReceiveMessageResult result;
        try {
            helper = new AWSHelper();
            if (!isTest) {
                articleSQSName = helper.getConfig().getSQSArticle();
                siteSQSName = helper.getConfig().getSQSSite();
                siteS3Name = helper.getConfig().getS3Site();
            }
            //timeout 5 minutes
            result = helper.getSQS().receiveMessage(siteSQSName, 60*5);
        } catch (Exception e) {
            String message = "FAIL. " + NewArticlesCrawler.class.getSimpleName() + ". Initialization failure";
            log.error(message, e);
            throw new ExecutionFailureException(message, e);
        }

        for (Message m : result.getMessages()) {
            Site site = null;
            try {
                site = helper.getSQS().decodeMessage(m, Site.class);
                if (System.currentTimeMillis() > (site.getLastCheckDate() + site.getCheckInterval())) {
                    //load articles
                    SiteCrawler crawler = (SiteCrawler) Class.forName(site.getNewArticlesCrawler()).newInstance();
                    List<Article> articles = crawler.getNewArticles(site);
                    for (Article article : articles) {
                        //TODO MINOR sqsClient.sendMessageBatch()
                        helper.getSQS().sendMessage(articleSQSName, article);
                        log.info("SUCCESS. " + NewArticlesCrawler.class.getSimpleName() + ". ARTICLE ADDED TO SQS " + article.getUrl());
                        if (isTest) break;
                    }
                    //update site in s3
                    site.setLastCheckDate(System.currentTimeMillis());
                    helper.getS3().saveJSONObject(siteS3Name, Site.generateId(site.getUrl()), site);
                    log.info("SUCCESS. " + NewArticlesCrawler.class.getSimpleName() + ". SITE UPDATED IN S3 " + site.getUrl());
                }
                //remove from queue
                helper.getSQS().deleteMessage(siteSQSName, m.getReceiptHandle());
            } catch (Exception e) {
                String message = "FAIL. " + NewArticlesCrawler.class.getSimpleName() + ". SITE FAILED " + (site != null ? site.getUrl() : "null");
                log.error(message, e);
                throw new ExecutionFailureException(message, e);
            }
        }
    }

}
