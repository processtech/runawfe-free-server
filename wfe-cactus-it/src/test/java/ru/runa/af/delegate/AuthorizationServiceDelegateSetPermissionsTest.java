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
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.GroupPermission;

import com.google.common.collect.Lists;

/**
 * Created on 20.08.2004
 */
public class AuthorizationServiceDelegateSetPermissionsTest extends ServletTestCase {
    private ServiceTestHelper helper;

    private AuthorizationService authorizationService;

    private Collection<Permission> p = Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS);

    @Override
    protected void setUp() throws Exception {
        helper = new ServiceTestHelper(AuthorizationServiceDelegateSetPermissionsTest.class.getName());
        helper.createDefaultExecutorsMap();

        Collection<Permission> systemP = Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS);
        helper.setPermissionsToAuthorizedPerformerOnSystem(systemP);

        Collection<Permission> executorP = Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS);
        helper.setPermissionsToAuthorizedPerformer(executorP, helper.getBaseGroupActor());
        helper.setPermissionsToAuthorizedPerformer(executorP, helper.getBaseGroup());

        authorizationService = Delegates.getAuthorizationService();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.releaseResources();
        authorizationService = null;
        super.tearDown();
    }

    public void testSetPermissionsNullUser() throws Exception {
        try {
            authorizationService.setPermissions(null, helper.getBaseGroupActor().getId(), p, helper.getAASystem());
            fail("AuthorizationDelegate.setPermissions() allows null subject");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testSetPermissionsFakeSubject() throws Exception {
        try {
            authorizationService.setPermissions(helper.getFakeUser(), helper.getBaseGroupActor().getId(), p, helper.getAASystem());
            fail("AuthorizationDelegate.setPermissions() allows fake subject");
        } catch (AuthenticationException e) {
        }
    }

    public void testSetPermissionsFakeExecutor() throws Exception {
        try {
            authorizationService.setPermissions(helper.getAuthorizedPerformerUser(), helper.getFakeActor().getId(), p, helper.getAASystem());
            fail("AuthorizationDelegate.setPermissions() allows null executor");
        } catch (AuthorizationException e) {
        } catch (ExecutorDoesNotExistException e) {
            fail("TODO trap");
        }
    }

    public void testSetPermissionsNullPermissions() throws Exception {
        try {
            authorizationService.setPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroupActor().getId(), null, helper.getAASystem());
            fail("AuthorizationDelegate.setPermissions() allows null permissions");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testSetPermissionsNullIdentifiable() throws Exception {
        try {
            authorizationService.setPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroupActor().getId(), p, null);
            fail("AuthorizationDelegate.setPermissions() allows null identifiable");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testSetPermissionsFakeIdentifiable() throws Exception {
        try {
            authorizationService.setPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroupActor().getId(), p, helper.getFakeActor());
            fail("AuthorizationDelegate.setPermissions() allows null identifiable");
        } catch (InternalApplicationException e) {
        }
    }

    public void testSetPermissions() throws Exception {
        authorizationService.setPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroupActor().getId(), p, helper.getBaseGroup());
        Collection<Permission> actual = authorizationService.getIssuedPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroupActor(),
                helper.getBaseGroup());
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions", p, actual);

        authorizationService.setPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroup().getId(), p, helper.getBaseGroupActor());
        actual = authorizationService.getIssuedPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroup(), helper.getBaseGroupActor());
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions", p, actual);

        authorizationService.setPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroupActor().getId(), p, helper.getAASystem());
        actual = authorizationService.getIssuedPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroupActor(), helper.getAASystem());
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions", p, actual);
    }

    public void testSetNoPermission() throws Exception {
        p = GroupPermission.getNoPermissions();
        authorizationService.setPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroupActor().getId(), p, helper.getBaseGroup());
        Collection<Permission> actual = authorizationService.getIssuedPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroupActor(),
                helper.getBaseGroup());
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions", p, actual);
    }

    public void testSetPermissionsUnauthorized() throws Exception {
        try {
            authorizationService.setPermissions(helper.getUnauthorizedPerformerUser(), helper.getBaseGroupActor().getId(), p, helper.getBaseGroup());
            fail("AuthorizationDelegate.setPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
        }

        try {
            authorizationService.setPermissions(helper.getUnauthorizedPerformerUser(), helper.getBaseGroup().getId(), p, helper.getBaseGroupActor());
            fail("AuthorizationDelegate.setPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
        }

        try {
            authorizationService.setPermissions(helper.getUnauthorizedPerformerUser(), helper.getBaseGroupActor().getId(), p, helper.getAASystem());
            fail("AuthorizationDelegate.setPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
        }
    }

}
