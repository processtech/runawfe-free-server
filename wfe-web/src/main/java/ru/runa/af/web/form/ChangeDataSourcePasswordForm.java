package ru.runa.af.web.form;

import org.apache.struts.action.ActionForm;

/**
 * @struts:form name = "changeDataSourcePasswordForm"
 */
public class ChangeDataSourcePasswordForm extends ActionForm {

    private static final long serialVersionUID = -6778295273263421817L;

    public static final String DATA_SOURCE_ID = "dataSourceId";
    public static final String DATA_SOURCE_PASSWORD = "dataSourcePassword";

    private String dataSourceId;
    private String dataSourcePassword;

    public String getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getDataSourcePassword() {
        return dataSourcePassword;
    }

    public void setDataSourcePassword(String dataSourcePassword) {
        this.dataSourcePassword = dataSourcePassword;
    }
}
