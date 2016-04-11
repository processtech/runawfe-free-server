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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import ru.runa.common.WebResources;

public class ConfirmationPopupHelper {
    private static ConfirmationPopupHelper instance;

    public static final String ACCEPT_TASK_PARAMETER = "accept.task";
    public static final String EXECUTE_TASK_PARAMETER = "execute.task";
    public static final String CANCEL_PROCESS_PARAMETER = "cancel.process";
    public static final String REMOVE_PROCESS_PARAMETER = "remove.process";
    public static final String START_PROCESS_PARAMETER = "start.process";
    public static final String START_PROCESS_FORM_PARAMETER = "start.process.noform";
    public static final String DEPLOY_PROCESS_DEFINITION_PARAMETER = "deploy.processdefinition";
    public static final String REDEPLOY_PROCESS_DEFINITION_PARAMETER = "redeploy.processdefinition";
    public static final String UNDEPLOY_PROCESS_DEFINITION_PARAMETER = "undeploy.processdefinition";
    public static final String REMOVE_SUBSTITUTION_CRITERIA_PARAMETER = "remove.substitutioncriteria";

    public static final String REMOVE_EXECUTORS_PARAMETER = "remove.executors";
    public static final String REMOVE_EXECUTORS_FROM_GROUPS_PARAMETER = "remove.executorsfromgroups";
    public static final String REMOVE_BOT_STATION_PARAMETER = "remove.botstation";
    public static final String REMOVE_BOT_PARAMETER = "remove.bot";
    public static final String USE_DEFAULT_PROPERTIES_PARAMETER = "use.default.properties";

    private static final Map<String, String> confirmationResource = new HashMap<String, String>();
    private static final Map<String, String> confirmationCookies = new HashMap<String, String>();

    static {
        confirmationCookies.put(ACCEPT_TASK_PARAMETER, "accept.task.cookie");
        confirmationCookies.put(EXECUTE_TASK_PARAMETER, "execute.task.cookie");
        confirmationCookies.put(CANCEL_PROCESS_PARAMETER, "cancel.process.cookie");
        confirmationCookies.put(REMOVE_PROCESS_PARAMETER, "remove.process.cookie");
        confirmationCookies.put(START_PROCESS_PARAMETER, "start.process.cookie");
        confirmationCookies.put(START_PROCESS_FORM_PARAMETER, "start.process.form.cookie");
        confirmationCookies.put(DEPLOY_PROCESS_DEFINITION_PARAMETER, "deploy.processdefinition.cookie");
        confirmationCookies.put(REDEPLOY_PROCESS_DEFINITION_PARAMETER, "redeploy.processdefinition.cookie");
        confirmationCookies.put(UNDEPLOY_PROCESS_DEFINITION_PARAMETER, "undeploy.processdefinition.cookie");
        confirmationCookies.put(REMOVE_SUBSTITUTION_CRITERIA_PARAMETER, "remove.substitutioncriteria.cookie");

        confirmationCookies.put(REMOVE_BOT_PARAMETER, "remove.bot.cookie");
        confirmationCookies.put(REMOVE_BOT_STATION_PARAMETER, "remove.botstation.cookie");
        confirmationCookies.put(REMOVE_EXECUTORS_PARAMETER, "remove.executors.cookie");
        confirmationCookies.put(REMOVE_EXECUTORS_FROM_GROUPS_PARAMETER, "remove.executorsfromgroups.cookie");
        confirmationCookies.put(USE_DEFAULT_PROPERTIES_PARAMETER, "use.default.properties.cookie");
    }

    static {
        confirmationResource.put(ACCEPT_TASK_PARAMETER, Messages.CONF_POPUP_ACCEPT_TASK);
        confirmationResource.put(EXECUTE_TASK_PARAMETER, Messages.CONF_POPUP_EXECUTE_TASK);
        confirmationResource.put(CANCEL_PROCESS_PARAMETER, Messages.CONF_POPUP_CANCEL_PROCESS);
        confirmationResource.put(REMOVE_PROCESS_PARAMETER, Messages.CONF_POPUP_REMOVE_PROCESS);
        confirmationResource.put(START_PROCESS_PARAMETER, Messages.CONF_POPUP_START_PROCESS);
        confirmationResource.put(START_PROCESS_FORM_PARAMETER, Messages.CONF_POPUP_START_PROCESS);
        confirmationResource.put(DEPLOY_PROCESS_DEFINITION_PARAMETER, Messages.CONF_POPUP_DEPLOY_PROCESSDEFINIION);
        confirmationResource.put(REDEPLOY_PROCESS_DEFINITION_PARAMETER, Messages.CONF_POPUP_REDEPLOY_PROCESSDEFINIION);
        confirmationResource.put(UNDEPLOY_PROCESS_DEFINITION_PARAMETER, Messages.CONF_POPUP_UNDEPLOY_PROCESSDEFINIION);
        confirmationResource.put(REMOVE_SUBSTITUTION_CRITERIA_PARAMETER, Messages.CONF_POPUP_REMOVE_SUBSTITUTION_CRITERIA);

        confirmationResource.put(REMOVE_BOT_PARAMETER, Messages.CONF_POPUP_REMOVE_BOT);
        confirmationResource.put(REMOVE_BOT_STATION_PARAMETER, Messages.CONF_POPUP_REMOVE_BOT_STATION);
        confirmationResource.put(REMOVE_EXECUTORS_PARAMETER, Messages.CONF_POPUP_REMOVE_EXECUTORS);
        confirmationResource.put(REMOVE_EXECUTORS_FROM_GROUPS_PARAMETER, Messages.CONF_POPUP_REMOVE_EXECUTORS_FROM_GROUPS);
        confirmationResource.put(USE_DEFAULT_PROPERTIES_PARAMETER, Messages.CONF_POPUP_USE_DEFAULT_PROPERTIES);
    }

    public static ConfirmationPopupHelper getInstance() {
        if (instance == null) {
            instance = new ConfirmationPopupHelper();
        }
        return instance;
    }

    public boolean isEnabled(String parameter) {
        return WebResources.getBooleanProperty("confirmation." + parameter, false);
    }

    public String getConfirmationPopupCodeHTML(String parameter, PageContext pageContext) {
        return "openConfirmPopup(this,'" + Messages.getMessage(confirmationCookies.get(parameter), pageContext) + "', '"
                + Messages.getMessage(confirmationResource.get(parameter), pageContext) + "', '"
                + Messages.getMessage(Messages.CONF_POPUP_CONFIRM_ACTION, pageContext) + "','"
                + Messages.getMessage(Messages.CONF_POPUP_BUTTON_CANCEL, pageContext) + "', '"
                + Messages.getMessage(Messages.CONF_POPUP_BUTTON_OK, pageContext) + "'); return false;";
    }
}
