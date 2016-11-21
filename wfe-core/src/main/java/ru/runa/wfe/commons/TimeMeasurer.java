package ru.runa.wfe.commons;

import org.apache.commons.logging.Log;

/**
 * Utility for measuring executing time and logging it.
 * 
 * @author dofs
 */
public class TimeMeasurer {
    private final Log log;
    private long startTime;
    private long thresholdInMs;

    public TimeMeasurer(Log log, long thresholdInMs) {
        this.log = log;
        this.thresholdInMs = thresholdInMs;
    }

    public TimeMeasurer(Log log) {
        this(log, 0);
    }

    public void jobStarted() {
        startTime = System.currentTimeMillis();
    }

    public void jobEnded(String jobName) {
        long jobTime = System.currentTimeMillis() - startTime;
        if (jobTime > thresholdInMs) {
            log.info("Task '" + jobName + "' executed for " + jobTime + " ms");
        }
    }
}
