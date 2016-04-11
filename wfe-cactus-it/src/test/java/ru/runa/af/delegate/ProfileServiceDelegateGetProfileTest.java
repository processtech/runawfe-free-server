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
import ru.runa.wfe.service.ProfileService;
import ru.runa.wfe.service.delegate.Delegates;

public class ProfileServiceDelegateGetProfileTest extends ServletTestCase {
    private static final String PREFIX = ProfileServiceDelegateGetProfileTest.class.getName();

    private ServiceTestHelper th;

    private ProfileService profileService;

    @Override
    protected void setUp() throws Exception {
        th = new ServiceTestHelper(PREFIX);
        profileService = Delegates.getProfileService();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        th.releaseResources();
        th = null;
        profileService = null;
        super.tearDown();
    }

    public void testNullUser() throws Exception {
        try {
            profileService.getProfile(null);
            fail("ProfileServiceDelegate.saveProfile() allows null subject");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testFakeUser() throws Exception {
        try {
            profileService.getProfile(th.getFakeUser());
            fail("ProfileServiceDelegate.saveProfile() allows fake subject");
        } catch (AuthenticationException e) {
        }
    }

}
