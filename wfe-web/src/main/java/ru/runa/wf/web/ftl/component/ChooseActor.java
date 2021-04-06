package ru.runa.wf.web.ftl.component;

import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.var.dto.WfVariable;
import freemarker.template.TemplateModelException;

/**
 * @deprecated code moved to {@link InputVariable}.
 * 
 * @author dofs
 * @since 4.0
 */
@Deprecated
public class ChooseActor extends FormComponent {

    private static final long serialVersionUID = 1L;

    @Override
    protected Object renderRequest() throws Exception {
        String actorVarName = getParameterAsString(0);
        String view = getParameterAsString(1);
        if ("all".equals(view)) {
            WfVariable variable = variableProvider.getVariableNotNull(actorVarName);
            return ViewUtil.createExecutorSelect(user, variable);
        } else if ("raw".equals(view)) {
            BatchPresentation batchPresentation = BatchPresentationFactory.ACTORS.createNonPaged();
            int[] sortIds = { 1 };
            boolean[] sortOrder = { true };
            batchPresentation.setFieldsToSort(sortIds, sortOrder);
            return Delegates.getExecutorService().getExecutors(user, batchPresentation);
        } else {
            throw new TemplateModelException("Unexpected value of VIEW parameter: " + view);
        }
    }

}
