package ru.runa.wf.web.ftl.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import ru.runa.wfe.commons.ftl.FormComponentSubmissionHandler;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.EditableCheckBoxFormat;

public class MultipleSelectFromUserTypeList extends AbstractListUserVariables implements FormComponentSubmissionHandler {
    private static final long serialVersionUID = 1L;
    private static final String CHECKED = "checked";

    @Override
    protected Object renderRequest() throws Exception {
        initFields();
        return ViewUtil.getUserTypeListTable(user, webHelper, variableProvider.getVariableNotNull(variableName),
                variableProvider.getVariableNotNull(dectVariableName), variableProvider.getProcessId(), new MultySelectUsertTableColumns(
                        variableProvider.getVariableNotNull(variableName), sortField, displayMode == DisplayMode.MULTI_DIMENTIONAL_TABLE));
    }

    @Override
    public Map<String, ? extends Object> extractVariables(Interaction interaction, VariableDefinition variableDefinition,
            Map<String, ? extends Object> userInput, Map<String, String> formatErrors) throws Exception {
        final Map<String, Object> result = Maps.newHashMap();
        final String variablename = getVariableNameForSubmissionProcessing();
        final List<Object> selected = new ArrayList<Object>();
        final List<?> list = getParameterVariableValue(List.class, 1, null);
        for (int i = 0; i < list.size(); i++) {
            final String[] checked = (String[]) userInput.get(variablename + "[" + i + "]." + CHECKED);
            if (null != checked && 0 < checked.length && checked[0].equals("on")) {
                selected.add(list.get(i));
            }
        }
        result.put(getVariableNameForSubmissionProcessing(), selected);
        return result;
    }

    public class MultySelectUsertTableColumns extends UserTableColumns {

        public MultySelectUsertTableColumns(WfVariable variable, String sortField, boolean isMultiDim) {
            super(variable, sortField, isMultiDim);
        }

        @Override
        public Integer getSortColumn() {
            return super.getSortColumn() + 1;
        }

        @Override
        public List<Integer> getNoSortableColumns() {
            final List<Integer> columns = new ArrayList<Integer>();
            columns.add(0);
            for (Integer column : super.getNoSortableColumns()) {
                columns.add(column + 1);
            }
            return columns;
        }

        @Override
        protected List<VariableDefinition> createAttributes() {
            if (null == getUserType()) {
                return Collections.emptyList();
            }
            final List<VariableDefinition> attributes = new ArrayList<VariableDefinition>();
            attributes.add(createChekBoxDefinition(-1));
            attributes.addAll(super.createAttributes());
            return attributes;
        }

        private VariableDefinition createChekBoxDefinition(final int i) {
            return new VariableDefinition(dectVariableName + "[" + i + "]." + CHECKED, "", new EditableCheckBoxFormat());
        }

        @Override
        protected List<WfVariable> createValues(UserTypeMap userTypeMap) {
            if (null == getUserType()) {
                return Collections.emptyList();
            }
            final WfVariable dectVariable = variableProvider.getVariableNotNull(dectVariableName);
            Boolean val = false;
            if (dectVariable.getValue() instanceof List) {
                for (Object dectElement : (List<?>) dectVariable.getValue()) {
                    if (userTypeMap.equals(dectElement)) {
                        val = true;
                        break;
                    }
                }
            }
            final List<WfVariable> values = new ArrayList<WfVariable>();
            List<?> list = getParameterVariableValue(List.class, 1, null);
            for (int i = 0; i < list.size(); i++) {
                if (userTypeMap.equals(list.get(i))) {
                    values.add(new WfVariable(createChekBoxDefinition(i), val));
                    break;
                }
            }
            values.addAll(super.createValues(userTypeMap));
            return values;
        }
    }
}
