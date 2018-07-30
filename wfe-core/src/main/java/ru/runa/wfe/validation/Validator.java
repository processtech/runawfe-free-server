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
package ru.runa.wfe.validation;

import com.google.common.base.Objects;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.MapDelegableVariableProvider;

public abstract class Validator {
    protected final Log log = LogFactory.getLog(getClass());
    private User user;
    private ExecutionContext executionContext;
    private IVariableProvider oldVariableProvider;
    private IVariableProvider variableProvider;
    private ValidatorConfig config;
    private ValidatorContext validatorContext;
    private Map<String, Object> newVariables;

    public void init(User user, ExecutionContext executionContext, IVariableProvider variableProvider, ValidatorConfig config,
            ValidatorContext validatorContext, Map<String, Object> variables) {
        this.user = user;
        this.executionContext = executionContext;
        this.oldVariableProvider = variableProvider;
        this.variableProvider = new MapDelegableVariableProvider(config.getParams(), new MapDelegableVariableProvider(variables, variableProvider));
        this.config = config;
        this.validatorContext = validatorContext;
        this.newVariables = variables;
    }

    protected User getUser() {
        return user;
    }

    protected ExecutionContext getExecutionContext() {
        return executionContext;
    }

    /**
     * Used by TNMS. Access only to old values (without submitted ones).
     */
    @SuppressWarnings("unused")
    protected IVariableProvider getOldVariableProvider() {
        return oldVariableProvider;
    }

    protected IVariableProvider getVariableProvider() {
        return variableProvider;
    }

    public ValidatorConfig getConfig() {
        return config;
    }

    protected ValidatorContext getValidatorContext() {
        return validatorContext;
    }

    /**
     * Used by TNMS. Access only to submitted values (with previous ones).
     */
    @SuppressWarnings("unused")
    protected Map<String, Object> getNewVariables() {
        return newVariables;
    }

    private <T> T getParameter(Class<T> clazz, String name) {
        String stringValue = config.getParams().get(name);
        if (stringValue == null) {
            return null;
        }
        Object value = ExpressionEvaluator.evaluateVariable(variableProvider, stringValue);
        return TypeConversionUtil.convertTo(clazz, value);
    }

    protected <T> T getParameter(Class<T> clazz, String name, T defaultValue) {
        T value = getParameter(clazz, name);
        return value == null ? defaultValue : value;
    }

    @SuppressWarnings("SameParameterValue")
    protected <T> T getParameterNotNull(Class<T> clazz, String name) {
        T value = getParameter(clazz, name);
        if (value == null) {
            throw new InternalApplicationException("parameter '" + name + "' is null");
        }
        return value;
    }

    public String getMessage() {
        String m = ExpressionEvaluator.substitute(config.getMessage(), variableProvider);
        return m.replaceAll("\t", " ").replaceAll("\n", " ").trim();
    }

    protected void addError(String userMessage) {
        validatorContext.addGlobalError(userMessage);
    }

    protected void addError() {
        addError(getMessage());
    }

    public abstract void validate() throws Exception;

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("config", config).toString();
    }
}
