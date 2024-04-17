package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import lombok.val;
import org.apache.cactus.ServletTestCase;
import ru.runa.junit.ArrayAssert;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

public class MultiInstanceTest extends ServletTestCase {
    private WfServiceTestHelper h;
    private ExecutionService executionService;

    private Group group1 = null;
    private Relation relation1 = null;
    private Actor actor1 = null;
    private Actor actor2 = null;
    private Actor actor3 = null;
    private Actor actor4 = null;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        executionService = h.getExecutionService();

        group1 = h.createGroupIfNotExist("group1", MultiInstanceTest.class.getName());
        relation1 = h.createRelation("relation1", MultiInstanceTest.class.getName());
        actor1 = h.createActorIfNotExist("actor1", MultiInstanceTest.class.getName());
        h.addExecutorToGroup(actor1, group1);
        actor2 = h.createActorIfNotExist("actor2", MultiInstanceTest.class.getName());
        h.addExecutorToGroup(actor2, group1);
        actor3 = h.createActorIfNotExist("actor3", MultiInstanceTest.class.getName());
        h.addExecutorToGroup(actor3, group1);
        actor4 = h.createActorIfNotExist("relationparam1", MultiInstanceTest.class.getName());
        h.addRelationPair(relation1.getId(), actor4, actor1);
        h.addRelationPair(relation1.getId(), actor4, actor2);
        h.addRelationPair(relation1.getId(), actor4, actor3);
        h.deployValidProcessDefinition("multiinstance superprocess.par");
        h.deployValidProcessDefinition("multiinstance subprocess.par");
        h.deployValidProcessDefinition("MultiInstance - MainProcess.par");
        h.deployValidProcessDefinition("MultiInstance - SubProcess.par");
        h.deployValidProcessDefinition("MultiInstance - TypeMainProcess.par");
    }

    public void testSimple() {
        User user = h.getAdminUser();
        val vars = new HashMap<String, Object>();
        vars.put("discriminator", Lists.newArrayList("d1", "d2", "d3"));
        vars.put("discriminator_r", Lists.newArrayList("d1_r", "d2_r", "d3_r"));
        vars.put("discriminator_rw", Lists.newArrayList("d1_rw", "d2_rw", "d3_rw"));
        long processId = executionService.startProcess(user, "multiinstance superprocess", vars);
        List<WfTask> tasks = h.getTaskService().getMyTasks(user, BatchPresentationFactory.TASKS.createDefault());
        assertEquals(3, tasks.size());
        for (WfTask task : tasks) {
            String descriminatorValue = (String) executionService.getVariable(user, task.getProcessId(), "d").getValue();
            assertEquals(descriminatorValue + "_r", (String) executionService.getVariable(user, task.getProcessId(), "d_r").getValue());
            assertEquals(descriminatorValue + "_rw", (String) executionService.getVariable(user, task.getProcessId(), "d_rw").getValue());
            h.getTaskService().completeTask(user, task.getId(), null);
        }
        ArrayAssert.assertEqualArrays("discriminator", Lists.newArrayList("d1", "d2", "d3"),
                (List<?>) executionService.getVariable(user, processId, "discriminator").getValue());
        ArrayAssert.assertEqualArrays("discriminator_r", Lists.newArrayList("d1_r", "d2_r", "d3_r"),
                (List<?>) executionService.getVariable(user, processId, "discriminator_r").getValue());
        ArrayAssert.assertEqualArrays("discriminator_w", Lists.newArrayList("d1", "d2", "d3"),
                (List<?>) executionService.getVariable(user, processId, "discriminator_w").getValue());
        ArrayAssert.assertEqualArrays("discriminator_rw", Lists.newArrayList("d1", "d2", "d3"),
                (List<?>) executionService.getVariable(user, processId, "discriminator_rw").getValue());
    }

    @Override
    protected void tearDown() {
        if (relation1 != null) {
            h.removeRelation(relation1.getId());
        }
        h.undeployValidProcessDefinition("MultiInstance - MainProcess");
        h.undeployValidProcessDefinition("MultiInstance - TypeMainProcess");
        h.undeployValidProcessDefinition("MultiInstance - SubProcess");
        h.undeployValidProcessDefinition("multiinstance superprocess");
        h.undeployValidProcessDefinition("multiinstance subprocess");
        h.releaseResources();
    }

    public void testNullDiscriminator() {
        User user = h.getAdminUser();
        val vars = new HashMap<String, Object>();
        vars.put("discriminator", Lists.newArrayList("d1", "d2", "d3"));
        vars.put("discriminator_r", Lists.newArrayList("d1_r", "d2_r", "d3_r"));
        long processId = executionService.startProcess(user, "multiinstance superprocess", vars);
        List<WfTask> tasks = h.getTaskService().getMyTasks(user, BatchPresentationFactory.TASKS.createDefault());
        assertEquals(3, tasks.size());
        for (WfTask task : tasks) {
            String descriminatorValue = (String) executionService.getVariable(user, task.getProcessId(), "d").getValue();
            assertEquals(descriminatorValue + "_r", (String) executionService.getVariable(user, task.getProcessId(), "d_r").getValue());
            assertNull(executionService.getVariable(user, task.getProcessId(), "d_rw").getValue());
            h.getTaskService().completeTask(user, task.getId(), null);
        }
        ArrayAssert.assertEqualArrays("", Lists.newArrayList("d1", "d2", "d3"),
                (List<?>) executionService.getVariable(user, processId, "discriminator").getValue());
        ArrayAssert.assertEqualArrays("", Lists.newArrayList("d1_r", "d2_r", "d3_r"),
                (List<?>) executionService.getVariable(user, processId, "discriminator_r").getValue());
        ArrayAssert.assertEqualArrays(Lists.newArrayList("d1", "d2", "d3"),
                (List<?>) executionService.getVariable(user, processId, "discriminator_w").getValue());
        ArrayAssert.assertEqualArrays(Lists.newArrayList("d1", "d2", "d3"),
                (List<?>) executionService.getVariable(user, processId, "discriminator_rw").getValue());
    }

    public void testEmptyDiscriminator() {
        User user = h.getAdminUser();
        val vars = new HashMap<String, Object>();
        vars.put("discriminator", Lists.newArrayList());
        vars.put("discriminator_r", Lists.newArrayList());
        Long processId = executionService.startProcess(user, "multiinstance superprocess", vars);
        System.out.println("multiinstancesubprocesses=" + executionService.getSubprocesses(user, processId, true));
        assertTrue(executionService.getProcess(user, processId).isEnded());
    }

    public void testManySubprocessInToken() {
        User user = h.getAdminUser();
        val vars = new HashMap<String, Object>();
        vars.put("Variable1", "Variable for subprocess 1");
        vars.put("Variable2", "Variable for subprocess 2");
        vars.put("multi", Lists.newArrayList("sub-mult 1", "sub-mult 2", "sub-mult 3"));
        long processId = executionService.startProcess(user, "MultiInstance - MainProcess", vars);

        List<WfTask> tasks = h.getTaskService().getMyTasks(user, BatchPresentationFactory.TASKS.createDefault());
        assertEquals(1, tasks.size());
        assertEquals("Variable for subprocess 1", (String) executionService.getVariable(user, tasks.get(0).getProcessId(), "Variable1").getValue());
        h.getTaskService().completeTask(user, tasks.get(0).getId(), null);

        tasks = h.getTaskService().getMyTasks(user, BatchPresentationFactory.TASKS.createDefault());
        assertEquals(3, tasks.size());
        Collections.sort(tasks, new Comparator<WfTask>() {
            @Override
            public int compare(WfTask o1, WfTask o2) {
                return o1.getId() < o2.getId() ? -1 : Objects.equals(o1.getId(), o2.getId()) ? 0 : 1;
            }
        });
        int idx = 1;
        for (WfTask task : tasks) {
            String descriminatorValue = (String) executionService.getVariable(user, task.getProcessId(), "Variable1").getValue();
            assertEquals("sub-mult " + idx, descriminatorValue);
            h.getTaskService().completeTask(user, task.getId(), null);
            ++idx;
        }

        tasks = h.getTaskService().getMyTasks(user, BatchPresentationFactory.TASKS.createDefault());
        assertEquals(3, tasks.size());
        Collections.sort(tasks, new Comparator<WfTask>() {
            @Override
            public int compare(WfTask o1, WfTask o2) {
                return o1.getId() < o2.getId() ? -1 : Objects.equals(o1.getId(), o2.getId()) ? 0 : 1;
            }
        });
        idx = 1;
        for (WfTask task : tasks) {
            String descriminatorValue = (String) executionService.getVariable(user, task.getProcessId(), "Variable1").getValue();
            assertEquals("sub-mult " + idx, descriminatorValue);
            h.getTaskService().completeTask(user, task.getId(), null);
            ++idx;
        }

        tasks = h.getTaskService().getMyTasks(user, BatchPresentationFactory.TASKS.createDefault());
        assertEquals(1, tasks.size());
        assertEquals("Variable for subprocess 2", (String) executionService.getVariable(user, tasks.get(0).getProcessId(), "Variable1").getValue());
        h.getTaskService().completeTask(user, tasks.get(0).getId(), null);

        ArrayAssert.assertEqualArrays(Lists.newArrayList("sub-mult 1", "sub-mult 2", "sub-mult 3"),
                (List<?>) executionService.getVariable(user, processId, "multiOut").getValue());
    }

    public void testDifferentTypes() {
        internalDifferentTypes(1L);
        internalDifferentTypes(1d);
    }

    public void testAllTypes() {
        User user = h.getAdminUser();
        val vars = new HashMap<String, Object>();
        vars.put("Variable1", "group1");
        vars.put("Variable2", "relation1");
        vars.put("Variable3", "relationparam1");
        vars.put("multi", Lists.newArrayList("sub-mult 1", "sub-mult 2", "sub-mult 3"));
        executionService.startProcess(user, "MultiInstance - TypeMainProcess", vars);

        List<WfTask> tasks = h.getTaskService().getMyTasks(user, BatchPresentationFactory.TASKS.createDefault());
        assertEquals(3, tasks.size());
        Collections.sort(tasks, new Comparator<WfTask>() {
            @Override
            public int compare(WfTask o1, WfTask o2) {
                return o1.getId() < o2.getId() ? -1 : Objects.equals(o1.getId(), o2.getId()) ? 0 : 1;
            }
        });
        int idx = 1;
        for (WfTask task : tasks) {
            String discriminatorValue = (String) executionService.getVariable(user, task.getProcessId(), "Variable1").getValue();
            assertEquals("sub-mult " + idx, discriminatorValue);
            h.getTaskService().completeTask(user, task.getId(), null);
            ++idx;
        }

        List<String> actorList = Lists.newArrayList(actor1.getCode().toString(), actor2.getCode().toString(), actor3.getCode().toString());
        tasks = h.getTaskService().getMyTasks(user, BatchPresentationFactory.TASKS.createDefault());
        assertEquals(3, tasks.size());
        idx = 1;
        for (WfTask task : tasks) {
            String discriminatorValue = (String) executionService.getVariable(user, task.getProcessId(), "Variable1").getValue();
            assertTrue(discriminatorValue, actorList.contains(discriminatorValue));
            actorList.remove(discriminatorValue);
            h.getTaskService().completeTask(user, task.getId(), null);
            ++idx;
        }
        assertTrue(actorList.isEmpty());

        actorList.add(actor1.getCode().toString());
        actorList.add(actor2.getCode().toString());
        actorList.add(actor3.getCode().toString());
        tasks = h.getTaskService().getMyTasks(user, BatchPresentationFactory.TASKS.createDefault());
        assertEquals(3, tasks.size());
        idx = 1;
        for (WfTask task : tasks) {
            String discriminatorValue = (String) executionService.getVariable(user, task.getProcessId(), "Variable1").getValue();
            assertTrue(actorList.contains(discriminatorValue));
            actorList.remove(discriminatorValue);
            h.getTaskService().completeTask(user, task.getId(), null);
            ++idx;
        }
        assertTrue(actorList.isEmpty());
    }

    private void internalDifferentTypes(Object varValue) {
        User user = h.getAdminUser();
        val vars = new HashMap<String, Object>();
        vars.put("discriminator", Lists.newArrayList("d1", "d2", "d3"));
        vars.put("discriminator_r", Lists.newArrayList("d1_r", "d2_r", "d3_r"));
        vars.put("discriminator_rw", Lists.newArrayList("d1_rw", "d2_rw", "d3_rw"));
        long processId = executionService.startProcess(user, "multiinstance superprocess", vars);
        List<WfTask> tasks = h.getTaskService().getMyTasks(user, BatchPresentationFactory.TASKS.createDefault());
        assertEquals(3, tasks.size());
        for (WfTask task : tasks) {
            String descriminatorValue = (String) executionService.getVariable(user, task.getProcessId(), "d").getValue();
            assertEquals(descriminatorValue + "_r", (String) executionService.getVariable(user, task.getProcessId(), "d_r").getValue());
            assertEquals(descriminatorValue + "_rw", (String) executionService.getVariable(user, task.getProcessId(), "d_rw").getValue());
            val vars2 = new HashMap<String, Object>();
            vars2.put("d_rw", varValue);
            vars2.put("d_w", varValue);
            h.getTaskService().completeTask(user, task.getId(), vars2);
        }
        ArrayAssert.assertEqualArrays("", Lists.newArrayList("d1", "d2", "d3"),
                (List<?>) executionService.getVariable(user, processId, "discriminator").getValue());
        ArrayAssert.assertEqualArrays("", Lists.newArrayList("d1_r", "d2_r", "d3_r"),
                (List<?>) executionService.getVariable(user, processId, "discriminator_r").getValue());
        ArrayAssert.assertEqualArrays(Lists.newArrayList("d1", "d2", "d3"),
                (List<?>) executionService.getVariable(user, processId, "discriminator_w").getValue());
        ArrayAssert.assertEqualArrays(Lists.newArrayList("d1", "d2", "d3"),
                (List<?>) executionService.getVariable(user, processId, "discriminator_rw").getValue());
    }
}
