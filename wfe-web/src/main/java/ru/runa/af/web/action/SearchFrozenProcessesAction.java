package ru.runa.af.web.action;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.af.web.form.FrozenProcessesForm;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wf.web.tag.ShowFrozenProcessesTag;
import ru.runa.wfe.execution.dao.FrozenTokenFilterHandler;
import ru.runa.wfe.execution.process.check.FrozenProcessFilter;
import ru.runa.wfe.execution.process.check.FrozenProcessSearchData;
import ru.runa.wfe.execution.process.check.FrozenProcessesSearchParameter;
import ru.runa.wfe.service.delegate.Delegates;

public class SearchFrozenProcessesAction extends ActionBase {
    public static final String ACTION_PATH = "/searchFrozenProcesses";
    public static final String FIRST_FILTER_DATE = "firstFilterDate";
    public static final String SECOND_FILTER_DATE = "secondFilterDate";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        try {
            handleFrozenProcessForm(actionForm, request, response);
        } catch (Exception e) {
            addError(request, e);
        }
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), new HashMap<>());
    }

    private void handleFrozenProcessForm(ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        FrozenProcessesForm frozenProcessesForm = (FrozenProcessesForm) form;
        // Checkbox and input values
        request.setAttribute(FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_TIMER_NODES.getName(),
                String.valueOf(frozenProcessesForm.isSearchFrozenInTimerNodes()));
        request.setAttribute(FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_UNEXPECTED_NODES.getName(),
                String.valueOf(frozenProcessesForm.isSearchFrozenInUnexpectedNodes()));
        request.setAttribute(FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_TASK_NODES.getName(),
                String.valueOf(frozenProcessesForm.isSearchFrozenInTaskNodes()));
        request.setAttribute(FrozenProcessesSearchParameter.SEARCH_FROZEN_BY_SIGNAL_TIME_EXCEEDED.getName(),
                String.valueOf(frozenProcessesForm.isSearchFrozenBySignalTimeExceeded()));
        request.setAttribute(FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_PARALLEL_GATEWAYS.getName(),
                String.valueOf(frozenProcessesForm.isSearchFrozenInParallelGateways()));
        request.setAttribute(FrozenProcessesSearchParameter.SEARCH_FROZEN_BY_SUBPROCESSES.getName(),
                String.valueOf(frozenProcessesForm.isSearchFrozenSubprocesses()));
        request.setAttribute(FrozenProcessesSearchParameter.SEARCH_FROZEN_BY_SIGNAL.getName(),
                String.valueOf(frozenProcessesForm.isSearchFrozenBySignal()));
        request.setAttribute(FrozenProcessesSearchParameter.SEARCH_FROZEN_BY_SIGNAL_TIME_EXCEEDED.getValueName(),
                String.valueOf(frozenProcessesForm.getSearchFrozenBySignalTimeExceededTimeLapse()));
        request.setAttribute(FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_TASK_NODES.getValueName(),
                String.valueOf(frozenProcessesForm.getSearchFrozenInTaskNodesTimeThreshold()));
        // Filters
        request.setAttribute(FrozenProcessFilter.PROCESS_NAME.getName(), String.valueOf(frozenProcessesForm.getProcessName()));
        request.setAttribute(FrozenProcessFilter.PROCESS_VERSION.getName(), String.valueOf(frozenProcessesForm.getProcessVersion()));
        request.setAttribute(FrozenProcessFilter.NODE_TYPE.getName(), String.valueOf(frozenProcessesForm.getNodeType()));
        request.setAttribute(FIRST_FILTER_DATE, String.valueOf(frozenProcessesForm.getFirstFilterDate()));
        request.setAttribute(SECOND_FILTER_DATE, String.valueOf(frozenProcessesForm.getSecondFilterDate()));

        // Getting tokens
        Map<FrozenProcessFilter, String> filters = new EnumMap<>(FrozenProcessFilter.class);
        processRequestFilters(filters, request);
        Map<String, FrozenProcessSearchData> searchData = new HashMap<>();
        processRequestForRequiredSearchParameters(searchData, request);

        request.setAttribute(ShowFrozenProcessesTag.FROZEN_TOKENS,
                Delegates.getExecutionService().getFrozenTokens(Commons.getUser(request.getSession()), searchData, filters));
        request.setAttribute(ShowFrozenProcessesTag.SEARCH_STARTED, true);
    }

    private void processRequestFilters(Map<FrozenProcessFilter, String> filters, HttpServletRequest request) {
        for (FrozenProcessFilter filter : FrozenProcessFilter.values()) {
            if (filter == FrozenProcessFilter.NODE_ENTER_DATE) {
                String firstFilterDateValue = (String) request.getAttribute(FIRST_FILTER_DATE);
                String secondFilterDateValue = (String) request.getAttribute(SECOND_FILTER_DATE);
                if (firstFilterDateValue != null && secondFilterDateValue != null) {
                    filters.put(filter, firstFilterDateValue + FrozenTokenFilterHandler.DATE_DELIMITER + secondFilterDateValue);
                }
            } else {
                String filterValue = (String) request.getAttribute(filter.getName());
                if (filterValue != null && filterValue.length() > 0) {
                    filters.put(filter, filterValue);
                }
            }
        }

    }

    private void processRequestForRequiredSearchParameters(Map<String, FrozenProcessSearchData> searchData, HttpServletRequest request) {
        for (FrozenProcessesSearchParameter type : FrozenProcessesSearchParameter.values()) {
            Object booleanValue = request.getAttribute(type.getName());
            Object integerValue = request.getAttribute(type.getValueName());
            FrozenProcessSearchData data = null;
            if (booleanValue != null) {
                if (Boolean.parseBoolean((String) booleanValue)) {
                    data = new FrozenProcessSearchData(type.getName());
                    searchData.put(data.getSeekerId(), data);
                }
                if (integerValue != null && data != null) {
                    data.setTimeValue(Integer.parseInt((String) integerValue));
                }
            }
        }
    }
}
