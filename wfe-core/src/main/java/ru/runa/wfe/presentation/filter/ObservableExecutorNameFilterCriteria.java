package ru.runa.wfe.presentation.filter;

import java.util.Map;

import ru.runa.wfe.presentation.hibernate.QueryParameter;

public class ObservableExecutorNameFilterCriteria extends FilterCriteria {
    private static final long serialVersionUID = 1L;

    public ObservableExecutorNameFilterCriteria() {
        super(1);
    }

    @Override
    public String buildWhereCondition(String aliasedFieldName, Map<String, QueryParameter> placeholders) {
        return "(1 = 1)";
    }

}
