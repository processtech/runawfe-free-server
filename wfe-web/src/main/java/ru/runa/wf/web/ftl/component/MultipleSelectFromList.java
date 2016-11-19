package ru.runa.wf.web.ftl.component;

public class MultipleSelectFromList extends AbstractSelectFromList {
    private static final long serialVersionUID = 1L;

    @Override
    protected boolean isMultiple() {
        return true;
    }

}
