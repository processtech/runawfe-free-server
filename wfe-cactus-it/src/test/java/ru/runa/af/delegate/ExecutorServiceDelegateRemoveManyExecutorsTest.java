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
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;

import com.google.common.collect.Lists;

public class ExecutorServiceDelegateRemoveManyExecutorsTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateRemoveManyExecutorsTest.class.getName();

    private List<Executor> additionalActorsGroupsMixed;

    private final List<Permission> updatePermissions = Lists.newArrayList(Permission.UPDATE);

    @Override
    protected void setUp() throws Exception {
        executorService = Delegates.getExecutorService();
        th = new ServiceTestHelper(testPrefix);
        additionalActorsGroupsMixed = th.createMixedActorsGroupsArray("additionalMixed", "Additional Mixed");
        th.setPermissionsToAuthorizedPerformerOnExecutorsList(updatePermissions, additionalActorsGroupsMixed);
    }

    public void testRemoveExecutorsByAuthorizedPerformer() throws Exception {
        executorService.remove(th.getAuthorizedPerformerUser(), th.toIds(additionalActorsGroupsMixed));
        for (Executor executor : additionalActorsGroupsMixed) {
            assertFalse("Executor was not deleted.", th.isExecutorExist(executor));
            // th.removeCreatedExecutor(executor);
        }
    }

    public void testRemoveExecutorsByUnauthorizedPerformer() throws Exception {
        try {
            executorService.remove(th.getUnauthorizedPerformerUser(), th.toIds(additionalActorsGroupsMixed));
            fail("Executors were deleted by unauthorize performer.");
        } catch (AuthorizationException e) {
            // that's what we expect to see
        }
        for (Executor executor : additionalActorsGroupsMixed) {
            assertTrue("Executor was deleted.", th.isExecutorExist(executor));
        }
    }

    public void testRemoveNullExecutors() throws Exception {
        List<Long> ids = Lists.newArrayList((Long) null, null, null);
        try {
            executorService.remove(th.getAuthorizedPerformerUser(), ids);
            fail("IllegalArgumentException was not thrown on RemoveNullExecutors.");
        } catch (IllegalArgumentException e) {
            // that's what we expect to see
        }
    }

    public void testRemoveFakeExecutors() throws Exception {
        try {
            for (Executor executor : th.getFakeExecutors()) {
                executorService.remove(th.getAuthorizedPerformerUser(), Lists.newArrayList(executor.getId()));
            }
            fail("ExecutorOutOfDateException was not thrown on remove method call with fakeExecutors argument");
        } catch (IllegalArgumentException e) {
            // TODO
        } catch (ExecutorDoesNotExistException e) {
            // that's what we expect to see
            fail("TODO trap");
        }
    }

    @Override
    protected void tearDown() throws Exception {
        th.releaseResources();
        executorService = null;
        additionalActorsGroupsMixed = null;
        super.tearDown();
    }
}
