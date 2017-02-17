package ru.runa.wfe.script.processes;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;

import com.google.common.base.Strings;

@XmlTransient()
public abstract class FilterableProcessInstancesOperation extends ScriptOperation {

    @XmlAttribute(name = AdminScriptConstants.NAME_ATTRIBUTE_NAME)
    public String name;

    @XmlAttribute(name = AdminScriptConstants.VERSION_ATTRIBUTE_NAME)
    public Long version;

    @XmlAttribute(name = AdminScriptConstants.ID_ATTRIBUTE_NAME)
    public Long id;

    @XmlAttribute(name = AdminScriptConstants.ID_FROM_ATTRIBUTE_NAME)
    public Long fromId;

    @XmlAttribute(name = AdminScriptConstants.ID_TO_ATTRIBUTE_NAME)
    public Long toId;

    @XmlAttribute(name = AdminScriptConstants.START_DATE_FROM_ATTRIBUTE_NAME)
    public String startDateFrom;

    @XmlAttribute(name = AdminScriptConstants.START_DATE_TO_ATTRIBUTE_NAME)
    public String startDateTo;

    @XmlAttribute(name = AdminScriptConstants.END_DATE_FROM_ATTRIBUTE_NAME)
    public String endDateFrom;

    @XmlAttribute(name = AdminScriptConstants.END_DATE_TO_ATTRIBUTE_NAME)
    public String endDateTo;

    @XmlAttribute(name = AdminScriptConstants.FINISHED_ATTRIBUTE_NAME)
    public Boolean onlyFinished;

    @Override
    public void validate(ScriptExecutionContext context) {
    }

    protected ProcessFilter createProcessFilter() {
        ProcessFilter filter = new ProcessFilter();
        if (!Strings.isNullOrEmpty(name)) {
            filter.setDefinitionName(name);
        }
        if (id != null) {
            filter.setId(id);
        } else {
            if (fromId != null) {
                filter.setIdFrom(fromId);
            }
            if (toId != null) {
                filter.setIdTo(toId);
            }
        }
        if (version != null) {
            filter.setDefinitionVersion(version);
        }
        if (onlyFinished != null) {
            filter.setFinished(onlyFinished);
        }
        if (!Strings.isNullOrEmpty(startDateFrom)) {
            filter.setStartDateFrom(CalendarUtil.convertToDate(startDateFrom, CalendarUtil.DATE_WITHOUT_TIME_FORMAT_STR));
        }
        if (!Strings.isNullOrEmpty(startDateTo)) {
            filter.setStartDateTo(CalendarUtil.convertToDate(startDateTo, CalendarUtil.DATE_WITHOUT_TIME_FORMAT_STR));
        }
        if (!Strings.isNullOrEmpty(endDateFrom)) {
            filter.setStartDateFrom(CalendarUtil.convertToDate(endDateFrom, CalendarUtil.DATE_WITHOUT_TIME_FORMAT_STR));
        }
        if (!Strings.isNullOrEmpty(endDateTo)) {
            filter.setEndDateTo(CalendarUtil.convertToDate(endDateTo, CalendarUtil.DATE_WITHOUT_TIME_FORMAT_STR));
        }
        return filter;
    }
}
