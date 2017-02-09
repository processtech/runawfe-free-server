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
package ru.runa.common;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.logging.LogFactory;

import com.google.common.base.Strings;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.PropertyResources;
import ru.runa.wfe.commons.SystemProperties;

/**
 * Created on 30.09.2004
 *
 */
public class WebResources {
    private static final PropertyResources RESOURCES = new PropertyResources("web.properties");

    public static final String ACTION_MAPPING_UPDATE_EXECUTOR = "/manage_executor";
    public static final String ACTION_MAPPING_MANAGE_RELATION = "/manage_relation";

    /* Validation rules */
    public static final int VALIDATOR_STRING_255 = 255;

    public static final String ACTION_MAPPING_MANAGE_DEFINITION = "/manage_process_definition";
    public static final String ACTION_MAPPING_MANAGE_PROCESS = "/manage_process";
    public static final String ACTION_SHOW_PROCESS_GRAPH = "/show_process_graph";
    public static final String ACTION_SHOW_GRAPH_HISTORY = "/show_graph_history";
    public static final String ACTION_MAPPING_START_PROCESS = "/startProcess";
    public static final String ACTION_MAPPING_SUBMIT_TASK_DISPATCHER = "/submitTaskDispatcher";
    public static final String ACTION_MAPPING_REDEPLOY_PROCESS_DEFINITION = "/redeploy_process_definition";
    public static final String ACTION_UPDATE_PROCESS_VARIABLES = "/update_process_variables";
    public static final String ACTION_UPDATE_PROCESS_SWIMLANES = "/update_process_swimlanes";
    public static final String FORWARD_SUCCESS_DISPLAY_START_FORM = "success_display_start_form";

    public static final String START_PROCESS_IMAGE = "/images/start.gif";
    public static final String START_PROCESS_DISABLED_IMAGE = "/images/start-disabled.gif";

    public static final String HIDDEN_ONE_TASK_INDICATOR = "one_task_hidden_field";
    public static final String HIDDEN_TASK_PREVIOUS_OWNER_ID = "taskOwnerId_hidden_field";

    public static final String ACTION_MAPPING_MANAGE_REPORT = "/manage_report";
    public static final String ACTION_MAPPING_BUILD_REPORT = "/build_report";

    public static PropertyResources getResources() {
        return RESOURCES;
    }

    public static boolean getBooleanProperty(String name, boolean defaultValue) {
        return RESOURCES.getBooleanProperty(name, defaultValue);
    }

    public static String getTaskFormBuilderClassName(String formFileType) {
        return RESOURCES.getStringPropertyNotNull("task.form.builder." + formFileType);
    }

    public static boolean isHighlightRequiredFields() {
        return RESOURCES.getBooleanProperty("task.form.highlightRequiredFields", false);
    }

    public static boolean useImagesForValidationErrors() {
        return RESOURCES.getBooleanProperty("task.form.useImagesForValidationErrors", true);
    }

    /**
     * Used from JSP page
     */
    public static boolean isTaskDelegationEnabled() {
        return RESOURCES.getBooleanProperty("task.delegation.enabled", true);
    }

    /**
     * Used from JSP page
     */
    public static int getDiagramRefreshInterval() {
        return RESOURCES.getIntegerProperty("process.graph.autoRefreshInterval.seconds", 0);
    }

    /**
     * Used from JSP page
     */
    public static String getTaskExpiredWarningThreshold() {
        return SystemProperties.getTaskAlmostDeadlineInPercents() + "%";
    }

    public static boolean isGroupBySubprocessEnabled() {
        return RESOURCES.getBooleanProperty("group.subprocess.enabled", false);
    }

    public static boolean isShowGraphMode() {
        return RESOURCES.getBooleanProperty("process.showGraphMode", false);
    }

    public static boolean isNTLMSupported() {
        return RESOURCES.getBooleanProperty("ntlm.enabled", false);
    }

    public static String getDomainName() {
        return RESOURCES.getStringPropertyNotNull("ntlm.domain");
    }

    public static boolean isVersionDisplay() {
        return RESOURCES.getBooleanProperty("version.display", true);
    }

    public static boolean isAutoShowForm() {
        return RESOURCES.getBooleanProperty("task.form.autoShowNext", false);
    }

    /**
     * Used from JSP page
     */
    public static List<String> getTaskFormExternalJsLibs() {
        return RESOURCES.getMultipleStringProperty("task.form.external.js.libs");
    }

    /**
     * Used from JSP page
     */
    public static String getAdditionalLinks() {
        try {
            String className = RESOURCES.getStringProperty("menu.additional_links");
            if (!Strings.isNullOrEmpty(className)) {
                Class<?> clazz = ClassLoaderUtil.loadClass(className);
                Method getter = clazz.getDeclaredMethod("getAdditionalLinks", (Class[]) null);
                return getter.invoke(clazz, (Object[]) null).toString();
            }
        } catch (Exception e) {
            LogFactory.getLog(WebResources.class).error("Unable to get additional links", e);
        }
        return "";
    }

    public static int getViewLogsLimitLinesCount() {
        return RESOURCES.getIntegerProperty("view.logs.limit.lines.count", 10000);
    }

    public static int getViewLogsAutoReloadTimeout() {
        return RESOURCES.getIntegerProperty("view.logs.timeout.autoreload.seconds", 15);
    }

    public static boolean isDisplayVariablesJavaType() {
        return RESOURCES.getBooleanProperty("process.variables.displayJavaType", true);
    }

    public static boolean isBulkDeploymentElements() {
        return RESOURCES.getBooleanProperty("process.definition.ajax.bulk.deployment.enabled", true);
    }

    public static boolean isLDAPSynchronizationEnabled() {
        return RESOURCES.getBooleanProperty("synchronization.ldap.link.enabled", false);
    }

    public static boolean isLDAPSynchronizationCreate() {
        return RESOURCES.getBooleanProperty("synchronization.ldap.create.executors", false);
    }

    public static boolean isLDAPSynchronizationUpdate() {
        return RESOURCES.getBooleanProperty("synchronization.ldap.update.executors", false);
    }

    public static boolean isLDAPSynchronizationDelete() {
        return RESOURCES.getBooleanProperty("synchronization.ldap.delete.executors", false);
    }

    public static boolean isAjaxFileInputEnabled() {
        return RESOURCES.getBooleanProperty("task.form.ajaxFileInputEnabled", true);
    }

    public static boolean isProcessRemovalEnabled() {
        return RESOURCES.getBooleanProperty("process.removal.enabled", false);
    }

    public static boolean isUpdateProcessSwimlanesEnabled() {
        return RESOURCES.getBooleanProperty("process.swimlane.assignment.enabled", false);
    }

    /**
     * Used from JSP page
     */
    public static boolean isProcessTaskFiltersEnabled() {
        return RESOURCES.getBooleanProperty("process.task.filters.enabled", true);
    }

}
