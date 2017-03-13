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
package ru.runa.wfe.var.format;

import java.util.Date;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.CalendarUtil;

/**
 * Created on 30.11.2004
 *
 */
public class TimeFormat extends AbstractDateFormat {

    public TimeFormat() {
        super(CalendarUtil.HOURS_MINUTES_FORMAT_STR);
    }

    @Override
    public String getName() {
        return "time";
    }

    @Override
    protected Date convertFromStringValue(String source) {
        Date date = super.convertFromStringValue(source);
        if (!CalendarUtil.areCalendarsEqualIgnoringTime(CalendarUtil.dateToCalendar(date), CalendarUtil.getZero())) {
            throw new InternalApplicationException("Time " + source + " does not belong to day range");
        }
        return date;
    }

    @Override
    public <TResult, TContext> TResult processBy(VariableFormatVisitor<TResult, TContext> operation, TContext context) {
        return operation.onTime(this, context);
    }
}
