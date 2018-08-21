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
package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.task.Task;

/**
 * Logging task creation.
 *
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "1")
public class CurrentTaskCreateLog extends CurrentTaskLog implements TaskCreateLog {
    private static final long serialVersionUID = 1L;

    public CurrentTaskCreateLog() {
    }

    public CurrentTaskCreateLog(Task task) {
        super(task);
        if (task.getDeadlineDate() != null) {
            addAttribute(ATTR_DUE_DATE, CalendarUtil.formatDateTime(task.getDeadlineDate()));
        }
        setSeverity(Severity.INFO);
    }
}
