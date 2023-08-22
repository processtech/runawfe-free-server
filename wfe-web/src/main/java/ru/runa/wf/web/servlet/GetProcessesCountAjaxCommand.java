package ru.runa.wf.web.servlet;

import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.execution.ProcessClassPresentation;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.filter.DateFilterCriteria;
import ru.runa.wfe.presentation.filter.LongFilterCriteria;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

public class GetProcessesCountAjaxCommand extends JsonAjaxCommand {
    @Override
    protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
        String definitionName = request.getParameter("definitionName");
        if (definitionName == null) {
            throw new InternalApplicationException("definition name not found");
        }
        Long definitionVersion = Long.valueOf(request.getParameter("definitionVersion"));
        if (definitionVersion == null) {
            throw new InternalApplicationException("definition version not found");
        }
        BatchPresentation presentation = BatchPresentationFactory.PROCESSES.createDefault();
        int definitionNameFieldIndex = presentation.getType().getFieldIndex(ProcessClassPresentation.DEFINITION_NAME);
        int definitionVersionFieldIndex = presentation.getType().getFieldIndex(ProcessClassPresentation.DEFINITION_VERSION);
        int processEndDateFieldIndex = presentation.getType().getFieldIndex(ProcessClassPresentation.PROCESS_END_DATE);
        presentation.getFilteredFields().put(definitionNameFieldIndex, new StringFilterCriteria(definitionName));
        presentation.getFilteredFields().put(definitionVersionFieldIndex, new LongFilterCriteria(definitionVersion));
        presentation.getFilteredFields().put(processEndDateFieldIndex, new DateFilterCriteria());
        int activeCount = Delegates.getExecutionService().getProcessesCount(user, presentation);
        int allCount = Delegates.getExecutionService().getProcessesCount(user, presentation);

        JSONObject object = new JSONObject();
        object.put("activeProcessesCount", activeCount);
        object.put("allProcessesCount", allCount);

        return object;
    }
}
