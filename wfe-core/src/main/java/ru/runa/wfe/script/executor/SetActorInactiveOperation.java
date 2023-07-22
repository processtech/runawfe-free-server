package ru.runa.wfe.script.executor;

import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.NamedIdentitySet;
import ru.runa.wfe.script.common.NamedIdentitySet.NamedIdentityType;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.script.common.ScriptValidation;
import ru.runa.wfe.user.Actor;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@XmlType(name = SetActorInactiveOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class SetActorInactiveOperation extends ScriptOperation {

    public static final String SCRIPT_NAME = "setActorInactive";

    @XmlAttribute(name = AdminScriptConstants.NAME_ATTRIBUTE_NAME)
    public String name;

    @XmlElement(name = AdminScriptConstants.NAMED_IDENTITY_ELEMENT_NAME, namespace = AdminScriptConstants.NAMESPACE)
    public List<NamedIdentitySet> identities = Lists.newArrayList();

    @Override
    public void validate(ScriptExecutionContext context) {
        ScriptValidation.requiredAttribute(this, AdminScriptConstants.NAME_ATTRIBUTE_NAME, name);
        for (NamedIdentitySet set : identities) {
            set.validate(this, NamedIdentityType.EXECUTOR);
        }
    }

    @Override
    public void execute(ScriptExecutionContext context) {
        Set<String> actorNames = Sets.newHashSet();
        if (!Strings.isNullOrEmpty(name)) {
            actorNames.add(name);
        }
        for (NamedIdentitySet set : identities) {
            actorNames.addAll(set.get(context));
        }
        for (String actorName : actorNames) {
            Actor actor = context.getExecutorLogic().getActor(context.getUser(), actorName);
            context.getExecutorLogic().setStatus(context.getUser(), actor, false, true);
        }
    }
}
