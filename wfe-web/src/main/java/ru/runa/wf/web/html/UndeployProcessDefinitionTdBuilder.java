package ru.runa.wf.web.html;

import com.google.common.collect.Maps;
import java.util.Map;
import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import ru.runa.common.web.Commons;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.form.IdVersionForm;
import ru.runa.common.web.html.BaseTdBuilder;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.UndeployProcessDefinitionAction;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.execution.CurrentProcessClassPresentation;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.filter.LongFilterCriteria;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @since 4.3.0
 */
public class UndeployProcessDefinitionTdBuilder extends BaseTdBuilder {

    public UndeployProcessDefinitionTdBuilder() {
        super(Permission.DELETE);
    }

    @Override
    public TD build(Object object, Env env) {
        WfDefinition definition = (WfDefinition) object;
        BatchPresentation presentation = BatchPresentationFactory.CURRENT_PROCESSES.createDefault();
        int definitionNameFieldIndex = presentation.getType().getFieldIndex(CurrentProcessClassPresentation.DEFINITION_NAME);
        int definitionVersionFieldIndex = presentation.getType().getFieldIndex(CurrentProcessClassPresentation.DEFINITION_VERSION);
        presentation.getFilteredFields().put(definitionNameFieldIndex, new StringFilterCriteria(definition.getName()));
        presentation.getFilteredFields().put(definitionVersionFieldIndex, new LongFilterCriteria(definition.getVersion()));
        int allCount = Delegates.getExecutionService().getProcessesCount(env.getUser(), presentation);
        ConcreteElement element;
        if (isEnabled(object, env) && allCount == 0) {
            Map<String, Object> parameters = Maps.newHashMap();
            parameters.put(IdForm.ID_INPUT_NAME, definition.getId());
            parameters.put(IdVersionForm.VERSION_INPUT_NAME, definition.getVersion());
            String url = Commons.getActionUrl(UndeployProcessDefinitionAction.ACTION_PATH, parameters, env.getPageContext(), PortletUrlType.Render);
            element = new A(url, MessagesProcesses.BUTTON_UNDEPLOY_DEFINITION.message(env.getPageContext()));
        } else {
            element = new StringElement();
        }
        TD td = new TD(element);
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        return MessagesProcesses.BUTTON_UNDEPLOY_DEFINITION.message(env.getPageContext());
    }
}
