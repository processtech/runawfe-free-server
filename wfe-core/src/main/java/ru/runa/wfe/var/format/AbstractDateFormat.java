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

import ru.runa.wfe.commons.CalendarUtil;

public abstract class AbstractDateFormat extends VariableFormat {
    private final String format;

    public AbstractDateFormat(String format) {
        this.format = format;
    }

    @Override
    public Class<Date> getJavaClass() {
        return Date.class;
    }

    @Override
    protected String convertToStringValue(Object object) {
        return CalendarUtil.format((Date) object, format);
    }

    @Override
    protected Date convertFromStringValue(String source) {
        return CalendarUtil.convertToDate(source, format);
    }

    @Override
    public Object parseJSON(String json) {
        if (json == null) {
            return null;
        }
        return convertFromStringValue(json);
    }

    @Override
    protected Object convertToJSONValue(Object value) {
        return convertToStringValue(value);
    }
}
