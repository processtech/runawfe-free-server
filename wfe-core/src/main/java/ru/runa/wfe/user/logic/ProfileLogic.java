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
package ru.runa.wfe.user.logic;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.dao.BatchPresentationDao;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Profile;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.dao.ProfileDao;

/**
 * Actor's profile management.
 * 
 * @author Dofs
 * @since 1.0
 */
public class ProfileLogic extends CommonLogic {

    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private BatchPresentationDao batchPresentationDao;

    public List<Profile> getProfiles(User user, List<Long> actorIds) throws ExecutorDoesNotExistException {
        List<Profile> result = Lists.newArrayListWithCapacity(actorIds.size());
        for (Long actorId : actorIds) {
            Actor actor = executorDao.getActor(actorId);
            permissionDao.checkAllowed(user, Permission.LIST, actor);
            result.add(getProfile(actor));
        }
        return result;
    }

    public Profile getProfile(Actor actor) {
        Profile profile = profileDao.get(actor);
        if (profile == null) {
            profile = new Profile(actor);
            profileDao.create(profile);
        }
        profile.setAdministrator(executorDao.isAdministrator(actor));
        List<BatchPresentation> sharedPresentations = batchPresentationDao.getAllShared();
        Set<BatchPresentation> existing = profile.getBatchPresentations();
        for (BatchPresentation presentation : sharedPresentations) {
            if (!existing.contains(presentation)) {
                profile.addSharedBatchPresentation(presentation);
            }
        }
        return profile;
    }

    public void updateProfiles(User user, List<Profile> profiles) {
        for (Profile profile : profiles) {
            permissionDao.checkAllowed(user, Permission.UPDATE, profile.getActor());
            profileDao.update(profile);
        }
    }

    public Profile changeActiveBatchPresentation(User user, String category, String newActiveBatchName) {
        Profile profile = getProfile(user.getActor());
        if (!profile.isAdministrator()) {
            if (getBatchPresentationByName(profile.getBatchPresentations(), category, newActiveBatchName) == null) {
                if (getBatchPresentationByName(profile.getBatchPresentations(), category, BatchPresentation.REFERENCE_SIGN + newActiveBatchName) != null) {
                    newActiveBatchName = BatchPresentation.REFERENCE_SIGN + newActiveBatchName;
                } else {
                    List<BatchPresentation> sharedPresentations = batchPresentationDao.getAllShared();
                    BatchPresentation sharedPresentation = getBatchPresentationByName(sharedPresentations, category, newActiveBatchName);
                    if (sharedPresentation != null) {
                        BatchPresentation presentationRef = sharedPresentation.clone();
                        presentationRef.setName(BatchPresentation.REFERENCE_SIGN + sharedPresentation.getName());
                        presentationRef.setShared(false);
                        presentationRef.setFieldsData(null);
                        profile.addBatchPresentation(presentationRef);
                        newActiveBatchName = BatchPresentation.REFERENCE_SIGN + newActiveBatchName;
                    }
                }
            }
        }
        profile.setActiveBatchPresentation(category, newActiveBatchName);
        return profile;
    }

    public Profile deleteBatchPresentation(User user, BatchPresentation batchPresentation) {
        Profile profile = getProfile(user.getActor());
        if (batchPresentation.isShared() && !profile.isAdministrator()) {
            throw new InternalApplicationException("cannot delete batch presentation, user is not administrator");
        }
        profile.deleteBatchPresentation(batchPresentation);
        return profile;
    }

    public Profile createBatchPresentation(User user, BatchPresentation batchPresentation) {
        Profile profile = getProfile(user.getActor());
        profile.addBatchPresentation(batchPresentation);
        profile.setActiveBatchPresentation(batchPresentation.getCategory(), batchPresentation.getName());
        return profile;
    }

    public Profile saveBatchPresentation(User user, BatchPresentation batchPresentation) {
        if (BatchPresentationConsts.DEFAULT_NAME.equals(batchPresentation.getName())) {
            throw new InternalApplicationException("default batch presentation cannot be changed");
        }
        if (batchPresentation.isShared() && !getProfile(user.getActor()).isAdministrator()) {
            throw new InternalApplicationException("cannot save batch presentation, user is not administrator");
        }
        batchPresentationDao.update(batchPresentation);
        batchPresentationDao.flushPendingChanges();
        return getProfile(user.getActor());
    }

    private BatchPresentation getBatchPresentationByName(Collection<BatchPresentation> presentations, String category, String name) {
        for (BatchPresentation presentation : presentations) {
            if (Objects.equal(presentation.getCategory(), category) && Objects.equal(presentation.getName(), name)) {
                return presentation;
            }
        }
        return null;
    }

}
