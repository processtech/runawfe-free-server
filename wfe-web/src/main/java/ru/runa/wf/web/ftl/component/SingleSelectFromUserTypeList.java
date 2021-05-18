package ru.runa.wf.web.ftl.component;

public class SingleSelectFromUserTypeList extends AbstractSelectFromUserTypeList {

    private static final long serialVersionUID = 7198988804348108048L;

    @Override
    protected boolean isMultiple() {
        return false;
    }
}
