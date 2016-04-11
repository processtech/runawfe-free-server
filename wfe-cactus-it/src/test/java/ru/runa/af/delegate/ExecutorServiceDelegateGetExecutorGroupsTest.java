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

import java.util.List;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.service.ServiceTestHelper;
import ru.runa.junit.ArrayAssert;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.ExecutorPermission;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.GroupPermission;
import ru.runa.wfe.user.User;

import com.google.common.collect.Lists;

public class ExecutorServiceDelegateGetExecutorGroupsTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateGetExecutorGroupsTest.class.getName();

    private Group group;

    private Group subGroup;

    private Actor actor;

    @Override
    protected void setUp() throws Exception {
        executorService = Delegates.getExecutorService();
        th = new ServiceTestHelper(testPrefix);
        th.createDefaultExecutorsMap();
        List<Permission> readUpdatePermissions = Lists.newArrayList(Permission.READ, ExecutorPermission.UPDATE);

        actor = th.getSubGroupActor();
        th.setPermissionsToAuthorizedPerformer(readUpdatePermissions, actor);
        group = th.getBaseGroup();
        subGroup = th.getSubGroup();
        th.setPermissionsToAuthorizedPerformer(readUpdatePermissions, group);
        th.setPermissionsToAuthorizedPerformer(readUpdatePermissions, subGroup);
        super.setUp();
    }

    final public void testGetExecutorGroupsByAuthorizedPerformer1() throws Exception {
        List<Group> calculatedGroups = executorService.getExecutorGroups(th.getAuthorizedPerformerUser(), actor, th.getExecutorBatchPresentation(),
                false);
        List<Group> realGroups = Lists.newArrayList(subGroup);
        ArrayAssert.assertWeakEqualArrays("businessDelegate.getExecutorGroups( actor ) returns wrong group set", realGroups, calculatedGroups);
    }

    final public void testGetExecutorGroupsByAuthorizedPerformer2() throws Exception {
        List<Group> calculatedGroups = executorService.getExecutorGroups(th.getAuthorizedPerformerUser(), subGroup,
                th.getExecutorBatchPresentation(), false);
        List<Group> realGroups = Lists.newArrayList(group);
        ArrayAssert.assertWeakEqualArrays("businessDelegate.getExecutorGroups( group ) returns wrong group set", realGroups, calculatedGroups);
    }

    final public void testGetExecutorGroupsByAuthorizedPerformer3() throws Exception {
        List<Permission> readUpdateAddToGroupPermissions = Lists.newArrayList(Permission.READ, ExecutorPermission.UPDATE,
                GroupPermission.ADD_TO_GROUP);
        th.setPermissionsToAuthorizedPerformer(readUpdateAddToGroupPermissions, group);
        th.addExecutorToGroup(getActor(), getGroup());
        List<Group> calculatedGroups = executorService.getExecutorGroups(th.getAuthorizedPerformerUser(), getActor(),
                th.getExecutorBatchPresentation(), false);
        List<Group> realGroups = Lists.newArrayList(subGroup, getGroup());
        ArrayAssert.assertWeakEqualArrays("businessDelegate.getExecutorGroups() returns wrong group set", realGroups, calculatedGroups);
    }

    private Group getGroup() throws InternalApplicationException, AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        return executorService.getExecutor(th.getAdminUser(), group.getId());
    }

    private Actor getActor() throws InternalApplicationException, AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        return executorService.getExecutor(th.getAdminUser(), actor.getId());
    }

    final public void testGetExecutorGroupsByAuthorizedPerformer4() throws Exception {
        List<Permission> readUpdateAddToGroupPermissions = Lists.newArrayList(Permission.READ, ExecutorPermission.UPDATE,
                GroupPermission.ADD_TO_GROUP);
        List<Permission> noPermissionArray = Lists.newArrayList();
        th.setPermissionsToAuthorizedPerformer(readUpdateAddToGroupPermissions, group);
        th.addExecutorToGroup(getActor(), getGroup());
        th.setPermissionsToAuthorizedPerformer(noPermissionArray, subGroup);
        List<Group> calculatedGroups = executorService.getExecutorGroups(th.getAuthorizedPerformerUser(), getActor(),
                th.getExecutorBatchPresentation(), false);
        List<Group> realGroups = Lists.newArrayList(getGroup());
        ArrayAssert.assertWeakEqualArrays("businessDelegate.getExecutorGroups() returns wrong group set", realGroups, calculatedGroups);
    }

    public void testGetExecutorGroupsByUnauthorizedPerformer1() throws Exception {
        try {
            executorService.getExecutorGroups(th.getUnauthorizedPerformerUser(), actor, th.getExecutorBatchPresentation(), false);
            fail("businessDelegate.getExecutorGroupsByUnauthorizedPerformer(actor) no AuthorizationFailedException");
        } catch (AuthorizationException e) {
            // That's what we expect
        }
    }

    public void testGetExecutorGroupsByUnauthorizedPerformer2() throws Exception {
        try {
            executorService.getExecutorGroups(th.getUnauthorizedPerformerUser(), subGroup, th.getExecutorBatchPresentation(), false);
            fail("businessDelegate.getExecutorGroupsByUnauthorizedPerformer(subGroup) no AuthorizationFailedException");
        } catch (AuthorizationException e) {
            // That's what we expect
        }
    }

    public void testGetExecutorGroupsWithNullSubject() throws Exception {
        try {
            executorService.getExecutorGroups(null, actor, th.getExecutorBatchPresentation(), false);
            fail("GetExecutorGroupswithNullSubject no Exception");
        } catch (IllegalArgumentException e) {
            // That's what we expect
        }
    }

    public void testGetExecutorGroupsWithFakeSubject() throws Exception {
        try {
            User fakeUser = th.getFakeUser();
            executorService.getExecutorGroups(fakeUser, actor, th.getExecutorBatchPresentation(), false);
            fail("testGetExecutorGroupsWithFakeSubject no Exception");
        } catch (AuthenticationException e) {
            // That's what we expect
        }
    }

    @Override
    protected void tearDown() throws Exception {
        th.releaseResources();
        th = null;
        executorService = null;
        actor = null;
        group = null;
        subGroup = null;
        super.tearDown();
    }
}
