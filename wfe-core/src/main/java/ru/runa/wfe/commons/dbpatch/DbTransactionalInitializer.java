package ru.runa.wfe.commons.dbpatch;

import com.google.common.collect.Lists;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.dao.ConstantDao;
import ru.runa.wfe.commons.dao.Localization;
import ru.runa.wfe.commons.dao.LocalizationDao;
import ru.runa.wfe.commons.logic.LocalizationParser;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.dao.PermissionDao;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.SystemExecutors;
import ru.runa.wfe.user.dao.ExecutorDao;

@Component
@Transactional
@CommonsLog
public class DbTransactionalInitializer {
    @Autowired
    private ConstantDao constantDao;
    @Autowired
    private ExecutorDao executorDao;
    @Autowired
    private PermissionDao permissionDao;
    @Autowired
    private LocalizationDao localizationDao;

    public void postExecute(DbPatchPostProcessor dbPatch) throws Exception {
        dbPatch.postExecute();
    }

    /**
     * Inserts initial data into empty database.
     */
    public void initialize(int version) {
        try {
            // Create privileged Executors.
            String administratorName = SystemProperties.getAdministratorName();
            Actor admin = new Actor(administratorName, administratorName, administratorName);
            admin = executorDao.create(admin);
            executorDao.setPassword(admin, SystemProperties.getAdministratorDefaultPassword());
            String administratorsGroupName = SystemProperties.getAdministratorsGroupName();
            Group adminGroup = executorDao.create(new Group(administratorsGroupName, administratorsGroupName));
            executorDao.create(new Group(SystemProperties.getBotsGroupName(), SystemProperties.getBotsGroupName()));
            List<? extends Executor> adminWithGroupExecutors = Lists.newArrayList(adminGroup, admin);
            executorDao.addExecutorToGroup(admin, adminGroup);
            executorDao.create(new Actor(SystemExecutors.PROCESS_STARTER_NAME, SystemExecutors.PROCESS_STARTER_DESCRIPTION));
            for (SecuredObjectType t : SecuredObjectType.values()) {
                permissionDao.addType(t, adminWithGroupExecutors);
            }

            constantDao.setDatabaseVersion(version);
        } catch (Throwable e) {
            log.error("unable to insert initial data", e);
        }
    }

    public Integer getDatabaseVersion() throws Exception {
        return constantDao.getDatabaseVersion();
    }

    public void initPermissions() {
        permissionDao.init();
    }

    public void initLocalizations() {
        localizationDao.init();
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
        localizationDao.saveLocalizations(localizations, false);
    }
}