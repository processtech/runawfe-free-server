package ru.runa.wfe.service.delegate;

import ru.runa.wfe.service.ArchivingService;
import ru.runa.wfe.user.User;

public class ArchivingServiceDelegate extends EJB3Delegate implements ArchivingService {

    public ArchivingServiceDelegate() {
        super("ArchivingServiceBean", ArchivingService.class, "wfe-archiving");
    }

    private ArchivingService getArchivingService() {
        return getService();
    }

    @Override
    public void backupProcess(User user, Long processId) {
        try {
            getArchivingService().backupProcess(user, processId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void backupProcessDefinition(User user, String definitionName, Long version) {
        try {
            getArchivingService().backupProcessDefinition(user, definitionName, version);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void restoreProcess(User user, Long processId) {
        try {
            getArchivingService().restoreProcess(user, processId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void restoreProcessDefinition(User user, String definitionName, Long version) {
        try {
            getArchivingService().restoreProcessDefinition(user, definitionName, version);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    public static ArchivingService getArchivingServiceStatic() {
        ArchivingServiceDelegate archivingService = Delegates.createDelegate(ArchivingServiceDelegate.class);
        return archivingService;
    }

}
