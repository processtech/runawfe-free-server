/*
 * This file is part of the RUNA WFE project.
 * Copyright (C) 2004-2006, Joint stock company "RUNA Technology"
 * All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package ru.runa.wfe.task;

import java.util.Date;

import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDBSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.presentation.SubstringDBSource;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.var.Variable;

/**
 * Created on 22.10.2005
 */
public class TaskClassPresentation extends ClassPresentation {
    public static final String NAME = "batch_presentation.task.name";
    public static final String DESCRIPTION = "batch_presentation.task.description";
    public static final String DEFINITION_NAME = "batch_presentation.task.definition_name";
    public static final String PROCESS_ID = "batch_presentation.task.process_id";
    public static final String OWNER = "batch_presentation.task.owner";
    public static final String TASK_SWIMLINE = "batch_presentation.task.swimlane";
    public static final String TASK_VARIABLE = editable_prefix + "name:batch_presentation.task.variable";
    public static final String TASK_DEADLINE = "batch_presentation.task.deadline";
    public static final String TASK_CREATE_DATE = "batch_presentation.task.create_date";
    public static final String TASK_ASSIGN_DATE = "batch_presentation.task.assing_date";
    public static final String TASK_DURATION = "batch_presentation.task.duration";

    private static final ClassPresentation INSTANCE = new TaskClassPresentation();

    private static class VariableDBSource extends DefaultDBSource {
        public VariableDBSource(Class<?> sourceObject) {
            super(sourceObject, "stringValue");
        }

        @Override
        public String getJoinExpression(String alias) {
            return classNameSQL + ".process=" + alias + ".process";
        }
    }

    private TaskClassPresentation() {
        super(Task.class, "", false, new FieldDescriptor[] {
                // display name field type DB source isSort filter mode
                // get value/show in web getter parameters
                new FieldDescriptor(NAME, String.class.getName(), new DefaultDBSource(Task.class, "name"), true, 3, BatchPresentationConsts.ASC,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTDBuilder", new Object[] { new Permission(), "name" }),
                new FieldDescriptor(DESCRIPTION, String.class.getName(), new SubstringDBSource(Task.class, "description"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.TaskDescriptionTDBuilder", new Object[] {}),
                new FieldDescriptor(DEFINITION_NAME, String.class.getName(), new DefaultDBSource(Task.class, "process.deployment.name"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.TaskProcessDefinitionTDBuilder", new Object[] {}),
                new FieldDescriptor(PROCESS_ID, Integer.class.getName(), new DefaultDBSource(Task.class, "process.id"), true, 2,
                        BatchPresentationConsts.ASC, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.TaskProcessIdTDBuilder", new Object[] {}),
                new FieldDescriptor(OWNER, String.class.getName(), new DefaultDBSource(Task.class, "executor.name"), true, FieldFilterMode.DATABASE,
                        "ru.runa.wf.web.html.TaskOwnerTDBuilder", new Object[] {}),
                new FieldDescriptor(TASK_SWIMLINE, String.class.getName(), new DefaultDBSource(Task.class, "swimlane.name"), false,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.TaskRoleTDBuilder", new Object[] {}),
                new FieldDescriptor(TASK_VARIABLE, String.class.getName(), new VariableDBSource(Variable.class), true, FieldFilterMode.DATABASE,
                        "ru.runa.wf.web.html.TaskVariableTDBuilder", new Object[] {}, true),
                new FieldDescriptor(TASK_DEADLINE, Date.class.getName(), new DefaultDBSource(Task.class, "deadlineDate"), true, 1,
                        BatchPresentationConsts.DESC, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.TaskDeadlineTDBuilder", new Object[] {}),
                new FieldDescriptor(TASK_CREATE_DATE, Date.class.getName(), new DefaultDBSource(Task.class, "createDate"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.TaskCreationDateTDBuilder", new Object[] {}),
                new FieldDescriptor(TASK_ASSIGN_DATE, Date.class.getName(), new DefaultDBSource(Task.class, "createDate"), false,
                        FieldFilterMode.NONE, "ru.runa.wf.web.html.TaskAssignmentDateTDBuilder", new Object[] {}),
                new FieldDescriptor(TASK_DURATION, String.class.getName(), new DefaultDBSource(Task.class, "name"), false, FieldFilterMode.NONE,
                        "ru.runa.wf.web.html.TaskDurationTDBuilder", new Object[] {}) });
    }

    public static final ClassPresentation getInstance() {
        return INSTANCE;
    }
}
