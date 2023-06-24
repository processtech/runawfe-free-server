package ru.runa.wfe.commons;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class TransactionalExecutor {

    public void execute(TransactionalCallback callback) throws RuntimeException {
        try {
            callback.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object executeWithResult(TransactionalCallbackWithResult callbackWithResult) throws RuntimeException {
        try {
            return callbackWithResult.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public static interface TransactionalCallback {

        void run() throws Exception;

    }

    @FunctionalInterface
    public static interface TransactionalCallbackWithResult {

        Object run() throws Exception;

    }

}
