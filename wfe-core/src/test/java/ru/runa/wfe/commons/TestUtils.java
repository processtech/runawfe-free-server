package ru.runa.wfe.commons;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class TestUtils {
    public static void tryAcquireSemaphore(Semaphore semaphore, int timeoutMs) {
        try {
            if (!semaphore.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("TestUtils.acquireSemaphore seems to be stuck. Ellapsed " + timeoutMs + " ms.");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Semaphore acquire interrupted", e);
        }
    }

    public static void tryAcquireSemaphore(Semaphore semaphore) {
        tryAcquireSemaphore(semaphore, 10000);
    }
}
