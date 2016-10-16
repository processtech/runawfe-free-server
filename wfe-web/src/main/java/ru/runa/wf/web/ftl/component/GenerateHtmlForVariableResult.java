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

    public GenerateHtmlForVariableResult(String htmlStructureContent, String scriptContent) {
        super();
        this.htmlStructureContent = Strings.isNullOrEmpty(htmlStructureContent) ? "" : htmlStructureContent;
        this.scriptContent = Strings.isNullOrEmpty(scriptContent) ? "" : scriptContent;
    }
}
