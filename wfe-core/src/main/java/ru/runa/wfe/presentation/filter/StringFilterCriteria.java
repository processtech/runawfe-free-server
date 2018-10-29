package ru.runa.wfe.presentation.filter;

import ru.runa.wfe.commons.SqlCommons;
import ru.runa.wfe.commons.SqlCommons.StringEqualsExpression;
import ru.runa.wfe.presentation.hibernate.QueryParametersMap;

public class StringFilterCriteria extends FilterCriteria {
    public static final String ANY_SYMBOLS = SqlCommons.ANY_SYMBOLS;
    public static final String ANY_SYMBOL = SqlCommons.ANY_SYMBOL;
    private static final long serialVersionUID = -1849845246809052465L;
    private boolean ignoreCase;

    public StringFilterCriteria() {
        super(1);
    }

    public StringFilterCriteria(String filterValue) {
        super(new String[] { filterValue });
    }

    public StringFilterCriteria(String filterValue, boolean ignoreCase) {
        this(filterValue);
        this.ignoreCase = ignoreCase;
    }

    @Override
    public String buildWhereCondition(String aliasedFieldName, QueryParametersMap placeholders) {
        final String template = getFilterTemplate(0);
        if (template.contains(",")) {
            return buildInOperator(aliasedFieldName);
        } else if (template.contains("-")) {
            return buildBetweenOperator(aliasedFieldName);
        }
        StringEqualsExpression expression = SqlCommons.getStringEqualsExpression(template);
        String searchValue = expression.getValue();
        String alias = makePlaceHolderName(aliasedFieldName);
        String where = "";
        if (ignoreCase) {
            where += "lower(";
        }
        where += aliasedFieldName;
        if (ignoreCase) {
            where += ")";
            searchValue = searchValue.toLowerCase();
        }
        where += " ";
        where += expression.getComparisonOperator();
        where += " :" + alias + " ";
        placeholders.add(alias, searchValue);
        return where;
    }

}
