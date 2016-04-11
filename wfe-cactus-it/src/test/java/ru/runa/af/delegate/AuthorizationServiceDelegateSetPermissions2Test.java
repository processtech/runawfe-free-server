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

import ru.runa.af.service.ServiceTestHelper;
import ru.runa.junit.ArrayAssert;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;

import com.google.common.collect.Lists;

/**
 */
public class AuthorizationServiceDelegateSetPermissions2Test extends ServletTestCase {
    private ServiceTestHelper helper;

    private AuthorizationService authorizationService;

    private ExecutorService executorService;

    private List<Collection<Permission>> legalPermissions = null;

    private List<Long> legalActorIds = null;

    @Override
    protected void setUp() throws Exception {
        helper = new ServiceTestHelper(AuthorizationServiceDelegateSetPermissionsTest.class.getName());
        helper.createDefaultExecutorsMap();

        Collection<Permission> systemPermissions = Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS);
        helper.setPermissionsToAuthorizedPerformerOnSystem(systemPermissions);

        Collection<Permission> executorPermissions = Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS);
        helper.setPermissionsToAuthorizedPerformer(executorPermissions, helper.getBaseGroupActor());
        helper.setPermissionsToAuthorizedPerformer(executorPermissions, helper.getSubGroupActor());
        helper.setPermissionsToAuthorizedPerformer(executorPermissions, helper.getBaseGroup());

        authorizationService = Delegates.getAuthorizationService();
        executorService = Delegates.getExecutorService();

        legalActorIds = Lists.newArrayList(helper.getSubGroupActor().getId(), helper.getBaseGroupActor().getId());
        legalPermissions = Lists.newArrayList();
        legalPermissions.add(Lists.newArrayList(Permission.READ));
        legalPermissions.add(Lists.newArrayList(Permission.READ));
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.releaseResources();
        authorizationService = null;
        executorService = null;
        super.tearDown();
    }

    public void testSetPermissionsNullUser() throws Exception {
        try {
            authorizationService.setPermissions(null, legalActorIds, legalPermissions, helper.getAASystem());
            fail("AuthorizationDelegate.setPermission allows Null subject");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testSetPermissionsFakeSubject() throws Exception {
        try {
            authorizationService.setPermissions(helper.getFakeUser(), legalActorIds, legalPermissions, helper.getAASystem());
            fail("AuthorizationDelegate.setPermission allows Fake subject");
        } catch (AuthenticationException e) {
        }
    }

    public void testSetPermissionsFakeExecutor() throws Exception {
        try {
            List<Long> fakedActorIds = Lists.newArrayList(0L, 0L, 0L);
            List<Collection<Permission>> permissions = Lists.newArrayList();
            for (int i = 0; i < fakedActorIds.size(); i++) {
                permissions.add(Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS));
            }
            authorizationService.setPermissions(helper.getAuthorizedPerformerUser(), fakedActorIds, permissions, helper.getAASystem());
            fail("AuthorizationDelegate.setPermission allows Fake executor");
        } catch (ExecutorDoesNotExistException e) {
        }
    }

    public void testSetPermissionsNullExecutor() throws Exception {
        try {
            authorizationService.setPermissions(helper.getAuthorizedPerformerUser(), null, legalPermissions, helper.getAASystem());
            fail("AuthorizationDelegate.setPermission allows Fake executor");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testSetPermissionsNullPermissions() throws Exception {
        try {
            authorizationService.setPermissions(helper.getAuthorizedPerformerUser(), legalActorIds, (List<Collection<Permission>>) null,
                    helper.getAASystem());
            fail("AuthorizationDelegate.setPermission allows Null permissions");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testSetPermissionsNullIdentifiable() throws Exception {
        try {
            authorizationService.setPermissions(helper.getAuthorizedPerformerUser(), legalActorIds, legalPermissions, null);
            fail("AuthorizationDelegate.setPermission allows Null identifiable");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testSetPermissions() throws Exception {
        authorizationService.setPermissions(helper.getAuthorizedPerformerUser(), legalActorIds, legalPermissions, helper.getAASystem());
        for (int i = 0; i < legalActorIds.size(); i++) {
            Executor executor = executorService.getExecutor(helper.getAuthorizedPerformerUser(), legalActorIds.get(i));
            Collection<Permission> actual = authorizationService.getIssuedPermissions(helper.getAuthorizedPerformerUser(), executor,
                    helper.getAASystem());
            ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions on system",
                    legalPermissions.get(i), actual);
        }

        legalPermissions = Lists.newArrayList();
        legalPermissions.add(Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS));
        legalPermissions.add(Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS));
        authorizationService.setPermissions(helper.getAuthorizedPerformerUser(), legalActorIds, legalPermissions, helper.getBaseGroupActor());
        for (int i = 0; i < legalActorIds.size(); i++) {
            Executor executor = executorService.getExecutor(helper.getAuthorizedPerformerUser(), legalActorIds.get(i));
            Collection<Permission> actual = authorizationService.getIssuedPermissions(helper.getAuthorizedPerformerUser(), executor,
                    helper.getBaseGroupActor());
            ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions on base group actor",
                    legalPermissions.get(i), actual);
        }

        legalPermissions = Lists.newArrayList();
        legalPermissions.add(Lists.newArrayList(Permission.READ));
        legalPermissions.add(Lists.newArrayList(Permission.READ));
        authorizationService.setPermissions(helper.getAuthorizedPerformerUser(), legalActorIds, legalPermissions, helper.getBaseGroup());
        for (int i = 0; i < legalActorIds.size(); i++) {
            Executor executor = executorService.getExecutor(helper.getAuthorizedPerformerUser(), legalActorIds.get(i));
            Collection<Permission> actual = authorizationService.getIssuedPermissions(helper.getAuthorizedPerformerUser(), executor,
                    helper.getBaseGroup());
            ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions on base group actor",
                    legalPermissions.get(i), actual);
        }
    }

    public void testSetPermissionsUnauthorized() throws Exception {
        try {
            authorizationService.setPermissions(helper.getUnauthorizedPerformerUser(), legalActorIds, legalPermissions, helper.getAASystem());
            fail("AuthorizationDelegate.setPermission allows unauthorized subject");
        } catch (AuthorizationException e) {
        }

        try {
            authorizationService.setPermissions(helper.getUnauthorizedPerformerUser(), legalActorIds, legalPermissions, helper.getBaseGroup());
            fail("AuthorizationDelegate.setPermission allows unauthorized subject");
        } catch (AuthorizationException e) {
        }
    }
}
