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
import lombok.val;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

public class ExecutorServiceDelegateGetExecutorTest extends ServletTestCase {
    private final String PREFIX = getClass().getName();
    private ServiceTestHelper h;
    private ExecutorService executorService;

    private Group group;
    private Actor actor;

    @Override
    protected void setUp() {
        h = new ServiceTestHelper(PREFIX);
        executorService = Delegates.getExecutorService();

        h.createDefaultExecutorsMap();
        val executorsMap = h.getDefaultExecutorsMap();
        actor = (Actor) executorsMap.get(ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
        group = (Group) executorsMap.get(ServiceTestHelper.BASE_GROUP_NAME);

        val pp = Lists.newArrayList(Permission.READ);
        h.setPermissionsToAuthorizedActor(pp, actor);
        h.setPermissionsToAuthorizedActor(pp, group);
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        executorService = null;
        actor = null;
        group = null;
    }

    public void testGetActorByAuthorizedUser() {
        Actor returnedBaseGroupActor = executorService.getExecutorByName(h.getAuthorizedUser(),
                PREFIX + ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
        assertEquals("actor retuned by businessDelegate differes with expected", actor, returnedBaseGroupActor);
        Group returnedBaseGroup = executorService.getExecutorByName(h.getAuthorizedUser(), PREFIX + ServiceTestHelper.BASE_GROUP_NAME);
        assertEquals("actor retuned by businessDelegate differes with expected", group, returnedBaseGroup);
    }

    public void testGetExecutorByUnauthorizedUser() {
        User unauthorizedUser = h.getUnauthorizedUser();
        try {
            executorService.getExecutorByName(unauthorizedUser, PREFIX + ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
            fail("businessDelegate allow to getExecutor() to performer without Permission.READ.");
        } catch (AuthorizationException e) {
            // Expected.
        }
        try {
            executorService.getExecutorByName(unauthorizedUser, PREFIX + ServiceTestHelper.BASE_GROUP_NAME);
            fail("businessDelegate allow to getExecutor() to performer without Permission.READ.");
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testGetUnexistentExecutorByAuthorizedUser() {
        User authorizedUser = h.getAuthorizedUser();
        try {
            executorService.getExecutorByName(authorizedUser, PREFIX + "unexistent actor name");
            fail("businessDelegate does not throw Exception to getExecutor() to performer without Permission.READ");
        } catch (ExecutorDoesNotExistException e) {
            // Expected.
        }
    }
}
