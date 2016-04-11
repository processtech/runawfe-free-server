package ru.runa.wfe.service.client;

import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.IExecutorLoader;
import ru.runa.wfe.user.User;

public class DelegateExecutorLoader implements IExecutorLoader {
    private final User user;
    private final ExecutorService executorService;

    public DelegateExecutorLoader(User user, ExecutorService executorService) {
        this.user = user;
        this.executorService = executorService;
    }

    public DelegateExecutorLoader(User user) {
        this(user, Delegates.getExecutorService());
    }

    @Override
    public Executor getExecutor(Long id) {
        return executorService.getExecutor(user, id);
    }

    @Override
    public Actor getActorByCode(Long code) {
        return executorService.getActorByCode(user, code);
    }

    @Override
    public Executor getExecutor(String name) {
        return executorService.getExecutorByName(user, name);
    }
}
