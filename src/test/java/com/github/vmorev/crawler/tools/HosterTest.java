package com.github.vmorev.crawler.tools;

import com.github.vmorev.crawler.AbstractAWSTest;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.utils.amazon.S3Service;
import com.github.vmorev.crawler.utils.amazon.SDBService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HosterTest extends AbstractAWSTest {
    private SDBService.Domain<Site> siteDomain;

    @Before
    public void setUp() throws Exception {
        String modifier = "-" + random.nextLong();
        siteName = sdb.getConfig().getSite() + modifier;
        siteDomain = sdb.getDomain(siteName, Site.class);
        siteDomain.createDomain();
        Hoster.siteDomain = siteDomain;
    }

    @After
    public void cleanUp() throws Exception {
        siteDomain.deleteDomain();
    }

    @Test
    public void testSaveSites() throws Exception {
        Hoster.saveSites("HosterTest.testSaveSites.json");

        final long[] count = new long[1];
        siteDomain.listObjects("select * from " + siteDomain.getName(), new S3Service.ListFunc<Site>() {
            public void process(Site obj) {
                count[0] += 1;
            }
        });

        assertEquals(3, count[0]);
    }
}