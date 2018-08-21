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
package ru.runa.wfe.presentation.hibernate;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.hibernate.Hibernate;
import org.springframework.util.Assert;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.querydsl.HibernateQueryFactory;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DbSource.AccessType;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.presentation.FieldState;
import ru.runa.wfe.presentation.filter.FilterCriteria;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.PermissionSubstitutions;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.dao.PermissionDao;
import ru.runa.wfe.security.dao.QPermissionMapping;
import ru.runa.wfe.user.dao.ExecutorDao;

/**
 * Builds HQL query for {@link BatchPresentation}.
 */
public class HibernateCompilerHqlBuider {

    /**
     * {@link BatchPresentation}, used to build HQL query.
     */
    private final BatchPresentation batchPresentation;

    /**
     * Parameters, used to build query.
     */
    private final CompilerParameters parameters;

    /**
     * Mapping from {@link FieldDescriptor} to HQL query parameters aliases.
     */
    private final HibernateCompilerAliasMapping aliasMapping;

    /**
     * HQL query is builded here.
     */
    private final StringBuilder query = new StringBuilder(128);

    /**
     * Flag, equals true, if HQL query must be tuned for correct inheritance filtering.
     */
    private boolean isFilterByInheritance;

    /**
     * Flag, equals true, if HQL query must be tuned for correct inheritance ordering.
     */
    private boolean isOrderByInheritance;

    /**
     * Map from HQL positional parameter name to parameter value. All place holders must be put into this map (maybe with null values). It will be
     * used later to replace positional parameters in SQL query.
     */
    private final QueryParametersMap placeholders = new QueryParametersMap();

    /**
     * Creates component to build HQL query for {@link BatchPresentation}.
     * 
     * @param batchPresentation
     *            {@link BatchPresentation}, used to build HQL query.
     * @param parameters
     *            Parameters, used to build query.
     */
    public HibernateCompilerHqlBuider(BatchPresentation batchPresentation, CompilerParameters parameters) {
        this.batchPresentation = batchPresentation;
        this.parameters = parameters;
        aliasMapping = new HibernateCompilerAliasMapping(batchPresentation);
    }

    /**
     * Returns Map from HQL positional parameter name to parameter value, generated after build method call.
     * 
     * @return Map from HQL positional parameter name to parameter value.
     */
    public QueryParametersMap getPlaceholders() {
        return placeholders;
    }

    /**
     * Returns HQL query string, generated after build method call.
     * 
     * @return HQL query string.
     */
    public String getQuery() {
        return query.toString();
    }

    /**
     * Check, if query has some filters on fields with inheritance. This method must be called after build method call.
     * 
     * @return Flag, equals true, if HQL query must be tuned for correct inheritance filtering.
     */
    public boolean isFilterByInheritance() {
        return isFilterByInheritance;
    }

    /**
     * Check, if query has some sorting on fields with inheritance. This method must be called after build method call.
     * 
     * @return Flag, equals true, if HQL query must be tuned for correct inheritance ordering.
     */
    public boolean isOrderByInheritance() {
        return isOrderByInheritance;
    }

    /**
     * Returns mapping from {@link FieldDescriptor} to HQL query parameters aliases, initialized after build method call.
     * 
     * @return Mapping from {@link FieldDescriptor} to HQL query parameters aliases.
     */
    public HibernateCompilerAliasMapping getAliasMapping() {
        return aliasMapping;
    }

    /**
     * Builds HQL query for batch presentation according to parameters.
     */
    public void build() {
        buildSelectClause();
        buildFromClauseForAliases();
        buildWhereClause();
        buildOrderClause();
    }

    /**
     * Builds 'select' HQL clause and 'from' clause with root persistent object.
     */
    private void buildSelectClause() {
        if (parameters.isCountQuery()) {
            query.append("select count (").append(ClassPresentation.classNameSQL).append(")");
        } else {
            query.append("select ").append(ClassPresentation.classNameSQL);
            if (parameters.isOnlyIdentityLoad()) {
                query.append(".id");
            }
        }
        query.append(" from ");
        if (parameters.getQueriedClass() != null) {
            query.append(parameters.getQueriedClass().getName());
        } else {
            query.append(batchPresentation.getType().getPresentationClass().getName());
        }
        query.append(" as ").append(ClassPresentation.classNameSQL);
    }

    /**
     * Append to HQL query 'from' clause aliases for fields, with persistent object differs from root.
     */
    private void buildFromClauseForAliases() {
        for (String alias : aliasMapping.getAliases()) {
            if (alias.equals(ClassPresentation.classNameSQL)) {
                continue;
            }
            final List<FieldDescriptor> fields = aliasMapping.getFields(alias);
            for (final FieldDescriptor field : fields) {
                if (!HibernateCompilerHelper.isFieldSQLAffects(field, batchPresentation)) {
                    continue;
                }
                query.append(", ").append(field.dbSources[0].getSourceObject().getName()).append(" as ").append(alias);
                break;
            }
        }
    }

    /**
     * Builds where clause.
     */
    private void buildWhereClause() {
        List<String> conditions = new LinkedList<>();
        conditions.addAll(addClassPresentationRestriction());
        conditions.addAll(addJoinFieldRestrictions());
        conditions.addAll(addOwners());
        conditions.addAll(addFilters());
        conditions.addAll(addSecureCheck());
        conditions.addAll(addIdRestrictions());
        query.append(" where (1=1)");
        for (String condition : conditions) {
            query.append(" and (").append(condition).append(")");
        }
    }

    /**
     * Generates expressions to satisfy {@link ClassPresentation} restriction.
     * 
     * @return List of string, represents expressions.
     */
    private List<String> addClassPresentationRestriction() {
        List<String> result = new LinkedList<>();
        String restrictions = batchPresentation.getType().getRestrictions();
        if (!Strings.isNullOrEmpty(restrictions)) {
            result.add(restrictions);
        }
        return result;
    }

    /**
     * Generates expressions to satisfy fields join restrictions (How to join root persistent object with field database source).
     * 
     * @return List of string, represents expressions.
     */
    private List<String> addJoinFieldRestrictions() {
        List<String> result = new LinkedList<>();
        for (String alias : aliasMapping.getAliases()) {
            if (alias.equals(ClassPresentation.classNameSQL)) {
                continue;
            }
            for (final FieldDescriptor field : aliasMapping.getFields(alias)) {
                if (!HibernateCompilerHelper.isFieldSQLAffects(field, batchPresentation)) {
                    continue;
                }
                String joinExpr = field.dbSources[0].getJoinExpression(alias);
                if (Strings.isNullOrEmpty(joinExpr)) {
                    continue;
                }
                StringBuilder joinRestriction = new StringBuilder();
                joinRestriction.append("((").append(joinExpr).append(")");
                if (field.displayName.startsWith(ClassPresentation.removable_prefix)) {
                    String propertyDBPath = field.displayName.substring(ClassPresentation.removable_prefix.length(),
                            field.displayName.indexOf(':', ClassPresentation.removable_prefix.length()));
                    joinRestriction.append(" and (").append(alias).append(".").append(propertyDBPath).append("=:removableUserValue")
                            .append(field.fieldIdx).append(")");
                    placeholders.add("removableUserValue" + field.fieldIdx, field.displayName.substring(field.displayName.lastIndexOf(':') + 1));
                }
                joinRestriction.append(")");
                result.add(joinRestriction.toString());
                break;
            }
        }
        return result;
    }

    /**
     * Generates expressions to satisfy owners restrictions.
     * 
     * @return List of string, represents expressions.
     */
    private List<String> addOwners() {
        List<String> result = new LinkedList<>();
        if (!parameters.hasOwners()) {
            return result;
        }
        String owners = "(" + ClassPresentation.classNameSQL + "." + parameters.getOwnerDBPath() + " in (:ownersIds) )";
        placeholders.add("ownersIds", parameters.getOwners());
        result.add(owners);
        return result;
    }

    /**
     * Generates expressions to satisfy fields filtering restrictions. This function doesn't generates filtering for fields with inheritance. It must
     * be handled in SQL translation stage.
     * 
     * @return List of string, represents expressions.
     */
    private List<String> addFilters() {
        List<String> result = new LinkedList<>();
        Map<Integer, FilterCriteria> fieldsToFilter = batchPresentation.getFilteredFields();
        for (Map.Entry<Integer, FilterCriteria> entry : fieldsToFilter.entrySet()) {
            FieldDescriptor field = batchPresentation.getAllFields()[entry.getKey()];
            if (field.fieldState == FieldState.DISABLED
                    || (field.filterMode != FieldFilterMode.DATABASE && field.filterMode != FieldFilterMode.DATABASE_ID_RESTRICTION)) {
                continue;
            }
            if (field.dbSources[0].getValueDBPath(AccessType.FILTER, null) == null && field.dbSources.length > 1) {
                isFilterByInheritance = true;
                continue; // Fields with inheritance will be processed later
            }
            if (field.filterMode == FieldFilterMode.DATABASE) {
                StringBuilder filter = new StringBuilder();
                String condition = entry.getValue().buildWhereCondition(
                        field.dbSources[0].getValueDBPath(AccessType.FILTER, aliasMapping.getAlias(field)), placeholders);
                filter.append("(").append(condition).append(")");
                result.add(filter.toString());
            }
            if (field.filterMode == FieldFilterMode.DATABASE_ID_RESTRICTION) {
                StringBuilder filter = new StringBuilder();
                String condition = entry.getValue().buildWhereCondition(field.dbSources[0].getValueDBPath(AccessType.FILTER, "subQuery"),
                        placeholders);
                filter.append("(").append(ClassPresentation.classNameSQL).append(".id IN (SELECT ")
                        .append(field.dbSources[0].getJoinExpression("subQuery")).append(" FROM ")
                        .append(field.dbSources[0].getSourceObject().getName()).append(" AS subQuery WHERE ").append(condition).append("))");
                result.add(filter.toString());
            }
        }
        return result;
    }

    /**
     * Generates expressions to satisfy security restrictions (to load only objects with permission).
     * 
     * @return List of string, represents expressions.
     */
    // TODO Largely duplicates PermissionDAO logic. After (if ever) BatchPresentation uses QueryDSL, try to merge duplicates.
    private List<String> addSecureCheck() {
        List<String> result = new LinkedList<>();
        RestrictionsToPermissions pp = parameters.getPermissionRestrictions();
        if (pp == null) {
            return result;
        }

        // Check all types have same list type and permission substitutions. List type must not be null, since we're querying list.
        // TODO After ACTOR and GROUP types are merged into EXECUTOR, consider to replace `types` to single `type`.
        SecuredObjectType listType = pp.types[0].getListType();
        PermissionSubstitutions.ForCheck subst = PermissionSubstitutions.getForCheck(pp.types[0], pp.permission);
        Assert.notNull(listType);
        for (int i = 1;  i < pp.types.length;  i++) {
            Assert.isTrue(listType == pp.types[i].getListType());
            Assert.isTrue(Objects.equals(subst, PermissionSubstitutions.getForCheck(pp.types[i], pp.permission)));
        }

        ExecutorDao executorDao = ApplicationContextFactory.getExecutorDao();
        PermissionDao permissionDao = ApplicationContextFactory.getPermissionDao();
        HibernateQueryFactory queryFactory = HibernateQueryFactory.getInstance();

        // Need to check privileged & list permissions only once.
        // ATTENTION!!! Also, HQL query with two conditions (on both type and listType) always returns empty rowset. :(
        List<Long> executorIds = executorDao.getActorAndNotTemporaryGroupsIds(pp.user.getActor());
        if (permissionDao.hasPrivilegedExecutor(executorIds)) {
            return result;
        }
        QPermissionMapping pm = QPermissionMapping.permissionMapping;
        if (!subst.listPermissions.isEmpty() && queryFactory.select(pm.id).from(pm)
                .where(pm.executor.id.in(executorIds)
                        .and(pm.objectType.eq(listType))
                        .and(pm.objectId.eq(0L))
                        .and(pm.permission.in(subst.listPermissions)))
                .fetchFirst() != null) {
            return result;
        }

        // TODO After Spring upgrade (to 4 or 5, don't know), try to use lambdas (see commented code below).
        ArrayList<String> typeNames = new ArrayList<>(pp.types.length);
        for (SecuredObjectType t : pp.types) {
            typeNames.add(t.getName());
        }
        ArrayList<String> permissionNames = new ArrayList<>(subst.selfPermissions.size());
        for (Permission p : subst.selfPermissions) {
            permissionNames.add(p.getName());
        }

        result.add("(instance.id in (select pm.objectId from PermissionMapping pm where pm.executor.id in (:securedOwnerIds) and " +
                "pm.objectType in (:securedTypes) and pm.permission in (:securedPermissions)" +
                "))");
        placeholders.add("securedOwnerIds", executorIds);
//        placeholders.add("securedTypes", Arrays.stream(types).map(SecuredObjectType::getName).collect(Collectors.toList()), Hibernate.STRING);
//        placeholders.add("securedPermissions", subst.selfPermissions.stream().map(Permission::getName).collect(Collectors.toList()), Hibernate.STRING);
        placeholders.add("securedTypes", typeNames, Hibernate.STRING);
        placeholders.add("securedPermissions", permissionNames, Hibernate.STRING);

        return result;
    }

    /**
     * Generates expressions for identity restrictions.
     * 
     * @return List of string, represents expressions.
     */
    private List<String> addIdRestrictions() {
        List<String> result = new LinkedList<>();
        if (parameters.getIdRestriction() == null) {
            return result;
        }
        for (String restriction : parameters.getIdRestriction()) {
            if (!Strings.isNullOrEmpty(restriction)) {
                result.add(ClassPresentation.classNameSQL + ".id " + restriction);
            }
        }
        return result;
    }

    /**
     * Builds 'order by' clause of HQL query. This function doesn't build sorting for fields with inheritance. It must be handled in SQL translation
     * stage.
     */
    private void buildOrderClause() {
        if (parameters.isCountQuery()) {
            return;
        }
        FieldDescriptor[] sortedFields = batchPresentation.getSortedFields();
        boolean[] fieldsToSortModes = batchPresentation.getFieldsToSortModes();
        if (sortedFields.length == 0) {
            return;
        }
        query.append(" order by");
        boolean needComma = false;
        for (int i = 0; i < sortedFields.length; i++) {
            if (!sortedFields[i].sortable || sortedFields[i].fieldState == FieldState.DISABLED) {
                continue;
            }
            if (sortedFields[i].dbSources[0].getValueDBPath(AccessType.ORDER, null) == null && sortedFields[i].dbSources.length > 1) {
                isOrderByInheritance = true;
                continue; // Fields with inheritance will be processed later
            }
            query.append(needComma ? ", " : " ").append(
                    sortedFields[i].dbSources[0].getValueDBPath(AccessType.ORDER, aliasMapping.getAlias(sortedFields[i])));
            query.append(fieldsToSortModes[i] ? " asc" : " desc");
            needComma = true;
        }
    }
}
