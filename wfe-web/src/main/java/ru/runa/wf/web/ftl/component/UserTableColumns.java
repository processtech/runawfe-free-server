package ru.runa.wf.web.ftl.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.UserTypeFormat;
import ru.runa.wfe.var.format.VariableFormat;

public class UserTableColumns {
    private final UserType userType;
    private final String sortFieldName;
    private final boolean multiDim;
    protected final List<Integer> noSortableColumns;
    private int sortColumn = 0;

    public UserTableColumns(WfVariable variable, String sortField, boolean isMultiDim) {
        final VariableFormat componentFormat = FormatCommons.createComponent(variable, 0);
        if ((componentFormat instanceof UserTypeFormat)) {
            this.userType = ((UserTypeFormat) componentFormat).getUserType();
        } else {
            this.userType = null;
        }
        this.sortFieldName = sortField;
        this.multiDim = isMultiDim;
        this.noSortableColumns = new ArrayList<Integer>();
        if (null != this.userType) {
            if (isMultiDim) {
                this.noSortableColumns.add(1);
            } else {
                final List<VariableDefinition> attributes = this.userType.getAttributes();
                for (int i = 0; i < attributes.size(); i++) {
                    final VariableDefinition attribute = attributes.get(i);
                    if (attribute.getName().equals(sortField)) {
                        this.sortColumn = i;
                        break;
                    }
                }
            }
        }
    }

    public UserType getUserType() {
        return userType;
    }

    public String getSortFieldName() {
        return sortFieldName;
    }

    public boolean isMultiDim() {
        return multiDim;
    }

    public Integer getSortColumn() {
        return sortColumn;
    }

    public List<Integer> getNoSortableColumns() {
        return noSortableColumns;
    }

    protected List<VariableDefinition> createAttributes() {
        if (null == getUserType()) {
            return Collections.emptyList();
        }
        final List<VariableDefinition> attributes = new ArrayList<VariableDefinition>();
        if (isMultiDim()) {
            attributes.add(getUserType().getAttribute(getSortFieldName()));
            attributes.add(createSelfDefinition());
        } else {
            attributes.addAll(getUserType().getAttributes());
        }
        return attributes;
    }

    private VariableDefinition createSelfDefinition() {
        return new VariableDefinition("", "_self", new UserTypeFormat(getUserType()));
    }

    protected List<WfVariable> createValues(final UserTypeMap userTypeMap) {
        if (null == getUserType()) {
            return Collections.emptyList();
        }
        final List<WfVariable> values = new ArrayList<WfVariable>();
        if (isMultiDim()) {
            values.add(userTypeMap.getAttributeValue(getSortFieldName()));
            values.add(new WfVariable(createSelfDefinition(), userTypeMap));
        } else {
            for (final String key : userTypeMap.keySet()) {
                values.add(userTypeMap.getAttributeValue(key));
            }
        }
        return values;
    }
}