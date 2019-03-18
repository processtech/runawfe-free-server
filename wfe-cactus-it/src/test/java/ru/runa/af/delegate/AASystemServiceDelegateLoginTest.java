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

import org.apache.cactus.ServletTestCase;

import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.SystemService;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.collect.Lists;

/**
 * Created on 16.08.2004
 */
public class AASystemServiceDelegateLoginTest extends ServletTestCase {
    private ServiceTestHelper th;
    private SystemService systemService;
    private static String testPrefix = AASystemServiceDelegateLoginTest.class.getName();

    @Override
    protected void tearDown() throws Exception {
        th.releaseResources();
        systemService = null;
        super.tearDown();
    }

    @Override
    protected void setUp() throws Exception {
        systemService = Delegates.getSystemService();
        th = new ServiceTestHelper(testPrefix);
        th.createDefaultExecutorsMap();
        th.setPermissionsToAuthorizedPerformerOnExecutors(Lists.newArrayList(Permission.LOGIN));
        super.setUp();
    }

    public void testLoginWithUnauthorizedPerformer() throws Exception {
        try {
            systemService.login(th.getUnauthorizedPerformerUser());
            fail("SystemServiceDelegate does not throw AuthorizationFailedException on login() with unauthorized performer user call.");
        } catch (AuthorizationException e) {
            // that's what we expected
        }
    }

    public void testLoginWithAuthorizedPerformer() throws Exception {
        systemService.login(th.getAuthorizedPerformerUser());
        assertTrue("SystemServiceDelegate.login() works.", true);
    }

    public void testLoginWithFakeUser() throws Exception {
        try {
            systemService.login(th.getFakeUser());
            fail("SystemServiceDelegate does not throw AuthorizationFailedException on login() with fakeUser call.");
        } catch (AuthenticationException e) {
            // that's what we expected
        }
    }

}
