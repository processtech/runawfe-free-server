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
