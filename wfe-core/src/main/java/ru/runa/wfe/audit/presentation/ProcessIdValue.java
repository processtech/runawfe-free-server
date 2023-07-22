package ru.runa.wfe.audit.presentation;

import java.io.Serializable;

public class ProcessIdValue implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;

    public ProcessIdValue() {
    }

    public ProcessIdValue(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

}
