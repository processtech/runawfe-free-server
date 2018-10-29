package ru.runa.af.web.orgfunction;

import java.util.List;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

public class ActorCodeRenderer extends ExecutorRendererBase {

    @Override
    protected List<? extends Executor> loadExecutors(User user) throws Exception {
        BatchPresentation batchPresentation = BatchPresentationFactory.ACTORS.createNonPaged();
        batchPresentation.setFieldsToSort(new int[] { 1 }, new boolean[] { true });
        return Delegates.getExecutorService().getExecutors(user, batchPresentation);
    }

    @Override
    protected String getValue(Executor executor) {
        return String.valueOf(((Actor) executor).getCode());
    }

    @Override
    protected Executor getExecutor(User user, String code) throws Exception {
        return Delegates.getExecutorService().getActorByCode(user, Long.valueOf(code));
    }
}
