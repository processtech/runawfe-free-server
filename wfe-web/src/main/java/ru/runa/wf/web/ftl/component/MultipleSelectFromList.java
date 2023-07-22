package ru.runa.wf.web.ftl.component;

public class MultipleSelectFromList extends AbstractCheckboxSelectFromVariableList {
    private static final long serialVersionUID = 1L;

    @Override
    protected boolean isMultiple() {
        return true;
    }

}
