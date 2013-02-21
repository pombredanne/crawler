package com.github.vmorev.crawler.workers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class LogsAnalyzerTest {
    private LogsAnalyzer worker;

    @Before
    public void setUp() throws IOException {
        worker = new LogsAnalyzer();
    }

    @After
    public void cleanUp() throws Exception {
    }

    @Test
    public void testExecution() throws Exception {
        //worker.performWork();
    }
}
