package ru.runa.wfe.extension.handler.var;

import com.google.common.base.Objects;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import ru.runa.wfe.commons.xml.XmlUtils;

import java.util.ArrayList;
import java.util.List;

public class DeadlineTransferConfig {
    private static final Log log = LogFactory.getLog(DeadlineTransferConfig.class);

    private String processId;
    private String variableName = "";
    private Boolean isInputVariable = false;
    private final List<CalendarOperation> operations = new ArrayList<CalendarOperation>();

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public Boolean isInputVariable() {
        return isInputVariable;
    }

    public List<CalendarOperation> getOperations() {
        return operations;
    }

    public static DeadlineTransferConfig parse(String xml) {
        DeadlineTransferConfig model = new DeadlineTransferConfig();
        Document document = XmlUtils.parseWithoutValidation(xml);
        Element rootElement = document.getRootElement();
        model.processId = rootElement.attributeValue("processId");
        model.variableName = rootElement.attributeValue("variable");
        model.isInputVariable = Boolean.parseBoolean(rootElement.attributeValue("is_input"));
        List<Element> operationElements = rootElement.elements("operation");
        for (Element operationElement : operationElements) {
            CalendarOperation mapping = new CalendarOperation(operationElement);
            model.operations.add(mapping);
        }
        return model;
    }

    public static class CalendarOperation {
        public static final String ADD = "+";
        public static final String SET = "=";
        private final int field;
        private String expression;
        private final String type;
        private final boolean businessTime;

        public CalendarOperation(Element element) {
            type = element.attributeValue("type");
            field = Integer.parseInt(element.attributeValue("field"));
            expression = element.attributeValue("expression");
            businessTime = "true".equals(element.attributeValue("businessTime"));
        }

        public boolean isBusinessTime() {
            return businessTime;
        }

        public String getExpression() {
            return expression;
        }

        public int getField() {
            return field;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(getClass()).add("type", type).add("businessTime", businessTime).add("field", field).add("expression", expression).toString();
        }
    }
}
