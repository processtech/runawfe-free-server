package ru.runa.wfe.var.converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.var.Converter;
import ru.runa.wfe.var.Variable;

public class ExecutorToLongConverter implements Converter {
    private static final long serialVersionUID = 1L;
    private static Log log = LogFactory.getLog(ExecutorToLongConverter.class);

    @Override
    public boolean supports(Object value) {
        return value instanceof Executor;
    }

    @Override
    public Object convert(ExecutionContext executionContext, Variable<?> variable, Object o) {
        return ((Executor) o).getId();
    }

    @Override
    public Object revert(Object o) {
        Long executorId = (Long) o;
        if (executorId == null) {
            return null;
        }
        try {
            return ApplicationContextFactory.getExecutorDao().getExecutor(executorId);
        } catch (ExecutorDoesNotExistException e) {
            log.warn(e);
            return null;
        }
    }
}
