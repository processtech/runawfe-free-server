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
import java.util.Map;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.service.ServiceTestHelper;
import ru.runa.junit.ArrayAssert;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

import com.google.common.collect.Lists;

public class ExecutorServiceDelegateGetGroupsInThatExecutorNotPresentTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateGetGroupsInThatExecutorNotPresentTest.class.getName();

    private Group group;

    private Group subGroup;

    private Actor actor;

    private Map<String, Executor> executorsMap;

    @Override
    protected void setUp() throws Exception {
        executorService = Delegates.getExecutorService();
        th = new ServiceTestHelper(testPrefix);
        th.createDefaultExecutorsMap();
        List<Permission> readPermissions = Lists.newArrayList(Permission.READ);
        executorsMap = th.getDefaultExecutorsMap();

        actor = (Actor) executorsMap.get(ServiceTestHelper.SUB_GROUP_ACTOR_NAME);
        th.setPermissionsToAuthorizedPerformer(readPermissions, actor);
        group = (Group) executorsMap.get(ServiceTestHelper.BASE_GROUP_NAME);
        subGroup = (Group) executorsMap.get(ServiceTestHelper.SUB_GROUP_NAME);
        th.setPermissionsToAuthorizedPerformer(readPermissions, group);
        th.setPermissionsToAuthorizedPerformer(readPermissions, subGroup);

        actor = executorService.getExecutor(th.getAdminUser(), actor.getId());
        group = executorService.getExecutor(th.getAdminUser(), group.getId());
        subGroup = executorService.getExecutor(th.getAdminUser(), subGroup.getId());

        super.setUp();
    }

    final public void testgetExecutorsInThatExecutorNotPresentByAuthorizedPerformer1() throws Exception {
        List<Group> calculatedGroups = executorService.getExecutorGroups(th.getAuthorizedPerformerUser(), actor, th.getExecutorBatchPresentation(),
                true);
        List<Group> realGroups = Lists.newArrayList(group);
        ArrayAssert.assertWeakEqualArrays("businessDelegate.getExecutorsInThatExecutorNotPresent() returns wrong group set", realGroups,
                calculatedGroups);
    }

    final public void testgetExecutorsInThatExecutorNotPresentByAuthorizedPerformer2() throws Exception {
        List<Group> calculatedGroups = executorService.getExecutorGroups(th.getAuthorizedPerformerUser(), group, th.getExecutorBatchPresentation(),
                true);
        List<Group> realGroups = Lists.newArrayList(subGroup);
        ArrayAssert.assertWeakEqualArrays("businessDelegate.getExecutorsInThatExecutorNotPresent() returns wrong group set", realGroups,
                calculatedGroups);
    }

    public void testGetExecutorGroupsByUnauthorizedPerformer() throws Exception {
        try {
            executorService.getExecutorGroups(th.getUnauthorizedPerformerUser(), actor, th.getExecutorBatchPresentation(), true);
            assertTrue("businessDelegate.getExecutorsInThatExecutorNotPresent() no AuthorizationFailedException", false);
        } catch (AuthorizationException e) {
            // That's what we expect
        }
    }

    public void testGetExecutorGroupsWithNullSubject() throws Exception {
        try {
            executorService.getExecutorGroups(null, actor, th.getExecutorBatchPresentation(), true);
            assertTrue("getExecutorsInThatExecutorNotPresentwithNullSubject no Exception", false);
        } catch (IllegalArgumentException e) {
            // That's what we expect
        }
    }

    public void testGetExecutorGroupsWithoutPermission() throws Exception {
        try {
            List<Permission> noPermissions = Lists.newArrayList();
            th.setPermissionsToAuthorizedPerformer(noPermissions, actor);
            actor = executorService.getExecutor(th.getAdminUser(), actor.getId());
            executorService.getExecutorGroups(th.getAuthorizedPerformerUser(), actor, th.getExecutorBatchPresentation(), true);
            assertTrue("testgetExecutorsInThatExecutorNotPresentwithoutPermission no Exception", false);
        } catch (AuthorizationException e) {
            // That's what we expect
        }
    }

    public void testGetExecutorGroupsWithFakeSubject() throws Exception {
        try {
            User fakeUser = th.getFakeUser();
            executorService.getExecutorGroups(fakeUser, actor, th.getExecutorBatchPresentation(), true);
            assertTrue("testGetExecutorGroupsWithFakeSubject no Exception", false);
        } catch (AuthenticationException e) {
            // That's what we expect
        }
    }

    @Override
    protected void tearDown() throws Exception {
        th.releaseResources();
        th = null;
        executorsMap = null;
        executorService = null;
        actor = null;
        group = null;
        subGroup = null;
        super.tearDown();
    }
}
