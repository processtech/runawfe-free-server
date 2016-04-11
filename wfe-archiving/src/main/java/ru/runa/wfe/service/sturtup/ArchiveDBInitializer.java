package ru.runa.wfe.service.sturtup;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.service.ArchiveInitializerService;
import ru.runa.wfe.service.delegate.ArchiveInitializerServiceDelegate;
import ru.runa.wfe.service.delegate.Delegates;

@Singleton
@Startup
public class ArchiveDBInitializer {

    public static Log log = LogFactory.getLog(ArchiveDBInitializer.class);

    @PostConstruct
    public void initialize() {
        log.info("initializing archive database");
        try {
            ArchiveInitializerService archiveInitializerService = Delegates.createDelegate(ArchiveInitializerServiceDelegate.class);
            archiveInitializerService.onSystemStartup();
        } catch (Exception e) {
            log.error("", e);
        }
        log.info("initialization done in class loader " + Thread.currentThread().getContextClassLoader());
    }
}
