package ru.runa.wfe.extension.handler.var;

import java.util.Map;

import org.dom4j.Element;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.VariableProvider;

import com.google.common.base.Objects;

public class TargetProcessSetDateVariableHandler extends SetDateVariableHandler {

    @Override
    public void setConfiguration(String configuration) {
        config = new TargetProcessSetDateVariableConfig(configuration);
    }

    @Override
    protected Map<String, Object> executeAction(VariableProvider variableProvider) throws Exception {
        Long processId = variableProvider.getValueNotNull(Long.class, ((TargetProcessSetDateVariableConfig) config).processIdVariableName);
        Process process = ApplicationContextFactory.getProcessDao().getNotNull(processId);
        ProcessDefinition processDefinition = ApplicationContextFactory.getProcessDefinitionLoader().getDefinition(process);
        ExecutionContext context = new ExecutionContext(processDefinition, process);
        Map<String, Object> map = super.executeAction(context.getVariableProvider());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            context.setVariableValue(entry.getKey(), entry.getValue());
        }
        return null;
    }

    public static class TargetProcessSetDateVariableConfig extends SetDateVariableHandler.CalendarConfig {
        private String processIdVariableName;

        public TargetProcessSetDateVariableConfig(String xml) {
            super(xml);
        }

        @Override
        protected void init(Element rootElement) {
            this.processIdVariableName = rootElement.attributeValue("processId");
            super.init(rootElement);
        }

        @Override
        public void applySubstitutions(VariableProvider variableProvider) {
            super.applySubstitutions(variableProvider);
            {
                String substitutedValue = (String) ExpressionEvaluator.evaluateVariableNotNull(variableProvider, baseVariableName);
                if (!Objects.equal(substitutedValue, baseVariableName)) {
                    log.debug("Substituted " + baseVariableName + " -> " + substitutedValue);
                }
                baseVariableName = substitutedValue;
            }
            {
                String substitutedValue = (String) ExpressionEvaluator.evaluateVariableNotNull(variableProvider, outVariableName);
                if (!Objects.equal(substitutedValue, outVariableName)) {
                    log.debug("Substituted " + outVariableName + " -> " + substitutedValue);
                }
                outVariableName = substitutedValue;
            }
        }

    }

}
