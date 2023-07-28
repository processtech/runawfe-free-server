package ru.runa.wfe.extension.orgfunction;

import java.util.List;

import ru.runa.wfe.extension.OrgFunction;
import ru.runa.wfe.extension.OrgFunctionException;
import ru.runa.wfe.user.Executor;

import com.google.common.base.Throwables;

/**
 * Created on 08.01.2007
 **/
public abstract class GetActorsOrgFunctionBase extends OrgFunction {

    @Override
    public final List<? extends Executor> getExecutors(Object... parameters) throws OrgFunctionException {
        try {
            List<Long> codes = getActorCodes(parameters);
            log.debug("Actor codes result: " + codes);
            return executorDao.getActorsByCodes(codes);
        } catch (Exception e) {
            Throwables.propagateIfPossible(e, OrgFunctionException.class);
            throw new OrgFunctionException(e);
        }
    }

    protected abstract List<Long> getActorCodes(Object... parameters);

}
