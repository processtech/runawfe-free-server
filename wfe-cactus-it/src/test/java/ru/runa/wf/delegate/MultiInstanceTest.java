package ru.runa.wf.delegate;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.google.common.collect.Lists;

public class MultiInstanceTest extends ServletTestCase {
    private WfServiceTestHelper th;

    private Group group1 = null;
    private Relation relation1 = null;
    private Actor actor1 = null;
    private Actor actor2 = null;
    private Actor actor3 = null;
    private Actor actor4 = null;
    private ExecutionService executionService;

    @Override
    protected void setUp() throws Exception {
        th = new WfServiceTestHelper(MultiInstanceTest.class.getName());
        executionService = th.getExecutionService();
        group1 = th.createGroupIfNotExist("group1", MultiInstanceTest.class.getName());
        relation1 = th.createRelation("relation1", MultiInstanceTest.class.getName());
        actor1 = th.createActorIfNotExist("actor1", MultiInstanceTest.class.getName());
        th.addExecutorToGroup(actor1, group1);
        actor2 = th.createActorIfNotExist("actor2", MultiInstanceTest.class.getName());
        th.addExecutorToGroup(actor2, group1);
        actor3 = th.createActorIfNotExist("actor3", MultiInstanceTest.class.getName());
        th.addExecutorToGroup(actor3, group1);
        actor4 = th.createActorIfNotExist("relationparam1", MultiInstanceTest.class.getName());
        th.addRelationPair(relation1.getId(), actor4, actor1);
        th.addRelationPair(relation1.getId(), actor4, actor2);
        th.addRelationPair(relation1.getId(), actor4, actor3);
        th.deployValidProcessDefinition("multiinstance superprocess.par");
        th.deployValidProcessDefinition("multiinstance subprocess.par");
        th.deployValidProcessDefinition("MultiInstance - MainProcess.par");
        th.deployValidProcessDefinition("MultiInstance - SubProcess.par");
        th.deployValidProcessDefinition("MultiInstance - TypeMainProcess.par");
    }

    @Override
    protected void tearDown() throws Exception {
        if (relation1 != null) {
            th.removeRelation(relation1.getId());
        }
        th.undeployValidProcessDefinition("MultiInstance - MainProcess");
        th.undeployValidProcessDefinition("MultiInstance - TypeMainProcess");
        th.undeployValidProcessDefinition("MultiInstance - SubProcess");
        th.undeployValidProcessDefinition("multiinstance superprocess");
        th.undeployValidProcessDefinition("multiinstance subprocess");
        th.releaseResources();
        super.tearDown();
    }

    public void testSimple() throws Exception {
        User user = th.getAdminUser();
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("discriminator", new String[] { "d1", "d2", "d3" });
        variables.put("discriminator_r", new String[] { "d1_r", "d2_r", "d3_r" });
        variables.put("discriminator_rw", new String[] { "d1_rw", "d2_rw", "d3_rw" });
        variables.put("discriminator_w", new String[] { "d1_w", "d2_w", "d3_w" });
        long processId = executionService.startProcess(user, "multiinstance superprocess", variables);
        List<WfTask> tasks = th.getTaskService().getMyTasks(user, BatchPresentationFactory.TASKS.createDefault());
        assertEquals(3, tasks.size());
        for (WfTask task : tasks) {
            String descriminatorValue = (String) executionService.getVariable(user, task.getProcessId(), "d").getValue();
            assertEquals(descriminatorValue + "_r", (String) executionService.getVariable(user, task.getProcessId(), "d_r").getValue());
            assertEquals(descriminatorValue + "_rw", (String) executionService.getVariable(user, task.getProcessId(), "d_rw").getValue());
            th.getTaskService().completeTask(user, task.getId(), new HashMap<String, Object>());
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

    public void testSimpleWithLists() throws Exception {
        User user = th.getAdminUser();
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("discriminator", Lists.newArrayList("d1", "d2", "d3"));
        variables.put("discriminator_r", Lists.newArrayList("d1_r", "d2_r", "d3_r"));
        variables.put("discriminator_rw", Lists.newArrayList("d1_rw", "d2_rw", "d3_rw"));
        variables.put("discriminator_w", new String[] { "d1_w", "d2_w", "d3_w" });
        long processId = executionService.startProcess(user, "multiinstance superprocess", variables);
        List<WfTask> tasks = th.getTaskService().getMyTasks(user, BatchPresentationFactory.TASKS.createDefault());
        assertEquals(3, tasks.size());
        for (WfTask task : tasks) {
            String descriminatorValue = (String) executionService.getVariable(user, task.getProcessId(), "d").getValue();
            assertEquals(descriminatorValue + "_r", (String) executionService.getVariable(user, task.getProcessId(), "d_r").getValue());
            assertEquals(descriminatorValue + "_rw", (String) executionService.getVariable(user, task.getProcessId(), "d_rw").getValue());
            th.getTaskService().completeTask(user, task.getId(), new HashMap<String, Object>());
        }
        ArrayAssert.assertEqualArrays(Lists.newArrayList("d1", "d2", "d3"),
                (List<?>) executionService.getVariable(user, processId, "discriminator").getValue());
        ArrayAssert.assertEqualArrays(Lists.newArrayList("d1_r", "d2_r", "d3_r"),
                (List<?>) executionService.getVariable(user, processId, "discriminator_r").getValue());
        ArrayAssert.assertEqualArrays(Lists.newArrayList("d1", "d2", "d3"),
                (List<?>) executionService.getVariable(user, processId, "discriminator_w").getValue());
        ArrayAssert.assertEqualArrays(Lists.newArrayList("d1", "d2", "d3"),
                (List<?>) executionService.getVariable(user, processId, "discriminator_rw").getValue());
    }

    //public void testNullDiscriminator() throws Exception {
    //    User user = th.getAdminUser();
    //    Map<String, Object> variables = new HashMap<String, Object>();
    //    variables.put("discriminator", new String[] { "d1", "d2", "d3" });
    //    variables.put("discriminator_r", new String[] { "d1_r", "d2_r", "d3_r" });
    //    try {
    //        executionService.startProcess(user, "multiinstance superprocess", variables);
    //        fail("testNullDiscriminator(), no NullPointerException");
    //    } catch (NullPointerException e) {
    //    }
    //}

    public void testEmptyDiscriminator() throws Exception {
        User user = th.getAdminUser();
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("discriminator", new String[] {});
        variables.put("discriminator_r", new String[] {});
        variables.put("discriminator_w", new String[] {});
        variables.put("discriminator_rw", new String[] {});
        Long processId = executionService.startProcess(user, "multiinstance superprocess", variables);
        System.out.println("multiinstancesubprocesses=" + executionService.getSubprocesses(user, processId, true));
        assertTrue(executionService.getProcess(user, processId).isEnded());
    }

    public void testManySubprocessInToken() throws Exception {
        User user = th.getAdminUser();
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("Variable1", "Variable for subprocess 1");
        variables.put("Variable2", "Variable for subprocess 2");
        variables.put("multi", new String[] { "sub-mult 1", "sub-mult 2", "sub-mult 3" });
        variables.put("multiOut", new String[] { "sub-mult-out 1", "sub-mult-out 2", "sub-mult-out 3" });
        long processId = executionService.startProcess(user, "MultiInstance - MainProcess", variables);

        List<WfTask> tasks = th.getTaskService().getMyTasks(user, BatchPresentationFactory.TASKS.createDefault());
        assertEquals(1, tasks.size());
        assertEquals("Variable for subprocess 1", (String) executionService.getVariable(user, tasks.get(0).getProcessId(), "Variable1").getValue());
        th.getTaskService().completeTask(user, tasks.get(0).getId(), new HashMap<String, Object>());

        tasks = th.getTaskService().getMyTasks(user, BatchPresentationFactory.TASKS.createDefault());
        assertEquals(3, tasks.size());
        Collections.sort(tasks, new Comparator<WfTask>() {
            @Override
            public int compare(WfTask o1, WfTask o2) {
                return o1.getId() < o2.getId() ? -1 : o1.getId() == o2.getId() ? 0 : 1;
            }
        });
        int idx = 1;
        for (WfTask task : tasks) {
            String descriminatorValue = (String) executionService.getVariable(user, task.getProcessId(), "Variable1").getValue();
            assertEquals("sub-mult " + idx, descriminatorValue);
            th.getTaskService().completeTask(user, task.getId(), new HashMap<String, Object>());
            ++idx;
        }

        tasks = th.getTaskService().getMyTasks(user, BatchPresentationFactory.TASKS.createDefault());
        assertEquals(3, tasks.size());
        Collections.sort(tasks, new Comparator<WfTask>() {
            @Override
            public int compare(WfTask o1, WfTask o2) {
                return o1.getId() < o2.getId() ? -1 : o1.getId() == o2.getId() ? 0 : 1;
            }
        });
        idx = 1;
        for (WfTask task : tasks) {
            String descriminatorValue = (String) executionService.getVariable(user, task.getProcessId(), "Variable1").getValue();
            assertEquals("sub-mult " + idx, descriminatorValue);
            th.getTaskService().completeTask(user, task.getId(), new HashMap<String, Object>());
            ++idx;
        }

        tasks = th.getTaskService().getMyTasks(user, BatchPresentationFactory.TASKS.createDefault());
        assertEquals(1, tasks.size());
        assertEquals("Variable for subprocess 2", (String) executionService.getVariable(user, tasks.get(0).getProcessId(), "Variable1").getValue());
        th.getTaskService().completeTask(user, tasks.get(0).getId(), new HashMap<String, Object>());

        ArrayAssert.assertEqualArrays(Lists.newArrayList("sub-mult 1", "sub-mult 2", "sub-mult 3"),
                (List<?>) executionService.getVariable(user, processId, "multiOut").getValue());
    }

    public void testDifferentTypes() throws Exception {
        internalDifferentTypes(new Long(1));
        internalDifferentTypes(new Double(1));
    }

    public void testAllTypes() throws Exception {
        User user = th.getAdminUser();
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("Variable1", "group1");
        variables.put("Variable2", "relation1");
        variables.put("Variable3", "relationparam1");
        variables.put("multi", new String[] { "sub-mult 1", "sub-mult 2", "sub-mult 3" });
        executionService.startProcess(user, "MultiInstance - TypeMainProcess", variables);

        List<WfTask> tasks = th.getTaskService().getMyTasks(user, BatchPresentationFactory.TASKS.createDefault());
        assertEquals(3, tasks.size());
        Collections.sort(tasks, new Comparator<WfTask>() {
            @Override
            public int compare(WfTask o1, WfTask o2) {
                return o1.getId() < o2.getId() ? -1 : o1.getId() == o2.getId() ? 0 : 1;
            }
        });
        int idx = 1;
        for (WfTask task : tasks) {
            String discriminatorValue = (String) executionService.getVariable(user, task.getProcessId(), "Variable1").getValue();
            assertEquals("sub-mult " + idx, discriminatorValue);
            th.getTaskService().completeTask(user, task.getId(), new HashMap<String, Object>());
            ++idx;
        }

        List<String> actorList = Lists.newArrayList(actor1.getCode().toString(), actor2.getCode().toString(), actor3.getCode().toString());
        tasks = th.getTaskService().getMyTasks(user, BatchPresentationFactory.TASKS.createDefault());
        assertEquals(3, tasks.size());
        idx = 1;
        for (WfTask task : tasks) {
            String discriminatorValue = (String) executionService.getVariable(user, task.getProcessId(), "Variable1").getValue();
            assertTrue(discriminatorValue, actorList.contains(discriminatorValue));
            actorList.remove(actorList.indexOf(discriminatorValue));
            th.getTaskService().completeTask(user, task.getId(), new HashMap<String, Object>());
            ++idx;
        }
        assertTrue(actorList.isEmpty());

        actorList.add(actor1.getCode().toString());
        actorList.add(actor2.getCode().toString());
        actorList.add(actor3.getCode().toString());
        tasks = th.getTaskService().getMyTasks(user, BatchPresentationFactory.TASKS.createDefault());
        assertEquals(3, tasks.size());
        idx = 1;
        for (WfTask task : tasks) {
            String discriminatorValue = (String) executionService.getVariable(user, task.getProcessId(), "Variable1").getValue();
            assertTrue(actorList.contains(discriminatorValue));
            actorList.remove(actorList.indexOf(discriminatorValue));
            th.getTaskService().completeTask(user, task.getId(), new HashMap<String, Object>());
            ++idx;
        }
        assertTrue(actorList.isEmpty());
    }

    private void internalDifferentTypes(Object varValue) throws Exception {
        User user = th.getAdminUser();
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("discriminator", new String[] { "d1", "d2", "d3" });
        variables.put("discriminator_r", new String[] { "d1_r", "d2_r", "d3_r" });
        variables.put("discriminator_rw", new String[] { "d1_rw", "d2_rw", "d3_rw" });
        variables.put("discriminator_w", new String[] { "d1_w", "d2_w", "d3_w" });
        long processId = executionService.startProcess(user, "multiinstance superprocess", variables);
        List<WfTask> tasks = th.getTaskService().getMyTasks(user, BatchPresentationFactory.TASKS.createDefault());
        assertEquals(3, tasks.size());
        for (WfTask task : tasks) {
            String descriminatorValue = (String) executionService.getVariable(user, task.getProcessId(), "d").getValue();
            assertEquals(descriminatorValue + "_r", (String) executionService.getVariable(user, task.getProcessId(), "d_r").getValue());
            assertEquals(descriminatorValue + "_rw", (String) executionService.getVariable(user, task.getProcessId(), "d_rw").getValue());
            Map<String, Object> var = new HashMap<String, Object>();
            var.put("d_rw", varValue);
            var.put("d_w", varValue);
            th.getTaskService().completeTask(user, task.getId(), var);
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
