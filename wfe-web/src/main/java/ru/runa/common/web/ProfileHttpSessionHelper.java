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
package ru.runa.common.web;

import javax.portlet.PortletSession;
import javax.servlet.http.HttpSession;

import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Profile;

/**
 * Created on 24.01.2005
 * 
 */
public class ProfileHttpSessionHelper {

    public static final String PROFILE_ATTRIBUTE_NAME = Profile.class.getName();

    public static void setProfile(Profile profile, HttpSession session) {
        session.setAttribute(PROFILE_ATTRIBUTE_NAME, profile);
    }

    public static void setProfile(Profile profile, PortletSession session) {
        session.setAttribute(PROFILE_ATTRIBUTE_NAME, profile);
    }

    public static void removeProfile(HttpSession session) {
        session.removeAttribute(PROFILE_ATTRIBUTE_NAME);
    }

    public static Profile getProfile(HttpSession session) {
        Profile profile = (Profile) Commons.getSessionAttribute(session, PROFILE_ATTRIBUTE_NAME);
        if (profile == null) {
            throw new InvalidSessionException("Session does not contain profile.");
        }
        return profile;
    }

    public static void reloadProfile(HttpSession session) {
        Profile profile = Delegates.getProfileService().getProfile(Commons.getUser(session));
        setProfile(profile, session);
    }

}
