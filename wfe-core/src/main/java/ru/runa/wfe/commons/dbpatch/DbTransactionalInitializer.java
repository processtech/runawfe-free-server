package ru.runa.wfe.commons.dbpatch;

import com.google.common.collect.Lists;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.dao.ConstantDao;
import ru.runa.wfe.commons.dao.Localization;
import ru.runa.wfe.commons.dao.LocalizationDao;
import ru.runa.wfe.commons.logic.LocalizationParser;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.dao.PermissionDAO;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.SystemExecutors;
import ru.runa.wfe.user.dao.ExecutorDAO;

@Transactional
@CommonsLog
public class DbTransactionalInitializer {
    @Autowired
    private ConstantDao constantDAO;
    @Autowired
    private ExecutorDAO executorDAO;
    @Autowired
    private PermissionDAO permissionDAO;
    @Autowired
    private LocalizationDao localizationDAO;

    public void execute(DBPatch dbPatch, int databaseVersion) throws Exception {
        dbPatch.execute();
        constantDAO.setDatabaseVersion(databaseVersion);
    }

    public void postExecute(IDbPatchPostProcessor dbPatch) throws Exception {
        dbPatch.postExecute();
    }

    /**
     * Initialize empty database
     */
    public void initialize(int version) {
        try {
            insertInitialData();
            constantDAO.setDatabaseVersion(version);
        } catch (Throwable th) {
            log.info("unable to insert initial data", th);
        }
    }

    public Integer getDatabaseVersion() throws Exception {
        return constantDAO.getDatabaseVersion();
    }

    public void initPermissions() {
        permissionDAO.init();
    }

    public void initLocalizations() {
        localizationDAO.init();
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
    }

    /**
     * Inserts initial data on database creation stage
     */
    private void insertInitialData() {
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
        for (SecuredObjectType t : SecuredObjectType.values()) {
            permissionDAO.addType(t, adminWithGroupExecutors);
        }
    }

}