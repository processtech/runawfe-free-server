package ru.runa.af.web.form;

import ru.runa.common.web.form.FileForm;

/**
 * @struts:form name = "deployDataSourceForm"
 */
public class DeployDataSourceForm extends FileForm {

    private static final long serialVersionUID = -6778295273263121817L;

    public static final String DATA_SOURCE_ID = "dataSourceId";

    private String dataSourceId;

    public String getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }
}
