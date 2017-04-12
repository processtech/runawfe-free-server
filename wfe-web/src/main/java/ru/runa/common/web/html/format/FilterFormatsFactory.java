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
package ru.runa.common.web.html.format;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ru.runa.wfe.presentation.SystemLogTypeFilterCriteria;
import ru.runa.wfe.presentation.SystemLogTypeHelper;
import ru.runa.wfe.presentation.filter.AnywhereStringFilterCriteria;
import ru.runa.wfe.presentation.filter.ObservableExecutorNameFilterCriteria;
import ru.runa.wfe.presentation.filter.TaskDurationFilterCriteria;
import ru.runa.wfe.presentation.filter.UserOrGroupFilterCriteria;
import ru.runa.wfe.var.Variable;

/**
 * Powered by Dofs
 */
public class FilterFormatsFactory {

    private static Map<String, FilterTDFormatter> formattersMap = new HashMap<String, FilterTDFormatter>();
    private static FiltersParser filtersParser = new FilterParserImpl();

    static {
        formattersMap.put(String.class.getName(), new StringFilterTDFormatter());
        formattersMap.put(Integer.class.getName(), new StringFilterTDFormatter());
        formattersMap.put(Date.class.getName(), new DateFilterTDFormatter());
        formattersMap.put(AnywhereStringFilterCriteria.class.getName(), new StringFilterTDFormatter());
        formattersMap.put(Variable.class.getName(), new StringFilterTDFormatter());
        formattersMap.put(SystemLogTypeFilterCriteria.class.getName(), new StringEnumerationFilterTDFormatter(SystemLogTypeHelper.getValues()));
        formattersMap.put(UserOrGroupFilterCriteria.class.getName(), new UserOrGroupFilterTDFormatter());
        formattersMap.put(TaskDurationFilterCriteria.class.getName(), new DurationFilterTDFormatter());
        formattersMap.put(ObservableExecutorNameFilterCriteria.class.getName(), new ObservableExecutorNameFilterTDFormatter());
    }

    public static FilterTDFormatter getFormatter(String fieldType) {
        return formattersMap.get(fieldType);
    }

    public static FiltersParser getParser() {
        return filtersParser;
    }
}
