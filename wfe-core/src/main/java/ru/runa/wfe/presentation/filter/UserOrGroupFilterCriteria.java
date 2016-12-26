package ru.runa.wfe.presentation.filter;

import java.util.Map;

import ru.runa.wfe.commons.SQLCommons;
import ru.runa.wfe.commons.SQLCommons.StringEqualsExpression;
import ru.runa.wfe.presentation.hibernate.QueryParameter;

public class UserOrGroupFilterCriteria extends FilterCriteria {
    private static final long serialVersionUID = 1L;

    public UserOrGroupFilterCriteria() {
        super(2);
    }

    @Override
    public String buildWhereCondition(String aliasedFieldName, Map<String, QueryParameter> placeholders) {
        boolean includeGroup = false;
        if (!getFilterTemplate(1).isEmpty()) {
            includeGroup = 1 == Integer.parseInt(getFilterTemplate(1));
        }
        final StringEqualsExpression expression = SQLCommons.getStringEqualsExpression(getFilterTemplate(0));
        final String alias = makePlaceHolderName(aliasedFieldName);
        final StringBuilder paramStringBuilder = new StringBuilder();
        paramStringBuilder.append(expression.getComparisonOperator());
        paramStringBuilder.append(":");
        paramStringBuilder.append(alias);
        placeholders.put(alias, new QueryParameter(alias, expression.getValue()));

        final StringBuilder where = new StringBuilder();
        where.append("( ").append(aliasedFieldName).append(paramStringBuilder);
        if (includeGroup) {
            where.append(" OR ").append(aliasedFieldName);
            where.append(" in ( select distinct egm.group.name from ru.runa.wfe.user.ExecutorGroupMembership as egm where egm.executor.name");
            where.append(paramStringBuilder).append(")");
        }
        where.append(")");
        return where.toString();
    }
}
