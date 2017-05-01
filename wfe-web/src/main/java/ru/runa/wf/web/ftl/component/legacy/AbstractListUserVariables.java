package ru.runa.wf.web.ftl.component.legacy;

import java.util.List;

import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.var.UserTypeMap;

abstract class AbstractListUserVariables extends FormComponent {
    private static final long serialVersionUID = 1L;

    protected List<UserTypeMap> list;
    protected String variableName;
    protected String dectVariableName;
    protected DisplayMode displayMode;
    protected String sortField;

    protected void initFields() {
        if (getClass().equals(DisplayListUserVariables.class)) {
            variableName = getParameterAsString(0);
            displayMode = DisplayMode.fromString(getParameterAsString(1));
            sortField = getParameterAsString(2);
        } else if (getClass().equals(MultipleSelectFromListUserVariables.class)) {
            variableName = getParameterAsString(1);
            list = variableProvider.getValue(List.class, variableName);
            dectVariableName = getParameterAsString(0);
            displayMode = DisplayMode.fromString(getParameterAsString(2));
            sortField = getParameterAsString(3);
        }
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
