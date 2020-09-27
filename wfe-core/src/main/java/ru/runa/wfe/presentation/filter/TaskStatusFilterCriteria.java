package ru.runa.wfe.presentation.filter;

import ru.runa.wfe.audit.aggregated.TaskEndReason;
import ru.runa.wfe.commons.SqlCommons;
import ru.runa.wfe.commons.SqlCommons.StringEqualsExpression;
import ru.runa.wfe.presentation.hibernate.QueryParametersMap;

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
	public String buildWhereCondition(String aliasedFieldName, QueryParametersMap placeholders) {
        StringEqualsExpression expression = SqlCommons.getStringEqualsExpression(getFilterTemplate(0));
        String alias = makePlaceHolderName(aliasedFieldName);
        StringBuilder paramStringBuilder = new StringBuilder();
        paramStringBuilder.append(expression.getComparisonOperator());
        paramStringBuilder.append(":");
        paramStringBuilder.append(alias);
        placeholders.add(alias, expression.getValue());

        int taskStatus = TASK_STATUS_ACTIVE;
        if (!getFilterTemplate(1).isEmpty()) {
            taskStatus = Integer.parseInt(getFilterTemplate(1));
        }
        StringBuilder where = new StringBuilder("( ");
        where.append(aliasedFieldName).append(paramStringBuilder);

        switch (taskStatus) {
            case TASK_STATUS_ACTIVE:
            where.append(" and ").append(aliasedFieldName.replace(".taskName", ".endReason")).append(" = '").append(TaskEndReason.PROCESSING.name())
                    .append("'");
                break;
            case TASK_STATUS_HAS_ASSIGNED:
            where.append(" and ").append(aliasedFieldName.replace(".taskName", ".endReason")).append(" = '").append(TaskEndReason.COMPLETED.name())
                    .append("'");
                break;
            case TASK_STATUS_HAS_NOT_STARTED:
            where.delete(0, where.length()).append(
                    "subQuery.processId NOT IN (SELECT subquery2.processId FROM ru.runa.wfe.audit.aggregated.TaskAggregatedLog as subquery2 WHERE (subquery2.taskName=:subQuerytaskName)");
        }

        where.append(")");
        return where.toString();
	}

}
