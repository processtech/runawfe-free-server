package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.StrIdsForm;
import ru.runa.wfe.service.delegate.Delegates;

public class DeleteDataSourceAction extends ActionBase {

    public static final String DELETE_DATA_SOURCE_ACTION_PATH = "/delete_data_source";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        String[] dataSourceToDeleteIds = ((StrIdsForm) form).getStrIds();
        if (dataSourceToDeleteIds.length == 0) {
            return mapping.findForward(Resources.FORWARD_SUCCESS);
        }
        for (String dataSourceId : dataSourceToDeleteIds) {
            Delegates.getDataSourceService().removeDataSource(getLoggedUser(request), dataSourceId);
        }
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }
}
