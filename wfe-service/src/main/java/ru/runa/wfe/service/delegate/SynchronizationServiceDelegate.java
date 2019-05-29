package ru.runa.wfe.service.delegate;

import ru.runa.wfe.service.SynchronizationService;
import ru.runa.wfe.user.User;

public class SynchronizationServiceDelegate extends Ejb3Delegate implements SynchronizationService {

    public SynchronizationServiceDelegate() {
        super("SynchronizationServiceBean", SynchronizationService.class);
    }

    private SynchronizationService getSynchronizationService() {
        return (SynchronizationService) getService();
    }

    @Override
    public int synchronizeExecutorsWithLdap(User user) {
        try {
            return getSynchronizationService().synchronizeExecutorsWithLdap(user);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

}
