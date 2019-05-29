package ru.runa.wfe.presentation;

import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.presentation.filter.EnumerationFilterCriteria;
import ru.runa.wfe.presentation.filter.FilterCriteria;

/**
 * Implementation of {@link FilterCriteria} to select only {@link SystemLog} of some type.
 */
public class SystemLogTypeFilterCriteria extends EnumerationFilterCriteria {
    private static final long serialVersionUID = 1L;

    /**
     * Creates filter for system log type filtering.
     */
    public SystemLogTypeFilterCriteria() {
        super(SystemLogTypeHelper.getValues());
    }
}
