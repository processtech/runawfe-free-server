package ru.runa.wfe.job.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.execution.dao.SignalDao;

public class ExpiredSignalsCleaner {
    protected final Log log = LogFactory.getLog(getClass());
    @Autowired
    protected SignalDao signalDao;

    @Transactional
    @Scheduled(fixedDelayString = "${timertask.period.millis.expired.signals.execution}")
    public void execute() {
        log.debug("Cleaning expired signals");
        signalDao.deleteAllExpired();
    }

}
