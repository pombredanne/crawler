package com.github.vmorev.crawler.tools;

import com.amazonaws.services.simpleworkflow.flow.ActivityWorker;
import com.github.vmorev.crawler.awsflow.AWSHelper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ActivityHoster {

    public static void main(String[] args) throws Exception {
        if (!(args.length > 0 && args[0] != null && args[0].length() > 0)) {
            System.out.println("Full activities class name should be provided as an argument");
            System.exit(1);
        }
        hostActivity(Class.forName(args[0]));

        System.out.println(ActivityHoster.class.getSimpleName() + " Service Started...");
        System.out.println("Please press any key to terminate service.");
        try {
            //noinspection ResultOfMethodCallIgnored
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static ActivityWorker hostActivity(Class clazz)
            throws IOException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        AWSHelper awsHelper = new AWSHelper();
        final ActivityWorker worker =
                new ActivityWorker(awsHelper.createSWFClient(), awsHelper.getSWFDomain(), awsHelper.getSwfTasklist());

        // Create activity implementations
        worker.addActivitiesImplementation(clazz.newInstance());
        worker.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    worker.shutdownAndAwaitTermination(1, TimeUnit.MINUTES);
                    System.out.println(ActivityHoster.class.getSimpleName() + " Service Terminated...");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        return worker;
    }
}
