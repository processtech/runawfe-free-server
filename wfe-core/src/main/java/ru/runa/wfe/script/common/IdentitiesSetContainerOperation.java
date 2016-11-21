package ru.runa.wfe.script.common;

import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.NamedIdentitySet.NamedIdentityType;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@XmlTransient()
public abstract class IdentitiesSetContainerOperation extends ScriptOperation {

    private final NamedIdentityType identitiesType;

    @XmlElement(name = AdminScriptConstants.NAMED_IDENTITY_ELEMENT_NAME, namespace = AdminScriptConstants.NAMESPACE)
    public List<NamedIdentitySet> identitySets = Lists.newArrayList();

    protected IdentitiesSetContainerOperation() {
        identitiesType = null;
    }

    protected IdentitiesSetContainerOperation(NamedIdentityType identitiesType) {
        super();
        this.identitiesType = identitiesType;
    }

    protected void validate(boolean requiredNotEmpty) {
        for (NamedIdentitySet set : identitySets) {
            set.ensureType(this, identitiesType);
        }
        if (requiredNotEmpty) {
            if (identitySets.size() == 0) {
                throw new ScriptValidationException(this, "Required " + AdminScriptConstants.NAMED_IDENTITY_ELEMENT_NAME + " elements.");
            }
        }
    }

    protected boolean isStandartIdentitiesSetDefined() {
        return identitySets.size() != 0;
    }

    protected Set<String> getIdentityNames(ScriptExecutionContext context) {
        Set<String> identityNames = Sets.newHashSet();
        for (NamedIdentitySet identity : identitySets) {
            identityNames.addAll(identity.get(context));
        }
        return identityNames;
    }
}
