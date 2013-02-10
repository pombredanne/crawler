package com.github.vmorev.crawler.tools;

import com.amazonaws.services.s3.model.ObjectListing;
import com.github.vmorev.crawler.AbstractAWSTest;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class HosterTest extends AbstractAWSTest {

    @Before
    public void setUp() throws IOException {
        String modifier = "-" + random.nextLong();
        siteS3Name = helper.getConfig().getS3BucketSite() + modifier;
        siteSQSName = helper.getConfig().getSQSQueueSite() + modifier;
        helper.getS3().createBucket(siteS3Name);
        helper.getSQS().createQueue(siteSQSName);
        Hoster.helper = helper;
    }

    @Test
    public void testSaveSites() throws Exception {
        Hoster.saveSites(siteS3Name, "HosterTest.testSaveSites.json");

        //get all sites
        long count = 0;
        ObjectListing objectListing = helper.getS3().getS3().listObjects(siteS3Name);
        do {
            count += objectListing.getObjectSummaries().size();
            objectListing.setMarker(objectListing.getNextMarker());
        } while (objectListing.isTruncated());

        assertEquals(3, count);
    }
}