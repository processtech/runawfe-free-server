package ru.runa.wfe.script.common;

import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.NamedIdentitySet.NamedIdentityType;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@XmlType(name = "IdentitiesSetContainerType", namespace = AdminScriptConstants.NAMESPACE)
public class IdentitiesSetContainer {

    private final NamedIdentityType identitiesType;

    @XmlElement(name = AdminScriptConstants.NAMED_IDENTITY_ELEMENT_NAME, namespace = AdminScriptConstants.NAMESPACE)
    public List<NamedIdentitySet> identitySets = Lists.newArrayList();

    @XmlElement(name = AdminScriptConstants.IDENTITY_ELEMENT_NAME, namespace = AdminScriptConstants.NAMESPACE)
    public List<Identity> identities = Lists.newArrayList();

    protected IdentitiesSetContainer() {
        identitiesType = null;
    }

    protected IdentitiesSetContainer(NamedIdentityType identitiesType) {
        super();
        this.identitiesType = identitiesType;
    }

    protected void validate(ScriptOperation scriptOperation, boolean requiredNotEmpty) {
        for (NamedIdentitySet set : identitySets) {
            set.validate(scriptOperation, identitiesType);
        }
        for (Identity identity : identities) {
            identity.validate(scriptOperation);
        }
        if (requiredNotEmpty) {
            if (identitySets.size() == 0 && identities.size() == 0) {
                throw new ScriptValidationException(scriptOperation, "Required " + AdminScriptConstants.NAMED_IDENTITY_ELEMENT_NAME + " or "
                        + AdminScriptConstants.IDENTITY_ELEMENT_NAME + " elements.");
            }
        }
    }

    protected boolean isStandartIdentitiesSetDefined() {
        return identitySets.size() != 0 || identities.size() != 0;
    }

    protected Set<String> getIdentityNames(ScriptExecutionContext context) {
        Set<String> identityNames = Sets.newHashSet();
        for (NamedIdentitySet identity : identitySets) {
            identityNames.addAll(identity.get(context));
        }
        for (Identity identity : identities) {
            identityNames.add(identity.name);
        }
        return identityNames;
    }
}
