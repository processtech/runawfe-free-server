package ru.runa.wfe.presentation.filter;

import java.util.Map;

import ru.runa.wfe.commons.SQLCommons;
import ru.runa.wfe.commons.SQLCommons.StringEqualsExpression;
import ru.runa.wfe.presentation.hibernate.QueryParameter;

/**
 * 
 * @author estet90
 * @since 4.3.0
 *
 */
public class TaskStatusFilterCriteria extends FilterCriteria {
	private static final long serialVersionUID = 1L;

	private static final int TASK_STATUS_ACTIVE = 0;
	private static final int TASK_STATUS_HAS_ASSIGNED = 1;
	private static final int TASK_STATUS_HAS_NOT_STARTED = 2;
	
	public TaskStatusFilterCriteria() {
		super(2);
	}
    
	@Override
	public String buildWhereCondition(String aliasedFieldName, Map<String, QueryParameter> placeholders) {
        StringEqualsExpression expression = SQLCommons.getStringEqualsExpression(getFilterTemplate(0));
        String alias = makePlaceHolderName(aliasedFieldName);
        StringBuilder paramStringBuilder = new StringBuilder();
        paramStringBuilder.append(expression.getComparisonOperator());
        paramStringBuilder.append(":");
        paramStringBuilder.append(alias);
        placeholders.put(alias, new QueryParameter(alias, expression.getValue()));
        
        int taskStatus = TASK_STATUS_ACTIVE;
        if (!getFilterTemplate(1).isEmpty())
        	taskStatus = Integer.parseInt(getFilterTemplate(1)); 
        StringBuilder where = new StringBuilder("( ");
        where.append(aliasedFieldName).append(paramStringBuilder);
        if (taskStatus == TASK_STATUS_ACTIVE)
        	where.append(" and ").append(aliasedFieldName.replace(".taskName", ".endReason")).append(" = 0");
        else if (taskStatus == TASK_STATUS_HAS_ASSIGNED)
        	where.append(" and ").append(aliasedFieldName.replace(".taskName", ".endReason")).append(" = 1");
        //else if (taskStatus == TASK_STATUS_HAS_NOT_STARTED)
        //TODO задача ещё не выполнялась
        
        where.append(")");
        return where.toString();
	}

}
