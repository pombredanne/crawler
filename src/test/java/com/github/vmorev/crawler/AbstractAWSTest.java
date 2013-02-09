package com.github.vmorev.crawler;

import com.github.vmorev.crawler.utils.AWSHelper;
import com.github.vmorev.crawler.utils.ConfigStorage;
import org.junit.After;
import org.junit.BeforeClass;

import java.io.IOException;
import java.util.Random;

/**
 * User: valentin
 * Date: 09.02.13
 */
public class AbstractAWSTest {
    protected static AWSHelper helper;
    protected static Random random;
    protected String articleSQSName;
    protected String articleS3Name;
    protected String siteSQSName;
    protected String siteS3Name;

    @BeforeClass
    public static void setUpClass() throws Exception {
        ConfigStorage.updateLogger();
        helper = new AWSHelper();
        random = new Random();
    }

    @After
    public void cleanUp() throws IOException {
        if (articleSQSName != null)
            helper.getSQS().deleteQueue(articleSQSName);

        if (siteSQSName != null)
            helper.getSQS().deleteQueue(siteSQSName);

        if (articleS3Name != null)
            helper.getS3().deleteBucket(articleS3Name);

        if (siteS3Name != null)
            helper.getS3().deleteBucket(siteS3Name);
    }

}
