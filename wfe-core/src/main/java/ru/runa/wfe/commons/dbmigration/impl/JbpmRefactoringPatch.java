package ru.runa.wfe.commons.dbmigration.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import java.sql.Blob;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.DbType;
import ru.runa.wfe.commons.PropertyResources;
import ru.runa.wfe.commons.dbmigration.DbMigration;
import ru.runa.wfe.definition.Language;
import ru.runa.wfe.lang.NodeType;

/**
 * Increase TransactionTimeout in jboss-service.xml in case of timed out
 * rollback.
 * 
 * What is missed: process logs; batch presentations
 * 
 * @author dofs
 * @since 4.0
 */
public class JbpmRefactoringPatch extends DbMigration {
    private boolean jbpmIdTablesExist;
    private boolean jbpmCommentTableExists;
    private static final PropertyResources RESOURCES = new PropertyResources("JbpmRefactoringPatch.properties", false);
    private static final boolean handleManualIndexes = !RESOURCES.getBooleanProperty("skipPatchV21Indexes", true);

    private String getObjectName(String name) {
        return RESOURCES.getStringProperty(name, name);
    }

    @Override
    protected void executeDDLBefore() {
        if (dbType != DbType.MSSQL) {
            throw new InternalApplicationException("Database migration patch from RunaWFE 3.x to 4.x is currently supported only for MS SQL Server");
        }
        System.out.println("handleManualIndexes = " + handleManualIndexes);
        // "MySQL DB update to version RunaWFE4.x is not supported because of mass column (which are foreign keys) renames [Error on rename of (errno: 150)]"

        executeUpdates(
                // removed unused tables and columns
                getDDLDropTable("PROPERTY_IDS"),
                getDDLDropTable("JBPM_TASKACTORPOOL"),
                getDDLDropTable("JBPM_POOLEDACTOR"),
        
                // refactored batch presentations;
                getDDLDropTable("ACTIVE_BATCH_PRESENTATIONS"),
                getDDLDropTable("BP_DYNAMIC_FIELDS"),
                getDDLDropTable("CRITERIA_CONDITIONS"),
                getDDLDropTable("DISPLAY_FIELDS"),
                getDDLDropTable("FILTER_CRITERIAS"),
                getDDLDropTable("GROUP_FIELDS"),
                getDDLDropTable("GROUP_FIELDS_EXPANDED"),
                getDDLDropTable("SORTED_FIELDS"),
                getDDLDropTable("SORTING_MODES"),
                getDDLTruncateTable("BATCH_PRESENTATIONS"),
                getDDLTruncateTableUsingDelete("PROFILES"),
        
                // ru.runa.wfe.user.Profile
                getDDLRenameTable("PROFILES", "PROFILE"),
                getDDLCreateForeignKey("PROFILE", "FK_PROFILE_ACTOR", "ACTOR_ID", "EXECUTORS", "ID"),
                // ru.runa.wfe.presentation.BatchPresentation
                getDDLRenameTable("BATCH_PRESENTATIONS", "BATCH_PRESENTATION"),
                getDDLRenameColumn("BATCH_PRESENTATION", "PRESENTATION_NAME", new ColumnDef("NAME", Types.VARCHAR)),
                getDDLRenameColumn("BATCH_PRESENTATION", "PRESENTATION_ID", new ColumnDef("CATEGORY", Types.VARCHAR)),
                getDDLCreateColumn("BATCH_PRESENTATION", new ColumnDef("IS_ACTIVE", Types.TINYINT)),
                getDDLCreateColumn("BATCH_PRESENTATION", new ColumnDef("FIELDS", Types.VARBINARY)),
                getDDLCreateColumn("BATCH_PRESENTATION", new ColumnDef("CLASS_TYPE", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))),
                getDDLDropIndex("BATCH_PRESENTATION", "PRESENTATION_NAME_ID_IDX"),
                getDDLCreateIndex("BATCH_PRESENTATION", "IX_BATCH_PRESENTATION_PROFILE", "PROFILE_ID"),
                getDDLDropColumn("BATCH_PRESENTATION", "CLASS_PRESENTATION_ID"),
                // ru.runa.wfe.user.dao.ActorPassword
                getDDLRenameTable("PASSWORDS", "ACTOR_PASSWORD"),
                getDDLRenameColumn("ACTOR_PASSWORD", "PASSWD", new ColumnDef("PASSWORD", Types.VARBINARY)),
                // ru.runa.wfe.bot.BotStation
                getDDLRenameTable("BOT_STATIONS", "BOT_STATION"),
                getDDLDropColumn("BOT_STATION", "BS_USER"),
                getDDLDropColumn("BOT_STATION", "BS_PASS"),
                // ru.runa.wfe.bot.Bot
                getDDLRenameTable("BOTS", "BOT"),
                getDDLDropColumn("BOT", "MAX_PERIOD"),
                getDDLRenameColumn("BOT", "LAST_INVOKED", new ColumnDef("START_TIMEOUT", Types.VARCHAR)),
                getDDLRenameColumn("BOT", "WFE_USER", new ColumnDef("USERNAME", Types.VARCHAR)),
                getDDLRenameColumn("BOT", "WFE_PASS", new ColumnDef("PASSWORD", Types.VARCHAR)),
                getDDLCreateColumn("BOT", new ColumnDef("VERSION", Types.BIGINT)),
                getDDLRenameIndex("BOT", getObjectName("B_BS_IDX"), "IX_BOT_STATION"),
                // ru.runa.wfe.bot.BotTask
                getDDLRenameTable("BOT_TASKS", "BOT_TASK"),
                getDDLDropColumn("BOT_TASK", "CONFIG"),
                getDDLRenameColumn("BOT_TASK", "CLAZZ", new ColumnDef("TASK_HANDLER", Types.VARCHAR)),
                getDDLRenameIndex("BOT_TASK", getObjectName("BT_B_IDX"), "IX_BOT_TASK_BOT"),
                getDDLCreateColumn("BOT_TASK", new ColumnDef("VERSION", Types.BIGINT)),
                // ru.runa.wfe.user.Executor
                getDDLRenameTable("EXECUTORS", "EXECUTOR"),
                getDDLRenameColumn("EXECUTOR", "IS_GROUP", new ColumnDef("DISCRIMINATOR", Types.VARCHAR)),
                getDDLCreateColumn("EXECUTOR", new ColumnDef("ESCALATION_LEVEL", Types.INTEGER)),
                getDDLCreateColumn("EXECUTOR", new ColumnDef("ESCALATION_EXECUTOR_ID", Types.BIGINT)),
                getDDLCreateForeignKey("EXECUTOR", "FK_GROUP_ESCALATION_EXECUTOR", "ESCALATION_EXECUTOR_ID", "EXECUTOR", "ID"),
                // ru.runa.wfe.user.Act,
                getDDLRenameIndex("EXECUTOR", getObjectName("EXECUTORS_CODE_IDX"), "IX_EXECUTOR_CODE"),
                // ru.runa.wfe.user.ExecutorGroupMembership
                getDDLRenameTable("EXECUTOR_GROUP_RELATIONS", "EXECUTOR_GROUP_MEMBER"),
                getDDLRenameIndex("EXECUTOR_GROUP_MEMBER", getObjectName("EXEC_GROUP_REL_EXEC_ID_IDX"), "IX_MEMBER_EXECUTOR"),
                getDDLRenameIndex("EXECUTOR_GROUP_MEMBER", getObjectName("EXEC_GROUP_REL_GROUP_ID_IDX"), "IX_MEMBER_GROUP"),
                // ru.runa.wfe.relation.Relation
                getDDLRenameTable("RELATION_GROUPS", "EXECUTOR_RELATION"),
                // ru.runa.wfe.relation.RelationPair
                getDDLRenameTable("EXECUTOR_RELATIONS", "EXECUTOR_RELATION_PAIR"),
                getDDLRenameForeignKey(getObjectName("FK_RELATION_FROM_EXECUTOR"), "FK_ERP_EXECUTOR_FROM"),
                getDDLRenameForeignKey(getObjectName("FK_RELATION_TO_EXECUTOR"), "FK_ERP_EXECUTOR_TO"),
                getDDLRenameForeignKey(getObjectName("FK_RELATION_GROUP_ID"), "FK_ERP_RELATION"),
                getDDLRenameColumn("EXECUTOR_RELATION_PAIR", "RELATION_GROUP", new ColumnDef("RELATION_ID", Types.BIGINT)),
                getDDLRenameIndex("EXECUTOR_RELATION_PAIR", getObjectName("IDX_RELATION_FROM_EXECUTOR"), "IX_ERP_EXECUTOR_FROM"),
                getDDLRenameIndex("EXECUTOR_RELATION_PAIR", getObjectName("IDX_RELATION_GROUP_ID"), "IX_ERP_RELATION"),
                getDDLRenameIndex("EXECUTOR_RELATION_PAIR", getObjectName("IDX_RELATION_TO_EXECUTOR"), "IX_ERP_EXECUTOR_TO"),
                // ru.runa.wfe.commons.dao.Localization
                getDDLCreateTable("LOCALIZATION", list(
                        new ColumnDef("ID", Types.BIGINT, false).setPrimaryKey(),
                        new ColumnDef("NAME", dialect.getTypeName(Types.VARCHAR, 255, 255, 255)),
                        new ColumnDef("VALUE", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))
                )),
                // ru.runa.wfe.security.dao.PrivelegedMapping
                getDDLDropTable("PRIVELEGE_MAPPINGS"),
                getDDLCreateTable("PRIVELEGED_MAPPING", list(
                        new ColumnDef("ID", Types.BIGINT, false).setPrimaryKey(),
                        new ColumnDef("TYPE", dialect.getTypeName(Types.VARCHAR, 255, 255, 255), false),
                        new ColumnDef("EXECUTOR_ID", Types.BIGINT, false)
                )),
                getDDLCreateForeignKey("PRIVELEGED_MAPPING", "FK_PM_EXECUTOR", "EXECUTOR_ID", "EXECUTOR", "ID"),
                getDDLCreateIndex("PRIVELEGED_MAPPING", "IX_PRIVELEGE_TYPE", "TYPE"),
                getDDLCreateTable("PERMISSION_MAPPING", list(
                        new ColumnDef("ID", Types.BIGINT, false).setPrimaryKey(),
                        new ColumnDef("TYPE", dialect.getTypeName(Types.VARCHAR, 255, 255, 255), false),
                        new ColumnDef("VERSION", Types.BIGINT, false),
                        new ColumnDef("MASK", Types.BIGINT, false),
                        new ColumnDef("IDENTIFIABLE_ID", Types.BIGINT, false),
                        new ColumnDef("EXECUTOR_ID", Types.BIGINT, false)
                )),
                getDDLCreateForeignKey("PERMISSION_MAPPING", "FK_PERMISSION_EXECUTOR", "EXECUTOR_ID", "EXECUTOR", "ID"),
                getDDLCreateIndex("PERMISSION_MAPPING", "IX_PERMISSION_EXECUTOR", "EXECUTOR_ID"),
                getDDLCreateIndex("PERMISSION_MAPPING", "IX_PERMISSION_TYPE", "TYPE"),
                getDDLCreateIndex("PERMISSION_MAPPING", "IX_PERMISSION_IDENTIFIABLE_ID", "IDENTIFIABLE_ID"),
        
                // ru.runa.wfe.ss.SubstitutionCriteria
                getDDLRenameTable("SUBSTITUTION_CRITERIAS", "SUBSTITUTION_CRITERIA"),
                getDDLRenameColumn("SUBSTITUTION_CRITERIA", "TYPE", new ColumnDef("DISCRIMINATOR", Types.VARCHAR)),
                // ru.runa.wfe.ss.Substitution
                getDDLRenameTable("SUBSTITUTIONS", "SUBSTITUTION"),
                getDDLRenameColumn("SUBSTITUTION", "IS_TERMINATOR", new ColumnDef("DISCRIMINATOR", Types.VARCHAR)),
                getDDLRenameColumn("SUBSTITUTION", "SUBSITUTION_ORG_FUNCTION", new ColumnDef("ORG_FUNCTION", Types.VARCHAR)),
                getDDLCreateIndex("SUBSTITUTION", "IX_SUBSTITUTION_CRITERIA", "CRITERIA_ID"),
                getDDLCreateIndex("SUBSTITUTION", "IX_SUBSTITUTION_ACTOR", "ACTOR_ID"),
                getDDLCreateForeignKey("SUBSTITUTION", "FK_SUBSTITUTION_CRITERIA", "CRITERIA_ID", "SUBSTITUTION_CRITERIA", "ID"),
                // ru.runa.wfe.audit.SystemLog
                getDDLTruncateTable("SYSTEM_LOG"),
                getDDLRenameColumn("SYSTEM_LOG", "LOG_TYPE", new ColumnDef("DISCRIMINATOR", Types.VARCHAR)),
                getDDLRenameColumn("SYSTEM_LOG", "ACTOR_CODE", new ColumnDef("ACTOR_ID", Types.BIGINT)),
                getDDLRenameColumn("SYSTEM_LOG", "PROCESS_INSTANCE", new ColumnDef("PROCESS_ID", Types.BIGINT)),
        
                // ru.runa.wfe.definition.ProcessDefinition
                getDDLRenameTable("JBPM_PROCESSDEFINITION", "BPM_PROCESS_DEFINITION"),
                getDDLDropColumn("BPM_PROCESS_DEFINITION", "CLASS_"),
                getDDLDropForeignKey("BPM_PROCESS_DEFINITION", getObjectName("FK_PROCDEF_STRTSTA")),
                getDDLDropIndex("BPM_PROCESS_DEFINITION", getObjectName("IDX_PROCDEF_STRTST")),
                getDDLDropColumn("BPM_PROCESS_DEFINITION", "STARTSTATE_"),
                getDDLDropColumn("BPM_PROCESS_DEFINITION", "ISTERMINATIONIMPLICIT_"),
                getDDLRenameColumn("BPM_PROCESS_DEFINITION", "ID_", new ColumnDef("ID", Types.BIGINT)),
                getDDLRenameColumn("BPM_PROCESS_DEFINITION", "NAME_", new ColumnDef("NAME", Types.VARCHAR)),
                getDDLRenameColumn("BPM_PROCESS_DEFINITION", "DESCRIPTION_", new ColumnDef("DESCRIPTION", Types.VARCHAR)),
                getDDLRenameColumn("BPM_PROCESS_DEFINITION", "VERSION_", new ColumnDef("VERSION", Types.BIGINT)),
                getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("LANGUAGE", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))),
                getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("CATEGORY", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))),
                getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("BYTES", Types.VARBINARY)),
                getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("DEPLOYED", Types.TIMESTAMP)),

                // ru.runa.wfe.execution.Process
                getDDLRenameTable("JBPM_PROCESSINSTANCE", "BPM_PROCESS"),
                getDDLDropIndex("BPM_PROCESS", getObjectName("IDX_PROCIN_KEY")),
                getDDLDropColumn("BPM_PROCESS", "KEY_"),
                getDDLDropColumn("BPM_PROCESS", "ISSUSPENDED_"),
                getDDLRenameColumn("BPM_PROCESS", "ID_", new ColumnDef("ID", Types.BIGINT)),
                getDDLRenameColumn("BPM_PROCESS", "VERSION_", new ColumnDef("VERSION", Types.BIGINT)),
                getDDLRenameColumn("BPM_PROCESS", "START_", new ColumnDef("START_DATE", Types.DATE)),
                getDDLRenameColumn("BPM_PROCESS", "END_", new ColumnDef("END_DATE", Types.DATE)),
                getDDLRenameColumn("BPM_PROCESS", "PROCESSDEFINITION_", new ColumnDef("DEFINITION_ID", Types.BIGINT)),
                getDDLRenameIndex("BPM_PROCESS", getObjectName("IDX_PROCIN_PROCDEF"), "IX_PROCESS_DEFINITION"),
                getDDLRenameForeignKey(getObjectName("FK_PROCIN_PROCDEF"), "FK_PROCESS_DEFINITION"),
                getDDLRenameColumn("BPM_PROCESS", "ROOTTOKEN_", new ColumnDef("ROOT_TOKEN_ID", Types.BIGINT)),
                getDDLRenameIndex("BPM_PROCESS", getObjectName("IDX_PROCIN_ROOTTK"), "IX_PROCESS_ROOT_TOKEN"),
                getDDLRenameForeignKey(getObjectName("FK_PROCIN_ROOTTKN"), "FK_PROCESS_ROOT_TOKEN"),
                getDDLDropForeignKey("BPM_PROCESS", getObjectName("FK_PROCIN_SPROCTKN")),
                getDDLDropIndex("BPM_PROCESS", getObjectName("IDX_PROCIN_SPROCTK")),

                // ru.runa.wfe.execution.Token
                getDDLRenameTable("JBPM_TOKEN", "BPM_TOKEN"),
                getDDLDropColumn("BPM_TOKEN", "ISTERMINATIONIMPLICIT_"),
                getDDLDropColumn("BPM_TOKEN", "ISSUSPENDED_"),
                getDDLDropColumn("BPM_TOKEN", "LOCK_"),
                getDDLDropIndex("BPM_TOKEN", getObjectName("IDX_TOKEN_NODE")),
                getDDLDropForeignKey("BPM_TOKEN", getObjectName("FK_TOKEN_NODE")),
                getDDLDropIndex("BPM_TOKEN", getObjectName("IDX_TOKEN_SUBPI")),
                getDDLDropIndex("BPM_TOKEN", getObjectName("IDX_TOKEN_PROCIN")),
                getDDLDropIndex("BPM_TOKEN", getObjectName("IDX_TOKEN_PARENT")),
                getDDLDropForeignKey("BPM_TOKEN", getObjectName("FK_TOKEN_SUBPI")),
                getDDLDropColumn("BPM_TOKEN", "NODEENTER_"),
                getDDLDropColumn("BPM_TOKEN", "NEXTLOGINDEX_"),
                getDDLRenameColumn("BPM_TOKEN", "ID_", new ColumnDef("ID", Types.BIGINT)),
                getDDLRenameColumn("BPM_TOKEN", "VERSION_", new ColumnDef("VERSION", Types.BIGINT)),
                getDDLRenameColumn("BPM_TOKEN", "NAME_", new ColumnDef("NAME", Types.VARCHAR)),
                getDDLRenameColumn("BPM_TOKEN", "START_", new ColumnDef("START_DATE", Types.DATE)),
                getDDLRenameColumn("BPM_TOKEN", "END_", new ColumnDef("END_DATE", Types.DATE)),
                getDDLRenameColumn("BPM_TOKEN", "PROCESSINSTANCE_", new ColumnDef("PROCESS_ID", Types.BIGINT)),
                getDDLRenameForeignKey(getObjectName("FK_TOKEN_PROCINST"), "FK_TOKEN_PROCESS"),
                getDDLRenameColumn("BPM_TOKEN", "PARENT_", new ColumnDef("PARENT_ID", Types.BIGINT)),
                getDDLRenameColumn("BPM_TOKEN", "ISABLETOREACTIVATEPARENT_", new ColumnDef("REACTIVATE_PARENT", Types.VARCHAR)),
                getDDLCreateColumn("BPM_TOKEN", new ColumnDef("NODE_TYPE", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))),
                getDDLCreateColumn("BPM_TOKEN", new ColumnDef("NODE_ID", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))),
                getDDLCreateColumn("BPM_TOKEN", new ColumnDef("TRANSITION_ID", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))),
        
                // ru.runa.wfe.execution.Swimlane
                getDDLRenameTable("JBPM_SWIMLANEINSTANCE", "BPM_SWIMLANE"),
                getDDLDropForeignKey("BPM_SWIMLANE", getObjectName("FK_SWIMLANEINST_SL")),
                getDDLDropIndex("BPM_SWIMLANE", getObjectName("IDX_SWIMLINST_SL")),
                getDDLDropColumn("BPM_SWIMLANE", "SWIMLANE_"),
                getDDLDropForeignKey("BPM_SWIMLANE", getObjectName("FK_SWIMLANEINST_TM")),
                getDDLRenameColumn("BPM_SWIMLANE", "ID_", new ColumnDef("ID", Types.BIGINT)),
                getDDLRenameColumn("BPM_SWIMLANE", "NAME_", new ColumnDef("NAME", Types.VARCHAR)),
                getDDLRenameColumn("BPM_SWIMLANE", "VERSION_", new ColumnDef("VERSION", Types.BIGINT)),
                getDDLCreateColumn("BPM_SWIMLANE", new ColumnDef("PROCESS_ID", Types.BIGINT)),
                getDDLCreateForeignKey("BPM_SWIMLANE", "FK_SWIMLANE_PROCESS", "PROCESS_ID", "BPM_PROCESS", "ID"),
                getDDLCreateColumn("BPM_SWIMLANE", new ColumnDef("EXECUTOR_ID", Types.BIGINT)),
                getDDLCreateForeignKey("BPM_SWIMLANE", "FK_SWIMLANE_EXECUTOR", "EXECUTOR_ID", "EXECUTOR", "ID")
        );
        if (handleManualIndexes) {
            executeUpdates(getDDLDropIndex("BPM_SWIMLANE", getObjectName("IDX_SWIMLINST_TASKMGMTINST")));
        }

        // ru.runa.wfe.task.Task
        executeUpdates(
                getDDLRenameTable("JBPM_TASKINSTANCE", "BPM_TASK"),
                getDDLDropColumn("BPM_TASK", "ISBLOCKING_"),
                getDDLDropColumn("BPM_TASK", "PRIORITY_"),
                getDDLDropColumn("BPM_TASK", "ISCANCELLED_"),
                getDDLDropColumn("BPM_TASK", "ISSUSPENDED_"),
                getDDLDropColumn("BPM_TASK", "CLASS_"),
                getDDLDropColumn("BPM_TASK", "START_"),
                getDDLDropIndex("BPM_TASK", getObjectName("IDX_TASKINST_TSK")),
                getDDLDropForeignKey("BPM_TASK", getObjectName("FK_TASKINST_TASK")),
                getDDLDropForeignKey("BPM_TASK", getObjectName("FK_TASKINST_TMINST")),
                getDDLDropColumn("BPM_TASK", "ISOPEN_"),
                getDDLDropColumn("BPM_TASK", "ISSIGNALLING_"),
                getDDLRenameColumn("BPM_TASK", "ID_", new ColumnDef("ID", Types.BIGINT)),
                getDDLRenameColumn("BPM_TASK", "NAME_", new ColumnDef("NAME", Types.VARCHAR)),
                getDDLRenameColumn("BPM_TASK", "DESCRIPTION_", new ColumnDef("DESCRIPTION", Types.VARCHAR)),
                getDDLRenameColumn("BPM_TASK", "VERSION_", new ColumnDef("VERSION", Types.BIGINT)),
                getDDLRenameColumn("BPM_TASK", "CREATE_", new ColumnDef("CREATE_DATE", Types.DATE)),
                getDDLRenameColumn("BPM_TASK", "END_", new ColumnDef("END_DATE", Types.DATE)),
                getDDLRenameColumn("BPM_TASK", "DUEDATE_", new ColumnDef("DEADLINE_DATE", Types.DATE)),
                getDDLRenameColumn("BPM_TASK", "TOKEN_", new ColumnDef("TOKEN_ID", Types.BIGINT)),
                getDDLRenameForeignKey(getObjectName("FK_TASKINST_TOKEN"), "FK_TASK_TOKEN"),
                getDDLDropIndex("BPM_TASK", getObjectName("IDX_TASKINST_TOKN")),
                getDDLRenameColumn("BPM_TASK", "SWIMLANINSTANCE_", new ColumnDef("SWIMLANE_ID", Types.BIGINT)),
                getDDLDropForeignKey("JBPM_TASK", getObjectName("FK_TASK_SWIMLANE")),
                getDDLRenameForeignKey(getObjectName("FK_TASKINST_SLINST"), "FK_TASK_SWIMLANE"),
                getDDLDropIndex("BPM_TASK", getObjectName("IDX_TSKINST_SLINST")),
                getDDLRenameColumn("BPM_TASK", "PROCINST_", new ColumnDef("PROCESS_ID", Types.BIGINT)),
                getDDLRenameForeignKey(getObjectName("FK_TSKINS_PRCINS"), "FK_TASK_PROCESS"),
                getDDLCreateColumn("BPM_TASK", new ColumnDef("FIRST_OPEN", Types.TINYINT)),
                getDDLCreateColumn("BPM_TASK", new ColumnDef("NODE_ID", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))),
                getDDLCreateColumn("BPM_TASK", new ColumnDef("EXECUTOR_ID", Types.BIGINT)),
                getDDLCreateForeignKey("BPM_TASK", "FK_TASK_EXECUTOR", "EXECUTOR_ID", "EXECUTOR", "ID")
        );
        if (handleManualIndexes) {
            executeUpdates(getDDLDropIndex("BPM_TASK", getObjectName("IDX_TASKINST_ACTOREND")));
        }
        executeUpdates(
                getDDLDropIndex("BPM_TASK", getObjectName("IDX_TASK_ACTORID")),
                getDDLDropIndex("BPM_TASK", getObjectName("IDX_TSKINST_TMINST")),
        
                // ru.runa.wfe.audit.ProcessLog
                getDDLTruncateTable("JBPM_LOG"),
                getDDLRenameTable("JBPM_LOG", "BPM_LOG"),
                getDDLRenameColumn("BPM_LOG", "ID_", new ColumnDef("ID", Types.BIGINT)),
                getDDLRenameColumn("BPM_LOG", "CLASS_", new ColumnDef("DISCRIMINATOR", Types.CHAR))
        );
        if (handleManualIndexes && !Strings.isNullOrEmpty(getObjectName("LOG_TOKEN_IDX"))) {
            executeUpdates(getDDLDropIndex("BPM_LOG", getObjectName("LOG_TOKEN_IDX")));
        }
        executeUpdates(
                getDDLDropColumn("BPM_LOG", "INDEX_"),
                getDDLRenameColumn("BPM_LOG", "DATE_", new ColumnDef("LOG_DATE", Types.DATE)),
                getDDLDropForeignKey("BPM_LOG", getObjectName("FK_LOG_TOKEN")),
                getDDLRenameColumn("BPM_LOG", "TOKEN_", new ColumnDef("TOKEN_ID", Types.BIGINT)),
                getDDLDropForeignKey("BPM_LOG", getObjectName("FK_LOG_PARENT")),
                getDDLDropColumn("BPM_LOG", "PARENT_"),
                getDDLDropColumn("BPM_LOG", "MESSAGE_"),
                getDDLDropColumn("BPM_LOG", "EXCEPTION_"),
                getDDLDropForeignKey("BPM_LOG", getObjectName("FK_LOG_ACTION")),
                getDDLDropColumn("BPM_LOG", "ACTION_"),
                getDDLDropForeignKey("BPM_LOG", getObjectName("FK_LOG_NODE")),
                getDDLDropColumn("BPM_LOG", "NODE_"),
                getDDLDropColumn("BPM_LOG", "ENTER_"),
                getDDLDropColumn("BPM_LOG", "LEAVE_"),
                getDDLDropColumn("BPM_LOG", "DURATION_"),
                getDDLDropColumn("BPM_LOG", "NEWLONGVALUE_"),
                getDDLDropForeignKey("BPM_LOG", getObjectName("FK_LOG_TRANSITION")),
                getDDLDropColumn("BPM_LOG", "TRANSITION_"),
                getDDLDropForeignKey("BPM_LOG", getObjectName("FK_LOG_CHILDTOKEN")),
                getDDLDropColumn("BPM_LOG", "CHILD_"),
                getDDLDropForeignKey("BPM_LOG", getObjectName("FK_LOG_SOURCENODE")),
                getDDLDropColumn("BPM_LOG", "SOURCENODE_"),
                getDDLDropForeignKey("BPM_LOG", getObjectName("FK_LOG_DESTNODE")),
                getDDLDropColumn("BPM_LOG", "DESTINATIONNODE_"),
                getDDLDropForeignKey("BPM_LOG", getObjectName("FK_LOG_VARINST")),
                getDDLDropColumn("BPM_LOG", "VARIABLEINSTANCE_"),
                getDDLDropColumn("BPM_LOG", "OLDDATEVALUE_"),
                getDDLDropColumn("BPM_LOG", "NEWDATEVALUE_"),
                getDDLDropColumn("BPM_LOG", "OLDDOUBLEVALUE_"),
                getDDLDropColumn("BPM_LOG", "NEWDOUBLEVALUE_"),
                getDDLDropColumn("BPM_LOG", "OLDLONGIDCLASS_"),
                getDDLDropColumn("BPM_LOG", "OLDLONGIDVALUE_"),
                getDDLDropColumn("BPM_LOG", "OLDLONGVALUE_"),
                getDDLDropColumn("BPM_LOG", "NEWLONGIDCLASS_"),
                getDDLDropColumn("BPM_LOG", "NEWLONGIDVALUE_"),
                getDDLDropColumn("BPM_LOG", "OLDSTRINGIDCLASS_"),
                getDDLDropColumn("BPM_LOG", "OLDSTRINGIDVALUE_"),
                getDDLDropColumn("BPM_LOG", "NEWSTRINGIDCLASS_"),
                getDDLDropColumn("BPM_LOG", "NEWSTRINGIDVALUE_"),
                getDDLDropColumn("BPM_LOG", "OLDSTRINGVALUE_"),
                getDDLDropColumn("BPM_LOG", "NEWSTRINGVALUE_"),
                getDDLDropForeignKey("BPM_LOG", getObjectName("FK_LOG_TASKINST")),
                getDDLDropColumn("BPM_LOG", "TASKINSTANCE_"),
                getDDLDropColumn("BPM_LOG", "TASKACTORID_"),
                getDDLDropColumn("BPM_LOG", "TASKOLDACTORID_"),
                getDDLDropForeignKey("BPM_LOG", getObjectName("FK_LOG_SWIMINST")),
                getDDLDropColumn("BPM_LOG", "SWIMLANEINSTANCE_"),
                getDDLCreateColumn("BPM_LOG", new ColumnDef("BYTES", Types.VARBINARY)),
                getDDLCreateColumn("BPM_LOG", new ColumnDef("CONTENT", dialect.getTypeName(Types.VARCHAR, 4000, 255, 255))),
                getDDLCreateColumn("BPM_LOG", new ColumnDef("PROCESS_ID", Types.BIGINT)),
                getDDLCreateColumn("BPM_LOG", new ColumnDef("SEVERITY", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))),
        
                // ru.runa.wfe.job.Job
                getDDLRenameTable("JBPM_JOB", "BPM_JOB"),
                getDDLDropColumn("BPM_JOB", "ISSUSPENDED_"),
                getDDLDropColumn("BPM_JOB", "LOCKTIME_"),
                getDDLDropColumn("BPM_JOB", "ISEXCLUSIVE_"),
                getDDLDropColumn("BPM_JOB", "EXCEPTION_"),
                getDDLDropColumn("BPM_JOB", "RETRIES_"),
                getDDLDropColumn("BPM_JOB", "REPEAT_"),
                getDDLDropForeignKey("BPM_JOB", getObjectName("FK_JOB_TSKINST")),
                getDDLDropIndex("BPM_JOB", getObjectName("IDX_JOB_TSKINST")),
                getDDLDropColumn("BPM_JOB", "TASKINSTANCE_"),
                getDDLDropForeignKey("BPM_JOB", getObjectName("FK_JOB_NODE")),
                getDDLDropColumn("BPM_JOB", "NODE_"),
                getDDLDropForeignKey("BPM_JOB", getObjectName("FK_JOB_ACTION")),
                getDDLDropColumn("BPM_JOB", "ACTION_"),
                getDDLDropColumn("BPM_JOB", "GRAPHELEMENTTYPE_"),
                getDDLDropColumn("BPM_JOB", "GRAPHELEMENT_"),
                getDDLRenameColumn("BPM_JOB", "CLASS_", new ColumnDef("DISCRIMINATOR", Types.CHAR)),
                getDDLRenameColumn("BPM_JOB", "ID_", new ColumnDef("ID", Types.BIGINT)),
                getDDLRenameColumn("BPM_JOB", "NAME_", new ColumnDef("NAME", Types.VARCHAR)),
                getDDLRenameColumn("BPM_JOB", "VERSION_", new ColumnDef("VERSION", Types.BIGINT)),
                getDDLRenameColumn("BPM_JOB", "DUEDATE_", new ColumnDef("DUE_DATE", Types.DATE)),
                getDDLRenameColumn("BPM_JOB", "PROCESSINSTANCE_", new ColumnDef("PROCESS_ID", Types.BIGINT)),
                getDDLRenameIndex("BPM_JOB", getObjectName("IDX_JOB_PRINST"), "IX_JOB_PROCESS"),
                getDDLDropIndex("BPM_JOB", getObjectName("IDX_JOB_TOKEN")),
                getDDLRenameColumn("BPM_JOB", "TOKEN_", new ColumnDef("TOKEN_ID", Types.BIGINT)),
                getDDLRenameColumn("BPM_JOB", "TRANSITIONNAME_", new ColumnDef("TRANSITION_NAME", Types.VARCHAR)),
                getDDLCreateColumn("BPM_JOB", new ColumnDef("REPEAT_DURATION", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))),
                getDDLRenameForeignKey(getObjectName("FK_JOB_PRINST"), "FK_JOB_PROCESS"),
        
                // ru.runa.wfe.var.Variable
                getDDLRenameTable("JBPM_VARIABLEINSTANCE", "BPM_VARIABLE"),
                getDDLDropIndex("BPM_VARIABLE", getObjectName("IDX_VARINST_TKVARMP")),
                getDDLDropForeignKey("BPM_VARIABLE", getObjectName("FK_VARINST_TKVARMP")),
                getDDLDropColumn("BPM_VARIABLE", "TOKENVARIABLEMAP_"),
                getDDLDropIndex("BPM_VARIABLE", getObjectName("IDX_VARINST_TK")),
                getDDLDropForeignKey("BPM_VARIABLE", getObjectName("FK_VARINST_TK")),
                getDDLDropColumn("BPM_VARIABLE", "TOKEN_"),
                getDDLDropForeignKey("BPM_VARIABLE", getObjectName("FK_VAR_TSKINST"))
        );
        if (handleManualIndexes) {
            executeUpdates(getDDLDropIndex("BPM_VARIABLE", getObjectName("IDX_VARINST_TASKINST")));
        }
        executeUpdates(
                getDDLDropColumn("BPM_VARIABLE", "TASKINSTANCE_"),
                getDDLDropColumn("BPM_VARIABLE", "STRINGIDCLASS_"),
                getDDLDropColumn("BPM_VARIABLE", "LONGIDCLASS_"),
                getDDLRenameColumn("BPM_VARIABLE", "CLASS_", new ColumnDef("DISCRIMINATOR", Types.CHAR)),
                getDDLRenameColumn("BPM_VARIABLE", "ID_", new ColumnDef("ID", Types.BIGINT)),
                getDDLRenameColumn("BPM_VARIABLE", "NAME_", new ColumnDef("NAME", Types.VARCHAR)),
                getDDLRenameColumn("BPM_VARIABLE", "VERSION_", new ColumnDef("VERSION", Types.BIGINT)),
                getDDLRenameColumn("BPM_VARIABLE", "CONVERTER_", new ColumnDef("CONVERTER", Types.CHAR)),
                getDDLRenameColumn("BPM_VARIABLE", "PROCESSINSTANCE_", new ColumnDef("PROCESS_ID", Types.BIGINT)),
                getDDLRenameIndex("BPM_VARIABLE", getObjectName("IDX_VARINST_PRCINS"), "IX_VARIABLE_PROCESS"),
                getDDLRenameForeignKey(getObjectName("FK_VARINST_PRCINST"), "FK_VARIABLE_PROCESS"),
                getDDLRenameColumn("BPM_VARIABLE", "LONGVALUE_", new ColumnDef("LONGVALUE", Types.BIGINT)),
                getDDLRenameColumn("BPM_VARIABLE", "STRINGVALUE_", new ColumnDef("STRINGVALUE", Types.VARCHAR)),
                getDDLRenameColumn("BPM_VARIABLE", "DATEVALUE_", new ColumnDef("DATEVALUE", Types.DATE)),
                getDDLRenameColumn("BPM_VARIABLE", "DOUBLEVALUE_", new ColumnDef("DOUBLEVALUE", Types.FLOAT)),
                getDDLRenameColumn("BPM_VARIABLE", "BYTES_", new ColumnDef("BYTES", Types.VARBINARY)),
                getDDLDropTable("JBPM_TOKENVARIABLEMAP"),
        
                // ru.runa.wfe.execution.NodeProcess
                getDDLRenameTable("JBPM_NODE_SUBPROC", "BPM_SUBPROCESS"),
                getDDLDropIndex("BPM_SUBPROCESS", getObjectName("IDX_NODE_SUBPROC_NODE")),
                getDDLRenameColumn("BPM_SUBPROCESS", "ID_", new ColumnDef("ID", Types.BIGINT)),
                getDDLRenameColumn("BPM_SUBPROCESS", "PROCESSINSTANCE_", new ColumnDef("PARENT_PROCESS_ID", Types.BIGINT)),
                getDDLRenameIndex("BPM_SUBPROCESS", getObjectName("IDX_NODE_SUBPROC_PROCINST"), "IX_SUBPROCESS_PARENT_PROCESS"),
                getDDLRenameForeignKey(getObjectName("FK_NODE_SUBPROC_SUBPROCINST"), "FK_SUBPROCESS_PROCESS"),
                getDDLRenameColumn("BPM_SUBPROCESS", "SUBPROCESSINSTANCE_", new ColumnDef("PROCESS_ID", Types.BIGINT)),
                getDDLRenameIndex("BPM_SUBPROCESS", getObjectName("IDX_NODE_SUBPROC_SUBPROCINST"), "IX_SUBPROCESS_PROCESS"),
                getDDLRenameForeignKey(getObjectName("FK_NODE_SUBPROC_PROCINST"), "FK_SUBPROCESS_PARENT_PROCESS"),
                getDDLDropForeignKey("BPM_SUBPROCESS", getObjectName("FK_NODE_SUBPROC_NODE")),
                getDDLCreateColumn("BPM_SUBPROCESS", new ColumnDef("PARENT_TOKEN_ID", Types.BIGINT)),
                getDDLCreateForeignKey("BPM_SUBPROCESS", "FK_SUBPROCESS_TOKEN", "PARENT_TOKEN_ID", "BPM_TOKEN", "ID"),
                getDDLCreateColumn("BPM_SUBPROCESS", new ColumnDef("PARENT_NODE_ID", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))),
        
                getDDLDropTable("EXECUTOR_OPEN_TASKS"),
                // for next patch
                getDDLCreateColumn("JBPM_PASSTRANS", new ColumnDef("NODE_ID", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))),
                getDDLCreateColumn("JBPM_PASSTRANS", new ColumnDef("TRANSITION_ID", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))),
                getDDLRenameColumn("JBPM_PASSTRANS", "ID_", new ColumnDef("ID", Types.BIGINT)),
                getDDLRenameColumn("JBPM_PASSTRANS", "PROCESSINSTANCE_", new ColumnDef("PROCESS_ID", Types.BIGINT)),
                getDDLDropForeignKey("JBPM_PASSTRANS", getObjectName("FK_PASSTRANS_PROCINST")),
                getDDLDropForeignKey("JBPM_PASSTRANS", getObjectName("FK_PASSTRANS_TRANS")),
        
                getDDLDropIndex("PERMISSION_MAPPINGS", "PERM_MAPPINGS_SEC_OBJ_ID_IDX"),
                getDDLDropIndex("PERMISSION_MAPPINGS", getObjectName("PERM_MAPPINGS_EXEC_ID_IDX"))
        );
    }

    @Override
    protected void executeDDLAfter() {
        if (jbpmIdTablesExist) {
            executeUpdates(
                    getDDLDropTable("JBPM_ID_MEMBERSHIP"),
                    getDDLDropTable("JBPM_ID_PERMISSIONS"),
                    getDDLDropTable("JBPM_ID_GROUP"),
                    getDDLDropTable("JBPM_ID_USER")
            );
        }
        if (jbpmCommentTableExists) {
            executeUpdates(getDDLDropTable("JBPM_COMMENT"));
        }
        executeUpdates(
                getDDLDropColumn("BPM_SWIMLANE", "TASKMGMTINSTANCE_"),
                getDDLDropColumn("BPM_SWIMLANE", "ACTORID_"),
                getDDLDropColumn("BPM_TASK", "ACTORID_"),
                getDDLDropColumn("BPM_TASK", "TASKMGMTINSTANCE_"),
        
                getDDLDropForeignKey("JBPM_NODE", getObjectName("FK_NODE_ACTION")),
                getDDLDropForeignKey("JBPM_NODE", getObjectName("FK_NODE_SCRIPT")),
                getDDLDropForeignKey("JBPM_VARIABLEACCESS", getObjectName("FK_VARACC_PROCST")),
                getDDLDropForeignKey("JBPM_VARIABLEACCESS", getObjectName("FK_VARACC_SCRIPT")),
                getDDLDropForeignKey("JBPM_VARIABLEACCESS", getObjectName("FK_VARACC_TSKCTRL")),
                getDDLDropForeignKey("JBPM_SWIMLANE", getObjectName("FK_SWL_ASSDEL")),
                getDDLDropForeignKey("JBPM_TASK", getObjectName("FK_TASK_STARTST")),
                getDDLDropForeignKey("JBPM_TASK", getObjectName("FK_TASK_TASKNODE")),
                getDDLDropForeignKey("JBPM_TASK", getObjectName("FK_TASK_ASSDEL")),
                getDDLDropForeignKey("JBPM_TASK", getObjectName("FK_TSK_TSKCTRL")),
                getDDLDropColumn("JBPM_TASK", "TASKCONTROLLER_"),
                getDDLDropForeignKey("JBPM_TRANSITION", getObjectName("FK_TRANSITION_FROM")),
                getDDLDropForeignKey("JBPM_TRANSITION", getObjectName("FK_TRANSITION_TO")),
        
                getDDLDropTable("JBPM_DECISIONCONDITIONS"),
                getDDLDropTable("JBPM_RUNTIMEACTION"),
                getDDLDropTable("JBPM_ACTION"),
                getDDLDropTable("JBPM_EVENT"),
                getDDLDropTable("JBPM_PROCESSFILES"),
                getDDLDropTable("JBPM_VARIABLEACCESS"),
                getDDLDropForeignKey("JBPM_NODE", "FK_DECISION_DELEG"),
                getDDLDropForeignKey("JBPM_NODE", "FK_NODE_PROCDEF"),
                getDDLDropForeignKey("JBPM_NODE", "FK_PROCST_SBPRCDEF"),
                getDDLDropForeignKey("JBPM_NODE", "FK_NODE_SUPERSTATE"),
                getDDLDropTable("JBPM_NODE"),
                getDDLDropTable("JBPM_EXCEPTIONHANDLER"),
                getDDLDropTable("JBPM_TASKCONTROLLER"),
                getDDLDropTable("JBPM_DELEGATION"),
        
                getDDLDropForeignKey("JBPM_MODULEDEFINITION", getObjectName("FK_TSKDEF_START")),
                getDDLDropTable("JBPM_TASK"),
                getDDLDropTable("JBPM_SWIMLANE"),
        
                getDDLDropTable("PERMISSION_MAPPINGS"),
                getDDLDropTable("SECURED_OBJECT_TYPES"),
                getDDLDropTable("SECURED_OBJECTS"),
        
                getDDLDropTable("PROCESS_TYPES"),
                getDDLDropTable("PROCESS_DEFINITION_INFO"),
                getDDLDropTable("JBPM_TRANSITION"),
        
                getDDLDropTable("JBPM_MODULEINSTANCE"),
                getDDLDropTable("JBPM_MODULEDEFINITION"),
        
                getDDLDropColumn("BPM_TOKEN", "NODE_"),
                getDDLDropColumn("BPM_TOKEN", "SUBPROCESSINSTANCE_"),
        
                getDDLDropColumn("BPM_TASK", "TASK_"),
                getDDLDropColumn("BPM_JOB", "LOCKOWNER_"),
                getDDLDropColumn("BPM_SUBPROCESS", "NODE_"),
                getDDLDropColumn("BPM_PROCESS", "SUPERPROCESSTOKEN_"),
        
                getDDLCreateIndex("BPM_TASK", "IX_TASK_PROCESS", "PROCESS_ID"),
                getDDLCreateIndex("BPM_TASK", "IX_TASK_EXECUTOR", "EXECUTOR_ID"),
                getDDLCreateIndex("BPM_LOG", "IX_LOG_PROCESS", "PROCESS_ID"),
                getDDLCreateIndex("BPM_SWIMLANE", "IX_SWIMLANE_PROCESS", "PROCESS_ID"),
                getDDLCreateIndex("BPM_TOKEN", "IX_TOKEN_PROCESS", "PROCESS_ID"),
                getDDLCreateIndex("BPM_TOKEN", "IX_TOKEN_PARENT", "PARENT_ID")
        );
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
        // define executor permissions
        addPrivilegedMapping(session, "ACTOR");
        addPrivilegedMapping(session, "GROUP");
        // define system permissions
        addPrivilegedMapping(session, "SYSTEM");
        addPrivilegedMapping(session, "RELATIONGROUP");
        addPrivilegedMapping(session, "RELATION");
        addPrivilegedMapping(session, "RELATIONPAIR");
        addPrivilegedMapping(session, "BOTSTATION");
        addPrivilegedMapping(session, "DEFINITION");
        addPrivilegedMapping(session, "PROCESS");
        // Variable
        q = "DELETE FROM BPM_VARIABLE WHERE PROCESS_ID IS NULL";
        log.info("Deleted broken variables [by PROCESS_ID]: " + session.createSQLQuery(q).executeUpdate());
        // convert PermissionMapping
        q = "DELETE FROM PERMISSION_MAPPINGS where EXECUTOR_ID in (1, 2, 3)";
        log.info("Deleted admin permission mappings " + session.createSQLQuery(q).executeUpdate());
        q = "WITH TMP (type, version, mask, identifiable_id, executor_id) AS (SELECT so.TYPE_CODE, 0, p.MASK, so.EXT_ID, p.EXECUTOR_ID FROM PERMISSION_MAPPINGS p left join SECURED_OBJECTS so ON p.SECURED_OBJECT_ID=so.ID) INSERT INTO PERMISSION_MAPPING SELECT type, version, mask, identifiable_id, executor_id FROM TMP";
        log.info("Inserted permission mappings " + session.createSQLQuery(q).executeUpdate());
        q = "UPDATE PERMISSION_MAPPING SET TYPE='ACTOR' WHERE TYPE='-984354279'";
        log.info("Updated permission mappings (SecuredObjectType.ACTOR): " + session.createSQLQuery(q).executeUpdate());
        q = "UPDATE PERMISSION_MAPPING SET TYPE='GROUP' WHERE TYPE='-978370909'";
        log.info("Updated permission mappings (SecuredObjectType.GROUP): " + session.createSQLQuery(q).executeUpdate());
        q = "UPDATE PERMISSION_MAPPING SET TYPE='SYSTEM' WHERE TYPE='-1524981484'";
        log.info("Updated permission mappings (SecuredObjectType.SYSTEM): " + session.createSQLQuery(q).executeUpdate());
        q = "UPDATE PERMISSION_MAPPING SET TYPE='DEFINITION' WHERE TYPE='344855614'";
        log.info("Updated permission mappings (SecuredObjectType.DEFINITION): " + session.createSQLQuery(q).executeUpdate());
        q = "UPDATE PERMISSION_MAPPING SET TYPE='PROCESS' WHERE TYPE='-1929624128'";
        log.info("Updated permission mappings (SecuredObjectType.PROCESS): " + session.createSQLQuery(q).executeUpdate());
        q = "UPDATE PERMISSION_MAPPING SET TYPE='BOTSTATION' WHERE TYPE='-582775863'";
        log.info("Updated permission mappings (SecuredObjectType.BOTSTATION): " + session.createSQLQuery(q).executeUpdate());
        q = "UPDATE PERMISSION_MAPPING SET TYPE='RELATIONGROUP' WHERE TYPE='-222568517'";
        log.info("Updated permission mappings (SecuredObjectType.RELATIONGROUP)" + session.createSQLQuery(q).executeUpdate());
        q = "UPDATE PERMISSION_MAPPING SET TYPE='RELATION' WHERE TYPE='-2060382376'";
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

    private void addPrivilegedMapping(Session session, String type) {
        String idName, idValue;
        switch (dbType) {
            case ORACLE:
                idName = "id, ";
                idValue = "seq_privileged_mapping.nextval, ";
                break;
            case POSTGRESQL:
                idName = "id, ";
                idValue = "nextval('seq_privileged_mapping'), ";
                break;
            default:
                idName = "";
                idValue = "";
        }

        SQLQuery query = session.createSQLQuery("insert into privileged_mapping(" + idName + ", type, executor_id) values(" + idValue + ", :type, :executorId)");
        query.setParameter("type", type);
        query.setParameter("executorId", 1);
        query.executeUpdate();
        query.setParameter("type", type);
        query.setParameter("executorId", 2);
        query.executeUpdate();
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
