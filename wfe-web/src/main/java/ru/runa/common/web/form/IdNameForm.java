package ru.runa.common.web.form;

public class IdNameForm extends IdForm {
    private static final long serialVersionUID = 1L;
    public static final String NAME_INPUT_NAME = "name";

    private String action;
    private String name;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
