package ru.runa.wf.web.ftl.component;

import java.util.List;

import com.google.common.collect.Lists;

import ru.runa.wfe.commons.ftl.FormComponent;

abstract class AbstractListUserVariables extends FormComponent {
    private static final long serialVersionUID = 1L;

    protected String variableName;
    protected String dectVariableName;
    protected DisplayMode displayMode;
    protected String sortField;
    protected List<String> displayFields;

    protected void initFields() {
        if (getClass().equals(DisplayUserTypeList.class)) {
            variableName = getParameterAsString(0);
            displayMode = DisplayMode.fromString(getParameterAsString(1));
            sortField = getParameterAsString(2);
            displayFields = getOptions(3);
        } else if (getClass().equals(MultipleSelectFromUserTypeList.class)) {
            variableName = getParameterAsString(1);
            dectVariableName = getParameterAsString(0);
            displayMode = DisplayMode.fromString(getParameterAsString(2));
            sortField = getParameterAsString(3);
            displayFields = getOptions(4);
        }
    }
    
    protected List<String> getOptions(int numParam) {
        List<String> list = Lists.newArrayList();
        int i = numParam;
        while (true) {
            String option = getParameterAsString(i);
            if (option == null) {
                break;
            }
            list.add(option);
            i++;
        }
        return list;
    }

    @Override
    abstract protected Object renderRequest() throws Exception;

    public enum DisplayMode {
        TWO_DIMENTIONAL_TABLE("two-dimentional"), MULTI_DIMENTIONAL_TABLE("multi-dimentional");

        private final String mode;

        private DisplayMode(String s) {
            mode = s;
        }

        public static final DisplayMode fromString(String md) {
            for (DisplayMode dm : DisplayMode.values()) {
                if (!dm.mode.equals(md)) {
                    continue;
                }
                return dm;
            }
            return null;
        }
    }

}
