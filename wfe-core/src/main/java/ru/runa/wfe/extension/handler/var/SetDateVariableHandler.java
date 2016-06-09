package ru.runa.wfe.extension.handler.var;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.bc.BusinessCalendar;
import ru.runa.wfe.commons.bc.BusinessDuration;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.extension.handler.CommonHandler;
import ru.runa.wfe.extension.handler.var.SetDateVariableHandler.CalendarConfig.CalendarOperation;
import ru.runa.wfe.var.DelegableVariableProvider;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

public class SetDateVariableHandler extends CommonHandler {
    protected CalendarConfig config;
    @Autowired
    private BusinessCalendar businessCalendar;

    @Override
    public void setConfiguration(String configuration) {
        config = new CalendarConfig(configuration);
    }

    protected Map<String, Object> executeAction(IVariableProvider variableProvider, boolean pre430CompatibilityMode) throws Exception {
        config.applySubstitutions(variableProvider);
        Calendar calendar;
        if (config.getBaseVariableName() != null) {
            Date baseDate = variableProvider.getValueNotNull(Date.class, config.getBaseVariableName());
            calendar = CalendarUtil.dateToCalendar(baseDate);
        } else {
            calendar = Calendar.getInstance();
        }
        for (CalendarOperation operation : config.getOperations()) {
            log.debug("Executing " + operation + " on " + CalendarUtil.formatDateTime(calendar));
            Integer amount = Integer.parseInt(operation.getExpression());
            if (CalendarOperation.ADD.equals(operation.getType())) {
                BusinessDuration duration = new BusinessDuration(operation.getField(), amount, operation.isBusinessTime());
                Date date = businessCalendar.apply(calendar.getTime(), duration);
                calendar.setTime(date);
            }
            if (CalendarOperation.SET.equals(operation.getType())) {
                if (operation.getField() == Calendar.MONTH && !pre430CompatibilityMode) {
                    amount--;
                }
                calendar.set(operation.getField(), amount);
            }
            log.debug("Result: " + CalendarUtil.formatDateTime(calendar));
        }
        Map<String, Object> result = Maps.newHashMap();
        result.put(config.getOutVariableName(), calendar.getTime());
        return result;
    }

    @Override
    protected Map<String, Object> executeAction(IVariableProvider variableProvider) throws Exception {
        return executeAction(variableProvider, false);
    }

    public static class CalendarConfig {
        protected static final Log log = LogFactory.getLog(CalendarConfig.class);
        protected String baseVariableName;
        protected final List<CalendarOperation> operations = new ArrayList<CalendarOperation>();
        protected String outVariableName;

        public CalendarConfig(String xml) {
            Document document = XmlUtils.parseWithoutValidation(xml);
            Element rootElement = document.getRootElement();
            init(rootElement);
        }

        protected void init(Element rootElement) {
            this.baseVariableName = rootElement.attributeValue("basedOn");
            this.outVariableName = rootElement.attributeValue("result");
            List<Element> operationElements = rootElement.elements("operation");
            for (Element operationElement : operationElements) {
                CalendarOperation mapping = new CalendarOperation(operationElement);
                this.operations.add(mapping);
            }
        }

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
                if (!Objects.equal(substitutedValue, operation.expression)) {
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
                return Objects.toStringHelper(getClass()).add("type", type).add("businessTime", businessTime).add("field", field)
                        .add("expression", expression).toString();
            }
        }

    }
}
