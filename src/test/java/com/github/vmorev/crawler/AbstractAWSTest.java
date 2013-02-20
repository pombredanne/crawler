package com.github.vmorev.crawler;

import com.github.vmorev.crawler.utils.LogHelper;
import org.junit.BeforeClass;

import java.util.Random;

/**
 * User: valentin
 * Date: 09.02.13
 */
public class AbstractAWSTest {
    protected static Random random;

    @BeforeClass
    public static void setUpClass() throws Exception {
        LogHelper.updateLogger();
        random = new Random();
    }

}
