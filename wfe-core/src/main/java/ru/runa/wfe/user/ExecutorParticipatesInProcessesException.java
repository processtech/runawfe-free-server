package ru.runa.wfe.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import lombok.val;
import ru.runa.wfe.InternalApplicationException;

/**
 * Signals that some processes are depends from this {@link Executor}.
 * 
 * @since 4.0.5
 */
public class ExecutorParticipatesInProcessesException extends InternalApplicationException {
    private static final long serialVersionUID = 1L;
    public static final int LIMIT = 100;

    private final String executorName;
    private final String idsInfo;
    private final String message;

    public ExecutorParticipatesInProcessesException(String executorName, Set<Long> processIds) {
        super();

        // This is still non-deterministic, since *ProcessDao.getDependentProcessIds() does not orderBy(id) before applying limit(...).
        val processIdsSorted = new ArrayList<Long>(processIds);
        Collections.sort(processIdsSorted);

        this.executorName = executorName;
        this.idsInfo = processIds.size() > LIMIT ? " > " + LIMIT : processIdsSorted.toString();
        this.message = executorName + " " + processIdsSorted;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getExecutorName() {
        return executorName;
    }
    
    public String getIdsInfo() {
        return idsInfo;
    }
}
