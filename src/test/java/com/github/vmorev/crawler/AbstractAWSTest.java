package com.github.vmorev.crawler;

import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.github.vmorev.crawler.utils.AWSHelper;
import com.github.vmorev.crawler.utils.ConfigStorage;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * User: valentin
 * Date: 09.02.13
 */
public class AbstractAWSTest {
    protected static AWSHelper helper;
    protected String articleSQSName;
    protected String articleS3Name;
    protected String siteSQSName;
    protected String siteS3Name;

    @BeforeClass
    public static void setUpClass() throws Exception {
        ConfigStorage.updateLogger();
        helper = new AWSHelper();
    }

    @After
    public void cleanUp() throws IOException {
        if (articleSQSName != null)
            helper.getSQS().deleteQueue(new DeleteQueueRequest(articleSQSName));

        if (siteSQSName != null)
            helper.getSQS().deleteQueue(new DeleteQueueRequest(siteSQSName));

        if (articleS3Name != null)
            helper.deleteS3Bucket(articleS3Name);

        if (siteS3Name != null)
            helper.deleteS3Bucket(siteS3Name);
    }

}
