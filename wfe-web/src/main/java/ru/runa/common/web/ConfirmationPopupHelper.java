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
    public static final String UNDEPLOY_REPORT_PARAMETER = "undeploy.report";

    public static final String REMOVE_EXECUTORS_PARAMETER = "remove.executors";
    public static final String REMOVE_EXECUTORS_FROM_GROUPS_PARAMETER = "remove.executorsfromgroups";
    public static final String REMOVE_BOT_STATION_PARAMETER = "remove.botstation";
    public static final String REMOVE_BOT_PARAMETER = "remove.bot";
    public static final String REMOVE_DATA_SOURCE_PARAMETER = "remove.datasource";
    public static final String USE_DEFAULT_PROPERTIES_PARAMETER = "use.default.properties";
    public static final String UPDATE_DIGITAL_SIGNATURE_PARAMETER = "update.digitalsignature";
    public static final String DELETE_DIGITAL_SIGNATURE_PARAMETER = "delete.digitalsignature";

    private static final Map<String, StrutsMessage> confirmationResource = new HashMap<String, StrutsMessage>();
    private static final Map<String, StrutsMessage> confirmationCookies = new HashMap<String, StrutsMessage>();

    static {
        confirmationCookies.put(ACCEPT_TASK_PARAMETER, MessagesConfirmation.COOKIE_ACCEPT_TASK);
        confirmationCookies.put(EXECUTE_TASK_PARAMETER, MessagesConfirmation.COOKIE_EXECUTE_TASK);
        confirmationCookies.put(CANCEL_PROCESS_PARAMETER, MessagesConfirmation.COOKIE_CANCEL_PROCESS);
        confirmationCookies.put(REMOVE_PROCESS_PARAMETER, MessagesConfirmation.COOKIE_REMOVE_PROCESS);
        confirmationCookies.put(START_PROCESS_PARAMETER, MessagesConfirmation.COOKIE_START_PROCESS);
        confirmationCookies.put(START_PROCESS_FORM_PARAMETER, MessagesConfirmation.COOKIE_START_PROCESS);
        confirmationCookies.put(DEPLOY_PROCESS_DEFINITION_PARAMETER, MessagesConfirmation.COOKIE_DEPLOY_PROCESSDEFINIION);
        confirmationCookies.put(REDEPLOY_PROCESS_DEFINITION_PARAMETER, MessagesConfirmation.COOKIE_REDEPLOY_PROCESSDEFINIION);
        confirmationCookies.put(UNDEPLOY_PROCESS_DEFINITION_PARAMETER, MessagesConfirmation.COOKIE_UNDEPLOY_PROCESSDEFINIION);
        confirmationCookies.put(REMOVE_SUBSTITUTION_CRITERIA_PARAMETER, MessagesConfirmation.COOKIE_REMOVE_SUBSTITUTION_CRITERIA);

        confirmationCookies.put(REMOVE_BOT_PARAMETER, MessagesConfirmation.COOKIE_REMOVE_BOT);
        confirmationCookies.put(REMOVE_BOT_STATION_PARAMETER, MessagesConfirmation.COOKIE_REMOVE_BOT_STATION);
        confirmationCookies.put(REMOVE_EXECUTORS_PARAMETER, MessagesConfirmation.COOKIE_REMOVE_EXECUTORS);
        confirmationCookies.put(REMOVE_EXECUTORS_FROM_GROUPS_PARAMETER, MessagesConfirmation.COOKIE_REMOVE_EXECUTORS_FROM_GROUPS);
        confirmationCookies.put(REMOVE_DATA_SOURCE_PARAMETER, MessagesConfirmation.COOKIE_REMOVE_DATA_SOURCE);
        confirmationCookies.put(USE_DEFAULT_PROPERTIES_PARAMETER, MessagesConfirmation.COOKIE_USE_DEFAULT_PROPERTIES);
        confirmationCookies.put(UPDATE_DIGITAL_SIGNATURE_PARAMETER, MessagesConfirmation.CONF_POPUP_UPDATE_DS);
        confirmationCookies.put(DELETE_DIGITAL_SIGNATURE_PARAMETER, MessagesConfirmation.CONF_POPUP_DELETE_DS);
    }

    static {
        confirmationResource.put(ACCEPT_TASK_PARAMETER, MessagesConfirmation.CONF_POPUP_ACCEPT_TASK);
        confirmationResource.put(EXECUTE_TASK_PARAMETER, MessagesConfirmation.CONF_POPUP_EXECUTE_TASK);
        confirmationResource.put(CANCEL_PROCESS_PARAMETER, MessagesConfirmation.CONF_POPUP_CANCEL_PROCESS);
        confirmationResource.put(REMOVE_PROCESS_PARAMETER, MessagesConfirmation.CONF_POPUP_REMOVE_PROCESS);
        confirmationResource.put(START_PROCESS_PARAMETER, MessagesConfirmation.CONF_POPUP_START_PROCESS);
        confirmationResource.put(START_PROCESS_FORM_PARAMETER, MessagesConfirmation.CONF_POPUP_START_PROCESS);
        confirmationResource.put(DEPLOY_PROCESS_DEFINITION_PARAMETER, MessagesConfirmation.CONF_POPUP_DEPLOY_PROCESSDEFINIION);
        confirmationResource.put(REDEPLOY_PROCESS_DEFINITION_PARAMETER, MessagesConfirmation.CONF_POPUP_REDEPLOY_PROCESSDEFINIION);
        confirmationResource.put(UNDEPLOY_PROCESS_DEFINITION_PARAMETER, MessagesConfirmation.CONF_POPUP_UNDEPLOY_PROCESSDEFINIION);
        confirmationResource.put(REMOVE_SUBSTITUTION_CRITERIA_PARAMETER, MessagesConfirmation.CONF_POPUP_REMOVE_SUBSTITUTION_CRITERIA);

        confirmationResource.put(REMOVE_BOT_PARAMETER, MessagesConfirmation.CONF_POPUP_REMOVE_BOT);
        confirmationResource.put(REMOVE_BOT_STATION_PARAMETER, MessagesConfirmation.CONF_POPUP_REMOVE_BOT_STATION);
        confirmationResource.put(REMOVE_EXECUTORS_PARAMETER, MessagesConfirmation.CONF_POPUP_REMOVE_EXECUTORS);
        confirmationResource.put(REMOVE_EXECUTORS_FROM_GROUPS_PARAMETER, MessagesConfirmation.CONF_POPUP_REMOVE_EXECUTORS_FROM_GROUPS);
        confirmationResource.put(REMOVE_DATA_SOURCE_PARAMETER, MessagesConfirmation.CONF_POPUP_REMOVE_DATA_SOURCE);
        confirmationResource.put(USE_DEFAULT_PROPERTIES_PARAMETER, MessagesConfirmation.CONF_POPUP_USE_DEFAULT_PROPERTIES);
        confirmationResource.put(UPDATE_DIGITAL_SIGNATURE_PARAMETER, MessagesConfirmation.CONF_POPUP_UPDATE_DS);
        confirmationResource.put(DELETE_DIGITAL_SIGNATURE_PARAMETER, MessagesConfirmation.CONF_POPUP_DELETE_DS);
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
        return "openConfirmPopup(this,'" + confirmationCookies.get(parameter).message(pageContext) + "', '"
                + confirmationResource.get(parameter).message(pageContext) + "', '"
                + MessagesConfirmation.CONF_POPUP_CONFIRM_ACTION.message(pageContext) + "','"
                + MessagesConfirmation.CONF_POPUP_BUTTON_CANCEL.message(pageContext) + "', '"
                + MessagesConfirmation.CONF_POPUP_BUTTON_OK.message(pageContext) + "'); return false;";
    }
    public String getDeletePopupCodeHTML(String parameter, PageContext pageContext) {
        return "openDeletePopup(this,'" + confirmationCookies.get(parameter).message(pageContext) + "', '"
                + confirmationResource.get(parameter).message(pageContext) + "', '"
                + MessagesConfirmation.CONF_POPUP_CONFIRM_ACTION.message(pageContext) + "','"
                + MessagesConfirmation.CONF_POPUP_BUTTON_CANCEL.message(pageContext) + "', '"
                + MessagesConfirmation.CONF_POPUP_BUTTON_OK.message(pageContext) + "'); return false;";
    }
}
