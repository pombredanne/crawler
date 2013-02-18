package com.github.vmorev.crawler.workers;

import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.sitecrawler.SiteCrawler;
import com.github.vmorev.crawler.utils.amazon.AmazonService;
import com.github.vmorev.crawler.utils.amazon.S3Service;
import com.github.vmorev.crawler.utils.amazon.SQSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArticleContentCrawler extends AbstractWorker {
    private static final Logger log = LoggerFactory.getLogger(ArticleContentCrawler.class);
    protected SQSService.Queue<Article> articleQueue;
    protected S3Service.S3Bucket<Article> articleBucket;

    protected void performWork() throws InterruptedException, ExecutionFailureException {
        try {
            S3Service s3 = new S3Service();
            SQSService sqs = new SQSService();

            if (articleQueue == null)
                articleQueue = sqs.getQueue(sqs.getConfig().getArticle(), Article.class);

            if (articleBucket == null)
                articleBucket = s3.getBucket(s3.getConfig().getArticle(), Article.class);

            //timeout 3 minutes
            articleQueue.receiveMessages(new AmazonService.ListFunc<Article>() {
                public void process(Article article) throws Exception {
                    String key = Article.generateId(article.getUrl());
                    //TODO MINOR put all found articles in cache and use it to check
                    try {
                        if (articleBucket.getObject(key) == null) {
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
            }, 60 * 3, 1);
        } catch (Exception e) {
            String message = "FAIL. " + ArticleContentCrawler.class.getSimpleName() + ". Initialization failure";
            log.error(message, e);
            throw new ExecutionFailureException(message, e);
        }
    }

}
