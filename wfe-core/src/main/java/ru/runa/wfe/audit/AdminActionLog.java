package ru.runa.wfe.audit;

public interface AdminActionLog extends ProcessLog {
    String ACTION_UPDATE_VARIABLES = "update_variables";
    String ACTION_UPGRADE_PROCESS_TO_NEXT_VERSION = "upgrade_to_next_version";
    String ACTION_UPGRADE_CURRENT_PROCESS_VERSION = "upgrade_current_process_version";
    String ACTION_UPGRADE_PROCESS_TO_VERSION = "upgrade_to_version";
}
