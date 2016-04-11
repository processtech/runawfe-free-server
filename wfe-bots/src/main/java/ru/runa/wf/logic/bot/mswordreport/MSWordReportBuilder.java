/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.logic.bot.mswordreport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * 
 * Created on 23.11.2006
 * 
 */
public abstract class MSWordReportBuilder {
    protected final Log log = LogFactory.getLog(getClass());
    protected final MSWordReportTaskSettings settings;
    protected final IVariableProvider variableProvider;

    public MSWordReportBuilder(MSWordReportTaskSettings settings, IVariableProvider variableProvider) {
        this.settings = settings;
        this.variableProvider = variableProvider;
    }

    public abstract void build(String reportTemporaryFileName);

    protected String getVariableValue(String variableName, boolean strictMode) throws MSWordReportException {
        WfVariable variable = variableProvider.getVariable(variableName);
        if (variable == null || variable.getValue() == null) {
            if (strictMode) {
                throw new MSWordReportException(MSWordReportException.VARIABLE_NOT_FOUND_IN_PROCESS, variableName);
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
