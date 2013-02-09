package com.github.vmorev.crawler.tools;

import com.amazonaws.services.s3.model.ObjectListing;
import com.github.vmorev.crawler.AbstractAWSTest;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.utils.JsonHelper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class HosterTest extends AbstractAWSTest {

    @Before
    public void setUp() throws IOException {
        String modifier = "-" + random.nextLong();
        siteS3Name = helper.getConfig().getS3BucketSite() + modifier;
        siteSQSName = helper.getConfig().getSQSQueueSite() + modifier;
        helper.getS3().createBucket(siteS3Name);
        helper.getSQS().createQueue(siteSQSName);
        Hoster.siteS3Name = siteS3Name;
        Hoster.siteSQSName = siteSQSName;
        Hoster.helper = helper;
    }

    @Test
    public void testSaveSites() throws Exception {
        Hoster.saveSites("HosterTest.testSaveSites.json");

        //get all sites
        long count = 0;
        ObjectListing objectListing = helper.getS3().getS3().listObjects(siteS3Name);
        do {
            count+=objectListing.getObjectSummaries().size();
            objectListing.setMarker(objectListing.getNextMarker());
        } while (objectListing.isTruncated());

        assertEquals(3, count);
    }

    @Test
    public void testCheckSites() throws Exception {
        List<Site> sites = JsonHelper.parseJson(ClassLoader.getSystemResource("HosterTest.testCheckSites.json"), new TypeReference<List<Site>>() {
        });
        for (Site site : sites)
            helper.getS3().saveObject(siteS3Name, Site.generateId(site.getUrl()), site);

        Hoster.checkSites();

        int count = 0;
        int lastCount;
        do {
            lastCount = helper.getSQS().receiveMessage(siteSQSName).getMessages().size();
            count += lastCount;
        } while (lastCount > 0);

        assertEquals(sites.size(), count);
    }
}