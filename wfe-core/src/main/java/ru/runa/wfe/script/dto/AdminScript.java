package ru.runa.wfe.script.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ru.runa.wfe.script.AdmScript;

@XmlAccessorType(XmlAccessType.FIELD)
public class AdminScript implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;

    public AdminScript() {
    }

    public AdminScript(AdmScript script) {
        this.id = script.getId();
        this.name = script.getName();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
