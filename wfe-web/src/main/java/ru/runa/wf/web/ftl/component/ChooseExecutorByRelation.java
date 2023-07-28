package ru.runa.wf.web.ftl.component;

import java.util.Set;

import ru.runa.wfe.user.Executor;

public class ChooseExecutorByRelation extends ChooseByRelationBase {
    private static final long serialVersionUID = 1L;

    @Override
    protected void fillExecutors(Set<Executor> result, Executor executor) {
        result.add(executor);
    }

}
