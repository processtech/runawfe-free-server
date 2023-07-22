/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
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
