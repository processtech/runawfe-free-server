package ru.runa.common.web;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.taglib.TagUtils;
import org.apache.struts.util.MessageResources;
import org.apache.struts.util.ModuleUtils;
import org.apache.struts.util.RequestUtils;

import com.google.common.collect.Maps;

public class AjaxWebHelper extends RequestWebHelper {
    private final Log log = LogFactory.getLog(AjaxWebHelper.class);
    private final TagUtils tagUtils = TagUtils.getInstance();

    public AjaxWebHelper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getMessage(String key) {
        String message = null;
        MessageResources resources = (MessageResources) request.getServletContext().getAttribute(Globals.MESSAGES_KEY);
        if (resources != null) {
            Locale userLocale = request.getLocale();
            message = resources.getMessage(userLocale, key);
            if (message == null && log.isDebugEnabled()) {
                log.debug(resources.getMessage("message.resources", key, Globals.MESSAGES_KEY, userLocale));
            }
        }
        return message;
    }

    @Override
    public String getUrl(String relativeUrl) {
        return getUrl(relativeUrl, null, null);
    }

    @Override
    public String getActionUrl(String relativeUrl, Map<String, ? extends Object> params) {
        if (ACTION_DOWNLOAD_PROCESS_FILE.equals(relativeUrl)) {
            return getUrl(null, "/variableDownloader", params);
        }
        if (ACTION_DOWNLOAD_LOG_FILE.equals(relativeUrl)) {
            Map<String, Object> adjusted = Maps.newHashMap();
            adjusted.put("logId", params.remove(PARAM_ID));
            return getUrl(null, "/variableDownloader", adjusted);
        }
        return getUrl(null, relativeUrl, params);
    }

    private String getUrl(String href, String action, Map<String, ? extends Object> params) {
        String charEncoding = "UTF-8";
        ModuleConfig moduleConfig = ModuleUtils.getInstance().getModuleConfig(null, request, request.getServletContext());
        StringBuilder url = new StringBuilder();
        int n = 0;
        if (href != null) {
            ++n;
        }
        if (action != null) {
            ++n;
        }

        if (n != 1) {
            return "";
        }

        if (href != null) {
            url.append(request.getContextPath());
            url.append(href);
        } else if (action != null) {
            ActionServlet servlet = (ActionServlet) request.getServletContext().getAttribute("org.apache.struts.action.ACTION_SERVLET");
            String actionIdPath = RequestUtils.actionIdURL(action, moduleConfig, servlet);
            if (actionIdPath != null) {
                action = actionIdPath;
                url.append(request.getContextPath());
                url.append(actionIdPath);
            } else {
                url.append(getActionMappingURL(action));
            }
        }

        if (params != null && !params.isEmpty()) {
            String temp = url.toString();
            int hash = temp.indexOf(35);
            if (hash > -1) {
                url.setLength(hash);
                temp = url.toString();
            }

            String separator = "&";

            boolean question = temp.indexOf(63) >= 0;
            for(Map.Entry<String, ? extends Object> entry : params.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value == null) {
                    if (!question) {
                        url.append('?');
                        question = true;
                    } else {
                        url.append(separator);
                    }

                    url.append(tagUtils.encodeURL(key, charEncoding));
                    url.append('=');
                } else if (value instanceof String) {
                    if (!question) {
                        url.append('?');
                        question = true;
                    } else {
                        url.append(separator);
                    }

                    url.append(tagUtils.encodeURL(key, charEncoding));
                    url.append('=');
                    url.append(tagUtils.encodeURL((String) value, charEncoding));
                } else if (value instanceof String[]) {
                    String[] values = (String[]) value;

                    for (int i = 0; i < values.length; ++i) {
                        if (!question) {
                            url.append('?');
                            question = true;
                        } else {
                            url.append(separator);
                        }

                        url.append(tagUtils.encodeURL(key, charEncoding));
                        url.append('=');
                        url.append(tagUtils.encodeURL(values[i], charEncoding));
                    }
                } else {
                    if (!question) {
                        url.append('?');
                        question = true;
                    } else {
                        url.append(separator);
                    }

                    url.append(tagUtils.encodeURL(key, charEncoding));
                    url.append('=');
                    url.append(tagUtils.encodeURL(value.toString(), charEncoding));
                }
            }
        }
        return url.toString();
    }

    private String getActionMappingURL(String url) {

        String contextPath = request.getContextPath();
        StringBuffer value = new StringBuffer();

        if (contextPath.length() > 1) {
            value.append(contextPath);
        }

        ModuleConfig moduleConfig = ModuleUtils.getInstance().getModuleConfig(null, request, request.getServletContext());

        if (moduleConfig != null) {
            value.append(moduleConfig.getPrefix());
        }

        String servletMapping = (String) request.getServletContext().getAttribute("org.apache.struts.action.SERVLET_MAPPING");

        if (servletMapping != null) {
            String queryString = null;
            int question = url.indexOf("?");

            if (question >= 0) {
                queryString = url.substring(question);
            }

            String actionMapping = tagUtils.getActionMappingName(url);

            if (servletMapping.startsWith("*.")) {
                value.append(actionMapping);
                value.append(servletMapping.substring(1));
            } else if (servletMapping.endsWith("/*")) {
                value.append(servletMapping.substring(0, servletMapping.length() - 2));

                value.append(actionMapping);
            } else if (servletMapping.equals("/")) {
                value.append(actionMapping);
            }

            if (queryString != null) {
                value.append(queryString);
            }

        } else {
            if (!url.startsWith("/")) {
                value.append("/");
            }
            value.append(url);
        }
        return value.toString();
    }

}
