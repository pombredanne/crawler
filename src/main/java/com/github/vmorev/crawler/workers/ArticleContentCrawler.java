package com.github.vmorev.crawler.workers;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.sitecrawler.SiteCrawler;
import com.github.vmorev.crawler.utils.AWSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArticleContentCrawler extends AbstractWorker {
    private static final Logger log = LoggerFactory.getLogger(ArticleContentCrawler.class);
    protected String articleSQSName;
    protected String articleS3Name;

    protected void performWork() throws InterruptedException, ExecutionFailureException {
        AWSHelper helper;
        ReceiveMessageResult result;
        try {
            helper = new AWSHelper();
            if (articleSQSName == null)
                articleSQSName = helper.getConfig().getSQSArticle();
            if (articleS3Name == null)
                articleS3Name = helper.getConfig().getS3Article();
            //timeout 3 minutes
            result = helper.getSQS().receiveMessage(articleSQSName, 60 * 3);
        } catch (Exception e) {
            String message = "FAIL. " + ArticleContentCrawler.class.getSimpleName() + ". Initialization failure";
            log.error(message, e);
            throw new ExecutionFailureException(message, e);
        }

        for (Message m : result.getMessages()) {
            Article article = null;
            try {
                article = helper.getSQS().decodeMessage(m, Article.class);
                String key = Article.generateId(article.getSiteId(), article.getUrl());
                //TODO MINOR put all found articles in cache and use it to check
                //check if exist
                if (helper.getS3().getJSONObject(articleS3Name, key, Article.class) == null) {
                    SiteCrawler crawler = (SiteCrawler) Class.forName(article.getArticleCrawler()).newInstance();
                    //crawl article
                    article = crawler.getArticle(article);
                    //save article
                    helper.getS3().saveJSONObject(articleS3Name, key, article);
                }
                log.info("SUCCESS. " + ArticleContentCrawler.class.getSimpleName() + ". ARTICLE ADDED TO S3 " + article.getUrl());
                //remove article from queue
                helper.getSQS().deleteMessage(articleSQSName, m.getReceiptHandle());
            } catch (Exception e) {
                String message = "FAIL. " + ArticleContentCrawler.class.getSimpleName() + ". ARTICLE FAILED " + (article != null ? article.getUrl() : m.getBody());
                log.error(message, e);
                throw new ExecutionFailureException(message, e);
            }
        }
    }

}
