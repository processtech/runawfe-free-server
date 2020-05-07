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
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 20.08.2004.
 */
public class AuthorizationServiceDelegateIsAllowedTest extends ServletTestCase {
    private AuthorizationService authorizationService;
    private ServiceTestHelper h;

    @Override
    protected void setUp() {
        authorizationService = Delegates.getAuthorizationService();
        h = new ServiceTestHelper(AuthorizationServiceDelegateIsAllowedTest.class.getName());
        h.createDefaultExecutorsMap();

        val ppSystem = Lists.newArrayList(Permission.LOGIN);
        h.setPermissionsToAuthorizedActor(ppSystem, SecuredSingleton.SYSTEM);

        val ppExecutors = Lists.newArrayList(Permission.READ, Permission.VIEW_TASKS);
        h.setPermissionsToAuthorizedActor(ppExecutors, h.getBaseGroupActor());
        h.setPermissionsToAuthorizedActor(ppExecutors, h.getBaseGroup());
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        authorizationService = null;
    }

    public void testIsAllowedFakeUser() {
        try {
            authorizationService.isAllowed(h.getFakeUser(), Permission.READ, SecuredSingleton.SYSTEM);
            fail("AuthorizationDelegate.isAllowed() allows fake subject");
        } catch (AuthenticationException e) {
            // Expected.
        }
    }

    public void testIsAllowedAASystem() {
        assertTrue("AuthorizationDelegate.isAllowed() returns wrong info",
                authorizationService.isAllowed(h.getAuthorizedUser(), Permission.LOGIN, SecuredSingleton.SYSTEM));

        assertFalse("AuthorizationDelegate.isAllowed() returns wrong info",
                authorizationService.isAllowed(h.getAuthorizedUser(), Permission.READ, SecuredSingleton.SYSTEM));
    }

    public void testIsAllowedExecutor() {
        assertTrue("AuthorizationDelegate.isAllowed() returns wrong info",
                authorizationService.isAllowed(h.getAuthorizedUser(), Permission.READ, h.getBaseGroupActor()));

        assertTrue("AuthorizationDelegate.isAllowed() returns wrong info",
                authorizationService.isAllowed(h.getAuthorizedUser(), Permission.VIEW_TASKS, h.getBaseGroupActor()));

        assertFalse("AuthorizationDelegate.isAllowed() returns wrong info",
                authorizationService.isAllowed(h.getAuthorizedUser(), Permission.UPDATE, h.getBaseGroupActor()));

        assertTrue("AuthorizationDelegate.isAllowed() returns wrong info",
                authorizationService.isAllowed(h.getAuthorizedUser(), Permission.READ, h.getBaseGroup()));

        assertTrue("AuthorizationDelegate.isAllowed() returns wrong info",
                authorizationService.isAllowed(h.getAuthorizedUser(), Permission.VIEW_TASKS, h.getBaseGroup()));

        assertFalse("AuthorizationDelegate.isAllowed() returns wrong info",
                authorizationService.isAllowed(h.getAuthorizedUser(), Permission.UPDATE, h.getBaseGroup()));
    }

    public void testIsAllowedExecutorUnauthorized() {
        assertFalse(authorizationService.isAllowed(h.getUnauthorizedUser(), Permission.READ, SecuredSingleton.SYSTEM));

        assertFalse(authorizationService.isAllowed(h.getUnauthorizedUser(), Permission.READ, h.getBaseGroupActor()));

        assertFalse(authorizationService.isAllowed(h.getUnauthorizedUser(), Permission.READ, h.getBaseGroup()));
    }
}
