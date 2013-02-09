package com.github.vmorev.crawler.tools;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.github.vmorev.crawler.AbstractAWSTest;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.utils.JsonHelper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HosterTest extends AbstractAWSTest {

    @Before
    public void setUp() throws IOException {
        String modifier = "-" + System.currentTimeMillis();
        siteS3Name = helper.getS3BucketSite() + modifier;
        siteSQSName = helper.getSQSQueueSite() + modifier;
        helper.createS3Bucket(siteS3Name);
        helper.createSQSQueue(siteSQSName);
        Hoster.siteS3Name = siteS3Name;
        Hoster.siteSQSName = siteSQSName;
    }

    @Test
    public void testSaveSites() throws Exception {
        Hoster.saveSites("HosterTest.testSaveSites.json");

        //get all sites
        long count = 0;
        ObjectListing objectListing = helper.getS3().listObjects(siteS3Name);
        do {
            count+=objectListing.getObjectSummaries().size();
            objectListing.setMarker(objectListing.getNextMarker());
        } while (objectListing.isTruncated());

        assertTrue(count > 0);
    }

    @Test
    public void testCheckSites() throws Exception {
        List<Site> sites = JsonHelper.parseJson(new File("HosterTest.testCheckSites.json"), new TypeReference<List<Site>>() {
        });
        for (Site site : sites)
            helper.saveS3Object(helper.getS3BucketSite(), Site.generateId(site.getUrl()), site);

        Hoster.checkSites();

        assertEquals(sites.size(), helper.getSQS().receiveMessage(new ReceiveMessageRequest(siteSQSName)).getMessages().size());
    }
}