package ru.runa.wfe.job.impl;

import java.util.List;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.user.TemporaryGroup;
import ru.runa.wfe.user.dao.ExecutorDao;
import ru.runa.wfe.user.logic.ExecutorLogic;

@CommonsLog
public class UnusedTemporaryGroupsCleaner {

    @Autowired
    private ExecutorLogic executorLogic;
    @Autowired
    private ExecutorDao executorDao;

    @Transactional
    public void execute() {
        if (!ApplicationContextFactory.getInitializerLogic().isInitialized()) {
            // Do not interfere with migrations.
            return;
        }

        List<TemporaryGroup> groups = executorDao.getUnusedTemporaryGroups();
        log.debug("Removing " + groups.size() + " groups");
        for (TemporaryGroup group : groups) {
            executorLogic.remove(group);
        }
    }
}
