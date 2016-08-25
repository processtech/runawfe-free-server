package ru.runa.wf.web.ftl.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

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
    private final List<String> displayFields;

    public UserTableColumns(WfVariable variable, String sortField, List<String> displayFields, boolean isMultiDim) {
        this.displayFields = Lists.newArrayList(displayFields);
        final VariableFormat componentFormat = FormatCommons.createComponent(variable, 0);
        if ((componentFormat instanceof UserTypeFormat)) {
            this.userType = ((UserTypeFormat) componentFormat).getUserType();
        } else {
            this.userType = null;
        }
        if (Strings.isNullOrEmpty(sortField) && null != this.userType && !this.userType.getAttributes().isEmpty()) {
            if (null == displayFields || displayFields.isEmpty()) {
                this.sortFieldName = this.userType.getAttributes().get(0).getName();
            } else {
                this.sortFieldName = displayFields.get(0);
            }
        } else {
            this.sortFieldName = sortField;
        }
        this.multiDim = isMultiDim;
        this.noSortableColumns = new ArrayList<Integer>();
        if (null != this.userType) {
            if (isMultiDim) {
                this.noSortableColumns.add(1);
            } else {
                for (int i = 0; i < this.displayFields.size(); i++) {
                    final String attribute = this.displayFields.get(i);
                    if (attribute.equals(sortField)) {
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

    public List<VariableDefinition> createAttributes() {
        if (null == getUserType()) {
            return Collections.emptyList();
        }
        final LinkedList<VariableDefinition> attributes = new LinkedList<VariableDefinition>();
        if (isMultiDim()) {
            attributes.add(getUserType().getAttribute(getSortFieldName()));
            attributes.add(createSelfDefinition());
        } else {
            if (null != displayFields && !displayFields.isEmpty()) {
                boolean sortFieldNotFound = true;
                for (final String field : displayFields) {
                    attributes.add(getUserType().getAttribute(field));
                    if (field.equals(getSortFieldName())) {
                        sortFieldNotFound = false;
                    }
                }
                if (sortFieldNotFound) {
                    attributes.addFirst(getUserType().getAttribute(getSortFieldName()));
                }
            } else {
                attributes.addAll(getUserType().getAttributes());
            }
        }
        return attributes;
    }

    private VariableDefinition createSelfDefinition() {
        return new VariableDefinition("", "_self", new UserTypeFormat(getUserType()));
    }

    public List<WfVariable> createValues(final UserTypeMap userTypeMap) {
        if (null == getUserType()) {
            return Collections.emptyList();
        }
        final LinkedList<WfVariable> values = new LinkedList<WfVariable>();
        if (isMultiDim()) {
            values.add(userTypeMap.getAttributeValue(getSortFieldName()));
            values.add(new WfVariable(createSelfDefinition(), userTypeMap));
        } else {
            if (null != displayFields && !displayFields.isEmpty()) {
                boolean sortFieldNotFound = true;
                for (final String field : displayFields) {
                    values.add(userTypeMap.getAttributeValue(field));
                    if (field.equals(getSortFieldName())) {
                        sortFieldNotFound = false;
                    }
                }
                if (sortFieldNotFound) {
                    values.addFirst(userTypeMap.getAttributeValue(getSortFieldName()));
                }
            } else {
                for (final String key : userTypeMap.keySet()) {
                    values.add(userTypeMap.getAttributeValue(key));
                }
            }
        }
        return values;
    }
}