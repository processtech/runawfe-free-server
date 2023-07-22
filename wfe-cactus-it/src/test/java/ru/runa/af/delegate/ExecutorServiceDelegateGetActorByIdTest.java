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
import java.util.Map;
import lombok.val;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;

/**
 * Created on 01.11.2004.
 */
public class ExecutorServiceDelegateGetActorByIdTest extends ServletTestCase {
    private ServiceTestHelper h;
    private ExecutorService executorService;

    private Group group;
    private Actor actor;
    private Map<String, Executor> executorsMap;

    @Override
    protected void setUp() {
        h = new ServiceTestHelper(getClass().getName());
        executorService = Delegates.getExecutorService();

        h.createDefaultExecutorsMap();
        executorsMap = h.getDefaultExecutorsMap();
        actor = (Actor) executorsMap.get(ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
        group = (Group) executorsMap.get(ServiceTestHelper.BASE_GROUP_NAME);

        val readPermissions = Lists.newArrayList(Permission.READ);
        h.setPermissionsToAuthorizedActor(readPermissions, actor);
        h.setPermissionsToAuthorizedActor(readPermissions, group);
        h.setPermissionsToAuthorizedActor(readPermissions, executorsMap.get(ServiceTestHelper.SUB_GROUP_ACTOR_NAME));
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        executorService = null;
        actor = null;
        group = null;
    }

    public void testGetActorByAuthorizedUser() {
        Actor returnedBaseGroupActor = executorService.getExecutor(h.getAuthorizedUser(), actor.getId());
        assertEquals("actor retuned by businessDelegate differes with expected", actor, returnedBaseGroupActor);
        Actor subGroupActor = (Actor) executorsMap.get(ServiceTestHelper.SUB_GROUP_ACTOR_NAME);
        Actor returnedSubGroupActor = executorService.getExecutor(h.getAuthorizedUser(), subGroupActor.getId());
        assertEquals("actor retuned by businessDelegate differes with expected", subGroupActor, returnedSubGroupActor);
    }

    public void testGetActorByUnauthorizedUser() {
        try {
            executorService.getExecutor(h.getUnauthorizedUser(), actor.getId());
            fail();
        } catch (AuthorizationException e) {
            // Expected.
        }
        try {
            executorService.getExecutor(h.getUnauthorizedUser(), h.getSubGroupActor().getId());
            fail();
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testGetUnexistedActorByAuthorizedUser() {
        try {
            executorService.getExecutor(h.getAuthorizedUser(), -1L);
            fail();
        } catch (ExecutorDoesNotExistException e) {
            // Expected.
        }
    }

    public void testGetActorInsteadOfGroup() {
        try {
            Actor actor = executorService.<Actor>getExecutor(h.getAuthorizedUser(), group.getId());
            fail("businessDelegete allow to getActor() where the group really is returned.");
        } catch (ClassCastException e) {
            // Expected.
        }
    }
}
