package ru.runa.wfe.extension.orgfunction;

import com.google.common.collect.Lists;
import java.util.List;
import ru.runa.wfe.extension.OrgFunction;
import ru.runa.wfe.extension.OrgFunctionException;
import ru.runa.wfe.user.Executor;

public class ExecutorsFromListFunction extends OrgFunction {

    @SuppressWarnings("unchecked")
    @Override
    public List<? extends Executor> getExecutors(Object... parameters) throws OrgFunctionException {
        List<Executor> result = Lists.newArrayList();
        for (Object parameter : parameters) {
            if (parameter instanceof Executor) {
                result.add((Executor) parameter);
            }
        }
        return result;
    }

}
