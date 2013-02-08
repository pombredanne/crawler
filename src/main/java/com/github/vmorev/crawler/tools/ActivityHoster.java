package com.github.vmorev.crawler.tools;

import com.amazonaws.services.simpleworkflow.flow.ActivityWorker;
import com.github.vmorev.crawler.awsflow.AWSHelper;
import com.github.vmorev.crawler.utils.ConfigStorage;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ActivityHoster {

    public static void main(String[] args) throws Exception {
        ConfigStorage.updateLogger();

        if (!(args.length > 1 && args[0] != null && args[0].length() > 0 && args[1] != null && args[1].length() > 0)) {
            System.out.println("Two parameters required: Full activities class name and # of workers to start");
            System.exit(1);
        }
        long workers = Long.parseLong(args[1]);
        for (long i = 0; i < workers; i++)
            hostActivity(Class.forName(args[0]));

        System.out.println(Class.forName(args[0]) + " started " + workers + " times");
        System.out.println("Please press ENTER key to terminate service.");
        try {
            //noinspection ResultOfMethodCallIgnored
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static ActivityWorker hostActivity(final Class clazz)
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
                    System.out.println(clazz.getName() + " was terminated...");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        return worker;
    }
}
