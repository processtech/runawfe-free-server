package ru.runa.wfe.commons.ftl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Form component which allows user interaction with server through ajax requests.
 *
 * @author dofs
 *
 */
public abstract class AjaxFormComponent extends FormComponent {
    private static final long serialVersionUID = 1L;
    public static final String COMPONENT_SESSION_PREFIX = "ajax_form_component_";

    /**
     * Invoked on ajax request
     */
    public void processAjaxRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    }

    @Override
    protected String exportScript(Map<String, String> substitutions, boolean globalScope) {
        addJsonUrlSubstitution(substitutions);
        return super.exportScript(substitutions, globalScope);
    }

    @Override
    protected String exportScript(Map<String, String> substitutions, boolean globalScope, String name) {
        addJsonUrlSubstitution(substitutions);
        return super.exportScript(substitutions, globalScope, name);
    }

    private void addJsonUrlSubstitution(Map<String, String> substitutions) {
        substitutions.put("JSON_URL", webHelper.getUrl("/form.fp?component=" + getName() + "&qualifier=" + getVariableNameForSubmissionProcessing()));
    }
}
