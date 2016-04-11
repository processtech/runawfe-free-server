package ru.runa.wfe.var.format;

import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Provides a way to customize a variable input.
 *
 * @author Dofs
 */
public interface VariableInputSupport {

    /**
     * Generates HTML for variable value input.
     */
    public String getInputHtml(User user, WebHelper webHelper, WfVariable variable);

}
