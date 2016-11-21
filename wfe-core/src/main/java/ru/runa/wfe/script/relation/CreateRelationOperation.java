package ru.runa.wfe.script.relation;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationDoesNotExistException;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ExecutorsSetContainer;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.script.common.ScriptValidation;
import ru.runa.wfe.user.Executor;

@XmlType(name = CreateRelationOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class CreateRelationOperation extends ScriptOperation {

    public static final String SCRIPT_NAME = "createRelation";

    @XmlAttribute(name = AdminScriptConstants.NAME_ATTRIBUTE_NAME, required = true)
    public String relationName;

    @XmlAttribute(name = AdminScriptConstants.DESCRIPTION_ATTRIBUTE_NAME)
    public String description;

    @XmlElement(name = "left", namespace = AdminScriptConstants.NAMESPACE)
    public ExecutorsSetContainer left;

    @XmlElement(name = "right", namespace = AdminScriptConstants.NAMESPACE)
    public ExecutorsSetContainer right;

    @Override
    public void validate(ScriptExecutionContext context) {
        ScriptValidation.requiredAttribute(this, AdminScriptConstants.NAME_ATTRIBUTE_NAME, relationName);
    }

    @Override
    public void execute(ScriptExecutionContext context) {
        Relation relation;
        try {
            relation = context.getRelationLogic().getRelation(context.getUser(), relationName);
        } catch (RelationDoesNotExistException e) {
            relation = context.getRelationLogic().createRelation(context.getUser(), new Relation(relationName, description));
        }
        Collection<Executor> leftExecutors = left.getExecutors(context);
        Collection<Executor> rightExecutors = right.getExecutors(context);
        if (leftExecutors.isEmpty() || rightExecutors.isEmpty()) {
            return;
        }
        for (Executor right : rightExecutors) {
            for (Executor left : leftExecutors) {
                context.getRelationLogic().addRelationPair(context.getUser(), relation.getId(), left, right);
            }
        }
    }
}
