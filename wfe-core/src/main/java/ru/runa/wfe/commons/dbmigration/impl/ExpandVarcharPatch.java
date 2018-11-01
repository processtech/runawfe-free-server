package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class ExpandVarcharPatch extends DbMigration {
    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLModifyColumn("BATCH_PRESENTATION", new VarcharColumnDef("CATEGORY", 1024)),
                getDDLModifyColumn("BATCH_PRESENTATION", new VarcharColumnDef("CLASS_TYPE", 1024)),
                getDDLModifyColumn("BATCH_PRESENTATION", new VarcharColumnDef("NAME", 1024)),
                getDDLModifyColumn("BOT", new VarcharColumnDef("PASSWORD", 1024)),
                getDDLModifyColumn("BOT", new VarcharColumnDef("USERNAME", 1024)),
                getDDLModifyColumn("BOT_STATION", new VarcharColumnDef("ADDRESS", 1024)),
                getDDLModifyColumn("BOT_STATION", new VarcharColumnDef("NAME", 1024)),
                getDDLModifyColumn("BOT_TASK", new VarcharColumnDef("NAME", 1024)),
                getDDLModifyColumn("BOT_TASK", new VarcharColumnDef("TASK_HANDLER", 1024)),
                getDDLModifyColumn("BPM_AGGLOG_ASSIGNMENTS", new VarcharColumnDef("NEW_EXECUTOR_NAME", 1024)),
                getDDLModifyColumn("BPM_AGGLOG_ASSIGNMENTS", new VarcharColumnDef("OLD_EXECUTOR_NAME", 1024)),
                getDDLModifyColumn("BPM_AGGLOG_PROCESS", new VarcharColumnDef("CANCEL_ACTOR_NAME", 1024)),
                getDDLModifyColumn("BPM_AGGLOG_PROCESS", new VarcharColumnDef("START_ACTOR_NAME", 1024)),
                getDDLModifyColumn("BPM_AGGLOG_TASKS", new VarcharColumnDef("COMPLETE_ACTOR_NAME", 1024)),
                getDDLModifyColumn("BPM_AGGLOG_TASKS", new VarcharColumnDef("INITIAL_ACTOR_NAME", 1024)),
                getDDLModifyColumn("BPM_AGGLOG_TASKS", new VarcharColumnDef("NODE_ID", 1024)),
                getDDLModifyColumn("BPM_AGGLOG_TASKS", new VarcharColumnDef("SWIMLANE_NAME", 1024)),
                getDDLModifyColumn("BPM_AGGLOG_TASKS", new VarcharColumnDef("TASK_NAME", 1024)),
                getDDLModifyColumn("BPM_JOB", new VarcharColumnDef("NAME", 1024)),
                getDDLModifyColumn("BPM_JOB", new VarcharColumnDef("REPEAT_DURATION", 1024)),
                getDDLModifyColumn("BPM_JOB", new VarcharColumnDef("TRANSITION_NAME", 1024)),
                getDDLModifyColumn("BPM_LOG", new VarcharColumnDef("NODE_ID", 1024)),
                getDDLModifyColumn("BPM_LOG", new VarcharColumnDef("SEVERITY", 1024)),
                getDDLModifyColumn("BPM_PROCESS", new VarcharColumnDef("TREE_PATH", 1024)),
                getDDLModifyColumn("BPM_PROCESS_DEFINITION", new VarcharColumnDef("CATEGORY", 1024)),
                getDDLModifyColumn("BPM_PROCESS_DEFINITION", new VarcharColumnDef("LANGUAGE", 1024)),
                getDDLModifyColumn("BPM_PROCESS_DEFINITION", new VarcharColumnDef("NAME", 1024)),
                getDDLModifyColumn("BPM_SETTING", new VarcharColumnDef("FILE_NAME", 1024)),
                getDDLModifyColumn("BPM_SETTING", new VarcharColumnDef("NAME", 1024)),
                getDDLModifyColumn("BPM_SETTING", new VarcharColumnDef("VALUE", 1024)),
                getDDLModifyColumn("BPM_SUBPROCESS", new VarcharColumnDef("PARENT_NODE_ID", 1024)),
                getDDLModifyColumn("BPM_SWIMLANE", new VarcharColumnDef("NAME", 1024)),
                getDDLModifyColumn("BPM_TASK", new VarcharColumnDef("NAME", 1024)),
                getDDLModifyColumn("BPM_TASK", new VarcharColumnDef("NODE_ID", 1024)),
                getDDLModifyColumn("BPM_TOKEN", new VarcharColumnDef("NAME", 1024)),
                getDDLModifyColumn("BPM_TOKEN", new VarcharColumnDef("NODE_ID", 1024)),
                getDDLModifyColumn("BPM_TOKEN", new VarcharColumnDef("NODE_TYPE", 1024)),
                getDDLModifyColumn("BPM_TOKEN", new VarcharColumnDef("TRANSITION_ID", 1024)),
                getDDLModifyColumn("BPM_VARIABLE", new VarcharColumnDef("NAME", 1024)),
                getDDLModifyColumn("EXECUTOR", new VarcharColumnDef("E_MAIL", 1024)),
                getDDLModifyColumn("EXECUTOR", new VarcharColumnDef("FULL_NAME", 1024)),
                getDDLModifyColumn("EXECUTOR", new VarcharColumnDef("NAME", 1024)),
                getDDLModifyColumn("EXECUTOR", new VarcharColumnDef("NODE_ID", 1024)),
                getDDLModifyColumn("EXECUTOR_RELATION", new VarcharColumnDef("NAME", 1024)),
                getDDLModifyColumn("LOCALIZATION", new VarcharColumnDef("NAME", 1024)),
                getDDLModifyColumn("LOCALIZATION", new VarcharColumnDef("VALUE", 1024)),
                getDDLModifyColumn("PRIVELEGED_MAPPING", new VarcharColumnDef("TYPE", 1024)),
                getDDLModifyColumn("SUBSTITUTION", new VarcharColumnDef("ORG_FUNCTION", 1024)),
                getDDLModifyColumn("SUBSTITUTION_CRITERIA", new VarcharColumnDef("CONF", 1024)),
                getDDLModifyColumn("SUBSTITUTION_CRITERIA", new VarcharColumnDef("NAME", 1024)),
                getDDLModifyColumn("SYSTEM_LOG", new VarcharColumnDef("PROCESS_DEFINITION_NAME", 1024)),
                getDDLModifyColumn("WFE_CONSTANTS", new VarcharColumnDef("NAME", 1024)),
                getDDLModifyColumn("WFE_CONSTANTS", new VarcharColumnDef("VALUE", 1024))
        );
    }
}
