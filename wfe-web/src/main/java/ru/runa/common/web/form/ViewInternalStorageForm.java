package ru.runa.common.web.form;

import org.apache.struts.action.ActionForm;

/**
 * @struts:form name = "viewInternalStorageForm"
 */
public class ViewInternalStorageForm extends ActionForm {
    private static final long serialVersionUID = 1L;

    public static final int MODE_DOWNLOAD = 5;

    private int mode;
    private String workbookPath;
    private String sheetName;

    public String getWorkbookPath() {
        return workbookPath;
    }

    public void setWorkbookPath(String workbookPath) {
        this.workbookPath = workbookPath;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

}
