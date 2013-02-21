package com.github.vmorev.crawler.tools;

import com.github.vmorev.amazon.S3Bucket;
import com.github.vmorev.amazon.SDBDomain;
import com.github.vmorev.crawler.AbstractAWSTest;
import com.github.vmorev.crawler.beans.Site;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HosterTest extends AbstractAWSTest {
    private SDBDomain siteDomain;

    @Before
    public void setUp() throws Exception {
        String modifier = "-" + random.nextLong();
        siteDomain = new SDBDomain(SDBDomain.getConfig().getValue(Site.VAR_SDB_DOMAIN) + modifier);
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

        long objCount;
        long tryCount = 0;
        do {
            objCount = loadObjects();
            tryCount++;
        } while (objCount < 3 && tryCount < 5);

        assertEquals(3, objCount);
    }

    private long loadObjects() throws Exception {
        final long[] count = new long[1];
        siteDomain.listObjects("select itemname() from `" + siteDomain.getName() + "`", Site.class, new S3Bucket.ListFunc<Site>() {
            public void process(Site obj) {
                count[0] += 1;
            }
        });
        return count[0];
    }

}