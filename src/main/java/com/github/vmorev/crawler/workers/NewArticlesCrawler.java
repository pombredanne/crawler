package com.github.vmorev.crawler.workers;

import com.amazonaws.services.sqs.model.*;
import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.sitecrawler.SiteCrawler;
import com.github.vmorev.crawler.utils.AWSHelper;
import com.github.vmorev.crawler.utils.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NewArticlesCrawler extends AbstractWorker {
    private static final Logger log = LoggerFactory.getLogger(NewArticlesCrawler.class);

    protected void performWork() throws InterruptedException, ExecutionFailureException {
        AWSHelper helper;
        ReceiveMessageResult result;
        try {
            helper = new AWSHelper();
            ReceiveMessageRequest request = new ReceiveMessageRequest(helper.getSQSQueueSite());
            //timeout 5 minutes
            request.setVisibilityTimeout(60 * 5);
            //TODO MINOR request.setMaxNumberOfMessages();
            result = helper.getSQS().receiveMessage(request);
        } catch (Exception e) {
            String message = "FAIL. " + NewArticlesCrawler.class.getSimpleName() + ". Initialization failure";
            log.error(message, e);
            throw new ExecutionFailureException(message, e);
        }

        for (Message m : result.getMessages()) {
            Site site = null;
            try {
                site = JsonHelper.parseJson(m.getBody(), Site.class);
                if (System.currentTimeMillis() > (site.getLastCheckDate() + site.getCheckInterval())) {
                    //load articles
                    SiteCrawler crawler = (SiteCrawler) Class.forName(site.getNewArticlesCrawler()).newInstance();
                    List<Article> articles = crawler.getNewArticles(site);
                    for (Article article : articles) {
                        //TODO MINOR sqsClient.sendMessageBatch()
                        helper.getSQS().sendMessage(new SendMessageRequest(helper.getSQSQueueArticleContent(), JsonHelper.parseObject(article)));
                        log.info("SUCCESS. " + NewArticlesCrawler.class.getSimpleName() + ". ARTICLE ADDED TO SQS " + article.getUrl());
                    }
                    //update site in s3
                    site.setLastCheckDate(System.currentTimeMillis());
                    helper.saveS3Object(helper.getS3BucketSite(), Site.generateId(site.getUrl()), site);
                    log.info("SUCCESS. " + NewArticlesCrawler.class.getSimpleName() + ". SITE UPDATED IN S3" + site.getUrl());
                }
                //remove from queue
                helper.getSQS().deleteMessage(new DeleteMessageRequest(helper.getSQSQueueSite(), m.getReceiptHandle()));
            } catch (Exception e) {
                String message = "FAIL. " + NewArticlesCrawler.class.getSimpleName() + ". SITE FAILED " + (site != null ? site.getUrl() : "null");
                log.error(message, e);
                throw new ExecutionFailureException(message, e);
            }
        }
    }

}
