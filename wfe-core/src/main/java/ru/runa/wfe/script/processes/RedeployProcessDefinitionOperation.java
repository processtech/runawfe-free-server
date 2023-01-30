package ru.runa.wfe.script.processes;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.script.common.ScriptValidation;
import ru.runa.wfe.script.common.ScriptValidationException;

@XmlType(name = RedeployProcessDefinitionOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class RedeployProcessDefinitionOperation extends ScriptOperation {

    public static final String SCRIPT_NAME = "redeployProcessDefinition";

    @XmlAttribute(name = AdminScriptConstants.TYPE_ATTRIBUTE_NAME)
    public String type;

    @XmlAttribute(name = AdminScriptConstants.NAME_ATTRIBUTE_NAME, required = false)
    public String name;

    @XmlAttribute(name = AdminScriptConstants.DEFINITION_ID_ATTRIBUTE_NAME, required = false)
    public Long definitionId;

    @XmlAttribute(name = AdminScriptConstants.FILE_ATTRIBUTE_NAME, required = true)
    public String file;

    @Override
    public void validate(ScriptExecutionContext context) {
        ScriptValidation.requiredAttribute(this, AdminScriptConstants.FILE_ATTRIBUTE_NAME, file);
        if (Strings.isNullOrEmpty(name)) {
            if (definitionId == null) {
                throw new ScriptValidationException(this, "Required definition name or id");
            }
        } else {
            ScriptValidation.requiredAttribute(this, AdminScriptConstants.NAME_ATTRIBUTE_NAME, name);
        }
    }

    @Override
    public void execute(ScriptExecutionContext context) {
        if (!Strings.isNullOrEmpty(name)) {
            definitionId = ApplicationContextFactory.getDeploymentDAO().findLatestDeployment(name).getId();
        }
        List<String> parsedType = null;
        if (Strings.isNullOrEmpty(type)) {
            parsedType = Splitter.on('/').splitToList(type);
        }
        context.getDefinitionLogic().redeployProcessDefinition(context.getUser(), definitionId, context.getExternalResource(file), parsedType);
    }
}
