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

import java.util.Collection;
import java.util.List;

import org.apache.cactus.ServletTestCase;

import com.google.common.collect.Lists;

import ru.runa.af.service.ServiceTestHelper;
import ru.runa.junit.ArrayAssert;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.delegate.Delegates;

public class AuthorizationServiceDelegateIsAllowedReturnsArrayTest extends ServletTestCase {
    private ServiceTestHelper helper;

    private AuthorizationService authorizationService;

    @Override
    protected void setUp() throws Exception {
        helper = new ServiceTestHelper(AuthorizationServiceDelegateIsAllowedTest.class.getName());
        helper.createDefaultExecutorsMap();

        Collection<Permission> executorsP = Lists.newArrayList(Permission.CREATE);
        helper.setPermissionsToAuthorizedPerformerOnExecutors(executorsP);

        Collection<Permission> executorP = Lists.newArrayList(Permission.READ, Permission.UPDATE_STATUS);
        helper.setPermissionsToAuthorizedPerformer(executorP, helper.getBaseGroupActor());
        helper.setPermissionsToAuthorizedPerformer(executorP, helper.getSubGroupActor());

        authorizationService = Delegates.getAuthorizationService();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.releaseResources();
        helper = null;
        authorizationService = null;
        super.tearDown();
    }

    public void testIsAllowedNullUser() throws Exception {
        try {
            authorizationService.isAllowed(null, Permission.READ, Lists.newArrayList(SecuredSingleton.EXECUTORS));
            fail("AuthorizationDelegate.isAllowed() allows null subject");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIsAllowedFakeSubject() throws Exception {
        try {
            authorizationService.isAllowed(helper.getFakeUser(), Permission.READ, Lists.newArrayList(SecuredSingleton.EXECUTORS));
            fail("AuthorizationDelegate.isAllowed() allows fake subject");
        } catch (AuthenticationException e) {
        }
    }

    public void testIsAllowedPermissionSubject() throws Exception {
        try {
            authorizationService.isAllowed(helper.getAuthorizedPerformerUser(), null, Lists.newArrayList(SecuredSingleton.EXECUTORS));
            fail("AuthorizationDelegate.isAllowed() allows null permission");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIsAllowedNullSecuredObject() throws Exception {
        try {
            authorizationService.isAllowed(helper.getAuthorizedPerformerUser(), Permission.READ, (List<SecuredObject>) null);
            fail("AuthorizationDelegate.isAllowed() allows null SecuredObject");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIsAllowedNullSecuredObjects() throws Exception {
        try {
            authorizationService.isAllowed(helper.getAuthorizedPerformerUser(), Permission.READ, Lists.newArrayList((SecuredObject) null, null));
            fail("AuthorizationDelegate.isAllowed() allows null SecuredObject");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIsAllowedFakeSecuredObject() throws Exception {
        try {
            authorizationService.isAllowed(helper.getAuthorizedPerformerUser(), Permission.READ, Lists.newArrayList(helper.getFakeActor()));
            // TODO
            // fail("AuthorizationDelegate.isAllowed() allows fake SecuredObject");
        } catch (InternalApplicationException e) {
            fail("TODO trap");
        }

        try {
            authorizationService.isAllowed(helper.getAuthorizedPerformerUser(), Permission.READ,
                    Lists.newArrayList(helper.getBaseGroupActor(), helper.getFakeActor()));
            // TODO
            // fail("AuthorizationDelegate.isAllowed() allows fake SecuredObject");
        } catch (InternalApplicationException e) {
            fail("TODO trap");
        }
    }

    public void testIsAllowedAASystem() throws Exception {
        boolean[] isAllowed = authorizationService.isAllowed(helper.getAuthorizedPerformerUser(), Permission.CREATE,
                Lists.newArrayList(SecuredSingleton.EXECUTORS));
        boolean[] expected = { true };
        ArrayAssert.assertEqualArrays("AuthorizationDelegate.isAllowed() returns wrong info", expected, isAllowed);

        isAllowed = authorizationService.isAllowed(helper.getAuthorizedPerformerUser(), Permission.READ,
                Lists.newArrayList(SecuredSingleton.EXECUTORS));
        expected = new boolean[] { false };
        ArrayAssert.assertEqualArrays("AuthorizationDelegate.isAllowed() returns wrong info", expected, isAllowed);
    }

    public void testIsAllowedExecutor() throws Exception {
        boolean[] isAllowed = authorizationService.isAllowed(helper.getAuthorizedPerformerUser(), Permission.READ,
                Lists.newArrayList(helper.getBaseGroupActor(), helper.getSubGroupActor()));
        boolean[] expected = { true, true };
        ArrayAssert.assertEqualArrays("AuthorizationDelegate.isAllowed() returns wrong info", expected, isAllowed);

        isAllowed = authorizationService.isAllowed(helper.getAuthorizedPerformerUser(), Permission.UPDATE,
                Lists.newArrayList(helper.getBaseGroupActor(), helper.getSubGroupActor()));
        expected = new boolean[] { false, false };
        ArrayAssert.assertEqualArrays("AuthorizationDelegate.isAllowed() returns wrong info", expected, isAllowed);

        isAllowed = authorizationService.isAllowed(helper.getAuthorizedPerformerUser(), Permission.UPDATE_STATUS,
                Lists.newArrayList(helper.getBaseGroupActor(), helper.getSubGroupActor()));
        expected = new boolean[] { true, true };
        ArrayAssert.assertEqualArrays("AuthorizationDelegate.isAllowed() returns wrong info", expected, isAllowed);
    }

    public void testIsAllowedExecutorDifferentObjects() throws Exception {
        try {
            authorizationService.isAllowed(helper.getUnauthorizedPerformerUser(), Permission.READ,
                    Lists.newArrayList(SecuredSingleton.EXECUTORS, helper.getBaseGroupActor(), helper.getBaseGroup()));
            fail("no error");
        } catch (InternalApplicationException e) {
            assertEquals("SecuredObjects should be of the same secured object type (SYSTEM)", e.getMessage());
        }
    }

}
