package ru.runa.wfe.extension.function;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;

public class IsExecutorInGroup extends Function<Boolean> {

    public IsExecutorInGroup() {
        super(Param.required(String.class), Param.required(String.class));
    }

    @Override
    protected Boolean doExecute(Object... parameters) {
        Group group = TypeConversionUtil.convertTo(Group.class, parameters[0]);
        Executor executor = TypeConversionUtil.convertTo(Executor.class, parameters[1]);
        return ApplicationContextFactory.getExecutorDAO().isExecutorInGroup(executor, group);
    }

    @Override
    public String getName() {
        return "isExecutorInGroup";
    }

}
