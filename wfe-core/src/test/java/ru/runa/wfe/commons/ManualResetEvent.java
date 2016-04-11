package ru.runa.wfe.commons;

import java.util.Calendar;

public class ManualResetEvent {

    private volatile boolean isSet = false;
    private final int timeoutMs;

    public ManualResetEvent() {
        this(2000);
    }

    public ManualResetEvent(int maxWaitTimeoutMs) {
        timeoutMs = maxWaitTimeoutMs;
    }

    public synchronized void setEvent() {
        isSet = true;
        this.notifyAll();
    }

    public synchronized void tryWaitEvent() {
        while (!isSet) {
            try {
                long startTime = Calendar.getInstance().getTimeInMillis();
                this.wait(timeoutMs + 100);
                long ellapsed = Calendar.getInstance().getTimeInMillis() - startTime;
                if (ellapsed >= timeoutMs) {
                    throw new RuntimeException("ManualResetEvent.waitEvent seems to be stuck. Ellapsed " + ellapsed + " ms.");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("ManualResetEvent wait interrupted", e);
            }
        }
    }

    public synchronized void resetEvent() {
        isSet = false;
    }
}
