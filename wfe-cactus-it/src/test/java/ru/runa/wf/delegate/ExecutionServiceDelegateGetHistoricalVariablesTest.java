package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.Assert;
import lombok.SneakyThrows;
import org.apache.cactus.ServletTestCase;
import ru.runa.junit.ArrayAssert;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.dto.WfVariableHistoryState;

/**
 * Tests for getting historical state of process variables.
 **/
public class ExecutionServiceDelegateGetHistoricalVariablesTest extends ServletTestCase {
    private static final int defaultDelayMs = 100;

    private WfServiceTestHelper h;
    private ExecutionService executionService;
    private Long processId;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        h.deployValidProcessDefinition(WfServiceTestHelper.LONG_WITH_VARIABLES_PROCESS_FILE_NAME);
        h.setPermissionsToAuthorizedActorOnDefinitionByName(Lists.newArrayList(Permission.START_PROCESS, Permission.READ_PROCESS),
                WfServiceTestHelper.LONG_WITH_VARIABLES_PROCESS_NAME);
        for (Stage stage : Stage.values()) {
            stage.DoStageAction(this);
        }
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition(WfServiceTestHelper.LONG_WITH_VARIABLES_PROCESS_NAME);
        h.releaseResources();
        executionService = null;
    }

    public void testGetVariablesOnTime() {
        ProcessLogFilter filter = new ProcessLogFilter(processId);
        for (Stage stage : Stage.values()) {
            filter.setCreateDateTo(stage.stageTime);
            WfVariableHistoryState historicalVariables = executionService.getHistoricalVariables(h.getAuthorizedUser(), filter);
            checkVariables(stage, historicalVariables);
            checkChangedVariables(stage, historicalVariables, stage.simpleVariablesChangedFromStart);
        }
    }

    public void testGetVariablesChangedOnRange() {
        ProcessLogFilter filter = new ProcessLogFilter(processId);
        for (Stage stage : Stage.values()) {
            if (stage.prevStage == null) {
                continue;
            }
            filter.setCreateDateFrom(stage.prevStage.stageTime);
            filter.setCreateDateTo(stage.stageTime);
            WfVariableHistoryState historicalVariables = executionService.getHistoricalVariables(h.getAuthorizedUser(), filter);
            checkVariables(stage, historicalVariables);
            checkChangedVariables(stage, historicalVariables, stage.simpleVariablesChanged);
        }
    }

    public void testGetVariablesChangedOnTask() {
        ProcessLogFilter filter = new ProcessLogFilter(processId);
        for (Stage stage : Stage.values()) {
            if (stage.prevStage == null) {
                continue;
            }
            filter.setCreateDateFrom(stage.prevStage.stageTime);
            filter.setCreateDateTo(stage.stageTime);
            WfVariableHistoryState historicalVariables = executionService.getHistoricalVariables(h.getAuthorizedUser(), processId,
                    stage.taskId);
            checkVariables(stage, historicalVariables);
            checkChangedVariables(stage, historicalVariables, stage.simpleVariablesChanged);
        }
    }

    private void checkVariables(Stage stage, WfVariableHistoryState historicalVariables) {
        List<WfVariable> variables = historicalVariables.getVariables();
        Map<String, Object> expected = Maps.newHashMap();
        expected.putAll(stage.afterExecuteVariables);
        for (WfVariable wfVariable : variables) {
            String name = wfVariable.getDefinition().getName();
            if ((name.contains(".") || name.contains("[")) && !expected.containsKey(name)) {
                continue;
            }
            if (wfVariable.getValue() instanceof List) {
                ArrayAssert.assertEqualArrays((List) expected.get(name), (List) wfVariable.getValue());
            } else if (wfVariable.getValue() instanceof UserTypeMap && ((UserTypeMap) wfVariable.getValue()).isEmpty()) {
                Assert.assertTrue(name + " value is not equals on " + stage,
                        expected.get(name) == null || (expected.get(name) instanceof UserTypeMap && ((UserTypeMap) expected.get(name)).isEmpty()));
            } else {
                Assert.assertEquals(name + " value is not equals on " + stage, expected.get(name), wfVariable.getValue());
            }
            expected.remove(name);
        }
        for (String notFoundVariable : expected.keySet()) {
            Assert.assertNull(notFoundVariable + " is not null, but not found in variables on " + stage, expected.get(notFoundVariable));
        }
    }

    private void checkChangedVariables(Stage stage, WfVariableHistoryState historicalVariables, Set<String> changedVariables) {
        if (changedVariables != null && !changedVariables.isEmpty()) {
            StringBuilder builder = new StringBuilder("On stage " + stage + ":");
            builder.append("Expected: { ");
            for (String n : changedVariables) {
                builder.append(n).append(", ");
            }
            builder.append("} got : { ");
            for (String n : historicalVariables.getSimpleVariablesChanged()) {
                builder.append(n).append(", ");
            }
            builder.append("}");
            ArrayAssert.assertWeakEqualArrays(builder.toString(), changedVariables, historicalVariables.getSimpleVariablesChanged());
        }
    }

    private enum Stage {
        NOT_STARTED(null) {
            @Override
            protected Long DoStageActionInternal(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance) {
                return null;
            }
        },

        STARTED(NOT_STARTED) {
            @Override
            protected Long DoStageActionInternal(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance) {
                User user = testInstance.h.getAuthorizedUser();
                String processName = WfServiceTestHelper.LONG_WITH_VARIABLES_PROCESS_NAME;
                changedVariables.put("varLong", 1L);
                simpleVariablesChanged.add("varLong");
                testInstance.processId = testInstance.executionService.startProcess(user, processName, changedVariables);
                return null;
            }
        },

        TASK1_COMPLETED(STARTED) {

            @Override
            protected Long DoStageActionInternal(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance) {
                WfServiceTestHelper th2 = testInstance.h;
                User user = th2.getAuthorizedUser();
                WfTask taskStub = th2.getTaskService().getMyTasks(user, th2.getTaskBatchPresentation()).get(0);
                changedVariables.put("varLong", 2L);
                changedVariables.put("varListString", Lists.newArrayList("str1", "str2"));
                changedVariables.put("varString", "123");
                HashMap<UserTypeMap, UserTypeMap> map = Maps.newHashMap();
                map.put(createUserTypeK(testInstance, 199L, "str22"), createUserType(testInstance, 1L, null, Lists.newArrayList("123", "@34"), null));
                changedVariables.put("varMapStringUT", map);
                simpleVariablesChanged.add("varLong");
                simpleVariablesChanged.add("varListString[0]");
                simpleVariablesChanged.add("varListString[1]");
                simpleVariablesChanged.add("varListString.size");
                simpleVariablesChanged.add("varString");
                simpleVariablesChanged.add("varMapStringUT[0:k].fieldStringK");
                simpleVariablesChanged.add("varMapStringUT[0:k].fieldLongK");
                simpleVariablesChanged.add("varMapStringUT[0:v].fieldListString[0]");
                simpleVariablesChanged.add("varMapStringUT[0:v].fieldListString[1]");
                simpleVariablesChanged.add("varMapStringUT[0:v].fieldListString.size");
                simpleVariablesChanged.add("varMapStringUT[0:v].fieldLong");
                simpleVariablesChanged.add("varMapStringUT.size");
                testInstance.h.getTaskService().completeTask(user, taskStub.getId(), changedVariables);
                return taskStub.getId();
            }
        },

        TASK2_COMPLETED(TASK1_COMPLETED) {

            @Override
            protected Long DoStageActionInternal(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance) {
                WfServiceTestHelper th2 = testInstance.h;
                User user = th2.getAuthorizedUser();
                WfTask taskStub = th2.getTaskService().getMyTasks(user, th2.getTaskBatchPresentation()).get(0);
                changedVariables.put("varLong", 3L);
                changedVariables.put("varListString", Lists.newArrayList("str1", "str2", "str3"));
                changedVariables.put("varString", "1243");
                changedVariables.put("varListUT",
                        Lists.newArrayList(createUserType(testInstance, 3L, "ss", null, null), createUserType(testInstance, 6L, "ss", null, null)));
                simpleVariablesChanged.add("varLong");
                simpleVariablesChanged.add("varListString[2]");
                simpleVariablesChanged.add("varListString.size");
                simpleVariablesChanged.add("varString");
                simpleVariablesChanged.add("varListUT[0].fieldLong");
                simpleVariablesChanged.add("varListUT[0].fieldString");
                simpleVariablesChanged.add("varListUT[1].fieldLong");
                simpleVariablesChanged.add("varListUT[1].fieldString");
                simpleVariablesChanged.add("varListUT.size");
                testInstance.h.getTaskService().completeTask(user, taskStub.getId(), changedVariables);
                return taskStub.getId();
            }
        },

        TASK3_COMPLETED(TASK2_COMPLETED) {

            @Override
            protected Long DoStageActionInternal(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance) {
                WfServiceTestHelper th2 = testInstance.h;
                User user = th2.getAuthorizedUser();
                WfTask taskStub = th2.getTaskService().getMyTasks(user, th2.getTaskBatchPresentation()).get(0);
                changedVariables.put("varLong", 4L);
                changedVariables.put("varListString", null);
                changedVariables.put("varUT", createUserType(testInstance, 3L, "ss", null, null));
                simpleVariablesChanged.add("varLong");
                simpleVariablesChanged.add("varListString[0]");
                simpleVariablesChanged.add("varListString[1]");
                simpleVariablesChanged.add("varListString[2]");
                simpleVariablesChanged.add("varListString.size");
                simpleVariablesChanged.add("varUT.fieldLong");
                simpleVariablesChanged.add("varUT.fieldString");
                testInstance.h.getTaskService().completeTask(user, taskStub.getId(), changedVariables);
                return taskStub.getId();
            }
        },

        TASK4_COMPLETED(TASK3_COMPLETED) {

            @Override
            protected Long DoStageActionInternal(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance) {
                WfServiceTestHelper th2 = testInstance.h;
                User user = th2.getAuthorizedUser();
                WfTask taskStub = th2.getTaskService().getMyTasks(user, th2.getTaskBatchPresentation()).get(0);
                changedVariables.put("varLong", 5L);
                changedVariables.put("varListString", Lists.newArrayList("str4", "str5"));
                changedVariables.put("varUT", createUserType(testInstance, null, "ss", Lists.newArrayList("s1", "s2"), null));
                simpleVariablesChanged.add("varLong");
                simpleVariablesChanged.add("varListString.size");
                simpleVariablesChanged.add("varListString[0]");
                simpleVariablesChanged.add("varListString[1]");
                simpleVariablesChanged.add("varUT.fieldLong");
                simpleVariablesChanged.add("varUT.fieldListString.size");
                simpleVariablesChanged.add("varUT.fieldListString[0]");
                simpleVariablesChanged.add("varUT.fieldListString[1]");
                testInstance.h.getTaskService().completeTask(user, taskStub.getId(), changedVariables);
                return taskStub.getId();
            }
        },

        TASK5_COMPLETED(TASK4_COMPLETED) {

            @Override
            protected Long DoStageActionInternal(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance) {
                WfServiceTestHelper th2 = testInstance.h;
                User user = th2.getAuthorizedUser();
                WfTask taskStub = th2.getTaskService().getMyTasks(user, th2.getTaskBatchPresentation()).get(0);
                changedVariables.put("varLong", 6L);
                changedVariables.put("varString", null);
                HashMap<String, String> map = Maps.newHashMap();
                map.put("1", "2");
                changedVariables.put("varUT", createUserType(testInstance, null, null, Lists.newArrayList("s1"), map));
                changedVariables.put("varListUT", Lists.newArrayList(createUserType(testInstance, 5L, "ss", Lists.newArrayList("s2"), null),
                        createUserType(testInstance, 6L, "ss", Lists.newArrayList("s4"), null)));
                changedVariables.put("varMapStringUT", null);
                simpleVariablesChanged.add("varLong");
                simpleVariablesChanged.add("varString");

                simpleVariablesChanged.add("varListUT[0].fieldLong");
                simpleVariablesChanged.add("varListUT[0].fieldListString.size");
                simpleVariablesChanged.add("varListUT[0].fieldListString[0]");
                simpleVariablesChanged.add("varListUT[1].fieldListString[0]");
                simpleVariablesChanged.add("varListUT[1].fieldListString.size");
                simpleVariablesChanged.add("varMapStringUT.size");
                simpleVariablesChanged.add("varMapStringUT[0:k].fieldStringK");
                simpleVariablesChanged.add("varMapStringUT[0:k].fieldLongK");
                simpleVariablesChanged.add("varMapStringUT[0:v].fieldLong");
                simpleVariablesChanged.add("varMapStringUT[0:v].fieldListString.size");
                simpleVariablesChanged.add("varMapStringUT[0:v].fieldListString[0]");
                simpleVariablesChanged.add("varMapStringUT[0:v].fieldListString[1]");
                simpleVariablesChanged.add("varUT.fieldString");
                simpleVariablesChanged.add("varUT.fieldListString.size");
                simpleVariablesChanged.add("varUT.fieldListString[1]");
                simpleVariablesChanged.add("varUT.fieldMapStringString.size");
                simpleVariablesChanged.add("varUT.fieldMapStringString[0:k]");
                simpleVariablesChanged.add("varUT.fieldMapStringString[0:v]");
                testInstance.h.getTaskService().completeTask(user, taskStub.getId(), changedVariables);
                return taskStub.getId();
            }
        },

        TASK6_COMPLETED(TASK5_COMPLETED) {

            @Override
            protected Long DoStageActionInternal(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance) {
                WfServiceTestHelper th2 = testInstance.h;
                User user = th2.getAuthorizedUser();
                WfTask taskStub = th2.getTaskService().getMyTasks(user, th2.getTaskBatchPresentation()).get(0);
                changedVariables.put("varLong", 7L);
                changedVariables.put("varListString", null);
                HashMap<String, String> map = Maps.newHashMap();
                map.put("1", "2");
                map.put("4", "5");
                changedVariables.put("varUT", createUserType(testInstance, 13L, "tt", null, map));
                changedVariables.put("varListUT", Lists.newArrayList(createUserType(testInstance, 5L, "ss", Lists.newArrayList("s2"), map),
                        createUserType(testInstance, 6L, "ss", Lists.newArrayList("s4"), map)));
                testInstance.h.getTaskService().completeTask(user, taskStub.getId(), changedVariables);
                return taskStub.getId();
            }
        },

        TASK7_COMPLETED(TASK6_COMPLETED) {

            @Override
            protected Long DoStageActionInternal(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance) {
                WfServiceTestHelper th2 = testInstance.h;
                User user = th2.getAuthorizedUser();
                WfTask taskStub = th2.getTaskService().getMyTasks(user, th2.getTaskBatchPresentation()).get(0);
                changedVariables.put("varLong", 8L);
                changedVariables.put("varListUT", null);
                HashMap<UserTypeMap, UserTypeMap> map = Maps.newHashMap();
                map.put(createUserTypeK(testInstance, 198L, "str44"),
                        createUserType(testInstance, 1L, null, Lists.newArrayList("123", "4@34"), null));
                changedVariables.put("varMapStringUT", map);
                testInstance.h.getTaskService().completeTask(user, taskStub.getId(), changedVariables);
                return taskStub.getId();
            }
        },

        TASK8_COMPLETED(TASK7_COMPLETED) {

            @Override
            protected Long DoStageActionInternal(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance) {
                WfServiceTestHelper th2 = testInstance.h;
                User user = th2.getAuthorizedUser();
                WfTask taskStub = th2.getTaskService().getMyTasks(user, th2.getTaskBatchPresentation()).get(0);
                changedVariables.put("varLong", 9L);
                changedVariables.put("varListString", Lists.newArrayList("str7", "str8"));
                changedVariables.put("varUT", null);
                changedVariables.put("varString", "12553");
                testInstance.h.getTaskService().completeTask(user, taskStub.getId(), changedVariables);
                return taskStub.getId();
            }
        },

        TASK9_COMPLETED(TASK8_COMPLETED) {

            @Override
            protected Long DoStageActionInternal(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance) {
                WfServiceTestHelper th2 = testInstance.h;
                User user = th2.getAuthorizedUser();
                WfTask taskStub = th2.getTaskService().getMyTasks(user, th2.getTaskBatchPresentation()).get(0);
                changedVariables.put("varLong", 10L);
                changedVariables.put("varListString", Lists.newArrayList("str8", "str9"));
                testInstance.h.getTaskService().completeTask(user, taskStub.getId(), changedVariables);
                return taskStub.getId();
            }
        },

        FINISHED(TASK9_COMPLETED) {

            @Override
            protected Long DoStageActionInternal(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance) {
                WfServiceTestHelper th2 = testInstance.h;
                User user = th2.getAuthorizedUser();
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
                testInstance.h.getTaskService().completeTask(user, taskStub.getId(), changedVariables);
                return taskStub.getId();
            }
        };

        public final Map<String, Object> changedVariables = Maps.newHashMap();

        public final Map<String, Object> afterExecuteVariables = Maps.newHashMap();

        public final Set<String> simpleVariablesChanged = Sets.newHashSet();

        public final Set<String> simpleVariablesChangedFromStart = Sets.newHashSet();

        public Date stageTime;

        public Long taskId;

        protected final Stage prevStage;

        Stage(Stage prev) {
            this.prevStage = prev;
        }

        @SneakyThrows
        public void DoStageAction(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance) {
            taskId = DoStageActionInternal(testInstance);
            Thread.sleep(defaultDelayMs);
            stageTime = Calendar.getInstance().getTime();
            Thread.sleep(defaultDelayMs);
            initializeAfterExecuteVariables();
        }

        private void initializeAfterExecuteVariables() {
            if (prevStage != null) {
                afterExecuteVariables.putAll(prevStage.afterExecuteVariables);
            }
            afterExecuteVariables.putAll(changedVariables);
            if (!simpleVariablesChanged.isEmpty()) {
                simpleVariablesChangedFromStart.addAll(simpleVariablesChanged);
                if (prevStage != null) {
                    simpleVariablesChangedFromStart.addAll(prevStage.simpleVariablesChangedFromStart);
                }
            }
        }

        protected UserTypeMap createUserType(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance, Long longVal, String str,
                List<String> list, Map<String, String> map) {
            WfServiceTestHelper th2 = testInstance.h;
            User user = th2.getAuthorizedUser();
            WfProcess process = th2.getExecutionService().getProcess(user, testInstance.processId);
            UserTypeMap type = new UserTypeMap(th2.getDefinitionService().getUserType(user, process.getDefinitionId(), "UT"));
            type.put("fieldLong", longVal);
            type.put("fieldString", str);
            type.put("fieldListString", list);
            type.put("fieldMapStringString", map);
            return type;
        }

        protected UserTypeMap createUserTypeK(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance, Long longVal, String str) {
            WfServiceTestHelper th2 = testInstance.h;
            User user = th2.getAuthorizedUser();
            WfProcess process = th2.getExecutionService().getProcess(user, testInstance.processId);
            UserTypeMap type = new UserTypeMap(th2.getDefinitionService().getUserType(user, process.getDefinitionId(), "UK"));
            type.put("fieldStringK", str);
            type.put("fieldLongK", longVal);
            return type;
        }

        protected abstract Long DoStageActionInternal(ExecutionServiceDelegateGetHistoricalVariablesTest testInstance);
    }
}
