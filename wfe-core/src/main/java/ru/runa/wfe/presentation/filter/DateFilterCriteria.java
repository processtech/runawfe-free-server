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

import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.presentation.hibernate.QueryParameter;

import com.google.common.base.Strings;

public class DateFilterCriteria extends FilterCriteria {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(DateFilterCriteria.class);

    private Date dateStart;
    private Date dateEnd;

    public DateFilterCriteria() {
        super(2);
    }

    public DateFilterCriteria(Date fromDate, Date toDate) {
        applyFilterTemplates(new String[] { CalendarUtil.formatDateTime(fromDate), CalendarUtil.formatDateTime(toDate) });
    }

    @Override
    protected void validate(String[] newTemplates) throws FilterFormatException {
        super.validate(newTemplates);
        try {
            if (!Strings.isNullOrEmpty(newTemplates[0])) {
                CalendarUtil.convertToDate(newTemplates[0], CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
            }
            if (!Strings.isNullOrEmpty(newTemplates[1])) {
                CalendarUtil.convertToDate(newTemplates[1], CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
            }
        } catch (Exception e) {
            throw new FilterFormatException(e.getMessage());
        }
    }

    private void initDates() {
        try {
            if (!Strings.isNullOrEmpty(getFilterTemplate(0))) {
                dateStart = CalendarUtil.convertToDate(getFilterTemplate(0), CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
            }
            if (!Strings.isNullOrEmpty(getFilterTemplate(1))) {
                dateEnd = CalendarUtil.convertToDate(getFilterTemplate(1), CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
            }
        } catch (Exception e) {
            log.error("date parsing error: " + e);
        }
    }

    @Override
    public String buildWhereCondition(String aliasedFieldName, Map<String, QueryParameter> placeholders) {
        initDates();

        String placeholderStart = makePlaceHolderName(aliasedFieldName + "Start");
        String placeholderEnd = makePlaceHolderName(aliasedFieldName + "End");

        StringBuilder whereStringBuilder = new StringBuilder(aliasedFieldName);

        if (dateStart == null) {
            if (dateEnd == null) {
                // empty date (NULL value)
                whereStringBuilder.append(" is null");
            } else {
                // less than
                whereStringBuilder.append(" < :").append(placeholderEnd);
            }
        } else {
            if (dateEnd == null) {
                // more than
                whereStringBuilder.append(" > :").append(placeholderStart);
            } else {
                // between
                whereStringBuilder.append(" between :");
                whereStringBuilder.append(placeholderStart);
                whereStringBuilder.append(" and :");
                whereStringBuilder.append(placeholderEnd);
            }
        }

        if (dateStart != null) {
            placeholders.put(placeholderStart, new QueryParameter(placeholderStart, dateStart));
        }
        if (dateEnd != null) {
            placeholders.put(placeholderEnd, new QueryParameter(placeholderEnd, dateEnd));
        }

        whereStringBuilder.append(" ");
        return whereStringBuilder.toString();
    }
}
