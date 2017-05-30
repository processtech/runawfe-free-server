package ru.runa.common.web.filter;

import javax.servlet.http.HttpServletRequest;

import net.bull.javamelody.MonitoringFilter;
import ru.runa.common.web.Commons;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

/**
 * Filter for access rules to monitoring 
 * @author vkuimov
 * @since 24.05.2017
 */
public class WfeMonitoringFilter extends MonitoringFilter {
	
	@Override
	protected boolean isRequestAllowed(HttpServletRequest request) {
		User user = (User) Commons.getSessionAttribute(request.getSession(), User.class.getName());
		boolean isAdministrator = Delegates.getExecutorService().isAdministrator(user);
		return isAdministrator && super.isRequestAllowed(request);
	}

}
