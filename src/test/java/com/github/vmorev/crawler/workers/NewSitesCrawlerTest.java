package com.github.vmorev.crawler.workers;

import com.github.vmorev.crawler.AbstractAWSTest;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.utils.JsonHelper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class NewSitesCrawlerTest extends AbstractAWSTest {
    NewSitesCrawler crawler;

    @Before
    public void setUp() {
        String modifier = "-" + random.nextLong();
        siteS3Name = helper.getConfig().getS3Site() + modifier;
        siteSQSName = helper.getConfig().getSQSSite() + modifier;
        helper.getS3().createBucket(siteS3Name);
        helper.getSQS().createQueue(siteSQSName);
        crawler = new NewSitesCrawler();
        crawler.siteS3Name = siteS3Name;
        crawler.siteSQSName = siteSQSName;
        crawler.isTest = true;
    }

    @Test
    public void testCheckSites() throws Exception {
        List<Site> sites = JsonHelper.parseJson(ClassLoader.getSystemResource("NewSitesCrawlerTest.testCheckSites.json"), new TypeReference<List<Site>>() {
        });
        for (Site site : sites)
            helper.getS3().saveJSONObject(siteS3Name, Site.generateId(site.getUrl()), site);

        crawler.performWork();

        int count = 0;
        int lastCount;
        do {
            lastCount = helper.getSQS().receiveMessage(siteSQSName).getMessages().size();
            count += lastCount;
        } while (lastCount > 0);

        assertEquals(sites.size(), count);
    }
}
