package ru.runa.wfe.extension.orgfunction;

import java.util.List;

import ru.runa.wfe.extension.OrgFunction;
import ru.runa.wfe.extension.OrgFunctionException;
import ru.runa.wfe.user.Executor;

public class NullOrgFunction extends OrgFunction {

    @Override
    public List<? extends Executor> getExecutors(Object... parameters) throws OrgFunctionException {
        return null;
    }

}
