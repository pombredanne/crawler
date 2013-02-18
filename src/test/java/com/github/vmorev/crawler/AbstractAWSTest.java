package com.github.vmorev.crawler;

import com.github.vmorev.crawler.utils.ConfigStorage;
import com.github.vmorev.crawler.utils.amazon.S3Service;
import com.github.vmorev.crawler.utils.amazon.SDBService;
import com.github.vmorev.crawler.utils.amazon.SQSService;
import org.junit.After;
import org.junit.BeforeClass;

import java.util.Random;

/**
 * User: valentin
 * Date: 09.02.13
 */
public class AbstractAWSTest {
    protected static S3Service s3;
    protected static SDBService sdb;
    protected static SQSService sqs;
    protected static Random random;
    protected String articleName;
    protected String siteName;

    @BeforeClass
    public static void setUpClass() throws Exception {
        ConfigStorage.updateLogger();
        s3 = new S3Service();
        sdb = new SDBService();
        sqs = new SQSService();
        random = new Random();
    }

}
