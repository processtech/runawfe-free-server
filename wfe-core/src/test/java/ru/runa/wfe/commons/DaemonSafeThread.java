package ru.runa.wfe.commons;

import java.lang.Thread.UncaughtExceptionHandler;

public class DaemonSafeThread implements UncaughtExceptionHandler {
    private final Thread thread;
    private volatile Throwable throwable;

    DaemonSafeThread(Runnable runnable) {
        thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread arg0, Throwable arg1) {
        throwable = arg1;
    }

    public void start() {
        thread.start();
    }

    public void join() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException("DaemonSafeThread join failed", e);
        }
        if (throwable != null) {
            throw new RuntimeException("DaemonSafeThread: detected exception in background thread: " + throwable.getMessage(), throwable);
        }
    }

    public static DaemonSafeThread create(Runnable runnable) {
        return new DaemonSafeThread(runnable);
    }

    public static DaemonSafeThread createAndStart(Runnable runnable) {
        DaemonSafeThread safeThread = create(runnable);
        safeThread.start();
        return safeThread;
    }

    public static DaemonSafeThread createAndStartAndJoin(Runnable runnable) {
        DaemonSafeThread safeThread = createAndStart(runnable);
        safeThread.join();
        return safeThread;
    }
}
