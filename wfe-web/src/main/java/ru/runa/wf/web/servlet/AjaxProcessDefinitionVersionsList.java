package ru.runa.wf.web.servlet;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

public class AjaxProcessDefinitionVersionsList extends JsonAjaxCommand {

    @Override
    protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
        String definitionName = request.getParameter("definitionName");
        BatchPresentation batchPresentation = getBatchPresentation(definitionName);
        List<WfDefinition> definitions = Delegates.getDefinitionService().getDeployments(user, batchPresentation, false);
        return createJSONResponse(definitions);
    }

    @SuppressWarnings("unchecked")
    private JSONObject createJSONResponse(List<WfDefinition> definitions) {
        JSONObject root = new JSONObject();
        JSONArray data = new JSONArray();
        for (WfDefinition definition : definitions) {
            data.add(definitionToJson(definition));
        }
        root.put("data", data);
        return root;
    }

    @SuppressWarnings("unchecked")
    private JSONObject definitionToJson(WfDefinition definition) {
        JSONObject result = new JSONObject();
        result.put("id", definition.getId());
        result.put("version", definition.getVersion());
        String description = definition.getDescription();
        result.put("description", description != null ? description : "");
        result.put("createDate", CalendarUtil.formatDateTime(definition.getCreateDate()));
        result.put("createUserName", definition.getCreateActor() != null ? definition.getCreateActor().getFullName() : "");
        result.put("updateDate", definition.getUpdateDate() != null ? CalendarUtil.formatDateTime(definition.getUpdateDate()) : "");
        result.put("updateUserName", definition.getUpdateActor() != null ? definition.getUpdateActor().getFullName() : "");
        return result;
    }

    private BatchPresentation getBatchPresentation(String definitionName) {
        BatchPresentation batchPresentation = BatchPresentationFactory.DEFINITIONS_HISTORY.createDefault();
        batchPresentation.getFilteredFields().clear();
        batchPresentation.getFilteredFields().put(0, new StringFilterCriteria(definitionName));
        batchPresentation.setFieldsToSort(new int[] { 1 }, new boolean[] { false });
        batchPresentation.setFieldsToGroup(new int[] {});
        return batchPresentation;
    }

}
