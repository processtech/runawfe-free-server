package ru.runa.wfe.lang.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.logic.RelationSwimlaneInitializer;
import ru.runa.wfe.lang.MultiTaskNode;
import ru.runa.wfe.lang.VariableContainerNode;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.var.VariableMapping;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class MultiNodeParameters {
    private static final Log log = LogFactory.getLog(MultiNodeParameters.class);
    // back compatibility with processes before version 4.1.1
    private static final String USAGE_MULTIINSTANCE_VARS = "multiinstance-vars";
    private String discriminatorVariableName;
    private String iteratorVariableName;
    private Object discriminatorValue;

    public MultiNodeParameters(ExecutionContext executionContext, VariableContainerNode node) {
        boolean fallbackToV410CompatibleMode = false;
        boolean canBeParsedInV3CompatibleMode = false;
        boolean modernMode = false;
        for (VariableMapping mapping : node.getVariableMappings()) {
            if (mapping.hasUsage(USAGE_MULTIINSTANCE_VARS)) {
                fallbackToV410CompatibleMode = true;
                break;
            }
            if (mapping.isMultiinstanceLinkByVariable() || mapping.isMultiinstanceLinkByGroup() || mapping.isMultiinstanceLinkByRelation()) {
                modernMode = true;
            }
            if (mapping.isMultiinstanceLink() && mapping.isReadable() && !mapping.isWritable()) {
                canBeParsedInV3CompatibleMode = true;
            }
        }
        if (fallbackToV410CompatibleMode) {
            parseBackCompatibleWithV410(executionContext, node);
        } else if (modernMode) {
            parseInModernMode(executionContext, node);
        } else if (canBeParsedInV3CompatibleMode) {
            parseBackCompatibleWithV3(executionContext, node);
        } else {
            throw new InternalApplicationException("No valid parameters found for multiinstances in " + node);
        }
        check(node);
    }

    public MultiNodeParameters(ExecutionContext executionContext, MultiTaskNode node) {
        discriminatorVariableName = node.getDiscriminatorVariableName();
        VariableMapping mapping = new VariableMapping(discriminatorVariableName, null, node.getDiscriminatorUsage());
        if (Strings.isNullOrEmpty(mapping.getUsage()) || mapping.isMultiinstanceLinkByVariable()) {
            discriminatorValue = executionContext.getVariableProvider().getValueNotNull(List.class, discriminatorVariableName);
        } else if (mapping.isMultiinstanceLinkByGroup()) {
            setDiscriminatorValueByGroup(executionContext, mapping);
        } else if (mapping.isMultiinstanceLinkByRelation()) {
            setDiscriminatorValueByRelation(executionContext, mapping);
        } else {
            throw new InternalApplicationException("invalid discriminator mode: '" + mapping.getUsage() + "'");
        }
    }

    private void setDiscriminatorValueByGroup(ExecutionContext executionContext, VariableMapping mapping) {
        Group group;
        if (mapping.isText()) {
            group = ApplicationContextFactory.getExecutorDAO().getGroup(discriminatorVariableName);
        } else {
            group = executionContext.getVariableProvider().getValueNotNull(Group.class, discriminatorVariableName);
        }
        discriminatorValue = Lists.newArrayList(ApplicationContextFactory.getExecutorDAO().getGroupActors(group));
    }

    private void setDiscriminatorValueByRelation(ExecutionContext executionContext, VariableMapping mapping) {
        RelationSwimlaneInitializer initializer = ApplicationContextFactory.autowireBean(new RelationSwimlaneInitializer());
        initializer.parse(discriminatorVariableName);
        if (!mapping.isText()) {
            String relationName = initializer.getRelationName();
            relationName = executionContext.getVariableProvider().getValueNotNull(String.class, relationName);
            initializer.setRelationName(relationName);
        }
        discriminatorValue = initializer.evaluate(executionContext.getVariableProvider());
    }

    private void parseInModernMode(ExecutionContext executionContext, VariableContainerNode node) {
        for (VariableMapping mapping : node.getVariableMappings()) {
            if (mapping.isMultiinstanceLinkByVariable() || mapping.isMultiinstanceLinkByGroup() || mapping.isMultiinstanceLinkByRelation()) {
                discriminatorVariableName = mapping.getName();
                iteratorVariableName = mapping.getMappedName();
                if (mapping.isMultiinstanceLinkByVariable()) {
                    discriminatorValue = executionContext.getVariableValue(discriminatorVariableName);
                }
                if (mapping.isMultiinstanceLinkByGroup()) {
                    setDiscriminatorValueByGroup(executionContext, mapping);
                }
                if (mapping.isMultiinstanceLinkByRelation()) {
                    setDiscriminatorValueByRelation(executionContext, mapping);
                }
                break;
            }
        }
    }

    private void parseBackCompatibleWithV410(ExecutionContext executionContext, VariableContainerNode node) {
        log.debug("in BackCompatibleWithV410 mode");
        String miRelationDiscriminatorTypeParam = null;
        String miDiscriminatorType = null;
        {
            String varName = null, groupName = null, relationName = null;
            String varSubName = null, groupSubName = null, relationSubName = null;
            for (VariableMapping vm : node.getVariableMappings()) {
                if (vm.hasUsage(USAGE_MULTIINSTANCE_VARS)) {
                    if ("tabVariableProcessVariable".equals(vm.getName())) {
                        varName = vm.getMappedName();
                    } else if ("tabVariableSubProcessVariable".equals(vm.getName())) {
                        varSubName = vm.getMappedName();
                    } else if ("tabGroupName".equals(vm.getName())) {
                        groupName = vm.getMappedName();
                    } else if ("tabGroupSubProcessVariable".equals(vm.getName())) {
                        groupSubName = vm.getMappedName();
                    } else if ("tabRelationName".equals(vm.getName())) {
                        relationName = vm.getMappedName();
                    } else if ("tabRelationParam".equals(vm.getName())) {
                        miRelationDiscriminatorTypeParam = vm.getMappedName();
                    } else if ("tabRelationSubProcessVariable".equals(vm.getName())) {
                        relationSubName = vm.getMappedName();
                    } else if ("typeMultiInstance".equals(vm.getName())) {
                        miDiscriminatorType = vm.getMappedName();
                    }
                }
            }
            if ("variable".equals(miDiscriminatorType)) {
                discriminatorVariableName = varName;
                iteratorVariableName = varSubName;
            } else if ("group".equals(miDiscriminatorType)) {
                discriminatorVariableName = groupName;
                iteratorVariableName = groupSubName;
            } else if ("relation".equals(miDiscriminatorType)) {
                discriminatorVariableName = relationName;
                iteratorVariableName = relationSubName;
            }
        }
        if ("variable".equals(miDiscriminatorType) && discriminatorVariableName != null) {
            discriminatorValue = executionContext.getVariableValue(discriminatorVariableName);
        } else if ("group".equals(miDiscriminatorType) && discriminatorVariableName != null) {
            Object miVar = ExpressionEvaluator.evaluateVariableNotNull(executionContext.getVariableProvider(), discriminatorVariableName);
            Group group = TypeConversionUtil.convertTo(Group.class, miVar);
            discriminatorValue = Lists.newArrayList(ApplicationContextFactory.getExecutorDAO().getGroupActors(group));
        } else if ("relation".equals(miDiscriminatorType) && discriminatorVariableName != null && miRelationDiscriminatorTypeParam != null) {
            String relationName = (String) ExpressionEvaluator.evaluateVariableNotNull(executionContext.getVariableProvider(),
                    discriminatorVariableName);
            Object relationParam = ExpressionEvaluator.evaluateVariableNotNull(executionContext.getVariableProvider(),
                    miRelationDiscriminatorTypeParam);
            Executor rightExecutor = TypeConversionUtil.convertTo(Executor.class, relationParam);
            discriminatorValue = getActorsByRelation(relationName, rightExecutor, true);
        }
    }

    private void parseBackCompatibleWithV3(ExecutionContext executionContext, VariableContainerNode node) {
        log.debug("in BackCompatibleWithV3 mode");
        for (VariableMapping mapping : node.getVariableMappings()) {
            if (mapping.isMultiinstanceLink() && mapping.isReadable() && !mapping.isWritable()) {
                discriminatorVariableName = mapping.getName();
                discriminatorValue = executionContext.getVariableValue(discriminatorVariableName);
                if (discriminatorValue != null) {
                    iteratorVariableName = mapping.getMappedName();
                    break;
                }
            }
        }
    }

    private List<Actor> getActorsByRelation(String relationName, Executor paramExecutor, boolean inversed) {
        List<Executor> executors = Lists.newArrayList(paramExecutor);
        Relation relation = ApplicationContextFactory.getRelationDAO().getNotNull(relationName);
        List<RelationPair> relationPairs;
        if (inversed) {
            relationPairs = ApplicationContextFactory.getRelationPairDAO().getExecutorsRelationPairsLeft(relation, executors);
        } else {
            relationPairs = ApplicationContextFactory.getRelationPairDAO().getExecutorsRelationPairsRight(relation, executors);
        }
        Set<Actor> actors = new HashSet<Actor>();
        for (RelationPair pair : relationPairs) {
            Executor executor = pair.getRight();
            if (executor instanceof Actor) {
                actors.add((Actor) executor);
            } else if (executor instanceof Group) {
                actors.addAll(ApplicationContextFactory.getExecutorDAO().getGroupActors((Group) executor));
            }
        }
        return Lists.newArrayList(actors);
    }

    private void check(VariableContainerNode node) {
        if (discriminatorVariableName == null) {
            throw new RuntimeException("processVariableName == null in " + node);
        }
        if (iteratorVariableName == null) {
            throw new RuntimeException("subprocessVariableName == null in " + node);
        }
        if (discriminatorValue == null) {
            throw new RuntimeException("discriminatorValue == null in " + node);
        }
    }

    public String getDiscriminatorVariableName() {
        return discriminatorVariableName;
    }

    public Object getDiscriminatorValue() {
        return discriminatorValue;
    }

    public String getIteratorVariableName() {
        return iteratorVariableName;
    }
}
