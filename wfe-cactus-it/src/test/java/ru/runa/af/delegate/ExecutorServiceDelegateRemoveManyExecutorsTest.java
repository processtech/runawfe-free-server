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
import java.util.List;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;

public class ExecutorServiceDelegateRemoveManyExecutorsTest extends ServletTestCase {
    private ServiceTestHelper h;
    private ExecutorService executorService;

    private List<Executor> maxedActorsGroups;

    @Override
    protected void setUp() {
        h = new ServiceTestHelper(getClass().getName());
        executorService = Delegates.getExecutorService();
        
        maxedActorsGroups = h.createMixedActorsGroupsArray("additionalMixed", "Additional Mixed");
        h.setPermissionsToAuthorizedActor(Lists.newArrayList(Permission.UPDATE), maxedActorsGroups);
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        executorService = null;
        maxedActorsGroups = null;
    }

    public void testRemoveExecutorsByAuthorizedUser() {
        executorService.remove(h.getAuthorizedUser(), h.toIds(maxedActorsGroups));
        for (Executor executor : maxedActorsGroups) {
            assertFalse("Executor was not deleted.", h.isExecutorExist(executor));
        }
    }

    public void testRemoveExecutorsByUnauthorizedUser() {
        try {
            executorService.remove(h.getUnauthorizedUser(), h.toIds(maxedActorsGroups));
            fail("Executors were deleted by unauthorize performer.");
        } catch (AuthorizationException e) {
            // Expected.
        }
        for (Executor executor : maxedActorsGroups) {
            assertTrue("Executor was deleted.", h.isExecutorExist(executor));
        }
    }
}
