package ru.runa.wfe.service.logic.archiving;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.dao.ConstantDAO;
import ru.runa.wfe.commons.dao.Localization;
import ru.runa.wfe.commons.dao.LocalizationDAO;
import ru.runa.wfe.commons.dbpatch.DBPatch;
import ru.runa.wfe.commons.logic.InitializerLogic;
import ru.runa.wfe.commons.logic.LocalizationParser;
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
 * Initial DB for archiving and update during version change.
 * 
 * @author m.koshutin
 * 
 */
public class ArchivingInitializerLogic {

    protected static final Log log = LogFactory.getLog(ArchivingInitializerLogic.class);

    @Autowired(required = false)
    @Qualifier("archConstantDAO")
    private ConstantDAO archConstantDAO;
    @Autowired(required = false)
    @Qualifier("archExecutorDAO")
    private ExecutorDAO archExecutorDAO;
    @Autowired(required = false)
    @Qualifier("archPermissionDAO")
    private PermissionDAO archPermissionDAO;
    @Autowired(required = false)
    @Qualifier("archLocalizationDAO")
    private LocalizationDAO archLocalizationDAO;

    public void onStartup(UserTransaction transaction) {
        try {
            Integer databaseVersion = archConstantDAO.getDatabaseVersion();
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
            archLocalizationDAO.saveLocalizations(localizations, false);
            JobTask.setSystemStartupCompleted(true);
        } catch (Exception e) {
            log.error("initialization failed", e);
        }
    }

    /**
     * Backups database if needed.
     */
    public void backupDatabase(UserTransaction transaction) {
    }

    public static Configuration getConfiguration() {
        LocalSessionFactoryBean factoryBean = (LocalSessionFactoryBean) ApplicationContextFactory.getContext().getBean("&sessionFactoryA");
        return factoryBean.getConfiguration();
    }

    public SessionFactory getArchiveSessionFactory() {
        return archConstantDAO.getSessionFactory();
    }

    /**
     * Initialize database.
     * 
     * @param daoHolder
     *            Helper object for getting DAO's.
     */
    protected void initializeDatabase(UserTransaction transaction) {
        log.info("database is not initialized. initializing...");
        Configuration configuration = getConfiguration();
        SchemaExport schemaExport = new SchemaExport(configuration);
        schemaExport.create(true, true);
        try {
            transaction.begin();
            insertInitialData();
            archConstantDAO.setDatabaseVersion(InitializerLogic.dbPatches.size());
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
        admin = archExecutorDAO.createWithoutCacheVerify(admin);
        archExecutorDAO.setPassword(admin, SystemProperties.getAdministratorDefaultPassword());
        String administratorsGroupName = SystemProperties.getAdministratorsGroupName();
        Group adminGroup = null;
        adminGroup = archExecutorDAO.createWithoutCacheVerify(new Group(administratorsGroupName, administratorsGroupName));
        archExecutorDAO.createWithoutCacheVerify(new Group(SystemProperties.getBotsGroupName(), SystemProperties.getBotsGroupName()));
        List<? extends Executor> adminWithGroupExecutors = Lists.newArrayList(adminGroup, admin);
        archExecutorDAO.addExecutorToGroup(admin, adminGroup);
        archExecutorDAO.createWithoutCacheVerify(new Actor(SystemExecutors.PROCESS_STARTER_NAME, SystemExecutors.PROCESS_STARTER_DESCRIPTION));
        // define executor permissions
        archPermissionDAO.addType(SecuredObjectType.ACTOR, adminWithGroupExecutors);
        archPermissionDAO.addType(SecuredObjectType.GROUP, adminWithGroupExecutors);
        // define system permissions
        archPermissionDAO.addType(SecuredObjectType.SYSTEM, adminWithGroupExecutors);
        archPermissionDAO.addType(SecuredObjectType.RELATIONGROUP, adminWithGroupExecutors);
        archPermissionDAO.addType(SecuredObjectType.RELATION, adminWithGroupExecutors);
        archPermissionDAO.addType(SecuredObjectType.RELATIONPAIR, adminWithGroupExecutors);
        archPermissionDAO.addType(SecuredObjectType.BOTSTATION, adminWithGroupExecutors);
        archPermissionDAO.addType(SecuredObjectType.DEFINITION, adminWithGroupExecutors);
        archPermissionDAO.addType(SecuredObjectType.PROCESS, adminWithGroupExecutors);
    }

    /**
     * Apply patches to initialized database.
     */
    protected void applyPatches(UserTransaction transaction, int dbVersion) {
        log.info("Database version: " + dbVersion + ", code version: " + InitializerLogic.dbPatches.size());
        while (dbVersion < InitializerLogic.dbPatches.size()) {
            DBPatch patch = ApplicationContextFactory.createAutowiredBean(InitializerLogic.dbPatches.get(dbVersion));
            dbVersion++;
            log.info("Applying patch " + patch + " (" + dbVersion + ")");
            try {
                transaction.begin();
                Session session = getArchiveSessionFactory().getCurrentSession();
                patch.internalExecuteDDLBefore(session);
                patch.internalExecuteDML(session);
                patch.internalExecuteDDLAfter(session);
                archConstantDAO.setDatabaseVersion(dbVersion);
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
