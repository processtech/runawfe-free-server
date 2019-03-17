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
import ru.runa.wfe.security.SecuredSingleton;
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

        Collection<Permission> executorsP = Lists.newArrayList(Permission.UPDATE);
        helper.setPermissionsToAuthorizedPerformerOnExecutors(executorsP);

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

    public void testSetPermissionsFakeSubject() throws Exception {
        try {
            authorizationService.setPermissions(helper.getFakeUser(), legalActorIds, legalPermissions, SecuredSingleton.EXECUTORS);
            fail("AuthorizationDelegate.setPermission allows Fake subject");
        } catch (AuthenticationException e) {
        }
    }

    public void testSetPermissions() throws Exception {
        authorizationService.setPermissions(helper.getAuthorizedPerformerUser(), legalActorIds, legalPermissions, SecuredSingleton.EXECUTORS);
        for (int i = 0; i < legalActorIds.size(); i++) {
            Executor executor = executorService.getExecutor(helper.getAuthorizedPerformerUser(), legalActorIds.get(i));
            Collection<Permission> actual = authorizationService.getIssuedPermissions(helper.getAuthorizedPerformerUser(), executor,
                    SecuredSingleton.EXECUTORS);
            ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions on system",
                    legalPermissions.get(i), actual);
        }

        legalPermissions = Lists.newArrayList();
        legalPermissions.add(Lists.newArrayList(Permission.READ, Permission.UPDATE_STATUS));
        legalPermissions.add(Lists.newArrayList(Permission.READ, Permission.UPDATE_STATUS));
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
            authorizationService.setPermissions(helper.getUnauthorizedPerformerUser(), legalActorIds, legalPermissions, SecuredSingleton.EXECUTORS);
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
