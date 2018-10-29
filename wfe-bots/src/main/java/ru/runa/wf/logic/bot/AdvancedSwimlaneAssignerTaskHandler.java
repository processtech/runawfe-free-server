package ru.runa.wf.logic.bot;

import java.util.List;
import java.util.Map;

import ru.runa.wf.logic.bot.assigner.AssignerSettings;
import ru.runa.wf.logic.bot.assigner.AssignerSettings.Condition;
import ru.runa.wf.logic.bot.assigner.AssignerSettingsXmlParser;
import ru.runa.wf.logic.bot.assigner.EvaluationFunction;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.extension.handler.TaskHandlerBase;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;

import com.google.common.collect.Maps;

public class AdvancedSwimlaneAssignerTaskHandler extends TaskHandlerBase {
    private AssignerSettings settings;

    @Override
    public void setConfiguration(String configuration) throws Exception {
        settings = AssignerSettingsXmlParser.read(configuration);
    }

    @Override
    public Map<String, Object> handle(User user, VariableProvider variableProvider, WfTask task) throws Exception {
        Map<String, Object> outputVariables = Maps.newHashMap();
        List<Condition> conditions = settings.getAssignerConditions();
        for (Condition condition : conditions) {
            if (isAppliedCondition(condition.getFunctionClassName(), variableProvider)) {
                String actor = variableProvider.getValue(String.class, condition.getVariableName());
                outputVariables.put(condition.getSwimlaneName(), actor);
                break;
            }
        }
        return outputVariables;
    }

    private boolean isAppliedCondition(String functionClassName, VariableProvider variableProvider) {
        if ("true".equalsIgnoreCase(functionClassName)) {
            return true;
        }
        EvaluationFunction evaluationFunction = ClassLoaderUtil.instantiate(functionClassName);
        return evaluationFunction.evaluate(variableProvider);
    }
}
