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
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.*;

import java.util.Collection;
import java.util.Map;

public class ExecutorServiceDelegateHierarchyPermissionsTest extends ServletTestCase {

    private static final String ACTOR_PWD = "ActorPWD";

    private ServiceTestHelper th;

    private ExecutorService executorService;

    private AuthorizationService authorizationService;

    private static String testPrefix = ExecutorServiceDelegateHierarchyPermissionsTest.class.getName();

    private Actor actor;

    private Group group;

    protected void setUp() throws Exception {
        executorService = Delegates.getExecutorService();
        authorizationService = Delegates.getAuthorizationService();
        th = new ServiceTestHelper(testPrefix);
        th.createDefaultExecutorsMap();
        Collection<Permission> updatePermission = Lists.newArrayList(Permission.UPDATE);
        Collection<Permission> loginPermissions = Lists.newArrayList(Permission.LOGIN);
        Collection<Permission> createExecutorPermissions = Lists.newArrayList(Permission.CREATE);

        Map<String, Executor> executorsMap = th.getDefaultExecutorsMap();

        actor = (Actor) executorsMap.get(ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
        group = (Group) executorsMap.get(ServiceTestHelper.BASE_GROUP_NAME);

        th.setPermissionsToAuthorizedPerformerOnExecutors(updatePermission);

        actor = executorService.getExecutor(th.getAdminUser(), actor.getId());
        authorizationService.setPermissions(th.getAuthorizedPerformerUser(), actor.getId(), loginPermissions, SecuredSingleton.EXECUTORS);
        group = executorService.getExecutor(th.getAdminUser(), group.getId());
        authorizationService.setPermissions(th.getAuthorizedPerformerUser(), group.getId(), createExecutorPermissions, SecuredSingleton.EXECUTORS);
        actor = executorService.getExecutor(th.getAdminUser(), actor.getId());
        executorService.setPassword(th.getAuthorizedPerformerUser(), actor, ACTOR_PWD);

        super.setUp();
    }

    public void testPermissionsInheritance() throws Exception {
        User additionalUser = Delegates.getAuthenticationService().authenticateByLoginPassword(actor.getName(), ACTOR_PWD);

        if (!authorizationService.isAllowed(additionalUser, Permission.CREATE, SecuredSingleton.EXECUTORS)) {
            assertTrue("unproper createExecutor permission ", false);
        }
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        executorService = null;
        authorizationService = null;
        actor = null;
        group = null;

        super.tearDown();
    }

}
