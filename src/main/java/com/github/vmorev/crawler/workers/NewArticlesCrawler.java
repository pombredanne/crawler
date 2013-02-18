package com.github.vmorev.crawler.workers;

import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.sitecrawler.DiffbotSiteCrawler;
import com.github.vmorev.crawler.sitecrawler.SiteCrawler;
import com.github.vmorev.crawler.utils.amazon.AmazonService;
import com.github.vmorev.crawler.utils.amazon.SDBService;
import com.github.vmorev.crawler.utils.amazon.SQSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NewArticlesCrawler extends AbstractWorker {
    private static final Logger log = LoggerFactory.getLogger(NewArticlesCrawler.class);
    protected SQSService.Queue<Article> articleQueue;
    protected SQSService.Queue<Site> siteQueue;
    protected SDBService.Domain<Site> siteDomain;
    protected boolean isTest = false;

    protected void performWork() throws InterruptedException, ExecutionFailureException {
        try {
            SQSService sqs = new SQSService();
            SDBService sdb = new SDBService();

            if (articleQueue == null)
                articleQueue = sqs.getQueue(sqs.getConfig().getArticle(), Article.class);
            if (siteQueue == null)
                siteQueue = sqs.getQueue(sqs.getConfig().getSite(), Site.class);
            if (siteDomain == null)
                siteDomain = sdb.getDomain(sdb.getConfig().getSite(), Site.class);

            //timeout 5 minutes
            siteQueue.receiveMessages(new AmazonService.ListFunc<Site>() {
                public void process(Site site) throws Exception {
                    try {
                        if (System.currentTimeMillis() > (site.getLastCheckDate() + site.getCheckInterval())) {
                            //load articles
                            SiteCrawler crawler = (SiteCrawler) Class.forName(site.getNewArticlesCrawler()).newInstance();
                            List<Article> articles = crawler.getNewArticles(site);

                            long latestArticleDate = 0;
                            for (Article article : articles) {
                                if (article.getDate() > 0 && article.getDate() > latestArticleDate)
                                    latestArticleDate = article.getDate();

                                //TODO MINOR sqsClient.sendMessageBatch()
                                articleQueue.sendMessage(article);
                                log.info("SUCCESS. " + NewArticlesCrawler.class.getSimpleName() + ". ARTICLE ADDED TO SQS " + article.getUrl());
                                if (isTest) break;
                            }

                            //update site in s3
                            if (crawler instanceof DiffbotSiteCrawler)
                                site.setExternalId(((DiffbotSiteCrawler) crawler).getExternalId());
                            if (latestArticleDate > 0 && latestArticleDate > site.getLatestArticleDate())
                                site.setLatestArticleDate(latestArticleDate);
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
            }, 60*5, 1);
        } catch (Exception e) {
            String message = "FAIL. " + NewArticlesCrawler.class.getSimpleName() + ". Initialization failure";
            log.error(message, e);
            throw new ExecutionFailureException(message, e);
        }
    }

}
