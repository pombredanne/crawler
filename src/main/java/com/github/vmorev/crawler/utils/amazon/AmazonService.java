package com.github.vmorev.crawler.utils.amazon;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

/**
 * User: Valentin_Morev
 * Date: 21.01.13
 */
public abstract class AmazonService {
    private AWSCredentials credentials;
    private AmazonConfig config;

    public AmazonService() {
        config = new AmazonConfig();
    }

    protected AWSCredentials getCredentials() {
        if (credentials == null)
            credentials = new BasicAWSCredentials(config.getAccessKey(), config.getSecretKey());
        return credentials;
    }

    public AmazonConfig getConfig() {
        return config;
    }

    public interface ListFunc<T> {
        void process(T obj) throws Exception;
    }

}
