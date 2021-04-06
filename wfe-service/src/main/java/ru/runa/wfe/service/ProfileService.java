package ru.runa.wfe.service;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.user.Profile;
import ru.runa.wfe.user.User;

/**
 * Service to operate with user profiles.
 * 
 * @author dofs
 * @since 4.0
 */
public interface ProfileService {

    /**
     * Loads user profile
     */
    public Profile getProfile(User user);

    /**
     * Sets specified by category and name batch presentation as active for this
     * user profile.
     * 
     * @return updated profile
     */
    public Profile setActiveBatchPresentation(User user, String category, String newName);

    /**
     * Deletes batch presentation in user profile.
     * 
     * @param user
     * @param batchPresentation
     * @return updated profile
     */
    public Profile deleteBatchPresentation(User user, BatchPresentation batchPresentation);

    /**
     * Creates new batch presentation in user profile.
     * 
     * @param user
     * @param batchPresentation
     * @return updated profile
     */
    public Profile createBatchPresentation(User user, BatchPresentation batchPresentation);

    /**
     * Saves batch presentation in user profile.
     * 
     * @return updated profile
     */
    public Profile saveBatchPresentation(User user, BatchPresentation batchPresentation);

}
