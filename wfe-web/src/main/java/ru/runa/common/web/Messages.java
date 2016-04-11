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

import javax.servlet.jsp.PageContext;

/**
 * Created 14.05.2005
 *
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
public class Messages {
    public static final String BUTTON_ADD = "button.add";
    public static final String BUTTON_APPLY = "button.apply";
    public static final String BUTTON_CANCEL = "button.cancel";
    public static final String BUTTON_REMOVE = "button.remove";
    public static final String BUTTON_SAVE = "button.save";
    public static final String BUTTON_SAVE_AS = "button.save_as";
    public static final String BUTTON_CREATE = "button.create";
    public static final String BUTTON_UPDATE = "button.update";

    public static final String TITLE_RELATIONS = "title.relations";
    public static final String LINK_CREATE_RELATION = "link.create_relation";
    public static final String LINK_CREATE_RELATION_PAIR = "link.create_relation_pair";
    public static final String LABEL_RELATION_FROM = "label.relation_pair.from";
    public static final String LABEL_RELATION_TO = "label.relation_pair.to";
    public static final String LABEL_RELATION_NAME = "label.relation.name";
    public static final String LABEL_RELATION_DESCRIPTION = "label.relation.description";
    public static final String TITLE_EXECUTOR_RIGHT_RELATIONS = "title.executor.right.relations";
    public static final String TITLE_EXECUTOR_LEFT_RELATIONS = "title.executor.left.relations";
    public static final String TITLE_RELATION_DETAILS = "title.relation.details";
    public static final String TITLE_CREATE_RELATION_PAIR = "title.create_relation_pair";

    public static final String LABEL_SETTING_TITLE = "label.setting_title";
    public static final String LABEL_SETTING_DESCRIPTION = "label.setting_description";
    public static final String LABEL_SETTING_VALUE = "label.setting_value";

    public static final String MESSAGE_RELATION_GROUP_EXISTS = "label.relation.exists";
    public static final String MESSAGE_RELATION_GROUP_DOESNOT_EXISTS = "label.relation.not_exists";

    public static final String TITLE_EXECUTORS = "title.executors";
    public static final String TITLE_PERMISSION_OWNERS = "title.permission_owners";
    public static final String TITLE_SUBSTITUTION_CRITERIA = "title.substitution_criteria";
    public static final String TITLE_ADD_EXECUTORS_TO_GROUP = "title.add_executors_to_group";
    public static final String TITLE_ADD_EXECUTOR_TO_GROUP = "title.add_executor_to_groups";
    public static final String TITLE_EXECUTOR_DETAILS = "title.executor_details";
    public static final String TITLE_EXECUTOR_GROUPS = "title.executor_groups";
    public static final String TITLE_GROUP_MEMBERS = "title.group_members";
    public static final String TITLE_ACTOR_PASSWORD = "title.actor_password";
    public static final String TITLE_ACTOR_STATUS = "title.actor_status";
    public static final String TITLE_GRANT_PERMISSION = "title.grant_permission";

    public static final String TITLE_PROCESS_DEFINITIONS = "title.process_definitions";
    public static final String TITLE_DEPLOY_DEFINITION = "title.deploy_definition";
    public static final String TITLE_REDEPLOY_DEFINITION = "title.redeploy_definition";
    public static final String BUTTON_DEPLOY_DEFINITION = "button.deploy_definition";
    public static final String TITLE_DEFINITIONS_HISTORY = "title.definitions_history";

    public static final String TITLE_PROCESSES = "title.processes";
    public static final String TITLE_INSANCE_SWINLANE_LIST = "title.process_swimlane_list";
    public static final String TITLE_INSANCE_TASKS_LIST = "title.process_tasks_list";
    public static final String TITLE_INSANCE_VARIABLE_LIST = "title.process_variable_list";
    public static final String LINK_UPDATE_VARIABLE = "link.update_variable";
    public static final String BUTTON_UPDATE_VARIABLE = "button.update_variable";
    public static final String TITLE_PROCESS = "title.process";
    public static final String TITLE_PROCESS_DEFINITION = "title.process_definition";
    public static final String TITLE_TASKS = "title.tasks";
    public static final String TITLE_PROCESS_GRAPH = "title.process_graph";

    public static final String TITLE_HISTORY = "title.history";
    public static final String TITLE_SYSTEM_HISTORY = "title.system.history";

    public static final String MANAGE_SETTINGS = "manage_settings";

    public static final String BUTTON_CREATE_ACTOR = "button.create_actor";
    public static final String BUTTON_CREATE_GROUP = "button.create_group";

    public static final String BUTTON_FORM = "button.form";
    public static final String BUTTON_COMPLETE = "button.complete";
    public static final String BUTTON_LOGOUT = "button.logout";

    public static final String BUTTON_CANCEL_PROCESS = "button.cancel_process";
    public static final String BUTTON_UNDEPLOY_DEFINITION = "button.undeploy_definition";

    public static final String BUTTON_ACCEPT_TASK = "button.accept_task";
    public static final String BUTTON_DELEGATE_TASK = "button.delegate_task";

    public static final String BUTTON_USE_DAFAULT_PROPERTIES = "button.use.default.properties";

    public static final String LABEL_PASSWORD = "label.password";
    public static final String LABEL_PASSWORD_CONFIRM = "label.password_confirm";

    public static final String LABEL_EXECUTOR_NAME = "label.executor_name";
    public static final String LABEL_EXECUTOR_DESCRIPTION = "label.executor_description";
    public static final String LABEL_ACTOR_FULL_NAME = "label.actor_fullname";
    public static final String LABEL_ACTOR_CODE = "label.actor_code";
    public static final String LABEL_ACTOR_IS_ACTIVE = "label.actor_is_active";
    public static final String LABEL_ACTOR_EMAIL = "label.actor_email";
    public static final String LABEL_ACTOR_PHONE = "label.actor_phone";
    public static final String LABEL_GROUP_AD = "label.group_ad";

    public static final String DYNAMIC_GROUP_NAME = "dynamic_group.name";
    public static final String ESCALATION_GROUP_NAME = "escalation_group.name";
    public static final String PROCESS_STARTER_NAME = "process_starter.name";

    public static final String LABEL_START_PROCESS = "label.start_process";
    public static final String LABEL_PROPERTIES = "label.properties";

    public static final String LABEL_REDEPLOY_PROCESS_DEFINIION = "label.redeploy_process_definition";

    public static final String LABEL_VIEW_SIZE = "label.view_size";
    public static final String LABEL_FIELD_NAMES = "label.field_name";
    public static final String LABEL_DISPLAY_POSITION = "label.display_position";
    public static final String LABEL_SORTING_TYPE = "label.sorting_type";
    public static final String LABEL_SORTING_POSITION = "label.sorting_position";
    public static final String LABEL_FILTER_CRITERIA = "label.filter_criteria";
    public static final String LABEL_GROUPING = "label.grouping";
    public static final String LABEL_NONE = "label.none";
    public static final String LABEL_ASC = "label.asc";
    public static final String LABEL_DESC = "label.desc";

    public static final String LABEL_TOTAL = "label.total";
    public static final String LABEL_PAGING_NEXT_PAGE = "label.paging_next_page";
    public static final String LABEL_PAGING_PREV_PAGE = "label.paging_prev_page";
    public static final String LABEL_PAGING_PREV_RANGE = "label.paging_prev_range";

    public static final String LABEL_SWIMLANE = "label.swimlane";
    public static final String LABEL_SWIMLANE_NAME = "label.swimlane_name";
    public static final String LABEL_SWIMLANE_ASSIGNMENT = "label.swimlane_assigned_to";
    public static final String LABEL_SWIMLANE_ORGFUNCTION = "label.swimlane_organization_function";
    public static final String SUBSTITUTION_ALWAYS = "substitution.always";
    public static final String SUBSTITUTION_OUT_OF_DATE = "substitution.out.of.date.error";

    public static final String LABEL_STATE_NAME = "label.state_name";
    public static final String LABEL_PARENT_PROCESS = "label.parent_process";

    public static final String TITLE_UPDATE_VARIABLE = "title.update_variable";
    public static final String LABEL_VARIABLE_NAME = "label.variable_name";
    public static final String LABEL_VARIABLE_VALUE = "label.variable_value";
    public static final String LABEL_VARIABLE_TYPE = "label.variable_type";
    public static final String LABEL_VARIABLE_SCRIPTING_VALUE = "label.variable_script_value";
    public static final String LABEL_VARIABLE = "label.variable";
    public static final String LABEL_VARIABLE_NULL_VALUE = "label.variable_null_value";
    public static final String LABEL_VARIABLE_OLD_VALUE = "label.variable_old_value";
    public static final String LABEL_VARIABLE_NEW_VALUE = "label.variable_new_value";
    public static final String LABEL_NO_VARIABLES = "label.no_variables";
    public static final String LABEL_SHOW_CONTROLS = "label.show_controls";
    public static final String LABEL_HIDE_CONTROLS = "label.hide_controls";

    public static final String LABEL_SHOW_DEPLOY_DEFINITION_CONTROLS = "label.show_deploy_definition_controls";
    public static final String LABEL_HIDE_DEPLOY_DEFINITION_CONTROLS = "label.hide_deploy_definition_controls";

    public static final String LABEL_SUBSTITUTORS = "label.substitutors";
    public static final String LABEL_SUBSTITUTORS_CRITERIA = "label.substitutors_criteria";
    public static final String LABEL_SUBSTITUTORS_ENABLED = "label.substitutors_enabled";

    public static final String LABEL_SUBSTITUTION_CRITERIA_NAME = "label.substitution_criteria_name";
    public static final String LABEL_SUBSTITUTION_CRITERIA_TYPE = "label.substitution_criteria_type";
    public static final String LABEL_SUBSTITUTION_CRITERIA_CONF = "label.substitution_criteria_conf";

    public static final String EXCEPTION_UNKNOWN = "unknown.exception";

    public static final String EXCEPTION_SESSION_INVALID = "session.invalid";

    public static final String EXCEPTION_TABLE_VIEW_SETUP_FORMAT_INCORRECT = "view.setup.format.invalid";

    public static final String EXCEPTION_AUTHORIZATION = "authorization.exception";
    public static final String EXCEPTION_AUTHENTICATION = "authentication.exception";

    public static final String EXCEPTION_PASSWORD_IS_WEAK = "executor.weak.password";
    public static final String EXCEPTION_EXECUTOR_ALREADY_EXISTS = "executor.already.exists.exception";
    public static final String EXCEPTION_EXECUTOR_DOES_NOT_EXISTS = "executor.does.not.exists.exception";
    public static final String EXCEPTION_ACTOR_DOES_NOT_EXISTS = "ru.runa.wf.web.actor.does.not.exists.exception";
    public static final String EXCEPTION_GROUP_DOES_NOT_EXISTS = "ru.runa.wf.web.group.does.not.exists.exception";
    public static final String EXCEPTION_EXECUTOR_PARTICIPATES_IN_PROCESSES = "executor.participates.in.processes";

    public static final String ERROR_NULL_VALUE = "emptyvalue";
    public static final String ERROR_FILL_REQUIRED_VALUES = "error.fill.required.values";
    public static final String ERROR_VALIDATION = "validation.error";
    public static final String ERROR_PASSWORDS_NOT_MATCH = "executor.passwords.not.match";

    public static final String ERROR_DEFINITION_ALREADY_EXISTS = "definition.already.exists.error";

    public static final String ERROR_DEFINITION_DOES_NOT_EXIST = "definition.does.not.exist.error";
    public static final String ERROR_DEFINITION_NAME_MISMATCH = "definition.name.mismatch.error";
    public static final String ERROR_PROCESS_DOES_NOT_EXIST = "process.does.not.exist.error";
    public static final String ERROR_TASK_DOES_NOT_EXIST = "task.does.not.exist.error";

    public static final String DEFINITION_ARCHIVE_FORMAT_ERROR = "definition.archive.format.error";
    public static final String DEFINITION_FILE_FORMAT_ERROR = "definition.file.format.error";
    public static final String DEFINITION_FILE_DOES_NOT_EXIST_ERROR = "definition.file.does.not.exist.error";
    public static final String EXCEPTION_DEFINITION_TYPE_NOT_PRESENT = "definition.type.not.present";

    public static final String TASK_COMPLETED = "task.completed";
    public static final String PROCESS_STARTED = "process.started";
    public static final String PROCESS_CANCELED = "process.canceled";
    public static final String PROCESS_REMOVED = "process.removed";
    public static final String PROCESS_UPGRADED_TO_DEFINITION_VERSION = "process.upgraded.to.definition.version";
    public static final String PROCESS_UPGRADE_TO_DEFINITION_VERSION = "process.upgrade.to.definition.version";

    public static final String MESSAGE_VARIABLE_FORMAT_ERROR = "variable.format.error";
    public static final String MESSAGE_VALIDATION_ERROR = "validation.form.error";

    public static final String TASK_WAS_ALREADY_ACCEPTED = "task.was.already.accepted";
    public static final String TASK_WAS_ALREADY_COMPLETED = "task.was.already.completed";

    public static final String PROCESS_HAS_SUPER_PROCESS = "process.has.super.process";

    public static final String BUTTON_BOT_STATION_CONFIGURE_PERMISSION = "button.bot_station_configure";
    public static final String BUTTON_DELETE_BOT_STATION = "button.delete_bot_station";
    public static final String BUTTON_ADD_BOT_STATION = "button.add_bot_station";
    public static final String BUTTON_ADD_BOT = "button.add_bot";
    public static final String BUTTON_SAVE_BOT = "button.save_bot";
    public static final String BUTTON_DEPLOY_BOT = "button.deploy_bot";
    public static final String LABEL_REPLACE_BOT_TASKS = "label.replace_bot_tasks";
    public static final String BUTTON_SAVE_BOT_STATION = "button.save_bot_station";
    public static final String BUTTON_DEPLOY_BOT_STATION = "button.deploy_bot_station";
    public static final String BUTTON_DELETE_BOT = "button.delete_bot";
    public static final String TITLE_BOT_STATIONS = "title.bot_stations";
    public static final String TITLE_BOT_LIST = "title.bot_list";
    public static final String TITLE_BOT_TASK_LIST = "title.bot_task_list";
    public static final String TITLE_ADD_BOT_STATION = "title.add_bot_station";
    public static final String TITLE_ADD_BOT = "title.add_bot";
    public static final String LABEL_BOT_STATION_NAME = "label.bot_station_name";
    public static final String LABEL_BOT_STATION_ADDRESS = "label.bot_station_address";
    public static final String LABEL_BOT_PASSWORD = "label.bot_password";
    public static final String LABEL_BOT_NAME = "label.bot_name";
    public static final String LABEL_BOT_SEQUENTIAL = "label.bot_sequential";
    // public static final String LABEL_BOT_TIMEOUT = "label.bot_timeout";
    public static final String LABEL_BOT_TASK_DETAILS = "label.bot_task_details";
    public static final String LABEL_BOT_TASK_NAME = "label.bot_task_name";
    public static final String LABEL_BOT_TASK_HANDLER = "label.bot_task_handler";
    public static final String LABEL_BOT_TASK_CONFIG = "label.bot_task_config";
    public static final String LABEL_BOT_TASK_SEQUENTIAL = "label.bot_task_sequential";
    public static final String TITLE_BOT_STATION_DETAILS = "title.bot_station_details";
    public static final String TITLE_BOT_STATION_STATUS = "title.bot_station_status";
    public static final String TITLE_BOT_DETAILS = "title.bot_details";
    public static final String LABEL_UNKNOWN_BOT_HANDLER = "label.unknown_bot_handler";
    public static final String LABEL_BOT_TASK_CONFIG_DOWNLOAD = "label.bot_task_config.download";
    public static final String LABEL_BOT_TASK_CONFIG_EDIT = "label.bot_task_config.edit";
    public static final String MESSAGE_BOTSTATION_ON = "button.botstation_on";
    public static final String MESSAGE_BOTSTATION_OFF = "button.botstation_off";
    public static final String MESSAGE_PERIODIC_BOTS_INVOCATION_ON = "button.periodic_bots_invocation_on";
    public static final String MESSAGE_PERIODIC_BOTS_INVOCATION_OFF = "button.periodic_bots_invocation_off";
    public static final String BUTTON_STOP_PERIODIC_BOTS_INVOCATION = "button.stop_periodic_bots_invocation";
    public static final String BUTTON_START_PERIODIC_BOTS_INVOCATION = "button.start_periodic_bots_invocation";

    public static final String LABEL_SHOW_GRAPH_HISTORY = "label.manage_graph_history";

    public static final String SYSTEM_LOG_PROCESS_DELETED = "system.log.process.delete";
    public static final String SYSTEM_LOG_DEFINITION_DELETED = "system.log.definition.delete";
    public static final String SYSTEM_LOG_UNDEFINED_TYPE = "history.system.type.undefined";
    public static final String HISTORY_SYSTEM_PH_PI = "history.system.placeholders.process";
    public static final String HISTORY_SYSTEM_PH_PD = "history.system.placeholders.process_definition";
    public static final String HISTORY_SYSTEM_PH_VERSION = "history.system.placeholders.version";

    public static final String LABEL_SHOW_HISTORY = "label.manage_history";
    public static final String LABEL_HISTORY_DATE = "label.history.date";
    public static final String LABEL_HISTORY_EVENT = "label.history.event";

    public static final String CONF_POPUP_BUTTON_OK = "confirmpopup.button.ok";
    public static final String CONF_POPUP_BUTTON_CANCEL = "confirmpopup.button.cancel";
    public static final String CONF_POPUP_CONFIRM_ACTION = "confirmpopup.confirm.action";
    public static final String CONF_POPUP_REMOVE_EXECUTORS = "confirmpopup.remove.executors";
    public static final String CONF_POPUP_REMOVE_EXECUTORS_FROM_GROUPS = "confirmpopup.remove.executorsfromgroups";
    public static final String CONF_POPUP_REMOVE_BOT = "confirmpopup.remove.bot";
    public static final String CONF_POPUP_REMOVE_BOT_STATION = "confirmpopup.remove.botstation";
    public static final String CONF_POPUP_DEPLOY_PROCESSDEFINIION = "confirmpopup.deploy.processdefinition";
    public static final String CONF_POPUP_REDEPLOY_PROCESSDEFINIION = "confirmpopup.redeploy.processdefinition";
    public static final String CONF_POPUP_UNDEPLOY_PROCESSDEFINIION = "confirmpopup.undeploy.processdefinition";
    public static final String CONF_POPUP_REMOVE_SUBSTITUTION_CRITERIA = "confirmpopup.remove.substitutioncriteria";
    public static final String CONF_POPUP_CANCEL_PROCESS = "confirmpopup.cancel.process";
    public static final String CONF_POPUP_REMOVE_PROCESS = "confirmpopup.remove.process";
    public static final String CONF_POPUP_ACCEPT_TASK = "confirmpopup.accept.task";
    public static final String CONF_POPUP_EXECUTE_TASK = "confirmpopup.execute.task";
    public static final String CONF_POPUP_START_PROCESS = "confirmpopup.start.process";
    public static final String CONF_POPUP_SUBSTITUTION_CRITERIA_BUTTON_ALL = "confirmpopup.substitutioncriteria.button.all";
    public static final String CONF_POPUP_SUBSTITUTION_CRITERIA_BUTTON_ONLY = "confirmpopup.substitutioncriteria.button.only";
    public static final String CONF_POPUP_USE_DEFAULT_PROPERTIES = "confirmpopup.use.default.properties";

    public static final String LABEL_SUBSTITUTION_CRITERIA_USED_BY = "label.substitutioncriteria.usedby";

    public static final String LABEL_SHOW_TASKS_HISTORY = "label.manage_tasks_history";
    public static final String LABEL_TASK_HISTORY_TABLE_NAME = "label.task_history_table_name";
    public static final String LABEL_TASK_HISTORY_TABLE_TASK_NAME = "label.task_history_table_task_name";
    public static final String LABEL_TASK_HISTORY_TABLE_EXECUTOR = "label.task_history_table_executor";
    public static final String LABEL_TASK_HISTORY_TABLE_START_DATE = "label.task_history_table_start_date";
    public static final String LABEL_TASK_HISTORY_TABLE_END_DATE = "label.task_history_table_end_date";
    public static final String LABEL_TASK_HISTORY_TABLE_DURATION = "label.task_history_table_duration";

    public static final String LABEL_SHOW_GANTT_DIAGRAM = "label.show_gantt_diagram";

    public static final String TITLE_IMPORT_DATAFILE = "managesystem.datafile.import.title";
    public static final String TITLE_EXPORT_DATAFILE = "managesystem.datafile.export.title";
    public static final String EXCEPTION_DATAFILE_NOT_PRESENT = "managesystem.datafile.not.present";

    public static final String LINK_DROP_SETTINGS = "link.drop_settings";

    public static final String EXECUTOR_STATE_DONT_UPDATE = "executor.state.dont.update";
    public static final String IMPORT_DATA_SUCCESS = "import.data.success";

    public static final String ERROR_BLOCKED_FILE = "error.blocked.file";

    private Messages() {
    }

    public static String getMessage(String key, PageContext pageContext) {
        String value = Commons.getMessage(key, pageContext);
        if (value == null) {
            value = '!' + key + '!';
        }
        return value;
    }
}
