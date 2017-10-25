package ru.runa.wfe.var;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.VariableLog;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.var.dao.VariableLoader;
import ru.runa.wfe.var.format.VariableFormatContainer;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 * @author Alekseev Vitaly
 * @since Oct 24, 2017
 */
public class VariableBlobLoader {

    private static final Log log = LogFactory.getLog(VariableBlobLoader.class);

    private static final Pattern listEntryPattern = Pattern
            .compile("^(?<listPath>[[^\\[]]*)\\[[\\s]*(?<index>\\d{1,2})[\\s]*\\][\\s]*[\\.]{0,1}[\\s]*(?<fieldPath>.*)$");

    private final ExecutionContext executionContext;

    private final VariableLoader variableLoader;

    public VariableBlobLoader(ExecutionContext executionContext, VariableLoader variableLoader) {
        this.executionContext = executionContext;
        this.variableLoader = variableLoader;
    }

    public Variable<?> getVariableContainer(VariableDefinition variableDefinition) {
        if (variableDefinition.isListEntry()) {
            return getVariableListContainer(variableDefinition);
        } else if (variableDefinition.isUserTypeField()) {
            return getVariableUserTypeContainer(variableDefinition);
        }
        return null;
    }

    public VariableDefinition getVariableContainerDefinition(VariableDefinition variableDefinition) {
        if (variableDefinition.isListEntry()) {
            return getVariableListContainerDefinition(variableDefinition);
        } else if (variableDefinition.isUserTypeField()) {
            return getVariableUserTypeContainerDefinition(variableDefinition);
        }
        return null;
    }

    public VariableLog setVariableValue(Variable<?> container, VariableDefinition variableDefinition, Object value) {
        if (variableDefinition.isListEntry()) {
            return setVariableListEntryValue(container, variableDefinition, value);

        } else if (variableDefinition.isUserTypeField()) {
            return setVariableUserTypeFieldValue(container, variableDefinition, value);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    protected VariableLog setVariableListEntryValue(Variable<?> container, VariableDefinition variableDefinition, Object value) {
        final List<ListVariableEntryToken> tokens = tokenizeListEntryPath(variableDefinition.getName());
        if (tokens.isEmpty()) {
            return null;
        }
        final VariableDefinition containerDefinition = executionContext.getProcessDefinition().getVariable(tokens.get(0).listPath, false);
        List<?> containerValue = (List<?>) container.getValue();
        if (containerValue == null) {
            initializeList(containerDefinition, tokens.get(0));
            containerValue = (List<?>) variableLoader.get(executionContext.getProcess(), containerDefinition.getName()).getValue();
        }
        final StringBuilder path = new StringBuilder();
        List<Object> multiDimentionList = (List<Object>) containerValue;
        for (int i = 0; i < tokens.size(); i++) {
            final ListVariableEntryToken token = tokens.get(i);
            path.append(token.listPath);
            int index = Integer.valueOf(token.index);
            path.append(VariableFormatContainer.COMPONENT_QUALIFIER_START);
            path.append(token.index);
            path.append(VariableFormatContainer.COMPONENT_QUALIFIER_END);
            final VariableDefinition entryDefinition = executionContext.getProcessDefinition().getVariable(path.toString(), false);
            Object entry = getListEntryValue(multiDimentionList, path.toString(), index);
            if (entry == null) {
                if (i == tokens.size() - 1) {
                    if (entryDefinition.isUserType()) {
                        UserTypeMap entryValue = new UserTypeMap(entryDefinition.getUserType());
                        entryValue.put(userTypeSubpath(entryDefinition, variableDefinition), value);
                        setListEntryValue(multiDimentionList, index, entryValue);
                    } else {
                        setListEntryValue(multiDimentionList, index, value);
                    }
                } else {
                    if (entryDefinition.isList()) {
                        List<Object> nextList = Lists.newArrayList();
                        setListEntryValue(multiDimentionList, index, nextList);
                        multiDimentionList = nextList;
                    } else if (entryDefinition.isUserType()) {
                        UserTypeMap entryValue = new UserTypeMap(entryDefinition.getUserType());
                        List<Object> nextList = Lists.newArrayList();
                        entryValue.put(tokens.get(i + 1).listPath, nextList);
                        setListEntryValue(multiDimentionList, index, entryValue);
                        multiDimentionList = nextList;
                        path.append(UserType.DELIM);
                    } else {
                        throw new InternalApplicationException(String.format(
                                "unespected list entry type: token index: %s tokens: %s variableDefinition: %s", i, tokens, variableDefinition));
                    }
                }
            } else if (entry instanceof List) {
                multiDimentionList = (List<Object>) entry;
            } else if (entry instanceof UserTypeMap) {
                if (i == tokens.size() - 1) {
                    log.debug(String.format("setVariableListEntryValue: set entry as usertype: entryDefinition: %s variableDefinition: %s",
                            entryDefinition, variableDefinition));
                    ((UserTypeMap) entry).put(userTypeSubpath(entryDefinition, variableDefinition), value);
                } else {
                    multiDimentionList = (List<Object>) ((UserTypeMap) entry).get(tokens.get(i + 1).listPath, null);
                    if (multiDimentionList == null) {
                        multiDimentionList = Lists.newArrayList();
                        ((UserTypeMap) entry).put(tokens.get(i + 1).listPath, multiDimentionList);
                    }
                }
                path.append(UserType.DELIM);
            } else {
                setListEntryValue(multiDimentionList, index, value);
                break;
            }
        }
        return container.setValue(executionContext, containerValue, containerDefinition);
    }

    protected VariableLog setVariableUserTypeFieldValue(Variable<?> container, VariableDefinition variableDefinition, Object value) {
        final VariableDefinition containerDefinition = getVariableUserTypeContainerDefinition(variableDefinition);
        if (containerDefinition == null) {
            return null;
        }
        UserTypeMap containerValue = getUserTypeValue(containerDefinition);
        log.debug(String.format("setVariableUserTypeFieldValue: containerDefinition: %s variableDefinition: %s", containerDefinition,
                variableDefinition));
        containerValue.put(userTypeSubpath(containerDefinition, variableDefinition), value);
        return container.setValue(executionContext, containerValue, containerDefinition);
    }

    protected Variable<?> getVariableListContainer(VariableDefinition variableDefinition) {
        final List<ListVariableEntryToken> tokens = tokenizeListEntryPath(variableDefinition.getName());
        if (tokens.isEmpty()) {
            return null;
        }
        final VariableDefinition containerDefinition = executionContext.getProcessDefinition().getVariable(tokens.get(0).listPath, false);
        if (containerDefinition == null || containerDefinition.getStoreType() != VariableStoreType.BLOB) {
            return null;
        }
        initializeList(containerDefinition, tokens.get(0));
        return variableLoader.get(executionContext.getProcess(), containerDefinition.getName());
    }

    protected Variable<?> getVariableUserTypeContainer(VariableDefinition variableDefinition) {
        final VariableDefinition containerDefinition = getVariableUserTypeContainerDefinition(variableDefinition);
        if (containerDefinition == null || !containerDefinition.isUserType() || containerDefinition.getStoreType() != VariableStoreType.BLOB) {
            return null;
        }
        setUserTypeValue(containerDefinition, variableDefinition, null);
        return variableLoader.get(executionContext.getProcess(), containerDefinition.getName());
    }

    private void initializeList(VariableDefinition listDefinition, ListVariableEntryToken token) {
        List<?> value = (List<?>) executionContext.getVariableValue(listDefinition.getName());
        if (value == null) {
            value = Lists.newArrayList();
        }
        int index = Integer.valueOf(token.index);
        while (value.size() <= index) {
            value.add(null);
        }
        final VariableDefinition containerDefinition = getVariableUserTypeContainerDefinition(listDefinition);
        if (containerDefinition != null) {
            setUserTypeValue(containerDefinition, listDefinition, value);
        } else {
            executionContext.setVariableValue(listDefinition.getName(), value);
        }
    }

    private Object getListEntryValue(List<?> list, String listPath, int index) {
        while (list.size() <= index) {
            list.add(null);
        }
        return list.get(index);
    }

    private void setListEntryValue(List<Object> list, int index, Object value) {
        while (list.size() <= index) {
            list.add(null);
        }
        list.set(index, value);
    }

    private void setUserTypeValue(VariableDefinition containerDefinition, VariableDefinition field, Object value) {
        final UserTypeMap containerValue = getUserTypeValue(containerDefinition);
        containerValue.put(userTypeSubpath(containerDefinition, field), value);
        executionContext.setVariableValue(containerDefinition.getName(), containerValue);
    }

    private UserTypeMap getUserTypeValue(VariableDefinition definition) {
        UserTypeMap result = (UserTypeMap) executionContext.getVariableValue(definition.getName());
        if (result == null) {
            result = new UserTypeMap(definition.getUserType());
        }
        return result;
    }

    private VariableDefinition getVariableListContainerDefinition(VariableDefinition variableDefinition) {
        final List<ListVariableEntryToken> tokens = tokenizeListEntryPath(variableDefinition.getName());
        if (tokens.isEmpty()) {
            return null;
        }
        return executionContext.getProcessDefinition().getVariable(tokens.get(0).listPath, false);
    }

    private VariableDefinition getVariableUserTypeContainerDefinition(VariableDefinition definition) {
        final List<String> tokens = tokenizeUserTypePath(definition.getName());
        if (tokens.isEmpty()) {
            return null;
        }
        final VariableDefinition result = executionContext.getProcessDefinition().getVariable(tokens.get(0), false);
        if (result == null) {
            throw new InternalApplicationException(String.format("not found user type container of definition: %s by tokens: %s", definition, tokens));
        }
        if (!result.isUserType()) {
            return null;
        }
        return result;
    }

    private static final List<ListVariableEntryToken> tokenizeListEntryPath(String name) {
        final List<ListVariableEntryToken> result = Lists.newArrayList();
        Matcher matcher;
        while ((matcher = listEntryPattern.matcher(name)) != null && matcher.find()) {
            final ListVariableEntryToken token = new ListVariableEntryToken();
            token.listPath = matcher.group("listPath");
            token.fieldPath = matcher.group("fieldPath");
            token.index = matcher.group("index");
            name = token.fieldPath;
            result.add(token);
        }
        return result;
    }

    private static final List<String> tokenizeUserTypePath(String name) {
        return Lists.newArrayList(Splitter.on(UserType.DELIM).trimResults().split(name));
    }

    private static final String userTypeSubpath(VariableDefinition root, VariableDefinition field) {
        return field.getName().replaceFirst(root.getName().replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\][\\.]{0,1}"), "");
    }

    private static class ListVariableEntryToken {
        private String listPath;
        private String index;
        private String fieldPath;

        @Override
        public String toString() {
            return String.format("{listPath: \"%s\", index: \"%s\" fieldPath: \"%s\"}", listPath, index, fieldPath);
        }
    }
}
