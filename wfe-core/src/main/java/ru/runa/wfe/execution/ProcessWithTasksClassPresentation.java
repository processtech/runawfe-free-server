/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.execution;

import java.util.Date;

import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDBSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.presentation.VariableDBSources;
import ru.runa.wfe.presentation.filter.TaskDurationFilterCriteria;
import ru.runa.wfe.presentation.filter.UserOrGroupFilterCriteria;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.var.Variable;

/**
 * Created on 22.10.2005
 * 
 */
public class ProcessWithTasksClassPresentation extends ClassPresentation {
    public static final String PROCESS_ID = "batch_presentation.process.id";
    public static final String DEFINITION_NAME = "batch_presentation.process.definition_name";
    public static final String PROCESS_START_DATE = "batch_presentation.process.started";
    public static final String PROCESS_END_DATE = "batch_presentation.process.ended";
    public static final String DEFINITION_VERSION = "batch_presentation.process.definition_version";
    public static final String PROCESS_EXECUTION_STATUS = "batch_presentation.process.execution_status";
    public static final String TASK_EXECUTOR = "batch_presentation.process.task.executor.name";
    public static final String TASK_SWIMLINE = "batch_presentation.task.swimlane";
    public static final String TASK_NAME = "batch_presentation.process.task.name";
    public static final String TASK_DURATION = "batch_presentation.process.task.duration";
    public static final String TASK_DURATION_CURRENT = "batch_presentation.process.task.duration_current";
    public static final String TASK_CREATE_DATE = "batch_presentation.process.task.create_date";
    public static final String TASK_TAKE_DATE = "batch_presentation.process.task.take_date";
    public static final String TASK_DEADLINE = "batch_presentation.process.task.deadline";
    public static final String PROCESS_VARIABLE = editable_prefix + "name:batch_presentation.process.variable";

    private static final ClassPresentation INSTANCE = new ProcessWithTasksClassPresentation();

    private static class ChildDBSource extends DefaultDBSource {

        public ChildDBSource(Class<?> sourceObject, String valueDBPath) {
            super(sourceObject, valueDBPath);
        }

        @Override
        public String getJoinExpression(String alias) {
            return alias + ".process.id";
        }
    }

    private static class DeltaDataChildDBSource extends ChildDBSource {

        protected String secondDBPath;

        public DeltaDataChildDBSource(Class<?> sourceObject, String firstDBPath, String secondDBPath) {
            super(sourceObject, firstDBPath);
            this.secondDBPath = secondDBPath;
        }

        @Override
        public String getValueDBPath(AccessType accessType, String parAlias) {
            final String alias = parAlias == null ? "" : parAlias + ".";
            final String first = makeDbPath(alias, valueDBPath);
            final String second = makeDbPath(alias, secondDBPath);
            return first + " - " + second;
        }

        private String makeDbPath(String alias, String dbPath) {
            if ('$' == dbPath.charAt(0)) {
                return dbPath.substring(1);
            } else {
                return alias + dbPath;
            }
        }
    }

    private static class NotNullChildDBSource extends ChildDBSource {

        protected String secondDBPath;

        public NotNullChildDBSource(Class<?> sourceObject, String firstDBPath, String secondDBPath) {
            super(sourceObject, firstDBPath);
            this.secondDBPath = secondDBPath;
        }

        @Override
        public String getValueDBPath(AccessType accessType, String parAlias) {
            final String alias = parAlias == null ? "" : parAlias + ".";
            final String first = alias + valueDBPath;
            final String second = alias + secondDBPath;
            return "case when " + first + " is null then null else " + second + " end ";
        }
    }

    private ProcessWithTasksClassPresentation() {
        super(Process.class, "", true, new FieldDescriptor[] {
                new FieldDescriptor(PROCESS_ID, Integer.class.getName(), new DefaultDBSource(Process.class, "id"), true, FieldFilterMode.DATABASE,
                        "ru.runa.common.web.html.PropertyTDBuilder", new Object[] { new Permission(), "id" }),
                new FieldDescriptor(DEFINITION_NAME, String.class.getName(), new DefaultDBSource(Process.class, "deployment.name"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTDBuilder", new Object[] { new Permission(), "name" }),
                new FieldDescriptor(PROCESS_START_DATE, Date.class.getName(), new DefaultDBSource(Process.class, "startDate"), true, 1,
                        BatchPresentationConsts.DESC, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.ProcessStartDateTDBuilder", new Object[] {}),
                new FieldDescriptor(PROCESS_END_DATE, Date.class.getName(), new DefaultDBSource(Process.class, "endDate"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.ProcessEndDateTDBuilder", new Object[] {}),
                new FieldDescriptor(DEFINITION_VERSION, Integer.class.getName(), new DefaultDBSource(Process.class, "deployment.version"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTDBuilder", new Object[] { new Permission(), "version" }),
                new FieldDescriptor(PROCESS_EXECUTION_STATUS, String.class.getName(), new DefaultDBSource(Process.class, "executionStatus"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.ProcessExecutionStatusTDBuilder", new Object[] {}),
                new FieldDescriptor(TASK_EXECUTOR, UserOrGroupFilterCriteria.class.getName(), new ChildDBSource(Task.class, "executor.name"), false,
                        FieldFilterMode.DATABASE_ID_RESTRICTION, "ru.runa.common.web.html.PropertyTDBuilder", new Object[] { new Permission(),
                                "executor" }).setShowable(false),
                new FieldDescriptor(TASK_SWIMLINE, String.class.getName(), new ChildDBSource(Task.class, "swimlane.name"), false,
                        FieldFilterMode.DATABASE_ID_RESTRICTION, "ru.runa.common.web.html.PropertyTDBuilder", new Object[] { new Permission(),
                                "swimlane" }).setShowable(false),
                new FieldDescriptor(TASK_NAME, String.class.getName(), new ChildDBSource(Task.class, "name"), false,
                        FieldFilterMode.DATABASE_ID_RESTRICTION, "ru.runa.common.web.html.PropertyTDBuilder", new Object[] { new Permission(),
                                "taskName" }).setShowable(false),
                new FieldDescriptor(TASK_DURATION, TaskDurationFilterCriteria.class.getName(), new DeltaDataChildDBSource(Task.class, "deadlineDate",
                        "createDate"), false, FieldFilterMode.DATABASE_ID_RESTRICTION, "ru.runa.common.web.html.PropertyTDBuilder", new Object[] {
                        new Permission(), "taskDuration" }).setShowable(false),
                new FieldDescriptor(TASK_DURATION_CURRENT, TaskDurationFilterCriteria.class.getName(), new DeltaDataChildDBSource(Task.class,
                        "$current_date", "createDate"), false, FieldFilterMode.DATABASE_ID_RESTRICTION, "ru.runa.common.web.html.PropertyTDBuilder",
                        new Object[] { new Permission(), "currentTaskDuration" }).setShowable(false),
                new FieldDescriptor(TASK_CREATE_DATE, Date.class.getName(), new ChildDBSource(Task.class, "createDate"), false,
                        FieldFilterMode.DATABASE_ID_RESTRICTION, "ru.runa.wf.web.html.PropertyTDBuilder", new Object[] {}).setShowable(false),
                new FieldDescriptor(TASK_TAKE_DATE, Date.class.getName(), new NotNullChildDBSource(Task.class, "executor", "swimlane.createDate"),
                        false, FieldFilterMode.DATABASE_ID_RESTRICTION, "ru.runa.wf.web.html.PropertyTDBuilder", new Object[] {}).setShowable(false),
                new FieldDescriptor(TASK_DEADLINE, Date.class.getName(), new ChildDBSource(Task.class, "deadlineDate"), false,
                        FieldFilterMode.DATABASE_ID_RESTRICTION, "ru.runa.wf.web.html.PropertyTDBuilder", new Object[] {}).setShowable(false),
                new FieldDescriptor(filterable_prefix + "batch_presentation.process.id", String.class.getName(), new SubProcessDBSource(
                        Process.class, "hierarchyIds"), true, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.RootProcessTDBuilder", new Object[] {},
                        true),
                new FieldDescriptor(PROCESS_VARIABLE, Variable.class.getName(), VariableDBSources.get(null), true, FieldFilterMode.DATABASE,
                        "ru.runa.wf.web.html.ProcessVariableTDBuilder", new Object[] {}, true) });
    }

    public static final ClassPresentation getInstance() {
        return INSTANCE;
    }
}
