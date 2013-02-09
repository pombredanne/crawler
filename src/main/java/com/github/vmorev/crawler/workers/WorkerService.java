package com.github.vmorev.crawler.workers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WorkerService {
    private int terminationTimeout = 1000;
    private final ExecutorService executor;
    private final Thread shutDownHook;

    public WorkerService() {
        //init executor service
        this.executor = Executors.newCachedThreadPool();
        //create shutdown hook
        this.shutDownHook = new ShutDownHook();
        //install shutdown hook
        Runtime.getRuntime().addShutdownHook(this.shutDownHook);
    }

    /**
     * set wait interval for thread termination
     *
     * @param terminationTimeout - time in milliseconds to wait till thread termination
     */
    public void setTerminationTimeout(int terminationTimeout) {
        this.terminationTimeout = terminationTimeout;
    }

    public int getTerminationTimeout() {
        return this.terminationTimeout;
    }

    /**
     * This method provides an access to shutdown hook which handles shutdown for executor service
     *
     * @return
     */
    public Thread getShutDownHook() {
        return this.shutDownHook;
    }

    /**
     * Primary method to get executor service
     *
     * @return
     */
    public ExecutorService getExecutor() {
        return this.executor;
    }

    private class ShutDownHook extends Thread {
        public void run() {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(terminationTimeout, TimeUnit.MILLISECONDS)) {
                    executor.shutdownNow();
                    if (!executor.awaitTermination(terminationTimeout, TimeUnit.MILLISECONDS))
                        System.out.println(this.getClass().getName() + "Can't stop threads");
                }
            } catch (InterruptedException ie) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println(this.getClass().getName() + " all threads were shut down");
        }
    }

}
