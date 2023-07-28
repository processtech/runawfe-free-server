package ru.runa.wf.web.form;

import org.apache.struts.action.ActionForm;

public class SwimlaneForm extends ActionForm {
    private static final long serialVersionUID = -7959952772049496506L;

    public static final String SWIMLANE_NAME_INPUT_NAME = "swimlaneName";

    private Long swimlaneName;

    public Long getSwimlaneName() {
        return swimlaneName;
    }

    public void setSwimlaneName(Long swimlaneName) {
        this.swimlaneName = swimlaneName;
    }
}
