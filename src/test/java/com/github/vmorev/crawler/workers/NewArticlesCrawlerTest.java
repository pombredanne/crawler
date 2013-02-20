package com.github.vmorev.crawler.workers;

import com.github.vmorev.amazon.AmazonService;
import com.github.vmorev.amazon.SDBDomain;
import com.github.vmorev.amazon.SQSQueue;
import com.github.vmorev.crawler.AbstractAWSTest;
import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.beans.Site;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NewArticlesCrawlerTest extends AbstractAWSTest {
    private NewArticlesCrawler crawler;
    private SQSQueue articleQueue;
    private SQSQueue siteQueue;
    private SDBDomain siteDomain;

    @Before
    public void setUp() throws Exception {
        String modifier = "-" + random.nextLong();
        articleQueue = new SQSQueue(SQSQueue.getConfig().getValue(Article.VAR_SQS_QUEUE) + modifier);
        siteQueue = new SQSQueue(SQSQueue.getConfig().getValue(Site.VAR_SQS_QUEUE) + modifier);
        siteDomain = new SDBDomain(SDBDomain.getConfig().getValue(Site.VAR_SDB_DOMAIN) + modifier);
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
        Site site = new ObjectMapper().readValue(ClassLoader.getSystemResource(fileName), Site.class);

        siteQueue.sendMessage(site);
        crawler.performWork();

        final long[] count = new long[1];
        siteQueue.receiveMessages(1, 1, Site.class, new AmazonService.ListFunc<Site>() {
            public void process(Site site) throws Exception {
                count[0]++;
            }
        });
        assertEquals(0, count[0]);

        count[0] = 0;
        articleQueue.receiveMessages(1, 1, Article.class, new AmazonService.ListFunc<Article>() {
            public void process(Article article) throws Exception {
                count[0]++;
            }
        });
        assertTrue(count[0] > 0);
    }
}
