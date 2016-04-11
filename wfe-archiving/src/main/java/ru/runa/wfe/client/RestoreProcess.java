package ru.runa.wfe.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RestoreProcess {

    protected static final Log log = LogFactory.getLog(RestoreProcess.class);

    public static void main(String[] args) {
        try {
            ArchivingApplication app = new ArchivingApplication();
            app.restoreProcess(args);
        } catch (Exception e) {
            log.error("", e);
        }
    }
}
