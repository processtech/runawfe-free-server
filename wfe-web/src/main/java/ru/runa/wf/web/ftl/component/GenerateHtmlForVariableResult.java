package ru.runa.wf.web.ftl.component;

import com.google.common.base.Strings;

public class GenerateHtmlForVariableResult {
    /**
     * This is html structure content, generated for variable.
     */
    public final String content;

    public GenerateHtmlForVariableResult(GenerateHtmlForVariableContext context, String content) {
        this.content = Strings.isNullOrEmpty(content) ? "" : content;
    }

}
