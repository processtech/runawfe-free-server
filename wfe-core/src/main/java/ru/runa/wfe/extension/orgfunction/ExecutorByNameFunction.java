package ru.runa.wfe.extension.orgfunction;

import java.util.List;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.extension.OrgFunction;
import ru.runa.wfe.extension.OrgFunctionException;
import ru.runa.wfe.user.Executor;

import com.google.common.collect.Lists;

/**
 * 
 * Returns executors by name, can accept multiple names at once.
 * 
 * Created on Jul 12, 2006
 */
public class ExecutorByNameFunction extends OrgFunction {

    @Override
    public List<? extends Executor> getExecutors(Object... parameters) throws OrgFunctionException {
        List<Executor> result = Lists.newArrayListWithExpectedSize(parameters.length);
        for (Object parameter : parameters) {
            Executor executor = TypeConversionUtil.convertToExecutor(parameter, executorDao);
            if (executor != null) {
                result.add(executor);
            }
        }
        return result;
    }
}
