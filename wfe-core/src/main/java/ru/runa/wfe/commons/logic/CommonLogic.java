package ru.runa.wfe.commons.logic;

import com.google.common.collect.Lists;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.commons.PropertyResources;
import ru.runa.wfe.commons.dao.Localization;
import ru.runa.wfe.commons.dao.LocalizationDao;
import ru.runa.wfe.commons.dao.SettingDao;
import ru.runa.wfe.commons.querydsl.HibernateQueryFactory;
import ru.runa.wfe.execution.dao.ArchivedProcessDao;
import ru.runa.wfe.execution.dao.CurrentProcessDao;
import ru.runa.wfe.execution.dao.ProcessDao;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.dao.PermissionDao;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.SystemExecutors;
import ru.runa.wfe.user.TemporaryGroup;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.dao.ExecutorDao;

/**
 * Created on 14.03.2005
 */
public class CommonLogic {
    protected final Log log = LogFactory.getLog(getClass());

    @Autowired
    protected PermissionDao permissionDao;
    @Autowired
    protected ExecutorDao executorDao;
    @Autowired
    protected LocalizationDao localizationDao;
    @Autowired
    protected CurrentProcessDao currentProcessDao;
    @Autowired
    protected ArchivedProcessDao archivedProcessDao;
    @Autowired
    protected ProcessDao processDao;
    @Autowired
    protected SettingDao settingDao;

    // For the sake of mering DAO and logic layers:
    @Autowired
    protected SessionFactory sessionFactory;
    @Autowired
    protected HibernateQueryFactory queryFactory;

    protected <T extends Executor> T checkPermissionsOnExecutor(User user, T executor, Permission permission) {
        if (executor.getName().equals(SystemExecutors.PROCESS_STARTER_NAME) && permission.equals(Permission.LIST)) {
            return executor;
        }
        if (executor instanceof TemporaryGroup && permission.equals(Permission.LIST)) {
            return executor;
        }
        permissionDao.checkAllowed(user, permission, executor);
        return executor;
    }

    protected <T extends Executor> List<T> checkPermissionsOnExecutors(User user, List<T> executors, Permission permission) {
        for (Executor executor : executors) {
            checkPermissionsOnExecutor(user, executor, permission);
        }
        return executors;
    }

    public <T extends SecuredObject> void isPermissionAllowed(User user, List<T> securedObjects, Permission permission,
            CheckMassPermissionCallback<SecuredObject> callback) {
        boolean[] allowedArray = permissionDao.isAllowed(user, permission, securedObjects);
        for (int i = 0; i < allowedArray.length; i++) {
            if (allowedArray[i]) {
                callback.onPermissionGranted(securedObjects.get(i));
            } else {
                callback.onPermissionDenied(securedObjects.get(i));
            }
        }
    }

    public void isPermissionAllowed(User user, SecuredObjectType type, List<Long> ids, Permission permission,
            CheckMassPermissionCallback<Long> callback) {
        boolean[] allowedArray = permissionDao.isAllowed(user, permission, type, ids);
        for (int i = 0; i < allowedArray.length; i++) {
            if (allowedArray[i]) {
                callback.onPermissionGranted(ids.get(i));
            } else {
                callback.onPermissionDenied(ids.get(i));
            }
        }
    }

    protected <T extends SecuredObject> List<T> filterSecuredObject(User user, List<T> securedObjects, Permission permission) {
        boolean[] allowedArray = permissionDao.isAllowed(user, permission, securedObjects);
        List<T> securedObjectList = Lists.newArrayListWithExpectedSize(securedObjects.size());
        for (int i = 0; i < allowedArray.length; i++) {
            if (allowedArray[i]) {
                securedObjectList.add(securedObjects.get(i));
            }
        }
        return securedObjectList;
    }

    /**
     * Load objects list according to {@linkplain BatchPresentation} with permission check for subject.
     * 
     * @param user
     *            Current actor {@linkplain User}.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} to load objects.
     * @param permission
     *            {@linkplain Permission}, which current actor must have on loaded objects.
     * @param securedObjectTypes
     *            Classes, loaded by query. Must be subset of classes, loaded by {@linkplain BatchPresentation}. For example {@linkplain Actor} for
     *            {@linkplain BatchPresentation}, which loads {@linkplain Executor}.
     * @param enablePaging
     *            Flag, equals true, if paging must be enabled; false to load all objects.
     * @return Loaded according to {@linkplain BatchPresentation} objects list.
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getPersistentObjects(User user, BatchPresentation batchPresentation, Permission permission,
            SecuredObjectType[] securedObjectTypes, boolean enablePaging) {
        return (List<T>) permissionDao.getPersistentObjects(user, batchPresentation, permission, securedObjectTypes, enablePaging);
    }

    /**
     * Load objects count according to {@linkplain BatchPresentation} with permission check for subject.
     *
     * @param user
     *            Current actor {@linkplain User}.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} to load objects count.
     * @param permission
     *            {@linkplain Permission}, which current actor must have on loaded objects.
     * @param securedObjectTypes
     *            Classes, loaded by query. Must be subset of classes, loaded by {@linkplain BatchPresentation}. For example {@linkplain Actor} for
     *            {@linkplain BatchPresentation}, which loads {@linkplain Executor}.
     * @return Objects count, which will be loaded according to {@linkplain BatchPresentation}.
     */
    public int getPersistentObjectCount(User user, BatchPresentation batchPresentation, Permission permission, SecuredObjectType[] securedObjectTypes) {
        return permissionDao.getPersistentObjectCount(user, batchPresentation, permission, securedObjectTypes);
    }

    public List<Localization> getLocalizations() {
        return localizationDao.getAll();
    }

    public String getLocalized(String name) {
        return localizationDao.getLocalized(name);
    }

    public void saveLocalizations(User user, List<Localization> localizations) {
        if (!executorDao.isAdministrator(user.getActor())) {
            throw new AuthorizationException("Not admin");
        }
        localizationDao.saveLocalizations(localizations, true);
    }

    public String getSetting(String fileName, String name) {
        return settingDao.getValue(fileName, name);
    }

    public void setSetting(String fileName, String name, String value) {
        settingDao.setValue(fileName, name, value);
        PropertyResources.renewCachedProperty(fileName, name, value);
    }

    public void clearSettings() {
        settingDao.clear();
        PropertyResources.clearPropertiesCache();
    }
}
