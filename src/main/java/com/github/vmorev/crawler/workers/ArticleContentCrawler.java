package com.github.vmorev.crawler.workers;

import com.github.vmorev.amazon.AmazonService;
import com.github.vmorev.amazon.S3Bucket;
import com.github.vmorev.amazon.SQSQueue;
import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.sitecrawler.SiteCrawler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArticleContentCrawler extends AbstractWorker {
    private static final Logger log = LoggerFactory.getLogger(ArticleContentCrawler.class);
    protected SQSQueue articleQueue;
    protected S3Bucket articleBucket;

    protected void performWork() throws InterruptedException, ExecutionFailureException {
        try {
            if (articleQueue == null)
                articleQueue = new SQSQueue(SQSQueue.getConfig().getValue(Article.VAR_SQS_QUEUE));

            if (articleBucket == null)
                articleBucket = new S3Bucket(S3Bucket.getConfig().getValue(Article.VAR_S3_BUCKET));

            //timeout 3 minutes
            articleQueue.receiveMessages(60 * 3, 1, Article.class, new AmazonService.ListFunc<Article>() {
                public void process(Article article) throws Exception {
                    String key = Article.generateId(article.getUrl());
                    //TODO MINOR put all found articles in cache and use it to check
                    try {
                        if (articleBucket.getObject(key, article.getClass()) == null) {
                            SiteCrawler crawler = (SiteCrawler) Class.forName(article.getArticleCrawler()).newInstance();
                            //crawl article
                            article = crawler.getArticle(article);
                            //save article
                            articleBucket.saveObject(key, article);
                        }
                        log.info("SUCCESS. " + ArticleContentCrawler.class.getSimpleName() + ". ARTICLE ADDED TO S3 " + article.getUrl());
                    } catch (Exception e) {
                        String message = "FAIL. " + ArticleContentCrawler.class.getSimpleName() + ". ARTICLE FAILED " + article.getUrl();
                        log.error(message, e);
                        throw new ExecutionFailureException(message, e);
                    }
                }
            });
        } catch (Exception e) {
            String message = "FAIL. " + ArticleContentCrawler.class.getSimpleName() + ". Initialization failure";
            log.error(message, e);
            throw new ExecutionFailureException(message, e);
        }
    }

}
