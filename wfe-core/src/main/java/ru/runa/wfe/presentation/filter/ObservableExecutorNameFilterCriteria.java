package ru.runa.wfe.presentation.filter;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.presentation.hibernate.QueryParameter;

public class ObservableExecutorNameFilterCriteria extends FilterCriteria {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(ObservableExecutorNameFilterCriteria.class);

    public ObservableExecutorNameFilterCriteria() {
        super(1);
    }

    @Override
    public String buildWhereCondition(String aliasedFieldName, Map<String, QueryParameter> placeholders) {
        return "(1 = 1)";
    }

}
