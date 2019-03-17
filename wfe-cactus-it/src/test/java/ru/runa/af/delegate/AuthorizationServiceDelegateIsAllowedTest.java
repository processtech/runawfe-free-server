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
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.collect.Lists;

/**
 * Created on 20.08.2004
 * 
 */
public class AuthorizationServiceDelegateIsAllowedTest extends ServletTestCase {
    private ServiceTestHelper helper;

    private AuthorizationService authorizationService;

    @Override
    protected void setUp() throws Exception {
        helper = new ServiceTestHelper(AuthorizationServiceDelegateIsAllowedTest.class.getName());
        helper.createDefaultExecutorsMap();

        Collection<Permission> executorsP = Lists.newArrayList(Permission.CREATE);
        helper.setPermissionsToAuthorizedPerformerOnExecutors(executorsP);

        List<Permission> executorP = Lists.newArrayList(Permission.READ, Permission.UPDATE_STATUS);
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

    public void testIsAllowedFakeSubject() throws Exception {
        try {
            authorizationService.isAllowed(helper.getFakeUser(), Permission.READ, SecuredSingleton.EXECUTORS);
            fail("AuthorizationDelegate.isAllowed() allows fake subject");
        } catch (AuthenticationException e) {
        }
    }

    public void testIsAllowedAASystem() throws Exception {
        assertTrue("AuthorizationDelegate.isAllowed() returns wrong info",
                authorizationService.isAllowed(helper.getAuthorizedPerformerUser(), Permission.CREATE, SecuredSingleton.EXECUTORS));

        assertFalse("AuthorizationDelegate.isAllowed() returns wrong info",
                authorizationService.isAllowed(helper.getAuthorizedPerformerUser(), Permission.READ, SecuredSingleton.EXECUTORS));
    }

    public void testIsAllowedExecutor() throws Exception {
        assertTrue("AuthorizationDelegate.isAllowed() returns wrong info",
                authorizationService.isAllowed(helper.getAuthorizedPerformerUser(), Permission.READ, helper.getBaseGroupActor()));

        assertTrue("AuthorizationDelegate.isAllowed() returns wrong info",
                authorizationService.isAllowed(helper.getAuthorizedPerformerUser(), Permission.UPDATE_STATUS, helper.getBaseGroupActor()));

        assertFalse("AuthorizationDelegate.isAllowed() returns wrong info",
                authorizationService.isAllowed(helper.getAuthorizedPerformerUser(), Permission.UPDATE, helper.getBaseGroupActor()));

        assertTrue("AuthorizationDelegate.isAllowed() returns wrong info",
                authorizationService.isAllowed(helper.getAuthorizedPerformerUser(), Permission.READ, helper.getBaseGroup()));

        assertTrue("AuthorizationDelegate.isAllowed() returns wrong info",
                authorizationService.isAllowed(helper.getAuthorizedPerformerUser(), Permission.UPDATE_STATUS, helper.getBaseGroup()));

        assertFalse("AuthorizationDelegate.isAllowed() returns wrong info",
                authorizationService.isAllowed(helper.getAuthorizedPerformerUser(), Permission.UPDATE, helper.getBaseGroup()));
    }

    public void testIsAllowedExecutorUnauthorized() throws Exception {
        assertFalse(authorizationService.isAllowed(helper.getUnauthorizedPerformerUser(), Permission.READ, SecuredSingleton.EXECUTORS));

        assertFalse(authorizationService.isAllowed(helper.getUnauthorizedPerformerUser(), Permission.READ, helper.getBaseGroupActor()));

        assertFalse(authorizationService.isAllowed(helper.getUnauthorizedPerformerUser(), Permission.READ, helper.getBaseGroup()));
    }

}
