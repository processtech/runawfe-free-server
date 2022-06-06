package ru.runa.wfe.commons;

import com.google.common.base.Objects;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ru.runa.wfe.commons.email.EmailErrorNotifier;
import ru.runa.wfe.commons.error.SystemError;

public class SystemErrors {
    private static final Set<SystemError> errors = Collections.synchronizedSet(new HashSet<>());

    public static List<SystemError> getErrors() {
        synchronized (errors) {
            List<SystemError> list = new ArrayList<>(errors);
            Collections.sort(list);
            return list;
        }
    }

    public static void addError(Throwable throwable) {
        SystemError systemError = new SystemError(throwable);
        boolean alreadyExists = errors.add(systemError);
        if (!alreadyExists) {
            EmailErrorNotifier.sendNotification(systemError);
        }
    }

    public static void removeError(String errorMessage) {
        synchronized (errors) {
            for (SystemError systemError : errors) {
                if (Objects.equal(systemError.getMessage(), errorMessage)) {
                    errors.remove(systemError);
                    break;
                }
            }
        }
    }
}
