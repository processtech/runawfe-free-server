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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.portlet.PortletSession;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.portals.bridges.struts.PortletServlet;
import org.apache.portals.bridges.struts.StrutsPortletURL;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionForward;
import org.apache.struts.taglib.TagUtils;

import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.user.User;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

/**
 * Created on 20.08.2004
 *
 */
public class Commons {
    private static final TagUtils tagUtils = TagUtils.getInstance();
    private static final String LOGGED_USER_ATTRIBUTE_NAME = User.class.getName();

    /**
     * Add parameters to provided ActionForward
     *
     * @param forward
     *            ActionForward to add parameters to
     * @param parameters
     *            table of parameters
     * @return ActionForward with added parameters to URL
     */
    public static ActionForward forward(ActionForward forward, Map<String, ? extends Object> parameters) {
        ActionForward newActionForward = new ActionForward(forward);
        newActionForward.setPath(appendParams(forward.getPath(), parameters));
        return newActionForward;
    }

    /**
     * Add parameter to provided ActionForward
     *
     * @param forward
     *            ActionForward to add parameters to
     * @param parameter
     *            parameter name
     * @param value
     *            parameter value
     * @return ActionForward with added parameters to URL
     */
    public static ActionForward forward(ActionForward forward, String parameter, Object value) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(parameter, value);
        return forward(forward, parameters);
    }

    public static String getActionUrl(String actionMapping, PageContext pageContext, PortletUrlType portletUrlType) {
        return getActionUrl(actionMapping, null, pageContext, portletUrlType);
    }

    public static String getActionUrl(String actionMapping, String parameterName, Object parameterValue, PageContext pageContext,
            PortletUrlType portletUrlType) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(parameterName, parameterValue);
        return getActionUrl(actionMapping, parameters, pageContext, portletUrlType);
    }

    public static String getActionUrl(String actionMapping, Map<String, ? extends Object> parameters, PageContext pageContext,
            PortletUrlType portletUrlType) {
        return getActionUrl(actionMapping, parameters, null, pageContext, portletUrlType);
    }

    public static String getActionUrl(String actionMapping, Map<String, ? extends Object> parameters, String anchor, PageContext pageContext,
            PortletUrlType portletUrlType) {
        try {
            String url = tagUtils.computeURL(pageContext, null, null, null, actionMapping, null, convertMap(parameters), anchor, false, false);
            url = applyPortlet(url, pageContext, portletUrlType);
            return url;
        } catch (MalformedURLException e) {
            throw Throwables.propagate(e);
        }
    }

    public static String getForwardUrl(String forward, PageContext pageContext, PortletUrlType portletUrlType) {
        return getForwardUrl(forward, null, pageContext, portletUrlType);
    }

    public static String getForwardUrl(String forward, Map<String, ? extends Object> parameters, PageContext pageContext,
            PortletUrlType portletUrlType) {
        try {
            String url = tagUtils.computeURL(pageContext, forward, null, null, null, null, convertMap(parameters), null, false, false);
            url = applyPortlet(url, pageContext, portletUrlType);
            return url;
        } catch (MalformedURLException e) {
            throw Throwables.propagate(e);
        }
    }

    public static String getUrl(String href, PageContext pageContext, PortletUrlType portletUrlType) {
        return getUrl(href, null, pageContext, portletUrlType);
    }

    public static String getUrl(String href, Map<String, ? extends Object> parameters, PageContext pageContext, PortletUrlType portletUrlType) {
        try {
            String url = tagUtils.computeURL(pageContext, null, null, href, null, null, convertMap(parameters), null, false, false);
            url = applyPortlet(url, pageContext, portletUrlType);
            return url;
        } catch (MalformedURLException e) {
            throw Throwables.propagate(e);
        }
    }

    public static String getMessage(String key, PageContext pageContext) {
        return getMessage(key, pageContext, null);
    }

    public static String getMessage(String key, PageContext pageContext, Object[] args) {
        try {
            return tagUtils.message(pageContext, Globals.MESSAGES_KEY, Globals.LOCALE_KEY, key, args);
        } catch (JspException e) {
            return "!" + key + "!";
        }
    }

    /**
     * Used from JSP
     */
    public static Locale getLocale(PageContext pageContext) {
        return tagUtils.getUserLocale(pageContext, Globals.LOCALE_KEY);
    }

    /**
     * Filter entries with null values
     */
    private static Map<String, String> convertMap(Map<String, ? extends Object> parameters) {
        if (parameters == null) {
            return null;
        }
        Map<String, String> map = Maps.newHashMap();
        for (Map.Entry<String, ? extends Object> entry : parameters.entrySet()) {
            if (entry.getValue() != null) {
                map.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return map;
    }

    private static String appendParams(String url, Map<String, ? extends Object> parameters) {
        // taken from ParamSupport from jakarta taglib-standard 1.1
        StringBuilder newParams = new StringBuilder();
        for (Map.Entry<String, ? extends Object> entry : parameters.entrySet()) {
            newParams.append(encodeToUTF8(entry.getKey())).append("=").append(encodeToUTF8(entry.getValue().toString())).append("&");
        }
        return appendParameterString(url, newParams);
    }

    private static String appendParameterString(String url, StringBuilder newParams) {
        // taken from ParamSupport from jakarta taglib-standard 1.1
        if (newParams.length() > 0) {
            int questionMark = url.indexOf('?');
            if (questionMark == -1) {
                return url + "?" + newParams;
            }
            StringBuilder workingUrl = new StringBuilder(url);
            workingUrl.insert(questionMark + 1, newParams + "&");
            return workingUrl.toString();
        }
        return url;
    }

    private static String encodeToUTF8(String string) {
        try {
            return URLEncoder.encode(string, Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw Throwables.propagate(e);
        }
    }

    private static String applyPortlet(String url, PageContext pageContext, PortletUrlType portletUrlType) {
        if (PortletServlet.isPortletRequest(pageContext.getRequest()) && portletUrlType != PortletUrlType.Resource) {
            url = portletUrlType.equals(PortletUrlType.Render) ? StrutsPortletURL.createRenderURL(pageContext.getRequest(), url).toString()
                    : StrutsPortletURL.createActionURL(pageContext.getRequest(), url).toString();
        }
        return url;
    }

    public static Object getSessionAttribute(HttpSession session, String attributeName) {
        Object retVal = null;
        try {
            retVal = session.getAttribute(attributeName);
            if (retVal == null) {
                retVal = session.getAttribute("?" + attributeName);
            }
            if (retVal == null) {
                Enumeration<String> attributes = session.getAttributeNames();
                while (attributes.hasMoreElements()) {
                    String attribute = attributes.nextElement();
                    if (attribute.endsWith(attributeName)) {
                        retVal = session.getAttribute(attribute);
                        break;
                    }
                }
            }
        } catch (IllegalStateException e) {
        }
        return retVal;
    }

    public static void setUser(User user, HttpSession session) {
        session.setAttribute(LOGGED_USER_ATTRIBUTE_NAME, user);
    }

    public static void setUser(User user, PortletSession session) {
        session.setAttribute(LOGGED_USER_ATTRIBUTE_NAME, user);
    }

    public static void removeUser(HttpSession session) {
        session.removeAttribute(LOGGED_USER_ATTRIBUTE_NAME);
    }

    public static User getUser(HttpSession session) {
        try {
            User user = (User) getSessionAttribute(session, LOGGED_USER_ATTRIBUTE_NAME);
            if (user == null) {
                throw new InvalidSessionException("Session does not contain subject.");
            }
            return user;
        } catch (IllegalStateException e) {
            throw new InvalidSessionException("Session does not contain subject.");
        }
    }

}
