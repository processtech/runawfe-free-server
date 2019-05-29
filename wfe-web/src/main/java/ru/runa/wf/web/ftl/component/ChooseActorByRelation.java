package ru.runa.wf.web.ftl.component;

import java.util.Set;

import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;

public class ChooseActorByRelation extends ChooseByRelationBase {
    private static final long serialVersionUID = 1L;

    @Override
    protected void fillExecutors(Set<Executor> result, Executor executor) {
        if (executor instanceof Actor) {
            result.add(executor);
        } else if (executor instanceof Group) {
            result.addAll(Delegates.getExecutorService().getGroupActors(user, (Group) executor));
        }
    }

}
