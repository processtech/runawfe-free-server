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
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SystemPermission;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.ExecutorPermission;
import ru.runa.wfe.user.Group;

import com.google.common.collect.Lists;

/**
 * Created on 16.02.2005
 */
public class AuthorizationServiceDelegateSetMultiExecutorsPermissionsTest extends ServletTestCase {
    private ServiceTestHelper th;
    private AuthorizationService authorizationService;

    Collection<Permission> testPermission = Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS);

    private static String testPrefix = AuthorizationServiceDelegateSetMultiExecutorsPermissionsTest.class.getName();

    private Group additionalGroup;
    private Actor additionalActor;

    private List<Executor> additionalActorGroupsMixed;
    private List<Long> executorIDs;

    @Override
    protected void setUp() throws Exception {
        th = new ServiceTestHelper(testPrefix);

        Collection<Permission> readUpdateSystemPermission = Lists.newArrayList(SystemPermission.READ, SystemPermission.UPDATE_PERMISSIONS);
        Collection<Permission> readUpdateExecutorPermission = Lists.newArrayList(ExecutorPermission.READ, ExecutorPermission.UPDATE_PERMISSIONS);

        th.setPermissionsToAuthorizedPerformerOnSystem(readUpdateSystemPermission);

        authorizationService = Delegates.getAuthorizationService();

        additionalActor = th.createActorIfNotExist("additionalA", "Additional Actor");
        additionalGroup = th.createGroupIfNotExist("additionalG", "Additional Group");
        additionalActorGroupsMixed = th.createMixedActorsGroupsArray("mixed", "Additional Mixed");
        executorIDs = Lists.newArrayList();
        for (Executor executor : additionalActorGroupsMixed) {
            executorIDs.add(executor.getId());
            th.setPermissionsToAuthorizedPerformer(readUpdateExecutorPermission, executor);
        }

        th.setPermissionsToAuthorizedPerformer(readUpdateExecutorPermission, additionalActor);
        th.setPermissionsToAuthorizedPerformer(readUpdateExecutorPermission, additionalGroup);

        super.setUp();
    }

    public void testSetPermissions() throws Exception {
        authorizationService.setPermissions(th.getAuthorizedPerformerUser(), executorIDs, testPermission, additionalActor);
        for (int i = 0; i < executorIDs.size(); i++) {
            additionalActorGroupsMixed.set(i, Delegates.getExecutorService().getExecutor(th.getAuthorizedPerformerUser(), executorIDs.get(i)));
        }
        for (int i = 0; i < executorIDs.size(); i++) {
            Collection<Permission> expected = authorizationService.getIssuedPermissions(th.getAuthorizedPerformerUser(),
                    additionalActorGroupsMixed.get(i), additionalActor);
            ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions", testPermission, expected);
        }

        authorizationService.setPermissions(th.getAuthorizedPerformerUser(), executorIDs, testPermission, additionalGroup);

        for (int i = 0; i < executorIDs.size(); i++) {
            additionalActorGroupsMixed.set(i, Delegates.getExecutorService().getExecutor(th.getAuthorizedPerformerUser(), executorIDs.get(i)));
        }

        for (int i = 0; i < executorIDs.size(); i++) {
            Collection<Permission> expected = authorizationService.getIssuedPermissions(th.getAuthorizedPerformerUser(),
                    additionalActorGroupsMixed.get(i), additionalGroup);
            ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions", testPermission, expected);
        }
    }

    public void testSetPermissionsNullSubject() throws Exception {
        try {
            authorizationService.setPermissions(null, executorIDs, testPermission, additionalActor);
            fail("AuthorizationDelegate.setPermissions() allows null subject");
        } catch (IllegalArgumentException e) {
            // This is what we expect
        }
    }

    public void testSetPermissionsFakeSubject() throws Exception {
        try {
            authorizationService.setPermissions(th.getFakeUser(), executorIDs, testPermission, additionalActor);
            fail("AuthorizationDelegate.setPermissions() allows fake subject");
        } catch (AuthenticationException e) {
            // This is what we expect
        }
    }

    public void testSetPermissionsFakeIdentifiable() throws Exception {
        try {
            authorizationService.setPermissions(th.getAuthorizedPerformerUser(), executorIDs, testPermission, th.getFakeActor());
            fail("AuthorizationDelegate.setPermissions() allows fake executor");
        } catch (InternalApplicationException e) {
            // This is what we expect
        }
    }

    public void testSetPermissionsNullPermissions() throws Exception {
        try {
            authorizationService.setPermissions(th.getAuthorizedPerformerUser(), executorIDs, (Collection<Permission>) null, additionalActor);
            fail("AuthorizationDelegate.setPermissions() allows null permissions");
        } catch (IllegalArgumentException e) {
            // This is what we expect
        }
    }

    public void testSetPermissionsNullIdentifiable() throws Exception {
        try {
            authorizationService.setPermissions(th.getAuthorizedPerformerUser(), executorIDs, testPermission, null);
            fail("AuthorizationDelegate.setPermissions() allows null identifiable");
        } catch (IllegalArgumentException e) {
            // This is what we expect
        }
    }

    public void testSetPermissionsFakeExecutors() throws Exception {
        try {
            authorizationService.setPermissions(th.getAuthorizedPerformerUser(), Lists.newArrayList(-1L, -2L, -3L), testPermission, additionalActor);
            fail("AuthorizationDelegate.setPermissions() allows Fake Executors");
        } catch (ExecutorDoesNotExistException e) {
            // This is what we expect
        }
    }

    public void testSetPermissionsUnauthorized() throws Exception {
        try {
            authorizationService.setPermissions(th.getUnauthorizedPerformerUser(), executorIDs, testPermission, additionalActor);
            fail("AuthorizationDelegate.setPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
            // This is what we expect
        }

        try {
            authorizationService.setPermissions(th.getUnauthorizedPerformerUser(), executorIDs, testPermission, additionalGroup);
            fail("AuthorizationDelegate.setPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
            // This is what we expect
        }

    }

    @Override
    protected void tearDown() throws Exception {
        th.releaseResources();
        authorizationService = null;
        super.tearDown();
    }

}
