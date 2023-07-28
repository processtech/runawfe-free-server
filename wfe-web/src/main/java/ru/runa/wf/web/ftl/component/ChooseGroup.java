package ru.runa.wf.web.ftl.component;

import freemarker.template.TemplateModelException;
import java.util.List;
import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * @deprecated code moved to {@link InputVariable}.
 * 
 * @author dofs
 * @since 4.0
 */
@Deprecated
public class ChooseGroup extends FormComponent {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object renderRequest() throws Exception {
        String varName = getParameterAsString(0);
        String view = getParameterAsString(1);
        List<Group> groups = (List<Group>) Delegates.getExecutorService().getExecutors(user, BatchPresentationFactory.GROUPS.createNonPaged());
        if ("raw".equals(view)) {
            return groups;
        } else if ("selectName".equals(view)) {
            StringBuffer html = new StringBuffer();
            html.append("<select name=\"").append(varName).append("\">");
            for (Group group : groups) {
                html.append("<option value=\"").append(group.getName()).append("\">").append(group.getName()).append("</option>");
            }
            html.append("</select>");
            return html.toString();
        } else if ("selectId".equals(view)) {
            WfVariable variable = variableProvider.getVariableNotNull(varName);
            return ViewUtil.createExecutorSelect(user, variable);
        } else {
            throw new TemplateModelException("Unexpected value of VIEW parameter: " + view);
        }
    }
}
