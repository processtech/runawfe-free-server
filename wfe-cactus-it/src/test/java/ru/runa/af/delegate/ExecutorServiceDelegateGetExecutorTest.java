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
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.*;

import java.util.List;
import java.util.Map;

public class ExecutorServiceDelegateGetExecutorTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateGetExecutorTest.class.getName();

    private Group group;

    private Actor actor;

    private Map<String, Executor> executorsMap;

    protected void setUp() throws Exception {
        executorService = Delegates.getExecutorService();
        th = new ServiceTestHelper(testPrefix);
        th.createDefaultExecutorsMap();
        List<Permission> readPermissions = Lists.newArrayList(Permission.READ);
        executorsMap = th.getDefaultExecutorsMap();

        actor = (Actor) executorsMap.get(ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
        th.setPermissionsToAuthorizedPerformer(readPermissions, actor);
        group = (Group) executorsMap.get(ServiceTestHelper.BASE_GROUP_NAME);
        th.setPermissionsToAuthorizedPerformer(readPermissions, group);
        super.setUp();
    }

    public void testGetActorByAuthorizedPerformer() throws Exception {
        Actor returnedBaseGroupActor = executorService.getExecutorByName(th.getAuthorizedPerformerUser(),
                testPrefix + ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
        assertEquals("actor retuned by businessDelegate differes with expected", actor, returnedBaseGroupActor);
        Group returnedBaseGroup = executorService.getExecutorByName(th.getAuthorizedPerformerUser(), testPrefix + ServiceTestHelper.BASE_GROUP_NAME);
        assertEquals("actor retuned by businessDelegate differes with expected", group, returnedBaseGroup);
    }

    public void testGetExecutorByUnauthorizedPerformer() throws Exception {
        User unauthorizedPerformerUser = th.getUnauthorizedPerformerUser();
        try {
            executorService.getExecutorByName(unauthorizedPerformerUser, testPrefix + ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
            fail("businessDelegate allow to getExecutor() to performer without Permission.READ.");
        } catch (AuthorizationException e) {
            // That's what we expect
        }
        try {
            executorService.getExecutorByName(unauthorizedPerformerUser, testPrefix + ServiceTestHelper.BASE_GROUP_NAME);
            fail("businessDelegate allow to getExecutor() to performer without Permission.READ.");
        } catch (AuthorizationException e) {
            // That's what we expect
        }
    }

    public void testGetUnexistentExecutorByAuthorizedPerformer() throws Exception {
        User authorizedPerformerUser = th.getAuthorizedPerformerUser();
        try {
            executorService.getExecutorByName(authorizedPerformerUser, testPrefix + "unexistent actor name");
            fail("businessDelegate does not throw Exception to getExecutor() to performer without Permission.READ");
        } catch (ExecutorDoesNotExistException e) {
            // That's what we expect
        }
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        executorService = null;
        actor = null;
        group = null;
        super.tearDown();
    }
}
