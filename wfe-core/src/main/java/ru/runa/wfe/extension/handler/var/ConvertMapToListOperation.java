package ru.runa.wfe.extension.handler.var;

import java.util.Observable;

import org.dom4j.Element;

public class ConvertMapToListOperation extends Observable {

    private String mapVariableName = "";
    private String listVariableName = "";

    public String getMapVariableName() {
        return mapVariableName;
    }

    public void setMapVariableName(String mapVariableName) {
        this.mapVariableName = mapVariableName;
    }

    public String getListVariableName() {
        return listVariableName;
    }

    public void setListVariableName(String listVariableName) {
        this.listVariableName = listVariableName;
    }

    public void serialize(Element parent) {
        Element element = parent.addElement("operation");
        element.addAttribute("map", mapVariableName);
        element.addAttribute("list", listVariableName);
    }

    public static ConvertMapToListOperation deserialize(Element element) {
        ConvertMapToListOperation model = new ConvertMapToListOperation();
        model.mapVariableName = element.attributeValue("map");
        model.listVariableName = element.attributeValue("list");
        return model;
    }

}
