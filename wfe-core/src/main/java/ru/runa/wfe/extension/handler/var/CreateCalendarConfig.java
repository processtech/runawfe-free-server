package ru.runa.wfe.extension.handler.var;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import com.google.common.base.Objects;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.var.DelegableVariableProvider;
import ru.runa.wfe.var.IVariableProvider;

public class CreateCalendarConfig {
    private static final Log log = LogFactory.getLog(CreateCalendarConfig.class);

    private String baseVariableName;
    private final List<CalendarOperation> operations = new ArrayList<CalendarOperation>();
    private String outVariableName;

    public String getBaseVariableName() {
        return baseVariableName;
    }

    public List<CalendarOperation> getOperations() {
        return operations;
    }

    public String getOutVariableName() {
        return outVariableName;
    }

    public void applySubstitutions(IVariableProvider variableProvider) {
        for (CalendarOperation operation : operations) {
            String substitutedValue = substitute(operation, variableProvider);
            if (!substitutedValue.equals(operation.expression)) {
                log.debug("Substituted " + operation.expression + " -> " + substitutedValue);
            }
            operation.expression = substitutedValue;
        }
    }

    private String substitute(final CalendarOperation operation, IVariableProvider variableProvider) {
        DelegableVariableProvider delegableVariableProvider = new DelegableVariableProvider(variableProvider) {

            @Override
            public Object getValue(String variableName) {
                Object object = super.getValue(variableName);
                if (object instanceof Date) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime((Date) object);
                    return calendar.get(operation.field);
                }
                return object;
            }
        };
        Object value = ExpressionEvaluator.evaluateVariableNotNull(delegableVariableProvider, operation.expression);
        return TypeConversionUtil.convertTo(Integer.class, value).toString();
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

    public static CreateCalendarConfig parse(String xml) {
        CreateCalendarConfig model = new CreateCalendarConfig();
        Document document = XmlUtils.parseWithoutValidation(xml);
        Element rootElement = document.getRootElement();
        model.baseVariableName = rootElement.attributeValue("basedOn");
        model.outVariableName = rootElement.attributeValue("result");
        List<Element> operationElements = rootElement.elements("operation");
        for (Element operationElement : operationElements) {
            CalendarOperation mapping = new CalendarOperation(operationElement);
            model.operations.add(mapping);
        }
        return model;
    }
}
