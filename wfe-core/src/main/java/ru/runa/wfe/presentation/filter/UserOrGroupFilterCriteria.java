package ru.runa.wfe.presentation.filter;

import java.util.Map;

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

        final StringLikeFilter likeFilter = StringFilterCriteria.calcUseLike(getFilterTemplate(0));
        final String alias = makePlaceHolderName(aliasedFieldName);
        final StringBuilder paramStringBuilder = new StringBuilder();
        if (likeFilter.isUseLike()) {
            paramStringBuilder.append(" like ");
        } else {
            paramStringBuilder.append(" = ");
        }
        paramStringBuilder.append(":");
        paramStringBuilder.append(alias);
        placeholders.put(alias, new QueryParameter(alias, likeFilter.getSearchFilter()));

        final StringBuilder whereStringBuilder = new StringBuilder();
        whereStringBuilder.append("( ").append(aliasedFieldName).append(paramStringBuilder);
        if (includeGroup) {
            whereStringBuilder.append(" OR ").append(aliasedFieldName);
            whereStringBuilder.append(" in ( select distinct egm.group.name from ru.runa.wfe.user.ExecutorGroupMembership as egm where egm.executor.name");
            whereStringBuilder.append(paramStringBuilder).append(")");
        }
        whereStringBuilder.append(")");
        return whereStringBuilder.toString();
    }
}
