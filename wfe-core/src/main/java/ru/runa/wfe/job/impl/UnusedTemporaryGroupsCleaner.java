package ru.runa.wfe.job.impl;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.execution.dao.ProcessDao;
import ru.runa.wfe.user.TemporaryGroup;
import ru.runa.wfe.user.dao.ExecutorDAO;
import ru.runa.wfe.user.logic.ExecutorLogic;

public class UnusedTemporaryGroupsCleaner {
    protected final Log log = LogFactory.getLog(getClass());
    @Autowired
    private ExecutorLogic executorLogic;
    @Autowired
    private ExecutorDAO executorDAO;
    @Autowired
    private ProcessDao processDao;

    @Transactional
    public void execute() {
        List<TemporaryGroup> groups = executorDAO.getUnusedTemporaryGroups();
        log.debug("Removing " + groups.size() + " groups");
        for (TemporaryGroup group : groups) {
            executorLogic.remove(group);
        }
    }

}
