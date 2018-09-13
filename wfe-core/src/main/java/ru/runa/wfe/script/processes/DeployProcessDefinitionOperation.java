package ru.runa.wfe.script.processes;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.script.common.ScriptValidation;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@XmlType(name = DeployProcessDefinitionOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class DeployProcessDefinitionOperation extends ScriptOperation {

    public static final String SCRIPT_NAME = "deployProcessDefinition";

    @XmlAttribute(name = AdminScriptConstants.TYPE_ATTRIBUTE_NAME)
    public String type;

    @XmlAttribute(name = AdminScriptConstants.FILE_ATTRIBUTE_NAME, required = true)
    public String file;

    @Override
    public List<String> getExternalResources() {
        return Lists.newArrayList(file);
    }

    @Override
    public void validate(ScriptExecutionContext context) {
        ScriptValidation.requiredAttribute(this, AdminScriptConstants.FILE_ATTRIBUTE_NAME, file);
    }

    @Override
    public void execute(ScriptExecutionContext context) {
        List<String> parsedType = Splitter.on("/").splitToList(Strings.isNullOrEmpty(type) ? "Script" : type);
        context.getProcessDefinitionLogic().deployProcessDefinition(context.getUser(), context.getExternalResource(file), parsedType);
    }
}
