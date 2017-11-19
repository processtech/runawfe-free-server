package ru.runa.wf.web.ftl.component;

import com.google.common.base.Strings;

public class GenerateHtmlForVariableResult {
    /**
     * This is html structure content, generated for variable.
     */
    public final String content;
    static final boolean markStructureWithComments = false;
    static final boolean markJsWithComments = false;

    public GenerateHtmlForVariableResult(GenerateHtmlForVariableContext context, String content) {
        this.content = Strings.isNullOrEmpty(content) ? "" : mark(context, content);
    }

    private static String mark(GenerateHtmlForVariableContext context, String structureContent) {
        if (!markStructureWithComments) {
            return structureContent;
        }
        String format = "<!-- HTML Content for %1$s -->\n%2$s<!-- End of HTML Content for %1$s -->";
        return String.format(format, context.getVariableName(), structureContent);
    }

}
