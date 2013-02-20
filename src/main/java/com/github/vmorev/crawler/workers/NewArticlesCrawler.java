package com.github.vmorev.crawler.workers;

import com.github.vmorev.amazon.AmazonService;
import com.github.vmorev.amazon.SDBDomain;
import com.github.vmorev.amazon.SQSQueue;
import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.sitecrawler.DiffbotSiteCrawler;
import com.github.vmorev.crawler.sitecrawler.SiteCrawler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NewArticlesCrawler extends AbstractWorker {
    private static final Logger log = LoggerFactory.getLogger(NewArticlesCrawler.class);
    protected SQSQueue articleQueue;
    protected SQSQueue siteQueue;
    protected SDBDomain siteDomain;
    protected boolean isTest = false;

    protected void performWork() throws InterruptedException, ExecutionFailureException {
        try {
            if (articleQueue == null)
                articleQueue = new SQSQueue(SQSQueue.getConfig().getValue(Article.VAR_SQS_QUEUE));
            if (siteQueue == null)
                siteQueue = new SQSQueue(SQSQueue.getConfig().getValue(Site.VAR_SQS_QUEUE));
            if (siteDomain == null)
                siteDomain = new SDBDomain(SDBDomain.getConfig().getValue(Site.VAR_SDB_DOMAIN));

            //timeout 5 minutes
            siteQueue.receiveMessages(60*5, 1, Site.class, new AmazonService.ListFunc<Site>() {
                public void process(Site site) throws Exception {
                    try {
                        if (System.currentTimeMillis() > (site.getLastCheckDate() + site.getCheckInterval())) {
                            //load articles
                            SiteCrawler crawler = (SiteCrawler) Class.forName(site.getNewArticlesCrawler()).newInstance();
                            List<Article> articles = crawler.getNewArticles(site);

                            for (Article article : articles) {
                                //TODO MINOR sqsClient.sendMessageBatch()
                                articleQueue.sendMessage(article);
                                log.info("SUCCESS. " + NewArticlesCrawler.class.getSimpleName() + ". ARTICLE ADDED TO SQS " + article.getUrl());
                                if (isTest) break;
                            }

                            //update site in s3
                            if (crawler instanceof DiffbotSiteCrawler)
                                site.setExternalId(((DiffbotSiteCrawler) crawler).getExternalId());
                            site.setLastCheckDate(System.currentTimeMillis());

                            siteDomain.saveObject(Site.generateId(site.getUrl()), site);
                            log.info("SUCCESS. " + NewArticlesCrawler.class.getSimpleName() + ". SITE UPDATED IN S3 " + site.getUrl());
                        }
                    } catch (Exception e) {
                        String message = "FAIL. " + NewArticlesCrawler.class.getSimpleName() + ". SITE FAILED " + (site != null ? site.getUrl() : "null");
                        log.error(message, e);
                        throw new ExecutionFailureException(message, e);
                    }
                }
            });
        } catch (Exception e) {
            String message = "FAIL. " + NewArticlesCrawler.class.getSimpleName() + ". Initialization failure";
            log.error(message, e);
            throw new ExecutionFailureException(message, e);
        }
    }

}
