/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
}
