package com.github.vmorev.crawler.workers;

import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.sitecrawler.SiteCrawler;
import com.github.vmorev.crawler.utils.AWSHelper;
import com.github.vmorev.crawler.utils.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArticleContentCrawler extends AbstractWorker {
    private static final Logger log = LoggerFactory.getLogger(ArticleContentCrawler.class);

    protected void performWork() throws InterruptedException, ExecutionFailureException {
        AWSHelper helper;
        ReceiveMessageResult result;
        try {
            helper = new AWSHelper();
            ReceiveMessageRequest request = new ReceiveMessageRequest(helper.getSQSQueueArticleContent());
            //timeout 3 minutes
            request.setVisibilityTimeout(60 * 3);
            //TODO MINOR request.setMaxNumberOfMessages();

            result = helper.getSQS().receiveMessage(request);
        } catch (Exception e) {
            String message = "FAIL. " + ArticleContentCrawler.class.getSimpleName() + ". Initialization failure";
            log.error(message, e);
            throw new ExecutionFailureException(message, e);
        }

        for (Message m : result.getMessages()) {
            Article article = null;
            try {
                article = JsonHelper.parseJson(m.getBody(), Article.class);
                String key = Article.generateId(article.getSiteId(), article.getUrl());
                //TODO MINOR put all found articles in cache and use it to check
                //check if exist
                if (helper.getS3Object(helper.getS3BucketArticle(), key, Article.class) == null) {
                    SiteCrawler crawler = (SiteCrawler) Class.forName(article.getArticleCrawler()).newInstance();
                    //crawl article
                    article = crawler.getArticle(article);
                    //save article
                    helper.saveS3Object(helper.getS3BucketArticle(), key, article);
                }
                log.info("SUCCESS. " + ArticleContentCrawler.class.getSimpleName() + ". ARTICLE ADDED TO S3 " + article.getUrl());
                //remove article from queue
                helper.getSQS().deleteMessage(new DeleteMessageRequest(helper.getSQSQueueArticleContent(), m.getReceiptHandle()));
            } catch (Exception e) {
                String message = "FAIL. " + ArticleContentCrawler.class.getSimpleName() + ". ARTICLE FAILED " + (article != null ? article.getUrl() : "null");
                log.error(message, e);
                throw new ExecutionFailureException(message, e);
            }
        }
    }

}
