package ru.runa.wfe.extension.orgfunction;

import java.util.List;

import ru.runa.wfe.commons.TypeConversionUtil;

import com.google.common.base.Preconditions;

public abstract class ActorOrgFunctionBase extends GetActorsOrgFunctionBase {

    @Override
    protected List<Long> getActorCodes(Object... parameters) {
        Preconditions.checkNotNull(parameters, "parameters");
        Preconditions.checkArgument(parameters.length == 1, "expected parameters with 1 element");
        Long actorCode = TypeConversionUtil.convertTo(Long.class, parameters[0]);
        return getActorCodes(actorCode);
    }

    protected abstract List<Long> getActorCodes(Long actorCode);

}
