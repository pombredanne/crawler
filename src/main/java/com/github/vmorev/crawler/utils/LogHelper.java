package com.github.vmorev.crawler.utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;

import java.util.Properties;

/**
 * User: Valentin_Morev
 * Date: 13.01.13
 */
public class LogHelper {

    public static void updateLogger() {
        Properties props = new Properties();
        try {
            props.load(ClassLoader.getSystemResource("log4j.local.properties").openStream());
            if (!props.isEmpty()) {
                LogManager.resetConfiguration();
                PropertyConfigurator.configure(props);
            }
        } catch (Exception e) {
            //just skip, no local config exist
        }
        props = new Properties();
        try {
            props.load(ClassLoader.getSystemResource("log4j.test.properties").openStream());
            if (!props.isEmpty()) {
                LogManager.resetConfiguration();
                PropertyConfigurator.configure(props);
            }
        } catch (Exception e) {
            //just skip, no local config exist
        }
    }
}
