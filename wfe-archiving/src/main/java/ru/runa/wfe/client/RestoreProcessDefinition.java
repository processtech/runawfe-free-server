package ru.runa.wfe.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RestoreProcessDefinition {

    protected static final Log log = LogFactory.getLog(RestoreProcessDefinition.class);

    public static void main(String[] args) {
        try {
            ArchivingApplication app = new ArchivingApplication();
            app.restoreProcessDefinition(args);
        } catch (Exception e) {
            log.error("", e);
        }
    }

}
