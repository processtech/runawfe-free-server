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
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 20.08.2004.
 */
public class AuthorizationServiceDelegateGetExecutorsWithPermissionTest extends ServletTestCase {
    private ServiceTestHelper h;
    private AuthorizationService authorizationService;
    private BatchPresentation batchPresentation;

    @Override
    protected void setUp() {
        h = new ServiceTestHelper(AuthorizationServiceDelegateGetExecutorsWithPermissionTest.class.getName());
        authorizationService = Delegates.getAuthorizationService();
        batchPresentation = h.getExecutorBatchPresentation();

        h.createDefaultExecutorsMap();

        val pp = Lists.newArrayList(Permission.READ);
        h.setPermissionsToAuthorizedActor(pp, h.getBaseGroupActor());
        h.setPermissionsToAuthorizedActor(pp, h.getBaseGroup());
        h.setPermissionsToAuthorizedActor(pp, h.getAuthorizedActor());
        authorizationService.setPermissions(h.getAdminUser(), h.getBaseGroupActor().getId(), pp, h.getBaseGroupActor());
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        authorizationService = null;
    }

    public void testGetExecutorsWithPermissionFakeUser() {
        try {
            authorizationService.getExecutorsWithPermission(h.getFakeUser(), h.getBaseGroupActor(), batchPresentation, true);
            fail("AuthorizationDelegate.getExecutorsWithPermission() allows fake subject");
        } catch (AuthenticationException e) {
            // Expected. Although we also can get here because user is not fake but is not allowed to read object's permissions.
        }
    }

    public void testGetExecutorsWithPermissionFakeObject() {
        try {
            authorizationService.getExecutorsWithPermission(h.getAuthorizedUser(), h.getFakeActor(), batchPresentation, true);
            fail("AuthorizationDelegate.getExecutorsWithPermission() allows fake SecuredObject");
        } catch (InternalApplicationException e) {
            // Expected.
        }
    }

    public void testGetExecutorsWithPermission() {
        val expected = Lists.newArrayList(h.getAuthorizedActor(), h.getBaseGroupActor());
        val actual = authorizationService.getExecutorsWithPermission(h.getAuthorizedUser(), h.getBaseGroupActor(), batchPresentation, true);
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.getExecutorsWithPermission() returns wrong executors", expected, actual);
    }

    public void testGetExecutorsWithPermissionUnauthorized() {
        try {
            authorizationService.getExecutorsWithPermission(h.getUnauthorizedUser(), h.getBaseGroupActor(), batchPresentation, true);
            fail("AuthorizationDelegate.getExecutorsWithPermission() allows unauthorized operation");
        } catch (AuthorizationException e) {
            // Expected.
        }
    }
}
