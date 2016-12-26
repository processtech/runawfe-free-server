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
package ru.runa.wfe.presentation.filter;

import java.util.Calendar;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.bc.BusinessDuration;
import ru.runa.wfe.commons.bc.BusinessDurationParser;
import ru.runa.wfe.presentation.filter.dialect.DurationDialectFactory;
import ru.runa.wfe.presentation.filter.dialect.IDurationDialect;
import ru.runa.wfe.presentation.hibernate.QueryParameter;

public class TaskDurationFilterCriteria extends FilterCriteria {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(TaskDurationFilterCriteria.class);

    private BusinessDuration durationStart;
    private BusinessDuration durationEnd;

    public TaskDurationFilterCriteria() {
        super(4);
    }

    @Override
    protected void validate(String[] newTemplates) throws FilterFormatException {
        super.validate(newTemplates);
        try {
            if (newTemplates[0].length() > 0) {
                parseDuration(newTemplates[0] + " " + newTemplates[1]);
            }
            if (newTemplates[2].length() > 0) {
                parseDuration(newTemplates[2] + " " + newTemplates[1]);
            }
        } catch (Exception e) {
            throw new FilterFormatException(e.getMessage());
        }
    }

    private void initTimes() {
        try {
            if (getFilterTemplate(0).length() > 0) {
                durationStart = parseDuration(getFilterTemplate(0) + " " + getFilterTemplate(1));
            }
            if (getFilterTemplate(2).length() > 0) {
                durationEnd = parseDuration(getFilterTemplate(2) + " " + getFilterTemplate(3));
            }
        } catch (Exception e) {
            log.error("time parsing error: " + e);
        }
    }

    private BusinessDuration parseDuration(final String parDuration) {
        final BusinessDuration parsedDuration = BusinessDurationParser.parse(parDuration);
        final BusinessDuration normalizedDuration = new BusinessDuration(parsedDuration.getCalendarField(), parsedDuration.getAmount(), true);
        int calendarField = normalizedDuration.getCalendarField();
        int amount = normalizedDuration.getAmount();
        if (Calendar.DAY_OF_YEAR == calendarField) {
            calendarField = Calendar.HOUR;
            amount *= 24;
        }
        return new BusinessDuration(calendarField, amount, true);
    }

    @Override
    public String buildWhereCondition(String aliasedFieldName, Map<String, QueryParameter> placeholders) {
        initTimes();
        final IDurationDialect durationDialect = DurationDialectFactory.createDialect();
        final String placeholderStart = makePlaceHolderName(aliasedFieldName + "Start");
        final String placeholderEnd = makePlaceHolderName(aliasedFieldName + "End");
        final String wrappedPlaceholderStart = durationDialect.wrapParameter(placeholderStart);
        final String wrappedPlaceholderEnd = durationDialect.wrapParameter(placeholderEnd);

        StringBuilder whereStringBuilder = new StringBuilder(durationDialect.convertOperator(aliasedFieldName));

        if (durationStart == null) {
            if (durationEnd == null) {
                // empty date (NULL value)
                whereStringBuilder.append(" is null");
            } else {
                // less than
                whereStringBuilder.append(" <= ").append(wrappedPlaceholderEnd);
            }
        } else {
            if (durationEnd == null) {
                // more than
                whereStringBuilder.append(" >= ").append(wrappedPlaceholderStart);
            } else {
                // between
                whereStringBuilder.append(" between ");
                whereStringBuilder.append(wrappedPlaceholderStart);
                whereStringBuilder.append(" and ");
                whereStringBuilder.append(wrappedPlaceholderEnd);
            }
        }

        if (durationStart != null) {
            placeholders.put(placeholderStart, new QueryParameter(placeholderStart, durationDialect.convertValue(durationStart.getAmount())));
        }
        if (durationEnd != null) {
            placeholders.put(placeholderEnd, new QueryParameter(placeholderEnd, durationDialect.convertValue(durationEnd.getAmount())));
        }

        whereStringBuilder.append(" ");
        return whereStringBuilder.toString();
    }
}
