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
import java.util.LinkedList;
import lombok.val;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.junit.ArrayAssert;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

public class ExecutorServiceDelegateGetAllTest extends ServletTestCase {
    private ServiceTestHelper h;
    private ExecutorService executorService;

    @Override
    protected void setUp() {
        executorService = Delegates.getExecutorService();
        h = new ServiceTestHelper(getClass().getName());

        h.createDefaultExecutorsMap();

        val ee = h.getDefaultExecutorsMap();
        h.setPermissionsToAuthorizedActor(Lists.newArrayList(Permission.READ), Lists.newArrayList(
                ee.get(ServiceTestHelper.BASE_GROUP_ACTOR_NAME),
                ee.get(ServiceTestHelper.BASE_GROUP_NAME),
                ee.get(ServiceTestHelper.SUB_GROUP_ACTOR_NAME),
                ee.get(ServiceTestHelper.SUB_GROUP_NAME)
        ));
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        executorService = null;
    }

    final public void testGetExecutorsByAuthorizedUser() {
        val actual = executorService.getExecutors(h.getAuthorizedUser(), h.getExecutorBatchPresentation());  // includes AuthorizedActor
        val expected = new LinkedList<Executor>(h.getDefaultExecutorsMap().values());
        // ExecutorLogic.create(User, Executor) adds READ permission on executor to itself:
        expected.add(h.getAuthorizedActor());
        ArrayAssert.assertWeakEqualArrays("businessDelegate.getExecutors() returns wrong executor set", expected, actual);
    }

    public void testGetExecutorsByUnauthorizedUser() {
        val actual = executorService.getExecutors(h.getUnauthorizedUser(), h.getExecutorBatchPresentation());
        val expected = Lists.newArrayList(h.getUnauthorizedActor());
        ArrayAssert.assertWeakEqualArrays("businessDelegate.getExecutors() must return only unauthorizedActor itself", expected, actual);
    }

    public void testGetExecutorsWithFakeUser() {
        try {
            User fakeUser = h.getFakeUser();
            executorService.getExecutors(fakeUser, h.getExecutorBatchPresentation());
            fail("businessDelegate.getExecutors() with fake subject throws no AuthenticationException");
        } catch (AuthenticationException e) {
            // Expected.
        }
    }
}
