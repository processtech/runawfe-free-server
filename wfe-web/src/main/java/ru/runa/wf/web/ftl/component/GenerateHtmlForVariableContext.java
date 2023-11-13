package ru.runa.wf.web.ftl.component;

import ru.runa.wfe.var.dto.WfVariable;

/**
 * HTML code generation operation context.
 */
public class GenerateHtmlForVariableContext {
    /**
     * Variable, code generated for.
     */
    public final WfVariable variable;
    /**
     * Process id. May be 0 if process id is unknown.
     */
    public final Long processId;
    /**
     * HTML code generation mode: for user input (false) or for display (true).
     */
    public final boolean readonly;

    /**
     * Whether html is printed on chat page or not
     */
    public final boolean isChatView;

    public GenerateHtmlForVariableContext(WfVariable variable, Long processId, boolean readonly, boolean isChatView) {
        this.variable = variable;
        this.processId = processId;
        this.readonly = readonly;
        this.isChatView = isChatView;
    }

    /**
     * Creates copy of HTML code generation context for different variable.
     * 
     * @param newVariable
     *            Variable, requested new context.
     * @return Returns HTML code generation operation context for variable.
     */
    public GenerateHtmlForVariableContext copyFor(WfVariable newVariable) {
        return new GenerateHtmlForVariableContext(newVariable, processId, readonly, isChatView);
    }

    /**
     * Current variable name.
     * 
     * @return Returns current variable name.
     */
    public String getVariableName() {
        return variable.getDefinition().getName();
    }

    /**
     * Get scripting name for current variable, replacing all special symbols.
     * 
     * @return Returns scripting name for current variable, replacing all special symbols.
     */
    public String getScriptingNameWithoutDots() {
        return variable.getDefinition().getScriptingNameWithoutDots();
    }
}
