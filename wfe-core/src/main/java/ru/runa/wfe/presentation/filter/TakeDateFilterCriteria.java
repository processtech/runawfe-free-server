package ru.runa.wfe.presentation.filter;

import java.util.Map;

import ru.runa.wfe.presentation.hibernate.QueryParameter;

public class TakeDateFilterCriteria extends DateFilterCriteria{

    private static final long serialVersionUID = 1L;
    
    @Override
    public String buildWhereCondition(String fieldName, String persistetObjectQueryAlias, Map<String, QueryParameter> placeholders) {
        final StringBuilder sb = new StringBuilder(super.buildWhereCondition(fieldName, persistetObjectQueryAlias, placeholders));
        sb.append("and ").append(persistetObjectQueryAlias).append(".executor is not null ");
        return sb.toString();
    }
}
