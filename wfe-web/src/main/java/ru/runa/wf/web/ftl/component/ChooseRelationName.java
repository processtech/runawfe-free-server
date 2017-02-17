package ru.runa.wf.web.ftl.component;

import java.util.List;

import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.base.Objects;

public class ChooseRelationName extends FormComponent {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object renderRequest() {
        String variableName = getParameterAsString(0);
        Object value = variableProvider.getValue(variableName);
        BatchPresentation batchPresentation = BatchPresentationFactory.RELATIONS.createNonPaged();
        List<Relation> relations = Delegates.getRelationService().getRelations(user, batchPresentation);
        StringBuilder html = new StringBuilder("<select name=\"").append(variableName).append("\">");
        html.append("<option value=\"\"> ------------------------- </option>");
        for (Relation relation : relations) {
            html.append("<option");
            if (Objects.equal(relation.getName(), value)) {
                html.append(" selected");
            }
            html.append(">").append(relation.getName()).append("</option>");
        }
        html.append("</select>");
        return html;
    }

}
