package ru.runa.common.web;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Map;

import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.portals.bridges.struts.PortletServlet;
import org.apache.portals.bridges.struts.StrutsPortletURL;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionForward;
import org.apache.struts.taglib.TagUtils;

import org.springframework.util.Assert;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.service.delegate.Delegates;
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
    private static final String LOGGED_USER_IS_ADMIN_ATTRIBUTE_NAME = User.class.getName() + "_admin";

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

    public static String encodeToUTF8(String string) {
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
        try {
            return session.getAttribute(attributeName);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    public static void setUser(User user, HttpSession session) {
        session.setAttribute(LOGGED_USER_ATTRIBUTE_NAME, user);
        session.setAttribute(LOGGED_USER_IS_ADMIN_ATTRIBUTE_NAME, Delegates.getExecutorService().isAdministrator(user));
    }

    public static void setUser(User user, PortletSession session) {
        session.setAttribute(LOGGED_USER_ATTRIBUTE_NAME, user);
        session.setAttribute(LOGGED_USER_IS_ADMIN_ATTRIBUTE_NAME, Delegates.getExecutorService().isAdministrator(user));
    }

    public static void removeUser(HttpSession session) {
        session.removeAttribute(LOGGED_USER_ATTRIBUTE_NAME);
    }

    public static User getUser(HttpSession session) {
        User user = (User) getSessionAttribute(session, LOGGED_USER_ATTRIBUTE_NAME);
        if (user == null) {
            throw new InvalidSessionException("Session does not contain subject.");
        }
        return user;
    }

    public static boolean isAdministrator(HttpSession session) {
        Boolean value = (Boolean) getSessionAttribute(session, LOGGED_USER_IS_ADMIN_ATTRIBUTE_NAME);
        if (value != null) {
            return value;
        }
        return false;
    }

    public static String getSelfActionWithQueryString(PageContext ctx) {
        HttpServletRequest rq = (HttpServletRequest) ctx.getRequest();

        String uri = (String)rq.getAttribute("javax.servlet.forward.request_uri");

        String pfx = rq.getContextPath();
        Assert.isTrue(uri.startsWith(pfx));
        uri = uri.substring(pfx.length());

        String qs = rq.getQueryString();
        if (qs != null) {
            uri = uri + "?" + qs;
        }

        return uri;
    }
}
