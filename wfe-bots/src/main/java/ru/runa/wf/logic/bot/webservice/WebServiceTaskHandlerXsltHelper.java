package ru.runa.wf.logic.bot.webservice;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import ru.runa.wf.logic.bot.WebServiceTaskHandler;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Helper class for XSLT transformation in {@link WebServiceTaskHandler}.
 */
public class WebServiceTaskHandlerXsltHelper {

    /**
     * Current task, processed by {@link WebServiceTaskHandler}.
     */
    final WfTask task;

    /**
     * Current {@link WebServiceTaskHandler} subject.
     */
    final User user;

    /**
     * Variables, changed by result xslt transformation.
     */
    final Map<String, Object> variables;

    /**
     * Create instance for specified task and subject.
     * 
     * @param task
     *            Current task, processed by {@link WebServiceTaskHandler}.
     * @param user
     *            Current {@link WebServiceTaskHandler} user.
     */
    public WebServiceTaskHandlerXsltHelper(WfTask task, User user) {
        this.task = task;
        this.user = user;
        variables = new HashMap<String, Object>();
    }

    public Long getProcessId() {
        return task.getProcessId();
    }

    /**
     * Read variable from current task instance.
     * 
     * @param name
     *            Variable name.
     * @return Variable value converted to string.
     */
    public String getVariable(String name) {
        WfVariable variable = Delegates.getExecutionService().getVariable(user, task.getProcessId(), name);
        if (variable != null && variable.getValue() != null) {
            return variable.getValue().toString();
        }
        throw new InternalApplicationException("Can't create SOAP request. WFE variable " + name + " not found");
    }

    /**
     * Read process instance id from variable and returns process instance graph
     * for this process instance encoded in {@link Base64}.
     * 
     * @param processIdVariable
     *            Variable name to read process instance id.
     * @return Process instance graph for this process instance encoded in
     *         {@link Base64}.
     */
    public String getProcessGraph(String processIdVariable) {
        WfVariable variable = Delegates.getExecutionService().getVariable(user, task.getProcessId(), processIdVariable);
        if (variable != null && variable.getValue() != null) {
            return Base64.encodeBase64String(Delegates.getExecutionService().getProcessDiagram(user, (Long) variable.getValue(), null, null, null));
        }
        throw new InternalApplicationException("Can't create SOAP request. WFE variable " + processIdVariable + " not found");
    }

    /**
     * Add variable to internal storage. You can merge this variables into your
     * storage using MergeVariablesIn call.
     * 
     * @param name
     *            Variable name.
     * @param value
     *            Variable value.
     */
    public void setNewVariable(String name, String value) {
        variables.put(name, value);
    }

    /**
     * Merging variables from internal storage into given map.
     * 
     * @param mergedTo
     *            Storage to merge in variables.
     */
    public void mergeVariablesIn(Map<String, Object> mergedTo) {
        mergedTo.putAll(variables);
    }
}
