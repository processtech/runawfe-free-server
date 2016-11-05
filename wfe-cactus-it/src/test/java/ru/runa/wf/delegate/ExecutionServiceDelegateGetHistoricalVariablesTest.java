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
package ru.runa.wf.delegate;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.cactus.ServletTestCase;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import junit.framework.Assert;
import ru.runa.junit.ArrayAssert;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Tests for getting historical state of process variables.
 **/
public class ExecutionServiceDelegateGetHistoricalVariablesTest extends ServletTestCase {
    private final int defaultDelayMs = 100;

    private ExecutionService executionService;

    private WfServiceTestHelper th = null;

    private Long processId;

    @Override
    protected void setUp() throws Exception {
        th = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();
        th.deployValidProcessDefinition(WfServiceTestHelper.LONG_WITH_VARIABLES_PROCESS_FILE_NAME);
        Collection<Permission> permissions = Lists.newArrayList(DefinitionPermission.START_PROCESS, DefinitionPermission.READ_STARTED_PROCESS);
        th.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.LONG_WITH_VARIABLES_PROCESS_NAME);
        for (Stage stage : Stage.values()) {
            stage.DoStageAction(this);
        }
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        th.undeployValidProcessDefinition(WfServiceTestHelper.LONG_WITH_VARIABLES_PROCESS_NAME);
        th.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void testGetVariablesOnTime() throws Exception {
        ProcessLogFilter filter = new ProcessLogFilter(processId);
        for (Stage stage : Stage.values()) {
            filter.setCreateDateTo(stage.stageTime);
            List<WfVariable> variables = executionService.getHistoricalVariables(th.getAuthorizedPerformerUser(), filter);
            Map<String, Object> expected = Maps.newHashMap();
            expected.putAll(stage.afterExecuteVariables);
            for (WfVariable wfVariable : variables) {
                String name = wfVariable.getDefinition().getName();
                if (wfVariable.getValue() instanceof List) {
                    ArrayAssert.assertEqualArrays((List) expected.get(name), (List) wfVariable.getValue());
                } else {
                    Assert.assertEquals(name + " value is not equals on " + stage, expected.get(name), wfVariable.getValue());
                }
                expected.remove(name);
            }
            for (String notFoundVariable : expected.keySet()) {
                Assert.assertNull(notFoundVariable + " is not null, but not found in variables on " + stage, expected.get(notFoundVariable));
            }
        }
    }

    public void testGetVariablesChangedOnRange() throws Exception {
        ProcessLogFilter filter = new ProcessLogFilter(processId);
        for (Stage stage : Stage.values()) {
            if (stage.prevStage == null) {
                continue;
            }
            filter.setCreateDateFrom(stage.prevStage.stageTime);
            filter.setCreateDateTo(stage.stageTime);
            List<WfVariable> variables = executionService.getHistoricalVariables(th.getAuthorizedPerformerUser(), filter);
            Map<String, Object> expected = Maps.newHashMap();
            expected.putAll(stage.changedVariables);
            for (WfVariable wfVariable : variables) {
                String name = wfVariable.getDefinition().getName();
                if (wfVariable.getValue() instanceof List) {
                    ArrayAssert.assertEqualArrays((List) expected.get(name), (List) wfVariable.getValue());
                } else {
                    Assert.assertEquals(name + " value is not equals on " + stage, expected.get(name), wfVariable.getValue());
                }
                expected.remove(name);
            }
            for (String notFoundVariable : expected.keySet()) {
                Assert.assertNull(notFoundVariable + " is not null, but not found in variables on " + stage, expected.get(notFoundVariable));
            }
        }
    }

    public void testGetVariablesOnTimeWithFilter() throws Exception {
        Set<String> variableNames = Sets.newHashSet();
        variableNames.add("varLong");
        variableNames.add("varString");
        variableNames.add("varUT");
        variableNames.add("varTemp");
        ProcessLogFilter filter = new ProcessLogFilter(processId);
        for (Stage stage : Stage.values()) {
            filter.setCreateDateTo(stage.stageTime);
            List<WfVariable> variables = executionService.getHistoricalVariables(th.getAuthorizedPerformerUser(), filter, variableNames);
            Map<String, Object> expected = Maps.newHashMap();
            expected.putAll(stage.afterExecuteVariables);
            for (String name : Sets.newHashSet(expected.keySet())) {
                if (!variableNames.contains(name)) {
                    expected.remove(name);
                }
            }
            for (WfVariable wfVariable : variables) {
                String name = wfVariable.getDefinition().getName();
                if (wfVariable.getValue() instanceof List) {
                    ArrayAssert.assertEqualArrays(name + " value is not equals on " + stage, (List) expected.get(name), (List) wfVariable.getValue());
                } else {
                    Assert.assertEquals(name + " value is not equals on " + stage, expected.get(name), wfVariable.getValue());
                }
                expected.remove(name);
            }
            for (String notFoundVariable : expected.keySet()) {
                Assert.assertNull(notFoundVariable + " is not null, but not found in variables on " + stage, expected.get(notFoundVariable));
            }
        }
    }

    private enum Stage {
        NOT_STARTED(null) {
            @Override
            protected void DoStageActionInternal(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance) throws Exception {
            }
        },

        STARTED(NOT_STARTED) {
            @Override
            protected void DoStageActionInternal(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance) throws Exception {
                User user = testInstance.th.getAuthorizedPerformerUser();
                String processName = WfServiceTestHelper.LONG_WITH_VARIABLES_PROCESS_NAME;
                changedVariables.put("varLong", 1L);
                testInstance.processId = testInstance.executionService.startProcess(user, processName, changedVariables);
            }
        },

        TASK1_COMPLETED(STARTED) {

            @Override
            protected void DoStageActionInternal(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance) throws Exception {
                WfServiceTestHelper th2 = testInstance.th;
                User user = th2.getAuthorizedPerformerUser();
                WfTask taskStub = th2.getTaskService().getMyTasks(user, th2.getTaskBatchPresentation()).get(0);
                changedVariables.put("varLong", 2L);
                changedVariables.put("varListString", Lists.newArrayList("str1", "str2"));
                changedVariables.put("varString", "123");
                HashMap<String, UserTypeMap> map = Maps.newHashMap();
                map.put("11", createUserType(testInstance, 1L, null, Lists.newArrayList("123", "@34"), null));
                changedVariables.put("varMapStringUT", map);
                testInstance.th.getTaskService().completeTask(user, taskStub.getId(), changedVariables, null);
            }
        },

        TASK2_COMPLETED(TASK1_COMPLETED) {

            @Override
            protected void DoStageActionInternal(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance) throws Exception {
                WfServiceTestHelper th2 = testInstance.th;
                User user = th2.getAuthorizedPerformerUser();
                WfTask taskStub = th2.getTaskService().getMyTasks(user, th2.getTaskBatchPresentation()).get(0);
                changedVariables.put("varLong", 3L);
                changedVariables.put("varListString", Lists.newArrayList("str1", "str2", "str3"));
                changedVariables.put("varString", "1243");
                changedVariables.put("varListUT", Lists.newArrayList(createUserType(testInstance, 3L, "ss", null, null), createUserType(testInstance,
                        6L, "ss", null, null)));
                testInstance.th.getTaskService().completeTask(user, taskStub.getId(), changedVariables, null);
            }
        },

        TASK3_COMPLETED(TASK2_COMPLETED) {

            @Override
            protected void DoStageActionInternal(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance) throws Exception {
                WfServiceTestHelper th2 = testInstance.th;
                User user = th2.getAuthorizedPerformerUser();
                WfTask taskStub = th2.getTaskService().getMyTasks(user, th2.getTaskBatchPresentation()).get(0);
                changedVariables.put("varLong", 4L);
                changedVariables.put("varListString", null);
                changedVariables.put("varUT", createUserType(testInstance, 3L, "ss", null, null));
                testInstance.th.getTaskService().completeTask(user, taskStub.getId(), changedVariables, null);
            }
        },

        TASK4_COMPLETED(TASK3_COMPLETED) {

            @Override
            protected void DoStageActionInternal(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance) throws Exception {
                WfServiceTestHelper th2 = testInstance.th;
                User user = th2.getAuthorizedPerformerUser();
                WfTask taskStub = th2.getTaskService().getMyTasks(user, th2.getTaskBatchPresentation()).get(0);
                changedVariables.put("varLong", 5L);
                changedVariables.put("varListString", Lists.newArrayList("str4", "str5"));
                changedVariables.put("varUT", createUserType(testInstance, null, "ss", Lists.newArrayList("s1", "s2"), null));
                testInstance.th.getTaskService().completeTask(user, taskStub.getId(), changedVariables, null);
            }
        },

        TASK5_COMPLETED(TASK4_COMPLETED) {

            @Override
            protected void DoStageActionInternal(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance) throws Exception {
                WfServiceTestHelper th2 = testInstance.th;
                User user = th2.getAuthorizedPerformerUser();
                WfTask taskStub = th2.getTaskService().getMyTasks(user, th2.getTaskBatchPresentation()).get(0);
                changedVariables.put("varLong", 6L);
                changedVariables.put("varString", null);
                HashMap<String, String> map = Maps.newHashMap();
                map.put("1", "2");
                changedVariables.put("varUT", createUserType(testInstance, null, null, Lists.newArrayList("s1"), map));
                changedVariables.put("varListUT", Lists.newArrayList(createUserType(testInstance, 5L, "ss", Lists.newArrayList("s2"), null),
                        createUserType(testInstance, 6L, "ss", Lists.newArrayList("s4"), null)));
                changedVariables.put("varMapStringUT", null);
                testInstance.th.getTaskService().completeTask(user, taskStub.getId(), changedVariables, null);
            }
        },

        TASK6_COMPLETED(TASK5_COMPLETED) {

            @Override
            protected void DoStageActionInternal(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance) throws Exception {
                WfServiceTestHelper th2 = testInstance.th;
                User user = th2.getAuthorizedPerformerUser();
                WfTask taskStub = th2.getTaskService().getMyTasks(user, th2.getTaskBatchPresentation()).get(0);
                changedVariables.put("varLong", 7L);
                changedVariables.put("varListString", null);
                HashMap<String, String> map = Maps.newHashMap();
                map.put("1", "2");
                map.put("4", "5");
                changedVariables.put("varUT", createUserType(testInstance, 13L, "tt", null, map));
                changedVariables.put("varListUT", Lists.newArrayList(createUserType(testInstance, 5L, "ss", Lists.newArrayList("s2"), map),
                        createUserType(testInstance, 6L, "ss", Lists.newArrayList("s4"), map)));
                testInstance.th.getTaskService().completeTask(user, taskStub.getId(), changedVariables, null);
            }
        },

        TASK7_COMPLETED(TASK6_COMPLETED) {

            @Override
            protected void DoStageActionInternal(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance) throws Exception {
                WfServiceTestHelper th2 = testInstance.th;
                User user = th2.getAuthorizedPerformerUser();
                WfTask taskStub = th2.getTaskService().getMyTasks(user, th2.getTaskBatchPresentation()).get(0);
                changedVariables.put("varLong", 8L);
                changedVariables.put("varListUT", null);
                HashMap<String, UserTypeMap> map = Maps.newHashMap();
                map.put("11", createUserType(testInstance, 1L, null, Lists.newArrayList("123", "4@34"), null));
                changedVariables.put("varMapStringUT", map);
                testInstance.th.getTaskService().completeTask(user, taskStub.getId(), changedVariables, null);
            }
        },

        TASK8_COMPLETED(TASK7_COMPLETED) {

            @Override
            protected void DoStageActionInternal(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance) throws Exception {
                WfServiceTestHelper th2 = testInstance.th;
                User user = th2.getAuthorizedPerformerUser();
                WfTask taskStub = th2.getTaskService().getMyTasks(user, th2.getTaskBatchPresentation()).get(0);
                changedVariables.put("varLong", 9L);
                changedVariables.put("varListString", Lists.newArrayList("str7", "str8"));
                changedVariables.put("varUT", null);
                changedVariables.put("varString", "12553");
                testInstance.th.getTaskService().completeTask(user, taskStub.getId(), changedVariables, null);
            }
        },

        TASK9_COMPLETED(TASK8_COMPLETED) {

            @Override
            protected void DoStageActionInternal(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance) throws Exception {
                WfServiceTestHelper th2 = testInstance.th;
                User user = th2.getAuthorizedPerformerUser();
                WfTask taskStub = th2.getTaskService().getMyTasks(user, th2.getTaskBatchPresentation()).get(0);
                changedVariables.put("varLong", 10L);
                changedVariables.put("varListString", Lists.newArrayList("str8", "str9"));
                testInstance.th.getTaskService().completeTask(user, taskStub.getId(), changedVariables, null);
            }
        },

        FINISHED(TASK9_COMPLETED) {

            @Override
            protected void DoStageActionInternal(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance) throws Exception {
                WfServiceTestHelper th2 = testInstance.th;
                User user = th2.getAuthorizedPerformerUser();
                WfTask taskStub = th2.getTaskService().getMyTasks(user, th2.getTaskBatchPresentation()).get(0);
                changedVariables.put("varLong", -1L);
                changedVariables.put("varString", null);
                changedVariables.put("varListString", Lists.newArrayList("str8", "str9", "str!"));
                HashMap<String, String> map = Maps.newHashMap();
                map.put("11", "2");
                map.put("14", "5");
                changedVariables.put("varUT", createUserType(testInstance, 133L, "teet", Lists.newArrayList("s1", "s23"), map));
                changedVariables.put("varListUT", Lists.newArrayList(createUserType(testInstance, 5L, "ss", Lists.newArrayList("s2"), map),
                        createUserType(testInstance, 6L, "ss", Lists.newArrayList("s4"), map)));
                changedVariables.put("varMapStringUT", null);
                testInstance.th.getTaskService().completeTask(user, taskStub.getId(), changedVariables, null);
            }
        };

        public final Map<String, Object> changedVariables = Maps.newHashMap();

        public final Map<String, Object> afterExecuteVariables = Maps.newHashMap();

        public Date stageTime;

        protected final Stage prevStage;

        private Stage(Stage prev) {
            this.prevStage = prev;
        }

        public void DoStageAction(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance) throws Exception {
            DoStageActionInternal(testInstance);
            Thread.sleep(testInstance.defaultDelayMs);
            stageTime = Calendar.getInstance().getTime();
            Thread.sleep(testInstance.defaultDelayMs);
            initializeAfterExecuteVariables();
        }

        private void initializeAfterExecuteVariables() {
            if (prevStage != null) {
                afterExecuteVariables.putAll(prevStage.afterExecuteVariables);
            }
            afterExecuteVariables.putAll(changedVariables);
        }

        protected UserTypeMap createUserType(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance, Long longVal, String str,
                List<String> list, Map<String, String> map) throws Exception {
            WfServiceTestHelper th2 = testInstance.th;
            User user = th2.getAuthorizedPerformerUser();
            WfProcess process = th2.getExecutionService().getProcess(user, testInstance.processId);
            UserTypeMap type = new UserTypeMap(th2.getDefinitionService().getUserType(user, process.getDefinitionId(), "UT"));
            type.put("fieldLong", longVal);
            type.put("fieldString", str);
            type.put("fieldListString", list);
            type.put("fieldMapStringString", map);
            return type;
        }

        protected abstract void DoStageActionInternal(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance) throws Exception;
    }
}
