package com.github.vmorev.crawler.tools;

import com.amazonaws.services.simpleworkflow.flow.WorkflowWorker;
import com.github.vmorev.crawler.awsflow.AWSHelper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class WorkflowHoster {

    public static void main(String[] args) throws Exception {
        if (!(args.length > 0 && args[0] != null && args[0].length() > 0)) {
            System.out.println("Full workflow class name should be provided as an argument");
            System.exit(1);
        }

        hostWorkflow(Class.forName(args[0]));

        System.out.println(WorkflowHoster.class.getSimpleName() + " Service Started...");
        System.out.println("Please press any key to terminate service.");
        try {
            //noinspection ResultOfMethodCallIgnored
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static WorkflowWorker hostWorkflow(Class clazz) throws IOException, IllegalAccessException, InstantiationException {
        AWSHelper awsHelper = new AWSHelper();
        final WorkflowWorker worker =
                new WorkflowWorker(awsHelper.createSWFClient(), awsHelper.getSWFDomain(), awsHelper.getSwfTasklist());
        worker.addWorkflowImplementationType(clazz);
        worker.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    worker.shutdownAndAwaitTermination(1, TimeUnit.MINUTES);
                    System.out.println(WorkflowHoster.class.getSimpleName() + " Service Terminated...");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        return worker;
    }
}
