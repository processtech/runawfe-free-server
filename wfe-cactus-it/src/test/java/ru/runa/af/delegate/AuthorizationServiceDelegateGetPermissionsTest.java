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

import org.apache.cactus.ServletTestCase;

import ru.runa.af.service.ServiceTestHelper;
import ru.runa.junit.ArrayAssert;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.collect.Lists;

/**
 * Created on 20.08.2004
 * 
 */
public class AuthorizationServiceDelegateGetPermissionsTest extends ServletTestCase {
    private ServiceTestHelper helper;

    private AuthorizationService authorizationService;

    @Override
    protected void setUp() throws Exception {
        helper = new ServiceTestHelper(AuthorizationServiceDelegateGetPermissionsTest.class.getName());
        helper.createDefaultExecutorsMap();

        Collection<Permission> executorP = Lists.newArrayList(Permission.UPDATE);
        helper.setPermissionsToAuthorizedPerformer(executorP, helper.getBaseGroupActor());
        helper.setPermissionsToAuthorizedPerformer(executorP, helper.getBaseGroup());
        helper.setPermissionsToAuthorizedPerformer(executorP, helper.getSubGroupActor());
        helper.setPermissionsToAuthorizedPerformerOnExecutors(Lists.newArrayList(Permission.LIST));
        authorizationService = Delegates.getAuthorizationService();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.releaseResources();
        authorizationService = null;
        super.tearDown();
    }

    public void testGetPermissionsFakeSubject() throws Exception {
        try {
            authorizationService.getIssuedPermissions(helper.getFakeUser(), helper.getBaseGroupActor(), helper.getBaseGroupActor());
            fail("AuthorizationDelegate.getIssuedPermissions() allows fake subject");
        } catch (AuthenticationException e) {
        }
    }

    public void testGetPermissions() throws Exception {
        Collection<Permission> noPermission = Lists.newArrayList();
        Collection<Permission> expected = Lists.newArrayList(Permission.READ);

        Collection<Permission> actual = authorizationService.getIssuedPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroupActor(),
                SecuredSingleton.EXECUTORS);
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.getIssuedPermissions() returns wrong permissions", noPermission, actual);

        authorizationService.setPermissions(helper.getAdminUser(), helper.getBaseGroupActor().getId(), expected, SecuredSingleton.EXECUTORS);
        actual = authorizationService.getIssuedPermissions(helper.getAdminUser(), helper.getBaseGroupActor(), SecuredSingleton.EXECUTORS);
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.getIssuedPermissions() returns wrong permissions", expected, actual);

        actual = authorizationService.getIssuedPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroup(), helper.getBaseGroupActor());
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.getIssuedPermissions() returns wrong permissions", noPermission, actual);

        authorizationService.setPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroup().getId(), expected, helper.getBaseGroupActor());
        actual = authorizationService.getIssuedPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroup(), helper.getBaseGroupActor());
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.getIssuedPermissions() returns wrong permissions", expected, actual);
    }

    public void testGetPermissionsUnauthorized() throws Exception {
        try {
            authorizationService.getIssuedPermissions(helper.getUnauthorizedPerformerUser(), helper.getBaseGroupActor(), SecuredSingleton.EXECUTORS);
            fail("AuthorizationDelegate.getIssuedPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
        }

        try {
            authorizationService.getIssuedPermissions(helper.getUnauthorizedPerformerUser(), helper.getBaseGroupActor(), helper.getBaseGroupActor());
            fail("AuthorizationDelegate.getIssuedPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
        }
    }
}
