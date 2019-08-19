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

package ru.runa.af.delegate;

import com.google.common.collect.Lists;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.*;

import java.util.Collection;
import java.util.List;

public class ExecutorServiceDelegateRemoveManyExecutorsFromGroupTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateRemoveManyExecutorsFromGroupTest.class.getName();

    private Group additionalGroup;

    private List<Actor> additionalActors;

    private final Collection<Permission> updatePermissions = Lists.newArrayList(Permission.UPDATE);

    private final Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);

    private List<Executor> getAdditionalActors()
            throws InternalApplicationException, AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        List<Long> ids = Lists.newArrayList();
        for (Executor executor : additionalActors) {
            ids.add(executor.getId());
        }
        return th.getExecutors(th.getAdminUser(), ids);
    }

    private Group getAdditionalGroup()
            throws InternalApplicationException, AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        return executorService.getExecutor(th.getAuthorizedPerformerUser(), additionalGroup.getId());
    }

    protected void setUp() throws Exception {
        executorService = Delegates.getExecutorService();
        th = new ServiceTestHelper(testPrefix);

        additionalGroup = th.createGroupIfNotExist("additionalG", "Additional Group");
        additionalActors = th.createActorArray("additionalMixed", "Additional Mixed");

        th.setPermissionsToAuthorizedPerformer(updatePermissions, additionalGroup);
        th.setPermissionsToAuthorizedPerformerOnExecutorsList(updatePermissions, additionalActors);

        executorService.addExecutorsToGroup(th.getAuthorizedPerformerUser(), th.toIds(additionalActors), additionalGroup.getId());

        super.setUp();
    }

    public void testRemoveExecutorsFromGroupByAuthorizedPerformer() throws Exception {

        assertTrue("Executor is not in group before removing", th.isExecutorsInGroup(getAdditionalActors(), getAdditionalGroup()));

        th.setPermissionsToAuthorizedPerformer(readPermissions, getAdditionalGroup());
        List<Executor> executors = getAdditionalActors();
        try {
            executorService.removeExecutorsFromGroup(th.getAuthorizedPerformerUser(), th.toIds(executors), getAdditionalGroup().getId());
            fail("Executors removed from group without corresponding permissions");
        } catch (AuthorizationException e) {
            // this is supposed result
        }

        th.setPermissionsToAuthorizedPerformer(updatePermissions, getAdditionalGroup());

        executorService.removeExecutorsFromGroup(th.getAuthorizedPerformerUser(), th.toIds(getAdditionalActors()), getAdditionalGroup().getId());

        assertFalse("Executor not removed from group ", th.isExecutorsInGroup(getAdditionalActors(), getAdditionalGroup()));
    }

    public void testRemoveExecutorsFromGroupByUnAuthorizedPerformer() throws Exception {
        List<Executor> executors = getAdditionalActors();
        try {
            executorService.removeExecutorsFromGroup(th.getUnauthorizedPerformerUser(), th.toIds(executors), getAdditionalGroup().getId());
            fail("Executors is removed from group ByUnAuthorizedPerformer");
        } catch (AuthorizationException e) {
            // this is supposed result
        }
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        executorService = null;
        additionalGroup = null;
        additionalActors = null;
        super.tearDown();
    }
}
