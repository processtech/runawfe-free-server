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
package ru.runa.wfe.security.logic;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mysema.commons.lang.CloseableIterator;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
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
import org.dom4j.Document;
import org.dom4j.Element;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.commons.logic.PresentationCompilerHelper;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.QDeployment;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.PresentationConfiguredCompiler;
import ru.runa.wfe.security.AuthorizationException;
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

import static ru.runa.wfe.security.SecuredObjectType.ACTOR;
import static ru.runa.wfe.security.SecuredObjectType.DEFINITION;
import static ru.runa.wfe.security.SecuredObjectType.GROUP;

/**
 * Created on 14.03.2005
 */
public class AuthorizationLogic extends CommonLogic {

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
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IdAndPermission that = (IdAndPermission) o;
            return Objects.equals(id, that.id) &&
                    Objects.equals(permission, that.permission);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, permission);
        }
    }

    public void checkAllowedUpdateExecutor(User user, Executor object) {
        if (!isAllowedUpdateExecutor(user, object)) {
            throw new AuthorizationException("User " + user + " does not have permissions to update " + object);
        }
    }

    public boolean isAllowed(User user, Permission permission, SecuredObject object) {
        return permissionDAO.isAllowed(user, permission, object.getSecuredObjectType(), object.getIdentifiableId());
    }

    public boolean isAllowed(User user, Permission permission, SecuredObjectType securedObjectType, Long identifiableId) {
        return permissionDAO.isAllowed(user, permission, securedObjectType, identifiableId);
    }

    public <T extends SecuredObject> boolean[] isAllowed(User user, Permission permission, List<T> securedObjects) {
        return permissionDAO.isAllowed(user, permission, securedObjects);
    }

    public boolean isAllowedForAny(User user, Permission permission, SecuredObjectType securedObjectType) {
        return permissionDAO.isAllowedForAny(user, permission, securedObjectType);
    }

    public boolean isAllowedUpdateExecutor(User user, Executor object) {
        return isAllowed(user, Permission.UPDATE, object) || (
                Objects.equals(user.getActor().getId(), object.getId()) &&
                        isAllowed(user, Permission.UPDATE_SELF, SecuredSingleton.EXECUTORS)
        );
    }

    public boolean isAllowedUpdateExecutor(User user, Long id) {
        return isAllowedUpdateExecutor(user, executorDAO.getExecutor(id));
    }

    public List<Permission> getIssuedPermissions(User user, Executor performer, SecuredObject securedObject) {
        checkPermissionsOnExecutor(user, performer, Permission.LIST);
        permissionDAO.checkAllowed(user, Permission.LIST, securedObject);
        return permissionDAO.getIssuedPermissions(performer, securedObject);
    }

    /**
     * Exports permissions to xml, see: manage_datafile, ExportDataFileAction.
     * <p>
     * Placed here and added all that PermissionService stuff, because must be executed under transaction.
     */
    public void exportDataFile(User user, Document script) {
        permissionDAO.checkAllowed(user, Permission.ALL, SecuredSingleton.DATAFILE);
        Element parentElement = script.getRootElement();
        QPermissionMapping pm = QPermissionMapping.permissionMapping;
        QExecutor e = QExecutor.executor;

        // Export permissions of all singletons.
        {
            List<SecuredObjectType> allTypes = SecuredObjectType.values();
            ArrayList<SecuredObjectType> singletonTypes = new ArrayList<>(allTypes.size());
            for (SecuredObjectType t : allTypes) {
                if (t.isSingleton()) {
                    singletonTypes.add(t);
                }
            }
            exportDataFileImpl(parentElement, queryFactory.select(pm.permission, e.name, pm.objectType)
                    .from(pm, e)
                    .where(pm.objectType.in(singletonTypes).and(pm.objectId.eq(0L)).and(pm.executor.eq(e)))
                    .orderBy(pm.objectType.asc(), e.name.asc(), pm.permission.asc()));
        }

        // Export ACTOR and GROUP permissions.
        {
            QExecutor e2 = new QExecutor("e2");  // same table as `e`, but different alias
            exportDataFileImpl(parentElement, queryFactory.select(pm.permission, e.name, pm.objectType, e2.name)
                    .from(pm, e, e2)
                    .where(pm.objectType.in(ACTOR, GROUP).and(pm.objectId.eq(e2.id)).and(pm.executor.eq(e)))
                    .orderBy(pm.objectType.asc(), e2.name.asc(), e.name.asc(), pm.permission.asc()));
        }

        // Export DEFINITION permissions.
        {
            QDeployment d = QDeployment.deployment;
            exportDataFileImpl(parentElement, queryFactory.select(pm.permission, e.name, pm.objectType, d.name)
                    .from(pm, e, d)
                    .where(pm.objectType.eq(DEFINITION).and(pm.objectId.eq(d.id)).and(pm.executor.eq(e)))
                    .orderBy(d.name.asc(), e.name.asc(), pm.permission.asc()));
        }
    }

    /**
     *
     * @param parentElement  Parent for "addPermissions" elements.
     * @param query  Must return fields in order: permission, executorName, objectType, [objectName].
     */
    private void exportDataFileImpl(Element parentElement, JPQLQuery<Tuple> query) {
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

                //noinspection ConstantConditions
                addPermissionsElement.addElement("permission", XmlUtils.RUNA_NAMESPACE).addAttribute("name", permission.getName());
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

    private void setPermissionsImpl(User user, String executorName, Map<SecuredObjectType, Set<String>> objectNames, Set<Permission> permissions,
            boolean deleteExisting) {
        Executor executor = executorDAO.getExecutor(executorName);  // [QSL] Only id is needed, or maybe even join would be enough.
        permissionDAO.checkAllowed(user, Permission.LIST, executor);

        QPermissionMapping pm = QPermissionMapping.permissionMapping;

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
                    objectIds = SecuredObjectFactory.getInstance().getIdsByNames(type, new HashSet<>(namesPart));
                }
                permissionDAO.checkAllowedForAll(user, Permission.UPDATE_PERMISSIONS, type, objectIds);

                HashSet<IdAndPermission> existing = new HashSet<>();
                try (CloseableIterator<Tuple> i = queryFactory.select(pm.objectId, pm.permission)
                        .from(pm)
                        .where(pm.executor.eq(executor)
                                .and(pm.objectType.eq(type))
                                .and(pm.objectId.in(objectIds))
                                .and(pm.permission.in(permissions)))
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
                    BooleanExpression cond = Expressions.FALSE;
                    for (IdAndPermission ip : existing) {
                        cond = cond.or(pm.objectId.eq(ip.id).and(pm.permission.eq(ip.permission)));
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
     *
     * @param objectNames Non-empty. Contains null values for singleton keys.
     * @param permissions Null if called from removeAllPermissions().
     */
    private void removePermissionsImpl(User user, String executorName, Map<SecuredObjectType, Set<String>> objectNames, Set<Permission> permissions) {
        Executor executor = executorDAO.getExecutor(executorName);  // [QSL] Only id is needed, or maybe even join would be enough.
        permissionDAO.checkAllowed(user, Permission.LIST, executor);

        QPermissionMapping pm = QPermissionMapping.permissionMapping;

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
                    objectIds = SecuredObjectFactory.getInstance().getIdsByNames(type, new HashSet<>(namesPart));
                }
                permissionDAO.checkAllowedForAll(user, Permission.UPDATE_PERMISSIONS, type, objectIds);

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
        List<Executor> executors = executorDAO.getExecutors(executorIds);
        for (Executor executor : executors) {
            setPermissions(user, executor, permissions, securedObject);
        }
    }

    public void setPermissions(User user, List<Long> executorIds, List<Collection<Permission>> permissions, SecuredObject securedObject) {
        List<Executor> executors = executorDAO.getExecutors(executorIds);
        Preconditions.checkArgument(executors.size() == permissions.size(), "arrays length differs");
        for (int i = 0; i < executors.size(); i++) {
            setPermissions(user, executors.get(i), permissions.get(i), securedObject);
        }
    }

    public void setPermissions(User user, Long executorId, Collection<Permission> permissions, SecuredObject securedObject) {
        Executor executor = executorDAO.getExecutor(executorId);
        setPermissions(user, executor, permissions, securedObject);
    }

    public void setPermissions(User user, Executor executor, Collection<Permission> permissions, SecuredObject securedObject) {
        checkPermissionsOnExecutor(user, executor, Permission.LIST);
        permissionDAO.checkAllowed(user, Permission.UPDATE_PERMISSIONS, securedObject);
        permissionDAO.setPermissions(executor, permissions, securedObject);
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
        permissionDAO.checkAllowed(user, Permission.READ_PERMISSIONS, securedObject);
        PresentationConfiguredCompiler<Executor> compiler = PresentationCompilerHelper.createExecutorWithPermissionCompiler(user, securedObject,
                batchPresentation, hasPermission);
        if (hasPermission) {
            List<Executor> executors = compiler.getBatch();
            for (Executor privelegedExecutor : permissionDAO.getPrivilegedExecutors(securedObject.getSecuredObjectType())) {
                if (batchPresentation.getType().getPresentationClass().isInstance(privelegedExecutor)
                        && permissionDAO.isAllowed(user, Permission.LIST, privelegedExecutor)) {
                    executors.add(0, privelegedExecutor);
                }
            }
            return executors;
        } else {
            return compiler.getBatch();
        }
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
        permissionDAO.checkAllowed(user, Permission.READ_PERMISSIONS, securedObject);
        PresentationConfiguredCompiler<Executor> compiler = PresentationCompilerHelper.createExecutorWithPermissionCompiler(user, securedObject,
                batchPresentation, hasPermission);
        return compiler.getCount();
    }

}
