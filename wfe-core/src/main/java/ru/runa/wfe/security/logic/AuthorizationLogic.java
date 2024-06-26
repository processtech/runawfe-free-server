package ru.runa.wfe.security.logic;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mysema.commons.lang.CloseableIterator;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.hibernate.HibernateDeleteClause;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.val;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.commons.logic.PresentationCompilerHelper;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.QProcessDefinitionPack;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.PresentationConfiguredCompiler;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectFactory;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.security.dao.PermissionMapping;
import ru.runa.wfe.security.dao.QPermissionMapping;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.QExecutor;
import ru.runa.wfe.user.User;

import static ru.runa.wfe.security.SecuredObjectType.DEFINITION;
import static ru.runa.wfe.security.SecuredObjectType.EXECUTOR;

/**
 * Created on 14.03.2005
 */
@Component
public class AuthorizationLogic extends CommonLogic {

    @Autowired
    private SecuredObjectFactory securedObjectFactory;

    /**
     * Used by addPermissions() and setPermissions(), to avoid duplicated rows in table "permission_mapping".
     */
    private static class IdAndPermission {
        final Long id;
        final Permission permission;

        IdAndPermission(Long id, Permission permission) {
            this.id = id;
            this.permission = permission;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            IdAndPermission that = (IdAndPermission) o;
            return Objects.equals(id, that.id) &&
                    Objects.equals(permission, that.permission);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, permission);
        }
    }
    
    public Set<Long> selectAllowedIds(Executor executor, Permission permission, SecuredObjectType securedObjectType, List<Long> idsOrNull) {
        return permissionDao.selectAllowedIds(executor, permission, securedObjectType, idsOrNull, true);
    }

    public boolean isAllowed(User user, Permission permission, SecuredObject object) {
        return permissionDao.isAllowed(user, permission, object.getSecuredObjectType(), object.getSecuredObjectId());
    }

    public boolean isAllowed(User user, Permission permission, SecuredObjectType type, Long identifiableId) {
        return permissionDao.isAllowed(user, permission, type, identifiableId);
    }

    public <T extends SecuredObject> boolean[] isAllowed(User user, Permission permission, List<T> securedObjects) {
        return permissionDao.isAllowed(user, permission, securedObjects);
    }

    public boolean[] isAllowed(User user, Permission permission, SecuredObjectType type, List<Long> ids) {
        return permissionDao.isAllowed(user, permission, type, ids);
    }

    public boolean isAllowedForAny(User user, Permission permission, SecuredObjectType securedObjectType) {
        return permissionDao.isAllowedForAny(user, permission, securedObjectType);
    }

    public List<Permission> getIssuedPermissions(User user, Executor performer, SecuredObject securedObject) {
        checkPermissionsOnExecutor(user, performer, Permission.READ);
        permissionDao.checkAllowed(user, Permission.READ_PERMISSIONS, securedObject);
        return permissionDao.getIssuedPermissions(performer, securedObject);
    }

    /**
     * Exports permissions to xml, see: ExportDataFileAction.
     * <p>
     * Placed here and added all that PermissionService stuff, because must be executed under transaction.
     */
    public void exportDataFile(User user, Document script) {
        // TODO See #1586-5, #1586-6. Looks like v4.3 had no permission checks for this operation at all.
        permissionDao.checkAllowed(user, Permission.READ, SecuredSingleton.SYSTEM);
        Element parentElement = script.getRootElement();
        val pm = QPermissionMapping.permissionMapping;
        val e = QExecutor.executor;

        // Export permissions of all singletons.
        {
            List<SecuredObjectType> allTypes = SecuredObjectType.values();
            ArrayList<SecuredObjectType> singletonTypes = new ArrayList<>(allTypes.size());
            for (SecuredObjectType t : allTypes) {
                if (t.isSingleton()) {
                    singletonTypes.add(t);
                }
            }
            exportDataFilePermissions(parentElement, queryFactory.select(pm.permission, e.name, pm.objectType)
                    .from(pm, e)
                    .where(pm.objectType.in(singletonTypes).and(pm.objectId.eq(0L)).and(pm.executor.eq(e)))
                    .orderBy(pm.objectType.asc(), e.name.asc(), pm.permission.asc()));
        }

        // Export ACTOR and GROUP permissions.
        {
            QExecutor e2 = new QExecutor("e2");  // same table as `e`, but different alias
            exportDataFilePermissions(parentElement, queryFactory.select(pm.permission, e.name, pm.objectType, e2.name)
                    .from(pm, e, e2)
                    .where(pm.objectType.eq(EXECUTOR).and(pm.objectId.eq(e2.id)).and(pm.executor.eq(e)))
                    .orderBy(pm.objectType.asc(), e2.name.asc(), e.name.asc(), pm.permission.asc()));
        }

        // Export DEFINITION permissions.
        {
            val p = QProcessDefinitionPack.processDefinitionPack;
            exportDataFilePermissions(parentElement, queryFactory.select(pm.permission, e.name, pm.objectType, p.name)
                    .from(pm, e, p)
                    .where(pm.objectType.eq(DEFINITION).and(pm.objectId.eq(p.id)).and(pm.executor.eq(e)))
                    .orderBy(p.name.asc(), e.name.asc(), pm.permission.asc()));
        }
    }

    /**
     *
     * @param parentElement  Parent for "addPermissions" elements.
     * @param query  Rows must be ordered by objectType, objectName, executorName, LAST by permission, because of rows grouping algorithm.
     *               Fields in tuple must go in this order: permission, executorName, objectType, [objectName].
     */
    private void exportDataFilePermissions(Element parentElement, JPQLQuery<Tuple> query) {
        SecuredObjectType lastObjectType = null;
        String lastObjectName = null;
        String lastExecutorName = null;
        Element addPermissionsElement = null;

        try (CloseableIterator<Tuple> i = query.iterate()) {
            while (i.hasNext()) {
                Tuple t = i.next();
                Permission permission = t.get(0, Permission.class);
                String executorName = t.get(1, String.class);
                SecuredObjectType objectType = t.get(2, SecuredObjectType.class);
                String objectName = t.size() == 4 ? t.get(3, String.class) : null;

                // Manually group by objectType, objectName, executorName.
                if (objectType != lastObjectType || !Objects.equals(objectName, lastObjectName) || !Objects.equals(executorName, lastExecutorName)) {
                    lastObjectType = objectType;
                    lastObjectName = objectName;
                    lastExecutorName = executorName;

                    addPermissionsElement = parentElement.addElement("addPermissions", XmlUtils.RUNA_NAMESPACE);
                    //noinspection ConstantConditions
                    addPermissionsElement.addAttribute("type", objectType.getName());
                    if (objectName != null) {
                        addPermissionsElement.addAttribute("name", objectName);
                    }
                    addPermissionsElement.addAttribute("executor", executorName);
                }

                if (addPermissionsElement != null) {
                    addPermissionsElement.addElement("permission", XmlUtils.RUNA_NAMESPACE).addAttribute("name", permission.getName());
                }
            }
        }
    }

    /**
     * Used by script's AddPermissionsOperation.
     */
    public void addPermissions(User user, String executorName, Map<SecuredObjectType, Set<String>> objectNames, Set<Permission> permissions) {
        setPermissionsImpl(user, executorName, objectNames, permissions, false);
    }

    /**
     * Used by script's SetPermissionsOperation.
     */
    public void setPermissions(User user, String executorName, Map<SecuredObjectType, Set<String>> objectNames, Set<Permission> permissions) {
        setPermissionsImpl(user, executorName, objectNames, permissions, true);
    }

    /**
     * Used by script's SetPermissionsOperationById.
     */
    public void setPermissionsById(User user, String executorName, Set<Permission> permissions, String type, Set<Long> ids) {
        grantPermissionsById(user, executorName, permissions, type, ids, true);
    }

    /**
     * Used by script's AddPermissionsOperationById.
     */
    public void addPermissionsById(User user, String executorName, Set<Permission> permissions, String type, Set<Long> ids) {
        grantPermissionsById(user, executorName, permissions, type, ids, false);
    }

    private void grantPermissionsById(User user, String executorName, Set<Permission> permissions, String type, Set<Long> ids, boolean dropExistingPermissions) {
        Executor executor = executorDao.getExecutor(executorName);
        for (Long id : ids) {
            SecuredObject securedObject = findSecuredObject(SecuredObjectType.valueOf(type), id);
            if (!dropExistingPermissions) {
                permissions.addAll(permissionDao.getIssuedPermissions(executor, securedObject));
            }
            setPermissions(user, executor, permissions, securedObject);
        }
    }

    private void setPermissionsImpl(User user, String executorName, Map<SecuredObjectType, Set<String>> objectNames, Set<Permission> permissions,
            boolean deleteExisting) {
        Executor executor = executorDao.getExecutor(executorName);  // [QSL] Only id is needed, or maybe even join would be enough.
        permissionDao.checkAllowed(user, Permission.READ, executor);

        val pm = QPermissionMapping.permissionMapping;

        for (Map.Entry<SecuredObjectType, Set<String>> kv : objectNames.entrySet()) {
            SecuredObjectType type = kv.getKey();
            Set<String> names = kv.getValue();

            if (type.isSingleton()) {
                // To handle both singletons and non-singletons in the same for(namesPart...) loop and thus avoid `q` construction duplication.
                // I'd rather have inner function (closure) for `q` construction, but this is java.
                names = new HashSet<>(1);
                names.add(null);
            }

            for (List<String> namesPart : Lists.partition(new ArrayList<>(names), SystemProperties.getDatabaseNameParametersCount())) {
                List<Long> objectIds;

                if (type.isSingleton()) {
                    // Ignore namesPart: it contains single null element added above, in single loop iteration.
                    objectIds = Collections.singletonList(0L);
                } else {
                    objectIds = securedObjectFactory.getIdsByNames(type, new HashSet<>(namesPart));
                }
                permissionDao.checkAllowedForAll(user, Permission.UPDATE_PERMISSIONS, type, objectIds);

                HashSet<IdAndPermission> existing = new HashSet<>();
                try (CloseableIterator<Tuple> i = queryFactory.select(pm.objectId, pm.permission)
                        .from(pm)
                        .where(pm.executor.eq(executor)
                                .and(pm.objectType.eq(type))
                                .and(pm.objectId.in(objectIds)))
                        .iterate()
                ) {
                    while (i.hasNext()) {
                        Tuple t = i.next();
                        existing.add(new IdAndPermission(t.get(0, Long.class), t.get(1, Permission.class)));
                    }
                }

                for (Permission perm : permissions) {
                    for (Long id : objectIds) {
                        if (!existing.remove(new IdAndPermission(id, perm))) {
                            // [SQL] Optimizable: for(perm) { insert-select from executor where id in (objectIds) }
                            sessionFactory.getCurrentSession().save(new PermissionMapping(executor, type, id, perm));
                        }
                    }
                }

                if (deleteExisting && !existing.isEmpty()) {
                    // Delete in single statement; getDatabaseNameParametersCount() is much less than getDatabaseParametersCount(), so should be OK.
                    BooleanBuilder cond = new BooleanBuilder();
                    for (IdAndPermission ip : existing) {
                        cond.or(pm.objectId.eq(ip.id).and(pm.permission.eq(ip.permission)));
                    }
                    queryFactory.delete(pm).where(pm.executor.eq(executor).and(pm.objectType.eq(type)).and(cond)).execute();
                }
            }
        }
    }

    /**
     * Used by script's RemovePermissionsOperation.
     */
    public void removePermissions(User user, String executorName, Map<SecuredObjectType, Set<String>> objectNames, Set<Permission> permissions) {
        removePermissionsImpl(user, executorName, objectNames, permissions);
    }

    /**
     * Used by script's RemoveAllPermissionsOperation.
     */
    public void removeAllPermissions(User user, String executorName, Map<SecuredObjectType, Set<String>> objectNames) {
        removePermissionsImpl(user, executorName, objectNames, null);
    }
    
    /**
     * Used by scripts RemovePermissionsOperationById and RemoveAllPermissionsOperationById.
     */
    public void removePermissionsById(User user, String executorName, Set<Permission> permissions, String type, Set<Long> ids) {
        Executor executor = executorDao.getExecutor(executorName);
        permissionDao.checkAllowed(user, Permission.READ, executor);
        QPermissionMapping pm = QPermissionMapping.permissionMapping; 
        for(Long id : ids) {
            SecuredObject securedObject = findSecuredObject(SecuredObjectType.valueOf(type), id);
            permissionDao.checkAllowed(user, Permission.UPDATE_PERMISSIONS, securedObject);
            HibernateDeleteClause q = queryFactory.delete(pm)
                    .where(pm.executor.eq(executor).and(pm.objectType.eq(securedObject.getSecuredObjectType())).and(pm.objectId.in(ids)));
            if (!permissions.isEmpty()){
                q.where(pm.permission.in(permissions));
            }
            q.execute();
        }
    }

    /**
     *
     * @param objectNames Non-empty. Contains null values for singleton keys.
     * @param permissions Null if called from removeAllPermissions().
     */
    private void removePermissionsImpl(User user, String executorName, Map<SecuredObjectType, Set<String>> objectNames, Set<Permission> permissions) {
        Executor executor = executorDao.getExecutor(executorName);  // [QSL] Only id is needed, or maybe even join would be enough.
        permissionDao.checkAllowed(user, Permission.READ, executor);

        val pm = QPermissionMapping.permissionMapping;

        for (Map.Entry<SecuredObjectType, Set<String>> kv : objectNames.entrySet()) {
            SecuredObjectType type = kv.getKey();
            Set<String> names = kv.getValue();

            if (type.isSingleton()) {
                names = new HashSet<>(1);
                names.add(null);
            }

            for (List<String> namesPart : Lists.partition(new ArrayList<>(names), SystemProperties.getDatabaseNameParametersCount())) {
                List<Long> objectIds;

                if (type.isSingleton()) {
                    objectIds = Collections.singletonList(0L);
                } else {
                    objectIds = securedObjectFactory.getIdsByNames(type, new HashSet<>(namesPart));
                }
                permissionDao.checkAllowedForAll(user, Permission.UPDATE_PERMISSIONS, type, objectIds);

                HibernateDeleteClause q = queryFactory.delete(pm)
                        .where(pm.executor.eq(executor).and(pm.objectType.eq(type)).and(pm.objectId.in(objectIds)));
                if (permissions != null) {
                    q.where(pm.permission.in(permissions));
                }
                q.execute();
            }
        }
    }

    public void setPermissions(User user, List<Long> executorIds, Collection<Permission> permissions, SecuredObject securedObject) {
        List<Executor> executors = executorDao.getExecutors(executorIds);
        for (Executor executor : executors) {
            setPermissions(user, executor, permissions, securedObject);
        }
    }

    public void setPermissions(User user, List<Long> executorIds, List<Collection<Permission>> permissions, SecuredObject securedObject) {
        List<Executor> executors = executorDao.getExecutors(executorIds);
        Preconditions.checkArgument(executors.size() == permissions.size(), "arrays length differs");
        for (int i = 0; i < executors.size(); i++) {
            setPermissions(user, executors.get(i), permissions.get(i), securedObject);
        }
    }

    public void setPermissions(User user, Long executorId, Collection<Permission> permissions, SecuredObject securedObject) {
        Executor executor = executorDao.getExecutor(executorId);
        setPermissions(user, executor, permissions, securedObject);
    }

    public void setPermissions(User user, Executor executor, Collection<Permission> permissions, SecuredObject securedObject) {
        checkPermissionsOnExecutor(user, executor, Permission.READ);
        permissionDao.checkAllowed(user, Permission.UPDATE_PERMISSIONS, securedObject);
        permissionDao.setPermissions(executor, permissions, securedObject);
    }

    /**
     * Load executor's which already has (or not has) some permission on specified securedObject. This query using paging.
     * 
     * @param user
     *            Current actor {@linkplain User}.
     * @param securedObject
     *            {@linkplain SecuredObject} to load executors, which has (or not) permission on this securedObject.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} for loading executors.
     * @param hasPermission
     *            Flag equals true to load executors with permissions on {@linkplain SecuredObject}; false to load executors without permissions.
     * @return Executors with or without permission on {@linkplain SecuredObject} .
     */
    public List<? extends Executor> getExecutorsWithPermission(User user, SecuredObject securedObject, BatchPresentation batchPresentation,
            boolean hasPermission) {
        permissionDao.checkAllowed(user, Permission.READ_PERMISSIONS, securedObject);
        PresentationConfiguredCompiler<Executor> compiler = PresentationCompilerHelper.createExecutorWithPermissionCompiler(user, securedObject,
                batchPresentation, hasPermission);
        List<Executor> executors = compiler.getBatch();
        for (Executor privelegedExecutor : permissionDao.getPrivilegedExecutors(securedObject.getSecuredObjectType())) {
            if (batchPresentation.getType().getPresentationClass().isInstance(privelegedExecutor)
                    && permissionDao.isAllowed(user, Permission.READ, privelegedExecutor)) {
                if (hasPermission) {
                    executors.add(0, privelegedExecutor);
                } else {
                    executors.remove(privelegedExecutor);
                }
            }
        }
        return executors;
    }

    /**
     * Load executor's count which already has (or not has) some permission on specified securedObject.
     * 
     * @param user
     *            Current actor {@linkplain User}.
     * @param securedObject
     *            {@linkplain SecuredObject} to load executors, which has (or not) permission on this securedObject.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} for loading executors.
     * @param hasPermission
     *            Flag equals true to load executors with permissions on {@linkplain SecuredObject}; false to load executors without permissions.
     * @return Count of executors with or without permission on {@linkplain SecuredObject}.
     */
    public int getExecutorsWithPermissionCount(User user, SecuredObject securedObject, BatchPresentation batchPresentation, boolean hasPermission) {
        permissionDao.checkAllowed(user, Permission.READ_PERMISSIONS, securedObject);
        PresentationConfiguredCompiler<Executor> compiler = PresentationCompilerHelper.createExecutorWithPermissionCompiler(user, securedObject,
                batchPresentation, hasPermission);
        return compiler.getCount();
    }

    public SecuredObject findSecuredObject(SecuredObjectType type, Long id) {
        return securedObjectFactory.findById(type, id);
    }
}
