package ru.runa.common.web;

import javax.portlet.PortletSession;
import javax.servlet.http.HttpSession;

/**
 * Created on 07.04.2005
 *
 */
public class TabHttpSessionHelper {
    private static final String TAB_ATTRIBUTE_NAME = TabHttpSessionHelper.class.getName() + "_CURRENT_TAB_FORWARD";

    public static void setTabForwardName(String tabForwardName, HttpSession session) {
        session.setAttribute(TAB_ATTRIBUTE_NAME, tabForwardName);
    }

    public static void setTabForwardName(String tabForwardName, PortletSession session) {
        session.setAttribute(TAB_ATTRIBUTE_NAME, tabForwardName);
    }

    public static void removeTabForwardName(HttpSession session) {
        session.removeAttribute(TAB_ATTRIBUTE_NAME);
    }

    public static String getTabForwardName(HttpSession session) {
        String tabForwardName = (String) Commons.getSessionAttribute(session, TAB_ATTRIBUTE_NAME);
        if (tabForwardName == null) {
            throw new InvalidSessionException("Session does not contain tab forward name.");
        }
        return tabForwardName;
    }
}
