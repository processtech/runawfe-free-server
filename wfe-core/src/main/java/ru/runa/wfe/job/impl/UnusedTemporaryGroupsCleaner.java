package ru.runa.wfe.job.impl;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.execution.dao.CurrentProcessDao;
import ru.runa.wfe.user.TemporaryGroup;
import ru.runa.wfe.user.dao.ExecutorDao;
import ru.runa.wfe.user.logic.ExecutorLogic;

public class UnusedTemporaryGroupsCleaner {
    protected final Log log = LogFactory.getLog(getClass());
    @Autowired
    private ExecutorLogic executorLogic;
    @Autowired
    private ExecutorDao executorDao;
    @Autowired
    private CurrentProcessDao currentProcessDao;

    @Transactional
    public void execute() {
        List<TemporaryGroup> groups = executorDao.getUnusedTemporaryGroups();
        log.debug("Removing " + groups.size() + " groups");
        for (TemporaryGroup group : groups) {
            executorLogic.remove(group);
        }
    }

}
