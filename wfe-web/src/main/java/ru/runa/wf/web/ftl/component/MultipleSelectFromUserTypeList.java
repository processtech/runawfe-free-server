package ru.runa.wf.web.ftl.component;

public class MultipleSelectFromUserTypeList extends AbstractSelectFromUserTypeList {

    private static final long serialVersionUID = 3883878151144701276L;

    @Override
    protected boolean isMultiple() {
        return true;
    }

}
