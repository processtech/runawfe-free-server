package ru.runa.wfe.report.impl;

import ru.runa.wfe.report.ReportParameter;
import ru.runa.wfe.report.ReportParameterType;

import com.google.common.base.Strings;

/**
 * A model to edit report parameters in reports administrative interface.
 */
public class ReportAdminParameterEditModel {
    /**
     * Parameter identifier for DB.
     */
    private Long id;
    /**
     * Parameter name that is shown to user.
     */
    private String name;

    /**
     * Parameter type.
     */
    private ReportParameterType type;

    /**
     * Parameter name for report.
     */
    private String innerName;

    /**
     * true if parameter is required and false if it's optional.
     */
    private boolean required;

    /**
     * Parameter description that is shown to user when pointed to information icon.
     */
    private String description;

    public ReportAdminParameterEditModel() {
    }

    public ReportAdminParameterEditModel(ReportParameterType type, String innerName, String description) {
        this.type = type;
        this.innerName = innerName;
        this.description = Strings.isNullOrEmpty(description) ? "Описание параметра не задано" : description;
    }

    public ReportAdminParameterEditModel(ReportParameter databaseDto, String description) {
        this.id = databaseDto.getId();
        this.name = databaseDto.getName();
        this.type = databaseDto.getType();
        this.innerName = databaseDto.getInnerName();
        this.required = databaseDto.isRequired();
        this.description = Strings.isNullOrEmpty(description) ? "Описание параметра не задано" : description;
    }

    /**
     * Creates parameter DTO from user defined data.
     * 
     * @return DB DTO set up with user defined parameters data.
     */
    public ReportParameter getDatabaseDto() {
        ReportParameter databaseDto = new ReportParameter();
        databaseDto.setName(name);
        databaseDto.setType(type);
        databaseDto.setInnerName(innerName);
        databaseDto.setRequired(required);
        return databaseDto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ReportParameterType getType() {
        return type;
    }

    public void setType(ReportParameterType type) {
        this.type = type;
    }

    public String getInnerName() {
        return innerName;
    }

    public void setInnerName(String innerName) {
        this.innerName = innerName;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
