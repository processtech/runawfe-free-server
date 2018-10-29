package ru.runa.wfe.service.delegate;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.service.ProfileService;
import ru.runa.wfe.user.Profile;
import ru.runa.wfe.user.User;

public class ProfileServiceDelegate extends Ejb3Delegate implements ProfileService {

    public ProfileServiceDelegate() {
        super(ProfileService.class);
    }

    private ProfileService getProfileService() {
        return getService();
    }

    @Override
    public Profile getProfile(User user) {
        try {
            return getProfileService().getProfile(user);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public Profile setActiveBatchPresentation(User user, String batchPresentationId, String newActiveBatchName) {
        try {
            return getProfileService().setActiveBatchPresentation(user, batchPresentationId, newActiveBatchName);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public Profile deleteBatchPresentation(User user, BatchPresentation batchPresentation) {
        try {
            return getProfileService().deleteBatchPresentation(user, batchPresentation);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public Profile createBatchPresentation(User user, BatchPresentation batchPresentation) {
        try {
            return getProfileService().createBatchPresentation(user, batchPresentation);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public Profile saveBatchPresentation(User user, BatchPresentation batchPresentation) {
        try {
            return getProfileService().saveBatchPresentation(user, batchPresentation);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

}
