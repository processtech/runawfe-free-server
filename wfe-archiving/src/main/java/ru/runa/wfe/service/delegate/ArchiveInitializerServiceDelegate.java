package ru.runa.wfe.service.delegate;

import ru.runa.wfe.service.ArchiveInitializerService;

public class ArchiveInitializerServiceDelegate extends EJB3Delegate implements ArchiveInitializerService {

    public ArchiveInitializerServiceDelegate() {
        super("ArchiveInitializerServiceBean", ArchiveInitializerService.class, "wfe-archiving");
    }

    private ArchiveInitializerService getArchiveInitializerService() {
        return (ArchiveInitializerService) getService();
    }

    @Override
    public void onSystemStartup() {
        try {
            getArchiveInitializerService().onSystemStartup();
        } catch (Exception e) {
            throw handleException(e);
        }
    }

}
