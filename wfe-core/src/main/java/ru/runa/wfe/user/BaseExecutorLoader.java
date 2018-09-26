package ru.runa.wfe.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.user.dao.ExecutorDao;

@Component
@Transactional
public class BaseExecutorLoader implements ExecutorLoader {

    @Autowired
    protected ExecutorDao executorDao;

    @Override
    public Executor getExecutor(Long id) {
        return executorDao.getExecutor(id);
    }

    @Override
    public Actor getActorByCode(Long code) {
        return executorDao.getActorByCode(code);
    }

    @Override
    public Executor getExecutor(String name) {
        return executorDao.getExecutor(name);
    }

}
