package ru.runa.wf.web;

import ru.runa.common.web.StrutsMessage;
import ru.runa.wfe.execution.process.check.FrozenProcessesSearchParameter;

public final class MessagesProcesses {

    public static final StrutsMessage BUTTON_CANCEL_PROCESS = new StrutsMessage("button.cancel_process");
    public static final StrutsMessage BUTTON_CANCEL_PROCESSES = new StrutsMessage("button.cancel_processes");
    public static final StrutsMessage BUTTON_ACTIVATE_PROCESSES = new StrutsMessage("button.activate_processes");
    public static final StrutsMessage BUTTON_RESTORE_PROCESS = new StrutsMessage("button.restore_process");
    public static final StrutsMessage TITLE_PROCESS_DEFINITIONS = new StrutsMessage("title.process_definitions");
    public static final StrutsMessage TITLE_DEPLOY_DEFINITION = new StrutsMessage("title.deploy_definition");
    public static final StrutsMessage TITLE_REDEPLOY_DEFINITION = new StrutsMessage("title.redeploy_definition");
    public static final StrutsMessage BUTTON_DEPLOY_DEFINITION = new StrutsMessage("button.deploy_definition");
    public static final StrutsMessage BUTTON_UNDEPLOY_DEFINITION = new StrutsMessage("button.undeploy_definition");
    public static final StrutsMessage TITLE_PROCESS_DEFINITION_HISTORY = new StrutsMessage("title.process_definition_history");
    public static final StrutsMessage LABEL_HISTORY = new StrutsMessage("label.history");
    public static final StrutsMessage TITLE_PROCESS_DEFINITION_FILE_ANNOTATION = new StrutsMessage("title.process_definition_file_annotation");
    public static final StrutsMessage TITLE_PROCESSES = new StrutsMessage("title.processes");
    public static final StrutsMessage TITLE_INSANCE_SWINLANE_LIST = new StrutsMessage("title.process_swimlane_list");
    public static final StrutsMessage TITLE_INSANCE_JOB_LIST = new StrutsMessage("title.process_job_list");
    public static final StrutsMessage TITLE_INSANCE_TASKS_LIST = new StrutsMessage("title.process_tasks_list");
    public static final StrutsMessage TITLE_INSANCE_VARIABLE_LIST = new StrutsMessage("title.process_variable_list");
    public static final StrutsMessage TITLE_PROCESS = new StrutsMessage("title.process");
    public static final StrutsMessage TITLE_PROCESS_DEFINITION = new StrutsMessage("title.process_definition");
    public static final StrutsMessage TITLE_PROCESS_DEFINITION_CHANGES = new StrutsMessage("title.process_definition_changes");
    public static final StrutsMessage TITLE_TASKS = new StrutsMessage("title.tasks");
    public static final StrutsMessage TITLE_OBSERVABLE_TASKS = new StrutsMessage("title.observable.tasks");
    public static final StrutsMessage TITLE_OBSERVABLE_EXECUTORS = new StrutsMessage("title.observable.executors");
    public static final StrutsMessage TITLE_EXECUTOR_TASKS = new StrutsMessage("title.executor_tasks");
    public static final StrutsMessage TITLE_ACTOR_TASKS = new StrutsMessage("title.actor_tasks");
    public static final StrutsMessage TITLE_GROUP_TASKS = new StrutsMessage("title.group_tasks");
    public static final StrutsMessage TITLE_TASK_FORM = new StrutsMessage("title.task_form");
    public static final StrutsMessage TITLE_VIEW_ONLY = new StrutsMessage("title.task_view_only");
    public static final StrutsMessage TITLE_PROCESS_GRAPH = new StrutsMessage("title.process_graph");
    public static final StrutsMessage TITLE_HISTORY = new StrutsMessage("title.history");
    public static final StrutsMessage TITLE_TOKENS = new StrutsMessage("title.tokens");
    public static final StrutsMessage TITLE_MOVE_TOKEN = new StrutsMessage("title.move_token");
    public static final StrutsMessage BUTTON_MOVE = new StrutsMessage("button.move");
    public static final StrutsMessage TITLE_CREATE_TOKEN = new StrutsMessage("title.create_token");
    public static final StrutsMessage LABEL_REDEPLOY_PROCESS_DEFINIION = new StrutsMessage("label.redeploy_process_definition");
    public static final StrutsMessage LABEL_SHOW_DEPLOY_DEFINITION_CONTROLS = new StrutsMessage("label.show_deploy_definition_controls");
    public static final StrutsMessage LABEL_HIDE_DEPLOY_DEFINITION_CONTROLS = new StrutsMessage("label.hide_deploy_definition_controls");
    public static final StrutsMessage PROCESS_STARTED = new StrutsMessage("process.started");
    public static final StrutsMessage PROCESS_ACTIVATE = new StrutsMessage("process.activate");
    public static final StrutsMessage PROCESS_ACTIVATE_FAILED_TOKENS = new StrutsMessage("process.activate.failed.tokens");
    public static final StrutsMessage PROCESS_ACTIVATED = new StrutsMessage("process.activated");
    public static final StrutsMessage FAILED_PROCESSES_ACTIVATED = new StrutsMessage("failed.processes.activated");
    public static final StrutsMessage PROCESS_SUSPEND = new StrutsMessage("process.suspend");
    public static final StrutsMessage PROCESS_SUSPENDED = new StrutsMessage("process.suspended");
    public static final StrutsMessage PROCESS_CANCELED = new StrutsMessage("process.canceled");
    public static final StrutsMessage PROCESSES_CANCELED = new StrutsMessage("processes.canceled");
    public static final StrutsMessage PROCESS_RESTORED = new StrutsMessage("process.restored");
    public static final StrutsMessage PROCESS_REMOVED = new StrutsMessage("process.removed");
    public static final StrutsMessage PROCESS_UPGRADED_TO_DEFINITION_VERSION = new StrutsMessage("process.upgraded.to.definition.version");
    public static final StrutsMessage PROCESS_UPGRADE_TO_DEFINITION_VERSION = new StrutsMessage("process.upgrade.to.definition.version");
    public static final StrutsMessage PROCESSES_UPGRADE_TO_DEFINITION_VERSION = new StrutsMessage("processes.upgrade.to.definition.version");
    public static final StrutsMessage PROCESSES_UPGRADED_TO_DEFINITION_VERSION = new StrutsMessage("processes.upgraded.to.definition.version");
    public static final StrutsMessage LABEL_SHOW_GRAPH_HISTORY = new StrutsMessage("label.manage_graph_history");
    public static final StrutsMessage LABEL_SHOW_GANTT_DIAGRAM = new StrutsMessage("label.show_gantt_diagram");
    public static final StrutsMessage LABEL_DEFINITIONS_ARCHIVE = new StrutsMessage("process_definition.archive");
    public static final StrutsMessage LABEL_DEFINITIONS_DAYS_BEFORE_ARCHIVING = new StrutsMessage("process_definition.days_before_archiving");
    public static final StrutsMessage LABEL_DEPLOY_APPLY_TYPE = new StrutsMessage("batch_presentation.process_definition.application_type");
    public static final StrutsMessage LABEL_DEPLOY_APPLY_NEW = new StrutsMessage("batch_presentation.process_definition.application_type.new.label");
    public static final StrutsMessage LABEL_DEPLOY_APPLY_ALL = new StrutsMessage("batch_presentation.process_definition.application_type.all.label");
    public static final StrutsMessage PROCESS_STARTER_NAME = new StrutsMessage("process_starter.name");
    public static final StrutsMessage LABEL_STATE_NAME = new StrutsMessage("label.state_name");
    public static final StrutsMessage LABEL_PROCESS = new StrutsMessage("label.process");
    public static final StrutsMessage LABEL_PARENT_PROCESS = new StrutsMessage("label.parent_process");
    public static final StrutsMessage LABEL_START_PROCESS = new StrutsMessage("label.start_process");
    public static final StrutsMessage LABEL_SWIMLANE = new StrutsMessage("label.swimlane");
    public static final StrutsMessage LABEL_SWIMLANE_NAME = new StrutsMessage("label.swimlane_name");
    public static final StrutsMessage LABEL_SWIMLANE_ASSIGNMENT = new StrutsMessage("label.swimlane_assigned_to");
    public static final StrutsMessage LABEL_CREATE_TIME = new StrutsMessage("label.create_time");
    public static final StrutsMessage LABEL_END_TIME = new StrutsMessage("label.end_time");
    public static final StrutsMessage LABEL_CURRENT_DURATION = new StrutsMessage("label.current_duration");
    public static final StrutsMessage LABEL_REMAINING_TIME = new StrutsMessage("label.remaining_time");
    public static final StrutsMessage LABEL_ASSIGNMENT_TIME = new StrutsMessage("label.assignment_time");
    public static final StrutsMessage LINK_UPDATE_SWIMLANE = new StrutsMessage("link.update_swimlane");
    public static final StrutsMessage BUTTON_UPDATE_SWIMLANE = new StrutsMessage("button.update_swimlane");
    public static final StrutsMessage LABEL_SWIMLANE_NEW_EXECUTOR = new StrutsMessage("label.swimlane_new_executor");
    public static final StrutsMessage LABEL_NO_SWIMLANES = new StrutsMessage("label.no_swimlanes");
    public static final StrutsMessage LINK_UPDATE_VARIABLE = new StrutsMessage("link.update_variable");
    public static final StrutsMessage BUTTON_UPDATE_VARIABLE = new StrutsMessage("button.update_variable");
    public static final StrutsMessage VARIABLE_WAS_UPDATED = new StrutsMessage("variable.was_updated");
    public static final StrutsMessage VARIABLE_HAS_NOT_CHANGES = new StrutsMessage("variable.has_not_changed");
    public static final StrutsMessage BUTTON_FORM = new StrutsMessage("button.form");
    public static final StrutsMessage BUTTON_COMPLETE = new StrutsMessage("button.complete");
    public static final StrutsMessage BUTTON_ACCEPT_TASK = new StrutsMessage("button.accept_task");
    public static final StrutsMessage BUTTON_DELEGATE_TASK = new StrutsMessage("button.delegate_task");
    public static final StrutsMessage BUTTON_DELEGATE_TASKS = new StrutsMessage("button.delegate_tasks");
    public static final StrutsMessage BUTTON_EXPORT_EXCEL = new StrutsMessage("button.export_excel");
    public static final StrutsMessage BUTTON_ANNOTATE_CHANGES = new StrutsMessage("button.annotate_changes");
    public static final StrutsMessage LABEL_ANNOTATION_CHANGES = new StrutsMessage("label.annotation_changes");
    public static final StrutsMessage TITLE_UPDATE_VARIABLE = new StrutsMessage("title.update_variable");
    public static final StrutsMessage LABEL_VARIABLE_NAME = new StrutsMessage("label.variable_name");
    public static final StrutsMessage LABEL_VARIABLE_VALUE = new StrutsMessage("label.variable_value");
    public static final StrutsMessage LABEL_VARIABLE_TYPE = new StrutsMessage("label.variable_type");
    public static final StrutsMessage LABEL_VARIABLE_SCRIPTING_VALUE = new StrutsMessage("label.variable_script_value");
    public static final StrutsMessage LABEL_VARIABLE = new StrutsMessage("label.variable");
    public static final StrutsMessage LABEL_VARIABLE_NULL_VALUE = new StrutsMessage("label.variable_null_value");
    public static final StrutsMessage LABEL_VARIABLE_OLD_VALUE = new StrutsMessage("label.variable_old_value");
    public static final StrutsMessage LABEL_VARIABLE_NEW_VALUE = new StrutsMessage("label.variable_new_value");
    public static final StrutsMessage LABEL_SEARCH_VARIABLE = new StrutsMessage("label.search_variable");
    public static final StrutsMessage LABEL_NO_VARIABLES = new StrutsMessage("label.no_variables");
    public static final StrutsMessage LABEL_NO_VARIABLES_IN_CHAT = new StrutsMessage("label.no_variables_in_chat");
    public static final StrutsMessage TASK_COMPLETED = new StrutsMessage("task.completed");
    public static final StrutsMessage LABEL_SHOW_TASKS_HISTORY = new StrutsMessage("label.manage_tasks_history");
    public static final StrutsMessage LABEL_TASK_HISTORY_TABLE_NAME = new StrutsMessage("label.task_history_table_name");
    public static final StrutsMessage LABEL_TASK_HISTORY_TABLE_TASK_NAME = new StrutsMessage("label.task_history_table_task_name");
    public static final StrutsMessage LABEL_TASK_HISTORY_TABLE_EXECUTOR = new StrutsMessage("label.task_history_table_executor");
    public static final StrutsMessage LABEL_TASK_HISTORY_TABLE_START_DATE = new StrutsMessage("label.task_history_table_start_date");
    public static final StrutsMessage LABEL_TASK_HISTORY_TABLE_END_DATE = new StrutsMessage("label.task_history_table_end_date");
    public static final StrutsMessage LABEL_TASK_HISTORY_TABLE_DURATION = new StrutsMessage("label.task_history_table_duration");
    public static final StrutsMessage LABEL_UPDATE_CURRENT_VERSION = new StrutsMessage("batch_presentation.process_definition.update_current_version");
    public static final StrutsMessage TITLE_SUBPROCESSES_LIST = new StrutsMessage("title.process_subprocess_list");
    public static final StrutsMessage LINK_SHOW_TASKS = new StrutsMessage("link.show_tasks");
    public static final StrutsMessage LABEL_GLOBAL = new StrutsMessage("label.global");
    public static final StrutsMessage LABEL_CREATE = new StrutsMessage("label.create");
    public static final StrutsMessage LABEL_COLLAPSE = new StrutsMessage("label.collapse");
    public static final StrutsMessage LABEL_EXPAND = new StrutsMessage("label.expand");
    public static final StrutsMessage LABEL_COLLAPSE_ALL = new StrutsMessage("label.collapse.all");
    public static final StrutsMessage LABEL_EXPAND_ALL = new StrutsMessage("label.expand.all");
    public static final StrutsMessage LABEL_JOB_NAME = new StrutsMessage("label.job.name");
    public static final StrutsMessage LABEL_JOB_NODE_ID = new StrutsMessage("label.job.node_id");
    public static final StrutsMessage LABEL_JOB_CREATION_DATE = new StrutsMessage("label.job.creation_date");
    public static final StrutsMessage LABEL_JOB_DUE_DATE_EXPRESSION = new StrutsMessage("label.job.due_date_expression");
    public static final StrutsMessage LABEL_JOB_DUE_DATE = new StrutsMessage("label.job.due_date");
    public static final StrutsMessage LABEL_JOB_UNITS = new StrutsMessage("label.job.units");
    public static final StrutsMessage TITLE_MANAGE_JOB = new StrutsMessage("title.job");
    public static final StrutsMessage BUTTON_SAVE_JOB = new StrutsMessage("button.job.save");
    public static final StrutsMessage LABEL_PROCESS_GRAPH_SHOW_LOGS = new StrutsMessage("label.process_graph.show_logs");
    public static final StrutsMessage LABEL_PROCESS_GRAPH_SHOW_ELEMENT_DETAILS = new StrutsMessage(
            "label.process_graph.show_element_definition_details");
    public static final StrutsMessage LABEL_PROCESS_GRAPH_TOOLTIP_NAME = new StrutsMessage("label.process_graph.tooltip.name");
    public static final StrutsMessage LABEL_PROCESS_GRAPH_TOOLTIP_SWIMLANE = new StrutsMessage("label.process_graph.tooltip.swimlane");
    public static final StrutsMessage LABEL_PROCESS_GRAPH_TOOLTIP_HANDLER = new StrutsMessage("label.process_graph.tooltip.handler");
    public static final StrutsMessage LABEL_PROCESS_GRAPH_TOOLTIP_CONFIGURATION = new StrutsMessage("label.process_graph.tooltip.configuration");
    public static final StrutsMessage LABEL_PROCESS_GRAPH_TOOLTIP_DURATION = new StrutsMessage("label.process_graph.tooltip.duration");
    public static final StrutsMessage LABEL_PROCESS_GRAPH_TOOLTIP_VARIABLE_MAPPING = new StrutsMessage(
            "label.process_graph.tooltip.variable_mapping");
    public static final StrutsMessage LABEL_PROCESS_GRAPH_TOOLTIP_ROUTING_DATA = new StrutsMessage("label.process_graph.tooltip.routing_data");
    public static final StrutsMessage LABEL_PROCESS_GRAPH_TOOLTIP_CONTENT_DATA = new StrutsMessage("label.process_graph.tooltip.content_data");
    public static final StrutsMessage LABEL_PROCESS_GRAPH_TOOLTIP_SUBPROCESS = new StrutsMessage("label.process_graph.tooltip.subprocess");
    public static final StrutsMessage LABEL_PROCESS_GRAPH_TOOLTIP_COMPOSITION = new StrutsMessage("label.process_graph.tooltip.composition");

    public static final StrutsMessage LABEL_SEND_PROCESS_SIGNAL = new StrutsMessage("label.send_process_signal");
    public static final StrutsMessage ROUTING_PARAMETER_NAME = new StrutsMessage("label.routing_parameter_name");
    public static final StrutsMessage ROUTING_PARAMETER_VALUE = new StrutsMessage("label.routing_parameter_value");
    public static final StrutsMessage PAYLOAD_PARAMETER_NAME = new StrutsMessage("label.payload_parameter_name");
    public static final StrutsMessage PAYLOAD_PARAMETER_VALUE = new StrutsMessage("label.payload_parameter_value");
    public static final StrutsMessage PASTE_SIGNAL_DATA_BUTTON_NAME = new StrutsMessage("button.paste_signal_data");

    public static final StrutsMessage BUTTON_VIEW_DIFFERENCES = new StrutsMessage("button.view_differences");
    public static final StrutsMessage FAILED_VIEW_DIFFERENCES = new StrutsMessage("failed.view_differences");
    public static final StrutsMessage LABEL_NO_DIFFERENCES_FOUND = new StrutsMessage("label.no_differences_found");

    public static final StrutsMessage PROCESS_LOG_CLEAN_SUCCESS = new StrutsMessage("process_log_clean.success");
    public static final StrutsMessage PROCESS_LOG_CLEAN_FAIL = new StrutsMessage("process_log_clean.fail");

    public static final StrutsMessage TITLE_CHAT_ROOMS = new StrutsMessage("title.chat_rooms");
    public static final StrutsMessage BUTTON_SEARCH_FROZEN_PROCESSES = new StrutsMessage("button.search_frozen_processes");
    public static final StrutsMessage LABEL_PROCESS_FROZEN_IN_PARALLEL_GATEWAYS = new StrutsMessage(
            FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_PARALLEL_GATEWAYS.getNameLabel());
    public static final StrutsMessage LABEL_PROCESS_FROZEN_HINT_TO_USE_FILTERS = new StrutsMessage("label.process_frozen.hint_to_use_filters");
    public static final StrutsMessage LABEL_PROCESS_FROZEN_IN_UNEXPECTED_NODES = new StrutsMessage(
            FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_UNEXPECTED_NODES.getNameLabel());
    public static final StrutsMessage LABEL_PROCESS_FROZEN_IN_TASK_NODES = new StrutsMessage(
            FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_TASK_NODES.getNameLabel());
    public static final StrutsMessage LABEL_PROCESS_FROZEN_IN_TIMER_NODES = new StrutsMessage(
            FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_TIMER_NODES.getNameLabel());
    public static final StrutsMessage LABEL_PROCESS_FROZEN_BY_SUBPROCESSES = new StrutsMessage(
            FrozenProcessesSearchParameter.SEARCH_FROZEN_BY_SUBPROCESSES.getNameLabel());
    public static final StrutsMessage LABEL_PROCESS_FROZEN_BY_AWAITING_A_SIGNAL = new StrutsMessage(
            FrozenProcessesSearchParameter.SEARCH_FROZEN_BY_SIGNAL_TIME_EXCEEDED.getNameLabel());
    public static final StrutsMessage LABEL_PROCESS_FROZEN_BY_SIGNAL = new StrutsMessage(
            FrozenProcessesSearchParameter.SEARCH_FROZEN_BY_SIGNAL.getNameLabel());
    public static final StrutsMessage LABEL_DAY_DURATION = new StrutsMessage("label.frozen_processes.day_duration");
    public static final StrutsMessage LABEL_NO_FROZEN_PROCESSES = new StrutsMessage("label.no_frozen_processes");
    public static final StrutsMessage FROZEN_PROCESS_NAME = new StrutsMessage("frozen_processes.process_name");
    public static final StrutsMessage FROZEN_PROCESS_ID = new StrutsMessage("frozen_processes.process_id");
    public static final StrutsMessage FROZEN_PROCESS_VERSION = new StrutsMessage("frozen_processes.process_version");
    public static final StrutsMessage FROZEN_PROCESS_NODE_ID = new StrutsMessage("frozen_processes.node_id");
    public static final StrutsMessage FROZEN_PROCESS_NODE_NAME = new StrutsMessage("frozen_processes.node_name");
    public static final StrutsMessage FROZEN_PROCESS_NODE_TYPE = new StrutsMessage("frozen_processes.node_type");
    public static final StrutsMessage FROZEN_PROCESS_NODE_ENTER_DATE = new StrutsMessage("frozen_processes.node_enter_date");
    public static final StrutsMessage FROZEN_PROCESS_CAUSE = new StrutsMessage("frozen_processes.cause");
    public static final StrutsMessage LABEL_LOADED = new StrutsMessage("label.loaded");
    public static final StrutsMessage LABEL_FILE = new StrutsMessage("label.file");
    public static final StrutsMessage LABEL_VERSIONS_LIMIT = new StrutsMessage("label.versions_limit");

}
