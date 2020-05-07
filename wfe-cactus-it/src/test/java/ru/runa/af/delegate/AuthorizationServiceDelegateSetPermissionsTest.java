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
import java.util.ArrayList;
import lombok.val;
import lombok.var;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.junit.ArrayAssert;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 20.08.2004.
 */
public class AuthorizationServiceDelegateSetPermissionsTest extends ServletTestCase {
    private ServiceTestHelper h;
    private AuthorizationService authorizationService;

    @Override
    protected void setUp() {
        h = new ServiceTestHelper(AuthorizationServiceDelegateSetPermissionsTest.class.getName());
        authorizationService = Delegates.getAuthorizationService();

        h.createDefaultExecutorsMap();

        // authorizationService.setPermissions() requires READ on subject (executors) and UPDATE_PERMISSIONS on object (other executors, system).
        // Since subject executors are also used as objects, we set both permissions for them.
        val ppSubject = Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS);
        val ppObject = Lists.newArrayList(Permission.UPDATE_PERMISSIONS);
        authorizationService.setPermissions(h.getAdminUser(), h.getAuthorizedActor().getId(), ppSubject, h.getBaseGroup());
        authorizationService.setPermissions(h.getAdminUser(), h.getAuthorizedActor().getId(), ppSubject, h.getBaseGroupActor());
        h.setPermissionsToAuthorizedActor(ppObject, SecuredSingleton.SYSTEM);
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        authorizationService = null;
    }

    public void testSetPermissionsFakeUser() {
        try {
            val expected = Lists.newArrayList(Permission.READ, Permission.LOGIN);
            authorizationService.setPermissions(h.getFakeUser(), h.getBaseGroupActor().getId(), expected, SecuredSingleton.SYSTEM);
            fail("AuthorizationDelegate.setPermissions() allows fake subject");
        } catch (AuthenticationException e) {
            // Expected.
        }
    }

    public void testSetPermissions() {
        {
            val expected = Lists.newArrayList(Permission.READ, Permission.UPDATE);

            authorizationService.setPermissions(h.getAuthorizedUser(), h.getBaseGroupActor().getId(), expected, h.getBaseGroup());
            var actual = authorizationService.getIssuedPermissions(h.getAdminUser(), h.getBaseGroupActor(), h.getBaseGroup());
            ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions", expected, actual);

            authorizationService.setPermissions(h.getAuthorizedUser(), h.getBaseGroup().getId(), expected, h.getBaseGroupActor());
            actual = authorizationService.getIssuedPermissions(h.getAdminUser(), h.getBaseGroup(), h.getBaseGroupActor());
            ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions", expected, actual);
        }
        {
            val expected = Lists.newArrayList(Permission.READ, Permission.VIEW_LOGS);

            authorizationService.setPermissions(h.getAuthorizedUser(), h.getBaseGroupActor().getId(), expected, SecuredSingleton.SYSTEM);
            val actual = authorizationService.getIssuedPermissions(h.getAdminUser(), h.getBaseGroupActor(), SecuredSingleton.SYSTEM);
            ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions", expected, actual);
        }
    }

    public void testSetNoPermission() {
        val expected = new ArrayList<Permission>();
        authorizationService.setPermissions(h.getAuthorizedUser(), h.getBaseGroupActor().getId(), expected, h.getBaseGroup());
        val actual = authorizationService.getIssuedPermissions(h.getAdminUser(), h.getBaseGroupActor(), h.getBaseGroup());
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions", expected, actual);
    }

    public void testSetPermissionsUnauthorized() {
        val expected = new ArrayList<Permission>();
        try {
            authorizationService.setPermissions(h.getUnauthorizedUser(), h.getBaseGroupActor().getId(), expected, h.getBaseGroup());
            fail("AuthorizationDelegate.setPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
            // Expected.
        }

        try {
            authorizationService.setPermissions(h.getUnauthorizedUser(), h.getBaseGroup().getId(), expected, h.getBaseGroupActor());
            fail("AuthorizationDelegate.setPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
            // Expected.
        }

        try {
            authorizationService.setPermissions(h.getUnauthorizedUser(), h.getBaseGroupActor().getId(), expected, SecuredSingleton.SYSTEM);
            fail("AuthorizationDelegate.setPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
            // Expected.
        }
    }
}
