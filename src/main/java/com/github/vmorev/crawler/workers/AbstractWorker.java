package com.github.vmorev.crawler.workers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public abstract class AbstractWorker implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(AbstractWorker.class);
    /**
     * defaultRetryCount default is not to retry
     */
    protected int defaultRetryCount = 0;
    protected int currentRetryCount = 0;

    /**
     * sleepTime default is 1 second
     */
    protected int sleepTime = 1000;

    /**
     * Random value for thread to sleep
     */
    Random randomGenerator = new Random();

    /**
     * This method should be implemented in each worker to actually do the work
     *
     * @throws InterruptedException      - in case of thread termination
     * @throws ExecutionFailureException - in case of work execution failure to trigger retry attempt
     */
    protected abstract void performWork() throws InterruptedException, ExecutionFailureException;

    protected boolean isReadyToSleep() {
        return currentRetryCount <= 0;
    }

    /**
     * This method is handling thread execution process and is not required to be re-implemented
     */
    public void run() {
        //thread will work till external interruption signal
        while (!Thread.currentThread().isInterrupted()) {
            try {
                try {
                    //perform work
                    performWork();
                } catch (ExecutionFailureException e) {
                    log.error("FAIL. " + this.getClass().getSimpleName() + ". EXECUTION FAILED", e);
                    //decrease retry count on failure
                    currentRetryCount--;
                }

                //check if thread is ready to sleep
                if (isReadyToSleep()) {
                    synchronized (this) {
                        //adding random value for threads to sleep random time.
                        int timeRandomizer = randomGenerator.nextInt(100);
                        wait(sleepTime + timeRandomizer);
                        //get initial retry count after sleep
                        currentRetryCount = defaultRetryCount;
                    }
                }
            } catch (InterruptedException e) {
                //using System.out due to unavailability of logger during interruption time
                System.out.println(this.getClass().getName() + " was interrupted");
                Thread.currentThread().interrupt();
            }
        }
    }

    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }
}
