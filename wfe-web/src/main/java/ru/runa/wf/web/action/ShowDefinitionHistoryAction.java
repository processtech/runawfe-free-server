package ru.runa.wf.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdNameForm;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;

/**
 * @struts:action path="/show_definitions_history" name="idNameForm" validate="false"
 */
public class ShowDefinitionHistoryAction extends ActionBase {

    public static final String ACTION = "/show_definitions_history";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        String processName = ((IdNameForm) form).getName();
        BatchPresentation batchPresentation = ProfileHttpSessionHelper.getProfile(request.getSession()).getActiveBatchPresentation(
                "listProcessesDefinitionsHistoryForm");
        if (processName != null) {
            batchPresentation.getFilteredFields().clear();
            batchPresentation.getFilteredFields().put(0, new StringFilterCriteria(processName));
            batchPresentation.setFieldsToSort(new int[] { 1 }, new boolean[] { false });
            batchPresentation.setPageNumber(1);
        } else {
            ProfileHttpSessionHelper.reloadProfile(request.getSession());
        }
        return new ActionForward("/definitions_history.do");
    }
}
