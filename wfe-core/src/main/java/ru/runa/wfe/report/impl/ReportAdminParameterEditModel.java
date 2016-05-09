package ru.runa.wfe.report.impl;

import ru.runa.wfe.report.ReportParameter;
import ru.runa.wfe.report.ReportParameterType;

import com.google.common.base.Strings;

/**
 * Модель для редактирования параметра отчета в административном интерфейсе отчетов.
 */
public class ReportAdminParameterEditModel {
    /**
     * Идентификатор параметра, под которым он сохранён в базе данных.
     */
    private Long id;
    /**
     * Название параметра, отображаемого пользователю.
     */
    private String name;

    /**
     * Тип параметра.
     */
    private ReportParameterType type;

    /**
     * Название, под которым параметр должен быть передан в отчет.
     */
    private String innerName;

    /**
     * Флаг, равный true, если параметр обязателен для заполнения и false иначе.
     */
    private boolean required;

    /**
     * Описание параметра, которое будет показано пользователю при наведении на значок информации.
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
     * Создаёт DTO параметра на основе введенных пользователем данных.
     * 
     * @return Возвращает DTO базы данных, настроенное в соответствии с введенными пользователем параметрами.
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
