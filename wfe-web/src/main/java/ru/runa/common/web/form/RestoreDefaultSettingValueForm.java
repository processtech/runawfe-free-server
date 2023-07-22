package ru.runa.common.web.form;

public class RestoreDefaultSettingValueForm extends IdForm  {
    private static final long serialVersionUID = 1L;

    private String settingName;
    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSettingName() {
        return settingName;
    }

    public void setSettingName(String settingName) {
        this.settingName = settingName;
    }
}
