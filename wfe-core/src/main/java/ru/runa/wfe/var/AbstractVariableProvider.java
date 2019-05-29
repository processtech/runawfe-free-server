package ru.runa.wfe.var;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorLoader;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Base implementation of {@link VariableProvider}.
 * 
 * @author Dofs
 * @since 4.0
 */
public abstract class AbstractVariableProvider implements VariableProvider {
    protected final Log log = LogFactory.getLog(getClass());

    protected ExecutorLoader getExecutorLoader() {
        return ApplicationContextFactory.getExecutorDao();
    }

    @Override
    public Object getValueNotNull(String variableName) throws VariableDoesNotExistException {
        Object object = getValue(variableName);
        if (object == null) {
            throw new VariableDoesNotExistException(variableName);
        }
        return object;
    }

    @Override
    public <T> T getValue(Class<T> clazz, String variableName) {
        Object object = getValue(variableName);
        if (Executor.class.isAssignableFrom(clazz)) {
            return (T) TypeConversionUtil.convertToExecutor(object, getExecutorLoader());
        }
        return TypeConversionUtil.convertTo(clazz, object);
    }

    @Override
    public <T> T getValueNotNull(Class<T> clazz, String variableName) throws VariableDoesNotExistException {
        T object = getValue(clazz, variableName);
        if (object == null) {
            throw new VariableDoesNotExistException(variableName);
        }
        return object;
    }

    @Override
    public WfVariable getVariableNotNull(String variableName) throws VariableDoesNotExistException {
        WfVariable variable = getVariable(variableName);
        if (variable == null) {
            throw new VariableDoesNotExistException(variableName);
        }
        return variable;
    }

    public AbstractVariableProvider getSameProvider(Long processId) {
        throw new UnsupportedOperationException("in " + getClass());
    }
}
