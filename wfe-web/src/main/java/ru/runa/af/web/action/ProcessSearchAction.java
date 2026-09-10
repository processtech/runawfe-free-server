package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.af.web.form.ProcessSearchForm;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.runa.common.web.Commons.getUser;


public class ProcessSearchAction extends ActionBase {
    public static final String ACTION_PATH = "/searchProcesses";
    public static final String VARIABLE_VALUE = "variableValue";
    public static final String RECORD_SHOW_COUNT = "recordShowCount";
    public static final String VARIABLE_NAME = "variableName";
    public static final String SEARCH_RESULTS = "searchResults";
    public static final String SEARCH_STARTED = "searchStarted";
    public static final String TOTAL_PROCESS_FOUND_COUNT = "totalProcessFoundCount";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        try {
            ProcessSearchForm processSearchForm = (ProcessSearchForm) form;
            String variableValue = processSearchForm.getVariableValue();
            String variableName = processSearchForm.getVariableName();
            int recordShowCount = processSearchForm.getRecordShowCount();
            User user = getUser(request.getSession());

            Map<WfProcess, Map<String, String>> processVariablesMap = Delegates.getExecutionService()
                    .getProcessesByVariableNameAndValueContaining(user, variableName, variableValue, recordShowCount);
            Long totalProcessFoundCount = Delegates.getExecutionService()
                    .getProcessCountByVariableNameAndValueContaining(user, variableName, variableValue);

            processVariablesMap = processVariablesMap.entrySet()
                    .stream()
                    .sorted(Comparator.comparing(entry -> entry.getKey().getId(), Comparator.reverseOrder()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));

            request.setAttribute(TOTAL_PROCESS_FOUND_COUNT, totalProcessFoundCount);
            request.setAttribute(SEARCH_RESULTS, processVariablesMap);
            request.setAttribute(SEARCH_STARTED, true);
            request.setAttribute(VARIABLE_VALUE, variableValue);
            request.setAttribute(VARIABLE_NAME, variableName);
            request.setAttribute(RECORD_SHOW_COUNT, recordShowCount);
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), new HashMap<>());
        }
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), new HashMap<>());
    }
}