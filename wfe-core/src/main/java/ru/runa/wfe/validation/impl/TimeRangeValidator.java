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
package ru.runa.wfe.validation.impl;

import java.util.Calendar;
import java.util.Date;

import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.TypeConversionUtil;

public class TimeRangeValidator extends AbstractRangeValidator<Date> {

    private Date getParameter(String name) {
        Calendar baseDate = TypeConversionUtil.convertTo(Calendar.class, getFieldValue());
        Calendar param = getParameter(Calendar.class, name, null);
        if (param == null) {
            return null;
        }
        CalendarUtil.setDateFromCalendar(param, baseDate);
        return param.getTime();
    }

    @Override
    protected Date getMaxComparatorValue() {
        return getParameter("max");
    }

    @Override
    protected Date getMinComparatorValue() {
        return getParameter("min");
    }

}
