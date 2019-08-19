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

/*
 */
public class ExecutorServiceDelegateRemoveExecutorFromManyGroupsTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateRemoveExecutorFromManyGroupsTest.class.getName();

    private long additionalGroupId;
    private long additionalActorId;

    private List<Long> additionalGroupsIds;

    private final Collection<Permission> updatePermissions = Lists.newArrayList(Permission.UPDATE);
    private final Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);

    protected void setUp() throws Exception {
        executorService = Delegates.getExecutorService();
        th = new ServiceTestHelper(testPrefix);

        Actor additionalActor = th.createActorIfNotExist("additionalA", "Additional Actor");
        Group additionalGroup = th.createGroupIfNotExist("additionalG", "Additional Group");
        List<Group> additionalGroups = th.createGroupArray("additionalGroups", "Additional Groups");

        additionalActorId = additionalActor.getId();
        additionalGroupId = additionalGroup.getId();
        additionalGroupsIds = Lists.newArrayList();
        for (Group group : additionalGroups) {
            additionalGroupsIds.add(group.getId());
        }

        th.setPermissionsToAuthorizedPerformer(updatePermissions, getAdditionalActor());
        th.setPermissionsToAuthorizedPerformer(updatePermissions, getAdditionalGroup());
        th.setPermissionsToAuthorizedPerformerOnExecutorsList(updatePermissions, getAdditionalGroups());

        executorService.addExecutorToGroups(th.getAuthorizedPerformerUser(), getAdditionalActor().getId(), th.toIds(getAdditionalGroups()));
        executorService.addExecutorToGroups(th.getAuthorizedPerformerUser(), getAdditionalGroup().getId(), th.toIds(getAdditionalGroups()));

        super.setUp();
    }

    private List<Group> getAdditionalGroups()
            throws InternalApplicationException, AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        return th.getExecutors(th.getAdminUser(), additionalGroupsIds);
    }

    private Actor getAdditionalActor()
            throws InternalApplicationException, AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        return executorService.getExecutor(th.getAdminUser(), additionalActorId);
    }

    private Group getAdditionalGroup()
            throws InternalApplicationException, AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        return executorService.getExecutor(th.getAdminUser(), additionalGroupId);
    }

    public void testRemoveActorFromGroupsByAuthorizedPerformer() throws Exception {

        assertTrue("Executor is not in groups before removing", th.isExecutorInGroups(getAdditionalActor(), getAdditionalGroups()));

        th.setPermissionsToAuthorizedPerformerOnExecutorsList(readPermissions, getAdditionalGroups());
        Executor executor = getAdditionalActor();
        try {
            executorService.removeExecutorFromGroups(th.getAuthorizedPerformerUser(), executor.getId(), th.toIds(getAdditionalGroups()));
            fail("Executors removed from group without corresponding permissions");
        } catch (AuthorizationException e) {
            // this is supposed result
        }

        th.setPermissionsToAuthorizedPerformerOnExecutorsList(updatePermissions, getAdditionalGroups());
        executorService.removeExecutorFromGroups(th.getAuthorizedPerformerUser(), executor.getId(), th.toIds(getAdditionalGroups()));

        assertFalse("Executor not removed from group ", th.isExecutorInGroups(getAdditionalActor(), getAdditionalGroups()));
    }

    public void testRemoveGrouprFromGroupsByAuthorizedPerformer() throws Exception {

        assertTrue("Executor is not in groups before removing", th.isExecutorInGroups(getAdditionalActor(), getAdditionalGroups()));

        th.setPermissionsToAuthorizedPerformerOnExecutorsList(readPermissions, getAdditionalGroups());
        Executor executor = getAdditionalGroup();
        try {
            executorService.removeExecutorFromGroups(th.getAuthorizedPerformerUser(), executor.getId(), th.toIds(getAdditionalGroups()));
            fail("Executors removed from group without corresponding permissions");
        } catch (AuthorizationException e) {
            // this is supposed result
        }

        th.setPermissionsToAuthorizedPerformerOnExecutorsList(updatePermissions, getAdditionalGroups());
        executorService.removeExecutorFromGroups(th.getAuthorizedPerformerUser(), executor.getId(), th.toIds(getAdditionalGroups()));

        assertFalse("Executor not removed from group ", th.isExecutorInGroups(getAdditionalGroup(), getAdditionalGroups()));
    }

    public void testRemoveActorFromGroupsByUnAuthorizedPerformer() throws Exception {
        Executor executor = getAdditionalActor();
        try {
            executorService.removeExecutorFromGroups(th.getUnauthorizedPerformerUser(), executor.getId(), th.toIds(getAdditionalGroups()));
            fail("Executor is removed from groups ByUnAuthorizedPerformer");
        } catch (AuthorizationException e) {
            // this is supposed result
        }
    }

    public void testRemoveGroupFromGroupsByUnAuthorizedPerformer() throws Exception {
        Executor executor = getAdditionalGroup();
        try {
            executorService.removeExecutorFromGroups(th.getUnauthorizedPerformerUser(), executor.getId(), th.toIds(getAdditionalGroups()));
            fail("Executor is removed from groups ByUnAuthorizedPerformer");
        } catch (AuthorizationException e) {
            // this is supposed result
        }
    }

    public void testRemoveFakeActorFromGroups() throws Exception {
        Executor executor = th.getFakeActor();
        try {
            executorService.removeExecutorFromGroups(th.getAuthorizedPerformerUser(), executor.getId(), th.toIds(getAdditionalGroups()));
            fail("FakeExecutor removed from groups ");
        } catch (AuthorizationException e) {
            // TODO
        } catch (ExecutorDoesNotExistException e) {
            // this is supposed result
            fail("TODO trap");
        }
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        executorService = null;
        super.tearDown();
    }
}
