package ru.runa.wfe.script.common;

import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

import com.google.common.collect.Lists;

@XmlTransient()
public abstract class ScriptOperation {

    public List<String> getExternalResources() {
        return Lists.newArrayList();
    }

    public abstract void validate(ScriptExecutionContext context);

    public abstract void execute(ScriptExecutionContext context);
}
