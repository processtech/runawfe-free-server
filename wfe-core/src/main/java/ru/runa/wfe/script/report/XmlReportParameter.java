package ru.runa.wfe.script.report;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.report.ReportParameterType;
import ru.runa.wfe.report.ReportParameterType.ReportParameterTypeVisitor;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.script.common.ScriptValidation;

@XmlType(name = "ReportParameterType", namespace = AdminScriptConstants.NAMESPACE)
public class XmlReportParameter {

    @XmlAttribute(name = AdminScriptConstants.NAME_ATTRIBUTE_NAME, required = true)
    public String name;

    @XmlAttribute(name = AdminScriptConstants.TYPE_ATTRIBUTE_NAME, required = true)
    public XmlReportParameterType type;

    @XmlAttribute(name = "innerName", required = true)
    public String innerName;

    @XmlAttribute(name = "required")
    public boolean required;

    public void validate(ScriptExecutionContext context, ScriptOperation operation) {
        ScriptValidation.requiredAttribute(operation, AdminScriptConstants.NAME_ATTRIBUTE_NAME, name);
        ScriptValidation.requiredAttribute(operation, "innerName", innerName);
    }

    @XmlEnum(value = String.class)
    public enum XmlReportParameterType {
        @XmlEnumValue(value = "String")
        STRING {
            @Override
            public ReportParameterType getType() {
                return ReportParameterType.STRING;
            }
        },

        @XmlEnumValue(value = "Number")
        NUMBER {
            @Override
            public ReportParameterType getType() {
                return ReportParameterType.NUMBER;
            }
        },

        @XmlEnumValue(value = "Date")
        DATE {
            @Override
            public ReportParameterType getType() {
                return ReportParameterType.DATE;
            }
        },

        @XmlEnumValue(value = "ProcessName")
        PROCESS_NAME_OR_NULL {
            @Override
            public ReportParameterType getType() {
                return ReportParameterType.PROCESS_NAME_OR_NULL;
            }
        },

        @XmlEnumValue(value = "Swimlane")
        SWIMLANE {
            @Override
            public ReportParameterType getType() {
                return ReportParameterType.SWIMLANE;
            }
        },

        @XmlEnumValue(value = "UncheckedBoolean")
        BOOLEAN_UNCHECKED {
            @Override
            public ReportParameterType getType() {
                return ReportParameterType.BOOLEAN_UNCHECKED;
            }
        },

        @XmlEnumValue(value = "CheckedBoolean")
        BOOLEAN_CHECKED {
            @Override
            public ReportParameterType getType() {
                return ReportParameterType.BOOLEAN_CHECKED;
            }
        };

        public abstract ReportParameterType getType();
    }

    public class TypyToXmlConverter implements ReportParameterTypeVisitor<XmlReportParameterType, Object> {

        @Override
        public XmlReportParameterType onString(Object data) {
            return XmlReportParameterType.STRING;
        }

        @Override
        public XmlReportParameterType onNumber(Object data) {
            return XmlReportParameterType.NUMBER;
        }

        @Override
        public XmlReportParameterType onDate(Object data) {
            return XmlReportParameterType.DATE;
        }

        @Override
        public XmlReportParameterType onUncheckedBoolean(Object data) {
            return XmlReportParameterType.BOOLEAN_UNCHECKED;
        }

        @Override
        public XmlReportParameterType onCheckedBoolean(Object data) {
            return XmlReportParameterType.BOOLEAN_CHECKED;
        }

        @Override
        public XmlReportParameterType onProcessNameOrNull(Object data) {
            return XmlReportParameterType.PROCESS_NAME_OR_NULL;
        }

        @Override
        public XmlReportParameterType onSwimlane(Object data) {
            return XmlReportParameterType.SWIMLANE;
        }
    }
}
