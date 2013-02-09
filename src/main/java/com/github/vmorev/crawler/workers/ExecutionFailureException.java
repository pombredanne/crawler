package com.github.vmorev.crawler.workers;

public class ExecutionFailureException extends Exception {

    public ExecutionFailureException(String message, Throwable e) {
        super(message, e);
    }

    public ExecutionFailureException(String message) {
        super(message);
    }

    public ExecutionFailureException(Throwable e) {
        super(e);
    }

}
