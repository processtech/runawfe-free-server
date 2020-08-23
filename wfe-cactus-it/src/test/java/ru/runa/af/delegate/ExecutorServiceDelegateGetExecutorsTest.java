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
import ru.runa.junit.ArrayAssert;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.User;

/**
 * Created on 16.05.2005
 */
// TODO executionService.getExecutorsByIds
public class ExecutorServiceDelegateGetExecutorsTest extends ServletTestCase {
    private ServiceTestHelper h;

    private List<Executor> additionalActorGroupsMixed;
    private final List<Permission> readPermissions = Lists.newArrayList(Permission.READ);
    private List<Long> executorsIDs;

    @Override
    protected void setUp() {
        h = new ServiceTestHelper(getClass().getName());

        additionalActorGroupsMixed = h.createMixedActorsGroupsArray("additionalMixed", "Additional Mixed");
        h.setPermissionsToAuthorizedActor(readPermissions, additionalActorGroupsMixed);

        executorsIDs = Lists.newArrayList();
        for (Executor executor : additionalActorGroupsMixed) {
            executorsIDs.add(executor.getId());
        }
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        executorsIDs = null;
        additionalActorGroupsMixed = null;
    }

    public void testGetExecutorsByAuthorizedUser() {
        List<Executor> returnedExecutors = h.getExecutors(h.getAuthorizedUser(), executorsIDs);
        ArrayAssert.assertWeakEqualArrays("businessDelegate.getExecutors() returns wrong executor set", additionalActorGroupsMixed,
                returnedExecutors);
    }

    public void testGetExecutorsByUnauthorizedUser() {
        User unauthorizedUser = h.getUnauthorizedUser();
        try {
            h.getExecutors(unauthorizedUser, executorsIDs);
            fail("businessDelegate allow to getExecutor() to performer without Permission.READ.");
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testGetUnexistedExecutorByAuthorizedUser() {
        executorsIDs = Lists.newArrayList(-1L, -2L, -3L);
        try {
            h.getExecutors(h.getAuthorizedUser(), executorsIDs);
            fail("businessDelegate does not throw Exception to getExecutor() for UnexistedExecutor");
        } catch (ExecutorDoesNotExistException e) {
            // Expected.
        }
    }
}
