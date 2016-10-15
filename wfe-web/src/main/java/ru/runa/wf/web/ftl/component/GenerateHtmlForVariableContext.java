package ru.runa.wf.web.ftl.component;

import ru.runa.wfe.var.dto.WfVariable;

public class GenerateHtmlForVariableContext {
    public final WfVariable variable;
    public final Long processId;
    public final boolean isReadonly;

    public GenerateHtmlForVariableContext(WfVariable variable, Long processId, boolean isReadonly) {
        super();
        this.variable = variable;
        this.processId = processId;
        this.isReadonly = isReadonly;
    }

    public GenerateHtmlForVariableContext CopyFor(WfVariable newVariable) {
        return new GenerateHtmlForVariableContext(newVariable, processId, isReadonly);
    }
}
