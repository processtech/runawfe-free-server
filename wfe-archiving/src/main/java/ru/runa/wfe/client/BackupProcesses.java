package ru.runa.wfe.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BackupProcesses {

    protected static final Log log = LogFactory.getLog(BackupProcesses.class);

    public static void main(String[] args) {
        try {
            ArchivingApplication app = new ArchivingApplication();
            app.backupProcesses(args);
        } catch (Exception e) {
            log.error("", e);
        }
    }
}
