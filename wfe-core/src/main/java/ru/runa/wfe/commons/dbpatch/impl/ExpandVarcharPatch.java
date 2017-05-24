package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import ru.runa.wfe.commons.dbpatch.DBPatch;

public class ExpandVarcharPatch extends DBPatch {
    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.add(getDDLModifyColumn("BATCH_PRESENTATION", "CATEGORY", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BATCH_PRESENTATION", "CLASS_TYPE", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BATCH_PRESENTATION", "NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BOT", "PASSWORD", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BOT", "USERNAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BOT_STATION", "ADDRESS", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BOT_STATION", "NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BOT_TASK", "NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BOT_TASK", "TASK_HANDLER", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_AGGLOG_ASSIGNMENTS", "NEW_EXECUTOR_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_AGGLOG_ASSIGNMENTS", "OLD_EXECUTOR_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_AGGLOG_PROCESS", "CANCEL_ACTOR_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_AGGLOG_PROCESS", "START_ACTOR_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_AGGLOG_TASKS", "COMPLETE_ACTOR_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_AGGLOG_TASKS", "INITIAL_ACTOR_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_AGGLOG_TASKS", "NODE_ID", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_AGGLOG_TASKS", "SWIMLANE_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_AGGLOG_TASKS", "TASK_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_JOB", "NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_JOB", "REPEAT_DURATION", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_JOB", "TRANSITION_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_LOG", "NODE_ID", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_LOG", "SEVERITY", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_PROCESS", "TREE_PATH", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_PROCESS_DEFINITION", "CATEGORY", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_PROCESS_DEFINITION", "LANGUAGE", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_PROCESS_DEFINITION", "NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_SETTING", "FILE_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_SETTING", "NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_SETTING", "VALUE", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_SUBPROCESS", "PARENT_NODE_ID", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_SWIMLANE", "NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_TASK", "NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_TASK", "NODE_ID", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_TOKEN", "NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_TOKEN", "NODE_ID", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_TOKEN", "NODE_TYPE", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_TOKEN", "TRANSITION_ID", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("BPM_VARIABLE", "NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("EXECUTOR", "E_MAIL", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("EXECUTOR", "FULL_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("EXECUTOR", "NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("EXECUTOR", "NODE_ID", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("EXECUTOR_RELATION", "NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("LOCALIZATION", "NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("LOCALIZATION", "VALUE", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("PRIVELEGED_MAPPING", "TYPE", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("SUBSTITUTION", "ORG_FUNCTION", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("SUBSTITUTION_CRITERIA", "CONF", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("SUBSTITUTION_CRITERIA", "NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("SYSTEM_LOG", "PROCESS_DEFINITION_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("WFE_CONSTANTS", "NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        sql.add(getDDLModifyColumn("WFE_CONSTANTS", "VALUE", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)));
        return sql;
    }

}
