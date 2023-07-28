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
