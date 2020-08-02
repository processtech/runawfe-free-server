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
import ru.runa.junit.ArrayAssert;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.delegate.Delegates;

public class AuthorizationServiceDelegateIsAllowedReturnsArrayTest extends ServletTestCase {
    private ServiceTestHelper h;
    private AuthorizationService authorizationService;

    @Override
    protected void setUp() {
        h = new ServiceTestHelper(AuthorizationServiceDelegateIsAllowedTest.class.getName());
        authorizationService = Delegates.getAuthorizationService();

        h.createDefaultExecutorsMap();

        val ppSystem = Lists.newArrayList(Permission.LOGIN);
        h.setPermissionsToAuthorizedActor(ppSystem, SecuredSingleton.SYSTEM);

        val ppExecutors = Lists.newArrayList(Permission.READ, Permission.VIEW_TASKS);
        h.setPermissionsToAuthorizedActor(ppExecutors, h.getBaseGroupActor());
        h.setPermissionsToAuthorizedActor(ppExecutors, h.getSubGroupActor());
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        h = null;
        authorizationService = null;
    }

    public void testIsAllowedFakeUser() {
        try {
            authorizationService.isAllowed(h.getFakeUser(), Permission.READ, Lists.newArrayList(SecuredSingleton.SYSTEM));
            fail("AuthorizationDelegate.isAllowed() allows fake subject");
        } catch (AuthenticationException e) {
            // Expected.
        }
    }

    public void testIsAllowedFakeObject() {
        try {
            authorizationService.isAllowed(h.getAuthorizedUser(), Permission.READ, Lists.newArrayList(h.getFakeActor()));
            // TODO
            // fail("AuthorizationDelegate.isAllowed() allows fake SecuredObject");
        } catch (InternalApplicationException e) {
            fail("TODO trap");
        }

        try {
            authorizationService.isAllowed(h.getAuthorizedUser(), Permission.READ,
                    Lists.newArrayList(h.getBaseGroupActor(), h.getFakeActor()));
            // TODO
            // fail("AuthorizationDelegate.isAllowed() allows fake SecuredObject");
        } catch (InternalApplicationException e) {
            fail("TODO trap");
        }
    }

    public void testIsAllowedSystem() {
        val ooSystem = Lists.newArrayList(SecuredSingleton.SYSTEM);

        boolean[] isAllowed = authorizationService.isAllowed(h.getAuthorizedUser(), Permission.LOGIN, ooSystem);
        boolean[] expected = { true };
        ArrayAssert.assertEqualArrays("AuthorizationDelegate.isAllowed() returns wrong info", expected, isAllowed);

        isAllowed = authorizationService.isAllowed(h.getAuthorizedUser(), Permission.READ, ooSystem);
        expected = new boolean[] { false };
        ArrayAssert.assertEqualArrays("AuthorizationDelegate.isAllowed() returns wrong info", expected, isAllowed);
    }

    public void testIsAllowedExecutor() {
        val ooActors = Lists.newArrayList(h.getBaseGroupActor(), h.getSubGroupActor());

        boolean[] isAllowed = authorizationService.isAllowed(h.getAuthorizedUser(), Permission.READ, ooActors);
        boolean[] expected = { true, true };
        ArrayAssert.assertEqualArrays("AuthorizationDelegate.isAllowed() returns wrong info", expected, isAllowed);

        isAllowed = authorizationService.isAllowed(h.getAuthorizedUser(), Permission.UPDATE, ooActors);
        expected = new boolean[] { false, false };
        ArrayAssert.assertEqualArrays("AuthorizationDelegate.isAllowed() returns wrong info", expected, isAllowed);

        isAllowed = authorizationService.isAllowed(h.getAuthorizedUser(), Permission.VIEW_TASKS, ooActors);
        expected = new boolean[] { true, true };
        ArrayAssert.assertEqualArrays("AuthorizationDelegate.isAllowed() returns wrong info", expected, isAllowed);
    }

    public void testIsAllowedExecutorDifferentObjects() {
        try {
            authorizationService.isAllowed(h.getUnauthorizedUser(), Permission.READ,
                    Lists.newArrayList(SecuredSingleton.SYSTEM, h.getBaseGroupActor(), h.getBaseGroup()));
            fail("No Exception: Secured objects should be of the same secured object type (SYSTEM)");
        } catch (InternalApplicationException e) {
            assertEquals("Secured objects should be of the same secured object type (SYSTEM)", e.getMessage());
        }
    }
}
