package com.mcg.scheduler;

import java.util.concurrent.CountDownLatch;

/**
 * The Class ServiceExecutionHook.
 */
public class ServiceExecutionHook {

    /** latch. */
    private CountDownLatch latch;

    /** exception. */
    private Exception exception;

    /**
     * Instantiates a new service execution hook.
     */
    public ServiceExecutionHook() {
     latch = new CountDownLatch(1);
     exception = null;
    }

    /**
     * Getter for exception.
     *
     * @return exception Exception
     */
    public final Exception getException() {
        return exception;
    }

    /**
     * Setter for exception.
     *
     * @param exception1 Exception
     */
    public final void setException(final Exception exception1) {
        this.exception = exception1;
    }

    /**
     * Getter for latch.
     *
     * @return latch CountDownLatch
     */
    public final CountDownLatch getLatch() {
        return latch;
    }

    /**
     * Getter for success.
     *
     * @return success boolean
     */
    public final boolean isSuccess() {
        return exception == null;
    }
}
