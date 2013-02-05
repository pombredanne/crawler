package com.github.vmorev.crawler.tools;

import com.amazonaws.services.simpleworkflow.flow.WorkflowWorker;
import com.github.vmorev.crawler.awsflow.AWSHelper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class WorkflowHoster {

    public static void main(String[] args) throws Exception {
        if (!(args.length > 1 && args[0] != null && args[0].length() > 0 && args[1] != null && args[1].length() > 0)) {
            System.out.println("Two parameters required: Full workflow class name and # of workers to start");
            System.exit(1);
        }
        long workers = Long.parseLong(args[1]);
        for (long i = 0; i < workers; i++)
            hostWorkflow(Class.forName(args[0]));

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

    public static WorkflowWorker hostWorkflow(final Class clazz) throws IOException, IllegalAccessException, InstantiationException {
        AWSHelper awsHelper = new AWSHelper();
        final WorkflowWorker worker =
                new WorkflowWorker(awsHelper.createSWFClient(), awsHelper.getSWFDomain(), awsHelper.getSwfTasklist());
        worker.addWorkflowImplementationType(clazz);
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
