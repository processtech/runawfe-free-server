package ru.runa.wfe.script.common;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.AdminScriptException;
import ru.runa.wfe.security.SecuredObjectType;

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
     * Returns initial capacity for get() target set. Counts possible duplicates; uselsess if "all named identities" are queried; so "estimated".
     */
    public Set<String> get(ScriptExecutionContext context) {
        if (identities.size() == 0 && identitySetsReferences.size() == 0) {
            if (Strings.isNullOrEmpty(name)) {
                throw new AdminScriptException("Identity set must have name (for named) or must not be empty.");
            }
            return context.getNamedIdentities(type, name);
        }
        Set<String> result = new HashSet<>();
        for (Identity id : identities) {
            result.add(id.name);
        }
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

    @XmlEnum
    public enum NamedIdentityType {
        @XmlEnumValue(value = "ProcessDefinition")
        PROCESS_DEFINITION(SecuredObjectType.DEFINITION, "ProcessDefinition"),

        @XmlEnumValue(value = "Executor")
        EXECUTOR(SecuredObjectType.EXECUTOR, "Executor"),

        @XmlEnumValue(value = "Report")
        REPORT(SecuredObjectType.REPORT, "Report");

        private SecuredObjectType securedObjectType;
        private String scriptName;

        NamedIdentityType(SecuredObjectType securedObjectType, String scriptName) {
            this.securedObjectType = securedObjectType;
            this.scriptName = scriptName;
        }

        public String getScriptName() {
            return scriptName;
        }

        public SecuredObjectType getSecuredObjectType() {
            if (securedObjectType == null) {
                throw new ScriptValidationException("namedIdentitySet/@type = " + name() + " is no longer supported");
            }
            return securedObjectType;
        }
    }
}
