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
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ExecutorAlreadyExistsException;
import ru.runa.wfe.user.Group;

public class ExecutorServiceDelegateCreateGroupTest extends ServletTestCase {
    private final String PREFIX = getClass().getName();
    private ServiceTestHelper h;
    private ExecutorService executorService;

    private Group group;

    @Override
    protected void setUp() {
        h = new ServiceTestHelper(PREFIX);
        executorService = Delegates.getExecutorService();

        h.setPermissionsToAuthorizedActor(Lists.newArrayList(Permission.CREATE_EXECUTOR), SecuredSingleton.SYSTEM);
    }

    @Override
    protected void tearDown() {
        h.removeExecutorIfExists(group);
        h.releaseResources();
        executorService = null;
        group = null;
    }

    public void testCreateGroupByAuthorizedUser() {
        group = new Group(PREFIX + "_Group", "description");
        group = executorService.create(h.getAuthorizedUser(), group);
        assertTrue("Executor (group) does not exists ", h.isExecutorExist(group));
        Group returnedGroup = executorService.getExecutor(h.getAuthorizedUser(), group.getId());
        assertEquals("Returned group differes with created one", group, returnedGroup);
    }

    public void testCreateExecutorByUnAuthorizedUser() {
        group = new Group(PREFIX + "_Group", "description");
        try {
            group = executorService.create(h.getUnauthorizedUser(), group);
            fail("ExecutorServiceDelegate.create(group) creates executor without permissions");
        } catch (AuthorizationException e) {
            // Expected.
        }
        assertFalse("Executor exists ", h.isExecutorExist(group));
    }

    public void testCreateAlreadyExistedGroup() {
        Group group2 = new Group(PREFIX + "_Group", "description");
        group = executorService.create(h.getAuthorizedUser(), group2);
        assertTrue("Executor does not exists ", h.isExecutorExist(group));
        try {
            executorService.create(h.getAuthorizedUser(), group2);
            fail("ExecutorServiceDelegate.create(group) creates already existed group");
        } catch (ExecutorAlreadyExistsException e) {
            // Expected.
        }
        Group returnedGroup = executorService.getExecutor(h.getAuthorizedUser(), group.getId());
        assertEquals("Returned actor differes with created one", group, returnedGroup);
    }

    public void testCreateAlreadyExistedActor() {
        Actor actor = h.createActorIfNotExist(PREFIX + "_Group", "description");
        group = new Group(actor.getName(), actor.getDescription());
        try {
            executorService.create(h.getAuthorizedUser(), group);
            fail("ExecutorServiceDelegate.create(group) creates already existed actor");
        } catch (ExecutorAlreadyExistsException e) {
            // Expected.
        }
    }

    public void testCreateExecutorWithFakeUser() {
        group = new Group(PREFIX + "_Group", "description");
        try {
            group = executorService.create(h.getFakeUser(), group);
            fail("executor with fake subject created");
        } catch (AuthenticationException e) {
            // Expected.
        }
        assertFalse("Executor does not exists ", h.isExecutorExist(group));
    }
}
