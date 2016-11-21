package ru.runa.wfe.script.common;

import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.AdminScriptException;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@XmlType(name = "NamedIdentitySetType", namespace = AdminScriptConstants.NAMESPACE)
public class NamedIdentitySet {

    @XmlAttribute(name = AdminScriptConstants.NAME_ATTRIBUTE_NAME)
    public String name;

    @XmlAttribute(name = AdminScriptConstants.TYPE_ATTRIBUTE_NAME, required = true)
    public NamedIdentityType type;

    @XmlElement(name = AdminScriptConstants.IDENTITY_ELEMENT_NAME, namespace = AdminScriptConstants.NAMESPACE)
    public List<Identity> identities = Lists.newArrayList();

    @XmlElement(name = AdminScriptConstants.NAMED_IDENTITY_ELEMENT_NAME, namespace = AdminScriptConstants.NAMESPACE)
    public List<NamedIdentitySet> identitySetsReferences = Lists.newArrayList();

    /**
     * Check identity set for requesting type match.
     * 
     * @param scriptOperation
     *            Script operation or null, if identity set declared in global scope.
     * @param requiredType
     *            Required identity set type
     */
    public void ensureType(ScriptOperation scriptOperation, NamedIdentityType requiredType) {
        if (requiredType != type) {
            throw new ScriptValidationException(scriptOperation, "Required identity set of type " + requiredType.getScriptName() + ".");
        }
        for (NamedIdentitySet innerSet : identitySetsReferences) {
            if (innerSet.type != type) {
                throw new ScriptValidationException(scriptOperation, "Inner named identity must be the same type as outer.");
            }
            innerSet.ensureType(scriptOperation, requiredType);
        }
    }

    /**
     * Get defined identities
     * 
     * @param context
     *            Script execution context.
     * @return Return set of identities, defined in current element.
     */
    public Set<String> get(ScriptExecutionContext context) {
        if (identities.size() == 0 && identitySetsReferences.size() == 0) {
            if (Strings.isNullOrEmpty(name)) {
                throw new AdminScriptException("Identity set must have name (for named) or must not be empty.");
            }
            return context.getNamedIdentities(type, name);
        }
        Set<String> result = Sets.newHashSet();
        result.addAll(Lists.transform(identities, new Function<Identity, String>() {

            @Override
            public String apply(Identity input) {
                return input.name;
            }
        }));
        for (NamedIdentitySet innerSet : identitySetsReferences) {
            if (innerSet.type != type) {
                throw new AdminScriptException("Inner named identity must be the same type as outer.");
            }
            result.addAll(innerSet.get(context));
        }
        return result;
    }

    /**
     * Register current set and all subset's in global context for future referencing.
     * 
     * @param context
     *            Script execution context.
     */
    public void register(ScriptExecutionContext context) {
        for (NamedIdentitySet innerSet : identitySetsReferences) {
            if (innerSet.type != type) {
                throw new AdminScriptException("Inner named identity must be the same type as outer.");
            }
            innerSet.register(context);
        }
        if (!Strings.isNullOrEmpty(name)) {
            context.registerNamedIdentities(type, name, get(context));
        }
    }

    @XmlEnum(value = String.class)
    public enum NamedIdentityType {
        @XmlEnumValue(value = "ProcessDefinition")
        PROCESS_DEFINITION {

            @Override
            public String getScriptName() {
                return "ProcessDefinition";
            }
        },

        @XmlEnumValue(value = "Executor")
        EXECUTOR {

            @Override
            public String getScriptName() {
                return "Executor";
            }
        },

        @XmlEnumValue(value = "Relation")
        RELATION {

            @Override
            public String getScriptName() {
                return "Relation";
            }
        },

        @XmlEnumValue(value = "Report")
        REPORT {

            @Override
            public String getScriptName() {
                return "Report";
            }
        };

        public abstract String getScriptName();
    }
}
