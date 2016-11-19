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
package ru.runa.wfe.job;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.audit.CreateTimerActionLog;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.job.dao.JobDAO;
import ru.runa.wfe.lang.Action;

import com.google.common.base.Objects;

public class CreateTimerAction extends Action {
    private static final long serialVersionUID = 1L;

    private String dueDate;
    private String transitionName;
    private String repeatDurationString;

    @Autowired
    private transient JobDAO jobDAO;

    @Override
    public void execute(ExecutionContext executionContext) {
        Timer timer = new Timer(executionContext.getToken());
        timer.setName(getName());
        timer.setDueDateExpression(dueDate);
        timer.setDueDate(ExpressionEvaluator.evaluateDueDate(executionContext.getVariableProvider(), dueDate));
        timer.setRepeatDurationString(repeatDurationString);
        timer.setOutTransitionName(transitionName);
        jobDAO.create(timer);
        log.debug("Created " + timer + " for duration '" + dueDate + "'");
        executionContext.addLog(new CreateTimerActionLog(this, timer.getDueDate()));
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDateDuration) {
        dueDate = dueDateDuration;
    }

    public void setRepeatDurationString(String repeatDurationString) {
        this.repeatDurationString = repeatDurationString;
    }

    public String getTransitionName() {
        return transitionName;
    }

    public void setTransitionName(String transitionName) {
        this.transitionName = transitionName;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("event", getEvent()).add("dueDate", dueDate).toString();
    }
}
