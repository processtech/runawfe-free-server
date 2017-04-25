package ru.runa.wfe.report.impl;

import java.util.ArrayList;
import java.util.List;

import ru.runa.wfe.report.ReportParameterType;
import ru.runa.wfe.report.dto.WfReportParameter;

/**
 * Parameter model that is required in order to build report.
 */
public class ReportParameterModel {
    /**
     * Name that is shown to user.
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
     * Parameter value.
     */
    private String value;
    /**
     * true if parameter is required, false if it's optional.
     */
    private boolean required;
    /**
     * Parameter type in input html element.
     */
    private String htmlInputType;
    /**
     * CSS-class in input html element 
     *      */
    private String htmlInputClass;
    /**
     * Values for dropdown list.
     */
    private List<ListValuesData> listValues = new ArrayList<ReportParameterModel.ListValuesData>();
    /**
     * Parameter description that is shown to user.
     */
    private String description;

    public ReportParameterModel() {
    }

    public ReportParameterModel(WfReportParameter parameterDto) {
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
         * Text that is shown to user.
         */
        private String valueName;
        /**
         * Value that corresponds to parameter.
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
