package ru.runa.wf.logic.bot.mswordreport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * 
 * Created on 23.11.2006
 * 
 */
public abstract class MsWordReportBuilder {
    protected final Log log = LogFactory.getLog(getClass());
    protected final MsWordReportTaskSettings settings;
    protected final VariableProvider variableProvider;

    public MsWordReportBuilder(MsWordReportTaskSettings settings, VariableProvider variableProvider) {
        this.settings = settings;
        this.variableProvider = variableProvider;
    }

    public abstract void build(String reportTemporaryFileName);

    protected String getVariableValue(String variableName, boolean strictMode) throws MsWordReportException {
        WfVariable variable = variableProvider.getVariable(variableName);
        if (variable == null || variable.getValue() == null) {
            if (strictMode) {
                throw new MsWordReportException(MsWordReportException.VARIABLE_NOT_FOUND_IN_PROCESS, variableName);
            }
            log.warn("Seems like variable is missed: '" + variableName + "'");
            return null;
        }
        try {
            if (SystemProperties.isV3CompatibilityMode() && variable.getValue() instanceof Actor) {
                return String.valueOf(((Actor) variable.getValue()).getCode());
            }
            return variable.getStringValue();
        } catch (Exception e) {
            log.warn("Unable to format " + variable + ": " + e.getMessage());
            return variable.getValue().toString();
        }
    }

}
