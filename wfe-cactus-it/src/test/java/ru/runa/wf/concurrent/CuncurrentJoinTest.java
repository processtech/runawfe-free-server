package ru.runa.wf.concurrent;

import java.util.concurrent.Semaphore;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Check if system behavior is incorrect :).
 */
public class CuncurrentJoinTest extends ServletTestCase {

    /**
     * Main process (with fork/join and subprocess start) par file.
     */
    private static String MainProcess = "Concurrent_Main.par";

    /**
     * Subprocess par file.
     */
    private static String SubProcess = "Concurrent_Sub.par";

    /**
     * Delegate to execute processes.
     */
    private ExecutionService executionService;

    /**
     * Test helper.
     */
    private WfServiceTestHelper helper = null;

    /**
     * Awaiting on this semaphore task completition.
     */
    private final Semaphore semaphore = new Semaphore(1);

    public static Test suite() {
        return new TestSuite(CuncurrentJoinTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        executionService = Delegates.getExecutionService();
        helper = new WfServiceTestHelper(getClass().getName());
        /*
         * helper.deployValidProcessDefinition(MainProcess); helper.deployValidProcessDefinition(SubProcess); Collection<Permission> permissions =
         * Lists.newArrayList(DefinitionPermission.START_PROCESS, DefinitionPermission.READ, DefinitionPermission.READ_STARTED_PROCESS);
         * helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, "Main");
         */
    }

    @Override
    protected void tearDown() throws Exception {
        /*
         * helper.undeployValidProcessDefinition("Main"); helper.undeployValidProcessDefinition("Sub");
         */
        helper.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void testConcurrentTaskExecution() throws Exception {
        /*
         * executionService.startProcess(helper.getAuthorizedPerformerUser(), "Main", null); final List<WfTask> tasks =
         * th.getTaskService().getMyTasks(helper.getAuthorizedPerformerUser(), BatchPresentationFactory.TASKS.createDefault()); assertEquals(2,
         * tasks.size()); new Thread(new Runnable() {
         * 
         * @Override public void run() { try { semaphore.acquire(); WfTask task = tasks.get(0).getName().contains("1") ? tasks.get(0) : tasks.get(1);
         * th.getTaskService().completeTask(helper.getAuthorizedPerformerUser(), task.getId(), new HashMap<String, Object>(), null); } catch (Exception
         * e) { } semaphore.release(); } }).start(); WfTask task = tasks.get(0).getName().contains("1") ? tasks.get(1) : tasks.get(0);
         * th.getTaskService().completeTask(helper.getAuthorizedPerformerUser(), task.getId(), new HashMap<String, Object>(), null); semaphore.acquire();
         * List<WfTask> newTasks = th.getTaskService().getMyTasks(helper.getAuthorizedPerformerUser(), BatchPresentationFactory.TASKS.createDefault());
         * assertEquals(1, newTasks.size()); assertTrue(newTasks.get(0).getName().contains("1")); assertEquals("Main",
         * newTasks.get(0).getDefinitionName());
         */
    }
}
