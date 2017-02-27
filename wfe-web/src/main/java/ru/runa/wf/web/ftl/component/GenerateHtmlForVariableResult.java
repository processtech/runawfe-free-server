package ru.runa.wf.web.ftl.component;

import com.google.common.base.Strings;

public class GenerateHtmlForVariableResult {
    /**
     * This is html structure content, generated for variable.
     */
    public final String htmlStructureContent;

    /**
     * This is java script content, required for variable input/output support.
     */
    public final String scriptContent;

    static final boolean markStructureWithComments = false;
    static final boolean markJsWithComments = false;

    public GenerateHtmlForVariableResult(GenerateHtmlForVariableContext context, String htmlStructureContent, String scriptContent) {
        this.htmlStructureContent = Strings.isNullOrEmpty(htmlStructureContent) ? "" : markStructure(context, htmlStructureContent);
        this.scriptContent = Strings.isNullOrEmpty(scriptContent) ? "" : markScript(context, scriptContent);
    }

    private static String markStructure(GenerateHtmlForVariableContext context, String structureContent) {
        if (!markStructureWithComments) {
            return structureContent;
        }
        String format = "<!-- HTML Content for %1$s -->\n%2$s<!-- End of HTML Content for %1$s -->";
        return String.format(format, context.getVariableName(), structureContent);
    }

    private static String markScript(GenerateHtmlForVariableContext context, String scriptContent) {
        if (!markJsWithComments) {
            return scriptContent;
        }
        String format = "<!-- Script Content for %1$s -->\n%2$s<!-- End of Script Content for %1$s -->";
        return String.format(format, context.getVariableName(), scriptContent);
    }
}
