package ru.runa.wfe.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BackupProcessDefinitions {

    protected static final Log log = LogFactory.getLog(BackupProcessDefinitions.class);

    public static void main(String[] args) {
        try {
            ArchivingApplication app = new ArchivingApplication();
            app.backupProcessDefinition(args);
        } catch (Exception e) {
            log.error("", e);
        }
    }
}
