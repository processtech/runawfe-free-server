package ru.runa.wfe.report.impl;

import java.util.ArrayList;
import java.util.List;

import ru.runa.wfe.report.ReportParameterType;
import ru.runa.wfe.report.dto.ReportParameterDto;

/**
 * Модель параметра, требуемого для построения отчета.
 */
public class ReportParameterModel {
    /**
     * Название, отображаемое пользователю.
     */
    private String name;
    /**
     * Тип параметра.
     */
    private ReportParameterType type;
    /**
     * Название параметра, в котором хранится значение при передаче в отчет.
     */
    private String innerName;
    /**
     * Значение параметра.
     */
    private String value;
    /**
     * Флаг, равный true, если параметр обязателен и false иначе.
     */
    private boolean required;
    /**
     * Тип параметра в html элементе input.
     */
    private String htmlInputType;
    /**
     * CSS-класс в html элементе input.
     */
    private String htmlInputClass;
    /**
     * Значения для выбора из списка.
     */
    private List<ListValuesData> listValues = new ArrayList<ReportParameterModel.ListValuesData>();
    /**
     * Описание параметра, отображаемое пользователю.
     */
    private String description;

    public ReportParameterModel() {
    }

    public ReportParameterModel(ReportParameterDto parameterDto) {
        this.name = parameterDto.getUserName();
        this.type = parameterDto.getType();
        this.innerName = parameterDto.getInternalName();
        this.required = parameterDto.isRequired();
        this.setHtmlInputType(parameterDto.getType().processBy(new ReportParameterHtmlTypeOperation(), null));
        this.setHtmlInputClass(parameterDto.getType().processBy(new ReportParameterHtmlClassOperation(), null));
        this.description = parameterDto.getDescription();
    }

    public boolean isSimpleInputProperty() {
        return !isFixedListProperty() && !isFlagProperty();
    }

    public boolean isFixedListProperty() {
        return type.processBy(IsFixedListPropertyOperation.INSTANCE, null);
    }

    public boolean isFlagProperty() {
        return type.processBy(IsFlagPropertyOperation.INSTANCE, null);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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

    public String getHtmlInputType() {
        return htmlInputType;
    }

    public void setHtmlInputType(String htmlInputType) {
        this.htmlInputType = htmlInputType;
    }

    public String getHtmlInputClass() {
        return htmlInputClass;
    }

    public void setHtmlInputClass(String htmlInputClass) {
        this.htmlInputClass = htmlInputClass;
    }

    public List<ListValuesData> getListValues() {
        return listValues;
    }

    public void setListValues(List<ListValuesData> listValues) {
        this.listValues = listValues;
    }

    public String getDescription() {
        return description;
    }

    public static class ListValuesData {
        /**
         * Текст, отображаемый пользователю.
         */
        private String valueName;
        /**
         * Значение, соответствующее параметру.
         */
        private Object value;

        public ListValuesData(String valueName, Object value) {
            this.valueName = valueName;
            this.value = value;
        }

        public String getValueName() {
            return valueName;
        }

        public void setValueName(String valueName) {
            this.valueName = valueName;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }
}
