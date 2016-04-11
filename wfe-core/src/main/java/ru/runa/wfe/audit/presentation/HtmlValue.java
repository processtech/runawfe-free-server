package ru.runa.wfe.audit.presentation;

import java.io.Serializable;

public class HtmlValue implements Serializable {
    private static final long serialVersionUID = 1L;
    private String string;

    public HtmlValue() {
    }

    public HtmlValue(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }

}
