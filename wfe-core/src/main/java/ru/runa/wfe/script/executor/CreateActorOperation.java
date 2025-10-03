package ru.runa.wfe.script.executor;

import com.google.common.base.Strings;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.AdminScriptException;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.script.common.ScriptValidation;
import ru.runa.wfe.user.Actor;

@XmlType(name = CreateActorOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class CreateActorOperation extends ScriptOperation {

    public static final String SCRIPT_NAME = "createActor";

    @XmlAttribute(name = AdminScriptConstants.NAME_ATTRIBUTE_NAME, required = true)
    public String name;

    @XmlAttribute(name = AdminScriptConstants.FULL_NAME_ATTRIBUTE_NAME, required = false)
    public String fullName;

    @XmlAttribute(name = AdminScriptConstants.DESCRIPTION_ATTRIBUTE_NAME, required = false)
    public String description;

    @XmlAttribute(name = AdminScriptConstants.PASSWORD_ATTRIBUTE_NAME, required = false)
    public String password;

    @XmlAttribute(name = AdminScriptConstants.EMAIL_ATTRIBUTE_NAME, required = false)
    public String email;

    @XmlAttribute(name = AdminScriptConstants.PHONE_ATTRIBUTE_NAME, required = false)
    public String phone;

    @XmlAttribute(name = AdminScriptConstants.CODE_ATTRIBUTE_NAME, required = false)
    public String code;

    @XmlAttribute(name = AdminScriptConstants.TITLE_ATTRIBUTE_NAME, required = false)
    public String title;

    @XmlAttribute(name = AdminScriptConstants.DEPARTMENT_ATTRIBUTE_NAME, required = false)
    public String department;

    @Override
    public void validate(ScriptExecutionContext context) {
        ScriptValidation.requiredAttribute(this, AdminScriptConstants.NAME_ATTRIBUTE_NAME, name);
        if (!Strings.isNullOrEmpty(code)) {
            try {
                Long.parseLong(code);
            } catch (Exception e) {
                throw new AdminScriptException("Code for actor " + name + " must be an integer.");
            }
        }
    }

    @Override
    public void execute(ScriptExecutionContext context) {
        Long actorCode = Strings.isNullOrEmpty(code) ? null : Long.valueOf(code);
        if (fullName == null) {
            fullName = name;
        }
        Actor actor = new Actor(name, description, fullName, actorCode, email, phone, title, department);
        actor = context.getExecutorLogic().create(context.getUser(), actor);
        String actorPassword = Strings.isNullOrEmpty(context.getDefaultPassword()) ? password : context.getDefaultPassword();
        if (!Strings.isNullOrEmpty(actorPassword)) {
            context.getExecutorLogic().setPassword(context.getUser(), actor, actorPassword);
        }
    }
}
