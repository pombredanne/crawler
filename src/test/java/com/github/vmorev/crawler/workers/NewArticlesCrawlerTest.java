package com.github.vmorev.crawler.workers;

import com.github.vmorev.crawler.AbstractAWSTest;
import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.utils.JsonHelper;
import com.github.vmorev.crawler.utils.amazon.AmazonService;
import com.github.vmorev.crawler.utils.amazon.SDBService;
import com.github.vmorev.crawler.utils.amazon.SQSService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NewArticlesCrawlerTest extends AbstractAWSTest {
    private NewArticlesCrawler crawler;
    private SQSService.Queue<Article> articleQueue;
    private SQSService.Queue<Site> siteQueue;
    private SDBService.Domain<Site> siteDomain;

    @Before
    public void setUp() throws Exception {
        String modifier = "-" + random.nextLong();
        articleName = s3.getConfig().getArticle() + modifier;
        siteName = s3.getConfig().getSite() + modifier;
        articleQueue = sqs.getQueue(siteName, Article.class);
        siteQueue = sqs.getQueue(siteName, Site.class);
        siteDomain = sdb.getDomain(siteName, Site.class);
        articleQueue.createQueue();
        siteQueue.createQueue();
        siteDomain.createDomain();

        crawler = new NewArticlesCrawler();
        crawler.siteDomain = siteDomain;
        crawler.siteQueue = siteQueue;
        crawler.articleQueue = articleQueue;
        crawler.isTest = true;
    }

    @After
    public void cleanUp() throws Exception {
        articleQueue.deleteQueue();
        siteQueue.deleteQueue();
        siteDomain.deleteDomain();
    }

    @Test
    public void testSiteCrawl() throws Exception {
        String fileName = "NewArticlesCrawler.testSiteCrawl.json";
        Site site = JsonHelper.parseJson(ClassLoader.getSystemResource(fileName), Site.class);

        siteQueue.sendMessage(site);
        crawler.performWork();

        final long[] count = new long[1];
        siteQueue.receiveMessages(new AmazonService.ListFunc<Site>() {
            public void process(Site site) throws Exception {
                count[0]++;
            }
        }, 1, 1);
        assertEquals(0, count[0]);

        count[0] = 0;
        articleQueue.receiveMessages(new AmazonService.ListFunc<Article>() {
            public void process(Article article) throws Exception {
                count[0]++;
            }
        }, 1, 1);
        assertTrue(count[0] > 0);
    }
}
