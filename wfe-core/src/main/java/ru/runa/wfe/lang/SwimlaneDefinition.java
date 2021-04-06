package ru.runa.wfe.lang;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ru.runa.wfe.execution.logic.BotSwimlaneInitializer;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.format.ExecutorFormat;

/**
 * is a process role (aka participant).
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SwimlaneDefinition extends GraphElement {
    private static final long serialVersionUID = 1L;

    private Delegation delegation;
    private String orgFunctionLabel;
    private List<String> flowNodeIds;
    private String scriptingName;
    private boolean global;

    public Delegation getDelegation() {
        return delegation;
    }

    public void setDelegation(Delegation delegation) {
        this.delegation = delegation;
    }

    public String getOrgFunctionLabel() {
        return orgFunctionLabel;
    }

    public void setOrgFunctionLabel(String displayOrgFunction) {
        orgFunctionLabel = null == displayOrgFunction ? null : displayOrgFunction.intern();
    }

    public List<String> getFlowNodeIds() {
        return flowNodeIds;
    }

    public void setFlowNodeIds(List<String> flowNodeIds) {
        this.flowNodeIds = flowNodeIds;
    }

    public String getScriptingName() {
        return scriptingName;
    }

    public void setScriptingName(String scriptingName) {
        this.scriptingName = null == scriptingName ? null : scriptingName.intern();
    }

    public VariableDefinition toVariableDefinition() {
        return new VariableDefinition(name, scriptingName, ExecutorFormat.class.getName(), null);
    }

    public boolean isBotExecutor() {
        if (delegation == null) {
            return false;
        }
        return BotSwimlaneInitializer.isValid(delegation.getConfiguration());
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

}
