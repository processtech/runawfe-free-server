package ru.runa.wfe.lang.utils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.NodeErrorLog;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.logic.RelationSwimlaneInitializer;
import ru.runa.wfe.lang.MultiTaskNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.VariableContainerNode;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.var.VariableMapping;
import ru.runa.wfe.var.format.VariableFormatContainer;

@CommonsLog
public class MultiinstanceUtils {
    // back compatibility with processes before version 4.1.1
    private static final String USAGE_MULTIINSTANCE_VARS = "multiinstance-vars";

    public static Parameters parse(ExecutionContext executionContext, VariableContainerNode node) {
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
        Parameters parameters;
        if (fallbackToV410CompatibleMode) {
            parameters = parseBackCompatibleWithV410(executionContext, node);
        } else if (modernMode) {
            parameters = parseInModernMode(executionContext, node);
        } else if (canBeParsedInV3CompatibleMode) {
            parameters = parseBackCompatibleWithV3(executionContext, node);
        } else {
            throw new InternalApplicationException("No valid parameters found for multiinstances in " + node);
        }
        parameters.check(node);
        parameters.logIfDiscriminatorValueEmpty(executionContext, node);
        return parameters;
    }

    public static Parameters parse(ExecutionContext executionContext, MultiTaskNode node) {
        Parameters parameters = new Parameters();
        parameters.discriminatorVariableName = node.getDiscriminatorVariableName();
        VariableMapping mapping = new VariableMapping(parameters.discriminatorVariableName, null, node.getDiscriminatorUsage());
        if (Strings.isNullOrEmpty(mapping.getUsage()) || mapping.isMultiinstanceLinkByVariable()) {
            parameters.discriminatorValue = executionContext.getVariableProvider().getValueNotNull(List.class, parameters.discriminatorVariableName);
            parameters.discriminatorTypeVariable();
        } else if (mapping.isMultiinstanceLinkByGroup()) {
            setDiscriminatorValueByGroup(parameters, executionContext, mapping);
        } else if (mapping.isMultiinstanceLinkByRelation()) {
            setDiscriminatorValueByRelation(parameters, executionContext, mapping);
        } else {
            throw new InternalApplicationException("invalid discriminator mode: '" + mapping.getUsage() + "'");
        }
        parameters.logIfDiscriminatorValueEmpty(executionContext, node);
        return parameters;
    }

    public static void autoExtendContainerVariables(ExecutionContext executionContext, List<VariableMapping> variableMappings, int newSize) {
        for (VariableMapping variableMapping : variableMappings) {
            if (variableMapping.isMultiinstanceLink() && (variableMapping.isWritable() || variableMapping.isSyncable())) {
                String sizeVariableName = variableMapping.getName() + VariableFormatContainer.SIZE_SUFFIX;
                Number oldSize = (Number) executionContext.getVariableValue(sizeVariableName);
                if (oldSize == null || oldSize.intValue() < newSize) {
                    log.debug("Auto-extending " + sizeVariableName + ": " + oldSize + " -> " + newSize);
                    executionContext.setVariableValue(sizeVariableName, newSize);
                }
            }
        }
    }

    private static void setDiscriminatorValueByGroup(Parameters parameters, ExecutionContext executionContext, VariableMapping mapping) {
        Group group;
        if (mapping.isText()) {
            group = ApplicationContextFactory.getExecutorDao().getGroup(parameters.discriminatorVariableName);
        } else {
            group = executionContext.getVariableProvider().getValueNotNull(Group.class, parameters.discriminatorVariableName);
        }
        parameters.discriminatorValue = Lists.newArrayList(ApplicationContextFactory.getExecutorDAO().getGroupActors(group));
        parameters.discriminatorTypeGroup();
    }

    private static void setDiscriminatorValueByRelation(Parameters parameters, ExecutionContext executionContext, VariableMapping mapping) {
        RelationSwimlaneInitializer initializer = ApplicationContextFactory.autowireBean(new RelationSwimlaneInitializer());
        initializer.parse(parameters.discriminatorVariableName);
        if (!mapping.isText()) {
            String relationName = initializer.getRelationName();
            relationName = executionContext.getVariableProvider().getValueNotNull(String.class, relationName);
            initializer.setRelationName(relationName);
        }
        parameters.discriminatorValue = initializer.evaluate(executionContext.getVariableProvider());
        parameters.discriminatorTypeRelation(initializer.getRelationName());
    }

    private static Parameters parseInModernMode(ExecutionContext executionContext, VariableContainerNode node) {
        Parameters parameters = new Parameters();
        for (VariableMapping mapping : node.getVariableMappings()) {
            if (mapping.isMultiinstanceLinkByVariable() || mapping.isMultiinstanceLinkByGroup() || mapping.isMultiinstanceLinkByRelation()) {
                parameters.discriminatorVariableName = mapping.getName();
                parameters.iteratorVariableName = mapping.getMappedName();
                if (mapping.isMultiinstanceLinkByVariable()) {
                    parameters.discriminatorValue = executionContext.getVariableValue(parameters.discriminatorVariableName);
                    parameters.discriminatorTypeVariable();
                }
                if (mapping.isMultiinstanceLinkByGroup()) {
                    setDiscriminatorValueByGroup(parameters, executionContext, mapping);
                }
                if (mapping.isMultiinstanceLinkByRelation()) {
                    setDiscriminatorValueByRelation(parameters, executionContext, mapping);
                }
                break;
            }
        }
        return parameters;
    }

    private static Parameters parseBackCompatibleWithV410(ExecutionContext executionContext, VariableContainerNode node) {
        Parameters parameters = new Parameters();
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
                parameters.discriminatorVariableName = varName;
                parameters.iteratorVariableName = varSubName;
            } else if ("group".equals(miDiscriminatorType)) {
                parameters.discriminatorVariableName = groupName;
                parameters.iteratorVariableName = groupSubName;
            } else if ("relation".equals(miDiscriminatorType)) {
                parameters.discriminatorVariableName = relationName;
                parameters.iteratorVariableName = relationSubName;
            }
        }
        if ("variable".equals(miDiscriminatorType) && parameters.discriminatorVariableName != null) {
            parameters.discriminatorValue = executionContext.getVariableValue(parameters.discriminatorVariableName);
            parameters.discriminatorTypeVariable();
        } else if ("group".equals(miDiscriminatorType) && parameters.discriminatorVariableName != null) {
            Object miVar = ExpressionEvaluator.evaluateVariableNotNull(executionContext.getVariableProvider(), parameters.discriminatorVariableName);
            Group group = TypeConversionUtil.convertTo(Group.class, miVar);
            parameters.discriminatorValue = Lists.newArrayList(ApplicationContextFactory.getExecutorDAO().getGroupActors(group));
            parameters.discriminatorTypeGroup();
        } else if ("relation".equals(miDiscriminatorType) && parameters.discriminatorVariableName != null && miRelationDiscriminatorTypeParam != null) {
            String relationName = (String) ExpressionEvaluator.evaluateVariableNotNull(executionContext.getVariableProvider(),
                    parameters.discriminatorVariableName);
            Object relationParam = ExpressionEvaluator.evaluateVariableNotNull(executionContext.getVariableProvider(),
                    miRelationDiscriminatorTypeParam);
            Executor rightExecutor = TypeConversionUtil.convertTo(Executor.class, relationParam);
            parameters.discriminatorValue = getActorsByRelation(relationName, rightExecutor, true);
            parameters.discriminatorTypeRelation(relationName);
        }
        return parameters;
    }

    private static Parameters parseBackCompatibleWithV3(ExecutionContext executionContext, VariableContainerNode node) {
        Parameters parameters = new Parameters();
        log.debug("in BackCompatibleWithV3 mode");
        for (VariableMapping mapping : node.getVariableMappings()) {
            if (mapping.isMultiinstanceLink() && mapping.isReadable() && !mapping.isWritable()) {
                parameters.discriminatorVariableName = mapping.getName();
                parameters.discriminatorValue = executionContext.getVariableValue(parameters.discriminatorVariableName);
                if (parameters.discriminatorValue != null) {
                    parameters.iteratorVariableName = mapping.getMappedName();
                    break;
                }
            }
        }
        return parameters;
    }

    private static List<Actor> getActorsByRelation(String relationName, Executor paramExecutor, boolean inversed) {
        List<Executor> executors = Lists.newArrayList(paramExecutor);
        Relation relation = ApplicationContextFactory.getRelationDao().getNotNull(relationName);
        List<RelationPair> relationPairs;
        if (inversed) {
            relationPairs = ApplicationContextFactory.getRelationPairDao().getExecutorsRelationPairsLeft(relation, executors);
        } else {
            relationPairs = ApplicationContextFactory.getRelationPairDao().getExecutorsRelationPairsRight(relation, executors);
        }
        Set<Actor> actors = new HashSet<>();
        for (RelationPair pair : relationPairs) {
            Executor executor = pair.getRight();
            if (executor instanceof Actor) {
                actors.add((Actor) executor);
            } else if (executor instanceof Group) {
                actors.addAll(ApplicationContextFactory.getExecutorDao().getGroupActors((Group) executor));
            }
        }
        return Lists.newArrayList(actors);
    }

    public static class Parameters {
        private static final Properties logMessageProperties = ClassLoaderUtil.getLocalizedProperties("log.messages",
                Parameters.class, null);
        private String discriminatorVariableName;
        private String iteratorVariableName;
        private Object discriminatorValue;
        private String relationName;
        private DiscriminatorType discriminatorType;

        public String getDiscriminatorVariableName() {
            return discriminatorVariableName;
        }

        public Object getDiscriminatorValue() {
            return discriminatorValue;
        }

        public String getIteratorVariableName() {
            return iteratorVariableName;
        }

        public void discriminatorTypeVariable() {
            discriminatorType = DiscriminatorType.VARIABLE;
        }

        public void discriminatorTypeGroup() {
            discriminatorType = DiscriminatorType.GROUP;
        }

        public void discriminatorTypeRelation(String relationName) {
            discriminatorType = DiscriminatorType.RELATION;
            this.relationName = relationName;
        }

        protected void check(VariableContainerNode node) {
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

        @SuppressWarnings("unchecked")
        protected void logIfDiscriminatorValueEmpty(ExecutionContext executionContext, Node node) {
            if (discriminatorValue instanceof List && ((List<Object>) discriminatorValue).isEmpty()) {
                executionContext.addLog(new NodeErrorLog(node,
                        String.format(discriminatorType.getMessage(), relationName != null ? relationName : discriminatorVariableName)));
            }
        }

        private enum DiscriminatorType {
            VARIABLE((String) logMessageProperties.get("multiple.instance.not.created.by.variable")),
            GROUP((String) logMessageProperties.get("multiple.instance.not.created.by.group")),
            RELATION((String) logMessageProperties.get("multiple.instance.not.created.by.relation"));
            private final String message;

            DiscriminatorType(String message) {
                this.message = message;
            }

            public String getMessage() {
                return logMessageProperties.get("multiple.instance.not.created") + " " + message;
            }
        }
    }
}
