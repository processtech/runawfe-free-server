package ru.runa.wfe.office.doc;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.LongFormat;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.VariableFormatContainer;

public class AbstractIteratorOperation extends Operation {
    protected IterateBy iterateBy;
    protected String containerVariableName;
    protected WfVariable containerVariable;
    protected Object containerValue;

    public IterateBy getIterateBy() {
        return iterateBy;
    }

    public void setIterateBy(IterateBy iterateBy) {
        this.iterateBy = iterateBy;
    }

    public String getContainerVariableName() {
        return containerVariableName;
    }

    public void setContainerVariableName(String containerName) {
        this.containerVariableName = containerName;
    }

    @Override
    public String getName() {
        return iterateBy.name();
    }

    @Override
    public boolean isValid() {
        return iterateBy != null && containerVariableName != null && (getContainerValue() instanceof Map || getContainerValue() instanceof List);
    }

    public Object getContainerValue() {
        return containerValue;
    }

    public void setContainerValue(Object containerValue) {
        this.containerValue = containerValue;
    }

    public void setContainerVariable(WfVariable containerVariable) {
        this.containerVariable = containerVariable;
        if (containerVariable != null) {
            setContainerValue(containerVariable.getValue());
        }
    }

    public Iterator<? extends Object> createIterator() {
        if (iterateBy == IterateBy.indexes) {
            List<?> list = (List<?>) getContainerValue();
            List<Long> indexes = Lists.newArrayListWithExpectedSize(list.size());
            for (int i = 0; i < list.size(); i++) {
                indexes.add(new Long(i));
            }
            return indexes.iterator();
        }
        if (iterateBy == IterateBy.numbers) {
            List<?> list = (List<?>) getContainerValue();
            List<Long> indexes = Lists.newArrayListWithExpectedSize(list.size());
            for (int i = 0; i < list.size(); i++) {
                indexes.add(new Long(i + 1));
            }
            return indexes.iterator();
        }
        if (iterateBy == IterateBy.keys) {
            return ((Map<?, ?>) getContainerValue()).keySet().iterator();
        }
        if (iterateBy == IterateBy.items) {
            return ((List<?>) getContainerValue()).iterator();
        }
        if (iterateBy == IterateBy.values) {
            return ((Map<?, ?>) getContainerValue()).values().iterator();
        }
        return null;
    }

    public String getIteratorFormatClassName() {
        if (iterateBy == IterateBy.indexes || iterateBy == IterateBy.numbers) {
            return LongFormat.class.getName();
        }
        if (containerVariable == null) {
            return StringFormat.class.getName();
        }
        if (iterateBy == IterateBy.keys) {
            return ((VariableFormatContainer) containerVariable.getDefinition().getFormatNotNull()).getComponentClassName(0);
        }
        if (iterateBy == IterateBy.items) {
            return ((VariableFormatContainer) containerVariable.getDefinition().getFormatNotNull()).getComponentClassName(0);
        }
        if (iterateBy == IterateBy.values) {
            return ((VariableFormatContainer) containerVariable.getDefinition().getFormatNotNull()).getComponentClassName(1);
        }
        return null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("by", iterateBy).add("container", containerVariableName).add("item", iterateBy).toString();
    }
}
