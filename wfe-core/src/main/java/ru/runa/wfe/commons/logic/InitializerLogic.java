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
package ru.runa.wfe.commons.logic;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.timer.ScheduledTimerTask;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.DatabaseProperties;
import ru.runa.wfe.commons.PropertyResources;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.dao.ConstantDAO;
import ru.runa.wfe.commons.dao.Localization;
import ru.runa.wfe.commons.dao.LocalizationDAO;
import ru.runa.wfe.commons.dbpatch.DBPatch;
import ru.runa.wfe.commons.dbpatch.UnsupportedPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddAggregatedTaskIndexPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddColumnForEmbeddedBotTaskFileName;
import ru.runa.wfe.commons.dbpatch.impl.AddColumnsToSubstituteEscalatedTasksPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddCreateDateColumns;
import ru.runa.wfe.commons.dbpatch.impl.AddDeploymentAuditPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddEmbeddedFileForBotTask;
import ru.runa.wfe.commons.dbpatch.impl.AddHierarchyProcess;
import ru.runa.wfe.commons.dbpatch.impl.AddMultiTaskIndexToTaskPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddNodeIdToProcessLogPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddParentProcessIdPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddSequentialFlagToBot;
import ru.runa.wfe.commons.dbpatch.impl.AddSettingsTable;
import ru.runa.wfe.commons.dbpatch.impl.AddSubProcessIndexColumn;
import ru.runa.wfe.commons.dbpatch.impl.CreateAggregatedLogsTables;
import ru.runa.wfe.commons.dbpatch.impl.CreateReportsTables;
import ru.runa.wfe.commons.dbpatch.impl.ExpandDescriptionsPatch;
import ru.runa.wfe.commons.dbpatch.impl.ExpandVarcharPatch;
import ru.runa.wfe.commons.dbpatch.impl.JbpmRefactoringPatch;
import ru.runa.wfe.commons.dbpatch.impl.NodeTypeChangePatch;
import ru.runa.wfe.commons.dbpatch.impl.PerformancePatch401;
import ru.runa.wfe.commons.dbpatch.impl.PermissionMappingPatch403;
import ru.runa.wfe.commons.dbpatch.impl.TaskCreateLogSeverityChangedPatch;
import ru.runa.wfe.commons.dbpatch.impl.TaskEndDateRemovalPatch;
import ru.runa.wfe.commons.dbpatch.impl.TaskOpenedByExecutorsPatch;
import ru.runa.wfe.commons.dbpatch.impl.TransitionLogPatch;
import ru.runa.wfe.job.impl.JobTask;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.dao.PermissionDAO;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.SystemExecutors;
import ru.runa.wfe.user.dao.ExecutorDAO;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

/**
 * Initial DB population and update during version change.
 * 
 * @author Dofs
 */
public class InitializerLogic {
    protected static final Log log = LogFactory.getLog(InitializerLogic.class);

    public static final List<Class<? extends DBPatch>> dbPatches = Lists.newArrayList();

    static {
        dbPatches.add(UnsupportedPatch.class);
        dbPatches.add(UnsupportedPatch.class);
        dbPatches.add(UnsupportedPatch.class);
        dbPatches.add(UnsupportedPatch.class);
        dbPatches.add(UnsupportedPatch.class);
        dbPatches.add(UnsupportedPatch.class);
        dbPatches.add(UnsupportedPatch.class);
        dbPatches.add(UnsupportedPatch.class);
        dbPatches.add(UnsupportedPatch.class);
        dbPatches.add(UnsupportedPatch.class);
        dbPatches.add(UnsupportedPatch.class);
        dbPatches.add(UnsupportedPatch.class);
        dbPatches.add(UnsupportedPatch.class);
        dbPatches.add(UnsupportedPatch.class);
        dbPatches.add(UnsupportedPatch.class);
        dbPatches.add(UnsupportedPatch.class);
        dbPatches.add(UnsupportedPatch.class);
        dbPatches.add(UnsupportedPatch.class);
        dbPatches.add(UnsupportedPatch.class);
        dbPatches.add(UnsupportedPatch.class);
        // version 20
        // 4.0.0
        dbPatches.add(AddHierarchyProcess.class);
        dbPatches.add(JbpmRefactoringPatch.class);
        dbPatches.add(TransitionLogPatch.class);
        // 4.0.1
        dbPatches.add(PerformancePatch401.class);
        dbPatches.add(TaskEndDateRemovalPatch.class);
        // 4.0.1
        dbPatches.add(PermissionMappingPatch403.class);
        // 4.0.5
        dbPatches.add(NodeTypeChangePatch.class);
        dbPatches.add(ExpandDescriptionsPatch.class);
        // 4.0.6
        dbPatches.add(TaskOpenedByExecutorsPatch.class);
        // 4.1.0
        dbPatches.add(AddNodeIdToProcessLogPatch.class);
        dbPatches.add(AddSubProcessIndexColumn.class);
        // 4.1.1
        dbPatches.add(AddCreateDateColumns.class);
        // 4.2.0
        dbPatches.add(AddEmbeddedFileForBotTask.class);
        dbPatches.add(AddColumnForEmbeddedBotTaskFileName.class);
        dbPatches.add(AddSettingsTable.class);
        dbPatches.add(AddSequentialFlagToBot.class);
        dbPatches.add(CreateAggregatedLogsTables.class);
        dbPatches.add(TaskCreateLogSeverityChangedPatch.class);
        dbPatches.add(AddColumnsToSubstituteEscalatedTasksPatch.class);
        // 4.2.1
        dbPatches.add(AddMultiTaskIndexToTaskPatch.class);
        // 4.2.2
        dbPatches.add(AddDeploymentAuditPatch.class);
        // 4.3.0
        dbPatches.add(AddAggregatedTaskIndexPatch.class);
        dbPatches.add(AddParentProcessIdPatch.class);
        // 4.4.0
        dbPatches.add(CreateReportsTables.class);
        dbPatches.add(ExpandVarcharPatch.class);
    };

    @Autowired
    protected ConstantDAO constantDAO;
    @Autowired
    protected ExecutorDAO executorDAO;
    @Autowired
    protected PermissionDAO permissionDAO;
    @Autowired
    protected LocalizationDAO localizationDAO;

    /**
     * Initialize database if needed.
     */
    public void onStartup(UserTransaction transaction) {
        try {
            Integer databaseVersion = constantDAO.getDatabaseVersion();
            if (databaseVersion != null) {
                applyPatches(transaction, databaseVersion);
            } else {
                initializeDatabase(transaction);
            }
            String localizedFileName = "localizations." + Locale.getDefault().getLanguage() + ".xml";
            InputStream stream = ClassLoaderUtil.getAsStream(localizedFileName, getClass());
            if (stream == null) {
                stream = ClassLoaderUtil.getAsStreamNotNull("localizations.xml", getClass());
            }
            List<Localization> localizations = LocalizationParser.parseLocalizations(stream);
            stream = ClassLoaderUtil.getAsStream(SystemProperties.RESOURCE_EXTENSION_PREFIX + localizedFileName, getClass());
            if (stream == null) {
                stream = ClassLoaderUtil.getAsStream(SystemProperties.RESOURCE_EXTENSION_PREFIX + "localizations.xml", getClass());
            }
            if (stream != null) {
                localizations.addAll(LocalizationParser.parseLocalizations(stream));
            }
            localizationDAO.saveLocalizations(localizations, false);
            if (DatabaseProperties.isDynamicSettingsEnabled()) {
                PropertyResources.setDatabaseAvailable(true);
            }
            setScheduledTaskTimerSettings();
            JobTask.setSystemStartupCompleted(true);
        } catch (Exception e) {
            log.error("initialization failed", e);
        }
    }

    @SuppressWarnings("deprecation")
    private void setScheduledTaskTimerSettings() {
        ApplicationContext context = ApplicationContextFactory.getContext();
        PropertyResources resources = SystemProperties.getResources();
        ScheduledTimerTask jobExecutorTask = context.getBean("jobExecutorTask", ScheduledTimerTask.class);
        ScheduledTimerTask tasksAssignTask = context.getBean("tasksAssignTask", ScheduledTimerTask.class);
        ScheduledTimerTask ldapSynchronizerTask = context.getBean("ldapSynchronizerTask", ScheduledTimerTask.class);

        jobExecutorTask.setDelay(resources.getLongProperty(SystemProperties.TIMERTASK_START_MILLIS_JOB_EXECUTION_NAME, 60000));
        jobExecutorTask.setPeriod(resources.getLongProperty(SystemProperties.TIMERTASK_PERIOD_MILLIS_JOB_EXECUTION_NAME, 60000));
        tasksAssignTask.setDelay(resources.getLongProperty(SystemProperties.TIMERTASK_START_MILLIS_UNASSIGNED_TASKS_EXECUTION_NAME, 60000));
        tasksAssignTask.setPeriod(resources.getLongProperty(SystemProperties.TIMERTASK_PERIOD_MILLIS_UNASSIGNED_TASKS_EXECUTION_NAME, 60000));
        ldapSynchronizerTask.setDelay(resources.getLongProperty(SystemProperties.TIMERTASK_START_MILLIS_LDAP_SYNC_NAME, 600000));
        ldapSynchronizerTask.setPeriod(resources.getLongProperty(SystemProperties.TIMERTASK_PERIOD_MILLIS_LDAP_SYNC_NAME, 600000));
    }

    /**
     * Backups database if needed.
     */
    public void backupDatabase(UserTransaction transaction) {
    }

    /**
     * Initialize database.
     * 
     * @param daoHolder
     *            Helper object for getting DAO's.
     */
    protected void initializeDatabase(UserTransaction transaction) {
        log.info("database is not initialized. initializing...");
        SchemaExport schemaExport = new SchemaExport(ApplicationContextFactory.getConfiguration());
        schemaExport.create(true, true);
        try {
            transaction.begin();
            insertInitialData();
            constantDAO.setDatabaseVersion(dbPatches.size());
            transaction.commit();
        } catch (Throwable th) {
            Utils.rollbackTransaction(transaction);
            throw Throwables.propagate(th);
        }
    }

    /**
     * Inserts initial data on database creation stage
     */
    protected void insertInitialData() {
        // create privileged Executors
        String administratorName = SystemProperties.getAdministratorName();
        Actor admin = new Actor(administratorName, administratorName, administratorName);
        admin = executorDAO.create(admin);
        executorDAO.setPassword(admin, SystemProperties.getAdministratorDefaultPassword());
        String administratorsGroupName = SystemProperties.getAdministratorsGroupName();
        Group adminGroup = executorDAO.create(new Group(administratorsGroupName, administratorsGroupName));
        executorDAO.create(new Group(SystemProperties.getBotsGroupName(), SystemProperties.getBotsGroupName()));
        List<? extends Executor> adminWithGroupExecutors = Lists.newArrayList(adminGroup, admin);
        executorDAO.addExecutorToGroup(admin, adminGroup);
        executorDAO.create(new Actor(SystemExecutors.PROCESS_STARTER_NAME, SystemExecutors.PROCESS_STARTER_DESCRIPTION));
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
        permissionDAO.addType(SecuredObjectType.REPORT, adminWithGroupExecutors);
    }

    /**
     * Apply patches to initialized database.
     */
    protected void applyPatches(UserTransaction transaction, int dbVersion) {
        log.info("Database version: " + dbVersion + ", code version: " + dbPatches.size());
        while (dbVersion < dbPatches.size()) {
            DBPatch patch = ApplicationContextFactory.createAutowiredBean(dbPatches.get(dbVersion));
            dbVersion++;
            log.info("Applying patch " + patch + " (" + dbVersion + ")");
            try {
                transaction.begin();
                patch.executeDDLBefore();
                patch.executeDML();
                patch.executeDDLAfter();
                constantDAO.setDatabaseVersion(dbVersion);
                transaction.commit();
                log.info("Patch " + patch.getClass().getName() + "(" + dbVersion + ") is applied to database successfully.");
            } catch (Throwable th) {
                log.error("Can't apply patch " + patch.getClass().getName() + "(" + dbVersion + ").", th);
                Utils.rollbackTransaction(transaction);
                break;
            }
        }
    }

}
