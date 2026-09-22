package ru.runa.wfe.var.dao;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.support.QueryBase;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.hibernate.HibernateQuery;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.SqlCommons;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.PermissionSubstitutions;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.SecurityCheckProperties;
import ru.runa.wfe.security.dao.PermissionDao;
import ru.runa.wfe.security.dao.QPermissionMapping;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.dao.ExecutorDao;
import ru.runa.wfe.var.CurrentVariable;
import ru.runa.wfe.var.QCurrentVariable;

@Component
@SuppressWarnings({ "unchecked", "rawtypes" })
public class CurrentVariableDao extends GenericDao<CurrentVariable> {

    public CurrentVariableDao() {
        super(CurrentVariable.class);
    }

    public CurrentVariable<?> get(CurrentProcess process, String name) {
        val v = QCurrentVariable.currentVariable;
        return queryFactory.selectFrom(v).where(v.process.eq(process).and(v.name.eq(name))).fetchFirst();
    }

    List<CurrentVariable<?>> getAllImpl(CurrentProcess process) {
        val v = QCurrentVariable.currentVariable;
        return queryFactory.selectFrom(v).where(v.process.eq(process)).fetch();
    }

    List<CurrentVariable<?>> getVariablesImpl(List<CurrentProcess> processesPart, List<String> variableNamesOrNull) {
        val v = QCurrentVariable.currentVariable;
        val q = queryFactory.selectFrom(v).where(v.process.in(processesPart));
        if (variableNamesOrNull != null) {
            q.where(v.name.in(variableNamesOrNull));
        }
        return q.fetch();
    }

    public void deleteAll(CurrentProcess process) {
        log.debug("deleting variables for process " + process.getId());
        val v = QCurrentVariable.currentVariable;
        queryFactory.delete(v).where(v.process.eq(process)).execute();
    }

    /**
     * Used by TNMS.
     */
    public List<CurrentVariable<?>> findNonEndedByNameLikeAndStringValueEqualTo(String variableNamePattern, String stringValue) {
        SqlCommons.StringEqualsExpression expression = SqlCommons.getStringEqualsExpression(variableNamePattern);

        return sessionFactory.getCurrentSession()
                .createQuery("from CurrentVariable " +
                        "where process.executionStatus != 'ENDED' " +
                        "  and name " + expression.getComparisonOperator() + " :name " +
                        "  and stringValue = :value")
                .setParameter("name", expression.getValue())
                .setParameter("value", stringValue)
                .list();
    }

    public List<CurrentVariable<?>> getVariablesByNameStartsWith(CurrentProcess process, String namePrefix) {
        final QCurrentVariable variable = QCurrentVariable.currentVariable;
        return queryFactory.selectFrom(variable).where(variable.process.eq(process).and(variable.name.startsWith(namePrefix))).fetch();
    }

    public Long getProcessCountByVariableNameAndValueContaining(Executor executor, String variableName, String variableValue) {
        final QCurrentVariable variable = QCurrentVariable.currentVariable;

        Predicate variableNameAndValuePredicate = createVariableNameAndValuePredicate(variable, variableName, variableValue);
        if (variableNameAndValuePredicate == null) {
            return 0L;
        }

        JPQLQuery<Long> baseQuery = queryFactory.select(variable.process.countDistinct()).from(variable);

        Set<Executor> executorWithGroups = getExecutorWithGroups(executor);

        if (!SecurityCheckProperties.isPermissionCheckRequired(SecuredObjectType.PROCESS)) {
            return baseQuery.where(variableNameAndValuePredicate).fetchOne();
        }
        if (isPrivilegedExecutor(SecuredObjectType.PROCESS, executorWithGroups)) {
            return baseQuery.where(variableNameAndValuePredicate).fetchOne();
        }

        PermissionSubstitutions.ForCheck subst = PermissionSubstitutions.getForCheck(
                SecuredObjectType.PROCESS,
                Permission.READ
        );

        JPQLQuery<Long> permissionQuery = buildPermissionQuery(baseQuery, executorWithGroups, subst);
        return permissionQuery.where(variableNameAndValuePredicate).fetchOne();
    }

    public List<CurrentVariable<?>> getVariablesByNameAndValueContaining(Executor executor, String variableName, String variableValue, int processLimit) {
        final QCurrentVariable variable = QCurrentVariable.currentVariable;

        Predicate variableNameAndValuePredicate = createVariableNameAndValuePredicate(
                QCurrentVariable.currentVariable,
                variableName,
                variableValue
        );
        if (variableNameAndValuePredicate == null) {
            return Collections.emptyList();
        }

        JPQLQuery<CurrentVariable<?>> baseQuery = queryFactory.selectFrom(variable);
        JPQLQuery<Long> processesQuery = queryFactory.select(variable.process.id)
                .distinct()
                .from(variable);

        if (!SecurityCheckProperties.isPermissionCheckRequired(SecuredObjectType.PROCESS)) {
            List<Long> processIds = processesQuery.where(variableNameAndValuePredicate)
                    .limit(processLimit)
                    .orderBy(variable.process.id.desc())
                    .fetch();
            return baseQuery.where(variable.process.id.in(processIds))
                    .where(variableNameAndValuePredicate)
                    .fetch();
        }

        Set<Executor> executorWithGroups = getExecutorWithGroups(executor);
        if (isPrivilegedExecutor(SecuredObjectType.PROCESS, executorWithGroups)) {
            List<Long> processIds = processesQuery.where(variableNameAndValuePredicate)
                    .limit(processLimit)
                    .orderBy(variable.process.id.desc())
                    .fetch();
            return baseQuery.where(variable.process.id.in(processIds))
                    .where(variableNameAndValuePredicate)
                    .fetch();
        }

        PermissionSubstitutions.ForCheck subst = PermissionSubstitutions.getForCheck(
                SecuredObjectType.PROCESS,
                Permission.READ
        );

        JPQLQuery<Long> permissionsProcessQuery = buildPermissionQuery(processesQuery, executorWithGroups, subst);
        List<Long> processIds = permissionsProcessQuery.where(variableNameAndValuePredicate)
                .limit(processLimit)
                .orderBy(variable.process.id.desc())
                .fetch();
        if (processIds.isEmpty()) {
            return Collections.emptyList();
        }

        return baseQuery.where(variable.process.id.in(processIds))
                .where(variableNameAndValuePredicate)
                .fetch();
    }

    private <T> JPQLQuery<T> buildPermissionQuery(
            JPQLQuery<T> baseQuery,
            Set<Executor> executorWithGroups,
            PermissionSubstitutions.ForCheck subst) {

        final QCurrentVariable variable = QCurrentVariable.currentVariable;
        final QPermissionMapping pm = QPermissionMapping.permissionMapping;

        return baseQuery.join(pm).on(pm.objectId.eq(variable.process.id))
                .where(pm.executor.in(executorWithGroups)
                        .and(pm.objectType.eq(SecuredObjectType.PROCESS))
                        .and(pm.permission.in(subst.selfPermissions)));
    }

    private Set<Executor> getExecutorWithGroups(Executor executor) {
        ExecutorDao executorDao = ApplicationContextFactory.getExecutorDao();
        Set<Executor> executorWithGroups = new HashSet<>(executorDao.getExecutorParentsAll(executor));
        executorWithGroups.add(executor);
        return executorWithGroups;
    }

    private boolean isPrivilegedExecutor(SecuredObjectType type, Set<Executor> executorWithGroups) {
        PermissionDao permissionDao = ApplicationContextFactory.getPermissionDao();
        for (Executor executor : executorWithGroups) {
            if (permissionDao.getPrivilegedExecutors(type).contains(executor)) {
                return true;
            }
        }
        return false;
    }

    private Predicate createVariableNameAndValuePredicate(QCurrentVariable variable, String variableName, String variableValue) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (variableName != null && !variableName.trim().isEmpty()) {
            String variableNameLikePattern = convertWildcardsToLike(variableName);
            booleanBuilder.and(variable.name.likeIgnoreCase(variableNameLikePattern));
        }

        if (variableValue != null && !variableValue.trim().isEmpty()) {
            String variableValueLikePattern = convertWildcardsToLike(variableValue);
            booleanBuilder.and(variable.stringValue.likeIgnoreCase(variableValueLikePattern));
        }

        return booleanBuilder.getValue();
    }

    private String convertWildcardsToLike(String pattern) {
        if (pattern == null) return null;

        pattern = pattern
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");

        pattern = pattern
                .replace("*", "%")
                .replace("?", "_");

        if (!pattern.contains("%") && !pattern.contains("_")) {
            pattern = "%" + pattern + "%";
        }

        return pattern;
    }
}
