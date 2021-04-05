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
import ru.runa.wfe.audit.aggregated.TaskAggregatedLog;
import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDbSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.presentation.VariableDbSources;
import ru.runa.wfe.presentation.filter.TaskDurationFilterCriteria;
import ru.runa.wfe.presentation.filter.TaskStatusFilterCriteria;
import ru.runa.wfe.presentation.filter.UserOrGroupFilterCriteria;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.var.Variable;

/**
 * Created on 22.10.2005
 *
 */
public class ProcessWithTasksClassPresentation extends ClassPresentation {
    public static final String PROCESS_ID = "id";
    public static final String DEFINITION_NAME = "definitionName";
    public static final String PROCESS_START_DATE = "startDate";
    public static final String PROCESS_END_DATE = "endDate";
    public static final String DEFINITION_VERSION = "definitionVersion";
    public static final String PROCESS_EXECUTION_STATUS = "executionStatus";
    public static final String ERRORS = "errors";
    public static final String TASK_EXECUTOR = "taskExecutorName";
    public static final String TASK_SWIMLINE_NAME = "taskSwimlaneName";
    public static final String TASK_NAME = "taskName";
    public static final String TASK_DURATION_DATE = "taskDurationDate";
    public static final String TASK_DURATION = "taskDuration";
    public static final String TASK_CREATE_DATE = "taskCreateDate";
    public static final String TASK_ASSIGN_DATE = "taskAssignDate";
    public static final String TASK_DEADLINE_DATE = "taskDeadlineDate";
    public static final String PROCESS_VARIABLE = "variable";

    private static final ClassPresentation INSTANCE = new ProcessWithTasksClassPresentation();

    private static class ChildDbSource extends DefaultDbSource {

        public ChildDbSource(Class<?> sourceObject, String valueDBPath) {
            super(sourceObject, valueDBPath);
        }

        @Override
        public String getJoinExpression(String alias) {
            return alias + ".process.id";
        }
    }
    
    private static class TaskAggregatedLogDbSource extends DefaultDbSource {

        public TaskAggregatedLogDbSource(Class<?> sourceObject, String valueDBPath) {
            super(sourceObject, valueDBPath);
        }

        @Override
        public String getJoinExpression(String alias) {
            return alias + ".processId";
        }
    }

    private static class DeltaDataChildDbSource extends ChildDbSource {

        protected String secondDBPath;

        public DeltaDataChildDbSource(Class<?> sourceObject, String firstDBPath, String secondDBPath) {
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

    private static class NotNullChildDbSource extends ChildDbSource {

        protected String secondDBPath;

        public NotNullChildDbSource(Class<?> sourceObject, String firstDBPath, String secondDBPath) {
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
                new FieldDescriptor(PROCESS_ID, Integer.class.getName(), new DefaultDbSource(Process.class, "id"), true, FieldFilterMode.DATABASE,
                        "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.READ, "id" }),
                new FieldDescriptor(DEFINITION_NAME, String.class.getName(), new DefaultDbSource(Process.class, "deployment.name"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.READ, "name" }),
                new FieldDescriptor(PROCESS_START_DATE, Date.class.getName(), new DefaultDbSource(Process.class, "startDate"), true, 1,
                        BatchPresentationConsts.DESC, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.ProcessStartDateTdBuilder", new Object[] {}),
                new FieldDescriptor(PROCESS_END_DATE, Date.class.getName(), new DefaultDbSource(Process.class, "endDate"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.ProcessEndDateTdBuilder", new Object[] {}),
                new FieldDescriptor(DEFINITION_VERSION, Integer.class.getName(), new DefaultDbSource(Process.class, "deployment.version"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.READ, "version" }),
                new FieldDescriptor(PROCESS_EXECUTION_STATUS, String.class.getName(), new DefaultDbSource(Process.class, "executionStatus"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.ProcessExecutionStatusTdBuilder", new Object[] {}),
                new FieldDescriptor(ERRORS, String.class.getName(), new DefaultDbSource(Token.class, "errorMessage"), false, FieldFilterMode.NONE,
                        "ru.runa.wf.web.html.ProcessErrorsTdBuilder", new Object[] {}).setVisible(false),
                new FieldDescriptor(TASK_EXECUTOR, UserOrGroupFilterCriteria.class.getName(), new ChildDbSource(Task.class, "executor.name"), false,
                        FieldFilterMode.DATABASE_ID_RESTRICTION, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.READ,
                                "executor" }).setShowable(false),
                new FieldDescriptor(TASK_SWIMLINE_NAME, String.class.getName(), new ChildDbSource(Task.class, "swimlane.name"), false,
                        FieldFilterMode.DATABASE_ID_RESTRICTION, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.READ,
                                "swimlane" }).setShowable(false),
                new FieldDescriptor(TASK_NAME, TaskStatusFilterCriteria.class.getName(), new TaskAggregatedLogDbSource(TaskAggregatedLog.class, "taskName"), false,
                        FieldFilterMode.DATABASE_ID_RESTRICTION, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.READ,
                                "taskName" }).setShowable(false),
                new FieldDescriptor(TASK_DURATION_DATE, TaskDurationFilterCriteria.class.getName(), new DeltaDataChildDbSource(Task.class, "deadlineDate",
                        "createDate"), false, FieldFilterMode.DATABASE_ID_RESTRICTION, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] {
                        Permission.READ, "taskDuration" }).setShowable(false),
                new FieldDescriptor(TASK_DURATION, TaskDurationFilterCriteria.class.getName(), new DeltaDataChildDbSource(Task.class,
                        "$current_date", "createDate"), false, FieldFilterMode.DATABASE_ID_RESTRICTION, "ru.runa.common.web.html.PropertyTdBuilder",
                        new Object[] { Permission.READ, "currentTaskDuration" }).setShowable(false),
                new FieldDescriptor(TASK_CREATE_DATE, Date.class.getName(), new ChildDbSource(Task.class, "createDate"), false,
                        FieldFilterMode.DATABASE_ID_RESTRICTION, "ru.runa.wf.web.html.PropertyTdBuilder", new Object[] {}).setShowable(false),
                new FieldDescriptor(TASK_ASSIGN_DATE, Date.class.getName(), new NotNullChildDbSource(Task.class, "executor", "swimlane.createDate"),
                        false, FieldFilterMode.DATABASE_ID_RESTRICTION, "ru.runa.wf.web.html.PropertyTdBuilder", new Object[] {}).setShowable(false),
                new FieldDescriptor(TASK_DEADLINE_DATE, Date.class.getName(), new ChildDbSource(Task.class, "deadlineDate"), false,
                        FieldFilterMode.DATABASE_ID_RESTRICTION, "ru.runa.wf.web.html.PropertyTdBuilder", new Object[] {}).setShowable(false),
                new FieldDescriptor(PROCESS_ID, String.class.getName(), new SubProcessDbSource(
                        Process.class, "hierarchyIds"), true, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.RootProcessTdBuilder", new Object[] {}).setGroupableByProcessId(true),
                new FieldDescriptor(PROCESS_VARIABLE, Variable.class.getName(), VariableDbSources.get(null), true, FieldFilterMode.DATABASE,
                        "ru.runa.wf.web.html.ProcessVariableTdBuilder", new Object[] {}).setVariablePrototype(true) });
    }

    public static ClassPresentation getInstance() {
        return INSTANCE;
    }
}
