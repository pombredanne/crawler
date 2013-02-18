package com.github.vmorev.crawler.workers;

import com.github.vmorev.crawler.AbstractAWSTest;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.utils.JsonHelper;
import com.github.vmorev.crawler.utils.amazon.AmazonService;
import com.github.vmorev.crawler.utils.amazon.SDBService;
import com.github.vmorev.crawler.utils.amazon.SQSService;
import org.codehaus.jackson.type.TypeReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class NewSitesCrawlerTest extends AbstractAWSTest {
    NewSitesCrawler crawler;
    private SQSService.Queue<Site> siteQueue;
    private SDBService.Domain<Site> siteDomain;

    @Before
    public void setUp() throws Exception {
        String modifier = "-" + random.nextLong();
        siteName = sdb.getConfig().getSite() + modifier;
        siteQueue = sqs.getQueue(siteName, Site.class);
        siteDomain = sdb.getDomain(siteName, Site.class);
        siteDomain.createDomain();
        siteQueue.createQueue();

        crawler = new NewSitesCrawler();
        crawler.siteDomain = siteDomain;
        crawler.siteQueue = siteQueue;
    }

    @After
    public void cleanUp() throws Exception {
        siteQueue.deleteQueue();
        siteDomain.deleteDomain();
    }

    @Test
    public void testCheckSites() throws Exception {
        List<Site> sites = JsonHelper.parseJson(ClassLoader.getSystemResource("NewSitesCrawlerTest.testCheckSites.json"), new TypeReference<List<Site>>() {
        });

        for (Site site : sites)
            siteDomain.saveObject(Site.generateId(site.getUrl()), site);

        crawler.performWork();

        final long[] count = new long[1];
        siteQueue.receiveMessages(new AmazonService.ListFunc<Site>() {
            public void process(Site site) throws Exception {
                count[0]++;
            }
        }, 1, 1);
        assertEquals(sites.size(), count[0]);
    }
}
