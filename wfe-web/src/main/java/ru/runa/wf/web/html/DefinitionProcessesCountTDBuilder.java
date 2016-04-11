package ru.runa.wf.web.html;

import org.apache.ecs.html.TD;

import ru.runa.common.web.html.BaseTDBuilder;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.execution.ProcessClassPresentation;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.filter.DateFilterCriteria;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.service.delegate.Delegates;

public class DefinitionProcessesCountTDBuilder extends BaseTDBuilder {

    public DefinitionProcessesCountTDBuilder() {
        super(null);
    }

    @Override
    public TD build(Object object, Env env) {
        TD td = new TD(getValue(object, env));
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        WfDefinition definition = (WfDefinition) object;
        BatchPresentation presentation = BatchPresentationFactory.PROCESSES.createDefault();
        int definitionNameFieldIndex = presentation.getClassPresentation().getFieldIndex(ProcessClassPresentation.DEFINITION_NAME);
        int definitionVersionFieldIndex = presentation.getClassPresentation().getFieldIndex(ProcessClassPresentation.DEFINITION_VERSION);
        int processEndDateFieldIndex = presentation.getClassPresentation().getFieldIndex(ProcessClassPresentation.PROCESS_END_DATE);
        presentation.getFilteredFields().put(definitionNameFieldIndex, new StringFilterCriteria(definition.getName()));
        presentation.getFilteredFields().put(definitionVersionFieldIndex, new StringFilterCriteria(String.valueOf(definition.getVersion())));
        int allCount = Delegates.getExecutionService().getProcessesCount(env.getUser(), presentation);
        presentation.getFilteredFields().put(processEndDateFieldIndex, new DateFilterCriteria());
        int activeCount = Delegates.getExecutionService().getProcessesCount(env.getUser(), presentation);
        return "<span style='color: darkgreen'>" + activeCount + "</span> / " + allCount;
    }
}
