package ru.runa.wfe.extension.handler.var;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;


/**
 * <config>
 *   <list name="Входной_Список"/>
 *   <operation attribute="Атрибут_1" function="Функция_1" result="Объект_1"/>
 *   <operation attribute="Атрибут_2" function="Функция_2" result="Объект_2"/>
 *  </config>
 */
public class UserTypeListAggregateFunctionConfig extends Observable {
    private String listName;
    private List<Operation> operations;

    public UserTypeListAggregateFunctionConfig(String listName, List<Operation> operations) {
        this.listName = listName;
        this.operations = operations != null ? operations : new ArrayList<>();
    }

    public static UserTypeListAggregateFunctionConfig fromXml(String xml) {
        Document document = XmlUtils.parseWithoutValidation(xml);
        Element rootElement = document.getRootElement();
        Element listElement = rootElement.element("list");
        String listName = listElement.attributeValue("name");
        List<Element> operationElements = rootElement.elements("operation");
        List<Operation> operations = new ArrayList<>();
        for (Element operationElement : operationElements) {
            String attribute = operationElement.attributeValue("attribute");
            String function = operationElement.attributeValue("function");
            String result = operationElement.attributeValue("result");
            operations.add(new Operation(attribute, function, result));
        }
        return new UserTypeListAggregateFunctionConfig(listName, operations);
    }

    public String getListName() {
        return listName;
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public void setListName(String listName) {
        this.listName = listName;
        setChanged();
        notifyObservers();
    }

    public void addOperation(Operation operation) {
        operations.add(operation);
        setChanged();
        notifyObservers();
    }

    public void removeOperation(int index) {
        operations.remove(index);
        setChanged();
        notifyObservers();
    }

    public void updateOperation(int index, Operation operation) {
        operations.set(index, operation);
        setChanged();
        notifyObservers();
    }

    @Override
    public String toString() {
        Document document = XmlUtils.createDocument("config");
        Element rootElement = document.getRootElement();
        Element listElement = rootElement.addElement("list");
        listElement.addAttribute("name", listName);
        for (Operation operation : operations) {
            Element operationElement = rootElement.addElement("operation");
            operationElement.addAttribute("attribute", operation.getAttribute());
            operationElement.addAttribute("function", operation.getFunction());
            operationElement.addAttribute("result", operation.getResult());
        }
        return XmlUtils.toString(document);
    }

    public static class Operation {
        private final String attribute;
        private final String function;
        private final String result;

        public Operation(String attribute, String function, String result) {
            this.attribute = attribute;
            this.function = function;
            this.result = result;
        }

        public String getAttribute() {
            return attribute;
        }

        public String getFunction() {
            return function;
        }

        public String getResult() {
            return result;
        }
    }
}
