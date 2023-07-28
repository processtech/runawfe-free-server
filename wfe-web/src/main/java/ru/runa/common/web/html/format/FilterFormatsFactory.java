package ru.runa.common.web.html.format;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import ru.runa.wfe.presentation.SystemLogTypeFilterCriteria;
import ru.runa.wfe.presentation.SystemLogTypeHelper;
import ru.runa.wfe.presentation.filter.AnywhereStringFilterCriteria;
import ru.runa.wfe.presentation.filter.TaskDurationFilterCriteria;
import ru.runa.wfe.presentation.filter.TaskStatusFilterCriteria;
import ru.runa.wfe.presentation.filter.UserOrGroupFilterCriteria;
import ru.runa.wfe.var.ArchivedVariable;
import ru.runa.wfe.var.CurrentVariable;

/**
 * Powered by Dofs
 */
public class FilterFormatsFactory {

    private static Map<String, FilterTDFormatter> formattersMap = new HashMap<>();
    private static FilterParser filtersParser = new FilterParser();

    static {
        formattersMap.put(String.class.getName(), new StringFilterTDFormatter2());
        formattersMap.put(Integer.class.getName(), new StringFilterTDFormatter());
        formattersMap.put(Long.class.getName(), new StringFilterTDFormatter());
        formattersMap.put(Date.class.getName(), new DateFilterTDFormatter());
        formattersMap.put(AnywhereStringFilterCriteria.class.getName(), new StringFilterTDFormatter());
        formattersMap.put(CurrentVariable.class.getName(), new StringFilterTDFormatter());
        formattersMap.put(ArchivedVariable.class.getName(), new StringFilterTDFormatter());
        formattersMap.put(SystemLogTypeFilterCriteria.class.getName(), new StringEnumerationFilterTDFormatter(SystemLogTypeHelper.getValues()));
        formattersMap.put(UserOrGroupFilterCriteria.class.getName(), new UserOrGroupFilterTDFormatter());
        formattersMap.put(TaskDurationFilterCriteria.class.getName(), new DurationFilterTDFormatter());
        formattersMap.put(TaskStatusFilterCriteria.class.getName(), new TaskStatusFilterTDFormatter());
    }

    public static FilterTDFormatter getFormatter(String fieldType) {
        return formattersMap.get(fieldType);
    }

    public static FilterParser getParser() {
        return filtersParser;
    }
}
