package ru.runa.wfe.var.format;


import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;

/**
 * Provides a way to customize a variable display.
 * 
 * @author Dofs
 */
public interface VariableDisplaySupport {

    /**
     * Generates HTML for variable value display.
     */
    public String formatHtml(User user, WebHelper webHelper, Long processId, String name, Object object);
    
}
