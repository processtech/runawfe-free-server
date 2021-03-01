package ru.runa.wf.web.ftl.component;

public class SingleSelectFromUserTypeList extends AbstractSelectFromUserTypeList {
    private static final long serialVersionUID = 1L;

    @Override
    protected boolean isMultiple() {
        return false;
    }
}
