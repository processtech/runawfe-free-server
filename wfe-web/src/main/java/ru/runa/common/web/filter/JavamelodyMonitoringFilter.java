package ru.runa.common.web.filter;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    protected boolean isAllowed(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        return  Commons.isAdministrator(httpRequest.getSession()) && super.isAllowed(httpRequest, httpResponse);
    }


}
