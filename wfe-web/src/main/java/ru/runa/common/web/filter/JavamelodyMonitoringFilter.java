package ru.runa.common.web.filter;

import javax.servlet.http.HttpServletRequest;

import net.bull.javamelody.MonitoringFilter;
import ru.runa.common.web.Commons;

/**
 * Filter for access rules to monitoring
 *
 * @author vkuimov
 * @since 24.05.2017
 */
public class JavamelodyMonitoringFilter extends MonitoringFilter {

    @Override
    protected boolean isRequestAllowed(HttpServletRequest request) {
        return Commons.isAdministrator(request.getSession()) && super.isRequestAllowed(request);
    }

}
