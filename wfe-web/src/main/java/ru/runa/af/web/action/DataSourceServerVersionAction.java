package ru.runa.af.web.action;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.datasource.JdbcDataSource;

public class DataSourceServerVersionAction extends ActionBase {

    public static final String DATA_SOURCE_SERVER_VERSION_ACTION_PATH = "/data_source_server_version";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        try {
            response.getWriter().write(((JdbcDataSource) DataSourceStorage.getDataSource(request.getParameter("dataSourceId"))).serverVersion());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
