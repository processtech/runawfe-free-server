package ru.runa.wfe.var.dto;

import ru.runa.wfe.commons.ftl.FreemarkerTagHelper;

public class QuickFormVariable {
    private String tagName;
    private String name;
    private String scriptingName;
    private String description;
    private String[] params;

    public String getTag() {
        return FreemarkerTagHelper.build(tagName, name, params);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScriptingName() {
        return scriptingName;
    }

    public void setScriptingName(String scriptingName) {
        this.scriptingName = scriptingName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
}
