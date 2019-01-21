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

import java.util.Collection;

/*
 */
public class ExecutorServiceDelegateAddExecutorsToGroupTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateAddExecutorsToGroupTest.class.getName();

    private Actor actor;

    private Group additionalGroup;

    private Actor additionalActor;

    private final Collection<Permission> updatePermissions = Lists.newArrayList(Permission.UPDATE);

    private final Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);

    protected void setUp() throws Exception {
        executorService = Delegates.getExecutorService();
        th = new ServiceTestHelper(testPrefix);
        th.createDefaultExecutorsMap();

        actor = th.getBaseGroupActor();
        th.setPermissionsToAuthorizedPerformer(readPermissions, actor);

        additionalGroup = th.createGroupIfNotExist("additionalG", "Additional Group");
        additionalActor = th.createActorIfNotExist("additionalA", "Additional Actor");
        th.setPermissionsToAuthorizedPerformer(readPermissions, additionalActor);
        th.setPermissionsToAuthorizedPerformer(readPermissions, additionalGroup);

        super.setUp();
    }

    public void testAddExecutorByAuthorizedPerformer() throws Exception {
        assertFalse("Executor not added to group ", th.isExecutorInGroup(additionalActor, additionalGroup));
        try {
            executorService.addExecutorsToGroup(th.getAuthorizedPerformerUser(), Lists.newArrayList(additionalActor.getId()),
                    additionalGroup.getId());
            fail("Executor added to group without corresponding permissions");
        } catch (AuthorizationException e) {
            // this is supposed result
        }

        th.setPermissionsToAuthorizedPerformer(updatePermissions, additionalGroup);

        executorService.addExecutorsToGroup(th.getAuthorizedPerformerUser(), Lists.newArrayList(additionalActor.getId()), additionalGroup.getId());

        additionalActor = executorService.getExecutor(th.getAuthorizedPerformerUser(), additionalActor.getId());
        additionalGroup = executorService.getExecutor(th.getAuthorizedPerformerUser(), additionalGroup.getId());

        assertTrue("Executor not added to group ", th.isExecutorInGroup(additionalActor, additionalGroup));
    }

    public void testAddExecutorByUnAuthorizedPerformer() throws Exception {
        try {
            executorService.addExecutorsToGroup(th.getUnauthorizedPerformerUser(), Lists.newArrayList(actor.getId()), additionalGroup.getId());
            assertTrue("Executor not added to group ", th.isExecutorInGroup(additionalActor, additionalGroup));
        } catch (AuthorizationException e) {
            // this is supposed result
        }
    }

    public void testAddFakeExecutor() throws Exception {
        th.setPermissionsToAuthorizedPerformer(updatePermissions, additionalGroup);
        try {
            executorService.addExecutorsToGroup(th.getAuthorizedPerformerUser(), Lists.newArrayList(th.getFakeActor().getId()),
                    additionalGroup.getId());
            fail("Executor added to group ");
        } catch (AuthorizationException e) {
            // TODO
        } catch (ExecutorDoesNotExistException e) {
            // this is supposed result
            fail("TODO trap");
        }
    }

    public void testAddNullExecutor() throws Exception {
        th.setPermissionsToAuthorizedPerformer(updatePermissions, additionalGroup);
        try {
            executorService.addExecutorsToGroup(th.getAuthorizedPerformerUser(), Lists.newArrayList((Long) null), additionalGroup.getId());
            fail("Executor added to group ");
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }

        try {
            executorService.addExecutorsToGroup(th.getAuthorizedPerformerUser(), null, additionalGroup.getId());
            fail("Executor added to group ");
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }
    }

    public void testAddExecutorWithNullUser() throws Exception {
        th.setPermissionsToAuthorizedPerformer(updatePermissions, additionalGroup);
        try {
            executorService.addExecutorsToGroup(null, Lists.newArrayList(actor.getId()), additionalGroup.getId());
            fail("Executor added to group ");
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        executorService = null;
        actor = null;
        additionalActor = null;
        additionalGroup = null;
        super.tearDown();
    }

}
