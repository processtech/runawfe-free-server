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
package ru.runa.wfe.lang.jpdl;

import ru.runa.wfe.audit.ActionLog;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandler;
import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.lang.GraphElement;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

public class Action extends GraphElement {
    private static final long serialVersionUID = 1L;
    private Delegation delegation;
    private ActionEvent actionEvent;
    private GraphElement parent;

    @Override
    public GraphElement getParent() {
        return parent;
    }

    public void setParent(GraphElement parent) {
        this.parent = parent;
    }

    @Override
    public void validate() {
        super.validate();
        Preconditions.checkNotNull(delegation, "delegation in " + this);
        Preconditions.checkNotNull(actionEvent, "event in " + this);
        Preconditions.checkNotNull(parent, "parent in " + this);
    }

    public void execute(ExecutionContext executionContext) {
        try {
            ActionHandler actionHandler = delegation.getInstance();
            log.info("Executing " + this);
            actionHandler.execute(executionContext);
            executionContext.addLog(new ActionLog(this));
        } catch (Exception e) {
            log.error("Failed " + this);
            throw Throwables.propagate(e);
        }
    }

    public ActionEvent getEvent() {
        return actionEvent;
    }

    public void setEvent(ActionEvent actionEvent) {
        this.actionEvent = actionEvent;
    }

    public Delegation getDelegation() {
        return delegation;
    }

    public void setDelegation(Delegation instantiatableDelegate) {
        delegation = instantiatableDelegate;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("event", actionEvent).add("delegation", delegation).toString();
    }

}
