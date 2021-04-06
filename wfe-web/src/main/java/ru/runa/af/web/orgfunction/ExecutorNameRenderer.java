package ru.runa.af.web.orgfunction;

import java.util.List;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

public class ExecutorNameRenderer extends ExecutorRendererBase {

    @Override
    protected List<? extends Executor> loadExecutors(User user) throws Exception {
        ExecutorService executorService = Delegates.getExecutorService();
        BatchPresentation batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        batchPresentation.setFieldsToSort(new int[] { 1 }, new boolean[] { true });
        return executorService.getExecutors(user, batchPresentation);
    }

    @Override
    protected String getValue(Executor executor) {
        return executor.getName();
    }

    @Override
    protected Executor getExecutor(User user, String name) throws Exception {
        ExecutorService executorService = Delegates.getExecutorService();
        return executorService.getExecutorByName(user, name);
    }
}
