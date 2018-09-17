package ru.runa.wfe.var;

import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Access to variables.
 *
 * @author Dofs
 * @since 4.0
 */
public interface VariableProvider {

    /**
     * Access to current process definition id.
     *
     * @return context process definition id, can be <code>null</code> (if
     *         variables does not linked with process definition)
     */
    Long getProcessDefinitionVersionId();

    /**
     * Access to current process definition name.
     *
     * @return context definition name, can be <code>null</code> (if variables
     *         does not linked with process definition)
     */
    String getProcessDefinitionName();

    /**
     * Access to current process definition.
     *
     * @return context process definition, can be <code>null</code> (if
     *         variables does not linked with process definition)
     */
    ParsedProcessDefinition getParsedProcessDefinition();

    /**
     * Access to current process id.
     *
     * @return context process id, can be <code>null</code> (if variables does
     *         not linked with process)
     */
    Long getProcessId();

    /**
     * Access to variable user type by name.
     *
     * @return variable user type, can be <code>null</code>
     */
    UserType getUserType(String name);

    /**
     * Access to variable value.
     *
     * @param variableName
     *            variable name
     * @return variable value or <code>null</code>
     */
    Object getValue(String variableName);

    /**
     * Access to variable value.
     *
     * @param variableName
     *            variable name
     * @return variable value, cannot be <code>null</code>
     * @throws VariableDoesNotExistException
     *             in case if variable not found
     */
    Object getValueNotNull(String variableName) throws VariableDoesNotExistException;

    /**
     * Access to variable value with optional conversion.
     *
     * @param clazz
     *            class of value you want to obtain
     * @param variableName
     *            variable name
     * @return converted to specified class variable value, can be
     *         <code>null</code>
     */
    <T extends Object> T getValue(Class<T> clazz, String variableName);

    /**
     * Access to variable value.
     *
     * @param clazz
     *            class of value you want to obtain
     * @param variableName
     *            variable name
     * @return converted to specified class variable value, cannot be
     *         <code>null</code>
     * @throws VariableDoesNotExistException
     *             in case if variable not found
     */
    <T extends Object> T getValueNotNull(Class<T> clazz, String variableName) throws VariableDoesNotExistException;

    /**
     * Access to variable.
     *
     * @param variableName
     *            variable name
     * @return variable, can be <code>null</code>
     */
    WfVariable getVariable(String variableName);

    /**
     * Access to variable.
     *
     * @param variableName
     *            variable name
     * @return variable, cannot be <code>null</code>
     * @throws VariableDoesNotExistException
     *             in case if variable not found
     */
    WfVariable getVariableNotNull(String variableName) throws VariableDoesNotExistException;

}
