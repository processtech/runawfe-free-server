package ru.runa.wfe.report.dto;

import java.io.Serializable;
import java.util.Objects;
import ru.runa.wfe.report.ReportParameterType;

public class WfReportParameter implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userName;
    private String description;
    private String internalName;
    private int position;
    private ReportParameterType type;
    private boolean isRequired;

    public WfReportParameter() {
    }

    public WfReportParameter(String userName, String description, String internalName, int position, ReportParameterType type, boolean isRequired) {
        this.userName = userName;
        this.description = description;
        this.internalName = internalName;
        this.position = position;
        this.type = type;
        this.isRequired = isRequired;
    }

    public String getUserName() {
        return userName == null ? "" : userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInternalName() {
        return internalName == null ? "" : internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public ReportParameterType getType() {
        return type;
    }

    public void setType(ReportParameterType type) {
        this.type = type;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(internalName);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WfReportParameter) {
            WfReportParameter other = (WfReportParameter) obj;
            return Objects.equals(internalName, other.internalName);
        }
        return false;
    }

}
