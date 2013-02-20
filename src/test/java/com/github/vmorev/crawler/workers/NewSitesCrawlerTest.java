package com.github.vmorev.crawler.workers;

import com.github.vmorev.amazon.AmazonService;
import com.github.vmorev.amazon.SDBDomain;
import com.github.vmorev.amazon.SQSQueue;
import com.github.vmorev.crawler.AbstractAWSTest;
import com.github.vmorev.crawler.beans.Site;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class NewSitesCrawlerTest extends AbstractAWSTest {
    NewSitesCrawler crawler;
    private SQSQueue siteQueue;
    private SDBDomain siteDomain;

    @Before
    public void setUp() throws Exception {
        String modifier = "-" + random.nextLong();
        siteQueue = new SQSQueue(SQSQueue.getConfig().getValue(Site.VAR_SQS_QUEUE) + modifier);
        siteDomain = new SDBDomain(SDBDomain.getConfig().getValue(Site.VAR_SDB_DOMAIN) + modifier);
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
        List<Site> sites = new ObjectMapper().readValue(ClassLoader.getSystemResource("NewSitesCrawlerTest.testCheckSites.json"), new TypeReference<List<Site>>() {
        });

        for (Site site : sites)
            siteDomain.saveObject(Site.generateId(site.getUrl()), site);

        crawler.performWork();

        final List<Site> resSites = new ArrayList<>();
        final long[] size = new long[1];
        do {
            size[0] = 0;
            siteQueue.receiveMessages(1, 3, Site.class, new AmazonService.ListFunc<Site>() {
                public void process(Site site) throws Exception {
                    resSites.add(site);
                    size[0]++;
                }
            });
        } while (size[0] > 0);
        assertEquals(sites.size(), resSites.size());
    }
}
