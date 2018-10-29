package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.af.web.form.ChangeDataSourcePasswordForm;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.datasource.DataSourceStorage;

public class ChangeDataSourcePasswordAction extends ActionBase {

    public static final String CHANGE_DATA_SOURCE_PASSWORD_ACTION_PATH = "/change_data_source_password";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        ChangeDataSourcePasswordForm pswForm = (ChangeDataSourcePasswordForm) form;
        DataSourceStorage.changePassword(pswForm.getDataSourceId(), pswForm.getDataSourcePassword());
        return null;
    }
}
