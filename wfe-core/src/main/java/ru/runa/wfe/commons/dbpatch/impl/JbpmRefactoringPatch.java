package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Blob;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.DBType;
import ru.runa.wfe.commons.PropertyResources;
import ru.runa.wfe.commons.dbpatch.DBPatch;
import ru.runa.wfe.definition.Language;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.dao.PermissionDAO;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.dao.ExecutorDAO;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;

/**
 * Increase TransactionTimeout in jboss-service.xml in case of timed out
 * rollback.
 * 
 * What is missed: process logs; batch presentations
 * 
 * @author dofs
 * @since 4.0
 */
public class JbpmRefactoringPatch extends DBPatch {
    private boolean jbpmIdTablesExist;
    private boolean jbpmCommentTableExists;
    private static final PropertyResources RESOURCES = new PropertyResources("JbpmRefactoringPatch.properties", false);
    private static final boolean handleManualIndexes = !RESOURCES.getBooleanProperty("skipPatchV21Indexes", true);

    @Autowired
    private PermissionDAO permissionDAO;
    @Autowired
    private ExecutorDAO executorDAO;

    private String getObjectName(String name) {
        return RESOURCES.getStringProperty(name, name);
    }

    @Override
    protected List<String> getDDLQueriesBefore() {
        if (dbType != DBType.MSSQL) {
            throw new InternalApplicationException("Database migration patch from RunaWFE 3.x to 4.x is currently supported only for MS SQL Server");
        }
        System.out.println("handleManualIndexes = " + handleManualIndexes);
        // "MySQL DB update to version RunaWFE4.x is not supported because of mass column (which are foreign keys) renames [Error on rename of (errno: 150)]"
        List<String> sql = super.getDDLQueriesBefore();
        // removed unused tables and columns
        sql.add(getDDLRemoveTable("PROPERTY_IDS"));
        sql.add(getDDLRemoveTable("JBPM_TASKACTORPOOL"));
        sql.add(getDDLRemoveTable("JBPM_POOLEDACTOR"));

        // refactored batch presentations;
        sql.add(getDDLRemoveTable("ACTIVE_BATCH_PRESENTATIONS"));
        sql.add(getDDLRemoveTable("BP_DYNAMIC_FIELDS"));
        sql.add(getDDLRemoveTable("CRITERIA_CONDITIONS"));
        sql.add(getDDLRemoveTable("DISPLAY_FIELDS"));
        sql.add(getDDLRemoveTable("FILTER_CRITERIAS"));
        sql.add(getDDLRemoveTable("GROUP_FIELDS"));
        sql.add(getDDLRemoveTable("GROUP_FIELDS_EXPANDED"));
        sql.add(getDDLRemoveTable("SORTED_FIELDS"));
        sql.add(getDDLRemoveTable("SORTING_MODES"));
        sql.add(getDDLTruncateTable("BATCH_PRESENTATIONS"));
        sql.add(getDDLTruncateTableUsingDelete("PROFILES"));

        // ru.runa.wfe.user.Profile
        sql.add(getDDLRenameTable("PROFILES", "PROFILE"));
        sql.add(getDDLCreateForeignKey("PROFILE", "FK_PROFILE_ACTOR", "ACTOR_ID", "EXECUTORS", "ID"));
        // ru.runa.wfe.presentation.BatchPresentation
        sql.add(getDDLRenameTable("BATCH_PRESENTATIONS", "BATCH_PRESENTATION"));
        sql.add(getDDLRenameColumn("BATCH_PRESENTATION", "PRESENTATION_NAME", new ColumnDef("NAME", Types.VARCHAR)));
        sql.add(getDDLRenameColumn("BATCH_PRESENTATION", "PRESENTATION_ID", new ColumnDef("CATEGORY", Types.VARCHAR)));
        sql.add(getDDLCreateColumn("BATCH_PRESENTATION", new ColumnDef("IS_ACTIVE", Types.TINYINT)));
        sql.add(getDDLCreateColumn("BATCH_PRESENTATION", new ColumnDef("FIELDS", Types.VARBINARY)));
        sql.add(getDDLCreateColumn("BATCH_PRESENTATION", new ColumnDef("CLASS_TYPE", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))));
        sql.add(getDDLRemoveIndex("BATCH_PRESENTATION", "PRESENTATION_NAME_ID_IDX"));
        sql.add(getDDLCreateIndex("BATCH_PRESENTATION", "IX_BATCH_PRESENTATION_PROFILE", "PROFILE_ID"));
        sql.add(getDDLRemoveColumn("BATCH_PRESENTATION", "CLASS_PRESENTATION_ID"));
        // ru.runa.wfe.user.dao.ActorPassword
        sql.add(getDDLRenameTable("PASSWORDS", "ACTOR_PASSWORD"));
        sql.add(getDDLRenameColumn("ACTOR_PASSWORD", "PASSWD", new ColumnDef("PASSWORD", Types.VARBINARY)));
        // ru.runa.wfe.bot.BotStation
        sql.add(getDDLRenameTable("BOT_STATIONS", "BOT_STATION"));
        sql.add(getDDLRemoveColumn("BOT_STATION", "BS_USER"));
        sql.add(getDDLRemoveColumn("BOT_STATION", "BS_PASS"));
        // ru.runa.wfe.bot.Bot
        sql.add(getDDLRenameTable("BOTS", "BOT"));
        sql.add(getDDLRemoveColumn("BOT", "MAX_PERIOD"));
        sql.add(getDDLRenameColumn("BOT", "LAST_INVOKED", new ColumnDef("START_TIMEOUT", Types.VARCHAR)));
        sql.add(getDDLRenameColumn("BOT", "WFE_USER", new ColumnDef("USERNAME", Types.VARCHAR)));
        sql.add(getDDLRenameColumn("BOT", "WFE_PASS", new ColumnDef("PASSWORD", Types.VARCHAR)));
        sql.add(getDDLCreateColumn("BOT", new ColumnDef("VERSION", Types.BIGINT)));
        sql.add(getDDLRenameIndex("BOT", getObjectName("B_BS_IDX"), "IX_BOT_STATION"));
        // ru.runa.wfe.bot.BotTask
        sql.add(getDDLRenameTable("BOT_TASKS", "BOT_TASK"));
        sql.add(getDDLRemoveColumn("BOT_TASK", "CONFIG"));
        sql.add(getDDLRenameColumn("BOT_TASK", "CLAZZ", new ColumnDef("TASK_HANDLER", Types.VARCHAR)));
        sql.add(getDDLRenameIndex("BOT_TASK", getObjectName("BT_B_IDX"), "IX_BOT_TASK_BOT"));
        sql.add(getDDLCreateColumn("BOT_TASK", new ColumnDef("VERSION", Types.BIGINT)));
        // ru.runa.wfe.user.Executor
        sql.add(getDDLRenameTable("EXECUTORS", "EXECUTOR"));
        sql.add(getDDLRenameColumn("EXECUTOR", "IS_GROUP", new ColumnDef("DISCRIMINATOR", Types.VARCHAR)));
        sql.add(getDDLCreateColumn("EXECUTOR", new ColumnDef("ESCALATION_LEVEL", Types.INTEGER)));
        sql.add(getDDLCreateColumn("EXECUTOR", new ColumnDef("ESCALATION_EXECUTOR_ID", Types.BIGINT)));
        sql.add(getDDLCreateForeignKey("EXECUTOR", "FK_GROUP_ESCALATION_EXECUTOR", "ESCALATION_EXECUTOR_ID", "EXECUTOR", "ID"));
        // ru.runa.wfe.user.Actor
        sql.add(getDDLRenameIndex("EXECUTOR", getObjectName("EXECUTORS_CODE_IDX"), "IX_EXECUTOR_CODE"));
        // ru.runa.wfe.user.ExecutorGroupMembership
        sql.add(getDDLRenameTable("EXECUTOR_GROUP_RELATIONS", "EXECUTOR_GROUP_MEMBER"));
        sql.add(getDDLRenameIndex("EXECUTOR_GROUP_MEMBER", getObjectName("EXEC_GROUP_REL_EXEC_ID_IDX"), "IX_MEMBER_EXECUTOR"));
        sql.add(getDDLRenameIndex("EXECUTOR_GROUP_MEMBER", getObjectName("EXEC_GROUP_REL_GROUP_ID_IDX"), "IX_MEMBER_GROUP"));
        // ru.runa.wfe.relation.Relation
        sql.add(getDDLRenameTable("RELATION_GROUPS", "EXECUTOR_RELATION"));
        // ru.runa.wfe.relation.RelationPair
        sql.add(getDDLRenameTable("EXECUTOR_RELATIONS", "EXECUTOR_RELATION_PAIR"));
        sql.add(getDDLRenameForeignKey(getObjectName("FK_RELATION_FROM_EXECUTOR"), "FK_ERP_EXECUTOR_FROM"));
        sql.add(getDDLRenameForeignKey(getObjectName("FK_RELATION_TO_EXECUTOR"), "FK_ERP_EXECUTOR_TO"));
        sql.add(getDDLRenameForeignKey(getObjectName("FK_RELATION_GROUP_ID"), "FK_ERP_RELATION"));
        sql.add(getDDLRenameColumn("EXECUTOR_RELATION_PAIR", "RELATION_GROUP", new ColumnDef("RELATION_ID", Types.BIGINT)));
        sql.add(getDDLRenameIndex("EXECUTOR_RELATION_PAIR", getObjectName("IDX_RELATION_FROM_EXECUTOR"), "IX_ERP_EXECUTOR_FROM"));
        sql.add(getDDLRenameIndex("EXECUTOR_RELATION_PAIR", getObjectName("IDX_RELATION_GROUP_ID"), "IX_ERP_RELATION"));
        sql.add(getDDLRenameIndex("EXECUTOR_RELATION_PAIR", getObjectName("IDX_RELATION_TO_EXECUTOR"), "IX_ERP_EXECUTOR_TO"));
        // ru.runa.wfe.commons.dao.Localization
        List<ColumnDef> lColumns = Lists.newArrayList();
        lColumns.add(new ColumnDef("ID", Types.BIGINT, false).setPrimaryKey());
        lColumns.add(new ColumnDef("NAME", dialect.getTypeName(Types.VARCHAR, 255, 255, 255)));
        lColumns.add(new ColumnDef("VALUE", dialect.getTypeName(Types.VARCHAR, 255, 255, 255)));
        sql.add(getDDLCreateTable("LOCALIZATION", lColumns, null));
        // ru.runa.wfe.security.dao.PrivelegedMapping
        sql.add(getDDLRemoveTable("PRIVELEGE_MAPPINGS"));
        List<ColumnDef> privColumns = Lists.newArrayList();
        privColumns.add(new ColumnDef("ID", Types.BIGINT, false).setPrimaryKey());
        privColumns.add(new ColumnDef("TYPE", dialect.getTypeName(Types.VARCHAR, 255, 255, 255), false));
        privColumns.add(new ColumnDef("EXECUTOR_ID", Types.BIGINT, false));
        sql.add(getDDLCreateTable("PRIVELEGED_MAPPING", privColumns, null));
        sql.add(getDDLCreateForeignKey("PRIVELEGED_MAPPING", "FK_PM_EXECUTOR", "EXECUTOR_ID", "EXECUTOR", "ID"));
        sql.add(getDDLCreateIndex("PRIVELEGED_MAPPING", "IX_PRIVELEGE_TYPE", "TYPE"));
        List<ColumnDef> permColumns = Lists.newArrayList();
        permColumns.add(new ColumnDef("ID", Types.BIGINT, false).setPrimaryKey());
        permColumns.add(new ColumnDef("TYPE", dialect.getTypeName(Types.VARCHAR, 255, 255, 255), false));
        permColumns.add(new ColumnDef("VERSION", Types.BIGINT, false));
        permColumns.add(new ColumnDef("MASK", Types.BIGINT, false));
        permColumns.add(new ColumnDef("IDENTIFIABLE_ID", Types.BIGINT, false));
        permColumns.add(new ColumnDef("EXECUTOR_ID", Types.BIGINT, false));
        sql.add(getDDLCreateTable("PERMISSION_MAPPING", permColumns, null));
        sql.add(getDDLCreateForeignKey("PERMISSION_MAPPING", "FK_PERMISSION_EXECUTOR", "EXECUTOR_ID", "EXECUTOR", "ID"));
        sql.add(getDDLCreateIndex("PERMISSION_MAPPING", "IX_PERMISSION_EXECUTOR", "EXECUTOR_ID"));
        sql.add(getDDLCreateIndex("PERMISSION_MAPPING", "IX_PERMISSION_TYPE", "TYPE"));
        sql.add(getDDLCreateIndex("PERMISSION_MAPPING", "IX_PERMISSION_IDENTIFIABLE_ID", "IDENTIFIABLE_ID"));

        // ru.runa.wfe.ss.SubstitutionCriteria
        sql.add(getDDLRenameTable("SUBSTITUTION_CRITERIAS", "SUBSTITUTION_CRITERIA"));
        sql.add(getDDLRenameColumn("SUBSTITUTION_CRITERIA", "TYPE", new ColumnDef("DISCRIMINATOR", Types.VARCHAR)));
        // ru.runa.wfe.ss.Substitution
        sql.add(getDDLRenameTable("SUBSTITUTIONS", "SUBSTITUTION"));
        sql.add(getDDLRenameColumn("SUBSTITUTION", "IS_TERMINATOR", new ColumnDef("DISCRIMINATOR", Types.VARCHAR)));
        sql.add(getDDLRenameColumn("SUBSTITUTION", "SUBSITUTION_ORG_FUNCTION", new ColumnDef("ORG_FUNCTION", Types.VARCHAR)));
        sql.add(getDDLCreateIndex("SUBSTITUTION", "IX_SUBSTITUTION_CRITERIA", "CRITERIA_ID"));
        sql.add(getDDLCreateIndex("SUBSTITUTION", "IX_SUBSTITUTION_ACTOR", "ACTOR_ID"));
        sql.add(getDDLCreateForeignKey("SUBSTITUTION", "FK_SUBSTITUTION_CRITERIA", "CRITERIA_ID", "SUBSTITUTION_CRITERIA", "ID"));
        // ru.runa.wfe.audit.SystemLog
        sql.add(getDDLTruncateTable("SYSTEM_LOG"));
        sql.add(getDDLRenameColumn("SYSTEM_LOG", "LOG_TYPE", new ColumnDef("DISCRIMINATOR", Types.VARCHAR)));
        sql.add(getDDLRenameColumn("SYSTEM_LOG", "ACTOR_CODE", new ColumnDef("ACTOR_ID", Types.BIGINT)));
        sql.add(getDDLRenameColumn("SYSTEM_LOG", "PROCESS_INSTANCE", new ColumnDef("PROCESS_ID", Types.BIGINT)));

        // ru.runa.wfe.definition.Deployment
        sql.add(getDDLRenameTable("JBPM_PROCESSDEFINITION", "BPM_PROCESS_DEFINITION"));
        sql.add(getDDLRemoveColumn("BPM_PROCESS_DEFINITION", "CLASS_"));
        sql.add(getDDLRemoveForeignKey("BPM_PROCESS_DEFINITION", getObjectName("FK_PROCDEF_STRTSTA")));
        sql.add(getDDLRemoveIndex("BPM_PROCESS_DEFINITION", getObjectName("IDX_PROCDEF_STRTST")));
        sql.add(getDDLRemoveColumn("BPM_PROCESS_DEFINITION", "STARTSTATE_"));
        sql.add(getDDLRemoveColumn("BPM_PROCESS_DEFINITION", "ISTERMINATIONIMPLICIT_"));
        sql.add(getDDLRenameColumn("BPM_PROCESS_DEFINITION", "ID_", new ColumnDef("ID", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_PROCESS_DEFINITION", "NAME_", new ColumnDef("NAME", Types.VARCHAR)));
        sql.add(getDDLRenameColumn("BPM_PROCESS_DEFINITION", "DESCRIPTION_", new ColumnDef("DESCRIPTION", Types.VARCHAR)));
        sql.add(getDDLRenameColumn("BPM_PROCESS_DEFINITION", "VERSION_", new ColumnDef("VERSION", Types.BIGINT)));
        sql.add(getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("LANGUAGE", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))));
        sql.add(getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("CATEGORY", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))));
        sql.add(getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("BYTES", Types.VARBINARY)));
        sql.add(getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("DEPLOYED", Types.TIMESTAMP)));

        // ru.runa.wfe.execution.Process
        sql.add(getDDLRenameTable("JBPM_PROCESSINSTANCE", "BPM_PROCESS"));
        sql.add(getDDLRemoveIndex("BPM_PROCESS", getObjectName("IDX_PROCIN_KEY")));
        sql.add(getDDLRemoveColumn("BPM_PROCESS", "KEY_"));
        sql.add(getDDLRemoveColumn("BPM_PROCESS", "ISSUSPENDED_"));
        sql.add(getDDLRenameColumn("BPM_PROCESS", "ID_", new ColumnDef("ID", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_PROCESS", "VERSION_", new ColumnDef("VERSION", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_PROCESS", "START_", new ColumnDef("START_DATE", Types.DATE)));
        sql.add(getDDLRenameColumn("BPM_PROCESS", "END_", new ColumnDef("END_DATE", Types.DATE)));
        sql.add(getDDLRenameColumn("BPM_PROCESS", "PROCESSDEFINITION_", new ColumnDef("DEFINITION_ID", Types.BIGINT)));
        sql.add(getDDLRenameIndex("BPM_PROCESS", getObjectName("IDX_PROCIN_PROCDEF"), "IX_PROCESS_DEFINITION"));
        sql.add(getDDLRenameForeignKey(getObjectName("FK_PROCIN_PROCDEF"), "FK_PROCESS_DEFINITION"));
        sql.add(getDDLRenameColumn("BPM_PROCESS", "ROOTTOKEN_", new ColumnDef("ROOT_TOKEN_ID", Types.BIGINT)));
        sql.add(getDDLRenameIndex("BPM_PROCESS", getObjectName("IDX_PROCIN_ROOTTK"), "IX_PROCESS_ROOT_TOKEN"));
        sql.add(getDDLRenameForeignKey(getObjectName("FK_PROCIN_ROOTTKN"), "FK_PROCESS_ROOT_TOKEN"));
        sql.add(getDDLRemoveForeignKey("BPM_PROCESS", getObjectName("FK_PROCIN_SPROCTKN")));
        sql.add(getDDLRemoveIndex("BPM_PROCESS", getObjectName("IDX_PROCIN_SPROCTK")));

        // ru.runa.wfe.execution.Token
        sql.add(getDDLRenameTable("JBPM_TOKEN", "BPM_TOKEN"));
        sql.add(getDDLRemoveColumn("BPM_TOKEN", "ISTERMINATIONIMPLICIT_"));
        sql.add(getDDLRemoveColumn("BPM_TOKEN", "ISSUSPENDED_"));
        sql.add(getDDLRemoveColumn("BPM_TOKEN", "LOCK_"));
        sql.add(getDDLRemoveIndex("BPM_TOKEN", getObjectName("IDX_TOKEN_NODE")));
        sql.add(getDDLRemoveForeignKey("BPM_TOKEN", getObjectName("FK_TOKEN_NODE")));
        sql.add(getDDLRemoveIndex("BPM_TOKEN", getObjectName("IDX_TOKEN_SUBPI")));
        sql.add(getDDLRemoveIndex("BPM_TOKEN", getObjectName("IDX_TOKEN_PROCIN")));
        sql.add(getDDLRemoveIndex("BPM_TOKEN", getObjectName("IDX_TOKEN_PARENT")));
        sql.add(getDDLRemoveForeignKey("BPM_TOKEN", getObjectName("FK_TOKEN_SUBPI")));
        sql.add(getDDLRemoveColumn("BPM_TOKEN", "NODEENTER_"));
        sql.add(getDDLRemoveColumn("BPM_TOKEN", "NEXTLOGINDEX_"));
        sql.add(getDDLRenameColumn("BPM_TOKEN", "ID_", new ColumnDef("ID", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_TOKEN", "VERSION_", new ColumnDef("VERSION", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_TOKEN", "NAME_", new ColumnDef("NAME", Types.VARCHAR)));
        sql.add(getDDLRenameColumn("BPM_TOKEN", "START_", new ColumnDef("START_DATE", Types.DATE)));
        sql.add(getDDLRenameColumn("BPM_TOKEN", "END_", new ColumnDef("END_DATE", Types.DATE)));
        sql.add(getDDLRenameColumn("BPM_TOKEN", "PROCESSINSTANCE_", new ColumnDef("PROCESS_ID", Types.BIGINT)));
        sql.add(getDDLRenameForeignKey(getObjectName("FK_TOKEN_PROCINST"), "FK_TOKEN_PROCESS"));
        sql.add(getDDLRenameColumn("BPM_TOKEN", "PARENT_", new ColumnDef("PARENT_ID", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_TOKEN", "ISABLETOREACTIVATEPARENT_", new ColumnDef("REACTIVATE_PARENT", Types.VARCHAR)));
        sql.add(getDDLCreateColumn("BPM_TOKEN", new ColumnDef("NODE_TYPE", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))));
        sql.add(getDDLCreateColumn("BPM_TOKEN", new ColumnDef("NODE_ID", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))));
        sql.add(getDDLCreateColumn("BPM_TOKEN", new ColumnDef("TRANSITION_ID", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))));

        // ru.runa.wfe.execution.Swimlane
        sql.add(getDDLRenameTable("JBPM_SWIMLANEINSTANCE", "BPM_SWIMLANE"));
        sql.add(getDDLRemoveForeignKey("BPM_SWIMLANE", getObjectName("FK_SWIMLANEINST_SL")));
        sql.add(getDDLRemoveIndex("BPM_SWIMLANE", getObjectName("IDX_SWIMLINST_SL")));
        sql.add(getDDLRemoveColumn("BPM_SWIMLANE", "SWIMLANE_"));
        sql.add(getDDLRemoveForeignKey("BPM_SWIMLANE", getObjectName("FK_SWIMLANEINST_TM")));
        sql.add(getDDLRenameColumn("BPM_SWIMLANE", "ID_", new ColumnDef("ID", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_SWIMLANE", "NAME_", new ColumnDef("NAME", Types.VARCHAR)));
        sql.add(getDDLRenameColumn("BPM_SWIMLANE", "VERSION_", new ColumnDef("VERSION", Types.BIGINT)));
        sql.add(getDDLCreateColumn("BPM_SWIMLANE", new ColumnDef("PROCESS_ID", Types.BIGINT)));
        sql.add(getDDLCreateForeignKey("BPM_SWIMLANE", "FK_SWIMLANE_PROCESS", "PROCESS_ID", "BPM_PROCESS", "ID"));
        sql.add(getDDLCreateColumn("BPM_SWIMLANE", new ColumnDef("EXECUTOR_ID", Types.BIGINT)));
        sql.add(getDDLCreateForeignKey("BPM_SWIMLANE", "FK_SWIMLANE_EXECUTOR", "EXECUTOR_ID", "EXECUTOR", "ID"));
        if (handleManualIndexes) {
            sql.add(getDDLRemoveIndex("BPM_SWIMLANE", getObjectName("IDX_SWIMLINST_TASKMGMTINST")));
        }

        // ru.runa.wfe.task.Task
        sql.add(getDDLRenameTable("JBPM_TASKINSTANCE", "BPM_TASK"));
        sql.add(getDDLRemoveColumn("BPM_TASK", "ISBLOCKING_"));
        sql.add(getDDLRemoveColumn("BPM_TASK", "PRIORITY_"));
        sql.add(getDDLRemoveColumn("BPM_TASK", "ISCANCELLED_"));
        sql.add(getDDLRemoveColumn("BPM_TASK", "ISSUSPENDED_"));
        sql.add(getDDLRemoveColumn("BPM_TASK", "CLASS_"));
        sql.add(getDDLRemoveColumn("BPM_TASK", "START_"));
        sql.add(getDDLRemoveIndex("BPM_TASK", getObjectName("IDX_TASKINST_TSK")));
        sql.add(getDDLRemoveForeignKey("BPM_TASK", getObjectName("FK_TASKINST_TASK")));
        sql.add(getDDLRemoveForeignKey("BPM_TASK", getObjectName("FK_TASKINST_TMINST")));
        sql.add(getDDLRemoveColumn("BPM_TASK", "ISOPEN_"));
        sql.add(getDDLRemoveColumn("BPM_TASK", "ISSIGNALLING_"));
        sql.add(getDDLRenameColumn("BPM_TASK", "ID_", new ColumnDef("ID", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_TASK", "NAME_", new ColumnDef("NAME", Types.VARCHAR)));
        sql.add(getDDLRenameColumn("BPM_TASK", "DESCRIPTION_", new ColumnDef("DESCRIPTION", Types.VARCHAR)));
        sql.add(getDDLRenameColumn("BPM_TASK", "VERSION_", new ColumnDef("VERSION", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_TASK", "CREATE_", new ColumnDef("CREATE_DATE", Types.DATE)));
        sql.add(getDDLRenameColumn("BPM_TASK", "END_", new ColumnDef("END_DATE", Types.DATE)));
        sql.add(getDDLRenameColumn("BPM_TASK", "DUEDATE_", new ColumnDef("DEADLINE_DATE", Types.DATE)));
        sql.add(getDDLRenameColumn("BPM_TASK", "TOKEN_", new ColumnDef("TOKEN_ID", Types.BIGINT)));
        sql.add(getDDLRenameForeignKey(getObjectName("FK_TASKINST_TOKEN"), "FK_TASK_TOKEN"));
        sql.add(getDDLRemoveIndex("BPM_TASK", getObjectName("IDX_TASKINST_TOKN")));
        sql.add(getDDLRenameColumn("BPM_TASK", "SWIMLANINSTANCE_", new ColumnDef("SWIMLANE_ID", Types.BIGINT)));
        sql.add(getDDLRemoveForeignKey("JBPM_TASK", getObjectName("FK_TASK_SWIMLANE")));
        sql.add(getDDLRenameForeignKey(getObjectName("FK_TASKINST_SLINST"), "FK_TASK_SWIMLANE"));
        sql.add(getDDLRemoveIndex("BPM_TASK", getObjectName("IDX_TSKINST_SLINST")));
        sql.add(getDDLRenameColumn("BPM_TASK", "PROCINST_", new ColumnDef("PROCESS_ID", Types.BIGINT)));
        sql.add(getDDLRenameForeignKey(getObjectName("FK_TSKINS_PRCINS"), "FK_TASK_PROCESS"));
        sql.add(getDDLCreateColumn("BPM_TASK", new ColumnDef("FIRST_OPEN", Types.TINYINT)));
        sql.add(getDDLCreateColumn("BPM_TASK", new ColumnDef("NODE_ID", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))));
        sql.add(getDDLCreateColumn("BPM_TASK", new ColumnDef("EXECUTOR_ID", Types.BIGINT)));
        sql.add(getDDLCreateForeignKey("BPM_TASK", "FK_TASK_EXECUTOR", "EXECUTOR_ID", "EXECUTOR", "ID"));
        if (handleManualIndexes) {
            sql.add(getDDLRemoveIndex("BPM_TASK", getObjectName("IDX_TASKINST_ACTOREND")));
        }
        sql.add(getDDLRemoveIndex("BPM_TASK", getObjectName("IDX_TASK_ACTORID")));
        sql.add(getDDLRemoveIndex("BPM_TASK", getObjectName("IDX_TSKINST_TMINST")));

        // ru.runa.wfe.audit.ProcessLog
        sql.add(getDDLTruncateTable("JBPM_LOG"));
        sql.add(getDDLRenameTable("JBPM_LOG", "BPM_LOG"));
        sql.add(getDDLRenameColumn("BPM_LOG", "ID_", new ColumnDef("ID", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_LOG", "CLASS_", new ColumnDef("DISCRIMINATOR", Types.CHAR)));
        if (handleManualIndexes && !Strings.isNullOrEmpty(getObjectName("LOG_TOKEN_IDX"))) {
            sql.add(getDDLRemoveIndex("BPM_LOG", getObjectName("LOG_TOKEN_IDX")));
        }
        sql.add(getDDLRemoveColumn("BPM_LOG", "INDEX_"));
        sql.add(getDDLRenameColumn("BPM_LOG", "DATE_", new ColumnDef("LOG_DATE", Types.DATE)));
        sql.add(getDDLRemoveForeignKey("BPM_LOG", getObjectName("FK_LOG_TOKEN")));
        sql.add(getDDLRenameColumn("BPM_LOG", "TOKEN_", new ColumnDef("TOKEN_ID", Types.BIGINT)));
        sql.add(getDDLRemoveForeignKey("BPM_LOG", getObjectName("FK_LOG_PARENT")));
        sql.add(getDDLRemoveColumn("BPM_LOG", "PARENT_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "MESSAGE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "EXCEPTION_"));
        sql.add(getDDLRemoveForeignKey("BPM_LOG", getObjectName("FK_LOG_ACTION")));
        sql.add(getDDLRemoveColumn("BPM_LOG", "ACTION_"));
        sql.add(getDDLRemoveForeignKey("BPM_LOG", getObjectName("FK_LOG_NODE")));
        sql.add(getDDLRemoveColumn("BPM_LOG", "NODE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "ENTER_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "LEAVE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "DURATION_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "NEWLONGVALUE_"));
        sql.add(getDDLRemoveForeignKey("BPM_LOG", getObjectName("FK_LOG_TRANSITION")));
        sql.add(getDDLRemoveColumn("BPM_LOG", "TRANSITION_"));
        sql.add(getDDLRemoveForeignKey("BPM_LOG", getObjectName("FK_LOG_CHILDTOKEN")));
        sql.add(getDDLRemoveColumn("BPM_LOG", "CHILD_"));
        sql.add(getDDLRemoveForeignKey("BPM_LOG", getObjectName("FK_LOG_SOURCENODE")));
        sql.add(getDDLRemoveColumn("BPM_LOG", "SOURCENODE_"));
        sql.add(getDDLRemoveForeignKey("BPM_LOG", getObjectName("FK_LOG_DESTNODE")));
        sql.add(getDDLRemoveColumn("BPM_LOG", "DESTINATIONNODE_"));
        sql.add(getDDLRemoveForeignKey("BPM_LOG", getObjectName("FK_LOG_VARINST")));
        sql.add(getDDLRemoveColumn("BPM_LOG", "VARIABLEINSTANCE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "OLDDATEVALUE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "NEWDATEVALUE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "OLDDOUBLEVALUE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "NEWDOUBLEVALUE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "OLDLONGIDCLASS_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "OLDLONGIDVALUE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "OLDLONGVALUE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "NEWLONGIDCLASS_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "NEWLONGIDVALUE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "OLDSTRINGIDCLASS_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "OLDSTRINGIDVALUE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "NEWSTRINGIDCLASS_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "NEWSTRINGIDVALUE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "OLDSTRINGVALUE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "NEWSTRINGVALUE_"));
        sql.add(getDDLRemoveForeignKey("BPM_LOG", getObjectName("FK_LOG_TASKINST")));
        sql.add(getDDLRemoveColumn("BPM_LOG", "TASKINSTANCE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "TASKACTORID_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "TASKOLDACTORID_"));
        sql.add(getDDLRemoveForeignKey("BPM_LOG", getObjectName("FK_LOG_SWIMINST")));
        sql.add(getDDLRemoveColumn("BPM_LOG", "SWIMLANEINSTANCE_"));
        sql.add(getDDLCreateColumn("BPM_LOG", new ColumnDef("BYTES", Types.VARBINARY)));
        sql.add(getDDLCreateColumn("BPM_LOG", new ColumnDef("CONTENT", dialect.getTypeName(Types.VARCHAR, 4000, 255, 255))));
        sql.add(getDDLCreateColumn("BPM_LOG", new ColumnDef("PROCESS_ID", Types.BIGINT)));
        sql.add(getDDLCreateColumn("BPM_LOG", new ColumnDef("SEVERITY", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))));

        // ru.runa.wfe.job.Job
        sql.add(getDDLRenameTable("JBPM_JOB", "BPM_JOB"));
        sql.add(getDDLRemoveColumn("BPM_JOB", "ISSUSPENDED_"));
        sql.add(getDDLRemoveColumn("BPM_JOB", "LOCKTIME_"));
        sql.add(getDDLRemoveColumn("BPM_JOB", "ISEXCLUSIVE_"));
        sql.add(getDDLRemoveColumn("BPM_JOB", "EXCEPTION_"));
        sql.add(getDDLRemoveColumn("BPM_JOB", "RETRIES_"));
        sql.add(getDDLRemoveColumn("BPM_JOB", "REPEAT_"));
        sql.add(getDDLRemoveForeignKey("BPM_JOB", getObjectName("FK_JOB_TSKINST")));
        sql.add(getDDLRemoveIndex("BPM_JOB", getObjectName("IDX_JOB_TSKINST")));
        sql.add(getDDLRemoveColumn("BPM_JOB", "TASKINSTANCE_"));
        sql.add(getDDLRemoveForeignKey("BPM_JOB", getObjectName("FK_JOB_NODE")));
        sql.add(getDDLRemoveColumn("BPM_JOB", "NODE_"));
        sql.add(getDDLRemoveForeignKey("BPM_JOB", getObjectName("FK_JOB_ACTION")));
        sql.add(getDDLRemoveColumn("BPM_JOB", "ACTION_"));
        sql.add(getDDLRemoveColumn("BPM_JOB", "GRAPHELEMENTTYPE_"));
        sql.add(getDDLRemoveColumn("BPM_JOB", "GRAPHELEMENT_"));
        sql.add(getDDLRenameColumn("BPM_JOB", "CLASS_", new ColumnDef("DISCRIMINATOR", Types.CHAR)));
        sql.add(getDDLRenameColumn("BPM_JOB", "ID_", new ColumnDef("ID", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_JOB", "NAME_", new ColumnDef("NAME", Types.VARCHAR)));
        sql.add(getDDLRenameColumn("BPM_JOB", "VERSION_", new ColumnDef("VERSION", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_JOB", "DUEDATE_", new ColumnDef("DUE_DATE", Types.DATE)));
        sql.add(getDDLRenameColumn("BPM_JOB", "PROCESSINSTANCE_", new ColumnDef("PROCESS_ID", Types.BIGINT)));
        sql.add(getDDLRenameIndex("BPM_JOB", getObjectName("IDX_JOB_PRINST"), "IX_JOB_PROCESS"));
        sql.add(getDDLRemoveIndex("BPM_JOB", getObjectName("IDX_JOB_TOKEN")));
        sql.add(getDDLRenameColumn("BPM_JOB", "TOKEN_", new ColumnDef("TOKEN_ID", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_JOB", "TRANSITIONNAME_", new ColumnDef("TRANSITION_NAME", Types.VARCHAR)));
        sql.add(getDDLCreateColumn("BPM_JOB", new ColumnDef("REPEAT_DURATION", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))));
        sql.add(getDDLRenameForeignKey(getObjectName("FK_JOB_PRINST"), "FK_JOB_PROCESS"));

        // ru.runa.wfe.var.Variable
        sql.add(getDDLRenameTable("JBPM_VARIABLEINSTANCE", "BPM_VARIABLE"));
        sql.add(getDDLRemoveIndex("BPM_VARIABLE", getObjectName("IDX_VARINST_TKVARMP")));
        sql.add(getDDLRemoveForeignKey("BPM_VARIABLE", getObjectName("FK_VARINST_TKVARMP")));
        sql.add(getDDLRemoveColumn("BPM_VARIABLE", "TOKENVARIABLEMAP_"));
        sql.add(getDDLRemoveIndex("BPM_VARIABLE", getObjectName("IDX_VARINST_TK")));
        sql.add(getDDLRemoveForeignKey("BPM_VARIABLE", getObjectName("FK_VARINST_TK")));
        sql.add(getDDLRemoveColumn("BPM_VARIABLE", "TOKEN_"));
        sql.add(getDDLRemoveForeignKey("BPM_VARIABLE", getObjectName("FK_VAR_TSKINST")));
        if (handleManualIndexes) {
            sql.add(getDDLRemoveIndex("BPM_VARIABLE", getObjectName("IDX_VARINST_TASKINST")));
        }
        sql.add(getDDLRemoveColumn("BPM_VARIABLE", "TASKINSTANCE_"));
        sql.add(getDDLRemoveColumn("BPM_VARIABLE", "STRINGIDCLASS_"));
        sql.add(getDDLRemoveColumn("BPM_VARIABLE", "LONGIDCLASS_"));
        sql.add(getDDLRenameColumn("BPM_VARIABLE", "CLASS_", new ColumnDef("DISCRIMINATOR", Types.CHAR)));
        sql.add(getDDLRenameColumn("BPM_VARIABLE", "ID_", new ColumnDef("ID", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_VARIABLE", "NAME_", new ColumnDef("NAME", Types.VARCHAR)));
        sql.add(getDDLRenameColumn("BPM_VARIABLE", "VERSION_", new ColumnDef("VERSION", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_VARIABLE", "CONVERTER_", new ColumnDef("CONVERTER", Types.CHAR)));
        sql.add(getDDLRenameColumn("BPM_VARIABLE", "PROCESSINSTANCE_", new ColumnDef("PROCESS_ID", Types.BIGINT)));
        sql.add(getDDLRenameIndex("BPM_VARIABLE", getObjectName("IDX_VARINST_PRCINS"), "IX_VARIABLE_PROCESS"));
        sql.add(getDDLRenameForeignKey(getObjectName("FK_VARINST_PRCINST"), "FK_VARIABLE_PROCESS"));
        sql.add(getDDLRenameColumn("BPM_VARIABLE", "LONGVALUE_", new ColumnDef("LONGVALUE", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_VARIABLE", "STRINGVALUE_", new ColumnDef("STRINGVALUE", Types.VARCHAR)));
        sql.add(getDDLRenameColumn("BPM_VARIABLE", "DATEVALUE_", new ColumnDef("DATEVALUE", Types.DATE)));
        sql.add(getDDLRenameColumn("BPM_VARIABLE", "DOUBLEVALUE_", new ColumnDef("DOUBLEVALUE", Types.FLOAT)));
        sql.add(getDDLRenameColumn("BPM_VARIABLE", "BYTES_", new ColumnDef("BYTES", Types.VARBINARY)));
        sql.add(getDDLRemoveTable("JBPM_TOKENVARIABLEMAP"));

        // ru.runa.wfe.execution.NodeProcess
        sql.add(getDDLRenameTable("JBPM_NODE_SUBPROC", "BPM_SUBPROCESS"));
        sql.add(getDDLRemoveIndex("BPM_SUBPROCESS", getObjectName("IDX_NODE_SUBPROC_NODE")));
        sql.add(getDDLRenameColumn("BPM_SUBPROCESS", "ID_", new ColumnDef("ID", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_SUBPROCESS", "PROCESSINSTANCE_", new ColumnDef("PARENT_PROCESS_ID", Types.BIGINT)));
        sql.add(getDDLRenameIndex("BPM_SUBPROCESS", getObjectName("IDX_NODE_SUBPROC_PROCINST"), "IX_SUBPROCESS_PARENT_PROCESS"));
        sql.add(getDDLRenameForeignKey(getObjectName("FK_NODE_SUBPROC_SUBPROCINST"), "FK_SUBPROCESS_PROCESS"));
        sql.add(getDDLRenameColumn("BPM_SUBPROCESS", "SUBPROCESSINSTANCE_", new ColumnDef("PROCESS_ID", Types.BIGINT)));
        sql.add(getDDLRenameIndex("BPM_SUBPROCESS", getObjectName("IDX_NODE_SUBPROC_SUBPROCINST"), "IX_SUBPROCESS_PROCESS"));
        sql.add(getDDLRenameForeignKey(getObjectName("FK_NODE_SUBPROC_PROCINST"), "FK_SUBPROCESS_PARENT_PROCESS"));
        sql.add(getDDLRemoveForeignKey("BPM_SUBPROCESS", getObjectName("FK_NODE_SUBPROC_NODE")));
        sql.add(getDDLCreateColumn("BPM_SUBPROCESS", new ColumnDef("PARENT_TOKEN_ID", Types.BIGINT)));
        sql.add(getDDLCreateForeignKey("BPM_SUBPROCESS", "FK_SUBPROCESS_TOKEN", "PARENT_TOKEN_ID", "BPM_TOKEN", "ID"));
        sql.add(getDDLCreateColumn("BPM_SUBPROCESS", new ColumnDef("PARENT_NODE_ID", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))));

        sql.add(getDDLRemoveTable("EXECUTOR_OPEN_TASKS"));
        // for next patch
        sql.add(getDDLCreateColumn("JBPM_PASSTRANS", new ColumnDef("NODE_ID", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))));
        sql.add(getDDLCreateColumn("JBPM_PASSTRANS", new ColumnDef("TRANSITION_ID", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))));
        sql.add(getDDLRenameColumn("JBPM_PASSTRANS", "ID_", new ColumnDef("ID", Types.BIGINT)));
        sql.add(getDDLRenameColumn("JBPM_PASSTRANS", "PROCESSINSTANCE_", new ColumnDef("PROCESS_ID", Types.BIGINT)));
        sql.add(getDDLRemoveForeignKey("JBPM_PASSTRANS", getObjectName("FK_PASSTRANS_PROCINST")));
        sql.add(getDDLRemoveForeignKey("JBPM_PASSTRANS", getObjectName("FK_PASSTRANS_TRANS")));

        sql.add(getDDLRemoveIndex("PERMISSION_MAPPINGS", "PERM_MAPPINGS_SEC_OBJ_ID_IDX"));
        sql.add(getDDLRemoveIndex("PERMISSION_MAPPINGS", getObjectName("PERM_MAPPINGS_EXEC_ID_IDX")));
        return sql;
    }

    @Override
    protected List<String> getDDLQueriesAfter() {
        List<String> sql = super.getDDLQueriesBefore();
        if (jbpmIdTablesExist) {
            sql.add(getDDLRemoveTable("JBPM_ID_MEMBERSHIP"));
            sql.add(getDDLRemoveTable("JBPM_ID_PERMISSIONS"));
            sql.add(getDDLRemoveTable("JBPM_ID_GROUP"));
            sql.add(getDDLRemoveTable("JBPM_ID_USER"));
        }
        if (jbpmCommentTableExists) {
            sql.add(getDDLRemoveTable("JBPM_COMMENT"));
        }
        sql.add(getDDLRemoveColumn("BPM_SWIMLANE", "TASKMGMTINSTANCE_"));
        sql.add(getDDLRemoveColumn("BPM_SWIMLANE", "ACTORID_"));
        sql.add(getDDLRemoveColumn("BPM_TASK", "ACTORID_"));
        sql.add(getDDLRemoveColumn("BPM_TASK", "TASKMGMTINSTANCE_"));

        sql.add(getDDLRemoveForeignKey("JBPM_NODE", getObjectName("FK_NODE_ACTION")));
        sql.add(getDDLRemoveForeignKey("JBPM_NODE", getObjectName("FK_NODE_SCRIPT")));
        sql.add(getDDLRemoveForeignKey("JBPM_VARIABLEACCESS", getObjectName("FK_VARACC_PROCST")));
        sql.add(getDDLRemoveForeignKey("JBPM_VARIABLEACCESS", getObjectName("FK_VARACC_SCRIPT")));
        sql.add(getDDLRemoveForeignKey("JBPM_VARIABLEACCESS", getObjectName("FK_VARACC_TSKCTRL")));
        sql.add(getDDLRemoveForeignKey("JBPM_SWIMLANE", getObjectName("FK_SWL_ASSDEL")));
        sql.add(getDDLRemoveForeignKey("JBPM_TASK", getObjectName("FK_TASK_STARTST")));
        sql.add(getDDLRemoveForeignKey("JBPM_TASK", getObjectName("FK_TASK_TASKNODE")));
        sql.add(getDDLRemoveForeignKey("JBPM_TASK", getObjectName("FK_TASK_ASSDEL")));
        sql.add(getDDLRemoveForeignKey("JBPM_TASK", getObjectName("FK_TSK_TSKCTRL")));
        sql.add(getDDLRemoveColumn("JBPM_TASK", "TASKCONTROLLER_"));
        sql.add(getDDLRemoveForeignKey("JBPM_TRANSITION", getObjectName("FK_TRANSITION_FROM")));
        sql.add(getDDLRemoveForeignKey("JBPM_TRANSITION", getObjectName("FK_TRANSITION_TO")));

        sql.add(getDDLRemoveTable("JBPM_DECISIONCONDITIONS"));
        sql.add(getDDLRemoveTable("JBPM_RUNTIMEACTION"));
        sql.add(getDDLRemoveTable("JBPM_ACTION"));
        sql.add(getDDLRemoveTable("JBPM_EVENT"));
        sql.add(getDDLRemoveTable("JBPM_PROCESSFILES"));
        sql.add(getDDLRemoveTable("JBPM_VARIABLEACCESS"));
        sql.add(getDDLRemoveForeignKey("JBPM_NODE", "FK_DECISION_DELEG"));
        sql.add(getDDLRemoveForeignKey("JBPM_NODE", "FK_NODE_PROCDEF"));
        sql.add(getDDLRemoveForeignKey("JBPM_NODE", "FK_PROCST_SBPRCDEF"));
        sql.add(getDDLRemoveForeignKey("JBPM_NODE", "FK_NODE_SUPERSTATE"));
        sql.add(getDDLRemoveTable("JBPM_NODE"));
        sql.add(getDDLRemoveTable("JBPM_EXCEPTIONHANDLER"));
        sql.add(getDDLRemoveTable("JBPM_TASKCONTROLLER"));
        sql.add(getDDLRemoveTable("JBPM_DELEGATION"));

        sql.add(getDDLRemoveForeignKey("JBPM_MODULEDEFINITION", getObjectName("FK_TSKDEF_START")));
        sql.add(getDDLRemoveTable("JBPM_TASK"));
        sql.add(getDDLRemoveTable("JBPM_SWIMLANE"));

        sql.add(getDDLRemoveTable("PERMISSION_MAPPINGS"));
        sql.add(getDDLRemoveTable("SECURED_OBJECT_TYPES"));
        sql.add(getDDLRemoveTable("SECURED_OBJECTS"));

        sql.add(getDDLRemoveTable("PROCESS_TYPES"));
        sql.add(getDDLRemoveTable("PROCESS_DEFINITION_INFO"));
        sql.add(getDDLRemoveTable("JBPM_TRANSITION"));

        sql.add(getDDLRemoveTable("JBPM_MODULEINSTANCE"));
        sql.add(getDDLRemoveTable("JBPM_MODULEDEFINITION"));

        sql.add(getDDLRemoveColumn("BPM_TOKEN", "NODE_"));
        sql.add(getDDLRemoveColumn("BPM_TOKEN", "SUBPROCESSINSTANCE_"));

        sql.add(getDDLRemoveColumn("BPM_TASK", "TASK_"));
        sql.add(getDDLRemoveColumn("BPM_JOB", "LOCKOWNER_"));
        sql.add(getDDLRemoveColumn("BPM_SUBPROCESS", "NODE_"));
        sql.add(getDDLRemoveColumn("BPM_PROCESS", "SUPERPROCESSTOKEN_"));

        sql.add(getDDLCreateIndex("BPM_TASK", "IX_TASK_PROCESS", "PROCESS_ID"));
        sql.add(getDDLCreateIndex("BPM_TASK", "IX_TASK_EXECUTOR", "EXECUTOR_ID"));
        sql.add(getDDLCreateIndex("BPM_LOG", "IX_LOG_PROCESS", "PROCESS_ID"));
        sql.add(getDDLCreateIndex("BPM_SWIMLANE", "IX_SWIMLANE_PROCESS", "PROCESS_ID"));
        sql.add(getDDLCreateIndex("BPM_TOKEN", "IX_TOKEN_PROCESS", "PROCESS_ID"));
        sql.add(getDDLCreateIndex("BPM_TOKEN", "IX_TOKEN_PARENT", "PARENT_ID"));
        return sql;
    }

    static final Map<String, NodeType> nodeTypes = Maps.newHashMap();
    static {
        nodeTypes.put("C", NodeType.SUBPROCESS);
        nodeTypes.put("D", NodeType.DECISION);
        nodeTypes.put("E", NodeType.END_PROCESS);
        nodeTypes.put("F", NodeType.FORK);
        nodeTypes.put("J", NodeType.JOIN);
        nodeTypes.put("K", NodeType.TASK_STATE);
        nodeTypes.put("W", NodeType.MULTI_SUBPROCESS);
        nodeTypes.put("X", NodeType.SEND_MESSAGE);
        nodeTypes.put("Y", NodeType.RECEIVE_MESSAGE);
    }

    @Override
    public void executeDML(Session session) throws Exception {
        try {
            session.createSQLQuery("SELECT COUNT(*) FROM JBPM_ID_USER").uniqueResult();
            jbpmIdTablesExist = true;
        } catch (Exception e) {
            // may be missed
        }
        try {
            session.createSQLQuery("SELECT COUNT(*) FROM JBPM_COMMENT").uniqueResult();
            jbpmCommentTableExists = true;
        } catch (Exception e) {
            // may be missed
        }
        String q;
        List<Object[]> list;
        ScrollableResults scrollableResults;

        Map<Integer, Long> actorIdByCode = Maps.newHashMap();
        q = "SELECT ID, CODE FROM EXECUTOR WHERE CODE IS NOT NULL";
        list = session.createSQLQuery(q).list();
        for (Object[] objects : list) {
            actorIdByCode.put(((Number) objects[1]).intValue(), ((Number) objects[0]).longValue());
        }
        List<Number> executorIds = Lists.newArrayList();
        List<Number> numbers = session.createSQLQuery("SELECT ID FROM EXECUTOR").list();
        for (Number number : numbers) {
            executorIds.add(number.longValue());
        }

        log.info("Cleaned keepAlive jobs: " + session.createSQLQuery("DELETE FROM BPM_JOB WHERE LOCKOWNER_ = 'keepAlive'").executeUpdate());
        log.info("update process definition PAR file");
        q = "SELECT md.PROCESSDEFINITION_, f.BYTES_ FROM JBPM_PROCESSFILES f, JBPM_MODULEDEFINITION md WHERE f.DEFINITION_ID_=md.ID_ AND f.NAME_='par'";
        scrollableResults = session.createSQLQuery(q).scroll(ScrollMode.FORWARD_ONLY);
        while (scrollableResults.next()) {
            Blob blob = (Blob) scrollableResults.get(1);
            SQLQuery query = session.createSQLQuery("UPDATE BPM_PROCESS_DEFINITION SET BYTES=:par WHERE ID=:id");
            query.setParameter("id", scrollableResults.get(0));
            query.setParameter("par", ByteStreams.toByteArray(blob.getBinaryStream()));
            query.executeUpdate();
        }
        log.info("update process definition type");
        q = "SELECT PROCESS_NAME, SORT_COLUMN FROM PROCESS_DEFINITION_INFO";
        list = session.createSQLQuery(q).list();
        for (Object[] objects : list) {
            SQLQuery query = session.createSQLQuery("UPDATE BPM_PROCESS_DEFINITION SET CATEGORY=:category WHERE NAME=:name");
            query.setParameter("name", objects[0]);
            query.setParameter("category", objects[1]);
            query.executeUpdate();
        }
        session.createSQLQuery("UPDATE BPM_PROCESS_DEFINITION SET LANGUAGE='" + Language.JPDL + "'").executeUpdate();
        log.info("Update BPM_TOKEN.PROCESS_ID FROM BPM_PROCESS.ROOT_TOKEN_ID");
        q = "SELECT t.ID, p.ID FROM BPM_TOKEN t, BPM_PROCESS p WHERE p.ROOT_TOKEN_ID=t.ID AND PROCESS_ID IS NULL";
        scrollableResults = session.createSQLQuery(q).scroll(ScrollMode.FORWARD_ONLY);
        while (scrollableResults.next()) {
            SQLQuery query = session.createSQLQuery("UPDATE BPM_TOKEN SET PROCESS_ID=:processId WHERE ID=:tokenId");
            query.setParameter("tokenId", scrollableResults.get(0));
            query.setParameter("processId", scrollableResults.get(1));
            query.executeUpdate();
        }
        log.info("Deleted broken tokens [by PROCESS_ID]: " + session.createSQLQuery("DELETE FROM BPM_TOKEN WHERE PROCESS_ID IS NULL").executeUpdate());
        log.info("Updating BPM_TOKEN.NODE_TYPE");
        q = "SELECT t.ID, n.CLASS_, n.NAME_ FROM BPM_TOKEN t, JBPM_NODE n WHERE t.NODE_= n.ID_";
        scrollableResults = session.createSQLQuery(q).scroll(ScrollMode.FORWARD_ONLY);
        while (scrollableResults.next()) {
            SQLQuery query = session.createSQLQuery("UPDATE BPM_TOKEN SET NODE_TYPE=:nodeType, NODE_ID=:nodeId WHERE ID=:id");
            query.setParameter("id", scrollableResults.get(0));
            NodeType nodeType = nodeTypes.get(scrollableResults.get(1).toString());
            if (nodeType == null) {
                throw new InternalApplicationException("nodeType == null for " + scrollableResults.get(1));
            }
            query.setParameter("nodeType", nodeType.name());
            query.setParameter("nodeId", scrollableResults.get(2));
            query.executeUpdate();
        }
        // TRANSITION_ID used only for bpmn2, leave it empty
        log.info("Deleted completed tasks: " + session.createSQLQuery("DELETE FROM BPM_TASK WHERE END_DATE IS NOT NULL").executeUpdate());
        q = "DELETE FROM BPM_TASK WHERE PROCESS_ID IN (SELECT ID FROM BPM_PROCESS WHERE END_DATE IS NOT NULL)";
        log.info("Deleted tasks for completed processes: " + session.createSQLQuery(q).executeUpdate());
        session.createSQLQuery("UPDATE BPM_TASK SET FIRST_OPEN=0").executeUpdate();
        q = "SELECT t.ID, t.ACTORID_, mi.PROCESSINSTANCE_, d.NAME_ FROM BPM_TASK t, JBPM_MODULEINSTANCE mi, JBPM_TASK d WHERE t.TASKMGMTINSTANCE_= mi.ID_ and t.TASK_=d.ID_";
        log.info("UPDATE BPM_TASK.EXECUTOR_ID");
        scrollableResults = session.createSQLQuery(q).scroll(ScrollMode.FORWARD_ONLY);
        while (scrollableResults.next()) {
            String executorIdentity = (String) scrollableResults.get(1);
            Long executorId = null;
            if (Strings.isNullOrEmpty(executorIdentity)) {
            } else if (executorIdentity.startsWith("G")) {
                executorId = Long.parseLong(executorIdentity.substring(1));
                if (!executorIds.contains(executorId)) {
                    log.warn("No executor found by ID=" + executorId + ", set it to null; task " + scrollableResults.get(0) + " (ACTORID_='"
                            + scrollableResults.get(1) + "')");
                    executorId = null;
                }
            } else {
                executorId = actorIdByCode.get(Integer.parseInt(executorIdentity));
            }
            if (executorId == null) {
                log.debug("Null executorId for task " + scrollableResults.get(0) + " (ACTORID_='" + scrollableResults.get(1) + "')");
            }
            SQLQuery query = session
                    .createSQLQuery("UPDATE BPM_TASK SET EXECUTOR_ID=:executorId, PROCESS_ID=:processId, NODE_ID=:nodeId WHERE ID=:id");
            query.setParameter("id", scrollableResults.get(0));
            query.setParameter("executorId", executorId);
            query.setParameter("processId", scrollableResults.get(2));
            query.setParameter("nodeId", scrollableResults.get(3));
            query.executeUpdate();
        }
        log.info("Deleted broken tasks [by NODE_ID]: " + session.createSQLQuery("DELETE FROM BPM_TASK WHERE NODE_ID IS NULL").executeUpdate());
        log.info("Deleted broken tasks [by PROCESS_ID]: " + session.createSQLQuery("DELETE FROM BPM_TASK WHERE PROCESS_ID IS NULL").executeUpdate());
        log.info("Deleted broken swimlanes [by ACTORID_]: "
                + session.createSQLQuery("DELETE FROM BPM_SWIMLANE WHERE ACTORID_ IS NULL OR ACTORID_=''").executeUpdate());
        q = "DELETE FROM BPM_SWIMLANE WHERE ID IN (SELECT s.ID FROM BPM_SWIMLANE s LEFT JOIN JBPM_MODULEINSTANCE mi ON s.TASKMGMTINSTANCE_ = mi.ID_ "
                + "LEFT JOIN BPM_PROCESS p ON mi.PROCESSINSTANCE_ = p.ID WHERE p.ID IS NULL OR p.END_DATE IS NOT NULL)";
        log.info("Deleted swimlanes for completed processes: " + session.createSQLQuery(q).executeUpdate());
        q = "SELECT t.ID, t.ACTORID_, mi.PROCESSINSTANCE_ FROM BPM_SWIMLANE t, JBPM_MODULEINSTANCE mi WHERE t.TASKMGMTINSTANCE_= mi.ID_";
        log.info("UPDATE BPM_SWIMLANE.EXECUTOR_ID");
        scrollableResults = session.createSQLQuery(q).scroll(ScrollMode.FORWARD_ONLY);
        while (scrollableResults.next()) {
            String executorIdentity = (String) scrollableResults.get(1);
            Long executorId = null;
            if (Strings.isNullOrEmpty(executorIdentity)) {
            } else if (executorIdentity.startsWith("G")) {
                executorId = Long.parseLong(executorIdentity.substring(1));
                if (!executorIds.contains(executorId)) {
                    log.warn("No executor found by ID=" + executorId + ", set it to null: swimlane " + scrollableResults.get(0) + " (ACTORID_='"
                            + scrollableResults.get(1) + "')");
                    executorId = null;
                }
            } else {
                executorId = actorIdByCode.get(Integer.parseInt(executorIdentity));
            }
            if (executorId == null) {
                log.debug("Null executorId for swimlane " + scrollableResults.get(0) + " (ACTORID_='" + scrollableResults.get(1) + "')");
            }
            SQLQuery query = session.createSQLQuery("UPDATE BPM_SWIMLANE SET EXECUTOR_ID=:executorId, PROCESS_ID=:processId WHERE ID=:id");
            query.setParameter("id", scrollableResults.get(0));
            query.setParameter("executorId", executorId);
            query.setParameter("processId", scrollableResults.get(2));
            query.executeUpdate();
        }
        log.info("Deleted broken swimlanes: " + session.createSQLQuery("DELETE FROM BPM_SWIMLANE WHERE PROCESS_ID IS NULL").executeUpdate());
        //
        List<Executor> adminWithGroupExecutors = executorDAO.getExecutors(Lists.newArrayList(1L, 2L));
        // define executor permissions
        permissionDAO.addType(SecuredObjectType.ACTOR, adminWithGroupExecutors);
        permissionDAO.addType(SecuredObjectType.GROUP, adminWithGroupExecutors);
        // define system permissions
        permissionDAO.addType(SecuredObjectType.SYSTEM, adminWithGroupExecutors);
        permissionDAO.addType(SecuredObjectType.RELATIONGROUP, adminWithGroupExecutors);
        permissionDAO.addType(SecuredObjectType.RELATION, adminWithGroupExecutors);
        permissionDAO.addType(SecuredObjectType.RELATIONPAIR, adminWithGroupExecutors);
        permissionDAO.addType(SecuredObjectType.BOTSTATION, adminWithGroupExecutors);
        permissionDAO.addType(SecuredObjectType.DEFINITION, adminWithGroupExecutors);
        permissionDAO.addType(SecuredObjectType.PROCESS, adminWithGroupExecutors);
        // Variable
        q = "DELETE FROM BPM_VARIABLE WHERE PROCESS_ID IS NULL";
        log.info("Deleted broken variables [by PROCESS_ID]: " + session.createSQLQuery(q).executeUpdate());
        // convert PermissionMapping
        q = "DELETE FROM PERMISSION_MAPPINGS where EXECUTOR_ID in (1, 2, 3)";
        log.info("Deleted admin permission mappings " + session.createSQLQuery(q).executeUpdate());
        q = "WITH TMP (type, version, mask, identifiable_id, executor_id) AS (SELECT so.TYPE_CODE, 0, p.MASK, so.EXT_ID, p.EXECUTOR_ID FROM PERMISSION_MAPPINGS p left join SECURED_OBJECTS so ON p.SECURED_OBJECT_ID=so.ID) INSERT INTO PERMISSION_MAPPING SELECT type, version, mask, identifiable_id, executor_id FROM TMP";
        log.info("Inserted permission mappings " + session.createSQLQuery(q).executeUpdate());
        q = "UPDATE PERMISSION_MAPPING SET TYPE='" + SecuredObjectType.ACTOR.name() + "' WHERE TYPE='-984354279'";
        log.info("Updated permission mappings (SecuredObjectType.ACTOR): " + session.createSQLQuery(q).executeUpdate());
        q = "UPDATE PERMISSION_MAPPING SET TYPE='" + SecuredObjectType.GROUP.name() + "' WHERE TYPE='-978370909'";
        log.info("Updated permission mappings (SecuredObjectType.GROUP): " + session.createSQLQuery(q).executeUpdate());
        q = "UPDATE PERMISSION_MAPPING SET TYPE='" + SecuredObjectType.SYSTEM.name() + "' WHERE TYPE='-1524981484'";
        log.info("Updated permission mappings (SecuredObjectType.SYSTEM): " + session.createSQLQuery(q).executeUpdate());
        q = "UPDATE PERMISSION_MAPPING SET TYPE='" + SecuredObjectType.DEFINITION.name() + "' WHERE TYPE='344855614'";
        log.info("Updated permission mappings (SecuredObjectType.DEFINITION): " + session.createSQLQuery(q).executeUpdate());
        q = "UPDATE PERMISSION_MAPPING SET TYPE='" + SecuredObjectType.PROCESS.name() + "' WHERE TYPE='-1929624128'";
        log.info("Updated permission mappings (SecuredObjectType.PROCESS): " + session.createSQLQuery(q).executeUpdate());
        q = "UPDATE PERMISSION_MAPPING SET TYPE='" + SecuredObjectType.BOTSTATION.name() + "' WHERE TYPE='-582775863'";
        log.info("Updated permission mappings (SecuredObjectType.BOTSTATION): " + session.createSQLQuery(q).executeUpdate());
        q = "UPDATE PERMISSION_MAPPING SET TYPE='" + SecuredObjectType.RELATIONGROUP.name() + "' WHERE TYPE='-222568517'";
        log.info("Updated permission mappings (SecuredObjectType.RELATIONGROUP)" + session.createSQLQuery(q).executeUpdate());
        q = "UPDATE PERMISSION_MAPPING SET TYPE='" + SecuredObjectType.RELATION.name() + "' WHERE TYPE='-2060382376'";
        log.info("Updated permission mappings (SecuredObjectType.RELATION)" + session.createSQLQuery(q).executeUpdate());
        //
        log.info("fill process history for diagram drawing ... prerequisite for next patch");
        q = "UPDATE JBPM_PASSTRANS SET TRANSITION_ID = (SELECT t.NAME_ FROM JBPM_TRANSITION t WHERE t.ID_=TRANSITION_), NODE_ID = (SELECT n.NAME_ FROM JBPM_NODE n, JBPM_TRANSITION t WHERE n.ID_=t.FROM_ AND t.ID_=TRANSITION_)";
        log.info("Updated JBPM_PASSTRANS " + session.createSQLQuery(q).executeUpdate());
        log.info("fill BPM_SUBPROCESS");
        q = "UPDATE BPM_SUBPROCESS SET PARENT_TOKEN_ID = (SELECT p.SUPERPROCESSTOKEN_ FROM BPM_PROCESS p WHERE p.ID=PROCESS_ID), PARENT_NODE_ID = (SELECT n.NAME_ FROM JBPM_NODE n WHERE n.ID_=NODE_)";
        log.info("Updated subprocesses " + session.createSQLQuery(q).executeUpdate());
        //
        log.info("Updated BOT_STATION.VERSION " + session.createSQLQuery("UPDATE BOT_STATION SET VERSION=1").executeUpdate());
        log.info("Updated BOT.VERSION " + session.createSQLQuery("UPDATE BOT SET VERSION=1").executeUpdate());
        log.info("Updated BOT_TASK.VERSION " + session.createSQLQuery("UPDATE BOT_TASK SET VERSION=1").executeUpdate());
    }

    /*
     * [sys].[indexes] 3.5 indexes CREATE INDEX [LOG_TOKEN_IDX] ON
     * [dbo].[JBPM_LOG] ( [INDEX_] ASC ) CREATE INDEX [IDX_VARINST_TASKINST] ON
     * [dbo].[JBPM_VARIABLEINSTANCE] ( [TASKINSTANCE_] ASC ) CREATE INDEX
     * [IDX_SWIMLINST_TASKMGMTINST] ON [dbo].[JBPM_SWIMLANEINSTANCE] (
     * [TASKMGMTINSTANCE_] ASC ) CREATE INDEX [IDX_TASKINST_ACTOREND] ON
     * [dbo].[JBPM_TASKINSTANCE] ( [ACTORID_] ASC, END_ ASC ) CREATE UNIQUE
     * INDEX [UQ_SECURED_TE] ON [dbo].[SECURED_OBJECTS] ( [TYPE_CODE] ASC,
     * [EXT_ID] ASC, [VERSION] ASC ) CREATE UNIQUE INDEX [UQ_PERMISSI_MES] ON
     * [dbo].[PERMISSION_MAPPINGS] ( [MASK] ASC, [EXECUTOR_ID] ASC,
     * [SECURED_OBJECT_ID] ASC, [VERSION] ASC )
     */
}
