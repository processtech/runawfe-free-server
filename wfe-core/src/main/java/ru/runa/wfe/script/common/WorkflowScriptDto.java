package ru.runa.wfe.script.common;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.w3c.dom.Element;
import ru.runa.wfe.script.AdminScriptConstants;

@XmlType(name = "workflowScriptType", namespace = AdminScriptConstants.NAMESPACE)
@XmlRootElement(name = "workflowScript", namespace = AdminScriptConstants.NAMESPACE)
public class WorkflowScriptDto extends OperationsListContainer {

    @XmlElement(name = AdminScriptConstants.NAMED_IDENTITY_ELEMENT_NAME, namespace = AdminScriptConstants.NAMESPACE)
    public List<NamedIdentitySet> identitySets = new ArrayList<>();

    @XmlAttribute(name = "defaultTransactionScope")
    public TransactionScopeType defaultTransactionScope;

    @XmlElement(name = "transactionScope", namespace = AdminScriptConstants.NAMESPACE)
    public List<TransactionScopeDto> transactionScopes = new ArrayList<>();

    @XmlAnyElement
    public List<Element> unknownOperations = new ArrayList<>();

    public void validate(boolean allowTransactionScope) {
        if (!operations.isEmpty() && !transactionScopes.isEmpty()) {
            throw new ScriptValidationException("workflowScript must contain only operations or transactionScope elements (not both at same time).");
        }
        if (!allowTransactionScope && !transactionScopes.isEmpty()) {
            throw new ScriptValidationException(
                    "workflowScript must not contains transactionScope elements at script execution stage (process it before service call).");
        }
    }

    /**
     * Validates script and register all named sets.
     *
     * @param context
     *            Script execution context.
     * @param allowTransactionScope
     *            Allow transaction scope elements ot not.
     */
    public void validateFullAndRegister(ScriptExecutionContext context, boolean allowTransactionScope) {
        validate(allowTransactionScope);
        for (NamedIdentitySet set : identitySets) {
            set.register(context);
        }
        for (ScriptOperation operation : operations) {
            operation.validate(context);
        }
    }

    @Override
    public List<String> getExternalResourceNames() {
        List<String> result = super.getExternalResourceNames();
        for (TransactionScopeDto scope : transactionScopes) {
            result.addAll(scope.getExternalResourceNames());
        }
        return result;
    }

}
